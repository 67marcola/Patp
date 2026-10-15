# Evidências operacionais do piloto

## Fechamento técnico — 14/10/2026

**Concluído e verificado — tudo ok. Incluído no diagnóstico.** T1 d526ccd, T2 62a8ba0 e T3 330d984; oito critérios técnicos Verified no [relatório independente](validation.md). Verifier fresco aprovou seis testes Node, lint e um cenário integrado real em MySQL fictício com dois contextos Edge, reinício e falhas, em 48,7s. Sensor único validate→update detectado pela assertion que recusa falso Piloto pronto em schema vazio; nenhum sobrevivente ou correção obrigatória. Não houve lição nova em PASS limpo.

Integridade independente: 4.701 hashes, HEAD e porcelain idênticos antes da escrita do relatório; scratch físico removido, sem compartilhamento de caches/target. Nenhum teste antigo alterado ou removido. Total de dez invocações comportamentais (autor sete, Verifier três, incluindo tentativas e sensor), 50% menos que a referência de vinte em AD-035; sem medir tokens ou repetir suítes completas. Gates documentais de fechamento: validate_spec --strict e validate_state exit0, zero erros/avisos; check_commit OK e diff-check sem erro. Nenhum teste comportamental repetido para fechar os documentos.

Após o Verifier liberar a fixture, o orquestrador conferiu por SQL porta 33823 e datadir exato C:/Users/Marco/AppData/Local/Temp/creral-arquivo-049cfd5cc86d4d8b840e41f1710d7a43/mysql/. mysqladmin shutdown exit0, nenhum listener 33823 restante. MySQL real 3306/PID5560, backend 8081/PID5184 e frontend 5173/PID23848 preservados. Nenhum diretório do banco fictício preexistente foi apagado.

Entrega concluída é a preparação técnica. Serviço piloto real não iniciado; acesso de um segundo computador físico, Ctrl+C físico e aceite dos estudantes ainda não observados. O gate usa encerrar, que segue a mesma rotina de fechamento. Roteiro de início e conexão em docs/OPERACAO.md; não transformar prova localhost em confirmação da rede física. Os checkpoints abaixo são históricos e não reabrem passos já concluídos.

## T3 guia implementado

OPERACAO.md descreve comandos reais deste computador, seleção do IPv4/porta, URL única, clientes somente navegador, servidor ligado, MySQL persistente, sessões após reinício, encerramento próprio e diagnóstico de dependência/estrutura/rede. Distingue previewH2 temporário, pilotoMySQL e futuro ambiente empresarial. Não informa acesso externo observado: esse teste humano continua pendente. Gate documental: validate_spec strict0 e diff-check; nenhuma repetição de teste por edição documental. T1 d526ccd,T2 62a8ba0,T3 neste commit, ainda Implemented até Verifier fresco.

## T2 ciclo persistente implementado e aprovado

14/10/2026. Um único teste integrado cobre o iniciador real, build Java/Vite em TEMP, MySQL8.0.43 fictício33823, dois contextos Edge independentes, reinício e falhas. A fixture confere porta/datadir exatos antes de qualquer DDL, cria schemas/usuário exclusivos creral_piloto_*, e os apaga no final. O banco real3306 nunca é banco de testes.

Último gate `npm run test:piloto:integracao`:1/1 PASS,45,3s,0falhas/skips;lint final PASS. Compilação Java e build Vite fazem parte do ciclo real e usam TEMP. Dois usuários comuns criam/consultam o mesmo quadro/demanda; colega não tem Nova etapa; após encerramento e reinício, novo login e snapshot integral idêntico. Captura TEMP/piloto-duas-sessoes-80987167678a.png conferida visualmente.

