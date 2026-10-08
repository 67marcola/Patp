# Evidências de edição de demandas

## Planejamento — 07/10/2026

AD-025/026 fecham número fixo e correção informativa de encerradas. Usuário autorizou implementação local. validate_spec --strict e validate_tasks --strict aprovados, zero erros/avisos, depois de corrigir a ligação T4→T5 no diagrama. Oito tarefas, execução inline; Verifier independente obrigatório após T8.

## T1 — parcial, gate ainda não aprovado

Arquivos: EditarDemandaRequest.java e DemandaEdicaoRequestTests.java. DTO não possui número, ID ou campos de ciclo. Reutiliza os deserializadores estritos de versão e datas do cadastro.

Primeira execução Maven/H2: 19 casos, 14 passaram e 5 falharam, 0 erros/skips. As cinco falhas são da expectativa de mensagem escrita no novo teste: o deserializador existente usa `A versão deve ser um número inteiro.`, mas o teste novo esperava `Informe uma versão inteira válida.`. Nenhum teste alterado depois dessa execução. Correção exata da expectativa foi apresentada ao usuário, mantendo todos os cinco tipos inválidos e a recusa do parser. T1 não pode ser marcada concluída antes da resposta e do gate PASS.

Log preservado: TEMP/creral-edicao-32565590f1a14a198df2fc432b1bdfeb/t1.log. Primeira tentativa de comando não iniciou Maven porque a resolução de diretório no PowerShell retornou null; o comando foi corrigido com o caminho absoluto do TEMP. Nenhum banco real acessado.

Também foi apresentada a adequação dos testes antigos em ProcessoArchiveTests, DemandaTransicaoServiceTests e nos três status de QuadroTransicoes.test.jsx. Manter todos os cenários e provas de criação, autorização, ciclo, autoria e versão; não enfraquecer/excluir/ignorar casos. Essa adequação ainda aguarda resposta.

### Fechamento T1

Usuário respondeu pode continuar após as duas perguntas, autorizando ambas. Expectativa de versão corrigida exatamente para o texto do validador existente. Gate t1-2.log: **19 PASS, 0 falhas/erros/skips**, BUILD SUCCESS. Código do validador existente inalterado; todos os cinco tipos inválidos mantidos. T1 Complete, requisitos de endpoint/serviço ainda Implementing.

| AC → prova | file:line e assertion | Outcome |
| --- | --- | --- |
| EDI-05/09/13, entrada | DemandaEdicaoRequestTests.java:18 `containsExactly(2L,"Cliente","Ana","Alta",LocalDate.of(2020,2,29),null,LocalDate.of(2026,10,7),"Texto")` | Todos os campos informativos e versão corretos, data/null preservados. |
| EDI-12/27, campos proibidos | DemandaEdicaoRequestTests.java:26 `hasMessageContaining("Dados da requisição inválidos.")` | Oito campos desconhecidos/ciclo/número recusados. |
| EDI-13, versão tipada | DemandaEdicaoRequestTests.java:32 `hasMessageContaining("A versão deve ser um número inteiro.")` | Cinco tipos inválidos recusados sem coerção. |
| EDI-09, datas | DemandaEdicaoRequestTests.java:38 `hasMessageContaining("Informe uma data ISO válida ou null.")` | Cinco formas inválidas recusadas, incluindo data impossível. |

Mapa reverso: entrada→EDI-05/09/13; campos proibidos→EDI-12/27; versões→EDI-13; datas→EDI-09. Check A/B/C/D PASS: outcomes definidos, valores de todos os campos, nenhum teste especulativo; padrões do pom/testes Spring Boot. Não afirmar que 401/HTTP ou limites de serviço foram provados pelo parser; essas provas pertencem às tarefas seguintes.
