# Operação local do sistema Creral

## Histórico integral das movimentações

A próxima entrega das transições usa `historicos.descricao LONGTEXT NOT NULL` para guardar motivos e os dados anteriores de encerramento sem truncamento. A alteração abaixo é somente documentação. Não foi executada no banco configurado; conferir backup, schema e tipo atual antes de uma atualização autorizada no ambiente escolhido:

```sql
ALTER TABLE historicos MODIFY COLUMN descricao LONGTEXT NOT NULL;
```

Testes usam bancos fictícios. A aplicação normal não deve ser iniciada para aplicar essa alteração por suposição. Datas ou motivos ausentes em registros antigos continuam ausentes; a reabertura registra somente os valores efetivamente encontrados.

Os testes Maven usam H2 em memória pelo classpath de teste. Execute `mvn.cmd -B verify` em `sistema/`. A aplicação normal continua usando MySQL. Não execute o perfil normal para verificar testes.

## Experimentar o CRUD com dados fictícios

Abra um terminal na pasta `frontend/` e execute:

```powershell
npm.cmd run preview:isolated
```

O comando prepara o Java e abre o sistema em `http://localhost:4173`. Cadastre uma conta fictícia e use a lista de gerenciamentos que abre diretamente após a confirmação. Experimente criar, editar, arquivar, consultar e restaurar gerenciamentos. O quadro já abre com Concluídos e Cancelados. Use Nova etapa e os controles Editar/Remover para configurar trabalhos. Criar processo cadastra uma demanda e mostra o cartão na primeira etapa de trabalho.

Requer as dependências npm instaladas, Maven no PATH e `JAVA_HOME` apontando para o JDK. As portas 4173 e 18082 precisam estar livres. O helper inicia diretamente o executável Java desse JDK, com URL, driver, usuário, senha e `create-drop` de H2 explícitos; não inicia o MySQL configurado.

Os dados ficam em memória enquanto o comando está aberto. Recarregar a página conserva esses dados; encerrar com **Ctrl+C** fecha os dois serviços e descarta o banco fictício. Se o Windows perguntar `Terminate batch job (Y/N)?`, responda `Y`. Uma nova execução começa vazia. O proxy e o ajuste de Origin são habilitados somente nesse ambiente isolado; a configuração normal de CORS permanece a mesma.

Para repetir o teste automático de navegador, execute `npm.cmd run test:e2e` na mesma pasta, com Microsoft Edge instalado. Os cenários percorrem cadastro direto, CRUDs de gerenciamentos/etapas, cadastro/leitura, transições e edição de demandas usando teclado, conferem persistência após recarga e encerram os serviços no final. Cada arquivo de teste usa um H2 novo e contas fictícias próprias. O console informa a pasta temporária das capturas desktop/mobile. Não mantenha o preview aberto ao iniciar esse teste, pois ambos usam as mesmas portas.

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

`GET /api/gerenciamentos/{id}/estrutura-etapas` devolve `{gerenciamento,etapas,demandas}`. Os metadados do gerenciamento incluem sua versão atual e a permissão `podeAdministrar`. Cada etapa informa ID, nome, setor, ordem, categoria e `quantidadeDemandas`, calculada a partir das demandas do mesmo snapshot, incluindo todos os status. Demandas estão em ordem de ID e contêm os 14 campos flat descritos abaixo. O GET anterior `/etapas` continua devolvendo um array. Consultas não corrigem ordens, setores ou referências antigas nem preparam quadros legados.

Criador/admin pode configurar etapas de quadros ativos. POST `/etapas` responde 201; PUT/DELETE `/etapas/{etapaId}` respondem 200. Todas as mutações retornam o snapshot completo em JSON, inclusive DELETE. POST/PUT recebem `{nome,setor,ordem,versao}`; DELETE recebe `{versao}`. Use a versão retornada na próxima operação e ao editar/arquivar o gerenciamento. Repetir uma mutação com a versão anterior recebe 409; não há retry automático.

Nome e Setor são obrigatórios nas etapas de trabalho, normalizados nas bordas e limitados a 255 unidades UTF-16. Setor é informativo. Posição vai de 1 até N+1 na criação e de 1 até N na edição, contando somente trabalhos. A próxima configuração reorganiza as ordens antigas de trabalho em posições consecutivas, sem inventar setor ou alterar demandas/históricos. Trabalho com qualquer demanda não pode ser removido; mova as demandas antes. Arquivados ficam somente consulta.

