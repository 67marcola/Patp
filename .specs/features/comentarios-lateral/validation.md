# Comentários por demanda Validation

**Result**: FAIL. Os gates completos passam, mas o sensor revelou cobertura insuficiente da concorrência do POST novo em COM-14. A implementação íntegra passou a prova adicional; a falha é de discriminação dos testes existentes, sem defeito reproduzido na implementação íntegra.

**Date:** 2026-10-10. **Verifier:** `/root/verifier_comentarios`, agente fresco independente, autor ≠ verificador. Primeira revisão deste corte.
**Spec:** `.specs/features/comentarios-lateral/spec.md`.
**Diff range:** `8584e5b..50da6da`; implementação `3a5934c..50da6da`.
**HEAD avaliado:** `50da6da9e6793fefab7db73961a888b6bf6b8fc7`, branch `testes`.
**Resultado:** 27/28 critérios com prova completa; COM-14 parcialmente provado na rota antiga e GAP na rota nova. Zero gaps de precisão da spec. Sensor: 7 injetados, 6 killed, 1 survived. UAT humano pendente separado.

## Task Completion

| Tarefa | Commit | Estado conferido |
| --- | --- | --- |
| T1 | 5dd0689 | Done; envio/autoria e 35 casos novos |
| T2 | 62d28f1 | Done; GET e 15 casos novos |
| T3 | 70f4c4d | Done; POST e 33 casos novos |
| T4 | d58729e | Done; cliente e 76 casos novos |
| T5 | e0e987a | Done; lateral e 30 casos novos |
| T6 | 29a37d3 | Done; Quadro e 13 casos novos |
| T7 | fa18670 | Done; precisão e 2 casos novos |
| T8 | 50da6da | Done; 2 E2E novos e documentação; fechamento da feature bloqueado pelo sensor |

Os oito commits incluem status/evidência das tarefas no mesmo commit. Há um commit de planejamento anterior. A sequência e os entregáveis correspondem a tasks.md; validate_spec e validate_tasks executados independentemente com zero erros/avisos. Não alterei tarefas, aplicação, testes ou traceabilidade reais.

## Spec-Anchored Acceptance Criteria

A tabela foi rederivada da spec e da leitura dos testes, sem usar o mapeamento do autor como prova. As citações apontam aos arquivos reais do HEAD avaliado. PASS significa assertion sobre o valor/estado especificado e execução independente aprovada em H2/MySQL ou Vitest/E2E aplicáveis.

