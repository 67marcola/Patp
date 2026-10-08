# Arquivamento e restauração de demandas Specification

**Status:** implementação autorizada em 08/10/2026 após apresentação da proposta concreta; execução em dois lotes sequenciais (servidor e interface). Base 3f9d78a.

## Problem Statement

Criação, leitura, edição e transições estão entregues. Falta retirar demandas sem apagar informações. A exclusão legada tenta gravar evento vinculado antes de apagar e falha por integridade; a interface não oferece retirada. O usuário escolheu arquivar/restaurar preservando dados.

## Goals

- [ ] Arquivar/restaurar sem perda de dados ou mudança de ciclo.
- [ ] Consultar arquivadas no quadro e proteger as rotas atuais e antigas.
- [ ] Provar os critérios, executar Verifier independente e registrar conclusão no diagnóstico.

## Out of Scope

| Item | Reason |
| --- | --- |
| Exclusão física, expiração ou limpeza | Usuário escolheu preservação e restauração. |
| Nova etapa ou estado ao restaurar | Restaurar recupera a situação anterior; ações de ciclo continuam próprias. |
| Corrigir status desconhecido | AD-022; não inventar estado. |
| Lateral de comentários, gráficos e atividade completa | Entregas próprias; aqui somente preservar e proteger arquivo. |
| Nova autoria dos endpoints manuais de comentário/histórico | Manter comportamento ativo atual, acrescentando proteção do arquivo. |
| Banco real, migração real, push/deploy ou atualizar tecnologias | Escopo local; AD-024 preservada. |

## Assumptions & Open Questions

| Assumption / decision | Chosen default | Rationale | Confirmed? |
| --- | --- | --- | --- |
| Retirada | Arquivar/restaurar | Preservar dados, comentários e histórico | Sim, resposta explícita 08/10 |
| Permissão | Criador/admin, quadro ativo; sem criador somente admin | Responsabilidade igual à edição | Sim, resposta explícita 08/10 |
| Estados | Em andamento, Concluido e Cancelado | Organizar trabalho e encerradas sem apagar | Sim, resposta explícita 08/10 |
| Restauração | Mesma etapa/estado/dados | Sem reabertura ou movimento implícito | Default informado ao usuário |
| Consulta e contagem | Área Arquivadas; colunas contam somente ativas | Separar arquivo do estado de trabalho | Default documentado |
| Remoção de etapa | Arquivadas continuam impedindo remoção | AD-008 e referência original | Default alinhado à decisão vigente |
| Número | Reservado globalmente inclusive no arquivo | AD-019/025 | Decisão vigente |
| GETs legados | Consultáveis, com marcador no retorno entidade | Compatibilidade; snapshot moderno separa listas | Default documentado |
| Escrita legada | DELETE bloqueado; JSON não controla marcador; arquivo só consulta | Evitar contorno da política | Default documentado |
| Auditoria | Um evento por ação; nenhum motivo novo obrigatório | Histórico existente registra autoria/data | Default documentado |
| UI e falhas | Confirmação por teclado, pending global, GET manual sem retry | Padrão validado | Default documentado |
| Tecnologias | AD-024, sem biblioteca nova | Reutilizar conjunto atual | Confirmadas anteriormente |

**Open questions:** none. Produto, proposta técnica, dois lotes sequenciais e duas adequações concretas de testes autorizados pelo “Pode continuar” após sua apresentação.

## User Stories

### P1: Retirar e recuperar sem perder informação

**User Story:** Como criador/admin, quero arquivar/restaurar demandas para organizar o quadro preservando o registro.

**Independent Test:** Arquivar demanda com comentários, consultar arquivo e restaurar; comparar campos, ciclo e histórico anterior.

**Acceptance Criteria:**

