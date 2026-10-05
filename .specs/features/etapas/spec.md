# CRUD de etapas de trabalho da Creral Specification

**Status:** implementação local autorizada pelo usuário; regras AD-007–010 confirmadas; defaults abaixo declarados pelo agente.
**Data:** 2026-10-05.
**Contexto:** `context.md`, decisões de `.specs/STATE.md` e contratos do CRUD de gerenciamentos já validado.

## Problem Statement

O quadro mostra colunas, mas não oferece controles de criação/edição/ordenação/remoção de etapas. A API atual permite a qualquer funcionário mudar sua estrutura, aceita ordens com lacunas e não informa claramente a remoção de etapa ocupada. Esta entrega completa o CRUD das etapas de trabalho respeitando as escolhas do usuário e preservando demandas e histórico.

## Goals

- [ ] Criador/admin cria, renomeia, muda setor, ordena e remove etapas vazias pela interface.
- [ ] Demais funcionários consultam; arquivados ficam somente consulta.
- [ ] Posições escolhidas reorganizam automaticamente a estrutura sem perder dados.
- [ ] Remoção ocupada, versões antigas e falhas não causam escrita parcial.
- [ ] Comprovar o contrato com testes isolados e Verificador independente.

## Out of Scope

| Funcionalidade | Encaminhamento |
| --- | --- |
| Etapas obrigatórias Concluídos/Cancelados e destinos extras | Próximo requisito; decisão de destinos ainda pendente. Nome não atribui categoria. |
| CRUD de demandas, botões de pular/finalizar/cancelar, reabertura | Entregas posteriores. Somente concorrência/integridade com remoção de etapa pertence aqui. |
| Comentários laterais, gráficos, painel de logs e login automático | Permanecem no backlog, sem declaração de implementação. |
| Drag and drop e novos filtros | Posição por campo de seleção, operável por teclado; não acrescentar outra interação. |
| Produção/MySQL configurado, contas reais, push/deploy | Esta implementação/testes são locais e isolados; sem executar migração real. |

## Assumptions & Open Questions

| Assumption / decision | Chosen default | Rationale | Confirmed? |
| --- | --- | --- | --- |
| Administração | Criador/admin apenas; autoria nula somente admin | AD-007/006 | Sim |
| Etapa ocupada | Bloquear remoção até mover demandas | AD-008, todas as demandas contam independentemente de status | Sim |
| Ordenação | Escolher posição e reorganizar automaticamente | AD-009 | Sim |
| Setor | Obrigatório, texto informativo | AD-010 | Sim |
| Nomes repetidos | Permitidos por ID; mostrar setor e posição | Mantém liberdade do modelo existente, sem unicidade inventada | Default do agente declarado no contexto |
| Última etapa vazia | Remoção permitida, quadro sem etapas | Não foi solicitado mínimo de etapas comuns | Default do agente |
| Editar etapa ocupada | Permitir sem mudar demandas/status/responsáveis/histórico | A proibição confirmada é de remoção, vínculos são por ID | Default do agente |
| Concorrência | Reusar versão do gerenciamento em alterações estruturais | Evita nova tabela/coluna e sobrescrita de tela antiga; após mudar etapas a versão do quadro também muda | Escolha técnica explicitada |
| Compatibilidade | GET /etapas mantém array; nova consulta de configuração retorna snapshot com DTO de quadro | Preserva leitores atuais e entrega versão/metadados consistentes à UI | Escolha técnica |
| Legado | Consulta conserva campos/ordens antigas; próxima mutação de etapas organiza ordem por ordem antiga e ID | Não inventar autoria/setor nem alterar estrutura por GET | Default explícito |
| Etapas iniciais do quadro | Contrato inicial de nome/setor/ordem positiva do primeiro CRUD permanece | UI já cria ordens consecutivas; evitar alterar outra criação nesta entrega | Limite de escopo explícito |

**Open questions:** none neste CRUD; destinos finais permanecem pendentes e fora do escopo. Autorização de continuar não será registrada como resultado de teste humano.

## User Stories

### P1: Consultar configuração e permissões atuais

**User Story:** como funcionário, quero ver a estrutura e saber se posso configurá-la.
**Why P1:** a interface precisa usar estado/versão/permissões persistidos.
**Acceptance Criteria**:
1. WHEN um usuário autenticado consulta a configuração de quadro existente THEN o sistema SHALL responder 200 com `gerenciamento` no DTO atual e `etapas` contendo ID, nome, setor, ordem e quantidade de demandas por etapa. (ETA-01)
2. WHEN etapas têm ordens antigas empatadas THEN a consulta SHALL retornar ordem determinística por `ordem` e depois ID, sem escrever nem preencher campos desconhecidos. (ETA-02)
3. WHEN o quadro está arquivado ou sem criador THEN a consulta SHALL continuar permitida para qualquer usuário autenticado. (ETA-03)
**Independent Test:** conferir snapshot, contagens reais, legado e consultas antes/depois sem alteração persistida.

