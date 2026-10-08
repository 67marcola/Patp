# Demandas edição: validação independente

## Validation: demandas-edicao — PASS

Os 29 critérios têm evidência alinhada ao resultado da spec. Todos os gates independentes passaram. Nove mutações comportamentais foram detectadas. Não há gap de implementação, cobertura ou precisão da spec nesta revisão.

**Data do relatório:** 08/10/2026. Gates/sensor começaram em 07/10; confirmação de restauração e relatório em 08/10.
**Spec:** `.specs/features/demandas-edicao/spec.md`.
**Diff:** `cd62ee4..c20a1bf`, HEAD completo `c20a1bf17b71b23088dc0339d9ad0a0c716b0811`.
**Verifier:** subagente independente `/root/verifier_edicao`; autor ≠ verifier. Evidências abaixo rederivadas diretamente da spec, código e assertions. O autor não participou das decisões de cobertura ou das mutações.
**Escopo:** implementação/testes/diff lidos sem escrita na árvore real. Só este relatório foi acrescentado depois da prova de isolamento. Sem push, deploy, migração ou banco real.

## Tarefas

T1–T8 têm Done when marcado e comportamento confirmado nos gates abaixo. DTO19 casos; servidor/concorrência32; legada9; API31; API frontend34; editor11; quadro15; E2E edição2. Os demais testes preservam o escopo anterior. O diagnóstico e o status final da spec ficam para o orquestrador após este PASS.

## Critérios, resultado esperado e assertion

O helper `DemandaEdicaoServiceTests.java:29` compara o status e a mensagem exatos; linhas31/32 comparam todas as tabelas de demandas,etapas,comentários,históricos e a versão. O helper HTTP `DemandaEdicaoApiTests.java:18` compara status/mensagem; linha19 prova ausência de efeitos. As referências na tabela incluem esses helpers e seus parâmetros, sem considerar mera chamada como cobertura.

