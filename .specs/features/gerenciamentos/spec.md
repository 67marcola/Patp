# CRUD de gerenciamentos da Creral Specification

**Status:** confirmado pelo usuário; implementação em andamento, sem conclusão da feature.
**Data:** 2026-10-04.
**Escopo:** primeira entrega funcional do sistema, seguindo a implementação de um requisito por vez.
**Contexto:** `context.md`, decisões AD-001 a AD-006 de `.specs/STATE.md` e artigo PATP resumido em `.specs/REFERENCIA_PATP.md`.

## Problem Statement

A interface atual permite criar e listar gerenciamentos, mas não editar nem retirar quadros de uso. A API não atribui criador, não distingue administradores e não possui arquivamento. Esta entrega completa a administração dos quadros e preserva seus dados ao arquivar, aplicando as permissões e a condição de somente consulta escolhidas pelo usuário.

## Goals

- [ ] Criar, consultar e editar gerenciamentos pela interface com persistência na API.
- [ ] Arquivar e restaurar sem apagar ou deslocar etapas, demandas, comentários e histórico.
- [ ] Aplicar as permissões no backend, inclusive para quadros sem criador.
- [ ] Bloquear as mutações existentes de conteúdo dos quadros arquivados.
- [ ] Comprovar os resultados por testes isolados e verificação independente.

## Out of Scope

| Funcionalidade | Motivo e encaminhamento |
| --- | --- |
| Interface de CRUD completo e reordenação de etapas | Próximo requisito do usuário; esta entrega mantém a configuração inicial existente e protege as rotas atuais durante arquivamento. |
| Criação automática e proteção das etapas Concluídos/Cancelados | Requisito posterior, a executar antes de finalizar os fluxos de demandas. A primeira entrega não será declarada como cumprimento desse pedido. |
| Interface de CRUD de demandas e botão Criar processo | Requisito posterior; a proteção das rotas já existentes em quadros arquivados pertence a esta entrega. |
| Novas regras de pular etapas, concluir/cancelar e reabrir demandas | Dependem das etapas finais e da especificação própria; não alterar o comportamento de quadros ativos nesta entrega além dos guardas necessários de consistência. |
| Lateral de comentários e novos logs completos por gerenciamento | Requisitos posteriores. Preservar os registros atuais é obrigatório nesta entrega; não declarar novo painel de auditoria implementado. |
| Gráficos, busca/filtro de demandas, login automático e redesign geral | Continuam no diagnóstico/backlog e serão tratados depois. O filtro de quadros ativos/arquivados está incluído. |
| Tela de administração de usuários | O usuário escolhe as contas administrativas; esta entrega permite configuração confiável no servidor e impede autoatribuição pelo cadastro público. |
| Apagar dados, promover contas reais, alterar banco configurado, publicar ou enviar código remotamente | Não são necessários para desenvolver/testar localmente este CRUD. Não executar como parte desta entrega. |

## Assumptions & Open Questions

As respostas expressas estão marcadas como confirmadas. Os padrões propostos estão marcados para revisão; não são respostas atribuídas ao usuário.

