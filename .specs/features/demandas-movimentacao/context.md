# Movimentação de demandas Context

**Gathered:** 2026-10-06
**Status:** regras fechadas; implementação local autorizada.

## Feature Boundary

Mover/pular entre etapas de trabalho, encerrar em Concluídos/Cancelados e reabrir para trabalho escolhido no mesmo quadro. Fechar o desvio das APIs antigas e da edição genérica, preservando cadastro/consulta já entregues.

## Implementation Decisions

- AD-011: fechamento tem destino automático, definido pela categoria persistida.
- AD-012/013: somente criador/admin em quadro ativo; reabertura para trabalho escolhido.
- AD-014: movimento pode pular etapas e voltar a uma anterior.
- AD-015: motivo obrigatório para novos cancelamentos.
- AD-021: resposta A recebida nesta retomada limpa três campos atuais e preserva encerramentos anteriores no histórico.

## Declined / Undiscussed Gray Areas → Assumptions

Defaults técnicos explícitos em spec.md: repetição409, motivo10000, nova API com versão, snapshot, rollback e histórico longo. Não são apresentados como novas escolhas do usuário. AD-022: resposta A recebida, manter consulta e bloquear transições de status desconhecido até correção explícita. Nenhuma normalização automática.

## Specific References

Instalação de poste: comprar → verificar local → enviar técnico. Mover/pular permite escolher trabalho sem passar por todas as colunas. Concluir/cancelar usam as duas colunas oficiais.

## Deferred Ideas

Edição/exclusão completas, comentários laterais, logs do quadro, gráficos e melhorias gerais de interface permanecem em recortes posteriores.
