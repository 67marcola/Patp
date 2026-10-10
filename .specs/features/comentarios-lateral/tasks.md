# Comentários por demanda Tasks

## Execution Protocol

Implementar com a skill `tlc-spec-driven`, um entregável por tarefa, teste derivado da spec, gate aprovado e um commit atômico com status/evidência. Verifier independente obrigatório depois de T8. Registrar progresso parcial no diagnóstico; somente depois do PASS registrar requisitos concluídos e informar “incluído no diagnóstico”. Sem push, deploy, banco real, upgrade ou alteração dos gerados preexistentes.

**Spec:** spec.md. **Design:** inline na spec, sem documento separado: complexidade média e padrões existentes. **Status:** Complete. Proposta/defaults/plano e adequações antigas explicitamente aprovados pelo usuário em 10/10/2026 nas duas perguntas. O plano inicial tinha sete tarefas. Dois E2E revelaram uma divergência real de precisão em COM-10/11/23; acrescentada uma correção atômica T7 antes da prova final T8, sem ampliar comportamento ou pedir mudança de expectation. Oito tarefas cabem em um lote; execução inline, sem workers. A verificação final usa um novo agente conforme a TLC.

## Test Coverage Matrix

Gerada da base: docs/OPERACAO.md, sistema/pom.xml, frontend/package.json, vitest.config.js e playwright.config.js. Não há AGENTS.md ou regra adicional de cobertura encontrada. Aplicar defaults fortes da TLC aos critérios e bordas deste corte. Amostras: ComentarioArchiveTests, DemandaArquivoComentarioTests, DemandaArquivoApiTests, DemandaEdicaoServiceTests, apiArquivo.test.js, QuadroTransicoes.test.jsx, QuadroArquivo.test.jsx e e2e/arquivamento.spec.js. Nenhuma expectativa antiga alterada durante o planejamento.

