# Entrada após cadastro: evidência de execução

## T1: Sessão do cadastro persistido

### Pré-implementação

- Suposições: o contrato AUT-01–05 preserva HTTP 200, entrada `Usuario`, FUNCIONARIO e BCrypt. A sessão só nasce depois de `cadastrar` devolver o usuário salvo. Testes usam somente contas fictícias e datasource do classpath de teste.
- Arquivos: `sistema/src/main/java/com/patp/sistema/controller/UsuarioController.java`, novo `sistema/src/test/java/com/patp/sistema/CadastroSessaoTests.java`, este `evidence.md`, `spec.md` e `tasks.md` desta feature.
- Sucesso: HTTP real em porta aleatória confirma seis campos seguros e identidade persistida; token funciona em me/gerenciamentos sem login; ID/JSON/duplicidade/save falho não emitem token nem alteram contas; login manual continua seguro. Gate completo Maven/H2 preserva os 238 casos anteriores.
- Testes escritos antes da implementação: nove casos derivados de AUT-01–05, com primeiro/segundo cadastro, ID existente/escolhido, JSON inválido, duplicidade, persistência falhando e login com ambos os papéis/setor null legado.

### Gate e revisão

- Teste inicial: `mvn.cmd -B -Dtest=CadastroSessaoTests test`, 9 executados, 2 falhas esperadas (resposta do cadastro tinha 5 campos, faltava token), zero erros/skips. Log `%TEMP%/autocadastro-t1-red.log`.
- Implementação: somente controlador, helper privado comum gera `LoginResponse` após o serviço devolver a conta salva. Serviço/modelo/handler intactos.
- Gate: `mvn.cmd -B verify` com JAVA_HOME `C:/Program Files/Java/jdk-25.0.2`, exit 0, 247 testes, zero falhas/erros/skips (238 anteriores + 9). Log `%TEMP%/autocadastro-t1-gate.log`.
- Limites: falha de save injetada antes da gravação; não afirma rollback após resposta de rede perdida. Nenhum banco configurado ou processo do usuário acessado.

**Mapeamento direto (Check A).** Prefixo físico abaixo: `sistema/src/test/java/com/patp/sistema/CadastroSessaoTests.java`.

| AC / caso | arquivo:linha + assertion | Resultado da spec | Coberto |
| --- | --- | --- | --- |
| AUT-01 HTTP/DTO completo | `CadastroSessaoTests.java:50` `assertThat(response.statusCode()).isEqualTo(200)`; `:52` `assertThat(body.size()).isEqualTo(6)`; `:53` `assertThat(body.path("token").asString()).isNotBlank()` | 200 e token não vazio | Sim |
| AUT-01 identidade e campos | `:54` `assertThat(body.path("id").asLong()).isEqualTo(saved.getId())`; `:55` `assertThat(body.path("nome").asString()).isEqualTo(saved.getNome())`; `:56` `assertThat(body.get("setor").isNull() ? null : body.path("setor").asString()).isEqualTo(saved.getSetor())`; `:57` `assertThat(body.path("email").asString()).isEqualTo(saved.getEmail())`; `:58` `assertThat(body.path("papel").asString()).isEqualTo(saved.getPapel().name())` | Campos da conta persistida, sem inventar setor | Sim |
| AUT-01 token em me/lista sem login | `:67` `assertThat(me.statusCode()).isEqualTo(200)`; `:69` `assertThat(body.path("id").asLong()).isEqualTo(saved.getId())`; `:74` `assertThat(list.statusCode()).isEqualTo(200)`; `:75` `assertThat(json.readTree(list.body()).isArray()).isTrue()`; `:76` `assertThat(json.readTree(list.body()).size()).isZero()` | Token do cadastro autoriza a mesma conta e lista vazia real | Sim |
| AUT-02 primeiro/seguinte e papel solicitado | `:94` `assertThat(usuarios.count()).isEqualTo(before + 1)`; `:95` `assertThat(saved.getNome()).isEqualTo("Novo")`; `:96` `assertThat(saved.getSetor()).isEqualTo("Comercial")`; `:97` `assertThat(saved.getEmail()).isEqualTo("novo@example.test")`; `:98` `assertThat(saved.getPapel()).isEqualTo(PapelUsuario.FUNCIONARIO)`; `:99` SQL `isEqualTo("FUNCIONARIO")` | Um registro com valores enviados e FUNCIONARIO inclusive ADMINISTRADOR recebido | Sim, dois casos |
| AUT-02 senha/hash | `:100` `assertThat(new BCryptPasswordEncoder().matches(" senha-ficticia ", saved.getSenha())).isTrue()`; `:101` `assertThat(saved.getSenha()).startsWith("$2").isNotEqualTo(" senha-ficticia ")`; `:59` `assertThat(body.has("senha")).isFalse()`; `:60` `assertThat(body.has("hash")).isFalse()`; `:61` `assertThat(response.body()).doesNotContain(saved.getSenha())` | BCrypt sem senha/hash na resposta | Sim |
| AUT-03 ID existente/escolhido | `:80` `assertThat(response.statusCode()).isEqualTo(status)` e `:82` `assertThat(body.path("erro").asString()).isEqualTo(message)` chamados em `:111` com 400 e `O ID do usuário deve ser definido pelo sistema.`; `:83` `assertThat(body.has("token")).isFalse()` | 400 exato e nenhum token | Sim, dois casos |
| AUT-03/04 contas preservadas | `:117` `assertThat(usuarios.count()).isEqualTo(1)`; `:119` nome, `:120` setor, `:121` email, `:122` senha, `:123` papel todos `isEqualTo(original.get...)` | Nenhuma conta extra ou alteração da anterior | Sim em cada falha |
| AUT-04 JSON/duplicidade/save | `:80` status e `:82` mensagem com chamada `:129` (400, `Dados da requisição inválidos.`), `:136` e `:146` (500, `Não foi possível concluir a operação.`); `:83` token ausente; `:117–123` preservação | Erros exatos com banco preservado | Sim, três casos |
| AUT-05 login manual/setor legado | `:50–61` DTO seguro; `:67–76` token utilizável; `:58` papel igual ao salvo; `:162` `assertThat(usuarios.count()).isEqualTo(1)` | Login 200, papel atual, setor null aceito e mesma conta | Sim, dois papéis |

