# Validação independente: CRUD de etapas, rodada 2

**Result**: PASS

Os 30 critérios têm evidência de outcome e os gates completos passaram em uma scratch nova. T7 fechou ETA-27: o M10 que sobreviveu na rodada 1 agora foi morto pelas novas assertions, assim como os equivalentes PUT e DELETE.

**Data:** 2026-10-05. **Verificador:** `/root/verify_etapas`, independente dos autores. Revisão auxiliar somente leitura por `/root/verify_etapas/round2_ui`. **Fonte única de ACs:** `.specs/features/etapas/spec.md`, relida integralmente; tasks e os novos testes foram lidos independentemente. **Branch/head:** `testes`, `d067682e8d22b159c7871c38c082ab34bf6b0634`. **Diff da feature:** ` 4dd4abe..d067682`, 39 arquivos. **Correção:** T7 no commit `d067682`. O relatório FAIL da rodada 1 permanece no histórico ` 367959e:.specs/features/etapas/validation.md`.

30/30 ACs verificados, zero gaps de precisão ou cobertura encontrados nesta rodada. Sensor atual: 5 faults compiláveis, 5 killed, 0 survived. Os nove kills da rodada 1 são históricos, não foram todos repetidos. UAT humano permanece pendente; este PASS é técnico.

## Gates da rodada 2

Todos os comandos foram executados em `C:/Users/Marco/AppData/Local/Temp/etapas-verifier-r2-0c2443badb934fbebed0e3905b2050e1/scratch`, worktree nova no head verificado. JAVA_HOME=`C:/Program Files/Java/jdk-25.0.2`. `npm.cmd ci` instalou 98 pacotes, exit 0. Maven Java e helper E2E nunca rodaram simultaneamente nessa scratch.

| Gate obrigatório | Resultado independente atual | Exit |
| --- | --- | --- |
| `mvn.cmd -B verify`, sistema | 238 testes, 0 falhas, 0 erros, 0 skips; BUILD SUCCESS, 16, 742s | 0 |
| `npm.cmd test`, frontend | 149 testes em 7 arquivos, 0 falhas/skips; 28, 31s | 0 |
| `npm.cmd run build` | Vite, 23 módulos; bundle gerado | 0 |
| `npm.cmd run lint` | oxlint em src/e2e/scripts/configs | 0 |
| `npm.cmd run test:e2e` | etapas.spec.js 1 PASS e gerenciamentos.spec.js 1 PASS; 2 totais, 0 falhas/skips/retries; primeira execução desta rodada | 0 |
| `git diff --check 4dd4abe..d067682` | sem whitespace errors | 0 |

O wrapper abriu uma invocação Playwright por arquivo, com `reuseExistingServer:false` e `retries:0`; cada helper iniciou H2 em memória novo, encerrando antes do arquivo seguinte. A aplicação normal/MySQL configurado não foi iniciado nem consultado. `sistema/src/test/java/com/patp/sistema/SistemaApplicationTests.java:22` continua assertando `assertTrue(connection.getMetaData().getURL().startsWith("jdbc:h2:mem:"))` no gate de 238.

Baseline antes da feature: 97 Java/71 Vitest/1 E2E. Final: 238 Java (+141), 149 Vitest (+78), 2 E2E (+1). Os seis casos EtapaArchiveTests permanecem. T7 acrescentou 12 casos, com os 137 Vitest anteriores intactos. Não houve remoção, skip ou enfraquecimento de assertion. O teste anterior com permissão/posição substituída foi adaptado somente ao contrato autorizado de criador, posições válidas, versão e snapshot, conservando integridade e arquivo.

Logs físicos atuais em `C:/Users/Marco/AppData/Local/Temp/etapas-verifier-r2-0c2443badb934fbebed0e3905b2050e1/`: `java-gate.log`, `test-gate.log`, `build-gate.log`, `lint-gate.log`, `e2e-gate.log`, `npm-ci.log`, `sensor-results.json`, `hash-identity.json` e logs de cada fault.

**MySQL histórico, conferência documental:** 237 testes passaram em c2e3cce, 0 falhas/erros/skips, conforme `.specs/features/etapas/mysql-validation.md:3` e o log físico `C:/Users/Marco/AppData/Local/Temp/creral-mysql-crud-be180f80b1f64ead87cc0113c6bd68c1/etapas-maven-mysql.log:9636/:9639`. As linhas :70/:71/:73 confirmam localhost:33817, Connector/J e MySQL 8.0.43; o resumo JSON confirma as dez classes. Os fontes/testes backend permanecem idênticos a c2e3cce; nenhum MySQL foi rerodado. O caso específico H2 permanece executado, não foi desabilitado para obter 237 testes MySQL.

## Evidência de estado e vocabulário da matriz

Paths nesta seção são relativos ao repositório. `API` = `sistema/src/test/java/com/patp/sistema/EtapaApiTests.java`; `RACE` = `sistema/src/test/java/com/patp/sistema/EtapaDemandConcurrencyTests.java`; `ARCH` = `sistema/src/test/java/com/patp/sistema/EtapaArchiveTests.java`; `EDITOR` = `frontend/src/pages/EditorEtapa.test.jsx`; `BOARD` = `frontend/src/pages/QuadroEtapas.test.jsx`; `CLIENT` = `frontend/src/services/api.test.js`; `APP` = `frontend/src/App.test.jsx`; `BROWSER` = `frontend/e2e/etapas.spec.js`.

Não são aliases para evidência autoral: todos apontam para assertions físicas lidas. `API:435` implementa `estado()` com `conteudoPersistido()` mais `select * from gerenciamentos order by id`; `sistema/src/test/java/com/patp/sistema/ApiIntegrationSupport.java:78` captura `etapas`, `processos`, `comentarios` e `historicos` com todas as colunas em ordem de ID. Portanto `assertThat(estado()).isEqualTo(antes)` verifica também versão/metadados, vínculos, status, responsáveis, comentários e históricos, além da contagem. `API:437` exclui somente etapas dessa mesma captura quando a mutação estrutural válida deve preservar demandas/comentários/históricos.

Mensagens exatas usadas pelas assertions: PERMISSAO = `Você não tem permissão para administrar este gerenciamento.` (`API:30`); ARQUIVO = `Gerenciamento arquivado. Restaure-o antes de alterar.` (:31); VERSAO = `Gerenciamento alterado por outro usuário. Atualize e tente novamente.` (:32); ETAPA = `Etapa não encontrada neste gerenciamento.` (:33); FORMATO = `Dados da requisição inválidos.` (:34). Cada referência abaixo inclui a assertion, seu valor esperado e o contexto parametrizado.

