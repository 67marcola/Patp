# Atividade por gerenciamento Specification

**Status:** Verified em13/10/2026,39/39 critérios e bordas PASS. Especificação aprovada em10/10/2026; três lotes sequenciais7/7/4 e correções T19/T20 concluídos. Base084757d,implementação18d27a8. Verifier independente:1087 Java/H2,1183 Vitest,18 Edge/H2,847 MySQL fictício,lint/build PASS,sensor8/8 killed evalidate_state0. Aceitação humana pendente. Relatório validation.md.

## Problem Statement

O quadro não reúne as alterações do gerenciamento e das etapas; as demandas têm histórico próprio, sem antes/depois nas edições. A equipe precisa entender o que mudou, por quem e quando, inclusive após encerramentos e arquivamentos, preservando o conteúdo anterior.

## Goals

- Registrar ações do quadro, etapas, demandas e envios de comentário na transação correspondente.
- Consultar a atividade pelo quadro com autoria da sessão e alterações anteriores/novas nas edições.
- Preservar o histórico existente e separar seus dados dos novos registros automáticos.

## Out of Scope

| Item | Motivo |
| --- | --- |
| Gráficos, exportação, filtros por autor/período e notificações | Próximas entregas; este corte é consulta da lista completa por quadro |
| Editar/apagar registros ou atribuir autores antigos | Contraria AD-031; não há rota nova de mutação da atividade |
| Migração aplicada ao banco real, push/deploy e upgrades | AD-024; somente testes fictícios e commits locais |
| Registrar leitura, login, tentativas recusadas, manutenção de etapas finais e mensagens antigas | Não são alterações de usuário no alcance; não inventar eventos retroativos |
| Alterar regras de acesso, versões ou ciclo das demandas | Preservar AD-001–029; a atividade acompanha a ação existente |
| Restringir intervenção direta de um administrador no banco | Imutabilidade pela aplicação; administração do banco é externa a este corte |

## Assumptions & Open Questions

| Tema | Chosen default | Rationale |
| --- | --- | --- |
| Escrita manual antiga, confirmada em AD-034 | HTTP409 no POST histórico e registrarExterno; manter precedência de recusas por arquivo e a leitura antiga | O navegador hoje escolhe autor/ação; eventos automáticos novos precisam acompanhar ações reais |
| Histórico existente | Exibir sem vínculo com novo evento como Histórico existente, literalmente, sem antes/depois nem autor confirmado | Pode conter entradas manuais; preservar valores sem afirmar confiança que o sistema não comprova |
| Persistência | Diário adicional, vinculado ao histórico automático da demanda por ID único quando houver | Preserva respostas antigas e evita mostrar duas vezes a mesma ação |
| Edição idêntica | Um evento de edição com lista de alterações vazia, exibido como Nenhum campo foi alterado | O sistema já aceita salvamento e incrementa versão; não alterar esse comportamento |
| Arquivo idempotente de quadro | Nenhum evento quando o quadro já está no estado pedido | O caminho atual não faz alteração; arquivo repetido da demanda continua409 |
| Reordenação | Uma ação de etapa, com posições alteradas de todas as etapas afetadas no mesmo evento | Mostrar efeitos automáticos sem atribuir ações adicionais ao usuário |
| Consulta extensa | 50 registros por página; Mais recentes primeiro; Carregar mais; Atualizar inicia nova consulta | Permite consultar todos os registros sem resposta sem limite |
| Paginação estável | Fixar IDs máximos das duas fontes na primeira página e carregar páginas desse mesmo conjunto | Novas ações não deslocam os resultados já paginados; aparecem após Atualizar |
| Interação | Painel de leitura no cabeçalho, exclusivo com formulários e lateral de comentários; fechar antes de outra ação | Preservar rascunhos, foco e bloqueios da entrega anterior |
| Datas | Novos eventos usam horário do servidor com microssegundos; legado mantém data nula se existente | Compatibilidade H2/MySQL já evidenciada; não inventar horário |
| Limites e ferramentas | Cursor até512 caracteres; tamanho de página fixo; ferramentas locais existentes/TLC já autorizadas | Entrada limitada e sem novos serviços/dependências |

