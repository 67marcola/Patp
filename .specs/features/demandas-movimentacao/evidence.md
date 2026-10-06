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
