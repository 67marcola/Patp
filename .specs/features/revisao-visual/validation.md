# Revisao visual: verificacao independente

## Validation: PASS

7/7 criterios correspondem aos resultados definidos. Gates focados aprovados e sensor visual morto pelo teste existente. Aceite visual humano continua pendente; este resultado cobre a verificacao automatizada e a inspecao independente do diff.

**Date:** 2026-10-14. **Diff range:** f75e675..d92b3ee. **HEAD:** d92b3ee7daabdf8c1fa9ed5b84817ed34ee8a3f5.
**Verifier:** subagente independente, distinto do autor. Fonte: spec.md aprovada, coding-principles.md e validate.md da skill TLC.

## Entrega e escopo

T1 (3bcb251), T2 (3650f34) e T3 (d92b3ee) implementados. Tres passos implicitos em spec.md, sem tasks.md formal. O diff muda identidade textual, metadados, associacao de rotulos e CSS; acrescenta tres testes unitarios e um cenario de navegador. Nenhum arquivo de teste antigo, dependencia, requisicao de aplicacao, regra de negocio ou servidor foi alterado. A preparacao de API no E2E cria somente dados ficticios no H2 temporario.

## Resultados por criterio

| AC | Resultado definido e evidencia independente | Resultado |
| --- | --- | --- |
| VIS-01 | Creral no login/cadastro/cabecalho, titulos Bem-vindo/Criar cadastro, lang pt-BR e aba Creral \| Gestao de demandas. `frontend/src/RevisaoVisual.test.jsx:9`, `frontend/src/RevisaoVisual.test.jsx:17`, `frontend/src/RevisaoVisual.test.jsx:25`, `frontend/src/RevisaoVisual.test.jsx:34` e `frontend/src/RevisaoVisual.test.jsx:35`: textos/headings e `toContain` exatos, incluindo acentos no arquivo. `frontend/e2e/visual.spec.js:37`, `frontend/e2e/visual.spec.js:47` e `frontend/e2e/visual.spec.js:59` confirmam documento e visibilidade no navegador. | PASS |
| VIS-02 | Clicar E-mail/Senha foca o campo associado; autocomplete username/current-password. `frontend/src/RevisaoVisual.test.jsx:13`: `document.activeElement === input` e `input.autocomplete === auto`; `frontend/e2e/visual.spec.js:39` a :42 confirma foco e atributos. O diff de Login.jsx conserva validacao e envio. | PASS |
| VIS-03 | Paleta #14232f/#5db28f, secundario #52616b, fonte herdada e altura >=44px. `frontend/e2e/visual.spec.js:44` compara array exato e :45 compara cor computada rgb(82,97,107); :24 exige altura >=44 e :26 fonte igual ao body em todos os controles visiveis, inclusive desabilitados. O diff nao modifica disabled. | PASS |
| VIS-04 | Contorno solid 3px, offset 3px, #147d64 claro e #8be0bb no cabecalho. `frontend/e2e/visual.spec.js:33` exige foco; :35 compara array exato com focus-visible, estilo, medidas e cor computada apos Tab/Shift+Tab. Chamadas :43 input, :59 botao escuro, :63 textarea e :78 select. `frontend/src/index.css:1556` aplica a regra comum aos quatro tipos; :1564 especializa cabecalho. Sem alteracao de ordem DOM. | PASS |
| VIS-05 | Sem overflow da pagina e controles dentro da viewport em 1280/360. `frontend/e2e/visual.spec.js:14` usa ambos tamanhos; :22 exige scrollWidth <= innerWidth e :25 limites do controle dentro da tela. Quinze estados, 30 capturas: login, cadastro, carregando, erro, lista vazia, formulario de quadro, lista preenchida, quadro, editor de etapa, editor de demanda, mover, arquivo de demanda, comentarios, atividade e arquivado. Nomes de quadro/etapa com 100 caracteres, numero/cliente com 180 e descricao com 240 em :66/:67/:70. Inspecao independente de mover-360.png confirma quebra e acoes visiveis. | PASS |
| VIS-06 | Mensagens e acoes preservadas em carregando/vazio/erro/arquivado, com distincao visual. `frontend/e2e/visual.spec.js:54` exige texto de status; :55 alerta com erro original; :56 fundo rgb(255,245,244); :58 heading vazio; :57 executa Tentar atualizar; :92 exige aviso de somente consulta e ausencia de Nova etapa. `frontend/src/index.css:1575` a :1577 distingue erro, arquivado e vazio. O diff nao muda permissoes nem estados. | PASS |
| VIS-07 | Sessao, acoes e rascunhos conservados, sem requisicoes/dependencias/backend novos. `frontend/src/RevisaoVisual.test.jsx:28` compara token e usuario integral e :29 exige uma consulta; `frontend/e2e/visual.spec.js:64`, :81 e :88 exigem rascunhos exatos; :76 exige numero de chamadas estavel ao conferir layout e :77 cache de sessao igual. Navegacao real login/lista/quadro/formularios cobre as acoes. Inspecao completa do diff confirma ausencia de alteracoes no servidor, banco e dependencias. | PASS |

