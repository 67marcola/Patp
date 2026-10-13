# STATE

## Decisions

Restrições declaradas pelo usuário: Java, Spring Boot, MySQL; implementação por requisito, com testes e uso da skill `tlc-spec-driven`. As regras de cada funcionalidade serão esclarecidas antes da implementação.

Nome da empresa confirmado pelo usuário: **Creral**. Usar essa grafia na documentação e nos textos da aplicação.

**Regra permanente de acompanhamento, reforçada pelo usuário em 10/10/2026:** toda implementação de requisito deve ser incluída em `.specs/DIAGNOSTICO.md`, com o estado real (em andamento, parcial ou concluído), e a comunicação ao usuário deve dizer **“incluído no diagnóstico”**. Só registrar **“Concluído e verificado — tudo ok”** depois de implementar, executar os testes pertinentes com aprovação e completar a verificação exigida pela TLC. Registrar data, resultado dos testes e referência à evidência/validação; não transformar preparação ou teste parcial em conclusão. Depois desse fechamento, atualizar o Handoff e seguir para o próximo requisito pendente, sem refazer entregas comprovadas. Fazer perguntas quando faltarem decisões necessárias, explicando a recomendação e o efeito no sistema. Esta regra vale para todas as próximas entregas e deve ser relida nas retomadas.

Esclarecimento recebido em 2026-10-04: "projeto" pode designar o gerenciamento ou o software, conforme a frase; o usuário descreveu instalação de poste e pediu pular etapas por botão. O artigo PATP foi fornecido como referência. O termo "processo" do artigo/código representa uma demanda em uma etapa, diferindo do uso conversacional do usuário. A escolha posterior do modelo está registrada em AD-001. Detalhes em `.specs/REFERENCIA_PATP.md`.

### AD-001
- **Decision**: um gerenciamento representa um quadro com várias demandas; cada demanda pertence a uma etapa atual e se movimenta independentemente das demais.
- **Reason**: o usuário escolheu explicitamente a opção A; ela corresponde às relações descritas no artigo e ao modelo existente.
- **Trade-off**: uma instalação específica será uma demanda do quadro, em vez de exigir um gerenciamento inteiro com etapa atual própria.
- **Scope**: CRUDs de gerenciamento/etapa/demanda, movimentação, finalização, comentários, histórico e métricas.
- **Date**: 2026-10-04
- **Status**: active

### AD-002
- **Decision**: todos os usuários autenticados podem visualizar e criar gerenciamentos; somente o criador de cada gerenciamento ou um administrador pode editá-lo e retirá-lo de uso.
- **Reason**: o usuário escolheu explicitamente a política 1A, mantendo colaboração e atribuindo responsabilidade pela administração do quadro.
- **Trade-off**: usuários que não são criadores nem administradores não poderão alterar nome/descrição nem retirar quadros de outras pessoas de uso.
- **Scope**: criação, visualização, edição e retirada de uso de gerenciamentos. Permissões para etapas e demandas serão esclarecidas nos respectivos requisitos.
- **Date**: 2026-10-04
- **Status**: active

### AD-003
- **Decision**: retirar um gerenciamento de uso significa arquivá-lo, removendo-o da lista de ativos, preservando demandas, comentários e histórico e permitindo restauração.
- **Reason**: o usuário escolheu explicitamente 2A; preservar os registros mantém a rastreabilidade descrita no artigo.
- **Trade-off**: arquivar não libera armazenamento nem apaga os registros; consulta e restauração precisam de fluxo próprio.
- **Scope**: ciclo de vida dos gerenciamentos e preservação de suas etapas, demandas, comentários e históricos associados. Regras de edição durante arquivamento serão esclarecidas separadamente.
- **Date**: 2026-10-04
- **Status**: active

