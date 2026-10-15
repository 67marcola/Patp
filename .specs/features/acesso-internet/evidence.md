# Evidências — acesso pela internet

## Primeiro administrador — bootstrap autorizado e conferido,15/10/2026

Usuário confirmou “Cadastro aguardando aprovação do administrador.” Nova leitura Aiven/defaultdb com TLS VERIFY_IDENTITY/READ ONLY encontrou ID1,Admin,admin@gmail.com,FUNCIONARIO,aprovado0; total1usuário/0admins. AD-048 já autoriza a ação, sem nova pergunta. PrepararBancoInternet compilado e previamente conferido executou --administrador --id1 --email admin@gmail.com, exit0: “Administrador explicitamente conferido e preparado: ID 1.” Ferramenta transacional verificou ID/e-mail antes de gravar papel/aprovação; nenhuma criação de tabela ou conta adicional.

Conexão posterior distinta (cliente nativoMySQL, TLS VERIFY_IDENTITY, transação READ ONLY) às2026-10-15 13:50:16.952883UTC: ID1,Admin,admin@gmail.com,ADMINISTRADOR,aprovado1,aprovado_por1,aprovado_em2026-10-15 13:49:09.017226. Total1usuário/1admin/1adminaprovado/0outrosadmins. Invariantes comparadas por auditor independente: PASS de liberação e unicidade, total preservado. Prova transcrita em qa/admin-online-20261015.json. Senha/hash da conta não selecionados ou alterados; credencial de banco fornecida só por entrada oculta, env restaurado e clientes encerrados. Login humano/uso4G permanecem pendentes; não inferir deles o resultado do bootstrap. Sem mudança de código ou suíte repetida.

## Login — origem do deploy específico versus domínio oficial,15/10/2026

Usuário confirmou tentativa pessoal de cadastro de admin@gmail.com (relatou conta existente) e mostrou login com ApiError de comunicação em URL específica de deploy Vercel ...dbg5gla89-marcola2.vercel.app. Nova consulta nativaMySQL TLS VERIFY_IDENTITY, READ ONLY, selecionando somente ID/nome/email/papel/aprovado e contagens:0usuários/0administradores, sem linha da conta. Nenhum bootstrap ou alteração executado; senha do banco só entrada oculta e MYSQL_PWD restaurado. Fontes do preparador compilado ainda conferem com as fontes atuais, mas faltou conta/ID para usá-lo.

Dois probes reais OPTIONS https://creral-piloto-online.onrender.com/api/usuarios/login, Access-Control-Request-Method POST e Access-Control-Request-Headers content-type: Origin https://creral-piloto-online.vercel.app retornou200/ACAO igual; Origin https://creral-piloto-online-dbg5gla89-marcola2.vercel.app retornou403/ACAO ausente. Nenhum POST/login/cadastro foi enviado pelo agente. GET da URL específica redirecionou à autenticação Vercel; não registrar parâmetro nonce. Revisor independente confirmou correção de aguardar conta e usar domínio oficial, sem ampliar CORS nem marcar admin pronto. Próximo: usuário cadastrar no domínio oficial, conferir ID/email e executar AD-048 já autorizada; verificar globalmente1admin aprovado. Nenhuma suíte repetida ou alteração de código.

## Vercel — Ready Production e assets públicos,15/10/2026

Captura do usuário mostra deployment BRX6zfr75, Ready/Latest, Production/Current, domínio creral-piloto-online.vercel.app e prévia da tela de login. GET sem autenticação às2026-10-15T13:14:48.3691857Z respondeu200, URL final https://creral-piloto-online.vercel.app/. Dois assets referenciados diretamente no HTML responderam200: /assets/index-Bsh78feL.js (287622caracteres, SHA256E757580717493541F3385AA1B61439D5F2E55EB5668DF8AFEB69FCEB961F74A5) e /assets/index-CjQnbSBo.css (19394caracteres, SHA256ED0157B862CEC0F73C5A2207C007260C297D0BF90844DDD3A78D0AFAE8743C3E). Hashes calculados do texto UTF8 obtido, não do fluxo comprimido HTTP. JS contém API pública esperada https://creral-piloto-online.onrender.com/api; marcadores listados no arquivo qa/vercel-publico-20261015.json ausentes. Não foram executados JS, login, cadastro ou alteração remota de configuração/dados pelo agente.

Isso confirma publicação da interface/alcance HTTP e configuração pública do bundle observado. NET-01/02/19 continuam com limites: imagem final completa não inspecionada; primeiro admin/login/persistência e celular4G ainda pendentes. Revisão independente somente leitura confirmou fluxo seguinte: usuário registra pessoalmente conta Marco; conferênciaID/email somente leitura; autorização explícita da ação concreta de bootstrap; login e teste real fora da LAN. Fonte/código/testes não mudaram e nenhuma suíte histórica foi repetida.

## Render — Live e consulta pública protegida,15/10/2026

