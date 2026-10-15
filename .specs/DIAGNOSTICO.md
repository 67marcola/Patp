# Diagnóstico e resumo atual do sistema Creral

## GitHub — código enviado e conferido em15/10/2026

**Incluído no diagnóstico:** usuário criou e informou https://github.com/67marcola/-creral-piloto-online. (o hífen inicial e o ponto final fazem parte do nome real). Conector confirmou repositório privado, permissão de escrita e branch main com somente README.md. Não é necessário login no navegador do Codex para enviar por esse conector.

**Concluído e verificado — tudo ok: envio da fonte ao GitHub privado. Incluído no diagnóstico.** Após autorização explícita do usuário, enviados139arquivos,801.156bytes,138textos e1imagem para main, preservando README.md. Commit remoto c496d91b495dea5b2fa2fa299c337cb15c375377; árvore4b8df7aa130df88c93c8daf7dd1589fe27ef7c57. Conferência posterior confirmou repositório privado,140arquivos totais e139/139 hashes gitblob iguais à cópia verificada. Atualização sem force, condicionada ao head anterior; sem histórico antigo, credenciais/configuração local/certificados. Aiven já preparado; Render/Vercel/publicação4G seguem pendentes.

## Aiven — base online preparada em15/10/2026

**Concluído e verificado — tudo ok: estrutura MySQL online. Incluído no diagnóstico.** Após consulta TLS confirmar defaultdb vazio (MySQL8.4.11), plano JDBC somente leitura passou. Usuário autorizou explicitamente criar as oito tabelas somente no Aiven. PrepararBancoInternet --aplicar concluiu exit0; consulta posterior confirmou acessos_usuarios,atividades,comentarios,etapas,gerenciamentos,historicos,processos,usuarios, todas com zero registros. Perfil internet do Spring Boot validou a estrutura com ddl-auto=validate/SQLinitnever, sem web ou migração, exit0.

Nenhuma conta/senha/demanda local foi copiada e nenhum administrador online existe ainda. Senha informada utilizada somente em memória e entrada oculta; usuário decidiu mantê-la para este teste. Fonte já enviada ao repositório privado separado pelo conector autorizado, sem necessidade de login no navegador do Codex. Próximo passo: configurar Render/Vercel e verificar API/interface públicas e4G.

## Aiven — conexão segura conferida em15/10/2026

**Incluído no diagnóstico — etapa de infraestrutura:** usuário informou o serviço MySQL creral-mysql-piloto-marcoimmarquin-2e37.f.aivencloud.com, porta21005, banco defaultdb e usuário avnadmin; CA em C:/Users/Marco/Desktop/ca.pem. Certificado local válido e handshake MySQL STARTTLS remoto conferido: TLS1.3, verificação da cadeia e hostname OK, exit0. CA pública e truststore PKCS12 preparados em diretório ignorado pelo Git.

Nenhuma senha foi usada, repetida ou gravada nesta conferência; nenhum login/SQL/DDL executado. A URI enviada continha senha, e foi recomendada sua redefinição antes da publicação. Próximo passo: consulta autenticada somente leitura do schema, com senha digitada localmente; depois preparar/revisar a criação das oito tabelas. Banco vazio, estrutura criada, API publicada e acesso4G continuam sem confirmação.

## Internet — preparação local verificada em15/10/2026

**Concluído e verificado — tudo ok: preparação técnica local. Incluído no diagnóstico.** Cadastro pendente, aprovação administrativa, sessão online de8h, saída com revogação, pacote Render/Vercel, preparador explícito da base nova e guia prontos. Verifier independente aprovou o controle final (1/1,88,9s), confirmou o histórico completo após reinício e preservou4.590 hashes; sensor de liberação indevida detectado. Gates focados:77casos distintos;15invocações totais (autor12,Verifier3), incluindo falhas/reforços registrados, sem suítes históricas. [Verificação e limites](features/acesso-internet/validation.md).

**Acesso público ainda pendente:** faltam criar/configurar os serviços, construir/conferir a imagem real no Render, publicar e observar computador/celular fora da LAN (NET-01/02/19). O gate global permanece FAIL por essas pendências externas; testes locais não comprovam4G. Usuário confirmou em15/10 que criou as três contas (Vercel,Render,Aiven). Próximo passo: serviço MySQL gratuito vazio no Aiven e dados de conexão/CA, sem senha no chat; depois Render e Vercel. [Guia de publicação](../docs/PUBLICACAO_INTERNET.md).

Os checkpoints abaixo preservam o andamento anterior; o estado atual é este fechamento local com publicação pendente.

## Internet — complemento de evidência em15/10/2026

**Incluído no diagnóstico — T12:** a revisão independente apontou que faltava comparar o histórico completo após reinício. Acrescentada a comparação dos seis campos, com histórico não vazio antes do reinício; cenário focado passou1/1 na primeira tentativa,91,4s. Nenhuma mudança no comportamento do sistema ou nos testes antigos. Autor12 invocações/77casos distintos; rechecagem independente desse complemento em seguida. Publicação/imagem real/4G continuam pendentes.

## Internet — progresso técnico em 14/10/2026

**Incluído no diagnóstico — T11:** guia operacional pronto em docs/PUBLICACAO_INTERNET.md e cópia local da fonte de publicação preparada sem configurações/segredos/histórico locais. Ainda sem envio, imagem Docker ou URL pública. Próximo passo técnico é o Verifier independente; depois, criação/configuração dos serviços e conferência real com dados móveis.

**Incluído no diagnóstico — T10:** integração nova passou na primeira tentativa (1 cenário,86,7s), com TLS MySQL real fictício33823, HTTPS local, dois contextos Edge e tela360px. Cadastro pendente, aprovação administrativa, demanda compartilhada, persistência após reinício e revogação ao sair conferidos; configurações inseguras/CA não confiável/estrutura incompatível recusadas. Capturas aprovadas em features/acesso-internet/qa. Total autor77 casos distintos,11 invocações incluindo tentativas anteriores; nenhuma suíte histórica repetida. Verifier independente e publicação/4G ainda pendentes.

**Incluído no diagnóstico — T9:** configuração Vercel preparada com build obrigatoriamente online e API HTTPS pública validada, sem credenciais/query/fragmento/host local ou privado. Build público fictício/assets e lint aprovados em TEMP; nove configurações inválidas recusadas, build local conservado e exceção de fixture HTTPS restrita ao processo de build. Sem nova invocação de testes: total comportamental permanece dez. Nenhum serviço externo ou URL real criado; T10/T11/Verifier/publicação e prova4G permanecem pendentes.

**Incluído no diagnóstico — T8:** pacote Render preparado, classes/runtime compilados em TEMP e sintaxe do inicializador conferida. Configuração online exige TLS, origem HTTPS e estrutura validada, excluindo credenciais locais do contexto Docker. Imagem Docker/publicação ainda não executadas; provas integradas e Verifier pendentes.

**Em implementação, ainda sem publicação:** T1–T9 concluídas localmente. Servidor/preparo explícito, cadastro pendente, painel administrativo, saída online e pacotes Render/Vercel preparados; papel conferido pelo servidor, cache público conservado e respostas incertas explícitas. Gates finais TEMP:76 casos distintos aprovados (21 Java,55 Vitest), dez invocações contando duas falhas corrigidas e reforço de StrictMode. Alvo de sete no lote excedido, sem declarar economia alvo como alcançada. Builds HTTPS fictícios/lint sem diagnósticos aprovados. Nenhum teste antigo alterado. Fixture MySQL33823 ativa para integração. [Evidências](features/acesso-internet/evidence.md). T10/T11, layout real360px e Verifier pendentes; acesso por dados móveis ainda não observado.

## Segundo administrador e teste pela rede — 14/10/2026

**Concluído e verificado — tudo ok. Incluído no diagnóstico:** conta existente Admin, admin@gmail.com, ID3 no MySQL local sistema, promovida de FUNCIONARIO para ADMINISTRADOR por pedido explícito do usuário. Alterado somente papel, em transação limitada por ID, e-mail e nome exatos e papel anterior; uma conta alterada. Leitura posterior e auditoria independente confirmaram Marco (ID2) e Admin (ID3) como administradores, total de dois. Novos cadastros continuam FUNCIONARIO. [Evidência operacional](features/acesso-piloto/evidence.md).

**Acesso por outro computador na mesma rede: confirmado pelo usuário. Incluído no diagnóstico.** Em 14/10/2026, o usuário informou que testou em outro computador no mesmo Wi-Fi e funcionou. Esse relato encerra a pendência de confirmar a conexão entre computadores. Não foram informados detalhes sobre operações realizadas, teste em celular ou aceite de todos os fluxos pelos estudantes.

