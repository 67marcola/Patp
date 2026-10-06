# Validation: marca-login PASS

**Result:** PASS técnico independente. MAR-01–04 atendidos; 222 Vitest e 3 E2E existentes passaram. As três falhas visuais compiláveis foram detectadas. UAT humano continua pendente.

**Data:** 2026-10-05. **Spec:** `.specs/features/marca-login/spec.md`. **Range:** `39ea59c..f4321c3` na branch `testes`. **Autor:** root. **Verificador:** subagente novo `/root/verify_marca_login`, autor diferente do verificador.

O verificador releu a spec e as referências completas `SKILL.md`, `validate.md`, `coding-principles.md` e `sub-agents.md`. Reexecutou os gates, derivou as assertions dos resultados MAR-01–04 e abriu suas próprias quatro capturas com `view_image`. Evidência do autor não foi usada como resultado desta validação. Artefatos físicos desta execução: `C:/Users/Marco/AppData/Local/Temp/creral-marca-verifier-muw1mb5z`.

## Escopo e tarefa

T1 está concluída no commit atômico `f4321c3a5fb1e9c7d5e61be52051a089a6c65504`. Design/tasks dispensados por tamanho: três arquivos de apresentação, uma remoção óbvia. A execução T1 está documentada em `evidence.md`.

O diff de aplicação contém **0 linhas adicionadas e 45 removidas**: 27 em `frontend/src/index.css`, 9 em `frontend/src/pages/Login.jsx` e 9 em `frontend/src/pages/Cadastro.jsx`. Só foram removidos os dois blocos `login-logo` e as duas regras CSS exclusivas. Os outros três caminhos alterados são documentos `.specs`.

A auditoria de metadados mode/type/blob comparou toda a árvore dos dois commits: **660 caminhos fora do escopo idênticos**, incluindo **96 caminhos próprios de aplicação/teste/configuração** e **28 caminhos de testes/configuração de testes**. Não há arquivo externo adicionado, removido ou alterado. Os trechos anteriores ao render, incluindo handlers, são iguais. Assertions físicas: `C:/Users/Marco/AppData/Local/Temp/creral-marca-verifier-muw1mb5z/audit-scope.mjs:9` (`after.get(path) === before.get(path)`), `:15` (conjunto de arquivos de aplicação igual ao escopo), `:16` (adições = 0), `:17` (remoções = 45) e `:20` (prefixos dos handlers iguais). Valores registrados em `audit-scope.log`.

Nenhum teste de repositório foi criado, removido, reescrito, enfraquecido ou desabilitado. A inspeção pontual permanece em TEMP. A superfície alterada é visual e reversível; a profundidade proporcional do sensor é de três falhas visuais. Não houve alteração de lógica de autenticação ou backend.

## Critérios derivados da spec

| AC | Resultado exigido e assertion física | Resultado observado |
| --- | --- | --- |
| MAR-01 | Login sem PM/bloco vazio; primeiro conteúdo `H1/Bem-vindo`. `C:/Users/Marco/AppData/Local/Temp/creral-marca-verifier-muw1mb5z/inspect-and-sensor.mjs:73`: `assert.equal(result.pm, false)`; `:74`: `firstTag === "H1"`; `:75`: `firstText === "Bem-vindo"`; `:76`: `logoCount === 0`. | PASS nos dois viewports: PM=false, primeiro=H1/Bem-vindo, logoCount=0. JSX resultante em `frontend/src/pages/Login.jsx:50`. |
| MAR-02 | Cadastro sem PM/bloco vazio; primeiro conteúdo `H1/Criar cadastro`. Mesmas assertions físicas `inspect-and-sensor.mjs:73`, `:74`, `:75` e `:76`, com título `Criar cadastro`. | PASS nos dois viewports: PM=false, primeiro=H1/Criar cadastro, logoCount=0. JSX resultante em `frontend/src/pages/Cadastro.jsx:83`. |
| MAR-03 | Ambas as telas em 1280x800 e 375x812 sem overflow horizontal; controles dentro da largura e legíveis por scroll. `C:/Users/Marco/AppData/Local/Temp/creral-marca-verifier-muw1mb5z/inspect-and-sensor.mjs:79`: `scrollWidth <= width`; `:80`: campos=2/5; `:81`: botões=2; `:82`: `left >= -1 && right <= width + 1`; `:83`: todos os controles visíveis após `scrollIntoViewIfNeeded`. A tolerância de 1 px só cobre arredondamento de retângulos de controles; a largura total não tem tolerância. | PASS nas quatro combinações: scrollWidth=1280 ou 375, exatamente a largura do viewport. Todos os controles passaram em largura e acesso vertical. As quatro imagens foram inspecionadas pelo verificador: títulos/campos/ações legíveis, sem marca ou caixa vazia. |
| MAR-04 | Preservar entrada direta após cadastro, sessão/quadro da mesma conta em reload/saída/login, teclado e erros/pending existentes. Assertions abaixo, executadas nas suítes existentes. | PASS: autocadastro real com H2/Edge passou, além dos 222 Vitest e outros dois E2E. Os 28 arquivos de teste/configuração e todos os fontes fora do escopo são idênticos no range. |