Captura fornecida pelo usuário mostra Deploy succeeded | Live, duração2m13s, deploy manual15/10/2026 às09:47:55GMT-3, fontec496d91. Em seguida, consulta real somente leitura GET https://creral-piloto-online.onrender.com/api/usuarios/me, sem credenciais e com Origin https://creral-piloto-online.vercel.app, às2026-10-15T12:53:06.7229013Z: HTTP401, Access-Control-Allow-Origin idêntico e URL HTTPS final preservada. Arquivo qa/render-publico-20261015.json contém somente metadados públicos da consulta; corpo não foi registrado. Isso comprova alcance da API protegida e CORS nesse GET, sem alegar fluxo autorizado, navegador/4G ou inspeção completa da imagem.

Revisão independente somente leitura confirmou configuração Vercel mínima: Production VITE_API_URL=https://creral-piloto-online.onrender.com/api, sem barra depois de/api; frontend/vercel.json já define modo internet no build. Root Directory frontend já usado no primeiro log. Próximo passo usuário salvar/redeploy e informar resultado. Nenhuma mudança produtiva, teste repetido ou deploy pelo agente. Não contar a consulta operacional como nova suíte comportamental. NET-01/02/19 avançam parcialmente, mantendo fechamento externo pendente.

## Render — recusa da origem na inicialização real,15/10/2026

Usuário relatou preenchimento de CRERAL_DATABASE_PASSWORD e inclusão da CA no painel; não foram lidos valores protegidos pelo agente. Primeiro recorte mostrava só exit1/JAVA_TOOL_OPTIONS, sem causa. Log completo posterior2026-10-15T12:15:29.279Z mostra IllegalStateException em ValidarAmbienteInternet.validarOrigem:56/initialize:34, “Informe origem HTTPS exata, sem caminho, barra final ou curinga.” Leitura local :49–56 confirmou HTTPS, host, caminho vazio, ausência de userinfo/query/fragmento/curinga e trim idêntico. Não se conhece o valor atual no painel. Correção indicada somente no Value da variável: https://creral-piloto-online.vercel.app; Save and deploy reutiliza imagem existente. Sem alteração produtiva ou testes repetidos. Log prova execução Java/Spring até o inicializador, ainda sem contexto ativo/conexão MySQL/acesso público verificados. Aguardar resultado após correção; NET-01/02/19 externos permanecem pendentes.

## Vercel — domínio fornecido pelo usuário,15/10/2026

Usuário enviou creral-piloto-online.vercel.app. Origem completa a configurar: https://creral-piloto-online.vercel.app, sem barra final/caminho. Domínio é relato do usuário, sem inferir disponibilidade da aplicação; primeiro build recusado ainda é a única evidência de execução Vercel. Conferência somente leitura de render.yaml/Dockerfile/entrypoint confirmou Docker/Free, raiz do contexto,6variáveis e CA em /etc/secrets/aiven-ca.pem. Documentação Render consultada confirmou importação de repo privado via Git Provider, Dockerfile Path e variáveis/Secret Files. Nenhum recurso/deploy criado pelo agente ou teste repetido; próximo passo é configuração no painel Render pelo usuário em sua própria conta.

## Vercel — log humano do primeiro build,15/10/2026

Usuário forneceu log08:30:07–08:30:12: clone main/c496d91 do repositório privado, instalação98pacotes concluída; comando VITE_MODO_INTERNET=true npm run build encerrou1 com “Build internet recusado. Informe VITE_API_URL HTTPS pública terminando em /api, sem credenciais, query ou fragmento.” Conferência somente leitura de frontend/vite.config.js confirmou a origem da recusa; frontend/vercel.json força modo internet. O log não distingue variável ausente de valor inválido e não contém domínio de produção. Warning npm cache-dir precede a instalação bem-sucedida e não é a exceção que encerra o build. Nenhum teste repetido ou código alterado. Solicitado domínio Settings → Domains para configuração exata CRERAL_FRONTEND_ORIGIN no Render; URL real da API depois alimentará VITE_API_URL e novo build. NET-01/02/19 continuam pendentes no alcance externo.

## GitHub privado — envio autorizado e conferido,15/10/2026

Destino exato fornecido pelo usuário:67marcola/-creral-piloto-online. (inclusive ponto final), confirmado pelo conector como private, owner67marcola, push/admin true. Main em1db23c882d533423768c5b0e0ef2e3c21d4d5a20, tree733ccfb0662cfe8ea8d090b5a0bbd40ed531d8b6, somenteREADME.md. Conector já consegue acessar; login do navegador não é necessário para o envio por API.

ZIP continua SHA256 BED19025F5FD8FB87529B2E8F2C721352CA0542F63F57B5C33731CB5CBCE20A6. Manifesto preparado somente localmente em deploy/internet/private/github-publicacao.json:139arquivos/801156bytes,138UTF8 e1PNG(frontend/src/assets/hero.png); gitblobSHA calculado de cada conteúdo. Recusados .git/node_modules/target/dist/private/.env/application.properties/certificados/chaves; senha local comparada em memória e prefixo de credencial Aiven ausente nos textos. EntrypointLF confirmado. Fonte é a mesma cópia já verificada, sem testes novos ou mudança produtiva. README remoto será conservado; atualização futura da main deve conferir expected_sha para impedir sobrescrever mudança concorrente.

