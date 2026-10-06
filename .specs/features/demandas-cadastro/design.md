# Cadastro e leitura de demandas Design

**Spec:** spec.md. **Base:** 28b8c88. **Status:** contrato fechado para execução autorizada na sessão.

## Architecture Overview

Ampliar ConfiguracaoEtapasResponse com demandas. O snapshot sob o lock existente atende GET, CRUD de etapas e cadastro, com contagens calculadas da mesma lista. Reutilizar Quadro/EditorEtapa para pendência/foco/cache e o interceptor/guard/transações existentes. Alternativas avaliadas: buscar demandas em GET separado exigiria reconciliar duas versões; novo serviço de snapshot duplicaria a composição existente. A ampliação preserva rotas/campos anteriores e evita essa coordenação adicional.

Fluxo: Quadro → EditorDemanda → criarDemanda → DemandaController → ProcessoService.criar → lock global de cadastro → lock ativo/versão/primeiro trabalho → demanda+histórico → incremento → EtapaService.resposta. Nenhuma preparação implícita de finais ou migração.

## Code Reuse Analysis

| Componente | Uso |
| --- | --- |
| GerenciamentoGuard/SessaoService | Lock global de cadastro antes do alvo, ativo,404, sessão/autoria, sem exigência de dono para criar |
| EtapaRepository.findByGerenciamentoIdOrderByOrdemAscIdAsc + getCategoria TRABALHO | Compatibilidade SQLnull e primeira etapa por ordem/ID; sem helper novo |
| EtapaService.resposta | Tornar package-private; snapshot comum, consulta única das demandas |
| HistoricoService.registrar | Um CRIACAO transacional com autor confiável |
| VersaoDeserializer/ApiExceptionHandler | Versão estrita, erros JSON400 e domínio |
| api.js.requisicao/ApiError | Bearer,AbortSignal, JSON/HTTP, sem retry |
| EditorEtapa/Quadro/App | Foco, rascunho, bloqueio de navegação e cache atualizado |

## Components and Interfaces

- DemandaResponse.java: DTO flat, fábrica do Processo persistido.
- ConfiguracaoEtapasResponse.java: acrescenta List<DemandaResponse> demandas; consumidores antigos de gerenciamento/etapas continuam válidos.
- EtapaService.resposta: lê findByEtapaGerenciamentoIdOrderByIdAsc, calcula counts por etapaId e DTOs da mesma lista. Mantém categorias/ordenação/podeAdministrar.
- DemandaController.java: POST /api/gerenciamentos/{gerenciamentoId}/demandas,201,snapshot. CriarDemandaRequest record com campos de cadastro e versao usando deserializer existente. JsonAnySetter rejeita propriedades desconhecidas com o formato400 existente. DataDemandaDeserializer local exige string ISO válida ou null para cada uma das três datas; array/número/booleano/texto vazio são400. Sem mudar ObjectMapper global. APIs confirmadas nos JARs Jackson3.1.5/Hibernate7.4.5.
- ProcessoService.criar: exige versão no fluxo novo, valida/salva entidade nova contendo apenas campos de cadastro, registra histórico, flush/incrementa1 e monta snapshot. Uso do EntityManager e EtapaService sem ciclo de dependência.
- ProcessoService.salvar legado: mantém assinatura/HTTP200/entidade e sem versão estrutural; resolve quadro pela etapa persistida, exige que ela seja primeiraTRABALHO. ID continua rejeitado; status só ausente/Em andamento, final dates/motivo rejeitados. Reutiliza validação/defaults/salvar+histórico, sem alterar métodos de edição/movimento/finais/exclusão.
- EditorDemanda.jsx: componente de formulário com props salvar/cancelar/bloqueado/aoErro; oito campos visíveis e dados normalizados.
- Quadro.jsx: estado de formulário de demanda integrado à exclusividade editor/confirmacao, autorização de criação separada de administração e artigos de leitura por etapaId.

## Data Models

Request novo, exatamente nove campos: {versao,numeroProcesso,pessoa,responsavel,prioridade,dataEmissao,prazoEtapa,prazoGeral,observacoes}. Sem ID,etapa,status,encerramento ou dados de usuário.

DemandaResponse, exatamente14 campos: {id,numeroProcesso,pessoa,responsavel,status,prioridade,dataEmissao,prazoEtapa,prazoGeral,dataConclusao,dataCancelamento,motivoCancelamento,observacoes,etapaId}. IDs positivos; datas LocalDate→ISO ou null, opcionais string/null, número/pessoa string. Legados não têm seus valores normalizados durante leitura. Sem gerenciamento/etapa/usuário aninhados.

Snapshot: {gerenciamento:GerenciamentoResponse,etapas:EtapaResponse[],demandas:DemandaResponse[]}. Inclui zero demandas como[]. IDs das demandas únicos; cada etapaId pertence a etapas; contagem de cada etapa igual à lista desse snapshot. Ler campos antigos extensos sem aplicar retroativamente os limites de criação.

Unicidade: ambos cadastros adquirem PESSIMISTIC_WRITE na primeira linha de gerenciamento persistida antes de bloquear o quadro alvo. Essa linha permanece estável porque gerenciamentos são arquivados/restaurados, não excluídos. Só bloquear, sem exigir ativo/admin nem alterar dados/versão do quadro global quando ele não é o alvo. Depois executar existsByNumeroProcessoIgnoreCase sob esse lock até o commit. Assim grafias com caixa diferente também disputam entre quadros mesmo com índice VARCHAR case-sensitive no H2. A ordem fixa global→alvo não cria ciclos nos endpoints atuais, cujas demais mutações bloqueiam somente o alvo; preparação percorre IDs em ordem. Tradeoff aceito: cadastros de quadros diferentes são serializados. Nenhum schema, collation, geração ou alteração da grafia é introduzido. O índice único existente permanece garantia física adicional. SQL23505(H2)/1062(MySQL) ou ConstraintViolationException.getKind()==UNIQUE confirmado no Hibernate7.4.5 distinguem duplicidade somente durante o INSERT de processos; falhas de CHECK/FK/histórico continuam500/rollback, não são mascaradas como409.

