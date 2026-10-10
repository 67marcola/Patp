# Comentários por demanda — evidências de execução

**Estado:** T1–T8 concluídas como tarefas com gates aprovados. Feature parcial até Verifier independente. As seções anteriores preservam os checkpoints históricos, inclusive autorizações resolvidas. Base de planejamento 3a5934c.

## Autorizações

Em 10/10/2026 o usuário respondeu explicitamente Sim, seguir com a proposta (recomendado) e Sim, adequar esses testes (recomendado). Isso confirma spec/defaults/sete tarefas, ferramentas atuais/TLC e somente as adequações antigas listadas em tasks.md. Não autoriza alteração de outra expectativa errada sem nova confirmação TLC.

## T1 — envio com autoria da sessão

Arquivos de aplicação: ComentarioService e ComentarioController; teste novo ComentarioEnvioTests e adequações autorizadas em ComentarioArchiveTests/DemandaArquivoComentarioTests. Número e campos da demanda não são alterados; texto novo é literal, obrigatório e limitado a10000 unidades UTF-16. Nome novo vem de SessaoService.

### Gates realizados

- Primeira invocação Maven foi feita na raiz, sem pom.xml: não executou testes. Invocação corrigida para sistema/; o erro de diretório não é tratado como falha do sistema.
- Seleção `mvn.cmd -B verify -Dtest=ComentarioEnvioTests,ComentarioArchiveTests,DemandaArquivoComentarioTests` em sistema/: 44 casos, 1 falha, 1 erro, 0 skips; 42 casos passaram. ComentarioEnvioTests tem35 casos; as duas classes antigas conservam seus9 casos. Log: `C:/Users/Marco/AppData/Local/Temp/comentarios-T1-selected.log`.
- Dois resultados negativos iniciais estavam nos novos casos de restauração de quadro: a fixture `q.setArquivado(true); quadros.saveAndFlush(q)` já incrementa a versão, mas o teste passava0 e esperava1. Usuário respondeu explicitamente Sim, corrigir a preparação do teste (recomendado). Aplicada somente a correção autorizada: ler versão após a fixture, usá-la na ação e exigir inicial+1. Quatro cenários e demais assertions preservados. Log negativo conservado em TEMP/comentarios-T1-selected-before-fixture-fix.log.
- Repetição da seleção:44 casos,0 falhas/erros/skips. Full `mvn.cmd -B verify`:782 casos,0 falhas/erros/skips, BUILD SUCCESS. Baseline747+35 novos. Log `C:/Users/Marco/AppData/Local/Temp/comentarios-T1-h2.log`.

### Mapeamento localizado e adequação de T1

| Critério | Evidência/assertion | Resultado definido pela spec | Estado |
| --- | --- | --- | --- |
| COM-01 | ComentarioEnvioTests.java:89, `status().isUnauthorized()` e mensagem exata, `conteudoPersistido().isEqualTo(antes)` |401, erro correto, sem gravação | Casos selecionados passaram |
| COM-02/03 | ComentarioEnvioTests.java:43/46, `funcionario=autor.getNome()` na resposta/banco; :95 autores Ana/Bia | Autor da sessão, comum/criador/admin e sem criador permitidos | Casos selecionados passaram |
| COM-04 | ComentarioEnvioTests.java:55–57,400/mensagem/sem efeitos; :69/70 limite literal | Nulo/vazio/whitespace/10001 recusados,10000 aceitos | Casos selecionados passaram |
| COM-07/08 | ComentarioEnvioTests.java:82–84,409/mensagem e snapshots iguais | Arquivos recusam para comum/admin | Casos selecionados passaram |
| COM-09 | ComentarioEnvioTests.java:75/76,200 e processos físicos iguais | Encerrado/desconhecido sem normalização | Casos selecionados passaram |
| COM-10 | ComentarioEnvioTests.java:43–49,ID positivo/texto/autor/vínculo/data entre limites/count2 | Uma mensagem nova, valores definidos | Casos selecionados passaram |
| COM-11/12 | ComentarioEnvioTests.java:49/50,registro antigo, processos/quadros/etapas/históricos iguais | Preservação integral fora da mensagem nova | Casos selecionados passaram |
| COM-13 | ComentarioEnvioTests.java:100/101, falha CHECK real e snapshots iguais | Rollback sem efeitos | Caso selecionado passou |
| COM-14 | DemandaArquivoComentarioTests.java:49–61, timeout409/preservação/autoria Dono; ComentarioEnvioTests.java:111–117 para quadro | Lock e decisão pelo estado confirmado nas duas ordens | Todos passaram após correção autorizada da fixture |