| Critério | Outcome exato definido na spec | file:line + assertion | Resultado |
| --- | --- | --- | --- |
| COM-01 | GET/POST nas duas rotas: ausência401/Usuário não autenticado.; token inválido401/Sessão inválida ou expirada.; sem efeitos | `sistema/src/test/java/com/patp/sistema/ComentarioEnvioTests.java:89`: `status().isUnauthorized()`, `jsonPath("$.erro").value(invalida?...)`, persistido igual; `ComentarioConsultaTests.java:66` e `ComentarioDemandaApiTests.java:68` + :30 cobrem novas rotas com os mesmos valores | PASS |
| COM-02 | Comum/criador/admin enviam em ativos; quadro sem criador permite envio | `sistema/src/test/java/com/patp/sistema/ComentarioEnvioTests.java:43` e :46: HTTP200, nome real, texto e vínculo físicos nos cinco parâmetros; `ComentarioDemandaApiTests.java:41` e :45: HTTP201 e valores físicos nos quatro parâmetros | PASS |
| COM-03 | Campo funcionario falso antigo ignorado; nome da sessão devolvido e persistido | `sistema/src/test/java/com/patp/sistema/ComentarioEnvioTests.java:32`, :43/:46: corpo Autor falso, `value(autor.getNome())` e `containsEntry("funcionario",autor.getNome())`; :95 autores Ana/Bia exatos | PASS |
| COM-04 | Ausente/null/vazio/String.isBlank/>10000 UTF-16:400/mensagem de limite, nenhuma gravação;10000 aceito literalmente | `sistema/src/test/java/com/patp/sistema/ComentarioEnvioTests.java:55`–:70:400/mensagem exata, estado igual; string/emoji10000 `containsExactly(texto)`; `ComentarioDemandaApiTests.java:55`, :59/:63 confirma rota nova | PASS |
| COM-05 | JSON/tipo/campo extra inválido:400/Dados da requisição inválidos.; nenhuma gravação | `sistema/src/test/java/com/patp/sistema/ComentarioDemandaApiTests.java:49`–:51: dez corpos incluindo objeto/array/boolean/número/autor/campo extra; helper :30/`:31` exige código/mensagem exatos e estado igual | PASS |
| COM-06 | Quadro ausente404/Gerenciamento não encontrado.; demanda ausente/externa404/Demanda não encontrada.; sem vazamento/efeito | `sistema/src/test/java/com/patp/sistema/ComentarioConsultaTests.java:57`:404/mensagem exata/`$.comentarios` ausente/estado igual; :61 GET antigo404; `ComentarioDemandaApiTests.java:68`–:69 + :30/:31 exige POST novo404 e estado igual | PASS |
| COM-07 | Quadro arquivado impede POST antigo/novo409/Gerenciamento arquivado. Restaure-o antes de alterar.; sem efeito | `sistema/src/test/java/com/patp/sistema/ComentarioEnvioTests.java:82`–:84: comum/admin409/mensagem/estado igual; `ComentarioDemandaApiTests.java:68`–:69 + :30/:31 para novo | PASS |
| COM-08 | Demanda arquivada em quadro ativo impede POST409/Demanda arquivada. Restaure-a antes de alterar.; sem efeito | `sistema/src/test/java/com/patp/sistema/ComentarioEnvioTests.java:82`–:84 e `ComentarioDemandaApiTests.java:68`–:69/:30/:31; `frontend/e2e/comentarios.spec.js:103` exige mensagem, rascunho, lista e snapshot inalterados | PASS |
| COM-09 | Em andamento/Concluido/Cancelado/status desconhecido permitem mensagem sem normalizar status | `sistema/src/test/java/com/patp/sistema/ComentarioEnvioTests.java:75`–:76:200, processos iguais e texto físico exato, incluindo null/vazio; `ComentarioDemandaApiTests.java:74`:201/processos iguais nos quatro estados | PASS |
| COM-10 | Uma mensagem, ID positivo, texto literal, sessão, horário servidor, demanda correta; POST novo201/antigo200 e valor persistido confirmado | `sistema/src/test/java/com/patp/sistema/ComentarioEnvioTests.java:43`–:49; `ComentarioDemandaApiTests.java:41`–:46: status, cinco valores, ID positivo, horários entre limites e count2 com um antigo; `ComentarioPrecisaoTests.java:32`–:34:123456789ns→123456000ns, resposta=banco=GET e count1 em ambas as rotas | PASS |
| COM-11 | Comentários anteriores, nomes/textos antigos acima do limite, conservados após consulta/envio/arquivo/restauração | `sistema/src/test/java/com/patp/sistema/ComentarioEnvioTests.java:49`: linhas anteriores iguais; `ComentarioConsultaTests.java:29`–:35: texto>10000/nome/data/ID/vínculo exatos e estado igual; `frontend/src/services/apiComentarios.test.js:15`: antigo>10000/autor vazio aceitos; `frontend/e2e/comentarios.spec.js:74`–:82: lista flat inteira igual antes/depois de reload/arquivo/restauração | PASS |
| COM-12 | 14 campos, arquivo, etapa, versão, contagens e histórico não mudam ao comentar | `sistema/src/test/java/com/patp/sistema/ComentarioEnvioTests.java:50`: todas as linhas de processos/quadro/etapas/históricos iguais; `ComentarioDemandaApiTests.java:47` idem; `frontend/e2e/comentarios.spec.js:71`/:74: estrutura inteira `toEqual(c.inicial)`, histórico inteiro `toEqual(c.eventos)` | PASS |
| COM-13 | Falha física reverte transação sem inserir ou mudar associados | `sistema/src/test/java/com/patp/sistema/ComentarioEnvioTests.java:100`–:101 e `ComentarioDemandaApiTests.java:78`: CHECK físico recusa Falha; `assertThatThrownBy(...).isInstanceOf(RuntimeException.class)`, conteúdo de tabelas/quadro igual ao anterior | PASS |
| COM-14 | Disputas arquivo/restauração quadro/demanda: aguarda lock,409 quando arquivado ou aceita após restauração; apenas efeitos permitidos | `sistema/src/test/java/com/patp/sistema/ComentarioEnvioTests.java:112`–:117: quatro disputas de quadro, TimeoutException200ms,409/mensagem ou sucesso, lista/autor/versão exatos; `DemandaArquivoComentarioTests.java:50`–:60: quatro disputas de demanda, mesmos outcomes. Ambos chamam somente `adicionar`, não `adicionarNoQuadro`. M7 nova rota sobreviveu94 testes; probe scratch6/6 íntegro contra4/6 mutado confirma distinção semântica | GAP |
| COM-15 | GET200/envelope/flags reais/lista exclusiva ordenada dataHora/ID; vazio e arquivados | `sistema/src/test/java/com/patp/sistema/ComentarioConsultaTests.java:28`–:35: todos os valores e flags nos quatro pares; :40–:45 IDs/textos ordenados e nenhuma mensagem da outra demanda; :51 IDs/flags/lista vazia exatos nas duas contas relevantes | PASS |
| COM-16 | Apenas cinco campos do envelope/registro, sem entidades/senha/hash/token | `sistema/src/test/java/com/patp/sistema/ComentarioConsultaTests.java:31`–:34: ausência explícita de senha/token/processo e `containsExactlyInAnyOrderElementsOf(...)` nos dois níveis; `ComentarioDemandaApiTests.java:43` exige exatamente cinco chaves POST | PASS |
| COM-17 | Comentários oferecidos a conectados em cartões ativos/arquivo/encerrados/desconhecidos | `frontend/src/pages/QuadroComentarios.test.jsx:20`–:21: seis combinações, leitor, lista exata de ação Comentários, URL da demanda e campo presente somente ativo; adequações antigas preservam listas exatas de mutações | PASS |
| COM-18 | Identifica número/cliente; loading/lista/vazio; falha mostra erro e Tentar novamente, não aplica resposta inválida | `frontend/src/pages/ComentariosDemanda.test.jsx:20`–:22: nome/cliente/status/vazio exatos; :31 alerta, ausência campo e apenas GET/GET no retry; `frontend/src/services/apiComentarios.test.js:39` rejeita IDs/flags/lista/campos/ordem inválidos com mensagem e status200 | PASS |
| COM-19 | Texto literal/quebras, autor,dd/MM/yyyy HH:mm:ss local,time.dateTime ISO; HTML não executado | `frontend/src/pages/ComentariosDemanda.test.jsx:27`: texto literal/autor/29/02/2020 12:34:56/ISO123456789, nenhum img; `frontend/e2e/comentarios.spec.js:49`–:50/:65/:71 exige texto exato,time/autor,pre-wrap e nenhum img | PASS |
| COM-20 | Flags da conversa arquivada: motivo readonly, sem campo/envio | `frontend/src/pages/ComentariosDemanda.test.jsx:35`: três pares de flags, campo/envio ausentes e motivo exato; :63 consulta manual descobre arquivo/conserva rascunho; `frontend/e2e/comentarios.spec.js:79`/:82/:104 confirma ambos arquivos | PASS |
| COM-21 | Vazio/whitespace/>10000 UTF-16 bloqueia POST, preserva rascunho e anuncia limite; limite válido aceita literal | `frontend/src/pages/ComentariosDemanda.test.jsx:38`: cinco inválidos, erro exato, campo igual, uma única chamada GET; :41 emoji10000/NBSP válido, campo limpo e conteúdo literal | PASS |
| COM-22 | Enquanto POST pendente, apenas uma requisição; campo/fechar/troca/mutações/Voltar/Sair bloqueados | `frontend/src/pages/ComentariosDemanda.test.jsx:45`: todos os controles disabled, submit/Escape sem efeitos; `QuadroComentarios.test.jsx:38`: Voltar/troca/submit sem ação, GET/GET/POST exatos; :53–:54 Sair disabled→liberado; `frontend/e2e/comentarios.spec.js:95`–:96 mesmos controles reais/posts1 | PASS |
| COM-23 | Só201/contrato confirmado: cinco valores incluídos uma vez, campo limpo, lateral aberta | `frontend/src/services/apiComentarios.test.js:42`–:43: objeto inteiro igual, só texto enviado e sessão; `frontend/src/pages/ComentariosDemanda.test.jsx:46`–:47: lista2, ID22,texto/autor Bia/time exatos, campo vazio/lateral visível; `frontend/e2e/comentarios.spec.js:56`–:59 HTTP201/cinco campos/vínculo/horário/campo vazio | PASS |
| COM-24 | HTTP/rede/JSON/contrato não confirmado conserva texto/lista; aviso; novo POST bloqueado até GET válido; sem retry | `frontend/src/pages/ComentariosDemanda.test.jsx:54`: cinco falhas, campo Nova, lista1, envio disabled, aviso exato, GET/POST apenas; `frontend/src/services/apiComentarios.test.js:53`/:56/:59–:63 rejeita campos/status/ID repetido, message/status e chamada única; E2E :98–:100 exige estado conservado depois de resposta perdida e leitura falha | PASS |
| COM-25 | GET manual substitui lista/flags confirmadas, conserva rascunho/aviso, nenhum POST automático; leitura falha mantém bloqueio | `frontend/src/pages/ComentariosDemanda.test.jsx:55`: lista2, rascunho Nova, envio liberado, aviso, GET/POST/GET; :59 falha mantém lista1/rascunho/bloqueio; :63 descobre arquivo; `frontend/e2e/comentarios.spec.js:100`–:104 confirma persistência real/flags/rascunho/posts1 até reenvio humano deliberado | PASS |
| COM-26 | Fechar/Escape ocioso fecha e descarta rascunho; foco retorna ao botão original ou Voltar ausente | `frontend/src/pages/QuadroComentarios.test.jsx:26`: painel ausente/foco origem/reabertura vazia nos dois modos; :29 origem removida→foco Voltar; `frontend/e2e/comentarios.spec.js:77` por teclado/mobile, campo descartado | PASS |
| COM-27 | Troca demanda/quadro/desmontagem descarta conversa/rascunho e não aplica mensagem/erro atrasado | `frontend/src/pages/ComentariosDemanda.test.jsx:72`–:73: dois destinos novos sem campo antigo/Atrasada/alerta; :77 POST sucesso/erro após unmount sem painel/callback indevido; `QuadroComentarios.test.jsx:34` troca de cartão com URLs exatas; :43 quadro novo sem painel/alerta antigos | PASS |
| COM-28 | Teclado desktop/celular abre/lê/escreve/envia/atualiza/fecha; painel dentro viewport e sem overflow horizontal | `frontend/e2e/comentarios.spec.js:39`–:40/:56/:77/:93–:104: ações com foco/Enter/Escape; :65 limites0..width/height,painel/página sem overflow,pre-wrap, em1280x800 e390x844. Dois cenários PASS; três capturas inspecionadas independentemente | PASS |

