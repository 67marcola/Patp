# Acesso pela internet — Tasks

## Execution Protocol (MANDATORY -- do not skip)

Implementar com a skill tlc-spec-driven, gates focados, evidência por assertion e commit atômico por tarefa. Atualizar este arquivo/spec/diagnóstico antes de cada commit. Nenhum teste antigo removido, alterado ou ignorado sem autorização aplicável. Verifier independente após último passo técnico; não declarar acesso4G sem URL/observação real.

**Design:** design.md. **Status:** Approved para preparo local pelo pedido de continuar; publicação requer recursos das contas do usuário. Ferramentas/skill atuais já autorizadas, sem nova pergunta sobre o mesmo uso.

## Test Coverage Matrix

> Inferida de CadastroSessaoTests, UsuarioIdentityTests, Cadastro.test.jsx, AppSessao.test.jsx, api.test.js e scripts/piloto-integracao.test.mjs. Guidelines: STATE AD-035, skill TLC, manifests Maven/npm e vitest.config.js; sem AGENTS/contributing/limite de coverage adicional encontrado. Nenhuma contagem histórica nova.

| Code Layer | Required Test Type | Coverage Expectation | Location Pattern | Run Command |
| --- | --- | --- | --- | --- |
| Política/entrada/sessão | integration | Outcomes NET-07 a12/15/16 e bordas; rollback/estado, sem mocks de mera chamada | sistema/src/test/java/com/patp/sistema/*Internet*Tests.java | Maven -Dtest=classeNova test em cópia TEMP/H2 |
| API administrativa | integration | GET/POST novos:200,401,403,404, repetição e persistência | mesma pasta Java | Maven -Dtest=classeNova test |
| Preparador SQL | integration | schema vazio/não vazio/DDL parcial/admin explícito, nenhum dado local | mesma pasta Java | teste focado com banco fictício guardado |
| API cliente/componentes | unit | Contratos novos, pendência, erro, vazio, usuário comum/admin e envio incerto | frontend/src/**/*.test.{js,jsx} | npm test -- arquivoNovo |
| Perfil/artefatos/configuração | none | Build/lint; ausência de segredos e falhas de startup conferidas no integrado | deploy e recursos | compilação Java/Vite TEMP + lint |
| Fluxo integrado | e2e | Dois contextos, pendente→aprovação→login/demanda, reinício/config e celular | frontend/scripts/internet-integracao.test.mjs | Node --test arquivoNovo |
| Guia/publicação real | none | Inspeção documental; URL/4G somente quando observados | docs/OPERACAO.md | validate_spec/state/diff; roteiro real separado |

## Gate Check Commands

| Gate Level | When to Use | Command |
| --- | --- | --- |
| Quick | Componente alterado | seleção Maven ou Vitest indicada, sem suíte completa |
| Full | Integração final | cenário novo em MySQL fictício/Edge, sem repetir domínio histórico |
| Build | Configuração/documentação | compile/package Java e build Vite em TEMP quando afetados, npm lint, validadores/diff para docs |

Alvo de economia: oito invocações comportamentais autor (T1–7 e T10) e duas no Verifier (integrado+sensor), dez contra referência anterior de vinte. Gates Build não repetem testes. Repetir só se falha/mudança justificar; contar tentativas honestamente. Não mede tokens. Sem prometer redução se houver falhas que precisem de nova prova.

## Execution Plan

### Phase 1: Servidor e base online
```text
T1 → T2 → T3 → T4
```

### Phase 2: Interface online
```text
T5 → T6 → T7
```

### Phase 3: Pacote, integração e operação
```text
T8 → T9 → T10 → T11
```

Ligações entre fases:
```text
T4 → T5
T7 → T8
```

## Task Breakdown

### T1: Política isolada de autorização de cadastros
**What**: componente JDBC opt-in para pendência/aprovação, sem entidade/DDL local.
**Where**: `sistema/src/main/java/com/patp/sistema/service/AprovacaoCadastroService.java`
**Depends on**: None
**Reuses**: JdbcTemplate, UsuarioRepository, ApiException.
**Requirement**: NET-07,NET-08,NET-10,NET-11,NET-12
**Tools**: shell/arquivos; skill tlc-spec-driven.
**Done when**:
- [x] Política local não acessa tabela; online cria/verifica pendência e aprova preservando papel/autoria/hora em repetição/concorrência; testes de estado/rollback e gate focado passam.
**Tests**: integration — AprovacaoCadastroServiceInternetTests; asserts de estado, não apenas chamadas.
**Gate**: Quick