Cada novo gerenciamento recebe as finais oficiais CONCLUIDA/Concluídos e CANCELADA/Cancelados, com IDs próprios e Setor nulo. O snapshot ordena trabalhos por ordem/ID, depois CONCLUIDA e CANCELADA. Essas finais não podem ser editadas, removidas nem reposicionadas; PUT/DELETE respondem 409 com `Etapas finais obrigatórias não podem ser alteradas.` mesmo quando vazias. Nomes iguais em etapas antigas ou de categoria TRABALHO continuam trabalhos. Categoria SQL nula é lida como TRABALHO, sem gravar essa classificação.

A interface informa no formulário que Concluídos e Cancelados são criados automaticamente. Ao abrir o quadro, as duas colunas oficiais mostram Etapa final obrigatória, sem Setor, posição ou Editar/Remover. Nova etapa e edição oferecem somente posições entre trabalhos. Ao remover o último trabalho, as finais permanecem e aparece Nenhuma etapa de trabalho cadastrada. Nomes homônimos de trabalho mantêm seu Setor e controles; a indicação distingue as oficiais.

Campos são preservados em falha; Atualizar quadro consulta os dados sem repetir gravação. Enquanto uma gravação está pendente, Voltar, Cancelar e Sair ficam bloqueados. As ações de concluir/cancelar e reabrir usam as categorias oficiais conforme o contrato de transições abaixo. A preparação continua sendo uma operação explícita para legados.

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

Resultado esperado: Comprar poste, setor Almoxarifado, posição 1 e zero demandas, seguido de Concluídos e Cancelados; os mesmos dados e as duas finais permanecem após recarga. Se remover Comprar poste enquanto vazio, somente o trabalho desaparece; as duas finais permanecem e o aviso de ausência de trabalho reaparece. Informe se funcionou ou descreva o problema observado. Esse resultado humano será registrado separadamente dos testes automáticos.

## Cadastro e leitura de demandas

Todos os usuários autenticados podem cadastrar demandas em quadros ativos, inclusive quadros criados por outras pessoas ou sem criador identificado. O servidor escolhe a primeira etapa de trabalho por ordem e ID; Concluídos e Cancelados ficam reservados às ações finais. Sem trabalho, Criar processo fica desabilitado. Criador/admin usa Nova etapa; outros funcionários solicitam essa configuração ao criador ou administrador.

`POST /api/gerenciamentos/{id}/demandas` recebe exatamente `{versao,numeroProcesso,pessoa,responsavel,prioridade,dataEmissao,prazoEtapa,prazoGeral,observacoes}` e responde 201 com o snapshot completo. O número é manual e único em todo o sistema, sem distinguir caixa. Número e cliente/solicitante são obrigatórios, entre 1 e 255 unidades UTF-16 após trim. Responsável e prioridade são textos opcionais até 255; observações até 10000, preservando linhas internas. Textos opcionais vazios viram null. As três datas são string ISO válida ou null; não há geração da data atual nem exigência de ordem entre prazos.

O registro começa com status `Em andamento`, datas de conclusão/cancelamento e motivo null. O novo cadastro incrementa a versão exatamente uma vez e grava um histórico CRIACAO com autor da sessão na mesma transação. Cadastro legado por `/api/processos` mantém HTTP200 e sua política de versão, mas também exige a primeira etapa persistida e estado inicial definido pelo sistema. A edição informativa está entregue conforme o contrato abaixo. A retirada de demandas ainda depende de uma política própria. Transições seguem o contrato abaixo.

Cada demanda do snapshot contém `{id,numeroProcesso,pessoa,responsavel,status,prioridade,dataEmissao,prazoEtapa,prazoGeral,dataConclusao,dataCancelamento,motivoCancelamento,observacoes,etapaId}`. A interface renderiza cartões reais no vínculo indicado, com ações de transição conforme permissão e estado, e apresenta datas DD/MM/YYYY. Valores antigos permanecem literais; ausências aparecem como Não informado. Cadastro e configuração de etapas usam o mesmo snapshot, preservando cartões e contagens. Quadros arquivados exibem os mesmos cartões somente para consulta.

Falhas preservam todos os campos. Atualizar quadro consulta os dados atuais antes de um novo envio manual; o sistema não repete POST automaticamente. Resposta204, JSON incompleto, IDs/vínculos inválidos ou contagens divergentes não confirmam sucesso. Durante o envio, campos, Cancelar, Voltar, Sair e outras mutações ficam desabilitados.