**Acesso por dados móveis em implementação — incluído no diagnóstico:** usuário escolheu Vercel + Render + Aiven, base nova de testes e aprovação de novos cadastros por administrador; pediu continuar após apresentação do fluxo e reforçou o uso no celular com dados móveis. [Spec com 20 critérios](features/acesso-internet/spec.md), [design](features/acesso-internet/design.md) e [11 tarefas](features/acesso-internet/tasks.md) preparados. Sessão online8h, aprovação individual e primeiro admin por procedimento explícito; piloto local preservado. Usuário tem somente Vercel e foi orientado a criar Render/Aiven grátis. Nenhuma publicação ou URL4G disponível ainda; não marcar Internet concluída antes da prova correspondente.

## Preparação técnica do piloto — concluída em 14/10/2026

**Concluído e verificado — tudo ok. Incluído no diagnóstico.** O comando do piloto está pronto para servir interface e API pelo mesmo endereço deste computador, com dados persistentes no MySQL. Os colegas usarão o navegador na mesma rede, inicialmente como estudantes; o uso posterior pelos empregados da Creral terá preparação própria. [Verificação independente](features/acesso-piloto/validation.md): oito critérios PASS, seis testes Node e um cenário integrado com dois contextos Edge aprovados, lint ok e sensor de proteção do banco 1/1 detectado.

| Requisito | Resultado | Estado |
| --- | --- | --- |
| PIL-01 | Interface e API pelo mesmo endereço central, com URL informada após prontidão | Concluído e verificado — tudo ok |
| PIL-02 | MySQL local, contas e dados conservados após encerrar e reiniciar | Concluído e verificado — tudo ok |
| PIL-03 | Estrutura validada; banco incompatível recusado sem criação de tabelas | Concluído e verificado — tudo ok |
| PIL-04 | Entradas inválidas, porta ocupada e dependência ausente recusadas sem falso sucesso | Concluído e verificado — tudo ok |
| PIL-05 | Encerramento dos próprios processos, limpeza restrita e preservação dos serviços existentes | Concluído e verificado — tudo ok |
| PIL-06 | Autenticação e papéis preservados, credenciais somente no servidor | Concluído e verificado — tudo ok |
| PIL-07 | Duas sessões autenticadas consultam a mesma demanda e respeitam permissões | Concluído e verificado — tudo ok |
| PIL-08 | Guia de operação e roteiro humano, com acesso físico e aceite explicitamente pendentes | Concluído e verificado — tudo ok |

Dez invocações de testes nesta entrega, incluindo tentativas e sensor: sete do autor e três do Verifier, 50% menos que a referência de vinte do fluxo anterior (AD-035). Essa contagem não mede tokens. Nenhum teste antigo foi alterado, removido ou ignorado; nenhuma suíte completa foi repetida. O scratch foi removido e 4.701 hashes da árvore real ficaram iguais. O MySQL fictício de testes na porta 33823 foi encerrado após conferência de identidade; os serviços reais 3306/8081/5173 permanecem ativos.

O MySQL local passou na conferência de estrutura com validate, sem servidor web ou migração. **O usuário confirmou acesso de outro computador no mesmo Wi-Fi em 14/10/2026.** O aceite dos demais fluxos pelos estudantes continua pendente. O endereço preparado foi http://192.168.0.102:8082; conferir o IP se a rede mudar. Comandos e roteiro em [docs/OPERACAO.md](../docs/OPERACAO.md). A prova automatizada usou duas sessões no mesmo computador e banco fictício; o teste físico foi relatado pelo usuário separadamente. Histórico dos passos e tentativas em [evidence.md](features/acesso-piloto/evidence.md).

**Administrador inicial: Concluído e verificado — tudo ok (configuração operacional).** Marco Polinski, marcoimmarquin@gmail.com, ID2 no MySQL local sistema, foi o primeiro administrador. Depois, por indicação explícita do usuário, admin@gmail.com (ID3) também foi promovido, total atual de dois administradores. Novos cadastros continuam FUNCIONARIO. [Evidências](features/acesso-piloto/evidence.md).

Os papéis existentes continuam Funcionário e Administrador; gerente ainda não é um papel separado. Conexão entre computadores confirmada pelo usuário; avaliação dos fluxos pelos estudantes e acesso pela internet permanecem pendentes.


**Atualizado em 14/10/2026.** Este resumo foi conferido com os relatórios de validação, as tarefas da skill `tlc-spec-driven`, o registro de decisões e os commits locais. “Pronto” significa implementado e aprovado tecnicamente nos testes e na revisão independente; a avaliação prática pelos usuários da Creral ainda está pendente.

## Revisão visual geral — concluída em14/10/2026

**Concluído e verificado — tudo ok. Incluído no diagnóstico.** Creral identificada em login, cadastro e cabeçalho; aba e idioma corrigidos, rótulos associados, contraste/foco/controles e quebras em celular padronizados. Estrutura e regras preservadas. Identidade textual, sem logotipo oficial ou certificação integral de acessibilidade. [Verificação independente](features/revisao-visual/validation.md):7/7 critérios PASS;3 testes focados e1 Edge/H2,lint/build aprovados;sensor visual1/1. Autor também executou os6 testes existentes de sessão. Quinze estados em1280/360px conferidos.

Dez invocações totais de testes, incluindo tentativas de preparação,50% menos que a referência de vinte do fluxo anterior (AD-035). Não mede tokens. Nenhum teste existente removido, ignorado ou enfraquecido. Sem suítes completas ou alteração de servidor/banco. Aceitação humana permanece pendente. Checkpoints anteriores e correções autorizadas estão nas [evidências](features/revisao-visual/evidence.md).

| Requisito | Resultado | Estado |
| --- | --- | --- |
| VIS-01 | Creral nas telas, aba identificada e documento em português | Concluído e verificado — tudo ok |
| VIS-02 | Rótulos do login focam campos com autocomplete adequado | Concluído e verificado — tudo ok |
| VIS-03 | Paleta preservada, contraste, fonte e controles44px | Concluído e verificado — tudo ok |
| VIS-04 | Foco de teclado visível nos quatro tipos de controle | Concluído e verificado — tudo ok |
| VIS-05 | Telas/formulários/painéis sem overflow em1280/360 | Concluído e verificado — tudo ok |
| VIS-06 | Carregamento, vazio, erro e arquivado preservados e distintos | Concluído e verificado — tudo ok |
| VIS-07 | Sessão, rascunhos e ações preservados, sem novas requisições | Concluído e verificado — tudo ok |

## Gráficos por quadro — concluídos em 14/10/2026

**Concluído e verificado — tudo ok. Incluído no diagnóstico.** O botão Gráficos junto ao título abre barras da situação atual por situação e etapa, com arquivadas separadas. Dados acompanham o quadro, sem requisição extra, alteração de banco ou biblioteca nova. [Verificação independente](features/graficos-quadro/validation.md):9/9 critérios PASS, seis testes focados e um cenário real Edge/H2 aprovados, lint/build ok, sensor2/2. Teste prático pelos usuários pendente.

| Requisito | Resultado | Estado |
| --- | --- | --- |
| GRAF-01 | Abrir/fechar pelo botão, identificação acessível e foco preservado | Concluído e verificado — tudo ok |
| GRAF-02 | Situações conhecidas e Não informado com quantidades exatas | Concluído e verificado — tudo ok |
| GRAF-03 | Distribuição por ID da etapa, inclusive zero e etapa não identificada | Concluído e verificado — tudo ok |
| GRAF-04 | Totais conferem; arquivadas separadas das barras | Concluído e verificado — tudo ok |
| GRAF-05 | Atualização pelo snapshot e troca sem dados do quadro anterior | Concluído e verificado — tudo ok |
| GRAF-06 | Erro/conflito oculta números até atualizar | Concluído e verificado — tudo ok |
| GRAF-07 | Consulta para todos autenticados, inclusive arquivados, sem nova chamada | Concluído e verificado — tudo ok |
| GRAF-08 | Vazio explícito, zeros e barras válidas | Concluído e verificado — tudo ok |
| GRAF-09 | Rótulos legíveis em360px e rascunhos preservados | Concluído e verificado — tudo ok |

## Como o sistema está organizado

Cada **gerenciamento é um quadro** com várias **demandas**. As etapas são as colunas desse quadro. Por exemplo: um quadro de instalações de postes pode acompanhar diferentes solicitações, passando por Comprar, Verificar local e Instalar. Cada demanda tem sua própria etapa atual. “Criar processo”, na interface, cadastra uma demanda.

A empresa está identificada como **Creral**. O projeto usa Java/Spring Boot no servidor, interface web e MySQL como banco previsto para uso; os testes também usam H2 isolado. Workbench é a ferramenta indicada para administrar o MySQL.

**Tecnologias confirmadas pelo usuário em 07/10/2026 (AD-024):**

| Parte | Tecnologias e finalidade |
| --- | --- |
| Servidor | Java, Spring Boot, Spring Web MVC e Spring Data JPA/Hibernate para API, regras de negócio e persistência. |
| Dados | MySQL como banco do sistema; MySQL Workbench para administração do banco. |
| Interface | React, JavaScript, HTML/CSS e Vite. |
| Construção | Maven no servidor; Node.js/npm na interface. |
| Testes | JUnit e H2 isolado; Vitest, Testing Library e Playwright. H2 é usado somente nos testes. |

