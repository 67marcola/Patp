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

## Handoff

- **Feature**: CRUD de etapas de trabalho da Creral.
- **Phase / Task**: T1–T6 implementadas; T7 reforça cobertura de ETA-27 após FAIL da revisão independente, rodada 1.
- **Completed**: gerenciamentos T1–T16 validado, 41/41 critérios. Etapas até 9a19344: 238 Java/H2, 137 Vitest, build/lint e 2 E2E Edge PASS; MySQL temporário 8.0.43, 237 PASS em mysql-validation.md. Revisão independente: 29/30 critérios completos, ETA-27 parcial; sensor 10 falhas compiláveis, 9 detectadas e M10 sobrevivente. Produção já trata o erro corretamente. Evidências em features/etapas/validation.md, evidence.md e frontend-evidence.md.
- **In-progress**: T7 por etapas_frontend cobre POST201/PUT200/DELETE200 com JSON ilegível, preservação de campos/colunas e recuperação somente por GET. Verificador liberou autores após remover scratch e comprovar porcelain byteigual. L-002 registrada pelo script como candidate; L-001 continua candidate. Teste humano permanece sem resultado.
- **Next step**: gate e commit atômico de T7, depois nova revisão independente; fechar somente após PASS e validate_state exit0.
- **Blockers**: nenhum para a correção autorizada. Regras futuras de movimentação/reabertura ainda aguardam resposta; destinos finais já confirmados em AD-011. Contas administrativas reais serão selecionadas posteriormente.
- **Uncommitted files**: artefatos gerados de teste/build já existentes, preservados. Relatório e lições desta rodada entram neste checkpoint documental. Arquivos de T7 pertencem ao trabalhador da interface e ficam fora deste staging.
- **Branch**: testes; base 5d8beb9.

A aplicação normal e o MySQL configurado não foram iniciados. Instância MySQL temporária encerrada; portas 33817/18082/4173 livres ao término dos testes. Preservar artefatos gerados e usar somente bancos fictícios isolados. Continuação autorizada pelo usuário em 2026-10-05 não equivale a resultado de UAT.

## Próxima conversa: destinos finais e movimentação

Confirmado em AD-011: somente Concluídos e Cancelados, com destino automático conforme a ação. Essas colunas e os fluxos de finalizar/cancelar pertencem à próxima entrega.

Perguntas já enviadas, ainda sem resposta; sugestões não são decisões:

- Quem poderá mover, pular etapas, concluir ou cancelar demandas de quadro ativo: todos os autenticados, ou somente criador/admin?
- Depois de concluir/cancelar, será permitido reabrir a demanda escolhendo uma etapa de trabalho?

CRUD de etapas segue AD-007–010, com detalhes e defaults declarados em `features/etapas/context.md`. Não inferir respostas novas de “pode continuar”.
