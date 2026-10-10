# Comentários por demanda Specification

**Data:** 10/10/2026. **Status:** proposta concreta para confirmação. **Base:** 8584e5b, branch testes. Alcance e participantes confirmados pelo usuário; defaults e plano abaixo aguardam revisão. Complexidade média: reutilizar a persistência, sessões, lock e padrões de interface existentes, sem nova tecnologia ou alteração de schema. Design separado dispensado; decisões técnicas descritas abaixo. Implementação ainda não iniciada.

## Problem Statement

Os comentários já existem no servidor, mas os usuários não conseguem conversar nos cartões pela interface. O nome do autor ainda é fornecido pelo navegador. Entregar uma lateral por demanda, com participação dos usuários autenticados e autoria confiável, conservando as mensagens anteriores.

## Goals

- [ ] Consultar e enviar comentários da demanda pelo cartão, com autor e horário.
- [ ] Preservar dados anteriores, estado da demanda e arquivo somente para consulta.
- [ ] Provar critérios, falhas e concorrência com testes e Verifier independente; incluir conclusão no diagnóstico.

## Out of Scope

| Feature | Reason |
| --- | --- |
| Conversa geral por quadro | Usuário escolheu por demanda. |
| Editar/apagar mensagens, anexos e notificações | O requisito pede consulta e envio de comentários com data. |
| Atualização em tempo real, polling e paginação | Consulta ao abrir e botão Atualizar comentários bastam para este corte; evolução separada quando necessária. |
| Garantia de deduplicação entre requisições POST distintas | Sem chave de idempotência neste corte. Bloquear duplo clique e nunca reenviar automaticamente; resultado incerto exige consulta e conferência humana. |
| Reatribuir autoria antiga, migração real, deploy e upgrade | Preservar registros e tecnologias AD-024; nenhuma operação real autorizada. |
| Painel completo de atividade e gráficos | Próximos requisitos próprios do diagnóstico. |

## Assumptions & Open Questions

| Assumption / decision | Chosen default | Rationale | Confirmed? |
| --- | --- | --- | --- |
| Alcance | Por demanda. | Escolha explícita do usuário nesta conversa. | Sim |
| Participantes | Qualquer usuário autenticado consulta/envia em quadro e demanda ativos. | Escolha explícita do usuário; participação de vários funcionários. | Sim |
| Encerradas e status antigos | Permitir comentários sem mudar estado; apenas arquivo bloqueia envio. | Conversar não move nem corrige o registro; conservar política atual do serviço. | Proposta explícita |
| Texto novo | Obrigatório, não composto somente de whitespace conforme Java String.isBlank, máximo 10.000 unidades UTF-16; preservar espaços e quebras. | Evitar mensagens vazias e limitar o envio, mantendo literalidade e compatibilidade com TEXT no MySQL. | Proposta explícita |
| Autoria e relógio | Nome da conta resolvida pela sessão na gravação; LocalDateTime do servidor; nome histórico permanece literal. | Aproveitar sessões e campo existente; não inventar identidade retroativa. | Recomendação apresentada nas perguntas respondidas |
| Interface | Lateral não modal; consulta inicial e atualização manual; fechar descarta o rascunho da conversa. | Permitir trocar cartões; não manter texto na demanda errada. | Proposta explícita |
| Repetição | Bloqueio enquanto pendente, sem retry automático; após falha consultar manualmente e conferir mensagens antes de novo envio. | POST pode ter sido salvo mesmo quando a resposta se perde. Não prometer deduplicação sem chave persistida. | Proposta explícita |
| Dados antigos | Ler mesmo textos/nome antigos fora dos limites novos; nenhuma limpeza ou truncamento. | Limites são de novos envios e não autorizam alterar registros existentes. | Política de preservação vigente |

**Open questions:** none — all resolved or logged above. Defaults estão apresentados para confirmação; não são respostas explícitas do usuário. Adequações de expectativas antigas descritas em tasks.md aguardam confirmação TLC.

## Contrato e implementação proposta

