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
