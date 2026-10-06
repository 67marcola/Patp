# Retirada de PM: execução

## Plano atômico

T1: retirar os dois blocos e seus estilos exclusivos; arquivos Login.jsx, Cadastro.jsx, index.css; conferir inspeção visual/DOM desktop e celular, gate frontend existente; commit `fix(ui): remove PM from authentication screens`. Design e tarefas formais dispensados: três arquivos de apresentação, sem mudança de comportamento.

## Pré-implementação T1

- Suposições: o usuário já autorizou retirar PM. Não inserir outra marca. Os handlers, campos, callbacks e estilos gerais ficam fora da alteração.
- Arquivos: frontend/src/pages/Login.jsx, frontend/src/pages/Cadastro.jsx, frontend/src/index.css e documentos de evidência/traceabilidade desta feature.
- Sucesso: título vira primeiro conteúdo nas duas telas, nenhuma marca/espaço decorativo, quatro capturas sem overflow horizontal, 222 testes frontend e 3 E2E anteriores intactos/passando, build TEMP/lint sem erro.
- Auditoria somente leitura independente: blocos Login.jsx:50 e Cadastro.jsx:83; únicas regras CSS index.css:621/629. rg não encontrou uso em outro componente. Cada cartão perde a altura do bloco; largura e padding existentes permanecem.
- Testes novos de repositório: nenhum para esta alteração visual reversível. Usar suítes existentes e verificação visual/DOM pontual derivada de MAR-01–03, com capturas e métricas arquivadas em TEMP. Nenhum teste anterior será modificado.

## Gate planejado

Em frontend: npm.cmd test; npm.cmd run build -- --outDir em pasta TEMP nova; npm.cmd run lint; npm.cmd run test:e2e (helper H2 isolado, JDK C:/Program Files/Java/jdk-25.0.2). Maven verify/MySQL não repetidos: Java e banco não mudam. Maven de preparo do helper não deve coincidir com outro Maven no mesmo target. Artefatos anteriores preservados; somente staging explícito de fontes/documentos desta tarefa.

## Resultados T1

Mudança: somente 45 linhas removidas em Login.jsx, Cadastro.jsx e index.css. Nenhum handler, campo, callback, regra de negócio ou teste anterior alterado. Sem SPEC_DEVIATION.

Logs e capturas em `C:/Users/Marco/AppData/Local/Temp/creral-marca-login-b682f512ca634a13a49857e4e2fb28a5`:

- `npm.cmd test`: exit 0, 222 testes/11 arquivos, zero falhas/skips, `vitest.log`.
- Build em TEMP novo: exit 0, bundle compilado; `build.log`. Aviso esperado de outDir externo não esvaziado, frontend/dist preservado. PowerShell registra stderr nativo do aviso; exit do processo permaneceu 0.
- `npm.cmd run lint`: exit 0, `lint.log`.
- `npm.cmd run test:e2e`: exit 0, três arquivos, 3/3 Edge/H2 PASS, zero falhas/skips/retries, `e2e.log`. Autocadastro, etapas e gerenciamentos executados sem alteração de assertions. Java somente compilado pelo helper, sem novo verify/MySQL.
- Verificação visual/DOM pontual: `visual-check.mjs` exit 0; quatro resultados em `visual-results.json`. PM false e primeiro conteúdo H1 esperado em todos; scrollWidth/viewportWidth 1280/1280 ou 375/375. Login mantém dois inputs; cadastro mantém cinco. Todos os campos/botões têm dimensões positivas e ficam dentro da largura visível. Zero chamadas API nessa inspeção; rotas API abortadas para não acessar aplicação normal.
- Quatro capturas inspecionadas: login-1280.png, cadastro-1280.png, login-375.png, cadastro-375.png. Formulários legíveis, sem símbolo/bloco vazio, corte ou rolagem horizontal. Preview próprio PID19828/4174 encerrado após conferir ownership; imagens/artefatos preservados. Fluxo funcional usa H2 dos E2E, não essa inspeção estática.

## Adequação direta

Prefixo visual físico: `C:/Users/Marco/AppData/Local/Temp/creral-marca-login-b682f512ca634a13a49857e4e2fb28a5/visual-check.mjs`.