Reutilizar Comentario, SessaoService, GerenciamentoGuard e requisição autenticada do frontend. Sem coluna nova. O nome continua guardado como snapshot em `funcionario`.

- GET novo `/api/gerenciamentos/{gerenciamentoId}/demandas/{processoId}/comentarios`: HTTP200, `{gerenciamentoId,processoId,quadroArquivado,demandaArquivada,comentarios}`. Os dois flags são booleanos. Cada comentário tem exatamente `{id,texto,funcionario,dataHora,processoId}`, sem entidade aninhada. Ordenar por dataHora ASC, id ASC. IDs positivos; dataHora ISO local com segundos e fração opcional.
- POST novo na mesma rota: objeto com apenas `texto` string; HTTP201 com os seis campos do comentário salvo. Nome, ID, vínculo e horário não são recebidos do cliente. JSON inválido/tipo/campo extra: HTTP400, `Dados da requisição inválidos.`.
- Rotas antigas `/api/processos/{processoId}/comentarios`: manter HTTP200 e shapes anteriores. POST continua aceitando o campo antigo funcionario, mas ignora seu valor e usa a sessão. Validar texto novo; GET de demanda inexistente passa a HTTP404, `Demanda não encontrada.`. Ordenar também por dataHora/ID.
- Texto inválido no domínio: HTTP400, `Comentário deve conter texto e ter no máximo 10000 caracteres.`. Ausência de quadro: HTTP404, `Gerenciamento não encontrado.`. Demanda ausente ou de outro quadro: HTTP404, `Demanda não encontrada.`.
- Quadro arquivado no envio: HTTP409, `Gerenciamento arquivado. Restaure-o antes de alterar.`. Demanda arquivada: HTTP409, `Demanda arquivada. Restaure-a antes de alterar.`. API segue autenticação global: ausência HTTP401 `Usuário não autenticado.`; token inválido HTTP401 `Sessão inválida ou expirada.`.
- Envios usam transação READ_COMMITTED e o lock existente do quadro, compartilhado com arquivo/restauração. A consulta nova também obtém o lock para produzir flags e lista coerentes na mesma transação; não grava nem incrementa versão. Na rota antiga, verificar existência e preservar contrato de consulta.
- Frontend consulta somente ao abrir/atualizar. POST confirmado acrescenta o comentário sem fechar a lateral e limpa o campo. Erro conserva o rascunho, bloqueia novo envio até GET manual válido e mostra `Confira as mensagens antes de enviar novamente; o comentário pode já ter sido salvo.`. GET não reenvia nem apaga o rascunho.
- Confirmação no frontend: GET exige HTTP200, exatamente os cinco campos do envelope, IDs iguais aos solicitados, flags booleanos e array ordenado dataHora/ID sem IDs repetidos. Cada registro exige exatamente os seis campos do contrato, IDs inteiros positivos seguros, processoId solicitado, texto/funcionario strings e dataHora ISO local válida (calendário e horário válidos; fração opcional de1–9 dígitos). Não aplicar limites novos aos valores antigos consultados. POST exige HTTP201, os seis campos válidos, texto exatamente igual ao enviado, nome não vazio e ID ausente da lista já carregada. Não validar nome contra cache de login: a fonte da autoria é a sessão no servidor. Comparar frações preenchidas até9 dígitos para preservar ordenação sem perda de precisão.
- Formatação do horário local do servidor: `dd/MM/yyyy HH:mm:ss`, sem conversão automática de fuso; `<time dateTime>` conserva a string ISO. Texto simples escapado por React, com quebras de linha visíveis.
- O painel é um aside identificado pela demanda. Botão Fechar comentários e Escape fecham quando não houver envio pendente, devolvendo foco ao cartão. Rascunho não persiste entre fechar/trocar demanda ou quadro. Envios bloqueiam alterações do quadro, saída, troca/fechamento e duplo clique até resposta; o painel não aplica resposta atrasada após desmontagem/troca.

## User Stories

### P1: Enviar mensagem com autoria confiável