Usuário respondeu “Sim, enviar o código (recomendado)” à pergunta explícita de envio. Conector criou blobPNG, árvore baseada na árvore inicial e commit c496d91b495dea5b2fa2fa299c337cb15c375377 (mensagem feat: publish verified internet pilot source). Ref main atualizada com expected_sha1db23c882d533423768c5b0e0ef2e3c21d4d5a20,forcefalse. Leitura posterior confirmou privado,head exato e árvore4b8df7aa130df88c93c8daf7dd1589fe27ef7c57; leitura recursiva sem truncamento confirmou140blobs,139/139 hashes iguais ao manifesto e README original1ffe075d47eaa7757c10547f9a45e4817a7b5d58 preservado. Nenhum teste comportamental repetido: transferência byte a byte do código verificado, sem mudança produtiva. Publicação Render/Vercel exige etapa própria e configurações reais; nomes sugeridos de serviço/projeto não precisam repetir a pontuação do nomeGitHub.

## Aiven real — preparação autorizada e conferida,15/10/2026

Usuário pediu manter a credencial existente para testar o projeto e autorizou explicitamente criar as oito tabelas após plano pronto. NativeMySQL com VERIFY_IDENTITY/CA confirmou defaultdb/MySQL8.4.11/0tabelas em transação somente leitura. Preparador Java atual compilado sem application.properties local em TEMP/creral-aiven-plan-0bfb6f34652644aebbee2780e9b9c4f6; --plano exit0 sem escrita; --aplicar autorizado exit0 criou8tabelas/semcontasouimportação.

Consulta posterior mostrou os oito nomes exatos esperados e COUNT(*)0 de cada tabela; sessão TLSv1.3. ValidaAiven temporário reutiliza SistemaApplication e ValidarAmbienteInternet, WebApplicationType.NONE, config internet exclusiva, validate e SQLinitnever. Contexto ativo e marcador ESTRUTURA_AIVEN_VALIDADA_SEM_WEB_OU_MIGRACAO, exit0. Origem https://validacao.invalid existiu somente no processo de validação e foi restaurada depois; não é URL publicada nem configuração para o Render.

Senha transmitida ao processo por terminal Read-Host -AsSecureString, exibindo somente asteriscos; credencial apenas em ambiente temporário, restaurado emfinally, sem valor em arquivos/argumentosCLI/saídas. Uma tentativa Node anterior falhou DNS11001 sem chegar ao banco; consulta nativa autorizada em contexto de rede permitido passou. Uma preparação por stdin nãointerativo recebeuEOF e recusou antes de SQL, substituída por entrada oculta emPTY. Não são falhas do sistema/testes nem houve operação no destino errado. Nenhuma suíte histórica executada.

Não executar --aplicar novamente: estrutura já pronta e qualquer tabela preexistente é recusada. Sem usuários/admins/dados online ainda. GitHub conector confirmou proprietário67marcola, Patp público; cópia de publicação privada separada continua recomendada. NavegadorCodex/IAB2/tab1 abriu github.com/new e está no login, marcado para continuação; sem criação de repositório/upload/push/deploy.

## Aiven real — TLS sem autenticação,15/10/2026

Destino fornecido pelo usuário: creral-mysql-piloto-marcoimmarquin-2e37.f.aivencloud.com:21005, defaultdb/avnadmin. CA original C:/Users/Marco/Desktop/ca.pem,1541bytes; certificado de projeto, válido de14/10/2026 a11/10/2036; fingerprintSHA256 C551151519A8BFE5EDF37A275332F2B65624ECBAC890386E5F4E6E61791EACDD. Cópia pública e PKCS12 em deploy/internet/private/aiven, ignorados peloGit; senha changeit protege só a CA pública.

OpenSSL3.5.6 s_client com starttls mysql, CAfile, verify_hostname e verify_return_error; timeout15s/stdinfechado; exit0, TLSv1.3/TLS_AES_256_GCM_SHA384, VerificationOK e peername compatível. Nenhum login ou SQL, senha não utilizada. Não equivale a JDBC autenticado, schema vazio, estrutura pronta ou publicação. Não houve nova execução das suítes de testes. Consulta do schema será feita com senha local oculta, sem copiá-la ao chat/projeto; recomendada rotação da senha que veio na URI.

## Fechamento do preparo local —15/10/2026

Verifier concluiu e persistiu validation.md sobre006f3b8: controle final independente1/1 em88,9s, histórico integral não vazio preservado;17NET com evidência local suficiente,NET-02 parcial eNET-01/19 externos. Sensor anterior1/1 morto por200≠403 reutilizado porque93arquivos produtivos tinham hashes idênticos; nenhuma alteração de código produtivo emT12. Total autor12 +Verifier3=15invocações,77casos distintos; sem fullsuite. Matriz/cobertura/limites no relatório independente. A mensagem final do agente foi interrompida por limite de uso depois que controle, limpeza, comparação de hashes e relatório já tinham sido concluídos; os arquivos persistidos confirmam resultado, não foi presumida uma aprovação ausente.

validate_state exit1 esperado e preservado: feature integral não concluída por construção real/publicação/4G. Preparação local aprovada, sem tarefa de correção local restante. Usuário confirmou as três contas em15/10, próximo passo criação do MySQL gratuito vazio no Aiven. Nenhuma operação remota nesta entrega. Logs finais do autor/controle/sensor preservados emqa; certificados e processos próprios serão encerrados com guarda de identidade, preservando datadir fictício original e sem ação dirigida aos serviços reais.

