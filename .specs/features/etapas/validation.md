# Validação independente: CRUD de etapas

**Result**: FAIL. Os gates passam, mas ETA-27 tem um ramo sem evidência: resposta HTTP 2xx com JSON ilegível ou truncado. Uma mutação que inventa sucesso nesse ramo sobreviveu à suíte completa. O cliente real trata o erro corretamente; a lacuna está na discriminação dos testes.

**Data:** 2026-10-05. **Verificador:** `/root/verify_etapas`, independente dos autores; leitura UI auxiliar independente por `/root/verify_etapas/coverage_ui`. **Spec única:** `.specs/features/etapas/spec.md`. **Branch/head:** `testes`, `9a19344844a9c4c0cd870726fc563441e0600aeb`. **Diff:** `4dd4abe..9a19344`, 36 arquivos. Leitura integral de SKILL.md, validate.md, sub-agents.md e coding-principles.md antes da verificação. Evidências autorais não substituíram leitura dos testes nem execução independente.

29/30 ACs completos; ETA-27 parcial. Zero gaps de precisão nos outcomes usados nesta matriz. Sensor expandido: 10 mutações compiláveis, 9 killed, 1 survived. Nenhuma alteração de código/teste no real. UAT humano não realizado e não declarado PASS.

## Gates executados

Todos os comandos rodaram em worktree TEMP nova no head acima. JAVA_HOME foi `C:/Program Files/Java/jdk-25.0.2`. `npm.cmd ci` exit 0 instalou 98 pacotes na scratch. Maven e helper E2E nunca rodaram simultaneamente nessa scratch.

| Gate obrigatório de tasks.md | Resultado observado | Exit |
| --- | --- | --- |
| `mvn.cmd -B verify`, sistema | 238 testes; 0 falhas, 0 erros, 0 skips; BUILD SUCCESS | 0 |
| `npm.cmd test`, frontend | 137 testes em 7 arquivos; 0 falhas, 0 skips | 0 |
| `npm.cmd run build` | Vite gerou bundle; 23 módulos transformados | 0 |
| `npm.cmd run lint` | oxlint no perímetro src/e2e/scripts/configs | 0 |
| `npm.cmd run test:e2e` | wrapper executou etapas.spec.js: 1 PASS; gerenciamentos.spec.js: 1 PASS; 2 PASS totais, 0 falhas/skips/retries | 0 |
| `git diff --check 4dd4abe..9a19344` | sem whitespace errors | 0 |

Baseline antes da feature: 97 Java, 71 Vitest, 1 E2E. Final: 238 Java (+141), 137 Vitest (+66), 2 E2E (+1). Os seis casos de EtapaArchiveTests continuam executados. Seu contrato de sucesso foi adaptado para criador, posição válida, versão e snapshot conforme AD-007/009; assertions de integridade/arquivo/concorrência foram conservadas ou fortalecidas. Os testes frontend anteriores conservaram assertions; fixtures do quadro foram adaptadas ao novo snapshot. Nenhuma exclusão, skip, disable ou enfraquecimento para passar foi encontrado.

`sistema/src/test/java/com/patp/sistema/SistemaApplicationTests.java:22` continua executando `assertTrue(connection.getMetaData().getURL().startsWith("jdbc:h2:mem:"))`. Configuração física de teste: `sistema/src/test/resources/application.properties:2`, H2 em memória com create-drop. A aplicação normal não foi iniciada.

Logs independentes preservados em `C:/Users/Marco/AppData/Local/Temp/etapas-verifier-892a061698bd4112b4cad0673e23bdbf/`: `java-gate.log`, `test-gate.log`, `build-gate.log`, `lint-gate.log`, `e2e-gate.log`, `npm-ci.log`, `sensor-results.json` e `sensor-M10.json`.

MySQL foi apenas conferido documentalmente, sem nova execução ou conexão: `.specs/features/etapas/mysql-validation.md:3` e log físico `C:/Users/Marco/AppData/Local/Temp/creral-mysql-crud-be180f80b1f64ead87cc0113c6bd68c1/etapas-maven-mysql.log:9636` registram 237/0/0/0 e BUILD SUCCESS em :9639. As linhas :70/:71/:73 e :1821/:1822/:1824 confirmam banco fictício em localhost:33817, Connector/J e MySQL 8.0.43. O resumo JSON físico confirma as dez classes. Fontes backend não mudaram após c2e3cce. O caso específico de H2 permaneceu no gate completo de 238 testes; não foi desabilitado para obter os 237 testes de MySQL.

## Evidência de estado e vocabulário da matriz

Paths nesta seção são relativos ao repositório. `API` = `sistema/src/test/java/com/patp/sistema/EtapaApiTests.java`; `RACE` = `sistema/src/test/java/com/patp/sistema/EtapaDemandConcurrencyTests.java`; `ARCH` = `sistema/src/test/java/com/patp/sistema/EtapaArchiveTests.java`; `EDITOR` = `frontend/src/pages/EditorEtapa.test.jsx`; `BOARD` = `frontend/src/pages/QuadroEtapas.test.jsx`; `CLIENT` = `frontend/src/services/api.test.js`; `APP` = `frontend/src/App.test.jsx`; `BROWSER` = `frontend/e2e/etapas.spec.js`.

