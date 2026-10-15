# Primeiro acesso pelo celular com dados móveis

**Estado em15/10/2026:** preparação técnica local implementada e verificada independentemente. API Render https://creral-piloto-online.onrender.com agora Live por captura do usuário; GET /api/usuarios/me real retornou401 sem sessão e CORS para a origem Vercel exata. Projeto Vercel/domain criados, mas primeiro build recusou API ausente/inválida. Falta configurar VITE_API_URL e Redeploy, conferir interface/assets, preparar conta/admin online e validar login/4G. Não declarar fechamento integral pela API estar Live.

**Atualização operacional:** MySQL Aiven criado e estrutura de oito tabelas aplicada com autorização explícita, todas vazias e validadas pelo Spring Boot. Não executar novamente o bloco Estrutura nesse banco. Ainda sem contas/admins online. Fonte enviada com autorização ao repositório privado [67marcola/-creral-piloto-online.](https://github.com/67marcola/-creral-piloto-online.), main c496d91b495dea5b2fa2fa299c337cb15c375377.139/139 hashes conferidos e README preservado; login no navegador do Codex não é necessário para o conectorGitHub. Próximo passo: importar na Vercel com raiz frontend, obter domínio de produção e configurar Render/Vercel conforme abaixo.

O caminho escolhido é React/Vercel, Java e Spring Boot/Render e MySQL/Aiven, com base nova de testes. As contas, quadros e dados deste computador não serão enviados para esse banco.

## O que pode ser preparado agora

1. Entre na sua conta da [Vercel](https://vercel.com/). A publicação da interface será configurada depois que existir o endereço da API.
2. Crie sua conta no [Render](https://dashboard.render.com/). A API usará um Web Service com execução Docker e plano Free. Não escolha plano pago para este piloto.
3. Crie sua conta no [Aiven](https://console.aiven.io/signup). Crie um serviço MySQL no plano gratuito e espere ficar disponível. Separe os dados de conexão e o certificado CA fornecidos no painel.

Não envie senha do banco, senha das contas ou códigos de autenticação pelo chat. A senha do MySQL irá somente para a configuração protegida do servidor. O certificado CA identifica a autoridade do banco; não é uma senha.

O endereço final será o endereço HTTPS da interface na Vercel. Os colegas não precisarão instalar Java, MySQL ou Workbench no celular/computador.

## Fonte e endereços antes da publicação

Há um application.properties local já versionado no repositório original. O .dockerignore impede que ele entre na imagem, mas não o remove do histórico Git. A cópia de publicação já foi preparada e enviada ao repositório privado separado, sem esse arquivo, .git, .env, certificados, node_modules, target, dist ou dados locais. O histórico original não foi enviado. Manter essas exclusões nas próximas atualizações.

Uma cópia local está preservada em deploy/internet/private/publicacao-6854ab28c5e8421d9412aa7c7e55e2f8.zip (139 arquivos, SHA256 BED19025F5FD8FB87529B2E8F2C721352CA0542F63F57B5C33731CB5CBCE20A6). A pasta é ignorada pelo Git. Conferência local encontrou zero arquivos proibidos e nenhuma ocorrência do valor da senha local nos arquivos de texto. Os139arquivos dessa fonte, sem histórico Git, foram enviados pelo conector e conferidos por hash no GitHub. Gerar outra cópia se o código mudar antes da publicação.

Na cópia de publicação conservar sistema/pom.xml e sistema/src/main (exceto application.properties), frontend/src, frontend/public, frontend/index.html, frontend/package.json, frontend/package-lock.json, frontend/vite.config.js, frontend/vercel.json, deploy/internet, .dockerignore e render.yaml. Os provedores instalarão as dependências.

Projeto Vercel criado com pasta raiz frontend; usuário informou domínio https://creral-piloto-online.vercel.app em15/10/2026. Primeiro build recusou VITE_API_URL ausente/inválida conforme esperado; ainda sem API real configurada. Esse domínio deve ser usado no CRERAL_FRONTEND_ORIGIN obrigatório do Render. Depois de obter URL real da API, configurar VITE_API_URL e publicar novamente a interface.

| Lugar | Configuração |
| --- | --- |
| Render Web Service, plano Free | Runtime Docker; Dockerfile deploy/internet/Dockerfile; contexto raiz do projeto. render.yaml fornece os mesmos defaults. |
| Ambiente protegido Render | CRERAL_DATABASE_HOST, CRERAL_DATABASE_PORT, CRERAL_DATABASE_NAME, CRERAL_DATABASE_USERNAME, CRERAL_DATABASE_PASSWORD, fornecidos pelo Aiven. Nome usual defaultdb, conferir no painel. |
| Secret File Render | Nome aiven-ca.pem, conteúdo da CA pública baixada do Aiven; fica em /etc/secrets/aiven-ca.pem. Nunca enviar a chave privada. |
| Origem permitida Render | CRERAL_FRONTEND_ORIGIN=https://creral-piloto-online.vercel.app, sem barra final/caminho. Domínio informado pelo usuário; somente essa origem de produção será liberada. |
| Build Vercel | Salvar VITE_API_URL=https://creral-piloto-online.onrender.com/api em Production e Redeploy. vercel.json já força VITE_MODO_INTERNET=true. Nenhuma variável de banco na Vercel. |

O Render define PORT e termina o HTTPS. O processo Java escuta em0.0.0.0:PORT. Seu entrypoint constrói a URL JDBC com TLS VERIFY_IDENTITY e importa a CA para PKCS12 temporário; não precisa fornecer CRERAL_DATABASE_URL no painel. Configuração ausente/insegura ou estrutura incompatível impede a inicialização, sem usar o banco local. Docker não está instalado neste computador: a construção da imagem no Render ainda precisa ser observada.

## Preparar a base nova pelo computador

O plano Free do Render não oferece Shell/SSH para esta tarefa. Usar o Java/Maven deste computador, com destino explicitamente Aiven. A ferramenta abaixo não inicia o servidor e não lê application.properties. Nunca usar localhost,127.0.0.1 ou o banco sistema como destino destes comandos. Conferir host, porta e banco no painel Aiven.

No PowerShell, na raiz do projeto, preencher os valores públicos do serviço e selecionar uma única ação. O padrão Plano não grava. Estrutura cria oito tabelas somente se o schema estiver totalmente vazio. Administrador só será usado depois do cadastro e da conferência ID/e-mail descritos na próxima seção.

```powershell
$dbHost = 'HOST-MYSQL-DO-AIVEN'
$dbPort = 12345 # Substituir pela porta do painel.
$dbName = 'defaultdb' # Conferir no painel.
$dbUser = 'USUARIO-DO-AIVEN'
$caPem = 'C:\CAMINHO\ca.pem' # CA pública baixada do Aiven.
$acao = 'Plano' # Plano, Estrutura ou Administrador.
$adminId = 0 # Preencher somente após conferir a conta nova.
$adminEmail = '' # E-mail exato da mesma conta.

$env:JAVA_HOME = 'C:\Program Files\Java\jdk-25.0.2'
$env:PATH = "C:\Users\Marco\apache-maven-3.9.16\bin;$env:JAVA_HOME\bin;$env:PATH"
$preparo = Join-Path $env:TEMP ('creral-preparo-online-' + [guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Path "$preparo\src\main" -Force | Out-Null
Copy-Item .\sistema\pom.xml "$preparo\pom.xml"
Copy-Item .\sistema\src\main\* "$preparo\src\main" -Recurse
# Remover só a cópia conhecida dentro do temporário criado acima.
Remove-Item -LiteralPath "$preparo\src\main\resources\application.properties" -ErrorAction SilentlyContinue
mvn.cmd -B -ntp -f "$preparo\pom.xml" compile dependency:copy-dependencies -DincludeScope=runtime
if ($LASTEXITCODE -ne 0) { throw 'Compilação recusada; não executar o preparo.' }
$caP12 = Join-Path $preparo 'aiven.p12'
keytool -importcert -noprompt -alias aiven -file $caPem -keystore $caP12 -storetype PKCS12 -storepass changeit
if ($LASTEXITCODE -ne 0) { throw 'CA inválida; não executar o preparo.' }
$storeParam = [Uri]::EscapeDataString(([Uri]([IO.Path]::GetFullPath($caP12))).AbsoluteUri)
$nomes = @('CRERAL_DATABASE_URL','CRERAL_DATABASE_USERNAME','CRERAL_DATABASE_PASSWORD','MYSQL_PWD')
$anteriores = @{}
foreach ($nome in $nomes) { $anteriores[$nome] = [Environment]::GetEnvironmentVariable($nome, 'Process') }
try {
    $env:CRERAL_DATABASE_URL = "jdbc:mysql://${dbHost}:${dbPort}/${dbName}?sslMode=VERIFY_IDENTITY&trustCertificateKeyStoreUrl=$storeParam&trustCertificateKeyStoreType=PKCS12&trustCertificateKeyStorePassword=changeit&fallbackToSystemTrustStore=false"
    $env:CRERAL_DATABASE_USERNAME = $dbUser
    $segredo = Read-Host 'Senha MySQL do Aiven (entrada oculta)' -AsSecureString
    $env:CRERAL_DATABASE_PASSWORD = [Net.NetworkCredential]::new('', $segredo).Password
    $cp = "$preparo\target\classes;$preparo\target\dependency\*"
    switch ($acao) {
        'Plano' { java -cp $cp com.patp.sistema.PrepararBancoInternet --plano }
        'Estrutura' { java -cp $cp com.patp.sistema.PrepararBancoInternet --aplicar }
        'Administrador' {
            java -cp $cp com.patp.sistema.PrepararBancoInternet --administrador --id $adminId --email $adminEmail
        }
        default { throw 'Ação desconhecida.' }
    }
    if ($LASTEXITCODE -ne 0) { throw 'Preparo não concluído; conferir a mensagem antes de repetir.' }
} finally {
    foreach ($nome in $nomes) { [Environment]::SetEnvironmentVariable($nome, $anteriores[$nome], 'Process') }
    $segredo = $null
}
```

Executar primeiro Plano e conferir o destino. Só depois repetir com Estrutura na base vazia escolhida. A ferramenta recusa qualquer tabela preexistente. Se houver falha de DDL, informa a estrutura parcial e conserva as tabelas; não apaga nem promete rollback de criação MySQL. Não aplicar novamente sobre dados existentes. A pasta temporária contém classes e CA pública, sem senha gravada; pode ser removida depois, conferindo seu caminho exato em $preparo.

Após a estrutura estar pronta, iniciar o serviço Render e conferir logs de inicialização sem erro. Configurar a API HTTPS na Vercel e fazer novo deploy da interface. Não acrescentar /api em CRERAL_FRONTEND_ORIGIN; esse sufixo pertence somente a VITE_API_URL.

## Primeiros administradores e equipe

Na interface online, criar novamente as contas controladas de marcoimmarquin@gmail.com e admin@gmail.com. Usar senhas definidas para esse ambiente, sem enviá-las pelo chat. Ambas começam pendentes e como funcionários. O cadastro não devolve ID nem concede administração por e-mail.

Conferir cada conta no MySQL Aiven com uma consulta somente leitura. Por exemplo, no PowerShell, usando os mesmos dbHost/dbPort/dbUser/caPem preenchidos acima, fornecer a senha de forma oculta e restaurar a variável ao terminar:

```powershell
$pwdAnterior = $env:MYSQL_PWD
try {
    $segredo = Read-Host 'Senha MySQL do Aiven' -AsSecureString
    $env:MYSQL_PWD = [Net.NetworkCredential]::new('', $segredo).Password
    & 'C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe' --protocol=TCP --host=$dbHost --port=$dbPort --user=$dbUser --ssl-mode=VERIFY_IDENTITY --ssl-ca=$caPem --database=$dbName --execute="SELECT id,nome,email,papel FROM usuarios WHERE BINARY email IN ('marcoimmarquin@gmail.com','admin@gmail.com');"
} finally { $env:MYSQL_PWD = $pwdAnterior; $segredo = $null }
```

Conferir nome, ID e e-mail exatos. Repetir o bloco de preparo com acao Administrador e o ID/e-mail de uma conta por vez. Não usar os IDs2/3 do banco local. A ferramenta aprova e concede ADMINISTRADOR somente à conta indicada; erro de ID/e-mail não altera outra conta. Depois entrar pela interface. Os demais cadastros permanecem pendentes até o administrador abrir **Aprovar cadastros** e aprovar individualmente. Aprovar um funcionário não o torna administrador.

## Conferência real com dados móveis

1. Abrir a URL HTTPS de produção da Vercel no computador e conferir login autorizado. Uma chamada anônima à API protegida /api/gerenciamentos deve retornar401.
2. Cadastrar um funcionário de teste: antes de aprovar, o login deve informar a espera; depois de aprovar, deve entrar e consultar os quadros.
3. No celular, desligar o Wi-Fi e deixar os dados móveis ligados. Abrir o mesmo endereço Vercel, entrar e consultar/criar uma demanda de teste; conferir os mesmos dados no computador.
4. Registrar no diagnóstico URL, data, dispositivo/rede, resultados e eventual erro. Até essa observação, manter publicação/acesso externo pendentes. Screenshots e testes com HTTPS local não comprovam acesso4G.

## Limites do piloto gratuito

O [Vercel Hobby](https://vercel.com/docs/plans/hobby) é para uso pessoal/não comercial. O [Render Free](https://render.com/docs/free) pode pausar após15 minutos sem tráfego; a primeira abertura depois disso demora, e um reinício exige novo login porque as sessões são temporárias. O [MySQL gratuito do Aiven](https://aiven.io/docs/products/mysql/concepts/mysql-free-tier) tem atualmente1GB de armazenamento e condições de inatividade. Dados salvos pertencem ao MySQL, não à memória do Render.

O uso diário pelos empregados da Creral terá avaliação própria de hospedagem, capacidade e operação. Este primeiro piloto não promete disponibilidade permanente ou gratuidade ilimitada.