| Assumption / decision | Chosen default | Rationale | Confirmed? |
| --- | --- | --- | --- |
| Organização do quadro | Várias demandas por gerenciamento, cada uma com etapa atual própria | Modelo A escolhido pelo usuário e compatível com o código/artigo | Sim, AD-001 |
| Permissões dos quadros | Todos logados veem/criam; criador ou administrador edita/arquiva | Política 1A escolhida | Sim, AD-002 |
| Retirada de uso | Arquivar preservando dados e permitindo restauração | Política 2A escolhida | Sim, AD-003 |
| Administradores | Contas escolhidas pelo usuário; demais cadastros são funcionários | Evita administração definida pela ordem dos cadastros | Sim, AD-004 |
| Arquivados | Apenas consulta até restaurar | Comportamento A escolhido | Sim, AD-005 |
| Quadros sem criador | Somente administradores editam/arquivam/restauram; todos logados consultam | Política A escolhida, sem inventar autoria | Sim, AD-006 |
| Restauração de quadro com criador | Criador ou administrador pode restaurar | Mantém a responsabilidade administrativa já escolhida | Proposta explícita para revisão |
| Campos do quadro | Nome obrigatório com 1–120 caracteres após remover espaços externos; descrição opcional com até 255 caracteres | Mantém nome/descrição atuais e estabelece limites verificáveis sem reduzir colunas legadas | Proposta explícita para revisão |
| Nomes iguais | Permitidos; identificação e relações são por ID | Modelo atual não exige unicidade e quadros independentes podem ter nomes iguais | Proposta explícita para revisão |
| Apresentação de arquivados | Filtro Ativos / Arquivados, com Ativos inicialmente | Permite consulta e restauração sem misturar quadros retirados de uso | Proposta explícita para revisão |
| Contas administrativas específicas | Selecionar contas já cadastradas mediante configuração confiável do servidor; usar contas fictícias nos testes | Implementar regras não exige pedir contas reais ou conceder papel pelo JSON do cadastro | Escolha técnica; contas reais serão indicadas na configuração |
| Dados existentes | Estado antigo sem marcador de arquivamento equivale a ativo; criador desconhecido continua desconhecido | Preserva dados sem adivinhar autoria nem arquivar registros automaticamente | Padrão técnico explícito |
| Limites em dados existentes | Aplicar limites às novas gravações sem truncar registros antigos | Preservação foi solicitada; um valor legado maior pode continuar sendo consultado | Padrão técnico explícito |
| Concorrência | Edição com versão antiga retorna 409; arquivamento e mutações de conteúdo são ordenados atomicamente | Evita sobrescrever alterações e gravar conteúdo depois do arquivamento confirmado | Escolha técnica com resultado testável |
| Repetição e comunicação | Bloquear duplo envio na interface; não repetir criação automaticamente; arquivar/restaurar repetidos não geram novo efeito | Nomes iguais são permitidos; não usar nome como chave de deduplicação | Padrão técnico explícito |
| Logs nesta entrega | Preservar históricos atuais e metadados de estado; novo histórico completo de ações e sua interface serão outra entrega | Respeita a execução por requisito e não promete um painel ainda inexistente | Divisão proposta para revisão |

**Open questions:** none — decisões respondidas ou padrões explicitamente registrados acima. A revisão confirma ou ajusta os padrões antes de implementar. Contas reais são configuração posterior, não lacuna para os testes fictícios.

## User Stories

Os critérios usam EARS: WHEN = quando, IF = se, WHILE = enquanto e SHALL = deve. Cada identificador GER-NN corresponde a um resultado a verificar.

### P1: Criar um gerenciamento com autoria confiável

**User Story:** como funcionário autenticado, quero criar um quadro com nome e descrição para organizar suas etapas e demandas.

**Why P1:** criação e identificação do responsável são a base das permissões do CRUD.

**Acceptance Criteria**:
1. WHEN um usuário autenticado cria um gerenciamento válido THEN o sistema SHALL responder HTTP 201 com o novo gerenciamento ativo e um ID próprio. (GER-01)
2. WHEN um gerenciamento é criado THEN o sistema SHALL atribuir como criador exclusivamente o usuário da sessão autenticada, independentemente de autoria enviada pelo cliente. (GER-02)
3. IF uma criação ou edição autorizada de quadro ativo tiver nome normalizado com zero ou mais de 120 caracteres THEN o sistema SHALL responder HTTP 400 com `Informe um nome entre 1 e 120 caracteres.`. (GER-03)
4. IF uma criação ou edição autorizada de quadro ativo tiver descrição com mais de 255 caracteres THEN o sistema SHALL responder HTTP 400 com `A descrição deve ter até 255 caracteres.`. (GER-04)
5. WHEN forem criados dois gerenciamentos com o mesmo nome válido THEN o sistema SHALL manter dois IDs distintos com relações de etapas e demandas independentes. (GER-05)
6. WHEN a criação válida omitir descrição ou lista de etapas THEN o sistema SHALL aceitar a omissão sem erro de referência nula. (GER-06)
7. IF a configuração inicial de etapas informada contiver dados inválidos ou falhar ao salvar THEN o sistema SHALL deixar zero gerenciamentos e zero etapas novas dessa tentativa persistidos. (GER-07)

**Independent Test:** usuário fictício cria dois quadros homônimos; nome com 1/120 caracteres e descrição ausente/255 são aceitos; 121/256 e nome em branco são recusados sem inserção. Uma falha na segunda etapa inicial desfaz toda a criação.

### P1: Consultar quadros ativos e arquivados

**User Story:** como usuário autenticado, quero consultar os quadros compartilhados e identificar se estão ativos ou arquivados.