Não são aliases para evidência autoral: todos apontam para assertions físicas lidas. `API:435` implementa `estado()` com `conteudoPersistido()` mais `select * from gerenciamentos order by id`; `sistema/src/test/java/com/patp/sistema/ApiIntegrationSupport.java:78` captura `etapas`, `processos`, `comentarios` e `historicos` com todas as colunas em ordem de ID. Portanto `assertThat(estado()).isEqualTo(antes)` verifica também versão/metadados, vínculos, status, responsáveis, comentários e históricos, além da contagem. `API:437` exclui somente etapas dessa mesma captura quando a mutação estrutural válida deve preservar demandas/comentários/históricos.

Mensagens exatas usadas pelas assertions: PERMISSAO = `Você não tem permissão para administrar este gerenciamento.` (`API:30`); ARQUIVO = `Gerenciamento arquivado. Restaure-o antes de alterar.` (:31); VERSAO = `Gerenciamento alterado por outro usuário. Atualize e tente novamente.` (:32); ETAPA = `Etapa não encontrada neste gerenciamento.` (:33); FORMATO = `Dados da requisição inválidos.` (:34). Cada referência abaixo inclui a assertion, seu valor esperado e o contexto parametrizado.

## Matriz dos 30 critérios

| AC | Outcome exigido pela spec | Assertion física, valores e branches | Resultado |
| --- | --- | --- | --- |
| ETA-01 | GET 200, DTO atual e cada etapa com ID/nome/setor/ordem/quantidade real | `API:55` `status().isOk()`; :56–61 `jsonPath("$.gerenciamento.id").value(quadro.getId())`, nome=`Quadro`, descricao=`Descrição`, arquivado=`ator.equals("arquivado")`, versao=`ator.equals("arquivado") ? 1 : 0`, podeAdministrar=`criador ou admin`; :75–79 criador nulo no legado ou ID e nome=`Criador`. :62–72 lista 2 e etapas: primeira ID/nome=`Antiga`/setor vazio/ordem 7/quantidade 0; segunda ID/nome=`Empatada`/setor=`Engenharia`/ordem 7/quantidade 3. Fixtures :47–51 contam Em andamento/Concluido/Cancelado e excluem quadro externo. `RACE:143` `jsonPath("$.gerenciamento.versao").value(1).match(resultado)` e :144–147 lista 1/nome=`Confirmada`/setor=`Projeto`/ordem 1 após esperar commit. | PASS |
| ETA-02 | ordem depois ID, legado sem escrita nem setor inventado | `API:63/68` IDs crescente com ordem 7/7; :65 setor vazio; :85 `assertThat(estado()).isEqualTo(antes)`. `API:167–172` array e snapshot retornam primeiro ID mais novo de ordem 2, depois antigo de ordem 9, e estado igual. `BOARD:281–288` edição usa posição visual2/1, opções `["1","2"]`, setor desconhecido=`""` e métodos=`["GET"]`. | PASS |
| ETA-03 | consulta autenticada permitida em arquivo/sem criador | `API:37–43` matriz criador/terceiro/admin/legado/arquivado, mesma consulta :55 `isOk()`, :75 criador=`null` no legado; array :81 `isOk()` e :85 estado igual. `BOARD:81–93` snapshot arquivado/terceiro/legado renderiza conteúdo consultável com flags atuais. | PASS |
| ETA-04 | criador/admin cria, edita e remove vazia em ativo | `API:89` casos criador/admin/adminLegado; :108 `status().isCreated()`, :128 e :142 `status().isOk()` para PUT/DELETE. :124 proprietário da nova etapa=`quadro.getId()`; :155 somente novoId removido; :156 conteúdo preservado. `ARCH:72/78/82` conserva ciclo anterior com 201/200/200. | PASS |
| ETA-05 | POST/PUT/DELETE terceiro 403, mensagem exata, zero escrita; legado somente admin | `API:219` `status().isForbidden()` e `jsonPath("$.erro").value(PERMISSAO)`; :220 `assertThat(estado()).isEqualTo(antes)`. :223–225 gera 3 métodos  ×  criador presente/ausente  ×  ativo/arquivo, com payload {} provando precedência de administração sobre campos/arquivo/versão. Sucesso adminLegado está em ETA-04. | PASS |
| ETA-06 | sem sessão 401 em configuração e CRUD, zero escrita | `API:253` `mvc.perform(request).andExpect(status().isUnauthorized())`; :254 estado igual. :257–258 gera GET array/GET snapshot/POST/PUT/DELETE  ×  header ausente/Bearer inexistente; mutações recebem JSON impossível para provar autenticação primeiro. | PASS |
| ETA-07 | criador/admin arquivo 409 e mensagem, zero escrita | `API:237` `status().isConflict()` e `jsonPath("$.erro").value(ARQUIVO)`; :238 estado igual; :241–242 gera 3 métodos  ×  criador/admin. `ARCH:55–60`409/mensagem/estado igual/versão 1/GET 200 com etapa Original. | PASS |
| ETA-08 | quadro/etapa ausente 404, mensagem do recurso; ID estrangeiro não transfere | `API:268–270` `status().isNotFound()`, `jsonPath("$.erro").value("Gerenciamento não encontrado.")`, estado igual em 5 rotas. :280–281 `status().isNotFound()`, `jsonPath("$.erro").value(ETAPA)`, estado igual em PUT/DELETE  ×  ausente/outro quadro. Tentativas de ID/quadro/criador recebidos não trocam entidade: :123 `novoId` diferente dos IDs existentes, :124 quadro persistido correto; :185/:190–192 PUT conserva ID/quadro da URL e conteúdo. | PASS |
| ETA-09 | POST/PUT nome normalizado0/>255 UTF-16 retorna 400 e mensagem exata | `API:297–299` `status().isBadRequest()`, `jsonPath("$.erro").value("Informe um nome de etapa entre 1 e 255 caracteres.")` no campo nome e estado igual; :303–304 ausente/nulo/vazio/whitespace NBSP+BOM/256 letras/128 emoji, nos 2 métodos. :314–316 limites 1/255 com whitespace JS retornam 201/200, nome exato=`texto`, versão 1 e nome persistido=`texto`. `EDITOR:53–56` alerta exato, drafts iguais e zero saves; :63 payload normalizado com acentos exato. | PASS |
| ETA-10 | POST/PUT setor normalizado0/>255 UTF-16 retorna 400 e mensagem exata | Mesma matriz `API:297–299`, campo setor, `jsonPath("$.erro").value("Informe um setor entre 1 e 255 caracteres.")` e estado igual. :315 `jsonPath("$.etapas[0].setor").value(texto)` nos limites 1/255 normalizados. `EDITOR:45–56` erros exatos e draft preservado; :63 `salvar` recebe `{ nome: texto, setor: texto, ordem: 2 }`. | PASS |
| ETA-11 | posição inteiro positivo 1..N+1 no POST e 1..N no PUT;400/formato ou limite exato, zero escrita | `API:328–329` `status().isBadRequest()`, `jsonPath("$.erro").value(erro)`, estado igual. :325 define erro de limite para -1/0/null/ausente/3; demais 1.1/string 1/true/overflow de inteiro usam FORMATO; :333 roda POST/PUT. :340–342 PUT N+1 recusa 400 com `Escolha uma posição válida para a etapa.` e estado igual. :346–354 criação 1/2/3 inclui fim e verifica ordens persistidas dos sobreviventes; :198–199 N=0 aceita 1. `EDITOR:71–72` posição fora de opções alerta exato e zero envio. | PASS |
| ETA-12 | POST 201 com ID novo e configuração persistida 1..N+1 | `API:108/114–124`201/lista 3/IDs antigos/nome Mesmo/setor Operação/ordens 1,2,3/quantidades 1,0,0; `assertThat(novoId).isNotEqualTo(primeira.getId()).isNotEqualTo(ultima.getId())`; associação persistida correto quadro. :351–354 todas posições 1/2/3 verificam ordem de ambos anteriores. `BROWSER:109/115/120/121` IDs positivos/3 distintos; :122–126 `expect(tripla.etapas.map(campos)).toEqual([{id:planejamentoId,nome:"Planejamento",setor:"Técnico",ordem:1,quantidadeDemandas:0},{id:analiseId,nome:"Análise",setor:"Engenharia",ordem:2,quantidadeDemandas:0},{id:execucaoId,nome:"Execução",setor:"Campo",ordem:3,quantidadeDemandas:0}])`; :130 reload lista igual. | PASS |
| ETA-13 | PUT 200/configuração 1..N, renomear/setor/mover preserva vínculos/histórico | `API:128/134–139`200/versão 2/lista ordenada `[novoId,ultimaId,primeiraId]`, ordens 1/2/3, primeira renomeada=`Renomeada`, setor=`Projeto`, quantidade 1; :156 `assertThat(somenteDemandas()).isEqualTo(conteudo)` inclui todas colunas de demanda/comment/history. :183–192 movimento inverso200, `[ultimaId,primeiraId,segundaId]` ordens 1/2/3, nome Movida/setor Projeto/quantidade 1 e vínculo/quadro/conteúdo iguais. `BROWSER:143–158` lista completa exata em ambos sentidos; sobreviventes conservam IDs/campos. | PASS |
| ETA-14 | nomes repetidos preservam IDs e conteúdos independentes | `API:95/106/115–123` anterior e novo nome=`Mesmo`, setores Engenharia/Operação, posições 1/2 e IDs diferentes; :134–154 editar/remover por ID conserva demais conteúdos e :156 vínculos iguais. `BOARD:243–247` duas regiões Planejamento com setores Técnico/Campo, posições 1/2; :253 headings=`["Planejamento","Revisão"]`, :254 rota PUT termina `/etapas/5`. | PASS |
| ETA-15 | DELETE 200 remove só alvo, reordena 1..N-1, conserva históricos; última vazia pode remover | `API:142–156`200/content JSON/lista 2 com IDs última/primeira, ordens 1/2 e nomes/setores/quantidades exatos; `assertThat(etapas.existsById(novoId)).isFalse()` e `assertThat(somenteDemandas()).isEqualTo(conteudo)` conserva históricos anteriores. :206–207200/versão 3/etapas vazias/contagem zero. `ARCH:83–88` IDs sobreviventes e ordens 1/2. `BROWSER:182–185` lista exata só Planejamento/Entrega, IDs conservados e ordens 1/2. | PASS |
| ETA-16 | qualquer demanda bloqueia DELETE 409/mensagem/zero escrita | `API:392` Em andamento/Concluido/Cancelado; :397–399 `status().isConflict()`, `jsonPath("$.erro").value("Esta etapa possui demandas. Mova-as para outra etapa antes de removê-la.")`, estado igual. `BOARD:201–209` ocupado 409 mantém região/confirmação, alerta exato, métodos=`["GET","DELETE","GET"]`, apenas 1 DELETE. | PASS |
| ETA-17 | versão inteira>=0;400/mensagem de ausência/negativa ou formato estrutural, zero escrita | `API:364–365` `status().isBadRequest()`, `jsonPath("$.erro").value(erro)`, estado igual; :361 erro ausente/negativa/nula=`Informe uma versão válida do gerenciamento.`; :368–369 POST/PUT/DELETE  ×  -1/null/ausente/0.1/string 0/true/overflow de long, últimos FORMATO. :423–424 JSON impossível400/FORMATO/estado igual nos 3 métodos. | PASS |
| ETA-18 | versão diferente 409/mensagem exata/zero escrita | `API:378–379` antigo 0 contra atual 1:409/VERSAO/estado igual, precede campos; :387–388 futuro 1 contra atual 0:409/VERSAO/estado igual, ambos 3 métodos. `RACE:130–137` segundo escritor 409/mensagem, lista 1/nome Confirmada/setor Projeto/versão 1. `BOARD:170–185`409 com 3 campos do rascunho conservados, atualização somente GET, próximo PUT manual com versão 7. | PASS |
| ETA-19 | cada confirmação incrementa exatamente uma vez e retorna versão, até edição idêntica | `API:108/128/143` POST/PUT/DELETE versões1/2/3; :158 `containsEntry("versao",3L)` no banco; :199/202/206 mesmo ciclo em N=0 e PUT idêntico versões1/2/3. `BOARD:70` versões nos callbacks=`[0,1,2,3]`. `BROWSER:106/112/118/142/153/180` versões reais1/2/3/4/5/6, arquivo seguinte 7 em:203. | PASS |
| ETA-20 | falha do banco 500/mensagem genérica, rollback config/versão/demandas | `API:409` constraint induz falha de nome ou reordenação para 3 métodos; :412–414 `status().isInternalServerError()`, `jsonPath("$.erro").value("Não foi possível concluir a operação.")`, `assertThat(estado()).isEqualTo(antes)`. Não depende só de mock nem só de contagem. | PASS |
| ETA-21 | disputa remoção/criar/mover não permite demanda órfã; demanda primeiro 409 | `RACE:75` segunda espera TimeoutException até o commit; :78–81 status=`removerPrimeiro ? 404 : 409`, mensagens ETAPA/ocupação exatas; :82–87 alvo existente=`!removerPrimeiro`, contagem de etapas 1 ou 2, versão 1 ou 0, `select count(*) ... where e.id is null`=`0`, contagem de históricos 0 ou 1, contagem de demandas 0 ou 1; :89–90 vínculo origem/destino exato. :95 gera 4 interleavings de criar/mover  ×  primeiro. :159–161 edita demanda para destino removido 404/mensagem, conteúdo igual, versão 1. | PASS |
| ETA-22 | ativo administrável mostra Nova/Editar/Remover; demais estados omitem controles | `BOARD:84–88` três controles têm presença=`permitido` na matriz de criador/admin legado/terceiro/funcionário em legado/arquivo; callback recebe DTO exato e os métodos são somente GET. :268–274 atualização de arquivo/permissão perdida: 3 controles ausentes, rascunho Rascunho mantido e desabilitado, métodos=`["GET","POST","GET"]`. `BROWSER:197–199/210` os 3 controles têm contagem 0 no arquivado, inclusive após recarga. | PASS |
| ETA-23 | Nome/Setor/Posição rotulados, valores atuais e opções válidas | `EDITOR:26–31` `getByLabelText("Nome da etapa").value`=`atual?.nome || ""`, setor=`atual?.setor || ""`, posição=`String(atual?.ordem || 4)`; opções=`atual ? ["1","2","3"] : ["1","2","3","4"]`; foco em Nome. :36 quadro vazio, opções=`["1"]`. `BOARD:53–55` edição mostra Execução/Campo/3; :281–288 legado 7/7 usa posição visual 2/1, setor=`""`, zero escrita. `BROWSER:133–135` valores reais Execução/Campo/3. | PASS |
| ETA-24 | confirmação com nome, aviso de ocupação, Cancelar/Remover | `BOARD:112–117` diálogo=`Remover Execução?`; `getByText(...).textContent`=`Demandas impedem a remoção. Mova-as para outra etapa antes de remover esta coluna.`; foco em Cancelar; :60–61 Remover acionável. `BROWSER:167–177` diálogo nomeia Análise, contém o mesmo aviso e permite Cancelar/Remover por Tab/Enter. | PASS |
| ETA-25 | cancelar envia zero mutações e devolve foco à origem conectada | `BOARD:118–121` formulário/diálogo ausentes, `expect(document.activeElement).toBe(origem)`, métodos=`["GET"]`, matriz criar/editar/remover. `EDITOR:91–92` cancelar chamado 1 vez, salvar nunca chamado. `BROWSER:100–102` Nova focado, versão 0, lista vazia, gravações=`[]`; :164/:172 foco em Editar/Remover; :173–175 conserva 5 gravações, lista igual e versão 5. | PASS |
| ETA-26 | pendência bloqueia reenvio e saída do formulário | `EDITOR:100–106` salvar chamado exatamente 1 vez com `{nome:"Planejamento",setor:"Técnico",ordem:2}`; 3 campos e Salvar desabilitados, cancelar não chama callback; após resolver, Salvar é habilitado. `BOARD:131–137` POST/PUT: Voltar/Cancelar desabilitados, 1 requisição, voltar nunca chamado; :146–153 DELETE: controle desabilitado, 1 requisição, navegação bloqueada. `APP:40–61` POST/PUT/DELETE  ×  sucesso/falha: Sair desabilitado, token=`sessao`, usuário igual, formulário conservado, 1 requisição; depois Sair habilitado, logout funciona e remove token/usuário. :68–72 GET pendente permite Sair. Escopo testado: navegação da aplicação Voltar/Cancelar/Sair; sem evidência de recarga/voltar/fechar do navegador durante a pendência. | PASS no escopo explícito das tarefas |
| ETA-27 | erro da API/comunicação em alerta; preserva campos; sem sucesso ou retry se falha/resultado não confirmado | `EDITOR:120–125` mensagens 403/409/500/rede exatas em região de alerta; nome Rascunho/setor Engenharia/posição 2; 1 chamada de salvar e callback aoErro(error) exato. `BOARD:170–185` 409/rede: alerta, 3 campos preservados e recuperação somente por GET; :201–209 DELETE: 403/404/409/500/rede conserva diálogo/coluna e 1 DELETE; `CLIENT:139–149` três métodos rejeitam 409 com mensagem/status ou comunicação com status 0, e fazem 1 fetch. **Sem assertion para 2xx com JSON ilegível/truncado**: `CLIENT:91–95` usa 500, não o ramo `frontend/src/services/api.js:33`. M10 sobrevive aos 137 testes. | GAP parcial |
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
| `API:247` rotasExigemSessaoAntesDeJSON | 10 | ETA-06;5 rotas × 2 sessões |
| `API:263` quadroAusenteRetorna404 | 5 | ETA-08;5 rotas |
| `API:275` etapaAusenteOuDeOutroQuadroRetorna404SemTransferencia | 4 | ETA-08;PUT/DELETEausente/estrangeiro |
| `API:288` validaNomeSetorUTF-16SemEscrever | 24 | ETA-09/10;2 métodos × 2 campos × 6 limites |
| `API:309` aceitaLimitesNormalizadosWhitespaceJS | 4 | ETA-09/10;1/255 UTF-16JS |
| `API:323` posicaoExigeInteiroEIntervalo | 18 | ETA-11;2 métodos × 9 formatos/limites |
| `API:337` editarNaoAceitaPosicaoNMaisUm | 1 | ETA-11;limitesPUT |
| `API:347` criaEmTodasAsPosicoesInclusiveUltima | 3 | ETA-12;início/meio/fim |
| `API:359` versaoEstritaRecusaSemEscrever | 21 | ETA-17;3 métodos × 7 formatos/limites |
| `API:374` versaoAntigaConflitaAntesDosCampos | 3 | ETA-18;precedência de versão |
| `API:384` versaoFuturaTambemConflitaSemEscrever | 3 | ETA-18;diferença em ambos os sentidos |
| `API:393` ocupadaNaoRemoveIndependentementeDoStatus | 3 | ETA-16;3 status |
| `API:404` falhaDeBancoReverteEtapasOrdemVersaoEConteudo | 3 | ETA-20;3 métodos |
| `API:420` jsonImpossivelRetorna400SemMutacao | 3 | ETA-17;formato estrutural/edge de zero escrita |
| `RACE:41` removerDisputaComCriarOuMoverSemOrfaoEHistoricoParcial | 4 | ETA-21;ambas as ordens de criar/mover |
| `RACE:98` duasConfiguracoesComVersaoAntigaNaoSobrescrevem | 1 | ETA-18/19 |
| `RACE:103` snapshotEsperaCommitECombinaVersaoComEtapasAtuais | 1 | ETA-01/02;consistência do snapshot |
| `RACE:151` edicaoDeDemandaComDestinoRemovidoReverteCamposEHistorico | 1 | ETA-21;destino removido |
| `ARCH:39` arquivadoBloqueiaTresRotasAteParaAdminEPreservaConsulta, adaptado | 3 | ETA-03/07; T1 preservação dos anteriores |
| `ARCH:64` ativoPermiteCriacaoEdicaoExclusaoPeloCriadorComPosicoesValidas, adaptado | 1 | ETA-04/12/13/15/19;contrato substituído |
| `ARCH:92/97` disputas arquivo/estrutura, adaptadas | 2 | Edge spec: arquivamento primeiro 409/estrutura primeiro, conflito com versão antiga; ETA-07/18/19 |
| `CLIENT:116` GET estrutura | 1 | ETA-01/29; T2 rota/token/signal |
| `CLIENT:125` POST/PUT/DELETE payload/snapshot | 3 | ETA-12/13/15/28; T2 |
| `CLIENT:136` erro HTTP sem reenvio | 3 | ETA-27/29; T2 |
| `CLIENT:143` rede ambígua sem reenvio | 3 | ETA-27; T2 |
| `EDITOR:24` criar/editar/setor nulo | 3 | ETA-23;legado |
| `EDITOR:34` vazio, opção 1 | 1 | ETA-23;N=0 |
| `EDITOR:49` campos inválidos | 8 | ETA-09/10/27 |
| `EDITOR:59`1/255 normalizados | 2 | ETA-09/10/23 |
| `EDITOR:67` posição inválida | 1 | ETA-11 |
| `EDITOR:75` teclado nos campos/salvar | 1 | ETA-23/30 |
| `EDITOR:88` teclado Cancelar | 1 | ETA-25/30 |
| `EDITOR:95` pending | 1 | ETA-26 |
| `EDITOR:114`403/409/500/rede | 4 | ETA-27 |
| `BOARD:37` CRUD, snapshots/IDs/versões | 1 | ETA-12/13/15/19/28/30 |
| `BOARD:81` roles/state | 5 | ETA-03/22 |
| `BOARD:96` contagem ocupada/vazia | 1 | ETA-01; T4 |
| `BOARD:107`3 cancelamentos | 3 | ETA-24/25/30 |
| `BOARD:124/140`POST/PUT/DELETE pendentes | 3 | ETA-26 |
| `BOARD:159`409/rede e retry manual | 2 | ETA-18/27/29/30 |
| `BOARD:195`DELETE 409/403/404/500/rede | 5 | ETA-16/27/29 |
| `BOARD:212`atualização falha | 1 | ETA-27/29/30 |
| `BOARD:238`repetidos | 1 | ETA-14/22/28 |
| `BOARD:257`atualização de flag arquivo/permissão | 2 | ETA-22/27/29 |
| `BOARD:277`legado 7/7 | 1 | ETA-02/23 |
| `BOARD:291`última vazia | 1 | ETA-15/29 |
| `BOARD:300`cache de arquivamento | 1 | ETA-28 |
| `APP:21`3 métodos × sucesso/falha | 6 | ETA-26; T6 |
| `APP:65`GET permite saída | 1 | T6 Done when; ETA-26 limite |
| `BROWSER:47`CRUD real e layout/arquivo | 1 | ETA-12/13/15/22/24/25/28/30; T5 |
| `frontend/src/pages/Quadro.test.jsx:32`, adaptado | 1 | ETA-03/22;preservação de fixture T4 |
| `frontend/src/pages/Quadro.test.jsx:47`, adaptado | 3 | ETA-27/29;erros iniciais exatos |
| `frontend/src/pages/Quadro.test.jsx:63`, adaptado | 1 | ETA-29/30;endpoint GET/teclado |
| `frontend/src/pages/Gerenciamentos.test.jsx:25–27`, fixture adaptada | testes anteriores:57/:221/expansão abrir:318 | T4 preservação de assertions;novo GET de snapshot |

