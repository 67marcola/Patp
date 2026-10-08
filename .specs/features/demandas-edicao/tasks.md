# Edição de demandas Tasks

## Execution Protocol

Usar tlc-spec-driven explicitamente invocada. Usuário autorizou implementação e commits locais após AD-025/026 e respondeu pode continuar às duas adequações concretas de testes apresentadas. Oito tarefas, executar inline em ordem. Verifier fresco obrigatório após T8. Nenhum push/deploy/banco real. Atualizar diagnóstico como progresso parcial em cada tarefa e como concluído somente após Verifier PASS.

**Design:** design.md. **Status:** In Progress. Base cd62ee4; 479 Java, 661 Vitest, 7 Edge.

## Test Coverage Matrix

Guidelines: docs/OPERACAO.md, frontend/vitest.config.js/playwright.config.js, sistema/pom.xml. Strong defaults: todos os ACs e edges; amostras ApiIntegrationSupport, DemandaTransicaoServiceTests/ConcurrencyTests/ApiTests, apiTransicoes.test.js, EditorDemanda.test.jsx, QuadroTransicoes.test.jsx, e2e/transicoes.spec.js. Nenhuma alteração de tecnologia.

| Code Layer | Required Test Type | Coverage Expectation | Location Pattern | Run Command |
| --- | --- | --- | --- | --- |
| DTO | integration | Campos e JSON inválido definidos pela spec | sistema/src/test/java/**/DemandaEdicaoRequestTests.java | Maven test selecionado |
| Serviço | integration | Cada AC/edge, persistência, rollback e concorrência | sistema/src/test/java/**/DemandaEdicao*Tests.java | Maven test selecionado |
| API | integration | Sucesso, autorização, cada entrada inválida e erros | sistema/src/test/java/**/DemandaEdicaoApiTests.java | Maven verify |
| API frontend | unit | Resposta completa/campos/ciclo e erros | frontend/src/services/apiEdicao.test.js | npm.cmd test |
| Componentes | unit | Dados, validação, teclado, pending, rascunho e foco | frontend/src/pages/*Edicao.test.jsx | npm.cmd test |
| Navegador | e2e | Fluxo real, encerramento preservado, recarga, terceiro | frontend/e2e/edicao.spec.js | npm.cmd run test:e2e |

## Gate Check Commands

JDK 25.0.2 instalado, Maven 3.9.16; Mockito 5.23.0 javaagent explícito. H2 classpath de teste; não usar configuração main real. Logs e builds em TEMP registrado no handoff.

| Gate Level | When to Use | Command |
| --- | --- | --- |
| Quick | DTO/serviço/legada | mvn.cmd -B test -Dtest=classe -DargLine=-javaagent:...mockito-core-5.23.0.jar |
| Full | T4 e fim | mvn.cmd -B verify -DargLine=...; frontend npm.cmd test |
| Build | T7/T8 | npm.cmd run lint; npm.cmd run build -- --outDir TEMP/build; npm.cmd run test:e2e |

## Execution Plan

### Phase 1: Servidor

```text
T1 → T2 → T3 → T4
```

### Phase 2: Interface

```text
T5 → T6 → T7 → T8
```

## Task Breakdown

```text
T1 → T2 → T3 → T4 → T5 → T6 → T7 → T8
```

### T1: Definir entrada de edição
**What:** DTO estrito com campos informativos e versão.
**Where:** sistema/src/main/java/com/patp/sistema/dto/EditarDemandaRequest.java
**Depends on:** None
**Requirement:** EDI-09/12/13/27
**Tests:** integration, DemandaEdicaoRequestTests; parsing e rejeição de tipos/campos estranhos.
**Gate:** Quick
**Done when:** [x] DTO e 19 testes aprovados; sem numeroProcesso no contrato. T1 Complete; t1-2.log.

### T2: Implementar edição transacional
**What:** Função de edição dedicada com persistência e lock.
**Where:** sistema/src/main/java/com/patp/sistema/service/ProcessoService.java
**Depends on:** T1
**Requirement:** EDI-01–08/10/11/13–18/27–29
**Tests:** integration, serviço/DB, cada recusa/limite/rollback e disputas de commit.
**Gate:** Quick
**Done when:** [x] Campos/ciclo/evento/autor/versão provados, sem efeitos nos erros.

**Result:** T2 Complete. 32 testes H2 PASS, zero falhas/erros/skips; t2-2.log. Primeira compilação falhou por adaptação do helper Runnable, corrigida sem mudar assertions

### T3: Proteger edição legada
**What:** Adaptador da edição antiga ao mesmo núcleo.
**Where:** sistema/src/main/java/com/patp/sistema/service/ProcessoService.java
**Depends on:** T2
**Requirement:** EDI-19/27
**Tests:** integration, rota antiga e autorização/renumeração/ciclo; adequações antigas autorizadas.
**Gate:** Quick
**Done when:** [x] Rota antiga não contorna número, autorização ou ciclo e incrementa versão.

**Result:** T3 Complete. 92 testes H2 PASS, zero falhas/erros/skips; t3.log. Nove novos casos de compatibilidade, demais casos anteriores preservados

### T4: Expor API de edição
**What:** Endpoint dedicado PUT com DTO e snapshot.
**Where:** sistema/src/main/java/com/patp/sistema/controller/DemandaController.java
**Depends on:** T3
**Requirement:** EDI-01–19/27–29
**Tests:** integration, HTTP feliz/erros e ausência de efeitos persistidos.
**Gate:** Full
**Done when:** [x] API e suíte Java completa aprovadas, nenhum caso anterior removido.

**Result:** T4 Complete. 570 Java/H2 e 661 Vitest PASS, zero falhas/erros/skips; t4-java-2.log e t4-vitest.log. Primeiro gate 569/570 por autoria antiga em EtapaDemandConcurrencyTests: adequação autorizada mantém todas as assertions

### T5: Confirmar resposta de edição
**What:** Função da API frontend com confirmação estrita.
**Where:** frontend/src/services/api.js
**Depends on:** T4
**Requirement:** EDI-23/24/27/28
**Tests:** unit, apiEdicao.test.js, payload correto e resposta divergente/rede/HTTP.
**Gate:** Full
**Done when:** [x] Confirmação prova campos, ciclo e versão e preserva erros.

**Result:** T5 Complete. 695 Vitest PASS, 17 arquivos, nenhum skip; t5-vitest.log. Mais 34 casos, nenhuma regressão perdida

### T6: Acrescentar modo edição ao formulário
**What:** Formulário preenchido com número somente leitura e validação.
**Where:** frontend/src/pages/EditorDemanda.jsx
**Depends on:** T5
**Requirement:** EDI-05–09/20–23/27
**Tests:** unit, EditorDemandaEdicao.test.jsx; cadastro anterior preservado.
**Gate:** Full
**Done when:** [x] Preenchimento, limpeza, limites e pending/cancelamento provados.

**Result:** T6 Complete. 706 Vitest PASS, 18 arquivos, nenhum skip; t6-vitest.log. Onze novos casos e cadastro anterior preservado

### T7: Integrar edição aos cartões
**What:** Estado e ações do quadro para edição permitida.
**Where:** frontend/src/pages/Quadro.jsx
**Depends on:** T6
**Requirement:** EDI-20–29
**Tests:** unit, QuadroEdicao.test.jsx e casos antigos autorizados; erro/recarga manual/troca/foco/cache.
**Gate:** Build
**Done when:** [x] Vitest integral, lint/build e integração aprovados.

**Result:** T7 Complete. 721 Vitest PASS, 19 arquivos; lint/build TEMP PASS; t7-vitest.log, t7-lint.log e t7-build.log. Quinze novos casos; três expectativas antigas incluem Editar e preservam as transições

### T8: Provar edição real no navegador
**What:** Fluxos reais de edição/encerramento/recarga e permissão.
**Where:** frontend/e2e/edicao.spec.js
**Depends on:** T7
**Requirement:** EDI-02/05/10/15/16/20–28
**Tests:** e2e, persistência HTTP e histórico, desktop/mobile/teclado.
**Gate:** Build
**Done when:** [ ] E2E integral PASS; documentação operacional atualizada; disparar Verifier independente.

## Task Granularity Check

Cada tarefa altera um DTO, uma função, um endpoint ou um componente/fluxo. Testes correspondentes e status são incluídos no mesmo commit. T2 inclui as provas de concorrência da própria função; T8 não adia testes de serviço.

## Diagram-Definition Cross-Check

| Task | Depends On | Diagram Shows | Status |
| --- | --- | --- | --- |
| T1 | None | None | Match |
| T2 | T1 | T1 | Match |
| T3 | T2 | T2 | Match |
| T4 | T3 | T3 | Match |
| T5 | T4 | Phase 1 completa | Match |
| T6 | T5 | T5 | Match |
| T7 | T6 | T6 | Match |
| T8 | T7 | T7 | Match |

## Test Co-location Validation

| Task | Layer | Matrix Requires | Task Says | Status |
| --- | --- | --- | --- | --- |
| T1 | DTO | integration | integration | Match |
| T2 | Serviço | integration | integration | Match |
| T3 | Legada | integration | integration | Match |
| T4 | API | integration | integration | Match |
| T5 | API frontend | unit | unit | Match |
| T6 | Formulário | unit | unit | Match |
| T7 | Quadro | unit | unit | Match |
| T8 | Navegador | e2e | e2e | Match |