Reverse mapping: cada caso novo tem os IDs no comentário de declaração; linhas43–50→COM-02/03/10/11/12,55–70→COM-04,75/76→COM-09/12,82–84→COM-07/08,89→COM-01,94/95→COM-02/03/10,100/101→COM-13,111–117→COM-07/10/12/14. São assertions de estado/payload, não só contagens de mocks. Nenhum caso excluído ou ignorado.

**Adequação T1:** aprovada. Critérios aplicáveis localizados, outcomes da spec correspondem às assertions, estado/payload físicos conferidos, todos os35 casos novos necessários e bidirecionalmente mapeados. Guidelines OPERACAO/ApiIntegrationSupport seguidos; nenhum skip/exclusão/SPEC_DEVIATION. Runner passou. Isso fecha a tarefa, não substitui o Verifier final.

## T2 — consulta isolada da conversa

Full `mvn.cmd -B verify` em sistema:797 casos,0 falhas/erros/skips, BUILD SUCCESS; baseline782+15 novos. Log TEMP/comentarios-T2-h2.log. A primeira compilação exigiu tornar explícito o tipo das chaves nas assertions de Map<?,?>. A primeira execução797 teve uma falha mecânica de JsonPath curinga: o corpo registrado continha os quatro textos corretos na ordem exigida. Substituída a leitura por quatro índices explícitos com os mesmos valores esperados; nenhum outcome/cenário foi alterado. Log negativo TEMP/comentarios-T2-before-jsonpath-fix.log. Corrigida somente a contagem textual da spec/tasks: a lista enumerada sempre teve cinco campos por comentário, embora o texto dissesse seis. Shape enumerado preservado.

| Critério | Evidência/assertion | Outcome da spec | Coberto |
| --- | --- | --- | --- |
| COM-01 | ComentarioConsultaTests.java:66, `status().isUnauthorized()`/mensagem exata e snapshots iguais; :71 `status().isOk()` | 401 sem sessão, leitura comum/criador/admin inclusive sem criador | Sim |
| COM-06 | ComentarioConsultaTests.java:57, `status().isNotFound()` e erro quadro/demanda exato; :61, GET antigo404 | Alvos inexistentes/externos sem dados ou efeitos | Sim |
| COM-11/15 | ComentarioConsultaTests.java:28–35, flags/IDs e texto antigo10001 literal; :41–46 IDs/textos exatos por data e ID nas duas rotas; :52 envelope vazio | Arquivo consultável; ordem, isolamento, desempate e vazio | Sim |
| COM-16 | ComentarioConsultaTests.java:31–34, `doesNotExist()` para senha/token/processo e chaves `containsExactlyInAnyOrderElementsOf(...)` | Somente os cinco campos enumerados de comentário e cinco do envelope | Sim |
| Sem efeitos de leitura | ComentarioConsultaTests.java:35, `conteudoPersistido().isEqualTo(antes)` e quadros físicos iguais; :46/:51/:57/:61/:66 | Nenhuma gravação ou incremento de versão | Sim |

Reverse mapping: cada caso parametrizado e teste de ComentarioConsultaTests corresponde às linhas de evidência acima: flags4→COM-11/15/16, ordem1→COM-11/15/16, vazio1→COM-15, alvos3/antigo4041→COM-06, sessões2→COM-01, papéis3→COM-01/15. Todos15 necessários. Adequação A–D PASS: payloads/estado efetivo, resultados da spec, testes co-localizados/padrão ApiIntegrationSupport; zero remoção/skip/SPEC_DEVIATION. Tarefa concluída, feature parcial até T3–T7 e Verifier.

## T3 — envio moderno com corpo estrito

