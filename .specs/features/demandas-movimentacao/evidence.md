# Evidências de execução

## T1: histórico integral

Gate `mvn.cmd -B verify` com Mockito javaagent explícito:392 testes,0falhas,0erros,0skips. Log TEMP/creral-movimentacao-af89b92e2e52431b8e1bf9156c10ba61/t1-verify.log. Primeira tentativa restrita teve11erros de bootstrap dos testes HTTP existentes, sem falhas de assertions; repetição com loopback autorizado passou. Nenhum serviço normal/MySQL configurado iniciado.

### AC → assertions

| Critério desta tarefa | Assertion e localização | Resultado esperado |
| --- | --- | --- |
| MOV-16/19, capacidade de descrição integral | HistoricoDescricaoTests.java:27, assertThat(registro.get("descricao").toString()).isEqualTo(descricao) | Texto longo completo, acentos/emoji, prefixo/fim preservados. |
| MOV-19, payload persistido | HistoricoDescricaoTests.java:28–31, acao REABERTURA, usuario Criador, processo_id exato, dataHora between | Evento completo corresponde ao registro solicitado. |
| Done when, sem efeitos sobre demanda | HistoricoDescricaoTests.java:32–33, count1 e status Em andamento | Exatamente um evento, status inalterado. |

### Assertions → AC

| Assertions | Maps to | Keep |
| --- | --- | --- |
| HistoricoDescricaoTests.java:27 | MOV-16/19 capacidade do histórico longo | sim |
| HistoricoDescricaoTests.java:28–31 | MOV-19 payload do evento | sim |
| HistoricoDescricaoTests.java:32–33 | Done when capacidade de guardar descrição sem mudar demanda | sim |

Adequação:2casos, valores completos, nenhum teste sem âncora; segue ApiIntegrationSupport e padrões de docs/OPERACAO. A limpeza real de reabertura e preservação dos campos anteriores ainda serão verificadas em T2/T3. Não declarar MOV-16/19 completos somente pelo schema.

## T2: serviço de transição

Gate final432Java/H2,40casos novos,0falhas/erros/skips, log TEMP/creral-movimentacao-af89b92e2e52431b8e1bf9156c10ba61/t2-final.log. Primeira compilação falhou no helper de teste Runnable/ThrowingCallable; corrigido por chamada::run, sem mudar resultado esperado. Nenhum teste excluído/ignorado.

Dados anteriores de reabertura são JSON com seis propriedades explícitas, incluindo null; evita ambiguidades de aspas/quebras de linha/motivo literalmente null. Snapshot frontend ainda T3–T6.

### AC → assertions