No ambiente fictício, cadastre uma demanda usando apenas número ainda não utilizado e cliente. Resultado esperado: cartão na primeira etapa de trabalho com os opcionais não informados. Recarregue e abra o quadro; ID, campos e contagem devem permanecer. Cadastre outra demanda com responsável, prioridade livre, datas e observações. Tente o primeiro número em outro quadro: deve aparecer Já existe uma demanda com esse número., mantendo o rascunho. Consulte o quadro arquivado e confirme a leitura dos mesmos cartões. O resultado humano desse roteiro permanece separado dos testes automáticos.

A verificação MySQL fictícia desta entrega usou MySQL8.0.43/JDK25 e passou145 testes selecionados: DemandaConsultaTests(6), DemandaCadastroTests(92), DemandaCadastroConcurrencyTests(14), EtapaDemandConcurrencyTests(7), ProcessoArchiveTests(11), HistoricoArchiveTests(15). Foram provados UTF-16/emoji, campos/defaults, UNIQUE1062/409, CHECK/500 e rollback, corridas globais e disputas estruturais. Relatório preservado em `C:/Users/Marco/AppData/Local/Temp/creral-demandas-mysql-70e509769b814ba59a5a557b6b39cdd3/mysql-evidence.md`; log PASS `mysql-selected-verify-2.log`. A primeira tentativa teve URL truncada no shell e145 erros de bootstrap, sem evidência funcional; permanece em `mysql-selected-verify.log`. A instância própria foi encerrada após conferência de proveniência. Nenhum banco configurado ou conta real foi alterado.

## Movimentação, encerramento e reabertura de demandas

Somente o criador do quadro ou um administrador pode usar essas ações em quadro ativo. Os demais usuários acompanham os cartões. Mover/pular etapa permite escolher qualquer outra etapa de trabalho do mesmo quadro, inclusive anterior ou não consecutiva. As colunas finais oficiais continuam protegidas contra edição e remoção.

Concluir envia para Concluídos e registra a data atual do servidor. Cancelar exige motivo entre 1 e 10000 caracteres UTF-16 após trim, envia para Cancelados e registra a data atual do servidor. A ação limpa a data e o motivo incompatíveis com seu novo estado. Reabrir escolhe uma etapa de trabalho e retorna para Em andamento, limpando dataConclusao, dataCancelamento e motivoCancelamento atuais; o evento REABERTURA preserva literalmente os valores anteriores. Histórico recebe autoria da sessão e dataHora, sem inventar encerramentos antigos.

As quatro rotas PUT `/api/gerenciamentos/{quadroId}/demandas/{demandaId}/{acao}` respondem HTTP 200 com o snapshot completo. Mover/reabrir recebem `{versao,etapaId}`; concluir `{versao}`; cancelar `{versao,motivo}`. Versão deve ser a do snapshot consultado. Cada ação aceita grava demanda, um evento e versão+1 na mesma transação, sob lock do quadro. Versão antiga recebe 409 sem efeitos. Repetição de encerramento, mesmo destino, estado incompatível ou desconhecido também recebe 409. Destino externo/inexistente recebe 404; destino final para mover/reabrir ou motivo inválido recebe 400. Sessão inválida recebe 401; usuário sem permissão recebe 403. Quadro arquivado exige restauração.

As rotas antigas de mover/concluir/cancelar preservam HTTP 200 com entidade e seguem as mesmas regras de permissão, estado, histórico e transação, incrementando a versão. PUT genérico não permite mudar etapa, status ou campos de encerramento; edição informativa agora segue criador/admin e número fixo, conforme o contrato abaixo. Quadro antigo sem a final necessária deve passar pela preparação explícita em ambiente autorizado. As ações normais não migram colunas ou registros.

## Edição informativa de demandas

Em quadro ativo, o criador ou administrador pode usar Editar no cartão. O formulário abre preenchido e permite corrigir cliente/solicitante, responsável, prioridade, data de emissão, prazo da etapa, prazo geral e observações. O número aparece somente para leitura. Demandas concluídas/canceladas podem receber essas correções sem reabertura: status, etapa, datas de encerramento e motivo ficam literalmente preservados. Quadros arquivados e demandas com status antigo desconhecido continuam somente consulta.

Cliente é obrigatório, de 1 a 255 unidades UTF-16 após normalização nas bordas. Responsável e prioridade são opcionais até 255; observações até 10000. Campos opcionais vazios viram null; datas opcionais usam ISO ou null. Não há exigência nova de ordem entre datas. Salvar registra um evento EDICAO com autor real da sessão, descrição Processo alterado. e data/hora; demanda, evento e versão são gravados numa transação. Mesmo um envio válido sem diferenças registra a edição explícita.