- **ARQ-01:** IF a sessão estiver ausente, inválida ou encerrada THEN o sistema SHALL devolver HTTP 401 sem efeitos persistidos.
- **ARQ-02:** IF funcionário que não criou o quadro solicitar arquivar/restaurar THEN o sistema SHALL devolver HTTP 403 sem efeitos persistidos.
- **ARQ-03:** IF o quadro estiver arquivado THEN o sistema SHALL devolver HTTP 409 com `Gerenciamento arquivado. Restaure-o antes de alterar.` sem efeitos.
- **ARQ-04:** WHILE o quadro não tiver criador o sistema SHALL permitir arquivar/restaurar somente a administrador, sem atribuir criador.
- **ARQ-05:** WHEN demanda ativa de estado reconhecido for arquivada THEN o sistema SHALL marcar arquivo preservando ID, número, sete dados informativos, status, etapa e campos de encerramento literalmente.
- **ARQ-06:** WHEN demanda arquivada de estado reconhecido for restaurada THEN o sistema SHALL torná-la ativa na mesma etapa/estado, preservando todos os dados originais.
- **ARQ-07:** IF status for null, vazio ou desconhecido THEN o sistema SHALL recusar arquivar/restaurar com HTTP 409 e `Status antigo não reconhecido. Solicite a correção do registro.`, sem efeitos.
- **ARQ-08:** IF a demanda já estiver no estado de arquivo solicitado THEN o sistema SHALL devolver HTTP 409 com `A demanda já está arquivada.` ou `A demanda já está ativa.`, conforme a ação, sem evento ou incremento.

### P1: Persistir com responsabilidade

**User Story:** Como responsável pelo quadro, quero autoria, transação e controle de conflito para evitar resultados parciais.

**Independent Test:** Disputar a mesma versão e provocar falhas nas escritas; somente um commit completo deve existir.

**Acceptance Criteria:**

- **ARQ-09:** IF versão estiver ausente ou negativa THEN o sistema SHALL devolver HTTP 400 com `Informe uma versão válida do gerenciamento.` sem efeitos.
- **ARQ-10:** IF versão não for inteiro JSON válido ou houver campo desconhecido THEN o sistema SHALL devolver HTTP 400 com `Dados da requisição inválidos.` sem efeitos.
- **ARQ-11:** IF versão enviada diferir da persistida THEN o sistema SHALL devolver HTTP 409 com `Gerenciamento alterado por outro usuário. Atualize e tente novamente.` sem efeitos.
- **ARQ-12:** IF demanda não existir no gerenciamento da rota THEN o sistema SHALL devolver HTTP 404 com `Demanda não encontrada neste gerenciamento.` sem efeitos.
- **ARQ-13:** WHEN arquivar/restaurar for aceito THEN o sistema SHALL incrementar a versão do quadro exatamente uma vez e devolver HTTP 200 com snapshot completo atualizado.
- **ARQ-14:** WHEN arquivar/restaurar for aceito THEN o sistema SHALL acrescentar exatamente um evento ARQUIVAMENTO/RESTAURACAO com descrição `Demanda arquivada.`/`Demanda restaurada.`, autor real da sessão e data/hora.
- **ARQ-15:** IF escrita da demanda, evento ou versão falhar THEN o sistema SHALL reverter todas as escritas da operação.
- **ARQ-16:** WHEN arquivo/restauração disputar edição, transição, comentário ou arquivamento do quadro THEN o sistema SHALL ordenar operações pelo lock do quadro e recusar estado/versão incompatível sem resultado parcial.
- **ARQ-17:** WHILE demanda estiver arquivada o sistema SHALL manter seu número reservado globalmente e preservar comentários/eventos anteriores literalmente.
- **ARQ-18:** The sistema SHALL interpretar marcador legado null como não arquivado sem gravação automática; novos cadastros começam ativos e JSON legado não pode definir o marcador.

### P1: Consultar e proteger o arquivo

**User Story:** Como usuário autenticado, quero consultar arquivadas com dados preservados.

