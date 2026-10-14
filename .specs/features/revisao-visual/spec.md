# Revisão visual geral Specification

**Date:** 2026-10-14. **Status:** Verified em14/10/2026, 7/7 critérios PASS independente, sensor visual1/1. Proposta autorizada pelo usuário. Porte médio, três passos implícitos.

## Problem Statement

Login/cadastro e sistema ainda não apresentam identificação consistente da Creral. A aba mostra frontend e o documento declara idioma inglês. A interface já tem azul-escuro/verde e suporte móvel, mas controles, foco, contraste e espaçamentos precisam de uma revisão conjunta, preservando os fluxos entregues.

## Goals

- Identificar a Creral e uniformizar a apresentação nas telas existentes.
- Tornar controles e textos legíveis e utilizáveis no teclado, em computador e em360px.
- Manter regras, permissões, dados e tecnologias, com verificação focada conforme AD-035.

## Out of Scope

| Feature | Reason |
| --- | --- |
| Logotipo oficial, imagens, nova paleta ou biblioteca | Estilo atual aprovado; nome textual não representa criação de marca oficial. |
| Renomear ações e redesenhar navegação | Preservar vocabulário e funcionamento já conhecidos, inclusive Criar processo. |
| Novos recursos, banco, API, alterações de permissão | Entrega de apresentação e semântica acessível. |
| Certificação completa de acessibilidade e aceite humano | Verificar critérios delimitados; aceite depende dos usuários. |

## Assumptions & Open Questions

| Assumption / decision | Chosen default | Rationale | Confirmed? |
| --- | --- | --- | --- |
| Alcance | Login, cadastro, lista de quadros, quadro e formulários/painéis existentes | Usuário escolheu revisão geral | Sim, 14/10 |
| Estilo | Azul-escuro e verde existentes, Creral em texto | Usuário confirmou manter estilo; sem asset oficial fornecido | Sim, 14/10 |
| Identificação | Creral visível nas três áreas; aba Creral \| Gestão de demandas; documento pt-BR | Nome da empresa e conteúdo já são conhecidos | Sim, conjunto aprovado em14/10 |
| Controles | Altura mínima44px; foco3px visível; texto secundário mais escuro | Melhor leitura e acionamento sem nova navegação | Sim, conjunto aprovado em14/10 |
| Layout | Manter estrutura atual; ajustar quebra de nomes, espaçamentos e colunas em360px | Limita risco e conserva aprendizado de uso | Sim, conjunto aprovado em14/10 |

**Open questions:** none — defaults não respondidos têm escolha e justificativa acima; conjunto autorizado pelo usuário ao pedir continuar a proposta preparada.

## User Stories

### P1: Reconhecer e usar o sistema com clareza

**User Story:** Como funcionário, quero identificar o sistema Creral e usar suas telas com clareza no computador e no celular.

**Acceptance Criteria:**

1. **VIS-01:** WHEN o usuário consulta login, cadastro ou área autenticada THEN a interface SHALL mostrar Creral em texto, preservar os títulos Bem-vindo e Criar cadastro e apresentar Creral no cabeçalho autenticado; o documento SHALL usar lang pt-BR e título de aba Creral | Gestão de demandas.
2. **VIS-02:** WHEN o usuário seleciona os rótulos E-mail e Senha no login THEN o sistema SHALL focar os respectivos campos, com nomes acessíveis correspondentes; os campos SHALL usar autocomplete username e current-password respectivamente, sem alterar validação ou envio.
3. **VIS-03:** The interface SHALL manter azul-escuro #14232f e verde #5db28f na paleta, usar texto secundário #52616b sobre superfícies claras e herdar a mesma fonte em botões, inputs, selects e textareas; campos e botões de ação SHALL ter altura mínima44px nas telas do escopo, sem mudar seu estado habilitado/desabilitado.
4. **VIS-04:** WHEN um controle recebe foco pelo teclado THEN a interface SHALL mostrar contorno sólido3px com afastamento3px, verde escuro #147d64 nas superfícies claras e verde claro #8be0bb nos botões do cabeçalho escuro, abrangendo botões, inputs, selects e textareas, sem remover foco ou alterar a ordem do DOM.
5. **VIS-05:** WHILE a viewport é360px ou1280px a interface SHALL manter login, cadastro, lista, formulário de quadro, quadro de demandas e seus formulários/painéis dentro da largura visível, quebrando nomes longos, organizando ações em linhas quando necessário e mantendo controles visíveis sem rolagem horizontal da página.
6. **VIS-06:** WHILE dados carregam, uma lista está vazia, há erro ou o quadro está arquivado a interface SHALL preservar as mensagens e ações existentes e distingui-las visualmente do conteúdo, sem transformar esses estados em sucesso ou alterar suas permissões.
7. **VIS-07:** WHEN o usuário percorre login, lista, abertura do quadro e um formulário THEN o sistema SHALL conservar sessão, ações e rascunhos existentes; os ajustes visuais SHALL não acrescentar requisições, dependências ou alterações no servidor/banco.

**Independent Test:** Visitar login/cadastro por teclado, entrar em conta fictícia, consultar lista vazia e preenchida, abrir formulário e quadro com nomes longos; verificar medidas, foco e legibilidade em1280/360, incluindo erro de consulta e arquivado.

## Edge Cases

- Nomes e descrições longos, avisos, erro de consulta, vazio e controles desabilitados seguem VIS-03/05/06.
- Rascunhos e navegação seguem VIS-07; campos mantêm limites e validações existentes.
- Dimensões presentes: acesso/estado/erro preservados, sem nova política. Demais dimensões N/A: nenhuma escrita de domínio, transição, dependência externa ou operação concorrente nova nesta entrega.

## Requirement Traceability

| Requirement ID | Story | Phase | Status |
| --- | --- | --- | --- |
| VIS-01 | P1: Clareza | Execute | Verified |
| VIS-02 | P1: Clareza | Execute | Verified |
| VIS-03 | P1: Clareza | Execute | Verified |
| VIS-04 | P1: Clareza | Execute | Verified |
| VIS-05 | P1: Clareza | Execute | Verified |
| VIS-06 | P1: Clareza | Execute | Verified |
| VIS-07 | P1: Clareza | Execute | Verified |

## Success Criteria

- [x] Identificação, rótulos e foco conferidos nos pontos definidos.
- [x] Telas do escopo legíveis e sem overflow horizontal em1280/360; fluxos existentes preservados.
- [x] Gates focados e Verifier independente PASS; diagnóstico e guia de aceite atualizados com resultado real.

## Plano local

1. Identificação e semântica de login: App.jsx, Login.jsx, Cadastro.jsx, index.html e testes focados. Gate: testes desses resultados e regressão próxima de sessão. Commit próprio.
2. Padronização visual: index.css e um cenário e2e/visual.spec.js cobrindo as telas/estados definidos, com capturas desktop/mobile. Gate: um cenário navegador, lint e buildTEMP. Commit próprio.
3. Guia e evidências: docs/OPERACAO.md e memória/diagnóstico/spec. Gate documental, sem repetir testes sem mudança de código. Commit próprio.

Depois: Verifier independente, somente testes afetados e sensor leve1–3 mutações em scratch. Não executar suítes completas Java/MySQL/Vitest ou refazer baseline histórica para revisão visual. Repetir gates somente após falha/mudança relevante. Aplicar orçamento reduzido AD-035, mantendo testes existentes. Nenhuma autorização para push, deploy ou banco real.
