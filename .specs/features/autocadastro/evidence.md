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
