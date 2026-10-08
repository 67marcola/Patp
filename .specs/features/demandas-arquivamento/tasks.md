# Arquivamento de demandas Tasks

## Execution Protocol

Skill tlc-spec-driven invocada. **Status:** execução autorizada pelo “Pode continuar” após apresentação da proposta, dois lotes sequenciais e adequações concretas. Base3f9d78a;570Java/H2,721Vitest,9Edge. Doze tarefas atômicas, fases8+4. Servidor termina antes de interface; Verifier fresco obrigatório no final. Sem push/deploy/banco real. Registrar diagnóstico parcial por tarefa, conclusão só após PASS independente. Cada tarefa inclui testes/gate/commit próprio. Ferramentas locais arquivos/shell/Git e TLC conforme preferências existentes; nenhuma ferramenta externa.

**Design:** design.md.

## Test Coverage Matrix

Guidelines: docs/OPERACAO.md, sistema/pom.xml, frontend/package.json, vitest.config.js e playwright.config.js. Samples: ApiIntegrationSupport, DemandaEdicaoServiceTests/ConcurrencyTests/ApiTests, ProcessoArchiveTests, HistoricoArchiveTests, apiEdicao.test.js, QuadroEdicao.test.jsx, AcaoDemanda.test.jsx e e2e/edicao.spec.js. Nenhuma lesson confirmada. Strong defaults: cada AC/edge com outcomes exatos, testes co-localizados.

| Code Layer | Required Test Type | Coverage Expectation | Location Pattern | Run Command |
| --- | --- | --- | --- | --- |
| Modelo/DTO | integration ou unit | Preservação, null legado e JSON exato | sistema/src/test/java/**/DemandaArquivo*Tests.java | mvn.cmd test selecionado |
| Serviço/consulta/guards | integration | Cada AC/borda, rollback/lock/zero efeitos | sistema/src/test/java/**/DemandaArquivo*Tests.java | mvn.cmd test/verify |
| API | integration | Happy/edge/erro de cada rota | sistema/src/test/java/**/DemandaArquivo*ApiTests.java | mvn.cmd verify |
| API frontend/componentes | unit | Resultado por valor e foco/pending/erro/conflito | frontend/src/**/*.test.js ou .test.jsx | npm.cmd test |
| Navegador | e2e | Estados/recarga/ciclo/autoria/permissão | frontend/e2e/arquivamento.spec.js | npm.cmd run test:e2e |

## Gate Check Commands

| Gate Level | When to Use | Command |
| --- | --- | --- |
| Quick | Por tarefa | Maven -B test -Dtest=classe -DargLine=-javaagent:Mockito5.23.0.jar; npm.cmd test -- arquivo |
| Full | Final backend/UI | Maven -B verify -DargLine=...; npm.cmd test |
| Build | Integração/fim | npm.cmd run lint; npm.cmd run build -- --outDir TEMP/build; npm.cmd run test:e2e; selecionados MySQL fictício |

JDK C:/Program Files/Java/jdk-25.0.2; Maven C:/Users/Marco/apache-maven-3.9.16/bin/mvn.cmd; javaagent C:/Users/Marco/.m2/repository/org/mockito/mockito-core/5.23.0/mockito-core-5.23.0.jar. H2 somente classpath teste/create-drop/server.port0. MySQL somente nova instância TEMP com datadir/porta conferidos; nunca datasource main. Um Maven por vez e testes de servidor/interface sequenciais para evitar timeout de CPU. Registrar contagem efetiva e logs sem excluir/ignorar casos.

## Execution Plan

### Phase 1: Servidor

```text
T1 → T2 → T3 → T4 → T5 → T6 → T7 → T8
```

### Phase 2: Interface e fluxo real

```text
T9 → T10 → T11 → T12
```

## Task Breakdown

```text
T1 → T2 → T3 → T4 → T5 → T6 → T7 → T8 → T9 → T10 → T11 → T12
```

### T1: Persistir marcador de arquivo

**What:** Boolean separado de status, null legado=false e JSON somente leitura.
**Where:** `sistema/src/main/java/com/patp/sistema/model/Processo.java`
**Depends on:** None
**Reuses:** padrões citados no design.
**Requirement:** ARQ-18
**Tools:** arquivos/shell/Git; Skill tlc-spec-driven.

**Done when:**

