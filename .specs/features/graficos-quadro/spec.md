# Gráficos por quadro Specification

**Status:** Approved pelo usuário em 14/10/2026. **Date:** 2026-10-14. **Scope:** Medium, interface de consulta usando a configuração já carregada do quadro; sem alteração de API ou banco.

## Problem Statement

O quadro já lista demandas e etapas, mas exige contar cartões para entender a distribuição do trabalho. O usuário confirmou gráficos dentro de cada quadro, por situação e etapa, com arquivadas contadas separadamente.

## Goals

- Permitir comparar quantidades atuais por situação e etapa, sem misturar quadros.
- Reutilizar os dados e tecnologias existentes, com testes proporcionais e sem repetir suítes sem necessidade.

## Out of Scope

| Feature | Reason |
| --- | --- |
| Visão geral de todos os quadros | Usuário escolheu por quadro. |
| Evolução temporal, filtros por período e produtividade | Este corte mostra a situação atual; não presume datas históricas completas. |
| Exportação, novas bibliotecas e novas rotas | Barras e valores podem usar React/HTML/CSS e os dados existentes. |
| Alteração de demandas, permissões ou banco | Recurso somente de consulta. |

## Assumptions & Open Questions

| Assumption / decision | Chosen default | Rationale | Confirmed? |
| --- | --- | --- | --- |
| Alcance e indicadores | Por quadro; situação, etapa e arquivadas separadas | Escolha explícita do usuário em 14/10 | Sim |
| Apresentação | Botão Gráficos abre/fecha seção no próprio quadro, sem cobrir cartões nem fechar rascunhos | Mantém consulta simples e evita outro painel modal | Sim, conjunto aprovado em 14/10 |
| Acesso | Todos os autenticados, inclusive em quadros arquivados | Acompanha acesso de consulta existente | Sim, conjunto aprovado em 14/10 |
| Atualização | Mesma configuração exibida nos cartões; acompanha recargas e ações bem-sucedidas; sem consulta própria nem atualização automática em segundo plano | Evita totais com origem divergente e requisições extras | Sim, conjunto aprovado em 14/10 |
| Legados | Situação desconhecida em Não informado; etapa ausente do cadastro em Etapa não identificada | Conserva o total sem inventar estado ou vínculo | Sim, conjunto aprovado em 14/10 |
| Barras | Valores absolutos; largura proporcional ao maior valor de cada gráfico, com legenda textual | Comparação de volumes sem arredondamento de percentuais | Sim, conjunto aprovado em 14/10 |

**Open questions:** none — escolhas não respondidas têm defaults e justificativas na tabela acima. O conjunto foi confirmado explicitamente pelo usuário.

## User Stories

### P1: Entender a distribuição atual

**User Story:** Como funcionário autenticado, quero consultar os gráficos do quadro para identificar onde estão suas demandas.

**Acceptance Criteria:**

