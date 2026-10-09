# Validation: demandas-arquivamento — PASS

**Veredicto:** 30/30 critérios e 10/10 bordas atendidos. Gates independentes aprovados, 11/11 mutações detectadas, árvore real preservada. Nenhuma lacuna de implementação ou evidência encontrada. **Aceitação humana pendente.**

**Data do relatório:** 09/10/2026. **Execuções:** 08/10/2026; resultados e isolamento reconferidos em 09/10, sem mudança de HEAD. **Verifier:** agente independente `/root/verifier_arquivamento`, autor ≠ verifier. **Spec:** `.specs/features/demandas-arquivamento/spec.md`. **Diff:** `3f9d78a..4f977ea`, branch `testes`, HEAD `4f977ea`. Superfície conferida por `git diff --name-only ... -- sistema/src frontend/src frontend/e2e`: 27 arquivos.

O Verifier leu integralmente a spec, tasks/matriz/gates, TLC SKILL.md, validate.md, sub-agents.md, coding-principles.md, lessons.md, docs/OPERACAO.md, configurações de teste, diff e testes em escopo. Os resultados abaixo derivam das assertions e das execuções próprias. Logs do autor foram usados somente para localizar runtime e nomes de classes, sem herdar o veredicto.

## Critérios e resultados definidos na spec

Os helpers de recusa verificam **status, mensagem, conteúdo físico e quadro/versão em conjunto**:

- `sistema/src/test/java/com/patp/sistema/DemandaArquivoApiTests.java:28`: `status().is(codigo)` e `jsonPath("$.erro").value(mensagem)`; linha29 compara `conteudoPersistido()` e `select * from gerenciamentos order by id` aos snapshots anteriores.
- `sistema/src/test/java/com/patp/sistema/DemandaArquivoServiceTests.java:24`: `getStatus().value().isEqualTo(codigo)` e `getMessage().isEqualTo(mensagem)`; linha25 compara conteúdo físico e todos os quadros.
- `sistema/src/test/java/com/patp/sistema/ApiIntegrationSupport.java:78`: o snapshot físico inclui todas as linhas/campos de etapas, processos, comentários e históricos; os quadros são comparados separadamente pelos helpers.

