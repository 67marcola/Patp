# Etapas finais: contexto

**Data:** 2026-10-06. **Status:** regras deste corte definidas; implementação local autorizada pela sessão, um requisito por vez com testes.

## Feature Boundary

Criar e proteger as colunas oficiais Concluídos e Cancelados, apresentá-las no quadro e preparar correção explícita dos dados antigos. Os botões e o contrato das ações sobre demandas são a próxima entrega.

## Implementation Decisions

- AD-011: exatamente dois destinos finais, automáticos conforme concluir/cancelar.
- AD-016: preparação dos legados alcança ativos, arquivados e quadros sem criador, preservando registros; não equivale à execução em banco real.
- AD-005 continua para ações normais: arquivados somente consulta. Correção de legado é manutenção explícita.
- A autorização geral de implementação/testes/commits locais já foi dada. Usar tlc-spec-driven, Java/Spring Boot/MySQL e os ambientes de teste isolados existentes.

## Defaults técnicos declarados

- Categoria explícita TRABALHO, CONCLUIDA, CANCELADA; nomes não determinam categoria.
- Nomes oficiais fixos, Setor nulo e posição final; finais sem edição, remoção ou reposicionamento. Etapas de trabalho mantêm Setor obrigatório e posição entre trabalhos.
- Legado homônimo permanece trabalho; a coluna oficial nova é distinguida pelo texto de etapa final na interface. Não renomear nem converter etapas antigas pelo nome.
- Reconhecer na correção apenas os status exatos produzidos hoje: Concluido e Cancelado. Inventariar os demais valores; preservar sem interpretar variantes ou campos contraditórios. Datas/motivos ausentes permanecem ausentes.
- Preparação oferece plano sem gravação e aplicação explícita idempotente. Não executar durante GET ou na inicialização normal. O código é testado com dados fictícios; aplicação real fica para operação posterior.
- Atualizar expectativas de contagem/lista antigas afetadas pelas duas colunas escolhidas pelo usuário, acrescentando assertions exatas das finais. Preservar todos os cenários/asserções de trabalho, permissão, rollback e arquivo; não eliminar/skipping testes.

## Deferred Ideas

- AD-012–015: criador/admin nas transições, reabrir, voltar/pular qualquer trabalho, justificativa obrigatória. Não alterar ProcessoService nem declarar esses fluxos prontos neste corte.
- CRUD e campos/permissões de demandas, botão Criar processo, comentários, logs e gráficos.
- Contas administrativas reais, execução no banco configurado, push/deploy e UAT humano.
