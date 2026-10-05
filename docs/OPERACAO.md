# Operação local do sistema Creral

Os testes Maven usam H2 em memória pelo classpath de teste. Execute `mvn.cmd -B verify` em `sistema/`. A aplicação normal continua usando MySQL. Não execute o perfil normal para verificar testes.

## Experimentar o CRUD com dados fictícios

Abra um terminal na pasta `frontend/` e execute:

```powershell
npm.cmd run preview:isolated
```

O comando prepara o Java e abre o sistema em `http://localhost:4173`. Cadastre uma conta fictícia, entre com ela e experimente criar, editar, arquivar, consultar e restaurar gerenciamentos. No quadro criado, use Nova etapa e os controles Editar/Remover para configurar colunas. O cadastro ainda exige entrar depois; a entrada automática será tratada em outro requisito.

Requer as dependências npm instaladas, Maven no PATH e `JAVA_HOME` apontando para o JDK. As portas 4173 e 18082 precisam estar livres. O helper inicia diretamente o executável Java desse JDK, com URL, driver, usuário, senha e `create-drop` de H2 explícitos; não inicia o MySQL configurado.

Os dados ficam em memória enquanto o comando está aberto. Recarregar a página conserva esses dados; encerrar com **Ctrl+C** fecha os dois serviços e descarta o banco fictício. Se o Windows perguntar `Terminate batch job (Y/N)?`, responda `Y`. Uma nova execução começa vazia. O proxy e o ajuste de Origin são habilitados somente nesse ambiente isolado; a configuração normal de CORS permanece a mesma.

Para repetir o teste automático de navegador, execute `npm.cmd run test:e2e` na mesma pasta, com Microsoft Edge instalado. Ele percorre os CRUDs de gerenciamentos e etapas usando Tab/Enter, confere persistência após recarga e encerra os serviços no final. Cada arquivo de teste usa um H2 novo e contas fictícias próprias. O console informa a pasta temporária das capturas desktop/mobile. Não mantenha o preview aberto ao iniciar esse teste, pois ambos usam as mesmas portas.

## Selecionar administrador

O cadastro público sempre cria funcionário, rejeita ID enviado e não permite escolher papel. O primeiro cadastro não recebe privilégios especiais.

Depois que uma conta escolhida pelo responsável já estiver cadastrada, um operador autorizado pode conferir seu ID e e-mail no banco correto e atualizar somente essa conta. O exemplo abaixo é documentação; não foi executado no banco configurado:

```sql
START TRANSACTION;
SELECT id, email, papel FROM usuarios WHERE id = :id_escolhido AND email = :email_conferido;
UPDATE usuarios SET papel = 'ADMINISTRADOR'
WHERE id = :id_escolhido AND email = :email_conferido;
-- Conferir que exatamente uma conta foi atualizada antes de confirmar.
COMMIT;
```

Substitua os parâmetros por valores conferidos. Não use IDs futuros, e-mails ainda não cadastrados nem atualização sem `WHERE`. Para retirar o papel, a mesma seleção explícita usa `papel = 'FUNCIONARIO'`. A sessão resolve o usuário persistido em cada requisição; alterações confiáveis de papel são reconhecidas sem depender do objeto salvo no login.

Contas antigas com papel nulo são tratadas como funcionários. Não há promoção automática de contas reais.

## Estrutura de gerenciamentos legados

Antes de aplicar uma atualização no ambiente escolhido, conferir backup, schema e nomes das colunas no Workbench. Os exemplos abaixo correspondem ao modelo atual e não foram executados no banco configurado. Não repetir `ADD COLUMN` se a coluna já existir.

```sql
ALTER TABLE usuarios ADD COLUMN papel VARCHAR(20) DEFAULT 'FUNCIONARIO';
ALTER TABLE gerenciamentos ADD COLUMN arquivado BOOLEAN DEFAULT FALSE;
ALTER TABLE gerenciamentos ADD COLUMN versao BIGINT DEFAULT 0;
UPDATE usuarios SET papel = 'FUNCIONARIO' WHERE papel IS NULL;
UPDATE gerenciamentos SET versao = 0 WHERE versao IS NULL;
```

Criador nulo permanece nulo; somente administrador administra esses quadros. O marcador de arquivamento nulo é lido como ativo. As colunas nome/descrição continuam com 255 caracteres, evitando reduzir ou truncar nomes antigos. A API limita novos nomes a 120 caracteres, sem alterar os valores antigos durante consultas.

H2 valida o contrato em memória. MySQL isolado deve verificar atualização de schema e diferenças de collation/locking; nunca usar o banco de dados configurado como instância de teste por suposição.

## Contrato da configuração de etapas

`GET /api/gerenciamentos/{id}/estrutura-etapas` devolve `{gerenciamento,etapas}`. Os metadados do gerenciamento incluem sua versão atual e a permissão `podeAdministrar`. Cada etapa informa ID, nome, setor, ordem e `quantidadeDemandas`, incluindo demandas de todos os status. O GET anterior `/etapas` continua devolvendo um array. Consultas não corrigem ordens ou setores antigos.

Criador/admin pode configurar etapas de quadros ativos. POST `/etapas` responde 201; PUT/DELETE `/etapas/{etapaId}` respondem 200. Todas as mutações retornam o snapshot completo em JSON, inclusive DELETE. POST/PUT recebem `{nome,setor,ordem,versao}`; DELETE recebe `{versao}`. Use a versão retornada na próxima operação e ao editar/arquivar o gerenciamento. Repetir uma mutação com a versão anterior recebe 409; não há retry automático.

Nome e Setor são obrigatórios, normalizados nas bordas e limitados a 255 unidades UTF-16. Setor é informativo. Posição vai de 1 até N+1 na criação e de 1 até N na edição. A próxima configuração reorganiza as ordens antigas em posições consecutivas, sem inventar setor ou alterar demandas/históricos. Etapa com qualquer demanda não pode ser removida; mova as demandas antes. Arquivados ficam somente consulta.

Essa atualização não acrescenta tabela/coluna. A interface oferece Nova etapa, edição de Nome/Setor/Posição e remoção com confirmação. Campos são preservados em falha; Atualizar quadro consulta os dados sem repetir gravação. Enquanto uma gravação está pendente, Voltar, Cancelar e Sair ficam bloqueados. As etapas finais Concluídos/Cancelados e o destino automático de AD-011 terão requisito próprio.

## Primeiro teste de uso das etapas

Neste computador, o JDK usado nos testes está em `C:/Program Files/Java/jdk-25.0.2`. Para preparar o terminal PowerShell e abrir o ambiente fictício:

```powershell
Set-Location -LiteralPath 'C:\Users\Marco\Desktop\sistema-grupo\frontend'
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-25.0.2'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
npm.cmd run preview:isolated
```

Com o ambiente isolado acima aberto, cadastre/entre com uma conta fictícia e crie um gerenciamento de teste sem etapas iniciais. Abra o quadro e selecione Nova etapa. Preencha Nome da etapa com `Comprar poste`, Setor responsável com `Almoxarifado` e Posição com `1`. Salve e recarregue a página.

Resultado esperado: uma coluna Comprar poste, setor Almoxarifado, posição 1 e zero demandas; os mesmos dados permanecem após recarga. Informe se funcionou ou descreva o problema observado. Esse resultado humano será registrado separadamente dos testes automáticos. Criar processo ainda será conectado no requisito de demandas.
