# Validation: Gráficos por quadro PASS

**Result**: PASS, 9/9 critérios com resultados observáveis correspondentes à spec. Sem gaps técnicos encontrados. UAT humano permanece pendente.

**Data**: 2026-10-14. **Verifier**: subagente independente, diferente do autor. **Spec**: `.specs/features/graficos-quadro/spec.md`. **Diff**: `feb0eea..68d427d`; HEAD conferido `68d427d23b60bcfec522b2324c081d9d49a4402f`.

Os três passos implícitos estão entregues: componente (`6b9f6ad`), integração (`bc631be`) e documentação (`68d427d`). Sem tasks.md, conforme escopo médio aprovado. Este relatório deriva da leitura da spec, diff e assertions, seguida de execução própria. Evidências do autor não substituíram os gates independentes.

## Critérios e resultados

Abreviações somente nesta tabela: C = `frontend/src/components/GraficosQuadro.test.jsx`; Q = `frontend/src/pages/QuadroGraficos.test.jsx`; E = `frontend/e2e/graficos.spec.js`. As citações abaixo preservam os caminhos completos para rastreabilidade.

| Critério | Resultado definido na spec e evidência de assertion | Resultado |
| --- | --- | --- |
| GRAF-01 | Recolhido, alternável, identificado e foco preservado: `frontend/src/pages/QuadroGraficos.test.jsx:28` exige `aria-expanded === "false"`; linhas 30–33 exigem true, `aria-controls === grafico().id`, `activeElement === botao` e fechamento por Enter. `frontend/e2e/graficos.spec.js:35` confirma estado inicial e linha 36 confirma foco real. | PASS |
| GRAF-02 | Literais Em andamento/Concluido/Cancelado, desconhecidos separados: `frontend/src/components/GraficosQuadro.test.jsx:16` exige exatamente `["Em andamento2", "Concluídas1", "Canceladas1", "Não informado2"]` para seis demandas, incluindo null/Antigo. Linha 37 exige somente três estados quando não há desconhecidas. | PASS |
| GRAF-03 | Ordem recebida, IDs distintos com nomes iguais, zero e órfãos: `frontend/src/components/GraficosQuadro.test.jsx:17` exige `["Trabalho2", "Trabalho1", "Concluídos1", "Cancelados1", "Etapa não identificada1"]`; linha 29 inclui todas as etapas zeradas; linha 35 exige órfã com cadastro vazio. | PASS |
| GRAF-04 | Não arquivadas=6 e arquivadas=1: `frontend/src/components/GraficosQuadro.test.jsx:18` e linha 19 exigem esses textos; arrays exatos das linhas 16/17 somam seis em cada gráfico e excluem a arquivada Em andamento. `frontend/e2e/graficos.spec.js:46` exige 2/1 depois de arquivar; linha 47 exige estados 0/1/1. | PASS |
| GRAF-05 | Mesmo snapshot após alteração e recolhimento na troca: `frontend/src/pages/QuadroGraficos.test.jsx:56` e linha 57 exigem 1/1 da resposta da ação; linhas 40/42 exigem ausência e recolhimento durante/depois da troca; linha 43 exige zero do novo quadro. `frontend/src/components/GraficosQuadro.test.jsx:37` exige novos estados após rerender. | PASS |
| GRAF-06 | Sem snapshot/erro/conflito não mostra totais antigos: `frontend/src/pages/QuadroGraficos.test.jsx:21` exige botão desabilitado e região ausente; linhas 23/24 exigem erro textual e bloqueio; linha 61 exige região ausente e bloqueio após conflito; linha 64 exige recuperação com total 1. | PASS |
| GRAF-07 | Acesso de leitura em ativo/arquivado e sem consulta extra: `frontend/src/pages/QuadroGraficos.test.jsx:27` habilita botão com `podeAdministrar:false, arquivado:true`; linha 35 exige somente duas consultas (inicial falha + retry), apesar das alternâncias. `frontend/src/pages/QuadroGraficos.test.jsx:77` exige uma consulta no formulário; `frontend/e2e/graficos.spec.js:53` e linha 54 exigem leitor real em quadro arquivado com totais 2/1. Sem escrita nem requisição no componente, `frontend/src/components/GraficosQuadro.jsx:19`. | PASS |
| GRAF-08 | Vazio não equivale a erro: `frontend/src/components/GraficosQuadro.test.jsx:27` exige mensagem literal `Nenhuma demanda não arquivada neste quadro.`; linhas 28/29 exigem estados/etapas com zero; linha 30 exige todas as larguras `0%`. | PASS |
| GRAF-09 | Rótulos e valores textuais exatos (C:16/17); `frontend/src/components/GraficosQuadro.test.jsx:21` exige barras aria-hidden; linha 22 exige proporções exatas; linhas 38/41 exigem nome HTML literal e nenhuma imagem. `frontend/src/pages/QuadroGraficos.test.jsx:74` e linha 76 preservam texto do formulário. `frontend/e2e/graficos.spec.js:42` preserva comentário real; linha 56 exige `scrollWidth <= clientWidth`, limites dentro da viewport de 360px. | PASS |

Não há gap de precisão na spec para estes resultados. Os seis testes Vitest e o cenário E2E mapeiam aos critérios acima; preparações de conta/API do E2E verificam seus códigos de resposta antes de medir os gráficos.

## Gate independente e integridade

