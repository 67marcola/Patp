# CRUD de etapas de trabalho: tarefas

## Execution Protocol

Usuário autorizou continuar a implementação, com AD-007–010 confirmadas. Executar uma tarefa por vez: testes derivados de ETA, gate, adequação direta/reversa com arquivo:linha e assertion, status e um Conventional Commit local. Sem push/deploy/MySQL configurado. Nenhuma fase seguinte começa antes do gate/commit anterior. Delegação técnica autorizada pelas instruções da sessão; não solicitar confirmação de rotina.

**Design:** `design.md`. **Status:** T1–T7 concluídas e validação técnica independente PASS na rodada 2, 30/30 critérios. T7 fechou ETA-27/M10. UAT humano pendente.

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
T2 -> T3 -> T4 -> T6 -> T5 -> T7
```

Dependência entre fases:

```text
T1 -> T2
```

Sete tarefas; servidor T1 por trabalhador, interface T2–T4/T6/T5/T7 por outro trabalhador, sequenciais. T6 fecha lacuna ETA26 da revisão; T7 fecha gap de cobertura ETA27/M10 do Verificador independente. Ao final, re-verificação independente por quem não escreveu código/testes.

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
**Companions:** scripts/run-e2e.mjs e package.json para H2 novo por arquivo (sem dados compartilhados que invalidem o E2E anterior); helper/configuração existente somente se necessária; evidências/screenshots/docs de teste. Assertions do E2E anterior conservadas.
**Depends on:** T6
**Requirement:** ETA-12/13/15/22/25/28/30 e conclusão ETA-01–30.
**Done when:**
- [x] Edge/H2 real percorre CRUD/reordenação com reload e IDs/campos/ordens persistidos; ações por Tab/Enter e remoção confirmada/cancelada.
- [x] Voltar à lista e arquivar usa cache/versão atual; snapshot arquivado não oferece configuração; layout desktop/mobile sem corte/overflow.
- [x] Gates final Java/React/build/lint/E2E passam sem skip; evidência por outcome registrada.
**Tests:** e2e
**Gate:** build
**Commit:** test(etapas): verify stage management in the browser

**Fechamento da feature:** após o commit da última tarefa (agora T7), root despacha Verificador novo e executa validate_state antes de declarar feature concluída. O trabalhador da interface não atua como Verificador e encerra as alterações após seu commit.

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

### T7: Cobrir respostas de sucesso ilegíveis

**What:** testar POST201/PUT200/DELETE200 com JSON ilegível e truncado, fechando gap V1/M10 sem alterar a produção correta nem adicionar validação de schema.
**Where:** frontend/src/services/api.test.js
**Companions:** frontend/src/pages/QuadroEtapas.test.jsx, frontend-evidence.md e spec/tasks; nenhuma alteração nos137 casos anteriores, fontes de produção, relatório do Verificador ou STATE.
**Depends on:** T5
**Requirement:** ETA-27/29, com versão/payload de ETA-28.
**Done when:**
- [x] Três wrappers rejeitam os dois corpos ilegíveis com mensagem de comunicação exata, status original201/200 e uma requisição, sem inventar sucesso ou retry.
- [x] Quadro conserva campos/posição noPOST/PUT ou confirmação/coluna noDELETE, alerta acessível exato, cache e colunas originais; refresh manual somenteGET e próximo envio somente manual com versão reconsultada/payload completo.
- [x] Gate Build passa com todos os 137 testes anteriores preservados e novos casos executados; adequação direta/reversa física registrada e commit local anterior à re-verificação independente.
**Tests:** integration
**Gate:** build
**Commit:** test(etapas): cover unreadable successful responses

## Diagram-Definition Cross-Check

| Task | Depends On | Diagram Shows | Status |
| --- | --- | --- | --- |
| T1 | None | entrada | OK |
| T2 | T1 | fase anterior | OK |
| T3 | T2 | T2 -> T3 | OK |
| T4 | T3 | T3 -> T4 | OK |
| T6 | T4 | T4 -> T6 | OK |
| T5 | T6 | T6 -> T5 | OK |
| T7 | T5 | T5 -> T7 | OK |

## Execution Evidence

Acrescentar gates, contagens, diffs e adequação de cada tarefa em `evidence.md` (servidor) e `frontend-evidence.md` (interface). Não criar arquivo vazio. Especificação deve registrar tarefas implementadas antes de seus commits; validação independente ao final, sem PASS humano inferido.

T1: verify H2 PASS, 238 testes (134 API etapas, 7 corridas, 6 regressões etapas, outros 91), zero falhas/erros/skips. RED anterior à produção: 116 testes, 95 falhas de outcomes, zero erros/skips. Adequação direta/reversa com assertions físicas em `evidence.md`; nenhum SPEC_DEVIATION. MySQL temporário verificado posteriormente pelo root, 237 testes PASS, em `mysql-validation.md`.

T7: gate local Build PASS em 05/10/2026: 238 Java/H2, 149 Vitest (137 preservados +12 novos), build/lint e 2 E2E Edge. Primeira execução E2E saiu 1 por ECONNRESET; trace preservado, causa do socket não determinada. Repetição diagnóstica completa saiu 0, sem retries nem alteração de assertions/runner. Adequação direta/reversa e limites em `frontend-evidence.md`. Nenhuma fonte de produção alterada; novo MySQL não necessário.

Fechamento independente: rodada 2 PASS sobre d067682, 30/30 critérios, os mesmos gates completos e 5/5 falhas compiláveis detectadas. M10 anterior e equivalentes PUT/DELETE agora são discriminados. Isolamento comprovado por comparação byteigual do porcelain antes/depois; `validate_state.py etapas` retornou zero erros, exit 0. Evidências atuais e histórico da primeira rodada em `validation.md`. Teste humano sem resultado.