| Critério | Resultado da spec | File:line e expressão observada | Resultado |
| --- | --- | --- | --- |
| EDI-01 | Sessão ausente, inválida ou encerrada:401 e zero efeitos. | `sistema/src/test/java/com/patp/sistema/DemandaEdicaoApiTests.java:18`: `status().is(codigo), jsonPath("$.erro").value(mensagem); casos401 em42–44; conteudoPersistido()==antes e versão==versao em19` | PASS |
| EDI-02 | Terceiro:403 e zero efeitos. | `sistema/src/test/java/com/patp/sistema/DemandaEdicaoApiTests.java:18`: `status().is(403) com caso terceiro em45; persistência/versão iguais em19; legada também403 em DemandaEdicaoLegadaTests.java:32` | PASS |
| EDI-03 | Arquivo:409 e Gerenciamento arquivado. Restaure-o antes de alterar., sem efeitos. | `sistema/src/test/java/com/patp/sistema/DemandaEdicaoServiceTests.java:73`: `recusa(...,409,...,"Gerenciamento arquivado. Restaure-o antes de alterar."); helper29/31/32 compara status,mensagem,persistência,versão` | PASS |
| EDI-04 | Sem criador:admin edita, funcionário recebe403; criador continua null. | `sistema/src/test/java/com/patp/sistema/DemandaEdicaoServiceTests.java:65`: `admin:versao().isEqualTo(1L); funcionário:recusa403 em66; getCriador().isNull() em67` | PASS |
| EDI-05 | Persistir exatamente os sete campos normalizados, sem regra cronológica adicional. | `sistema/src/test/java/com/patp/sistema/DemandaEdicaoServiceTests.java:47`: `extracting(...).containsExactly(id,numero,"Outro cliente","Ana",null,2020-02-29,null,2019-01-01,"Linha\ncompleta"); snapshot HTTP exato em DemandaEdicaoApiTests.java:28` | PASS |
| EDI-06 | Cliente ausente/branco/>255:400, mensagem exata e zero efeitos. | `sistema/src/test/java/com/patp/sistema/DemandaEdicaoServiceTests.java:78`: `recusa(...,400,...,"Informe um cliente/solicitante entre 1 e 255 caracteres."); parâmetros null,vazio,branco,256 em75–77 e helper29/31/32` | PASS |
| EDI-07 | Responsável/prioridade>255 e observações>10000:400 e mensagens do cadastro; limites aceitos. | `sistema/src/test/java/com/patp/sistema/DemandaEdicaoServiceTests.java:85`: `recusa(...,400,...,mensagem); isEqualTo("a".repeat(n)) em89; mensagens exatas em83; emoji10000/10002 em93–94` | PASS |
| EDI-08 | Opcional vazio/null/omitido é persistido null e pode retirar informação. | `sistema/src/test/java/com/patp/sistema/DemandaEdicaoServiceTests.java:97`: `extracting("responsavel","prioridade","dataEmissao","prazoEtapa","prazoGeral","observacoes").containsOnlyNulls(); requisição omitindo opcionais aceita nos casos legados em DemandaEdicaoLegadaTests.java:18` | PASS |
| EDI-09 | Datas ISO válidas/null aceitas; data impossível ou tipo incompatível:400 sem efeitos. | `sistema/src/test/java/com/patp/sistema/DemandaEdicaoRequestTests.java:38`: `hasMessageContaining("Informe uma data ISO válida ou null."); válido containsExactly(...,2020-02-29,...) em20; HTTP400 com mensagem JSON em DemandaEdicaoApiTests.java:62/18/19` | PASS |
| EDI-10 | ID,status,etapa,datas e motivo de encerramento literalmente preservados. | `sistema/src/test/java/com/patp/sistema/DemandaEdicaoServiceTests.java:49`: `containsExactly(status,p.getDataConclusao(),p.getDataCancelamento(),p.getMotivoCancelamento()); etapa==e.id em50 e ID/número em47` | PASS |
| EDI-11 | Inexistente/externa:404 Demanda não encontrada neste gerenciamento., sem efeitos. | `sistema/src/test/java/com/patp/sistema/DemandaEdicaoServiceTests.java:108`: `recusa(...,404,...,"Demanda não encontrada neste gerenciamento.") e inexistente em109; HTTP casos47–48/assertion18 em DemandaEdicaoApiTests.java` | PASS |
| EDI-12 | Ciclo/campo desconhecido dedicado:400 Dados da requisição inválidos., sem efeitos. | `sistema/src/test/java/com/patp/sistema/DemandaEdicaoApiTests.java:62`: `erro(...,400,"Dados da requisição inválidos.",...); protected fields enumerados em58 e helper18/19; DTO desconhecido em DemandaEdicaoRequestTests.java:25` | PASS |
| EDI-13 | Versão ausente/negativa:400 Informe uma versão válida do gerenciamento.; outros tipos JSON400. | `sistema/src/test/java/com/patp/sistema/DemandaEdicaoServiceTests.java:107`: `recusa(...,400,...,"Informe uma versão válida do gerenciamento."); negativo em103; tipos sem coerção hasMessageContaining("A versão deve ser um número inteiro.") em DemandaEdicaoRequestTests.java:32` | PASS |
| EDI-14 | Versão divergente:409 Gerenciamento alterado por outro usuário. Atualize e tente novamente., sem efeitos. | `sistema/src/test/java/com/patp/sistema/DemandaEdicaoConcurrencyTests.java:40`: `getStatus().value().isEqualTo(409); getMessage().isEqualTo(mensagem exata); conteudoPersistido().isEqualTo(antes) em42 e serviço em103` | PASS |
| EDI-15 | Edição aceita:200,snapshot atualizado e versão anterior+1 uma vez. | `sistema/src/test/java/com/patp/sistema/DemandaEdicaoApiTests.java:26`: `status().isOk(); gerenciamento.versao.value(1); campos/snapshot26–35; concorrência versão1 em DemandaEdicaoConcurrencyTests.java:42` | PASS |
| EDI-16 | Exatamente um EDICAO, autor real, data/hora e Processo alterado.. | `sistema/src/test/java/com/patp/sistema/DemandaEdicaoServiceTests.java:57`: `evento.containsEntry("usuario",autor.getNome()).containsEntry("descricao","Processo alterado.").containsEntry("processo_id",id); data_hora.isBetween(...) em58; total2 incluindo evento antigo em59; EDICAO único em DemandaEdicaoConcurrencyTests.java:44` | PASS |
| EDI-17 | Falha em demanda/histórico/versão reverte integralmente todas as escritas. | `sistema/src/test/java/com/patp/sistema/DemandaEdicaoServiceTests.java:124`: `conteudoPersistido().isEqualTo(antes); getVersao().isZero(); CHECK por cada tabela em117–123; executado em H2 e MySQL` | PASS |
| EDI-18 | Lock ordena edição×edição/arquivo/movimento; recusas sem parcial. | `sistema/src/test/java/com/patp/sistema/DemandaEdicaoConcurrencyTests.java:57`: `segunda.get(200ms) lança TimeoutException; erro409/mensagem exata em59; pessoa/etapa/status/número/versão/arquivo/eventos exatos em61–66; ambas ordens em69` | PASS |
| EDI-19 | Legada aplica autorização,validação,número fixo,ciclo e incremento sob mesmo lock. | `sistema/src/test/java/com/patp/sistema/DemandaEdicaoLegadaTests.java:32`: `status().is(terceiro?403:400),erro.value(mensagem); persistência igual e versão0 em33; ciclo containsExactly em21 e versão1/evento/autor em22–23; ciclo divergente400 em DemandaTransicaoServiceTests.java:211` | PASS |
| EDI-20 | Editor preenchido pelo snapshot; opcional null aparece vazio. | `frontend/src/pages/EditorDemandaEdicao.test.jsx:19`: `para todos campos expect(input.value).toBe(demanda[campo] ?? ""); snapshot nos três estados em QuadroEdicao.test.jsx:29` | PASS |
| EDI-21 | Cancelar sem mutação,fecha e restaura foco existente. | `frontend/src/pages/QuadroEdicao.test.jsx:42`: `form.toBeNull(); document.activeElement.toBe(botao); methods.toEqual(["GET"]) em43; cancelar sem envio em EditorDemandaEdicao.test.jsx:55` | PASS |
| EDI-22 | Pending impede duplicate,fechar,outras mutações e saída. | `frontend/src/pages/QuadroEdicao.test.jsx:64`: `Voltar,Criar,Nova etapa,Cancelar,Salvando,Reabrir.disabled.toBe(true); voltar.not.toHaveBeenCalled e fetch.times(2) em66; editor submit duplicado.times(1) em EditorDemandaEdicao.test.jsx:64; aoOcupar[true,false] em QuadroEdicao.test.jsx:55 + Sair.disabled em App.test.jsx:40 prova controle global compartilhado` | PASS |
| EDI-23 | Erro HTTP/rede/falso200 preserva campos,anuncia erro,exige GET manual e não repete edição. | `frontend/src/pages/QuadroEdicao.test.jsx:79`: `value.toBe(" Rascunho "); submit.disabled.true em80; alert exato em81; methods=[GET,PUT] em82 e [GET,PUT,GET] em86; versão do reenvio manual4 em90; API errors/retry exatos em services/apiEdicao.test.js:44/46/53/54/58/60` | PASS |
| EDI-24 | Somente200 com versão+1,demanda correta,campos normalizados e ciclo igual fecha e atualiza cartão/cache/foco. | `frontend/src/services/apiEdicao.test.js:44`: `divergências de quadro/versão/permissão/ID/número/ciclo/7campos/contagem rejeitam com ApiError200; response.toEqual(r) em22; QuadroEdicao.test.jsx:50/52/53/54/55/56 prova fechamento,cartão,contagem,cache3,foco` | PASS |
| EDI-25 | Troca de quadro descarta editor e ignora resposta tardia. | `frontend/src/pages/QuadroEdicao.test.jsx:101`: `atualizar.mock.calls.some(q.id===9 && q.versao===3).toBe(false); form.toBeNull() em98; headingOutro existe e article nulo em100` | PASS |
| EDI-26 | Sem permissão/arquivado:só consulta,sem Editar. | `frontend/src/pages/QuadroEdicao.test.jsx:35`: `queryByRole("button",Editar...).toBeNull(); card.textContent.toContain("Anterior") em36; E2E terceiro em frontend/e2e/edicao.spec.js:89` | PASS |
| EDI-27 | Número original fixo; dedicado não recebe; legada omitido/idêntico aceita e diferente400 exato. | `sistema/src/test/java/com/patp/sistema/DemandaEdicaoLegadaTests.java:20`: `numeroProcesso.value(p.getNumeroProcesso()); parâmetros omitido/idêntico em15/18; diferente400/mensagem em32; API dedicada rejeita número em DemandaEdicaoApiTests.java:58/62; input.readOnly.true e payload exato sem número em frontend/src/pages/EditorDemandaEdicao.test.jsx:20/29` | PASS |
| EDI-28 | Concluída/cancelada pode editar em quadro ativo por criador/admin sem modificar encerramento/etapa. | `sistema/src/test/java/com/patp/sistema/DemandaEdicaoServiceTests.java:49`: `6 combinações status×admin em34, encerramento.containsExactly(literais),etapa igual em50; frontend permite3 estados em frontend/src/pages/QuadroEdicao.test.jsx:29; E2E encerramentos e recarga em frontend/e2e/edicao.spec.js:58/62` | PASS |
| EDI-29 | Status null,vazio,desconhecido:409 mensagem exata,zero efeitos e consulta sem Editar. | `sistema/src/test/java/com/patp/sistema/DemandaEdicaoServiceTests.java:114`: `recusa(...,409,...,"Status antigo não reconhecido. Solicite a correção do registro."); status literal igual em115; frontend statuses null/vazio/Antigo em frontend/src/pages/QuadroEdicao.test.jsx:32/35` | PASS |