**Why P1:** arquivar deve retirar do uso cotidiano sem apagar nem impedir consulta.

**Acceptance Criteria**:
1. WHEN um usuário autenticado lista gerenciamentos sem selecionar estado THEN o sistema SHALL responder HTTP 200 somente com os quadros ativos. (GER-08)
2. WHEN um usuário autenticado seleciona o filtro Arquivados THEN o sistema SHALL responder HTTP 200 somente com os quadros arquivados. (GER-09)
3. WHEN qualquer usuário autenticado consulta um quadro existente por ID THEN o sistema SHALL responder HTTP 200 com seus dados e estado, inclusive quando arquivado ou sem criador. (GER-10)
4. IF o ID de gerenciamento consultado ou administrado não existir THEN o sistema SHALL responder HTTP 404 com `Gerenciamento não encontrado.`. (GER-11)

**Independent Test:** usuários diferentes consultam ativos, arquivados e quadro com criador nulo; cada filtro retorna apenas os IDs esperados; ID ausente retorna 404.

### P1: Editar com responsabilidade e sem sobrescrever outras alterações

**User Story:** como criador ou administrador, quero corrigir nome e descrição do quadro sem alterar suas etapas e demandas.

**Why P1:** a edição já existe parcialmente na API e precisa de autorização e interface.

**Acceptance Criteria**:
1. WHEN o criador ou administrador edita nome/descrição válidos de um quadro ativo usando sua versão atual THEN o sistema SHALL responder HTTP 200 com os valores persistidos e a nova versão. (GER-12)
2. IF um funcionário diferente do criador tentar editar, arquivar ou restaurar o quadro THEN o sistema SHALL responder HTTP 403 com `Você não tem permissão para administrar este gerenciamento.`. (GER-13)
3. IF um funcionário comum tentar editar, arquivar ou restaurar um quadro sem criador THEN o sistema SHALL responder HTTP 403 com `Você não tem permissão para administrar este gerenciamento.`. (GER-14)
4. WHEN nome/descrição de um quadro forem editados THEN o sistema SHALL limitar os efeitos dessa operação a nome/descrição/versão, sem modificar criador, estado ou atributos e vínculos dos registros associados. (GER-15)
5. IF uma edição ou mudança de estado válida usar versão anterior à persistida THEN o sistema SHALL responder HTTP 409 com `Gerenciamento alterado por outro usuário. Atualize e tente novamente.`, exceto quando o estado solicitado já estiver aplicado. (GER-16)

**Independent Test:** criador e administrador editam; terceiro e funcionário diante de autoria nula recebem 403; duas edições da mesma versão não sobrescrevem o resultado da primeira. Comparar os dados associados antes/depois.

### P1: Arquivar e restaurar preservando os dados

**User Story:** como criador ou administrador, quero retirar um quadro da lista de ativos e poder retomá-lo sem recriar seu conteúdo.

**Why P1:** o usuário escolheu arquivamento reversível em lugar de exclusão física.

**Acceptance Criteria**:
1. WHEN o criador ou administrador arquiva um quadro ativo com versão atual THEN o sistema SHALL responder HTTP 204 e persistir seu estado arquivado. (GER-17)
2. WHEN o criador ou administrador restaura um quadro arquivado com versão atual THEN o sistema SHALL responder HTTP 204 e persistir seu estado ativo. (GER-18)
3. WHEN um usuário autorizado repete arquivamento de quadro já arquivado ou restauração de quadro já ativo THEN o sistema SHALL responder HTTP 204 sem novo efeito sobre seus dados ou sua versão. (GER-19)
4. WHEN um quadro é arquivado ou restaurado THEN o sistema SHALL manter os efeitos dessa operação restritos aos metadados do quadro, sem alterar os IDs, atributos ou vínculos de etapas, demandas, comentários e históricos existentes. (GER-20)
5. WHILE um gerenciamento estiver arquivado, WHEN uma requisição autenticada e autorizada para a ação, com dados válidos, tenta modificar seu conteúdo pelas rotas existentes THEN o sistema SHALL responder HTTP 409 com `Gerenciamento arquivado. Restaure-o antes de alterar.`. (GER-21)
6. WHEN uma mutação de conteúdo disputa com o arquivamento do seu quadro THEN o sistema SHALL confirmar a mutação no banco antes da confirmação do arquivamento, ou recusar a mutação sem gravação se o arquivamento for confirmado primeiro. (GER-22)

