# Operação local do sistema Creral

Os testes Maven usam H2 em memória pelo classpath de teste. Execute `mvn.cmd -B verify` em `sistema/`. A aplicação normal continua usando MySQL. Não execute o perfil normal para verificar testes.

## Experimentar o CRUD com dados fictícios

Abra um terminal na pasta `frontend/` e execute:

```powershell
npm.cmd run preview:isolated
```

O comando prepara o Java e abre o sistema em `http://localhost:4173`. Cadastre uma conta fictícia e use a lista de gerenciamentos que abre diretamente após a confirmação. Experimente criar, editar, arquivar, consultar e restaurar gerenciamentos. O quadro já abre com Concluídos e Cancelados. Use Nova etapa e os controles Editar/Remover para configurar trabalhos.

Requer as dependências npm instaladas, Maven no PATH e `JAVA_HOME` apontando para o JDK. As portas 4173 e 18082 precisam estar livres. O helper inicia diretamente o executável Java desse JDK, com URL, driver, usuário, senha e `create-drop` de H2 explícitos; não inicia o MySQL configurado.

Os dados ficam em memória enquanto o comando está aberto. Recarregar a página conserva esses dados; encerrar com **Ctrl+C** fecha os dois serviços e descarta o banco fictício. Se o Windows perguntar `Terminate batch job (Y/N)?`, responda `Y`. Uma nova execução começa vazia. O proxy e o ajuste de Origin são habilitados somente nesse ambiente isolado; a configuração normal de CORS permanece a mesma.

Para repetir o teste automático de navegador, execute `npm.cmd run test:e2e` na mesma pasta, com Microsoft Edge instalado. Ele percorre o cadastro direto e os CRUDs de gerenciamentos e etapas usando Tab/Enter, confere persistência após recarga e encerra os serviços no final. Cada arquivo de teste usa um H2 novo e contas fictícias próprias. O console informa a pasta temporária das capturas desktop/mobile dos CRUDs. Não mantenha o preview aberto ao iniciar esse teste, pois ambos usam as mesmas portas.

## Primeiro teste de uso do cadastro direto

Abra o ambiente fictício com `npm.cmd run preview:isolated`, usando o JDK e as portas livres descritos acima. Na tela de login, escolha Criar cadastro. Use Tab para percorrer Nome completo, Setor, E-mail, Senha e Confirmar senha. Preencha uma conta fictícia com email ainda não usado e senha de pelo menos seis caracteres, confirme a mesma senha e pressione Enter em Criar cadastro.

Resultado esperado: Gerenciamentos abre diretamente, mostrando o nome cadastrado. Crie um quadro fictício e recarregue a página. O mesmo quadro deve aparecer, mantendo a conta. Escolha Sair; o sistema deve voltar ao login. Entre manualmente com o email e a senha cadastrados. O quadro deve continuar disponível na mesma conta.

Enquanto cadastra, os cinco campos e as ações ficam desabilitados. Se as senhas diferirem ou forem curtas, corrija a mensagem sem perder os campos. Falhas também preservam o rascunho. Se a resposta não confirmar o cadastro, siga a orientação para entrar pelo login caso a conta já tenha sido criada; o sistema não repete a inscrição automaticamente. Se o navegador recusar salvar a sessão, permita salvar dados do site e entre pelo login.

Informe se esse roteiro funcionou ou descreva o resultado observado. Esse teste humano será registrado separadamente dos testes automáticos. O papel inicial é FUNCIONARIO; encerrar o ambiente isolado descarta essas contas e quadros fictícios.

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

`GET /api/gerenciamentos/{id}/estrutura-etapas` devolve `{gerenciamento,etapas}`. Os metadados do gerenciamento incluem sua versão atual e a permissão `podeAdministrar`. Cada etapa informa ID, nome, setor, ordem, categoria e `quantidadeDemandas`, incluindo demandas de todos os status. O GET anterior `/etapas` continua devolvendo um array. Consultas não corrigem ordens, setores ou referências antigas nem preparam quadros legados.

