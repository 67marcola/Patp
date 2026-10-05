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

## Handoff

- **Feature**: CRUD de gerenciamentos da Creral.
- **Phase / Task**: retomada autorizada em 2026-10-05; implementação concluída, verificação independente pendente.
- **Completed**: diagnóstico, referência PATP, decisões AD-001 a AD-008, especificação com 41 critérios confirmada, design/tarefas validados; T1–T15 em commits locais (última tarefa: `aa981e6`). Servidor: 97 testes H2 e 96 testes de comportamento MySQL, zero falhas/erros/ignorados. Interface: 65 testes, build/lint PASS e um fluxo completo no Edge real com Tab/Enter e recarregamento. Revisão visual desktop/mobile sem cortes. Schema antigo temporário preservado nas seis tabelas e após reinicialização; detalhes em `features/gerenciamentos/mysql-validation.md`.
- **In-progress**: fechamento dos registros e despacho de um Verificador independente novo. Os serviços temporários de testes e migração foram encerrados.
- **Next step**: verificação dos 41 critérios e sensor de discriminação em cópia isolada; corrigir eventuais lacunas antes de declarar conclusão técnica. Depois, iniciar avaliação de uso com o usuário e especificar CRUD de etapas.
- **Blockers**: nenhum para o primeiro CRUD. Permissões de etapas e remoção de etapa com demandas já respondidas para a próxima entrega. Pergunta sobre destinos finais permanece pendente, sem assumir uma resposta. Contas administrativas reais serão selecionadas posteriormente; testes usam contas fictícias.
- **Uncommitted files**: registros de entrega e artefatos gerados de teste/build; staging somente da documentação intencional. Fontes e testes das 15 tarefas já estão commitados.
- **Branch**: testes; base 5d8beb9.

A aplicação normal não foi iniciada nem o banco MySQL configurado alterado. A instância temporária tem pasta de dados nova em TEMP e só contém dados fictícios. Usar apenas banco isolado durante os testes.

## Próxima conversa: etapas

Já confirmado: somente criador/administrador configura etapas; remover etapa com demandas é bloqueado (AD-007/008).

Pergunta enviada, aguardando resposta: exatamente duas colunas finais fixas, Concluídos/Cancelados, ou permitir destinos adicionais de cada categoria escolhidos ao finalizar? A proposta de destinos adicionais deve manter as duas etapas obrigatórias pedidas originalmente. Não iniciar a implementação desses destinos por ausência de resposta.

Leitura do código para discussão posterior: `ordem` permite empates/lacunas; proposta é escolher posição e reorganizar automaticamente. `setor` é texto informativo e não controla acesso; ainda esclarecer se deve continuar obrigatório nas etapas de trabalho. O histórico existente referencia a demanda e guarda nomes de etapas em texto; remover uma etapa vazia não precisa apagar registros anteriores nem reescrevê-los após renomear.