### AD-004
- **Decision**: o papel de administrador será atribuído a contas escolhidas pelo usuário; cadastros comuns terão o papel de funcionário.
- **Reason**: o usuário escolheu a opção A de atribuição, mantendo a definição dos administradores independente da ordem dos cadastros.
- **Trade-off**: as contas administrativas precisam ser identificadas e configuradas explicitamente; não haverá administrador automático por primeiro cadastro.
- **Scope**: cadastro, papéis de usuário e aplicação das permissões administrativas dos gerenciamentos. As contas específicas e o mecanismo técnico de atribuição ainda serão definidos.
- **Date**: 2026-10-04
- **Status**: active

### AD-005
- **Decision**: gerenciamentos arquivados ficam disponíveis apenas para consulta; é necessário restaurá-los antes de editar, movimentar demandas ou adicionar comentários.
- **Reason**: o usuário escolheu a opção A de comportamento do arquivado, preservando o conteúdo do quadro durante esse estado.
- **Trade-off**: até correções e novos comentários exigem restaurar o gerenciamento; a proteção precisa alcançar as ações sobre seus registros associados.
- **Scope**: estado arquivado de gerenciamentos, etapas, demandas, comentários e histórico. Restauração e logs automáticos das ações administrativas precisam permanecer possíveis.
- **Date**: 2026-10-04
- **Status**: active

### AD-006
- **Decision**: gerenciamentos sem criador identificado podem ser consultados por todos os usuários autenticados, mas apenas administradores podem editar, arquivar ou restaurar esses quadros.
- **Reason**: o usuário escolheu a opção A para quadros existentes sem autoria, preservando dados sem inventar um responsável.
- **Trade-off**: funcionários comuns não administram um quadro cujo criador é desconhecido; a atribuição automática de autoria não será realizada.
- **Scope**: registros de gerenciamento com criador nulo ou desconhecido e aplicação das permissões do primeiro CRUD.
- **Date**: 2026-10-04
- **Status**: active

### AD-007
- **Decision**: somente o criador do gerenciamento ou um administrador poderá criar, renomear, ordenar ou remover etapas de um quadro ativo no próximo CRUD de etapas.
- **Reason**: resposta explícita do usuário; mantém a definição do fluxo sob responsabilidade de quem administra o quadro.
- **Trade-off**: demais funcionários acompanham etapas, mas não mudam a estrutura do fluxo.
- **Scope**: próximo CRUD de etapas e configuração do fluxo; a entrega atual preserva as permissões existentes nas rotas de conteúdo ativo.
- **Date**: 2026-10-04
- **Status**: active

### AD-008
- **Decision**: impedir a remoção de uma etapa que contenha demandas; elas precisam ser movidas para outra etapa antes de removê-la.
- **Reason**: resposta explícita do usuário; não apagar nem deslocar demandas automaticamente ao remover uma coluna.
- **Trade-off**: a remoção pode exigir movimentar primeiro as demandas.
- **Scope**: próximo CRUD de etapas e integridade das relações com demandas.
- **Date**: 2026-10-04
- **Status**: active

### AD-009
- **Decision**: ao configurar etapas, escolher a posição da coluna e reorganizar as demais automaticamente.
- **Reason**: o usuário respondeu “Seguir com essas duas propostas” à pergunta sobre posição automática e Setor obrigatório.
- **Trade-off**: a interface trabalha com posições consecutivas; números antigos com lacunas/empates serão reorganizados na próxima alteração de etapas do quadro.
- **Scope**: CRUD de etapas de trabalho.
- **Date**: 2026-10-05
- **Status**: active

### AD-010
- **Decision**: Setor continua obrigatório nas etapas de trabalho; é informativo e não concede permissão de acesso.
- **Reason**: resposta explícita do usuário às duas propostas.
- **Trade-off**: editar uma etapa antiga sem setor exige informar o setor; consultas não inventam esse dado.
- **Scope**: criação/edição de etapas de trabalho; finais terão contrato próprio.
- **Date**: 2026-10-05
- **Status**: active