**User Story:** Como funcionário conectado, quero registrar uma mensagem na demanda para que colegas acompanhem o assunto e saibam quem comentou e quando.

**Acceptance Criteria:**

1. IF a sessão estiver ausente ou inválida THEN o sistema SHALL recusar GET/POST novos e antigos com HTTP401 e a mensagem de autenticação definida no contrato, sem efeitos persistidos. **COM-01**
2. WHEN um funcionário comum, criador ou administrador enviar texto válido em quadro/demanda ativos THEN o sistema SHALL aceitar o comentário, inclusive quando o quadro não tiver criador. **COM-02**
3. WHEN o POST antigo receber funcionario diferente do nome da conta conectada THEN o sistema SHALL persistir e devolver o nome da sessão, ignorando o autor informado pelo cliente. **COM-03**
4. IF texto novo for ausente, nulo, vazio, somente whitespace ou exceder 10000 unidades UTF-16 THEN o sistema SHALL recusar o envio com HTTP400 e a mensagem de texto definida no contrato, sem gravação. **COM-04**
5. IF o POST novo receber JSON inválido, valor não string para texto ou qualquer outro campo THEN o sistema SHALL responder HTTP400 com `Dados da requisição inválidos.`, sem gravação. **COM-05**
6. IF o quadro não existir, a demanda não existir ou a demanda não pertencer ao quadro da rota nova THEN o sistema SHALL responder HTTP404 com a mensagem correspondente do contrato e não revelar/gravar comentários de outro quadro. **COM-06**
7. WHILE o quadro estiver arquivado o sistema SHALL recusar envio novo/antigo com HTTP409 e a mensagem de quadro arquivado definida no contrato, sem efeitos. **COM-07**
8. WHILE a demanda estiver arquivada em quadro ativo o sistema SHALL recusar envio novo/antigo com HTTP409 e a mensagem de demanda arquivada definida no contrato, sem efeitos. **COM-08**
9. WHEN uma demanda não arquivada tiver estado Em andamento, Concluido, Cancelado ou status antigo desconhecido THEN o sistema SHALL permitir o comentário em quadro ativo sem normalizar o status. **COM-09**
10. WHEN um envio for aceito THEN o sistema SHALL salvar exatamente uma mensagem com ID gerado, texto literal, nome da sessão, dataHora entre início/fim da operação e processoId da demanda correta, devolvendo os seis valores no POST novo HTTP201 e o contrato antigo HTTP200. **COM-10**
11. The sistema SHALL conservar os valores dos comentários anteriores, inclusive nomes e textos antigos fora dos limites novos, após consulta, novo envio, arquivo e restauração. **COM-11**
12. The sistema SHALL manter os 14 campos da demanda, flags de arquivo, etapa, versão do quadro, contagens e histórico inalterados ao comentar. **COM-12**
13. IF ocorrer falha na gravação do comentário THEN o sistema SHALL reverter a transação sem inserir comentário ou alterar registros associados. **COM-13**
14. WHEN envio disputar o lock com arquivamento/restauração do quadro ou demanda THEN o sistema SHALL aguardar a operação anterior e decidir pela situação confirmada, preservando os efeitos somente da operação permitida. **COM-14**

**Independent Test:** POST pelas duas rotas com contas distintas, conferir banco e resposta; provar falhas, estado e concorrência em H2 e MySQL fictícios.

### P1: Consultar a conversa da demanda

**User Story:** Como usuário conectado, quero abrir as mensagens de um cartão para consultar seu histórico de conversa, inclusive depois de arquivar.

**Acceptance Criteria:**