**Independent Test:** com etapas, demanda, comentário e histórico pré-cadastrados, arquivar → consultar → restaurar preserva os mesmos registros. Exercitar a matriz de rotas e uma disputa coordenada entre arquivamento e mutação de conteúdo. Repetir operações já aplicadas não gera outro efeito.

### P1: Aplicar identidade e permissões no servidor

**User Story:** como equipe responsável, quero que os papéis e a identidade valham nas chamadas diretas à API, além dos botões da interface.

**Why P1:** a criação com autoria e a administração por contas selecionadas precisam impedir falsificação pelo cliente.

**Acceptance Criteria**:
1. IF uma rota protegida desta entrega receber sessão ausente ou inválida THEN o sistema SHALL responder HTTP 401. (GER-23)
2. WHEN um cadastro comum envia propriedades de papel administrativo THEN o sistema SHALL manter a conta resultante como funcionário, sem conceder privilégio pelo corpo da requisição. (GER-24)
3. IF um cadastro público informar ID de conta existente ou outro ID escolhido pelo cliente THEN o sistema SHALL responder HTTP 400 sem criar ou substituir conta. (GER-25)
4. The sistema SHALL reconhecer administradores somente a partir da seleção confiável de contas já cadastradas no servidor. (GER-26)
5. The sistema SHALL omitir senha e hash de senha em todas as respostas de cadastro e de recursos usados por este CRUD, inclusive nas relações aninhadas de usuários. (GER-27)

**Independent Test:** chamadas diretas com token inválido retornam 401; funcionario não vira administrador enviando campos falsificados; cadastro com ID não substitui conta; administrador fictício autorizado é reconhecido; nenhuma resposta divulga senha/hash.

### P1: Usar os controles do CRUD na interface

**User Story:** como usuário, quero entender quais quadros posso administrar e ver o resultado das operações sem perder informações digitadas.

**Why P1:** uma API sem interface não resolve o primeiro requisito solicitado.

**Acceptance Criteria**:
1. WHEN o usuário abre a lista de gerenciamentos THEN a interface SHALL mostrar o filtro Ativos inicialmente, permitindo alternar para Arquivados e preservando a seleção ao abrir um quadro e voltar. (GER-28)
2. WHILE o usuário consulta um quadro arquivado a interface SHALL exibir `Arquivado — somente consulta` e impedir controles de alteração de conteúdo. (GER-29)
3. WHILE o usuário visualiza os quadros a interface SHALL oferecer Editar/Arquivar em ativos e Restaurar em arquivados somente quando o usuário puder administrar o quadro. (GER-30)
4. WHEN o usuário cancela edição ou a confirmação de arquivamento THEN a interface SHALL retornar sem enviar requisição de mutação, devolvendo o foco ao controle de origem quando fechar a confirmação. (GER-31)
5. WHILE uma operação do CRUD está em envio a interface SHALL impedir um segundo envio da mesma operação por cliques adicionais. (GER-32)
6. IF a mutação falha ou a comunicação não confirma seu resultado THEN a interface SHALL mostrar o erro correspondente preservando os campos digitados, sem anunciar sucesso nem repetir criação automaticamente. (GER-33)
7. IF uma mutação foi confirmada pela API mas a recarga da lista falha THEN a interface SHALL informar `Alteração salva; não foi possível atualizar a lista.` e oferecer repetir somente a consulta. (GER-34)
8. WHEN uma mutação é confirmada e a consulta posterior termina com sucesso THEN a interface SHALL apresentar os dados e o estado persistidos também após recarregar a página. (GER-35)
9. The interface SHALL distinguir listagem em carregamento, lista vazia e falha de consulta, oferecendo repetir somente a consulta em caso de falha. (GER-36)
10. WHEN o usuário solicita arquivamento THEN a interface SHALL mostrar confirmação com o nome do quadro, a informação de preservação dos dados e ações Cancelar / Arquivar. (GER-37)
11. The interface SHALL permitir operar todos os controles deste CRUD pelo teclado. (GER-38)
12. The interface SHALL associar cada campo do CRUD a um rótulo identificável por tecnologia assistiva. (GER-39)
13. WHEN um erro do CRUD é exibido THEN a interface SHALL anunciá-lo por uma região de alerta acessível. (GER-40)
14. WHEN arquivamento ou restauração é confirmado THEN a interface SHALL retirar o cartão do filtro que ele deixou de atender, mantendo o filtro selecionado e exibindo estado vazio se não restarem cartões. (GER-41)