| AC | Resultado preciso da spec | Arquivo:linha e assertion conferida | Resultado |
| --- | --- | --- | --- |
| ARQ-01 | Duas ações: sessão ausente/inválida/encerrada →401, sem efeitos | `sistema/src/test/java/com/patp/sistema/DemandaArquivoApiTests.java:60` define401 para ausência; linhas61–62 definem inválida/encerrada; linha74 chama `erro(...,codigo,mensagem)` com assertions28–29; linha75 cruza ambos endpoints | PASS |
| ARQ-02 | Funcionário externo →403, sem efeitos | `sistema/src/test/java/com/patp/sistema/DemandaArquivoServiceTests.java:60`: `recusa(...,403,...,"Você não tem permissão para administrar este gerenciamento.")`, duas ações e snapshots24–25. `frontend/e2e/arquivamento.spec.js:127`: `expect(negada.status()).toBe(403)` e `expect(await c.consultar()).toEqual(arquivo)` | PASS |
| ARQ-03 | Quadro arquivado →409, `Gerenciamento arquivado. Restaure-o antes de alterar.`, sem efeitos | `sistema/src/test/java/com/patp/sistema/DemandaArquivoServiceTests.java:61`: `recusa(...,409,...,mensagem exata)` nas duas ações. `frontend/e2e/arquivamento.spec.js:133`: `toBe(409)`, mensagem literal e snapshot igual | PASS |
| ARQ-04 | Sem criador somente admin; não atribuir criador | `sistema/src/test/java/com/patp/sistema/DemandaArquivoApiTests.java:91`: `status().isOk()`, `$.gerenciamento.criador.isEmpty()`, versão1 e permissão true; linha92 recusa403; linha93 `getCriador().isNull()`, quatro combinações ação×papel | PASS |
| ARQ-05 | Arquivar os três estados preserva todos os dados; muda só marcador | `sistema/src/test/java/com/patp/sistema/DemandaArquivoServiceTests.java:39`: mapa físico completo `isEqualTo(esperado)` com só arquivado substituído; linha41 `r.demandasArquivadas().containsExactly(dto)` e ativa vazia. Linhas30–31 incluem datas incoerentes, espaços e observações/motivo de20k. API `DemandaArquivoApiTests.java:47` compara alvo e demais demandas fisicamente | PASS |
| ARQ-06 | Restaurar os três estados na mesma etapa/status, sem reabrir | `sistema/src/test/java/com/patp/sistema/DemandaArquivoServiceTests.java:36` percorre true/false; linha39 mapa físico exato e linha41 `r.demandas().containsExactly(dto)`. `frontend/e2e/arquivamento.spec.js:108`: `atual.demandas.toEqual([c.demanda])` e `status.toBe(status)` | PASS |
| ARQ-07 | null/vazio/desconhecido →409, mensagem de status antigo, sem efeitos/normalização | `sistema/src/test/java/com/patp/sistema/DemandaArquivoApiTests.java:85`: `erro(...,409,"Status antigo não reconhecido. Solicite a correção do registro.")`, `getStatus().isEqualTo(status)`; linha87 cruza duas rotas com null, vazio, espaços, acento e Antigo | PASS |
| ARQ-08 | Repetição →409 e mensagem específica, sem evento/incremento | `sistema/src/test/java/com/patp/sistema/DemandaArquivoServiceTests.java:73`: `recusa(...,409,...,arquivar?"A demanda já está arquivada.":"A demanda já está ativa.")`; helper24–25 compara todos os registros e quadros. API linha72/74 e matriz75 prova ambos endpoints | PASS |
| ARQ-09 | Versão ausente/null/negativa →400, `Informe uma versão válida do gerenciamento.`, sem efeitos | `sistema/src/test/java/com/patp/sistema/DemandaArquivoServiceTests.java:78`: `recusa(...,invalida?400:409,...,invalida?mensagem versão válida:mensagem conflito)`; matriz80 cruza null/-1/1 com as duas ações. API `DemandaArquivoApiTests.java:68` e linhas69–70 definem {},null,-1; assertions28–29 via linha74 | PASS |
| ARQ-10 | Tipo/fração/overflow/extras/JSON inválido →400, `Dados da requisição inválidos.`, sem efeitos | `sistema/src/test/java/com/patp/sistema/DemandaArquivoApiTests.java:79`: `erro(...,400,"Dados da requisição inválidos.")`; linha81 cruza duas rotas com texto/bool/objeto/array/0.5/0e0/overflow, extras autor/status/arquivo/etapa, JSON quebrado e corpo ausente | PASS |
| ARQ-11 | Versão diferente →409, mensagem de atualização, sem efeitos | `sistema/src/test/java/com/patp/sistema/DemandaArquivoServiceTests.java:78`: status409/mensagem literal para versão1 versus0, mapa inteiro intacto. `DemandaArquivoConcurrencyTests.java:36`: `conflito(segunda,"Gerenciamento alterado por outro usuário. Atualize e tente novamente.")`; linha37 compara físico/quadro após primeiro commit | PASS |
| ARQ-12 | Demanda inexistente/externa ao quadro →404, `Demanda não encontrada neste gerenciamento.`, sem efeitos | `sistema/src/test/java/com/patp/sistema/DemandaArquivoServiceTests.java:84`: duas chamadas `recusa(...,404,...,mensagem exata)`; parametrização nas duas ações. API `DemandaArquivoApiTests.java:65` e linha66 definem os dois alvos; linha74 helper e linha75 cruza rotas | PASS |
| ARQ-13 | Sucesso →200, snapshot completo, versão+1 uma vez | `sistema/src/test/java/com/patp/sistema/DemandaArquivoApiTests.java:40`: `status().isOk()`; linha42 quadro inteiro igual ao `GerenciamentoResponse(...,versao+1,true)`; linhas43–45 listas/etapas exatas; linha49 JSON de PUT igual ao GET. `frontend/e2e/arquivamento.spec.js:69`–74 confirma HTTP200, payload só versão, incremento1 e listas/contagens | PASS |
| ARQ-14 | Um evento por ação, tipo/descrição literais, autor real, data/hora | `sistema/src/test/java/com/patp/sistema/DemandaArquivoServiceTests.java:45`: `containsEntry("usuario",autor.getNome())`, descrição e processo_id; linha46 `data_hora.isBetween(inicio,now+1)`; linha47 count2/3 com evento antigo. `DemandaArquivoApiTests.java:50`–52 count=versao+1 e autoria/horário, seis combinações estado×criador/admin | PASS |
| ARQ-15 | Falha em demanda/evento/versão reverte tudo nas duas ações | `sistema/src/test/java/com/patp/sistema/DemandaArquivoServiceTests.java:91`: `assertThatThrownBy(...).isInstanceOf(RuntimeException.class)` sob CHECK; linha92 conteúdo inteiro/quadro `isEqualTo(antes)`; matriz86 cruza três escritas×duas ações. PASS em H2 e MySQL | PASS |
| ARQ-16 | Lock ordena edição/transição/comentário/quadro; incompatível recusa, sem parcial | `sistema/src/test/java/com/patp/sistema/DemandaArquivoConcurrencyTests.java:34`,49,69: `segunda.get(200ms)` lança TimeoutException enquanto primeiro retém lock; linhas52–56 e71–76 verificam recusa/sucesso físico, ciclo, versão e eventos nas duas ordens e ações. `DemandaArquivoComentarioTests.java:50`–59 confirma espera/visibilidade/409 e valores de comentário/processo/versão | PASS |
| ARQ-17 | Número reservado globalmente; comentários/eventos anteriores literais | `sistema/src/test/java/com/patp/sistema/DemandaArquivoServiceTests.java:97`: `recusa(criar em outro quadro com reserva-aBC,409,"Já existe uma demanda com esse número.")`; linha43 registros anteriores exatos. `frontend/e2e/arquivamento.spec.js:78`: comentários completos iguais; linhas82/106 histórico anterior completo igual | PASS |
| ARQ-18 | null legado ativo sem gravar; novos ativos; JSON não controla flag | `sistema/src/test/java/com/patp/sistema/DemandaArquivoModeloTests.java:24`–28: boolean/JSON `isEqualTo(Boolean.TRUE.equals(valor))` e mapa físico só marcador; `DemandaArquivoLeituraTests.java:27`,31–32 coloca null na lista ativa e mantém físico/versão. `DemandaArquivoModeloTests.java:34`–36: payload true/false e `new Processo()` continuam false. `DemandaArquivoLegadaTests.java:40`–41 POST true responde/persiste false | PASS |
| ARQ-19 | GET/mutações devolvem listas completas disjuntas ordenadas ID, mesmo14 campos | `sistema/src/test/java/com/patp/sistema/DemandaArquivoApiTests.java:43`–44: `containsExactlyElementsOf(...)` nas duas listas com ativo/arquivo/alvo e ordem; linha110 arquivo inteiro em seis mutações. `DemandaArquivoLeituraTests.java:27`–28 listas exatas; linha41 estrutura criar/editar/excluir devolve ambas. `ConfiguracaoArquivoResponseTests.java:19`–22 lista de chaves exata e DTO por valor | PASS |
| ARQ-20 | Contagens só ativas | `sistema/src/test/java/com/patp/sistema/DemandaArquivoLeituraTests.java:29`: `quantidadeDemandas.containsExactly(2L,0L)` com null/false/true; `DemandaArquivoApiTests.java:45`,113 cada contagem exata em ações e mutações reusadas. `frontend/src/services/apiArquivo.test.js:52`: divergência de contagem rejeitada nas duas ações | PASS |
| ARQ-21 | Etapa só com arquivo →409 e mensagem de demandas, referência intacta | `sistema/src/test/java/com/patp/sistema/DemandaArquivoApiTests.java:120`: `status().isConflict()`, `erro.value("Esta etapa possui demandas. Mova-as para outra etapa antes de removê-la.")`; linha121 conteúdo/quadro igual. `DemandaArquivoLeituraTests.java:48`–49 repete serviço e versão0 | PASS |
| ARQ-22 | Edição/transições/comentário/histórico manual, atuais/antigos →409 e mensagem de arquivo, sem efeitos | `sistema/src/test/java/com/patp/sistema/DemandaArquivoLegadaTests.java:21`–22: nove rotas, `status().isConflict()`, mensagem literal e físico/quadro igual. `DemandaArquivoComentarioTests.java:40`–41 POST409+físico; `DemandaArquivoHistoricoApiTests.java:21`,25 POST409 inclusive ação administrativa, físico/quadro igual | PASS |
| ARQ-23 | DELETE autorizado →409, `Exclusão definitiva indisponível. Use o arquivamento da demanda.`, sem apagar/evento | `sistema/src/test/java/com/patp/sistema/DemandaArquivoLegadaTests.java:27`–28: `status().isConflict()`, mensagem literal e dois snapshots iguais para ativa/arquivo. `ProcessoArchiveTests.java:129`–131 mantém recusa/físico e acrescenta versão anterior igual | PASS |
| ARQ-24 | Colunas ativas e Arquivadas com dados completos, consulta por outros | `frontend/src/pages/QuadroArquivo.test.jsx:34`–38: textos/datas/status/motivo/etapa/observações, nenhum article na coluna e count0; linha58 sem ações conserva informações. `frontend/e2e/arquivamento.spec.js:77`–79 área correta/snapshot/recarga; linha124 terceiro lê Externa sem botões | PASS |
| ARQ-25 | Ações só criador/admin em quadro ativo e estado reconhecido; demais consulta | `frontend/src/pages/QuadroArquivo.test.jsx:42`–53 cruza duas ações×três estados; linha58 `within(card).queryByRole("button").toBeNull()` para permissãofalse/quadroarquivo/statusnull/vazio/antigo. API `DemandaArquivoApiTests.java:32` e linha89 cobrem criador/admin/sem criador. E2E linhas124/130/136 cobrem funcionário, quadro arquivado e ação novamente disponível | PASS |
| ARQ-26 | Confirma número/cliente/efeito; restauração etapa/status; cancelar sem envio e foco existente | `frontend/src/pages/AcaoArquivamentoDemanda.test.jsx:17`–23: heading/cliente/efeito literal, foco Cancelar, sem campos/destino; linha27 `salvar.not.toHaveBeenCalled()`. `QuadroArquivo.test.jsx:63`: diálogo null, foco no botão anterior, métodos sóGET. E2E `arquivamento.spec.js:62`–66 confirma teclado/efeito | PASS |
| ARQ-27 | Pending impede duplicação/fechamento/alterações/saída | `frontend/src/pages/AcaoArquivamentoDemanda.test.jsx:36`–39: botões disabled/aria-busy true, submit repetido `salvar.toHaveBeenCalledTimes(1)`, cancelar não chamado. `QuadroArquivo.test.jsx:68`–73: todos os botões disabled, Voltar não chamado, fetch2, `aoOcupar.toHaveBeenCalledExactlyOnceWith(true)` e retorno false. Saída global reusa contrato provado em `frontend/src/App.test.jsx:40`–43: Sair disabled e token mantido; wiring `App.jsx:158`,168 e `Gerenciamentos.jsx:138` conferido | PASS |
| ARQ-28 | Só200, versão+1, alvo transferido com14 campos iguais, snapshot válido; cache/foco/contagens/fechamento | `frontend/src/services/apiArquivo.test.js:23`–27: `executar(...).toEqual(r)`, URL/método/header/body exatos; linha52 `rejects.toMatchObject({status:200,message:mensagem})` após divergir cada campo/lista/id/quadro/permissão/contagem/versão; linha56 rejeita201/204. `QuadroArquivo.test.jsx:46`–53: diálogo null, destino/count, cache versão3 e foco conectado | PASS |
| ARQ-29 | HTTP/rede/não confirmado permanece; GET manual obrigatório, sem retry; novo envio só manual | `frontend/src/services/apiArquivo.test.js:60`–65: seis HTTPs e rede nas duas ações, mensagem/status e fetch1. `QuadroArquivo.test.jsx:83`–89: erro persiste, envio disabled antes GET, métodos GET/PUT/GET e depois PUT comversao4. Linhas99–101: GET inelegível impede submit e permite cancelar. E2E `arquivamento.spec.js:118`–123 conflito e GET separado | PASS |
| ARQ-30 | Troca quadro descarta confirmação e ignora sucesso/erro tardio | `frontend/src/pages/QuadroArquivo.test.jsx:108`–111: rerender quadro10, diálogo null, resposta antiga200/409; headingOutro, nenhum article/alert, `atualizar.mock.calls.some(q.id===9&&q.versao===3).toBe(false)`, ambas ações | PASS |