1. WHEN GET novo consultar demanda existente THEN o sistema SHALL retornar HTTP200 com os dois IDs, flags reais e todas as mensagens exclusivamente dessa demanda em ordem dataHora/ID, incluindo lista vazia e conteúdo arquivado. **COM-15**
2. The API nova SHALL retornar somente os campos do contrato, sem entidades aninhadas, senha, hash ou token em suas respostas de comentários. **COM-16**
3. WHEN o quadro carregar seus cartões ativos ou arquivados THEN a interface SHALL oferecer Comentários a todos os usuários conectados, inclusive em demandas encerradas ou de status desconhecido. **COM-17**
4. WHEN o usuário abrir a lateral THEN a interface SHALL identificar número/cliente da demanda e mostrar Carregando comentários, a lista ou Nenhum comentário nesta demanda conforme a consulta, com erro e Tentar novamente quando o GET falhar. **COM-18**
5. WHEN mensagens forem exibidas THEN a interface SHALL mostrar texto literal com quebras, autor e data/hora no formato definido, sem executar HTML recebido. **COM-19**
6. WHILE quadro ou demanda estiver arquivado segundo a consulta da conversa a interface SHALL exibir somente consulta com o motivo e não oferecer envio. **COM-20**

**Independent Test:** Ler mensagens antigas e novas nas duas contas, verificar isolamento por demanda, arquivo, vazio e falha de consulta.

### P1: Enviar e navegar sem aplicar um resultado incerto

**User Story:** Como usuário, quero enviar uma vez e receber confirmação, conservando meu texto quando ocorrer falha.

**Acceptance Criteria:**

1. IF o rascunho for vazio/whitespace ou maior que 10000 unidades UTF-16 THEN a interface SHALL impedir POST, preservar o campo e informar o limite. **COM-21**
2. WHILE POST estiver pendente a interface SHALL permitir somente uma requisição e desabilitar campo, envio, fechar/trocar conversa, alterações do quadro, Voltar e Sair até conclusão. **COM-22**
3. WHEN POST retornar HTTP201 e comentário confirmado pelo contrato THEN a interface SHALL incluir os seis valores confirmados uma vez na conversa e limpar o rascunho, conservando a lateral aberta. **COM-23**
4. IF POST falhar por HTTP, rede, JSON ou resposta não confirmada THEN a interface SHALL conservar rascunho/lista, mostrar erro e aviso de possível gravação e bloquear novo envio até GET manual válido, sem retry automático. **COM-24**
5. WHEN o usuário atualizar comentários após falha de envio THEN a interface SHALL substituir a lista/flags pela consulta confirmada e conservar o rascunho, sem POST automático e com orientação para conferir mensagens antes de enviar de novo. **COM-25**
6. WHEN o usuário fechar por botão ou Escape sem envio pendente THEN a interface SHALL descartar rascunho, fechar a lateral e devolver foco ao botão Comentários de origem, ou Voltar se o botão já não existir. **COM-26**
7. WHEN mudar demanda/quadro ou desmontar a tela THEN a interface SHALL descartar a conversa anterior e ignorar sua resposta atrasada, sem mensagens, erros ou rascunho no novo destino. **COM-27**
8. WHEN a conversa for utilizada por teclado em desktop e celular THEN a interface SHALL permitir abrir, ler, escrever, enviar, atualizar e fechar, sem painel fora da viewport ou rolagem horizontal causada pelo painel. **COM-28**

**Independent Test:** Vitest para confirmação e erros controlados; Playwright/Edge com duas contas, recarga e capturas 1280x800 e 390x844.

## Edge Cases

- WHEN texto novo tiver exatamente 10000 unidades UTF-16 THEN o sistema SHALL aceitá-lo literalmente; com 10001 SHALL aplicar COM-04. **COM-04**
- WHEN mensagens tiverem dataHora igual THEN o sistema SHALL desempatar por ID crescente, sem perder nenhuma mensagem. **COM-15**
- IF uma mensagem nova contiver HTML, espaços e quebras THEN a interface SHALL mostrar seu conteúdo como texto, conforme COM-19.
- IF resposta de GET tiver ID de outra conversa, flags ausentes, IDs de comentários repetidos ou ordem inválida THEN a interface SHALL tratar como falha de consulta e não aplicar a lista, conforme COM-18.
- IF resposta de POST tiver status diferente de201, campo ausente/tipo inválido, vínculo/texto divergente ou ID já listado THEN a interface SHALL tratar como resposta não confirmada, conforme COM-24.
- WHEN outro usuário arquivar entre GET e POST THEN o sistema SHALL aplicar COM-07/08 e a interface SHALL descobrir o arquivo pelo GET manual de COM-25.
- IF um GET manual falhar após POST incerto THEN a interface SHALL continuar bloqueando envio e conservar o rascunho, conforme COM-24/25.