**Cobertura:**29/29; zero lacunas de precisão. Os campos de payload/snapshot são comparados por valor e estado; cliente,data,ciclo,versão,autor e eventos não foram inferidos só da ocorrência de uma chamada.

## Bordas da spec

| Borda | Evidência e outcome | Resultado |
| --- | --- | --- |
| 255/10000 aceitos; excesso inclusive UTF16 recusado | `sistema/src/test/java/com/patp/sistema/DemandaEdicaoServiceTests.java:88`:cliente255; linha89:opcionais no limite; linhas93/94:emoji10002 recusado e10000 aceito. `frontend/src/pages/EditorDemandaEdicao.test.jsx:38` compara payload exato com emoji255/10000. | PASS |
| Data bissexta aceita, impossível recusada, prazos anteriores à emissão permitidos | `sistema/src/test/java/com/patp/sistema/DemandaEdicaoServiceTests.java:47`:2020-02-29 e2019-01-01 persistidos; `sistema/src/test/java/com/patp/sistema/DemandaEdicaoRequestTests.java:38`:2023-02-29 recusada; API400 em `sistema/src/test/java/com/patp/sistema/DemandaEdicaoApiTests.java:62`. | PASS |
| Limpeza vira null no banco e Não informado no cartão | `sistema/src/test/java/com/patp/sistema/DemandaEdicaoServiceTests.java:97`:containsOnlyNulls; `frontend/src/pages/QuadroDemandas.test.jsx:128`:valorCartao(...).toBe("Não informado") para campos nulos. Renderizador compartilhado; E2E edição limpa opcionais e compara snapshot em `frontend/e2e/edicao.spec.js:46`. | PASS |
| Encerramentos antigos null,datas e motivos longos literais | `sistema/src/test/java/com/patp/sistema/DemandaEdicaoServiceTests.java:49`:motivo com espaços,CRLF e20000 caracteres/data antiga permanece literal; APInull em `sistema/src/test/java/com/patp/sistema/DemandaEdicaoApiTests.java:33`; legada literais em `sistema/src/test/java/com/patp/sistema/DemandaEdicaoLegadaTests.java:21`. | PASS |
| Criador null não atribuído automaticamente | `sistema/src/test/java/com/patp/sistema/DemandaEdicaoServiceTests.java:67`:getCriador().isNull() após edição admin/recusa funcionário. | PASS |
| Não mover nem alterar contagem das etapas | `sistema/src/test/java/com/patp/sistema/DemandaEdicaoServiceTests.java:50/53`:etapa e quantidade1; `frontend/e2e/edicao.spec.js:48`:contagens.toEqual(anteriores). | PASS |
| Número somente leitura e fora do dedicado | `frontend/src/pages/EditorDemandaEdicao.test.jsx:20`:readOnly.true; linha29:payload exato sem número; `frontend/e2e/edicao.spec.js:45`:postDataJSON().toEqual({versao,...7campos}). | PASS |
| Formulário completo8campos; cliente omitido inválido; opcionais omitidos null | `sistema/src/test/java/com/patp/sistema/DemandaEdicaoRequestTests.java:20`:DTOexato; `sistema/src/test/java/com/patp/sistema/DemandaEdicaoServiceTests.java:78`:null cliente400; `sistema/src/test/java/com/patp/sistema/DemandaEdicaoLegadaTests.java:18/20`:opcionais omitidos aceitos; núcleo partilhado salva gettersnull, provado por serviço97. | PASS |
| Legada entidade,sem versão enviada,lock/permissão/incremento; ciclo omitido/idêntico preservado,divergente recusado | `sistema/src/test/java/com/patp/sistema/DemandaEdicaoLegadaTests.java:20/21/22/23`:retorno entidade200,ciclo,versão1,evento/autor; `sistema/src/test/java/com/patp/sistema/ProcessoArchiveTests.java:102`:status idêntico aceito; `sistema/src/test/java/com/patp/sistema/DemandaTransicaoServiceTests.java:211`:cinco campos divergentes400 sem efeitos. | PASS |
| Mensagens opcionais exatas | `sistema/src/test/java/com/patp/sistema/DemandaEdicaoServiceTests.java:83/85`:Informe um responsável com até255 caracteres.; Informe uma prioridade com até255 caracteres.; Informe observações com até10000 caracteres. Valores reais têm os espaços normais especificados; helper29 compara igualdade, não substring. | PASS |
| Mensagens versão/destino/JSON e GET manual após não confirmação | `sistema/src/test/java/com/patp/sistema/DemandaEdicaoApiTests.java:18/19`:exatas nos casos38–68; `frontend/src/pages/QuadroEdicao.test.jsx:81/82/86`:mensagem atual e sequência sem retry,seguida apenas de GETmanual. | PASS |
| Envio explícito sem diferenças continua versão+1 eEDICAO | Fluxo servidor não possui short-circuit por igualdade; qualquer payload válido alcança gravação,eventual evento e incremento em `sistema/src/main/java/com/patp/sistema/service/ProcessoService.java:251/252/254`. UI envio sem diferenças confirmado em `frontend/src/pages/QuadroEdicao.test.jsx:62/67/68`,versão3 fecha o formulário. | PASS por revisão do fluxo + prova da UI |

