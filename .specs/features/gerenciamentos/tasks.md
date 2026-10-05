# CRUD de gerenciamentos da Creral: tarefas

## Execution Protocol

Executar com a skill `tlc-spec-driven`, uma tarefa por vez: testes derivados da especificação, gate, adequação com evidências, atualização de status/rastreabilidade e um commit local por tarefa. A implementação e os testes foram autorizados pelo usuário em 2026-10-04. Ferramentas: shell PowerShell, Python, Maven, npm e skill já escolhida pelo usuário. A delegação técnica segue as instruções da sessão; não exige outra confirmação de rotina.

**Design:** `design.md`
**Status:** In Progress

## Test Coverage Matrix

Gerada por leitura do único teste existente (`SistemaApplicationTests`), `pom.xml`, `package.json` e especificação. Não foram encontrados AGENTS, guias de contribuição ou limiares de cobertura; aplicar padrões fortes limitados a GER-01–41. JUnit/Spring Boot existentes são a base; testes React ainda serão configurados.

| Code Layer | Required Test Type | Coverage Expectation | Location Pattern | Run Command |
| --- | --- | --- | --- | --- |
| Identidade/CRUD/guardas | integration | Resultados 1:1 dos critérios aplicáveis e seus limites/negações/falhas, incluindo concorrência | `sistema/src/test/java/com/patp/sistema/**/*Tests.java` | `mvn.cmd -B test` em sistema |
| Repositório/modelo | integration | Estado persistido, legado, versão, lock e preservação | Mesmo diretório Java de teste | `mvn.cmd -B test` |
| Infra de teste | integration | Datasource isolado explícito e carregamento do contexto existente | Teste existente e recursos de teste | `mvn.cmd -B test` |
| Cliente HTTP | unit | 204, JSON/status/mensagens, falha de comunicação, contrato sem retry | `frontend/src/**/*.test.js` | `npm.cmd test` em frontend |
| Formulário/lista/quadro | integration | GER-28–41: fluxo, cancelamento, permissões, estados, erros, foco/teclado e duplicidade | `frontend/src/**/*.test.jsx` | `npm.cmd test` |
| Interface no navegador | e2e | Criar → editar → arquivar → consultar → restaurar → recarregar | `frontend/e2e/` | `npm.cmd run test:e2e` após configurar |

## Gate Check Commands

| Gate Level | When to Use | Command |
| --- | --- | --- |
| Quick | Testes unitários do frontend | `npm.cmd test` em frontend |
| Full | Integração backend/frontend | `mvn.cmd -B test` em sistema para tarefas Java; `npm.cmd test` em frontend para React |
| Build | Final de fase e infra | Backend: `mvn.cmd -B verify`; frontend existente: `npm.cmd run build` e `npm.cmd exec -- oxlint src vite.config.js`; depois de T9 incluir `npm.cmd test`; depois de T13 incluir `npm.cmd run test:e2e` |

Não executar a aplicação normal com o banco configurado para testar. Usar H2 in-memory somente de teste e, caso possível, MySQL temporário separado. Staging explícito; arquivos gerados previamente rastreados não fazem parte do código entregue.

## Execution Plan

### Phase 1: Base e CRUD no servidor

```text
T1 -> T2 -> T3 -> T4
```

### Phase 2: Proteção transacional dos registros associados

```text
T5 -> T6 -> T7 -> T8
```

### Phase 3: Interface e fluxo em navegador

```text
T9 -> T10 -> T11 -> T12 -> T13
```

Execução sequencial. Lote servidor: fases 1/2 (8 tarefas, T1 executada pelo orquestrador antes do trabalhador T2–T8). Lote interface: fase 3 (5 tarefas). Verificador novo após o último commit. Consultas/revisões independentes podem ocorrer em paralelo, sem editar os arquivos do trabalhador.

Dependências entre fases:

```text
T4 -> T5
T8 -> T9
```

## Task Breakdown

### T1: Isolar os testes do banco existente

**What:** configurar H2 de teste e comprovar que o contexto usa somente banco em memória.
**Where:** `sistema/pom.xml`
**Companions:** recursos de teste, teste de contexto existente; documentação de execução aprovada.
**Depends on:** None
**Requirement:** base dos testes GER-01–27.
**Done when:**
- [x] Contexto carrega e a URL JDBC é H2 em memória, com assertion explícita.
- [x] Dependência H2 é somente test; configuração real não é alterada.
- [x] Gate passa com pelo menos o teste existente, nenhum ignorado.
**Tests:** integration
**Gate:** build
**Commit:** `test(gerenciamentos): isolate database for application tests`

### T2: Proteger identidade e cadastro