## Matriz dos 30 critérios

| AC | Outcome exigido pela spec | Assertion física, valores e branches | Resultado |
| --- | --- | --- | --- |
| ETA-01 | GET 200, DTO atual e cada etapa com ID/nome/setor/ordem/quantidade real | `API:55` `status().isOk()`; :56–61 `jsonPath("$.gerenciamento.id").value(quadro.getId())`, nome=`Quadro`, descricao=`Descrição`, arquivado=`ator.equals("arquivado")`, versao=`ator.equals("arquivado") ? 1 : 0`, podeAdministrar=`criador ou admin`; :75–79 criador nulo no legado ou ID e nome=`Criador`. :62–72 lista 2 e etapas: primeira ID/nome=`Antiga`/setor vazio/ordem 7/quantidade 0; segunda ID/nome=`Empatada`/setor=`Engenharia`/ordem 7/quantidade 3. Fixtures :47–51 contam Em andamento/Concluido/Cancelado e excluem quadro externo. `RACE:143` `jsonPath("$.gerenciamento.versao").value(1).match(resultado)` e :144–147 lista 1/nome=`Confirmada`/setor=`Projeto`/ordem 1 após esperar commit. | PASS |
| ETA-02 | ordem depois ID, legado sem escrita nem setor inventado | `API:63/68` IDs crescente com ordem 7/7; :65 setor vazio; :85 `assertThat(estado()).isEqualTo(antes)`. `API:167–172` array e snapshot retornam primeiro ID mais novo de ordem 2, depois antigo de ordem 9, e estado igual. `BOARD:281–288` edição usa posição visual 2/1, opções `["1","2"]`, setor desconhecido=`""` e métodos=`["GET"]`. | PASS |
| ETA-03 | consulta autenticada permitida em arquivo/sem criador | `API:37–43` matriz criador/terceiro/admin/legado/arquivado, mesma consulta :55 `isOk()`, :75 criador=`null` no legado; array :81 `isOk()` e :85 estado igual. `BOARD:81–93` snapshot arquivado/terceiro/legado renderiza conteúdo consultável com flags atuais. | PASS |
| ETA-04 | criador/admin cria, edita e remove vazia em ativo | `API:89` casos criador/admin/adminLegado; :108 `status().isCreated()`, :128 e :142 `status().isOk()` para PUT/DELETE. :124 proprietário da nova etapa=`quadro.getId()`; :155 somente novoId removido; :156 conteúdo preservado. `ARCH:72/78/82` conserva ciclo anterior com 201/200/200. | PASS |
| ETA-05 | POST/PUT/DELETE terceiro 403, mensagem exata, zero escrita; legado somente admin | `API:219` `status().isForbidden()` e `jsonPath("$.erro").value(PERMISSAO)`; :220 `assertThat(estado()).isEqualTo(antes)`. :223–225 gera 3 métodos  ×  criador presente/ausente  ×  ativo/arquivo, com payload {} provando precedência de administração sobre campos/arquivo/versão. Sucesso adminLegado está em ETA-04. | PASS |
| ETA-06 | sem sessão 401 em configuração e CRUD, zero escrita | `API:253` `mvc.perform(request).andExpect(status().isUnauthorized())`; :254 estado igual. :257–258 gera GET array/GET snapshot/POST/PUT/DELETE  ×  header ausente/Bearer inexistente; mutações recebem JSON impossível para provar autenticação primeiro. | PASS |
| ETA-07 | criador/admin arquivo 409 e mensagem, zero escrita | `API:237` `status().isConflict()` e `jsonPath("$.erro").value(ARQUIVO)`; :238 estado igual; :241–242 gera 3 métodos  ×  criador/admin. `ARCH:55–60` 409/mensagem/estado igual/versão 1/GET 200 com etapa Original. | PASS |
| ETA-08 | quadro/etapa ausente 404, mensagem do recurso; ID estrangeiro não transfere | `API:268–270` `status().isNotFound()`, `jsonPath("$.erro").value("Gerenciamento não encontrado.")`, estado igual em 5 rotas. :280–281 `status().isNotFound()`, `jsonPath("$.erro").value(ETAPA)`, estado igual em PUT/DELETE  ×  ausente/outro quadro. Tentativas de ID/quadro/criador recebidos não trocam entidade: :123 `novoId` diferente dos IDs existentes, :124 quadro persistido correto; :185/:190–192 PUT conserva ID/quadro da URL e conteúdo. | PASS |
| ETA-09 | POST/PUT nome normalizado com tamanho 0 ou >255 UTF-16 retorna 400 e mensagem exata | `API:297–299` `status().isBadRequest()`, `jsonPath("$.erro").value("Informe um nome de etapa entre 1 e 255 caracteres.")` no campo nome e estado igual; :303–304 ausente/nulo/vazio/whitespace NBSP+BOM/256 letras/128 emoji, nos 2 métodos. :314–316 limites 1/255 com whitespace JS retornam 201/200, nome exato=`texto`, versão 1 e nome persistido=`texto`. `EDITOR:53–56` alerta exato, drafts iguais e zero saves; :63 payload normalizado com acentos exato. | PASS |
| ETA-10 | POST/PUT setor normalizado com tamanho 0 ou >255 UTF-16 retorna 400 e mensagem exata | Mesma matriz `API:297–299`, campo setor, `jsonPath("$.erro").value("Informe um setor entre 1 e 255 caracteres.")` e estado igual. :315 `jsonPath("$.etapas[0].setor").value(texto)` nos limites 1/255 normalizados. `EDITOR:45–56` erros exatos e draft preservado; :63 `salvar` recebe `{ nome: texto, setor: texto, ordem: 2 }`. | PASS |
| ETA-11 | posição inteiro positivo 1..N+1 no POST e 1..N no PUT; 400/formato ou limite exato, zero escrita | `API:328–329` `status().isBadRequest()`, `jsonPath("$.erro").value(erro)`, estado igual. :325 define erro de limite para -1/0/null/ausente/3; demais 1.1/string 1/true/overflow de inteiro usam FORMATO; :333 roda POST/PUT. :340–342 PUT N+1 recusa 400 com `Escolha uma posição válida para a etapa.` e estado igual. :346–354 criação 1/2/3 inclui fim e verifica ordens persistidas dos sobreviventes; :198–199 N=0 aceita 1. `EDITOR:71–72` posição fora de opções alerta exato e zero envio. | PASS |
| ETA-12 | POST 201 com ID novo e configuração persistida 1..N+1 | `API:108/114–124` 201/lista 3/IDs antigos/nome Mesmo/setor Operação/ordens 1, 2, 3/quantidades 1, 0, 0; `assertThat(novoId).isNotEqualTo(primeira.getId()).isNotEqualTo(ultima.getId())`; associação persistida correto quadro. :351–354 todas posições 1/2/3 verificam ordem de ambos anteriores. `BROWSER:109/115/120/121` IDs positivos/3 distintos; :122–126 `expect(tripla.etapas.map(campos)).toEqual([{id:planejamentoId,nome:"Planejamento",setor:"Técnico",ordem:1,quantidadeDemandas:0},{id:analiseId,nome:"Análise",setor:"Engenharia",ordem:2,quantidadeDemandas:0},{id:execucaoId,nome:"Execução",setor:"Campo",ordem:3,quantidadeDemandas:0}])`; :130 reload lista igual. | PASS |
| ETA-13 | PUT 200/configuração 1..N, renomear/setor/mover preserva vínculos/histórico | `API:128/134–139` 200/versão 2/lista ordenada `[novoId,ultimaId,primeiraId]`, ordens 1/2/3, primeira renomeada=`Renomeada`, setor=`Projeto`, quantidade 1; :156 `assertThat(somenteDemandas()).isEqualTo(conteudo)` inclui todas colunas de demanda/comment/history. :183–192 movimento inverso 200, `[ultimaId,primeiraId,segundaId]` ordens 1/2/3, nome Movida/setor Projeto/quantidade 1 e vínculo/quadro/conteúdo iguais. `BROWSER:143–158` lista completa exata em ambos sentidos; sobreviventes conservam IDs/campos. | PASS |
| ETA-14 | nomes repetidos preservam IDs e conteúdos independentes | `API:95/106/115–123` anterior e novo nome=`Mesmo`, setores Engenharia/Operação, posições 1/2 e IDs diferentes; :134–154 editar/remover por ID conserva demais conteúdos e :156 vínculos iguais. `BOARD:243–247` duas regiões Planejamento com setores Técnico/Campo, posições 1/2; :253 headings=`["Planejamento","Revisão"]`, :254 rota PUT termina `/etapas/5`. | PASS |
| ETA-15 | DELETE 200 remove só alvo, reordena 1..N-1, conserva históricos; última vazia pode remover | `API:142–156` 200/content JSON/lista 2 com IDs última/primeira, ordens 1/2 e nomes/setores/quantidades exatos; `assertThat(etapas.existsById(novoId)).isFalse()` e `assertThat(somenteDemandas()).isEqualTo(conteudo)` conserva históricos anteriores. :206–207200/versão 3/etapas vazias/contagem zero. `ARCH:83–88` IDs sobreviventes e ordens 1/2. `BROWSER:182–185` lista exata só Planejamento/Entrega, IDs conservados e ordens 1/2. | PASS |
| ETA-16 | qualquer demanda bloqueia DELETE 409/mensagem/zero escrita | `API:392` Em andamento/Concluido/Cancelado; :397–399 `status().isConflict()`, `jsonPath("$.erro").value("Esta etapa possui demandas. Mova-as para outra etapa antes de removê-la.")`, estado igual. `BOARD:201–209` ocupado 409 mantém região/confirmação, alerta exato, métodos=`["GET","DELETE","GET"]`, apenas 1 DELETE. | PASS |
| ETA-17 | versão inteira>=0; 400/mensagem de ausência/negativa ou formato estrutural, zero escrita | `API:364–365` `status().isBadRequest()`, `jsonPath("$.erro").value(erro)`, estado igual; :361 erro ausente/negativa/nula=`Informe uma versão válida do gerenciamento.`; :368–369 POST/PUT/DELETE  ×  -1/null/ausente/0.1/string 0/true/overflow de long, últimos FORMATO. :423–424 JSON impossível 400/FORMATO/estado igual nos 3 métodos. | PASS |
| ETA-18 | versão diferente 409/mensagem exata/zero escrita | `API:378–379` antigo 0 contra atual 1:409/VERSAO/estado igual, precede campos; :387–388 futuro 1 contra atual 0:409/VERSAO/estado igual, ambos 3 métodos. `RACE:130–137` segundo escritor 409/mensagem, lista 1/nome Confirmada/setor Projeto/versão 1. `BOARD:170–185` 409 com 3 campos do rascunho conservados, atualização somente GET, próximo PUT manual com versão 7. | PASS |
| ETA-19 | cada confirmação incrementa exatamente uma vez e retorna versão, até edição idêntica | `API:108/128/143` POST/PUT/DELETE versões 1/2/3; :158 `containsEntry("versao",3L)` no banco; :199/202/206 mesmo ciclo em N=0 e PUT idêntico versões 1/2/3. `BOARD:70` versões nos callbacks=`[0,1,2,3]`. `BROWSER:106/112/118/142/153/180` versões reais 1/2/3/4/5/6, arquivo seguinte 7 em :203. | PASS |
| ETA-20 | falha do banco 500/mensagem genérica, rollback config/versão/demandas | `API:409` constraint induz falha de nome ou reordenação para 3 métodos; :412–414 `status().isInternalServerError()`, `jsonPath("$.erro").value("Não foi possível concluir a operação.")`, `assertThat(estado()).isEqualTo(antes)`. Não depende só de mock nem só de contagem. | PASS |
| ETA-21 | disputa remoção/criar/mover não permite demanda órfã; demanda primeiro 409 | `RACE:75` segunda espera TimeoutException até o commit; :78–81 status=`removerPrimeiro ? 404 : 409`, mensagens ETAPA/ocupação exatas; :82–87 alvo existente=`!removerPrimeiro`, contagem de etapas 1 ou 2, versão 1 ou 0, `select count(*) ... where e.id is null`=` 0`, contagem de históricos 0 ou 1, contagem de demandas 0 ou 1; :89–90 vínculo origem/destino exato. :95 gera 4 interleavings de criar/mover  ×  primeiro. :159–161 edita demanda para destino removido 404/mensagem, conteúdo igual, versão 1. | PASS |
| ETA-22 | ativo administrável mostra Nova/Editar/Remover; demais estados omitem controles | `BOARD:84–88` três controles têm presença=`permitido` na matriz de criador/admin legado/terceiro/funcionário em legado/arquivo; callback recebe DTO exato e os métodos são somente GET. :268–274 atualização de arquivo/permissão perdida: 3 controles ausentes, rascunho Rascunho mantido e desabilitado, métodos=`["GET","POST","GET"]`. `BROWSER:197–199/210` os 3 controles têm contagem 0 no arquivado, inclusive após recarga. | PASS |
| ETA-23 | Nome/Setor/Posição rotulados, valores atuais e opções válidas | `EDITOR:26–31` `getByLabelText("Nome da etapa").value`=`atual?.nome || ""`, setor=`atual?.setor || ""`, posição=`String(atual?.ordem || 4)`; opções=`atual ? ["1","2","3"] : ["1","2","3","4"]`; foco em Nome. :36 quadro vazio, opções=`["1"]`. `BOARD:53–55` edição mostra Execução/Campo/3; :281–288 legado 7/7 usa posição visual 2/1, setor=`""`, zero escrita. `BROWSER:133–135` valores reais Execução/Campo/3. | PASS |
| ETA-24 | confirmação com nome, aviso de ocupação, Cancelar/Remover | `BOARD:112–117` diálogo=`Remover Execução?`; `getByText(...).textContent`=`Demandas impedem a remoção. Mova-as para outra etapa antes de remover esta coluna.`; foco em Cancelar; :60–61 Remover acionável. `BROWSER:167–177` diálogo nomeia Análise, contém o mesmo aviso e permite Cancelar/Remover por Tab/Enter. | PASS |
| ETA-25 | cancelar envia zero mutações e devolve foco à origem conectada | `BOARD:118–121` formulário/diálogo ausentes, `expect(document.activeElement).toBe(origem)`, métodos=`["GET"]`, matriz criar/editar/remover. `EDITOR:91–92` cancelar chamado 1 vez, salvar nunca chamado. `BROWSER:100–102` Nova focado, versão 0, lista vazia, gravações=`[]`; :164/:172 foco em Editar/Remover; :173–175 conserva 5 gravações, lista igual e versão 5. | PASS |
| ETA-26 | pendência bloqueia reenvio e saída do formulário | `EDITOR:100–106` salvar chamado exatamente 1 vez com `{nome:"Planejamento",setor:"Técnico",ordem:2}`; 3 campos e Salvar desabilitados, cancelar não chama callback; após resolver, Salvar é habilitado. `BOARD:131–137` POST/PUT: Voltar/Cancelar desabilitados, 1 requisição, voltar nunca chamado; :146–153 DELETE: controle desabilitado, 1 requisição, navegação bloqueada. `APP:40–61` POST/PUT/DELETE  ×  sucesso/falha: Sair desabilitado, token=`sessao`, usuário igual, formulário conservado, 1 requisição; depois Sair habilitado, logout funciona e remove token/usuário. :68–72 GET pendente permite Sair. Escopo testado: navegação da aplicação Voltar/Cancelar/Sair; sem evidência de recarga/voltar/fechar do navegador durante a pendência. | PASS no escopo explícito das tarefas |
| ETA-27 | API/comunicação em alerta, campos conservados e zero falso sucesso/reenvio quando o resultado não é confirmado | Erros HTTP/rede: `EDITOR:120–125`, `BOARD:170–185/201–209` e `CLIENT:139–149` mantêm os valores exatos descritos nas demais linhas. **2xx ilegível/truncado:** `frontend/src/services/api.test.js:160–165` exige rejeição `ApiError`, mensagem `Não foi possível confirmar a operação. Atualize a lista antes de tentar novamente.`, status original 201 no POST/200 no PUT/DELETE e uma chamada. `frontend/src/pages/QuadroEtapas.test.jsx:359–377` exige essa mensagem em role alert, headings `["Planejamento","Execução"]`, setores Técnico/Campo, counts 2/0, callback `[quadro]`, draft Rascunho/Engenharia/2 ou confirmação Remover Execução?, envio desabilitado e métodos `["GET",metodo]`. :381–405 prova recuperação só GET, draft conservado, envio manual com versão 7 e somente depois aplicação de versão 8. Seis casos de cada camada, HTML e JSON truncado nos três métodos. M10 e equivalentes foram mortos nesta rodada. | PASS |
| ETA-28 | snapshot retornado/cache atual/nova versão/persistência após recarga | `BOARD:51/59/63` títulos=`["Planejamento","Análise","Execução"]`, depois `["Entrega","Planejamento","Análise"]`, final `["Entrega","Planejamento"]`; :64–69 URLs/payloads POST com versão 0, PUT com 1 e DELETE com 2 exatos; :70 callbacks com versões 0/1/2/3 e :71 DTO final exato; :330 `expect(JSON.parse(arquivo[1].body)).toEqual({versao:1})`; :331 1 GET de snapshot, sem recarga extra. `BROWSER:122–158/182–185` listas completas de IDs/nome/setor/ordem/contagens; :130/:211 recarga preserva lista; :192 arquivamento envia `{versao:6}`. | PASS |
| ETA-29 | carregamento/erro/vazio distintos, retry somente GET; conflito permite atualização antes do reenvio manual | `frontend/src/pages/Quadro.test.jsx:23` estado de carregamento em região de status; :53–60 erro exato, 2 consultas exclusivamente GET. `BOARD:178–185` atualização somente por GET, próximo PUT manual com versão 7; :225–235 alerta Consulta indisponível, rascunho Rascunho/Engenharia/2, coluna existente e ausência de vazio em :231, métodos=`["GET","PUT","GET","GET"]`; :296–297 vazio real após remoção e métodos=`["GET","DELETE"]`. | PASS |
| ETA-30 | Tab/Enter opera todos os controles do CRUD; erros em região de alerta | `frontend/src/test/keyboard.js:4–8` Tab até o foco, `expect(document.activeElement).toBe(controle)`, Enter. `EDITOR:79–85` foco em Setor/Posição e payload de teclado exato; BOARD usa o helper em Nova/Editar/Salvar/Cancelar/Remover/Atualizar/Tentar novamente/Voltar; :225 `role`=`alert` e erros consultados por findByRole com alert. `BROWSER:13/18/34` foco por Tab, Enter e seleção nativa da ordem; fluxo real aciona os controles de CRUD, cancelar, Voltar e arquivamento. | PASS |