**Resultado:** 30/30 PASS, sem spec-precision gap. Referências a helpers/linhas adjacentes acima foram conferidas nas fontes, não inferidas das tags ARQ.

## Todas as bordas da spec

| Borda | Resultado e assertion exata | Resultado |
| --- | --- | --- |
| Null/false ativo; true arquivo; datas/motivos longos/criador/status antigo literais | `sistema/src/test/java/com/patp/sistema/DemandaArquivoModeloTests.java:24`–28: `isEqualTo(Boolean.TRUE.equals(valor))` e mapa literal; `DemandaArquivoServiceTests.java:39`–40 mapa inteiro/criador original; `DemandaArquivoApiTests.java:85` `getStatus().isEqualTo(status)` | PASS |
| Repetição sem evento; restaurar encerrada não reabre | `sistema/src/test/java/com/patp/sistema/DemandaArquivoServiceTests.java:73` recusa409 com físico/quadro iguais pelo helper25; `frontend/e2e/arquivamento.spec.js:108` demanda inteira igual e status original nos três estados | PASS |
| Número com caixa diferente reservado em todos os quadros | `sistema/src/test/java/com/patp/sistema/DemandaArquivoServiceTests.java:97` recusa409 em outro quadro; `frontend/e2e/arquivamento.spec.js:89` `duplicada.status().toBe(409)`, mensagem e snapshot igual | PASS |
| Etapa zero ativas+uma arquivada protegida; área explica restaurar/mover | `sistema/src/test/java/com/patp/sistema/DemandaArquivoLeituraTests.java:48`–49: status409/mensagem/físico/versão0; `frontend/src/pages/QuadroArquivo.test.jsx:37`–38 count0 e orientação literal | PASS |
| GET quadro arquivado mostra ambas; restaurar quadro mantém arquivo | `sistema/src/test/java/com/patp/sistema/DemandaArquivoLeituraTests.java:27`–32 listas/flag exatos, físico/versão iguais; linhas55–56 arquivo exato/ativa vazia/físico igual/versão2; `frontend/e2e/arquivamento.spec.js:131`,135 listas iguais após ação de quadro | PASS |
| JSON dedicado sóversao, strict TransicaoVersaoRequest | `sistema/src/test/java/com/patp/sistema/DemandaArquivoApiTests.java:79`–81: `erro(...,400,"Dados da requisição inválidos.")` para cada tipo/campo extra; `frontend/src/services/apiArquivo.test.js:26` `JSON.parse(req.body).toEqual({versao:2})` nas duas ações | PASS |
| Evento interno permitido; POST manual forjado não contorna | `sistema/src/test/java/com/patp/sistema/DemandaArquivoHistoricoTests.java:37`–43: evento ADMINISTRATIVO por valor, sequência exata e versão2; `DemandaArquivoHistoricoApiTests.java:21`,25:409 para MANUAL/ARQUIVAMENTO/RESTAURACAO e físico/quadro iguais | PASS |
| GET histórico/comentário preservado; payload não cria arquivada | `sistema/src/test/java/com/patp/sistema/DemandaArquivoHistoricoApiTests.java:22`–25: id/ação/descrição/usuário/dataHora/processo.id/flag e físico exatos; `DemandaArquivoComentarioTests.java:35`–36 GET e físico iguais; `DemandaArquivoLegadaTests.java:40`–41 POSTtrue responde/persiste false | PASS |
| GET manual sem mutação; alvo inelegível trava envio mas permite cancelar | `frontend/src/pages/QuadroArquivo.test.jsx:99`–101: `b.disabled.toBe(true)`, submit forçado sem PUT, `methods.toEqual(["GET","PUT","GET"])`, Cancelar enabled e diálogo null | PASS |
| Nova confirmação exige arquivo; contrato antigo ausência=vazio; presença inválida rejeitada | `frontend/src/services/apiArquivo.test.js:31`–33 e52: ausência/null/objeto rejeitados na confirmação; linha69 GET antigo `toEqual(r)`; linha74 GET inválido `rejects...status200`; `ConfiguracaoArquivoResponseTests.java:27`–29 construtor anterior arquivo vazio/array | PASS |