Java/H2 Full830 casos PASS,0 falhas/erros/skips (797+33 novos). Log TEMP/comentarios-T3-h2.log. Frontend Build em execução; primeira suíte881 teve somente timeout5000ms no CRUD antigo por teclado durante execução simultânea com Maven; teste intacto, runner repetido sozinho, log negativo TEMP/comentarios-T3-vitest-timeout.log.

| Critério | Evidência/assertion | Outcome da spec | Coberto |
| --- | --- | --- | --- |
| COM-02/10/16 | ComentarioDemandaApiTests.java:41–45, `status().isCreated()`, texto/nome/processoId exatos, chaves exatamente cinco, ID positivo e horas entre limites; query física contém os valores | HTTP201 e cinco campos definidos iguais à gravação da sessão | Sim |
| COM-04 | ComentarioDemandaApiTests.java:30/31/55/59,400/mensagem/snapshot; :63,10000 UTF16 literal no banco/resposta | Limites só novos; ausente/nulo/blank/10001 não grava | Sim |
| COM-05 | ComentarioDemandaApiTests.java:30/31/51,400 `Dados da requisição inválidos.` e snapshots físicos iguais | Dez formatos/tipos/campos extras recusados | Sim |
| COM-01/06/07/08 | ComentarioDemandaApiTests.java:30/31/68/69, status e erro exatos401/404/409, snapshots iguais | Sessão, vínculo e arquivos obrigatórios sem efeitos | Sim |
| COM-09/12 | ComentarioDemandaApiTests.java:74,201 e `processos` iguais | Comentar nos quatro estados sem normalização | Sim |
| COM-11/12 | ComentarioDemandaApiTests.java:46/47, count2, antigos e etapas/processos/históricos/quadros iguais | Apenas uma mensagem nova, preservação integral | Sim |
| COM-13 | ComentarioDemandaApiTests.java:78, CHECK real gera exceção e snapshots iguais | Rollback novo sem efeitos | Sim |
| COM-14 compartilhado | DemandaArquivoComentarioTests.java:49–61; ComentarioEnvioTests.java:111–117 timeout/409/snapshots/autoria; mesmo guard/lock e função gravar usada nas duas rotas | Decisão serializada por arquivo/restauração confirmada | Sim, testes físicos da função compartilhada em T1 preservados |

Reverse: sucesso4(:41–47)→COM-02/10/11/12/16; corpos10(:30/31/51)→COM-05; limites5+ausente1+exato1(:55/59/63)→COM-04; falhas7(:30/31/68/69)→COM-01/06/07/08; estados4(:74)→COM-09/12; rollback1(:78)→COM-13. Todos33 têm outcomes/campos reais e são necessários. Adequação A–D PASS: profundidade física/DTO, preservação, erros precisos, co-localização ApiIntegrationSupport. Sem exclusão/skip/SPEC_DEVIATION.

Gate T3 final PASS:830 Java/H2,881 Vitest,lint/build TEMP. Repetição solo da suíte antiga passou integralmente sem alterar teste ou timeout. Logs TEMP/comentarios-T3-vitest.log, comentarios-T3-lint.log, comentarios-T3-build.log; artefato TEMP/comentarios-T3-build. T3 concluída como tarefa; feature ainda parcial.

## T4 — confirmação do cliente

Quick76 Vitest e lint PASS; logs TEMP/comentarios-T4-vitest.log e comentarios-T4-lint.log. Sem retry, GET/POST com sessão e valores completos; datas/frações até9 dígitos validadas sem perda de ordem. Nenhum teste antigo alterado.

| Critério | Evidência/assertion | Outcome definido | Coberto |
| --- | --- | --- | --- |
| COM-18/15 | apiComentarios.test.js:10/11 `toEqual(dados)` e requisição completa com signal/token/rota | GET200 completo da demanda correta | Sim |
| COM-11/20 | apiComentarios.test.js:15 `toEqual(c)` nas quatro flags com nome antigo vazio/texto10001 | Consulta antiga literal, flags reais | Sim |
| Ordem/vazio | apiComentarios.test.js:18/19 `toEqual(c)` vazio, empateID/frações9 | Nenhuma perda ou reordenação indevida | Sim |
| Falhas GET | apiComentarios.test.js:39 `rejects.toMatchObject({message:erro,status:200})` | Cada campo/flag/ID/data/ordem inválida não aplicado | Sim |
| COM-23 | apiComentarios.test.js:42/43 `toEqual(r)`, JSON somente texto, POST/token/caminho | Cinco campos confirmados, autoria do servidor | Sim |
| COM-24 | apiComentarios.test.js:53/56 `rejects.toMatchObject` e chamada única | Divergência, extra, ausência, autor/data/ID inválidos/repetidos sem falso sucesso | Sim |
| COM-18/24 | apiComentarios.test.js:59–63 status exato, erro servidor ou comunicação e uma chamada | HTTP inesperado, rede e JSON sem retry | Sim |

