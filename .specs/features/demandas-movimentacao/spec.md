# Movimentação e encerramento de demandas Specification

**Status:** regras fechadas pelas decisões AD-011–015/021/022 e autorização persistente de implementação local. Em execução.

## Problem Statement

As APIs antigas permitem movimentar e encerrar demandas sem a permissão combinada e sem coerência entre etapa e status. O quadro precisa oferecer ações que preservem o histórico de cada ciclo, inclusive na reabertura definida em AD-021.

## Goals

- [ ] Mover ou pular etapas, concluir, cancelar e reabrir com integridade e autorização.
- [ ] Entregar botões e diálogos com confirmação do resultado, testes e revisão independente.

## Out of Scope

| Feature | Reason |
| --- | --- |
| Edição de outros campos e exclusão de demandas | Políticas próprias ainda pendentes; fechar somente o desvio de transições pela edição genérica. |
| Comentários, logs gerais do quadro e gráficos | Requisitos posteriores; preservar registros existentes. |
| Migração no banco real, contas reais, push e deploy | Sem autorização para essas operações. |

## Assumptions & Open Questions

| Assumption / decision | Chosen default | Rationale | Confirmed? |
| --- | --- | --- | --- |
| Permissão, destinos e reabertura | AD-005/006/011–015/021 | Escolhas explícitas do usuário; reabertura limpa campos atuais e guarda encerramentos antigos. | sim |
| Status legado vazio ou desconhecido | Consulta preservada; transições bloqueadas com409 | AD-022, resposta A do usuário. Não classificar automaticamente. | sim |
| Repetição e mesmo destino | HTTP 409 sem evento ou versão | Evitar histórico fictício e repetição de encerramento; novo ciclo requer reabrir. | default técnico explícito |
| Motivo novo | Normalizar com helper existente; 1..10000 UTF-16 | Mesmo teto das observações; antigos motivos permanecem completos, sem limite retroativo. | default técnico explícito |
| Datas | LocalDate.now do servidor; não inventar datas legadas | Compatibilidade com modelo existente; timestamps de reabertura são do novo evento. | default técnico explícito |
| Histórico longo | Descrição LONGTEXT e snapshot de campos anteriores em REABERTURA | VARCHAR255 atual não comporta nomes/motivos ou encerramentos antigos; não truncar. | default técnico explícito |
| Quadros antigos sem finais | Recusar fechamento e orientar preparação explícita | AD-016 já oferece ferramenta; ação normal não migra dados. | default técnico explícito |
| API e cache | Nova API tipada por quadro + versão; legado compartilha serviço e incrementa versão | Preservar formato legado sem desvio das regras e invalidar snapshots antigos. | default técnico explícito |

**Open questions:** none; todas resolvidas ou defaults técnicos explícitos acima.

## User Stories

### P1: Permissões

**User Story:** Como usuário do quadro, quero permissões coerentes para acompanhar cada demanda.

**Acceptance Criteria:**

1. IF a sessão estiver ausente, inválida ou expirada THEN o servidor SHALL responder HTTP 401 sem alterar demanda, histórico ou versão. <!-- MOV-01 -->
2. IF um usuário autenticado não for criador do quadro nem administrador THEN o servidor SHALL responder HTTP 403 nas quatro ações e manter demanda, histórico e versão intactos. <!-- MOV-02 -->
3. WHILE o quadro estiver arquivado o servidor SHALL responder HTTP 409 nas quatro ações sem alterar dados; sua mensagem será Gerenciamento arquivado. Restaure-o antes de alterar. <!-- MOV-03 -->
4. WHEN o quadro não tiver criador THEN o servidor SHALL permitir as quatro ações somente a administradores autenticados. <!-- MOV-04 -->

**Independent Test:** executar cenários correspondentes com contas e quadros fictícios e comparar estado completo antes/depois.

### P1: Movimentação

**User Story:** Como usuário do quadro, quero movimentação coerentes para acompanhar cada demanda.

**Acceptance Criteria:**