Cleanup final15/10 confirmado: identidadeSQL fresca33823/datadir exato e WMI processo próprio; mysqladminshutdownexit0; processo/listener ausentes antes de remover TEMP root por caminho absoluto confinado. Datadir original preservado. MySQL3306/PID5560 ativo;8081/5173 não observados nessa leitura, nenhuma parada direcionada a eles. Uma probe intermediária teve argumentoCA mal passado pelo PowerShell e falhou antes deSQL; argumento único corrigido e identidade reconfirmada, nenhuma operação no destino errado.

## T12 — cobertura explícita de histórico,15/10/2026

A primeira revisão independente passou controle1/1 e matou sensor de pendente1/1, mas a conferência final das conjunções NET-06 encontrou que o snapshot de estrutura não trazia histórico. Correção de cobertura, sem mudança produtiva: `frontend/scripts/internet-integracao.test.mjs:179` lê id/acao/descricao/data_hora/usuario/processo_id de todos os históricos ordenados e exige resultado não vazio antes do reinício; `:185` exige igualdade completa depois. Assim nenhum campo/histórico pode desaparecer ou mudar silenciosamente. Checks existentes permanecem intactos, sem ajuste de expectativa, exclusão ou skip.

GateT12 do mesmo cenário focado, primeira tentativa PASS1/1,91,4s/0skips. Java/Vite reais emTEMP, mesmos guards TLS/MySQL/HTTPS; cleanup próprio concluído. Total autor12 invocações,77 casos distintos, sem suítes históricas. A repetição foi necessária por lacuna concreta do Verifier, conforme AD-035; nenhum percentual de economia prometido. Cenário cobreNET-06 explicitamente (:179/185), mantémmapeamentosA/B/C/D deT10; sem testes além da spec. Código do sistema e fonte sanitizada de publicação permanecem idênticos; ZIP não inclui scripts de testes. Novo controle independente após commit, reaproveitando sensor anterior porque política e assertion403 não mudaram.

## T11 — operação e entrega local

docs/PUBLICACAO_INTERNET.md contém sequência de contas gratuitas, fonte sem histórico/configuração locais, endereços Vercel/Render, CASecretFile, preparação explícita via Java no PC (sem RenderShell), consulta SQL remota somente leitura ID/e-mail e bootstrap dos dois novos admins, aprovação comum e roteiro4G. docs/OPERACAO.md referencia o guia separado. Código PowerShell foi conferido sintaticamente; comandos remotos não executados. Variáveis de credenciais restauradas emfinally e senha obtida somente por entrada oculta.

Cópia de publicação local construída por allowlist em deploy/internet/private/publicacao-6854ab28c5e8421d9412aa7c7e55e2f8.zip:139arquivos; SHA256 BED19025F5FD8FB87529B2E8F2C721352CA0542F63F57B5C33731CB5CBCE20A6. Zeroapplication.properties/.env/certificados/chaves/.git/node_modules/target/dist, senha local ausente de todos textos conferidos em memória, sem imprimir seu valor. Cópia ignorada peloGit, não publicada. Guia identifica hash e necessidade de renovar se houver mudança posterior. Nenhuma credencial nova persistida.

Gate documental: validate_spec --strict e validate_tasks sem erros; trêsavisos none de configuração/docs previstos na matriz. diff --check PASS; check_commit PASS. Nenhuma execução de testes adicional T11. Verifier fresco será disparado depois do commit, para preparação técnica e sensor emscratch; NET-01/19 e construção Docker real/publicação continuam pendentes de recursos externos e observação. Não apresentar uma verificação local como aceite integral da spec ou4G.

## T10 — fluxo integrado

Premissas: fixture MySQL8.0.43 porta33823/datadir TEMP exatos, nunca3306; CA/servidor fictícios com SANlocalhost/127.0.0.1 e truststore PKCS12. Chaves só no TEMP próprio, sem credenciais reais. Dois servidores HTTPS locais representam interface/API distintas e preservam CORS real; Java usa configuração exclusiva internet. Fonte e node_modules copiados fisicamente, sem alterar target/dist/cache reais. SQL cria/remove somente dois schemas e usuário com nomes únicos depois da guarda de identidade.

Gate `node --test scripts/internet-integracao.test.mjs`: primeira tentativa PASS1/1,0skips,86,7s; compila Java e Vite no próprio TEMP. CA válida aceita pelo JDBC VERIFY_IDENTITY; CA desconhecida falha. Cenário fecha navegadores/servidores/processos filhos, remove seus schemas/usuário/TEMP; preserva fixture TLS para Verifier e serviços originais. Total autor11 invocações comportamentais e77casos distintos (21Java,55Vitest,1integrado), contando falhas anteriores; alvo original de oito invocações excedido. Não usar isso como medição de tokens ou prometer50% de economia atingida.