`PUT /api/gerenciamentos/{id}/demandas/{demandaId}` recebe o formulário completo:

```json
{"versao":2,"pessoa":"Cliente corrigido","responsavel":null,"prioridade":null,"dataEmissao":null,"prazoEtapa":"2026-10-07","prazoGeral":null,"observacoes":"Informação corrigida"}
```

Responde HTTP 200 com `{gerenciamento,etapas,demandas}` e versão anterior +1. Número, ID, status, etapa e encerramentos não pertencem à entrada. A rota antiga `PUT /api/processos/{id}` conserva retorno de entidade e entrada sem versão, mas aplica o mesmo lock, permissão, validação, preservação e incremento. Número omitido ou idêntico conserva a referência; diferente recebe 400 com O número da demanda não pode ser alterado após o cadastro. Essa compatibilidade não permite contornar o ciclo.

Durante envio, formulário e demais mutações/saída ficam bloqueados. Cancelar fecha sem gravar. Erro HTTP, rede ou resposta não confirmada preserva o rascunho e exige Atualizar quadro antes de novo envio manual. A atualização não substitui os campos digitados nem repete a mutação automaticamente. Só uma resposta que confirma demanda, novos campos, ciclo e versão fecha o formulário. Se o quadro exibido mudar, respostas antigas não são aplicadas nele.

### Roteiro humano da edição

No preview fictício, entre como criador, abra um quadro ativo e cadastre uma demanda. Use Editar por teclado, confira número somente leitura, altere cliente e observações e limpe responsável. Salve: cartão e recarga devem conservar os novos valores e mostrar Não informado para responsável. Abra Editar novamente e cancele após digitar; os dados gravados devem continuar iguais.

Conclua a demanda e edite somente cliente ou observações. Ela deve continuar em Concluídos, com a mesma data de conclusão. Reabra e cancele com justificativa; edite informações novamente e confira Cancelados, data e motivo preservados. Confira EDICAO e autoria no histórico disponível pela API. Entre como outra conta comum: ela consulta o cartão, mas não vê Editar. Arquive o quadro: edição deixa de estar disponível até a restauração.

Para conflito, mantenha Editar aberto numa aba e altere o quadro noutra sessão do criador/admin. Salvar com versão antiga deve anunciar o conflito, manter os campos e oferecer atualização manual. Depois de Atualizar quadro, revise o rascunho e envie somente se desejar aplicar aquelas informações. Informe o resultado observado; este roteiro humano ainda não foi declarado executado pelos usuários da Creral.

Enquanto uma ação está pendente, campos e outros controles de alteração, Voltar, Sair e Voltar sem alterar ficam bloqueados. Erro HTTP, rede ou resposta sem confirmação conserva diálogo e rascunho e exige Atualizar quadro antes de tentar manualmente. Não há repetição automática da gravação. Resposta confirmada atualiza cartões, contagens e versão e fecha o diálogo com foco em controle existente.

### Roteiro humano com dados fictícios

Abra `npm.cmd run preview:isolated` conforme as instruções anteriores e use uma conta fictícia para criar um quadro com Comprar, Verificar e Instalar. Cadastre uma demanda. No cartão, escolha Mover/pular etapa e envie diretamente para Instalar; repita voltando para Comprar. Conclua: o cartão deve aparecer em Concluídos com data. Reabra para Verificar: deve voltar a Em andamento sem campos atuais de encerramento. Cancele com motivo Cliente desistiu: deve aparecer em Cancelados com data e motivo. Reabra para Comprar e recarregue; a demanda deve continuar no trabalho escolhido com os demais campos preservados.

Uma segunda conta fictícia pode consultar, mas não deve ver essas ações. Arquive o quadro e confirme a consulta sem ações; restaure antes de novas alterações. Para observar conflito, abra o mesmo quadro em duas sessões do criador: altere na primeira e tente uma ação na segunda sem atualizar. Deve aparecer a orientação de atualização, com seleção/motivo preservados; Atualizar quadro não repete a gravação. Confira o histórico pela API de consulta existente; a apresentação visual de logs/comentários continua em entrega própria.

Registre se cada resultado ocorreu e qualquer problema observado. O roteiro está disponível; a aceitação humana só será registrada após o usuário experimentar e informar o resultado.