## Payload, conjunção e fonte da evidência

Os14 campos são `id,numeroProcesso,pessoa,responsavel,status,prioridade,dataEmissao,prazoEtapa,prazoGeral,dataConclusao,dataCancelamento,motivoCancelamento,observacoes,etapaId`. A lista de chaves exata é assertada em `sistema/src/test/java/com/patp/sistema/ConfiguracaoArquivoResponseTests.java:21`. Igualdade física em `DemandaArquivoServiceTests.java:39` impede perda que um DTO compartilhado poderia ocultar. A API compara alvo, demais demandas, quadro completo, listas e etapas por valor; o cliente rejeita uma divergência isolada em **cada campo, nas duas ações** (`frontend/src/services/apiArquivo.test.js:30`,52). Igualdade de DTO não é a única prova.

Evento exige tipo, descrição, processo, autor real, intervalo de horário e quantidade exata. Versão exige incremento exatamente1. Recusas e rollback comparam todos os campos e linhas das tabelas afetadas e do quadro. As assertions de CHECK conferem snapshot após rollback, além da exception. As assertions de lock usam latch e espera200ms antes de liberar o primeiro commit, depois verificam ambas as ordens e os resultados finais; passaram em H2 e MySQL.

O E2E preserva **todos os campos próprios** de comentário/histórico e processoId (`frontend/e2e/arquivamento.spec.js:7`,56–60,78,82,106). A associação embedded ao processo muda de marcador durante o ciclo; a comparação dos campos próprios e HTTP204 de quadro foram autorizados pelo usuário. As novas ações de demanda continuam exigindo200.