### AD-011
- **Decision**: usar somente as duas etapas finais Concluídos e Cancelados, com destino automático conforme a ação de concluir ou cancelar.
- **Reason**: o usuário respondeu explicitamente “Usar somente Concluídos e Cancelados, com destino automático conforme a ação”.
- **Trade-off**: não haverá escolha de destinos adicionais; a configuração de etapas de trabalho não atribui categoria por nome.
- **Scope**: entrega posterior das etapas finais obrigatórias e dos fluxos de concluir/cancelar. Fora do CRUD de etapas de trabalho atual.
- **Date**: 2026-10-05
- **Status**: active

### AD-012
- **Decision**: somente o criador do gerenciamento ou um administrador poderá mover demandas, pular etapas, concluir ou cancelar em um quadro ativo.
- **Reason**: o usuário respondeu B à escolha entre todos os usuários autenticados e somente criador/administradores.
- **Trade-off**: os demais usuários continuam acompanhando o quadro, mas não podem executar essas quatro ações sobre demandas.
- **Scope**: movimentação, salto de etapas, conclusão e cancelamento; aplicar a mesma autorização na interface e no servidor, inclusive em rotas alternativas que alterem etapa/status. Criar, editar outros dados, excluir demandas e reabrir ainda precisam de regras próprias.
- **Date**: 2026-10-05
- **Status**: active

### AD-013
- **Decision**: permitir reabrir uma demanda concluída ou cancelada, somente pelo criador do gerenciamento ou por um administrador, escolhendo uma etapa de trabalho do mesmo quadro ativo.
- **Reason**: o usuário respondeu A à proposta de reabertura para corrigir encerramentos por engano.
- **Trade-off**: conclusão e cancelamento não são irreversíveis; o sistema precisa distinguir reabrir de uma movimentação comum e manter o registro dos encerramentos anteriores.
- **Scope**: reabertura após conclusão/cancelamento e autorização na interface/servidor. Continua valendo AD-005: quadro arquivado precisa ser restaurado antes da ação. A escolha não decide se movimentos comuns podem voltar para etapas anteriores.
- **Date**: 2026-10-05
- **Status**: active

### AD-014
- **Decision**: ao mover ou pular etapas de uma demanda em andamento, permitir escolher qualquer etapa de trabalho do mesmo gerenciamento, inclusive uma anterior.
- **Reason**: o usuário respondeu A à escolha entre qualquer etapa de trabalho e somente etapas posteriores, permitindo correções e retrabalho.
- **Trade-off**: o fluxo de trabalho não é estritamente sequencial; a demanda pode retornar a uma etapa já percorrida.
- **Scope**: destinos da movimentação comum e do botão de pular etapas. A autorização continua em AD-012 e quadros arquivados continuam somente consulta (AD-005). Concluir/cancelar usam as ações e destinos de AD-011; demandas encerradas voltam ao trabalho por reabertura (AD-013).
- **Date**: 2026-10-05
- **Status**: active

### AD-015
- **Decision**: exigir uma justificativa preenchida para cancelar uma demanda.
- **Reason**: o usuário respondeu A à escolha entre motivo obrigatório e opcional, para registrar por que o trabalho foi cancelado.
- **Trade-off**: o cancelamento exige informar o motivo antes de ser confirmado; motivo ausente, vazio ou composto somente por espaços não atende à regra.
- **Scope**: novos cancelamentos, com validação na interface e no servidor e autoria/permissão de AD-012. Não inventar justificativas para demandas antigas; tratamento de legados ainda está em discussão.
- **Date**: 2026-10-05
- **Status**: active

### AD-016
- **Decision**: preparar a correção das demandas antigas concluídas/canceladas em todos os gerenciamentos, inclusive arquivados, direcionando-as à etapa final correspondente do mesmo quadro.
- **Reason**: o usuário respondeu A à escolha entre todos os quadros e somente os ativos.
- **Trade-off**: a preparação inclui uma exceção de manutenção dos dados antigos de quadros arquivados; as ações normais desses quadros continuam somente consulta conforme AD-005.
- **Scope**: ferramenta explícita de preparação/correção de legados, preservando IDs, comentários, histórico, campos e datas existentes. Não inventar motivos ou datas ausentes. Esta escolha define o comportamento a implementar e testar com dados fictícios; não autoriza executar migração no banco real.
- **Date**: 2026-10-06
- **Status**: active