| Critério | file:line + assertion | Resultado definido |
| --- | --- | --- |
| MOV-02/03/04/06/07/08/09/12/13/14/17/18/26/35 | DemandaTransicaoServiceTests.java:34, isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.getStatus().value()).isEqualTo(status)) | 403/409/404/400 definidos em cada cenário; helper compara todo conteúdo e versão nas linhas35/36. |
| MOV-04 | DemandaTransicaoServiceTests.java:61, isEqualTo(1L) | Admin em quadro sem criador executa as4ações; nãoadmin403. Autor persistido Admin na linha62. |
| MOV-05 | DemandaTransicaoServiceTests.java:76, isEqualTo(destino.getId()) e status Em andamento | Destinos nãoadjacente e anterior; payload completo containsExactly na linha77. |
| MOV-10/11 | DemandaTransicaoServiceTests.java:101, status/destino/datas/motivo exatos | Concluido/Cancelado, categoria do mesmo quadro, LocalDate.now, campos incompatíveis null, motivo normalizado; assertions consecutivas. |
| MOV-15/16 | DemandaTransicaoServiceTests.java:125, status Em andamento/destino e três campos null | Snapshot anterior status/IDs/datas/motivo longo asserted separadamente; evento antigo isEqualTo(antigo). |
| MOV-16 ausências | DemandaTransicaoServiceTests.java:195, dataConclusao/dataCancelamento/motivo isNull | Ausências antigas não inventadas; IDs/status anteriores asserted. |
| MOV-19 movimento | DemandaTransicaoServiceTests.java:85, containsExactly(descrições dos dois movimentos) | Ação/descrição/autor/processo e dataHora atual comparados; count2. |
| MOV-19 fechamento | DemandaTransicaoServiceTests.java:107, isEqualTo(CONCLUSAO/CANCELAMENTO) | Todoscampos de evento incluindo descrição exata, timestamp atual e autor Dono asserted. |
| MOV-19 reabertura | DemandaTransicaoServiceTests.java:129, isEqualTo(REABERTURA), autor Dono, processoId e timestamp | Descrição JSON contém os6valores antigos completos. |
| MOV-20 | DemandaTransicaoServiceTests.java:232, conteudoPersistido isEqualTo(antes), versao isZero | CHECK físico separado em processos/historicos/gerenciamentos falha e reverte toda a transição. |
| MOV-21 | DemandaTransicaoServiceTests.java:79, versao v+1 | Destino/count1 do mesmo snapshot asserted; sem incremento duplo. |
| MOV-26 edição comum | DemandaTransicaoServiceTests.java:76, status/etapa preservados; numero Alterada | Colaborador mantém edição de outros campos, com somente EDICAO e autoria Colaborador. |
| Edge andamento em final | DemandaTransicaoServiceTests.java:187, etapa antiga isEqualTo(finalizada.getId()) | 3ações409 sem normalização; helper prova demanda/histórico/versão intactos. |
| MOV-01 legado | HistoricoArchiveTests.java:107, status().isUnauthorized(), conteúdo igual:108 | Rotas antigas continuam401 sem gravação;4rotas novas serão testadasT3. |
| MOV-24 remoção/lock | EtapaDemandConcurrencyTests.java:75, assertThatThrownBy(segunda.get).isInstanceOf(TimeoutException.class), resposta/órfãos/eventos :78/:89/:90 | Leitura/ação aguarda lock; destino removido404, ocupado409, sem órfão/evento parcial. Demais novas corridasT7. |
| MOV-27 legado | ProcessoArchiveTests.java:118, containsExactly(CRIACAO,EDICAO,MUDANCA_ETAPA,CONCLUSAO,REABERTURA,CANCELAMENTO), autores:119 | Rotas200 preservadas; fechamento na final oficial, reabertura necessária entre ciclos. |

### Assertions → AC

Todos40casos de DemandaTransicaoServiceTests usam somente os critérios da tabela acima: helper34–36 prova negativa sem efeitos; admin/salto/fechamento/reabertura provam estado e payload; negativos cobrem estados/destinos/motivo;5guardas genéricas e edição comum cobremMOV-26;3CHECKs cobremMOV-20. Nenhuma assertion sem requisito. Arrays de payload e JSON têm valores/assertions por campo; eventos não são inferidos de chamadas mock.

Regressões adaptadas às decisões explícitas AD-011/012/013: ProcessoArchiveTests usa colaborador para criar/editar e criador para transições, acrescenta reabertura antes de novo cancelamento, exige coluna CANCELADA e sequência/autores exatos. HistoricoArchiveTests conserva histórico manual de terceiro e usa criador para concluir, versão1paraarquivar. EtapaDemandConcurrencyTests conserva criação colaborativa, usa criador para mover, informa versão incrementada para preservar a prova de etapa ocupada e mantém404de destino removido na ação dedicada; PUT genérico agora400sem efeitos. Não reduzir especificidade para acomodar comportamento antigo incompatível.

Adequação T2 PASS para o serviço e rotas legadas alteradas. Versão/DTO/4rotasHTTP completasT3, corridas novasT7 e UI permanecem pendentes. Fontefuncional verificada432H2; revisão independente somente após última tarefa.

## T3: quatro APIs tipadas

Gate459Java/H2 PASS,27casos novos,0falhas/erros/skips; log TEMP/creral-movimentacao-af89b92e2e52431b8e1bf9156c10ba61/t3-final.log. Quatro PUTs200, DTOs com versão/IDs inteiros estritos, motivo textual e propriedades extras rejeitadas inclusive null. Fonte nova testada em HTTP, nenhum schema/banco real operado.

### AC → assertions