| Comando, cwd frontend | Execuções | Resultado |
| --- | --- | --- |
| `npm test -- src/components/GraficosQuadro.test.jsx src/pages/QuadroGraficos.test.jsx` | 1 | 2 arquivos, 6 testes passaram, 0 falhas, 0 ignorados |
| `npm run lint` | 1 | exit 0 |
| `npm run build -- --outDir C:/Users/Marco/AppData/Local/Temp/graficos-verifier-build` | 1 | exit 0 |
| `npm run test:e2e -- graficos.spec.js` | 1 | 1 cenário passou, 0 falhas/ignorados, 28s; ambiente Edge/H2 isolado, sem testes Java |

Logs em `C:/Users/Marco/AppData/Local/Temp/graficos-verifier-{unit,lint,build,e2e}.log`. Captura mobile conferida visualmente em `C:/Users/Marco/AppData/Local/Temp/creral-graficos-6nR74a/arquivado-360.png`: rótulos longos quebram linha, números continuam legíveis, seção cabe na tela. A mesma execução confirmou que comentários não impedem acionar o botão de gráficos.

Integridade pelo diff, sem refazer baseline histórica: três testes C + três Q + um E novos; nenhum teste anterior removido, ignorado ou enfraquecido. Total novo executado: seis Vitest e um E2E. Não se declara execução da suíte completa, dos 45 testes antigos ou de testes Java/MySQL pelo Verifier.

## Sensor leve em scratch

Scratch físico `C:/Users/Marco/AppData/Local/Temp/graficos-sensor-5f90a9694d694fe4aa57c14125295da6`, com src/config/package e cópia independente de node_modules. Robocopy exit 1 (arquivos copiados), zero reparse points; nenhum junction, hardlink ou cache compartilhado usado. Controle do scratch antes das mutações: os mesmos seis Vitest passaram, exit 0. Cada mutação foi restaurada antes da próxima.

| Mutação comportamental | Teste único pertinente | Resultado |
| --- | --- | --- |
| `frontend/src/components/GraficosQuadro.jsx:20`: contagem com `status !== statusEsperado`, trocando a correspondência literal | `npm test -- src/components/GraficosQuadro.test.jsx -t "totais exatos"` | Morta, exit 1; assertion C:16 recebeu contagens erradas. 1 falhou, 2 não selecionados pelo filtro. |
| `frontend/src/pages/Quadro.jsx:353`: remove `!bloqueado` da condição de mostrar seção, exibindo snapshot antigo após conflito | `npm test -- src/pages/QuadroGraficos.test.jsx -t "conflito oculta"` | Morta, exit 1; assertion Q:61 recebeu região quando esperava null. 1 falhou, 2 não selecionados pelo filtro. |

**Sensor**: 2 injetadas, 2 mortas, 0 sobreviventes. Nenhum skip adicionado aos testes. Três invocações no scratch (controle + duas falhas intencionais), proporcionais ao recurso de consulta.

Antes/depois do sensor: HEAD idêntico, `git status --porcelain` idêntico e cinco SHA256 idênticos. Baselines persistidos em TEMP `graficos-sensor-{head.txt,porcelain.txt,hashes.json}`. Arquivos gerados preexistentes foram preservados. Cleanup usou caminho absoluto validado como filho de TEMP com prefixo `graficos-sensor-`; `Test-Path` final=false.

| Arquivo real | SHA256 antes e depois |
| --- | --- |
| GraficosQuadro.jsx | BC7424AE9EBE0280C7133380E659ED8711130445590BA8CC631FD47C1A8769F5 |
| Quadro.jsx | FFF03446982A467DB71A3F7AFF0B1079289119202F495EACC61687424F2C4CAD |
| GraficosQuadro.test.jsx | C6B5D4612CB1A04E7CC55531B928E255501052FF8776CC0B15210E1A47B2935C |
| QuadroGraficos.test.jsx | 5F7F2CA8AF5B9A0B66F9FDEE1433D2CA4EB11E209CDB94778D475E526DC3F712 |
| graficos.spec.js | 7DF39674BB0EFBB2C2E40AF421A6793B7E655A59D2F730F29E902DD579CAA3A6 |

## Escopo, bordas e qualidade

- Bordas verificadas: nomes repetidos, null/status legado, etapa órfã/ausente, listas vazias, erro inicial, retry, conflito, atualização, troca de quadro, leitor arquivado, texto HTML literal, formulário/comentário aberto e 360px.
- Diff cirúrgico: componente/CSS e seis linhas de integração; sem API, banco, dependência ou rota nova. Não há abstração configurável de uso único nem mudança de domínio. Contagem usa arrays do snapshot existente e identidade de etapa.
- Padrões React/HTML/CSS existentes preservados. Guidelines aplicadas: `.agents/skills/tlc-spec-driven/references/coding-principles.md` e AD-035 em `.specs/STATE.md:284`. Nenhum AGENTS.md localizado no escopo.
- Cobertura de domínio servidor/rotas novas: N/A, não alterados. Cobertura da interface inclui sucesso, bordas e erro. Gaps ranqueados: nenhum.
- AD-035: Verifier realizou cinco invocações de testes (duas de produto + três de sensor), sem repetição. Somadas às cinco invocações documentadas do autor, são dez nesta entrega frente às pelo menos vinte gates anteriores documentadas pelo autor: redução de pelo menos 50% por invocações. A comparação não mede tokens, duração ou equivalência de volume de casos e não refez baseline histórica.

## Fechamento

Verificação técnica PASS, 9/9 ACs e sensor 2/2. UAT humano pendente; este relatório não representa aceite humano. Traceabilidade de GRAF-01 a GRAF-09 pode passar de Implementing para Verified pelo orquestrador. Clean PASS sem sinal novo: nenhuma lição adicionada. Próximo passo: registrar o diagnóstico e apresentar a consulta ao usuário.