### AD-017
- **Decision**: todos os usuários autenticados poderão criar demandas em gerenciamentos ativos.
- **Reason**: o usuário escolheu explicitamente a proposta de criação por todos os usuários autenticados.
- **Trade-off**: a criação é colaborativa; ela não concede permissão para mover ou encerrar demandas depois de criadas.
- **Scope**: próximo CRUD de demandas e botão Criar processo. Movimentação, conclusão, cancelamento e reabertura continuam restritos ao criador do quadro e administradores conforme AD-012/013. Arquivados continuam somente consulta conforme AD-005.
- **Date**: 2026-10-06
- **Status**: active

### AD-018
- **Decision**: novas demandas começarão automaticamente na primeira etapa de trabalho do gerenciamento ativo.
- **Reason**: o usuário escolheu explicitamente começar na primeira etapa de trabalho.
- **Trade-off**: o cadastro não escolhe uma etapa posterior; movimentar e pular etapas continuam ações próprias. Quadros sem trabalho exigem cadastrar uma etapa de trabalho antes de criar demandas.
- **Scope**: próximo CRUD de demandas e botão Criar processo. Concluídos e Cancelados ficam reservados às ações de finalizar, não ao cadastro inicial.
- **Date**: 2026-10-06
- **Status**: active

### AD-019
- **Decision**: o número de cada demanda será digitado pelo usuário e único em todo o sistema, inclusive entre gerenciamentos diferentes.
- **Reason**: o usuário confirmou explicitamente 1A na escolha sobre o identificador.
- **Trade-off**: cadastrar exige informar um identificador; não haverá numeração automática nem reutilização em outro quadro.
- **Scope**: cadastro de demandas, formulário e validação de duplicidade. Preservar números existentes; não renumerar registros antigos.
- **Date**: 2026-10-06
- **Status**: active

### AD-020
- **Decision**: número e cliente/solicitante são obrigatórios; responsável, prioridade, data de emissão, prazo da etapa, prazo geral e observações são opcionais no cadastro.
- **Reason**: o usuário confirmou explicitamente 2A na escolha de obrigatoriedade.
- **Trade-off**: uma demanda pode iniciar sem responsável ou prazo definido; campos ausentes não serão preenchidos com dados inventados.
- **Scope**: novo cadastro de demandas. Etapa inicial permanece automática conforme AD-018; encerramentos e suas datas pertencem às ações futuras.
- **Date**: 2026-10-06
- **Status**: active

### AD-021
- **Decision**: reabrir uma demanda retorna seu status para Em andamento e limpa dataConclusao, dataCancelamento e motivoCancelamento atuais, preservando os encerramentos anteriores no histórico.
- **Reason**: o usuário respondeu A à escolha entre limpar ou manter os campos atuais de encerramento na reabertura.
- **Trade-off**: os campos atuais representam somente o ciclo vigente; consultar ciclos anteriores exige consultar o histórico.
- **Scope**: reabertura de demandas concluídas/canceladas conforme AD-013, com destino de trabalho escolhido no mesmo gerenciamento e autoria real. Preservar dados antigos existentes sem inventar datas ou motivos.
- **Date**: 2026-10-06
- **Status**: active

### AD-022
- **Decision**: demandas antigas com status vazio, nulo ou diferente de Em andamento, Concluido e Cancelado permanecem consultáveis, mas não podem mover, concluir, cancelar ou reabrir até a correção do registro.
- **Reason**: o usuário respondeu A à escolha entre bloquear ações ou tratar automaticamente status desconhecido como Em andamento.
- **Trade-off**: registros inconsistentes exigem correção explícita antes de voltar ao fluxo; nenhuma normalização automática presume o estado do trabalho.
- **Scope**: transições de demandas, rotas alternativas e interface; preservar dados legados durante consultas. O mecanismo de correção fica fora deste corte.
- **Date**: 2026-10-06
- **Status**: active