5. WHEN uma demanda Em andamento for movida para outra etapa TRABALHO do mesmo quadro ativo pelo criador/admin THEN o servidor SHALL persistir esse destino, inclusive anterior ou posterior não adjacente, mantendo status e demais campos. <!-- MOV-05 -->
6. IF o destino não existir no quadro persistido da demanda THEN o servidor SHALL responder HTTP 404 sem efeitos. <!-- MOV-06 -->
7. IF o destino for CONCLUIDA ou CANCELADA THEN o servidor SHALL responder HTTP 400 sem efeitos; concluir/cancelar usam ações próprias. <!-- MOV-07 -->
8. IF a demanda estiver Concluido ou Cancelado THEN a movimentação comum SHALL responder HTTP 409 sem efeitos e exigir reabertura. <!-- MOV-08 -->
9. IF o destino for a própria etapa atual THEN a movimentação SHALL responder HTTP 409 sem produzir novo histórico ou versão. <!-- MOV-09 -->

**Independent Test:** executar cenários correspondentes com contas e quadros fictícios e comparar estado completo antes/depois.

### P1: Encerramento

**User Story:** Como usuário do quadro, quero encerramento coerentes para acompanhar cada demanda.

**Acceptance Criteria:**

10. WHEN concluir uma demanda Em andamento em qualquer etapa de trabalho THEN o servidor SHALL gravar status Concluido, etapa CONCLUIDA do mesmo quadro, dataConclusao igual à data atual do servidor, dataCancelamento null e motivoCancelamento null. <!-- MOV-10 -->
11. WHEN cancelar uma demanda Em andamento em qualquer etapa de trabalho com motivo válido THEN o servidor SHALL gravar status Cancelado, etapa CANCELADA do mesmo quadro, dataCancelamento igual à data atual do servidor, dataConclusao null e motivoCancelamento normalizado. <!-- MOV-11 -->
12. IF o motivo de novo cancelamento for ausente, somente espaços ou exceder 10000 caracteres UTF-16 após normalização THEN o servidor SHALL responder HTTP 400 sem efeitos. <!-- MOV-12 -->
13. IF concluir ou cancelar uma demanda já Concluido ou Cancelado THEN o servidor SHALL responder HTTP 409 sem efeitos; um novo ciclo exige reabrir. <!-- MOV-13 -->
14. IF a coluna final necessária não existir ou tiver duplicidade de categoria no quadro THEN o servidor SHALL responder HTTP 409 sem efeitos e solicitar preparação das etapas finais; a ação não cria nem corrige colunas implicitamente. <!-- MOV-14 -->

**Independent Test:** executar cenários correspondentes com contas e quadros fictícios e comparar estado completo antes/depois.

### P1: Reabertura

**User Story:** Como usuário do quadro, quero reabertura coerentes para acompanhar cada demanda.

**Acceptance Criteria:**

15. WHEN reabrir uma demanda Concluido ou Cancelado para uma etapa TRABALHO escolhida no mesmo quadro ativo THEN o servidor SHALL persistir o destino, status Em andamento e dataConclusao, dataCancelamento e motivoCancelamento null. <!-- MOV-15 -->
16. WHEN reabrir uma demanda encerrada THEN o histórico REABERTURA SHALL preservar literalmente o status, os IDs das etapas anterior/destino e os valores anteriores de dataConclusao, dataCancelamento e motivoCancelamento, inclusive null, datas antigas e motivos longos, antes da limpeza. <!-- MOV-16 -->
17. IF reabrir uma demanda Em andamento THEN o servidor SHALL responder HTTP 409 sem efeitos. <!-- MOV-17 -->
18. IF o destino da reabertura não existir no mesmo quadro ou for uma etapa final THEN o servidor SHALL responder respectivamente HTTP 404 ou 400 sem efeitos. <!-- MOV-18 -->

**Independent Test:** executar cenários correspondentes com contas e quadros fictícios e comparar estado completo antes/depois.

### P1: Integridade

**User Story:** Como usuário do quadro, quero integridade coerentes para acompanhar cada demanda.