## Gates independentes

Todos executados na cópia TEMP, em sequência por recurso. JDK25.0.2, Maven3.9.16, javaagent Mockito5.23.0. H2 usa somente classpath de teste/create-drop. O MySQL usado foi exclusivamente `127.0.0.1:33821/creral_edit_tests`, usuário fictício restrito ao schema; create-drop de teste. Nenhuma execução usa o datasource main.

**Diretório de evidências:** `C:/Users/Marco/AppData/Local/Temp/creral-edicao-32565590f1a14a198df2fc432b1bdfeb/verifier/`.

| Gate | Comando | Resultado | Log |
| --- | --- | --- | --- |
| Java/H2 completo | `mvn.cmd -B verify -DargLine=-javaagent:C:/Users/Marco/.m2/repository/org/mockito/mockito-core/5.23.0/mockito-core-5.23.0.jar` |570 aprovados,0 falhas,0 erros,0 skips; exit0 |`h2.log` |
| Vitest completo |`npm.cmd test` |721 aprovados,19 arquivos,0 falhas/skips; exit0 |`vitest.log` |
| Lint |`npm.cmd run lint` |exit0 |`lint.log` |
| Build |`npm.cmd run build -- --outDir .../verifier/build` |exit0,artefatos somente TEMP |`build.log` |
| MySQL selecionado |`mvn.cmd -B test -Dtest=DemandaEdicaoRequestTests,DemandaEdicaoServiceTests,DemandaEdicaoConcurrencyTests,DemandaEdicaoLegadaTests,DemandaEdicaoApiTests,HistoricoDescricaoTests,DemandaTransicaoServiceTests,DemandaTransicaoApiTests,DemandaTransicaoMensagemTests,DemandaTransicaoConcurrencyTests,EtapaDemandConcurrencyTests,ProcessoArchiveTests,HistoricoArchiveTests -DargLine=...` com URL/credenciais fictícias no env |213 aprovados,0 falhas,0 erros,0 skips; exit0 |`mysql.log` |
| Edge/H2 completo |`npm.cmd run test:e2e` |9 aprovados em6 arquivos,0 falhas/retries/skips; exit0 |`e2e.log` |
| Restauração servidor |`mvn.cmd -B test -Dtest=DemandaEdicaoRequestTests,DemandaEdicaoServiceTests,DemandaEdicaoConcurrencyTests,DemandaEdicaoLegadaTests,DemandaEdicaoApiTests -DargLine=...` |91 aprovados,0 falhas/erros/skips; exit0 |`restored-h2.log` |
| Restauração frontend |`npm.cmd test -- src/services/apiEdicao.test.js` |34 aprovados,0 falhas/skips; exit0 |`restored-vitest.log` |

