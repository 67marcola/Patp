# Atividade por gerenciamento Tasks

## Execution Protocol

Implementar com a skill tlc-spec-driven já ativada e suas Critical Rules. Ler implement.md completamente antes de Execute. Se indisponível, parar e informar. Testes derivados dos critérios; gate aprovado antes de concluir tarefa; atualizar tarefas/evidência/traceabilidade e diagnóstico antes de cada commit atômico. Não remover/ignorar cenários. Verifier independente obrigatório automático após última tarefa, sensor mínimo5 em scratch e comparação da árvore real.

**Design:**design.md. **Status:**Approved, execução por três agentes sequenciais7/7/4 confirmada em10/10/2026. **Base:**084757d. Defaults/compatibilidade aprovados; T13 recebeu autorização exata para os três cenários descritos. Ferramentas: PowerShell, apply_patch, git local, Maven, npm, Python, runners isolados existentes; TLC. AD-024 e autorizações anteriores persistem; não repetir pedido de tecnologias/ferramentas.

## Test Coverage Matrix

Gerada de pom.xml, package.json, vitest.config.js, playwright.config.js, docs/OPERACAO.md, ApiIntegrationSupport, HistoricoArchiveTests, DemandaArquivoHistoricoTests/ApiTests, ComentarioEnvioTests, api/Quadro tests e runners E2E existentes. Não há AGENTS/quality guidelines adicionais encontrados; aplicar defaults fortes. Configuração Vitest não exige threshold numérico; Playwright é sequencial com retries0.

