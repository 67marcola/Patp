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
