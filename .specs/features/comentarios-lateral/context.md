# Lateral de comentários — contexto em discussão

**Gathered:** 09/10/2026. **Status:** auditoria inicial concluída; alcance e participantes aguardam resposta. Este documento registra uma proposta, não uma especificação aprovada nem uma entrega implementada.

## Feature Boundary

Entregar o requisito original de uma lateral de comentários de vários usuários com data. A conversa poderá ser por demanda, por quadro ou nos dois espaços, conforme a escolha do usuário. O alcance não foi inferido de “Pode continuar”.

## Base conferida

- `sistema/src/main/java/com/patp/sistema/model/Comentario.java:16`: comentários persistidos com ID, texto, nome, data/hora e vínculo obrigatório com uma demanda. Não existe vínculo de comentário diretamente com quadro.
- `sistema/src/main/java/com/patp/sistema/controller/ComentarioController.java:25`: POST e GET em `/api/processos/{processoId}/comentarios`. O POST recebe o nome do autor no corpo.
- `sistema/src/main/java/com/patp/sistema/config/AutenticacaoInterceptor.java:40`: consulta e envio exigem sessão válida.
- `sistema/src/main/java/com/patp/sistema/service/ComentarioService.java:32`: envio em transação, com bloqueio do quadro, recusa de quadro/demanda arquivados e horário gerado pelo servidor. O nome ainda vem da requisição e o texto não tem validação de vazio/limite.
- `sistema/src/main/java/com/patp/sistema/service/ComentarioService.java:59`: consulta ordenada por data/hora; ainda falta desempate por ID e resposta explícita para demanda inexistente.
- `frontend/src/pages/Quadro.jsx`: cartões ativos e área de arquivadas implementados; ainda não há painel de comentários.

## Decisões já vigentes

- Manter as tecnologias AD-024 e os comentários antigos, sem reatribuir autores retroativamente.
- AD-027 preserva comentários ao arquivar/restaurar; demandas arquivadas permanecem somente para consulta. Quadros arquivados também permanecem somente para consulta.
- Registrar cada novo requisito como concluído no diagnóstico somente após implementação e verificação independente.
- Ritmo guiado: explicar opções e recomendação, no máximo duas decisões independentes por rodada.

## Escolhas pendentes apresentadas ao usuário

| Escolha | Recomendação e motivo | Alternativas |
| --- | --- | --- |
| Alcance da conversa | Por demanda: cada cartão abre as mensagens daquele serviço, aproveitando o vínculo já persistido e evitando misturar assuntos. | Por quadro; por quadro e por demanda. |
| Participantes que enviam | Qualquer usuário autenticado: manter a participação existente em itens ativos, usando o nome da conta conectada e horário do servidor. | Somente criador do quadro ou administrador. |

As duas perguntas foram apresentadas em 09/10 e reapresentadas em 10/10/2026, após o usuário pedir o registro permanente de acompanhamento e autorizar perguntas necessárias ao próximo requisito. Não há resposta registrada; a opção preselecionada pela interface não é confirmação.

Em 10/10/2026, a regra de registrar cada implementação, comunicar “incluído no diagnóstico” e marcar conclusão somente após testes/verificação foi guardada de forma permanente em STATE.md. Isso não muda o alcance nem as permissões de comentários, que continuam pendentes.

## Proposta concreta para o painel

Estes defaults serão incluídos na especificação para revisão depois das escolhas acima:

- Botão Comentários abre uma lateral identificando a conversa, com lista de mensagens, autor, data/hora e campo de texto para envio quando permitido.
- Texto simples, com quebras de linha preservadas e sem interpretar HTML. Comentários antigos continuam com seus dados literais.
- Mensagens em ordem cronológica, com ID como desempate; lista vazia e falha de consulta têm mensagens próprias.
- Autoria de mensagens novas obtida da sessão, sem aceitar que o navegador substitua o nome do autor.
- Consulta disponível também em itens arquivados; envio bloqueado quando a conversa estiver arquivada. Encerramento da demanda, sem arquivamento, não impede a conversa na proposta.
- Evitar envio duplo enquanto houver uma requisição pendente; preservar o rascunho quando houver falha e exigir consulta manual diante de resultado incerto, sem reenviar automaticamente.
- Painel utilizável com teclado, desktop e celular; troca de conversa descarta respostas atrasadas para não mostrar mensagens no item errado.

Limite de texto, contrato de resposta e tratamento de repetição serão definidos com critérios verificáveis na especificação. Ainda não foram implementados.

## Compatibilidade de testes a resolver após a decisão

`ComentarioArchiveTests.java:48–63` usa uma sessão de Outro para gravar Ana/Bia. `DemandaArquivoComentarioTests.java:30–58` espera o nome Informado enviado pelo cliente. Se a nova especificação estabelecer autoria da sessão, será apresentada a adequação concreta dessas expectativas antes de alterá-las, mantendo preservação, consulta e os cenários de concorrência. Nenhum teste foi alterado nesta auditoria.

## Deferred Ideas

Edição/exclusão de comentários, anexos, notificações, conversa em tempo real, gráficos e painel completo de atividade não foram solicitados para este corte. A escolha de conversa por quadro é uma das alternativas pendentes do próprio requisito, não uma decisão já tomada.
