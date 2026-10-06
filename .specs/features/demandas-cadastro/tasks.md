# Cadastro e leitura de demandas Tasks

## Execution Protocol

Usar tlc-spec-driven escolhido pelo usuário; autorização da sessão e respostas1A/2A cobrem implementação/testes/commits locais. Uma tarefa, gate, adequação e commit por vez. Verificador fresco obrigatório depois de T6. Skills/ferramentas locais já autorizadas; nenhuma nova pergunta técnica de aprovação. Não operar MySQL configurado/contas reais/push/deploy. Preservar target/dist/node_modules e scratchs recusadas.

**Design:** design.md. **Status:** pronto para execução; seis tarefas em sequência. Base28b8c88; baseline278 Java,235Vitest,3E2E.

## Test Coverage Matrix

Gerada de docs/OPERACAO.md, pom.xml, package.json/vite/playwright e testes existentes. Sem AGENTS/CONTRIBUTING ou limiar adicional encontrado. Amostras: ProcessoArchiveTests/HistoricoArchiveTests/EtapaDemandConcurrencyTests/EtapaFinaisApiTests/QuadroEtapas.test/API.test/EditorEtapa e três E2E. Defaults fortes: todosACs/edges do corte, sem reduzir profundidade/regressão.

| Code Layer | Required Test Type | Coverage Expectation | Location Pattern | Run Command |
| --- | --- | --- | --- | --- |
| DTO/snapshot/serviço/HTTP | integration | Valores completos, filtros/auth/read sem escrita, cadaAC e edges | sistema/src/test/java/com/patp/sistema/*Tests.java | mvn.cmd -B verify |
| Cadastro/persistência/concurrency | integration | Todoscampos, erro/state, dedup, lock,rollback,versão e histórico | mesmoJava/H2, repetirMySQL TEMP | mvn.cmd -B verify |
| API/Form/Quadro | unit | Payload/resultado, cadaAC/edge, erro/pending/cache/keyboard/foco | frontend/src/**/*.test.* | npm.cmd test |
| Integração real | e2e | Novo fluxo mínimo/completo/duplicate/reload/archive,3 anteriores preservados | frontend/e2e/*.spec.js | npm.cmd run test:e2e |

## Gate Check Commands

| Gate Level | When to Use | Command |
| --- | --- | --- |
| Quick | T3/T4/T5 | npm.cmd test (suíte completa) |
| Full | T1/T2 | sistema mvn.cmd -B verify, H2, JAVA_HOME C:/Program Files/Java/jdk-25.0.2 e Maven3.9.16PATH; sem Maven concorrente |
| Build | T6 | npm.cmd test; build outDir TEMP novo; npm.cmd run lint; npm.cmd run test:e2e (4Edge/H2); MySQL TEMP selecionado/documentado |

## Execution Plan

### Phase 1: Cadastro e consulta integrada

```text
T1 → T2 → T3 → T4 → T5 → T6
```

## Task Breakdown

### T1: Snapshot com demandas persistidas

**What:** ampliar a composição existente com DTO flat e contagens da mesma lista sob lock.
**Where:** sistema/src/main/java/com/patp/sistema/service/EtapaService.java
**Depends on:** None
**Reuses:** ConfiguracaoEtapasResponse, ProcessoRepository/lock/ordenação existentes.
**Requirement:** CAD-21/22/23/24/25
**Related files:** dto/DemandaResponse.java, dto/ConfiguracaoEtapasResponse.java, DemandaConsultaTests.java; testes existentes de snapshot se necessário, somente contrato de leitura ampliado.
**Tests:** integration
**Gate:** Full
**Commit:** feat(demandas): include persisted cards in board snapshots
**Done when:**
- [x] DTO14campos/listavazia/filtro/ordem/counts/arquivo/legados semwrite e snapshot pósCRUD/concurrency comprovados; baseline278 preservada, gate/adequação PASS. Gate284/284 H2; evidências e dois mapas em evidence.md T1.

### T2: Cadastro automático na primeira etapa

**What:** criar endpoint tipado e proteger criação legada, com validação/transação/duplicidade.
**Where:** sistema/src/main/java/com/patp/sistema/service/ProcessoService.java
**Depends on:** T1
**Reuses:** guard/sessão/primeirotrabalho/histórico/snapshot/transação/versão existentes.
**Requirement:** CAD-01–20
**Related files:** controller/DemandaController.java, dto/CriarDemandaRequest.java, dto/DataDemandaDeserializer.java, ProcessoRepository.java, GerenciamentoRepository.java/GerenciamentoGuard.java para lock global de cadastro; DemandaCadastroTests.java/DemandaCadastroConcurrencyTests.java; ProcessoArchiveTests/HistoricoArchiveTests/EtapaDemandConcurrencyTests para preservação e fixtureprimeiraetapaautorizada; design/context/spec/evidence para correção do helper inexistente, APIs/datas estritas e decisão de serialização global sem schema novo.
**Tests:** integration
**Gate:** Full
**Commit:** feat(demandas): create validated demands in the first work stage
**Done when:**
- [x] Novo201 e legado200 cumpremcampos/defaults/auth/arquivo/primeiratrabalho; versionnovo+1/legadosame; duplicidade409globalrace e rollback real demanda/histórico; todosACs01–20 mapeados, gate/adequação PASS sem enfraquecer ações fora do corte. Gate390/390 H2,106casosT2; dois mapas e handoffMySQL em evidence.md T2.

### T3: Contrato frontend do snapshot e POST

**What:** adicionar criarDemanda e validar snapshots consumidos por GET/mutações.
**Where:** frontend/src/services/api.js
**Depends on:** T2
**Reuses:** requisicao/ApiError/Auth/AbortSignal existentes.
**Requirement:** CAD-30/33/36
**Related files:** services/api.test.js e fixtures existentes de Quadro.test.jsx, QuadroEtapas.test.jsx, App.test.jsx, Gerenciamentos.test.jsx, somente acrescentar demandas válidas/coerentes sem alterar cenários/assertions anteriores.
**Tests:** unit
**Gate:** Quick
**Commit:** feat(api): validate demand snapshots and creation responses
**Done when:**
- [x] Payload/rota/Bearer e snapshot íntegro/inseguro/204/abort/errors/no-retry comprovados; fixtures testonly, todos235 preservados, gate/adequação PASS. Gate377/377 Vitest,142casos novos; dois mapas em evidence.md T3.

### T4: Formulário de criação de demanda

**What:** implementar EditorDemanda com campos/validação/pendência/foco/rascunho.
**Where:** frontend/src/pages/EditorDemanda.jsx
**Depends on:** T3
**Reuses:** padrão EditorEtapa e classes CSS de formulário.
**Requirement:** CAD-02/03/04/10/28/29/30/31/33/40
**Related files:** EditorDemanda.test.jsx, index.css apenas se indispensável ao formulário.
**Tests:** unit
**Gate:** Quick
**Commit:** feat(ui): add the demand creation form
**Done when:**
- [x] Oito campos/required/limites/opcionaisnull/payload/erro/pending/foco/cancelar comprovados; sem camposcontroladospelo sistema; gate/adequação PASS. Gate406/406 Vitest,29casos novos; dois mapas em evidence.md T4.

### T5: Cartões e cadastro no quadro

**What:** conectar formulário e artigos reais por etapa, com snapshot/cache/permissão/read-only.
**Where:** frontend/src/pages/Quadro.jsx
**Depends on:** T4
**Reuses:** ciclooperando/aoOcupar/foco/Abort/aplicarConfiguracao existentes.
**Requirement:** CAD-01/07/08/24/26/27/30/31/32/33/34/35/37/38/39/40
**Related files:** QuadroDemandas.test.jsx, QuadroEtapas.test.jsx/Quadro.test.jsx/App.test.jsx se necessários; index.css para cards/datas/mobile.
**Tests:** unit
**Gate:** Quick
**Commit:** feat(ui): show demand cards and connect creation in boards
**Done when:**
- [x] Qualquerauthcria, zeroWorksorienta, successcache/cards/counts/foco, errorrefreshrascunho/archive/trocadequadro, leitura14campos/escape/ausências e controlesanteriores preservados; gate/adequação PASS. Gate440/440 Vitest,34casos novos; dois mapas em evidence.md T5.

### T6: Fluxo real e roteiro operacional

**What:** provar criação/leitura completa no navegador e documentar uso/evidência MySQL fictícia.
**Where:** frontend/e2e/demandas.spec.js
**Depends on:** T5
**Reuses:** helperH2/Edge e três E2E existentes, zero retries.
**Requirement:** CAD-01/05/13/21/22/26/28/30/31/32/37/39/40/41
**Related files:** package.json se listaE2Eexplícita exige adicionararquivo, docs/OPERACAO.md, evidence.md/spec/tasks; capturas em TEMP. Nenhumnovo JavaforaT2.
**Tests:** e2e
**Gate:** Build
**Commit:** test(demandas): verify creation and persisted cards in the browser
**Done when:**
- [ ] QuatroEdge/H2PASS mínimo/completo/dedupentrequadros/reload/arquivo/keyboard, fullVitest/buildTEMP/lint e MySQLTEMP documentados; QAvisualdesktop/mobile, adequação PASS; despachar Verificador fresco apóscommit.

## Diagram-Definition Cross-Check

| Task | Depends On | Diagram Shows | Status |
| --- | --- | --- | --- |
| T1 | None | None | Match |
| T2 | T1 | T1 | Match |
| T3 | T2 | T2 | Match |
| T4 | T3 | T3 | Match |
| T5 | T4 | T4 | Match |
| T6 | T5 | T5 | Match |

## Test Co-location Validation

| Task | Layer | Matrix Requires | Task Says | Status |
| --- | --- | --- | --- | --- |
| T1 | Snapshot/DTO/HTTP | integration | integration | Match |
| T2 | Cadastro/HTTP/concurrency | integration | integration | Match |
| T3 | API frontend | unit | unit | Match |
| T4 | Form React | unit | unit | Match |
| T5 | Quadro React | unit | unit | Match |
| T6 | Integração real | e2e | e2e | Match |

Cada tarefa tem um deliverable principal, testes e gate co-localizados. Seis tarefas, fontes/gates/commits sequenciais; auditorias somente leitura independentes podem ocorrer em paralelo.
