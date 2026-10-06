# Etapas finais Tasks

## Execution Protocol

Usar tlc-spec-driven já escolhido pelo usuário. Uma tarefa, gate, adequação e commit local por vez. Testes derivados de spec.md, sem exclusão/skip/enfraquecimento. Verificador novo automático ao final. Ferramentas locais de edição/shell, H2 e MySQL TEMP fictício; nenhum DB configurado, push/deploy ou reinício de serviços do usuário.

**Design:** design.md. **Status:** oito tarefas concluídas e verificadas independentemente, 30/30 critérios; UAT humano pendente. Relatório em validation.md. Oito tarefas em um lote, incluindo T8 corretiva de schema; execução sequencial. A autorização anterior cobre implementação e testes locais das escolhas confirmadas.

## Test Coverage Matrix

Gerada de código/spec e docs/OPERACAO.md, sistema/pom.xml, application.properties de teste, frontend/package.json e playwright.config.js. Sem AGENTS/CONTRIBUTING ou limiar numérico adicional encontrado. Amostras: GerenciamentoApiTests/PersistenceTests, EtapaApiTests/ArchiveTests, ProcessoArchiveTests, QuadroEtapas.test.jsx, CriarGerenciamento.test.jsx e os três E2E existentes. Aplicar todos os ACs/edges deste corte como alvo, preservando o restante como regressão.

