# Lateral de comentários — contexto em discussão

**Gathered:** 09/10/2026; escolhas recebidas em 10/10/2026. **Status:** alcance e participantes confirmados; spec e tarefas validadas estruturalmente (--strict: zero erros/avisos) e apresentadas para confirmação em duas perguntas. Defaults e adequações de testes antigos ainda aguardam resposta. Não há implementação concluída.

## Feature Boundary

Entregar o requisito original de uma lateral de comentários de vários usuários com data, por demanda, com participação de qualquer usuário autenticado. O usuário respondeu explicitamente às duas escolhas em 10/10/2026; a conversa não inclui espaço geral por quadro.

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

## Escolhas confirmadas pelo usuário

| Escolha | Resposta explícita | Efeito |
| --- | --- | --- |
| Alcance da conversa | Por demanda (recomendado). | Cada cartão abre as mensagens daquele serviço; não mistura assuntos de outras demandas. |
| Participantes que enviam | Qualquer usuário autenticado (recomendado). | Conta conectada identifica autoria; horário vem do servidor. Arquivados ficam somente para consulta. |

As duas perguntas foram apresentadas em 09/10 e reapresentadas em 10/10/2026. O usuário respondeu explicitamente às duas recomendações em 10/10. Essas decisões estão encerradas e não devem ser perguntadas de novo.

Em 10/10/2026, a regra de registrar cada implementação, comunicar “incluído no diagnóstico” e marcar conclusão somente após testes/verificação foi guardada de forma permanente em STATE.md.

## Proposta concreta para o painel

Estes defaults estão definidos de forma verificável na spec para revisão:

- Botão Comentários abre uma lateral identificando a conversa, com lista de mensagens, autor, data/hora e campo de texto para envio quando permitido.
- Texto simples, com quebras de linha preservadas e sem interpretar HTML. Comentários antigos continuam com seus dados literais.
- Mensagens em ordem cronológica, com ID como desempate; lista vazia e falha de consulta têm mensagens próprias.
- Autoria de mensagens novas obtida da sessão, sem aceitar que o navegador substitua o nome do autor.
- Consulta disponível também em itens arquivados; envio bloqueado quando a conversa estiver arquivada. Encerramento da demanda, sem arquivamento, não impede a conversa na proposta.
- Evitar envio duplo enquanto houver uma requisição pendente; preservar o rascunho quando houver falha e exigir consulta manual diante de resultado incerto, sem reenviar automaticamente.
- Painel utilizável com teclado, desktop e celular; troca de conversa descarta respostas atrasadas para não mostrar mensagens no item errado.

Limite novo de10000 unidades UTF-16, contratos GET/POST, mensagens e tratamento de repetição estão definidos na spec. Nenhum schema novo ou tecnologia nova; consultas/envio reaproveitam serviço, sessões, lock e modelo atuais. Sete tarefas propostas em tasks.md. Design separado dispensado pela complexidade média e pelos padrões já existentes. Ainda não implementado.

## Compatibilidade de testes a resolver após a decisão

`ComentarioArchiveTests.java:48–63` usa uma sessão de Outro para gravar Ana/Bia. `DemandaArquivoComentarioTests.java:30–58` espera o nome Informado enviado pelo cliente. A nova especificação estabelece autoria da sessão; as adequações concretas estão em tasks.md, mantendo preservação, consulta e concorrência. Há também seis expressões JSX que proíbem qualquer botão ou enumeram ações sem Comentários; a proposta permite a consulta e mantém a lista exata de mutações autorizadas. Confirmação TLC pendente. Nenhum teste foi alterado.

## Deferred Ideas

Edição/exclusão de comentários, anexos, notificações, conversa em tempo real, gráficos e painel completo de atividade fora deste corte. Conversa por quadro não foi escolhida.