- [x] DemandaArquivoModeloTests: true/false/null, preservação e payload legado não controla flag.
- [x] Gate Quick PASS, contagem registrada e nenhum skip/remoção não autorizado.
- [x] Atualizar traceabilidade/evidências/diagnóstico parcial e commit atômico.

**Tests:** integration
**Gate:** Quick
**Commit:** feat(demandas): persistir marcador de arquivo

### T2: Estender snapshot

**What:** Adicionar demandasArquivadas e compatibilidade do construtor.
**Where:** `sistema/src/main/java/com/patp/sistema/dto/ConfiguracaoEtapasResponse.java`
**Depends on:** T1
**Reuses:** padrões citados no design.
**Requirement:** ARQ-19
**Tools:** arquivos/shell/Git; Skill tlc-spec-driven.

**Done when:**

- [x] ConfiguracaoArquivoResponseTests: duas listas, 14campos por demanda, vazio compatível.
- [x] Gate Quick PASS, contagem registrada e nenhum skip/remoção não autorizado.
- [x] Atualizar traceabilidade/evidências/diagnóstico parcial e commit atômico.

**Tests:** unit
**Gate:** Quick
**Commit:** feat(demandas): estender snapshot

### T3: Separar arquivo nas leituras

**What:** Particionar snapshot e contar só ativas; proteger etapas com arquivo.
**Where:** `sistema/src/main/java/com/patp/sistema/service/EtapaService.java`
**Depends on:** T2
**Reuses:** padrões citados no design.
**Requirement:** ARQ-19, ARQ-20, ARQ-21
**Tools:** arquivos/shell/Git; Skill tlc-spec-driven.

**Done when:**

- [x] DemandaArquivoLeituraTests: nulo/false/true, listas/contagens, quadro arquivado, mutação de etapas e remoção bloqueada.
- [x] Gate Quick PASS, contagem registrada e nenhum skip/remoção não autorizado.
- [x] Atualizar traceabilidade/evidências/diagnóstico parcial e commit atômico.

**Tests:** integration
**Gate:** Quick
**Commit:** feat(demandas): separar arquivo nas leituras

### T4: Implementar arquivar/restaurar

**What:** Núcleo transacional com guard/versão/evento; proteger edição/transições/DELETE antigos.
**Where:** `sistema/src/main/java/com/patp/sistema/service/ProcessoService.java`
**Depends on:** T3
**Reuses:** padrões citados no design.
**Requirement:** ARQ-01, ARQ-02, ARQ-03, ARQ-04, ARQ-05, ARQ-06, ARQ-07, ARQ-08, ARQ-09, ARQ-11, ARQ-12, ARQ-13, ARQ-14, ARQ-15, ARQ-16, ARQ-17, ARQ-18, ARQ-22, ARQ-23
**Tools:** arquivos/shell/Git; Skill tlc-spec-driven.

**Done when:**

- [x] DemandaArquivoServiceTests/ConcurrencyTests/LegadaTests: status×criador/admin, recusas sem efeito, rollback nas três escritas, disputas e bloqueios antigos.
- [x] Gate Quick PASS, contagem registrada e nenhum skip/remoção não autorizado.
- [x] Atualizar traceabilidade/evidências/diagnóstico parcial e commit atômico.

**Tests:** integration
**Gate:** Quick
**Commit:** feat(demandas): implementar arquivar/restaurar

### T5: Proteger comentários

**What:** Recusar nova escrita no arquivo sob lock, consulta intacta.
**Where:** `sistema/src/main/java/com/patp/sistema/service/ComentarioService.java`
**Depends on:** T4
**Reuses:** padrões citados no design.
**Requirement:** ARQ-16, ARQ-17, ARQ-22
**Tools:** arquivos/shell/Git; Skill tlc-spec-driven.

**Done when:**

- [x] DemandaArquivoComentarioTests: ativo aceito, arquivo409, disputa nas duas ordens e GET preservado.
- [x] Gate Quick PASS, contagem registrada e nenhum skip/remoção não autorizado.
- [x] Atualizar traceabilidade/evidências/diagnóstico parcial e commit atômico.

**Tests:** integration
**Gate:** Quick
**Commit:** feat(demandas): proteger comentários

### T6: Separar história externa