Reverse mapping: linhas10/11→COM-18;15→COM-11/20;18/19→COM-15;39→COM-18/bordas GET;42/43→COM-23;53/56→COM-24/bordas POST;59–63→COM-18/24. Todos76 casos necessários; assertions payload/erro específicas e contratos da spec. Adequação A–D PASS; guidelines/apiArquivo seguidos;0 skip/exclusão/SPEC_DEVIATION. T4 concluída como tarefa, feature parcial.
## T5 — painel de comentários

Quick30 Vitest/lint PASS. Logs TEMP/comentarios-T5-vitest.log e comentarios-T5-lint.log. A primeira execução teve20 falhas de harness por matchers jest-dom ausentes neste projeto; adequação mecânica para propriedades reais com mesmos valores esperados e função de visibilidade (ancestros/display/visibility/hidden/conexão), sem mudar outcomes. Log TEMP/comentarios-T5-before-matchers.log. A primeira repetição usou caminho relativo duplicado frontend/frontend e não editou arquivos; corrigido workdir. Regex de whitespace replica String.isBlank e NBSP válido; supressão local explicada de no-control-regex, sem mudar lint global.

| Critério | Evidência/assertion | Outcome da spec | Coberto |
| --- | --- | --- | --- |
| COM-18 | ComentariosDemanda.test.jsx:20–22, número/cliente/loading, foco, vazio e campo vazio; :31 GET/GET e formulário só após leitura confirmada | Identificação/estados e recuperação de consulta | Sim |
| COM-19 | ComentariosDemanda.test.jsx:27 `textContent` literal/autor, horário formatado, time ISO, img nulo e ID21 | Texto escapado/multiline, autor e hora local | Sim |
| COM-20 | ComentariosDemanda.test.jsx:35 textbox/botão nulos e motivo visível nas3 flags; :63 rascunho literal preservado após arquivo | Somente consulta, flags da API prevalecem | Sim |
| COM-21 | ComentariosDemanda.test.jsx:38 erro exato/campo igual/chamada1; :41 sucesso literal10000UTF16/NBSP | Inválidos não enviam, limite válido aceita | Sim |
| COM-22/23 | ComentariosDemanda.test.jsx:45–47 disabled verdadeiro, POST único, Escape não fecha, depois2 itens/ID/texto/autor/time/campo vazio/painel aberto | Pending global via callback, confirmação única | Sim |
| COM-24/25 | ComentariosDemanda.test.jsx:54/55 lista1→2, rascunho Nova, disabled true→false, aviso visível e métodos GET/POST/GET; :59 GET falho mantém lista/rascunho/bloqueio; :63 flags atualizadas | Falhas HTTP/rede/JSON/status/contrato conservam estado; manual sem reenvio | Sim |
| COM-26 | ComentariosDemanda.test.jsx:66 `fechar` uma vez por botão/Escape | Fechar ocioso, foco de retorno na integração T6 | Sim |
| COM-27 | ComentariosDemanda.test.jsx:72/73 rascunho vazio e nenhum dado/erro atrasado; :77 desmontado e callback true/false | Troca demanda/quadro/desmontagem não vaza estado | Sim |
| COM-28 painel | ComentariosDemanda.test.jsx:21/45/66 foco/teclado/Escape e botões sem exclusão | Base acessível; viewport será exercitada em T7 | Parcial da feature, tarefa cumprida |