Total dos gates base:570+721+213+9=1513 execuções de testes, com sobreposição intencional H2/MySQL. Não somar como1513 casos únicos. Baseline informado nos tasks:479Java,661 Vitest,7 Edge. Atual:570/721/9, delta+91/+60/+2. Diff dos testes anteriores conferido diretamente:nenhum caso removido,nenhuma assertion menos específica,nenhum skip/todo/Disabled novo. Adequações autorizadas mantêm criador,número original,versão+1 e três listas com Editar, mantendo ações anteriores.

## Sensor expandido

Risco de autorização/persistência,9 falhas comportamentais isoladas. Cada mutação parte da fonte original,roda o teste alvo,restaura a fonte antes da próxima. Os oito Maven tiveram falhas de assertion e zero erros/compilação/bootstrap. O Vitest teve7 assertions rejeitando falso sucesso. Todas as mutações terminaram exit1,como esperado.

| Mutante | File:line | Falha injetada | Assertion que detectou | Outcome |
| --- | --- | --- | --- | --- |
| M1-auth | `sistema/src/main/java/com/patp/sistema/service/ProcessoService.java:224` | Remover exigirAdministracao somente do núcleo de edição. | `sistema/src/test/java/com/patp/sistema/DemandaEdicaoServiceTests.java:28/71`: edição de terceiro deixa de lançar403;1falha/1caso | KILLED,exit1 |
| M2-number | `sistema/src/main/java/com/patp/sistema/service/ProcessoService.java:240` | Trocar condição de número divergente por false na legada. | `sistema/src/test/java/com/patp/sistema/DemandaEdicaoLegadaTests.java:32`: HTTP esperado400,recebido200;1falha/3casos | KILLED,exit1 |
| M3-cycle | `sistema/src/main/java/com/patp/sistema/service/ProcessoService.java:248` | Forçar status Em andamento na edição informativa. | `sistema/src/test/java/com/patp/sistema/DemandaEdicaoServiceTests.java:49`: status encerrado esperado literal,recebido Em andamento;4falhas/6casos | KILLED,exit1 |
| M4-version | `sistema/src/main/java/com/patp/sistema/service/ProcessoService.java:254` | Remover PESSIMISTIC_FORCE_INCREMENT da edição. | `sistema/src/test/java/com/patp/sistema/DemandaEdicaoServiceTests.java:51`: versão esperada1L,recebida0L;6falhas/6casos | KILLED,exit1 |
| M5-event | `sistema/src/main/java/com/patp/sistema/service/ProcessoService.java:252` | Gravar autor fictício Falso no EDICAO. | `sistema/src/test/java/com/patp/sistema/DemandaEdicaoServiceTests.java:57`: autor esperado Dono/Admin,recebido Falso;6falhas/6casos | KILLED,exit1 |
| M6-data | `sistema/src/main/java/com/patp/sistema/service/ProcessoService.java:247` | Descartar observações enviadas e salvar null. | `sistema/src/test/java/com/patp/sistema/DemandaEdicaoServiceTests.java:47`: observações esperadas Linha\ncompleta,recebido null;6falhas/6casos | KILLED,exit1 |
| M7-unknown | `sistema/src/main/java/com/patp/sistema/service/ProcessoService.java:228` | Desativar bloqueio de status desconhecido. | `sistema/src/test/java/com/patp/sistema/DemandaEdicaoServiceTests.java:28/114`: status null,vazio,Antigo deixa de lançar409;3falhas/3casos | KILLED,exit1 |
| M8-archive | `sistema/src/main/java/com/patp/sistema/service/ProcessoService.java:223` | Usar somente lock em vez de exigirAtivo. | `sistema/src/test/java/com/patp/sistema/DemandaEdicaoServiceTests.java:29/73`: mensagem esperada arquivo,recebida conflito de versão;1falha/1caso | KILLED,exit1 |
| M9-confirmation | `frontend/src/services/api.js:143` | Aceitar campos divergentes retornando true no verificador de edição. | `frontend/src/services/apiEdicao.test.js:44`: promise deveria rejeitar sete divergências de campos,mas resolveu;7falhas/34casos | KILLED,exit1 |

