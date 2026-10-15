# Evidências — acesso pela internet

## Planejamento e autorização — 14/10/2026

Usuário escolheu Vercel/Render/Aiven, base nova de testes e aprovação administrativa de cadastros. Após apresentação do fluxo proposto, sessão8h, preservação local e publicação separada, pediu continuar para rodar no celular com dados móveis. Usuário possui somente Vercel; foi orientado a criar Render/Aiven gratuitos. Não houve compartilhamento de senha, criação de conta, contratação ou publicação.

Spec20NET validada sem erros/avisos. Design reutiliza componentes atuais; aprovação em tabela somente online, sem entidade JPA nova exigida pela validação local. Plano11 tarefas, testes focados por componente e um integrado; alvo oito invocações autor + duas Verifier/sensor, sem repetir suítes históricas ou medir tokens. Gates Build não repetem testes. Quantidade real/tentativas serão registradas, sem apagar testes existentes.

Revisão independente de spec identificou quatro ambiguidades, todas resolvidas antes de implementar: explicitar schema local intacto; obrigatoriedade do modo internet no artefato Render; vazio sem tabelas e recusa de preparo parcial; distinção entre aprovação comum e bootstrap administrativo. Spec permanece sem conclusão técnica enquanto tarefas/Verifier/acesso externo não forem comprovados.

## T1 — política isolada

Premissas: false por padrão; nenhuma entidade/DDL automático. Arquivos: AprovacaoCadastroService.java e AprovacaoCadastroServiceInternetTests.java, além deste registro/tasks/spec/diagnóstico. Sucesso: estado JDBC, recusa de pendência, repetição/concorrência e rollback preservam dados, sem tabela no modo local.

Gate Quick: Maven `-Dtest=AprovacaoCadastroServiceInternetTests test` em cópia física TEMP/creral-internet-t1t7-20261014/sistema, H2 próprio. Tentativa 1, 5 testes, 0 falhas/erros/skips. Total autor até aqui: 1 invocação comportamental. Nenhum teste antigo alterado/excluído/ignorado.

Check A, resultados derivados de NET; caminhos abaixo relativos a sistema/src/test/java/com/patp/sistema/AprovacaoCadastroServiceInternetTests.java:

| Critério | Assertion file:line | Outcome do spec | Coberto |
| --- | --- | --- | --- |
| NET-07 local sem tabela | :44 `assertThat(local.listarPendentes()).isEmpty()`; :45 count `isEqualTo(3)` após operações sem tabela | Sem consulta online/mutação | Sim |
| NET-08/09 pendência/ausência | :50 `containsEntry("APROVADO", false)` com autor/hora null; :52 `hasMessage(MENSAGEM_PENDENTE)` | Pendente não autorizado | Sim |
| NET-10 lista segura/ordem | :54 `containsExactly(new Pendente(...),...)` | ID/nome/setor/email, ID crescente | Sim |
| NET-11/12 aprovar/repetir/preservar | :64 aprovado true/autor2; :65 hora not null; :67 `isEqualTo(primeira)`; :68 conta `isEqualTo(conta)` | Primeira autoria/hora e dados/papel intactos | Sim |
| NET-11/12 falha/404 | :76/79 `isEqualTo(antes)`; :78 status `isEqualTo(404)` | Sem efeito parcial | Sim |
| NET-11 concorrência | :90 true; :91 autor `isIn(2L,3L)`; :92 hora; :94 primeira preservada; :95 count1 | Uma primeira aprovação | Sim |

Check C, mapeamento reverso:

| Assertions | Requisito | Manter |
| --- | --- | --- |
| :44–45 | NET-07 | Sim |
| :50–58 | NET-08/09/10 | Sim |
| :64–69 | NET-11/12 | Sim |
| :75–79 | NET-11/12 | Sim |
| :90–95 | NET-11 concorrência | Sim |

Adequação A/B/C/D: outcomes cobertos, payload conferido, sem assertions rasas; guidelines tasks/AD-035 e padrões JUnit existentes. Sem desvio. T1 completa localmente; requisitos continuam Implementing até Verifier.