**What:** fechar o contrato de cadastro e papel persistido para as permissões do CRUD.
**Where:** `sistema/src/main/java/com/patp/sistema/service/UsuarioService.java`
**Companions:** modelo Usuario, controller/advice se necessário, testes de cadastro/serialização, instrução de selecionar administrador existente.
**Depends on:** T1
**Requirement:** GER-23–27.
**Done when:**
- [x] Cadastro com ID retorna 400 sem substituir conta; campos de papel não concedem administração.
- [x] Conta existente promovida por operação confiável é reconhecida; legado sem papel é funcionário.
- [x] Todas as respostas de usuário/relações omitem senha/hash; sessão ausente/inválida continua 401.
- [x] Testes de resultado cobrem os critérios com pelo menos um cenário por resultado distinto; gate passa.
**Tests:** integration
**Gate:** full
**Commit:** `fix(auth): protect registration identity and administrative role`

### T3: Persistir estado e versão dos quadros

**What:** estender o contrato de persistência dos gerenciamentos preservando os dados existentes.
**Where:** `sistema/src/main/java/com/patp/sistema/model/Gerenciamento.java`
**Companions:** repositório Gerenciamento, consultas escalares de vínculo nos repositórios de filhos, testes JPA, instruções de migração local.
**Depends on:** T2
**Requirement:** GER-08–10/16–20/22; legado.
**Done when:**
- [x] Novos registros ativos, versão inicial; marcador nulo legado tratado como ativo, criador não inferido.
- [x] Filtrar ativos/arquivados preserva valores antigos maiores sem truncar.
- [x] Lock e projeções escalares necessários estão definidos e persistência de estado/versão é testada.
- [x] Gate passa, sem excluir/ignorar testes.
**Tests:** integration
**Gate:** full
**Commit:** `feat(gerenciamentos): persist archive state and version`

### T4: Completar o componente CRUD HTTP de gerenciamentos

**What:** entregar criação/consulta/edição/arquivamento/restauração do componente gerenciamentos conforme contratos.
**Where:** `sistema/src/main/java/com/patp/sistema/controller/GerenciamentoController.java`
**Companions:** serviço Gerenciamento, guarda compartilhado, exceção/advice, DTOs e testes HTTP/persistência. Controller/serviço são o mesmo contrato executável; os testes não são adiados.
**Depends on:** T3
**Requirement:** GER-01–20/23/26/27 e erro de quadro arquivado GER-21.
**Done when:**
- [x] Todos os endpoints têm sucesso, limites, autenticação, inexistência, autorização, estado e versão conforme aplicáveis, com textos/status exatos.
- [x] Nome normalizado conforme JavaScript, limites UTF-16, descrição/etapas opcionais, nomes iguais independentes.
- [x] Falha na segunda etapa desfaz pai e filhos; autoria vem da sessão; versão antiga não sobrescreve; repetição de estado não incrementa versão.
- [x] Editar/arquivar/restaurar preserva registros associados; metadados e permissão seguros retornados em DTO.
- [x] JSON/formato inválido recebe 400; erros de banco são genéricos; transações usam READ_COMMITTED e lock no pai.
- [x] Gate build passa e relatório de adequação localiza assertions por GER aplicável.
**Tests:** integration
**Gate:** build
**Commit:** `feat(gerenciamentos): complete authorized board lifecycle API`

### T5: Bloquear alterações de etapas em quadros arquivados

**What:** aplicar guarda transacional às mutações de EtapaService.
**Where:** `sistema/src/main/java/com/patp/sistema/service/EtapaService.java`
**Companions:** testes das três rotas de etapas e concorrência com arquivamento.
**Depends on:** T4
**Requirement:** GER-21/22.
**Done when:**
- [x] POST/PUT/DELETE válidos em arquivado retornam 409 exato sem gravar; consultas permanecem possíveis.
- [x] Mutações ativas preservam comportamento e usam lock no quadro; ambas as ordens de disputa com arquivamento têm resultado persistido assertado.
- [x] Gate passa sem excluir/ignorar testes.
**Tests:** integration
**Gate:** full
**Commit:** `fix(etapas): guard archived boards during writes`

### T6: Bloquear alterações de demandas em quadros arquivados

**What:** proteger todas as mutações de ProcessoService pelo vínculo persistido.
**Where:** `sistema/src/main/java/com/patp/sistema/service/ProcessoService.java`
**Companions:** testes da matriz de seis rotas e POST com ID/relações falsificadas.
**Depends on:** T5
**Requirement:** GER-21/22, contrato POST sem update.
**Done when:**
- [x] Criar/editar/mover/concluir/cancelar/excluir em arquivado retorna 409 e não muda registro/histórico.
- [x] POST com ID retorna 400, sem atualizar processo existente; guardas usam vínculos persistidos.
- [x] Mutação ativa conserva semântica anterior, com transação/lock ordenado; gate passa.
**Tests:** integration
**Gate:** full
**Commit:** `fix(processos): enforce archived board write protection`