### AD-023
- **Decision**: somente o criador do gerenciamento ou um administrador poderá editar os dados informativos das demandas na próxima entrega de edição.
- **Reason**: usuário escolheu explicitamente Criador do quadro ou administrador nesta conversa, após explicação da recomendação.
- **Trade-off**: funcionários que apenas acompanham o quadro não corrigem esses campos; precisam solicitar ao criador/admin.
- **Scope**: próxima entrega de edição de cliente, responsável, prioridade, prazos e observações em quadro ativo. Não alterar permissões de criação, transições ou campos de encerramento; arquivados continuam somente consulta. A regra foi decidida, mas ainda não está implementada neste corte de movimentação. Número e política de retirada/exclusão continuam com escolhas próprias pendentes.
- **Date**: 2026-10-07
- **Status**: active
### AD-024
- **Decision**: manter as tecnologias atuais: Java, Spring Boot, Spring Data JPA/Hibernate, MySQL e Workbench; React, JavaScript, HTML/CSS e Vite; Maven e Node.js/npm; JUnit/H2, Vitest/Testing Library e Playwright nos testes.
- **Reason**: usuário confirmou explicitamente a recomendação de manter o conjunto integrado e testado no projeto em 07/10/2026.
- **Trade-off**: a próxima entrega reutiliza a infraestrutura existente. Workbench administra o MySQL; H2 fica restrito aos testes e não substitui o banco do sistema.
- **Scope**: próximas entregas do sistema Creral. A confirmação não autoriza atualizar versões, migrar dados reais ou implantar o sistema.
- **Date**: 2026-10-07
- **Status**: active

### AD-025
- **Decision**: manter o número da demanda fixo após o cadastro, inclusive na edição legada.
- **Reason**: usuário autorizou explicitamente a proposta em 07/10/2026.
- **Trade-off**: correção do número requer uma futura operação explícita; o formulário atual não renumera registros.
- **Scope**: edição de demandas; criação e unicidade global continuam vigentes.
- **Date**: 2026-10-07
- **Status**: active

### AD-026
- **Decision**: permitir correção de informações em demandas concluídas/canceladas de quadro ativo, pelo criador/admin, preservando o encerramento.
- **Reason**: usuário autorizou explicitamente a proposta em 07/10/2026.
- **Trade-off**: informações podem ser corrigidas sem reabrir o trabalho; a edição precisa conservar status, etapa, datas e motivo do encerramento.
- **Scope**: edição informativa conforme AD-023; não autoriza mudanças de ciclo, arquivos ou migração real.
- **Date**: 2026-10-07
- **Status**: active

### AD-027
- **Decision**: retirar demandas significa arquivar/restaurar, preservando identificação, informações, comentários e histórico; permitir arquivo em andamento, concluídas e canceladas.
- **Reason**: usuário escolheu explicitamente Arquivar e restaurar e Todos os três estados em 08/10/2026 após explicação da recomendação.
- **Trade-off**: não libera armazenamento nem número; consulta e restauração exigem fluxo próprio. Exclusão física não é a política de retirada.
- **Scope**: ciclo de vida de demandas e preservação para comentários/histórico/métricas futuras; implementar na feature demandas-arquivamento, ainda pendente.
- **Date**: 2026-10-08
- **Status**: active

### AD-028
- **Decision**: somente criador do quadro ou administrador poderá arquivar/restaurar demandas em gerenciamento ativo; sem criador identificado somente admin.
- **Reason**: usuário escolheu explicitamente Criador do quadro ou administrador, mantendo responsabilidade igual à edição/transições.
- **Trade-off**: demais funcionários solicitam retirada/restauração ao responsável.
- **Scope**: novas ações de arquivo e rotas alternativas de retirada; quadro arquivado precisa ser restaurado primeiro.
- **Date**: 2026-10-08
- **Status**: active