## Adequação forward/reverse

Cada método/bloco parametrizado das14 suítes abaixo foi lido e relacionado a AC, borda ou Done when. Helpers e fixtures não são testes adicionais. A tabela anterior liga todos os ACs às assertions e aos valores esperados.

| Suíte nova | Invocações | Requisitos/bordas/Done when |
| --- | --- | --- |
| ConfiguracaoArquivoResponseTests | 2 | ARQ19; contrato14 campos/compatibilidade, T2 |
| DemandaArquivoModeloTests | 5 | ARQ18; null/false/true e JSON somente leitura, T1 |
| DemandaArquivoLeituraTests | 5 | ARQ18–21; estrutura e independência do quadro, T3 |
| DemandaArquivoServiceTests | 39 | ARQ02–09/11–15/17–18; três estados×dois papéis, rollback três escritas×duas ações, T4 |
| DemandaArquivoConcurrencyTests | 14 | ARQ05–06/11/13–16/22; mesma versão e edição/movimento/quadro nas duas ordens, T4 |
| DemandaArquivoComentarioTests | 6 | ARQ16–18/22; leitura, recusa e disputa duas ações×duas ordens, T5 |
| DemandaArquivoHistoricoTests | 6 | ARQ14/17–18/22; interno/externo e ação forjada, T6 |
| DemandaArquivoHistoricoApiTests | 4 | ARQ17/22; POST protegido/GET literal, T7 |
| DemandaArquivoLegadaTests | 13 | ARQ02/17–19/22–23; nove escritas, DELETE, GET e POST somente leitura, T4 |
| DemandaArquivoApiTests | 83 | ARQ01–14/18–22;6 sucessos,24 recusas,32 JSONs inválidos,10 status,4 sem criador,6 mutações reusadas e1 etapa protegida, T8 |
| apiArquivo.test.js | 106 | ARQ05–06/19–20/28–29; sucesso, divergências dos14 campos/snapshot, HTTP/rede/compatibilidade, T9 |
| AcaoArquivamentoDemanda.test.jsx | 12 | ARQ26–27/29; teclado/efeito/duplicação/pending/erro, T10 |
| QuadroArquivo.test.jsx | 42 | ARQ24–30 e orientação sobre referência de etapa, T11 |
| arquivamento.spec.js | 4 | ARQ02–03/05–06/13–14/17/19–21/23–26/28–29; três ciclos/recarga e conflito/permissão/quadro, teclado/mobile, T12 |

