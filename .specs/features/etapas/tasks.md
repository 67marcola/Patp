# CRUD de etapas de trabalho: tarefas

## Execution Protocol

Usuário autorizou continuar a implementação, com AD-007–010 confirmadas. Executar uma tarefa por vez: testes derivados de ETA, gate, adequação direta/reversa com arquivo:linha e assertion, status e um Conventional Commit local. Sem push/deploy/MySQL configurado. Nenhuma fase seguinte começa antes do gate/commit anterior. Delegação técnica autorizada pelas instruções da sessão; não solicitar confirmação de rotina.

**Design:** `design.md`. **Status:** T1–T4/T6 concluídas com gate; T5 pendente.

## Test Coverage Matrix

Nenhum AGENTS/guia adicional encontrado. Princípios da skill, JUnit/Spring Boot e RTL/Vitest/Playwright existentes. Base: 97 Java, 71 Vitest, 1 E2E. Testes de permissão/posições antigas se adaptam ao contrato explicitamente substituído, sem remoção/skip/enfraquecimento de integridade.

| Code Layer | Required Test Type | Coverage Expectation | Location Pattern | Run Command |
| --- | --- | --- | --- | --- |
| Serviço/repositório/HTTP etapas | integration | ETA-01–21: outcomes por rota com happy, limites, negações, falha e concorrência | sistema/src/test/java/com/patp/sistema/*Tests.java | mvn.cmd -B verify |
| Cliente HTTP | unit | Métodos/rota/token/corpo/versão/snapshot e erro sem retry | frontend/src/services/*.test.js | npm.cmd test |
| Formulário | integration | Campos/limites/posição/rascunho/pending/cancelar/teclado | frontend/src/pages/*.test.jsx | npm.cmd test |
| Quadro/cache | integration | CRUD/permissão/arquivo/ocupação/conflito/GETretry/foco/versão atual | frontend/src/pages/*.test.jsx | npm.cmd test |
| Fluxo real | e2e | CRUD posições e reload, Tab/Enter, archivedreadonly e arquivo com cache fresco | frontend/e2e | npm.cmd run test:e2e |

## Gate Check Commands

| Gate Level | When to Use | Command |
| --- | --- | --- |
| Quick | Cliente HTTP | npm.cmd test em frontend |
| Full | React ou API | npm.cmd test para React; mvn.cmd -B verify para servidor |
| Build | Final de fase/feature | mvn.cmd -B verify em sistema; npm.cmd test, npm.cmd run build, npm.cmd run lint em frontend; após T5 npm.cmd run test:e2e |

Logs Java em TEMP com resumo real. H2 classpath de teste; preview CLI H2 explícito. JAVA_HOME usa JDK real C:/Program Files/Java/jdk-25.0.2. Gates de banco nunca na aplicação normal.

## Execution Plan

### Phase 1: Configuração transacional no servidor

```text
T1
```

### Phase 2: Controles e fluxo na interface

```text
T2 -> T3 -> T4 -> T6 -> T5
```

Dependência entre fases:

```text
T1 -> T2
```

Seis tarefas; servidor T1 por trabalhador, interface T2–T4/T6/T5 por outro trabalhador, sequenciais. T6 fecha lacuna ETA26 apontada pela revisão depois do commit T4. Ao final, Verificador fresco que não escreveu código/testes.

## Task Breakdown

### T1: Entregar contrato transacional de etapas

**What:** substituir a administração permissiva por CRUD permissionado com posições, versão, snapshot e integridade.
**Where:** sistema/src/main/java/com/patp/sistema/service/EtapaService.java
**Companions:** controller/DTOs/repositórios/guarda necessária, representação atual do gerenciamento; tradução404 de destino removido em ProcessoService; testes novos e adaptação autorizada de EtapaArchiveTests; evidências/docs operacionais necessárias.
**Depends on:** None
**Requirement:** ETA-01–21.
**Done when:**
- [x] Snapshot/array legado são autenticados, determinísticos e não escrevem; quantidade real/legado conforme spec.
- [x] POST/PUT/DELETE cumprem permissão, campos/formatos, posições, ID/quadro, versão e JSON/status exatos; ocupada bloqueia todos os status, vazia remove somente alvo.
- [x] Reordenação/renomear preservam vínculos/histórico; falha rollback; concorrência com outra estrutura, arquivo e demanda não causa órfão/sobrescrita.
- [x] Todos os casos Java anteriores continuam executados, exceto contratos substituídos explicitamente adaptados; gate passa com adequação direta/reversa.
**Tests:** integration
**Gate:** full
**Commit:** feat(etapas): add transactional stage management API

### T2: Acrescentar operações de etapas ao cliente HTTP

**What:** wrappers snapshot/criar/editar/remover com payload/versão e erros sem retry.
**Where:** frontend/src/services/api.js
**Companions:** testes API e evidências.
**Depends on:** T1
**Requirement:** ETA-01/12/13/15/27–29.
**Done when:**
- [x] GETestrutura e POST/PUT/DELETE usam rota/token/corpo/versão corretos e devolvem snapshot.
- [x] Erros HTTP/rede mantêm mensagens/status atuais e não repetem mutação; gate passa e assertions API anteriores preservadas.
**Tests:** unit
**Gate:** quick
**Commit:** feat(frontend): add stage configuration API client

### T3: Entregar formulário de etapa

**What:** criar EditorEtapa para criar/editar campos e escolher posição.
**Where:** frontend/src/pages/EditorEtapa.jsx
**Companions:** testes RTL e CSS mínimo de formulário, evidências.
**Depends on:** T2
**Requirement:** ETA-09–11/23/25–27/30.
**Done when:**
- [x] Nome/Setor/Posição rotulados, valores atuais, seleção válida, limites/normalização e erro preservando campos.
- [x] Envio único pendente, teclado/alerts/cancelamento e foco conforme callbacks; gate passa.
**Tests:** integration
**Gate:** full
**Commit:** feat(frontend): add stage create and edit form

### T4: Integrar administração de etapas no quadro

**What:** snapshot, ações permissionadas, confirmação/ocupação, contagens e cache de gerenciamento.
**Where:** frontend/src/pages/Quadro.jsx
**Companions:** callback mínimo Gerenciamentos, testes RTL de quadro/cache, CSS mínimo, evidências; fixtures anteriores adaptadas ao snapshot preservando assertions funcionais.
**Depends on:** T3
**Requirement:** ETA-01/03/15/16/18/22/24–30.
**Done when:**
- [x] Operações funcionam com versão atual e DTO atualiza cache da lista; ocultar controles no arquivo/terceiro; contagens reais sem falso vazio ocupado.
- [x] Remover pede confirmação; cancelar não muta e devolve foco; erro/conflito/rede preserva rascunho e GET retry não repete mutação.
- [x] Carregamento/erro/vazio distintos, pending impede duplicação/saída e todos controles são alcançados/acionados por Tab/Enter; gate passa.
**Tests:** integration
**Gate:** full
**Commit:** feat(frontend): manage stage columns from the board

### T5: Verificar CRUD de etapas no navegador real

**What:** E2E real de criação/edição/ordenação/remoção/persistência e arquivo readonly.
**Where:** frontend/e2e/etapas.spec.js
**Companions:** configuração de fixture já existente somente se necessária, evidências/screenshots/docs de teste; ajuste de expectativa de versão no E2E anterior se ações existentes exigirem, sem reduzir sua cobertura.
**Depends on:** T6
**Requirement:** ETA-12/13/15/22/25/28/30 e conclusão ETA-01–30.
**Done when:**
- [ ] Edge/H2 real percorre CRUD/reordenação com reload e IDs/campos/ordens persistidos; ações por Tab/Enter e remoção confirmada/cancelada.
- [ ] Voltar à lista e arquivar usa cache/versão atual; snapshot arquivado não oferece configuração; layout desktop/mobile sem corte/overflow.
- [ ] Gates final Java/React/build/lint/E2E passam sem skip; evidência por outcome registrada; Verificador novo é despachado após commit.
**Tests:** e2e
**Gate:** build
**Commit:** test(etapas): verify stage management in the browser

### T6: Impedir saída global durante mutação de etapas

**What:** propagar a pendência de POST/PUT/DELETE para o botão e handler Sair; liberar sempre no finally, sem bloquear consultas GET.
**Where:** frontend/src/App.jsx
**Companions:** prop/callback mínimos em Gerenciamentos/Quadro, teste de integração App.test.jsx, evidência/spec.
**Depends on:** T4
**Requirement:** ETA-26.
**Done when:**
- [x] Sair e seu handler não descartam formulário/token/usuário durante as três mutações pendentes.
- [x] Sucesso/erro liberam Sair após o retorno, rascunho permanece em falha, e GET não bloqueia saída; gate e adequação passam.
**Tests:** integration
**Gate:** full
**Commit:** fix(frontend): prevent logout during stage mutations

## Diagram-Definition Cross-Check

| Task | Depends On | Diagram Shows | Status |
| --- | --- | --- | --- |
| T1 | None | entrada | OK |
| T2 | T1 | fase anterior | OK |
| T3 | T2 | T2 -> T3 | OK |
| T4 | T3 | T3 -> T4 | OK |
| T6 | T4 | T4 -> T6 | OK |
| T5 | T6 | T6 -> T5 | OK |

## Execution Evidence

Acrescentar gates, contagens, diffs e adequação de cada tarefa em `evidence.md` (servidor) e `frontend-evidence.md` (interface). Não criar arquivo vazio. Especificação deve registrar tarefas implementadas antes de seus commits; validação independente ao final, sem PASS humano inferido.

T1: verify H2 PASS, 238 testes (134 API etapas, 7 corridas, 6 regressões etapas, outros91), zero falhas/erros/skips. RED anterior à produção:116 testes,95 falhas de outcomes, zero erros/skips. Adequação direta/reversa com assertions físicas em `evidence.md`; nenhum SPEC_DEVIATION. MySQL isolado será verificado pelo root após este commit; T2–T5 e Verificador independente pendentes.