| Critério | file:line + assertion | Resultado definido |
| --- | --- | --- |
| MOV-01/02/03 | DemandaTransicaoApiTests.java:40 status().is(codigo), chamadas :73/:74/:75/:82, conteúdo/versão igual :41 | Cada rota401sem sessão/inválida/encerrada,403terceiro,409arquivo e mensagens exatas quando definidas. |
| MOV-05/10/11/15/21 | DemandaTransicaoApiTests.java:55 estado e etapaId isEqualTo(esperado), :56/:57/:58/:59/:60/:61 datas/motivo exatos, :62 opcionais null, :63 counts | Snapshot14campos, demanda correta, estado/destino exatos, data atual do servidor e fechamento incompatível limpo; versão1 persistida/retornada :49/:65. |
| MOV-19 | DemandaTransicaoApiTests.java:67 containsExactly(evento), containsExactly(Dono) | Uma ação/evento por endpoint; payload completo do evento já comprovado no serviçoT2. |
| MOV-22/23 | DemandaTransicaoApiTests.java:89/:91 recusa400, :76 recusa409mensagemexata, helper :40/:41 | Formato/ausência/negativo400; versãoantiga409 sem dados/histórico/versão alterados. |
| MOV-25 | DemandaTransicaoApiTests.java:79/:80 recusa404 Demanda não encontrada neste gerenciamento., helper :40/:41 | ID alheio/inexistente em todas4rotas não atravessa quadro. |
| MOV-06/07/18 | DemandaTransicaoApiTests.java:100/:101/:102/:103 com helper :40/:41 | Destino inválido400, removido/externo404, ambasfinais400 para mover/reabrir; sem efeitos. |
| MOV-12 e Done when DTOestrito | DemandaTransicaoApiTests.java:109 recusa400, :93 extras400 Dados da requisição inválidos., helper :40/:41 | Motivo ausente/vazio/branco/10001/número/boolean/mapa não convertido/truncado; extras não aceitos. |
| MOV-35 | DemandaTransicaoApiTests.java:118 recusa409mensagemexata, :119GET200, :121 isNull/asText isEqualTo(antigo) | Null/vazio/Pendente antigo consultáveis, nenhuma das4transições escreve ou normaliza. |
| MOV-08/13/17 | DemandaTransicaoApiTests.java:129 recusa409 com helper :40/:41 | Estado incompatível bloqueado em cada rota; matriz de estados ampliadaT2. |
| MOV-20 | DemandaTransicaoApiTests.java:135 recusa500mensagemexata com helper :40/:41 | CHECK de histórico no caminho HTTP reverte demanda/evento/versão. |
| MOV-27 | DemandaTransicaoApiTests.java:143 legado403, :145HTTP200entidade/id exato, :146versão1/:147count1 | Rotas antigas compartilham permissão e incrementam versão; formato200 preservado. |

### Assertions → AC

Todos27casos mapeiam às linhas da tabela:4sucessos,4matrizesauth/arquivo/versão/ID,4DTOs,2destinos,1motivo,4legadosdesconhecidos,4estadosincompatíveis,1rollbackHTTP e3compatibilidadeslegadas. Helper40–41 é assertion de código, mensagem quando especificada, conteúdo completo e versão; não só spy/callcount. Sucessos53–67 cobrem valores de todos14campos, metadados e counts. Dados antigos são consultados no HTTP e comparados literalmente.

Adequação T3 PASS; todas rotas novas exercitadas com sucesso/erros/edges definidos, nenhum teste removido/ignorado,27casos além dos432anteriores. Interface e corridas finais continuamT4–T7.

## T4: confirmação frontend

Gate602Vitest/14arquivos PASS,127casos novos; log TEMP/creral-mov-ui-0fccc14db03f4e5d9cb6220a95052a27/t4-final.log. Primeira execução restrita teve13arquivos com ENOENT no TEMP virtual do Vite e125casos da API passaram; não conta como gate. Repetição com TEMP real estável passou600, acrescentadas2provas de categoria duplicada e gate final602. Nenhuma alteração em cadastroConfirmado ou testes antigos.

### AC → assertions

| Critério | file:line + assertion | Resultado definido |
| --- | --- | --- |
| MOV-21/33 | apiTransicoes.test.js:36 toEqual(c.resposta), :37rota, :38PUT, :39headers, :40JSONbody toEqual(c.dados) |4ações confirmam snapshot completo com versão3a partirde2, demanda21/destino/status/campos atuais exatos; não enviar entidade/autor/datas. |
| MOV-32 | apiTransicoes.test.js:59 rejects.toMatchObject(ApiError200,mensagem), :61call1 |76casos de quadro/versão/arquivo/permissão/ID/status/destino/counts/categoria/8campospreservados rejeitam2xx semanticamenteincorreto. |
| MOV-32/33 encerramento | apiTransicoes.test.js:71 rejects.toMatchObject(status200) |12combinações de datas/motivo incongruentes recusadas. |
| MOV-32 formato/transporte | apiTransicoes.test.js:78/:82/:88/:94 rejects.toMatchObject(status/mensagem), :79/:89/:96call1 |201/204/JSONinválido,24HTTPs e4redes não confirmam nem repetemPUT. |
| MOV-05 legado | apiTransicoes.test.js:103 toEqual(c.resposta) | Mover preserva datas/motivo antigos em andamento; somente reabrir/encerrar os limpa. |
| MOV-14/32 categoria final única | apiTransicoes.test.js:109 rejects.toMatchObject(status200), :110call1 | Resposta com categoria final duplicada não confirma fechamento. |