## Reverse mapping: todos os testes novos/adaptados

Contagens abaixo são expansões parametrizadas reais. Toda declaração em arquivos novos/adaptados no diff tem um AC, edge ou Done-when; nenhum teste novo sem claim foi encontrado.

| Declaração física | Casos | Claim |
| --- | --- | --- |
| `API:38` snapshotPreservaLegadoContaTodosStatusEDevolveMetadadosAtuais | 5 | ETA-01/02/03; todos os status; readonly |
| `API:90` cicloCrudPosicoesNormalizaLegadoPreservaConteudoEUsaIdsGerados | 3 | ETA-04/12/13/14/15/19; IDs do payload ignorados; vínculos/history |
| `API:163` consultasOrdenamAntesPorOrdemMesmoQuandoIdDaEtapaEhMaior | 1 | ETA-02; prioridade da ordem |
| `API:176` edicaoDaUltimaParaPrimeiraReorganizaTodosEPreservaIdEVinculo | 1 | ETA-08/13; movimento inverso; ID/quadro |
| `API:196` vazioCriaNaPosicaoUmEditaIdenticoIncrementaUmaEPermiteRemoverUltima | 1 | ETA-12/15/19; N=0/última/idêntico |
| `API:213` terceiroOuSemCriadorRecusaTodaMutacaoAntesDeCampos | 12 | ETA-05; precedência de perfil/arquivo |
| `API:230` criadorOuAdminNaoMutaArquivadoNemVersao | 6 | ETA-07 |
| `API:247` rotasExigemSessaoAntesDeJSON | 10 | ETA-06; 5 rotas × 2 sessões |
| `API:263` quadroAusenteRetorna404 | 5 | ETA-08; 5 rotas |
| `API:275` etapaAusenteOuDeOutroQuadroRetorna404SemTransferencia | 4 | ETA-08; PUT/DELETE ausente/estrangeiro |
| `API:288` validaNomeSetorUTF16SemEscrever | 24 | ETA-09/10; 2 métodos × 2 campos × 6 limites |
| `API:309` aceitaLimitesNormalizadosWhitespaceJS | 4 | ETA-09/10; 1/255 UTF-16 JS |
| `API:323` posicaoExigeInteiroEIntervalo | 18 | ETA-11; 2 métodos × 9 formatos/limites |
| `API:337` editarNaoAceitaPosicaoNMaisUm | 1 | ETA-11; limitesPUT |
| `API:347` criaEmTodasAsPosicoesInclusiveUltima | 3 | ETA-12; início/meio/fim |
| `API:359` versaoEstritaRecusaSemEscrever | 21 | ETA-17; 3 métodos × 7 formatos/limites |
| `API:374` versaoAntigaConflitaAntesDosCampos | 3 | ETA-18; precedência de versão |
| `API:384` versaoFuturaTambemConflitaSemEscrever | 3 | ETA-18; diferença em ambos os sentidos |
| `API:393` ocupadaNaoRemoveIndependentementeDoStatus | 3 | ETA-16; 3 status |
| `API:404` falhaDeBancoReverteEtapasOrdemVersaoEConteudo | 3 | ETA-20; 3 métodos |
| `API:420` jsonImpossivelRetorna400SemMutacao | 3 | ETA-17; formato estrutural/edge de zero escrita |
| `RACE:41` removerDisputaComCriarOuMoverSemOrfaoEHistoricoParcial | 4 | ETA-21; ambas as ordens de criar/mover |
| `RACE:98` duasConfiguracoesComVersaoAntigaNaoSobrescrevem | 1 | ETA-18/19 |
| `RACE:103` snapshotEsperaCommitECombinaVersaoComEtapasAtuais | 1 | ETA-01/02; consistência do snapshot |
| `RACE:151` edicaoDeDemandaComDestinoRemovidoReverteCamposEHistorico | 1 | ETA-21; destino removido |
| `ARCH:39` arquivadoBloqueiaTresRotasAteParaAdminEPreservaConsulta, adaptado | 3 | ETA-03/07; T1 preservação dos anteriores |
| `ARCH:64` ativoPermiteCriacaoEdicaoExclusaoPeloCriadorComPosicoesValidas, adaptado | 1 | ETA-04/12/13/15/19; contrato substituído |
| `ARCH:92/97` disputas arquivo/estrutura, adaptadas | 2 | Edge spec: arquivamento primeiro 409/estrutura primeiro, conflito com versão antiga; ETA-07/18/19 |
| `CLIENT:116` GET estrutura | 1 | ETA-01/29; T2 rota/token/signal |
| `CLIENT:125` POST/PUT/DELETE payload/snapshot | 3 | ETA-12/13/15/28; T2 |
| `CLIENT:136` erro HTTP sem reenvio | 3 | ETA-27/29; T2 |
| `CLIENT:143` rede ambígua sem reenvio | 3 | ETA-27; T2 |
| `EDITOR:24` criar/editar/setor nulo | 3 | ETA-23; legado |
| `EDITOR:34` vazio, opção 1 | 1 | ETA-23; N=0 |
| `EDITOR:49` campos inválidos | 8 | ETA-09/10/27 |
| `EDITOR:59` 1/255 normalizados | 2 | ETA-09/10/23 |
| `EDITOR:67` posição inválida | 1 | ETA-11 |
| `EDITOR:75` teclado nos campos/salvar | 1 | ETA-23/30 |
| `EDITOR:88` teclado Cancelar | 1 | ETA-25/30 |
| `EDITOR:95` pending | 1 | ETA-26 |
| `EDITOR:114` 403/409/500/rede | 4 | ETA-27 |
| `BOARD:37` CRUD, snapshots/IDs/versões | 1 | ETA-12/13/15/19/28/30 |
| `BOARD:81` roles/state | 5 | ETA-03/22 |
| `BOARD:96` contagem ocupada/vazia | 1 | ETA-01; T4 |
| `BOARD:107` 3 cancelamentos | 3 | ETA-24/25/30 |
| `BOARD:124/140`POST/PUT/DELETE pendentes | 3 | ETA-26 |
| `BOARD:159` 409/rede e retry manual | 2 | ETA-18/27/29/30 |
| `BOARD:195`DELETE 409/403/404/500/rede | 5 | ETA-16/27/29 |
| `BOARD:212`atualização falha | 1 | ETA-27/29/30 |
| `BOARD:238`repetidos | 1 | ETA-14/22/28 |
| `BOARD:257`atualização de flag arquivo/permissão | 2 | ETA-22/27/29 |
| `BOARD:277`legado 7/7 | 1 | ETA-02/23 |
| `BOARD:291`última vazia | 1 | ETA-15/29 |
| `BOARD:300`cache de arquivamento | 1 | ETA-28 |
| `APP:21` 3 métodos × sucesso/falha | 6 | ETA-26; T6 |
| `APP:65` GET permite saída | 1 | T6 Done when; ETA-26 limite |
| `BROWSER:47` CRUD real e layout/arquivo | 1 | ETA-12/13/15/22/24/25/28/30; T5 |
| `frontend/src/pages/Quadro.test.jsx:32`, adaptado | 1 | ETA-03/22; preservação de fixture T4 |
| `frontend/src/pages/Quadro.test.jsx:47`, adaptado | 3 | ETA-27/29; erros iniciais exatos |
| `frontend/src/pages/Quadro.test.jsx:63`, adaptado | 1 | ETA-29/30; endpoint GET/teclado |
| `frontend/src/pages/Gerenciamentos.test.jsx:25–27`, fixture adaptada | testes anteriores:57/:221/expansão abrir:318 | T4 preservação de assertions; novo GET de snapshot |