**Open questions: none** — usuário confirmou explicitamente todos os defaults e o bloqueio manual, com as três adequações T13. Antes/depois e comentários resolvidos por AD-032/033. Nenhum cenário antigo será excluído ou ignorado.

## User Stories

### P1: Acompanhar alterações reais

**User Story:** Como funcionário, quero ver as ações do quadro com o detalhe que existia no momento, para entender o trabalho sem depender do estado atual.

**Acceptance Criteria**:

1. WHEN uma criação de quadro é confirmada THEN o sistema SHALL persistir um evento QUADRO_CRIADO com ID/nome do quadro, ID/nome do usuário da sessão e horário do servidor. [ACT-01]
2. WHEN uma edição de quadro é confirmada THEN o sistema SHALL registrar QUADRO_EDITADO com antes/depois de nome e descrição somente quando diferentes. [ACT-02]
3. WHEN o quadro muda de arquivo THEN o sistema SHALL registrar QUADRO_ARQUIVADO ou QUADRO_RESTAURADO com antes/depois do marcador. [ACT-03]
4. WHEN uma etapa de trabalho é criada THEN o sistema SHALL registrar ETAPA_CRIADA com ID/nome/setor/posição da etapa criada. [ACT-04]
5. WHEN uma etapa de trabalho é editada THEN o sistema SHALL registrar ETAPA_EDITADA com antes/depois dos campos nome, setor e posições efetivamente alteradas. [ACT-05]
6. WHEN uma etapa vazia é removida THEN o sistema SHALL registrar ETAPA_REMOVIDA preservando seu ID/nome/setor/posição anteriores mesmo sem a etapa existir depois. [ACT-06]
7. WHEN criar, editar ou remover etapa reorganiza posições THEN o sistema SHALL incluir no mesmo evento o ID/nome e antes/depois de cada posição alterada, incluindo normalização de lacunas e empates. [ACT-07]
8. WHEN uma demanda é cadastrada por uma rota moderna ou antiga THEN o sistema SHALL registrar DEMANDA_CRIADA com quadro, ID/número da demanda, autor real e valores iniciais informativos e de ciclo. [ACT-08]
9. WHEN uma demanda é editada por uma rota moderna ou antiga THEN o sistema SHALL registrar DEMANDA_EDITADA com antes/depois somente dos campos informativos efetivamente alterados, preservando número e encerramento. [ACT-09]
10. WHEN uma demanda muda de etapa THEN o sistema SHALL registrar DEMANDA_MOVIDA com IDs/nomes da etapa anterior e nova, autor e horário. [ACT-10]
11. WHEN uma demanda é concluída ou cancelada THEN o sistema SHALL registrar DEMANDA_CONCLUIDA ou DEMANDA_CANCELADA com alterações de etapa, status, datas e motivo efetivamente realizadas. [ACT-11]
12. WHEN uma demanda é reaberta THEN o sistema SHALL registrar DEMANDA_REABERTA com antes/depois de etapa, status e campos de encerramento limpos. [ACT-12]
13. WHEN uma demanda é arquivada ou restaurada THEN o sistema SHALL registrar DEMANDA_ARQUIVADA ou DEMANDA_RESTAURADA com antes/depois do marcador, sem alterar seu ciclo. [ACT-13]
14. WHEN um comentário é enviado por qualquer rota aceita THEN o sistema SHALL registrar COMENTARIO_ADICIONADO com demanda, autor e horário, sem texto da mensagem e sem alterações informativas. [ACT-14]
15. The sistema SHALL preservar nos novos registros o nome do autor e a identificação do alvo no momento da ação, sem substituí-los por nomes atuais nas consultas. [ACT-15]
16. WHEN uma edição aceita não muda nenhum campo THEN o sistema SHALL registrar a edição com alterações vazias. [ACT-16]
17. IF qualquer gravação da ação ou do evento falha THEN o sistema SHALL reverter ação, histórico e atividade juntos, sem registros parciais. [ACT-17]
18. IF uma ação é recusada por sessão, permissão, arquivo, validação ou versão THEN o sistema SHALL conservar todos os dados e a atividade; as rotas antigas SHALL obedecer aos mesmos caminhos centrais. [ACT-18]
19. The sistema SHALL conservar as regras atuais de versão e o histórico da demanda; comentário não incrementa a versão nem adiciona histórico de demanda. [ACT-19]
20. WHEN o arquivo de quadro já corresponde ao estado pedido THEN o sistema SHALL conservar a quantidade de eventos. [ACT-38]