**Sensor:**9 injetados,9 detectados,0 sobreviventes,0 inválidos. Diffs exatos,seleção de testes,contagens e logs individuais preservados em `sensor.json`,`sensor.py` e `M1-auth.log` até `M9-confirmation.log`.

## Isolamento e descarte

`before.json` foi capturado antes da cópia e de qualquer gate/sensor. `after.json` e `isolation-result.json` foram capturados após as restaurações,antes deste relatório. Ambos contêm HEAD,porcelain completo e SHA256 de cada arquivo rastreado/untracked não ignorado.4209 arquivos antes e depois; nenhum arquivo incluído/excluído/alterado; HEAD e porcelain idênticos. Artefatos modificados/untracked pré-existentes de target/node_modules e temp-path foram preservados.

Scratch inicialmente era uma cópia física das fontes e dependências,sem junction para node_modules real,sem git stash/reset. Restauração validada por testes e comparação byte a byte das duas fontes mutadas. Primeira comparação byte a byte da cópia frontend detectou apenas conversão LF→CRLF por Python write_text; texto já estava restaurado e34 testes haviam passado. A cópia foi reposta com os bytes originais; comparação repetida confirmou ambas as fontes idênticas. Árvore real permaneceu idêntica em ambas as capturas.

Após verificar o path absoluto exatamente dentro de TEMP/verifier/scratch,os logs H2 e relatórios Surefire foram preservados fora da cópia,e somente o scratch foi removido. Nenhuma mutação está ativa. Sem encerramento/reinício de MySQL pelo verifier; o root administra o fixture. Retomada em 08/10 encontrou o fixture inativo após os gates de07/10,sem necessidade de reabrir banco.