A evidência funcional de MAR-04 tem valores, payloads e estados, além de chamadas:

- `frontend/e2e/autocadastro.spec.js:10`: controle recebe foco por Tab; `:53`: lista visível após cadastro; `:54`: único POST de cadastro com valores exatos; `:55`: cache igual a token/usuário retornados.
- `frontend/e2e/autocadastro.spec.js:71`: quadro tem nome/descrição e criador da mesma conta; `:74`: quadro visível depois do reload; `:75`: cache permanece igual; `:76`: nenhum login adicional.
- `frontend/e2e/autocadastro.spec.js:83`: saída limpa token/usuário; `:88`: mesmo quadro visível após login manual; `:89`: payloads exatos de cadastro/login; `:94`: usuário permanece igual; `:99` e `:102`: API confirma conta e quadro.
- `frontend/src/pages/Cadastro.test.jsx:53`: mensagens de validação iguais ao esperado; `:83`, `:84` e `:85`: cinco campos e duas ações bloqueados enquanto pending; `:88`: somente um cadastro enviado; `:91`: mensagem de erro exata; `:29` e `:30`: rascunho preservado e campos liberados.
- `frontend/src/pages/Cadastro.test.jsx:102`: mensagens HTTP existentes preservadas; `:117`: mensagem de cadastro incerto exata; `:126`: retorno por teclado funciona. `frontend/src/AppSessao.test.jsx:43`, `:44`, `:48`, `:49` e `:52`: sessão, saída e login conservam os estados existentes.

Nenhum gap de precisão ou cobertura foi encontrado na superfície desta remoção. Não existe nova rota ou regra de domínio que exija nova matriz de casos.

## Capturas e métricas próprias

| Tela / viewport | PM / logo | Primeiro filho | Largura documento | Campos + botões | Acesso vertical |
| --- | --- | --- | --- | --- | --- |
| Login 1280x800 | false / 0 | H1 / Bem-vindo | 1280 | 2 + 2 | todos=true |
| Cadastro 1280x800 | false / 0 | H1 / Criar cadastro | 1280 | 5 + 2 | todos=true |
| Login 375x812 | false / 0 | H1 / Bem-vindo | 375 | 2 + 2 | todos=true |
| Cadastro 375x812 | false / 0 | H1 / Criar cadastro | 375 | 5 + 2 | todos=true |

Capturas: `C:/Users/Marco/AppData/Local/Temp/creral-marca-verifier-muw1mb5z/login-1280x800.png`, `cadastro-1280x800.png`, `login-375x812.png` e `cadastro-375x812.png`. Dados completos, incluindo cada retângulo, estão em `inspection-results.json`; execução em `inspection.log:1`, `:2`, `:3` e `:4`.

## Gates próprios

Executados em sequência, todos com exit 0:

| Gate | Comando | Resultado / evidência |
| --- | --- | --- |
| Frontend | `npm.cmd test` | 11 arquivos, 222 testes PASS; 0 falhas, 0 skips. `C:/Users/Marco/AppData/Local/Temp/creral-marca-verifier-muw1mb5z/vitest.log:9` e `:10`. |
| Build | `npm.cmd run build -- --outDir "C:/Users/Marco/AppData/Local/Temp/creral-marca-verifier-muw1mb5z/build"` | PASS, 130 ms. `C:/Users/Marco/AppData/Local/Temp/creral-marca-verifier-muw1mb5z/build.log:25`. Diretório novo; `frontend/dist` preservado. |
| Lint | `npm.cmd run lint` | PASS, exit 0; `C:/Users/Marco/AppData/Local/Temp/creral-marca-verifier-muw1mb5z/lint.log`. |
| E2E | `npm.cmd run test:e2e -- --output="C:/Users/Marco/AppData/Local/Temp/creral-marca-verifier-muw1mb5z/e2e-output"`, JAVA_HOME=`C:/Program Files/Java/jdk-25.0.2` | 3 testes, um por arquivo/ambiente H2 separado: autocadastro 17,0 s, etapas 18,3 s, gerenciamentos 15,7 s; 0 falhas, 0 skips. `C:/Users/Marco/AppData/Local/Temp/creral-marca-verifier-muw1mb5z/e2e.log:23`, `:37` e `:51`. |