Totais novos: **177 Java,160 Vitest,4 Edge**. Não há teste novo sem objetivo, skip/only/disable ou cenário antigo removido. Mocks da interface escolhem respostas; autorização real, HTTP200 e JSON estrito são provados por API/E2E. SaídaSair reusa o callback global já testado, com assertion de true/false pelo fluxo novo e wiring conferido.

Duas adequações antigas autorizadas preservam os cenários: `sistema/src/test/java/com/patp/sistema/ProcessoArchiveTests.java:129`–131 muda DELETE500 para409/mensagem específica, mantém snapshot e acrescenta versão intacta; `frontend/src/pages/QuadroTransicoes.test.jsx:44` acrescenta Arquivar sem retirar Editar, transições ou casos de consulta/status antigo. O diff dos demais testes antigos está intacto.

## Gates independentes

Todos os gates abaixo foram executados pelo Verifier em08/10. Resultados foram reconferidos em09/10 com HEAD e hashes idênticos, sem repetição desnecessária.

| Gate | Resultado | Log em `C:/Users/Marco/AppData/Local/Temp/` |
| --- | --- | --- |
| Maven -B verify, JDK25, Mockito javaagent5.23.0, H2 teste/create-drop/port0 | 747 pass,0 falhas,0 erros,0 skips, exit0 | verifier-arquivo-h2.log |
| npm.cmd test, Vitest completo após Maven | 881 pass em22 arquivos,0 falhas/0 skips, exit0 | verifier-arquivo-vitest.log |
| node.exe scripts/run-e2e.mjs, Edge, workers1/retries0, H2 novo por arquivo | 13 pass em7 arquivos,0 falhas/0 skips, exit0 | verifier-arquivo-e2e.log |
| npm.cmd run lint | exit0 | verifier-arquivo-lint.log |
| npm.cmd run build -- --outDir TEMP/verifier-arquivo-build | exit0 | verifier-arquivo-build.log |
| Maven -B verify, MySQL fictício,24 classes selecionadas | 438 pass,0 falhas,0 erros,0 skips, exit0 | verifier-arquivo-mysql.log |
| TLC validate_spec.py e validate_tasks.py | ambos exit0,0 erros/0 warnings | saída independente08/10 |

Edge: arquivo4 + autocadastro1 + demandas1 + edição2 + etapas1 + gerenciamentos1 + transições3. Baseline anterior fornecido:570H2/721Vitest/9Edge. Atual747/881/13; delta **+177/+160/+4**, sem diminuição. O diff preserva os testes antigos, salvo as duas adequações autorizadas. Doze commits atômicos T1–T12 entre52c7725..4f977ea correspondem aos arquivos primários/testes/documentação; tasks completas e gates estruturais aprovados.