### AD-029
- **Decision**: comentários da próxima entrega pertencem a cada demanda; qualquer usuário autenticado consulta e envia, usando autoria da conta conectada e horário do servidor. Quadros/demandas arquivados continuam somente para consulta.
- **Reason**: usuário respondeu explicitamente Por demanda (recomendado) e Qualquer usuário autenticado (recomendado) em 10/10/2026.
- **Trade-off**: não há conversa geral por quadro; enviar mensagem não depende de ser criador/admin. O navegador deixa de poder escolher o autor de mensagens novas.
- **Scope**: lateral de comentários e rotas novas/antigas de comentários. Não muda permissões de editar/mover/arquivar demandas, nem reatribui autores antigos.
- **Date**: 2026-10-10
- **Status**: active

### AD-030
- **Decision**: a próxima atividade por gerenciamento registra automaticamente alterações do quadro, das etapas e das demandas, incluindo movimentações, encerramentos e arquivamentos, com alvo, autor da conta e horário.
- **Reason**: usuário respondeu Quadro, etapas e demandas (recomendado) à pergunta concreta nesta conversa em10/10/2026.
- **Trade-off**: exige instrumentar também ações de quadro/etapas, além do histórico de demandas já existente; não inventar acontecimentos anteriores.
- **Scope**: próxima feature de atividade por gerenciamento, ainda em definição. Comentários por demanda já concluídos permanecem na entrega anterior.
- **Date**: 2026-10-10
- **Status**: active

### AD-031
- **Decision**: qualquer usuário autenticado consulta a atividade, inclusive dos arquivados; registros automáticos preservados, sem edição ou exclusão pela interface. Dados antigos mostrados como estão, sem inventar autores ou acontecimentos.
- **Reason**: usuário confirmou Sim, consulta para todos e registros preservados (recomendado) em10/10/2026.
- **Trade-off**: consulta compartilhada acompanha o acesso aos quadros; não reatribuir autoria histórica nem completar eventos ausentes.
- **Scope**: consulta/preservação da próxima atividade por gerenciamento. Ainda definir o detalhe das alterações e os contratos da nova entrega.
- **Date**: 2026-10-10
- **Status**: active

### AD-032
- **Decision**: nas novas edições, a atividade mostrará cada campo alterado com valor anterior e novo, junto com autor e horário; dados antigos conservam somente as informações já registradas.
- **Reason**: usuário respondeu explicitamente Mostrar antes e depois dos campos alterados (recomendado) em10/10/2026.
- **Trade-off**: persistir os valores no momento da alteração; consultar o estado atual não reconstrói o estado anterior.
- **Scope**: atividade por gerenciamento; quadro, etapas e demandas. Não inventar detalhes retroativos.
- **Date**: 2026-10-10
- **Status**: active

### AD-033
- **Decision**: incluir o envio de comentários na atividade como Comentário adicionado, identificando demanda, autor e horário, sem repetir o texto da mensagem.
- **Reason**: usuário respondeu explicitamente Sim, registrar o envio sem repetir a mensagem (recomendado) em10/10/2026.
- **Trade-off**: ler a mensagem exige abrir a conversa da demanda; a atividade acompanha o envio sem duplicar conteúdo.
- **Scope**: próximos envios nas rotas modernas e antigas de comentários; preservar histórico da demanda e versão do quadro conforme COM-12.
- **Date**: 2026-10-10
- **Status**: active

### AD-034
- **Decision**: bloquear novas entradas manuais na API antiga de histórico com409 em demanda ativa, preservando consulta, registros existentes e precedência de recusas por arquivo. Adequar exatamente os três cenários de testes descritos em atividade-gerenciamento/tasks.md T13, sem excluir/ignorar casos.
- **Reason**: usuário confirmou explicitamente Sim, bloquear e adequar esses testes em10/10/2026 após explicação da autoria/ação escolhidas pelo navegador.
- **Trade-off**: chamadas antigas deixam de criar histórico manual; ações reais continuam gerando seus registros automaticamente.
- **Scope**: POST histórico e registrarExterno; manter histórico interno das ações e dados antigos. Implementar na T13, ainda pendente.
- **Date**: 2026-10-10
- **Status**: active

## Handoff