Revisão documental encontrou a CLI aceitando opções TLS duplicadas depois da regex textual. Correção coesa NET-03/04: preparador reutiliza a guarda completa de URL de T8; regex original e seus testes permanecem. Integrado prova a recusa antes de DDL. Nenhuma expectativa/teste antigo alterado, removido ou ignorado. Lint final JSON diagnostics[]/74arquivos/96regras PASS; a captura inicial do PowerShell exigiu ler o JSON na chamada seguinte, sem repetir testes.

Check A, caminho `frontend/scripts/internet-integracao.test.mjs`:

| Critério | Assertion file:line | Outcome do spec | Coberto |
| --- | --- | --- | --- |
| Isolamento NET-05/07 | :21/26/27 status/porta/datadir exatos, :200 confinamento TEMP | Nenhum destino local real ou cleanup externo | Sim |
| NET-03/04 CLI/TLS/config | :105 exit1/mensagem; :114 senhaausente; :116 TLSfraco; :119 CA não confiável exit1/mensagem; :191 segredo ausente de todos logs | Falha explícita sem fallback/segredo | Sim |
| NET-04/05 estrutura | :107–111 plano0tabelas/aplicação8/0contas; :121 schema vazio continua0; :190 sem acesso falha e continua7 | Preparo explícito; execução normal só validate | Sim |
| NET-02 origem/build | :124 origempermitida exata; :125 intruso semCORS; :139 buildexit0; :141 endereçoHTTPS presente/localhost8081 e credenciais ausentes; :176 única origem APIHTTPS | Conexão navegador/APIHTTPS e CORS restrito | Sim, fixture, não nuvem |
| NET-08/09/15 | :130202/bodyexato; :131 FUNCIONARIO; :133403/erroexato; :135 CLI conta conferida; :146 login funcional | Cadastro público não concede papel/sessão; bootstrap explícito | Sim |
| NET-13 | :150 mensagemstatus exata; :151 token/usuário null | Pendente não entra nem grava sessão | Sim |
| NET-10/11/14 | :154401; :155200/lista quatrocampos/valores; :156 e-mailvisível; :161 geometria; :163 statusaprovação; :165 repetição/SQLidêntico; :166 FUNCIONARIO; :169 controleausente; :170403 | Admin aprova, comum não administra; autoria/hora/papel preservados | Sim |
| NET-06 persistência | :174/175 cartãoidêntico emduassessões; :180 count1apósstop; :182 tokenantigo401; :183 novo200; :184 snapshotinteiro igual; :185 aprovaçãoigual | Dados/conta sobrevivem reinício, sessão pede login novo | Sim, MySQLfictício |
| NET-17 | :187 LoginvisívelapósSair; :188 token/usuárionull e API401 | Saída revoga e limpa | Sim |

Check C reverso: guardas/compile/timeouts/cleanup→T10 Donewhen/NET-03/05/07; :105–125→NET-02/03/04/05; :130–151→NET-08/09/13/15; :154–170→NET-10/11/14; :174–185→NET-06/17; :187–191→NET-03/04/17. Todos necessários; componentes/rollback/clock8h e falhas incertas usam provas específicas T1–7 já citadas, sem refazer suítes históricas. Adequação A/B/C/D: outcomes de JSON/SQL/cache/dados completos, sem assertions apenas de chamada; guidelines tasks/AD-035/padrão integrado existente.

QA visual pelo autor: qa/aprovacoes-360.png mostra painel dentro360px, e-mail longo com quebra, controles acessíveis, foco no título; qa/demanda-360.png mostra os mesmos dados e colunas adaptadas, sem corte horizontal. Leitura visual e geometria real aprovadas. São capturas locais, não celular físico ou4G. NET-01/19 continuam sem observação externa; demais requisitos Implementing até Verifier.

## T8 — pacote Render

Build em cópia física TEMP/creral-internet-root-94babcdf879b474b87cbcb7f40acfbef, excluindo application.properties local: Maven compile + dependency:build-classpath runtime PASS,65 fontes/release17, sem fase de testes ou conexão ao banco. Bash -n entrypoint.sh PASS após permitir o runtime Bash fora do sandbox (a primeira chamada foi impedida antes da análise pelo MSYS). Nenhuma execução comportamental adicional. git diff --check PASS.

Dockerfile multistage copia classes e dependências, executa UID10001/GID1000, sem props locais; .dockerignore exclui credenciais/certificados/dependências/artefatos/testes. EntryPoint importa somente CA pública para PKCS12 temporário; banco VERIFY_IDENTITY, sem fallback. application-internet.properties externo e initializer recusam configuração incompleta antes de beans; esquema validate e SQL init never. CORS conserva localhost5173 no modo local e usa origemHTTPS exata no perfil internet. Provas de execução/falhas pertencem a T10. Docker não instalado: imagem real não construída, publicação pendente. Incluído no diagnóstico; requisitos permanecem Implementing.

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

## T4 — preparo explícito da base

Premissas: base nova, nenhum Spring/config local no preparador; criação somente no schema vazio. Arquivos: PrepararBancoInternet.java, internet/estrutura.sql e PrepararBancoInternetTests.java. Sucesso: plano sem gravação, recusa não vazio/parcial, SQL real compatível com validate, bootstrap exato e rollback.