Novos Java: API134+RACE7=141. ARCH 6 continuam. Novos Vitest: CLIENT10+EDITOR22+BOARD27+APP7=66. E2E antigo não mudou suas assertions; novo wrapper isola dados por arquivo. Fixture Gerenciamentos acrescenta rota sem retirar assertions. Testes não alterados do restante do repositório são regressão executada, fora do reverse mapping do diff.

## Discrimination sensor expandido

Cada fault foi aplicado sozinho em fontes scratch e restaurado byte a byte no finally antes do seguinte. Nenhum teste foi alterado. Não houve git stash. Falha de compilação não foi usada como kill: todos os dez faults chegaram a execução de testes compilados. A primeira coleta M1 teve problema de decoding do log; a fonte foi restaurada no finally e a execução/coleta M1 foi refeita, não contabilizada como fault adicional.

Nos comandos Java abaixo `mvn.cmd -B verify -Dtest=...` usa o Full gate targeted. UI usa Quick gate targeted ou completo. Logs físicos M1…M10 preservam command/exit/outcome; o JSON também os registra. Relatórios XML de outras classes antigas não foram usados como kill: para M7 somente seus 13 casos novos da execução são contados.

| Fault | Fonte real e alteração compilável | Comando | Assertion que matou / outcome | Exit |
| --- | --- | --- | --- | --- |
| M1 autorização | `sistema/src/main/java/com/patp/sistema/service/EtapaService.java:111`, remove `guard.exigirAdministracao(gerenciamento,usuario)` | `mvn.cmd -B verify -Dtest=EtapaApiTests#terceiroOuSemCriadorRecusaTodaMutacaoAntesDeCampos` | KILLED 12/12: `API:219`403 exigido, recebido 400 ou 409 | 1 |
| M2 arquivado | EtapaService.java:112, remove `guard.verificarAtivo(gerenciamento)` | `mvn.cmd -B verify -Dtest=EtapaApiTests#criadorOuAdminNaoMutaArquivadoNemVersao` | KILLED 6/6: `API:237`409 exigido, recebido 201/200 | 1 |
| M3 ordenação | EtapaService.java:130, `i+1` vira `i+2` | `mvn.cmd -B verify -Dtest=EtapaApiTests#cicloCrudPosicoesNormalizaLegadoPreservaConteudoEUsaIdsGerados+edicaoDaUltimaParaPrimeiraReorganizaTodosEPreservaIdEVinculo` | KILLED 4/4: `API:115/185` `jsonPath("$.etapas[0].ordem").value(1)`, recebeu 2 | 1 |
| M4 versão antiga | EtapaService.java:165, `if(false && !Objects.equals(...))` desativa comparação | `mvn.cmd -B verify -Dtest=EtapaApiTests#versaoAntigaConflitaAntesDosCampos+versaoFuturaTambemConflitaSemEscrever` | KILLED 6/6: `API:378/387`409 exigido, recebido 400/201/200 | 1 |
| M5 incremento de versão | EtapaService.java:134, remove lock FORCE_INCREMENT | `mvn.cmd -B verify -Dtest=EtapaApiTests#vazioCriaNaPosicaoUmEditaIdenticoIncrementaUmaEPermiteRemoverUltima` | KILLED 1/1: `API:199` versão 1 exigida, recebeu 0 | 1 |
| M6 ocupação | EtapaService.java:98, `if(false && processos.existsByEtapaId(etapaId))` | `mvn.cmd -B verify -Dtest=EtapaApiTests#ocupadaNaoRemoveIndependentementeDoStatus` | KILLED 3/3: `API:397`409 exigido, recebeu 500 da FK; fault semântico compilou | 1 |
| M7 serialização | EtapaService.java:110, `guard.bloquear(id)` vira `entityManager.find(Gerenciamento.class,id)` | `mvn.cmd -B verify -Dtest=EtapaDemandConcurrencyTests,EtapaArchiveTests` | KILLED 4/13: `ARCH:133` espera de TimeoutException ausente; `RACE:78` demanda primeiro 409 recebeu 200 em 2 casos; :130409 recebeu 500 | 1 |
| M8 UI versão | `frontend/src/pages/Quadro.jsx:86`, payload com versão atual vira 0 | `npm.cmd test -- src/pages/QuadroEtapas.test.jsx` | KILLED 3/27: `BOARD:64–69` payload PUT com versão 1 e :184–185 retry manual com versão 7 diferem | 1 |
| M9 UI saída | Quadro.jsx:82/:102, `aoOcupar?.(true)` vira false nas 2 mutações | `npm.cmd test -- src/App.test.jsx` | KILLED 6/7: `APP:40` `expect(Sair.disabled).toBe(true)` recebeu false em 3 métodos  ×  2 outcomes | 1 |
| M10 resultado não confirmado | `frontend/src/services/api.js:86`, wrapper POST captura ApiError com status 201 e devolve snapshot inventado `{gerenciamento:{id,versao:dados.versao+1,podeAdministrar:true,arquivado:false},etapas:[]}` | `npm.cmd test` completo | **SURVIVED 137/137**. Nenhuma assertion de 2xx com JSON ilegível/truncado; sem erro de compilação. Sucesso falso descartaria rascunho/colunas se esse caso ocorresse. | 0 |