### Assertions → AC

127casos:4sucessos→MOV-21/33;76incoerências+12camposclosure+4formato+24HTTP+4rede→MOV-32/33;1movimentaçãolegada→MOV-05;2duplasfinais→MOV-14/32. Assertion de resultado é snapshot completo ou erro exato; contagem de fetch complementa ausência de retry, não substitui estado.

Adequação T4 PASS,602casos preservando475anteriores. Datas do servidor são verificadas na interface por formato ISO e presença, sem presumir que relógio/fuso do navegador coincide; data atual comprovada no Java. Rascunho/diálogo/foco aindaT5/T6.

## T5: diálogo de ação

Gate630Vitest/15arquivos PASS,28casos novos, log TEMP/creral-mov-ui-0fccc14db03f4e5d9cb6220a95052a27/t5-vitest.log.

### AC → assertions

| Critério | Assertion | Resultado definido |
| --- | --- | --- |
| MOV-29 | AcaoDemanda.test.jsx:19 activeElement toBe(select), :20options toEqual(lista exata), :23aria-labelledby | Mover exclui etapa atual; reabrir permite ambas; finais excluídas e foco na seleção. |
| MOV-29 payload | AcaoDemanda.test.jsx:29alert exato/no save, :32toHaveBeenCalledExactlyOnceWith({etapaId:12}) | Seleção obrigatória; somente ID de trabalho escolhido após teclado. |
| MOV-30 | AcaoDemanda.test.jsx:36query combobox null/:37destino textual/:39requiredtrue/:40ajuda/:41/:42foco | Concluir/cancelar têm destino automático, motivo requerido e teto10000 visível. |
| MOV-12/30 | AcaoDemanda.test.jsx:50alert exato/:51value toBe(motivo)/:52no save/:58payload normalizado | BrancosUnicode/10001/emoji10002 recusados sem truncar rascunho; motivo10000 e multiline válidos. |
| MOV-30 conclusão | AcaoDemanda.test.jsx:63toHaveBeenCalledExactlyOnceWith({}) | Não enviar campos de encerramento inventados. |
| MOV-31 | AcaoDemanda.test.jsx:71buttonsdisabled/:72inputsdisabled/:74savecount1/no cancel/:75habilitado após resolver |4ações pendentes não duplicam nem abandonam operação. Bloqueio externo também não envia. |
| MOV-32 | AcaoDemanda.test.jsx:83alert/:84select12/:85motivo literal/:86aoErro(error)savecount1/:87dialog existe | Falha conserva seleção/motivo e diálogo, comunica erro ao quadro. |
| MOV-29 vazio | AcaoDemanda.test.jsx:92orientação/:93submitdisabled/:94voltar habilitado | Sem trabalho não inventar destino; saída sem mutação disponível. |
| MOV-32 voltar | AcaoDemanda.test.jsx:104cancelcount1/no save | Descartar diálogo por teclado não grava. |

### Assertions → AC

28casos:2listas+2seleções→MOV-29;2destinos+4motivosinválidos+2motivosválidos+1conclusão→MOV-12/30;4pending+4bloqueioexterno→MOV-31;4erros+1voltar→MOV-32;2vazios→MOV-29. As assertions de payload comprovam os dados submetidos; estado visual/rascunho/foco comprovam resposta do componente. Nenhum teste sem âncora.

Adequação T5 PASS, nenhum teste removido/ignorado. Busy/Voltar/Sair global, cartões/cache e troca de quadro sãoT6.

## T6: integração do quadro — concluída

Teste focal 31/31 PASS, log TEMP/creral-mov-ui-0fccc14db03f4e5d9cb6220a95052a27/t6-transicoes-2.log. Não substitui gate integral, que continua pendente devido às assertions antigas sobre botões das colunas finais; confirmação solicitada ao usuário antes de modificá-las. Nenhum teste removido/ignorado. Correção das referências de linha do mapa T5 acima, sem alterar seus testes/fonte.

### AC → assertions