Gate Quick Maven `-Dtest=PrepararBancoInternetTests test` em cópia TEMP, tentativa1:5/5, zero erros/falhas/skips. Total autor6 invocações. Fixture iniciada sem initialize/defaults, porta33823, PID atual4472; SQL `@@port,@@datadir` conferido exatamente antes do DDL. Datadir C:/Users/Marco/AppData/Local/Temp/creral-arquivo-049cfd5cc86d4d8b840e41f1710d7a43/mysql preservado. Testes criaram/removeram somente schemas/usuários exclusivos internet_t4_<uuid>; nenhum acesso3306/conta real. Fixture deixada ativa para integração posterior; logs em TEMP/creral-internet-t1t7-20261014/mysql-fixture.*.log. Uma conferência SQL inicial foi barrada pelo sandbox e repetida com runtime autorizado; não é execução de suíte.

Check A, assertions em sistema/src/test/java/com/patp/sistema/PrepararBancoInternetTests.java:

| Critério | Assertion file:line | Outcome do spec | Coberto |
| --- | --- | --- | --- |
| NET-05 plano/criação | :44 `contains("nenhum dado ou estrutura foi gravado")`; :45 `tabelas(...).isEmpty()`; :46–48 oito nomes exatos/count0 | Sem gravação no plano/dados importados | Sim |
| NET-04 validate | :54 `assertThat(app.isActive()).isTrue()` após startup com validate; :55 count0 | Estrutura SQL compatível sem DDL automático | Sim |
| NET-05 alvo existente | :57–58 recusa/size8; :63–66 recusa e tabela/id42 preservados | Recusar antes da primeira criação | Sim |
| NET-05 parcial | :75 `hasMessageContaining("estrutura parcial preservada")`; :77 somente usuarios; :78 recusa; :79 count0 | Sem apagar estrutura parcial | Sim |
| NET-15 conferência e alcance | :86–89 mismatch/ausente, contas iguais/acessos0; :91–95 campos/hash/papéis/conta2/autor/hora | Só conta explicitamente indicada promovida/aprovada | Sim |
| NET-15 repetição | :97 estado `isEqualTo(primeiro)` | Primeira hora preservada | Sim |
| NET-03/15 falhas | :107 SQLException; :108 contas `isEqualTo(antes)`; :109 configuração ausente; :110–112 TLS e sem segredo | Rollback/falha sem fallback ou segredos | Sim |

Check C reverso: guardas :20–22→isolamento NET-05/07; :44–58→NET-04/05; :63–66/75–79→NET-05; :86–97→NET-15; :107–112→NET-03/15. Todos mantidos/necessários. Adequação A/B/C/D aprovada, assertions de dados/payload/estado, guidelines tasks/AD-035. Decisão operacional de CLI/autoria inicial adicionada ao design; sem desvio de requisitos. Implementing até Verifier.

## T5 — contratos cliente

Premissas: logout conserva resposta textual do endpoint atual; entrada200 conserva validação pública atual,202 exige objeto exato de espera. Arquivos: frontend/src/services/api.js e apiInternet.test.js. Sucesso: nenhum payload malformado confirma entrada/aprovação/saída, sem retries; rotas/token/corpo e lista segura validados.

Gate Quick `node node_modules/vitest/vitest.mjs run src/services/apiInternet.test.js` em cópia física de todo frontend/dependências para TEMP/creral-internet-t1t7-20261014/frontend. Tentativa1:32/32, zero falhas/skips. Total autor7 invocações. Nenhum teste antigo alterado; nenhuma exclusão de dist/caches nas dependências.

Check A, assertions em frontend/src/services/apiInternet.test.js:

| Critério | Assertion file:line | Outcome do spec | Coberto |
| --- | --- | --- | --- |
| NET-08/07 cadastro202/local200 | :13 `toEqual(pendente)`; :14–16 rota/POST/corpo/1chamada; :21 `toEqual(sessao)` | Pendente separado de entrada local | Sim |
| NET-08/09/13 malformados/status | :29 `rejects.toMatchObject({status,...})`; :35/37 login202/403 exatos | Sem token/entrada incerta | Sim |
| NET-10 lista | :42 `toEqual([conta])`; :43–45 rota/Bearer/signal/vazio; :51 rejeição | Quatro campos/ordem/ID sem senha, erro explícito | Sim |
| NET-11 aprovar | :57 `toEqual(body)`; :58–59 rota/POST/Bearer sem body; :64–65 rejeição/1chamada | Confirma só ID e APROVADO exatos | Sim |
| NET-10/11 erros | :70 erro/status; :76 JSON inválido | Sem confirmação falsa | Sim |
| NET-17 logout | :82 texto exato; :83–84 rota/Bearer/POST; :89 rejeição | Revogação confirmada somente no contrato atual | Sim |
| NET-13/14/17 rede | :96 rejeição status0; :97 `toHaveBeenCalledTimes(1)` junto ao erro | Sem retry nem confirmação | Sim |

Check C reverso: :13–21→NET-07/08; :29–37→NET-08/09/13; :42–52→NET-10/14; :57–71/76–77→NET-11/14; :82–90→NET-17; :96–97→NET-13/14/17. Todos mantidos/necessários; chamadas provam transporte e assertions de resultado/erro provam outcomes. Adequação A/B/C/D aprovada, guidelines tasks/AD-035/Vitest existentes. Sem desvio; Implementing até Verifier.