**Mapeamento reverso (Check C), todos os nove casos.** As expressões físicas compartilhadas acima pertencem às chamadas de cada teste abaixo.

| Teste e assertion física | AC / caso | Manter |
| --- | --- | --- |
| `CadastroSessaoTests.java:88` primeiro e seguinte: `:94` count `before + 1`, `:98` FUNCIONARIO, `:100` matches BCrypt, `:50–61` DTO e `:67–76` token/lista | AUT-01/02 primeiro, segundo e ADMINISTRADOR recebido | Sim, 2 |
| `:107` ID existente/escolhido: `:80/82/83` status/mensagem/token, `:117–123` registro anterior | AUT-03 | Sim, 2 |
| `:127` JSON inválido: `:80/82/83` 400/texto/sem token, `:117–123` banco | AUT-04 JSON | Sim, 1 |
| `:134` duplicidade: `:80/82/83` 500/texto/sem token, `:117–123` banco | AUT-04 email repetido | Sim, 1 |
| `:143` save falhando: `:80/82/83` 500/texto/sem token, `:117–123` banco | AUT-04 falha de persistência | Sim, 1 |
| `:154` ambos os papéis: `:50–61` DTO/valor dos seis campos, `:67–76` sessão e `:162` count 1 | AUT-05 login manual e setor null legado | Sim, 2 |

- Checks A/B/C/D: completos. Valores do DTO/persistência verificados separadamente, além de status/call; nenhum teste raso ou sem requisito. Padrões JUnit/AssertJ/Spring existentes e coding-principles seguidos. Nenhum teste anterior modificado, removido ou ignorado; nenhuma SPEC_DEVIATION.
- Veredito: AUT-01–05 cobertos dentro dos limites declarados. T1 completo após gate.

## T2: Validar a resposta de autenticação

### Pré-implementação

- Suposições: AUT-06 exige objeto com token original não vazio após trim, id inteiro positivo, nome/email strings e setor string ou null. Campos extras do DTO são preservados aqui; App fará a projeção segura. Erros HTTP/rede/parser já têm ApiError e devem continuar iguais.
- Arquivos: `frontend/src/services/api.js`, novo `frontend/src/services/ApiAutenticacao.test.js`, evidence/spec/tasks desta feature.
- Sucesso: ambos os wrappers mantêm rota/POST/body, aceitam contrato válido e setor null, rejeitam os campos inválidos e JSON ilegível com ApiError/status 200/texto exato, sem repetir requisição. Gate npm completo preserva os 149 testes anteriores.
- Testes antes do código: 52 casos (4 contratos válidos, 36 contratos inválidos, 4 JSON ilegíveis, 6 erros HTTP e 2 erros de rede).


### Gate e revisão

- Inicial focal: `npm.cmd test -- --run src/services/ApiAutenticacao.test.js`, 36 falhas esperadas (contratos inválidos aceitos), 16 passes, 52 casos. Log `%TEMP%/autocadastro-t2-red.log`.
- Implementação: helper de autenticação compartilhado verifica somente contrato exigido, preservando token original e erros existentes. Nenhuma dependência/rota adicional.
- Gate: `npm.cmd test`, exit 0, 201 passes (149 anteriores + 52), oito arquivos, zero falhas/skips. Log `%TEMP%/autocadastro-t2-gate.log`.
- Limite: contratos simulados no transporte; valores reais do servidor cobertos por T1. Nenhum teste anterior alterado/removido/ignorado.