M10 foi implementado como `.catch(error => { if (error.status === 201) return { gerenciamento: { id, versao: dados.versao + 1, podeAdministrar: true, arquivado: false }, etapas: [] }; throw error; })` no wrapper novo. O parser real em api.js:33 já lança mensagem de comunicação em 2xx ilegível. O fault introduz exatamente falso resultado confirmado no ramo sem teste, mantendo todos os demais outcomes. Uma nova assertion derivada de ETA-27 deve matá-lo; não requer mudar o cliente real.

## Isolamento comprovado

Scratch absoluta validada dentro TEMP: `C:/Users/Marco/AppData/Local/Temp/etapas-verifier-892a061698bd4112b4cad0673e23bdbf/scratch`. Criada com `git worktree add --detach <scratch> 9a19344`. Baseline completo `git status --porcelain=v1 --untracked-files=all` do real foi capturado antes de setup/sensor:3403 linhas. Depois: fontes scratch restauradas (`git diff --exit-code -- sistema/src/main frontend/src` exit 0), nenhum processo java/node próprio restante, portas 18082/4173 livres, worktree removida com caminho absoluto validado em TEMP. Não foram encerrados processos de outros.

Antes de escrever este relatório e lições, a comparação byte a byte dos arquivos serializados em UTF-8 de porcelain antes/depois passou. SHA-256 de ambos: `D2228C50E42491119DEAE1F3F63786107EC3DA1F7AEEA362DE791801692C6A9A`;3403 linhas em ambos. Paths físicos: `real-porcelain-before.txt` e `real-porcelain-after.txt` na pasta TEMP do verificador. Artefatos antigos modified/untracked node_modules/target/dist/test-results do real foram preservados sem limpeza/restore. `git worktree list` voltou a somente o real em 9a19344. Nenhum app normal, credencial real, produção, conexão MySQL configurada, push/deploy foi usado.