**Independent Test:** Executar as ações modernas/antigas com dados fictícios e comparar linhas completas, campos esperados, autor, versão e rollback; arquivo concorrente exige leitura da demanda após o lock do quadro.

### P1: Consultar registros atuais e existentes

**User Story:** Como usuário autenticado, quero consultar a atividade inclusive dos arquivados, sem alterar ou reconstruir seus registros.

**Acceptance Criteria**:

1. WHEN qualquer usuário autenticado consulta um quadro existente THEN o sistema SHALL responder200 com o mesmo contrato de atividade em quadro ativo ou arquivado, incluindo demandas arquivadas e encerradas. [ACT-20]
2. IF a sessão está ausente/inválida THEN o sistema SHALL responder401 sem gravar dados. [ACT-21]
3. WHEN um histórico existente não possui vínculo com um evento novo THEN o sistema SHALL exibi-lo uma vez com origem HISTORICO_EXISTENTE, ID, ação, descrição, autor e data exatamente armazenados, alterações vazias e autorId nulo. [ACT-22]
4. WHEN uma ação de demanda grava histórico e atividade vinculados THEN a consulta SHALL exibir somente o evento novo dessa ação, conservando o histórico na rota antiga. [ACT-23]
5. WHEN o POST histórico antigo recebe escrita manual em uma demanda ativa THEN o sistema SHALL responder409 com Histórico é registrado automaticamente pelas ações do sistema., sem gravação; recusas de quadro/demanda arquivados SHALL conservar sua mensagem atual. [ACT-24]
6. WHEN a atividade é consultada THEN o sistema SHALL ordenar por dataHora decrescente com nulos por último, origem AUTOMATICA antes de HISTORICO_EXISTENTE em empate, e ID da fonte decrescente como desempate final. [ACT-25]
7. WHEN a primeira página é consultada THEN o sistema SHALL fixar IDs máximos das duas fontes e retornar no máximo50 registros com cursor para a página seguinte ou nulo no fim, inclusive vazio com registros[]. [ACT-26]
8. WHEN páginas seguintes desse cursor são consultadas THEN o sistema SHALL retornar o restante do mesmo conjunto, sem repetição, incluindo empates e datas nulas; novas ações SHALL aparecer somente ao iniciar nova consulta. [ACT-27]
9. IF o cursor é inválido, maior que512 caracteres, pertence a outro quadro ou contém números fora do intervalo seguro THEN o sistema SHALL responder400 sem gravação. [ACT-28]
10. The sistema SHALL responder DTOs sem entidades, senhas, sessão e texto de comentários; cursor e registros SHALL pertencer somente ao quadro consultado. [ACT-29]
11. IF o quadro consultado não existe THEN o sistema SHALL responder404 sem gravar dados. [ACT-39]

**Independent Test:** Sem painel, GET do quadro com fontes mistas, datas empatadas/nulas, mais de100 registros e inserções entre páginas; comparar valores literais e IDs completos em H2 e MySQL fictício.

### P1: Ler a atividade no quadro

**User Story:** Como funcionário, quero abrir Atividade no cabeçalho e ler alterações compreensíveis no computador e celular.