Reverse mapping: loading/vazio1→COM-18/26; literal1→COM-19; leitura falha3→COM-18; arquivos3→COM-20; texto5+limite2→COM-21/23; pending1→COM-22/23; falhas envio5+manual falho1+arquivo manual2→COM-24/25/20; fechar2→COM-26; troca2+desmontar2→COM-27. Todos30 necessários. Adequação A–D PASS; payload/estado/falhas reais, padrão de propriedades usado pelo projeto, co-localizados;0 skips/exclusão/SPEC_DEVIATION. T5 concluída, feature parcial até integração/viewport/Verifier.

## T6 — integração implementada, gate ainda parcial

13 casos novos QuadroComentarios aprovados dentro do Full1000 Vitest (881+76+30+13),0 falhas/skips; lint/build TEMP PASS. Logs TEMP/comentarios-T6-vitest.log, comentarios-T6-lint.log, comentarios-T6-build.log; artefato comentarios-T6-build. Seleção inicial13 teve1 falha por rota /gerenciamentos/9 não preparada na fixture de App; adicionada a resposta fictícia do quadro, sem mudar nenhuma assertion/outcome. As seis expressões JSX antigas foram adequadas exatamente conforme autorização de10/10, todos os cenários mantidos.

MySQL8.0.43 fictício:326 selecionados PASS,0 falhas/erros/skips. URL/driver efetivos confirmados no log TEMP/comentarios-T6-mysql.log:127.0.0.1:33823/schema creral_arquivo_test, datadir TEMP/creral-arquivo-049cfd5cc86d4d8b840e41f1710d7a43/mysql. Reaberta somente essa fixture própria após conferir porta livre e caminho dentroTEMP; listener atual7408/commandline confirmado, launcher24380 não usado para encerramento. Antes de testar, @@port/@@datadir/database() confirmados. Primeira invocação falhou por ampersands na passagem por mvn.cmd; URL movida para SPRING_DATASOURCE_URL no processo filho, sem alterar configuração do projeto. Log negativo comentarios-T6-mysql-before-launch-fix.log. Seleção Comentario*Tests,DemandaArquivo*Tests,ConfiguracaoArquivoResponseTests,HistoricoArchiveTests,GerenciamentoApiTests. Nenhum banco real utilizado.

E2E completo aguarda confirmação de quatro expectativas antigas de ausência total de botão (arquivo124/130, transicoes117, demandas173), antes de adequá-las para lista exata Comentários. TLC não permite modificar expectation genuinamente errada sem confirmação específica; nenhuma das quatro alterada. Quatro arquivos de navegador independentes em execução. T6 NÃO concluída/commitada, T7 não iniciada; manter gate estrito.

| Critério | Evidência/assertion | Outcome da spec | Situação |
| --- | --- | --- | --- |
| COM-17/20 | QuadroComentarios.test.jsx:20/21 lista exata Comentários, URL correta e campo somente quando ativo | Todos estados/arquivos e leitor com consulta | Passou |
| COM-26 | QuadroComentarios.test.jsx:26 foco origem/novo campo vazio; :29 foco Voltar após origem removida | Fechar/Escape descarte e fallback | Passou |
| COM-22/27 | QuadroComentarios.test.jsx:33/34 mutações disabled, ausência formulário, trocaP8/campo vazio e trêsURLs exatas | Troca permitida ociosa, sem rascunho cruzado | Passou |
| COM-22 | QuadroComentarios.test.jsx:38/39 todosbotões/campo disabled, POST único, volta nãochamada, callbacks true/false, confirmado campo vazio | Bloqueio global durante envio e liberação | Passou |
| COM-27 | QuadroComentarios.test.jsx:43 painel antigo/alert ausentes depois da resposta atrasada | Troca de quadro sem conversa anterior | Passou |
| COM-22 Sair | QuadroComentarios.test.jsx:53/54 disabled true→false e token preservado | App impede saída durantePOST | Passou |

Reverse mapping: consulta6→COM-17/20; fechar2+fallback1→COM-26; troca1→COM-22/27; pending1+App1→COM-22; quadro1→COM-27. Todos13 necessários, payload/estado/foco reais; adequação do conjunto Vitest A–D PASS. Gate global ainda parcial por E2E, sem done/Verified/commit.