## Qualidade, edges e limites

- Perímetro backend: controller/DTOs/consulta ordenada/contagem agrupada/serviço transacional/guarda administrativa/reuso do DTO do quadro e traduções404destino removido. Todas mudanças são necessárias a T1 e à integridade ETA-21; não foi alterada permissão funcional de demandas/comentários.
- Perímetro UI: wrappers/formulário/quadro/callback de cache/pendência global/CSS/tests e runner E2E. Reusa padrões existentes, não acrescenta biblioteca/dragdrop/categoriafinal. Arquivos de evidência/STATE/contratos documentais estão no escopo; nenhuma refatoração não relacionada encontrada.
- Guia de testes encontrado: `docs/OPERACAO.md:3/:19/:54`, H2 isolado, Edge e MySQL descartável. Nenhum AGENTS/README/contributing adicional encontrado por rg. Princípios da skill aplicados.
- Domain mapping 1:1 e rotas API happy+edge+error cobertos pelos 134 casos API/7 RACE/6 ARCH. integração UI cobre happy+erro+edge em todas as 3 mutações e GET; gap ETA-27 acima recebe zero evidência, não um PASS substituto.
- Edges:1/255 aceitos;0/whitespace/256/UTF-16emoji recusados; N=0/última vazia; legado com gaps/empates, somente consulta; normaliza ordem só na mutação; setor legado não preenchido; vínculos/histórico, campos preservados; nomes repetidos por ID;ID estrangeiro404; stale/rollback integral; disputa de estrutura/demanda/arquivo em ambas as ordens. `ARCH:137–151` também prova arquivamento confirmado primeiro 409 e estrutura primeiro, conflito antigo, seguido arquivo versão 1 e versão final 2.
- Browser E2E cumpre escopo explícito T5:CRUD/posições/IDs/reload/cancelamento/teclado/cache de arquivamento/readonly. Seus ramos negativos de API/pending são exercitados por RTL, não por Playwright. Não se afirma erro de todas as rotas no browser. Navegação reload/back/close do browser durante pendência não foi implementada/testada; critérios de tarefa explicitam controles da aplicação.
- Oito capturas próprias de E2E foram inspecionadas: `C:/Users/Marco/AppData/Local/Temp/creral-etapas-e2e-7d1a8G/{ativo,editor,confirmacao,arquivado}-{desktop,mobile}.png`. Labels, selects, ações e avisos legíveis em 1280 × 800/375 × 812, sem corte horizontal. Assertions físicas `BROWSER:39/:42` exigem `scrollWidth <= clientWidth`. Isso é QA automatizada/inspeção do verificador, sem aceite humano.
- T1/T2/T3/T4/T6/T5 estão marcadas done com gates locais; conclusão independente da feature permanece bloqueada por ETA-27. Não alterei status na spec/tasks/STATE por contrato de edição exclusiva do verificador.