Nenhum criterio sem evidencia ou lacuna de precisao. A verificacao nao declara certificacao completa de acessibilidade. Todos os testes novos se ligam a VIS-01/02/07 (unitarios) ou VIS-01 a VIS-07 (E2E); asserts de API apenas validam as precondicoes do cenario. As camadas de dominio/rotas nao sofreram alteracao nesta entrega.

## Gates focados

Executados sobre copia fisica TEMP do frontend e backend, com node_modules copiado integralmente, sem junction ou cache compartilhado. JDK 25.0.2, Node v24.15.0, Maven 3.9.16 e Edge; H2 em memoria create-drop. O runner compila classes de teste para iniciar H2, sem executar testes Java.

| Comando no frontend isolado | Resultado valido |
| --- | --- |
| `npm test -- src/RevisaoVisual.test.jsx` | 3/3 PASS, 2,86s; zero skips |
| `npm run lint` | exit 0 |
| `npm run build -- --outDir <TEMP>/build` | exit 0 |
| `npm run test:e2e -- visual.spec.js` | 1/1 PASS, 18,3s total; cenario 6,2s; zero skips |

Uma primeira preparacao omitiu diretorios dist internos de dependencias ao excluir o dist da aplicacao. Unit e navegador encerraram por ERR_MODULE_NOT_FOUND antes de executar casos. Essa tentativa nao constitui controle valido nem falha de produto. A copia fisica completa corrigiu a infraestrutura; os comandos acima passaram sem mudar fontes, testes ou expectativas.

Integridade de testes: zero remocoes e zero alteracoes de testes anteriores no diff. Nos arquivos adicionados, baseline 0 e atual 4 casos (3 unitarios +1 E2E). Nenhuma baseline historica ou suite completa foi executada. Contagem de invocacoes de testes: autor 5 + Verifier 5 (2 tentativas de preparo, 2 controles validos, 1 sensor) =10, reducao de 50% sobre a referencia de20 de AD-035. Lint/build e verificacoes documentais nao sao invocacoes de testes.

Logs preservados em `C:/Users/Marco/AppData/Local/Temp/creral-verifier-3beae32f1c6942c19e849c8aac6e80c2`: unit-control-valid.log, browser-control-valid.log, lint-valid.log, build-valid.log, browser-mutant.log e integrity.txt. Capturas validas: `C:/Users/Marco/AppData/Local/Temp/creral-visual-T5uGra`.

## Sensor de discriminacao

Profundidade leve, 1 mutacao proporcional a apresentacao nao critica. Controle valido: o E2E completo acima passou sobre a mesma copia antes da mutacao.

| Mutacao exclusivamente no scratch | Evidencia de deteccao | Resultado |
| --- | --- | --- |
| `frontend/src/index.css:1563`: contorno requerido de 3px trocado por 1px; estilo, offset e cor preservados | Mesmo `frontend/e2e/visual.spec.js:35`, chamado em :43, recebeu 1px em vez de 3px apos timeout padrao 5000ms. Runner exit1, 1 teste falhou por diferenca exata de largura, sem falha de infraestrutura. | KILLED |

1 injetado, 1 morto, 0 sobreviventes. Nenhum teste espelho, mudanca de timeout ou expectativa foi introduzido. Scratch frontend/sistema/build removido com caminhos absolutos previamente confinados ao diretorio temporario. Baselines capturados antes de qualquer sensor/gate: HEAD, porcelain integral e SHA256 de todos os fontes/testes/CSS/HTML reais e manifests do frontend. Apos cleanup, `HEAD_MATCH=True STATUS_MATCH=True HASH_MATCH=True SCRATCH_REMOVED=True`. Gerados preexistentes reais preservados. A unica escrita posterior no repositorio e este relatorio.

## Qualidade, limites e fechamento

Mudancas cirurgicas, estilo existente, sem abstracoes novas ou escopo adicional. `git diff --check f75e675..d92b3ee` aprovado. Guideline aplicada: `.agents/skills/tlc-spec-driven/references/coding-principles.md`. Os assertos conferem resultados da spec, incluindo medidas computadas em navegador real. Sem achados de produto, mutantes sobreviventes, criterios descobertos ou SPEC_DEVIATION; nenhuma licao nova necessaria.

Aceite humano pendente conforme spec. Sem gaps para tarefa corretiva. VIS-01 a VIS-07 recomendados como Verified; atualizacao de status permanece com o orquestrador. Gate documental de fechamento: validate_state.py revisao-visual.