E2E independentes T6:5/5 PASS,0 falhas/skips/retries em quatro arquivos (autocadastro1,edição2,etapas1,gerenciamentos1). Log TEMP/comentarios-T6-e2e-independentes.log. Restam oito cenários dos três arquivos cuja expectativa exige autorização específica; nenhum cenário apagado/ignorado. Fixture MySQL encerrada por mysqladmin em127.0.0.1:33823 após confirmar listener7408/name/commandline/datadir; shutdown0, listener ausente. Nenhum PID antigo ou banco real encerrado. T6 segue parcial, próxima ação depende da resposta à pergunta já enviada.

### Autorização adicional de T6

Usuário respondeu Sim, adequar essas quatro expectativas (recomendado) à pergunta que nomeou arquivo124/130, transicoes117 e demandas173. Aplicadas exatamente as quatro adequações: `getByRole("button").toHaveText(["Comentários"])` exige lista exata de um botão e continua negando todas as mutações. Nenhum outro cenário/assertion alterado. Gate E2E completo13 em execução, sem filtros/exclusões.

### Fechamento de T6

Gate Build PASS:830 Java/H2 vigente de T3 (servidor inalterado),1000 Vitest,326 MySQL fictício,13/13 Edge/H2 em todos os sete arquivos,lint reconferido e build TEMP vigente de T6. Log completo TEMP/comentarios-T6-e2e.log. Zero falhas/erros/skips/retries. Adequação T6 A–D PASS com mapeamento acima e quatro expectativas antigas autorizadas; contagem anterior13 E2E preservada. T6 concluída como tarefa e incluída no diagnóstico; feature parcial até T7/Verifier. Fixture MySQL inativa após shutdown conferido.

## T7 — precisão do horário persistido

Dois E2E novos, intactos, revelaram divergência real entre POST e consulta: frações .0132307/.1210835 retornadas versus .013231/.121084 persistidas. Log negativo TEMP/comentarios-T7-selected.log. As colunas existentes timestamp(6)/datetime(6) conservam microssegundos; a criação agora trunca a esse limite antes de salvar, sem alterar mensagens antigas ou esquema. Correção necessária dos outcomes COM-10/11/23, sem mudar expectations.

Full Java/H2 PASS:832 casos (830+2),0 falhas/erros/skips; BUILD SUCCESS. Log TEMP/comentarios-T7-precisao-h2.log.

| Critério | Assertion e outcome | Resultado |
| --- | --- | --- |
| COM-10 | ComentarioPrecisaoTests.java:28 exige HTTP201/200 e texto/autor; :31 exige hora controlada123456000ns e query física exatamente igual, originada de relógio123456789ns | PASS nas duas rotas |
| COM-11/23 | ComentarioPrecisaoTests.java:32 exige GET posterior exatamente igual ao horário confirmado; :33 exige uma única mensagem | PASS nas duas rotas |

Reverse mapping: os dois casos parametrizados :20–33 cobrem COM-10/11/23 com valores literais, banco de teste e resposta posterior. Adequação A–D PASS: expectativas derivadas da preservação/contrato, ambos necessários para rotas distintas, padrão ApiIntegrationSupport, sem remoção/skip/SPEC_DEVIATION. T7 concluída como tarefa; feature parcial até T8 e Verifier. Incluído no diagnóstico em10/10/2026.

## T8 — prova integrada em execução

Após T7, os dois E2E intactos avançaram e revelaram que os estilos exclusivos estavam no App.css, arquivo não importado pela aplicação. Movidos para index.css já carregado, sem trazer os estilos de exemplo do Vite. COM-19/28: quebras pre-wrap, palavras longas e painel dentro da viewport, sem overflow horizontal. Log negativo TEMP/comentarios-T8-before-styles-fix.log; repetição selecionada2/2 PASS,0 skips/retries em TEMP/comentarios-T8-selected.log. Uma invocação npm na raiz sem package.json não executou testes, corrigida para frontend.

QA visual das quatro capturas da execução selecionada realizada por inspeção direta: TEMP/creral-comentarios-qa-87uOpk/desktop-conversa.png, mobile-conversa.png, mobile-arquivo.png e TEMP/creral-comentarios-falha-qa-2PovS5/mobile-recuperacao.png. Autores/horários/textos literais legíveis, HTML escapado, quebras e palavra longa preservadas, nenhum transbordamento horizontal, fechamento visível e conversa/rascunho em consulta de arquivo. Painel usa rolagem vertical; capturas fullPage mostram também o quadro subjacente. Desktop1280x800 e celular390x844 com limites medidos/assertions.

