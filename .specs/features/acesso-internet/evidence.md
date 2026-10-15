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

## T2 — entrada e sessão

Premissas: contrato local de seis campos conservado; relógio UTC injetável somente para prova do limite absoluto. Arquivos: UsuarioService/UsuarioController/SessaoService e AutenticacaoInternetTests.java. Sucesso:202 sem sessão,403 pendente,401 protegido, cadastro transacional e expiração/revogação.

Gate Quick em mesma cópia TEMP/H2: Maven `-Dtest=Autenticacao*InternetTests test`. Tentativa1 falhou em compilação do helper novo (throws ausente, zero casos executados); correção só da assinatura, nenhuma assertion alterada. Tentativa2:6/6, zero falhas/erros/skips. Total autor:3 invocações, incluindo falha de compilação. Nenhum teste antigo alterado.

Check A, assertions em sistema/src/test/java/com/patp/sistema/AutenticacaoInternetTests.java:

| Critério | Assertion file:line | Outcome do spec | Coberto |
| --- | --- | --- | --- |
| NET-08 payload e persistência | :59 `getStatus().isEqualTo(202)`; :60 árvore `isEqualTo` JSON exato; :63–68 nome/setor/email/FUNCIONARIO/BCrypt e false/null/null |202 PENDENTE, sem token/promoção, conta persistida | Sim |
| NET-09 proteção/duplicado | :52–54 status403/mensagem/token ausente via :74; :75 status401; :77 `isEqualTo(antes)` | Pendente não entra, anônimo não consulta, duplicação sem efeito | Sim |
| NET-12 rollback | :82 status500; :83 `findByEmail(...).isEmpty()`; :84 count0 | Sem conta parcial quando pendência falha | Sim |
| NET-16 login/prazo | :92–99 status200 e seis campos/valores/token; :102 status200 aos7h59m59s; :104 status401 às8h | Prazo absoluto sem renovação por uso | Sim |
| NET-17 saída | :111 status200 logout; :112 status401; :115–116 senha errada sem token | Revogação efetiva | Sim |
| NET-07 local | :129 status200; :131–134 seis campos/token/id/nome/setor/email/papel; :135–136 me200 após DROP da tabela online | Cadastro/sessão locais sem tabela nova | Sim |

Check C reverso: :59–68→NET-08; :52–54/75–77→NET-09/12; :82–84→NET-12; :92–104→NET-16; :111–116→NET-17/09; :129–136→NET-07. Todos mantidos, nenhum teste sem requisito. Adequação A/B/C/D aprovada, payloads exatos e estado persistido; guidelines tasks/AD-035. Sem desvio; Implementing até Verifier.

## T3 — API administrativa

Premissas: controller somente com flag internet; papel e autor obtidos pela sessão/DB. Arquivos: AprovacaoCadastroController.java e AprovacaoCadastroApiInternetTests.java. Sucesso: GET/POST seguros, contratos exatos e persistência íntegra. Gate Quick Maven `-Dtest=AprovacaoCadastroApiInternetTests test`, TEMP/H2. Tentativa1:5 casos,1 falha na comparação IntNode/LongNode do expected recém-criado; JSON recebido tinha os mesmos campos/valores/ordem. Usuário autorizou explicitamente “Sim, corrigir a comparação (recomendado)”, conforme mensagem repassada pelo root. Aplicado somente `readTree(writeValueAsString(lista))`, sem mudar campos/valores/ordem/cenários. Tentativa2:5/5, zero erros/falhas/skips. Total autor:5 invocações; nenhum teste antigo alterado/excluído/ignorado.

Check A, assertions em sistema/src/test/java/com/patp/sistema/AprovacaoCadastroApiInternetTests.java:

| Critério | Assertion file:line | Outcome do spec | Coberto |
| --- | --- | --- | --- |
| NET-10/11 anônimo | :30/31 status `isEqualTo(401)`; :32 estado `isEqualTo(antes)` | GET/POST401, sem alteração | Sim |
| NET-10/11 funcionário | :37–39 status403; :40 `isEqualTo(antes)` com body tentando admin | GET/POST403, sem alteração | Sim |
| NET-10 vazio/lista | :45 status200/vazio; :48 status200; :49–51 árvore exata ID/nome/setor/email em ordem |200 seguro e ordenado | Sim |
| NET-11/12 aprovar/repetir | :59–60 status200/JSON `{id,estado:APROVADO}`; :62 true/autor da sessão; :63 hora; :65–68 repetição/conta iguais | Autoria/hora/papel/senha conservados | Sim |
| NET-11/12 inexistente/falha | :74 status404/mensagem; :77 status500; :78 `isEqualTo(antes)` | Sem efeito parcial | Sim |

Check C reverso: :30–32→NET-10/11; :37–40→NET-10/11; :45–51→NET-10; :59–68→NET-11/12; :74–78→NET-11/12. Todos necessários/mantidos. Adequação A/B/C/D aprovada; padrões/guidelines tasks/AD-035. Sem desvio de contrato; Implementing até Verifier.