**Acceptance Criteria:**

19. WHEN uma das quatro ações for aceita THEN o servidor SHALL criar exatamente um evento MUDANCA_ETAPA, CONCLUSAO, CANCELAMENTO ou REABERTURA correspondente, com usuário da sessão, dataHora do evento e descrição fiel da ação. <!-- MOV-19 -->
20. IF falhar a persistência da demanda, histórico ou versão THEN o servidor SHALL reverter integralmente esses três dados e responder erro sem sucesso parcial. <!-- MOV-20 -->
21. WHEN uma ação aceita terminar THEN o servidor SHALL incrementar a versão do gerenciamento exatamente uma vez e devolver snapshot coerente de etapas, contagens e demandas. <!-- MOV-21 -->
22. IF a versão enviada estiver ausente, negativa ou fora do formato inteiro aceito pelo DTO THEN a API tipada SHALL responder HTTP 400 sem efeitos. <!-- MOV-22 -->
23. IF a versão enviada diferir da versão persistida sob lock THEN a API tipada SHALL responder HTTP 409 sem efeitos com Gerenciamento alterado por outro usuário. Atualize e tente novamente. <!-- MOV-23 -->
24. WHEN ações concorrentes ou uma alteração de estrutura/arquivamento competirem pelo mesmo quadro THEN o servidor SHALL serializar a validação e gravação sob o lock existente, impedindo estado impossível, destino removido e evento parcial. <!-- MOV-24 -->
25. IF o ID de demanda não existir ou pertencer a outro quadro na API tipada THEN o servidor SHALL responder HTTP 404 sem efeitos. <!-- MOV-25 -->
26. IF a edição genérica PUT tentar mudar status, etapa ou campos de encerramento THEN o servidor SHALL recusar HTTP 400 sem efeitos; status/etapa omitidos preservam os valores persistidos, e editar outros campos mantém a política existente. <!-- MOV-26 -->
27. WHEN as rotas legadas de mover/concluir/cancelar forem usadas THEN o servidor SHALL aplicar as mesmas permissões, regras de estado, histórico, versão e transação, mantendo sua resposta entidade HTTP 200 e sem exigir versão no corpo legado. <!-- MOV-27 -->

**Independent Test:** executar cenários correspondentes com contas e quadros fictícios e comparar estado completo antes/depois.

### P1: Interface

**User Story:** Como usuário do quadro, quero interface coerentes para acompanhar cada demanda.

**Acceptance Criteria:**

28. WHILE o quadro ativo permitir administração e a demanda tiver estado reconhecido THEN o quadro SHALL oferecer Mover/pular etapa, Concluir e Cancelar para Em andamento, ou Reabrir para Concluido/Cancelado. <!-- MOV-28 -->
29. WHEN escolher Mover/pular etapa ou Reabrir THEN o diálogo SHALL listar somente etapas de trabalho do mesmo snapshot; mover exclui a etapa atual e reabrir permite qualquer etapa de trabalho. <!-- MOV-29 -->
30. WHEN cancelar THEN o diálogo SHALL exigir motivo e exibir o limite de 10000 caracteres; concluir apresenta destino automático Concluídos e cancelar apresenta Cancelados. <!-- MOV-30 -->
31. WHILE uma mutação estiver pendente o quadro SHALL bloquear envio repetido, outros formulários, Voltar, Sair e cancelamento do diálogo. <!-- MOV-31 -->
32. IF houver erro HTTP, rede ou resposta de sucesso que não confirme a ação solicitada THEN a interface SHALL manter diálogo e rascunho, mostrar erro, exigir atualização manual e não repetir automaticamente a mutação. <!-- MOV-32 -->
33. WHEN receber HTTP 200 com snapshot válido, versão enviada mais um e a demanda solicitada no destino/status/campos de encerramento esperados THEN a interface SHALL atualizar os cartões e contagens e fechar o diálogo restaurando foco em elemento existente. <!-- MOV-33 -->
34. WHEN o quadro exibido mudar ou uma consulta antiga terminar THEN a interface SHALL ignorar resultados do quadro anterior e não aplicar seu formulário ou snapshot ao novo quadro. <!-- MOV-34 -->