**Independent Test:** criar → editar → arquivar → consultar → restaurar pela interface; cancelar formulários/confirmação; simular duplo clique, falha da mutação e falha só da recarga; conferir botões para criador, terceiro e administrador.

## Contratos para implementação e teste

- Criação de quadro: HTTP 201; consultas/edição: HTTP 200; arquivar/restaurar: HTTP 204, sem corpo obrigatório.
- Recursos consultados informam ID, nome, descrição, criador identificável quando conhecido, estado e versão. A interface precisa conhecer a possibilidade de administração; o servidor continua responsável por autorizá-la.
- Nome é normalizado removendo whitespace externo conforme a semântica de `String.trim()` do JavaScript; os limites 1/120 e 255 são contados em unidades UTF-16, como o comprimento das strings Java/JavaScript atuais. Valores legados não são truncados; acentos são preservados.
- Nome vazio/nulo/só espaços e limites excedidos retornam 400. Descrição nula/ausente equivale a vazia. Não converter nomes iguais em erro ou em compartilhamento de conteúdo.
- A lista de etapas iniciais é opcional. Quando informada, mantém os campos existentes: nome e setor não vazios com até 255 caracteres, e ordem inteira positiva. Dados inválidos retornam 400; falha de persistência retorna 500. Ambas invalidam a criação inteira. Novas regras de reordenação pertencem ao CRUD de etapas.
- Edição/arquivamento/restauração aplicáveis requerem versão inteira não negativa; ausência ou formato inválido retorna 400. Uma mudança de estado com versão desatualizada retorna 409. Quando o estado solicitado já está aplicado, uma operação autorizada com versão em formato válido retorna 204 sem novo efeito nem incremento de versão, mesmo se a versão estiver desatualizada; não é necessário identificar uma requisição anterior.
- IDs e autoria são determinados pelo servidor. Criação de demanda não pode servir de atalho para atualizar demanda existente: ID recebido nesse POST deve ser recusado com 400, e a etapa deve ser resolvida pelo registro persistido antes de verificar arquivamento.
- Erros de domínio retornam JSON com `erro` e o texto previsto pelos critérios; erro de persistência não retorna sucesso nem detalhes internos. Falhas do banco mantêm a transação sem alterações parciais.
- Falha de conexão apresenta `Não foi possível confirmar a operação. Atualize a lista antes de tentar novamente.`; isso não afirma que o servidor não gravou. Conflito 409 preserva os campos e apresenta o erro da API, sem reenvio automático.
- A permissão administrativa não permite alterar conteúdo arquivado. Para operações administrativas, verificar identidade válida e acesso ao quadro antes da alteração. Guardas de somente consulta usam os registros persistidos, não relações recebidas do cliente.
- Para solicitações com JSON válido, a precedência de acesso é autenticação, existência do recurso, autorização administrativa quando aplicável e estado do quadro. Somente depois avaliar versão e gravação. Assim, terceiro usuário tentando editar quadro arquivado recebe 403; criador/admin tentando editar o mesmo quadro recebe 409. JSON malformado ou formato estrutural impossível retorna 400 quando a autenticação já for válida.
- GER-15 e GER-20 descrevem os efeitos da própria operação. Alterações independentes legitimamente confirmadas em um quadro ainda ativo não são revertidas nem proibidas por uma edição de nome/descrição.
- Dados ativos mantêm as permissões de etapas/demandas já existentes nesta entrega. Novas restrições específicas serão discutidas nos requisitos próprios; o bloqueio por arquivamento já se aplica a todos.

## Matriz de mutações bloqueadas em arquivados

GER-21 deve ser testado com dados de entrada válidos em cada rota. GER-13/14 aplicam-se às ações administrativas de gerenciamento; as demais rotas mantêm autenticação e passam a exigir quadro ativo.

| Operação | Rota existente |
| --- | --- |
| Editar quadro | `PUT /api/gerenciamentos/{id}` |
| Criar etapa | `POST /api/gerenciamentos/{id}/etapas` |
| Editar/reordenar etapa | `PUT /api/gerenciamentos/{id}/etapas/{etapaId}` |
| Excluir etapa | `DELETE /api/gerenciamentos/{id}/etapas/{etapaId}` |
| Criar demanda | `POST /api/processos` |
| Editar demanda | `PUT /api/processos/{id}` |
| Mover demanda | `PUT /api/processos/{id}/etapa/{etapaId}` |
| Concluir demanda | `PUT /api/processos/{id}/concluir` |
| Cancelar demanda | `PUT /api/processos/{id}/cancelar` |
| Excluir demanda | `DELETE /api/processos/{id}` |
| Adicionar comentário | `POST /api/processos/{id}/comentarios` |
| Adicionar histórico manual | `POST /api/processos/{id}/historico` |

