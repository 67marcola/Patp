# Evidências de execução dos gerenciamentos

Este registro pertence aos autores. Não substitui o relatório do Verificador independente.

## T2

Gate `mvn.cmd -B test`: PASS, 8 testes totais, zero falhas/erros/ignorados. Teste vermelho primeiro: 7 casos, 3 falhas e 3 erros antes da implementação. T2 cobre identidade/cadastro; autorização administrativa no CRUD será exercitada em T4.

| Critério / âncora | Evidência e expressão da assertion | Resultado exigido | Coberto? |
| --- | --- | --- | --- |
| GER-24/27 | `sistema/src/test/java/com/patp/sistema/UsuarioIdentityTests.java:29`: `.andExpect(status().isOk())` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-24/27 | `sistema/src/test/java/com/patp/sistema/UsuarioIdentityTests.java:30`: `.andExpect(jsonPath("$.papel").value("FUNCIONARIO"))` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-24/27 | `sistema/src/test/java/com/patp/sistema/UsuarioIdentityTests.java:31`: `.andExpect(jsonPath("$..senha").doesNotExist())` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-24/27 | `sistema/src/test/java/com/patp/sistema/UsuarioIdentityTests.java:34`: `assertThat(jdbc.queryForObject("select papel from usuarios where id=?", String.class, salvo.getId())).isEqualTo("FUNCIONARIO");` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-24/27 | `sistema/src/test/java/com/patp/sistema/UsuarioIdentityTests.java:35`: `assertThat(salvo.getSenha()).isNotEqualTo("senha-ficticia");` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-24/27 | `sistema/src/test/java/com/patp/sistema/UsuarioIdentityTests.java:36`: `assertThat(resposta).doesNotContain(salvo.getSenha());` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-25 | `sistema/src/test/java/com/patp/sistema/UsuarioIdentityTests.java:44`: `.andExpect(status().isBadRequest());` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-25 | `sistema/src/test/java/com/patp/sistema/UsuarioIdentityTests.java:45`: `assertThat(usuarios.count()).isEqualTo(1);` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-25 | `sistema/src/test/java/com/patp/sistema/UsuarioIdentityTests.java:47`: `assertThat(preservado.getNome()).isEqualTo("Original");` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-25 | `sistema/src/test/java/com/patp/sistema/UsuarioIdentityTests.java:48`: `assertThat(preservado.getEmail()).isEqualTo("original@example.test");` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-25 | `sistema/src/test/java/com/patp/sistema/UsuarioIdentityTests.java:49`: `assertThat(preservado.getSenha()).isEqualTo("hash-ficticio-nao-publico");` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-25 | `sistema/src/test/java/com/patp/sistema/UsuarioIdentityTests.java:56`: `.andExpect(status().isBadRequest());` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-25 | `sistema/src/test/java/com/patp/sistema/UsuarioIdentityTests.java:57`: `assertThat(usuarios.count()).isZero();` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-26 | `sistema/src/test/java/com/patp/sistema/UsuarioIdentityTests.java:66`: `.andExpect(status().isOk())` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-26 | `sistema/src/test/java/com/patp/sistema/UsuarioIdentityTests.java:67`: `.andExpect(jsonPath("$.id").value(escolhido.getId()))` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-26 | `sistema/src/test/java/com/patp/sistema/UsuarioIdentityTests.java:68`: `.andExpect(jsonPath("$.papel").value("ADMINISTRADOR"))` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-26 | `sistema/src/test/java/com/patp/sistema/UsuarioIdentityTests.java:69`: `.andExpect(jsonPath("$..senha").doesNotExist());` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| T2 legado | `sistema/src/test/java/com/patp/sistema/UsuarioIdentityTests.java:77`: `.andExpect(status().isOk())` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| T2 legado | `sistema/src/test/java/com/patp/sistema/UsuarioIdentityTests.java:78`: `.andExpect(jsonPath("$.papel").value("FUNCIONARIO"));` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-27 | `sistema/src/test/java/com/patp/sistema/UsuarioIdentityTests.java:91`: `.andExpect(status().isOk())` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-27 | `sistema/src/test/java/com/patp/sistema/UsuarioIdentityTests.java:92`: `.andExpect(jsonPath("$..senha").doesNotExist())` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-27 | `sistema/src/test/java/com/patp/sistema/UsuarioIdentityTests.java:94`: `assertThat(resposta).doesNotContain(criador.getSenha());` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-23 | `sistema/src/test/java/com/patp/sistema/UsuarioIdentityTests.java:100`: `mvc.perform(get("/api/gerenciamentos")).andExpect(status().isUnauthorized());` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-23 | `sistema/src/test/java/com/patp/sistema/UsuarioIdentityTests.java:102`: `.andExpect(status().isUnauthorized());` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |

| Assertion | Âncora | Manter? |
| --- | --- | --- |
| `sistema/src/test/java/com/patp/sistema/UsuarioIdentityTests.java:29`: `.andExpect(status().isOk())` | GER-24/27 | Sim |
| `sistema/src/test/java/com/patp/sistema/UsuarioIdentityTests.java:30`: `.andExpect(jsonPath("$.papel").value("FUNCIONARIO"))` | GER-24/27 | Sim |
| `sistema/src/test/java/com/patp/sistema/UsuarioIdentityTests.java:31`: `.andExpect(jsonPath("$..senha").doesNotExist())` | GER-24/27 | Sim |
| `sistema/src/test/java/com/patp/sistema/UsuarioIdentityTests.java:34`: `assertThat(jdbc.queryForObject("select papel from usuarios where id=?", String.class, salvo.getId())).isEqualTo("FUNCIONARIO");` | GER-24/27 | Sim |
| `sistema/src/test/java/com/patp/sistema/UsuarioIdentityTests.java:35`: `assertThat(salvo.getSenha()).isNotEqualTo("senha-ficticia");` | GER-24/27 | Sim |
| `sistema/src/test/java/com/patp/sistema/UsuarioIdentityTests.java:36`: `assertThat(resposta).doesNotContain(salvo.getSenha());` | GER-24/27 | Sim |
| `sistema/src/test/java/com/patp/sistema/UsuarioIdentityTests.java:44`: `.andExpect(status().isBadRequest());` | GER-25 | Sim |
| `sistema/src/test/java/com/patp/sistema/UsuarioIdentityTests.java:45`: `assertThat(usuarios.count()).isEqualTo(1);` | GER-25 | Sim |
| `sistema/src/test/java/com/patp/sistema/UsuarioIdentityTests.java:47`: `assertThat(preservado.getNome()).isEqualTo("Original");` | GER-25 | Sim |
| `sistema/src/test/java/com/patp/sistema/UsuarioIdentityTests.java:48`: `assertThat(preservado.getEmail()).isEqualTo("original@example.test");` | GER-25 | Sim |
| `sistema/src/test/java/com/patp/sistema/UsuarioIdentityTests.java:49`: `assertThat(preservado.getSenha()).isEqualTo("hash-ficticio-nao-publico");` | GER-25 | Sim |
| `sistema/src/test/java/com/patp/sistema/UsuarioIdentityTests.java:56`: `.andExpect(status().isBadRequest());` | GER-25 | Sim |
| `sistema/src/test/java/com/patp/sistema/UsuarioIdentityTests.java:57`: `assertThat(usuarios.count()).isZero();` | GER-25 | Sim |
| `sistema/src/test/java/com/patp/sistema/UsuarioIdentityTests.java:66`: `.andExpect(status().isOk())` | GER-26 | Sim |
| `sistema/src/test/java/com/patp/sistema/UsuarioIdentityTests.java:67`: `.andExpect(jsonPath("$.id").value(escolhido.getId()))` | GER-26 | Sim |
| `sistema/src/test/java/com/patp/sistema/UsuarioIdentityTests.java:68`: `.andExpect(jsonPath("$.papel").value("ADMINISTRADOR"))` | GER-26 | Sim |
| `sistema/src/test/java/com/patp/sistema/UsuarioIdentityTests.java:69`: `.andExpect(jsonPath("$..senha").doesNotExist());` | GER-26 | Sim |
| `sistema/src/test/java/com/patp/sistema/UsuarioIdentityTests.java:77`: `.andExpect(status().isOk())` | T2 legado | Sim |
| `sistema/src/test/java/com/patp/sistema/UsuarioIdentityTests.java:78`: `.andExpect(jsonPath("$.papel").value("FUNCIONARIO"));` | T2 legado | Sim |
| `sistema/src/test/java/com/patp/sistema/UsuarioIdentityTests.java:91`: `.andExpect(status().isOk())` | GER-27 | Sim |
| `sistema/src/test/java/com/patp/sistema/UsuarioIdentityTests.java:92`: `.andExpect(jsonPath("$..senha").doesNotExist())` | GER-27 | Sim |
| `sistema/src/test/java/com/patp/sistema/UsuarioIdentityTests.java:94`: `assertThat(resposta).doesNotContain(criador.getSenha());` | GER-27 | Sim |
| `sistema/src/test/java/com/patp/sistema/UsuarioIdentityTests.java:100`: `mvc.perform(get("/api/gerenciamentos")).andExpect(status().isUnauthorized());` | GER-23 | Sim |
| `sistema/src/test/java/com/patp/sistema/UsuarioIdentityTests.java:102`: `.andExpect(status().isUnauthorized());` | GER-23 | Sim |

Adequação: assertions sobre resposta HTTP e/ou estado persistido; sem skip/deleção/enfraquecimento; sem desvio de spec. Nenhuma guideline adicional foi encontrada; aplicada a matriz de tasks.md. Gate e inspeção de suficiência/necessidade aprovados; verificação independente da feature ainda pendente.

## T3

Gate `mvn.cmd -B test`: PASS, 12 testes totais, zero falhas/erros/ignorados. Persistencia do modelo e projeções verificadas; contratos HTTP e guarda serão fechados em T4–T8. A versão JPA nasce nula no objeto novo e recebe zero na persistência para preservar detecção de entidade nova.

| Critério / âncora | Evidência e expressão da assertion | Resultado exigido | Coberto? |
| --- | --- | --- | --- |
| T3 GER-08/10 legado | `sistema/src/test/java/com/patp/sistema/GerenciamentoPersistenceTests.java:32`: `assertThat(persistido.isArquivado()).isFalse();` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| T3 GER-08/10 legado | `sistema/src/test/java/com/patp/sistema/GerenciamentoPersistenceTests.java:33`: `assertThat(persistido.getVersao()).isZero();` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| T3 GER-08/10 legado | `sistema/src/test/java/com/patp/sistema/GerenciamentoPersistenceTests.java:34`: `assertThat(persistido.getCriador()).isNull();` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| T3 GER-08/09/10 legado | `sistema/src/test/java/com/patp/sistema/GerenciamentoPersistenceTests.java:47`: `assertThat(quadros.listarPorEstado(false)).extracting(Gerenciamento::getId).containsExactly(legado.getId());` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| T3 GER-08/09/10 legado | `sistema/src/test/java/com/patp/sistema/GerenciamentoPersistenceTests.java:48`: `assertThat(quadros.listarPorEstado(true)).extracting(Gerenciamento::getId).containsExactly(arquivo.getId());` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| T3 GER-08/09/10 legado | `sistema/src/test/java/com/patp/sistema/GerenciamentoPersistenceTests.java:50`: `assertThat(preservado.getNome()).isEqualTo("L".repeat(180));` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| T3 GER-08/09/10 legado | `sistema/src/test/java/com/patp/sistema/GerenciamentoPersistenceTests.java:51`: `assertThat(preservado.getCriador()).isNull();` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| T3 GER-08/09/10 legado | `sistema/src/test/java/com/patp/sistema/GerenciamentoPersistenceTests.java:52`: `assertThat(preservado.isArquivado()).isFalse();` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| T3 GER-16/17/18/22 | `sistema/src/test/java/com/patp/sistema/GerenciamentoPersistenceTests.java:64`: `assertThat(bloqueado.getVersao()).isEqualTo(1L);` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| T3 GER-16/17/18/22 | `sistema/src/test/java/com/patp/sistema/GerenciamentoPersistenceTests.java:67`: `assertThat(salvo.isArquivado()).isTrue();` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| T3 GER-16/17/18/22 | `sistema/src/test/java/com/patp/sistema/GerenciamentoPersistenceTests.java:68`: `assertThat(salvo.getVersao()).isEqualTo(1L);` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| T3 GER-20/22 guarda | `sistema/src/test/java/com/patp/sistema/GerenciamentoPersistenceTests.java:86`: `assertThat(etapas.buscarGerenciamentoId(etapa.getId())).contains(quadro.getId());` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| T3 GER-20/22 guarda | `sistema/src/test/java/com/patp/sistema/GerenciamentoPersistenceTests.java:87`: `assertThat(processos.buscarGerenciamentoId(demanda.getId())).contains(quadro.getId());` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| T3 GER-20/22 guarda | `sistema/src/test/java/com/patp/sistema/GerenciamentoPersistenceTests.java:88`: `assertThat(etapas.buscarGerenciamentoId(-1L)).isEmpty();` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| T3 GER-20/22 guarda | `sistema/src/test/java/com/patp/sistema/GerenciamentoPersistenceTests.java:89`: `assertThat(processos.buscarGerenciamentoId(-1L)).isEmpty();` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |

| Assertion | Âncora | Manter? |
| --- | --- | --- |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoPersistenceTests.java:32`: `assertThat(persistido.isArquivado()).isFalse();` | T3 GER-08/10 legado | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoPersistenceTests.java:33`: `assertThat(persistido.getVersao()).isZero();` | T3 GER-08/10 legado | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoPersistenceTests.java:34`: `assertThat(persistido.getCriador()).isNull();` | T3 GER-08/10 legado | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoPersistenceTests.java:47`: `assertThat(quadros.listarPorEstado(false)).extracting(Gerenciamento::getId).containsExactly(legado.getId());` | T3 GER-08/09/10 legado | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoPersistenceTests.java:48`: `assertThat(quadros.listarPorEstado(true)).extracting(Gerenciamento::getId).containsExactly(arquivo.getId());` | T3 GER-08/09/10 legado | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoPersistenceTests.java:50`: `assertThat(preservado.getNome()).isEqualTo("L".repeat(180));` | T3 GER-08/09/10 legado | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoPersistenceTests.java:51`: `assertThat(preservado.getCriador()).isNull();` | T3 GER-08/09/10 legado | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoPersistenceTests.java:52`: `assertThat(preservado.isArquivado()).isFalse();` | T3 GER-08/09/10 legado | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoPersistenceTests.java:64`: `assertThat(bloqueado.getVersao()).isEqualTo(1L);` | T3 GER-16/17/18/22 | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoPersistenceTests.java:67`: `assertThat(salvo.isArquivado()).isTrue();` | T3 GER-16/17/18/22 | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoPersistenceTests.java:68`: `assertThat(salvo.getVersao()).isEqualTo(1L);` | T3 GER-16/17/18/22 | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoPersistenceTests.java:86`: `assertThat(etapas.buscarGerenciamentoId(etapa.getId())).contains(quadro.getId());` | T3 GER-20/22 guarda | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoPersistenceTests.java:87`: `assertThat(processos.buscarGerenciamentoId(demanda.getId())).contains(quadro.getId());` | T3 GER-20/22 guarda | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoPersistenceTests.java:88`: `assertThat(etapas.buscarGerenciamentoId(-1L)).isEmpty();` | T3 GER-20/22 guarda | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoPersistenceTests.java:89`: `assertThat(processos.buscarGerenciamentoId(-1L)).isEmpty();` | T3 GER-20/22 guarda | Sim |

Adequação: assertions sobre resposta HTTP e/ou estado persistido; sem skip/deleção/enfraquecimento; sem desvio de spec. Nenhuma guideline adicional foi encontrada; aplicada a matriz de tasks.md. Gate e inspeção de suficiência/necessidade aprovados; verificação independente da feature ainda pendente.

## T4

Gate `mvn.cmd -B verify; npm.cmd run build; npm.cmd exec -- oxlint src vite.config.js`: PASS, 49 testes totais, zero falhas/erros/ignorados. HTTP e banco reais; rollback por CHECK na segunda etapa, formato estrito, versões e preservação. Rodada vermelha inicial: 31 casos/30 falhas; ordem estrita revelou 2 falhas adicionais antes da correção.

| Critério / âncora | Evidência e expressão da assertion | Resultado exigido | Coberto? |
| --- | --- | --- | --- |
| GER-01/02/03/04/06/27 | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:38`: `.andExpect(status().isCreated())` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-01/02/03/04/06/27 | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:39`: `.andExpect(jsonPath("$.nome").value(nome))` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-01/02/03/04/06/27 | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:40`: `.andExpect(jsonPath("$.descricao").value("D".repeat(255)))` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-01/02/03/04/06/27 | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:41`: `.andExpect(jsonPath("$.criador.id").value(criador.getId()))` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-01/02/03/04/06/27 | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:42`: `.andExpect(jsonPath("$.criador.nome").value("Criador"))` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-01/02/03/04/06/27 | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:43`: `.andExpect(jsonPath("$.arquivado").value(false))` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-01/02/03/04/06/27 | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:44`: `.andExpect(jsonPath("$.versao").value(0))` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-01/02/03/04/06/27 | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:45`: `.andExpect(jsonPath("$.podeAdministrar").value(true))` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-01/02/03/04/06/27 | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:46`: `.andExpect(jsonPath("$..senha").doesNotExist());` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-01/02/03/04/06/27 | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:48`: `assertThat(salvo.getNome()).isEqualTo(nome);` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-01/02/03/04/06/27 | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:49`: `assertThat(salvo.getCriador().getId()).isEqualTo(criador.getId());` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-01/02/03/04/06/27 | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:50`: `assertThat(salvo.isArquivado()).isFalse();` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-05/06 | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:58`: `.andExpect(status().isCreated())` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-05/06 | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:59`: `.andExpect(jsonPath("$.descricao").value(""));` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-05/06 | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:62`: `assertThat(todos).hasSize(2);` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-05/06 | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:63`: `assertThat(todos.get(0).getId()).isNotEqualTo(todos.get(1).getId());` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-05/06 | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:65`: `assertThat(etapas.findByGerenciamentoIdOrderByOrdem(todos.get(0).getId())).hasSize(1);` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-05/06 | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:66`: `assertThat(etapas.findByGerenciamentoIdOrderByOrdem(todos.get(1).getId())).isEmpty();` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-03 | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:74`: `.andExpect(status().isBadRequest()).andExpect(jsonPath("$.erro").value(NOME_ERRO));` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-03 | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:75`: `assertThat(quadros.count()).isZero();` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-03 UTF16 | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:82`: `.andExpect(status().isCreated()).andExpect(jsonPath("$.nome").value("😀".repeat(60)));` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-03 UTF16 | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:84`: `.andExpect(status().isBadRequest()).andExpect(jsonPath("$.erro").value(NOME_ERRO));` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-03 UTF16 | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:85`: `assertThat(quadros.count()).isEqualTo(1);` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-04 | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:92`: `.andExpect(status().isBadRequest()).andExpect(jsonPath("$.erro").value("A descrição deve ter até 255 caracteres."));` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-04 | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:93`: `assertThat(quadros.count()).isZero();` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-07 | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:100`: `.andExpect(status().isBadRequest());` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-07 | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:101`: `assertThat(quadros.count()).isZero();` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-07 | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:102`: `assertThat(etapas.count()).isZero();` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-07 falha parcial | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:111`: `.andExpect(status().isInternalServerError()).andExpect(jsonPath("$.erro").value("Não foi possível concluir a operação."));` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-07 falha parcial | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:112`: `assertThat(quadros.count()).isZero();` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-07 falha parcial | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:113`: `assertThat(etapas.count()).isZero();` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-07 contrato etapas | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:124`: `.andExpect(status().isBadRequest());` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-07 contrato etapas | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:125`: `assertThat(quadros.count()).isZero();` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-07 contrato etapas | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:126`: `assertThat(etapas.count()).isZero();` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-08/09/10/26/27 | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:138`: `mvc.perform(get("/api/gerenciamentos").header("Authorization", token)).andExpect(status().isOk())` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-08/09/10/26/27 | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:139`: `.andExpect(jsonPath("$.length()").value(1)).andExpect(jsonPath("$[0].id").value(ativo.getId())).andExpect(jsonPath("$[0].podeAdministrar").value(false));` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-08/09/10/26/27 | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:140`: `mvc.perform(get("/api/gerenciamentos?arquivado=true").header("Authorization", token)).andExpect(status().isOk())` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-08/09/10/26/27 | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:141`: `.andExpect(jsonPath("$.length()").value(1)).andExpect(jsonPath("$[0].id").value(arquivo.getId())).andExpect(jsonPath("$[0].criador").isEmpty());` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-08/09/10/26/27 | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:142`: `mvc.perform(get("/api/gerenciamentos/" + arquivo.getId()).header("Authorization", token)).andExpect(status().isOk())` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-08/09/10/26/27 | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:143`: `.andExpect(jsonPath("$.arquivado").value(true)).andExpect(jsonPath("$.podeAdministrar").value(false));` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-08/09/10/26/27 | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:145`: `mvc.perform(get("/api/gerenciamentos/" + arquivo.getId()).header("Authorization", token)).andExpect(status().isOk())` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-08/09/10/26/27 | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:146`: `.andExpect(jsonPath("$.podeAdministrar").value(true));` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-13/14 | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:157`: `.andExpect(status().isForbidden()).andExpect(jsonPath("$.erro").value(PERMISSAO_ERRO));` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-13/14 | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:159`: `assertThat(preservado.getNome()).isEqualTo("Quadro");` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-13/14 | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:160`: `assertThat(preservado.isArquivado()).isFalse();` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-13/14 | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:161`: `assertThat(preservado.getVersao()).isZero();` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-12/14/15/26 | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:172`: `.andExpect(status().isOk()).andExpect(jsonPath("$.nome").value("Nome novo")).andExpect(jsonPath("$.criador").isEmpty()).andExpect(jsonPath("$.versao").value(1));` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-12/14/15/26 | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:173`: `assertThat(quadros.findById(legado.getId()).orElseThrow().getCriador()).isNull();` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-12/15/17/18/19/20 | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:186`: `.andExpect(status().isOk()).andExpect(jsonPath("$.nome").value("Novo")).andExpect(jsonPath("$.descricao").value("Editada"))` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-12/15/17/18/19/20 | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:187`: `.andExpect(jsonPath("$.versao").value(1)).andExpect(jsonPath("$.arquivado").value(false)).andExpect(jsonPath("$.criador.id").value(criador.getId()));` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-12/15/17/18/19/20 | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:188`: `assertThat(conteudoPersistido()).isEqualTo(conteudo);` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-12/15/17/18/19/20 | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:190`: `.andExpect(status().isNoContent()).andExpect(content().string(""));` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-12/15/17/18/19/20 | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:191`: `assertThat(quadros.findById(quadro.getId()).orElseThrow().isArquivado()).isTrue();` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-12/15/17/18/19/20 | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:192`: `assertThat(quadros.findById(quadro.getId()).orElseThrow().getVersao()).isEqualTo(2L);` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-12/15/17/18/19/20 | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:194`: `.andExpect(status().isNoContent());` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-12/15/17/18/19/20 | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:195`: `assertThat(quadros.findById(quadro.getId()).orElseThrow().getVersao()).isEqualTo(2L);` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-12/15/17/18/19/20 | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:197`: `.andExpect(status().isNoContent());` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-12/15/17/18/19/20 | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:199`: `.andExpect(status().isNoContent());` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-12/15/17/18/19/20 | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:201`: `assertThat(salvo.isArquivado()).isFalse();` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-12/15/17/18/19/20 | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:202`: `assertThat(salvo.getVersao()).isEqualTo(3L);` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-12/15/17/18/19/20 | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:203`: `assertThat(salvo.getNome()).isEqualTo("Novo");` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-12/15/17/18/19/20 | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:204`: `assertThat(salvo.getDescricao()).isEqualTo("Editada");` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-12/15/17/18/19/20 | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:205`: `assertThat(salvo.getCriador().getId()).isEqualTo(criador.getId());` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-12/15/17/18/19/20 | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:206`: `assertThat(conteudoPersistido()).isEqualTo(conteudo);` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-12/16 | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:215`: `.andExpect(status().isOk()).andExpect(jsonPath("$.versao").value(1));` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-12/16 | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:217`: `.andExpect(status().isConflict()).andExpect(jsonPath("$.erro").value(VERSAO_ERRO));` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-12/16 | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:218`: `assertThat(quadros.findById(quadro.getId()).orElseThrow().getNome()).isEqualTo("Quadro");` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-12/16 | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:219`: `assertThat(quadros.findById(quadro.getId()).orElseThrow().getVersao()).isEqualTo(1L);` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-16 | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:231`: `.andExpect(status().isConflict()).andExpect(jsonPath("$.erro").value(VERSAO_ERRO));` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-16 | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:232`: `assertThat(quadros.findById(quadro.getId()).orElseThrow().getNome()).isEqualTo("Versão nova");` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| contrato versão GER-16/17/18/19 | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:243`: `.andExpect(status().isBadRequest());` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| contrato versão GER-16/17/18/19 | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:245`: `assertThat(quadros.findById(quadro.getId()).orElseThrow().isArquivado()).isFalse();` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| contrato versão GER-16/17/18/19 | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:246`: `assertThat(quadros.findById(quadro.getId()).orElseThrow().getVersao()).isZero();` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-13/21 precedência | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:257`: `.andExpect(status().isForbidden()).andExpect(jsonPath("$.erro").value(PERMISSAO_ERRO));` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-13/21 precedência | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:259`: `.andExpect(status().isConflict()).andExpect(jsonPath("$.erro").value(ARQUIVO_ERRO));` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-13/21 precedência | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:260`: `assertThat(quadros.findById(quadro.getId()).orElseThrow().getNome()).isEqualTo("Quadro");` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-03/04/12 | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:271`: `.andExpect(status().isBadRequest()).andExpect(jsonPath("$.erro").value(NOME_ERRO));` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-03/04/12 | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:275`: `.andExpect(status().isBadRequest()).andExpect(jsonPath("$.erro").value("A descrição deve ter até 255 caracteres."));` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-03/04/12 | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:276`: `assertThat(quadros.findById(quadro.getId()).orElseThrow().getVersao()).isZero();` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-11/23 contratos JSON/filtro | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:282`: `mvc.perform(get("/api/gerenciamentos/999999").header("Authorization", token)).andExpect(status().isNotFound()).andExpect(jsonPath("$.erro").value("Gerenciamento não encontrado."));` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-11/23 contratos JSON/filtro | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:285`: `.andExpect(status().isNotFound()).andExpect(jsonPath("$.erro").value("Gerenciamento não encontrado."));` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-11/23 contratos JSON/filtro | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:287`: `.andExpect(status().isUnauthorized());` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-11/23 contratos JSON/filtro | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:289`: `mvc.perform(post("/api/gerenciamentos").contentType(MediaType.APPLICATION_JSON).content("{}")) .andExpect(status().isUnauthorized());` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-11/23 contratos JSON/filtro | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:290`: `mvc.perform(post("/api/gerenciamentos").header("Authorization", token).contentType(MediaType.APPLICATION_JSON).content("{")) .andExpect(status().isBadRequest());` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-11/23 contratos JSON/filtro | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:291`: `mvc.perform(get("/api/gerenciamentos?arquivado=talvez").header("Authorization", token)).andExpect(status().isBadRequest());` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |

| Assertion | Âncora | Manter? |
| --- | --- | --- |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:38`: `.andExpect(status().isCreated())` | GER-01/02/03/04/06/27 | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:39`: `.andExpect(jsonPath("$.nome").value(nome))` | GER-01/02/03/04/06/27 | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:40`: `.andExpect(jsonPath("$.descricao").value("D".repeat(255)))` | GER-01/02/03/04/06/27 | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:41`: `.andExpect(jsonPath("$.criador.id").value(criador.getId()))` | GER-01/02/03/04/06/27 | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:42`: `.andExpect(jsonPath("$.criador.nome").value("Criador"))` | GER-01/02/03/04/06/27 | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:43`: `.andExpect(jsonPath("$.arquivado").value(false))` | GER-01/02/03/04/06/27 | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:44`: `.andExpect(jsonPath("$.versao").value(0))` | GER-01/02/03/04/06/27 | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:45`: `.andExpect(jsonPath("$.podeAdministrar").value(true))` | GER-01/02/03/04/06/27 | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:46`: `.andExpect(jsonPath("$..senha").doesNotExist());` | GER-01/02/03/04/06/27 | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:48`: `assertThat(salvo.getNome()).isEqualTo(nome);` | GER-01/02/03/04/06/27 | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:49`: `assertThat(salvo.getCriador().getId()).isEqualTo(criador.getId());` | GER-01/02/03/04/06/27 | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:50`: `assertThat(salvo.isArquivado()).isFalse();` | GER-01/02/03/04/06/27 | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:58`: `.andExpect(status().isCreated())` | GER-05/06 | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:59`: `.andExpect(jsonPath("$.descricao").value(""));` | GER-05/06 | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:62`: `assertThat(todos).hasSize(2);` | GER-05/06 | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:63`: `assertThat(todos.get(0).getId()).isNotEqualTo(todos.get(1).getId());` | GER-05/06 | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:65`: `assertThat(etapas.findByGerenciamentoIdOrderByOrdem(todos.get(0).getId())).hasSize(1);` | GER-05/06 | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:66`: `assertThat(etapas.findByGerenciamentoIdOrderByOrdem(todos.get(1).getId())).isEmpty();` | GER-05/06 | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:74`: `.andExpect(status().isBadRequest()).andExpect(jsonPath("$.erro").value(NOME_ERRO));` | GER-03 | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:75`: `assertThat(quadros.count()).isZero();` | GER-03 | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:82`: `.andExpect(status().isCreated()).andExpect(jsonPath("$.nome").value("😀".repeat(60)));` | GER-03 UTF16 | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:84`: `.andExpect(status().isBadRequest()).andExpect(jsonPath("$.erro").value(NOME_ERRO));` | GER-03 UTF16 | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:85`: `assertThat(quadros.count()).isEqualTo(1);` | GER-03 UTF16 | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:92`: `.andExpect(status().isBadRequest()).andExpect(jsonPath("$.erro").value("A descrição deve ter até 255 caracteres."));` | GER-04 | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:93`: `assertThat(quadros.count()).isZero();` | GER-04 | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:100`: `.andExpect(status().isBadRequest());` | GER-07 | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:101`: `assertThat(quadros.count()).isZero();` | GER-07 | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:102`: `assertThat(etapas.count()).isZero();` | GER-07 | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:111`: `.andExpect(status().isInternalServerError()).andExpect(jsonPath("$.erro").value("Não foi possível concluir a operação."));` | GER-07 falha parcial | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:112`: `assertThat(quadros.count()).isZero();` | GER-07 falha parcial | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:113`: `assertThat(etapas.count()).isZero();` | GER-07 falha parcial | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:124`: `.andExpect(status().isBadRequest());` | GER-07 contrato etapas | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:125`: `assertThat(quadros.count()).isZero();` | GER-07 contrato etapas | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:126`: `assertThat(etapas.count()).isZero();` | GER-07 contrato etapas | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:138`: `mvc.perform(get("/api/gerenciamentos").header("Authorization", token)).andExpect(status().isOk())` | GER-08/09/10/26/27 | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:139`: `.andExpect(jsonPath("$.length()").value(1)).andExpect(jsonPath("$[0].id").value(ativo.getId())).andExpect(jsonPath("$[0].podeAdministrar").value(false));` | GER-08/09/10/26/27 | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:140`: `mvc.perform(get("/api/gerenciamentos?arquivado=true").header("Authorization", token)).andExpect(status().isOk())` | GER-08/09/10/26/27 | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:141`: `.andExpect(jsonPath("$.length()").value(1)).andExpect(jsonPath("$[0].id").value(arquivo.getId())).andExpect(jsonPath("$[0].criador").isEmpty());` | GER-08/09/10/26/27 | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:142`: `mvc.perform(get("/api/gerenciamentos/" + arquivo.getId()).header("Authorization", token)).andExpect(status().isOk())` | GER-08/09/10/26/27 | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:143`: `.andExpect(jsonPath("$.arquivado").value(true)).andExpect(jsonPath("$.podeAdministrar").value(false));` | GER-08/09/10/26/27 | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:145`: `mvc.perform(get("/api/gerenciamentos/" + arquivo.getId()).header("Authorization", token)).andExpect(status().isOk())` | GER-08/09/10/26/27 | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:146`: `.andExpect(jsonPath("$.podeAdministrar").value(true));` | GER-08/09/10/26/27 | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:157`: `.andExpect(status().isForbidden()).andExpect(jsonPath("$.erro").value(PERMISSAO_ERRO));` | GER-13/14 | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:159`: `assertThat(preservado.getNome()).isEqualTo("Quadro");` | GER-13/14 | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:160`: `assertThat(preservado.isArquivado()).isFalse();` | GER-13/14 | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:161`: `assertThat(preservado.getVersao()).isZero();` | GER-13/14 | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:172`: `.andExpect(status().isOk()).andExpect(jsonPath("$.nome").value("Nome novo")).andExpect(jsonPath("$.criador").isEmpty()).andExpect(jsonPath("$.versao").value(1));` | GER-12/14/15/26 | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:173`: `assertThat(quadros.findById(legado.getId()).orElseThrow().getCriador()).isNull();` | GER-12/14/15/26 | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:186`: `.andExpect(status().isOk()).andExpect(jsonPath("$.nome").value("Novo")).andExpect(jsonPath("$.descricao").value("Editada"))` | GER-12/15/17/18/19/20 | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:187`: `.andExpect(jsonPath("$.versao").value(1)).andExpect(jsonPath("$.arquivado").value(false)).andExpect(jsonPath("$.criador.id").value(criador.getId()));` | GER-12/15/17/18/19/20 | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:188`: `assertThat(conteudoPersistido()).isEqualTo(conteudo);` | GER-12/15/17/18/19/20 | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:190`: `.andExpect(status().isNoContent()).andExpect(content().string(""));` | GER-12/15/17/18/19/20 | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:191`: `assertThat(quadros.findById(quadro.getId()).orElseThrow().isArquivado()).isTrue();` | GER-12/15/17/18/19/20 | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:192`: `assertThat(quadros.findById(quadro.getId()).orElseThrow().getVersao()).isEqualTo(2L);` | GER-12/15/17/18/19/20 | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:194`: `.andExpect(status().isNoContent());` | GER-12/15/17/18/19/20 | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:195`: `assertThat(quadros.findById(quadro.getId()).orElseThrow().getVersao()).isEqualTo(2L);` | GER-12/15/17/18/19/20 | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:197`: `.andExpect(status().isNoContent());` | GER-12/15/17/18/19/20 | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:199`: `.andExpect(status().isNoContent());` | GER-12/15/17/18/19/20 | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:201`: `assertThat(salvo.isArquivado()).isFalse();` | GER-12/15/17/18/19/20 | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:202`: `assertThat(salvo.getVersao()).isEqualTo(3L);` | GER-12/15/17/18/19/20 | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:203`: `assertThat(salvo.getNome()).isEqualTo("Novo");` | GER-12/15/17/18/19/20 | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:204`: `assertThat(salvo.getDescricao()).isEqualTo("Editada");` | GER-12/15/17/18/19/20 | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:205`: `assertThat(salvo.getCriador().getId()).isEqualTo(criador.getId());` | GER-12/15/17/18/19/20 | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:206`: `assertThat(conteudoPersistido()).isEqualTo(conteudo);` | GER-12/15/17/18/19/20 | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:215`: `.andExpect(status().isOk()).andExpect(jsonPath("$.versao").value(1));` | GER-12/16 | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:217`: `.andExpect(status().isConflict()).andExpect(jsonPath("$.erro").value(VERSAO_ERRO));` | GER-12/16 | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:218`: `assertThat(quadros.findById(quadro.getId()).orElseThrow().getNome()).isEqualTo("Quadro");` | GER-12/16 | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:219`: `assertThat(quadros.findById(quadro.getId()).orElseThrow().getVersao()).isEqualTo(1L);` | GER-12/16 | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:231`: `.andExpect(status().isConflict()).andExpect(jsonPath("$.erro").value(VERSAO_ERRO));` | GER-16 | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:232`: `assertThat(quadros.findById(quadro.getId()).orElseThrow().getNome()).isEqualTo("Versão nova");` | GER-16 | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:243`: `.andExpect(status().isBadRequest());` | contrato versão GER-16/17/18/19 | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:245`: `assertThat(quadros.findById(quadro.getId()).orElseThrow().isArquivado()).isFalse();` | contrato versão GER-16/17/18/19 | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:246`: `assertThat(quadros.findById(quadro.getId()).orElseThrow().getVersao()).isZero();` | contrato versão GER-16/17/18/19 | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:257`: `.andExpect(status().isForbidden()).andExpect(jsonPath("$.erro").value(PERMISSAO_ERRO));` | GER-13/21 precedência | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:259`: `.andExpect(status().isConflict()).andExpect(jsonPath("$.erro").value(ARQUIVO_ERRO));` | GER-13/21 precedência | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:260`: `assertThat(quadros.findById(quadro.getId()).orElseThrow().getNome()).isEqualTo("Quadro");` | GER-13/21 precedência | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:271`: `.andExpect(status().isBadRequest()).andExpect(jsonPath("$.erro").value(NOME_ERRO));` | GER-03/04/12 | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:275`: `.andExpect(status().isBadRequest()).andExpect(jsonPath("$.erro").value("A descrição deve ter até 255 caracteres."));` | GER-03/04/12 | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:276`: `assertThat(quadros.findById(quadro.getId()).orElseThrow().getVersao()).isZero();` | GER-03/04/12 | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:282`: `mvc.perform(get("/api/gerenciamentos/999999").header("Authorization", token)).andExpect(status().isNotFound()).andExpect(jsonPath("$.erro").value("Gerenciamento não encontrado."));` | GER-11/23 contratos JSON/filtro | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:285`: `.andExpect(status().isNotFound()).andExpect(jsonPath("$.erro").value("Gerenciamento não encontrado."));` | GER-11/23 contratos JSON/filtro | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:287`: `.andExpect(status().isUnauthorized());` | GER-11/23 contratos JSON/filtro | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:289`: `mvc.perform(post("/api/gerenciamentos").contentType(MediaType.APPLICATION_JSON).content("{}")) .andExpect(status().isUnauthorized());` | GER-11/23 contratos JSON/filtro | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:290`: `mvc.perform(post("/api/gerenciamentos").header("Authorization", token).contentType(MediaType.APPLICATION_JSON).content("{")) .andExpect(status().isBadRequest());` | GER-11/23 contratos JSON/filtro | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:291`: `mvc.perform(get("/api/gerenciamentos?arquivado=talvez").header("Authorization", token)).andExpect(status().isBadRequest());` | GER-11/23 contratos JSON/filtro | Sim |

Adequação: assertions sobre resposta HTTP e/ou estado persistido; sem skip/deleção/enfraquecimento; sem desvio de spec. Nenhuma guideline adicional foi encontrada; aplicada a matriz de tasks.md. Gate e inspeção de suficiência/necessidade aprovados; verificação independente da feature ainda pendente.

## T5

Gate `mvn.cmd -B test`: PASS, 55 testes totais, zero falhas/erros/ignorados. Seis casos T5: três rotas arquivadas, CRUD ativo por funcionário e duas ordens de concorrência coordenada. Rodada vermelha: cinco falhas; nenhuma assertion modificada. A segunda transação fica pendente até o commit da primeira, e status/conteúdo/estado final são verificados.

| Critério / âncora | Evidência e expressão da assertion | Resultado exigido | Coberto? |
| --- | --- | --- | --- |
| GER-20/21 | `sistema/src/test/java/com/patp/sistema/EtapaArchiveTests.java:55`: `.andExpect(status().isConflict()).andExpect(jsonPath("$.erro").value(ERRO));` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-20/21 | `sistema/src/test/java/com/patp/sistema/EtapaArchiveTests.java:56`: `assertThat(conteudoPersistido()).isEqualTo(antes);` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-20/21 | `sistema/src/test/java/com/patp/sistema/EtapaArchiveTests.java:57`: `mvc.perform(get(base).header("Authorization", token(admin))).andExpect(status().isOk())` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-20/21 | `sistema/src/test/java/com/patp/sistema/EtapaArchiveTests.java:58`: `.andExpect(jsonPath("$.length()").value(1)).andExpect(jsonPath("$[0].id").value(etapa.getId()))` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-20/21 | `sistema/src/test/java/com/patp/sistema/EtapaArchiveTests.java:59`: `.andExpect(jsonPath("$[0].nome").value("Original"));` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-21 comportamento ativo | `sistema/src/test/java/com/patp/sistema/EtapaArchiveTests.java:68`: `.andExpect(status().isOk()).andExpect(jsonPath("$.nome").value("Mudada"));` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-21 comportamento ativo | `sistema/src/test/java/com/patp/sistema/EtapaArchiveTests.java:70`: `assertThat(etapa.getGerenciamento().getId()).isEqualTo(quadro.getId());` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-21 comportamento ativo | `sistema/src/test/java/com/patp/sistema/EtapaArchiveTests.java:73`: `.andExpect(status().isOk()).andExpect(jsonPath("$.nome").value("Editada")).andExpect(jsonPath("$.ordem").value(3));` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-21 comportamento ativo | `sistema/src/test/java/com/patp/sistema/EtapaArchiveTests.java:74`: `assertThat(etapas.findById(etapa.getId()).orElseThrow().getNome()).isEqualTo("Editada");` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-21 comportamento ativo | `sistema/src/test/java/com/patp/sistema/EtapaArchiveTests.java:75`: `mvc.perform(delete(base + "/" + etapa.getId()).header("Authorization", token)).andExpect(status().isOk());` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-21 comportamento ativo | `sistema/src/test/java/com/patp/sistema/EtapaArchiveTests.java:76`: `assertThat(etapas.count()).isZero();` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| private void disputar(boolean arquivoPrimeiro) throws Exception { | `sistema/src/test/java/com/patp/sistema/EtapaArchiveTests.java:111`: `assertThat(primeiraEscrita.await(10, TimeUnit.SECONDS)).isTrue();` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| private void disputar(boolean arquivoPrimeiro) throws Exception { | `sistema/src/test/java/com/patp/sistema/EtapaArchiveTests.java:120`: `assertThat(segundaIniciada.await(10, TimeUnit.SECONDS)).isTrue();` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| private void disputar(boolean arquivoPrimeiro) throws Exception { | `sistema/src/test/java/com/patp/sistema/EtapaArchiveTests.java:125`: `assertThat(resposta.getStatus()).isEqualTo(arquivoPrimeiro ? 409 : 204);` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| private void disputar(boolean arquivoPrimeiro) throws Exception { | `sistema/src/test/java/com/patp/sistema/EtapaArchiveTests.java:127`: `assertThat(resposta.getContentAsString(java.nio.charset.StandardCharsets.UTF_8)).contains(ERRO);` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| private void disputar(boolean arquivoPrimeiro) throws Exception { | `sistema/src/test/java/com/patp/sistema/EtapaArchiveTests.java:129`: `assertThat(etapas.count()).isEqualTo(arquivoPrimeiro ? 0 : 1);` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| private void disputar(boolean arquivoPrimeiro) throws Exception { | `sistema/src/test/java/com/patp/sistema/EtapaArchiveTests.java:131`: `assertThat(etapas.findAll().get(0).getNome()).isEqualTo("Concorrente");` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| private void disputar(boolean arquivoPrimeiro) throws Exception { | `sistema/src/test/java/com/patp/sistema/EtapaArchiveTests.java:133`: `assertThat(quadros.findById(quadro.getId()).orElseThrow().isArquivado()).isTrue();` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| private void disputar(boolean arquivoPrimeiro) throws Exception { | `sistema/src/test/java/com/patp/sistema/EtapaArchiveTests.java:134`: `assertThat(quadros.findById(quadro.getId()).orElseThrow().getVersao()).isEqualTo(1L);` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |

| Assertion | Âncora | Manter? |
| --- | --- | --- |
| `sistema/src/test/java/com/patp/sistema/EtapaArchiveTests.java:55`: `.andExpect(status().isConflict()).andExpect(jsonPath("$.erro").value(ERRO));` | GER-20/21 | Sim |
| `sistema/src/test/java/com/patp/sistema/EtapaArchiveTests.java:56`: `assertThat(conteudoPersistido()).isEqualTo(antes);` | GER-20/21 | Sim |
| `sistema/src/test/java/com/patp/sistema/EtapaArchiveTests.java:57`: `mvc.perform(get(base).header("Authorization", token(admin))).andExpect(status().isOk())` | GER-20/21 | Sim |
| `sistema/src/test/java/com/patp/sistema/EtapaArchiveTests.java:58`: `.andExpect(jsonPath("$.length()").value(1)).andExpect(jsonPath("$[0].id").value(etapa.getId()))` | GER-20/21 | Sim |
| `sistema/src/test/java/com/patp/sistema/EtapaArchiveTests.java:59`: `.andExpect(jsonPath("$[0].nome").value("Original"));` | GER-20/21 | Sim |
| `sistema/src/test/java/com/patp/sistema/EtapaArchiveTests.java:68`: `.andExpect(status().isOk()).andExpect(jsonPath("$.nome").value("Mudada"));` | GER-21 comportamento ativo | Sim |
| `sistema/src/test/java/com/patp/sistema/EtapaArchiveTests.java:70`: `assertThat(etapa.getGerenciamento().getId()).isEqualTo(quadro.getId());` | GER-21 comportamento ativo | Sim |
| `sistema/src/test/java/com/patp/sistema/EtapaArchiveTests.java:73`: `.andExpect(status().isOk()).andExpect(jsonPath("$.nome").value("Editada")).andExpect(jsonPath("$.ordem").value(3));` | GER-21 comportamento ativo | Sim |
| `sistema/src/test/java/com/patp/sistema/EtapaArchiveTests.java:74`: `assertThat(etapas.findById(etapa.getId()).orElseThrow().getNome()).isEqualTo("Editada");` | GER-21 comportamento ativo | Sim |
| `sistema/src/test/java/com/patp/sistema/EtapaArchiveTests.java:75`: `mvc.perform(delete(base + "/" + etapa.getId()).header("Authorization", token)).andExpect(status().isOk());` | GER-21 comportamento ativo | Sim |
| `sistema/src/test/java/com/patp/sistema/EtapaArchiveTests.java:76`: `assertThat(etapas.count()).isZero();` | GER-21 comportamento ativo | Sim |
| `sistema/src/test/java/com/patp/sistema/EtapaArchiveTests.java:111`: `assertThat(primeiraEscrita.await(10, TimeUnit.SECONDS)).isTrue();` | private void disputar(boolean arquivoPrimeiro) throws Exception { | Sim |
| `sistema/src/test/java/com/patp/sistema/EtapaArchiveTests.java:120`: `assertThat(segundaIniciada.await(10, TimeUnit.SECONDS)).isTrue();` | private void disputar(boolean arquivoPrimeiro) throws Exception { | Sim |
| `sistema/src/test/java/com/patp/sistema/EtapaArchiveTests.java:125`: `assertThat(resposta.getStatus()).isEqualTo(arquivoPrimeiro ? 409 : 204);` | private void disputar(boolean arquivoPrimeiro) throws Exception { | Sim |
| `sistema/src/test/java/com/patp/sistema/EtapaArchiveTests.java:127`: `assertThat(resposta.getContentAsString(java.nio.charset.StandardCharsets.UTF_8)).contains(ERRO);` | private void disputar(boolean arquivoPrimeiro) throws Exception { | Sim |
| `sistema/src/test/java/com/patp/sistema/EtapaArchiveTests.java:129`: `assertThat(etapas.count()).isEqualTo(arquivoPrimeiro ? 0 : 1);` | private void disputar(boolean arquivoPrimeiro) throws Exception { | Sim |
| `sistema/src/test/java/com/patp/sistema/EtapaArchiveTests.java:131`: `assertThat(etapas.findAll().get(0).getNome()).isEqualTo("Concorrente");` | private void disputar(boolean arquivoPrimeiro) throws Exception { | Sim |
| `sistema/src/test/java/com/patp/sistema/EtapaArchiveTests.java:133`: `assertThat(quadros.findById(quadro.getId()).orElseThrow().isArquivado()).isTrue();` | private void disputar(boolean arquivoPrimeiro) throws Exception { | Sim |
| `sistema/src/test/java/com/patp/sistema/EtapaArchiveTests.java:134`: `assertThat(quadros.findById(quadro.getId()).orElseThrow().getVersao()).isEqualTo(1L);` | private void disputar(boolean arquivoPrimeiro) throws Exception { | Sim |

Adequação: assertions sobre resposta HTTP e/ou estado persistido; sem skip/deleção/enfraquecimento; sem desvio de spec. Nenhuma guideline adicional foi encontrada; aplicada a matriz de tasks.md. Gate e inspeção de suficiência/necessidade aprovados; verificação independente da feature ainda pendente.

## T6

Gate `mvn.cmd -B test`: PASS, 66 testes totais, zero falhas/erros/ignorados. Onze casos T6, onze falhas na rodada vermelha. As seis mutações arquivadas deixam snapshots integrais intactos; ID de POST não substitui registro; relações forjadas são resolvidas pelo vínculo persistido. DELETE ativo já falhava por FK de histórico: mantém 500 genérico com rollback, correção funcional fica para CRUD de demandas.

| Critério / âncora | Evidência e expressão da assertion | Resultado exigido | Coberto? |
| --- | --- | --- | --- |
| GER-20/21/22 matriz processos | `sistema/src/test/java/com/patp/sistema/ProcessoArchiveTests.java:43`: `.andExpect(status().isConflict()).andExpect(jsonPath("$.erro").value(ERRO));` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-20/21/22 matriz processos | `sistema/src/test/java/com/patp/sistema/ProcessoArchiveTests.java:44`: `assertThat(conteudoPersistido()).isEqualTo(antes);` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-20/21/22 matriz processos | `sistema/src/test/java/com/patp/sistema/ProcessoArchiveTests.java:45`: `mvc.perform(get(base).header("Authorization", token(criador))).andExpect(status().isOk())` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-20/21/22 matriz processos | `sistema/src/test/java/com/patp/sistema/ProcessoArchiveTests.java:46`: `.andExpect(jsonPath("$.id").value(demanda.getId())).andExpect(jsonPath("$.numeroProcesso").value(demanda.getNumeroProcesso()))` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-20/21/22 matriz processos | `sistema/src/test/java/com/patp/sistema/ProcessoArchiveTests.java:47`: `.andExpect(jsonPath("$.etapa.id").value(origem.getId())).andExpect(jsonPath("$..senha").doesNotExist());` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| contrato POST sem update | `sistema/src/test/java/com/patp/sistema/ProcessoArchiveTests.java:60`: `.andExpect(status().isBadRequest());` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| contrato POST sem update | `sistema/src/test/java/com/patp/sistema/ProcessoArchiveTests.java:61`: `assertThat(conteudoPersistido()).isEqualTo(antes);` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| contrato POST sem update | `sistema/src/test/java/com/patp/sistema/ProcessoArchiveTests.java:62`: `assertThat(processos.count()).isEqualTo(1);` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-21 vínculo persistido | `sistema/src/test/java/com/patp/sistema/ProcessoArchiveTests.java:77`: `.andExpect(status().isConflict()).andExpect(jsonPath("$.erro").value(ERRO));` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-21 vínculo persistido | `sistema/src/test/java/com/patp/sistema/ProcessoArchiveTests.java:78`: `assertThat(conteudoPersistido()).isEqualTo(antes);` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-21/22 comportamento ativo | `sistema/src/test/java/com/patp/sistema/ProcessoArchiveTests.java:91`: `.andExpect(status().isOk()).andExpect(jsonPath("$.etapa.gerenciamento.id").value(quadro.getId()));` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-21/22 comportamento ativo | `sistema/src/test/java/com/patp/sistema/ProcessoArchiveTests.java:96`: `.andExpect(status().isOk()).andExpect(jsonPath("$.numeroProcesso").value("Editado"));` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-21/22 comportamento ativo | `sistema/src/test/java/com/patp/sistema/ProcessoArchiveTests.java:97`: `mvc.perform(put(base + "/etapa/" + destino.getId()).header("Authorization", token)).andExpect(status().isOk())` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-21/22 comportamento ativo | `sistema/src/test/java/com/patp/sistema/ProcessoArchiveTests.java:98`: `.andExpect(jsonPath("$.etapa.id").value(destino.getId()));` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-21/22 comportamento ativo | `sistema/src/test/java/com/patp/sistema/ProcessoArchiveTests.java:99`: `mvc.perform(put(base + "/concluir").header("Authorization", token)).andExpect(status().isOk())` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-21/22 comportamento ativo | `sistema/src/test/java/com/patp/sistema/ProcessoArchiveTests.java:100`: `.andExpect(jsonPath("$.status").value("Concluido")).andExpect(jsonPath("$.dataConclusao").isNotEmpty());` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-21/22 comportamento ativo | `sistema/src/test/java/com/patp/sistema/ProcessoArchiveTests.java:102`: `.andExpect(status().isOk()).andExpect(jsonPath("$.status").value("Cancelado"))` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-21/22 comportamento ativo | `sistema/src/test/java/com/patp/sistema/ProcessoArchiveTests.java:103`: `.andExpect(jsonPath("$.motivoCancelamento").value("Solicitado")).andExpect(jsonPath("$.dataCancelamento").isNotEmpty());` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-21/22 comportamento ativo | `sistema/src/test/java/com/patp/sistema/ProcessoArchiveTests.java:105`: `assertThat(salvo.getNumeroProcesso()).isEqualTo("Editado");` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-21/22 comportamento ativo | `sistema/src/test/java/com/patp/sistema/ProcessoArchiveTests.java:106`: `assertThat(salvo.getEtapa().getId()).isEqualTo(destino.getId());` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-21/22 comportamento ativo | `sistema/src/test/java/com/patp/sistema/ProcessoArchiveTests.java:107`: `assertThat(salvo.getStatus()).isEqualTo("Cancelado");` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-21/22 comportamento ativo | `sistema/src/test/java/com/patp/sistema/ProcessoArchiveTests.java:108`: `assertThat(salvo.getMotivoCancelamento()).isEqualTo("Solicitado");` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-21/22 comportamento ativo | `sistema/src/test/java/com/patp/sistema/ProcessoArchiveTests.java:109`: `assertThat(jdbc.queryForList("select acao from historicos order by id", String.class))` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-21/22 comportamento ativo | `sistema/src/test/java/com/patp/sistema/ProcessoArchiveTests.java:111`: `assertThat(jdbc.queryForList("select usuario from historicos", String.class)).containsOnly("Outro");` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| consistência GER-22 limite ativo preexistente | `sistema/src/test/java/com/patp/sistema/ProcessoArchiveTests.java:120`: `.andExpect(status().isInternalServerError()).andExpect(jsonPath("$.erro").value("Não foi possível concluir a operação."));` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| consistência GER-22 limite ativo preexistente | `sistema/src/test/java/com/patp/sistema/ProcessoArchiveTests.java:121`: `assertThat(conteudoPersistido()).isEqualTo(antes);` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |

| Assertion | Âncora | Manter? |
| --- | --- | --- |
| `sistema/src/test/java/com/patp/sistema/ProcessoArchiveTests.java:43`: `.andExpect(status().isConflict()).andExpect(jsonPath("$.erro").value(ERRO));` | GER-20/21/22 matriz processos | Sim |
| `sistema/src/test/java/com/patp/sistema/ProcessoArchiveTests.java:44`: `assertThat(conteudoPersistido()).isEqualTo(antes);` | GER-20/21/22 matriz processos | Sim |
| `sistema/src/test/java/com/patp/sistema/ProcessoArchiveTests.java:45`: `mvc.perform(get(base).header("Authorization", token(criador))).andExpect(status().isOk())` | GER-20/21/22 matriz processos | Sim |
| `sistema/src/test/java/com/patp/sistema/ProcessoArchiveTests.java:46`: `.andExpect(jsonPath("$.id").value(demanda.getId())).andExpect(jsonPath("$.numeroProcesso").value(demanda.getNumeroProcesso()))` | GER-20/21/22 matriz processos | Sim |
| `sistema/src/test/java/com/patp/sistema/ProcessoArchiveTests.java:47`: `.andExpect(jsonPath("$.etapa.id").value(origem.getId())).andExpect(jsonPath("$..senha").doesNotExist());` | GER-20/21/22 matriz processos | Sim |
| `sistema/src/test/java/com/patp/sistema/ProcessoArchiveTests.java:60`: `.andExpect(status().isBadRequest());` | contrato POST sem update | Sim |
| `sistema/src/test/java/com/patp/sistema/ProcessoArchiveTests.java:61`: `assertThat(conteudoPersistido()).isEqualTo(antes);` | contrato POST sem update | Sim |
| `sistema/src/test/java/com/patp/sistema/ProcessoArchiveTests.java:62`: `assertThat(processos.count()).isEqualTo(1);` | contrato POST sem update | Sim |
| `sistema/src/test/java/com/patp/sistema/ProcessoArchiveTests.java:77`: `.andExpect(status().isConflict()).andExpect(jsonPath("$.erro").value(ERRO));` | GER-21 vínculo persistido | Sim |
| `sistema/src/test/java/com/patp/sistema/ProcessoArchiveTests.java:78`: `assertThat(conteudoPersistido()).isEqualTo(antes);` | GER-21 vínculo persistido | Sim |
| `sistema/src/test/java/com/patp/sistema/ProcessoArchiveTests.java:91`: `.andExpect(status().isOk()).andExpect(jsonPath("$.etapa.gerenciamento.id").value(quadro.getId()));` | GER-21/22 comportamento ativo | Sim |
| `sistema/src/test/java/com/patp/sistema/ProcessoArchiveTests.java:96`: `.andExpect(status().isOk()).andExpect(jsonPath("$.numeroProcesso").value("Editado"));` | GER-21/22 comportamento ativo | Sim |
| `sistema/src/test/java/com/patp/sistema/ProcessoArchiveTests.java:97`: `mvc.perform(put(base + "/etapa/" + destino.getId()).header("Authorization", token)).andExpect(status().isOk())` | GER-21/22 comportamento ativo | Sim |
| `sistema/src/test/java/com/patp/sistema/ProcessoArchiveTests.java:98`: `.andExpect(jsonPath("$.etapa.id").value(destino.getId()));` | GER-21/22 comportamento ativo | Sim |
| `sistema/src/test/java/com/patp/sistema/ProcessoArchiveTests.java:99`: `mvc.perform(put(base + "/concluir").header("Authorization", token)).andExpect(status().isOk())` | GER-21/22 comportamento ativo | Sim |
| `sistema/src/test/java/com/patp/sistema/ProcessoArchiveTests.java:100`: `.andExpect(jsonPath("$.status").value("Concluido")).andExpect(jsonPath("$.dataConclusao").isNotEmpty());` | GER-21/22 comportamento ativo | Sim |
| `sistema/src/test/java/com/patp/sistema/ProcessoArchiveTests.java:102`: `.andExpect(status().isOk()).andExpect(jsonPath("$.status").value("Cancelado"))` | GER-21/22 comportamento ativo | Sim |
| `sistema/src/test/java/com/patp/sistema/ProcessoArchiveTests.java:103`: `.andExpect(jsonPath("$.motivoCancelamento").value("Solicitado")).andExpect(jsonPath("$.dataCancelamento").isNotEmpty());` | GER-21/22 comportamento ativo | Sim |
| `sistema/src/test/java/com/patp/sistema/ProcessoArchiveTests.java:105`: `assertThat(salvo.getNumeroProcesso()).isEqualTo("Editado");` | GER-21/22 comportamento ativo | Sim |
| `sistema/src/test/java/com/patp/sistema/ProcessoArchiveTests.java:106`: `assertThat(salvo.getEtapa().getId()).isEqualTo(destino.getId());` | GER-21/22 comportamento ativo | Sim |
| `sistema/src/test/java/com/patp/sistema/ProcessoArchiveTests.java:107`: `assertThat(salvo.getStatus()).isEqualTo("Cancelado");` | GER-21/22 comportamento ativo | Sim |
| `sistema/src/test/java/com/patp/sistema/ProcessoArchiveTests.java:108`: `assertThat(salvo.getMotivoCancelamento()).isEqualTo("Solicitado");` | GER-21/22 comportamento ativo | Sim |
| `sistema/src/test/java/com/patp/sistema/ProcessoArchiveTests.java:109`: `assertThat(jdbc.queryForList("select acao from historicos order by id", String.class))` | GER-21/22 comportamento ativo | Sim |
| `sistema/src/test/java/com/patp/sistema/ProcessoArchiveTests.java:111`: `assertThat(jdbc.queryForList("select usuario from historicos", String.class)).containsOnly("Outro");` | GER-21/22 comportamento ativo | Sim |
| `sistema/src/test/java/com/patp/sistema/ProcessoArchiveTests.java:120`: `.andExpect(status().isInternalServerError()).andExpect(jsonPath("$.erro").value("Não foi possível concluir a operação."));` | consistência GER-22 limite ativo preexistente | Sim |
| `sistema/src/test/java/com/patp/sistema/ProcessoArchiveTests.java:121`: `assertThat(conteudoPersistido()).isEqualTo(antes);` | consistência GER-22 limite ativo preexistente | Sim |

Adequação: assertions sobre resposta HTTP e/ou estado persistido; sem skip/deleção/enfraquecimento; sem desvio de spec. Nenhuma guideline adicional foi encontrada; aplicada a matriz de tasks.md. Gate e inspeção de suficiência/necessidade aprovados; verificação independente da feature ainda pendente.

## T7

Gate `mvn.cmd -B test`: PASS, 69 testes totais, zero falhas/erros/ignorados. Três casos T7; duas falhas vermelhas antes da guarda. Comentários arquivados recusados para funcionário/admin com snapshot intacto; leitura mantém texto, pessoa e data. Escrita ativa preserva dois comentários e vínculos sem alterar demanda/quadro. Lock do pai precede carga de entidade.

| Critério / âncora | Evidência e expressão da assertion | Resultado exigido | Coberto? |
| --- | --- | --- | --- |
| GER-20/21/22 | `sistema/src/test/java/com/patp/sistema/ComentarioArchiveTests.java:38`: `.andExpect(status().isConflict()).andExpect(jsonPath("$.erro").value("Gerenciamento arquivado. Restaure-o antes de alterar."));` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-20/21/22 | `sistema/src/test/java/com/patp/sistema/ComentarioArchiveTests.java:39`: `assertThat(conteudoPersistido()).isEqualTo(antes);` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-20/21/22 | `sistema/src/test/java/com/patp/sistema/ComentarioArchiveTests.java:40`: `mvc.perform(get(rota).header("Authorization", token)).andExpect(status().isOk())` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-20/21/22 | `sistema/src/test/java/com/patp/sistema/ComentarioArchiveTests.java:41`: `.andExpect(jsonPath("$.length()").value(1)).andExpect(jsonPath("$[0].texto").value("Anterior"))` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-20/21/22 | `sistema/src/test/java/com/patp/sistema/ComentarioArchiveTests.java:42`: `.andExpect(jsonPath("$[0].funcionario").value("Pessoa")).andExpect(jsonPath("$[0].dataHora").isNotEmpty())` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-20/21/22 | `sistema/src/test/java/com/patp/sistema/ComentarioArchiveTests.java:43`: `.andExpect(jsonPath("$[0].processo.id").value(demanda.getId())).andExpect(jsonPath("$..senha").doesNotExist());` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-21 comportamento ativo | `sistema/src/test/java/com/patp/sistema/ComentarioArchiveTests.java:55`: `.andExpect(status().isOk()).andExpect(jsonPath("$.texto").value("Comentário de " + pessoa))` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-21 comportamento ativo | `sistema/src/test/java/com/patp/sistema/ComentarioArchiveTests.java:56`: `.andExpect(jsonPath("$.funcionario").value(pessoa)).andExpect(jsonPath("$.dataHora").isNotEmpty())` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-21 comportamento ativo | `sistema/src/test/java/com/patp/sistema/ComentarioArchiveTests.java:57`: `.andExpect(jsonPath("$.processo.id").value(demanda.getId()));` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-21 comportamento ativo | `sistema/src/test/java/com/patp/sistema/ComentarioArchiveTests.java:59`: `assertThat(jdbc.queryForList("select texto from comentarios order by id", String.class))` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-21 comportamento ativo | `sistema/src/test/java/com/patp/sistema/ComentarioArchiveTests.java:61`: `assertThat(jdbc.queryForList("select funcionario from comentarios order by id", String.class)).containsExactly("Ana", "Bia");` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-21 comportamento ativo | `sistema/src/test/java/com/patp/sistema/ComentarioArchiveTests.java:62`: `assertThat(jdbc.queryForObject("select count(*) from comentarios where data_hora is not null and processo_id=?", Long.class, demanda.getId())).isEqualTo(2L);` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-21 comportamento ativo | `sistema/src/test/java/com/patp/sistema/ComentarioArchiveTests.java:63`: `assertThat(processos.findById(demanda.getId()).orElseThrow().getStatus()).isEqualTo("Em andamento");` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-21 comportamento ativo | `sistema/src/test/java/com/patp/sistema/ComentarioArchiveTests.java:64`: `assertThat(quadros.findById(quadro.getId()).orElseThrow().getVersao()).isZero();` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |

| Assertion | Âncora | Manter? |
| --- | --- | --- |
| `sistema/src/test/java/com/patp/sistema/ComentarioArchiveTests.java:38`: `.andExpect(status().isConflict()).andExpect(jsonPath("$.erro").value("Gerenciamento arquivado. Restaure-o antes de alterar."));` | GER-20/21/22 | Sim |
| `sistema/src/test/java/com/patp/sistema/ComentarioArchiveTests.java:39`: `assertThat(conteudoPersistido()).isEqualTo(antes);` | GER-20/21/22 | Sim |
| `sistema/src/test/java/com/patp/sistema/ComentarioArchiveTests.java:40`: `mvc.perform(get(rota).header("Authorization", token)).andExpect(status().isOk())` | GER-20/21/22 | Sim |
| `sistema/src/test/java/com/patp/sistema/ComentarioArchiveTests.java:41`: `.andExpect(jsonPath("$.length()").value(1)).andExpect(jsonPath("$[0].texto").value("Anterior"))` | GER-20/21/22 | Sim |
| `sistema/src/test/java/com/patp/sistema/ComentarioArchiveTests.java:42`: `.andExpect(jsonPath("$[0].funcionario").value("Pessoa")).andExpect(jsonPath("$[0].dataHora").isNotEmpty())` | GER-20/21/22 | Sim |
| `sistema/src/test/java/com/patp/sistema/ComentarioArchiveTests.java:43`: `.andExpect(jsonPath("$[0].processo.id").value(demanda.getId())).andExpect(jsonPath("$..senha").doesNotExist());` | GER-20/21/22 | Sim |
| `sistema/src/test/java/com/patp/sistema/ComentarioArchiveTests.java:55`: `.andExpect(status().isOk()).andExpect(jsonPath("$.texto").value("Comentário de " + pessoa))` | GER-21 comportamento ativo | Sim |
| `sistema/src/test/java/com/patp/sistema/ComentarioArchiveTests.java:56`: `.andExpect(jsonPath("$.funcionario").value(pessoa)).andExpect(jsonPath("$.dataHora").isNotEmpty())` | GER-21 comportamento ativo | Sim |
| `sistema/src/test/java/com/patp/sistema/ComentarioArchiveTests.java:57`: `.andExpect(jsonPath("$.processo.id").value(demanda.getId()));` | GER-21 comportamento ativo | Sim |
| `sistema/src/test/java/com/patp/sistema/ComentarioArchiveTests.java:59`: `assertThat(jdbc.queryForList("select texto from comentarios order by id", String.class))` | GER-21 comportamento ativo | Sim |
| `sistema/src/test/java/com/patp/sistema/ComentarioArchiveTests.java:61`: `assertThat(jdbc.queryForList("select funcionario from comentarios order by id", String.class)).containsExactly("Ana", "Bia");` | GER-21 comportamento ativo | Sim |
| `sistema/src/test/java/com/patp/sistema/ComentarioArchiveTests.java:62`: `assertThat(jdbc.queryForObject("select count(*) from comentarios where data_hora is not null and processo_id=?", Long.class, demanda.getId())).isEqualTo(2L);` | GER-21 comportamento ativo | Sim |
| `sistema/src/test/java/com/patp/sistema/ComentarioArchiveTests.java:63`: `assertThat(processos.findById(demanda.getId()).orElseThrow().getStatus()).isEqualTo("Em andamento");` | GER-21 comportamento ativo | Sim |
| `sistema/src/test/java/com/patp/sistema/ComentarioArchiveTests.java:64`: `assertThat(quadros.findById(quadro.getId()).orElseThrow().getVersao()).isZero();` | GER-21 comportamento ativo | Sim |

Adequação: assertions sobre resposta HTTP e/ou estado persistido; sem skip/deleção/enfraquecimento; sem desvio de spec. Nenhuma guideline adicional foi encontrada; aplicada a matriz de tasks.md. Gate e inspeção de suficiência/necessidade aprovados; verificação independente da feature ainda pendente.

## T8

Gate `mvn.cmd -B verify; npm.cmd run build; npm.cmd exec -- oxlint src vite.config.js`: PASS, 95 testes totais, zero falhas/erros/ignorados. Quinze casos T8, duas falhas vermelhas antes da guarda. Matriz de doze rotas tem 401 sem gravação para sessão ausente/inválida; manual arquivado é 409 exato, consulta preservada, manual/automático ativo mantidos. Onze casos adicionais de adequação T4 reforçam admin alheio/nulo, homônimos com demandas, limites das etapas e ID retornado. Feature não concluída: frontend e Verifier pendentes; charset 401 e MySQL completo tratados pelo orquestrador após este lote.

| Critério / âncora | Evidência e expressão da assertion | Resultado exigido | Coberto? |
| --- | --- | --- | --- |
| GER-20/21/22/27 | `sistema/src/test/java/com/patp/sistema/HistoricoArchiveTests.java:38`: `.andExpect(status().isConflict()).andExpect(jsonPath("$.erro").value("Gerenciamento arquivado. Restaure-o antes de alterar."));` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-20/21/22/27 | `sistema/src/test/java/com/patp/sistema/HistoricoArchiveTests.java:39`: `assertThat(conteudoPersistido()).isEqualTo(antes);` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-20/21/22/27 | `sistema/src/test/java/com/patp/sistema/HistoricoArchiveTests.java:40`: `mvc.perform(get(rota).header("Authorization", token)).andExpect(status().isOk())` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-20/21/22/27 | `sistema/src/test/java/com/patp/sistema/HistoricoArchiveTests.java:41`: `.andExpect(jsonPath("$.length()").value(1)).andExpect(jsonPath("$[0].acao").value("CRIACAO"))` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-20/21/22/27 | `sistema/src/test/java/com/patp/sistema/HistoricoArchiveTests.java:42`: `.andExpect(jsonPath("$[0].descricao").value("Anterior")).andExpect(jsonPath("$[0].usuario").value("Pessoa"))` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-20/21/22/27 | `sistema/src/test/java/com/patp/sistema/HistoricoArchiveTests.java:43`: `.andExpect(jsonPath("$[0].dataHora").isNotEmpty()).andExpect(jsonPath("$[0].processo.id").value(demanda.getId()))` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-20/21/22/27 | `sistema/src/test/java/com/patp/sistema/HistoricoArchiveTests.java:44`: `.andExpect(jsonPath("$..senha").doesNotExist());` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-20/21/22 comportamento ativo | `sistema/src/test/java/com/patp/sistema/HistoricoArchiveTests.java:56`: `.andExpect(status().isOk()).andExpect(jsonPath("$.acao").value("MANUAL"))` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-20/21/22 comportamento ativo | `sistema/src/test/java/com/patp/sistema/HistoricoArchiveTests.java:57`: `.andExpect(jsonPath("$.descricao").value("Observação")).andExpect(jsonPath("$.usuario").value("Informado"))` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-20/21/22 comportamento ativo | `sistema/src/test/java/com/patp/sistema/HistoricoArchiveTests.java:58`: `.andExpect(jsonPath("$.dataHora").isNotEmpty()).andExpect(jsonPath("$.processo.id").value(demanda.getId()));` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-20/21/22 comportamento ativo | `sistema/src/test/java/com/patp/sistema/HistoricoArchiveTests.java:59`: `mvc.perform(put(base + "/concluir").header("Authorization", token)).andExpect(status().isOk())` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-20/21/22 comportamento ativo | `sistema/src/test/java/com/patp/sistema/HistoricoArchiveTests.java:60`: `.andExpect(jsonPath("$.status").value("Concluido"));` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-20/21/22 comportamento ativo | `sistema/src/test/java/com/patp/sistema/HistoricoArchiveTests.java:61`: `assertThat(jdbc.queryForList("select acao from historicos order by id", String.class)).containsExactly("MANUAL", "CONCLUSAO");` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-20/21/22 comportamento ativo | `sistema/src/test/java/com/patp/sistema/HistoricoArchiveTests.java:62`: `assertThat(jdbc.queryForList("select usuario from historicos order by id", String.class)).containsExactly("Informado", "Outro");` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-20/21/22 comportamento ativo | `sistema/src/test/java/com/patp/sistema/HistoricoArchiveTests.java:63`: `assertThat(jdbc.queryForObject("select count(*) from historicos where data_hora is not null and processo_id=?", Long.class, demanda.getId())).isEqualTo(2L);` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-20/21/22 comportamento ativo | `sistema/src/test/java/com/patp/sistema/HistoricoArchiveTests.java:66`: `.contentType(MediaType.APPLICATION_JSON).content("{\"versao\":0}")).andExpect(status().isNoContent());` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-20/21/22 comportamento ativo | `sistema/src/test/java/com/patp/sistema/HistoricoArchiveTests.java:67`: `mvc.perform(get(base + "/historico").header("Authorization", token)).andExpect(status().isOk())` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-20/21/22 comportamento ativo | `sistema/src/test/java/com/patp/sistema/HistoricoArchiveTests.java:68`: `.andExpect(jsonPath("$.length()").value(2)).andExpect(jsonPath("$[0].acao").value("MANUAL"))` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-20/21/22 comportamento ativo | `sistema/src/test/java/com/patp/sistema/HistoricoArchiveTests.java:69`: `.andExpect(jsonPath("$[1].acao").value("CONCLUSAO"));` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-20/21/22 comportamento ativo | `sistema/src/test/java/com/patp/sistema/HistoricoArchiveTests.java:70`: `assertThat(conteudoPersistido()).isEqualTo(antes);` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-23 matriz completa | `sistema/src/test/java/com/patp/sistema/HistoricoArchiveTests.java:105`: `mvc.perform(request).andExpect(status().isUnauthorized());` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-23 matriz completa | `sistema/src/test/java/com/patp/sistema/HistoricoArchiveTests.java:106`: `assertThat(conteudoPersistido()).isEqualTo(antes);` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-23 matriz completa | `sistema/src/test/java/com/patp/sistema/HistoricoArchiveTests.java:107`: `assertThat(quadros.findById(quadro.getId()).orElseThrow().getNome()).isEqualTo("Quadro");` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |
| GER-23 matriz completa | `sistema/src/test/java/com/patp/sistema/HistoricoArchiveTests.java:108`: `assertThat(quadros.findById(quadro.getId()).orElseThrow().getVersao()).isZero();` | Valor/estado/status expresso na assertion, derivado da âncora | Sim |

| Assertion | Âncora | Manter? |
| --- | --- | --- |
| `sistema/src/test/java/com/patp/sistema/HistoricoArchiveTests.java:38`: `.andExpect(status().isConflict()).andExpect(jsonPath("$.erro").value("Gerenciamento arquivado. Restaure-o antes de alterar."));` | GER-20/21/22/27 | Sim |
| `sistema/src/test/java/com/patp/sistema/HistoricoArchiveTests.java:39`: `assertThat(conteudoPersistido()).isEqualTo(antes);` | GER-20/21/22/27 | Sim |
| `sistema/src/test/java/com/patp/sistema/HistoricoArchiveTests.java:40`: `mvc.perform(get(rota).header("Authorization", token)).andExpect(status().isOk())` | GER-20/21/22/27 | Sim |
| `sistema/src/test/java/com/patp/sistema/HistoricoArchiveTests.java:41`: `.andExpect(jsonPath("$.length()").value(1)).andExpect(jsonPath("$[0].acao").value("CRIACAO"))` | GER-20/21/22/27 | Sim |
| `sistema/src/test/java/com/patp/sistema/HistoricoArchiveTests.java:42`: `.andExpect(jsonPath("$[0].descricao").value("Anterior")).andExpect(jsonPath("$[0].usuario").value("Pessoa"))` | GER-20/21/22/27 | Sim |
| `sistema/src/test/java/com/patp/sistema/HistoricoArchiveTests.java:43`: `.andExpect(jsonPath("$[0].dataHora").isNotEmpty()).andExpect(jsonPath("$[0].processo.id").value(demanda.getId()))` | GER-20/21/22/27 | Sim |
| `sistema/src/test/java/com/patp/sistema/HistoricoArchiveTests.java:44`: `.andExpect(jsonPath("$..senha").doesNotExist());` | GER-20/21/22/27 | Sim |
| `sistema/src/test/java/com/patp/sistema/HistoricoArchiveTests.java:56`: `.andExpect(status().isOk()).andExpect(jsonPath("$.acao").value("MANUAL"))` | GER-20/21/22 comportamento ativo | Sim |
| `sistema/src/test/java/com/patp/sistema/HistoricoArchiveTests.java:57`: `.andExpect(jsonPath("$.descricao").value("Observação")).andExpect(jsonPath("$.usuario").value("Informado"))` | GER-20/21/22 comportamento ativo | Sim |
| `sistema/src/test/java/com/patp/sistema/HistoricoArchiveTests.java:58`: `.andExpect(jsonPath("$.dataHora").isNotEmpty()).andExpect(jsonPath("$.processo.id").value(demanda.getId()));` | GER-20/21/22 comportamento ativo | Sim |
| `sistema/src/test/java/com/patp/sistema/HistoricoArchiveTests.java:59`: `mvc.perform(put(base + "/concluir").header("Authorization", token)).andExpect(status().isOk())` | GER-20/21/22 comportamento ativo | Sim |
| `sistema/src/test/java/com/patp/sistema/HistoricoArchiveTests.java:60`: `.andExpect(jsonPath("$.status").value("Concluido"));` | GER-20/21/22 comportamento ativo | Sim |
| `sistema/src/test/java/com/patp/sistema/HistoricoArchiveTests.java:61`: `assertThat(jdbc.queryForList("select acao from historicos order by id", String.class)).containsExactly("MANUAL", "CONCLUSAO");` | GER-20/21/22 comportamento ativo | Sim |
| `sistema/src/test/java/com/patp/sistema/HistoricoArchiveTests.java:62`: `assertThat(jdbc.queryForList("select usuario from historicos order by id", String.class)).containsExactly("Informado", "Outro");` | GER-20/21/22 comportamento ativo | Sim |
| `sistema/src/test/java/com/patp/sistema/HistoricoArchiveTests.java:63`: `assertThat(jdbc.queryForObject("select count(*) from historicos where data_hora is not null and processo_id=?", Long.class, demanda.getId())).isEqualTo(2L);` | GER-20/21/22 comportamento ativo | Sim |
| `sistema/src/test/java/com/patp/sistema/HistoricoArchiveTests.java:66`: `.contentType(MediaType.APPLICATION_JSON).content("{\"versao\":0}")).andExpect(status().isNoContent());` | GER-20/21/22 comportamento ativo | Sim |
| `sistema/src/test/java/com/patp/sistema/HistoricoArchiveTests.java:67`: `mvc.perform(get(base + "/historico").header("Authorization", token)).andExpect(status().isOk())` | GER-20/21/22 comportamento ativo | Sim |
| `sistema/src/test/java/com/patp/sistema/HistoricoArchiveTests.java:68`: `.andExpect(jsonPath("$.length()").value(2)).andExpect(jsonPath("$[0].acao").value("MANUAL"))` | GER-20/21/22 comportamento ativo | Sim |
| `sistema/src/test/java/com/patp/sistema/HistoricoArchiveTests.java:69`: `.andExpect(jsonPath("$[1].acao").value("CONCLUSAO"));` | GER-20/21/22 comportamento ativo | Sim |
| `sistema/src/test/java/com/patp/sistema/HistoricoArchiveTests.java:70`: `assertThat(conteudoPersistido()).isEqualTo(antes);` | GER-20/21/22 comportamento ativo | Sim |
| `sistema/src/test/java/com/patp/sistema/HistoricoArchiveTests.java:105`: `mvc.perform(request).andExpect(status().isUnauthorized());` | GER-23 matriz completa | Sim |
| `sistema/src/test/java/com/patp/sistema/HistoricoArchiveTests.java:106`: `assertThat(conteudoPersistido()).isEqualTo(antes);` | GER-23 matriz completa | Sim |
| `sistema/src/test/java/com/patp/sistema/HistoricoArchiveTests.java:107`: `assertThat(quadros.findById(quadro.getId()).orElseThrow().getNome()).isEqualTo("Quadro");` | GER-23 matriz completa | Sim |
| `sistema/src/test/java/com/patp/sistema/HistoricoArchiveTests.java:108`: `assertThat(quadros.findById(quadro.getId()).orElseThrow().getVersao()).isZero();` | GER-23 matriz completa | Sim |

Adequação: assertions sobre resposta HTTP e/ou estado persistido; sem skip/deleção/enfraquecimento; sem desvio de spec. Nenhuma guideline adicional foi encontrada; aplicada a matriz de tasks.md. Gate e inspeção de suficiência/necessidade aprovados; verificação independente da feature ainda pendente.

### Adequacao complementar da matriz T4 no gate T8

| Criterio e resultado da spec | Evidencia exata | Coberto? |
| --- | --- | --- |
| GER-17/18/20/26: admin, autoria alheia/nula; 204, estado true/false, versao 1/2, criador e snapshot intactos | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:309`: `.andExpect(status().isNoContent()).andExpect(content().string(""));` | Sim |
| GER-17/18/20/26: admin, autoria alheia/nula; 204, estado true/false, versao 1/2, criador e snapshot intactos | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:310`: `assertThat(quadros.findById(quadro.getId()).orElseThrow().isArquivado()).isTrue();` | Sim |
| GER-17/18/20/26: admin, autoria alheia/nula; 204, estado true/false, versao 1/2, criador e snapshot intactos | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:311`: `assertThat(quadros.findById(quadro.getId()).orElseThrow().getVersao()).isEqualTo(1L);` | Sim |
| GER-17/18/20/26: admin, autoria alheia/nula; 204, estado true/false, versao 1/2, criador e snapshot intactos | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:312`: `assertThat(conteudoPersistido()).isEqualTo(antes);` | Sim |
| GER-17/18/20/26: admin, autoria alheia/nula; 204, estado true/false, versao 1/2, criador e snapshot intactos | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:314`: `.andExpect(status().isNoContent()).andExpect(content().string(""));` | Sim |
| GER-17/18/20/26: admin, autoria alheia/nula; 204, estado true/false, versao 1/2, criador e snapshot intactos | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:316`: `assertThat(salvo.isArquivado()).isFalse();` | Sim |
| GER-17/18/20/26: admin, autoria alheia/nula; 204, estado true/false, versao 1/2, criador e snapshot intactos | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:317`: `assertThat(salvo.getVersao()).isEqualTo(2L);` | Sim |
| GER-17/18/20/26: admin, autoria alheia/nula; 204, estado true/false, versao 1/2, criador e snapshot intactos | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:318`: `assertThat(salvo.getNome()).isEqualTo("Quadro");` | Sim |
| GER-17/18/20/26: admin, autoria alheia/nula; 204, estado true/false, versao 1/2, criador e snapshot intactos | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:319`: `assertThat(salvo.getDescricao()).isEqualTo("Descrição");` | Sim |
| GER-17/18/20/26: admin, autoria alheia/nula; 204, estado true/false, versao 1/2, criador e snapshot intactos | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:320`: `assertThat(salvo.getCriador() == null ? null : salvo.getCriador().getId()).isEqualTo(semCriador ? null : criador.getId());` | Sim |
| GER-17/18/20/26: admin, autoria alheia/nula; 204, estado true/false, versao 1/2, criador e snapshot intactos | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:321`: `assertThat(conteudoPersistido()).isEqualTo(antes);` | Sim |
| GER-05/15/20: IDs e demandas independentes; outro quadro conserva nome/ativo/versao 0; snapshot intacto | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:330`: `.andExpect(status().isCreated());` | Sim |
| GER-05/15/20: IDs e demandas independentes; outro quadro conserva nome/ativo/versao 0; snapshot intacto | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:337`: `.content("{\"nome\":\"Editado\",\"versao\":0}")).andExpect(status().isOk());` | Sim |
| GER-05/15/20: IDs e demandas independentes; outro quadro conserva nome/ativo/versao 0; snapshot intacto | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:339`: `.content("{\"versao\":1}")).andExpect(status().isNoContent());` | Sim |
| GER-05/15/20: IDs e demandas independentes; outro quadro conserva nome/ativo/versao 0; snapshot intacto | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:340`: `assertThat(processos.findByEtapaId(primeira.getEtapa().getId())).extracting("id").containsExactly(primeira.getId());` | Sim |
| GER-05/15/20: IDs e demandas independentes; outro quadro conserva nome/ativo/versao 0; snapshot intacto | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:341`: `assertThat(processos.findByEtapaId(segunda.getEtapa().getId())).extracting("id").containsExactly(segunda.getId());` | Sim |
| GER-05/15/20: IDs e demandas independentes; outro quadro conserva nome/ativo/versao 0; snapshot intacto | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:342`: `assertThat(quadros.findById(pares.get(1).getId()).orElseThrow().getNome()).isEqualTo("Mesmo");` | Sim |
| GER-05/15/20: IDs e demandas independentes; outro quadro conserva nome/ativo/versao 0; snapshot intacto | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:343`: `assertThat(quadros.findById(pares.get(1).getId()).orElseThrow().isArquivado()).isFalse();` | Sim |
| GER-05/15/20: IDs e demandas independentes; outro quadro conserva nome/ativo/versao 0; snapshot intacto | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:344`: `assertThat(quadros.findById(pares.get(1).getId()).orElseThrow().getVersao()).isZero();` | Sim |
| GER-05/15/20: IDs e demandas independentes; outro quadro conserva nome/ativo/versao 0; snapshot intacto | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:345`: `assertThat(conteudoPersistido()).isEqualTo(antes);` | Sim |
| GER-07: nome/setor nulo/branco/256 e item nulo; 400; zero pais e etapas | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:358`: `.andExpect(status().isBadRequest());` | Sim |
| GER-07: nome/setor nulo/branco/256 e item nulo; 400; zero pais e etapas | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:359`: `assertThat(quadros.count()).isZero();` | Sim |
| GER-07: nome/setor nulo/branco/256 e item nulo; 400; zero pais e etapas | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:360`: `assertThat(etapas.count()).isZero();` | Sim |
| GER-01/06/07: 201, ID retornado existe; descricao vazia; etapa nome/setor 255, ordem 1 e vinculo pelo mesmo ID | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:368`: `.andExpect(status().isCreated()).andExpect(jsonPath("$.id").isNumber()).andExpect(jsonPath("$.descricao").value("")).andReturn();` | Sim |
| GER-01/06/07: 201, ID retornado existe; descricao vazia; etapa nome/setor 255, ordem 1 e vinculo pelo mesmo ID | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:370`: `assertThat(quadros.findById(id)).isPresent();` | Sim |
| GER-01/06/07: 201, ID retornado existe; descricao vazia; etapa nome/setor 255, ordem 1 e vinculo pelo mesmo ID | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:372`: `assertThat(etapasSalvas).hasSize(1);` | Sim |
| GER-01/06/07: 201, ID retornado existe; descricao vazia; etapa nome/setor 255, ordem 1 e vinculo pelo mesmo ID | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:373`: `assertThat(etapasSalvas.get(0).getNome()).isEqualTo("N".repeat(255));` | Sim |
| GER-01/06/07: 201, ID retornado existe; descricao vazia; etapa nome/setor 255, ordem 1 e vinculo pelo mesmo ID | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:374`: `assertThat(etapasSalvas.get(0).getSetor()).isEqualTo("S".repeat(255));` | Sim |
| GER-01/06/07: 201, ID retornado existe; descricao vazia; etapa nome/setor 255, ordem 1 e vinculo pelo mesmo ID | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:375`: `assertThat(etapasSalvas.get(0).getOrdem()).isEqualTo(1);` | Sim |
| GER-01/06/07: 201, ID retornado existe; descricao vazia; etapa nome/setor 255, ordem 1 e vinculo pelo mesmo ID | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:376`: `assertThat(etapasSalvas.get(0).getGerenciamento().getId()).isEqualTo(id);` | Sim |

| Assertion | Ancora | Manter? |
| --- | --- | --- |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:309`: `.andExpect(status().isNoContent()).andExpect(content().string(""));` | GER-17/18/20/26: admin, autoria alheia/nula; 204, estado true/false, versao 1/2, criador e snapshot intactos | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:310`: `assertThat(quadros.findById(quadro.getId()).orElseThrow().isArquivado()).isTrue();` | GER-17/18/20/26: admin, autoria alheia/nula; 204, estado true/false, versao 1/2, criador e snapshot intactos | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:311`: `assertThat(quadros.findById(quadro.getId()).orElseThrow().getVersao()).isEqualTo(1L);` | GER-17/18/20/26: admin, autoria alheia/nula; 204, estado true/false, versao 1/2, criador e snapshot intactos | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:312`: `assertThat(conteudoPersistido()).isEqualTo(antes);` | GER-17/18/20/26: admin, autoria alheia/nula; 204, estado true/false, versao 1/2, criador e snapshot intactos | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:314`: `.andExpect(status().isNoContent()).andExpect(content().string(""));` | GER-17/18/20/26: admin, autoria alheia/nula; 204, estado true/false, versao 1/2, criador e snapshot intactos | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:316`: `assertThat(salvo.isArquivado()).isFalse();` | GER-17/18/20/26: admin, autoria alheia/nula; 204, estado true/false, versao 1/2, criador e snapshot intactos | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:317`: `assertThat(salvo.getVersao()).isEqualTo(2L);` | GER-17/18/20/26: admin, autoria alheia/nula; 204, estado true/false, versao 1/2, criador e snapshot intactos | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:318`: `assertThat(salvo.getNome()).isEqualTo("Quadro");` | GER-17/18/20/26: admin, autoria alheia/nula; 204, estado true/false, versao 1/2, criador e snapshot intactos | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:319`: `assertThat(salvo.getDescricao()).isEqualTo("Descrição");` | GER-17/18/20/26: admin, autoria alheia/nula; 204, estado true/false, versao 1/2, criador e snapshot intactos | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:320`: `assertThat(salvo.getCriador() == null ? null : salvo.getCriador().getId()).isEqualTo(semCriador ? null : criador.getId());` | GER-17/18/20/26: admin, autoria alheia/nula; 204, estado true/false, versao 1/2, criador e snapshot intactos | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:321`: `assertThat(conteudoPersistido()).isEqualTo(antes);` | GER-17/18/20/26: admin, autoria alheia/nula; 204, estado true/false, versao 1/2, criador e snapshot intactos | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:330`: `.andExpect(status().isCreated());` | GER-05/15/20: IDs e demandas independentes; outro quadro conserva nome/ativo/versao 0; snapshot intacto | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:337`: `.content("{\"nome\":\"Editado\",\"versao\":0}")).andExpect(status().isOk());` | GER-05/15/20: IDs e demandas independentes; outro quadro conserva nome/ativo/versao 0; snapshot intacto | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:339`: `.content("{\"versao\":1}")).andExpect(status().isNoContent());` | GER-05/15/20: IDs e demandas independentes; outro quadro conserva nome/ativo/versao 0; snapshot intacto | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:340`: `assertThat(processos.findByEtapaId(primeira.getEtapa().getId())).extracting("id").containsExactly(primeira.getId());` | GER-05/15/20: IDs e demandas independentes; outro quadro conserva nome/ativo/versao 0; snapshot intacto | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:341`: `assertThat(processos.findByEtapaId(segunda.getEtapa().getId())).extracting("id").containsExactly(segunda.getId());` | GER-05/15/20: IDs e demandas independentes; outro quadro conserva nome/ativo/versao 0; snapshot intacto | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:342`: `assertThat(quadros.findById(pares.get(1).getId()).orElseThrow().getNome()).isEqualTo("Mesmo");` | GER-05/15/20: IDs e demandas independentes; outro quadro conserva nome/ativo/versao 0; snapshot intacto | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:343`: `assertThat(quadros.findById(pares.get(1).getId()).orElseThrow().isArquivado()).isFalse();` | GER-05/15/20: IDs e demandas independentes; outro quadro conserva nome/ativo/versao 0; snapshot intacto | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:344`: `assertThat(quadros.findById(pares.get(1).getId()).orElseThrow().getVersao()).isZero();` | GER-05/15/20: IDs e demandas independentes; outro quadro conserva nome/ativo/versao 0; snapshot intacto | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:345`: `assertThat(conteudoPersistido()).isEqualTo(antes);` | GER-05/15/20: IDs e demandas independentes; outro quadro conserva nome/ativo/versao 0; snapshot intacto | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:358`: `.andExpect(status().isBadRequest());` | GER-07: nome/setor nulo/branco/256 e item nulo; 400; zero pais e etapas | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:359`: `assertThat(quadros.count()).isZero();` | GER-07: nome/setor nulo/branco/256 e item nulo; 400; zero pais e etapas | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:360`: `assertThat(etapas.count()).isZero();` | GER-07: nome/setor nulo/branco/256 e item nulo; 400; zero pais e etapas | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:368`: `.andExpect(status().isCreated()).andExpect(jsonPath("$.id").isNumber()).andExpect(jsonPath("$.descricao").value("")).andReturn();` | GER-01/06/07: 201, ID retornado existe; descricao vazia; etapa nome/setor 255, ordem 1 e vinculo pelo mesmo ID | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:370`: `assertThat(quadros.findById(id)).isPresent();` | GER-01/06/07: 201, ID retornado existe; descricao vazia; etapa nome/setor 255, ordem 1 e vinculo pelo mesmo ID | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:372`: `assertThat(etapasSalvas).hasSize(1);` | GER-01/06/07: 201, ID retornado existe; descricao vazia; etapa nome/setor 255, ordem 1 e vinculo pelo mesmo ID | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:373`: `assertThat(etapasSalvas.get(0).getNome()).isEqualTo("N".repeat(255));` | GER-01/06/07: 201, ID retornado existe; descricao vazia; etapa nome/setor 255, ordem 1 e vinculo pelo mesmo ID | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:374`: `assertThat(etapasSalvas.get(0).getSetor()).isEqualTo("S".repeat(255));` | GER-01/06/07: 201, ID retornado existe; descricao vazia; etapa nome/setor 255, ordem 1 e vinculo pelo mesmo ID | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:375`: `assertThat(etapasSalvas.get(0).getOrdem()).isEqualTo(1);` | GER-01/06/07: 201, ID retornado existe; descricao vazia; etapa nome/setor 255, ordem 1 e vinculo pelo mesmo ID | Sim |
| `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:376`: `assertThat(etapasSalvas.get(0).getGerenciamento().getId()).isEqualTo(id);` | GER-01/06/07: 201, ID retornado existe; descricao vazia; etapa nome/setor 255, ordem 1 e vinculo pelo mesmo ID | Sim |