## Qualidade e integridade

| Check | Resultado |
| --- | --- |
| Escopo limitado à edição;nenhuma feature adicional |PASS |
| Reuso dos validadores,guard,lock,snapshot,editor;sem abstração ou flexibilidade desnecessária |PASS |
| Mudanças cirúrgicas e padrões existentes |PASS |
| Testes preservados e ajustes antigos expressamente autorizados |PASS |
| Domínio com mapeamento1:1 aos ACs e API feliz/edge/erro |PASS |
| Frontend verifica payload/resposta/ciclo por valor;sem retry automático |PASS |
| Cada teste novo tem AC,edge ou Done when:T1DTO,T2persistência/rollback/locks,T3legada,T4HTTP,T5confirmação,T6editor,T7quadro,T8persistência real |PASS |
| Guidelines:docs/OPERACAO.md,frontend/vitest.config.js,frontend/playwright.config.js,sistema/pom.xml |PASS |
| Sem SPEC_DEVIATION ou bypass de testes novo |PASS |

O trecho operacional novo descreve a edição e separa UAT humano de testes automáticos. Uma frase antiga fora dos hunks sobre edição ser entrega futura permanece na seção de cadastro; é referência histórica herdada,sem mudar o contrato específico novo. O root pode harmonizar essa redação no fechamento documental. Não foi considerada defeito funcional ou gap dos29 ACs.