Restaurar, repetir arquivamento já aplicado e ler dados não são alterações de conteúdo bloqueadas. Registros automáticos de ações administrativas não podem ser bloqueados indiscriminadamente quando o requisito de auditoria for implementado.

## Edge Cases

- Nome com 1/120 caracteres e descrição com 0/255 devem ser aceitos; nome com 0/121 e descrição com 256 devem ser recusados.
- Quadros homônimos continuam independentes; consultar/editar por ID nunca afeta o outro.
- Criador nulo nunca causa exceção de referência nula nem concede administração a um funcionário.
- Arquivar um quadro vazio é permitido; arquivar um quadro com conteúdo não apaga seus registros.
- Operação negada por autenticação, permissão, estado, versão ou validação deixa os dados inalterados.
- Cancelar a confirmação ou sair de um formulário não equivale a cancelar o gerenciamento nem suas demandas.
- Falha de rede após envio não comprova que o servidor não salvou; preservar campos e orientar consulta antes de novo envio manual.
- As consultas permanecem permitidas com quadro arquivado, inclusive comentários e histórico já existentes.
- O estado arquivado persiste após reinicialização. Valores legados sem autoria não recebem proprietário por inferência.

## Implicit-Requirement Dimensions

| Dimensão | Resolução neste escopo |
| --- | --- |
| Input validation & bounds | GER-03/04/06/07 e contratos definem formatos, limites e campos opcionais; limites não truncam dados antigos. |
| Failure / partial-failure states | GER-07/20/33/34 e transações: criação sem sucesso parcial; erro de recarga não vira reenvio de mutação. |
| Idempotency / retry / duplicate handling | GER-05/19/32/33: nomes iguais permitidos, estados repetidos sem efeito, duplo envio bloqueado, POST sem repetição automática. Deduplicação de POST por chave N/A porque esta entrega não promete retries automáticos de criação. |
| Auth boundaries & rate limits | GER-02/13/14/23–27 e cadastro sem papel/ID escolhidos pelo cliente. Novo rate limit N/A porque a entrega não redefine políticas de login/cadastro nem adiciona serviço externo; será avaliado no requisito de autenticação. |
| Concurrency / ordering | GER-16/22: conflito em edição antiga e ordenação entre mutação de conteúdo e arquivamento; implementação escolhe mecanismo de transação/bloqueio. |
| Data lifecycle / expiry | GER-17–21/35: arquivamento reversível, preservação e consulta; expiração automática N/A porque o usuário pediu conservação dos registros. |
| Observability | Preservação de histórico GER-20; estado e versão observáveis nas consultas. Novo painel/log completo N/A nesta entrega porque é requisito separado ainda pendente, não requisito omitido. |
| External-dependency failure | Erro de API/banco coberto por GER-07/33/34 e rollback; serviços externos/circuit breaker N/A porque o CRUD só depende da API própria e do banco. |
| State-transition integrity | GER-17–22 e matriz bloqueiam alteração de conteúdo após arquivar e permitem restauração controlada. |

## Requirement Traceability

