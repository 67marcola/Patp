# Etapas finais obrigatórias Specification

**Status:** escopo confirmado pela sessão; defaults técnicos declarados. **Base:** a118606. **Data:** 2026-10-06.

## Problem Statement

Quadros não possuem as duas colunas finais obrigatórias nem distinguem trabalho de destino final. Os dados antigos podem ter status final em uma coluna de trabalho. Esta entrega cria/protege as colunas e prepara correção explícita; as transições futuras ainda precisam ser conectadas.

## Goals

- [ ] Todo novo quadro tem exatamente uma coluna oficial Concluídos e uma Cancelados.
- [ ] CRUD e interface preservam finais e posições de trabalho.
- [ ] Preparar e testar correção idempotente em ativos/arquivados preservando os dados antigos.

## Out of Scope

| Item | Motivo |
| --- | --- |
| ProcessoService e botões de transição/reabertura | Próxima entrega; não prometer coerência permanente de status/etapa nesta etapa |
| CRUD de demandas e Criar processo | Campos/permissões ainda serão definidos |
| Comentários, logs de negócio, gráficos, identidade visual | Requisitos próprios |
| MySQL configurado, migração real, contas reais, push/deploy | Autorização apenas local e testes isolados |

## Assumptions & Open Questions

| Assumption / decision | Chosen default | Rationale | Confirmed? |
| --- | --- | --- | --- |
| Destinos finais | Somente Concluídos e Cancelados | AD-011 | Sim |
| Alcance da preparação | Todos os quadros inclusive arquivados/sem criador | AD-016; preservar autoria existente | Sim |
| Identidade da etapa | Categoria TRABALHO/CONCLUIDA/CANCELADA | Evitar interpretar nomes ou colisões | Default técnico declarado |
| Proteção e Setor finais | Nome/categoria fixos; Setor nulo; sem editar/remover/reordenar | Colunas obrigatórias do sistema, não setores de trabalho | Default técnico declarado |
| Homônimos legados | Preservar como trabalho, criar oficial separada | Não renomear dados ou classificar pelo nome | Default técnico declarado |
| Status antigos reconhecidos | Exatamente Concluido e Cancelado | Valores existentes do código; demais inventariados sem alteração | Default conservador declarado |
| Execução | Plano somente leitura, aplicação explícita; nunca GET/startup normal | Manutenção de registros arquivados e operação revisável | Default técnico declarado |
| Testes anteriores | Adaptar só expectativas de listas/contagens decorrentes das duas finais, mantendo cobertura | Novo contrato escolhido pelo usuário substitui quadro sem nenhuma coluna | Autorizado no escopo da alteração |

**Open questions:** none neste corte; regras posteriores registradas em STATE/context não são inferidas.

## User Stories

### P1: Colunas oficiais persistidas

**User Story:** como usuário, quero encontrar Concluídos e Cancelados em cada novo quadro.
**Why P1:** requisito explícito das duas etapas obrigatórias.
**Acceptance Criteria**:
1. WHEN um quadro é criado sem trabalhos THEN o sistema SHALL persistir exatamente duas etapas, CONCLUIDA/Concluídos e CANCELADA/Cancelados, com IDs distintos e Setor nulo. (FIN-01)
2. WHEN um quadro é criado com N trabalhos THEN o sistema SHALL preservar os trabalhos recebidos e acrescentar exatamente as duas finais oficiais do mesmo quadro. (FIN-02)
3. WHEN dois quadros são criados THEN o sistema SHALL atribuir IDs de finais distintos e vinculados ao respectivo quadro. (FIN-03)
4. IF uma etapa inicial enviada tem categoria final ou ID existente THEN o sistema SHALL responder 400 sem salvar quadro ou etapas. (FIN-04)
5. IF a criação de qualquer etapa falha THEN o sistema SHALL reverter o quadro e todas as suas etapas na mesma transação. (FIN-05)
6. WHEN uma etapa antiga tem categoria nula THEN o sistema SHALL devolvê-la como TRABALHO sem gravar ou inferir pelo nome. (FIN-06)
7. WHEN a estrutura é consultada THEN o sistema SHALL retornar categoria explícita, IDs, campos e contagens reais de cada etapa. (FIN-07)
8. WHEN a estrutura é consultada THEN o sistema SHALL ordenar trabalhos por ordem/ID seguidos de CONCLUIDA e CANCELADA. (FIN-08)
**Independent Test:** HTTP/H2 e reload do navegador; persistência/category/IDs/valores exatos.

