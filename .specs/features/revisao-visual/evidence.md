# Evidência da revisão visual

## T3 implementado

Guia em docs/OPERACAO.md descreve identificação, rótulos, controles, foco, contraste, quebra de nomes e preservação dos fluxos. Aceitação humana separada e pendente. Conferência documental contra VIS-01–07, sem alteração de código desde os gatesT2. `validate_spec --strict` e diff-check aprovados; nenhuma repetição de teste para texto documental. T1–T3 implementados, ainda aguardando Verifier independente e validate_state antes de concluir.

## T2 implementado, 14/10/2026

Arquivos: index.css e e2e/visual.spec.js. Premissa: preservar estrutura, regras e ações. Sucesso: contraste, controles44px, foco3px e telas sem overflow em1280/360. Um cenário de navegador percorre login, cadastro, carregando, erro, lista vazia/preenchida, formulário de quadro, quadro, editor de etapa/demanda, movimentação, arquivo de demanda, comentários, atividade e quadro arquivado; capturas dos15 estados em dois tamanhos.

Tentativa browser1 identificou leitura intermediária da transição CSS de foco. Ajustada sincronização para esperar o mesmo resultado definido (expect.poll), sem alterar valores ou timeout. Também corrigidos dois seletores de ações existentes: Tentar atualizar e Mover/pular etapa demanda. Nenhuma expectativa foi enfraquecida.

Browser2 detectou bug real: número de demanda longo no formulário de movimentação causava overflow em1280px. Corrigida quebra de texto de form-card e limite do select. Browser3 **1/1 Edge/H2 PASS**, 16,2s total, cenário5,9s. Lint/buildTEMP finais PASS. Caminho de capturas TEMP/creral-visual-bIt5pL; login360, quadro360 e mover1280 inspecionados visualmente, legíveis e sem corte. Build final TEMP/creral-visual-build-final-3704be8886fe45d1899beaf3cd56f73b. Logs TEMP/visual-autor-browser{,2,3}.log preservam tentativas.

| AC / resultado definido | Evidência em frontend/e2e/visual.spec.js | Coberto |
| --- | --- | --- |
| VIS-01 Creral/aba/idioma | :37 título exato e html lang; :38/47 Creral e títulos; :61 cabeçalho | Sim |
| VIS-02 associação/autocomplete | :39–42 rótulos focam inputs e atributos username/current-password | Sim |
| VIS-03 paleta, fonte e44px | :24 `c.altura >=44`; :26 `c.fonte ===true`; :44 variáveis exatas; :45 cor computada rgb82/97/107 | Sim |
| VIS-04 foco sólido3px/offset3px/cores | :33–35 foco real e array exato; :43 input, :61 botão escuro, :64 textarea, :77 select | Sim |
| VIS-05 dentro da tela1280/360 | :22 `medidas.pagina ===true`; :25 `c.dentro ===true`, em cada chamada conferir nos15 estados | Sim |
| VIS-06 mensagens e estados | :53 loading, :54 erro, :55 fundo distinto, :57 vazio; :92 arquivado e ausência de Nova etapa; dados/ações existentes preservados | Sim |
| VIS-07 sessão/rascunhos/sem chamadas de estilo | :66 rascunho quadro; :75/76 cache/calls estáveis na conferência; :81 demanda; :88 comentário | Sim |

Mapeamento reverso: o único teste :7, com helpers :12/31, cobre VIS-01–07; asserts de preparação de API comprovam que os dados fictícios são válidos antes da medição. Sem testes de domínio duplicados, dependências, mudanças de API/banco ou testes existentes alterados. Verificação de controles cobre botões/inputs/selects/textareas, incluindo controles desabilitados; formulários da mesma família mantêm as classes existentes. Testes focados da sessão vigentes, sem alteração de JS desdeT1.

Adequação A/B/C/D: resultados da spec concretos, casos necessários, medidas em navegador real, fluxo preservado; padrões Playwright e coding-principles existentes. Verifier independente ainda pendente. Cinco invocações acumuladas do autor (duas Vitest, três browser); nenhuma suíte completa Java/MySQL/Vitest.

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