## Edge Cases

| Borda da spec | Prova de outcome | Resultado |
| --- | --- | --- |
|10000/10001 UTF-16 e emoji | Envio:59/:67–:70; DemandaApi:57/:63; painel:37–:41, outcomes acima | PASS |
| Empate por ID sem perda | Consulta:40–:45, quatro IDs/textos exatos; apiComentarios:19 valida frações equivalentes com desempate e nanos | PASS |
| HTML/espaços/quebras | painel:27 e E2E:49/:65/:71, texto e ausência img exatos | PASS |
| GET de outra conversa/flag ausente/ID duplicado/ordem inválida | apiComentarios:22–:39, rejeição e chamada única; painel:31 não aplica falha de contrato | PASS |
| POST !=201/campo ausente/tipo/vínculo/texto/ID divergentes | apiComentarios:46–:63, rejeição; painel:54 conserva estado e bloqueia envio | PASS |
| Arquivo entre GET e POST | E2E:102–:104,409 exato, banco/lista iguais, GET descobre arquivo e mostra rascunho | PASS |
| GET manual falha após POST incerto | painel:59; E2E:100, bloqueio/rascunho/post-count conservados | PASS |

## Reverse Mapping e integridade dos testes

Cada família e cada parâmetro foi reivindicado por requisito/borda/Done when. Helpers não contam como testes. A lista abaixo cobre todos os testes novos; preservação das famílias antigas foi conferida no diff.