### T7: Bloquear novos comentários em quadros arquivados

**What:** aplicar guarda transacional ao componente de comentários.
**Where:** `sistema/src/main/java/com/patp/sistema/service/ComentarioService.java`
**Companions:** testes HTTP de escrita ativa/arquivada e leitura preservada.
**Depends on:** T6
**Requirement:** GER-20/21/22.
**Done when:**
- [x] Adicionar em arquivado retorna 409 e contagem/valores ficam intactos; ativo ainda permite; consultar arquivado funciona.
- [x] Lock no quadro ocorre antes de ler entidades do processo; gate passa.
**Tests:** integration
**Gate:** full
**Commit:** `fix(comentarios): reject writes to archived boards`

### T8: Bloquear histórico manual em quadros arquivados

**What:** aplicar guarda transacional ao componente de histórico manual existente.
**Where:** `sistema/src/main/java/com/patp/sistema/service/HistoricoService.java`
**Companions:** testes HTTP de leitura/escrita e matriz final; registros automáticos ativos preservados.
**Depends on:** T7
**Requirement:** GER-20/21/22.
**Done when:**
- [ ] Histórico manual em arquivado retorna 409 sem gravação; consultas e registro automático de operações ativas mantêm funcionamento.
- [ ] Todas as 12 rotas da matriz têm assertions de status/mensagem e estado persistido.
- [ ] Gate build passa e adequação da fase cita evidências exatas.
**Tests:** integration
**Gate:** build
**Commit:** `fix(historico): protect archived board records`

### T9: Preparar testes React e navegador

**What:** configurar os runners da interface com componentes e serviços testáveis por comportamento.
**Where:** `frontend/package.json`
**Companions:** lockfile, configuração Vitest/Playwright e setup. Primeiro teste de contrato do runner, sem testes tautológicos; manter baixo impacto no runtime.
**Depends on:** T8
**Requirement:** base de GER-28–41.
**Done when:**
- [ ] Vitest executa um teste significativo de comportamento existente em DOM e npm build/lint passam.
- [ ] Playwright usa navegador instalado; não requer dados reais nem banco configurado.
- [ ] Script de lint se restringe às fontes/configuração, sem varrer node_modules; gate passa.
**Tests:** integration
**Gate:** full
**Commit:** `test(frontend): configure behavioral UI test runners`

### T10: Completar cliente HTTP do CRUD

**What:** implementar chamadas seguras do contrato em api.js.
**Where:** `frontend/src/services/api.js`
**Companions:** testes unitários HTTP com Response real, incluindo 204 e falhas.
**Depends on:** T9
**Requirement:** GER-32–34/40; contratos.
**Done when:**
- [ ] Filtro, token, versão e métodos/rotas correspondem ao design; 204 é sucesso sem parse de JSON.
- [ ] Erros JSON mantêm mensagem/status; comunicação usa texto exato; não há retry de mutações.
- [ ] Gate passa com assertions de saída e requests efetivos.
**Tests:** unit
**Gate:** quick
**Commit:** `feat(frontend): add board lifecycle HTTP client`

### T11: Entregar formulário de criação e edição

**What:** reutilizar CriarGerenciamento para campos/validação/envio/cancelamento do CRUD.
**Where:** `frontend/src/pages/CriarGerenciamento.jsx`
**Companions:** testes React; CSS somente necessário ao formulário.
**Depends on:** T10
**Requirement:** GER-03/04/06/31–33/38–40.
**Done when:**
- [ ] Criar sem etapas/descrição funciona; edição muda só nome/descrição com versão; etapas iniciais opcionais somente ao criar.
- [ ] Cancelar não muta; duplo clique/envio pendente produz uma request; falhas conservam valores e alerta acessível.
- [ ] Rótulos/teclado e limites têm testes de comportamento; gate passa.
**Tests:** integration
**Gate:** full
**Commit:** `feat(frontend): add validated board create and edit form`

### T12: Entregar administração de quadros na lista

**What:** completar Gerenciamentos com filtros, permissões, confirmação e estados de consulta.
**Where:** `frontend/src/pages/Gerenciamentos.jsx`
**Companions:** testes React; CSS necessário à lista/ações/confirmação.
**Depends on:** T11
**Requirement:** GER-28/30–41.
**Done when:**
- [ ] Ativos inicialmente; troca/abrir/voltar mantém filtro; botões seguem podeAdministrar; confirmação nome/preservação/Cancelar/Arquivar e foco devolvido.
- [ ] Carregando/vazio/erro/retry são distintos; consultas antigas não trocam filtro atual; envio duplicado bloqueado.
- [ ] Sucesso seguido de erro GET mostra GER-34 e retry apenas GET; arquivar/restaurar remove cartão do filtro preservado.
- [ ] Valores editados e selecionados atualizados; erros alertados, teclado operável; gate passa.
**Tests:** integration
**Gate:** full
**Commit:** `feat(frontend): manage active and archived boards`

