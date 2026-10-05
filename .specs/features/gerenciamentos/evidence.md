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