Diagnóstico inicial T2-diagnostic.log: a corrida Igual/igual falhou sob H2 VARCHAR, enquanto Igual/Igual e os demais102casos passaram. A implementação do lock global corrige o comportamento mantendo CAD-13/17; testes não foram enfraquecidos e a URL H2 permanece inalterada.

## Error Handling Strategy

| Situação | Código/mensagem |
| --- | --- |
| Número inválido |400 Informe um número de demanda entre 1 e 255 caracteres. |
| Cliente inválido |400 Informe um cliente/solicitante entre 1 e 255 caracteres. |
| Responsável/prioridade longo |400 Informe um responsável com até 255 caracteres. / Informe uma prioridade com até 255 caracteres. |
| Observações longas |400 Informe observações com até 10000 caracteres. |
| Duplicidade |409 Já existe uma demanda com esse número. |
| Zero trabalho |409 Cadastre uma etapa de trabalho antes de criar demandas. |
| Nova versão ausente/negativa |400 Informe uma versão válida do gerenciamento. |
| Versão obsoleta |409 Gerenciamento alterado por outro usuário. Atualize e tente novamente. |
| Novo campo desconhecido/JSON/data/versão malformada |400 Dados da requisição inválidos. |
| Legado etapa posterior/final |400 Novas demandas devem começar na primeira etapa de trabalho. |
| Legado estado/final fields |400 O estado inicial da demanda é definido pelo sistema. |
| Arquivo/sessão/quadro/ID legado |Mensagens/códigos atuais do guard/interceptor/ProcessoService |
| Persistência não duplicada |500 Não foi possível concluir a operação. |
| Snapshot de sucesso inseguro no cliente |ApiError com ERRO_COMUNICACAO atual, sem retry, sem fechar formulário/confirmar sucesso |

## Interface

Título Novo processo; labels Número da demanda, Cliente/solicitante, Responsável, Prioridade, Data de emissão, Prazo da etapa, Prazo geral, Observações. Texto: A demanda começará automaticamente na primeira etapa de trabalho. Número e cliente/solicitante são obrigatórios; os demais campos são opcionais. Prioridade é input de texto; datas são input date, observações textarea. Cancelar/Criar demanda; pendente Criando....

Sem trabalho: criador/admin vê Cadastre uma etapa de trabalho usando Nova etapa antes de criar demandas. Outro vê Solicite ao criador do quadro ou a um administrador que cadastre uma etapa de trabalho. Criar processo desabilitado; arquivo oculta ação. Carregamento/erro/necessidade de refresh impedem abrir/enviar.

Cartão article com strong para número/cliente, preservando o único heading de cada seção de etapa. Campos em lista descritiva, status/responsável/prioridade/datas/observações e motivo se existente; ausente usa Não informado. Datas exibidas DD/MM/YYYY sem conversão de fuso. Textos renderizados por JSX, preservando quebra de linha das observações. Sem botões novos nos cartões. Manter resumo de contagem anterior, inclusive X demandas vinculadas a esta etapa., junto de cartões.

Validação frontend do snapshot aplicada a GET/configuração/criação: gerenciamento.id igual ao solicitado, versão não negativa, arquivado/podeAdministrar booleanos, arrays existentes; etapas/IDs únicos/contagens inteiras; demandas com14campos/types definidos/IDs únicos/etapaId pertencente; contagens da mesma lista. Não rejeitar status desconhecido, texto antigo longo ou null nos campos opcionais. Falha não produz fakecard nem mudança de cache. abort/troca de quadro preserva isolamento.

## Risks & Concerns

| Local | Risco | Mitigação |
| --- | --- | --- |
| ProcessoController.java:32 | Via alternativa cria com estado/etapa escolhidos | CAD-18–20 mesma validação/primeirotrabalho, preservar200 |
| ProcessoService.java:49–76 | Corrida/global unique e histórico parcial | Lock global→alvo até commit, precheckIgnoreCase+índice+transação+tratamento apenas UNIQUE, gates de falha/race |
| EtapaService.java:152–158 | Snapshots de configuração apagarem cartões | Ampliar composição comum; nenhum GET separado após CRUD |
| Quadro.jsx:44/89 | Cache/refresh/rascunhos e resposta atrasada | Unificar aplicarConfiguracao validado e exclusividade dos formulários, AbortSignal |
| EtapaDemandConcurrencyTests.java:43–88 | Fixture cria na segunda etapa | Ordenar Destino primeiro apenas em casos de criação; manter todos cenários/assertions/versão legado |
| APIs antigas de edição/transição | Ainda podem alterar estado/etapa fora das regras futuras | Limite explícito; não declarar coerência permanente nem alterar políticas indefinidas |

## Tech Decisions

| Decisão | Escolha | Razão |
| --- | --- | --- |
| Schema | Nenhuma mudança | Campos e índice global já existem |
| Snapshot | Ampliar record existente | Consistência e reutilização do lock/composição |
| Datas/prioridade | Null/texto livre | Regras produto não definem taxonomia ou prazos obrigatórios |
| Versão | Exigir/incrementar só novo endpoint | Contrato antigo permanece; não afirmar revisão geral de todas demandas |
| Verificação | H2/MySQL TEMP/Edge e VerificadorRAM | Mesma stack; evitar repetir limpezas recusadas |