| Arquivo/famílias de teste | Casos | Critérios/bordas reivindicados |
| --- | --- | --- |
| ComentarioEnvioTests:34 participanteReal; :53 textoInvalido; :60 ausente; :66 limite; :73 estado; :79 arquivo; :87 sessão; :91 duasContas; :97 falhaFisica; :105 disputaQuadro |35| Respectivamente02/03/10/11/12;04;04;04/10;09/12;07/08;01;02/03/10;13;07/10/12/14 |
| ComentarioConsultaTests:24 flags/histórico; :37 ordem/isolamento; :48 vazio; :54 alvo externo; :59 antigo404; :64 sessão; :69 participantes |15|11/15/16;11/15/16 e empate;15;06;06/compatibilidade;01;01/15 |
| ComentarioDemandaApiTests:34 confirmaCampos; :50 corpoEstrito; :54 limites; :58 ausente; :61 limiteExato; :66 falhasHttp; :72 estado; :76 rollback |33|02/10/11/12/16;05/16;04;04;04/10;01/06/07/08;09/12;13 |
| ComentarioPrecisaoTests:21 duas rotas |2|10/11/23, Done when T7 microssegundos exatos |
| apiComentarios.test.js:8 GET; :13 legado/flags; :17 vazio/empate/fração; :38 GET inválido; :41 POST completo; :52 POST inválido; :55 ID repetido; :59 statuses de GET/POST; :60 HTTP de GET/POST; :61 rede/JSON de GET/POST |76|15/18;11/20;15;18 e bordas GET;23;24 e bordas POST;24;18/24;18/24;18/24 |
| ComentariosDemanda.test.jsx:18 loading/vazio/foco; :24 literal; :29 falhaGET; :33 readonly; :37 inválidos; :40 limite/NBSP; :43 pending/sucesso; :49 incerto/manual; :57 manual falho; :61 arquivo descoberto; :65 fechamento; :68 trocaGET; :75 desmontagemPOST |30|18/26;19;18;20;21;21/23;22/23;24/25;24/25;20/25;26;27;27 |
| QuadroComentarios.test.jsx:18 status/permissão; :23 fechamento; :28 fallback foco; :31 troca/mutações; :36 pending; :41 troca quadro; :45 App/Sair |13|17/20;26;26;22/27;22/23;27;22 |
| comentarios.spec.js:69 colaboração; :86 resposta perdida |2|02/03/10/11/12/15/17/19/20/23/26/28;07/08/11/12/20/22/24/25/28 |