## Gap ranqueado e tarefa de correção

1. **Major de cobertura, ETA-27/M10:** cliente 2xx com body ilegível/truncado não discriminado. Origem física: `frontend/src/services/api.js:31–33`; wrappers novos:86/90/94; ausência de teste nos grupos `CLIENT:125/:136/:143`, JSON malformado preexistente `CLIENT:91` usa 500. Não foi demonstrado bug no cliente real.

**Fix V1:** acrescentar testes derivados para POST 201/PUT 200/DELETE 200 com body JSON ilegível/truncado. Em CLIENT, exigir rejeição com mensagem exata `Não foi possível confirmar a operação. Atualize a lista antes de tentar novamente.`, status original 201/200, fetch count 1. Em BOARD, exercitar os mesmos resultados: alerta exato acessível, Nome/Setor/Posição preservados para POST/PUT ou confirmação/coluna preservadas para DELETE; sem sucesso/cache inventado/retry automático; recuperação somente por GET e próximo envio somente manual com versão reconsultada. Conservar testes existentes e cliente já correto. Done when: Quick/Build passam e M10 morto por assertion desses outcomes. Re-verificação independente após commit do corretor, limitada pela skill.

**Lição:** L-002 registrada pelo script `lessons.py`, status candidate, recorrência 1, fonte M10/ETA-27. Regra: testar respostas 2xx com JSON ilegível e exigir erro de comunicação, rascunho preservado e nenhum reenvio automático. L-001 permanece candidate, sem promoção.

**Completion gate executado:** `python .agents/skills/tlc-spec-driven/scripts/validate_state.py etapas` retornou exit 1: `validation.md verdict is FAIL - route the ranked gaps to fix tasks, then re-verify (feature is not done)`. O parser reconheceu o FAIL correto, sem placeholder. A feature permanece pendente da correção V1/T7 e re-verificação independente.