| Critério | file:line + assertion | Resultado definido |
| --- | --- | --- |
| MOV-28 | QuadroTransicoes.test.jsx:44 toEqual(lista ações), :50 queryByRole(button).toBeNull() | Ações exatas por estado; consulta somente para arquivado/sem permissão. |
| MOV-35 | QuadroTransicoes.test.jsx:55 ausência de ações, :56 mensagem literal, :57 status literal | Null/vazio/desconhecido não normalizados. |
| MOV-29/30/33 | QuadroTransicoes.test.jsx:65 etapa exata, :66 status/preservação, :67 contagem, :68 cache versão1, :70 payload completo | Quatro ações enviam destino/motivo/versão exatos, atualizam cartões/count/cache; datas e campos adicionais corroborados por apiTransicoes.test.js:36. |
| MOV-31 | QuadroTransicoes.test.jsx:81–85 disabled true, :87 sem Voltar/sem envio extra, :90 desbloqueio, :161/:162/:164 Sair e token | Mutação pendente impede repetição e abandono; libera após confirmação. |
| MOV-32 | QuadroTransicoes.test.jsx:97 erro literal, :98 diálogo/bloqueio, :99/:100 rascunho, :101/:104 sequência GET PUT GET | Snapshot incoerente exige GET manual, preserva diálogo, nenhum retry automático. |
| MOV-32 HTTP/rede | QuadroTransicoes.test.jsx:129 mensagem, :130/:131 diálogo e bloqueio, :132/:133 rascunho, :134 cache0, :135 destino anterior, :137/:140 sequência exata | Oito casos de HTTP409/rede nas quatro ações preservam estado e rascunho e aguardam atualização manual. |
| MOV-33 foco | QuadroTransicoes.test.jsx:74 isConnected true/tag BUTTON, :146 activeElement.toBe(origem) | Fecha com foco em elemento existente; voltar sem alterar restaura origem sem PUT. |
| MOV-34 | QuadroTransicoes.test.jsx:113 diálogo null, :115 outro quadro sem cartão antigo, :116 cache antigo não aplicado | Troca de quadro ignora resposta de ação do quadro anterior. Consulta antiga já coberta pelos testes existentes; revisão final T7/Verifier. |

### Assertions → AC

31 casos: 3 estados e 2 permissões→MOV-28; 3 legados→MOV-35; 4 sucessos→MOV-29/30/33; 4 pending e 1 Sair→MOV-31; 4 snapshots incoerentes, 8 HTTP/rede e 1 voltar→MOV-32/33; 1 troca de quadro→MOV-34. As assertions verificam estado/payload/foco; contagens complementam ausência de repetição. Adequação integral e commit T6 permanecem pendentes.

### Fechamento T6 em 07/10/2026

Gate integral: **661/661 testes, 16/16 arquivos PASS**, sem exclusões ou skips. Log: C:/Users/Marco/AppData/Local/Temp/creral-tlc-retomada-e09b0601b70b48dc82f7ea9b26cf3efc/t6-gate.log. Controle anterior reproduziu 18 falhas nas assertions de colunas finais; log t6-baseline.log, preservado. Usuário autorizou explicitamente corrigir somente as três verificações em QuadroEtapas.test.jsx:59/:519/:560. A seleção agora exige ausência de botões com nome Editar etapa ou Remover etapa; identificação, setor, posição e contagens permanecem verificados. Reabrir nos cartões segue MOV-28.

Check A: os oito grupos de critérios e respectivos valores têm evidência na tabela T6 acima; MOV-34 consulta antiga também em QuadroDemandas.test.jsx:345, expect(screen.queryByRole("article", { name: "D-42 — Maria" })).toBeNull(), e :346, expect(atualizar.mock.calls.map(([registro]) => registro.id)).toEqual([10]); consulta atrasada não aplica cartão nem cache ao novo quadro. Check B: estado visual, rascunho, destino/status, payload, cache e foco observados; callcount apenas complementa a prova de ausência de repetição. Check C: os 31 casos têm mapeamento reverso acima; três correções mantêm FIN-15 e compatibilizam MOV-28, sem acrescentar requisito. Check D: localização e padrões de testes seguem a matriz tasks.md e docs/OPERACAO.md.

Adequação T6 PASS. Código da integração já estava preservado por b0ddbed; não foi refeito. O fechamento atômico registra as correções autorizadas e os status. T7 e Verifier continuam pendentes; a feature ainda não está entregue.

## T7: integração e operação — concluída