Novos totais:85 Java,119 Vitest,2 E2E. Baselines747/881/13→832/1000/15, nenhuma redução. ComentarioArchiveTests preserva3 casos; somente snapshot de autoria autorizado muda para Outro. DemandaArquivoComentarioTests preserva6 casos, inclusive quatro disputas e suas assertions; token/autor Leitor/Dono são as adequações autorizadas. As seis expressões JSX em QuadroTransicoes/QuadroDemandas/QuadroArquivo e quatro E2E antigos passam a listas exatas incluindo Comentários, preservando a ausência de mutações e todas as demais assertions. Fixture de versão em ComentarioEnvioTests autorizada no contexto, com versão de preparação e incremento exato+1; as quatro disputas continuam presentes.

Nenhum @Disabled, skip, todo ou SPEC_DEVIATION encontrado nos testes da feature. Os testes antigos alterados mantêm seus cenários e assertions de dados/permissão; o diff não exclui testes. O probe de concorrência abaixo não integra esses totais e foi criado/compilado apenas em scratch.

## Gate Check

Gates sequenciais, sem executar Java simultaneamente a Vitest. Todos contra cópias temporárias e dependências locais existentes, sem upgrade. Fonte/testes reais e gerados preexistentes foram preservados.

| Gate | Comando | Resultado |
| --- | --- | --- |
| Estrutura | python.exe .agents/skills/tlc-spec-driven/scripts/validate_spec.py comentarios-lateral; validate_tasks.py comentarios-lateral |0 erros,0 avisos |
| Java/H2 completo + empacotamento | Em sistema: mvn.cmd -B verify |832 passed,0 failures,0 errors,0 skipped, exit0 |
| Vitest completo | Em frontend: npm.cmd test |1000 passed/25 arquivos,0 failed,0 skipped, exit0 |
| MySQL fictício selecionado + empacotamento | mvn.cmd -B verify -Dtest=Comentario*Tests,DemandaArquivo*Tests,ConfiguracaoArquivoResponseTests,HistoricoArchiveTests,GerenciamentoApiTests, driver/credenciais/URL/create-drop/porta explícitos |328 passed,0 failures,0 errors,0 skipped, exit0 |
| Lint | npm.cmd run lint |exit0 |
| Vite build | npm.cmd run build -- --outDir TEMP absoluto próprio |exit0; artefato construído na cópia, nunca dist real |
| Edge/H2 completo | npm.cmd run test:e2e |15 passed/8 arquivos,0 failed,0 skipped, exit0 na terceira tentativa intacta |