**Mapeamento direto (Check A).** Prefixo físico: `frontend/src/services/ApiAutenticacao.test.js`.

| Critério | arquivo:linha + assertion | Resultado da spec | Coberto |
| --- | --- | --- | --- |
| AUT-06 objeto e campos | `ApiAutenticacao.test.js:47` `expect(error).toBeInstanceOf(ApiError)` e `:48` `expect(error).toMatchObject({ status: 200, message })`, matriz `:31–41` | 18 classes inválidas em ambos wrappers: objeto/token/id/nome/email/setor recusados; texto exato definido em `:6` | Sim, 36 casos |
| AUT-06 contrato válido/token original/setor null | `:22` `expect(await run()).toEqual(expected)`, expected `:20` usa os seis valores de session `:5` e setor Campo/null | Sem trim do token, setor null aceito, demais campos preservados | Sim, 4 casos |
| T2 rota/POST/corpo, transporte AUT-10 | `:23` URL `toBe(...)`; `:24` `expect(fetchMock.mock.calls[0][1]).toEqual({ method: "POST", headers: { "Content-Type": "application/json" }, signal: undefined, body: JSON.stringify(payload) })` | Rotas corretas, email/senha login, quatro campos cadastro com senha original | Sim |
| AUT-06 JSON ilegível/sem retry | `:56` `expect(error).toBeInstanceOf(ApiError)`; `:57` `expect(error).toMatchObject({ status: 200, message })`; `:58` `expect(fetchMock).toHaveBeenCalledTimes(1)` | HTML e JSON truncado não confirmam sessão, 200 e mensagem exata, uma requisição | Sim, 4 casos |
| AUT-06 sem retry contrato inválido | `:49` `expect(fetchMock).toHaveBeenCalledTimes(1)` junto ao erro `:47–48` | Uma requisição para cada payload inválido | Sim |
| T2/AUT-13 erro HTTP | `:69` `expect(error).toBeInstanceOf(ApiError)`; `:70` `expect(error).toMatchObject({ status, message: expected })`; `:71` call times 1 | 400/500 conservam mensagem; 500 ilegível conserva fallback seguro | Sim, 6 casos |
| T2/AUT-13 rede | `:78` `expect(error).toBeInstanceOf(ApiError)`; `:79` `expect(error).toMatchObject({ status: 0, message })`; `:80` call times 1 | ApiError/status zero/texto existente e nenhuma repetição | Sim, 2 casos |

**Mapeamento reverso (Check C), todos os 52 casos.**

| Teste + assertion física | AC / critério | Manter |
| --- | --- | --- |
| `ApiAutenticacao.test.js:18–28`, `:22` objeto exato, `:23–27` rota/POST/payload, `:28` call times 1; dois wrappers × dois setores | AUT-06, transporte AUT-10 e Done-when T2 | Sim, 4 |
| `:43–49`, `:47–48` tipo/status/mensagem do erro e `:49` call times 1; dois wrappers × 18 payloads | AUT-06 campos inválidos e ausência de retry | Sim, 36 |
| `:52–58`, `:56–57` tipo/status/mensagem e `:58` call times 1; dois wrappers × dois textos ilegíveis | AUT-06 JSON ilegível | Sim, 4 |
| `:61–71`, `:69–70` erro exato e `:71` call times 1; dois wrappers × três erros | AUT-13 transporte e preservação T2 | Sim, 6 |
| `:74–80`, `:78–79` erro zero/texto exato e `:80` call times 1; dois wrappers | AUT-13 rede e preservação T2 | Sim, 2 |

- Checks A/B/C/D: completos. Assertions conferem os valores retornados/enviados e os erros concretos, além da contagem. Cada matriz deriva do contrato ou falha especificados. Segue padrões Vitest/Response existentes e coding-principles; nenhuma SPEC_DEVIATION.
- Veredito: AUT-06 e transporte T2 cobertos. T2 completo após gate.

## T3: Entrada compartilhada em App

### Pré-implementação

- Suposições: `App.entrar` grava token original e projeção id/nome/setor/email antes de trocar de tela. Cache anterior é restaurado por tentativa independente de cada chave. Boot com cache inválido anterior continua fora do escopo.
- Arquivos: `frontend/src/App.jsx`, `frontend/src/pages/Login.jsx`, novo `frontend/src/AppSessao.test.jsx`, evidence/spec/tasks desta feature. Callback do cadastro será ligado em T4.
- Sucesso: login manual passa pelo cache comum, reload mantém sessão e logout remove ambas as chaves. Falha na primeira ou segunda gravação mantém autenticação e mensagem exata, restaura cache anterior quando possível, sem outro POST. Gate React preserva 201 testes já existentes.
- Testes antes do código: seis casos, um ciclo login/reload/logout/login, quatro falhas de escrita (duas chaves × cache vazio/anterior) e uma recusa também na restauração.