Checkpoint atual12/10/2026: T1–T15 Implemented. T14 confirmado em e53302f,sem trabalho pendente dessa tarefa. T15 quick1131/1131 em26 arquivos PASS,131 novos além dos1000 anteriores; próximos T16–T18 pelo trabalhador3 e depois Verifier fresco automático. Fonte/testes/docs T15 entram no commit atômico; gerados anteriores preservados. Nenhum ACT Verified. Branch testes; sem blocker,sem operações remotas/reais. A lista histórica abaixo registra o checkpoint anterior do lote2,com sua próxima ação já reconciliada neste parágrafo.

- **Feature:** atividade-gerenciamento, branch testes, execução aprovada em três lotes sequenciais7/7/4.
- **Completed:** checkpoint12/10/2026,T1–T14 Implemented. Datas originais T1–T5 em10/10 e T6–T14 em12/10 preservadas.1087 Java/H2 (840 antigos+247 novos),1000 Vitest,847 MySQL fictício,lint/buildTEMP PASS,zero falhas/erros/skips. Lote2 T8–T14 adicionou166 cenários; tarefas/evidência/diagnóstico atualizados. Nenhum ACT Verified. T8–T13 commits a6fa709,0ea333d,5e38434,e41c9c9,5e7ea07,47d710b; T14 incluído no próximo commit atômico.
- **In progress:** feature atividade-gerenciamento,ainda sem painel/browser ou Verifier final. Lote2 completo como implementação; próxima tarefa T15 no lote3 após revisão do orquestrador. Apenas três outcomes antigos T13 já autorizados foram adequados; demais testes preservados.
- **Next step:** iniciar trabalhador3 T15–T18 após revisão/commit T14. Contrato DTO RegistroAtividadeResponse/PaginaAtividadeResponse,cursor canônico Base64URL sem padding de quadro:maxA:maxH:offset,quatro inteiros seguros; formato em design.md. Transições incluem contexto etapaNome mesmo quando nomes iguais e etapaId muda. Verifier fresco automático após T18,sensor>=5/validate_state0 antes de concluir.
- **Blocker:** nenhum.
- **Environment:** MySQL fictício8.0.43 OWNED TEMP/creral-arquivo-049cfd5cc86d4d8b840e41f1710d7a43/mysql,schema creral_arquivo_test,porta33823 encerrado após confirmar identidade atual13172/datadir/porta: mysqladminrootfictíciosem senha shutdown0,listener33823 ausente. Banco real intocado. JAVA_HOME JDK25.0.2,Maven3.9.16,npm existentes. Logs TEMP/atividade-T14-{java,vitest,lint,build,mysql}.log; buildTEMP/atividade-T14-build-4a2d3f4a6e1443dbb688ac1f192bfc33.
- **Uncommitted files:** código/testes/documentos T14 serão incluídos juntos neste commit; depois restarão somente gerados target/node_modules/test-results preexistentes e dos gates,não limpar/reset/stash. Nenhuma atividade concluída/Verified antes do Verifier. Mensagem T13 corrigida só em metadata de92c9fc1 para47d710b com !/BREAKING CHANGE,sem mudança de árvore ou gates.
- **Restrictions:** somente implementação/commits locais; sem push/deploy/MySQL real/upgrades.
- **User preference:** registrar progresso real e dizer incluído no diagnóstico; concluir somente com gates e Verifier PASS.

## Registro histórico das conversas sobre destinos e movimentação

As notas abaixo registram as conversas anteriores. O estado atual é o Handoff acima; transições foram entregues e validadas em 07/10/2026, e AD-023 decide a próxima edição.

Confirmado em AD-011: somente Concluídos e Cancelados, com destino automático conforme a ação. As colunas obrigatórias já estão implementadas e verificadas; os fluxos de finalizar/cancelar continuam na próxima entrega.

Confirmado em AD-012: somente o criador do gerenciamento e administradores poderão mover, pular etapas, concluir e cancelar demandas de quadro ativo. A resposta B não decide reabertura nem permissões para criar/editar/excluir demandas.