MySQL confirmado ANTES do gate via SELECT @@port,@@datadir,database(),@@version:127.0.0.1:33823,creral_arquivo_test,MySQL8.0.43,datadir `C:/Users/Marco/AppData/Local/Temp/creral-arquivo-049cfd5cc86d4d8b840e41f1710d7a43/mysql/`. A URL foi variável SPRING_DATASOURCE_URL para não truncar & no mvn.cmd. Instância fictícia mantida ativa para root; nenhum banco normal iniciado/usado.

Tentativas E2E negativas preservadas: primeira1 passed/3 failed em arquivamento por ECONNRESET seguido de ECONNREFUSED durante preparo; segunda processo webServer encerrou3221226505 antes de Vite disponível, depois de Maven/H2 inicializados. Terceira cópia de caminho curto executou todos15 intactos. Não atribuí causa definitiva ao caminho, cache, biblioteca ou aplicação; nenhum teste/retry/timeout foi afrouxado. O preparo da primeira tentativa Vitest também detectou dist de dependência omitido pela cópia e parou antes de testes; cópia completa das mesmas dependências corrigiu apenas o scratch.

**Logs independentes:** `C:/Users/Marco/AppData/Local/Temp/comentarios-verifier-f712f725b2d4402ba0d697593f32b0d8/`, arquivos gate-java-h2.log, gate-vitest.log, mysql-identity.log, gate-mysql.log, gate-lint.log, gate-vite-build.log, gate-e2e.log, gate-e2e-repeat.log e gate-e2e-short.log.