| Critério / resultado | Evidência frontend/scripts/piloto-integracao.test.mjs | Coberto |
| --- | --- | --- |
| PIL-01 UI/API mesma origem | :80 HTML identificado; :81 API401 sem sessão; :83 bundle sem localhost:8081/api; :99 origens de API exatamente[url] | Sim |
| PIL-02 persistência | :103 MySQL vivo e1processo após encerrar; :105 login da conta preservada; :106 deepEqual(depois,antes) após reinício | Sim |
| PIL-03 validar sem criação | :109 processo inválido exit1/sem pronto e tabela count0 no schema vazio | Sim |
| PIL-04 falhas sem falso sucesso | :79 porta ocupada exit1/sem pronto e página do servidor original segue acessível; :111 JAVA_HOME ausente exit1/sem pronto | Sim, entradas inválidas nos unitários |
| PIL-05 saída/cleanup próprios | :62 exit0 em10s; :64 temporário ENOENT; :103 servidorMySQL continua disponível; :110 falha também limpa temporário | Sim |
| PIL-06 auth/segredos | :81 anon401; :83 bundle sem credenciais; :56/109 saída sem senha; :96 colega sem ação administrativa | Sim |
| PIL-07 duas sessões | :95 quadro salvo, :96 colega consulta; :97/98 mesmo cartão/demanda nas duas sessões | Sim |

Necessidade reversa: o único teste :47 e cada assertion acima cobrem os8PIL; preparações SQL/API somente garantem as precondições fictícias. Não duplicar suítes de domínio. Roteiro real entre computadores permanece PIL-08/documentação/humano. Adequação: valores concretos, nenhuma remoção/skip/teste antigo modificado,convenções Node/Playwright existentes. Verifier ainda pendente.

Tentativas preservadas:1 CLI MySQL escapava barras do datadir; corrigido --raw sem mudar expected path.2 seletor do cartão procurava heading em vez de article; texto e visible preservados.3 bug real no Windows: Java/TEMP fechavam,mas stdin encanado mantinha Node vivo; corrigido process.exit apenas DEPOIS do encerramento/cleanup e flush da mensagem. Processo preso da fixture foi identificado por PID/pai/comando e encerrado somente com seu conhost,sem Java/MySQL; harness limpou schemas. Temporário daquela tentativa removido com caminho absoluto,artefatos e ausência de processo conferidos.4 ciclo completo PASS44s.5 extensão dos checks porta ocupada/dependência ausente PASS45s. Logs TEMP/piloto-autor-integracao{,2,3,4,5}.log. Nenhum outcome/timeouts existentes alterado. Saída normal do novo cenário tem limite10s para detectar processo preso.

Execuções autor acumuladas:7 (2unitários+5integrados),incluindo falhas/preparação;sem suíte completa. Gerados preexistentes de node_modules/target preservados e fora dos commits.

## Conferência operacional do banco local

Após os testes, Hibernate validate foi executado em cópia compilada TEMP, com web-application-type=none, SQLinit=never e sem servidor web. Banco local configurado localhost3306/sistema confirmado antes de rodar: exit0,estrutura compatível;TEMP removido. Nenhuma migração/DML,sem publicar endereço LAN. Consulta dirigida confirmou Marco ID2 ainda ADMINISTRADOR. Essa conferência de preparação não é teste de aplicação em dados reais nem prova de acesso de um segundo computador.

## T1 iniciador implementado, ainda sem fechamento da feature

14/10/2026. Proposta do modo piloto aprovada explicitamente após spec validada. Passos: T1 iniciador/configuração/checks focados; T2 ciclo real com MySQL fictício e duas sessões; T3 guia/evidências. Verifier fresco ao final. Sem tasks/design separados pelo porte médio.