**Acompanhamento combinado, reforçado em 10/10/2026:** toda implementação de requisito será registrada aqui com o estado real e comunicada como **“incluído no diagnóstico”**. Tarefas e critérios parcialmente concluídos serão identificados como progresso parcial até a aprovação técnica da entrega. Depois da implementação, dos testes pertinentes aprovados e da verificação TLC, registrar **“Concluído e verificado — tudo ok”**, com data, resultado dos testes e referência à evidência/validação. Atualizar a memória e seguir para o próximo requisito pendente; não refazer entregas já comprovadas. Essa regra permanente está no início de [STATE.md](STATE.md).

**Registro de acompanhamento em 10/10/2026 — incluído no diagnóstico:** a regra solicitada está na memória e os comentários por demanda foram concluídos e verificados conforme a seção abaixo. Próximo requisito: atividade por gerenciamento; alcance de quadro/etapas/demandas e consulta por todos os autenticados confirmados em AD-030/031, definição detalhada em andamento.

## O que está pronto e validado

**14/10/2026 — incluído no diagnóstico:** gráficos por quadro: **Concluído e verificado — tudo ok**, 9/9 critérios independentes PASS, sensor2/2. Atividade por gerenciamento também encerrada. AD-035 registra redução de execuções: dez nesta entrega, pelo menos50% menos que os vinte gates mínimos da anterior; sem medir economia de tokens. Testes existentes preservados. Aceitação humana pendente.

| Entrega | O que já é possível fazer | Evidência |
| --- | --- | --- |
| Preparação técnica do piloto | Iniciar interface/API no mesmo endereço, com MySQL persistente, validação da estrutura e encerramento próprio; acesso de outro computador no mesmo Wi-Fi confirmado pelo usuário. | [8 critérios aprovados](features/acesso-piloto/validation.md); relato posterior em [evidence.md](features/acesso-piloto/evidence.md), aceite dos fluxos pendente. |
| Revisão visual geral | Identificação Creral, rótulos acessíveis, contraste/foco, controles44px e layout em computador/celular, com regras preservadas. | [7 critérios aprovados](features/revisao-visual/validation.md). |
| Gráficos por quadro | Consultar distribuição atual por situação e etapa, com total de arquivadas separado; acesso para todos autenticados, inclusive quadros arquivados. | [9 critérios aprovados](features/graficos-quadro/validation.md). |
| Gerenciamentos | Criar, listar, consultar, editar, arquivar e restaurar quadros. Todos os usuários autenticados podem criar e consultar; edição e administração ficam com o criador ou administradores. Arquivar preserva os dados e deixa o conteúdo somente para consulta. | [41 critérios aprovados](features/gerenciamentos/validation.md). |
| Etapas de trabalho | Criar, renomear, definir setor, escolher posição e remover colunas vazias. A ordem das demais se ajusta automaticamente. Uma etapa com demandas não pode ser removida. Somente criador/admin configura etapas de quadros ativos. | [30 critérios aprovados](features/etapas/validation.md). |
| Entrada após cadastro | Cadastro confirmado abre o sistema sem exigir outro login. Há proteção contra envio duplicado, tratamento de erros e preservação dos campos. Contas comuns começam como funcionário; respostas e dados da sessão não expõem senha ou hash. | [14 critérios aprovados](features/autocadastro/validation.md). |
| Retirada do PM | Os blocos PM foram removidos do login e cadastro, incluindo seus estilos exclusivos. As telas foram verificadas em desktop e celular. | [4 critérios aprovados](features/marca-login/validation.md). |
| Etapas finais obrigatórias | Novos quadros recebem Concluídos e Cancelados. Essas colunas são identificadas por categoria e protegidas contra edição e remoção. Existe ferramenta explícita de preparação para dados antigos, com plano sem gravação e aplicação transacional/idempotente. | [30 critérios aprovados](features/etapas-finais/validation.md). |
| Cadastro e consulta de demandas | O botão Criar processo funciona. Número e cliente/solicitante são obrigatórios; os demais campos do cadastro são opcionais. A demanda começa na primeira etapa de trabalho. O número é único no sistema, inclusive entre quadros e sem distinguir maiúsculas de minúsculas. Cartões mostram dados reais, com contagens e preservação após recarga. | [41 critérios aprovados](features/demandas-cadastro/validation.md). |
| Movimentação e encerramento de demandas | Criador/admin move ou pula etapas, conclui, cancela com motivo e reabre para trabalho escolhido. Cartões, contagens, versão e histórico são preservados. | [35 critérios e edge aprovados](features/demandas-movimentacao/validation.md). |
| Edição informativa de demandas | Corrigir cliente, responsável, prioridade, emissão, prazos e observações. Número fixo; demandas concluídas/canceladas mantêm seu encerramento. Somente criador/admin em quadro ativo. | [29 critérios e edges aprovados](features/demandas-edicao/validation.md). |
| Arquivamento e restauração de demandas | Arquivar demandas em andamento, concluídas ou canceladas; consultar Arquivadas e restaurar na mesma etapa e estado, preservando dados, número, comentários e histórico. Somente criador/admin em quadro ativo altera o arquivo. | [30 critérios e 10 bordas aprovados](features/demandas-arquivamento/validation.md). |
| Comentários por demanda | Conversar com autor da sessão e horário, mensagens separadas por demanda, consulta de arquivados, recuperação manual de envio incerto e uso por teclado no computador/celular. | [28 critérios e bordas aprovados](features/comentarios-lateral/validation.md). |
| Atividade por gerenciamento | Consultar alterações do quadro, etapas e demandas com autor, horário e antes/depois, inclusive em arquivados; histórico existente identificado e preservado, comentários registrados sem duplicar a mensagem,50 registros por página. | [39 critérios e bordas aprovados](features/atividade-gerenciamento/validation.md),sensor8/8. |

A criação de demandas registra histórico com autoria da sessão e é transacional: uma falha não deve deixar apenas parte dos dados gravada. O frontend confirma que a resposta contém a demanda solicitada antes de fechar o formulário; respostas incompletas ou divergentes preservam o rascunho e exigem atualização manual.

## Movimentação e encerramento de demandas

**Movimentação, salto de etapas, conclusão, cancelamento e reabertura de demandas** estão implementados e validados independentemente. T1 a T8 estão concluídas, incluindo a correção de duas lacunas de teste encontradas pelo Verifier. Consulte [tarefas](features/demandas-movimentacao/tasks.md), [evidências](features/demandas-movimentacao/evidence.md) e [validação independente](features/demandas-movimentacao/validation.md).

Já foram implementados e testados o serviço de transição, as quatro rotas da API, a confirmação de respostas no frontend e os diálogos das ações. As regras implementadas incluem:

- Somente criador do quadro ou administrador pode executar essas ações em gerenciamento ativo.
- Mover/pular permite escolher outra etapa de trabalho do mesmo quadro, inclusive anterior ou não consecutiva.
- Concluir leva automaticamente a Concluídos; cancelar leva a Cancelados e exige justificativa.
- Reabrir permite escolher uma etapa de trabalho, retorna para Em andamento e limpa os campos atuais de encerramento, preservando os valores anteriores no histórico.
- Status antigos desconhecidos permanecem consultáveis, mas as ações ficam bloqueadas até correção explícita.
- Versão do quadro, permissões, histórico e gravação transacional protegem contra alterações conflitantes e resultados parciais.

**Validação desta entrega:** 479 testes Java/H2, 661 da interface, lint/build, 7 fluxos Edge/H2 e 122 testes selecionados em MySQL fictício aprovados. O Verifier confirmou 35/35 critérios e o edge de estado incompatível. As 14 falhas simuladas foram detectadas após duas rodadas: 12 na primeira e as duas mensagens restantes depois dos dez novos casos HTTP. O código da aplicação não precisou de alteração nessa correção; nenhum teste anterior foi excluído ou enfraquecido. A aceitação humana permanece pendente.

## Edição de demandas — concluída

**Entrega técnica concluída em 08/10/2026:** as oito tarefas e os 29 critérios EDI-01 a EDI-29 foram aprovados pela revisão independente. Consulte [tarefas](features/demandas-edicao/tasks.md), [evidências](features/demandas-edicao/evidence.md) e [validação](features/demandas-edicao/validation.md).

O botão **Editar** abre o formulário preenchido para corrigir cliente/solicitante, responsável, prioridade, data de emissão, prazo da etapa, prazo geral e observações. O número aparece somente para leitura e permanece fixo, inclusive na rota antiga. Apenas o criador do quadro ou um administrador pode editar, em quadro ativo.

Demandas concluídas ou canceladas também permitem essas correções, preservando status, etapa, datas e motivo do encerramento. Cada salvamento registra um evento EDICAO com autor da sessão e atualiza a versão uma vez. Falhas revertem toda a gravação; conflitos ou respostas não confirmadas conservam o rascunho e exigem atualização manual. Registros com status antigo desconhecido permanecem para consulta.

