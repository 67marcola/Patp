# Comentários por demanda — evidências de execução

**Estado:** T1 concluída como tarefa com gate aprovado. Feature parcial, sem declaração de critério Verified antes do Verifier. Base de planejamento 3a5934c.

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