### P1: Finais protegidas e trabalho configurável

**User Story:** como criador/admin, quero configurar trabalhos sem remover as etapas obrigatórias.
**Why P1:** integridade do requisito e preservação do CRUD já entregue.
**Acceptance Criteria**:
1. IF criador/admin tenta editar uma final em quadro ativo THEN o sistema SHALL responder 409 com erro Etapas finais obrigatórias não podem ser alteradas. sem modificar registros/versão. (FIN-09)
2. IF criador/admin tenta remover uma final mesmo vazia THEN o sistema SHALL responder 409 com o mesmo erro sem modificar registros/versão. (FIN-10)
3. WHEN uma etapa de trabalho é criada/editada/removida THEN o sistema SHALL manter exatamente as duas finais, após os trabalhos e com seus IDs/campos preservados. (FIN-11)
4. WHEN há N trabalhos THEN o sistema SHALL aceitar posição 1..N+1 na criação e 1..N na edição, ignorando finais no intervalo. (FIN-12)
5. IF a posição invadir o intervalo final ou Setor estiver vazio THEN o sistema SHALL responder 400 preservando todos os registros/versão. (FIN-13)
6. WHEN uma etapa de trabalho tem nome Concluídos ou Cancelados THEN o sistema SHALL tratá-la como trabalho e aplicar as mesmas permissões e validações do CRUD existente. (FIN-14)
7. WHEN o quadro é aberto THEN a interface SHALL mostrar finais oficiais com a indicação Etapa final obrigatória e sem Setor/Editar/Remover/posição configurável. (FIN-15)
8. WHEN Nova etapa ou edição de trabalho é aberta THEN a interface SHALL oferecer somente posições entre trabalhos e manter Nome/Setor obrigatórios. (FIN-16)
9. WHEN um quadro não tem trabalho THEN a interface SHALL mostrar as duas finais e informar Nenhuma etapa de trabalho cadastrada. (FIN-17)
10. WHEN criar gerenciamento é aberto THEN a interface SHALL informar a criação automática das finais e enviar somente trabalhos no payload. (FIN-18)
11. IF um quadro está arquivado, a conta não administra ou a versão está obsoleta THEN o sistema SHALL preservar respectivamente 409/403/409 e impedir alterações conforme o CRUD anterior. (FIN-19)
**Independent Test:** HTTP direto (inclusive finais vazias/homônimos), Vitest e Edge com teclado/persistência.

### P1: Preparação explícita dos legados

**User Story:** como operador, quero revisar e preparar a correção dos quadros antigos sem perder seus registros.
**Why P1:** AD-016 inclui arquivos, sem autorização para operar o banco real nesta sessão.
**Acceptance Criteria**:
1. WHEN o plano de preparação é solicitado THEN a ferramenta SHALL inventariar todos os quadros, finais ausentes e status das demandas sem qualquer gravação. (FIN-20)
2. WHEN a aplicação explícita é executada em dados fictícios THEN a ferramenta SHALL criar as finais ausentes em todos os quadros, inclusive arquivados e sem criador. (FIN-21)
3. WHEN uma demanda tem status exatamente Concluido ou Cancelado THEN a preparação SHALL trocar somente sua referência de etapa para a final correspondente do mesmo quadro. (FIN-22)
4. WHEN a correção é aplicada THEN a preparação SHALL preservar IDs, status original, todos os demais campos/datas/motivos das demandas e todos os comentários/históricos existentes. (FIN-23)
5. WHEN a correção é aplicada THEN a preparação SHALL preservar os campos, IDs, ordem e Setor das etapas de trabalho e o estado/nome/descrição/criador dos quadros. (FIN-24)
6. WHEN status é nulo ou diferente dos dois valores reconhecidos THEN a preparação SHALL preservar a etapa e informar o valor no inventário sem criar motivo/data. (FIN-25)
7. WHEN a aplicação é repetida em dados já preparados THEN a ferramenta SHALL produzir zero criações/movimentos e preservar IDs, dados e versões. (FIN-26)
8. IF houver finais duplicadas para uma categoria ou falha durante a aplicação THEN a preparação SHALL abortar com erro explícito e reverter integralmente as alterações dessa execução. (FIN-27)
9. WHEN a aplicação altera a estrutura ou referência de demanda de um quadro THEN a preparação SHALL incrementar sua versão exatamente uma vez. (FIN-28)
10. WHEN uma consulta normal ou inicialização normal ocorre THEN o sistema SHALL não executar a preparação nem alterar os dados legados. (FIN-29)
11. WHEN a ferramenta é executada com ação inválida THEN ela SHALL recusar antes de abrir contexto/conectar banco e informar as ações plano/aplicar. (FIN-30)
**Independent Test:** serviço real H2 com todos os campos/filhos antes/depois, rollback/reexecução e MySQL TEMP/schema antigo. Roteiro operacional sem execução no banco configurado.

