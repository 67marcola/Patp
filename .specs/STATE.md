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

## Handoff

- **Feature**: cadastro e leitura de demandas, levantamento em features/demandas-cadastro/context.md.
- **Phase / Task**: Execute encerrado, cadastro/consulta41/41 PASS. T7 corrigiu CAD-33 em926c862; T6 teste/roteiro no commit do usuário11e1307, fechamento0477167. Plano5040dc3; T1 7c31608, T2 e24ce17, T3 133915e, T4 b6e13de, T5 f134fcb. Fonte Java permanece e24ce17.
- **Completed**: gerenciamentos41/41, etapas30/30, autocadastro14/14, retirada de PM4/4, finais30/30 e cadastro/consulta41/41, com revisões independentes. Gates atuais:390Java/H2,475Vitest,buildTEMP/lint,4Edge/H2 e145MySQL selecionados. Criação inicia na primeira TRABALHO; formulário só fecha com confirmação da demanda. Cadastro de conta permanece FUNCIONARIO. Edição/exclusão/transições não foram entregues neste corte.
- **In-progress**: nenhuma implementação do cadastro pendente. Verificador final PASS em validation.md; provas iniciais de backend reutilizadas com fonte inalterada. Sensor8/8 falhas detectadas:7backend e novo guardfrontend; controles112Java/242Vitest PASS. Comparação final integral3546bytes/SHA256a6d2b2b90f641cbc7b99aded1879a3a64eec6d21b85deb4a651e062cd39cb3c3 igual após todos processos encerrados, antes do relatório. TEMP/demandas-reverify-29e76cc43253a8. FAIL inicial/diagnósticos ambientais preservados; L-001/002/003 candidates, nenhuma promoção. UAT humano sem resultado.
- **Next step**: aguardar resposta sobre limpar ou manter os campos atuais de encerramento ao reabrir; pergunta enviada para a próxima entrega, sem decisão inferida. Depois especificar movimentar/pular/concluir/cancelar/reabrir conforme AD-011–015, incluindo fechar desvio por PUT genérico, autoria/histórico e testes. Não refazer cadastro/consulta ou etapas finais.
- **Blockers**: nenhum nesta entrega concluída. Escolha de campos da reabertura pendente para a próxima. Contas reais, permissões de edição/exclusão, comentários, logs e gráficos terão seus próprios recortes.
- **Uncommitted files**: documentação deste encerramento registrada em commit próprio; artefatos gerados pelos gates em target e dependências preservados. Commit do usuário11e1307 e dist/node_modules/target rastreados não foram reescritos nem limpos; staging somente por caminhos explícitos.
- **Branch**: testes; base do cadastro/leitura 28b8c88, base de etapas-finais a118606, base histórica 5d8beb9.

Autorização de implementação permanece válida. Não iniciar aplicação normal/MySQL configurado nem reiniciar serviços do usuário. Testes somente H2/helper isolado ou MySQL TEMP fictício explicitamente delimitado. Não push/deploy/alterar contas reais. Serviços próprios de testes encerrados; portas18082/4173/18083/33817 livres. Nenhum serviço8081/5173 estava em execução nesta retomada. Mysqld estrangeiros4304/5232 preservados. A falha ECONNRESET anterior de etapas permanece documentada em seu relatório, sem causa inventada.

MySQL do cadastro encerrado: instância TEMP em33817, launcher6628/filho18636, datadir fictício conferido C:/Users/Marco/AppData/Local/Temp/creral-mysql-crud-be180f80b1f64ead87cc0113c6bd68c1/data. Schema novo creral_demandas_tests_70e5/conta fictícia creral_cad_70e5 limitada a ele. Gate145 selecionados PASS, incluindo UNIQUE1062/409 e quatro CHECK/500/rollback. Primeira invocação teve URL truncada por ampersands no cmd/bootstrap145erros; preservada, sem contar como PASS. Comando corrigido transmitiu URL via ambiente. Evidências em C:/Users/Marco/AppData/Local/Temp/creral-demandas-mysql-70e509769b814ba59a5a557b6b39cdd3/mysql-evidence.md. Shutdown0 após conferir PID/comando/datadir; logs confirmam complete, só4304/5232 preservados e33817livre. Nenhuma limpeza.

Evidências de etapas-finais: MySQL/script/snapshots em C:/Users/Marco/AppData/Local/Temp/creral-etapas-finais-3b48b05a57314180a8f952ea6fa43d07; gates independentes e scratch recusada em C:/Users/Marco/AppData/Local/Temp/etapas-finais-verifier-47a7e6f8d5fc4be6bfd27345677d01c9; sensor RAM/harness/logs em C:/Users/Marco/AppData/Local/Temp/etapas-finais-ram-6cecccc8b93e4264aefc18773d7c7a0e. A rejeição não teve razão adicional. Nenhuma nova tentativa sobre a cópia recusada nem sobre limpezas históricas.

Sensor marca-login: falhas em cópias do JavaScript executadas em memória, sem modificar o projeto. Porcelain integral antes/depois byteigual: 231250 bytes, 3406 linhas, SHA256 7c00fa1801471477c0d988eb7155679c872148517310389fa817ecf86dec84d6. Contextos/browser e preview próprios encerrados; portas 18082/4173/4174 livres e serviços do usuário 8081/5173 preservados. Relatório integral em features/marca-login/validation.md. Nenhuma nova rejeição de limpeza nesta entrega.

Sensor autocadastro: 7/7 falhas compiláveis detectadas, baselines restauradas 9/9 Java e 73/73 Vitest. Porcelain integral antes/depois byteigual: 231321 bytes, 3408 linhas, SHA256 23f16a08d605c42dfe3179c2ff241faf88a5a65260d40629a7f41f1e45afff86. Comparação com scratch preservada, não após descarte. Revisão automática rejeitou três comandos de limpeza antes de execução com `blocked by policy`, sem razão adicional; interrompidas novas tentativas. Cópia em C:/Users/Marco/AppData/Local/Temp/autocadastro-verifier-968133323a4c4f8d9765d4cd69036d21/scratch, sem processos próprios. Portas 18082/4173/33817 livres; serviços do usuário 8081/5173 preservados. Relatório integral em features/autocadastro/validation.md.

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

Cadastro e leitura concluídos em2026-10-06,41/41 critérios verificados, sem prometer CRUD completo. AD-017 permite criar a todos autenticados em quadro ativo; AD-018 define primeira etapa de trabalho automaticamente. Número manual global e obrigatórios foram respondidos1A/2A e registrados em AD-019/020; não estão mais pendentes. Sem trabalho, funcionário solicita ao criador/admin a configuração conforme AD-007. Próxima entrega: transições; a escolha sobre os campos atuais ao reabrir permanece pendente.

Auditoria inicial da criação, antes da entrega28b8c88..926c862: ProcessoController.java:32 recebia entidade completa; ProcessoService.java:52 exigia etapa enviada e :65 salvava sem defaults. Quadro.jsx:140 tinha Criar processo sem ação e não carregava cartões. Esses pontos foram corrigidos e verificados em features/demandas-cadastro/validation.md. Exclusão ativa ainda registra histórico dependente antes de apagar; falha/rollback caracterizada em ProcessoArchiveTests.java:115–121. Edição/exclusão continuam fora do corte, com políticas próprias pendentes; não restringir seu helper indiscriminadamente.

CRUD de etapas segue AD-007–010, com detalhes e defaults declarados em `features/etapas/context.md`. Não inferir respostas novas de “pode continuar”.