**Validação:** 570 Java/H2, 721 testes da interface, 9 fluxos Edge/H2 e 213 selecionados em MySQL fictício; lint/build aprovados. O sensor independente detectou 9/9 falhas simuladas em cópia isolada e confirmou a preservação integral da árvore real. Todos esses gates foram executados novamente pelo Verifier; os detalhes estão no relatório. Não houve exclusão ou salto de testes. Desktop e celular foram conferidos. O MySQL temporário não está ativo no fechamento; nenhum banco real foi utilizado. **Aceitação humana pendente**, com roteiro em [OPERACAO.md](../docs/OPERACAO.md).

## Arquivamento e restauração de demandas — concluídos

**Entrega técnica concluída em 09/10/2026:** doze tarefas, 30/30 critérios e 10/10 bordas aprovados pelo [Verifier independente](features/demandas-arquivamento/validation.md). [Tarefas](features/demandas-arquivamento/tasks.md), [spec](features/demandas-arquivamento/spec.md) e [evidências](features/demandas-arquivamento/evidence.md) registram os commits, testes e adequações autorizadas.

**Arquivar** retira a demanda das colunas e a coloca em **Arquivadas**, preservando todos os dados e os registros anteriores. **Restaurar** devolve à mesma etapa e estado; uma demanda concluída ou cancelada mantém seu encerramento. O número continua fixo e reservado no sistema. Usuários autenticados consultam o arquivo; somente criador do quadro ou administrador executa as ações, em quadro ativo. Arquivadas precisam ser restauradas antes de receber edição, movimentação, comentário ou histórico manual.

Com esta entrega, **o CRUD de demandas solicitado está concluído tecnicamente**, com criação, consulta, edição e retirada por arquivamento/restauração, conforme a política escolhida. Movimentação, encerramento e reabertura também estão concluídos nas entregas anteriores.

Cada critério desta entrega está registrado abaixo como concluído. As assertions e os resultados definidos pela spec constam no relatório independente.

| Critério | Requisito concluído | Situação |
| --- | --- | --- |
| ARQ-01 | Sessão inválida recusa ações sem efeitos. | Concluído e verificado |
| ARQ-02 | Funcionário externo não arquiva nem restaura. | Concluído e verificado |
| ARQ-03 | Quadro arquivado bloqueia alterações. | Concluído e verificado |
| ARQ-04 | Quadro sem criador permite somente administrador. | Concluído e verificado |
| ARQ-05 | Arquivar preserva dados, etapa, estado e encerramento. | Concluído e verificado |
| ARQ-06 | Restaurar conserva a situação original. | Concluído e verificado |
| ARQ-07 | Estado antigo desconhecido permanece sem alteração. | Concluído e verificado |
| ARQ-08 | Repetição não acrescenta evento nem versão. | Concluído e verificado |
| ARQ-09 | Versão ausente ou negativa é recusada. | Concluído e verificado |
| ARQ-10 | JSON inválido e campos extras são recusados. | Concluído e verificado |
| ARQ-11 | Versão desatualizada exige atualização. | Concluído e verificado |
| ARQ-12 | Demanda inexistente ou de outro quadro é recusada. | Concluído e verificado |
| ARQ-13 | Sucesso devolve o quadro completo com versão incrementada uma vez. | Concluído e verificado |
| ARQ-14 | Um evento por ação registra autor e horário reais. | Concluído e verificado |
| ARQ-15 | Falhas revertem demanda, evento e versão. | Concluído e verificado |
| ARQ-16 | Operações concorrentes aguardam o lock do quadro. | Concluído e verificado |
| ARQ-17 | Número reservado, comentários e eventos anteriores preservados. | Concluído e verificado |
| ARQ-18 | Marcador vazio antigo significa ativo; cadastro e JSON não forjam arquivo. | Concluído e verificado |
| ARQ-19 | Ativas e arquivadas aparecem em listas completas e separadas. | Concluído e verificado |
| ARQ-20 | Contagens das colunas incluem somente ativas. | Concluído e verificado |
| ARQ-21 | Arquivada impede remover a etapa de origem. | Concluído e verificado |
| ARQ-22 | Arquivadas ficam somente para consulta, inclusive nas rotas antigas. | Concluído e verificado |
| ARQ-23 | Exclusão definitiva antiga é recusada sem apagar ou criar evento. | Concluído e verificado |
| ARQ-24 | Área Arquivadas apresenta os dados completos. | Concluído e verificado |
| ARQ-25 | Interface oferece ações conforme estado e permissão. | Concluído e verificado |
| ARQ-26 | Confirmação identifica demanda e efeito, com cancelamento e foco. | Concluído e verificado |
| ARQ-27 | Envio bloqueia duplicidade, fechamento, alterações e saída. | Concluído e verificado |
| ARQ-28 | Interface só aplica resposta confirmada por campos, listas e versão. | Concluído e verificado |
| ARQ-29 | Erros preservam confirmação e exigem GET manual antes de novo envio. | Concluído e verificado |
| ARQ-30 | Troca de quadro descarta confirmação e resposta anterior. | Concluído e verificado |

**Validação:** 747 Java/H2, 881 testes da interface, 438 selecionados em MySQL fictício, 13 fluxos Edge/H2, lint/build aprovados. O Verifier repetiu todos os gates e detectou 11/11 falhas simuladas; nenhum teste ignorado ou removido. HEAD, estado Git e hashes dos 4.259 arquivos ficaram idênticos antes e depois do sensor isolado. Gates executados em 08/10 e resultados reconferidos em 09/10, com o mesmo código. Desktop e celular conferidos. A fixture MySQL 33823 já estava inativa na retomada; nenhum PID antigo ou banco real foi alterado. **Aceitação humana pendente**, com roteiro em [OPERACAO.md](../docs/OPERACAO.md).

## Comentários por demanda — concluídos

**Concluído e verificado — tudo ok. Incluído no diagnóstico em10/10/2026.** Nove tarefas concluídas,28/28 critérios e bordas aprovados na segunda [revisão independente](features/comentarios-lateral/validation.md). [Spec](features/comentarios-lateral/spec.md), [tarefas](features/comentarios-lateral/tasks.md) e [evidências](features/comentarios-lateral/evidence.md) registram as decisões, os commits e as provas.

O botão **Comentários** abre a conversa exclusiva da demanda. Qualquer usuário autenticado pode enviar texto simples, com espaços e quebras preservados, nome obtido da conta e horário do servidor. Demandas concluídas/canceladas também permitem conversar quando não arquivadas. Quadro ou demanda arquivados permitem somente consulta, conservando as mensagens. Comentar preserva os dados, etapa, encerramento, contagens, versão e histórico da demanda.

Enquanto o envio está pendente, o sistema bloqueia duplicidade e outras ações. Uma resposta confirmada acrescenta a mensagem uma vez e limpa o campo. Falha conserva lista e rascunho e exige **Atualizar comentários** antes de tentar novamente; a consulta não reenvia. Fechar/Escape descarta o rascunho e devolve o foco ao cartão. Texto HTML aparece escrito, sem execução. As telas foram conferidas em1280x800 e390x844.

| Critério | Requisito | Situação |
| --- | --- | --- |
| COM-01 | Sessão obrigatória nas consultas e envios, sem efeitos quando inválida. | Concluído e verificado — tudo ok |
| COM-02 | Qualquer usuário autenticado pode comentar em quadro/demanda ativos. | Concluído e verificado — tudo ok |
| COM-03 | Autoria real da sessão, ignorando nome falso da rota antiga. | Concluído e verificado — tudo ok |
| COM-04 | Texto obrigatório, literal, com limite de10.000 unidades UTF-16 no servidor. | Concluído e verificado — tudo ok |
| COM-05 | Corpo novo estrito, sem campos extras ou tipos inválidos. | Concluído e verificado — tudo ok |
| COM-06 | Existência e vínculo quadro/demanda conferidos sem vazamento. | Concluído e verificado — tudo ok |
| COM-07 | Quadro arquivado bloqueia mensagens novas. | Concluído e verificado — tudo ok |
| COM-08 | Demanda arquivada bloqueia mensagens novas. | Concluído e verificado — tudo ok |
| COM-09 | Comentários em encerradas/status antigo conservam o estado. | Concluído e verificado — tudo ok |
| COM-10 | Uma mensagem com ID, texto, autor, horário e vínculo corretos e persistidos. | Concluído e verificado — tudo ok |
| COM-11 | Mensagens anteriores preservadas em consulta/envio/arquivo/restauração. | Concluído e verificado — tudo ok |
| COM-12 | Dados, ciclo, etapa, contagens, versão e histórico da demanda preservados. | Concluído e verificado — tudo ok |
| COM-13 | Falha de gravação reverte a transação inteira. | Concluído e verificado — tudo ok |
| COM-14 | Concorrência com arquivo/restauração decide após lock nas duas rotas. | Concluído e verificado — tudo ok |
| COM-15 | Conversa exclusiva, ordenada por horário/ID, inclusive vazia ou arquivada. | Concluído e verificado — tudo ok |
| COM-16 | Contrato exato sem entidades aninhadas, senha ou token. | Concluído e verificado — tudo ok |
| COM-17 | Botão de consulta em todos os cartões e estados. | Concluído e verificado — tudo ok |
| COM-18 | Painel identifica a demanda e apresenta carregamento, vazio, lista e erro. | Concluído e verificado — tudo ok |
| COM-19 | Texto escapado/literal com quebras, autor e data/hora legíveis. | Concluído e verificado — tudo ok |
| COM-20 | Arquivados ficam somente consulta conforme flags confirmadas. | Concluído e verificado — tudo ok |
| COM-21 | Texto inválido não envia e conserva o rascunho na interface. | Concluído e verificado — tudo ok |
| COM-22 | Envio pendente bloqueia duplicidade, alterações, fechamento e saída. | Concluído e verificado — tudo ok |
| COM-23 | Apenas resposta201 confirmada inclui mensagem uma vez e limpa o campo. | Concluído e verificado — tudo ok |
| COM-24 | Falha ou resposta incerta conserva texto/lista e exige consulta manual. | Concluído e verificado — tudo ok |
| COM-25 | Atualização manual conserva rascunho/aviso, atualiza flags e não reenvia. | Concluído e verificado — tudo ok |
| COM-26 | Fechar/Escape descarta rascunho e devolve o foco. | Concluído e verificado — tudo ok |
| COM-27 | Troca/desmontagem descarta conversa e ignora respostas atrasadas. | Concluído e verificado — tudo ok |
| COM-28 | Fluxo por teclado e painel sem transbordamento no computador/celular. | Concluído e verificado — tudo ok |