Novos testes: 10 casos Java de disputa entre ações com mesma versão e entre movimento/arquivo/remoção/reordenação, e 3 fluxos Edge com ciclo completo, conflito externo/pending e terceiro/arquivo. Gate H2 inicial 469/469 PASS; revisão adicional da ordem persistida incluída depois. Lint e build TEMP PASS. Vitest 661/661 da T6 reutilizado: código frontend e testes unitários permanecem inalterados. Nenhum skip/delete ou alteração de requisito.

### AC → assertions

| Critério | file:line + assertion | Resultado definido |
| --- | --- | --- |
| MOV-05/10/11/15/21/33 | frontend/e2e/transicoes.spec.js:53 `expect(res.request().postDataJSON()).toEqual(...)`; :55 `expect(salvo.gerenciamento.versao).toBe(atual.gerenciamento.versao + 1)`; :58 `expect(salvo.demandas[0]).toEqual(...)`; :62 `expect(salvo.etapas.map(e => e.quantidadeDemandas)).toEqual(...)` | Seis ações reais: salto, retorno, conclusão, duas reaberturas e cancelamento; demanda completa/preservação, destino, status, datas incompatíveis null, motivo normalizado, counts e versão. Data do servidor exata já afirmada nos testes Java. |
| MOV-16/19 | frontend/e2e/transicoes.spec.js:78 `expect(eventos.map(e => e.acao)).toEqual([...])`; :79 `expect(eventos.map(e => e.usuario)).toEqual(Array(7).fill("Dono"))`; :80 `expect(eventos[6].descricao).toContain("Cliente desistiu")`; :81 `expect(eventos.every(...)).toBe(true)` | Sete eventos incluindo criação, autoria real e dataHora; encerramentos anteriores preservados. Snapshot literal completo inclusive IDs/datas/null/motivo longo comprovado em DemandaTransicaoServiceTests.java:132–136. |
| MOV-28/29/30 | frontend/e2e/transicoes.spec.js:46 `expect(...allTextContents()).toEqual([...])`; :119 `await expect(c.card.getByRole("button")).toHaveCount(0)`; motivo payload :53 | Somente destinos de trabalho e ações por permissão; motivo normalizado. Obrigatoriedade/limite do motivo já afirmados em AcaoDemanda.test.jsx e API/service. |
| MOV-31/32 | frontend/e2e/transicoes.spec.js:99 `await expect(page.getByRole("alert")).toHaveText(...)`; :100 `await expect(...).toHaveValue("  Manter rascunho  ")`; :101 `expect(puts).toEqual([{ versao: 1, motivo: "Manter rascunho" }])`; :109 `await expect(...).toBeDisabled()`; :112 `expect(puts).toEqual([...])` | Conflito preserva rascunho e exige GET manual; um PUT por envio manual; pending impede abandono e outras mutações. |
| MOV-02/03 | frontend/e2e/transicoes.spec.js:121 `expect(negada.status()).toBe(403)`; :122 `expect(await c.consultar()).toEqual(antes)`; :124 `expect(fechada.status()).toBe(409)`; :125 `expect((await fechada.json()).erro).toBe(...)`; :126 `expect((await c.consultar()).demandas).toEqual(antes.demandas)` | Terceiro/arquivo sem alterações; mensagem de arquivo exata. |
| MOV-20/21/23/24 | sistema/src/test/java/com/patp/sistema/DemandaTransicaoConcurrencyTests.java:63 `assertThatThrownBy(...segunda.get(200,...)).isInstanceOf(TimeoutException.class)`; :66 `assertThat(resposta.getStatus()).isEqualTo(409)`; :68 `assertThat(conteudoPersistido()).isEqualTo(antes)`; :69 `assertThat(...getVersao()).isEqualTo(1L)`; :71–77 valores persistidos/evento/autor | Segunda ação espera commit, perde por versão e não grava; uma ação/evento/versão com destino/status/campos corretos nas quatro ações. Rollback com CHECK de demanda/histórico/versão já coberto na T2. |
| MOV-24 estrutura | sistema/src/test/java/com/patp/sistema/DemandaTransicaoConcurrencyTests.java:104 espera commit; :107 `assertThat(...getStatus()).isEqualTo(esperado)`; :109–115 destino, status, evento, versão, arquivo, existência; :116–117 ordem; :119 referência íntegra | Seis disputas em ambas as ordens de commit não deixam estado impossível, referência removida, evento parcial ou ordem incoerente. |
| MOV-33/34 | frontend/e2e/transicoes.spec.js:65 `expect(...document.activeElement.isConnected...).toBe(true)`; :83 recarga/ID e `expect((await c.consultar()).demandas).toEqual(atual.demandas)`; QuadroTransicoes.test.jsx:115/116 e QuadroDemandas.test.jsx:345/346 | Foco existente e persistência após reload; respostas antigas ignoradas nos testes unitários existentes. |

