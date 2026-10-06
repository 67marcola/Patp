# Cadastro e leitura de demandas — contexto

**Base de implementação:** 28b8c88, branch testes. **Data:** 2026-10-06.
**Status:** regras de produto confirmadas por 1A e 2A, além das autorizações anteriores de implementação/testes. Contrato técnico fixado em spec.md/design.md.

## Entrega delimitada

Conectar o botão Criar processo ao cadastro de uma demanda e mostrar os registros reais em cartões nas etapas do quadro. Depois de salvar, o cartão deve aparecer na primeira etapa de trabalho e permanecer após recarga. Essa entrega cobre criar e consultar; editar, excluir e executar transições continuam em entregas próprias.

Exemplo fictício: no gerenciamento Instalações de postes, cadastrar o serviço solicitado por Maria. A demanda começa em Comprar poste; mais adiante os botões de movimentação permitirão encaminhá-la às demais etapas. O exemplo não define um número real, responsável, prazo ou prioridade.

## Regras já confirmadas pelo usuário

- AD-001: um gerenciamento é um quadro com várias demandas independentes.
- AD-017: todos os usuários autenticados podem criar demandas em quadro ativo.
- AD-018: o servidor escolhe automaticamente a primeira etapa de trabalho. Concluídos e Cancelados não são destinos de cadastro.
- AD-005: quadro arquivado é somente consulta; restaurar antes de cadastrar.
- AD-007: criar etapas é tarefa do criador do quadro ou de um administrador. Um funcionário comum, diante de quadro sem trabalho, precisa receber orientação para solicitar a configuração a essas pessoas.
- AD-012/013: criar uma demanda não concede permissão para movê-la, concluir, cancelar ou reabrir.

## Decisões de cadastro confirmadas

1. **Número identificador:** digitado pelo usuário, único em todo o sistema. Escolha explícita 1A, registrada em AD-019. Não gerar números automaticamente nem reutilizá-los entre quadros.
2. **Campos obrigatórios:** número e cliente/solicitante. Responsável, prioridade, data de emissão, prazo da etapa, prazo geral e observações ficam opcionais. Escolha explícita 2A, registrada em AD-020.

O usuário respondeu literalmente “1A e 2A”. As duas escolhas estão encerradas; não precisam ser perguntadas novamente.

## Defaults técnicos declarados

- Estado inicial Em andamento, definido pelo servidor. ID, etapa, status, datas de encerramento e motivo de cancelamento não serão escolhidos no cadastro.
- Primeira etapa determinada por categoria TRABALHO, ordem e ID; categoria SQL nula conserva a compatibilidade já entregue, sem escrita durante consulta ou inferência pelo nome.
- O formulário usa os componentes e o fluxo de foco/pendência do sistema; erros preservam o rascunho, e envio pendente bloqueia duplicação. Nenhuma repetição automática de POST após resposta incerta.
- Retorno do quadro inclui cartões e contagens do mesmo estado persistido. Uma resposta de configuração de etapas não deve descartar cartões nem combinar etapas novas com demandas de uma consulta antiga.
- Dados antigos são exibidos sem corrigir status, datas ou referências durante GET. Ausências terão indicação visual, sem inventar conteúdo no banco.
- DTO de demanda devolve campos explícitos e etapaId; não exige serializar toda a cadeia de entidades de usuário e gerenciamento.
- Novo cadastro e histórico de criação permanecem na mesma transação. Usar o lock do gerenciamento antes de escolher o trabalho inicial, incluindo as disputas com arquivamento, remoção e reordenação.
- O novo cadastro exige a versão estrutural e a incrementa uma vez. A rota legada permanece sem versão exigida/incremento, mas também recusa escolher uma etapa posterior/final ou um estado final. A versão não passa a representar todas as mutações antigas de demandas. Não mudar indiscriminadamente as políticas ainda abertas de edição/exclusão.
- Número/cliente têm 1..255 unidades UTF-16 após trim; responsável/prioridade opcionais até255, observações até10000. Textos opcionais vazios viram null; datas opcionais ausentes permanecem null, sem default de hoje ou restrição de ordem entre prazos não pedida. Prioridade é texto livre.
- Duplicidade não distingue maiúsculas/minúsculas; a grafia digitada é preservada após trim. A restrição única do banco continua válida, incluindo sua collation própria; nenhuma mudança de schema ou renumeração.
- O diagnóstico de corrida em H2 comprovou que somente precheck+índice VARCHAR não impede duas grafias com caixa diferente em transações de quadros diferentes. Ambos cadastros bloqueiam a primeira linha estável de gerenciamento antes do quadro alvo e mantêm o lock até commit. O quadro global pode estar arquivado/sem criador; ele não sofre validação de administração/ativo nem alterações de dados/versão se não for o alvo. Cadastros entre quadros são serializados, tradeoff técnico aceito para cumprir a unicidade sem mudar schema, collation ou grafia. A ordem fixa global→alvo preserva as demais mutações atuais sobre um único alvo; edição/transições futuras continuam fora do corte.

Esses defaults resolvem detalhes técnicos da entrega e não são atribuídos às respostas de produto. Mensagens, códigos HTTP, resposta e testes estão fixados em spec.md/design.md antes de implementar.

## Evidências do código atual

- frontend/src/pages/Quadro.jsx:140: Criar processo ainda sem ação. :175 mostra contagens reais; não carrega os registros de demandas.
- sistema/src/main/java/com/patp/sistema/controller/ProcessoController.java:32: POST recebe entidade completa.
- sistema/src/main/java/com/patp/sistema/service/ProcessoService.java:43–76: criação já é transacional, verifica quadro ativo, persiste etapa enviada e registra histórico; ainda não escolhe primeira etapa nem aplica defaults de cadastro.
- sistema/src/main/java/com/patp/sistema/model/Processo.java:22–26: número único global e pessoa não nula são regras do schema atual; não substituem a decisão de produto.
- sistema/src/main/java/com/patp/sistema/repository/ProcessoRepository.java:25: consulta por gerenciamento com ordem de ID já disponível.
- sistema/src/main/java/com/patp/sistema/service/EtapaService.java:49–52: configuração lê sob lock do quadro. :152–158 monta somente metadados e etapas.
- sistema/src/main/java/com/patp/sistema/model/Usuario.java:29: senha já usa WRITE_ONLY; não alegar vazamento desse campo ao ler as entidades atuais.

## Verificação planejada

Derivar testes dos critérios que serão fixados: permissão de todos autenticados; ausência/sessão inválida; arquivo; quadro inexistente/sem trabalho; escolha inicial mesmo com lacunas, empates e homônimos; campos/payload sem status ou encerramento escolhido pelo cliente; número e obrigatoriedade conforme a resposta; rollback de demanda/histórico; duplicação; consistência de cartões/contagens; recarga e teclado no navegador. Testar o comportamento novo em H2, MySQL fictício e Edge, sem operar o banco configurado.

Reutilizar a cobertura existente de ProcessoArchiveTests, HistoricoArchiveTests e EtapaDemandConcurrencyTests. Adaptações de criação deverão preservar a proteção de arquivo, a recusa de ID e os testes de rollback/concorrência. Não enfraquecer os cenários de ações ainda fora do corte para fazer os testes passarem.

## Próximas entregas

Movimentação, salto, conclusão, cancelamento e reabertura seguem AD-011–015. Antes de editar/excluir, esclarecer quem pode fazê-lo, como tratar demandas encerradas e se retirar uma demanda de uso preservará comentários e histórico. Comentários, logs do gerenciamento e gráficos terão contratos próprios.