MySQL8.0.43 fictício:328 PASS,0 falhas/erros/skips em TEMP/comentarios-T8-mysql.log. Antes do runner, @@port33823, @@datadirTEMP/creral-arquivo-049cfd5cc86d4d8b840e41f1710d7a43/mysql e schema creral_arquivo_test confirmados. Listener22852, launcher23944; fixture própria, banco real intocado. Mesma seleção de T6 +2 precisão.

Primeira suíte Vitest1000 executada simultaneamente ao Maven:998 PASS, timeout5000ms em CRUD de etapas e foco do caso seguinte divergente. Testes/configuração intactos; execução solo será exigida antes de fechar o gate. Log negativo TEMP/comentarios-T8-vitest-concurrent.log.

### Adequação dos dois fluxos novos de T8

| Critério | Assertion (frontend/e2e/comentarios.spec.js) | Outcome definido |
| --- | --- | --- |
| COM-02/03/10/16 | :26 autoria Dono mesmo com nome falso; :57 shape exato5, texto/autor/vínculo/ID; :58 hora limitada | Conta conectada, HTTP201 e valores salvos |
| COM-11/12/15 | :71/:74 comparação integral snapshot/eventos e conversa exclusiva; :80/:82/:84 arquivo/restauração e mensagens iguais | Preservação de dados/ciclo/versão/histórico fora das ações próprias |
| COM-17/19/23 | :49/50 texto literal, autor/dataHora exata e formatada; :59 campo vazio; :71 img ausente; :72 conversa outra vazia; :75 recarga | Confirmação única, isolamento e persistência visual |
| COM-20 | :79/:83/:104 form/envio ausentes, motivo de arquivo e texto preservado | Arquivado somente consulta |
| COM-22 | :95/:96 todos controles/campo disabled, Escape conserva painel, segundo submit não faz POST | Um envio pendente com bloqueio global |
| COM-24/25 | :98–104 texto/lista/aviso/bloqueio, GET503 conserva, GET confirmado mostra salvo uma vez sem POST automático, arquivo entre GET/POST409 | Resposta perdida preserva rascunho e exige consulta manual |
| COM-26/28 | :39 foco Fechar via Enter; :72/:77 Escape/fechar retorna origem e descarta; :64/65 medidas e pre-wrap nos dois tamanhos | Teclado/foco, painel visível sem overflow e texto literal |

Reverse mapping: fluxo :69–84→COM-02/03/10/11/12/15/16/17/19/20/23/26/28; fluxo :86–105→COM-08/11/12/20/22/24/25/28. O título do segundo cita COM-07 como contexto compartilhado de arquivo; sua prova direta de quadro arquivado está no primeiro fluxo e no servidor. Dois fluxos necessários e distintos, payloads/estado/foco e persistência reais, guidelines isolados existentes seguidos,0 remoção/skip/SPEC_DEVIATION. Testes na camada e2e. Resultado de runner selecionado2 PASS; Build final ainda em execução. Roteiro humano documentado em docs/OPERACAO.md, aceite humano pendente.

### Fechamento de T8

Build final PASS:832 Java/H2 de T7 (servidor inalterado depois),1000 Vitest em execução solo,328 MySQL fictício,15 Edge/H2 em todos os oito arquivos,lint e build TEMP aprovados. Zero falhas/erros/skips/retries no gate final. Logs TEMP/comentarios-T7-precisao-h2.log e comentarios-T8-vitest.log, -mysql.log, -e2e.log, -lint.log, -build.log. Build TEMP/comentarios-T8-build. Baselines747/881/13 preservadas, acréscimos85/119/2. A repetição solo do Vitest passou sem mudança de teste/configuração. Adequação T8 A–D PASS; as duas falhas reais detectadas no navegador foram corrigidas no código, com testes intactos. Acertos de codificação dos novos textos da documentação não alteram critérios ou assertions. T8 concluída como tarefa; revisão independente obrigatória após commit, antes de declarar feature concluída. Incluído no diagnóstico em10/10/2026.