### Matriz final GER-21: doze mutacoes arquivadas

Todos os casos usam dados validos. Os testes parametrizados executam cada verbo/acao listado. Resultado exigido: 409, mensagem exata da spec e estado persistido intacto. GER-23 percorre as mesmas doze rotas sem sessao/com token invalido, retornando 401 e preservando os valores. As tabelas anteriores sao a adequacao reversa dessas assertions.

| Operacao | Status/mensagem exatos | Preservacao no banco | Resultado |
| --- | --- | --- | --- |
| Editar quadro | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:259`: `.andExpect(status().isConflict()).andExpect(jsonPath("$.erro").value(ARQUIVO_ERRO));` | `sistema/src/test/java/com/patp/sistema/GerenciamentoApiTests.java:260`: `assertThat(quadros.findById(quadro.getId()).orElseThrow().getNome()).isEqualTo("Quadro");` | PASS |
| Criar etapa | `sistema/src/test/java/com/patp/sistema/EtapaArchiveTests.java:55`: `.andExpect(status().isConflict()).andExpect(jsonPath("$.erro").value(ERRO));` | `sistema/src/test/java/com/patp/sistema/EtapaArchiveTests.java:56`: `assertThat(conteudoPersistido()).isEqualTo(antes);` | PASS |
| Editar etapa | `sistema/src/test/java/com/patp/sistema/EtapaArchiveTests.java:55`: `.andExpect(status().isConflict()).andExpect(jsonPath("$.erro").value(ERRO));` | `sistema/src/test/java/com/patp/sistema/EtapaArchiveTests.java:56`: `assertThat(conteudoPersistido()).isEqualTo(antes);` | PASS |
| Excluir etapa | `sistema/src/test/java/com/patp/sistema/EtapaArchiveTests.java:55`: `.andExpect(status().isConflict()).andExpect(jsonPath("$.erro").value(ERRO));` | `sistema/src/test/java/com/patp/sistema/EtapaArchiveTests.java:56`: `assertThat(conteudoPersistido()).isEqualTo(antes);` | PASS |
| Criar demanda | `sistema/src/test/java/com/patp/sistema/ProcessoArchiveTests.java:43`: `.andExpect(status().isConflict()).andExpect(jsonPath("$.erro").value(ERRO));` | `sistema/src/test/java/com/patp/sistema/ProcessoArchiveTests.java:44`: `assertThat(conteudoPersistido()).isEqualTo(antes);` | PASS |
| Editar demanda | `sistema/src/test/java/com/patp/sistema/ProcessoArchiveTests.java:43`: `.andExpect(status().isConflict()).andExpect(jsonPath("$.erro").value(ERRO));` | `sistema/src/test/java/com/patp/sistema/ProcessoArchiveTests.java:44`: `assertThat(conteudoPersistido()).isEqualTo(antes);` | PASS |
| Mover demanda | `sistema/src/test/java/com/patp/sistema/ProcessoArchiveTests.java:43`: `.andExpect(status().isConflict()).andExpect(jsonPath("$.erro").value(ERRO));` | `sistema/src/test/java/com/patp/sistema/ProcessoArchiveTests.java:44`: `assertThat(conteudoPersistido()).isEqualTo(antes);` | PASS |
| Concluir demanda | `sistema/src/test/java/com/patp/sistema/ProcessoArchiveTests.java:43`: `.andExpect(status().isConflict()).andExpect(jsonPath("$.erro").value(ERRO));` | `sistema/src/test/java/com/patp/sistema/ProcessoArchiveTests.java:44`: `assertThat(conteudoPersistido()).isEqualTo(antes);` | PASS |
| Cancelar demanda | `sistema/src/test/java/com/patp/sistema/ProcessoArchiveTests.java:43`: `.andExpect(status().isConflict()).andExpect(jsonPath("$.erro").value(ERRO));` | `sistema/src/test/java/com/patp/sistema/ProcessoArchiveTests.java:44`: `assertThat(conteudoPersistido()).isEqualTo(antes);` | PASS |
| Excluir demanda | `sistema/src/test/java/com/patp/sistema/ProcessoArchiveTests.java:43`: `.andExpect(status().isConflict()).andExpect(jsonPath("$.erro").value(ERRO));` | `sistema/src/test/java/com/patp/sistema/ProcessoArchiveTests.java:44`: `assertThat(conteudoPersistido()).isEqualTo(antes);` | PASS |
| Adicionar comentario | `sistema/src/test/java/com/patp/sistema/ComentarioArchiveTests.java:38`: `.andExpect(status().isConflict()).andExpect(jsonPath("$.erro").value("Gerenciamento arquivado. Restaure-o antes de alterar."));` | `sistema/src/test/java/com/patp/sistema/ComentarioArchiveTests.java:39`: `assertThat(conteudoPersistido()).isEqualTo(antes);` | PASS |
| Adicionar historico manual | `sistema/src/test/java/com/patp/sistema/HistoricoArchiveTests.java:38`: `.andExpect(status().isConflict()).andExpect(jsonPath("$.erro").value("Gerenciamento arquivado. Restaure-o antes de alterar."));` | `sistema/src/test/java/com/patp/sistema/HistoricoArchiveTests.java:39`: `assertThat(conteudoPersistido()).isEqualTo(antes);` | PASS |

Limites do lote: H2 demonstra API/transacoes/locks/persistencia isolados; nao demonstra sozinho migracao, collation ou configuracao real do MySQL. O orquestrador verificou schema antigo em MySQL temporario e executara a suite completa separada apos este lote. O defeito de charset das recusas 401 identificado nessa verificacao sera corrigido em tarefa propria; nao equivale a alterar senha, papel ou guardas. DELETE ativo de processo continua com a falha preexistente de FK documentada em T6; a nova transacao impede log parcial. A interface e o Verificador independente ainda sao necessarios para concluir a feature. Nenhum dado real foi alterado por este trabalhador.