Contagem antes/depois: **222 → 222 Vitest e 3 → 3 E2E, delta 0**, sustentada pela identidade dos arquivos de testes dos dois commits e pela execução atual. Maven foi usado somente pelo helper existente para preparar o H2 fictício; não foi executado `Maven verify`, MySQL configurado ou aplicativo normal.

## Sensor de discriminação

A cópia compilada `build/assets/index-DbpqVClK.js` foi lida em memória. Cada falha inseriu um nó JSX-runtime antes do H1 na array de filhos de um único cartão. O navegador recebeu a cópia por `page.route/fulfill` em contexto novo e o React fez o render. Não houve patch direto de DOM, alteração de fonte real, stash, worktree ou cópia de repositório.

`C:/Users/Marco/AppData/Local/Temp/creral-marca-verifier-muw1mb5z/inspect-and-sensor.mjs:91` exige parse exit 0 antes de render; `:67` exige ausência de erro de runtime. `:109` reaplica as mesmas assertions MAR-01/02 e `:110` exige que a falha seja detectada. Os dados reais do mutant e seu SHA estão em `inspection-results.json`.

| Falha isolada | Resultado real | Assertion que matou | Resultado |
| --- | --- | --- | --- |
| Restaurar PM no Login | firstTag=DIV, firstText=PM, pm=true, logoCount=1; parse exit 0, sem erro de runtime | `inspect-and-sensor.mjs:73`: PM deve estar ausente | KILLED, `inspection.log:5` |
| Restaurar PM no Cadastro | firstTag=DIV, firstText=PM, pm=true, logoCount=1; parse exit 0, sem erro de runtime | `inspect-and-sensor.mjs:73`: PM deve estar ausente | KILLED, `inspection.log:7` |
| Restaurar bloco vazio no Login | firstTag=DIV, firstText vazio, pm=false, logoCount=1; parse exit 0, sem erro de runtime | `inspect-and-sensor.mjs:74`: primeiro conteúdo deve ser H1 | KILLED, `inspection.log:9` |

**Sensor:** 3 injetados, 3 mortos por resultado visual, 0 sobreviventes. Cada contexto foi encerrado e a cópia em memória descartada. Após cada falha, o artefato original foi renderizado em contexto novo e passou novamente: **3/3 restores PASS**, `inspection.log:6`, `:8`, `:10`.

O verificador capturou **todo** `git status --porcelain=v1 --untracked-files=all` como bytes antes do sensor e depois do fechamento dos contextos/browser/servidor. Não filtrou caminhos nem normalizou texto. `inspect-and-sensor.mjs:124` e `:126` exigem igualdade. Os arquivos `porcelain-before.bin` e `porcelain-after.bin` são **idênticos: 231250 bytes, 3406 linhas, SHA-256 7c00fa1801471477c0d988eb7155679c872148517310389fa817ecf86dec84d6**. O relatório foi escrito somente depois dessa comparação.

Browser e servidor próprios fechados, confirmado por `inspection-results.json`: browserClosed=true/serverClosed=true. PID próprio **11200** não existe mais. Portas **4173, 4174 e 18082** sem listener. Processos do usuário continuam intactos: **8081/PID22976** e **5173/PID19612**. Logs, build e imagens TEMP foram preservados como evidência; nenhum cleanup destrutivo foi tentado nesta validação.

## Fechamento

Mudança mínima, cirúrgica e conforme os padrões existentes. As duas caixas inteiras saíram; os controles e handlers permanecem iguais. Os três edge cases MAR foram verificados. Sem gaps ou fix tasks.

UAT humano está **pendente**; PASS técnico não infere aprovação humana. Lessons: **nenhuma registrada**, pois esta execução não encontrou mutant sobrevivente, AC ausente/falha, gap de precisão ou desvio da spec. Fechamento determinístico executado: `python .agents/skills/tlc-spec-driven/scripts/validate_state.py marca-login`, **PASS, exit 0, 0 erros**.