## Edge Cases

Homônimos e ordem antiga com lacunas/empates permanecem trabalho (FIN-06/14/24). Finais vazias também protegidas (FIN-10). Quadro só com finais continua válido (FIN-17). Datas/motivos finais ausentes ou contraditórios permanecem intactos (FIN-23/25). Preparação não fabrica eventos de negócio. Arquivados somente consulta nas rotas normais (FIN-19/29). Falha parcial e duplicidade abortam a execução explícita (FIN-27).

## Implicit Requirements Sweep

| Dimensão | Resolução |
| --- | --- |
| Inputs/bounds | Categorias fixas, ID inicial rejeitado, limites/Setor/posição existentes FIN-04/12/13 |
| Falha parcial | Transações/rollback FIN-05/27 |
| Idempotência | Preparação repetível FIN-26; mutations mantêm versão sem retry automático |
| Autorização | Guard/arquivo/versão existente FIN-19; ferramenta é comando de operador explícito, sem endpoint público |
| Concorrência/ordem | Lock de quadro e uma versão por mutação; finais após trabalhos FIN-08/11/28 |
| Ciclo de dados | Preservação/arquivados FIN-21–25; nenhuma exclusão de registros |
| Observabilidade | Relatório de preparação; não criar log de negócio/data antiga FIN-20/23 |
| Dependências externas | N/A: nenhuma chamada externa nova; falha de banco segue rollback FIN-27 |
| Transições de estado | N/A neste corte: operações de demanda ficam na próxima entrega; somente correção explícita FIN-22 |
| Rate limit/TTL | N/A: não cria autenticação ou API pública adicional |

## Requirement Traceability

| Requirement | Task | Status |
| --- | --- | --- |
| FIN-01 | T2, T3, T7 | Pending |
| FIN-02 | T3, T7 | Pending |
| FIN-03 | T2, T3, T7 | Pending |
| FIN-04 | T3 | Pending |
| FIN-05 | T3 | Pending |
| FIN-06 | T1 | Pending |
| FIN-07 | T1, T4, T7 | Pending |
| FIN-08 | T2, T4, T7 | Pending |
| FIN-09 | T4 | Pending |
| FIN-10 | T4 | Pending |
| FIN-11 | T2, T4, T7 | Pending |
| FIN-12 | T4 | Pending |
| FIN-13 | T4 | Pending |
| FIN-14 | T2, T4 | Pending |
| FIN-15 | T6, T7 | Pending |
| FIN-16 | T6, T7 | Pending |
| FIN-17 | T6, T7 | Pending |
| FIN-18 | T6, T7 | Pending |
| FIN-19 | T4, T6, T7 | Pending |
| FIN-20 | T5 | Pending |
| FIN-21 | T5 | Pending |
| FIN-22 | T5 | Pending |
| FIN-23 | T5 | Pending |
| FIN-24 | T5 | Pending |
| FIN-25 | T5 | Pending |
| FIN-26 | T2, T5 | Pending |
| FIN-27 | T2, T5 | Pending |
| FIN-28 | T5 | Pending |
| FIN-29 | T4, T5, T7 | Pending |
| FIN-30 | T5 | Pending |

## Success Criteria

Gates H2 por tarefa, suíte Java completa preservada, Vitest completo (baseline 222), build TEMP/lint e os três E2E existentes com expectativas exatas das finais. MySQL TEMP com schema antigo/legados/reexecução. Verificador novo automático, evidência por critério e sensor proporcional em scratch seguro. validate_spec/tasks/state PASS; UAT humano separado e pendente.
