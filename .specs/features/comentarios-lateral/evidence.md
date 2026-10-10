# Comentários por demanda — evidências de execução

**Estado:** T1 e T2 concluídas como tarefas com gates aprovados. Feature parcial, sem declaração de critério Verified antes do Verifier. Base de planejamento 3a5934c.

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
| COM-01 | ComentarioConsultaTests.java:67, `status().isUnauthorized()`/mensagem exata e snapshots iguais; :72–74 `status().isOk()` | 401 sem sessão, leitura comum/criador/admin inclusive sem criador | Sim |
| COM-06 | ComentarioConsultaTests.java:58, `status().isNotFound()` e erro quadro/demanda exato; :62, GET antigo404 | Alvos inexistentes/externos sem dados ou efeitos | Sim |
| COM-11/15 | ComentarioConsultaTests.java:28–35, flags/IDs e texto antigo10001 literal; :41–46 IDs/textos exatos por data e ID nas duas rotas; :52 envelope vazio | Arquivo consultável; ordem, isolamento, desempate e vazio | Sim |
| COM-16 | ComentarioConsultaTests.java:32–34, `doesNotExist()` para senha/token/processo e chaves `containsExactlyInAnyOrderElementsOf(...)` | Somente os cinco campos enumerados de comentário e cinco do envelope | Sim |
| Sem efeitos de leitura | ComentarioConsultaTests.java:35, `conteudoPersistido().isEqualTo(antes)` e quadros físicos iguais; :47/:52/:58/:62/:67 | Nenhuma gravação ou incremento de versão | Sim |

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
