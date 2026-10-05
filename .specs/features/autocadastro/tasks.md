# Entrada após cadastro: tarefas

## Execution Protocol

Implementação local autorizada pelo pedido e continuações. Uma tarefa por vez, teste derivado de AUT, gate, adequação direta/reversa com arquivo:linha e assertion, status/traceabilidade antes do Conventional Commit local. Não enfraquecer/remover testes anteriores. Sem push, deploy, banco real ou limpeza de artefatos anteriores. Cinco tarefas em um batch de fases consecutivas; delegação técnica permitida pelas instruções da sessão. Verificador independente automático ao final.

## Test Coverage Matrix

| Code Layer | Required Test Type | Coverage Expectation | Location Pattern | Run Command |
| --- | --- | --- | --- | --- |
| Cadastro/login HTTP e banco | integration | AUT-01–05, identidade/DTO/role/senha/falhas com estado real | sistema/src/test/java/com/patp/sistema/*Tests.java | mvn.cmd -B verify |
| Wrappers autenticação | unit | AUT-06, campos/payloads válidos e inválidos, HTTP/rede/parse sem retry | frontend/src/services/*.test.js | npm.cmd test |
| Sessão App/login | integration | AUT-07–09, dois caches/projeção/reload/logout/falha parcial | frontend/src/*.test.jsx | npm.cmd test |
| Formulário cadastro | integration | AUT-10–14, validação/pending/erro/rascunho/acessibilidade | frontend/src/pages/*.test.jsx | npm.cmd test |
| Fluxo real | e2e | Cadastro direto por teclado, conta/quadro/reload/logout/login | frontend/e2e/*.spec.js | npm.cmd run test:e2e |

Sem AGENTS adicional encontrado. Seguir coding-principles da skill e padrões JUnit, RTL, Vitest e Playwright existentes. Base 238 Java/H2, 149 Vitest e 2 E2E preservada.

## Gate Check Commands

| Gate Level | When to Use | Command |
| --- | --- | --- |
| Quick | T2 cliente | npm.cmd test em frontend |
| Full | T1 servidor ou T3/T4 React | mvn.cmd -B verify em sistema para T1; npm.cmd test em frontend para T3/T4 |
| Build | T5 final | mvn.cmd -B verify em sistema; npm.cmd test, npm.cmd run build, npm.cmd run lint, npm.cmd run test:e2e em frontend |

Usar JAVA_HOME C:/Program Files/Java/jdk-25.0.2 e PATH correspondente por comando. Testes Java H2 e navegador pelo helper isolado H2. Não executar Maven enquanto E2E recompila no mesmo target. Logs em TEMP, evidência com contagens reais; commits só de paths explícitos.

## Execution Plan

### Phase 1: Sessão no servidor

```text
T1
```

### Phase 2: Entrada e formulário

```text
T2 -> T3 -> T4
```

### Phase 3: Fluxo real

```text
T5
```

Dependências entre fases:

```text
T1 -> T2
T4 -> T5
```

## Task Breakdown

### T1: Devolver sessão após persistir cadastro

**What:** reutilizar o DTO seguro de login no cadastro com sessão do ID salvo.
**Where:** sistema/src/main/java/com/patp/sistema/controller/UsuarioController.java
**Companions:** novo CadastroSessaoTests.java, evidence.md, status/traceabilidade desta feature. Serviço/modelo/handler existentes não requerem redesign.
**Depends on:** None
**Requirement:** AUT-01–05.
**Done when:**
- [x] Cadastro200 devolve todos os campos seguros, token funciona em me/gerenciamentos; conta salva é FUNCIONARIO e BCrypt sem senha na resposta.
- [x] ID recebido/JSON inválido/duplicidade/save falhando têm status/mensagens da spec, sem novos registros ou token; login manual preservado.
- [x] Todos os testes Java anteriores executam e gate passa; adequação direta/reversa registrada.
**Tests:** integration
**Gate:** full
**Commit:** feat(auth): return a session after registration

### T2: Validar resposta de autenticação no cliente

**What:** conferir o contrato mínimo de login/cadastro antes de entregar dados à interface.
**Where:** frontend/src/services/api.js
**Companions:** api.test.js ou novo ApiAutenticacao.test.js, evidence.md e status/traceabilidade.
**Depends on:** T1
**Requirement:** AUT-06, transporte de AUT-10/13.
**Done when:**
- [x] Ambos os wrappers usam rota/corpo corretos, aceitam setor null e rejeitam campos inválidos/JSON ilegível com mensagem/status exatos sem retry.
- [x] Erros HTTP e rede preservam ApiError/status/mensagem existentes; gate passa com testes anteriores intactos e adequação registrada.
**Tests:** unit
**Gate:** quick
**Commit:** fix(auth): reject incomplete authentication responses

### T3: Compartilhar gravação e entrada da sessão

**What:** concentrar cache seguro e troca de tela em App para login e cadastro.
**Where:** frontend/src/App.jsx
**Companions:** Login.jsx, novo AppAutocadastro.test.jsx ou AppSessao.test.jsx, evidence.md e status/traceabilidade. Cadastro receberá o callback em T4.
**Depends on:** T2
**Requirement:** AUT-07–09.
**Done when:**
- [x] Entrada grava token e somente quatro campos do usuário antes de mostrar Gerenciamentos; reload/logout/login preservados.
- [x] Falha de storage inclusive segunda chave mantém tela/erro exato, tenta restaurar cache anterior e não repete autenticação; gate e testes anteriores passam com adequação registrada.
**Tests:** integration
**Gate:** full
**Commit:** refactor(auth): share session entry between authentication forms

### T4: Entrar no sistema pelo formulário de cadastro

**What:** consumir a sessão e proteger o formulário durante envio e falhas.
**Where:** frontend/src/pages/Cadastro.jsx
**Companions:** wiring mínimo de App se necessário, Cadastro.test.jsx e casos AppAutocadastro.test.jsx, evidence.md e status/traceabilidade. Labels/alert de Login apenas se necessários ao teste de entrada compartilhada. Não remover PM.
**Depends on:** T3
**Requirement:** AUT-10–14 e integração AUT-07/08.
**Done when:**
- [x] Cadastro confirmado abre Gerenciamentos pela mesma entrada sem segundo login; payload normaliza só nome/setor/email.
- [x] Pending impede submit duplicado imediato, desabilita cinco campos e ações; erro libera e mantém campos, com mensagens exatas de validação/HTTP/rede/resposta/storage e sem retry.
- [x] Labels/alert/teclado e validação nativa disponíveis; gate e testes anteriores passam com adequação registrada.
**Tests:** integration
**Gate:** full
**Commit:** feat(auth): enter the system directly after registration

### T5: Verificar cadastro direto no navegador

**What:** provar conta/quadro/reload/saída/login por fluxo real isolado.
**Where:** frontend/e2e/autocadastro.spec.js
**Companions:** evidence.md, docs/OPERACAO.md com roteiro de cadastro direto, status/traceabilidade. Runner/helper só se estritamente necessário, sem mudar os dois E2E anteriores.
**Depends on:** T4
**Requirement:** AUT-07/09/10/14 e conclusão AUT-01–14.
**Done when:**
- [x] Edge/H2 percorre cadastro por teclado, direto para lista sem POSTlogin; ID/role/cache/quadro sobrevivem reload, logout limpa e login manual retorna à mesma conta/quadro.
- [x] Gates Java/React/build/lint e três E2E passam sem skip/retry; adequação direta/reversa final e roteiro de UAT registrado.
**Tests:** e2e
**Gate:** build
**Commit:** test(auth): verify direct registration and session persistence

## Diagram-Definition Cross-Check

| Task | Depends On | Diagram Shows | Status |
| --- | --- | --- | --- |
| T1 | None | entrada | OK |
| T2 | T1 | fase anterior | OK |
| T3 | T2 | T2 -> T3 | OK |
| T4 | T3 | T3 -> T4 | OK |
| T5 | T4 | fase anterior | OK |

## Execution Evidence

Criar evidence.md somente com execução real. Cada tarefa registra suposições/paths/sucesso antes de código, comandos/contagens/limites, mapeamento direto e reverso de assertions físicas e verdict de adequação antes do commit. Após T5, Verificador novo executa validate.md, escreve validation.md e o root roda validate_state. Nenhum resultado humano será inferido de autorização para continuar.