## T6 — espera no cadastro

Premissas:202 confirmado mostra espera sem onLogin/storage;200 local mantém fluxo. Arquivos: Cadastro.jsx e CadastroInternet.test.jsx. Sucesso: formulário bloqueia duplo envio, erros preservam campos, pendência oferece voltar para login por teclado.

Gate Quick `node node_modules/vitest/vitest.mjs run src/pages/CadastroInternet.test.jsx`, fonte ressincronizada na cópia TEMP. Tentativa1:6/6, zero falhas/skips. Total autor8 invocações. Nenhum teste antigo alterado/excluído/ignorado.

Check A, assertions em frontend/src/pages/CadastroInternet.test.jsx:

| Critério | Assertion file:line | Outcome do spec | Coberto |
| --- | --- | --- | --- |
| NET-13 espera sem entrada/storage | :23 `textContent.toBe(pendente.mensagem)`; :24 título; :25 onLogin não chamado; :26 token/usuario `toBe("anterior")` |202 não entra nem grava sessão | Sim |
| NET-13 dados e retorno | :27 corpo `toEqual(dados)`/1 envio; :29 voltar1 por Enter | Cadastro/retorno utilizáveis | Sim |
| NET-13 duplicação | :37 fetch1; :38–39 controles disabled true; :41 espera sem login | Envios simultâneos bloqueados | Sim |
| NET-13 rede/HTTP/malformado | :50 alert exato; :52 campos `toBe(valor)`; :53 status ausente/onLogin não chamado; :54 cache null; :55 fetch1; :56 desbloqueado | Erro conserva campos sem retry/sessão | Sim |
| NET-07 local | :63 onLogin `toHaveBeenCalledWith(sessao)`; :64 sem tela pendente/cache |200 local preservado | Sim |

Check C reverso: :23–29→NET-13; :37–41→NET-13; :50–56→NET-13; :63–64→NET-07. Todos necessários/mantidos. Adequação A/B/C/D aprovada; outcomes com valores/cache e transporte, guidelines tasks/AD-035/Vitest existentes. Sem desvio; Implementing até Verifier.

## T7 — painel, App e saída

Premissas: papel vem de /me, somente online; cache público continua com id/nome/setor/email. Arquivos: AprovacoesCadastros.jsx/.css/.test.jsx, App.jsx/AppInternet.test.jsx. Sucesso: lista/aprovação conservadoras, foco, estados explícitos, nenhum request novo no local e saída com revogação/aviso.

Gate Quick: seleção única `node node_modules/vitest/vitest.mjs run src/components/AprovacoesCadastros.test.jsx src/AppInternet.test.jsx`, TEMP físico. Tentativa1:16/16. Revisão identificou risco concreto no StrictMode já usado em main.jsx: abort da primeira montagem bloquearia a seguinte por ref de operação. Correção pela identidade da requisição e prova nova do carregamento; assertions/casos anteriores intactos. Tentativa2:17/17, zero falhas/skips. Total autor T1–T7:10 invocações comportamentais, incluindo compilação falha T2, comparação numérica falha T3 corrigida com autorização e reforço T7. Gates finais cobrem76 casos distintos; nenhuma suíte histórica repetida. Alvo original de sete invocações neste lote foi excedido por motivos registrados; não afirmar economia alvo como alcançada.

Build Vite HTTPS fictício/modo online e lint JSON (73 arquivos,96 regras, diagnostics[]) passaram no TEMP. Build repetido após correção StrictMode. Lint inicialmente silencioso exigiu conferência direta/JSON, sem execução extra de testes. Não houve alteração de dist/caches reais ou exclusão nas dependências.

Check A, assertions em frontend/src/components/AprovacoesCadastros.test.jsx (P) e frontend/src/AppInternet.test.jsx (A):

| Critério | Assertion file:line | Outcome do spec | Coberto |
| --- | --- | --- | --- |
| NET-14 carregar/dados/foco | P:20 `textContent.toBe("Carregando cadastros...")`; P:21 foco título; P:23–24 e-mails/setor | Carregamento explícito/lista segura e foco | Sim |
| NET-11/14 aprovação individual | P:26 status `toBe("Cadastro de Ana aprovado.")`; P:27 Ana ausente/Bia presente; P:28 foco e ocupação | Só conta confirmada sai da lista | Sim |
| NET-14 vazio/erro | P:35 texto exato/vazio; P:42 alert; P:43 sem vazio falso/1request; P:45 atualização | Estados explícitos sem retry | Sim |
| NET-14 incerto | P:55 mensagem exata; P:56 lista; P:57 sem sucesso/2requests; P:58 controles bloqueados; P:60 atualização explícita | Lista preservada e orientação atualizar | Sim |
| NET-14 recusa/refresh falho | P:66–67 recusado/lista/sem sucesso; P:73–74 refresh falho mantém dados/desabilita | Sem falso sucesso | Sim |
| NET-14 envio/StrictMode | P:81–84 bloqueio/resultado; P:93 e-mail; P:94 loading ausente; P:95 botão habilitado | Fluxo carregado no ambiente atual, sem envio duplicado | Sim |
| NET-10/14 App admin | A:25 cache `toEqual(publico)`; A:26 me1; A:27/29 lista/status; A:31 fechar/foco | Administração da sessão confirmada, quatro campos cache | Sim |
| NET-10 funcionário/cache/erro | A:38/40 controle null após meFUNCIONARIO; A:46 alert; A:47 controle null | Papel do cache não concede botão; falha oculta | Sim |
| NET-07 App local | A:52 sistema/sem botão; A:54 fetch não chamado; A:55 cache null | Nenhuma consulta/saída online no local | Sim |
| NET-17 logout incerto/confirmado | A:63 mensagem exata; A:64 cache null/login; A:65 logout1; A:66 Bearer/POST; A:73 duplicação/busy; A:75–76 login/semalert/cache null | Revoga, limpa e avisa falha sem retry | Sim |