**What:** registrarExterno protegido; manter registrar interno para eventos administrativos.
**Where:** `sistema/src/main/java/com/patp/sistema/service/HistoricoService.java`
**Depends on:** T5
**Reuses:** padrões citados no design.
**Requirement:** ARQ-14, ARQ-16, ARQ-17, ARQ-22
**Tools:** arquivos/shell/Git; Skill tlc-spec-driven.

**Done when:**

- [ ] DemandaArquivoHistoricoTests: externo ativo/arquivo409, nome forjado sem bypass, interno aceito e histórico anterior literal.
- [ ] Gate Quick PASS, contagem registrada e nenhum skip/remoção não autorizado.
- [ ] Atualizar traceabilidade/evidências/diagnóstico parcial e commit atômico.

**Tests:** integration
**Gate:** Quick
**Commit:** feat(demandas): separar história externa

### T7: Proteger endpoint de histórico

**What:** POST usa registrarExterno, GET preservado.
**Where:** `sistema/src/main/java/com/patp/sistema/controller/HistoricoController.java`
**Depends on:** T6
**Reuses:** padrões citados no design.
**Requirement:** ARQ-22
**Tools:** arquivos/shell/Git; Skill tlc-spec-driven.

**Done when:**

- [ ] DemandaArquivoHistoricoApiTests: POST arquivo409/zero efeitos, GET completo e POST ativo compatível.
- [ ] Gate Quick PASS, contagem registrada e nenhum skip/remoção não autorizado.
- [ ] Atualizar traceabilidade/evidências/diagnóstico parcial e commit atômico.

**Tests:** integration
**Gate:** Quick
**Commit:** feat(demandas): proteger endpoint de histórico

### T8: Expor ações dedicadas

**What:** PUT arquivar/restaurar com versão e200snapshot.
**Where:** `sistema/src/main/java/com/patp/sistema/controller/DemandaController.java`
**Depends on:** T7
**Reuses:** padrões citados no design.
**Requirement:** ARQ-01, ARQ-02, ARQ-03, ARQ-07, ARQ-08, ARQ-09, ARQ-10, ARQ-11, ARQ-12, ARQ-13, ARQ-14
**Tools:** arquivos/shell/Git; Skill tlc-spec-driven.

**Done when:**

- [ ] DemandaArquivoApiTests: duas rotas/três estados, todos HTTP/JSON inválidos exatos sem efeitos; Java completo.
- [ ] Gate Full PASS, contagem registrada e nenhum skip/remoção não autorizado.
- [ ] Atualizar traceabilidade/evidências/diagnóstico parcial e commit atômico.

**Tests:** integration
**Gate:** Full
**Commit:** feat(demandas): expor ações dedicadas

### T9: Confirmar API frontend

**What:** Cliente e validação das listas/14campos/contagens/versão antes de200.
**Where:** `frontend/src/services/api.js`
**Depends on:** T8
**Reuses:** padrões citados no design.
**Requirement:** ARQ-19, ARQ-20, ARQ-28, ARQ-29
**Tools:** arquivos/shell/Git; Skill tlc-spec-driven.

**Done when:**

- [ ] apiArquivo.test.js: ações, cada divergência de campo/lista/ID/contagem/permissão/versão, HTTP/rede/201/204.
- [ ] Gate Quick PASS, contagem registrada e nenhum skip/remoção não autorizado.
- [ ] Atualizar traceabilidade/evidências/diagnóstico parcial e commit atômico.

**Tests:** unit
**Gate:** Quick
**Commit:** feat(demandas): confirmar api frontend

### T10: Criar confirmação

**What:** Diálogo acessível, foco/cancelar/pending/erro.
**Where:** `frontend/src/pages/AcaoArquivamentoDemanda.jsx`
**Depends on:** T9
**Reuses:** padrões citados no design.
**Requirement:** ARQ-26, ARQ-27, ARQ-29
**Tools:** arquivos/shell/Git; Skill tlc-spec-driven.

**Done when:**

- [ ] AcaoArquivamentoDemanda.test.jsx: efeitos/etapa/estado, teclado/cancelar, duplicate/pending e erro sem fechar.
- [ ] Gate Quick PASS, contagem registrada e nenhum skip/remoção não autorizado.
- [ ] Atualizar traceabilidade/evidências/diagnóstico parcial e commit atômico.

**Tests:** unit
**Gate:** Quick
**Commit:** feat(demandas): criar confirmação