**Acceptance Criteria**:

1. WHEN Atividade é acionado num quadro consultável THEN a interface SHALL abrir painel somente de leitura com ação, alvo, autor, horário e antes/depois dos campos alterados; origem existente SHALL ser identificada sem afirmar autoria confirmada. [ACT-30]
2. WHILE o painel carrega ou está vazio THEN a interface SHALL mostrar Carregando atividade… ou Nenhuma atividade registrada., respectivamente; falha SHALL oferecer Tentar novamente sem mostrar conteúdo de outro quadro. [ACT-31]
3. WHEN Carregar mais é acionado THEN a interface SHALL acrescentar a próxima página uma única vez, bloqueando duplo clique; falha SHALL conservar os registros já carregados e permitir repetir a mesma consulta. [ACT-32]
4. WHEN Atualizar é acionado THEN a interface SHALL iniciar a primeira página de uma nova consulta, conservando registros atuais se a atualização falhar e identificando que não foi atualizada. [ACT-33]
5. WHEN o painel é fechado, desmontado ou o quadro muda THEN a interface SHALL cancelar/ignorar respostas tardias; Fechar e Escape no mesmo quadro ainda montado SHALL devolver foco ao botão Atividade. [ACT-34]
6. WHILE um formulário ou lateral de comentários está aberto THEN o botão Atividade SHALL permanecer desabilitado; WHILE a atividade está aberta THEN outras ações do quadro SHALL permanecer desabilitadas até fechar, preservando rascunhos e bloqueios atuais. [ACT-35]
7. The interface SHALL mostrar os valores como texto simples escapado, identificar ausências como Não informado, mostrar edição vazia como Nenhum campo foi alterado e oferecer painel usável em360px e desktop sem corte de controles. [ACT-36]
8. IF a resposta de atividade viola o contrato, excede50 registros, possui chave ou par origem/ID repetido, cursor incorreto ou gerenciamentoId divergente da rota na página ou em algum registro THEN o cliente SHALL recusá-la com erro de leitura, sem tratá-la como vazia ou mesclar registros parciais. [ACT-37]

**Independent Test:** Painel com API controlada em Vitest; navegação real Edge/H2 com ações de duas contas, arquivo/restauração, fontes mistas, antes/depois e celular.

## Edge Cases

- Normalização antes/depois usa valores realmente persistidos, incluindo null, espaços significativos e observações de10.000 caracteres; não truncar valores históricos para caber em VARCHAR255.
- Remover etapa conserva alvo no diário; não criar FK para etapa, demanda ou autor que impeça preservar identificação. FK do quadro é obrigatória; exclusão física de quadro não é a política atual.
- Criação de quadro reúne etapas iniciais em seu evento; não gerar eventos fictícios para as finais criadas/protegidas automaticamente. Ferramenta PrepararEtapasFinais permanece sem registros de ações de usuário.
- Edição não registra campos imutáveis iguais. Movimentar para a mesma etapa continua recusado conforme contrato atual, sem evento.
- Duas etapas podem ter o mesmo nome. Em transições, IDs distintos exigem contexto etapaNome anterior/novo mesmo quando os nomes são iguais; isso identifica origem/destino e não uma renomeação. Diferenças informativas permanecem estritas. Precisão técnica confirmada em12/10/2026, sem alterar escolhas aprovadas.
- Nas duas fontes, todos os registros pertencem ao quadro; histórico sem data/autor conserva null. Demanda antiga pode usar identificação atual porque não existe snapshot, deixando origem explícita.
- Concorrência: quadro/demanda arquivado ou restaurado em outra transação antes do lock da ação; testar caminhos modernos e antigos. Uma falha do diário não pode deixar ação/histórico persistidos.
- Não adicionar timers, envio automático, limpeza de dados, TTL ou serviços externos.
- Precisão de apresentação ACT-36 em13/10: somente o campo arquivado mostra true como Sim e false como Não. DTOs e outros textos, inclusive literais true/false em observações ou histórico existente, permanecem inalterados.