MySQL: SELECT somente leitura confirmou **@@port33823, @@datadir C:/Users/Marco/AppData/Local/Temp/creral-arquivo-049cfd5cc86d4d8b840e41f1710d7a43/mysql/, DATABASE()creral_arquivo_test, @@version8.0.43** antes do teste. O primeiro comando de proveniência falhou antes de conectar por argumento de host interpretado incorretamente; repetir com argumentos literais confirmou a instância. Os438 testes usaram somente o schema fictício/create-drop, connectionTimeZoneLOCAL e port0. Nenhuma aplicação/banco real foi iniciado. Em09/10, a fixture estava inativa; nenhum PID antigo ou banco real foi tocado, nenhum teste precisou ser repetido.

Seleção MySQL adequada: todas as10 classes novasArquivo (177 invocações) mais **DemandaEdicaoApi/Concurrency/Legada/Request/Service, DemandaTransicaoApi/Concurrency/Mensagem/Service, ComentarioArchive, EtapaArchive, GerenciamentoApi, HistoricoArchive e ProcessoArchive**. Cobrem modelo/snapshot, JSON, autorização, legado, rollback/lock e helpers sensíveis compartilhados pelos paths afetados. Descobrir nomes não substituiu execução/análise.

## Sensor expandido:11/11 mortos

Controles na cópia passaram antes de qualquer mutação: **70 Java e160 Vitest**, exit0. A primeira tentativa de controleJS não iniciou porque uma exclusão recursiva de dist na cópia retirou o CLI do Vitest. Foi corrigido apenas no scratch por cópia completa de node_modules; ambos os controles foram executados novamente e passaram antes dos mutants. Essa falha de montagem não conta como mutant morto nem como falha da feature; log preservado em `verifier-arquivo-sensor-control-js-bootstrap.log`.

Todas as11 falhas abaixo foram escolhidas pelo Verifier e executadas uma por vez. Java compilou e chegou às assertions, com **0 erros e0 skips** em todas as execuções; JS transformou/executou e falhou por assertions. Nenhuma morte deriva de compilation/bootstrap error. Os logs próprios foram relidos em09/10, incluindo as assertions e stacktraces.

| Mutant | Fonte/linha e falha introduzida só na cópia | Execução e assertion que matou | Resultado |
| --- | --- | --- | --- |
| M1 | `sistema/src/main/java/com/patp/sistema/service/ProcessoService.java:392`: remover exigirAdministracao apenas em mudarArquivo |39 testes,4 falhas; `DemandaArquivoServiceTests.java:24` espera ApiException403 para terceiro/sem criador; operação indevida não lança | KILLED |
| M2 | `sistema/src/main/java/com/patp/sistema/service/ProcessoService.java:396`: bypass do vínculo demanda-quadro com if(false) |39 testes,2 falhas; `DemandaArquivoServiceTests.java:24`,84 espera404, recebeu409 ou nenhuma recusa | KILLED |
| M3 | `sistema/src/main/java/com/patp/sistema/service/ProcessoService.java:406`: setArquivado(!arquivar) |39 testes,11 falhas; `DemandaArquivoServiceTests.java:39` mapa físico diverge do marcador esperado | KILLED |
| M4 | `sistema/src/main/java/com/patp/sistema/service/ProcessoService.java:406`: acrescentar setStatus("Em andamento") |39 testes,4 falhas; `DemandaArquivoServiceTests.java:39` preservação física de Concluido/Cancelado falha | KILLED |
| M5 | `sistema/src/main/java/com/patp/sistema/service/ProcessoService.java:409`: autor="Autor forjado" |39 testes,6 falhas; `DemandaArquivoServiceTests.java:45` containsEntry(usuario,autor.getNome()) recebe forjado | KILLED |
| M6 | `sistema/src/main/java/com/patp/sistema/service/ProcessoService.java:411`: remover PESSIMISTIC_FORCE_INCREMENT |39 testes,10 falhas; `DemandaArquivoServiceTests.java:40` espera1L, recebeu0L; contrato de versão/rollback detecta | KILLED |
| M7 | `sistema/src/main/java/com/patp/sistema/service/ProcessoService.java:417`: guard de demanda arquivada if(false) |13 testes,9 falhas; `DemandaArquivoLegadaTests.java:21` espera409, recebeu200 nas escritas protegidas | KILLED |
| M8 | `sistema/src/main/java/com/patp/sistema/controller/HistoricoController.java:30`: registrarExterno→registrar interno |4 testes,3 falhas; `DemandaArquivoHistoricoApiTests.java:21` espera409, recebeu200 para POST forjado | KILLED |
| M9 | `frontend/src/services/api.js:96`: ignorar observacoes na igualdade dos campos |106 testes,2 falhas; `frontend/src/services/apiArquivo.test.js:52` espera rejeição, Promise resolve nas duas ações | KILLED |
| M10 | `frontend/src/pages/Quadro.jsx:321`: aoErro usa setPrecisaAtualizar(false) |42 testes,14 falhas; `frontend/src/pages/QuadroArquivo.test.jsx:83` espera botão disabled=true após falha, recebeufalse; casos de atualização também detectam | KILLED |
| M11 | `sistema/src/main/java/com/patp/sistema/service/ProcessoService.java:391`: entityManager.find+verificarAtivo no lugar de exigirAtivo, preservando check de arquivo mas retirando lock inicial |14 testes,6 falhas; `DemandaArquivoConcurrencyTests.java:49` e69 espera bloqueio/TimeoutException antes da liberação; operação chega ao estado incompatível sem aguardar | KILLED |