### Assertions → AC

Ciclo completo e recarga → MOV-05/10/11/15/16/19/21/28/29/30/33. Conflito/pending → MOV-23/31/32. Terceiro/arquivo → MOV-02/03/28. Quatro disputas de mesma versão → MOV-20/21/23/24; seis disputas de estrutura → MOV-24. Conta e criação dos helpers são precondições para esses fluxos, com status explícitos. Duas capturas desktop/mobile verificadas visualmente, sem cortes de controles nem rolagem horizontal.

Primeira invocação integral do navegador falhou na inicialização do processo com código nativo 3221226505; nenhum teste executado, não conta como PASS. Segunda passou autocadastro e falhou consulta do segundo quadro em demandas.spec.js:182: trace mostrou GET sem resposta com ERR_CONNECTION_RESET. Diagnóstico isolado inalterado passou 1/1, sem retries configurados. Logs de falha preservados e nova execução integral pendente. Causa raiz da falha de conexão não comprovada.

Primeira seleção MySQL: 112 casos executados, 5 erros de conversão Timestamp/LocalDateTime nos testes do serviço, sem falhas de assertion. Correção tipada solicitada explicitamente ao usuário; gate não aprovado ainda. Nova instância própria: MySQL 8.0.43 em 127.0.0.1:33819, datadir TEMP/mysql/data e schema creral_mov_tests, conta restrita a esse schema. Nenhum dado/banco real acessado.

Atualização de gates em 07/10/2026: navegador integral **7/7 PASS** em cinco arquivos (t7-e2e-full-3.log), sem retries/skips; inclui os três fluxos novos e quatro regressões. H2 completo após adaptação JDBC e prova adicional de ordem: **469/469 PASS**, t7-java-full-2.log. Usuário autorizou explicitamente a correção da leitura de dataHora em três verificações do serviço. Intervalos isBetween e todas as assertions de estado permanecem iguais; leitura JDBC agora usa LocalDateTime tipado em :88–89/:109/:131.

