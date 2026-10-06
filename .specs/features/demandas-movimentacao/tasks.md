# Movimentação de demandas Tasks

## Execution Protocol

Usar tlc-spec-driven já escolhido. Autorização persistente cobre implementação, testes e commits locais, uma tarefa por vez. Não push/deploy/MySQL configurado. Até7tarefas: executar inline. Depois da última, Verifier fresco obrigatório, sensor em memória e comparação integral de porcelain antes/depois. Nenhum teste será enfraquecido, excluído ou ignorado; adaptar comportamentos antigos contraditos pelas decisões explícitas mantendo a prova de integridade.

**Design:** design.md. **Status:** In Progress. Base400f8c5,390Java/475Vitest/4E2E.

## Test Coverage Matrix

Gerada de docs/OPERACAO.md, sistema/pom.xml, frontend/package.json/vitest/playwright e amostras ApiIntegrationSupport, ProcessoArchiveTests, HistoricoArchiveTests, EtapaDemandConcurrencyTests, DemandaCadastroTests, api.test, QuadroDemandas.test, EditorDemanda.test e E2E. Sem AGENTS/limiar adicional. Defaults fortes: todosACs/edges, afirmações de valores completos e reversão integral.

| Code Layer | Required Test Type | Coverage Expectation | Location Pattern | Run Command |
| --- | --- | --- | --- | --- |
| Modelo/serviço/rotas/persistência | integration | TodosACs backend, auth, estados, versão, longos, rollback | sistema/src/test/java/com/patp/sistema/*Tests.java | mvn.cmd -B verify |
| API/dialog/Quadro | unit | Payload e resultado completos, erro/pending/foco/cache, cadaAC | frontend/src/**/*.test.* | npm.cmd test |
| Integração navegador/concorrência | e2e | Fluxos e erros, sessão externa, recarga e regressão | frontend/e2e/*.spec.js e JavaConcurrencyTests | npm.cmd run test:e2e; mvn.cmd -B verify |

## Gate Check Commands

| Gate Level | When to Use | Command |
| --- | --- | --- |
| Quick | T4–T6 | frontend npm.cmd test |
| Full | T1–T3 | sistema mvn.cmd -B verify, H2 isolado, Mockito javaagent explícito/JDK25; um Maven por vez |
| Build | T7 | mvn.cmd -B verify; npm.cmd test; npm.cmd run lint; npm.cmd run build -- --outDir TEMP novo; npm.cmd run test:e2e; backend selecionado MySQL TEMP fictício |

## Execution Plan

### Phase 1: Implementação sequencial

```text
T1 → T2 → T3 → T4 → T5 → T6 → T7
```

## Task Breakdown

### T1: Histórico com descrição integral

**What:** LONGTEXT e operação documentada, sem truncar motivos antigos.
**Where:** sistema/src/main/java/com/patp/sistema/model/Historico.java
**Depends on:** None
**Reuses:** padrões e componentes identificados em design.md.
**Requirement:** MOV-16/19
**Related files:** HistoricoDescricaoTests.java, docs/OPERACAO.md
**Tools:** filesystem/terminal; skill tlc-spec-driven; ferramentas locais autorizadas na sessão.
**Tests:** integration
**Gate:** Full
**Done when:**
- [x] LONGTEXT preserva descrição completa e payload em2casos; gate392Java/H2 PASS, mapas em evidence.md T1. Nenhuma mudança no banco real. MOV-16/19 têm capacidade entregue; regras de reabertura/eventos continuam T2/T3.

### T2: Serviço único de transição

**What:** Permissões, estados, destinos, histórico, versão, rollback e guard da edição genérica.
**Where:** sistema/src/main/java/com/patp/sistema/service/ProcessoService.java
**Depends on:** T1
**Reuses:** padrões e componentes identificados em design.md.
**Requirement:** MOV-01–21/24/26/27/35
**Related files:** DemandaTransicaoServiceTests.java, ProcessoArchiveTests.java, HistoricoArchiveTests.java, EtapaDemandConcurrencyTests.java
**Tools:** filesystem/terminal; skill tlc-spec-driven; ferramentas locais autorizadas na sessão.
**Tests:** integration
**Gate:** Full
**Done when:**
- [x] Serviço compartilhado e guard da edição implementados;40casos novos,432Java/H2 PASS; adequação e regressões traceadas em evidence.md T2. Rotas tipadas/DTOs e suas provas HTTP sãoT3; corridas adicionaisT7.

### T3: API tipada das ações

**What:** Quatro PUTs com DTOs estritos, versão e snapshot por quadro.
**Where:** sistema/src/main/java/com/patp/sistema/controller/DemandaController.java
**Depends on:** T2
**Reuses:** padrões e componentes identificados em design.md.
**Requirement:** MOV-01–27/35
**Related files:** dto/TransicaoDestinoRequest.java, dto/TransicaoVersaoRequest.java, dto/TransicaoCancelarRequest.java, DemandaTransicaoApiTests.java
**Tools:** filesystem/terminal; skill tlc-spec-driven; ferramentas locais autorizadas na sessão.
**Tests:** integration
**Gate:** Full
**Done when:**
- [x] QuatroPUTs e3DTOs estritos,27casosHTTP novos; gate459Java/H2 PASS. Payload completo, auth/estado/versão/legados e erros sem efeitos mapeados em evidence.md T3.

### T4: Confirmação de transição na API frontend

**What:** Enviar ações e confirmar resultado correlacionado sem alterar confirmação de cadastro.
**Where:** frontend/src/services/api.js
**Depends on:** T3
**Reuses:** padrões e componentes identificados em design.md.
**Requirement:** MOV-21/29/30/32/33
**Related files:** apiTransicoes.test.js; preservar api.test.js e confirmação do cadastro.
**Tools:** filesystem/terminal; skill tlc-spec-driven; ferramentas locais autorizadas na sessão.
**Tests:** unit
**Gate:** Quick
**Done when:**
- [x] Confirmação correlacionada das4ações,127casos novos; gate602Vitest PASS,475anteriores preservados, mapas em evidence.md T4. Datas ISO do servidor não presumem fuso/relógio do navegador.

### T5: Diálogo de ação sobre demanda

**What:** Seleção de trabalho, conclusão automática, cancelamento com motivo, foco e rascunho.
**Where:** frontend/src/pages/AcaoDemanda.jsx
**Depends on:** T4
**Reuses:** padrões e componentes identificados em design.md.
**Requirement:** MOV-29–32
**Related files:** AcaoDemanda.test.jsx
**Tools:** filesystem/terminal; skill tlc-spec-driven; ferramentas locais autorizadas na sessão.
**Tests:** unit
**Gate:** Quick
**Done when:**
- [ ] Critérios mapeados têm assertions de valores e estado em file:line, gate verde sem perdas de testes, mapas AC→assertion e assertion→AC em evidence.md, task e trace atualizados antes do commit local.

### T6: Ações e cache no quadro

**What:** Conectar ações por permissão/estado ao busy, refresh, snapshots e foco.
**Where:** frontend/src/pages/Quadro.jsx
**Depends on:** T5
**Reuses:** padrões e componentes identificados em design.md.
**Requirement:** MOV-28–35
**Related files:** QuadroTransicoes.test.jsx e ajustes de fixtures mantendo assertions existentes
**Tools:** filesystem/terminal; skill tlc-spec-driven; ferramentas locais autorizadas na sessão.
**Tests:** unit
**Gate:** Quick
**Done when:**
- [ ] Critérios mapeados têm assertions de valores e estado em file:line, gate verde sem perdas de testes, mapas AC→assertion e assertion→AC em evidence.md, task e trace atualizados antes do commit local.

### T7: Integração e operação das transições

**What:** Fluxo navegador, concorrência backend, MySQL fictício e roteiro humano.
**Where:** frontend/e2e/transicoes.spec.js
**Depends on:** T6
**Reuses:** padrões e componentes identificados em design.md.
**Requirement:** MOV-05/10/11/15/16/24/28–34
**Related files:** DemandaTransicaoConcurrencyTests.java, docs/OPERACAO.md
**Tools:** filesystem/terminal; skill tlc-spec-driven; ferramentas locais autorizadas na sessão.
**Tests:** e2e
**Gate:** Build
**Done when:**
- [ ] Critérios mapeados têm assertions de valores e estado em file:line, gate verde sem perdas de testes, mapas AC→assertion e assertion→AC em evidence.md, task e trace atualizados antes do commit local.

## Diagram-Definition Cross-Check

| Task | Depends On | Diagram Shows | Status |
| --- | --- | --- | --- |
| T1 | None | None | Match |
| T2 | T1 | T1 | Match |
| T3 | T2 | T2 | Match |
| T4 | T3 | T3 | Match |
| T5 | T4 | T4 | Match |
| T6 | T5 | T5 | Match |
| T7 | T6 | T6 | Match |

## Test Co-location Validation

| Task | Code Layer | Matrix Requires | Task Says | Status |
| --- | --- | --- | --- | --- |
| T1 | backend | integration | integration | OK |
| T2 | backend | integration | integration | OK |
| T3 | backend | integration | integration | OK |
| T4 | frontend | unit | unit | OK |
| T5 | frontend | unit | unit | OK |
| T6 | frontend | unit | unit | OK |
| T7 | integração | e2e | e2e | OK |