Logs: `C:/Users/Marco/AppData/Local/Temp/verifier-arquivo-sensor-M1.log` até `M11.log`; controles `verifier-arquivo-sensor-control-java.log` e `control-js.log`; resultados estruturados `verifier-arquivo-sensor-results.json`; script independente `verifier-arquivo-sensor.py`. Sensor de risco crítico cobre autorização, vínculo, marcador, preservação de ciclo, auditoria, versão, proteção legada/manual, payload, GET obrigatório e lock. **11 mortos,0 sobreviventes.**

## Isolamento verificado

Antes do sensor foi capturado HEAD, `git status --porcelain -uall` e SHA256 de **todos os4259 arquivos versionados e não versionados não ignorados** em `C:/Users/Marco/AppData/Local/Temp/verifier-arquivo-baseline.json`. Gerados previamente sujos foram incluídos e preservados. `.git` não foi alterado.

Scratch profundo: `C:/Users/Marco/AppData/Local/Temp/verifier-arquivo-sensor-p6hee9_l`, com source/test/pom/config e node_modules copiado como arquivos próprios, sem symlink/hardlink ao cache real. Não houve stash/reset/clean nem alteração de fonte/teste real. Cada fonte copiada foi restaurada após seu mutant e novamente no finally.

Em09/10 as quatro fontes mutadas da cópia foram comparadas com as reais (normalizando apenas CRLF/LF): **SCRATCH SOURCES RESTORED True**. A comparação de baseline antes de descarte retornou **ISOLATION True FILES4259**. O scratch próprio foi descartado após o script verificar o nome e o paiTEMP exatos; a comparação após descarte retornou novamente **ISOLATION True FILES4259**, antes desta primeira escrita do relatório. HEAD, porcelain e todos os hashes iguais. Nenhuma limitação de limpeza.

## Qualidade e guidelines

Todas as27 paths do diff foram revistas. O modelo acrescenta marcador separado somente leitura; DTO acrescenta lista; EtapaService particiona; ProcessoService implementa duas ações e guards antigos; Comentario/Historico protegem escritas; controllers expõem ações; cliente valida; Quadro/diálogo integram o fluxo. Nenhuma biblioteca nova ou upgrade. Guards/snapshots reusam padrões existentes. Não há normalização de registros antigos, exclusão física ou recurso alheio.

| Princípio | Resultado |
| --- | --- |
| Escopo solicitado, mínimo código, sem abstração descartável | PASS |
| Mudanças cirúrgicas, estilo e padrões existentes | PASS |
| Matriz por camada: domínio/API/UI/E2E, sucesso/erro/borda | PASS |
| Assertions de valores/estados/payload, sem efeitos em recusas | PASS |
| Suítes antigas preservadas, adequações autorizadas | PASS |
| Guidelines docs/OPERACAO.md, pom.xml, package.json, playwright.config.js e application.properties de teste | PASS |
| Sem SPEC_DEVIATION ou skip/disable/suppress de testes | PASS |

Capturas próprias de08/10 em TEMP/creral-arquivo-qa-aGLaSm: mobile-arquivo.png e mobile-confirmacao.png foram inspecionadas, com dados completos/legíveis, foco em Cancelar e controles bloqueados; desktop-arquivo.png também foi produzida. `frontend/e2e/arquivamento.spec.js:99` verifica ausência de overflow horizontal. Isso não equivale a aceitação humana.

## Encerramento e aceitação humana

T1–T12 estão completos; revisão independente e sensor PASS. `python.exe -B .agents/skills/tlc-spec-driven/scripts/validate_state.py demandas-arquivamento` retornou **exit0,0 erros** em 09/10/2026. Sem falha de AC, sobrevivente, spec-precision gap ou SPEC_DEVIATION: **nenhuma lesson nova registrada**, conforme a regra de clean PASS.

O roteiro humano de docs/OPERACAO.md continua **pendente**. O Verifier não alterou spec/tasks/STATE/diagnóstico nem os marcou encerrados; o root fará a reconciliação documental após este veredicto. Nenhum push, deploy, upgrade, banco configurado ou dado real foi alterado.