**Independent Test:** Consultar duas listas, contar só ativas e tentar as rotas antigas sobre arquivo; todas recusadas sem efeitos.

**Acceptance Criteria:**

- **ARQ-19:** WHEN snapshot for consultado ou devolvido por qualquer mutação do quadro THEN o sistema SHALL incluir listas demandas e demandasArquivadas disjuntas, ordenadas por ID, com mesmo contrato de campos e todas as demandas do quadro.
- **ARQ-20:** The sistema SHALL calcular quantidadeDemandas das etapas somente pelas demandas não arquivadas.
- **ARQ-21:** WHILE etapa contiver qualquer demanda arquivada o sistema SHALL impedir sua remoção com HTTP 409 e `Esta etapa possui demandas. Mova-as para outra etapa antes de removê-la.`, preservando a referência de restauração.
- **ARQ-22:** WHILE demanda estiver arquivada o sistema SHALL recusar edição, transições, novos comentários e histórico manual com HTTP 409 e `Demanda arquivada. Restaure-a antes de alterar.`, inclusive nas rotas antigas.
- **ARQ-23:** WHEN DELETE legado /api/processos/{id} for chamado por usuário autorizado em quadro ativo THEN o sistema SHALL devolver HTTP 409 com `Exclusão definitiva indisponível. Use o arquivamento da demanda.`, sem apagar nem acrescentar evento.

### P1: Usar o fluxo no quadro

**User Story:** Como criador/admin, quero confirmar ações por teclado e conferir persistência após recarga.

**Independent Test:** Confirmar, cancelar, provocar conflito e recarregar; conferir arquivo e retorno à coluna original.

**Acceptance Criteria:**

- **ARQ-24:** WHEN quadro for consultado THEN a interface SHALL mostrar cartões ativos nas colunas e área Arquivadas com dados completos das arquivadas para consulta também pelos demais usuários autenticados.
- **ARQ-25:** WHILE criador/admin consultar quadro ativo e demanda de estado reconhecido a interface SHALL oferecer Arquivar no cartão ativo e Restaurar no arquivo; demais usuários, quadro arquivado e status desconhecido ficam somente consulta.
- **ARQ-26:** WHEN Arquivar/Restaurar for aberto THEN a interface SHALL mostrar confirmação com número, cliente e efeito, incluindo etapa/estado preservados na restauração; cancelar não envia mutação e restaura foco existente.
- **ARQ-27:** WHILE mutação estiver pendente a interface SHALL impedir envio duplicado, fechamento, outras mutações e saída do quadro.
- **ARQ-28:** WHEN HTTP 200 confirmar versão anterior+1 e demanda correta transferida entre listas com todos os campos preservados THEN a interface SHALL atualizar cartões, contagens/cache, fechar confirmação e focar controle existente; respostas divergentes não são aceitas.
- **ARQ-29:** IF ocorrer erro HTTP, rede ou sucesso não confirmado THEN a interface SHALL manter confirmação/erro, exigir GET manual antes de novo envio e não repetir mutação automaticamente.
- **ARQ-30:** WHEN quadro exibido mudar THEN a interface SHALL descartar confirmação anterior e ignorar respostas tardias do quadro antigo.

## Edge Cases

- Null/false legado ativo; true arquivado; preservar datas/motivos longos/criador e status antigos sem normalização.
- Repetição recusa sem evento; restaurar encerrada não a reabre.
- Número com caixa diferente permanece reservado em todos os quadros.
- Etapa com zero ativas e uma arquivada continua protegida; área do arquivo explica restaurar e mover para liberar a referência.
- GET de quadro arquivado mostra ambas as listas; restaurar quadro não restaura demandas automaticamente.
- JSON dedicado apenas {versao}; reusar TransicaoVersaoRequest que rejeita campos extras e tipos/fração/texto/bool inválidos.
- Evento administrativo interno pode ser registrado, mas POST manual não contorna proteção mesmo por acao=ARQUIVAMENTO.
- Histórico/comentários GET preservados; payload não cria demanda já arquivada.
- GET manual não envia mutação; alvo que deixou de ser elegível não permite novo envio, mas confirmação pode ser cancelada.
- Nova confirmação API exige demandasArquivadas; em contratos antigos ausência equivale a vazio por compatibilidade, presença inválida nunca é aceita.