### T11: Integrar arquivo ao quadro

**What:** Botões, área Arquivadas, snapshot e proteção de respostas tardias.
**Where:** `frontend/src/pages/Quadro.jsx`
**Depends on:** T10
**Reuses:** padrões citados no design.
**Requirement:** ARQ-24, ARQ-25, ARQ-26, ARQ-27, ARQ-28, ARQ-29, ARQ-30
**Tools:** arquivos/shell/Git; Skill tlc-spec-driven.

**Done when:**

- [ ] QuadroArquivo.test.jsx: estados/permissões, arquivo/restauração, foco/cache/contagem, erroGETmanual, pending/troca; Vitest completo/lint/build.
- [ ] Gate Full PASS, contagem registrada e nenhum skip/remoção não autorizado.
- [ ] Atualizar traceabilidade/evidências/diagnóstico parcial e commit atômico.

**Tests:** unit
**Gate:** Full
**Commit:** feat(demandas): integrar arquivo ao quadro

### T12: Provar fluxo real

**What:** Arquivo/restauração nos três estados, recarga/autoria/conflito/permissão/teclado/mobile.
**Where:** `frontend/e2e/arquivamento.spec.js`
**Depends on:** T11
**Reuses:** padrões citados no design.
**Requirement:** ARQ-05, ARQ-06, ARQ-13, ARQ-14, ARQ-17, ARQ-19, ARQ-20, ARQ-21, ARQ-23, ARQ-24, ARQ-25, ARQ-26, ARQ-28, ARQ-29
**Tools:** arquivos/shell/Git; Skill tlc-spec-driven.

**Done when:**

- [ ] Edge/H2: comparar campos/ciclo/eventos/contagens; repetir Java/Vitest/Edge e MySQL fictício selecionado; documentar operação/UAT.
- [ ] Gate Build PASS, contagem registrada e nenhum skip/remoção não autorizado.
- [ ] Atualizar traceabilidade/evidências/diagnóstico parcial e commit atômico.

**Tests:** e2e
**Gate:** Build
**Commit:** feat(demandas): provar fluxo real

## Diagram-Definition Cross-Check

| Task | Depends On | Diagram Shows | Status |
| --- | --- | --- | --- |
| T1 | None | None | PASS |
| T2 | T1 | T1 | PASS |
| T3 | T2 | T2 | PASS |
| T4 | T3 | T3 | PASS |
| T5 | T4 | T4 | PASS |
| T6 | T5 | T5 | PASS |
| T7 | T6 | T6 | PASS |
| T8 | T7 | T7 | PASS |
| T9 | T8 | T8, fase anterior concluída | PASS |
| T10 | T9 | T9 | PASS |
| T11 | T10 | T10 | PASS |
| T12 | T11 | T11 | PASS |

## Test Co-location Validation

| Task | Code Layer | Matrix Requires | Task Says | Status |
| --- | --- | --- | --- | --- |
| T1 | Servidor | integration | integration | PASS |
| T2 | Servidor | unit | unit | PASS |
| T3 | Servidor | integration | integration | PASS |
| T4 | Servidor | integration | integration | PASS |
| T5 | Servidor | integration | integration | PASS |
| T6 | Servidor | integration | integration | PASS |
| T7 | Servidor | integration | integration | PASS |
| T8 | Servidor | integration | integration | PASS |
| T9 | Frontend | unit | unit | PASS |
| T10 | Frontend | unit | unit | PASS |
| T11 | Frontend | unit | unit | PASS |
| T12 | Navegador | e2e | e2e | PASS |

## Task Granularity Check

Um arquivo/componente primário por tarefa e seus testes no mesmo commit. Sem teste adiado; T12 acrescenta fluxo real, não substitui provas anteriores. Documentos/evidências acompanham cada commit apropriado.

## Adequações antigas autorizadas em 08/10/2026

- ProcessoArchiveTests.java:128: DELETE ativo passa de500 acidental para409 explícito e mensagem de usar arquivo. Manter snapshot sem efeitos e acrescentar versão inalterada.
- QuadroTransicoes.test.jsx:44: três listas de botões incluem Arquivar ao fim; manter Editar e todas as ações de ciclo anteriores. Casos de usuário/quadro/status desconhecido preservados.
- Outras alterações antigas somente após necessidade concreta; não excluir/ignorar/enfraquecer testes.