| Code Layer | Required Test Type | Coverage Expectation | Location Pattern | Run Command |
| --- | --- | --- | --- | --- |
| Serviço/autoria/transação | integration | Cada COM-01–14 aplicável e borda, valores físicos, rollback e concorrência | sistema/src/test/java/com/patp/sistema/*Tests.java | mvn.cmd -B verify, em sistema |
| Rotas novas/antigas e query | integration | GET/POST: sucesso, falhas, isolamento, shape e todos os campos/flags; H2 e seleção MySQL fictícia | sistema/src/test/java/com/patp/sistema/Comentario*Tests.java | mvn.cmd -B verify; selecionados com -Dtest, banco fictício explícito |
| API cliente | unit | Confirmação completa de dados, status, valores, erros e ausência de retry | frontend/src/services/apiComentarios.test.js | npm.cmd test -- src/services/apiComentarios.test.js |
| Painel e integração Quadro | unit | Cada COM-17–28 aplicável; campos/lista/foco/pending/erros/respostas atrasadas | frontend/src/pages/*Comentarios*.test.jsx | npm.cmd test |
| Navegador | e2e | Duas contas, duas demandas, consulta/envio/recarga/arquivo/erro/manual; teclado desktop/celular | frontend/e2e/comentarios.spec.js | npm.cmd run test:e2e |

## Gate Check Commands

Comandos extraídos dos manifests/OPERACAO; usar npm.cmd no Windows. Logs e build final em TEMP. Nenhum teste deve usar o MySQL configurado do sistema. Seleção MySQL usa fixture criada com dados fictícios, porta/schema/TEMP próprios documentados no evidence.md; parâmetros somente depois de existirem. Validar zero falhas, zero skips, contagem e nenhuma exclusão antes do commit.

| Gate Level | When to Use | Command |
| --- | --- | --- |
| Quick | T4/T5, API/painel | Em frontend: npm.cmd test -- seguido dos arquivos da tarefa; npm.cmd run lint |
| Full | T1/T2, serviço/rotas | Em sistema: mvn.cmd -B verify, com todos os testes Java/H2; teste HTTP via MockMvc co-localizado |
| Build | T3, fim servidor | mvn.cmd -B verify; em frontend npm.cmd test e npm.cmd run lint; build Vite em diretório TEMP |
| Build | T6/T8, integração/final | Java/H2 completo, npm.cmd test, npm.cmd run lint, npm.cmd run build -- --outDir TEMP absoluto, npm.cmd run test:e2e; seleção MySQL fictícia dos testes de comentários/arquivo com URL/credenciais explícitas |

Baseline documentada, não reexecutada no planejamento: 747 Java/H2, 881 Vitest, 13 E2E/7 arquivos. Gates finais não podem reduzir esses cenários. Contagem exata dos casos novos será registrada após execução; metas mínimas por tarefa abaixo são propostas, parametrizações contam como casos.

## Execution Plan

```text
T1 -> T2 -> T3 -> T4 -> T5 -> T6 -> T7 -> T8 -> T9
```

Primeiro fechar autoria/compatibilidade e rotas, depois cliente/painel/Quadro e prova de navegador. A sequência é estrita. Todas as tarefas dependem somente da anterior; o gate da anterior precisa estar aprovado antes de iniciar.

## Task Breakdown

### T1: Corrigir a função de envio existente para autoria da sessão

**What:** tornar o envio atual autenticado no serviço, validar texto, conservar lock/rollback e usar o autor real em toda escrita de comentário.
**Where:** `sistema/src/main/java/com/patp/sistema/service/ComentarioService.java`
**Supporting files:** ComentarioController.java para passar token; novos ComentarioEnvioTests.java e adequações autorizadas em ComentarioArchiveTests.java/DemandaArquivoComentarioTests.java. São wiring/testes do mesmo envio, sem segunda funcionalidade.
**Depends on:** None
**Reuses:** SessaoService, GerenciamentoGuard, READ_COMMITTED e ApiIntegrationSupport.
**Requirements:** COM-01–04, COM-07–14.
**Tools:** exec_command/apply_patch; skill tlc-spec-driven já escolhida pelo usuário.
**Done when:**
- [x] Envio antigo conserva shape/HTTP200 e passa todos os critérios aplicáveis, inclusive nome falso ignorado e texto literal.
- [x] 35 casos novos de autenticação, permissões, limites, estados, rollback e disputa com quadro/demanda; todos os testes antigos preservados, adequações autorizadas.
- [x] Full aprovado: 782 Java/H2,0 falhas/erros/skips. Seleção44 PASS. Evidência bidirecional em evidence.md; T1 concluída como tarefa, feature parcial até Verifier.
**Tests:** integration
**Gate:** Full
**Commit:** feat(comentarios): use session authorship for writes

### T2: Criar a consulta da conversa por quadro e demanda

**What:** disponibilizar GET novo com flags/lista flat ordenada e conservar GET antigo, com404 de demanda inexistente.
**Where:** `sistema/src/main/java/com/patp/sistema/controller/ComentarioDemandaController.java`
**Supporting files:** DTOs ComentarioResponse/ConversaComentariosResponse, método de leitura em ComentarioService, query em ComentarioRepository e ComentarioConsultaTests.java. Apenas componentes necessários ao GET.
**Depends on:** T1
**Reuses:** SessaoService, lock do quadro, ApiExceptionHandler e model Comentario sem migração.
**Requirements:** COM-01, COM-06, COM-11, COM-15/16.
**Tools:** exec_command/apply_patch; skill tlc-spec-driven.
**Done when:**
- [x] GET novo possui shape exato, flags e IDs reais, mensagens isoladas/ordenadas, arquivo consultável e histórico literal; GET antigo mantém formato, com desempate e existência explícita.
- [x] HTTP de erro, empate, vazio, conteúdo antigo e ausência de senha/token com15 casos novos de consulta.
- [x] Full797 Java/H2 PASS,0 falhas/erros/skips; evidências e progresso parcial incluídos no diagnóstico.
**Tests:** integration
**Gate:** Full
**Commit:** feat(comentarios): expose demand conversation reads

### T3: Criar o endpoint de envio da lateral

**What:** disponibilizar POST novo com corpo estrito, vínculo conferido e resposta flat201.
**Where:** `sistema/src/main/java/com/patp/sistema/controller/ComentarioDemandaController.java`
**Supporting files:** método transacional de envio no quadro em ComentarioService e ComentarioDemandaApiTests.java; reusar DTO de T2 e função de T1.
**Depends on:** T2
**Reuses:** envio autenticado, guard, DTO flat e erros existentes.
**Requirements:** COM-01/02, COM-04–14, COM-16.
**Tools:** exec_command/apply_patch; skill tlc-spec-driven.
**Done when:**
- [x] POST201 confirma todos os cinco valores; corpo inválido/campos extras/tipos errados não gravam; vínculo e estado verificados sob lock.
- [x] 33 casos novos para o POST, valores físicos, rollback, efeitos e precedências especificadas; preservar todas as rotas antigas.
- [x] Build servidor/interface aprovado:830 Java/H2,881 Vitest,lint/build TEMP PASS;, dados fictícios explícitos e evidências; nenhum critério declarado concluído antes do Verifier.
**Tests:** integration
**Gate:** Build
**Commit:** feat(comentarios): add validated demand comment endpoint

### T4: Confirmar respostas de comentários no cliente

**What:** adicionar funções GET/POST autenticadas e validação completa do contrato, sem retry.
**Where:** `frontend/src/services/api.js`
**Supporting files:** apiComentarios.test.js, co-localizado com a API.
**Depends on:** T3
**Reuses:** requisicao, ApiError e validação de IDs/datas existentes.
**Requirements:** COM-18, COM-23/24 e bordas de confirmação.
**Tools:** exec_command/apply_patch; skill tlc-spec-driven.
**Done when:**
- [x] GET/POST só aceitam status/shapes/valores definidos; query de outra demanda, ordem/IDs repetidos e POST velho/divergente são falhas, sem aplicar/reenviar.
- [x] 76 casos de API, cobrindo dados completos, cada campo relevante e falhas HTTP/rede/JSON; todos os envios omitindo autor/horário.
- [x] Quick aprovado com assertions e outcomes registrados no evidence.md.
**Tests:** unit
**Gate:** Quick
**Commit:** feat(comentarios): confirm conversation API responses

### T5: Criar o componente lateral

**What:** entregar painel de consulta/envio com rascunho, atualização manual, readonly e fechamento acessível.
**Where:** `frontend/src/pages/ComentariosDemanda.jsx`
**Supporting files:** ComentariosDemanda.test.jsx e estilos exclusivos em App.css.
**Depends on:** T4
**Reuses:** funções de T4, padrões de estado/foco dos formulários existentes e botões comuns.
**Requirements:** COM-18–28 aplicáveis ao painel.
**Tools:** exec_command/apply_patch; skill tlc-spec-driven.
**Done when:**
- [x] Lista/autor/time/texto, limites, estados, pending, resultado incerto, GET manual, fechar/Escape e resposta atrasada cumprem os outcomes da spec.
- [x] 30 casos do painel, incluindo preservar rascunho, sem envio automático, falso sucesso, readonly descoberto por GET e descarte por troca/desmontagem.
- [x] Quick aprovado; evidências e status parcial registrados.
**Tests:** unit
**Gate:** Quick
**Commit:** feat(comentarios): add accessible demand sidebar

### T6: Integrar a lateral aos cartões do quadro

**What:** ligar o botão de todos os cartões ao painel e coordenar pending/navegação/foco do quadro.
**Where:** `frontend/src/pages/Quadro.jsx`
**Supporting files:** QuadroComentarios.test.jsx e adequações autorizadas nas seis expressões de QuadroTransicoes.test.jsx/QuadroDemandas.test.jsx/QuadroArquivo.test.jsx, mais quatro expectativas E2E em arquivamento.spec.js/transicoes.spec.js/demandas.spec.js explicitamente aprovadas nesta retomada.
**Depends on:** T5
**Reuses:** origem, montado, quadroAtual, operando, aoOcupar e bloqueio dos outros formulários.
**Requirements:** COM-17/20/22/26/27.
**Tools:** exec_command/apply_patch; skill tlc-spec-driven.
**Done when:**
- [x] Todos os cartões oferecem consulta; abrir não consulta outra demanda; status/permissão de mutações existentes preservados e nova ação de consulta permitida.
- [x] Pending alcança Voltar/Sair/formulários/troca; focus e late responses não vazam entre quadros; 13 casos novos de integração.
- [x] Build integração aprovado:830 Java/H2,1000 Vitest,326 MySQL fictício,13 Edge/H2,lint/build TEMP PASS; com suítes anteriores preservadas e MySQL fictício selecionado.
**Tests:** unit
**Gate:** Build
**Commit:** feat(comentarios): connect sidebar to demand cards

### T7: Conservar a precisão do horário confirmado

**What:** usar microssegundos na criação de comentários para resposta e valor persistido coincidirem exatamente nas duas rotas.
**Where:** `sistema/src/main/java/com/patp/sistema/service/ComentarioService.java`
**Supporting files:** ComentarioPrecisaoTests.java e evidências. Correção necessária de COM-10/11/23 encontrada pelos dois E2E de T8; testes de navegador permanecem intactos. Timestamp(6)/datetime(6) existentes, sem migração.
**Depends on:** T6
**Reuses:** função gravar comum às duas rotas, LocalDateTime e persistência existentes.
**Requirements:** COM-10/11/23.
**Tools:** exec_command/apply_patch; skill tlc-spec-driven.
**Done when:**
- [x] Com relógio controlado123456789ns, devolver/persistir123456000ns; GET preserva exatamente esse valor nas duas rotas.
- [x] Dois casos novos e Full Java/H2 aprovado, nenhum teste antigo alterado/removido/ignorado.
- [x] Evidências/status/commit atômico; novo horário preciso não modifica mensagens antigas.
**Tests:** integration
**Gate:** Full
**Commit:** fix(comentarios): preserve persisted timestamp precision

### T8: Provar o fluxo real de colaboração

**What:** exercitar a entrega integrada no navegador com dados fictícios e documentar o roteiro humano.
**Where:** `frontend/e2e/comentarios.spec.js`
**Supporting files:** docs/OPERACAO.md e evidence.md; mover os estilos exclusivos de comentários do App.css não importado para index.css ativo, correção de COM-19/28 revelada pelos E2E; cada cenário mantém todas as verificações de estado e persistência.
**Depends on:** T7
**Reuses:** isolado H2/Edge, duas contas, randomUUID e capturas em TEMP das suítes anteriores.
**Requirements:** COM-11/12/19/20/22/25/28 e fluxo P1 completo.
**Tools:** exec_command/apply_patch; skill tlc-spec-driven. Novo Verifier depois do commit final.
**Done when:**
- [x] Dois fluxos novos: colaboração/isolamento/recarga/arquivo e falha de envio com consulta manual sem retry; teclado, desktop/celular e QA visual aprovados.
- [x] Build final aprovado:832 Java/H2,1000 Vitest,328 MySQL fictício,15 Edge/H2,lint/build TEMP, zero falhas/skips e nenhum cenário anterior ignorado; logs e capturas localizados.
- [x] Status/evidências preparados para commit atômico e despacho imediato do Verifier fresco. Conclusão técnica somente após PASS e validate_state.
**Tests:** e2e
**Gate:** Build
**Commit:** test(comentarios): prove collaborative sidebar workflow

### T9: Provar concorrência também no envio moderno

**What:** fechar a lacuna COM-14 confirmada pelo Verifier: leitura da demanda antes do lock na rota nova sobrevivia aos testes. Acrescentar os mesmos cenários de arquivo/restauração nas duas ordens para adicionarNoQuadro, preservando todos os casos e resultados antigos.
**Where:** `sistema/src/test/java/com/patp/sistema/ComentarioEnvioTests.java`
**Supporting files:** DemandaArquivoComentarioTests.java para disputa com arquivo de demanda; somente duas classes da mesma prova de concorrência. Spec/evidências/memória/diagnóstico acompanham a correção. Sem mudança no comportamento do sistema.
**Depends on:** T8
**Reuses:** fixtures, transações READ_COMMITTED, latches, locks e assertions de preservação existentes.
**Requirements:** COM-07/08/10/11/12/14.
**Tools:** exec_command/apply_patch; re-verificação pelo Verifier independente após commit.
**Done when:**
- [x] Oito novos casos modernos cobrem quadro e demanda, arquivo/restauração, ambas as ordens, decisão após lock e dados físicos preservados.
- [x] Full840 Java/H2 e seleção336 MySQL fictícia aprovados, zero falhas/erros/skips, nenhum caso antigo removido ou expectativa alterada.
- [x] Evidências bidirecionais/status preparados para commit atômico; nova rodada independente obrigatória antes de Verified.
**Tests:** integration
**Gate:** Full
**Commit:** test(comentarios): cover modern archive concurrency

T9 é correção do primeiro ciclo de verificação, executada inline. O plano inicial de oito tarefas foi concluído antes do Verifier; esta tarefa fecha a cobertura de um requisito já aprovado e não adiciona funcionalidade.

## Task Granularity Check

| Task | Entregável atômico | Resultado |
| --- | --- | --- |
| T1 | Uma função de envio autenticada e seu wiring/testes | OK |
| T2 | Um endpoint GET e dependências para torná-lo testável | OK |
| T3 | Um endpoint POST e sua transação/testes | OK |
| T4 | Um componente de API de comentários no arquivo existente | OK |
| T5 | Um componente lateral e seus estilos/testes | OK |
| T6 | Integração do componente Quadro e seus testes | OK |
| T7 | Precisão de uma função de gravação e dois testes de regressão | OK |
| T8 | Fluxo E2E integrado e documentação de uso | OK |
| T9 | Prova de concorrência do envio moderno | OK |

## Diagram-Definition Cross-Check

| Task | Depends On | Diagram Shows | Status |
| --- | --- | --- | --- |
| T1 | None | None | OK |
| T2 | T1 | T1 | OK |
| T3 | T2 | T2 | OK |
| T4 | T3 | T3 | OK |
| T5 | T4 | T4 | OK |
| T6 | T5 | T5 | OK |
| T7 | T6 | T6 | OK |
| T8 | T7 | T7 | OK |
| T9 | T8 | T8 | OK |

## Test Co-location Validation

| Task | Code Layer | Matrix Requires | Task Says | Status |
| --- | --- | --- | --- | --- |
| T1 | Serviço/envio antigo | integration | integration | OK |
| T2 | Endpoint GET/query/DTO | integration | integration | OK |
| T3 | Endpoint POST/serviço | integration | integration | OK |
| T4 | API cliente | unit | unit | OK |
| T5 | Painel React | unit | unit | OK |
| T6 | Quadro React | unit | unit | OK |
| T7 | Serviço/precisão | integration | integration | OK |
| T8 | Navegador | e2e | e2e | OK |
| T9 | Concorrência do envio | integration | integration | OK |

## Adequações antigas autorizadas em 10/10/2026

Usuário respondeu Sim, adequar esses testes (recomendado) à descrição concreta abaixo. Preservar todos os cenários e demais assertions. Essa confirmação cobre somente estas adequações.

- ComentarioArchiveTests.java:48–63: manter sessão Outro e textos Comentário de Ana/Bia, inclusive nomes enviados Ana/Bia; expectativa de funcionario passa a Outro nas respostas e nas duas linhas físicas. Isso prova que o nome recebido não substitui a conta.
- DemandaArquivoComentarioTests.java:30–37: POST autenticado por Leitor passa a esperar Leitor na gravação e na consulta após arquivo, em vez de Informado. Texto literal, timestamp, ID, versão e preservação integral permanecem.
- DemandaArquivoComentarioTests.java:49/50/58: chamadas diretas passam o token de Dono à nova assinatura, sem parâmetro de autor livre; expectativa física passa de Informado a Dono. Manter as quatro disputas, timeout/lock, mensagens de409, rollback, flags, dados e histórico.
- QuadroTransicoes.test.jsx:44: listas exatas passam a começar com Comentários, seguidas das mesmas mutações atuais; :50/:55 passam a exigir lista exata somente Comentários, mantendo ausência de mutações em readonly/status desconhecido.
- QuadroDemandas.test.jsx:114: lista exata somente Comentários nos cartões antigos/arquivados, com todos os campos e restrições de cadastro preservados.
- QuadroArquivo.test.jsx:35: lista exata Comentários, Restaurar; :58: somente Comentários nos casos inelegíveis, mantendo todas as restrições de retirada/restauração.

São duas classes Java e seis expressões em três arquivos JSX. Novos testes de autoria/multiusuário e consulta serão acrescentados nas tarefas correspondentes. Nenhum teste é removido, ignorado ou reduzido.

## Technical Completion

T1-T9 complete, revision2 PASS, 28/28 COM Verified, 7/7 mutants killed, validate_state exit0. Human UAT pending separately. Evidence and diagnostic updated on 2026-10-10.