## Implicit Requirement Dimensions

| Dimension | Resolution |
| --- | --- |
| Input validation & bounds | ARQ-09/10/12; payload só versão. |
| Failure / partial failure | ARQ-15/29; snapshot físico após recusa. |
| Idempotency / retry / duplicate | ARQ-08/11/27/29; um evento, sem retry. |
| Auth boundaries & rate limits | ARQ-01–04/25; novo rate limit N/A, API autenticada existente. |
| Concurrency / ordering | ARQ-11/13/16; lock persistido. |
| Data lifecycle / expiry | ARQ-05–08/17–23; expiração N/A, preservação indefinida. |
| Observability | ARQ-14, autoria/data reais. |
| External-dependency failure | ARQ-15/29; integração nova N/A. |
| State-transition integrity | ARQ-05–08/18/22/28/30. |

## Requirement Traceability

| Requirement ID | Story | Phase | Status |
| --- | --- | --- | --- |
| ARQ-01 | P1: Autorização | Execute | Implemented |
| ARQ-02 | P1: Autorização | Execute | Implemented |
| ARQ-03 | P1: Autorização | Execute | Implemented |
| ARQ-04 | P1: Autorização | Execute | Implemented |
| ARQ-05 | P1: Preservação | Execute | Implemented |
| ARQ-06 | P1: Preservação | Execute | Implemented |
| ARQ-07 | P1: Preservação | Execute | Implemented |
| ARQ-08 | P1: Consistência | Execute | Implemented |
| ARQ-09 | P1: Consistência | Execute | Implemented |
| ARQ-10 | P1: Consistência | Execute | Implemented |
| ARQ-11 | P1: Consistência | Execute | Implemented |
| ARQ-12 | P1: Consistência | Execute | Implemented |
| ARQ-13 | P1: Consistência | Execute | Implemented |
| ARQ-14 | P1: Histórico | Execute | Implemented |
| ARQ-15 | P1: Consistência | Execute | Implemented |
| ARQ-16 | P1: Consistência | Execute | Implemented |
| ARQ-17 | P1: Leitura | Execute | Implemented |
| ARQ-18 | P1: Leitura | Execute | Implemented |
| ARQ-19 | P1: Leitura | Execute | Implemented |
| ARQ-20 | P1: Leitura | Execute | Implemented |
| ARQ-21 | P1: Leitura | Execute | Implemented |
| ARQ-22 | P1: Proteção | Execute | Implemented |
| ARQ-23 | P1: Proteção | Execute | Implemented |
| ARQ-24 | P1: Interface | Specify | Pending |
| ARQ-25 | P1: Interface | Specify | Pending |
| ARQ-26 | P1: Interface | Specify | Pending |
| ARQ-27 | P1: Interface | Specify | Pending |
| ARQ-28 | P1: Interface | Specify | Pending |
| ARQ-29 | P1: Interface | Specify | Pending |
| ARQ-30 | P1: Interface | Specify | Pending |

**Coverage:** 23 critérios do servidor Implemented; 7 critérios da interface pendentes. Nenhum Verified antecipado; revisão independente após T12.

## Success Criteria

- [x] Política e permissão registradas no STATE em AD-027/028.
- [ ] Todos os critérios/bordas provados com asserts exatos e gates aprovados.
- [ ] Suítes anteriores preservadas; adequações concretas autorizadas.
- [ ] Verifier independente/sensor expandido/validate_state PASS.
- [ ] Diagnóstico atualizado por requisito concluído; aceitação humana separada.