## T7: assertions adicionais e reverse mapping dos 12 casos

A spec define a mensagem exata e que resultado ilegível não confirma sucesso; o contrato novo conserva o status HTTP original, sem afirmar rollback no servidor nem exigir validação de schema. A produção correta não mudou.

| Outcome | Assertion física completa e valor exigido |
| --- | --- |
| Cliente rejeita resultado ilegível e conserva status | `frontend/src/services/api.test.js:160` `await expect(executar()).rejects.toMatchObject({ name:"ApiError", message:"Não foi possível confirmar a operação. Atualize a lista antes de tentar novamente.", status })`; :111–113 define POST 201/PUT 200/DELETE 200; :165 `expect(fetchSpy).toHaveBeenCalledTimes(1)` |
| Alerta acessível e conteúdo original | `frontend/src/pages/QuadroEtapas.test.jsx:359–360` `expect((await screen.findByRole("alert")).textContent).toBe("Não foi possível confirmar a operação. Atualize a lista antes de tentar novamente.")`; :361–362 headings `.toEqual(["Planejamento","Execução"])`; :363–366 setores exatos Técnico/Campo e textos ` 2 demandas`/` 0 demandas`; :367 vazio `.toBeNull()`; :368 `expect(atualizar.mock.calls.map(([registro])=>registro)).toEqual([quadro])` |
| Rascunho/confirm conservados | `QuadroEtapas.test.jsx:369` dialog `Remover Execução?` `.not.toBeNull()` para DELETE; :371–373 Nome/Setor/Posição `.toBe("Rascunho")`, `.toBe("Engenharia")`, `.toBe("2")` para POST/PUT; :375 botão `.disabled` `.toBe(true)`; :377 métodos `.toEqual(["GET",metodo])` após tentativa de clique |
| GET-only recovery e mesma decisão pendente | `QuadroEtapas.test.jsx:381–382` calls após atualização `.toEqual([[`${endpoint}/estrutura-etapas`,"GET"]])`; :383 callbacks `.toEqual([quadro,{...quadro,versao:7}])`; :384 mutation calls `.toHaveLength(1)`; :385/:387–389 mesmos dialog/campos; :391 botão habilitado sem execução automática |
| Somente nova ação manual confirma snapshot | Ação manual :392; `QuadroEtapas.test.jsx:394` callbacks `.toEqual([quadro,{...quadro,versao:7},{...quadro,versao:8}])`; :395–397 alert/editor/dialog `.toBeNull()`; :398–399 headings DELETE=`["Planejamento"]`, PUT=`["Planejamento","Rascunho"]`, POST=`["Planejamento","Rascunho","Execução"]` |
| Payload completo com versão atual | `QuadroEtapas.test.jsx:402–404` `expect(fetchMock.mock.calls.filter(([,request])=>request.method===metodo).map(([url,request])=>[url,request.method,JSON.parse(request.body)])).toEqual([[rota,metodo,{...payload,versao:0}],[rota,metodo,{...payload,versao:7}]])`; :405 métodos `.toEqual(["GET",metodo,"GET",metodo])`. POST rota `/9/etapas`, PUT/DELETE `/9/etapas/5`. POST/PUT `{nome:"Rascunho",setor:"Engenharia",ordem:2,versao:0}` e igual versão 7; DELETE somente `{versao:0}`/`{versao:7}` |