| Code Layer | Required Test Type | Coverage Expectation | Location Pattern | Run Command |
| --- | --- | --- | --- | --- |
| Modelo/DTO | integration | Categoria, legado nulo e campos persistidos | sistema/src/test/java/com/patp/sistema/*Tests.java | mvn.cmd -B verify |
| Serviço/repositório/HTTP | integration | Todos os ACs da tarefa; resultado persistido, rejeição sem efeito, rollback e versão | Mesmo padrão Java existente, H2 | mvn.cmd -B verify |
| Preparação/CLI | integration | Plano, todos os campos antes/depois, arquivo, desconhecidos, repetição, rollback e main inválido | PreparacaoEtapasFinaisTests.java | mvn.cmd -B verify; repetir em MySQL TEMP |
| React | unit | Render, intervalos, controles/erros/arquivo, payload de trabalho e homônimos | frontend/src/pages/*.test.jsx | npm.cmd test |
| Integração real | e2e | Criação, teclado, reload, CRUD/versão, arquivo e duas finais reais | frontend/e2e/*.spec.js | npm.cmd run test:e2e |

## Gate Check Commands

| Gate Level | When to Use | Command |
| --- | --- | --- |
| Quick | React e serviço isolado durante desenvolvimento | npm.cmd test ou mvn.cmd -B -Dtest=Classe test; teste seletivo não substitui Full antes do commit Java |
| Full | Tarefas Java/HTTP/preparação | mvn.cmd -B verify em sistema, JAVA_HOME C:/Program Files/Java/jdk-25.0.2; somente H2 de teste |
| Build | Fechar interface/integração | npm.cmd test; build com outDir TEMP novo; npm.cmd run lint; npm.cmd run test:e2e, sem Maven concorrente no mesmo target |

## Execution Plan

### Phase 1: Categorias e colunas

```text
T1 → T2 → T8 → T3 → T4 → T5 → T6 → T7
```

## Task Breakdown

### T1: Categoria persistida e DTO

**What:** acrescentar categoria explícita e compatibilidade de categoria nula no modelo/DTO existente.
**Where:** `sistema/src/main/java/com/patp/sistema/model/Etapa.java`
**Depends on:** None
**Reuses:** mapeamento JPA e resposta existentes.
**Requirement:** FIN-06/07
**Related files:** CategoriaEtapa.java, dto/EtapaResponse.java, ajuste de construção do DTO em EtapaService; EtapaCategoriaTests.java.
**Tests:** integration
**Gate:** Full
**Commit:** feat(etapas): persist explicit stage categories
**Done when:**
- [x] Categoria enum/string é persistida e devolvida; legado null/homônimo retorna TRABALHO sem escrita; gate Java completo e adequação PASS.

### T2: Garantia e ordenação do par oficial

**What:** criar um componente que garante uma final de cada tipo e ordena etapas sem inferir pelo nome.
**Where:** `sistema/src/main/java/com/patp/sistema/service/EtapasFinaisService.java`
**Depends on:** T1
**Reuses:** EtapaRepository e transação/lock do chamador.
**Requirement:** FIN-01/03/08/11/14/26/27
**Related files:** EtapasFinaisServiceTests.java; métodos de repositório se necessários.
**Tests:** integration
**Gate:** Full
**Commit:** feat(etapas): guarantee the required final stage pair
**Done when:**
- [x] Par possui nomes/categorias/IDs/campos corretos; repetição não duplica; ordenação preserva trabalhos; duplicidade explícita é rejeitada; gate/adequação PASS.

### T8: Tipo físico VARCHAR da categoria

**What:** fixar VARCHAR nullable em vez de ENUM nativo do dialect MySQL.
**Where:** `sistema/src/main/java/com/patp/sistema/model/Etapa.java`
**Depends on:** T2
**Reuses:** enum STRING e mapeamento JPA existentes.
**Requirement:** FIN-06/07 e modelo VARCHAR do design.
**Related files:** EtapaCategoriaTests.java, evidence.md.
**Tests:** integration
**Gate:** Full
**Commit:** fix(etapas): use nullable varchar for stage categories
**Done when:**
- [x] INFORMATION_SCHEMA H2 e MySQL TEMP confirmam VARCHAR20 nullable; suíte Java completa e verificação física PASS.

Commit corretivo a06550f concluído antes da formalização administrativa de T8, incorporada à próxima tarefa sem reescrita de histórico. Evidência/gate e pré-plano estavam no mesmo commit corretivo.

### T3: Criação atômica de novos quadros

**What:** integrar as finais na transação de criação e rejeitar identidade/categoria inicial fornecida pelo cliente.
**Where:** `sistema/src/main/java/com/patp/sistema/service/GerenciamentoService.java`
**Depends on:** T8
**Reuses:** criação/validação/rollback existentes.
**Requirement:** FIN-01/02/03/04/05
**Related files:** GerenciamentoController.java (repassar ID/categoria para validação HTTP), GerenciamentoApiTests.java e GerenciamentoFinaisTests.java (inclui falha parcial).
**Tests:** integration
**Gate:** Full
**Commit:** feat(gerenciamentos): create required final columns atomically
**Done when:**
- [x] HTTP sem/com trabalho retorna persistência exata do par; quadros independentes; entrada inválida e falha parcial não salvam; expectativas antigas mudam somente para incluir finais; gate/adequação PASS.

### T4: Proteção e sequência no CRUD de etapas

**What:** limitar configuração a trabalhos, proteger finais e devolver snapshot com categorias/ordem/contagens reais.
**Where:** `sistema/src/main/java/com/patp/sistema/service/EtapaService.java`
**Depends on:** T3
**Reuses:** guard, versão, contagens, confirmação e EtapasFinaisService.
**Requirement:** FIN-07/08/09/10/11/12/13/14/19/29
**Related files:** EtapaApiTests.java/EtapaArchiveTests.java e testes de snapshot/concurrency afetados.
**Tests:** integration
**Gate:** Full
**Commit:** feat(etapas): protect final columns during stage management
**Done when:**
- [x] HTTP direto protege finais mesmo vazias; trabalhos contam só N para posições; mutations garantem par no mesmo lock/versão; GET nunca prepara legados; arquivos/permissões/conflitos/rollback preservados; gate/adequação PASS.

### T5: Preparação explícita e idempotente

**What:** oferecer inventário/plano e aplicação transacional dos legados, acessíveis somente pelo main explícito de operador.
**Where:** `sistema/src/main/java/com/patp/sistema/service/PreparacaoEtapasFinaisService.java`
**Depends on:** T4
**Reuses:** garantia de finais, locks ordenados, persistência e incremento de versão existentes.
**Requirement:** FIN-20/21/22/23/24/25/26/27/28/29/30
**Related files:** PrepararEtapasFinais.java (entry point próprio), consulta por quadro em ProcessoRepository e IDs ordenados em GerenciamentoRepository, pom.xml (manter SistemaApplication como main normal do pacote), PreparacaoEtapasFinaisTests.java, docs/OPERACAO.md (comando/scope/schema).
**Tests:** integration
**Gate:** Full
**Commit:** feat(etapas): prepare legacy final stage references explicitly
**Done when:**
- [x] Plano sem escrita; ativos/arquivados/sem criador; somente etapa_id de status exatos muda; snapshot integral preservado; repetição zero efeitos; falha/duplicidade rollback; versão uma vez por quadro; main inválido recusa antes do contexto; nunca startup/GET; H2/MySQL TEMP/adequação PASS.

### T6: Apresentação e editor somente de trabalhos

**What:** distinguir finais oficiais no quadro e explicar sua criação automática no formulário.
**Where:** `frontend/src/pages/Quadro.jsx`
**Depends on:** T5
**Reuses:** snapshot, foco, pendência e EditorEtapa existentes.
**Requirement:** FIN-15/16/17/18/19
**Related files:** CriarGerenciamento.jsx, testes React afetados; CSS somente se necessário para identificação.
**Tests:** unit
**Gate:** Quick
**Commit:** feat(ui): display protected final columns in boards
**Done when:**
- [x] Finais vêm do servidor, indicação obrigatória, sem Setor/ações; handlers também guardados; intervalos só trabalho; homônimos editáveis; arquivo/erros/foco preservados; baseline 222 + 13 casos novos (235) PASS, adequação PASS.

### T7: Integração de navegador e encerramento operacional

**What:** adaptar os três fluxos reais ao contrato das finais e completar evidência/roteiro de uso.
**Where:** `frontend/e2e/etapas.spec.js`
**Depends on:** T6
**Reuses:** H2/Edge/helper com zero retries e cenários existentes.
**Requirement:** FIN-01/02/03/07/08/11/15/16/17/18/19/29
**Related files:** gerenciamentos.spec.js, autocadastro.spec.js, docs/OPERACAO.md e design.md (contagem administrativa de oito tarefas, incluindo T8); evidência física em TEMP.
**Tests:** e2e
**Gate:** Build
**Commit:** test(etapas): verify final columns across board workflows
**Done when:**
- [x] Três cenários existentes preservados, listas/categorias/IDs finais exatos em criação/reload/arquivo; 3/3 Edge PASS, 235 Vitest/build TEMP/lint PASS; testes MySQL TEMP documentados; adequação PASS. Verificador novo despachado após o commit d1f5e47: 30/30 critérios, gates independentes e sensor em memória 7/7 PASS, registrados em validation.md. UAT humano pendente.

## Diagram-Definition Cross-Check

| Task | Depends On | Diagram Shows | Status |
| --- | --- | --- | --- |
| T1 | None | None | Match |
| T2 | T1 | T1 | Match |
| T8 | T2 | T2 | Match |
| T3 | T8 | T8 | Match |
| T4 | T3 | T3 | Match |
| T5 | T4 | T4 | Match |
| T6 | T5 | T5 | Match |
| T7 | T6 | T6 | Match |

## Test Co-location Validation

| Task | Layer | Matrix Requires | Task Says | Status |
| --- | --- | --- | --- | --- |
| T1 | Modelo/DTO | integration | integration | Match |
| T2 | Serviço | integration | integration | Match |
| T8 | Modelo/schema | integration | integration | Match |
| T3 | Serviço/HTTP | integration | integration | Match |
| T4 | Serviço/HTTP | integration | integration | Match |
| T5 | Preparação/CLI | integration | integration | Match |
| T6 | React | unit | unit | Match |
| T7 | Integração real | e2e | e2e | Match |

Cada tarefa tem um componente/deliverable principal, seus testes e arquivos auxiliares indispensáveis; nenhum trabalho de negócio está escondido em teste separado. Oito tarefas, nenhum lote adicional; auditorias somente leitura puderam ocorrer em paralelo, gravações/gates/commits seguem em sequência.