Criador/admin pode configurar etapas de quadros ativos. POST `/etapas` responde 201; PUT/DELETE `/etapas/{etapaId}` respondem 200. Todas as mutações retornam o snapshot completo em JSON, inclusive DELETE. POST/PUT recebem `{nome,setor,ordem,versao}`; DELETE recebe `{versao}`. Use a versão retornada na próxima operação e ao editar/arquivar o gerenciamento. Repetir uma mutação com a versão anterior recebe 409; não há retry automático.

Nome e Setor são obrigatórios nas etapas de trabalho, normalizados nas bordas e limitados a 255 unidades UTF-16. Setor é informativo. Posição vai de 1 até N+1 na criação e de 1 até N na edição, contando somente trabalhos. A próxima configuração reorganiza as ordens antigas de trabalho em posições consecutivas, sem inventar setor ou alterar demandas/históricos. Trabalho com qualquer demanda não pode ser removido; mova as demandas antes. Arquivados ficam somente consulta.

Cada novo gerenciamento recebe as finais oficiais CONCLUIDA/Concluídos e CANCELADA/Cancelados, com IDs próprios e Setor nulo. O snapshot ordena trabalhos por ordem/ID, depois CONCLUIDA e CANCELADA. Essas finais não podem ser editadas, removidas nem reposicionadas; PUT/DELETE respondem 409 com `Etapas finais obrigatórias não podem ser alteradas.` mesmo quando vazias. Nomes iguais em etapas antigas ou de categoria TRABALHO continuam trabalhos. Categoria SQL nula é lida como TRABALHO, sem gravar essa classificação.

A interface informa no formulário que Concluídos e Cancelados são criados automaticamente. Ao abrir o quadro, as duas colunas oficiais mostram Etapa final obrigatória, sem Setor, posição ou Editar/Remover. Nova etapa e edição oferecem somente posições entre trabalhos. Ao remover o último trabalho, as finais permanecem e aparece Nenhuma etapa de trabalho cadastrada. Nomes homônimos de trabalho mantêm seu Setor e controles; a indicação distingue as oficiais.

Campos são preservados em falha; Atualizar quadro consulta os dados sem repetir gravação. Enquanto uma gravação está pendente, Voltar, Cancelar e Sair ficam bloqueados. O destino automático das ações de concluir/cancelar e a reabertura de demandas serão conectados em entrega posterior. A preparação abaixo corrige explicitamente referências antigas e não garante ainda coerência permanente nas APIs anteriores de demandas.

## Preparar etapas finais de quadros legados

A coluna nova é `etapas.categoria VARCHAR(20) NULL`. Antes de usar o comando, revisar backup/schema no ambiente escolhido e acrescentar a coluna somente se ausente. Não classificar etapas antigas pelo nome nem executar atualização global de categoria:

```sql
ALTER TABLE etapas ADD COLUMN categoria VARCHAR(20) NULL;
```

Plano e aplicação são comandos explícitos de operador, sem endpoint público, runner ou execução pelo startup/GET normal. A ferramenta sempre usa `ddl-auto=validate`; assim, plano não acrescenta/ajusta schema. Schema incompatível interrompe o contexto. Concluir a manutenção de schema separadamente antes de pedir o plano.

O exemplo PowerShell abaixo usa um MySQL fictício separado, porta 33817 e schema `etapas_finais_teste`. A instância e o schema precisam existir e conter somente dados de teste; esses nomes/porta não autorizam usar outro banco. Execute na pasta `sistema/`, com o JDK configurado e o jar já construído por `mvn.cmd -B verify`:

```powershell
$preparacaoArgs = @(
    '--spring.datasource.url=jdbc:mysql://127.0.0.1:33817/etapas_finais_teste',
    '--spring.datasource.username=operador_teste',
    '--spring.datasource.password=senha_ficticia',
    '--spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver',
    '--spring.jpa.hibernate.ddl-auto=validate',
    '--spring.jpa.show-sql=false'
)
java '-Dloader.main=com.patp.sistema.PrepararEtapasFinais' -cp 'target/sistema-0.0.1-SNAPSHOT.jar' org.springframework.boot.loader.launch.PropertiesLauncher plano @preparacaoArgs
```

Conferir o relatório: cada quadro informa ID/nome/arquivo/criador, `finaisAusentes`, status e quantidades com os valores exatos, `referenciasAjustar` e versão. Status nulo aparece como null. No plano, `finaisCriadas` e `referenciasAlteradas` são zero; nada é gravado. A ação deve ser exatamente `plano` ou `aplicar` como primeiro argumento; qualquer outra ação é recusada antes de abrir contexto/conectar banco.

Depois de revisar o plano do ambiente fictício, executar o mesmo comando trocando somente a ação:

```powershell
java '-Dloader.main=com.patp.sistema.PrepararEtapasFinais' -cp 'target/sistema-0.0.1-SNAPSHOT.jar' org.springframework.boot.loader.launch.PropertiesLauncher aplicar @preparacaoArgs
```

A aplicação bloqueia os quadros em ordem de ID e usa uma única transação para toda a execução. Cria apenas as finais ausentes de todos os quadros, inclusive arquivados e sem criador. Para status exatamente `Concluido` ou `Cancelado`, troca somente `etapa_id` pela final do mesmo quadro. Demais valores, inclusive null, espaços, acentos ou diferenças de caixa, são inventariados e preservados. IDs, campos/status/datas/motivos das demandas, trabalhos, comentários e históricos permanecem intactos; nenhum motivo, data ou evento de negócio é criado. Campos do quadro ficam iguais, com versão incrementada uma vez somente se houve criação ou mudança de referência.

Finais duplicadas para a mesma categoria ou qualquer falha abortam e revertem integralmente a execução, inclusive quadros já processados. Não tentar corrigir automaticamente a duplicidade. Uma segunda aplicação sobre dados já preparados retorna zero criações/movimentos e preserva IDs/dados/versões. O relatório informa contagens por quadro e totais; conservar plano, resultado e backup para revisão operacional. Operar o banco configurado exige autorização própria e não foi feito nesta entrega.

## Primeiro teste de uso das etapas

Neste computador, o JDK usado nos testes está em `C:/Program Files/Java/jdk-25.0.2`. Para preparar o terminal PowerShell e abrir o ambiente fictício:

```powershell
Set-Location -LiteralPath 'C:\Users\Marco\Desktop\sistema-grupo\frontend'
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-25.0.2'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
npm.cmd run preview:isolated
```

Com o ambiente isolado acima aberto, cadastre/entre com uma conta fictícia e crie um gerenciamento de teste sem etapas de trabalho iniciais. O formulário informa que as finais são automáticas. Abra o quadro: Concluídos e Cancelados já devem aparecer, identificados como Etapa final obrigatória e sem controles de configuração, junto do aviso Nenhuma etapa de trabalho cadastrada. Selecione Nova etapa. Preencha Nome da etapa com `Comprar poste`, Setor responsável com `Almoxarifado` e Posição com `1`. A seleção deve oferecer somente `1`, pois as finais não entram na contagem. Salve e recarregue a página.

Resultado esperado: Comprar poste, setor Almoxarifado, posição 1 e zero demandas, seguido de Concluídos e Cancelados; os mesmos dados e as duas finais permanecem após recarga. Se remover Comprar poste enquanto vazio, somente o trabalho desaparece; as duas finais permanecem e o aviso de ausência de trabalho reaparece. Informe se funcionou ou descreva o problema observado. Esse resultado humano será registrado separadamente dos testes automáticos. Criar processo ainda será conectado no requisito de demandas.