### T13: Entregar consulta arquivada e validar fluxo completo

**What:** completar o contrato de consulta do Quadro e comprovar o fluxo em navegador.
**Where:** `frontend/src/pages/Quadro.jsx`
**Companions:** testes React de readonly, testes E2E/configuração necessária e backend de teste isolado para fluxo real.
**Depends on:** T12
**Requirement:** GER-29/35/38–40; conclusão GER-01–41.
**Done when:**
- [ ] Arquivado exibe texto exato e não oferece alteração; consulta/carregamento/erro funcionam com teclado e alerta.
- [ ] Navegador percorre CRUD completo, recarrega e vê dados persistidos com contas fictícias.
- [ ] Todos os gates backend/frontend passam; nenhum teste ignorado; adequação completa registrada.
**Tests:** e2e + integration
**Gate:** build
**Commit:** `feat(frontend): show archived boards as read only`

## Diagram-Definition Cross-Check

| Task | Depends On | Diagram Shows | Status |
| --- | --- | --- | --- |
| T1 | None | entrada | OK |
| T2 | T1 | T1 -> T2 | OK |
| T3 | T2 | T2 -> T3 | OK |
| T4 | T3 | T3 -> T4 | OK |
| T5 | T4 | fase anterior | OK |
| T6 | T5 | T5 -> T6 | OK |
| T7 | T6 | T6 -> T7 | OK |
| T8 | T7 | T7 -> T8 | OK |
| T9 | T8 | fase anterior | OK |
| T10 | T9 | T9 -> T10 | OK |
| T11 | T10 | T10 -> T11 | OK |
| T12 | T11 | T11 -> T12 | OK |
| T13 | T12 | T12 -> T13 | OK |

## Test Co-location Validation

| Tarefa | Camada | Matriz | Tests | Status |
| --- | --- | --- | --- | --- |
| T1 | Infra de teste | integration | integration | OK |
| T2–T8 | Identidade/CRUD/JPA/guardas | integration | integration | OK |
| T9 | Infra React | integration | integration | OK |
| T10 | Cliente HTTP | unit | unit | OK |
| T11–T12 | React | integration | integration | OK |
| T13 | React/navegador | integration/e2e | integration/e2e | OK |

## Task Granularity Check

Cada tarefa entrega um componente/contrato definido. Arquivos acompanhantes são somente wiring mínimo e testes que precisam executar no mesmo commit. T4 reúne controller/serviço porque seu resultado verificável é um único contrato CRUD; não separa validação/permissões do endpoint nem adia testes. T3 reúne modelo e consultas como contrato de persistência. Sem mudanças em componentes não listados.

## Execution Evidence

Por tarefa, acrescentar resultado real, contagem de testes e tabela bidirecional de adequação com `arquivo:linha` e expressão da assertion antes do commit. Os resultados ainda não foram executados.

### T1 conclu?da

Premissas: preservar o teste de contexto, H2 somente de teste e nenhuma conex?o ? inst?ncia configurada. Arquivos: pom, recursos de teste, teste de contexto e documenta??o .specs produzida antes da execu??o. Sucesso: aplica??o inicializa e URL JDBC ? H2 in-memory.

Gates: `mvn.cmd -B verify` PASS, 1 teste, 0 falhas/erros/ignorados; build React e oxlint das fontes PASS. N?o h? desvio de especifica??o.

| Crit?rio / assertion (adequa??o direta) | Evid?ncia e valor | Resultado |
| --- | --- | --- |
| Contexto isolado | `sistema/src/test/java/com/patp/sistema/SistemaApplicationTests.java:22`, `assertTrue(connection.getMetaData().getURL().startsWith("jdbc:h2:mem:"))` | H2 em mem?ria confirmado |
| H2 n?o entra no runtime | `sistema/pom.xml`, dependency `scope=test`, pacote compilado com sucesso | PASS por inspe??o/build |

| Assertion (adequa??o reversa) | ?ncora | Manter? |
| --- | --- | --- |
| `SistemaApplicationTests.java:22`, startsWith jdbc:h2:mem | Done when T1: datasource isolado | Sim |

Adequa??o: assertion de conex?o real, sem mocks/tautologias; teste existente preservado, nenhum skip; sem guidelines adicionais. T1 completa.