## Implicit Requirements Sweep

| Dimension | Resultado no escopo |
| --- | --- |
| Input validation & bounds | COM-04/05/19/21; limite só para mensagens novas, preservação COM-11. |
| Failure / partial-failure states | COM-13/18/24/25, rollback e falhas de leitura/confirmação. |
| Idempotency / retry / duplicate handling | COM-22/24/25; sem deduplicação de POSTs distintos, explicitamente fora do corte. |
| Auth boundaries & rate limits | COM-01/02/03/16; sem novo limite por tempo, pois não há requisito de frequência. |
| Concurrency / ordering | COM-14/15/27; locks existentes, ordem dataHora/ID e descarte de respostas. |
| Data lifecycle / expiry | COM-07/08/11/20; preservação, sem expiração/remoção de comentários. |
| Observability | Mensagens persistidas com autor/data/ID em COM-10; sem evento duplicado no histórico COM-12. Logs completos são requisito separado. |
| External-dependency failure | COM-13/24 cobre banco/rede; N/A para serviços externos porque nenhum é introduzido. |
| State-transition integrity | COM-07/08/09/12/14; comentar não altera ciclo, arquivo ou versão. |

## Requirement Traceability

| Requirement ID | Story | Phase | Status |
| --- | --- | --- | --- |
| COM-01 | Enviar | T1/T2/T3 | Pending |
| COM-02 | Enviar | T1/T3 | Pending |
| COM-03 | Enviar | T1 | Pending |
| COM-04 | Enviar | T1/T3 | Pending |
| COM-05 | Enviar | T3 | Pending |
| COM-06 | Enviar | T2/T3 | Pending |
| COM-07 | Enviar | T1/T3 | Pending |
| COM-08 | Enviar | T1/T3 | Pending |
| COM-09 | Enviar | T1/T3 | Pending |
| COM-10 | Enviar | T1/T3 | Pending |
| COM-11 | Enviar | T1/T2/T7 | Pending |
| COM-12 | Enviar | T1/T3/T7 | Pending |
| COM-13 | Enviar | T1/T3 | Pending |
| COM-14 | Enviar | T1/T3 | Pending |
| COM-15 | Consultar | T2 | Pending |
| COM-16 | Consultar | T2/T3 | Pending |
| COM-17 | Consultar | T6 | Pending |
| COM-18 | Consultar | T4/T5 | Pending |
| COM-19 | Consultar | T5/T7 | Pending |
| COM-20 | Consultar | T5/T6/T7 | Pending |
| COM-21 | Navegar | T5 | Pending |
| COM-22 | Navegar | T5/T6/T7 | Pending |
| COM-23 | Navegar | T4/T5 | Pending |
| COM-24 | Navegar | T4/T5 | Pending |
| COM-25 | Navegar | T5/T7 | Pending |
| COM-26 | Navegar | T5/T6 | Pending |
| COM-27 | Navegar | T5/T6 | Pending |
| COM-28 | Navegar | T5/T7 | Pending |

**Coverage:** 28 total, 28 mapped to proposed tasks, 0 unmapped. Nenhum critério implementado/verificado nesta proposta.

## Success Criteria

- [ ] Todos os 28 critérios e bordas com evidências de resultado; nenhum teste ignorado/excluído.
- [ ] Gates Java/H2, Vitest, MySQL fictício selecionado, Edge/H2, lint/build aprovados.
- [ ] Verifier independente PASS, sensor detecta as falhas propostas e confirma árvore real preservada.
- [ ] Diagnóstico registra cada requisito concluído e a comunicação inclui “incluído no diagnóstico”; aceitação humana registrada separadamente.