Layout: inspeção de AprovacoesCadastros.css:24 em360 implica largura336px border-box; :5/12/14/15/16/20/21 permitem texto/quebras/min-width0; controles44px e foco :18. A geometria real será verificada no integrado T10, sem alegar que jsdom mede layout.

Check C reverso: P:20–30→NET-11/14; P:35–45→NET-14; P:55–60→NET-14; P:66–74→NET-14; P:81–84/93–95→NET-14; A:25–32→NET-10/14; A:38–47→NET-10; A:52–55→NET-07; A:63–76→NET-17. Todos necessários/mantidos. Adequação A/B/C/D aprovada nos outcomes funcionais/foco/cache, guidelines tasks/AD-035/Vitest existentes. Prova visual final reservada a T10; requisitos continuam Implementing até Verifier. Publicação/URL4G não realizadas.

## T9 — Vercel e guarda do build público

Premissas: raiz frontend na Vercel; build público exige modo internet, API pública HTTPS absoluta terminando em /api e nenhum dado de banco. Arquivos: frontend/vercel.json e frontend/vite.config.js, além dos registros de tarefa/spec/diagnóstico. Sucesso: build válido/assets/lint aprovados, configuração inválida recusada cedo, padrão local preservado. Tests:none conforme matriz; nenhum teste novo ou invocação comportamental nesta tarefa.

frontend/vercel.json:4 força `VITE_MODO_INTERNET=true npm run build` no shell Linux da Vercel. VITE_API_URL continua variável pública configurada no projeto; sem API válida o build recusa. vite.config.js:7–27 valida HTTPS, /api, ausência de userinfo/query/fragmento, host não local/privado; :32–33 aplica a guarda apenas ao build online. Proxy de desenvolvimento existente preservado.

Exceção exclusivamente de fixture: processo Vite com `CRERAL_TEST_INTERNET=true`, `VITE_MODO_INTERNET=true` e `VITE_API_URL=https://localhost:PORT/api` permite loopback HTTPS. Também reconhece127/::1; não libera privados remotos, HTTP, credenciais, query ou fragmento. A flag sem prefixo VITE é lida somente pela configuração Node e não entra no runtime/assets.

Gate Build em cópia física TEMP/creral-internet-t1t7-20261014/frontend, fonte/config ressincronizadas. Uma passagem de validação, sem falha inesperada:

| Requisito / caso | Comando / evidência | Outcome observado |
| --- | --- | --- |
| NET-01/02 build online | `vite build`, VITE_MODO_INTERNET=true e API `https://creral-api-exemplo.onrender.com/api`; T9-build-publico.log | exit0,32 módulos, HTML/CSS/JS gerados |
| NET-02 assets públicos | Inspeção de todos assets após build público | API esperada presente; localhost:8081/api, jdbc:mysql, CRERAL_DATABASE_, BEGIN PRIVATE KEY e CRERAL_TEST_INTERNET ausentes. Nenhuma credencial real foi fornecida/lida para esse gate |
| NET-02 configuração recusada | Nove comandos `vite build` com API ausente, HTTP, userinfo fictício, query, fragmento, localhost,192.168.x,fd00:: e caminho sem/api |9/9 exit nãozero identificável `Build internet recusado.`, antes da transformação; sentinela de credencial fictícia não aparece no log |
| NET-02 fixture explícita | `vite build` com flagCRERAL_TEST_INTERNET e APIHTTPSlocalhost | exit0,URLfixture presente; nome da exceção ausente nos assets |
| NET-07 compatibilidade local | `vite build` sem flags online/API | exit0,padrão localhost:8081/api mantido |
| Qualidade/configuração | oxlint `--format json src e2e scripts vite.config.js vitest.config.js playwright.config.js`; T9-lint.json | exit0,73arquivos/96regras,diagnostics[] |

Foram três builds que geram assets (público/fixture/local) e nove recusas rápidas de configuração, sem suítes repetidas. Logs próprios T9-*.log/T9-lint.json no TEMP acima; dist/cache reais preservados. Adequação: testes:none deliberado conforme matriz; outcomes de configuração/build/inspeção acima, sem testes antigos alterados. Total de invocações comportamentais permanece10 até T9. Nenhum recurso Vercel/Render/Aiven criado, push ou publicação. NET-01/02 permanecem Implementing até integração/Verifier/publicação observada.