### T2: Entrada e sessão com autorização online
**What**: um fluxo de autenticação integra a política no cadastro/login/sessão existentes, mantendo contrato local.
**Where**: `sistema/src/main/java/com/patp/sistema/controller/UsuarioController.java`
**Depends on**: T1
**Reuses**: UsuarioService, SessaoService, BCrypt; alterações coesas nesses colaboradores na mesma tarefa.
**Requirement**: NET-07,NET-08,NET-09,NET-12,NET-16,NET-17
**Tools**: shell/arquivos; skill tlc-spec-driven.
**Done when**:
- [x] Cadastro online202 sem token, local200/seis campos, pendente403, persistência atômica, token expirado8h401 e revogação; testes focados por outcomes aprovados.
**Tests**: integration — AutenticacaoInternetTests; relógio controlado, sem esperar horas.
**Gate**: Quick

### T3: Controller de aprovação administrativa
**What**: componente com GET pendentes e POST aprovar, mesmo contrato e autorização.
**Where**: `sistema/src/main/java/com/patp/sistema/controller/AprovacaoCadastroController.java`
**Depends on**: T2
**Reuses**: SessaoService e política T1; nenhum papel do body.
**Requirement**: NET-10,NET-11,NET-12
**Tools**: shell/arquivos; skill tlc-spec-driven.
**Done when**:
- [x] Controller online expõe contratos do design; testes HTTP provam200/401/403/404, payload exato, repetição e dados preservados.
**Tests**: integration — AprovacaoCadastroApiInternetTests.
**Gate**: Quick

### T4: Preparador explícito da base online
**What**: ferramenta JDBC de plano/aplicação e bootstrap de admin, sem ler credenciais locais.
**Where**: `sistema/src/main/java/com/patp/sistema/PrepararBancoInternet.java`
**Depends on**: T3
**Reuses**: driver MySQL; SQL de estrutura da aplicação como recurso, sem DROP; base fictícia guardada para gate.
**Requirement**: NET-03,NET-04,NET-05,NET-15
**Tools**: shell/arquivos; skill tlc-spec-driven.
**Done when**:
- [x] Plano não grava; alvo não vazio/estrutura parcial recusados; criação explícita em vazio conserva banco local; bootstrap confere ID/e-mail e não depende do cadastro público; SQL/schema e casos de falha têm gate real fictício.
**Tests**: integration — PrepararBancoInternetTests; sem3306 ou credenciais reais.
**Gate**: Quick

### T5: Contratos do cliente para aprovação e saída
**What**: helpers do componente API para202, lista pendente, aprovação e logout, conservando API atual.
**Where**: `frontend/src/services/api.js`
**Depends on**: T4
**Reuses**: requisicao/ApiError e validações explícitas.
**Requirement**: NET-08,NET-10,NET-11,NET-13,NET-14,NET-17
**Tools**: shell/arquivos; skill tlc-spec-driven.
**Done when**:
- [x] Respostas exatas validadas; malformed/sucesso incerto não entra nem aprova; contratos antigos preservados, gate focado aprovado.
**Tests**: unit — apiInternet.test.js.
**Gate**: Quick

### T6: Cadastro informa espera de aprovação
**What**: componente Cadastro reconhece202 e mostra pendência sem sessão.
**Where**: `frontend/src/pages/Cadastro.jsx`
**Depends on**: T5
**Reuses**: formulário, teclado/duplo clique e mensagens existentes.
**Requirement**: NET-07,NET-13
**Tools**: shell/arquivos; skill tlc-spec-driven.
**Done when**:
- [x] Pendente não chama onLogin, não muda storage, mantém proteção de envio/erros;200 local segue fluxo atual; testes focados aprovados.
**Tests**: unit — CadastroInternet.test.jsx.
**Gate**: Quick

### T7: Painel de aprovação e integração online
**What**: componente AprovacoesCadastros integrado ao App no modo online, incluindo saída com revogação.
**Where**: `frontend/src/components/AprovacoesCadastros.jsx`
**Depends on**: T6
**Reuses**: App, usuário conferido via /me, helpers T5; mudanças mínimas coesas no App e estilos do componente.
**Requirement**: NET-10,NET-11,NET-14,NET-17
**Tools**: shell/arquivos; skill tlc-spec-driven.
**Done when**:
- [x] Admin consulta/aprova; funcionário não recebe controle; estados carregar/vazio/erro/incerto preservados, foco e CSS para360px; App local continua sem consulta nova; saída online informa falha incerta; gate novo integrado de componente aprovado. Geometria real em360px será conferida no integrado T10, sem tratar jsdom como prova visual.
**Tests**: unit — AprovacoesCadastros.test.jsx e AppInternet.test.jsx em uma seleção.
**Gate**: Quick