Fixture física `QuadroEtapas.test.jsx:8–13`: quadro id 9/Instalações/Postes/criador{id 3, Ana}/ativo/versão 0/administrável; etapas id 4 Planejamento/Técnico/ordem 1/count 2 e id 5 Execução/Campo/ordem 2/count 0. Logo as assertions de callback conservam o DTO inteiro, não apenas sua versão.

| Caso novo | Declaração física | Resposta | Claim |
| --- | --- | --- | --- |
| API1 | `frontend/src/services/api.test.js:153–155` | POST 201, HTML ilegível | ETA-27, T7; assertions :160–165 |
| API2 | mesma declaração | POST 201, JSON truncado | ETA-27, T7; :160–165 |
| API3 | mesma declaração | PUT 200, HTML ilegível | ETA-27, T7; :160–165 |
| API4 | mesma declaração | PUT 200, JSON truncado | ETA-27, T7; :160–165 |
| API5 | mesma declaração | DELETE 200, HTML ilegível | ETA-27, T7; :160–165 |
| API6 | mesma declaração | DELETE 200, JSON truncado | ETA-27, T7; :160–165 |
| UI1 | `frontend/src/pages/QuadroEtapas.test.jsx:336–338` | POST 201, HTML ilegível | ETA-27/28/29, T7; :359–405 |
| UI2 | mesma declaração | POST 201, JSON truncado | ETA-27/28/29, T7; :359–405 |
| UI3 | mesma declaração | PUT 200, HTML ilegível | ETA-27/28/29, T7; :359–405 |
| UI4 | mesma declaração | PUT 200, JSON truncado | ETA-27/28/29, T7; :359–405 |
| UI5 | mesma declaração | DELETE 200, HTML ilegível | ETA-27/28/29, T7; :359–405 |
| UI6 | mesma declaração | DELETE 200, JSON truncado | ETA-27/28/29, T7; :359–405 |

