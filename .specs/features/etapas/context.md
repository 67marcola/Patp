# CRUD de etapas: decisões e contexto

**Data:** 2026-10-05.
**Status:** implementação T1–T7 e validação técnica independente concluídas, 30/30 critérios PASS na rodada 2. Teste de uso humano pendente. Defaults menores abaixo são escolhas do agente, sem respostas atribuídas ao usuário. Destinos finais confirmados em AD-011, fora deste escopo.

## Limite desta entrega

Criar, consultar, renomear, mudar setor, ordenar e remover as colunas de trabalho de um gerenciamento existente, pela interface e API. Preservar suas demandas e o histórico. Este requisito segue o CRUD de gerenciamentos já validado tecnicamente.

Etapas obrigatórias de conclusão/cancelamento e os fluxos de finalizar/pular etapas permanecem em requisitos próprios. O nome de uma coluna não deve ganhar significado de conclusão/cancelamento por inferência.

## Decisões confirmadas

- AD-007: somente criador do quadro ou administrador configura etapas de um gerenciamento ativo.
- AD-008: remover etapa com demandas é bloqueado; primeiro elas devem ser movidas para outra etapa.
- AD-005/006: arquivados somente consulta; quadro sem criador é administrado somente por administrador.
- AD-009/010: posição escolhida com reorganização automática e Setor obrigatório, confirmados pelo usuário em 2026-10-05.

## Decisão de outro requisito, recebida durante T1

**AD-011, 2026-10-05:** o usuário respondeu “Usar somente Concluídos e Cancelados, com destino automático conforme a ação”. As duas colunas finais e os fluxos de concluir/cancelar pertencem à próxima entrega. Esta API administra somente etapas de trabalho e não classifica etapas por nome.

## Defaults locais para esta implementação autorizada

- Permitir nomes repetidos nas etapas de trabalho; destinos identificados por ID, nome, setor e posição. Exemplo: Engenharia no início e no fim do fluxo.
- Renomear, trocar setor e ordenar uma etapa ocupada preserva suas demandas, responsáveis/status e vínculos. Os históricos conservam o nome registrado na ocasião.
- Permitir remover a última etapa de trabalho se vazia; o quadro poderá aguardar configuração. Não acrescentar mínimo de etapas comuns sem decisão.
- Ao configurar etapas, validar nome/setor normalizados de 1–255 unidades UTF-16; não truncar dados existentes. Os mesmos limites de campos já valem para etapas iniciais de novos quadros. A ordem inicial mantém o contrato anterior de inteiro positivo; o intervalo e a reorganização deste CRUD se aplicam à configuração de quadro existente.
- Informar conflito quando a configuração exibida ficou antiga. Gravar a reorganização inteira em uma transação; falha não pode deixar ordem parcial nem remover/mover demandas automaticamente.
- No editor de uma sequência antiga com empates/lacunas, Posição representa a posição visual da coluna na lista ordenada (1..N). O número antigo permanece no snapshot; preparar o formulário não grava uma normalização. Nome e setor continuam com seus valores conhecidos.
- Fornecer controles de teclado, confirmação de remoção, mensagens claras e recuperação por nova consulta sem repetir gravação automaticamente.

## Evidência histórica e situação após a entrega

`Etapa` tem ID, nome, setor nullable, ordem e gerenciamento; não tem categoria de destino final. Ordens antigas podem empatar ou ter lacunas. O histórico referencia a demanda e conserva nomes em texto, sem vínculo obrigatório com a etapa apagada.

Antes desta entrega, as escritas já bloqueavam o gerenciamento, mas alterações de etapas não incrementavam a versão do quadro. T1 agora incrementa essa versão exatamente uma vez, recusa configuração antiga e confere demandas na mesma transação da remoção. T4 atualiza o cache da lista com o snapshot retornado.

A migração das etapas finais deverá tratar quadros antigos e arquivados explicitamente, sem classificar por nome, inventar setores ou inserir colunas como efeito escondido de uma consulta/restauração.

## Próximo passo

Registrar o teste de uso humano quando houver resposta. A próxima entrega tratará os destinos finais de AD-011. Permissões de movimentação, reabertura, alcance do botão Pular, justificativa de cancelamento e tratamento de demandas antigas aguardam respostas; perguntas pendentes em `.specs/STATE.md`. O botão Criar processo pertence ao CRUD de demandas e continua no backlog.