| Critério | arquivo:linha + assertion | Resultado esperado | Coberto |
| --- | --- | --- | --- |
| MAR-01 | visual-check.mjs:39 `assert.equal(metrics.marcaPM,false)`; :40 `assert.equal(metrics.firstTag,"H1")`; :41 `assert.equal(metrics.firstText,heading)` | Login sem PM/bloco anterior; heading Bem-vindo, definido :23 | Sim, dois viewports |
| MAR-02 | visual-check.mjs:39/40/41, mesmas assertions de resultado | Cadastro sem PM/bloco anterior; heading Criar cadastro :23 | Sim, dois viewports |
| MAR-03 | visual-check.mjs:42 `assert.ok(metrics.scrollWidth <= metrics.viewportWidth)`; :43 inputs 2/5; :44 `assert.ok(metrics.controls.every(control => control.width > 0 && control.height > 0 && control.x >= 0 && control.right <= metrics.viewportWidth))` | Quatro combinações sem overflow horizontal/controles cortados; todos os campos presentes | Sim e capturas inspecionadas |
| MAR-04 fluxo real | frontend/e2e/autocadastro.spec.js:53 `expect(...Gerenciamentos...).toBeVisible()`; :54 `expect(posts).toEqual([{path:"/api/usuarios/cadastro",body:{nome:"Nova Automática",setor:"Testes",email,senha}}])`; :75 cache exato; :82 login visível; :83 cache null; :94 usuário igual; :102 quadro com ID/nome/descrição/criador iguais | Cadastro direto, mesma conta/quadro após reload/login, logout limpa cache | Sim, E2E anterior executado intacto |
| MAR-04 teclado/erros/pending | frontend/e2e/autocadastro.spec.js:10 `expect(controle).toBeFocused()` antes de Tab/Enter; frontend/src/pages/Cadastro.test.jsx:53 texto exato das validações; :83/84/85 campos/ações disabled; :88 só um cadastro; :91 erro exato; :29/30 campos preservados/enabled; frontend/src/AppSessao.test.jsx:75 mensagem storage exata, :78/79 cache anterior restaurado | Controles e tratamentos existentes permanecem | Sim, suíte existente executada |

## Adequação reversa

| Assertions físicas usadas | Mapeamento | Manter |
| --- | --- | --- |
| visual-check.mjs:39–41 | MAR-01/02: marca ausente e primeiro conteúdo correto | Sim, verificação visual pontual TEMP |
| visual-check.mjs:42–44 | MAR-03: dimensões/largura/campos | Sim, verificação pontual TEMP |
| visual-check.mjs:49 `assert.equal(apiRequests.length,0)` | Restrição operacional explícita: não acessar aplicação normal na inspeção | Sim |
| autocadastro.spec.js:10/53/54/75/82/83/94/102 | MAR-04: regressão funcional/teclado e valores da mesma conta/quadro | Sim, testes anteriores intactos |
| Cadastro.test.jsx:29/30/53/83–85/88/91, AppSessao.test.jsx:75/78/79 | MAR-04: regressão de erro/pending/cache | Sim, testes anteriores intactos |

Checks A/B/C/D: resultados concretos, não apenas chamadas; todos os novos checks visuais têm critério, nenhum teste novo de implementação. Suítes anteriores são regressões de funcionalidades já entregues, preservadas integralmente. Princípios da skill e padrões do projeto seguidos. Veredito de adequação do autor: MAR-01–04 atendidos no gate e inspeção, sem gap encontrado.

**Status T1:** complete após gate e adequação. Implementação e evidências do autor no commit local `f4321c3`.

## Encerramento técnico

O Verificador independente confirmou MAR-01–04, 4/4 PASS, com relatório físico em `validation.md`. Seus próprios gates passaram: 222 Vitest em 11 arquivos, build em TEMP, lint e 3 E2E Edge/H2, sem falhas ou skips. As quatro combinações de tela e viewport foram medidas e inspecionadas em capturas próprias.

O sensor detectou 3/3 falhas visuais compiláveis: PM recolocado no login, PM recolocado no cadastro e bloco vazio antes do título. As cópias do JavaScript foram executadas em memória, sem alterar arquivos do projeto; os três controles originais voltaram a PASS. Comparação integral do porcelain antes/depois: 231250 bytes, 3406 linhas, SHA256 `7c00fa1801471477c0d988eb7155679c872148517310389fa817ecf86dec84d6`, byteigual. Contextos e preview próprios encerrados; serviços do usuário preservados. Nenhuma nova lição ou tarefa de correção encontrada.

`validate_state.py marca-login` passou na execução independente e na conferência do autor. UAT humano continua pendente e não é inferido da autorização para continuar. As perguntas de movimentação/finalização permanecem abertas em `STATE.md`.