Os corpos físicos em api.test.js:152 e QuadroEtapas.test.jsx:335 são `<html>resposta incompleta</html>` e `{"gerenciamento":`. Novos Java: API 134 + RACE 7 = 141; ARCH 6 conservados. Vitest novo desde baseline: CLIENT 16 + EDITOR 22 + BOARD 33 + APP 7 = 78. T7 é acréscimo de 6 CLIENT + 6 BOARD; todos os casos antigos continuam mapeados pela matriz acima.

## Sensor atual, rodada 2

Cinco faults foram aplicados um por vez em fontes scratch e restaurados byte a byte no finally antes do próximo. Nenhum teste foi alterado. Todos compilam e chegam à execução das assertions; falha de compilação não foi contada. Gate completo passou antes do sensor.

Nos três wrappers, o fault adiciona `.catch(error=>{ if(error.status===STATUS) return {gerenciamento:{id,versao:VERSION,podeAdministrar:true,arquivado:false},etapas:[]}; throw error; })`, inventando snapshot quando o parser rejeita a resposta de sucesso. STATUS=201 para POST/200 para PUT/DELETE; VERSION=`dados.versao+1` para POST/PUT, `versao+1` para DELETE. M10 POST repete exatamente o fault semântico sobrevivente da rodada 1.

