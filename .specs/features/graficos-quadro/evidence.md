# Evidências de gráficos por quadro

14/10/2026. Proposta aprovada explicitamente. Três passos locais, sem tasks/design separados pelo porte médio.

## Plano e gates

| Passo | Arquivos | Gate | Estado |
| --- | --- | --- | --- |
| T1 Componente de contagens | components/GraficosQuadro.jsx, .css, .test.jsx | Vitest somente componente | Implemented, 3/3 PASS |
| T2 Integração | pages/Quadro.jsx, QuadroGraficos.test.jsx, e2e/graficos.spec.js | Vitest focado + um E2E, lint/build | Implemented, PASS |
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

## T2: integração

Arquivos: Quadro.jsx, QuadroGraficos.test.jsx e e2e/graficos.spec.js. Premissa: gráficos são consulta independente dos formulários, sem novo fetch. Sucesso: toggle acessível, snapshot atual, nenhuma quantidade após falha/conflito, rascunhos preservados e leitor/arquivado em 360px.

Vitest focado: `npm test -- src/components/GraficosQuadro.test.jsx src/pages/QuadroGraficos.test.jsx src/pages/QuadroEtapas.test.jsx`: **51/51 PASS**, três arquivos, 57,57s, zero falhas/skips. Inclui os seis testes novos e 45 testes existentes do quadro, sem alterar nenhum deles. Segunda execução do autor, sem suíte completa.

| AC / resultado | Evidência em frontend/src/pages/QuadroGraficos.test.jsx | Coberto |
| --- | --- | --- |
| GRAF-01: expandido/controle/foco e recolher | :28/30 `getAttribute("aria-expanded")` false/true; :31 `getAttribute("aria-controls")).toBe(grafico().id)` e `activeElement).toBe(botao)`; :33 ausência após Enter | Sim |
| GRAF-05: troca não mostra quadro anterior | :40/42 `expect(grafico()).toBeNull()` durante e após troca; :43 total0 do novo quadro | Sim |
| GRAF-05: resultado da ação recalcula | :56/57 total1, arquivadas1 depois de arquivar, em vez do snapshot inicial2/0 | Sim |
| GRAF-06: carga/erro/conflito não viram zero | :21/24 botão desabilitado e gráfico ausente; :61 idem após conflito; :64 total1 retorna após consulta | Sim |
| GRAF-07: leitor arquivado sem chamadas próprias | :18 fixture leitor/arquivado, :32 total2, :35 calls=2 (carga falha + nova tentativa) | Sim |
| GRAF-09: rascunho de formulário | :74/76 `.value).toBe("Rascunho preservado")` abrindo e fechando | Sim |

| Teste / expressões | Mapeamento reverso |
| --- | --- |
| :14, asserts :21–43 | GRAF-01/05/06/07, carga, erro, toggle e troca |
| :46, asserts :56–64 | GRAF-04/05/06, ação e conflito |
| :67, asserts :74–77 | GRAF-09/07, rascunho e ausência de consulta extra |

Adequação: seis testes novos agregam cenários com resultados explícitos, sem duplicar o comportamento do servidor. Os 45 testes existentes não foram alterados. Browser verifica integração real e layout; falhas de consulta/legados ficam no nível focado. Não há novo endpoint para exigir suíte de servidor.

Browser inicial encontrou falha real: o painel de comentários interceptava o botão no grupo de ações à direita (`graficos-autor-browser.log`, trace em frontend/test-results). Corrigido somente o posicionamento do botão, junto ao título do quadro. Nenhuma expectativa ou timeout alterado. Após a correção: **6/6 testes novos PASS**, lint e build TEMP PASS; **1/1 Edge/H2 PASS** (17,3s total, cenário 3,1s). Log: TEMP/graficos-autor-browser2.log. Capturas desktop e arquivado360 em TEMP/creral-graficos-QJWDXz, QA mobile conferido visualmente.

E2E `frontend/e2e/graficos.spec.js`: :34–38 foco, valores3/0, estados1/1/1 e etapas; :42 rascunho de comentário preservado; :46–47 totais2/1 e estado0/1/1 após arquivar; :53–54 consulta real de leitor em quadro arquivado; :56 layout dentro de360px. Todos os asserts mapeiam GRAF-01/02/03/04/05/07/09. GRAF-06/08 e legados foram provados nos testes focados, sem fixture adicional de banco.

Execuções de testes do autor até T2: **5** (componente3; integração/regressão51; browser1 falhou; após correção novos6 e browser1 PASS). Redução de75% contra os20 gates mínimos de tarefas da entrega anterior, sem contar suas repetições. Comparação de volume por entrega, não de tempo ou tokens medidos. Suítes completas Java/MySQL/Vitest não foram executadas nesta entrega; cobertura declarada é somente a realmente executada. Build final: TEMP/creral-graficos-build-final-f391d60ad9a34459adc01d0985aa8dbd.