Capturas novas inspecionadas: `C:/Users/Marco/AppData/Local/Temp/creral-comentarios-qa-VbOtO4/desktop-conversa.png`, mobile-conversa.png e `C:/Users/Marco/AppData/Local/Temp/creral-comentarios-falha-qa-ON9BlQ/mobile-recuperacao.png`. Texto literal/autor/time legíveis, quebras preservadas, consulta readonly e rascunho visíveis; geometria/ausência de overflow aprovadas por assertions de navegador. Isso não é UAT humano.

## Discrimination Sensor

**Profundidade:** crítica, autenticação/autoria e integridade de dados;7 falhas semânticas manuais, acima do mínimo5. Nenhuma falha de compilação aceita como kill. Nenhum stash. Controles antes e depois:94/94 Java/H2,76/76 API PASS. Cada mutação partiu do código íntegro e foi revertida somente na cópia.

| Mutante | Código real de referência | Fault injetado exclusivamente no scratch | Outcome dos testes existentes |
| --- | --- | --- | --- |
| M1-session-author | `sistema/src/main/java/com/patp/sistema/service/ComentarioService.java:71` | Nome de sessão→Intruso | KILLED:94 testes,20 assertion failures,0 errors/skips,exit1 |
| M2-utf16-limit | `sistema/src/main/java/com/patp/sistema/service/ComentarioService.java:63` | >10000→>=10000 | KILLED:94 testes,3 assertion failures,0 errors/skips,exit1 |
| M3-literal-text | `sistema/src/main/java/com/patp/sistema/service/ComentarioService.java:70` | texto literal→texto.trim() | KILLED:94 testes,12 assertion failures,0 errors/skips,exit1 |
| M4-demand-archive | `sistema/src/main/java/com/patp/sistema/service/ComentarioService.java:59` | Remover recusa de demanda arquivada | KILLED:94 testes,6 assertion failures,0 errors/skips,exit1 |
| M5-conversation-flags | `sistema/src/main/java/com/patp/sistema/service/ComentarioService.java:108` | Inverter quadroArquivado no envelope | KILLED:94 testes,5 assertion failures,0 errors/skips,exit1 |
| M6-post-status | `frontend/src/services/api.js:296` | Confirmar HTTP200 além de201 | KILLED:76 testes,1 failed/75 passed,exit1; assertion de rejeição `frontend/src/services/apiComentarios.test.js:59` |
| M7-new-post-prefetch | `sistema/src/main/java/com/patp/sistema/service/ComentarioService.java:81` | Inserir `processoRepository.findById(processoId);` imediatamente ANTES de bloquear no POST novo; entidade/quadro ficam no persistence context antes de aguardar outra transação | SURVIVED:94 passed,0 failures/errors/skips,exit0 |

**Resultado:**6 killed/1 survived, FAIL.

M7 não é equivalente: criei em scratch um probe derivado dos outcomes de COM-14, usando os cenários de arquivo/restauração de demanda nas duas ordens e chamando adicionarNoQuadro. Código preservado como `C:/Users/Marco/AppData/Local/Temp/comentarios-verifier-f712f725b2d4402ba0d697593f32b0d8/probe-SensorConcorrenciaNovaTests.java:49`–:60. Controle íntegro:6 passed,0 failures/errors/skips. Mutante:6 executados,4 passed,1 assertion failure,1 error. Quando arquivo vinha primeiro, :53 esperava ApiException409/mensagem de demanda arquivada; recebeu ObjectOptimisticLockingFailureException por versão conflitante do quadro já carregado. Quando restauração vinha primeiro, :53 esperava sucesso; recebeu a mesma exceção. Isso comprova uma regressão observável que os94 testes existentes deixam passar. Não alego gravação indevida observada: os outcomes efetivamente observados foram essas exceções.