**Validação final independente:**840 Java/H2,1000 Vitest,336 selecionados no MySQL8.0.43 fictício,15 Edge/H2 em oito arquivos,lint/build PASS, zero falhas/erros/skips. Sensor7/7 falhas detectadas; controles íntegros102 Java/76 API PASS. HEAD/estado Git e4.147 hashes reais preservados. A primeira revisão revelou uma lacuna de concorrência no envio moderno; T9 acrescentou oito provas, preservou as antigas e detectou o mesmo mutante na segunda rodada. Tentativas ambientais negativas e lições candidatas ficam documentadas. Gate validate_state: zero erros. MySQL temporário encerrado com identidade atual conferida,shutdown0 e listener ausente; nenhum banco real utilizado. **Aceitação humana pendente**, com roteiro em [OPERACAO.md](../docs/OPERACAO.md).

## Atividade por gerenciamento — concluída

**Concluído e verificado — tudo ok, incluído no diagnóstico em13/10/2026.** Vinte tarefas locais e39/39 critérios aprovados pelo [Verifier independente](features/atividade-gerenciamento/validation.md). [Tarefas](features/atividade-gerenciamento/tasks.md) e [evidências](features/atividade-gerenciamento/evidence.md) registram cada commit e suas provas.

O botão **Atividade** no quadro mostra ações do gerenciamento, das etapas e das demandas,com autor da conta,horário e valores anteriores/novos. Todos os autenticados podem consultar,inclusive arquivados. Registros antigos aparecem como **Histórico existente**,sem inventar autoria ou dados. A lista carrega50 por página; **Carregar mais** conserva o conjunto e **Atualizar** consulta os registros novos. Comentários aparecem como envio,sem repetir a mensagem. Fechar/Escape devolve foco e libera as ações do quadro; formulários e comentários preservam seus bloqueios e rascunhos. Arquivado aparece como Sim/Não; dados originais continuam preservados.

**Validação independente:**1087 Java/H2,1183 Vitest,18 Edge/H2,847 MySQL fictício,lint/build e validate_state aprovados. Baseline reexecutada840 Java/1000 Vitest confirma os247/183 casos adicionados. Sensor8/8 falhas detectadas,controles247/183 PASS;HEAD/porcelain e4392 hashes reais preservados após limpeza da cópia. A primeira execução de navegador perdeu o serviço4173; traces foram preservados e o gate completo passou na repetição,sem mudar testes/timeouts/retries. MySQL fictício encerrado; nenhum banco real utilizado. **Aceitação humana pendente**,com roteiro em [OPERACAO.md](../docs/OPERACAO.md).

| Critério | Resultado no sistema | Estado |
| --- | --- | --- |
| ACT-01 | Registrar criação do quadro com autor e horário. | Concluído e verificado — tudo ok |
| ACT-02 | Edição do quadro mostra campos anteriores e novos. | Concluído e verificado — tudo ok |
| ACT-03 | Arquivar/restaurar quadro registra a mudança. | Concluído e verificado — tudo ok |
| ACT-04 | Criação de etapa identifica nome,setor e posição. | Concluído e verificado — tudo ok |
| ACT-05 | Edição de etapa registra diferenças reais. | Concluído e verificado — tudo ok |
| ACT-06 | Remoção conserva identificação e dados anteriores da etapa. | Concluído e verificado — tudo ok |
| ACT-07 | Reorganização registra todas as posições afetadas. | Concluído e verificado — tudo ok |
| ACT-08 | Cadastro de demanda registra dados iniciais e autor. | Concluído e verificado — tudo ok |
| ACT-09 | Edição informativa mostra diferenças sem mudar número/encerramento. | Concluído e verificado — tudo ok |
| ACT-10 | Movimentação identifica etapa anterior e destino,inclusive nomes iguais. | Concluído e verificado — tudo ok |
| ACT-11 | Conclusão/cancelamento registra estado,datas,etapa e motivo alterados. | Concluído e verificado — tudo ok |
| ACT-12 | Reabertura registra retorno ao trabalho e limpeza do encerramento. | Concluído e verificado — tudo ok |
| ACT-13 | Arquivar/restaurar demanda conserva seu ciclo. | Concluído e verificado — tudo ok |
| ACT-14 | Envio de comentário registra autor/horário,sem copiar mensagem. | Concluído e verificado — tudo ok |
| ACT-15 | Nomes e identificações anteriores permanecem nos novos registros. | Concluído e verificado — tudo ok |
| ACT-16 | Edição sem diferenças informa que nenhum campo mudou. | Concluído e verificado — tudo ok |
| ACT-17 | Falha reverte ação,histórico e atividade juntos. | Concluído e verificado — tudo ok |
| ACT-18 | Ações recusadas conservam todos os dados. | Concluído e verificado — tudo ok |
| ACT-19 | Versões e histórico atuais são preservados;comentário não altera versão. | Concluído e verificado — tudo ok |
| ACT-20 | Todos os autenticados consultam,inclusive arquivados e encerrados. | Concluído e verificado — tudo ok |
| ACT-21 | Sessão ausente/inválida recebe401 sem gravação. | Concluído e verificado — tudo ok |
| ACT-22 | Histórico existente conserva valores e origem explícita. | Concluído e verificado — tudo ok |
| ACT-23 | Ação nova não aparece duplicada,histórico da demanda continua disponível. | Concluído e verificado — tudo ok |
| ACT-24 | Nova escrita manual de histórico é recusada409;consulta antiga preservada. | Concluído e verificado — tudo ok |
| ACT-25 | Mais recentes primeiro,desempates estáveis e datas ausentes no fim. | Concluído e verificado — tudo ok |
| ACT-26 | Página limitada a50,com continuação ou fim identificado. | Concluído e verificado — tudo ok |
| ACT-27 | Paginação conserva conjunto sem repetição;novos aparecem ao atualizar. | Concluído e verificado — tudo ok |
| ACT-28 | Cursor inválido ou de outro quadro é recusado400. | Concluído e verificado — tudo ok |
| ACT-29 | Respostas pertencem ao quadro e não expõem senha,sessão ou mensagem. | Concluído e verificado — tudo ok |
| ACT-30 | Painel de leitura mostra ação,alvo,autor,horário e diferenças. | Concluído e verificado — tudo ok |
| ACT-31 | Carregamento,vazio e erro possuem estados claros e nova tentativa. | Concluído e verificado — tudo ok |
| ACT-32 | Carregar mais bloqueia duplicidade e conserva lista na falha. | Concluído e verificado — tudo ok |
| ACT-33 | Atualizar inicia nova consulta;falha conserva lista e avisa. | Concluído e verificado — tudo ok |
| ACT-34 | Fechamento/troca ignora respostas atrasadas e devolve foco adequado. | Concluído e verificado — tudo ok |
| ACT-35 | Atividade,formulários e comentários ficam exclusivos,sem perder rascunhos. | Concluído e verificado — tudo ok |
| ACT-36 | Texto escapado,ausências claras,Sim/Não e controles em desktop/360px. | Concluído e verificado — tudo ok |
| ACT-37 | Resposta inconsistente é rejeitada inteira,sem lista parcial ou falso vazio. | Concluído e verificado — tudo ok |
| ACT-38 | Arquivo repetido do quadro não cria evento adicional. | Concluído e verificado — tudo ok |
| ACT-39 | Quadro inexistente recebe404 sem gravação. | Concluído e verificado — tudo ok |

## Checkpoints históricos da atividade

