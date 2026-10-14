# Evidências de gráficos por quadro

14/10/2026. Proposta aprovada explicitamente. Três passos locais, sem tasks/design separados pelo porte médio.

## Plano e gates

| Passo | Arquivos | Gate | Estado |
| --- | --- | --- | --- |
| T1 Componente de contagens | components/GraficosQuadro.jsx, .css, .test.jsx | Vitest somente componente | Implemented, 3/3 PASS |
| T2 Integração | pages/Quadro.jsx, QuadroGraficos.test.jsx, e2e/graficos.spec.js | Vitest focado + um E2E, lint/build | Pending |
| T3 Uso e evidências | docs/OPERACAO.md, spec/evidence, diagnóstico/STATE | conferência documental, gates técnicos T2 sem mudança de código | Pending |

Premissa: snapshot já validado pela API atual, sem escrita ou chamada adicional. T1 toca somente componente, CSS e testes. Sucesso: contagens literais por situação/ID, arquivadas separadas, vazio e texto escapado.

## T1: adequação e necessidade

Base dos caminhos: frontend/src/components/GraficosQuadro.test.jsx.

| AC / resultado da spec | Evidência e expressão | Coberto |
| --- | --- | --- |
| GRAF-02: status literais e desconhecidos | :16 `expect(linhas("Demandas por situação")).toEqual(["Em andamento2", "Concluídas1", "Canceladas1", "Não informado2"])` | Sim |
| GRAF-03: IDs separados e vínculo desconhecido | :17 `expect(linhas("Demandas por etapa")).toEqual(["Trabalho2", "Trabalho1", "Concluídos1", "Cancelados1", "Etapa não identificada1"])`; :35 ausência de etapas | Sim |
| GRAF-04: totais e separação | :18–19 `getByText("Total não arquivadas: 6")` e `getByText("Arquivadas: 1")`; barras anteriores excluem arquivada | Sim |
| GRAF-08: vazio sem divisão inválida | :27–30 mensagem, listas exatas e `every(b => b.style.width === "0%")` | Sim |
| GRAF-09: texto acessível/escapado, barras decorativas | :21 `every(b => b.getAttribute("aria-hidden") === "true")`; :38 texto literal; :41 `expect(document.querySelector("img")).toBeNull()` | Sim; 360px em T2 |

| Teste / expressão | Necessidade |
| --- | --- |
| :11, asserts :16–22 | GRAF-02/03/04/09, valores e proporções definidos |
| :25, asserts :27–30 | GRAF-08, vazio |
| :33, asserts :35–41 | GRAF-03/05/09, ausência, recálculo e texto literal |

Check A/B/C/D: resultados concretos correspondem à spec, nenhum teste especulativo ou alterado, convenção Vitest/Testing Library existente. Critérios de integração ficam para T2, sem declaração prematura de cobertura.

Execuções do autor: 1. `npm test -- src/components/GraficosQuadro.test.jsx`: 3 PASS, zero falhas/skips, 2,93s. Não executadas suítes completas, Java ou MySQL.