Logs M1–M7, M7-witness-control.log, M7-witness-mutant.log, sensor-java-control-before/after.log, sensor-api-control-before/after.log, sensor-results.json e script sensor.py permanecem no diretório de logs. O probe é evidência auxiliar, não foi incorporado à aplicação nem à suite real.

**Isolamento comprovado depois de todos os processos encerrados e descarte das fontes scratch:** HEAD igual, git status --porcelain byte a byte igual e4147 hashes SHA256 de todos os arquivos de sistema/frontend iguais antes/depois. Também eram iguais antes/depois dos gates. Manifestos real-hashes-before.json, real-hashes-sensor-before.json, real-hashes-sensor-after.json e head/porcelain-sensor-before/after.txt preservados. Sem processos java/node próprios residuais. Removidas somente pastas temporárias próprias sistema/frontend/vite-build e a segunda cópia curta; gerados reais target/node_modules/test-results preexistentes intactos.

## Code Quality

| Princípio | Resultado |
| --- | --- |
| Escopo/tecnologias/modelo atuais, sem migração/upgrades/recursos extras | PASS |
| Mudanças cirúrgicas, padrões de sessão/guard/transação/API/React existentes | PASS; App.css contém apenas linha em branco residual sem efeito funcional |
| Payload/conjunction: assertions sobre valores e estado, não somente call-count | PASS |
| Testes não enfraquecidos/excluídos/ignorados; adequações antigas autorizadas | PASS |
| Outcome ancorado na spec e mensagens/status exatos | PASS em27 critérios; COM-14 parcial |
| Cobertura de serviço/rotas/cliente/React/navegador | GAP de discriminação no ramo de concorrência da rota nova |
| Diretrizes documentadas | docs/OPERACAO.md:13/:29/:74: H2/fictício/isolamento; manifests/vitest.config/playwright.config e tasks.md matrix; nenhuma AGENTS.md encontrada; defaults fortes TLC aplicados |

## Ranked Gap / Fix Plan

1. **Major: COM-14, concorrência do POST novo sem teste discriminante.** As disputas existentes de `ComentarioEnvioTests.java:105` e `DemandaArquivoComentarioTests.java:44` chamam apenas adicionar. AdicionarNoQuadro tem ordenação de leitura/lock própria (`ComentarioService.java:79`–:87), não exercitada pelas mesmas disputas. M7 sobrevive, mas o probe prova409/sucesso quebrados sob concorrência.
   - Fix task: ampliar cobertura de arquivo/restauração de quadro E demanda nas duas ordens para executar também adicionarNoQuadro, preservando todos os outcomes/assertions antigos. Exigir espera200ms, código/mensagem de409 onde aplicável, sucesso após restauração e exatidão dos efeitos permitidos nas tabelas/versão.
   - Gate: suites H2 completas + seleção MySQL fictícia + Vitest/lint/build/E2E completos e repetir sensor M7 com controle íntegro e isolamento. Resultado esperado: o mesmo mutante killed, nenhum caso antigo removido/relaxado.
   - Não há correção de aplicação indicada pela prova íntegra; começar pela cobertura. Root recebe a tarefa; eu não alterei testes/aplicação.

## Interactive UAT

**Pendente.** Nenhum humano executou/aprovou o roteiro de docs/OPERACAO.md. E2E e inspeção de capturas são validação automatizada/do agente. Não registrar aceitação humana nem concluir diagnóstico como Verified enquanto este FAIL não for resolvido.

## Completion Gate e lições

validate_state.py executado: exit1, veredicto FAIL e orientação para encaminhar o gap às fix tasks, feature não concluída. Lições candidatas L-006 (surviving_mutant/M7) e L-007 (ac_gap/COM-14) registradas por lessons.py; ambas exigem testar cada caminho de gravação JPA contra arquivo/restauração concorrentes para detectar entidades carregadas antes do lock. As tentativas ambientais negativas não sustentam uma regra causal reutilizável sobre a aplicação; nenhuma regra causal foi inventada. Máximo três revisões corretivas antes de escalonar; esta é revisão inicial.