| Fault atual | Fonte e alteração | Comando | Killed por assertion física / resultado | Exit |
| --- | --- | --- | --- | --- |
| R2-M10-POST | `frontend/src/services/api.js:86`, falso snapshot ao erro status 201 | `npm.cmd test -- src/services/api.test.js src/pages/QuadroEtapas.test.jsx` | KILLED: :160 exige rejeição mas a promise resolveu; QuadroEtapas.test.jsx:359 exige alerta ausente no mutante. Ambos corpos, 4 falhas/60 PASS/64 casos | 1 |
| R2-M11-PUT | `frontend/src/services/api.js:90`, falso snapshot ao erro status 200 | mesmo comando Quick | KILLED pelas mesmas assertions para PUT, 4 falhas/60 PASS/64 casos | 1 |
| R2-M12-DELETE | `frontend/src/services/api.js:94`, falso snapshot ao erro status 200 | mesmo comando Quick | KILLED pelas mesmas assertions para DELETE, 4 falhas/60 PASS/64 casos | 1 |
| R2-M1-autorização | `sistema/src/main/java/com/patp/sistema/service/EtapaService.java:111`, remove exigirAdministracao | `mvn.cmd -B verify -Dtest=EtapaApiTests#terceiroOuSemCriadorRecusaTodaMutacaoAntesDeCampos` | KILLED 12/12: `EtapaApiTests.java:219` exige 403, recebeu 400/409 | 1 |
| R2-M4-versão | EtapaService.java:165, comparação `if(false && !Objects.equals(...))` | `mvn.cmd -B verify -Dtest=EtapaApiTests#versaoAntigaConflitaAntesDosCampos+versaoFuturaTambemConflitaSemEscrever` | KILLED 6/6: EtapaApiTests.java:378/387 exige 409, recebeu 400/201/200 | 1 |

Resultado atual: 5/5 killed, 0 survived, 0 faults inválidos. O gap V1/M10 está resolvido por outcome e por discriminação empírica, sem mudar a produção.

## Histórico do sensor da rodada 1 e identidade

A rodada 1, head 9a19344, executou 10 faults compiláveis: 9 killed e M10 sobrevivente. Seu relatório foi preservado no commit 367959e. Não houve reexecução dos nove como conjunto nesta rodada: apenas autorização e stale foram repetidos, com novos equivalentes para o parser. M10 foi reexecutado e morto.

| Fault histórico | Resultado real da rodada 1 | Situação nesta rodada |
| --- | --- | --- |
| M1 remove autorização | KILLED 12/12, 403 virou 400/409 | repetido em R2-M1 |
| M2 remove verificarAtivo | KILLED 6/6, 409 virou 201/200 | histórico, não rerodado |
| M3 renumerar i+2 | KILLED 4/4, ordem 1 virou 2 | histórico, não rerodado |
| M4 remove comparação de versão | KILLED 6/6, 409 virou 400/201/200 | repetido em R2-M4 |
| M5 remove FORCE_INCREMENT | KILLED 1/1, versão 1 virou 0 | histórico, não rerodado |
| M6 remove check de ocupação | KILLED 3/3, 409 virou 500 da FK | histórico, não rerodado |
| M7 remove lock do quadro | KILLED 4/13, espera/409 violados | histórico, não rerodado |
| M8 UI manda versão 0 | KILLED 3/27, payload versão 1/7 violado | histórico, não rerodado |
| M9 UI não bloqueia Sair | KILLED 6/7, Sair.disabled false | histórico, não rerodado |
| M10 POST inventa sucesso ilegível | SURVIVED 137/137, exit 0 | resolvido: R2-M10 KILLED |

Logs históricos permanecem em `C:/Users/Marco/AppData/Local/Temp/etapas-verifier-892a061698bd4112b4cad0673e23bdbf/`. Comparação independente entre 9a19344 e d067682 por `git show`/blobs confirma fontes relevantes e testes antigos intactos. Hashes atuais/antigos:

| Fonte/teste | Git blob antigo e atual iguais ou alteração preservada |
| --- | --- |
| EtapaService.java | ` 89d0bbb35d5de5e5517b6e9d50b3b053d9987cee`, igual |
| EtapaApiTests.java | ` 75d292526e44fb8767cbcf2a1cec2010880697a0`, igual |
| EtapaDemandConcurrencyTests.java | ` 00b07ef964582a6102ecde49a275ffd19176cbc8`, igual |
| EtapaArchiveTests.java | `efcc77eb32acb0ec99c58fdae14090e7d022c6c7`, igual |
| frontend/src/services/api.js | ` 10ec66981df28d167c70167a456ea20ecdd816df`, igual |
| frontend/src/pages/Quadro.jsx | ` 72a9f461cd1c427611888d1f9c48511e3f40d30d`, igual |
| frontend/src/App.test.jsx | `b8f534211f0494bcb870771f9044a92d1bffcda6`, igual |
| frontend/src/services/api.test.js | antigo ` 3a2b9d73475edd5ad086555b1c9b2082a141524d`, novo `bfc5e2f845334afa4e882c9e37f4162dbc2dd504`; prefixo antigo idêntico byte a byte |
| frontend/src/pages/QuadroEtapas.test.jsx | antigo `e8650816e8a8de17191d50967772a377b6949cb7`, novo ` 82fb14dc7199c9536392870f415cc0f0f38e4ea6`; prefixo antigo idêntico byte a byte |