A segunda seleção MySQL já executou os 112 testes sem erros de conversão, mas teve cinco falhas de horário: DATETIME lido com três horas de diferença devido à conexão herdada serverTimezone=UTC frente à JVM local. Não se ampliou o intervalo esperado. Somente a URL da conexão fictícia foi corrigida para connectionTimeZone=LOCAL, sem alterar configuração/código da aplicação. A documentação oficial descreve a conversão entre fuso da JVM/conexão e o mapeamento DATETIME: [MySQL Connector/J, Datetime types processing](https://dev.mysql.com/doc/connector-j/en/connector-j-connp-props-datetime-types-processing.html). Logs anteriores de ambas as falhas MySQL preservados; resultado da terceira execução ainda precisa ser conferido.
### Fechamento T7

Terceira seleção MySQL: **112/112 PASS**, zero falhas/erros/skips, BUILD SUCCESS, t7-mysql-selected-3.log. URL fictícia connectionTimeZone=LOCAL; o intervalo de horário das assertions foi preservado. Proveniência e grants em TEMP/mysql/runtime.json, owned-processes.json, setup.log e provenance.log. Instância fica disponível somente para a revisão independente e será encerrada ao final.

Gates finais: Java/H2 469, Vitest 661, lint/build TEMP e Edge/H2 7 PASS. Nenhum caso perdido frente à base 390Java/475Vitest/4E2E. Check A: todos os critérios/edges da T7 mapeados acima; Check B: valores persistidos e visuais, não apenas chamadas; Check C: todos os 13 casos novos têm requisito; Check D: padrões/matriz do projeto respeitados. Adequação T7 PASS, sem SPEC_DEVIATION. Revisão independente permanece obrigatória e pendente.

Correção de referências das provas de terceiro/arquivo: frontend/e2e/transicoes.spec.js:117 ausência de ações; :119 `expect(negada.status()).toBe(403)`; :120 `expect(await c.consultar()).toEqual(antes)`; :122 `expect(fechada.status()).toBe(409)`; :123 mensagem exata; :124 `expect((await c.consultar()).demandas).toEqual(antes.demandas)`. Pending global :108, campo :109 e envios manuais exatos :111. Referências anteriores :119–126 são substituídas por estas linhas conferidas.
## T8: mensagens e ausência de efeitos na API — concluída

Primeira revisão independente (diff400f8c5..5defae7) retornou FAIL:34/35 ACs completos mais edge parcial. Sensor14compiláveis,12killed e2survived; controle antes/depois112/112 e comparação4173arquivos/porcelain intacta. Código da aplicação já continha as mensagens corretas, mas os testes só exigiam409. Relatório da rodada1 preservado no commit da T8; lessonsL-004/L-005 registradas pelo Verifier.

Novo DemandaTransicaoMensagemTests.java contém dez casos HTTP sobre serviço/DB reais, sem mocks: concluir/cancelar com final necessária ausente/duplicada (4) e mover/concluir/cancelar com Em andamento em CONCLUIDA/CANCELADA (6). Nenhum teste antigo modificado, removido ou ignorado; nenhuma fonte da aplicação alterada.

| Critério | file:line + assertion | Resultado definido |
| --- | --- | --- |
| MOV-14 | DemandaTransicaoMensagemTests.java:25–26 chama helper com orientação explícita; :43 `.andExpect(status().isConflict()).andExpect(jsonPath("$.erro").value(mensagem))` |409 e instrução de preparar as etapas finais do gerenciamento antes de encerrar. Spec define o sentido da orientação, não texto literal; teste fixa o texto vigente que atende esse sentido. Ambas ações, ausência e duplicidade. |
| Edge MOV-05/10/11 | DemandaTransicaoMensagemTests.java:35–36 chama helper com literal da spec; :43 compara HTTP409 e `$.erro` com mensagem |Estado da demanda incompatível com a etapa atual. Solicite a correção do registro. Ambas categorias finais e três ações; sem normalização. |
| Sem efeitos MOV-14/edge | DemandaTransicaoMensagemTests.java:44 `assertThat(conteudoPersistido()).isEqualTo(antes)`; :45 `assertThat(quadros.findById(quadro).orElseThrow().getVersao()).isEqualTo(versao)` |Etapas/demandas/comentários/históricos literalmente intactos e versão preservada em todos os dez erros. Prova também ausência de preparação implícita. |

Mapa reverso:4casosfechamento→MOV-14;6casosincoerentes→edgeMOV-05/10/11; helper43–45→mensagem/status/sem efeitos desses mesmos cenários. Nenhum teste reivindica requisito novo. As chamadas HTTP exercitam o serviço real e a persistência; testes de serviço existentes continuam fornecendo as provas de estado/consulta e rollback.

Gates após a T8: **479/479 Java/H2**, t8-java-full.log; **122/122 selecionados MySQL fictício**, t8-mysql-selected.log; falhas/erros/skips0 e BUILD SUCCESS. Java aumentou10casos frente à T7, MySQL selecionado aumentou10. Frontend permanece byteigual à revisão independente: **661Vitest,lint/build,7E2E PASS** em verifier-vitest-2.log, verifier-lint.log, verifier-build.log e verifier-e2e.log; reutilização justificada, nenhum reparo nessa camada. Check A/B/C/D PASS, adequado aos contratos e padrões do projeto. Task status antes do commit atômico; feature permanece Implementing até novo Verifier/sensorPASS.
## Fechamento independente — 07/10/2026

Revisão independente final **PASS: 35/35 critérios e edge**, sem lacunas. Gates próprios da rodada 2: **479 Java/H2 e 122 MySQL fictício PASS**, sem falhas/erros/skips. Os gates frontend independentes **661 Vitest, lint/build e 7 Edge/H2 PASS** foram reutilizados com comprovação de fontes e testes inalterados, conforme validation.md.

Sensor: rodada 1 detectou 12/14 falhas simuladas; rodada 2 detectou as duas restantes com as novas assertions HTTP (M08: 6 falhas; M09: 4 falhas). Controles da rodada 2: 122/122 antes e depois; SHA256/porcelain de 4.178 arquivos preservados. Relatório final conserva o FAIL da primeira rodada e sua proveniência; validate_state PASS.

MySQL fictício próprio encerrado via mysqladmin após conferir datadir, porta 33819, PID e versão; listener ausente e Shutdown complete confirmados. Evidências de encerramento em TEMP/mysql. Nenhum banco real, push ou deploy. Roteiro humano em docs/OPERACAO.md disponível; aceitação humana ainda pendente. AD-023 registra edição futura pelo criador/admin, sem acrescentar edição ao escopo da movimentação entregue.