### P1: Administrar somente o próprio quadro ou como administrador

**User Story:** como criador/admin, quero configurar o fluxo sem permitir alterações por terceiros.
**Why P1:** AD-007 define quem administra a estrutura.
**Acceptance Criteria**:
1. WHEN criador ou administrador solicita mutação válida em quadro ativo THEN o sistema SHALL permitir criar, editar ou remover etapa vazia. (ETA-04)
2. IF outro funcionário solicita criar/editar/remover etapas THEN o sistema SHALL responder 403 com `Você não tem permissão para administrar este gerenciamento.` sem alteração persistida. (ETA-05)
3. IF não há sessão válida THEN as rotas da configuração e CRUD SHALL responder 401 sem alteração persistida. (ETA-06)
4. IF criador/admin solicita alteração em quadro arquivado THEN o sistema SHALL responder 409 com `Gerenciamento arquivado. Restaure-o antes de alterar.` sem alteração persistida. (ETA-07)
5. IF quadro ou etapa desse quadro não existe THEN o sistema SHALL responder 404 com `Gerenciamento não encontrado.` ou `Etapa não encontrada neste gerenciamento.`, conforme o recurso ausente. (ETA-08)
**Independent Test:** matriz criador/terceiro/admin/legado em POST/PUT/DELETE, consulta e ausência de recurso.

### P1: Criar e editar com posições claras

**User Story:** como responsável pelo quadro, quero definir campos e posição sem renumerar outras colunas manualmente.
**Why P1:** AD-009/010 e o CRUD completo exigem persistência verificável.
**Acceptance Criteria**:
1. IF nome normalizado tem zero ou mais de 255 unidades UTF-16 THEN criação/edição autorizada SHALL responder 400 com `Informe um nome de etapa entre 1 e 255 caracteres.`. (ETA-09)
2. IF setor normalizado tem zero ou mais de 255 unidades UTF-16 THEN criação/edição autorizada SHALL responder 400 com `Informe um setor entre 1 e 255 caracteres.`. (ETA-10)
3. IF posição não é inteiro positivo no intervalo 1..N+1 para criação ou 1..N para edição THEN a mutação SHALL responder 400 sem alteração; formato numérico inválido recebe `Dados da requisição inválidos.` e limite inválido recebe `Escolha uma posição válida para a etapa.`. (ETA-11)
4. WHEN uma etapa válida é criada na posição escolhida THEN o sistema SHALL responder 201 com novo ID e configuração persistida com posições consecutivas 1..N+1. (ETA-12)
5. WHEN uma etapa válida é editada ou reposicionada THEN o sistema SHALL responder 200 com configuração persistida e posições consecutivas 1..N, conservando seus vínculos com demandas e registros históricos. (ETA-13)
6. WHEN duas etapas recebem nome igual válido THEN o sistema SHALL manter IDs e conteúdos independentes, sem unificar as etapas. (ETA-14)
**Independent Test:** inserir no meio/fim, mover nas duas direções, trocar campos de etapa ocupada, verificar lista inteira/IDs/vínculos e limites inclusive Unicode.

### P1: Remover com integridade e sem sobrescrita

**User Story:** como responsável, quero remover colunas vazias sem apagar demandas nem perder alterações recentes.
**Why P1:** AD-008 e mutações concorrentes exigem integridade.
**Acceptance Criteria**:
1. WHEN uma etapa vazia é removida THEN o sistema SHALL responder 200 com configuração restante em posições 1..N-1, apagando somente a etapa escolhida e conservando históricos anteriores. (ETA-15)
2. IF a etapa contém qualquer demanda THEN remover SHALL responder 409 com `Esta etapa possui demandas. Mova-as para outra etapa antes de removê-la.` sem alteração persistida. (ETA-16)
3. IF a versão não é inteira não negativa THEN mutação SHALL responder 400 sem alteração; ausência/negativa recebe `Informe uma versão válida do gerenciamento.` e formato estrutural inválido recebe `Dados da requisição inválidos.`. (ETA-17)
4. IF a versão informada difere da atual THEN mutação autorizada de quadro ativo SHALL responder 409 com `Gerenciamento alterado por outro usuário. Atualize e tente novamente.` sem alteração persistida. (ETA-18)
5. WHEN uma mutação estrutural é confirmada THEN o sistema SHALL incrementar a versão do gerenciamento exatamente uma vez e retornar esse valor na configuração. (ETA-19)
6. IF a gravação falha no banco THEN a mutação SHALL responder 500 com `Não foi possível concluir a operação.` e conservar a configuração, versão e demandas anteriores. (ETA-20)
7. WHEN remover etapa disputa com gravação/movimentação de demanda THEN as transações SHALL impedir etapa removida com demanda persistida referenciando-a; demanda confirmada primeiro bloqueia remoção com 409. (ETA-21)
**Independent Test:** ocupado em diferentes status, última coluna, histórico com nome antigo, versões inválidas/antigas, rollback de reordenação e disputas controladas pelo lock do quadro.