Confirmado agora em AD-013: criador do gerenciamento e administradores também poderão reabrir demandas concluídas/canceladas, escolhendo uma etapa de trabalho do mesmo quadro. Quadros arquivados precisam ser restaurados antes. Reabertura ainda não foi implementada; o contrato de status/datas e preservação do histórico será explicitado na especificação, sem inventar datas de eventos antigos.

Confirmado agora em AD-014: uma demanda em andamento poderá ir para qualquer etapa de trabalho do mesmo gerenciamento, inclusive anterior, pelo criador/admin. Etapas finais continuam destinadas às ações de concluir/cancelar; para sair de uma etapa final, usar reabertura. Essa escolha ainda não foi implementada.

Confirmado agora em AD-015: novos cancelamentos exigirão justificativa preenchida. Nenhum motivo será criado retroativamente para demandas antigas. Validação e ação ainda não foram implementadas.

Auditoria somente leitura: `GerenciamentoGuard.java:44–54` já oferece a verificação criador/admin; criador nulo deixa apenas admin autorizado. `ProcessoService.java:204/256/286` ainda protege somente o estado ativo nas ações dedicadas. A edição genérica também altera status (:137) e etapa (:161–187); esses caminhos precisarão obedecer à mesma regra quando executarem as ações de AD-012. Não basta ocultar botões. Nenhuma alteração de código ou teste executado nesta rodada de decisões.

A criação recebe entidade completa e salva status/datas enviados (`ProcessoController.java:32`, `ProcessoService.java:49–65`); estado/etapa iniciais serão definidos no contrato de demandas. Não restringir indiscriminadamente o helper compartilhado com edição de campos e exclusão, cujas permissões ainda não foram decididas. Testes antigos que aceitam terceiro movendo/finalizando precisarão refletir AD-012 e verificar bloqueio sem efeitos: `ProcessoArchiveTests.java:82–111`, `HistoricoArchiveTests.java:52–62`, `EtapaDemandConcurrencyTests.java:45–70/157–159`. Nenhum desses testes foi alterado nesta conversa.

Confirmado em AD-016: preparar correção para todos os quadros, inclusive arquivados. Ferramenta explícita já implementada e testada em H2/MySQL fictícios; plano não grava e aplicação preserva registros/repetição. Nenhuma migração real autorizada/executada. Este bloco de cinco perguntas está resolvido.

Entrega de etapas-finais concluída: categorias explícitas, criação/proteção das duas colunas oficiais, interface e preparação idempotente de legados. Não promete ainda coerência permanente de status/etapa nas APIs antigas de demandas; esses fluxos, a permissão de AD-012, reabertura e justificativa pertencem à entrega seguinte. Testes desses fluxos existentes foram preservados neste corte.

Cadastro e leitura concluídos em2026-10-06,41/41 critérios verificados, sem prometer CRUD completo. AD-017 permite criar a todos autenticados em quadro ativo; AD-018 define primeira etapa de trabalho automaticamente. Número manual global e obrigatórios foram respondidos1A/2A e registrados em AD-019/020; não estão mais pendentes. Sem trabalho, funcionário solicita ao criador/admin a configuração conforme AD-007. Próxima entrega: transições; campos atuais ao reabrir resolvidos em AD-021. Status desconhecido permanece uma pergunta nova pendente.

Auditoria inicial da criação, antes da entrega28b8c88..926c862: ProcessoController.java:32 recebia entidade completa; ProcessoService.java:52 exigia etapa enviada e :65 salvava sem defaults. Quadro.jsx:140 tinha Criar processo sem ação e não carregava cartões. Esses pontos foram corrigidos e verificados em features/demandas-cadastro/validation.md. Exclusão ativa ainda registra histórico dependente antes de apagar; falha/rollback caracterizada em ProcessoArchiveTests.java:115–121. Edição/exclusão continuam fora do corte, com políticas próprias pendentes; não restringir seu helper indiscriminadamente.

CRUD de etapas segue AD-007–010, com detalhes e defaults declarados em `features/etapas/context.md`. Não inferir respostas novas de “pode continuar”.