## Implicit dimensions

| Dimensão | Resolução |
| --- | --- |
| Validação/limites | ACT-28/29/37; página50, cursor512, IDs seguros positivos, textos escapados |
| Falha/parcial | ACT-17/18/31–34; rollback e leitura sem mistura |
| Repetição | ACT-16/23/27/32; seguir ação existente, consulta repetível |
| Auth/rate limit | ACT-18/20/21/24; nenhuma nova escrita externa; rate limit N/A porque fora do corte e sem política existente a ampliar |
| Concorrência/ordem | ACT-18/25–27; lock atual e conjunto fixo na paginação |
| Ciclo/expiração | ACT-15/19/20/22; preservar sem expiração |
| Observabilidade | Diário por operação confirmada; logs técnicos existentes; métricas/tracing N/A porque não necessários à consulta |
| Dependência externa | Apenas banco/HTTP atuais, ACT-17/31–34; circuit breaker N/A porque não há serviço adicional |
| Integridade de transição | ACT-10–13/18/19, regras existentes preservadas |

## Requirement Traceability

| Requirement ID | Story | Phase | Status |
| --- | --- | --- | --- |
| ACT-01 | Ações | Execute | Verified |
| ACT-02 | Ações | Execute | Verified |
| ACT-03 | Ações | Execute | Verified |
| ACT-04 | Ações | Execute | Verified |
| ACT-05 | Ações | Execute | Verified |
| ACT-06 | Ações | Execute | Verified |
| ACT-07 | Ações | Execute | Verified |
| ACT-08 | Ações | Execute | Verified |
| ACT-09 | Ações | Execute | Verified |
| ACT-10 | Ações | Execute | Verified |
| ACT-11 | Ações | Execute | Verified |
| ACT-12 | Ações | Execute | Verified |
| ACT-13 | Ações | Execute | Verified |
| ACT-14 | Ações | Execute | Verified |
| ACT-15 | Ações | Execute | Verified |
| ACT-16 | Ações | Execute | Verified |
| ACT-17 | Ações | Execute | Verified |
| ACT-18 | Ações | Execute | Verified |
| ACT-19 | Ações | Execute | Verified |
| ACT-20 | Consulta | Execute | Verified |
| ACT-21 | Consulta | Execute | Verified |
| ACT-22 | Consulta | Execute | Verified |
| ACT-23 | Consulta | Execute | Verified |
| ACT-24 | Consulta | Execute | Verified |
| ACT-25 | Consulta | Execute | Verified |
| ACT-26 | Consulta | Execute | Verified |
| ACT-27 | Consulta | Execute | Verified |
| ACT-28 | Consulta | Execute | Verified |
| ACT-29 | Consulta | Execute | Verified |
| ACT-30 | Painel | Execute | Verified |
| ACT-31 | Painel | Execute | Verified |
| ACT-32 | Painel | Execute | Verified |
| ACT-33 | Painel | Execute | Verified |
| ACT-34 | Painel | Execute | Verified |
| ACT-35 | Painel | Execute | Verified |
| ACT-36 | Painel | Execute | Verified |
| ACT-37 | Painel | Execute | Verified |
| ACT-38 | Ações | Execute | Verified |
| ACT-39 | Consulta | Execute | Verified |

39 critérios Verified após PASS independente em13/10/2026; T1–T20 verificados. Evidências e sensor no relatório validation.md,aceitação humana separada e pendente.

## Success Criteria

- [x]39 critérios com evidência positiva e negativa; não inferir sucesso de compilação.
- [x]Java/H2, Vitest, MySQL fictício, Edge/H2, lint/build aprovados, sem skips/retries/deletar cenários.
- [x]Verifier independente PASS e sensor mínimo5 falhas comportamentais detectadas, árvore real preservada.
- [x]Diagnóstico atualizado com conclusão somente após gates; aceitação humana registrada separadamente.