### P1: Configurar etapas pela interface

**User Story:** como responsável, quero realizar o CRUD pelo quadro com mensagens compreensíveis e teclado.
**Why P1:** endpoints sozinhos não cumprem o uso solicitado.
**Acceptance Criteria**:
1. WHILE o snapshot indica quadro ativo administrável a interface SHALL oferecer Nova etapa e Editar/Remover em cada etapa; nos demais estados esses controles ficam ausentes. (ETA-22)
2. WHEN o usuário cria/edita etapa THEN o formulário SHALL oferecer Nome, Setor e Posição rotulados, exibindo valores atuais na edição e posições válidas em seleção. (ETA-23)
3. WHEN remover é solicitado THEN a interface SHALL mostrar confirmação com nome da etapa, aviso de que demandas impedem remoção e ações Cancelar/Remover. (ETA-24)
4. WHEN formulário/confirmação é cancelado THEN a interface SHALL enviar zero mutações e devolver foco ao controle de origem ainda conectado. (ETA-25)
5. WHILE uma mutação está pendente a interface SHALL impedir novo envio e navegação que possa descartar o formulário pendente. (ETA-26)
6. IF mutação falha ou seu resultado não é confirmado THEN a interface SHALL apresentar a mensagem da API/comunicação em alerta, preservar campos e não anunciar sucesso nem repetir gravação automaticamente. (ETA-27)
7. WHEN mutação é confirmada THEN a interface SHALL usar a configuração retornada e atualizar os metadados do quadro na lista, permitindo nova operação com a versão atual e mantendo os dados após recarga. (ETA-28)
8. IF consulta falha THEN a interface SHALL distinguir erro de carregamento/vazio e oferecer repetir somente GET; conflito também permite atualizar a configuração antes de novo envio manual. (ETA-29)
9. The interface SHALL permitir operar todos os controles deste CRUD com Tab/Enter e anunciar erros em região de alerta acessível. (ETA-30)
**Independent Test:** criar/editar/reposicionar/remover, cancelar, duplicidade pendente, erros/rede/409 e atualização de cache; browser real com Tab/Enter e reload.

## Contratos

- Nova consulta: `GET /api/gerenciamentos/{id}/estrutura-etapas`; resposta `{gerenciamento: DTO atual, etapas:[{id,nome,setor,ordem,quantidadeDemandas}]}`.
- `GET /api/gerenciamentos/{id}/etapas` conserva resposta array para leitores atuais, agora 404 quando quadro não existe.
- `POST /{id}/etapas` 201; `PUT /{id}/etapas/{etapaId}` 200; `DELETE /{id}/etapas/{etapaId}` 200. Mutações retornam a configuração inteira. POST/PUT recebem `{nome,setor,ordem,versao}`; DELETE recebe `{versao}` em JSON.
- `versao` é a versão existente do gerenciamento; cada mutação estrutural, mesmo editar para valores iguais, incrementa uma vez. Criação inicial conjunta de quadro continua no contrato do primeiro CRUD. Nenhuma nova coluna/tabela é necessária.
- IDs/quadro/autoria são resolvidos no servidor; campos de ID/quadro recebidos não podem atualizar ou transferir outra etapa. ID de etapa de outro quadro resulta no mesmo 404 de etapa ausente.
- Para payload estrutural válido: autenticação, existência do quadro, administração, estado ativo, existência da etapa quando aplicável, versão, campos/ocupação e escrita. JSON/formato estrutural impossível recebe 400 após autenticação. Em arquivados, terceiro recebe 403 e criador/admin 409.
- Trim de nome/setor acompanha JavaScript `String.trim()`, com NBSP/BOM, e limites são UTF-16. Descrição/nome/autoria/estado do quadro, demandas e históricos não são alterados por este CRUD, salvo sua versão.
- Contagens de demandas incluem todos os status. GETs não normalizam ordens nem inserem etapas finais; próxima mutação estrutura ordens por ordem antiga e ID antes de aplicar posição escolhida.
- Campos desconhecidos de setor legado não são preenchidos por suposição. Consultar/reordenar outras etapas não renomeia nem atribui setor a uma etapa antiga.
- Erro de comunicação usa `Não foi possível confirmar a operação. Atualize a lista antes de tentar novamente.`. Repetir consulta não repete POST/PUT/DELETE. Há nenhum efeito idempotente prometido para DELETE de etapa já ausente.