### Gate e revisão

- Focal inicial: `npm.cmd test -- --run src/AppSessao.test.jsx`, 6 casos, 5 falhas esperadas de AUT-08, um passe de lifecycle; log `%TEMP%/autocadastro-t3-red.log`.
- Gate: `npm.cmd test`, exit 0, 207 passes (201 anteriores + 6), nove arquivos, zero falhas/skips; log `%TEMP%/autocadastro-t3-gate.log`.
- Implementação: cache movido de Login para App; os dois valores são gravados antes de setUsuario. Cada chave anterior é restaurada independentemente por tentativa. Nenhum teste anterior alterado/removido/ignorado; nenhuma SPEC_DEVIATION.
- Limites: reload em RTL é remontagem de App usando o mesmo storage. Reload real do navegador fica para T5. Recusa permanente do storage impede garantir restauração, conforme AUT-08.

**Mapeamento direto (Check A).** Prefixo físico `frontend/src/AppSessao.test.jsx`.

| Critério | arquivo:linha + assertion | Resultado da spec | Coberto |
| --- | --- | --- | --- |
| AUT-07 token original/cache quatro campos antes da tela | `AppSessao.test.jsx:15` `expect(localStorage.getItem("token")).toBe(session.token)`; `:16` `expect(JSON.parse(localStorage.getItem("usuario"))).toEqual(publicUser)` no handler GET; `:17` `expect(request.headers.Authorization).toBe(...)`; `:38` `expect(Object.keys(JSON.parse(localStorage.getItem("usuario"))).sort()).toEqual(["email", "id", "nome", "setor"])` | Token original, id 3/nome Ana/setor null/email fixture, sem senha/hash/papel, antes da consulta | Sim |
| AUT-07 abrir lista/reload | `:35` e `:42` heading `toBe("Gerenciamentos")`; `:43` token `toBe(session.token)`; `:44` usuário `toEqual(publicUser)`; `:45` login count `toHaveLength(1)` | Lista direta e remontagem usando a mesma sessão sem nova autenticação | Sim em RTL; T5 navegador pendente |
| AUT-08 duas escritas/caches | `:75` `expect((await screen.findByText(storageMessage)).textContent).toBe(storageMessage)` (literal `:8`); `:76` heading `toBe("Bem-vindo")`; `:77` lista `toBeNull()`; `:78` token `toBe(oldToken)`; `:79` usuario `toBe(oldUser)`; `:83` URLs `toEqual(["/usuarios/login"])` | Erro exato, tela mantida, restauração de vazio/anterior, uma autenticação | Sim, quatro casos |
| AUT-08 campos/controle em erro | `:80` email `toBe(publicUser.email)`; `:81` senha `toBe("senha-ficticia")`; `:82` Entrar disabled `toBe(false)` | Rascunho e login utilizável após falha | Sim |
| AUT-08 restauração recusada | `:92` erro exato; `:93–94` autenticação/lista ausente; `:95` `expect(setItem.mock.calls).toEqual([["token", session.token], ["token", "anterior"], ["usuario", JSON.stringify(publicUser)]])`; `:96–97` valores anteriores; `:98` uma URL | Tenta ambas as chaves mesmo com recusa, mantém tela e erro | Sim |
| AUT-09 login/logout/login | `:47` Bem-vindo; `:48` `expect(localStorage.getItem("token")).toBeNull()`; `:49` usuario `toBeNull()`; `:51` Gerenciamentos; `:52` usuário `toEqual(publicUser)`; `:53` login count 2 | Saída remove sessão; credenciais válidas entram novamente | Sim |

**Mapeamento reverso (Check C), todos os seis casos.**

| Teste + assertion física | AC / caso | Manter |
| --- | --- | --- |
| `AppSessao.test.jsx:32`, `:15–17` cache/GET, `:35–45` lista/cache/reload, `:47–53` logout e novo login | AUT-07/09 lifecycle | Sim, 1 |
| `:56` token/usuario × vazio/anterior; `:75` erro, `:76–79` tela/cache, `:80–82` rascunho/liberação, `:83` uma requisição | AUT-08 primeira/segunda escrita e cache prévio | Sim, 4 |
| `:86`, `:92–98` erro/tela/tentativas/cache/requisição, `:99` controle liberado | AUT-08 limite de restauração por tentativa | Sim, 1 |

- Checks A/B/C/D: completos no escopo T3. Projeção é verificada como objeto exato, com os quatro valores e ausência de extras. Falhas verificam estado final e mensagem, não só chamadas. Padrões RTL/Vitest existentes e coding-principles seguidos.
- Veredito: T3 completo; ligação do cadastro e navegador seguem T4/T5.