Os registros abaixo preservam o estado de cada etapa naquela data. A conclusão atual é a seção acima; suas pendências já foram resolvidas.

Incluído no diagnóstico em13/10/2026: T17 Implemented,botão/coordenação/foco por quadro;1176 Vitest em28 arquivos e1087 Java/H2 PASS em12/10,16 Edge/H2 em9 arquivos (15 anteriores+1 novo),lint/buildTEMP PASS em13/10. Expectativa novaHTTP200→204 ematividade.spec.js autorizada pelo usuário,sem mudar produto ou outcomes antigos. T18/ajustes pequenos já identificados/Verifier pendentes; nenhum ACT Verified.

Incluído no diagnóstico em12/10/2026: T16 Implemented,painel de leitura/antes-depois/fontes antigas/paginação/atualização e cancelamento;1156 Vitest PASS em27 arquivos (25 novos emT16),lint/buildTEMP PASS. T17–T18 e Verifier pendentes; nenhum ACT Verified.

Incluído no diagnóstico em12/10/2026: T15 Implemented,cliente de atividade com validação integral de página/cursor/fontes;1131 Vitest PASS em26 arquivos (1000 anteriores+131 novos),zero falhas/skips. T14 já está no commit e53302f. T16–T18 e Verifier pendentes,nenhum ACT Verified. [Evidência T15](features/atividade-gerenciamento/evidence.md#t15--contrato-do-cliente).

**Checkpoint anterior ao Verifier:** incluído no diagnóstico em13/10/2026,T1–T20 Implemented e gates técnicos aprovados,com primeira página validada e Sim/Não. A verificação independente foi concluída posteriormente neste mesmo dia,conforme relatório acima. Datas e autorizações anteriores preservadas.

## O que ainda falta nos requisitos originais

| Requisito | Situação atual / próximo trabalho |
| --- | --- |
| Aceitação dos fluxos pelos estudantes | Conexão de outro computador no mesmo Wi-Fi confirmada pelo usuário em 14/10/2026. Falta registrar a avaliação dos fluxos de trabalho e uso em celular. |
| Acesso pela internet | Vercel + Render + Aiven escolhidos, base nova e cadastros aprovados por admin. Preparo local autorizado e tarefas em execução; faltam contas Render/Aiven, publicação e acesso real com dados móveis. |
| Preparação posterior para os empregados | Definir ambiente definitivo e eventual importação de registros antigos, se houver. O administrador inicial já foi configurado somente no banco local; nenhuma migração do banco da empresa foi executada. |

## Testes já registrados

Em 10/10/2026 o usuário confirmou alcance por demanda/todos autenticados, proposta/plano e adequações antigas. T1 entregou autoria da sessão e validação de texto, preservando registros. A primeira seleção teve dois resultados negativos por versão incorreta na fixture; correção própria autorizada antes de alterar o teste. Repetição44 PASS e Full782 Java/H2 PASS,0 falhas/erros/skips. Tarefa concluída, feature ainda parcial; detalhes e evidências por assertion no relatório da execução.

Os números abaixo são registros das execuções aprovadas, não uma nova execução realizada para escrever este resumo. Eles não devem ser somados, pois as suítes acumulam testes de entregas anteriores.

- Cadastro e consulta de demandas (entrega anterior): **390 testes Java/H2, 475 testes da interface, 4 fluxos Edge/H2 e 145 testes selecionados em MySQL temporário**, além de build/lint e revisão independente. O sensor dessa revisão detectou 8 de 8 falhas simuladas.
- Movimentação concluída: **479 Java/H2, 661 testes da interface, 7 fluxos Edge/H2 e 122 selecionados em MySQL fictício**, lint/build e revisão independente PASS. Sensor final: 14 padrões de falha detectados, após corrigir duas lacunas de mensagens. Logs de tentativas com timeout/conexão/conversão de data permanecem nas evidências; gates finais e limites documentados no relatório.
- Edição concluída: **570 Java/H2, 721 testes da interface, 9 fluxos Edge/H2 e 213 selecionados em MySQL fictício**, lint/build e revisão independente PASS; sensor 9/9. Todos os gates foram repetidos independentemente.
- Arquivamento/restauração concluídos: **747 Java/H2, 881 testes da interface, 13 fluxos Edge/H2 e 438 selecionados em MySQL fictício**, lint/build e revisão independente PASS; sensor 11/11. Todos os gates foram repetidos independentemente.
- As entregas anteriores têm seus próprios resultados e limites nos relatórios vinculados acima. MySQL foi verificado com dados fictícios em ambiente temporário.

## Onde acompanhar o projeto

- [STATE.md](STATE.md): decisões AD-001 a AD-045, regra permanente de acompanhamento e ponto de retomada.
- [features/](features/): especificações, tarefas, evidências e relatórios de cada entrega; demandas-arquivamento está concluída tecnicamente, com aceitação humana pendente.
- [OPERACAO.md](../docs/OPERACAO.md): orientações de execução e verificação com dados fictícios.

Este arquivo registra o diagnóstico do projeto. A skill `tlc-spec-driven` fornece o método de especificação, execução e verificação; suas instruções não foram alteradas para guardar o resumo.

---

## Registro histórico do diagnóstico inicial e das entregas

**As seções abaixo preservam a análise da base de 04/10/2026 e atualizações anteriores. Afirmações como “botão sem ação” ou “decisão pendente” descrevem aquele momento. Para a situação atual, use o resumo acima e os relatórios vinculados.**

# Diagnóstico inicial do sistema Creral

Data: 2026-10-04. Branch analisada: `testes`. Base: `5d8beb9`.

A leitura do código confirma uma implementação parcial do gerenciador de etapas. O botão de criar processo está sem ação no frontend. Nenhuma funcionalidade de aplicação foi alterada nesta análise. As regras de negócio abaixo ainda precisam ser esclarecidas com o usuário.

Atualização em 2026-10-05: o CRUD de gerenciamentos está implementado e validado tecnicamente, com 41/41 critérios. A API e interface permitem criar/consultar/editar/arquivar/restaurar, aplicam permissões e protegem os registros associados. Seus gates foram 97 testes H2, 71 testes da interface e 1 fluxo real no Edge; houve também 96 testes de comportamento em MySQL temporário e verificação de schema antigo/reinicialização. Relatório: `features/gerenciamentos/validation.md`.

O CRUD de etapas de trabalho também está implementado e validado, 30/30 critérios na rodada 2. Criador/admin cria, renomeia, muda setor, escolhe posição e remove etapas vazias. A ordem das demais colunas se ajusta automaticamente; demandas bloqueiam remoção. Arquivados ficam somente consulta. Gates dessa entrega: 238 Java/H2, 149 testes da interface, build/lint e 2 fluxos Edge PASS; MySQL temporário, 237 testes PASS. A revisão independente detectou as cinco falhas simuladas, incluindo o falso sucesso com resposta incompleta que motivou T7. Uma falha anterior de conexão E2E permanece documentada, com causa indeterminada; a repetição diagnóstica e a execução independente passaram sem retries. Relatório: `features/etapas/validation.md`.

A entrada automática após cadastro está implementada e validada independentemente, 14/14 critérios. O servidor devolve sessão da conta salva; cadastro e login usam a mesma gravação segura no navegador. Cadastro confirmado abre Gerenciamentos sem segundo login. Pending bloqueia envios duplicados e erros preservam campos; respostas não confirmadas orientam login caso a conta já exista. Papel inicial permanece FUNCIONARIO, sem senha/hash na resposta/cache. Gates dessa entrega: 247 Java/H2, 222 testes da interface, build/lint e 3 E2E Edge PASS. Nove casos também passaram em MySQL TEMP. O Verificador detectou 7/7 falhas simuladas e confirmou porcelain integral byteigual. A cópia temporária restaurada foi preservada porque sua limpeza foi rejeitada automaticamente. Relatório: `features/autocadastro/validation.md`.

A retirada de PM do login e cadastro também foi implementada e verificada independentemente, 4/4 critérios. Os dois blocos e seus estilos exclusivos foram removidos; os títulos iniciam os cartões, sem bloco vazio. As duas telas passaram em desktop e celular, sem rolagem horizontal. Gates próprios: 222 testes da interface, build/lint e 3 E2E Edge/H2 PASS. O sensor independente detectou 3/3 falhas visuais simuladas em cópias executadas em memória e confirmou que o projeto permaneceu intacto. Java/MySQL não foram repetidos nesta alteração de apresentação. Relatório: `features/marca-login/validation.md`.

Atualização em 2026-10-06: as etapas finais obrigatórias estão implementadas e verificadas independentemente, 30/30 critérios. Novos quadros recebem Concluídos e Cancelados, identificados pela categoria, preservados após recarga/CRUD/arquivamento e protegidos contra alteração/remoção. Posições contam somente trabalhos; quadros sem trabalho conservam as finais. A ferramenta de preparação oferece plano sem gravação e aplicação explícita, transacional e idempotente para legados, inclusive arquivados/sem criador, preservando campos/datas/comentários/históricos. Gates atuais: **278 Java/H2, 235 testes da interface, build TEMP/lint e 3 E2E Edge/H2 PASS**, sem falhas/skips/retries. MySQL TEMP8.0.43: **30 selecionados PASS**, schema antigo de17 registros preservado; startup/plano/aplicação/repetição conferidos fisicamente. QA12 capturas desktop/mobile PASS. Sensor independente em memória: controle29/29 PASS, sete falhas compiláveis detectadas7/7 e comparação integral do projeto após encerrar JVMs. A primeira cópia em disco foi preservada porque sua remoção foi recusada automaticamente; o novo sensor em memória completou a prova sem removê-la. Relatório: `features/etapas-finais/validation.md`.

O teste de uso humano permanece sem resultado; autorização para continuar não equivale a teste aprovado. Para experimentar com dados fictícios, ver `../docs/OPERACAO.md`. AD-001 a AD-020 estão em `STATE.md`. Somente criador do gerenciamento e administradores poderão mover demandas, pular etapas, concluir, cancelar e reabrir. Movimentos e reabertura poderão escolher qualquer etapa de trabalho do mesmo quadro ativo, inclusive anterior; novos cancelamentos exigirão justificativa. Essas ações são a próxima entrega; a escolha sobre manter ou limpar os campos atuais de encerramento ao reabrir foi perguntada e continua pendente. Nenhuma migração foi executada no banco real.

Na entrega de criação/leitura de demandas, todos os usuários autenticados poderão criar em quadro ativo (AD-017), começando automaticamente na primeira etapa de trabalho (AD-018). Se não houver trabalho, criador/admin deverá cadastrar uma etapa antes. O usuário confirmou 1A e 2A: número digitado e único globalmente (AD-019); número e cliente/solicitante obrigatórios, demais campos opcionais (AD-020). Edição e exclusão terão escolhas próprias antes da implementação.

Cadastro e consulta de demandas estão implementados e verificados independentemente, **41/41 critérios PASS**. O botão Criar processo abre o formulário; número manual e cliente são obrigatórios, demais campos opcionais. A demanda começa automaticamente na primeira etapa de trabalho. Cartões reais, contagens e dados permanecem após recarga, configuração das etapas e consulta do quadro arquivado. Número duplicado é recusado em todo o sistema, inclusive com caixa diferente. Criação e histórico são transacionais, com autoria da sessão.

A revisão encontrou e corrigiu uma confirmação falsa: resposta201 sem a demanda solicitada fechava o formulário. T7/926c862 agora confirma os campos/estado/etapa e versão antes de fechar; respostas divergentes preservam o rascunho/cache e exigem atualização manual. Gates: **390Java/H2,475testes da interface,buildTEMP/lint,4Edge/H2 e145selecionados em MySQL8.0.43 fictício PASS**. O backend inalterado reutiliza as provas Java/MySQL independentes; frontend/build/lint/navegador foram executados novamente após a correção. Sensor em memória detectou8/8 falhas, com controles válidos e comparação integral após encerrar processos. Seis capturas desktop/mobile passaram em QA. Relatório: `features/demandas-cadastro/validation.md`. Edição/exclusão e transições continuam em entregas próprias.

As tabelas e linhas de código abaixo registram o diagnóstico da base `5d8beb9`; não descrevem como pendentes os reparos já comprovados nos relatórios atuais.

## Escopo solicitado

O usuário pediu implementação incremental, um requisito por vez, com testes, usando a skill local `tlc-spec-driven`. Pediu explicações simples e perguntas sobre as decisões que faltarem. O sistema será usado pela empresa de energia Creral, conforme nome confirmado pelo usuário. Java, Spring Boot e MySQL são tecnologias indicadas pelo usuário; Workbench será a ferramenta de acesso ao banco.

| Pedido | Situação observada no código | Trabalho necessário |
| --- | --- | --- |
| CRUD de gerenciamentos | Backend cria, lista, busca e edita. Frontend cria, lista e abre. | Implementar arquivamento/restauração e permissões escolhidas; completar API e interface conforme a especificação em revisão. |
| CRUD de etapas | Backend tem CRUD. Frontend só configura etapas ao criar gerenciamento. | Permitir manutenção no quadro; definir regras para etapas ocupadas e ordem. |
| Concluir/cancelar a qualquer momento, escolhendo destino | Backend altera status/data, mas não a etapa. Frontend não oferece essas ações. | Definir destinos permitidos e regras após finalização; manter status e etapa coerentes. |
| CRUD de projetos/processos e botão criar | Backend tem CRUD de processos. Frontend não cria nem carrega processos. | Definir projeto versus processo e campos; conectar formulário, listagem, edição e exclusão. |
| Pular etapas | Backend permite qualquer etapa do mesmo gerenciamento. Frontend não permite movimentação. | Definir retorno, finalização e restrições; conectar movimentação à interface. |
| Etapas obrigatórias Concluídos e Cancelados | Não são criadas automaticamente; etapa não tem categoria. | Definir relação com escolha do destino; preservar as etapas obrigatórias. |
| Lateral de comentários de vários usuários com data | Backend tem comentários por processo com data/hora. Frontend não apresenta comentários. | Definir se conversa é do gerenciamento, do item ou de ambos; identificar autor pela sessão. |
| Gráficos, se possível | Nenhuma implementação encontrada. | Definir indicadores úteis e seus filtros; implementar após dados e regras. |
| Entrar no sistema após cadastro | Cadastro salva usuário; frontend permanece na tela de cadastro. | Criar sessão e navegar após cadastro bem-sucedido; definir quem pode se cadastrar. |
| Interface geral e retirar PM de login/cadastro | PM está presente nas duas telas. | Remover marca solicitada e definir identidade visual da Creral. |
| Logs em cada gerenciamento | Histórico atual é por processo; não há histórico de gerenciamento/etapa. | Definir eventos e visibilidade; registrar autor e data de forma confiável. |

CRUD significa criar, consultar/listar, editar e excluir. Arquivar, quando escolhido, significa retirar da lista de ativos preservando dados e histórico.

## Evidências principais

Os caminhos são relativos à raiz do projeto.

- `frontend/src/pages/Quadro.jsx:83`: botão Criar processo sem `onClick` ou formulário.
- `frontend/src/pages/Quadro.jsx:18`: quadro busca somente etapas.
- `frontend/src/pages/Quadro.jsx:127`: quantidade fixa em zero; não representa dados do banco.
- `frontend/src/services/api.js:1`: API fixa em localhost. Serviços atuais cobrem autenticação e listar/criar gerenciamentos.
- `frontend/src/pages/Gerenciamentos.jsx:89`: criar gerenciamento; sem controles de edição/exclusão.
- `frontend/src/pages/CriarGerenciamento.jsx:19`: etapas configuradas durante criação.
- `sistema/src/main/java/com/patp/sistema/controller/GerenciamentoController.java:29`: API sem exclusão de gerenciamento.
- `sistema/src/main/java/com/patp/sistema/controller/EtapaController.java:27`: CRUD de etapas já existente.
- `sistema/src/main/java/com/patp/sistema/controller/ProcessoController.java:29`: API de processos já existente.
- `sistema/src/main/java/com/patp/sistema/service/ProcessoService.java:177`: mudança para qualquer etapa do mesmo gerenciamento.
- `sistema/src/main/java/com/patp/sistema/service/ProcessoService.java:228`: concluir e cancelar não alteram etapa.
- `sistema/src/main/java/com/patp/sistema/service/GerenciamentoService.java:26`: criação não adiciona etapas finais obrigatórias nem atribui criador.
- `sistema/src/main/java/com/patp/sistema/model/Etapa.java:20`: etapa contém nome, setor, ordem e gerenciamento; sem categoria terminal.
- `sistema/src/main/java/com/patp/sistema/model/Processo.java:22`: número único global e pessoa obrigatórios no modelo atual; isso não confirma a regra desejada pela Creral.
- `sistema/src/main/java/com/patp/sistema/controller/ComentarioController.java:25`: comentários por processo; nome do autor recebido do cliente.
- `sistema/src/main/java/com/patp/sistema/service/HistoricoService.java:27`: histórico vinculado ao processo.
- `sistema/src/main/java/com/patp/sistema/controller/UsuarioController.java:31`: cadastro não cria sessão.
- `frontend/src/pages/Cadastro.jsx:49`: cadastro não autentica nem redireciona.
- `frontend/src/pages/Login.jsx:70` e `frontend/src/pages/Cadastro.jsx:91`: marca PM.
- `sistema/src/main/java/com/patp/sistema/service/SessaoService.java:13`: sessões guardadas somente na memória do backend.
- `sistema/src/test/java/com/patp/sistema/SistemaApplicationTests.java:6`: único teste encontrado, carrega o contexto sem verificar uma regra de negócio.

## Lacunas técnicas ligadas aos pedidos

- Não há entidade Projeto nem distinção de tipo no processo. Não presumir que os termos são equivalentes.
- Não há papéis ou participantes por gerenciamento. O código permite acesso geral aos usuários autenticados; isso não confirma a autorização desejada.
- Concluir depois de cancelar, ou cancelar depois de concluir, não limpa os dados do estado anterior. Editar também permite alterar status livremente.
- Movimentação não bloqueia itens finalizados nem distingue movimentar de reabrir.
- Exclusão de processo registra histórico vinculado antes de excluir. Referências de comentários/históricos podem impedir exclusão. Essa falha não foi reproduzida no banco.
- Exclusão de etapa não define tratamento amigável para processos existentes.
- Gravação do item e de seu histórico não é transacional. Criação do gerenciamento e suas etapas também não é transacional.
- Cadastro e busca de usuário por ID retornam a entidade com getter público da senha armazenada. As respostas precisam preservar somente dados apropriados ao cliente.
- Histórico manual aceita autor/ação enviados pelo cliente. Auditoria confiável precisa derivar autoria da sessão e definir quais eventos são automáticos.
- Não há testes frontend, perfil de banco isolado de teste ou migrações encontrados.

## Verificações executadas

| Verificação | Resultado | Limite da evidência |
| --- | --- | --- |
| `npm.cmd run build`, em frontend | PASS | Compila a interface; não verifica comportamento dos botões nem comunicação com banco. |
| `npm.cmd exec -- oxlint src vite.config.js`, em frontend | PASS, sem diagnósticos | Analisa os arquivos da aplicação; não comprova regras de negócio. |
| `mvn.cmd -B test-compile`, em sistema | PASS | Compila fontes e testes; Maven informou arquivos atualizados. Não executa testes. |

O comando padrão `npm.cmd run lint` também foi executado, mas percorreu dependências em `node_modules` e gerou muitos avisos externos. A análise restrita aos arquivos do projeto está registrada acima.

Na coleta inicial, o teste `contextLoads` não havia sido executado. A configuração padrão usa MySQL com `ddl-auto=update`; a implementação posterior estabeleceu H2 isolado de teste e uma instância MySQL temporária. O banco configurado na aplicação não foi consultado nem alterado.

O build gerou `frontend/dist/`. A revisão automática recusou a remoção dessa pasta por política. O artefato permaneceu no workspace.

## Decisões a esclarecer, por dependência

Esta lista organiza futuras conversas; não é uma especificação aprovada nem uma lista de regras assumidas.

1. **Vocabulário e exemplo de uso:** o que é gerenciamento; se projeto e processo são iguais ou diferentes; exemplo de um item e suas etapas na Creral.
2. **Acesso:** quem utiliza; quem pode criar/editar/excluir gerenciamentos, etapas e itens; quem pode ver cada gerenciamento.
3. **Primeiro CRUD:** dados do gerenciamento; limites de nomes/descrições; significado de excluir; tratamento quando há itens e histórico.
4. **Etapas:** ordem, setor, alterações durante uso, exclusão de etapa ocupada e criação automática das finais.
5. **Destinos finais:** esclarecer duas etapas obrigatórias versus escolha de uma etapa específica; se há exatamente uma de cada categoria ou vários destinos.
6. **Dados do item:** nome/número/cliente/pessoa/responsável/prazos/prioridade; obrigatoriedade; geração e alcance da unicidade do número; etapa inicial.
7. **Movimentação:** pular, voltar, escolher destino, atravessar gerenciamentos, repetir destino e comportamento após conclusão/cancelamento.
8. **Finalização:** motivo obrigatório de cancelamento; possibilidade e permissão de reabertura; edição e comentários após finalização; manutenção de datas e histórico.
9. **Comentários:** lateral do gerenciamento versus item; leitores/autores; texto, ordenação, data/hora, edição/exclusão e preservação até depois da finalização.
10. **Registros de atividade:** eventos, antes/depois, autoria, leitores, filtros e preservação após exclusão/arquivamento.
11. **Cadastro e sessão:** cadastro aberto versus convite/aprovação; acesso imediato; recuperação de sessão inválida; regras necessárias para uso interno.
12. **Interface e gráficos:** identidade Creral, dispositivos usados, comportamento de telas vazias/erros, indicadores e períodos relevantes.
13. **Validação com usuários:** exemplo fictício representativo, ambiente de teste, tratamento de dados existentes e critérios objetivos para aceitar cada requisito.

As questões técnicas descobríveis pelo código serão resolvidas pela análise do projeto. Não pedir senhas ou credenciais no chat. As decisões de negócio serão explicadas com exemplos e registradas quando respondidas.

## Forma de execução

- Esclarecer as regras do requisito atual em blocos pequenos, conforme preferência do usuário.
- Registrar critérios objetivos de aceitação e decisões antes de implementar.
- Escrever testes que verifiquem esses critérios e usar ambiente isolado.
- Entregar uma funcionalidade completa na API e interface, verificar os testes e registrar uma mudança local por tarefa conforme a skill.
- Fazer verificação independente após finalizar a funcionalidade, incluindo evidências exigidas pela skill.
- Avançar ao próximo requisito mantendo a cobertura dos pedidos da tabela inicial.

Gerenciamentos, etapas de trabalho, entrada após cadastro, retirada de PM, etapas finais obrigatórias, CRUD de demandas,comentários por demanda e atividade por gerenciamento foram entregues e verificados. Gráficos por quadro também concluídos e verificados em14/10/2026. Revisão visual geral também concluída em14/10/2026 no alcance aprovado. Próximo passo: aceitação prática pelos usuários e planejamento da preparação para uso real, sem autorização implícita para executar no banco da empresa.

## Registro histórico de execução do arquivamento

As notas abaixo preservam os checkpoints da execução e suas pendências naquele momento. A conclusão técnica atual está na seção Arquivamento e restauração de demandas, acima; a autorização dos ajustes de T12 foi recebida e aplicada, e a revisão independente foi concluída em 09/10/2026.

Progresso parcial demandas-arquivamento T1: persistir marcador de arquivo; gate 5 testes/0 skips. Implemented, sem conclusão de requisito antes do Verifier. Evidências em features/demandas-arquivamento/evidence.md.

Progresso parcial demandas-arquivamento T2: estender snapshot; gate 2 testes/0 skips. Implemented, sem conclusão de requisito antes do Verifier. Evidências em features/demandas-arquivamento/evidence.md.

Progresso parcial demandas-arquivamento T3: separar arquivo nas leituras; gate 5 testes/0 skips. Implemented, sem conclusão de requisito antes do Verifier. Evidências em features/demandas-arquivamento/evidence.md.

Progresso parcial demandas-arquivamento T4: implementar arquivar/restaurar; gate 77 testes/0 skips. Implemented, sem conclusão de requisito antes do Verifier. Evidências em features/demandas-arquivamento/evidence.md.

Progresso parcial demandas-arquivamento T5: proteger comentários; gate 9 testes/0 skips. Implemented, sem conclusão de requisito antes do Verifier. Evidências em features/demandas-arquivamento/evidence.md.

Progresso parcial demandas-arquivamento T6: separar história externa; gate 21 testes/0 skips. Implemented, sem conclusão de requisito antes do Verifier. Evidências em features/demandas-arquivamento/evidence.md.

Progresso parcial demandas-arquivamento T7: proteger endpoint de histórico; gate 19 testes/0 skips. Implemented, sem conclusão de requisito antes do Verifier. Evidências em features/demandas-arquivamento/evidence.md.

Progresso parcial demandas-arquivamento T8: expor ações dedicadas; gate 747 testes/0 skips. Implemented, sem conclusão de requisito antes do Verifier. Evidências em features/demandas-arquivamento/evidence.md.

Progresso parcial demandas-arquivamento T9: confirmar api frontend; gate 106 Vitest. Implemented, conclusão de requisito depende do Verifier. Evidências em features/demandas-arquivamento/evidence.md.

Progresso parcial demandas-arquivamento T10: criar confirmação; gate 12 Vitest. Implemented, conclusão de requisito depende do Verifier. Evidências em features/demandas-arquivamento/evidence.md.

Progresso parcial demandas-arquivamento T11: integrar arquivo ao quadro; gate 881 Vitest; lint e build TEMP PASS. Implemented, conclusão de requisito depende do Verifier. Evidências em features/demandas-arquivamento/evidence.md.

Progresso parcial demandas-arquivamento T12: Java/H2 747, Vitest881, MySQL438 selecionados e9Edge anteriores PASS/0skips; lint PASS/buildT11 vigente. Novo E2E4falhas de expectativas próprias aguardando autorização humana (comparar campos próprios completos+vínculo processoId; HTTP204 de arquivo/restauração do quadro). T12 não concluída, sem commit/Verified; QA arquivo e Verifier pendentes. Contrato/roteiro humano em docs/OPERACAO.md; aceitação humana pendente.

Progresso parcial demandas-arquivamento T12: provar fluxo real; gate 747 Java/H2; 881 Vitest; 438 MySQL; 13 Edge/H2; lint/build PASS, 0 skips. Implemented, conclusão de requisito depende do Verifier. Evidências em features/demandas-arquivamento/evidence.md.
