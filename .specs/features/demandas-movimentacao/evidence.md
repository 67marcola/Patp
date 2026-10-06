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
