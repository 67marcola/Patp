# Evidências de implementação da atividade

Estado: em andamento. Tarefas Implemented não significam Verified; Verifier independente posterior a T18.

## T1 — entidade

10/10/2026: Maven -B test-compile exit0, BUILD SUCCESS,54 fontes principais/45 fontes de testes. Nenhum teste executado neste gate, conforme matriz de entidade. validate_tasks exit0, aviso esperado Tests:none conferido.

| Critério | Evidência | Resultado esperado | Cobertura |
| --- | --- | --- | --- |
| FK obrigatória ao quadro; sem FK/cascade ao alvo/autor | Atividade.java:25 `@ManyToOne(optional = false)`; :34–46 IDs/nomes escalares | Identificação independente do alvo/autor | Inspeção de mapeamento e compile |
| Valores longos e precisão | Atividade.java:51 `TIMESTAMP(6)`; :54 `LONGTEXT` | Micros e JSON longo | Compile; persistência real atribuída a T2 |
| Vínculo único | Atividade.java:57 `@Column(name = "historico_id", unique = true)` | Vínculo escalar único | Compile; persistência real atribuída a T2 |

Reverse-map: sem testes novos em T1, conforme matriz; nenhuma asserção especulativa. Adequação A/B/C/D: entidade sem lógica, gate compile aprovado; comportamento e rollback serão provados no writer T2. Sem desvio da spec ou alteração de testes antigos.