## QA e UAT

As capturas do autor `C:/Users/Marco/AppData/Local/Temp/creral-edicao-qa-yAeiwS/desktop.png` e `mobile.png` foram abertas e revisadas pelo verifier. Formulário,títulos,campos,ações e cartão cancelado estão legíveis; número permanece somente leitura; layouts desktop/mobile sem corte relevante. Gate próprio produziu novas capturas em `C:/Users/Marco/AppData/Local/Temp/creral-edicao-qa-M9Epun/`; o E2E verifica largura sem overflow em `frontend/e2e/edicao.spec.js:69`.

**Aceitação humana:**pendente. Nenhuma resposta humana de UAT foi inventada. O roteiro em docs/OPERACAO.md continua disponível; este PASS é técnico e independente.

## Proveniência e tentativas preservadas

Os resultados do autor foram comparados,mas não usados como substituto dos gates independentes:570 H2 em `t4-java-2.log`,721 Vitest em `t7-vitest.log`,lint/buildTEMP,9 Edge em `t8-e2e.log` e213 MySQL em `t8-mysql.log`,no diretório TEMP pai. Gates independentes reproduziram esses totais.

Histórico do autor preservado: `t1.log` teve14/19 aprovados e5 expectativas de mensagem divergentes; o usuário autorizou o ajuste exato e `t1-2.log` passou19. `t2.log` teve falha de compilação no helper Runnable,sem enfraquecer assertions; `t2-2.log` passou32. `t4-java.log` teve569/570 por ator antigo em EtapaDemandConcurrencyTests; adequação expressamente autorizada preservou a prova de ciclo,seguida de570 no `t4-java-2.log`. Tentativas antigas de timeout por concorrência de CPU foram relatadas pelo autor; não há arquivo independente dessa tentativa entre os logs fornecidos e ela não sustenta PASS funcional. Nesta revisão os gates rodaram sequencialmente e não tiveram timeout.

A revisão independente não teve gap,gate funcional quebrado ou mutante sobrevivente. O ajuste LF/CRLF ocorreu somente na cópia scratch e foi corrigido antes de descartá-la; não é falha de execução do produto. Não se grava lesson nova em PASS limpo. Histórico anterior não foi apagado nem reclassificado como uma nova falha atual.

## Fechamento

**Result:** PASS.29/29 ACs,edges verificados,todos os gates aprovados,sensor9/9 e isolamento confirmado.
**Lacunas ranqueadas:**nenhuma.
**Traceabilidade recomendada:**EDI-01–29 de Implementing para Verified após o orquestrador persistir o fechamento; T1–T8 completos. Verifier não alterou spec/tasks/diagnóstico.
**Próximo passo:** fechar documentação/estado local e registrar UAT humano quando o usuário realizar o roteiro.