1. **GRAF-01:** WHEN o usuário abre um quadro com configuração carregada THEN o sistema SHALL oferecer o botão Gráficos, inicialmente recolhido; acioná-lo SHALL alternar uma seção identificada, com aria-expanded e aria-controls, mantendo o foco no botão.
2. **GRAF-02:** WHILE os gráficos estão abertos o sistema SHALL mostrar Em andamento, Concluídas e Canceladas com as quantidades exatas das demandas não arquivadas do quadro, usando respectivamente os status literais Em andamento, Concluido e Cancelado; outros valores SHALL compor Não informado, exibido somente quando houver ocorrências.
3. **GRAF-03:** WHILE os gráficos estão abertos o sistema SHALL mostrar todas as etapas da configuração na ordem recebida, inclusive com zero, contando demandas não arquivadas por etapaId; vínculos sem etapa correspondente SHALL compor Etapa não identificada; nomes iguais SHALL continuar em linhas distintas.
4. **GRAF-04:** WHILE os gráficos estão abertos o sistema SHALL exibir Total não arquivadas igual ao tamanho de demandas e Arquivadas igual ao tamanho de demandasArquivadas; arquivadas SHALL permanecer fora das barras e cada gráfico SHALL somar exatamente o total não arquivadas.
5. **GRAF-05:** WHEN a configuração do quadro muda após consulta ou ação bem-sucedida THEN o sistema SHALL recalcular os gráficos a partir desse mesmo snapshot; trocar de quadro SHALL recolher os gráficos e impedir a exibição dos dados do quadro anterior.
6. **GRAF-06:** IF não há snapshot válido, a consulta falhou ou o quadro exige atualização após conflito THEN o sistema SHALL impedir abrir gráficos e ocultar totais anteriores, sem apresentar falha de consulta como quantidade zero.
7. **GRAF-07:** WHEN um usuário autenticado consulta quadro ativo ou arquivado THEN o sistema SHALL permitir os mesmos gráficos de leitura independentemente de podeAdministrar, sem criar ação de gravação ou requisição adicional para os gráficos.
8. **GRAF-08:** IF o total não arquivadas é zero THEN o sistema SHALL mostrar Nenhuma demanda não arquivada neste quadro., três situações com zero e etapas existentes com zero; nenhuma barra SHALL usar largura inválida ou divisão por zero.
9. **GRAF-09:** The system SHALL apresentar valores numéricos e rótulos em texto acessível, barras decorativas ocultas das tecnologias assistivas, nomes como texto escapado e layout sem rolagem horizontal própria a 360px; abrir gráficos SHALL preservar comentários e formulários existentes.

**Independent Test:** Quadro com duas etapas de nomes iguais e demandas em três estados, uma com status desconhecido e uma arquivada; conferir totais, separação por IDs e atualização após arquivar. Consultar como leitor e em quadro arquivado.

## Edge Cases

- Etapas ausentes, nomes repetidos, status nulo/desconhecido e listas vazias seguem GRAF-02/03/08.
- Falha, conflito e troca de quadro seguem GRAF-05/06; não adicionar fluxo de requisição paralelo.
- Dimensões presentes: acesso de consulta, estado/arquivamento, snapshot, erro e ordenação explicitados acima. Demais dimensões N/A: não há persistência, escrita, dependência externa nova ou transição de domínio neste recurso.

## Requirement Traceability

| Requirement ID | Story | Phase | Status |
| --- | --- | --- | --- |
| GRAF-01 | P1: Distribuição atual | Specify | Pending |
| GRAF-02 | P1: Distribuição atual | Execute | Implementing |
| GRAF-03 | P1: Distribuição atual | Execute | Implementing |
| GRAF-04 | P1: Distribuição atual | Execute | Implementing |
| GRAF-05 | P1: Distribuição atual | Specify | Pending |
| GRAF-06 | P1: Distribuição atual | Specify | Pending |
| GRAF-07 | P1: Distribuição atual | Specify | Pending |
| GRAF-08 | P1: Distribuição atual | Execute | Implementing |
| GRAF-09 | P1: Distribuição atual | Execute | Implementing |

## Success Criteria

- [ ] Quantidades por situação e etapa conferem com as demandas do próprio quadro; arquivadas separadas.
- [ ] Consulta acessível em desktop e 360px, incluindo vazio, erro, leitor e arquivado.
- [ ] Testes pertinentes e verificação independente aprovados; diagnóstico atualizado com resultado real.

## Execução proposta e orçamento de testes

Três passos atômicos, sem design/tasks separados: (1) componente de gráficos e testes de contagem/acessibilidade; (2) integração no quadro, testes focados de troca/erro/atualização e um cenário de navegador; (3) instruções de uso e evidências. Cada passo terá gate e commit próprio. Verifier independente após o último commit.

Aplicar AD-035: no máximo uma execução focada por gate, repetindo somente se houver falha ou nova alteração. Não executar suítes Java/H2/MySQL, pois servidor/banco permanecem inalterados; não refazer baseline histórica nem rodar a suíte completa a cada tarefa. Usar casos que agrupem resultados independentes do mesmo cenário. Planejar pelo menos 50% menos execuções que o fluxo anterior de suítes completas por tarefa, registrando os comandos efetivos e o escopo coberto. Verificação independente focada, com sensor em scratch e saída compacta, sem repetir todos os testes do projeto.