Arquivos T1: scripts/piloto-config.mjs, piloto-lan.mjs, piloto-config.test.mjs, package.json e WebConfig.java. Premissa: mesmo endereço para interface/API, build em cópia TEMP de src/main/POM para não tocar target real, MySQL original resolvido pelo Spring e override explícito quando fornecido. Interceptor limitado a /api/** para permitir HTML/assets; autenticação da API preservada e prova HTTP fica em T2. A documentação oficial Spring confirma index.html em static-locations como welcome page: https://docs.spring.io/spring-boot/reference/web/servlet.html.

Gate focado `npm run test:piloto`:6/6 PASS,0falhas/skips,92ms;lint PASS. Duas invocações até aqui, repetição após reforços de cleanup/credenciais apontados por revisão de código independente,sem falhas de teste. Runtime e prova Java/duas sessões aguardam T2,sem declarar piloto concluído.

| Critério / resultado | Evidência frontend/scripts/piloto-config.test.mjs | Coberto em T1 |
| --- | --- | --- |
| PIL-01/04 endereço local e porta | :11 deepEqual host/8082; :12 assert.throws para ausente,externo,wildcard,porta0/texto,argumento desconhecido/repetido | Sim, acesso real em T2 |
| PIL-02 MySQL local e configuração | :16 objeto de configuração exato; :17 override fictício; :18 throws para H2/remoto/banco vazio | Sim, persistência em T2 |
| PIL-03 validate/sem SQLinit | :22/23 args includes validate e never | Sim, banco incompatível em T2 |
| PIL-06 credenciais só servidor | :27 args.every semusername/password/url; :29/30 JSON contém sóURL e env mantém literal da senha fictícia | Sim, assets/auth em T2 |
| PIL-04/05 serviço ocupado preservado | teste Porta ocupada exige rejeição e servidor.listening true; nova reserva após fechar | Sim |
| PIL-05 limpeza confinada | teste Limpeza rejeita cwd e TEMPraiz,remove somente creral-piloto-* próprio | Sim |
| PIL-04 corrida/prontidão | teste Página própria aceita nonce correto,rejeita outra página e nonce diferente | Sim |

Mapeamento reverso: os seis testes correspondem às sete linhas acima; nenhuma asserção especulativa. Resultados esperados derivam dos8PIL. Revisão independente de código resolveu: preservação de credenciais escapadas pelo Spring, cancelamento durante cópia, identidade da própria página e preservação de TEMP se filho não confirma saída. Nenhum teste antigo alterado ou removido. T2 comprovará efeitos observáveis sem suite completa Java/MySQL/Vitest.

## Administrador inicial, 14/10/2026

**Concluído e verificado — tudo ok: configuração operacional do administrador inicial. Incluído no diagnóstico.** A funcionalidade de autorização já existia; não houve novo código ou mudança de regra. O acesso em rede como um todo ainda não está concluído.

Autorização: usuário escolheu Marco Polinski como primeiro administrador, informou marcoimmarquin@gmail.com e confirmou este computador como servidor central do primeiro piloto na mesma rede.

Identificação somente leitura: MySQL8.0.43, localhost:3306, banco sistema, servidor DESKTOP-ESO6LOG. Consulta dirigida retornou ID2, nome Marco Polinski, e-mail exato e papel FUNCIONARIO; contagem de administradores0. Nenhuma senha de usuário foi consultada.

Aplicação: transação com SELECT FOR UPDATE e UPDATE somente de papel, limitado por ID2, BINARY email exato, BINARY nome exato e papel anterior FUNCIONARIO/nulo. ROW_COUNT=1. Leitura da mesma conta retornou ADMINISTRADOR; COMMIT concluído; contagem final1. Nenhuma migração ou alteração de estrutura.

Conferência independente pelo agente auditoria_acesso_piloto, somente SELECT: mesma conta ID2/Marco Polinski/marcoimmarquin@gmail.com com ADMINISTRADOR e exatamente1 administrador no banco local. Código de cadastro mantém FUNCIONARIO em UsuarioService.java:29. Não houve login em nome da conta ou acesso à sua senha. Conferência do uso pela interface ainda faz parte do teste humano.

Credenciais do banco usadas somente em memória para o cliente MySQL, sem senha em argumentos ou evidências. Nenhuma outra conta promovida/demovida; sem novo teste automatizado ou suíte completa por ser aplicação operacional de permissão existente, conferida por leitura antes/depois e auditor independente. Nenhum sensor ou dado fictício aplicado neste banco.

## Rede ainda não habilitada

Auditoria de leitura: frontend/src/services/api.js usa VITE_API_URL e padrão localhost:8081/api; CorsConfig.java:14 permite localhost:5173. Serviço atual de interface5173 ouve loopback. Os atuais serviços3306/8081/5173 não foram interrompidos ou reconfigurados. Computadores externos ainda não tiveram acesso verificado.