| Requirement ID | Story | Phase | Status |
| --- | --- | --- | --- |
| GER-01 | Criar com autoria | Execute | T4 verificado; demais tarefas/Verifier pendentes |
| GER-02 | Criar com autoria | Execute | T4 verificado; demais tarefas/Verifier pendentes |
| GER-03 | Criar com autoria | Execute | T4 verificado; demais tarefas/Verifier pendentes |
| GER-04 | Criar com autoria | Execute | T4 verificado; demais tarefas/Verifier pendentes |
| GER-05 | Criar com autoria | Execute | T4 verificado; demais tarefas/Verifier pendentes |
| GER-06 | Criar com autoria | Execute | T4 verificado; demais tarefas/Verifier pendentes |
| GER-07 | Criar com autoria | Execute | T4 verificado; demais tarefas/Verifier pendentes |
| GER-08 | Consultar | Execute | T4 verificado; demais tarefas/Verifier pendentes |
| GER-09 | Consultar | Execute | T4 verificado; demais tarefas/Verifier pendentes |
| GER-10 | Consultar | Execute | T4 verificado; demais tarefas/Verifier pendentes |
| GER-11 | Consultar | Execute | T4 verificado; demais tarefas/Verifier pendentes |
| GER-12 | Editar | Execute | T4 verificado; demais tarefas/Verifier pendentes |
| GER-13 | Editar | Execute | T4 verificado; demais tarefas/Verifier pendentes |
| GER-14 | Editar | Execute | T4 verificado; demais tarefas/Verifier pendentes |
| GER-15 | Editar | Execute | T4 verificado; demais tarefas/Verifier pendentes |
| GER-16 | Editar | Execute | T4 verificado; demais tarefas/Verifier pendentes |
| GER-17 | Arquivar/restaurar | Execute | T4 verificado; demais tarefas/Verifier pendentes |
| GER-18 | Arquivar/restaurar | Execute | T4 verificado; demais tarefas/Verifier pendentes |
| GER-19 | Arquivar/restaurar | Execute | T4 verificado; demais tarefas/Verifier pendentes |
| GER-20 | Arquivar/restaurar | Execute | T4 verificado; demais tarefas/Verifier pendentes |
| GER-21 | Arquivar/restaurar | Execute | T5 verificado; demais tarefas/Verifier pendentes |
| GER-22 | Arquivar/restaurar | Execute | T5 verificado; demais tarefas/Verifier pendentes |
| GER-23 | Identidade/permissões | Execute | T4 verificado; demais tarefas/Verifier pendentes |
| GER-24 | Identidade/permissões | Execute | T2 verificado; demais tarefas/Verifier pendentes |
| GER-25 | Identidade/permissões | Execute | T2 verificado; demais tarefas/Verifier pendentes |
| GER-26 | Identidade/permissões | Execute | T4 verificado; demais tarefas/Verifier pendentes |
| GER-27 | Identidade/permissões | Execute | T4 verificado; demais tarefas/Verifier pendentes |
| GER-28 | Interface | Specify | Pending |
| GER-29 | Interface | Specify | Pending |
| GER-30 | Interface | Specify | Pending |
| GER-31 | Interface | Specify | Pending |
| GER-32 | Interface | Specify | Pending |
| GER-33 | Interface | Specify | Pending |
| GER-34 | Interface | Specify | Pending |
| GER-35 | Interface | Specify | Pending |
| GER-36 | Interface | Specify | Pending |
| GER-37 | Interface | Specify | Pending |
| GER-38 | Interface | Specify | Pending |
| GER-39 | Interface | Specify | Pending |
| GER-40 | Interface | Specify | Pending |
| GER-41 | Interface | Specify | Pending |

**Coverage:** 41 requisitos; nenhum implementado nesta fase; tarefas e matriz de testes serão produzidas após a revisão da especificação.

## Success Criteria

- [ ] Fluxo criar → editar → arquivar → consultar → restaurar concluído pela interface e confirmado após nova consulta/recarregamento.
- [ ] Criador, terceiro usuário, administrador e quadro sem criador produzem os resultados da matriz de permissões.
- [ ] Todos os registros associados permanecem intactos ao arquivar/restaurar.
- [ ] Todas as rotas da matriz recusam conteúdo válido enquanto arquivado, sem gravação.
- [ ] Falha parcial, concorrência e tentativas de falsificar autoria/papel são cobertas por testes de resultado.
- [ ] Testes do requisito passam em ambiente isolado do MySQL configurado; limitações entre banco de teste e MySQL são registradas quando existirem.
- [ ] Verificador independente apresenta evidências por critério e resultado do sensor de discriminação conforme a skill.

## Review Gate

A revisão confirma os padrões explicitamente propostos, a divisão entre este CRUD e os próximos requisitos e o comportamento testável acima. Não equivale à conclusão da implementação. As mudanças de código, os testes executados e as evidências serão registrados nas fases seguintes.

Conferência estrutural executada em 2026-10-04: `python .agents/skills/tlc-spec-driven/scripts/validate_spec.py .specs/features/gerenciamentos/spec.md --strict`, resultado zero erros e zero avisos. Duas revisões independentes de leitura apontaram precisões de versão/repetição, precedência de permissões, concorrência e interface; as correções foram incorporadas. Nenhum teste de comportamento da aplicação foi executado nesta fase.
