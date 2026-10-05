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