### T8: Pacote seguro de execução no Render
**What**: um artefato de entrega online com perfil obrigatório, guardas, TLS/CORS e Docker/Render.
**Where**: `deploy/internet/`
**Depends on**: T7
**Reuses**: perfil Spring, CorsConfig e POM existentes; sem dependências novas. Perfil/configuração são partes do mesmo artefato.
**Requirement**: NET-01,NET-02,NET-03,NET-04,NET-07
**Tools**: shell/arquivos; skill tlc-spec-driven.
**Done when**:
- [x] Artefato com guardas de configuração, schema validate/SQLinitnever, TLS VERIFY_IDENTITY, origemHTTPS exata e PORT; contexto exclui props locais/segredos; classes Java e classpath runtime compilados emTEMP. Sintaxe shell conferida. Falhas reais de startup serão provadas em T10. Build Docker no Render fica pendente: Docker indisponível aqui.
**Tests**: none — configuração/artefato; comportamento de execução conferido em T10.
**Gate**: Build

### T9: Configuração Vercel do frontend online
**What**: configuração do build Vite na Vercel com variáveis públicas documentadas.
**Where**: `frontend/vercel.json`
**Depends on**: T8
**Reuses**: package.json e VITE_API_URL; flag VITE_MODO_INTERNET.
**Requirement**: NET-01,NET-02
**Tools**: shell/arquivos; skill tlc-spec-driven.
**Done when**:
- [x] Build TEMP apontaAPIHTTPS, semlocalhost/segredos, assets válidos;lint passa; nenhum recurso externo criado por esse arquivo.
**Tests**: none — configuração, gate Build.
**Gate**: Build

### T10: Fluxo integrado online em ambiente fictício
**What**: cenário focado demonstra cadastro pendente, aprovação, uso, reinício e falhas do pacote.
**Where**: `frontend/scripts/internet-integracao.test.mjs`
**Depends on**: T9
**Reuses**: isolamento MySQL/Edge/pastas próprias do piloto, sem repetir suítes históricas.
**Requirement**: NET-02,NET-03,NET-04,NET-06,NET-07,NET-08,NET-09,NET-10,NET-11,NET-13,NET-14,NET-16,NET-17
**Tools**: shell/arquivos/Edge; skill tlc-spec-driven.
**Done when**:
- [x] MySQL fictício TLS guardado, dois contextos e360px, sem dados reais; outcomes do spec com file:line; compile/build reais; cleanup próprio; gate1/1 passou na primeira tentativa,86,7s. Total autor11 invocações,77 casos distintos; publicação/4G não inferidos.
**Tests**: e2e — cenário novo Node/Playwright e sem3306.
**Gate**: Full

### T11: Guia e entrega para publicação assistida
**What**: procedimento de contas, configuração e primeiro acesso4G com estado real.
**Where**: `docs/OPERACAO.md`
**Depends on**: T10
**Reuses**: contexto confirmado e arquivos T8/T9; diagnóstico/memória/evidence atualizados na tarefa.
**Requirement**: NET-01,NET-05,NET-15,NET-18,NET-19,NET-20
**Tools**: shell/arquivos; skill tlc-spec-driven.
**Done when**:
- [ ] Guia de planos grátis/base nova/primeirosadmins/aprovação/URL/4G pronto; validadores/diff passam; nada não observado marcado concluído. Disparar Verifier fresco ao concluir passos locais; publicar/acesso4G seguem pendentes até recursos/execução reais.
**Tests**: none — documentação, sem repetição de testes.
**Gate**: Build

## Diagram-Definition Cross-Check

| Task | Depends On | Diagram Shows | Status |
| --- | --- | --- | --- |
| T1 | None | None | OK |
| T2 | T1 | T1 | OK |
| T3 | T2 | T2 | OK |
| T4 | T3 | T3 | OK |
| T5 | T4 | Phase1 concluída | OK |
| T6 | T5 | T5 | OK |
| T7 | T6 | T6 | OK |
| T8 | T7 | Phase2 concluída | OK |
| T9 | T8 | T8 | OK |
| T10 | T9 | T9 | OK |
| T11 | T10 | T10 | OK |

## Test Co-location Validation

| Task | Layer | Matrix Requires | Task Says | Status |
| --- | --- | --- | --- | --- |
| T1–4 | Política/auth/API/preparo | integration | integration | OK |
| T5–7 | Cliente/componentes | unit | unit | OK |
| T8–9 | Configuração/pacote | none/build | none/build | OK |
| T10 | Integrado | e2e | e2e | OK |
| T11 | Guia | none/documental | none/documental | OK |

Granularidade: cada tarefa entrega um componente/fluxo coeso ou artefato. Colaboradores mínimos de T2/T7/T8 entram na mesma tarefa para que o componente possa ser testado sem adiar seus testes. Sem tarefa separada para escrever testes. Nenhum gate Full histórico é necessário; AD-035 prevalece sobre repetição rotineira de todas as suítes.
