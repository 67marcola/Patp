# CRUD de etapas: esclarecimento em andamento

**Data:** 2026-10-05.
**Status:** decisões parciais confirmadas; perguntas/propostas abaixo aguardam resposta ou revisão. Nenhuma implementação deste CRUD foi iniciada.

## Limite desta entrega

Criar, consultar, renomear, mudar setor, ordenar e remover as colunas de trabalho de um gerenciamento existente, pela interface e API. Preservar suas demandas e o histórico. Este requisito segue o CRUD de gerenciamentos já validado tecnicamente.

Etapas obrigatórias de conclusão/cancelamento e os fluxos de finalizar/pular etapas permanecem em requisitos próprios. O nome de uma coluna não deve ganhar significado de conclusão/cancelamento por inferência.

## Decisões confirmadas

- AD-007: somente criador do quadro ou administrador configura etapas de um gerenciamento ativo.
- AD-008: remover etapa com demandas é bloqueado; primeiro elas devem ser movidas para outra etapa.
- AD-005/006: arquivados somente consulta; quadro sem criador é administrado somente por administrador.

## Perguntas enviadas, ainda sem resposta

1. **Ordem:** escolher uma posição e reorganizar automaticamente as demais etapas (recomendação), ou digitar números de ordem manualmente? Exemplo: colocar Verificar local antes de Comprar poste.
2. **Setor:** continuar obrigatório nas etapas de trabalho (recomendação), ou torná-lo opcional? É um texto identificando a área responsável, como Compras/Engenharia; não define permissão de acesso.
3. **Destinos finais, pergunta anterior:** exatamente duas colunas finais fixas Concluídos/Cancelados, ou permitir destinos finais adicionais de cada categoria? Em ambas as propostas, as duas etapas obrigatórias originais permanecem. Esta decisão pertence ao requisito das finais e não será inferida de “continue”.

Não considerar a opção pré-selecionada no formulário uma resposta. As recomendações acima são propostas do agente.

## Propostas adicionais para a especificação, sem aprovação presumida

- Permitir nomes repetidos nas etapas de trabalho; destinos identificados por ID, nome, setor e posição. Exemplo: Engenharia no início e no fim do fluxo.
- Renomear, trocar setor e ordenar uma etapa ocupada preserva suas demandas, responsáveis/status e vínculos. Os históricos conservam o nome registrado na ocasião.
- Permitir remover a última etapa de trabalho se vazia; o quadro poderá aguardar configuração. Não acrescentar mínimo de etapas comuns sem decisão.
- Ao configurar etapas, validar nome normalizado de 1–255 caracteres e setor conforme a resposta acima; não truncar dados existentes. Aplicar os mesmos contratos às etapas iniciais de novos quadros.
- Informar conflito quando a configuração exibida ficou antiga. Gravar a reorganização inteira em uma transação; falha não pode deixar ordem parcial nem remover/mover demandas automaticamente.
- Fornecer controles de teclado, confirmação de remoção, mensagens claras e recuperação por nova consulta sem repetir gravação automaticamente.

## Evidência do código para o próximo design

`Etapa` tem ID, nome, setor nullable, ordem e gerenciamento; não tem categoria de destino final. Ordens antigas podem empatar ou ter lacunas. O histórico referencia a demanda e conserva nomes em texto, sem vínculo obrigatório com a etapa apagada.

As escritas já bloqueiam o gerenciamento, mas alterar uma etapa não incrementa automaticamente a versão do quadro. A implementação futura deve impedir que uma configuração de tela antiga sobrescreva outra e conferir existência de demandas dentro da mesma transação da remoção.

A migração das etapas finais deverá tratar quadros antigos e arquivados explicitamente, sem classificar por nome, inventar setores ou inserir colunas como efeito escondido de uma consulta/restauração.

## Próximo passo

Usar as respostas para fechar requisitos testáveis do CRUD de etapas, registrar as propostas restantes e apresentar a especificação concreta antes de implementar e testar esta entrega.
