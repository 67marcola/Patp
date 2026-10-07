# STATE

## Decisions

Restrições declaradas pelo usuário: Java, Spring Boot, MySQL; implementação por requisito, com testes e uso da skill `tlc-spec-driven`. As regras de cada funcionalidade serão esclarecidas antes da implementação.

Nome da empresa confirmado pelo usuário: **Creral**. Usar essa grafia na documentação e nos textos da aplicação.

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

## Handoff

- **Feature:** demandas-movimentacao.
- **Branch:** testes. Último commit de tarefa: 7aa0485 (T5).
- **Completed:** T1–T5. Gates já aprovados: 459 Java/H2 e 630 Vitest na T5.
- **In progress:** T6, ações nos cartões, busy global, snapshots/cache/foco e consulta manual após falhas. Fonte frontend/src/pages/Quadro.jsx; testes frontend/src/pages/QuadroTransicoes.test.jsx.
- **Latest evidence:** 31/31 casos de QuadroTransicoes PASS em TEMP/creral-mov-ui-0fccc14db03f4e5d9cb6220a95052a27/t6-transicoes-2.log. O teste de Sair tinha fixture de sessão incompleta; corrigido token+usuario e GET dos metadados, sem alterar assertions.
- **Pending clarification:** enviada pergunta assíncrona para autorizar adequação das três assertions de QuadroEtapas.test.jsx:59/:519/:560 que proíbem todo botão dentro de colunas finais. A proposta concreta é continuar proibindo editar/remover a coluna final, permitindo botões das demandas. Essas assertions ainda não foram modificadas. A skill implement.md exige perguntar antes de corrigir assertion antiga incompatível.
- **Gate:** T6 ainda NÃO completo/commitado. Última suite integral anterior à correção da fixture: 634 PASS/19 FAIL de 653; 18 falhas ligadas à proibição de botões nas finais. Os 8 novos casos HTTP/rede aumentam a expectativa para 661 na próxima suite integral.
- **Next step:** incorporar resposta do usuário, ajustar somente seleção das assertions autorizadas, executar npm test integral com TEMP real, adequação/mapas/status e commit atômico T6. Depois T7: navegador, concorrência Java, MySQL fictício, operação. Após commit T7, Verifier fresco obrigatório, sensor e validate_state; não declarar funcionalidade completa antes disso.
- **Uncommitted source:** Quadro.jsx, QuadroTransicoes.test.jsx; documentação STATE/evidence. Artefatos gerados rastreados de node_modules/target devem ser preservados e excluídos dos commits de fonte; não git add geral.
- **Restrictions:** sem push/deploy/banco real; nenhuma operação no MySQL da empresa. Autorizações de implementação/testes/commits locais persistem.

## Próxima conversa: destinos finais e movimentação

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
