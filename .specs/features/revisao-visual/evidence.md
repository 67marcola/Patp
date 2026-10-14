# Evidência da revisão visual

14/10/2026. Proposta autorizada pelo usuário ao pedir continuar após a apresentação do plano. Escopo médio, três passos implícitos em spec.md.

## T1: checkpoint inicial antes da autorização

Premissa: conservar fluxos e títulos existentes; Creral textual, sem logo oficial. Arquivos: App.jsx, Login.jsx, Cadastro.jsx, index.html e RevisaoVisual.test.jsx. Resultado esperado: VIS-01/02 e sessão preservada, com regressão focada AppSessao.test.jsx.

Identificação e rótulos implementados. Primeira execução `npm test -- src/RevisaoVisual.test.jsx src/AppSessao.test.jsx`: nove casos, sete PASS e dois FAIL. Os seis testes existentes de sessão passaram; o novo caso de login/cadastro passou. Nenhum teste antigo foi modificado.

Falhas de preparação do teste novo: :26 procura mensagem Nenhum gerenciamento encontrado, mas o texto existente preservado é Nenhum gerenciamento ativo; :33 usa new URL com import.meta.url no ambiente jsdom, obtendo URL HTTP incompatível com readFileSync. Solução recomendada: corrigir a referência ao texto existente e usar caminho de arquivo de index.html, sem mudar expectativas de identidade, metadados, autoria ou sessão. Autorização solicitada ao usuário antes da correção de expectativa, aguardando resposta. Nenhuma correção aplicada ou teste ignorado.

T1 não passou no gate, sem commit ou conclusão. T2 CSS/navegador e T3 guia ainda não iniciados. Verificação independente somente após a implementação concluída. Uma invocação de testes até este ponto; sem suítes completas Java/MySQL/Vitest.

## T1 implementado após autorização

Usuário autorizou continuar após a pergunta sobre as duas referências em14/10/2026. Corrigidos somente o texto para a mensagem já existente e o caminho de leitura de index.html. Gate repetido: **9/9 PASS**, 6,84s, zero falhas/skips; três novos casos e seis existentes de sessão preservados. Duas invocações acumuladas do autor, sem suítes completas.

| AC / resultado | Evidência em frontend/src/RevisaoVisual.test.jsx | Coberto |
| --- | --- | --- |
| VIS-01 identificação e títulos | :9/17 `getByText("Creral")`, headings Bem-vindo/Criar cadastro; :25 `getByRole("heading", {name:"Creral",exact:true})`; :34/35 `toContain` lang pt-BR e título exatos | Sim |
| VIS-02 associação e autocomplete | :13 `expect(document.activeElement).toBe(input)` e `expect(input.autocomplete).toBe(auto)` para E-mail/username e Senha/current-password | Sim |
| VIS-07 valores e sessão | :14 input.value igual ao texto digitado; :28 token sessao e usuario integral; :29 uma consulta; seis testes existentes AppSessao aprovados | Sim, demais fluxos em T2 |

Mapeamento reverso: teste :7 cobre VIS-01/02/07; :20 cobre VIS-01/07; :32 cobre VIS-01. Todos usam resultados definidos pela spec. Sem cenários especulativos, exclusão ou alteração de testes antigos. Fonte/convenções: coding-principles e Vitest/Testing Library existentes. Adequação T1 aprovada; estilos e medidas aguardam T2, Verifier ainda pendente.