## Edge Cases

- Nome/setor 1 e 255 aceitos; 0, whitespace e 256 recusados; normalização conserva acentos.
- N=0 aceita criar em posição1; remover última etapa vazia retorna lista vazia; editar tem posição1..N.
- Ordens antigas com empates/lacunas são lidas deterministicamente e reorganizadas apenas em mutação confirmada.
- Etapa vazia com histórico antigo pode ser removida sem apagar histórico; nomes repetidos não misturam vínculos.
- Dados inválidos/recusa/stale/banco falho conservam estado persistido; no rollout local não consultar banco configurado.
- Uma tela antiga de etapas não sobrescreve outra nem a alteração de metadados/arquivamento; voltar do quadro usa versão atual na administração da lista.
- Disputa com arquivamento: arquivamento confirmado primeiro bloqueia etapa; alteração estrutural primeiro torna versão antiga do arquivamento um conflito e exige atualização.

## Implicit-Requirement Dimensions

| Dimensão | Resolução |
| --- | --- |
| Input validation & bounds | ETA-09–11/17 e contratos de normalização/posição/versão. |
| Failure / partial-failure states | ETA-20/27/29; rollback integral, comunicação ambígua, consulta não é reenvio. |
| Idempotency / retry / duplicate handling | ETA-26/27/29; duplicidade bloqueada em UI, sem retry automático de mutação; idempotency key N/A sem promessa de deduplicação de POST. |
| Auth boundaries & rate limits | ETA-04–08/22; política definida; novo rate limit N/A pois não redefine autenticação/serviço externo. |
| Concurrency / ordering | ETA-11–13/18–21, versão do quadro e lock único dos escritores. |
| Data lifecycle / expiry | ETA-15/16; exclusão de etapa vazia, preservação de vínculos; expiração N/A sem política solicitada. |
| Observability | Configuração/versão/contagens e erros visíveis; novo painel/log N/A porque requisito posterior. |
| External-dependency failure | ETA-20/27; API/banco; circuit breakers externos N/A sem serviço externo. |
| State-transition integrity | ETA-07/16/21; arquivado readonly, ocupada não removida, categoria final não inferida por nome. |

## Requirement Traceability

| Requirement ID | Story | Phase | Status |
| --- | --- | --- | --- |
| ETA-01 | Consulta | Tasks | Pending |
| ETA-02 | Consulta | Tasks | Pending |
| ETA-03 | Consulta | Tasks | Pending |
| ETA-04 | Permissões | Tasks | Pending |
| ETA-05 | Permissões | Tasks | Pending |
| ETA-06 | Permissões | Tasks | Pending |
| ETA-07 | Permissões | Tasks | Pending |
| ETA-08 | Permissões | Tasks | Pending |
| ETA-09 | Campos/posições | Tasks | Pending |
| ETA-10 | Campos/posições | Tasks | Pending |
| ETA-11 | Campos/posições | Tasks | Pending |
| ETA-12 | Campos/posições | Tasks | Pending |
| ETA-13 | Campos/posições | Tasks | Pending |
| ETA-14 | Campos/posições | Tasks | Pending |
| ETA-15 | Integridade | Tasks | Pending |
| ETA-16 | Integridade | Tasks | Pending |
| ETA-17 | Integridade | Tasks | Pending |
| ETA-18 | Integridade | Tasks | Pending |
| ETA-19 | Integridade | Tasks | Pending |
| ETA-20 | Integridade | Tasks | Pending |
| ETA-21 | Integridade | Tasks | Pending |
| ETA-22 | Interface | Tasks | Pending |
| ETA-23 | Interface | Tasks | Pending |
| ETA-24 | Interface | Tasks | Pending |
| ETA-25 | Interface | Tasks | Pending |
| ETA-26 | Interface | Tasks | Pending |
| ETA-27 | Interface | Tasks | Pending |
| ETA-28 | Interface | Tasks | Pending |
| ETA-29 | Interface | Tasks | Pending |
| ETA-30 | Interface | Tasks | Pending |

**Coverage:** 30 requisitos, mapeados às cinco tarefas planejadas; nenhum implementado ainda.

## Success Criteria

- [ ] API e UI cumprem matriz de permissões/arquivamento, CRUD e contagens reais.
- [ ] Reordenação/deleção conservam dados e recusas não escrevem parcialmente.
- [ ] Toda operação usa versão atual, stale409 e cache atualizado para ações seguintes.
- [ ] Gates isolados passam sem reduzir cobertura anterior legitimamente aplicável.
- [ ] Verificador novo comprova 30 ACs e sensor expandido; uso humano registrado separadamente.
