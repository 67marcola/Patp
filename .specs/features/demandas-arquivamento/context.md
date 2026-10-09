# Arquivamento de demandas Context

**Gathered:** 08/10/2026. **Spec:** spec.md. **Status:** proposta concreta autorizada; execução em andamento.

## Feature Boundary

Retirar significa arquivar e permitir consulta/restauração. Preservar identificação, campos, encerramento, comentários/eventos. Somente criador/admin em quadro ativo. Os três estados conhecidos podem ser arquivados.

## Implementation Decisions

- Usuário escolheu explicitamente Arquivar e restaurar (recomendado).
- Usuário escolheu explicitamente Criador do quadro ou administrador (recomendado).
- Usuário escolheu explicitamente Todos os três estados (recomendado).
- Após apresentação da proposta concreta, o “Pode continuar” autoriza sua implementação, dois lotes sequenciais por agentes (servidor T1–T8 e interface T9–T12), e as adequações de ProcessoArchiveTests e QuadroTransicoes descritas em tasks.md. Não altera as políticas já escolhidas.
- Em 08/10/2026, o usuário respondeu “Sim, corrigir essas expectativas (recomendado)” à adequação concreta dos testes E2E novos: comparar todos os campos próprios de comentários/eventos e o ID do vínculo com a demanda, comparar os 14 campos da demanda e a versão separadamente, e exigir HTTP204 nas duas ações existentes de arquivo/restauração do quadro. As ações de demanda continuam HTTP200; todos os outros cenários/asserts permanecem.
- Ritmo guiado pelas preferências anteriores: perguntas com recomendação/efeitos, no máximo duas independentes; não repetir decisões e tecnologias confirmadas.

### Declined / Undiscussed Gray Areas → Assumptions

Restaurar conserva etapa/estado e número reservado; área Arquivadas consultável aos autenticados; arquivo não entra na contagem das colunas; referência impede remover etapa. Defaults informados ou registrados na spec, sem apresentá-los como respostas explícitas. Sem autorização para exclusão física, banco real ou deploy.

## Specific References

Manter Java/Spring Boot/MySQL e React/Vite. Cada requisito só entra como concluído no diagnóstico após provas e revisão independente. Explicar ao usuário o que será colocado e por quê.

## Deferred Ideas

Comentários laterais, gráficos, atividade completa por quadro e correção de status desconhecido fora deste corte.