O arquivo físico `hash-identity.json` da rodada 2 guarda Git blobs/SHA256 e igualdade dos prefixos. Nenhuma assertion antiga foi acomodada à correção. A comparação do diff também confirma ausência de mudança em toda produção, backend, E2E e runner após 9a19344.

## E2E histórico de T7, limite preservado

O autor teve uma primeira invocação E2E com exit 1 por `apiRequestContext.get: read ECONNRESET`, após POST 201. Essa falha não foi apagada nem convertida em PASS. Artefatos físicos preservados em `C:/Users/Marco/AppData/Local/Temp/creral-etapas-t7-e2e-failure-20261005-1806/{trace.zip,error-context.md,first-failure.log}`.

A leitura independente do ZIP confirma: `test.trace:232–233` heading da etapa visível; :234–236 GET de estrutura falha, source etapas.spec.js:68/caller:105. ` 0-trace.trace:44/:50` registra GET/ECONNRESET e ` 0-trace.network:4` não tem resposta. ` 2-trace.network:49` POST 201 aponta body `resources/068e97c2ad15990fbb5c8b9f1f63916f4abbdaea.json`: quadro id 1/ativo/versão 1, etapa id 1/Planejamento/Técnico/ordem 1/count 0. `error-context.md` mostra coluna renderizada e editor fechado.

O log físico da repetição diagnóstica do autor, `C:/Users/Marco/AppData/Local/Temp/creral-etapas-t7-e2e-20261005-diagnostic.log:123/:127/:240/:244`, registra os dois PASS e encerramento normal dos helpers depois em :124–125/:241–242. Essa é evidência histórica examinada, não o gate independente atual. A causa original do socket continua indeterminada. O gate atual da rodada 2 passou na primeira execução, sem repetição nem retries.

## Isolamento da rodada 2

Baseline completo `git status --porcelain=v1 --untracked-files=all` capturado antes do setup/sensor: 3403 linhas. Scratch absoluta validada dentro TEMP, única desta rodada, foi restaurada (`git diff --exit-code -- sistema/src frontend/src` exit 0) e removida com caminho absoluto validado. Nenhum processo java/node próprio permaneceu e portas 18082/4173 estavam livres. Processos do usuário em 8081/PID 22976 e 5173/PID 19612 permaneceram; não foram encerrados, reiniciados ou usados pelo gate.

Antes de escrever este relatório, os arquivos serializados UTF-8 do porcelain real antes/depois foram idênticos byte a byte, 3403 linhas, SHA256 de ambos `D2228C50E42491119DEAE1F3F63786107EC3DA1F7AEEA362DE791801692C6A9A`. Evidência física na pasta TEMP atual: `real-porcelain-before.txt`/`real-porcelain-after.txt`. `git worktree list` voltou a somente o real em d067682. Artefatos modified/untracked anteriores, inclusive node_modules/target/dist/test-results, foram preservados sem limpeza/restore. Nenhum git stash, app normal, conexão MySQL configurada, credencial real, produção, push/deploy foi usado.

## Qualidade, edges e limites

- T7 é append-only nos dois arquivos de testes. As alterações de produção da feature seguem padrões existentes e são necessárias ao contrato; nenhuma biblioteca, schema, drag and drop ou categoria final foi acrescentada.
- Domain mapping cobre ETA-01–21 e rotas API happy/edge/error. UI integration cobre GET e as três mutações em happy/edge/error, incluindo agora resposta 2xx ilegível e recovery/manual. Todo teste novo/adaptado tem claim nas tabelas; não foram encontrados testes sem requisito.
- Edges comprovados: limites UTF-16/normalização JS; vazio/última etapa; gaps/empates legados somente leitura; ID estrangeiro; nomes repetidos por ID; vínculos/status/responsável/comentários/histórico preservados; recusas/stale/banco com rollback integral; disputas estrutura/demanda/arquivo em ambas as ordens. ARCH:137–151 conserva a prova de arquivo confirmado primeiro e conflito de versão quando a estrutura confirmou primeiro.
- Guias: `docs/OPERACAO.md:3/:19/:54`, testes H2/Edge e MySQL isolado. Nenhum AGENTS/guia adicional encontrado; princípios da skill aplicados. Sem SPEC_DEVIATION, simplificação fora de escopo ou weakening.
- Browser E2E verifica o escopo T5: CRUD/posições/IDs/reload/cancelamento/teclado/cache de arquivo/readonly. Ramos negativos de API/pending são cobertos por RTL, não por Playwright. Navegação pendente foi verificada nos controles da aplicação Voltar/Cancelar/Sair; recarga/voltar/fechar do navegador durante a pendência não é declarado implementado ou testado.
- QA visual independente da rodada 1 inspecionou 8 capturas desktop/mobile; frontend/CSS e E2E não mudaram. As novas capturas atuais estão em `C:/Users/Marco/AppData/Local/Temp/creral-etapas-e2e-Thdqvm/`; assertions físicas BROWSER:39/:42 exigem `scrollWidth<=clientWidth`. Não foi feita nova inspeção visual destas imagens nem inferido aceite humano.
- T1–T7 estão concluídas localmente. O verificador alterou somente este relatório após comprovar isolamento. Spec/tasks/STATE e fontes/testes não foram editados pelo verificador nesta rodada.

## Fechamento técnico e lições

V1/T7 está resolvida: 30/30 ACs, todos os gates atuais PASS e 5/5 mutantes mortos. Não há gap ranqueado pendente encontrado nesta rodada. A falha de transporte histórica e o escopo de UAT acima permanecem explícitos.

Não há novo sinal de falha para distilar nesta rodada. L-001 e L-002 permanecem candidates, sem promoção, penalização ou novo registro. O FAIL histórico de M10 continua fundamentando L-002; a correção não apaga esse histórico.

**Completion gate:** `python .agents/skills/tlc-spec-driven/scripts/validate_state.py etapas` retornou `validate_state: 0 error(s) across [etapas]`, exit 0, após gravar o relatório PASS. UAT humano continua pendente; nenhum PASS humano foi declarado.