**Independent Test:** executar cenários correspondentes com contas e quadros fictícios e comparar estado completo antes/depois.

35. IF o status persistido for null, vazio ou diferente dos três estados reconhecidos THEN o servidor SHALL responder HTTP 409 em qualquer transição sem efeitos, mantendo consulta e valores antigos; a interface não oferece ações e informa Status antigo não reconhecido. Solicite a correção do registro. <!-- MOV-35 -->

## Edge Cases

Cobertos por MOV-01–27: autorização negada, quadro sem criador/arquivado, destinos externos/finais/iguais, ausência de finais, versões antigas, concorrência, falha de persistência e campos antigos preservados. MOV-35 cobre os status legados desconhecidos, sem normalização ou migração automática.

IF uma demanda Em andamento estiver incoerentemente em coluna final THEN o servidor SHALL recusar movimentação/conclusão/cancelamento com409 sem normalizar o registro; informar Estado da demanda incompatível com a etapa atual. Solicite a correção do registro. Esse edge preserva a precondição de trabalho em MOV-05/10/11.

## Requirement Traceability

| Requirement ID | Story | Phase | Status |
| --- | --- | --- | --- |
| MOV-01 | P1: Permissões | Execute | Implementing |
| MOV-02 | P1: Permissões | Execute | Implementing |
| MOV-03 | P1: Permissões | Execute | Implementing |
| MOV-04 | P1: Permissões | Execute | Implementing |
| MOV-05 | P1: Movimentação | Execute | Implementing |
| MOV-06 | P1: Movimentação | Execute | Implementing |
| MOV-07 | P1: Movimentação | Execute | Implementing |
| MOV-08 | P1: Movimentação | Execute | Implementing |
| MOV-09 | P1: Movimentação | Execute | Implementing |
| MOV-10 | P1: Encerramento | Execute | Implementing |
| MOV-11 | P1: Encerramento | Execute | Implementing |
| MOV-12 | P1: Encerramento | Execute | Implementing |
| MOV-13 | P1: Encerramento | Execute | Implementing |
| MOV-14 | P1: Encerramento | Execute | Implementing |
| MOV-15 | P1: Reabertura | Execute | Implementing |
| MOV-16 | P1: Reabertura | Execute | Implementing |
| MOV-17 | P1: Reabertura | Execute | Implementing |
| MOV-18 | P1: Reabertura | Execute | Implementing |
| MOV-19 | P1: Integridade | Execute | Implementing |
| MOV-20 | P1: Integridade | Execute | Implementing |
| MOV-21 | P1: Integridade | Execute | Implementing |
| MOV-22 | P1: Integridade | Execute | Implementing |
| MOV-23 | P1: Integridade | Execute | Implementing |
| MOV-24 | P1: Integridade | Execute | Implementing |
| MOV-25 | P1: Integridade | Execute | Implementing |
| MOV-26 | P1: Integridade | Execute | Implementing |
| MOV-27 | P1: Integridade | Execute | Implementing |
| MOV-28 | P1: Interface | Specify | Pending |
| MOV-29 | P1: Interface | Specify | Pending |
| MOV-30 | P1: Interface | Specify | Pending |
| MOV-31 | P1: Interface | Specify | Pending |
| MOV-32 | P1: Interface | Specify | Pending |
| MOV-33 | P1: Interface | Specify | Pending |
| MOV-34 | P1: Interface | Specify | Pending |

| MOV-35 | P1: Integridade | Tasks | Pending |

**Coverage:** 35 critérios mapeados nas tarefas T1–T7.

## Success Criteria

- [ ] Todos os critérios mapeados a assertions de estado/resultado e aprovados nos gates locais.
- [ ] Revisão independente e sensor de falhas aprovados, com nenhum dado real operado.
- [ ] Roteiro de teste humano disponível; não declarar UAT realizado sem resposta do usuário.