| Code Layer | Required Test Type | Coverage Expectation | Location Pattern | Run Command |
| --- | --- | --- | --- | --- |
| Entity/config | none | Compilar; persistência crítica no writer da mesma fase | sistema/src/main/java | Maven test-compile |
| Writer/services | integration | Todos os ramos e critérios atribuídos, happy/failure/rollback, antes/depois literal e versão | sistema/src/test/java/com/patp/sistema/*Tests.java | Maven test |
| Repository/query/controller | integration | Toda rota happy/401/404/400 e casos limites, união/paginação/isolamento H2 e MySQL fictício | sistema/src/test/java/com/patp/sistema/*Tests.java | Maven test + seleção MySQL |
| API cliente/componente | unit | Contrato completo e erros, estados, foco, abort, resposta tardia | frontend/src/**/*.test.js e *.test.jsx | npm.cmd test |
| Página integrada | unit + e2e | Acesso de todos, arquivo e coordenação do cabeçalho, desktop/mobile | frontend/src/pages/*.test.jsx e frontend/e2e/*.spec.js | npm.cmd test + npm.cmd run test:e2e |

## Gate Check Commands

Maven C:/Users/Marco/apache-maven-3.9.16/bin/mvn.cmd, JAVA_HOME JDK25.0.2, source17; Node/npm existentes. Executar na pasta correta. Gates completos sequenciais para evitar contenção já observada; nenhum timeout alterado.

| Gate | Comando/condição |
| --- | --- |
| compile | Em sistema: mvn.cmd -B test-compile, perfil de teste existente; nenhuma aplicação contra banco real |
| java | Em sistema: mvn.cmd -B test; >=840 casos anteriores + todos novos executados, zero falhas/erros/skips |
| quick | Em frontend: npm.cmd test; >=1000 casos anteriores + todos novos, zero falhas/skips |
| full | java e quick sequenciais; lint npm.cmd run lint; build Vite em pasta TEMP |
| browser | full + npm.cmd run test:e2e; >=15 casos anteriores + novos, zero falhas/skips/retries; desktop e360px |
| mysql | Instância fictícia identificada por datadirTEMP/schema/porta, driverMySQL, schema create-drop; seleção dos testes novos e regressão de rotas tocadas, zero skips; nunca usar configuração real |
| close | browser + mysql + Verifier e sensor mínimo5 killed + validate_state.py0; nenhuma conclusão antes desses resultados |

Contagens novas abaixo são mínimos planejados para evitar tarefa sem prova, não números já executados. Mais casos parametrizados necessários aos ramos não podem ser omitidos para atingir o mínimo. registrar contagem real e assertions na evidence.md criada pela execução.

## Execution Plan

Fases sequenciais; dependência na tarefa imediatamente anterior para evitar edição concorrente dos serviços compartilhados.

### Phase 1: Diário e estrutura do quadro

```text
T1 -> T2 -> T3 -> T4 -> T5 -> T6 -> T7
```

### Phase 2: Demandas, comentários e leitura

```text
T7 -> T8 -> T9 -> T10 -> T11 -> T12 -> T13 -> T14
```

### Phase 3: Interface e fluxo completo

```text
T14 -> T15 -> T16 -> T17 -> T18
```

18 tarefas em3 lotes de fases inteiras (7/7/4), com aceite explícito recebido. Executar sequencialmente, um trabalhador por lote. A validação independente final permanece obrigatória.

## Task Breakdown

### T1: Persistir entidade de atividade

**Status:** Implemented em10/10/2026; compile PASS. Verificação independente pendente.

**What**: Criar a entidade do diário com snapshots, índice e vínculo histórico único conforme design.
**Where**: `sistema/src/main/java/com/patp/sistema/model/Atividade.java`
**Depends on**: None
**Requirement**: ACT-15/17/23/29
**Reuses**: Entidades JPA existentes e precisão micros.
**Tests**: none — entidade, conforme matriz; sem lógica de writer nesta tarefa.
**Gate**: compile
**Done when**:
- [x] Mapeamento compila, sem FK ao alvo/autor nem cascade que apague snapshots; coluna comporta valores longos.
- [x] Gate compile PASS; tarefa/evidência incluídas no diagnóstico, sem declarar feature concluída.
**Commit**: feat(atividade): persist activity snapshots

### T2: Gravar diário na transação atual

**Status:** Implemented em10/10/2026;853 Java PASS,13 novos. Verificação independente pendente.

**What**: Criar writer com diferenças e persistência atômica; repositório e tipos são suporte deste único entregável.
**Where**: `sistema/src/main/java/com/patp/sistema/service/AtividadeRegistroService.java`
**Depends on**: T1
**Requirement**: ACT-15/17/23/29
**Reuses**: Sessão passada pelo serviço autorizado, relógio micros e JPA.
**Tests**: integration — mínimo10 casos de diferenças, null, literal10000, vínculo único/mesmo quadro e rollback; limpeza mecânica em ApiIntegrationSupport e snapshot adicional dos novos testes.
**Gate**: java
**Done when**:
- [x] Writer exige transação existente, não autoriza payload de cliente nem grava texto de comentário; diferenças ordenadas/snapshots preservados.
- [x] Persistência H2 e rollback aprovados; baseline840 conservada e gate java PASS.
**Commit**: feat(atividade): record transactional changes

### T3: Registrar criação do quadro

**Status:** Implemented em10/10/2026;860 Java PASS,7 novos. Verificação independente pendente.

**What**: Instrumentar criarGerenciamento com evento único e identificação das etapas iniciais.
**Where**: `sistema/src/main/java/com/patp/sistema/service/GerenciamentoService.java`
**Depends on**: T2
**Requirement**: ACT-01/15/17/18/19
**Reuses**: Criação transacional e etapas finais automáticas.
**Tests**: integration — mínimo6, com/sem trabalho, autoria e dados forjados, falha da atividade e contagem única.
**Gate**: java
**Done when**:
- [x] Evento de criação único com autor/horário e snapshot inicial; finais não viram ações falsas.
- [x] Ramo inválido e rollback sem atividade; gate java PASS.
**Commit**: feat(atividade): audit board creation

### T4: Registrar edição e arquivo do quadro

**Status:** Implemented em10/10/2026;873 Java PASS,13 novos. Verificação independente pendente.

**What**: Instrumentar editar e mudarEstado no mesmo serviço com diferenças e regras de no-op atuais.
**Where**: `sistema/src/main/java/com/patp/sistema/service/GerenciamentoService.java`
**Depends on**: T3
**Requirement**: ACT-02/03/15/16/17/18/19/38
**Reuses**: Guard, versões e idempotência existentes.
**Tests**: integration — mínimo12, alteração real/idêntica, arquivo/restauração/repetição, autor/criador/admin, falha/versão/concorrência.
**Gate**: java
**Done when**:
- [x] Antes/depois após lock; arquivo idempotente sem evento; versão atual mantida.
- [x] Contagens e rollback completos; gate java PASS.
**Commit**: feat(atividade): audit board changes

### T5: Registrar criação de etapa

**Status:** Implemented em10/10/2026;887 Java PASS,14 novos. Verificação independente pendente.

**What**: Instrumentar criar preservando sequência anterior e posições reorganizadas.
**Where**: `sistema/src/main/java/com/patp/sistema/service/EtapaService.java`
**Depends on**: T4
**Requirement**: ACT-04/07/15/17/18/19
**Reuses**: Sequência, confirmar e validações existentes.
**Tests**: integration — mínimo7, primeira/meio/fim, empates/lacunas, permissão, conflito e rollback.
**Gate**: java
**Done when**:
- [x] Um evento por criação inclui todos os alvos com posição alterada.
- [x] Valores persistidos e falhas conferidos; gate java PASS.
**Commit**: feat(atividade): audit stage creation

### T6: Registrar edição e reordenação de etapa

**What**: Instrumentar editar com snapshots de nome/setor e de todas as posições alteradas.
**Where**: `sistema/src/main/java/com/patp/sistema/service/EtapaService.java`
**Depends on**: T5
**Requirement**: ACT-05/07/15/16/17/18/19
**Reuses**: Edição sequencial e normalização atuais.
**Tests**: integration — mínimo8, renomear/setor/posição, idêntica, normalização, renomeação posterior e recusas/rollback.
**Gate**: java
**Done when**:
- [ ] Antes/depois somente dos campos diferentes, com efeitos da reordenação no mesmo evento.
- [ ] Identificação histórica não muda após edição futura; gate java PASS.
**Commit**: feat(atividade): audit stage edits

### T7: Registrar remoção de etapa

**What**: Instrumentar excluir com snapshot anterior independente da entidade removida.
**Where**: `sistema/src/main/java/com/patp/sistema/service/EtapaService.java`
**Depends on**: T6
**Requirement**: ACT-06/07/15/17/18/19
**Reuses**: Bloqueio de etapa ocupada e proteção das finais.
**Tests**: integration — mínimo7, remoção vazia/última/meio, demandas ativas/arquivadas, finais e falha do writer.
**Gate**: java
**Done when**:
- [ ] Evento continua persistido após remover; posições registradas; recusas não geram evento.
- [ ] Rollback restaura etapa/sequência/diário; gate java PASS.
**Commit**: feat(atividade): preserve removed stage activity

### T8: Registrar cadastro de demanda

**What**: Instrumentar salvarInicial com valores iniciais e vínculo ao histórico automático.
**Where**: `sistema/src/main/java/com/patp/sistema/service/ProcessoService.java`
**Depends on**: T7
**Requirement**: ACT-08/15/17/18/19/23
**Reuses**: Cadastro moderno/antigo, guard global e unicidade.
**Tests**: integration — mínimo10 parametrizados nas duas rotas, campos opcionais/null, autoria, duplicidade, arquivo/versão e rollback.
**Gate**: java
**Done when**:
- [ ] Um evento e um histórico vinculados por cadastro, versões modernas/antigas atuais mantidas.
- [ ] ID/número não reconstruídos do estado futuro; gate java PASS.
**Commit**: feat(atividade): audit demand creation

### T9: Registrar edição informativa da demanda

**What**: Instrumentar executarEdicao com diferenças de todos os campos editáveis.
**Where**: `sistema/src/main/java/com/patp/sistema/service/ProcessoService.java`
**Depends on**: T8
**Requirement**: ACT-09/15/16/17/18/19/23
**Reuses**: Edição central moderna/antiga e validações.
**Tests**: integration — mínimo12, cada campo,10000 literal, null/limpeza, idêntica, concluída/cancelada, imutabilidade do número e rollback/recusas.
**Gate**: java
**Done when**:
- [ ] Valores normalizados reais antes/depois, sem alterar encerramento ou número.
- [ ] Edição idêntica aceita com alterações[]; falhas conservam tudo; gate java PASS.
**Commit**: feat(atividade): audit demand edits

### T10: Registrar transições de demanda

**What**: Instrumentar executarTransicao com evento e diferenças do ciclo para mover/concluir/cancelar/reabrir.
**Where**: `sistema/src/main/java/com/patp/sistema/service/ProcessoService.java`
**Depends on**: T9
**Requirement**: ACT-10/11/12/15/17/18/19/23
**Reuses**: Transição central, finais e histórico atuais.
**Tests**: integration — mínimo18, destinos/finais, motivo, reabertura de dois estados, campos limpos, rotas antigas, todos os ramos recusados e rollback.
**Gate**: java
**Done when**:
- [ ] Cada ação aceita tem tipo certo e diferenças completas, conservando ciclos antigos.
- [ ] Concorrência coordenada e falhas não deixam atividade; gate java PASS.
**Commit**: feat(atividade): audit demand transitions

### T11: Registrar arquivo de demanda

**What**: Instrumentar mudarArquivo sem alterar etapa, ciclo ou identificação.
**Where**: `sistema/src/main/java/com/patp/sistema/service/ProcessoService.java`
**Depends on**: T10
**Requirement**: ACT-13/15/17/18/19/23
**Reuses**: Arquivar/restaurar central e bloqueios.
**Tests**: integration — mínimo12, três estados, arquivo/restauração, repetição409, versão/permissão, falha e corrida de arquivo de quadro.
**Gate**: java
**Done when**:
- [ ] Um evento vinculado ao histórico por mudança, com somente marcador alterado.
- [ ] Antes/depois e versões corretos sob concorrência; gate java PASS.
**Commit**: feat(atividade): audit demand archive

### T12: Registrar envio de comentário

**What**: Instrumentar gravar nas rotas de comentários sem repetir a mensagem.
**Where**: `sistema/src/main/java/com/patp/sistema/service/ComentarioService.java`
**Depends on**: T11
**Requirement**: ACT-14/15/17/18/19
**Reuses**: Autor real, validação, micros e lock antes da entidade.
**Tests**: integration — mínimo12, duas rotas, três estados, texto ausente do diário/DTO, horário exato, rollback e arquivo/restauração concorrentes.
**Gate**: java
**Done when**:
- [ ] Evento tem autor/demanda/horário e alterações[], sem mensagem, histórico ou incremento de versão.
- [ ] Corridas modernas/antigas e rollback completos; gate java PASS.
**Commit**: feat(atividade): audit comment sends

### T13: Fechar escrita manual externa

**What**: Recusar registrarExterno em demanda ativa mantendo consulta e precedência de arquivo.
**Where**: `sistema/src/main/java/com/patp/sistema/service/HistoricoService.java`
**Depends on**: T12
**Requirement**: ACT-18/22/24
**Reuses**: Verificações de existência/arquivo e POST atual.
**Tests**: integration — mínimo8 novos: autor/ação forjados, nulo/false de arquivo, leitura literal antiga,401/arquivo/ausência; três cenários antigos abaixo somente após autorização.
**Gate**: java
**Done when**:
- [ ] ACT-24 confirmado e autorização exata recebida antes de alterar qualquer outcome antigo.
- [ ] Externo409 sem escrita, interno automático intacto, dados antigos e leitura preservados; gate java PASS.
**Commit**: fix(atividade): reject manual activity forgery

Adequações concretas propostas: HistoricoArchiveTests.ativoRegistraManualEAutomaticoNaMesmaProtecaoDepoisPermiteConsultaArquivada passa a preparar MANUAL/Informado via fixture, provar POST409 sem mudança e manter todas as verificações de conclusão/autoria/leitura/preservação. DemandaArquivoHistoricoApiTests.postAtivoPermaneceCompativelParaQualquerAutenticado passa a exigir409/erro exato e preservação integral, preparando uma linha literal anterior para conferir leitura. DemandaArquivoHistoricoTests.externoAceitaAtivoIncluindoNullSemNormalizacao conserva os dois estados null/false, exige409 e compara toda a persistência, com linha anterior literal de fixture. Não mudar os casos de arquivo, histórico interno ou401; não excluir/ignorar testes.

### T14: Consultar atividade por quadro

**What**: Criar consulta de duas fontes com cursor estável, DTO e GET de suporte ao único entregável de leitura.
**Where**: `sistema/src/main/java/com/patp/sistema/service/AtividadeService.java`
**Depends on**: T13
**Requirement**: ACT-20/21/22/23/25/26/27/28/29/39
**Reuses**: Sessão/guard/erros atuais, JdbcTemplate parametrizado; controller/DTO são suporte direto do endpoint testado aqui.
**Tests**: integration — mínimo22, happy/vazio/arquivos/leitor/401/404/400, mais100 entradas, empate/nulos, inserts entre páginas, repetição, fonte literal, DTO sem segredo/texto e outro quadro; H2 e mysql.
**Gate**: full + mysql
**Done when**:
- [ ] GET tem todas as branches e isolamento provados; nenhuma escrita, duplicação ou alteração histórica.
- [ ] Mesmos resultados e rollback das ações tocadas em MySQL fictício; gates full/mysql PASS.
**Commit**: feat(atividade): expose stable board activity

### T15: Validar consulta no cliente

**What**: Acrescentar consultarAtividade com validação estrita do contrato e cursor.
**Where**: `frontend/src/services/api.js`
**Depends on**: T14
**Requirement**: ACT-28/29/37
**Reuses**: requisicao, abort e validações atuais.
**Tests**: unit — mínimo24, cada campo inválido, origem, enum, mudança, null permitido, pares origem/ID e chaves duplicados, gerenciamentoId divergente,51 registros, cursor e status/abort. Pertencimento real do alvo é provado no servidor; o cliente verifica os vínculos explícitos do DTO.
**Gate**: quick
**Done when**:
- [ ] Rejeita página inválida inteira; não retorna vazio nem dados parciais no erro.
- [ ] >=1000 casos anteriores preservados e gate quick PASS.
**Commit**: feat(atividade): validate activity responses

### T16: Exibir painel de atividade

**What**: Criar AtividadeLateral de leitura com estados, paginação, foco e atualização.
**Where**: `frontend/src/components/AtividadeLateral.jsx`
**Depends on**: T15
**Requirement**: ACT-30/31/32/33/34/36/37
**Reuses**: Padrões de ComentariosLateral, CSS carregado em index.css como suporte visual da mesma tarefa.
**Tests**: unit — mínimo24, antes/depois literal, vazio/erro, duas fontes/nulos, retry/duplo clique, abort/late/unmount, foco/Escape e atualização que falha.
**Gate**: quick + lint/build
**Done when**:
- [ ] Nenhuma ação de escrita; foco, lista conservada e texto escapado provados.
- [ ] CSS no arquivo efetivamente carregado; gate quick/lint/build PASS.
**Commit**: feat(atividade): add activity sidebar

### T17: Integrar atividade ao quadro

**What**: Adicionar botão no cabeçalho e coordenação exclusiva com os fluxos existentes.
**Where**: `frontend/src/pages/Quadro.jsx`
**Depends on**: T16
**Requirement**: ACT-20/30/34/35/36
**Reuses**: ID/snapshot do quadro, pending/formulários/comentários e arquivo.
**Tests**: unit + e2e — mínimo12 casos Vitest de criador/admin/leitor/arquivo, cada formulário/pending, rascunho/foco/mudança de quadro; cenário de navegador co-localizado em atividade.spec.js demonstra abrir/fechar em quadro ativo/arquivado.
**Gate**: browser
**Done when**:
- [ ] Cabeçalho disponível a todos na consulta; cartões mantêm ações atuais.
- [ ] Rascunhos e bloqueios anteriores preservados, >=15 casos E2E anteriores e novo cenário aprovados.
**Commit**: feat(atividade): integrate board activity

### T18: Provar fluxo completo entre duas contas

**What**: Acrescentar cenário de navegador de ações reais, antes/depois e preservação histórica.
**Where**: `frontend/e2e/atividade.spec.js`
**Depends on**: T17
**Requirement**: ACT-01–39
**Reuses**: Runner Edge/H2 e API request das contas fictícias; roteiro humano em docs/OPERACAO.md é suporte da mesma demonstração.
**Tests**: e2e — mínimo2 novos cenários de fluxo completo e paginação/fontes mistas, além do cenário integrado T17; desktop e360px, captura/asserções de controles e strings reais.
**Gate**: browser + mysql; close executado pelo orquestrador após o commit desta tarefa
**Done when**:
- [ ] Dados e autores reais da sessão, edição, etapas removidas, comentário sem texto, arquivo/restauração e histórico existente comprovados no navegador.
- [ ] Gates técnicos finais aprovados; commit da tarefa com estado Implemented, sem declarar Verified antes do Verifier fresco automático.

Fechamento obrigatório posterior ao commit: Verifier fresco com critérios39/39, sensor>=5 killed e validate_state0; fechamento documental marca conclusão e aceitação humana separada. O commit de T18 exige gates técnicos aprovados e não pode depender de um Verifier que só é disparado depois dele.
**Commit**: test(atividade): verify complete activity flow

## Diagram-Definition Cross-Check

| Task | Depends On | Diagram Shows | Status |
| --- | --- | --- | --- |
| T1 | None | início | Match |
| T2 | T1 | T1 -> T2 | Match |
| T3 | T2 | T2 -> T3 | Match |
| T4 | T3 | T3 -> T4 | Match |
| T5 | T4 | T4 -> T5 | Match |
| T6 | T5 | T5 -> T6 | Match |
| T7 | T6 | T6 -> T7 | Match |
| T8 | T7 | fronteira Phase1 -> Phase2 | Match |
| T9 | T8 | T8 -> T9 | Match |
| T10 | T9 | T9 -> T10 | Match |
| T11 | T10 | T10 -> T11 | Match |
| T12 | T11 | T11 -> T12 | Match |
| T13 | T12 | T12 -> T13 | Match |
| T14 | T13 | T13 -> T14 | Match |
| T15 | T14 | fronteira Phase2 -> Phase3 | Match |
| T16 | T15 | T15 -> T16 | Match |
| T17 | T16 | T16 -> T17 | Match |
| T18 | T17 | T17 -> T18 | Match |

## Test Co-location Validation

| Task | Layer | Matrix requires | Task says | Status |
| --- | --- | --- | --- | --- |
| T1 | Entity | none/compile | none/compile | OK |
| T2 | Writer/repository | integration | integration/java | OK |
| T3 | Service | integration | integration/java | OK |
| T4 | Service | integration | integration/java | OK |
| T5 | Service | integration | integration/java | OK |
| T6 | Service | integration | integration/java | OK |
| T7 | Service | integration | integration/java | OK |
| T8 | Service | integration | integration/java | OK |
| T9 | Service | integration | integration/java | OK |
| T10 | Service | integration | integration/java | OK |
| T11 | Service | integration | integration/java | OK |
| T12 | Service | integration | integration/java | OK |
| T13 | Service/POST | integration | integration/java | OK |
| T14 | Query/GET | integration | integration/full/mysql | OK |
| T15 | API client | unit | unit/quick | OK |
| T16 | Component | unit | unit/quick/lint/build | OK |
| T17 | Page | unit + e2e | unit + e2e/browser | OK |
| T18 | Browser | e2e | e2e/close | OK |

## Requirement map

ACT-01 T3;02/03 T4;04 T5;05 T6;06 T7;07 T5–7;08 T8;09 T9;10–12 T10;13 T11;14 T12;15 T2–12;16 T4/T6/T9;17/18/19 T2–13 conforme operação;20/21/22/23/25/26/27/28/29/39 T14;24 T13;30/34 T16–17;31/32/33/36 T16;35 T17;37 T15–16;38 T4. T18 prova vertical sem substituir testes co-localizados. Todas pendentes, nenhuma implementada.

## Execution notes

Não alterar testes antigos além dos outcomes autorizados T13. Se um outro teste espera comportamento genuinamente errado, registrar arquivo/caso/antes/depois e perguntar antes de editar; fixture/compilação/limpeza sem alteração do comportamento esperado são ajustes mecânicos. Não limpar target/node_modules/test-results, não stash/reset. Evidências e validação serão criadas quando suas fases acontecerem, não arquivos vazios antecipados.
