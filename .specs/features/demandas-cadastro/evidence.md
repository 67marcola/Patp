# Cadastro e leitura de demandas: evidências

## T6: fluxo real e roteiro operacional

PASS no gate do autor. A revisão independente da feature continua obrigatória. O teste e o roteiro operacional foram incorporados pelo commit do usuário `11e1307`, depois de T5 `f134fcb`; esta retomada preserva esse commit e registra adequação/status em um commit próprio, sem reescrever o histórico.

Assumptions: usar funcionário autenticado que não criou o quadro, dados fictícios e somente helper H2/Edge. Files: frontend/e2e/demandas.spec.js, docs/OPERACAO.md e evidência/spec/tasks. Success: cadastro mínimo/completo persistido, número global duplicado, pendência, recarga, arquivo, teclado e QA desktop/mobile. Nenhuma mudança de fonte ou de assertion foi necessária nesta conclusão.

Gate: `npm.cmd test` 440/440 em13arquivos; build Vite em pasta TEMP nova; `npm.cmd run lint` exit0; `npm.cmd run test:e2e` quatro testes Edge/H2 PASS, zero falhas/skips/retries. Autocadastro, etapas e gerenciamentos anteriores permanecem intactos. Logs completos em `C:/Users/Marco/AppData/Local/Temp/creral-demandas-frontend-6f5009ddd35e4cc59860034d2acf8c67`: vitest-final.log, build.log, lint.log, e2e-final.log. Build em subpasta `build`, sem remover dist. O gate Java anterior permanece390/390 H2; MySQL é uma seleção própria de145/145, não390.

### Check A: resultados e suficiência

Prefixo E=`frontend/e2e/demandas.spec.js`. Os valores mínimo/completo são definidos em E:93/95; as comparações integrais abaixo conferem valores de cada campo, IDs, etapa, estado e datas finais. O teste usa HTTP real, sem fabricar resposta do servidor; a interceptação de E:116 apenas suspende e depois continua a requisição real.

| AC / critério | file:line e assertion | Resultado da spec | Coberto |
| --- | --- | --- | --- |
| CAD-01/05/21 cadastro mínimo | E:129 `expect(minimaResponse.status()).toBe(201)`; E:135 `expect(minimoSalvo.demandas).toEqual([{ ...minimo, id: minimaId, etapaId: primeiraId, status: "Em andamento", dataConclusao: null, dataCancelamento: null, motivoCancelamento: null }])` | Persistência201,14campos flat, primeira etapa e estado inicial obrigatório | Sim |
| CAD-01/05/21 cadastro completo | E:156 status201; E:162 `expect(completoSalvo.demandas).toEqual([...minimoSalvo.demandas, { ...completo, id: completaId, etapaId: primeiraId, status: "Em andamento", dataConclusao: null, dataCancelamento: null, motivoCancelamento: null }])`; E:161 IDmaior | Todos campos persistidos e demandas ordenadas por ID | Sim |
| CAD-22/32 contagens e versão | E:137 versão1; E:138 counts `[1, 0, 0, 0]`; E:164 versão2; E:165 counts `[2, 0, 0, 0]`; E:218 payloadarquivo `{ versao: 2 }` | Um incremento por cadastro, cards/counts coerentes e versão aplicada ao cache real | Sim |
| CAD-26 funcionário não criador | E:102 `expect(inicial.gerenciamento).toMatchObject({ id: quadro.id, versao: 0, podeAdministrar: false, arquivado: false })`; E:107 Novaetapacount0; E:129 status201 | Criar permitido independentemente de podeAdministrar | Sim |
| CAD-28/40 formulário e foco | E:81 Númerofocused; E:82 ajuda inicialexata visível; E:83 comboboxcount0; E:150 preenche os oito labels; E:140/206 focoCriar | Campos definidos, etapa automática, teclado/foco e cancelamento sem cadastro | Sim |
| CAD-30 payload | E:126 `expect(posts).toEqual([{ path: ... , dados: { ...minimo, versao: 0 } }])`; E:157 `expect(completaResponse.request().postDataJSON()).toEqual({ ...completo, versao: 1 })`; E:238 lista completa dos três payloads | Exatamente oito campos e versão corrente, trim/opcionaisnull | Sim |
| CAD-31 pendência | E:121 oito camposdisabled; E:122 Cancelar/Sair/Criardisabled; E:123 Voltardisabled; E:124 Criandodisabled; E:126 payloadúnico apósEnter | Somente um POST, campos e navegação bloqueados | Sim |
| CAD-32 atualização confirmada | E:139 formulário count0; E:140 focoCriar; E:142 IDcard; E:143 articlecount1; E:218 arquivo usa versão2 | Fecharform, aplicar snapshot/card/cache e devolver foco | Sim |
| CAD-37 cartões reais | E:142/167 data-demanda-id; E:143 vínculo Comprarposte; E:144 status Em andamento; E:170 `toHaveText(valor)` com responsável/prioridade/datas DD/MM/YYYY/observações; E:173/174 buttons/headingscount0 | Exibir campos literais na etapa certa, sem controles de alteração | Sim |
| CAD-13/41 duplicidade global | E:189 status409; E:190 corpo `{ erro: "Já existe uma demanda com esse número." }`; E:191 alertliteral; E:197 versão0; E:198 demandas[]; E:199 countszeros; E:200 quadro originaligual | Outro quadro/caixa diferente recebe409 sem dados parciais | Sim |
| CAD-41 recarga | E:147 cardIDigual; E:148 `expect((await consultar(quadro.id)).demandas).toEqual(minimoSalvo.demandas)`; E:178/179 IDsiguais; E:180 snapshotinteiroigual | Mínimo/completo e identidade/etapa/counts persistem apósreload | Sim |
| CAD-41 erro preservado | E:192/193 `toHaveValue` dos campos; E:194 Criardisabled; E:195/204 postslength3; E:202 enabled apósrefresh; E:203 númeroigual | Mensagem/rascunho preservados, refreshmanual e nenhum POST extra | Sim |
| CAD-39 consulta arquivada | E:221 Criarcount0; E:222 avisoarquivo; E:223/224 IDs; E:228 `expect(arquivado.demandas).toEqual(completoSalvo.demandas)`; E:229 counts `[2,0,0,0]`; E:236 controlescount0; E:237 snapshotigual apósreload | Mesmos cartões arquivados somente leitura | Sim |
| T6 desktop/mobile | E:31 `expect(...scrollWidth <= ...clientWidth).toBe(true)`; E:32/35 todos limitesdosboxesinsideviewporttrue; E:152/175/230 fotografar | Formulário/cartões/arquivo sem corte horizontal em1280/375px | Sim |

### Check C: necessidade

| file:line e assertion | AC / critério | Manter |
| --- | --- | --- |
| E:13 foco; E:46 cadastro200; E:58 quadro201; E:65 consulta200; E:72/76/77 visibilidade | Pré-condições reais do fluxo CAD-41, teclado CAD-40 e regressões preservadas | Sim |
| E:81 foco, E:82 ajuda, E:83 ausência de seleção | CAD-28/40 formulário automático | Sim |
| E:102 permissão/versão; E:103 demandas[]; E:104 zeros; E:106 primeiraTRABALHO; E:107 semNovaetapa | CAD-26/41 funcionário inicia quadro real | Sim |
| E:121–126 disabled e payload exato único | CAD-30/31 pendência/normalização | Sim |
| E:129/133/134/135 status/ID/14campos; E:137/138 versão/counts; E:139/140 form/foco; E:142/143/144 card/vínculo/status | CAD-01/05/21/22/32/37/40 mínimo | Sim |
| E:147/148 ID/demandas apósreload | CAD-41 persistência mínima | Sim |
| E:156/157 status/payload; E:160/161 ID/ordem; E:162/164/165 campos/versão/counts; E:167/170 valores; E:172 HTMLliteral; E:173/174 semcontroles | CAD-01/05/21/22/30/32/37/41 completo e texto literal CAD-38 | Sim |
| E:178/179/180 IDs/snapshot apósreload | CAD-41 persistência completa | Sim |
| E:189/190/191 código/corpo/mensagem; E:192/193 campos; E:194/195 bloqueio/postslength; E:197/198/199/200 estado; E:202/203/204 refresh/draft/POSTs | CAD-13/33/34/41 erro global sem falso sucesso/retry | Sim |
| E:206 foco; E:208 logout; E:211 cardpermanece | CAD-40/41 cancelamento e troca de sessão do fluxo real | Sim |
| E:217/218 arquivo204/versão; E:221–229 controles/cards/estado/counts; E:234–239 IDs/ausência/snapshot/payloads | CAD-32/39/41 cache e consulta arquivada apósreload | Sim |
| E:31/35 nos três paresdesktop/mobile | T6 QAvisual e responsividade | Sim |

Adequação PASS: valores/campos/estado são assertados, não apenas chamadas. Cada assertion serve a um critério do corte ou pré-condição do E2E. Nenhum teste existente foi reduzido, removido ou desabilitado. Segue a matriz de tasks.md e o helper operacional existente; não há limiar adicional em AGENTS/CONTRIBUTING. Os casos HTTP de erro/formato/rollback/concorrência são aprofundados em T1/T2, e os estados de formulário/API em T3–T5; T6 comprova a integração real definida, sem replicar toda a suíte em navegador.

### QA visual e MySQL

O root inspecionou as seis capturas em `C:/Users/Marco/AppData/Local/Temp/creral-demandas-e2e-BcRvBD`: formulario-desktop/mobile, cartoes-desktop/mobile e arquivado-desktop/mobile. PASS: duas colunas no formulário desktop, uma no mobile; rótulos/ações legíveis sem sobreposição; números longos quebram linha; cartões exibem datas DD/MM/YYYY, texto com tags literalmente e observações com linhas; arquivo mantém cartões e aviso somente consulta. Campo date usa apresentação nativa do navegador, sem impor texto de data do cartão. As verificações DOM E:31/35 confirmam limites nas seis capturas. Teste humano permanece pendente.

MySQL TEMP8.0.43:145/145 selecionados PASS em `C:/Users/Marco/AppData/Local/Temp/creral-demandas-mysql-70e509769b814ba59a5a557b6b39cdd3/mysql-evidence.md` e `mysql-selected-verify-2.log`. Inclui UNIQUE1062 nas duas rotas, CHECK/500/rollback e corridas reais; fonte Java e24ce17 inalterada. Primeira tentativa falhou no bootstrap por URL truncada pelo cmd e não conta como prova funcional. URL via ambiente corrigiu a invocação. Instância fictícia33817 foi encerrada com proveniência conferida; serviços/banco configurado e contas reais preservados. T6 não fez limpeza de artefatos.

## T5: cartões e cadastro no quadro

PASS. Gate `frontend npm.cmd test`:440testes em13arquivos, zero falhas/skips.235anteriores/142T3/29T4 preservados,34casosT5 novos. Primeirogate439/439 antecedeu o caso adicional de cache real e ajuste CSS de observações/motivo emlargura completa; gatefinal440/440 depois dessas alterações. Nenhum Maven/E2E/build/serviço normal foi iniciado pelo worker.

Assumptions: criar independe de podeAdministrar e exige snapshot ativo com trabalho. Snapshot de mesmoquadro permanece visível no refresh e draft não é remontado; trocaid elimina estado anterior e ignora resposta tardia. Files: Quadro.jsx, QuadroDemandas.test.jsx, index.css responsivo de cartões, status/evidência. Success: permissão, zeroWorks, cadastro/cache/foco/erro/pending, isolamento, cartões14dados/literais/arquivo e controles anteriores.

### Check A: resultados e suficiência

Prefixo `frontend/src/pages/`; Q=QuadroDemandas.test.jsx. Payload emitido contém os oito valores explícitos Q:21–23 eversão0/7 nascomparações integrais. `conferirRascunho` Q:54 confere separadamente cada campo. Os14campos retornados são validados emT3 e exibidos/vinculados aqui.

| AC / critério | file:line e assertion | Resultado da spec | Coberto |
| --- | --- | --- | --- |
| CAD-01/08/26 qualquerautenticado | Q:69 `expect(cartao.dataset.demandaId).toBe("42")`; Q:70 vínculo4; Q:74 `expect(fetchMock.mock.calls.map(...)).toEqual([[...GET...],[...POST..., { ...dados, versao: 0 }]])` |Criador/admin/terceiro/semcriador autenticado cadastra sem exigiradmin | Sim |
| CAD-07/27 zeroWorks | Q:82 Criarprocessodisabledtrue; Q:83 mensagemexata conforme podeAdministrar; Q:85 Novaetapapresença; Q:87 formnull; Q:88 métodosGET |Sófinais impede cadastro e orienta criador/admin vsfuncionário | Sim |
| CAD-26/33 consulta pendente/erro | Q:94 botão disabledtrue; Q:96 campoa usente; Q:97 métodosGET |Carregamento/erro não abreformnemgrava | Sim |
| CAD-30 novecampos/versioncorrente | Q:74 payloadintegralcomversao0; Q:212 `expect(fetchMock.mock.calls.filter(...).map(...)).toEqual([{ ...dados, versao: 0 }, { ...dados, versao: 7 }])` |Somente8camposform+versãovigente emPOSTmanual | Sim |
| CAD-31 POSTúnico e bloqueios | Q:144 oitocamposdisabled; Q:146 controlesdisabled; Q:148 Voltardisabled; Q:151 voltarnãochamado; Q:152 payloadúnico; Q:154/Q:157 aoOcupartrue/false |POST único, navegação/Cancelar/etapas bloqueados atéresposta | Sim |
| CAD-31 Sairreal | Q:344 Sairdisabledtrue; Q:346 tokenpreservado; Q:347 usuáriopreservado; Q:350 Sairenabled; Q:352 login; Q:353 tokennull |App impede saída durantePOST e libera apóssucesso/erro | Sim |
| CAD-32 snapshot/cache/foco | Q:69 ID42; Q:71 formnull; Q:73 `expect(atualizar.mock.calls.map(([registro]) => registro)).toEqual([...versao0,...versao1])`; Q:76 focoCriar; Q:210 count2 |Fecharform, aplicarID/counts/metadata/versão, focoCriar semGETextra | Sim |
| CAD-32 cache real da lista | Q:387 Nenhumgerenciamentoativo; Q:388 mutações `toEqual([[...POST...,versao0],[...arquivar...,versao1]])`; Q:393 ID42; Q:396 consultas2 |Voltar usa versão retornada do cadastro paraarquivo e mantémcard naconsulta | Sim |
| CAD-33 erro/inseguro/draft/refreshexigido | Q:195 alertmensagem; Q:54 todosvalores; Q:197 anteriorvisível; Q:198 novoausente; Q:199 cacheversão0; Q:200 Criardisabled; Q:202 GET/POST |400/401/404/409/500/rede0/incompleto201/204 mantêmstate sem falso sucesso ouretry | Sim |
| CAD-33/34 refreshmanual/draft/version | Q:206 métodosGET/POST/GET; Q:207 cache0/7; Q:54 draft; Q:210 count2; Q:211 cache0/7/8; Q:212 payloadsversão0/7 |GETmanual preservadraft, aplica nova versão, sóPOSTmanual reenvia | Sim |
| CAD-34 arquivo/zeroWork/adminfalse | Q:229 Criardisabledigualbloqueado; Q:230 oitocamposdisabledigualbloqueado; Q:54 draft; Q:232 métodosGET/POST/GET |Arquivo/zerotrabalho impedemsubmitforçado; perdaadmin mantémcadastropermitido | Sim |
| CAD-33/34 refreshGETfalho | Q:247 antigoID42visível; Q:248 Criardisabledtrue; Q:249 cache0; Q:253 cache0/8; Q:254 métodosGET/POST/GET/GET; Q:54 draft |FalhaGETconservastate e repetiçãoGETsemretryPOST | Sim |
| CAD-24 pósCRUDetapas | Q:271 ID42; Q:272 `expect(...getByText("1 demanda").textContent).toBe("1 demanda")`; Q:273 métodosGET/metodo |POST/PUT/DELETE etapas mantêmcardID/countsemGETextra | Sim |
| CAD-35 trocaid semghost | Q:283 cardanteriornull; Q:284 títuloOutro; Q:286 artigosausentes; Q:287 cacheIDs9/10 |Cardantigo removido imediatamente antes denovoGETconfirmar | Sim |
| CAD-35 GETantigo/unmount | Q:298/Q:308 signalabortedtrue; Q:300 cardantigonull; Q:301 cacheIDs10; Q:310 atualizarsemchamada |Abort/ignore evita cache/cards de quadroanterior e apósunmount | Sim |
| CAD-35 POSTantigo | Q:324 cardantigonull; Q:325 `expect(atualizar.mock.calls.map(...)).toEqual([[9,0],[10,0]])` |Respostatardia de cadastro anterior não altera novoquadro/cache | Sim |
| CAD-37/38 id/numero/pessoa/etapa | Q:104 articlelookup comnomeexato D-42 — Maria; Q:105 ID42; Q:106 etapa20; Q:107 semartigoemPlanejamento |14camposflat preservam identidade e etapaId correta; strongsemheading | Sim |
| CAD-37/38 status/responsável/prioridade | Q:111 `expect(valorCartao(cartao, label)).toBe(valor)` com Status=Situação desconhecida,Responsável=Ana,Prioridade=Prioridade livre |Valores literais, inclusive status desconhecido | Sim |
| CAD-37/38 cinco datas | Q:111 com29/02/2020,01/01/2019,01/01/2018,04/03/2021,06/05/2022 |DatasISO exibidasDD/MM/YYYY porstring semfuso, sem alterarcontradiçãoantiga | Sim |
| CAD-37/38 observações/motivo | Q:111 comUma\nOutra/Motivo antigo; Q:130 obsHTML+10001porvalorexato; Q:132 motivausente |Texto/linhas preservados, motivo somente seexistente | Sim |
| CAD-37/38 ausências/escape/controles | Q:128 `expect(valorCartao(cartao, label)).toBe("Não informado")` paraoitolabels; Q:131 tagsnull; Q:133 tagSTRONG; Q:113 headingnull; Q:114 buttonnull; Q:115 stageheadings completos |Nullsindicamausência, textoHTML escapado, cartõesreadonly e únicoheading porstage | Sim |
| CAD-39 arquivo | Q:117 Criarausenteigualarquivado; Q:118 somenteGET; Q:393 ID42; Q:394 Criarnull; Q:395 avisoarquivo |Mesmoscards arquivados readonly | Sim |
| CAD-40 teclado/cancel/exclusividade/regressão | Q:46 focoNúmero; Q:166 focoCriar; Q:165 formfecha; Q:168 CriardisabledcomEditoretapa; Q:170 etapaexistente; Q:171 demandaformausente; Q:172 somenteGET; QuadroEtapas.test.jsx:135 resumopositivoexato |Tab/Enter/cancelarsemwrite e controles/formularios anteri orespreservados | Sim |

### Check C: necessidade

| file:line e assertion | AC / critério | Manter |
| --- | --- | --- |
| Q:69/70 identidade/vínculo; Q:73 metadata; Q:74 payload; Q:76 foco | CAD-01/08/26/30/32/40 trêsatores | Sim |
| Q:82/83/85 orientação/disabled/presença; Q:87 formnull; Q:88 métodosGET | CAD-07/27 quadro sófinais | Sim |
| Q:94/96/97 disabled/formausente/sóGET | CAD-26/33 GETpendente/erro | Sim |
| Q:105/106 IDs; Q:111 camposliteral; Q:113/114 semheading/botão; Q:115 stageheadings; Q:117/118 arquivo/GET | CAD-37/38/39 leitura ativa/arquivo | Sim |
| Q:128 ausências; Q:130 obser vação; Q:131 tagsnull; Q:132 motivonull; Q:133 strong | CAD-38 HTML/legados/ausências | Sim |
| Q:144/146/148 disabled; Q:151 nogo; Q:152 payloadúnico; Q:154/157 ocupação | CAD-31 pendência | Sim |
| Q:165/166 formfecha/foco; Q:168/170/171 exclusividade; Q:172 sóGET | CAD-40 cancel/controlesanteriores | Sim |
| Q:195 mensagem; Q:54 todosdraft; Q:197/198 cards; Q:199/207/211 cache; Q:200/202 bloqueio; Q:206 métodos; Q:210 count; Q:212 payloads | CAD-33/34 oitoerros, refreshmanual ePOSTatualizado | Sim |
| Q:229/230 disabledporflags; Q:54 draft; Q:232 métodos | CAD-34 arquivo/zeroWorks/perdaadmin | Sim |
| Q:247 card; Q:248 disabled; Q:249/253 cache; Q:254 métodos | CAD-33/34 falhaGETrefresh | Sim |
| Q:271 cardID42; Q:272 count1; Q:273 métodos | CAD-24 trêsCRUDetapas | Sim |
| Q:283/284/286/287 cardausente/título/cache; Q:298/300/301 abort/cards/cache; Q:308/310 unmount; Q:324/325 POSTantigo | CAD-35 quatrocenários deisolamento | Sim |
| Q:344/346/347 Sair/cache; Q:350/352/353 retorno | CAD-31 Appsucesso/erro | Sim |
| Q:387 Nenhumativo; Q:388 payloadarquivo1; Q:393 cardID42; Q:394/395 readonly; Q:396 consultas2 | CAD-32/39 cache realGerenciamentos | Sim |

Adequação PASS: cadaAC/critério/edge T5 tem estado/payload literal/assertion efile:line. Campos/novevalores emitidos/14dados de leitura conferidos; cache é provado também pelo fluxo real da lista, além do callback. Todos34casos têm requisito;235testes anteriores e assertions preservados, inclusive resumos/finais/teclado/foco/cache/arquivo. Datas usam somente manipulação de string; JSXescapa texto; CSS cobrelayout mobile, QAvisualreal pertenceT6. Nenhum SPEC_DEVIATION.

## T4: formulário de demanda

PASS. Gate `frontend npm.cmd test`:406testes em12arquivos, zero falhas/skips,235anteriores e142T3preservados,29casosT4 novos. Assumptions: EditorDemanda envia oito campos; Quadro acrescentará versão atual emT5. Datas são inputdate e nenhuma data é gerada. Files: EditorDemanda.jsx/test.jsx e index.css para grid responsivo dos oito campos, além de status/evidência. Success: campos/regras/payload/foco/pending/erro/rascunho sem controles do servidor.

### Check A: resultados e suficiência

Prefixo `frontend/src/pages/`; E=EditorDemanda.test.jsx. Payloads explícitos: E:11–14 completo/mínimo; limites E:79–80. `conferirRascunho` E:31 compara valor de cada um dosoito controles separadamente.

| AC / critério | file:line e assertion | Resultado da spec | Coberto |
| --- | --- | --- | --- |
| CAD-28 oito campos/required/tipos/auto | E:39 `expect(controle.value).toBe("")`; E:40 `expect(controle.required).toBe(campo === "numeroProcesso" || campo === "pessoa")`; E:43 prioridadetext; E:44 trêsdate; E:45 textarea; E:46/47 ausência controles; E:48/50 textos exatos | Somente número/pessoarequired, seisopcionais, primeira etapa automática e sem status/etapa/finais | Sim |
| CAD-02/30 numeroProcesso/pessoa | E:58 `expect(salvar).toHaveBeenCalledExactlyOnceWith(minimo)` confere D-1/João; E:83 `...With(dados)` confere255UTF-16 |TrimUnicode, grafia e limites1..255 preservados | Sim |
| CAD-03/30 responsavel/prioridade | E:67 `...With(completo)` confere Ana/Livre e urgente; E:74 `...With(minimo)` confere ambosnull; E:83 ambos255 |Trim, prioridade livre, vaziosnull, limite255 | Sim |
| CAD-03/30 observacoes | E:67 `...With(completo)` confere Uma\nOutra; E:74 obsnull; E:83 obs10000 |Trim de bordas, linhas internas, branco→null,10000permitido | Sim |
| CAD-04/30 dataEmissao/prazoEtapa/prazoGeral | E:67 `...With(completo)` confere2020-02-29/2019-01-01/2018-01-01; E:58 `...With(minimo)` confere trêsnull |Datas passadas/invertidasISO e ausência semdata atual | Sim |
| CAD-10/29 inválidos/draft/norequest | E:100 `expect(screen.getByRole("alert").textContent).toBe(mensagem)`; E:31 valores; E:102 `expect(salvar).not.toHaveBeenCalled()`; E:103 aoErronãochamado |13casos limites/brancos/Unicode, mensagens exatas doscinco textos, semgravar/perdervalores | Sim |
| CAD-31 únicoPOST/pending/cancel | E:134 `...With(completo)`; E:135 aria-busytrue; E:136 oitocamposdisabled; E:137 botãoCriando disabled; E:138 Cancelardisabled; E:140 nãochamado |Duplicação/submissãoforçada impedidas; campos eCancelar bloqueados | Sim |
| CAD-31/33 bloqueio externo | E:147/E:148 disabled; E:150 `expect(salvar).not.toHaveBeenCalled()`; E:152 cancelaruma |Refresh/arquivo/zeroWorks poderá bloquear submit forçado; cancelamento após erro permanece disponível | Sim |
| CAD-33 erro/conservação | E:166 alertmensagemexata; E:31 oito valores; E:168 `...With(completo)`; E:169 `expect(aoErro).toHaveBeenCalledExactlyOnceWith(erro)` |400/401/404/409/500/0/201 preservam draft e informamQu adro para refresh | Sim |
| CAD-40 teclado/foco/cancel | E:42 focoNúmero; E:111 foco de cada campo; E:116 payloadteclado completo; E:123 cancelaruma; E:124 salvarnãochamado |Foco inicial,Tab/Enter e cancelarsemwrite | Sim |

### Check C: necessidade

| file:line e assertion | AC / critério | Manter |
| --- | --- | --- |
| E:36 título; E:39/40 vazio/required; E:43–50 tipos/ausência/textos | CAD-28 interface | Sim |
| E:58 `...With(minimo)`; E:67 `...With(completo)`; E:74 `...With(minimo)`; E:83 `...With(dados)` | CAD-02/03/04/30 normalização e payload por valor | Sim |
| E:100 mensagem; E:31 draft; E:102/103 semcallbacks | CAD-10/29 erros locais | Sim |
| E:111 foco; E:116 `...With(...)`; E:123 cancel; E:124 nograva | CAD-28/30/40 teclado/cancel | Sim |
| E:134–142 payload/pending/disabled/cancel; E:147–152 bloqueioexterno | CAD-31 eCAD-33 controledependente deQuadro | Sim |
| E:166 alert; E:31 draft; E:168 payload; E:169 callbackerro | CAD-33 seteformas de erro | Sim |

Adequação PASS: todos oscampos emitidos conferidos por valor nosobjetos completos/mínimos/limites; cadaedge ecritério temassertion localizada. Pending temdados emitidos+estado, nãoapenascontagem de mock. Todos29casos têm requisito, sem assertions anteriores alteradas, testes apagados/ignorados ouSPEC_DEVIATION. PadrãoEditorEtapa, classes existentes e localização/runner do projeto seguidos.

## T3: contrato frontend de snapshot e cadastro

PASS. Gate `frontend npm.cmd test`:377 testes em11arquivos, zero falhas/skips;235anteriores preservados e142novos. Gate repetido após fortalecer mensagens das assertions de campos incompletos, com377/377 novamente. Nenhum Maven/E2E/build ou artefato anterior alterado por esta tarefa.

Assumptions: validação comum nas cinco rotas de snapshot;14campos exatos, opcionaisnull/status desconhecido/texto antigo longo permitidos. Files: api.js/api.test.js e fixtures de Quadro.test/QuadroEtapas.test/App.test/Gerenciamentos.test, além de status/evidência. Success: rota/Bearer/novecampos, saída inteira/incompleta/inconsistente/204, erros/abort sem retry e baseline preservada.

### Check A: resultados e suficiência

Prefixo: `frontend/src/services/`; A=api.test.js. Os objetos comparados por `toEqual` contêm valores explícitos dos14campos em A:108–111, e dosnovecampos de cadastro em A:173–175.

| AC / critério | file:line e assertion | Resultado da spec | Coberto |
| --- | --- | --- | --- |
| CAD-30 rota/Bearer/novecampos | A:183 `expect(await criarDemanda("sessao", 9, dadosDemanda)).toEqual(configuracao)`; A:184 `expect(fetchSpy).toHaveBeenCalledExactlyOnceWith(...)` com URL demanda,POST,Bearer e `body: JSON.stringify(dadosDemanda)` | UmPOST com novecampos e snapshot integral | Sim |
| CAD-36 todasrotas/snapshot completo/legados/vazio | A:196 `expect(await executar()).toEqual(legado)`; A:199 `expect(await executar()).toEqual(vazio)` nascinco rotas | Preservar14campos, datasISO/status desconhecido/texto10001, arraysvazios | Sim |
| CAD-36 metadata/listas/IDs/vínculo/counts/flat | A:226 `await expect(executar()).rejects.toMatchObject({ name: "ApiError", status, message: ... })`; A:228 `expect(fetchSpy).toHaveBeenCalledTimes(1)` |19 formas inseguras rejeitadas nascinco rotas, mensagemERRO_COMUNICACAO exata e sem retry | Sim |
| CAD-36 presença/tipo de todos14campos | A:235/A:248 `await expect(buscarConfiguracaoEtapas("sessao", 9)).rejects.toMatchObject({ status: 200, message: ... })` para14ausentes e14tipos/datas inválidos | Campos incompletos, IDs string e datas impossíveis não confirmam sucesso | Sim |
| CAD-33/36 HTTP204 | A:255 `await expect(executar()).rejects.toMatchObject({ status: 204, message: ... })`; A:257 chamadaúnica |204 não confirma nenhuma dascinco rotas | Sim |
| CAD-33 HTTP/rede/JSON | A:262 `rejects.toMatchObject({ message: "Mensagem do servidor", status })`; A:270 `rejects.toMatchObject({ status: falha === "rede" ? 0 : 201, message: ... })`; A:263/A:272 chamadaúnica |400/401/404/409/500 preservados; rede0/JSON201 incertos semPOST repetido | Sim |
| Abort/isolation infraestruturaCAD-35 | A:280 `rejects.toBe(erro)`; A:281 `expect(fetchSpy.mock.calls[0][1].signal).toBe(controller.signal)`; A:282 chamadaúnica |AbortError original e AbortSignal preservados, semretry | Sim |
| Fixtures/baseline | api.test.js:108 array comduas demandas; QuadroEtapas.test.jsx:19 `demandas: colunas.flatMap(...)`; App/Quadro/Gerenciamentos fixtures vazios |Counts2/3/1 correspondem aos cartões de fixtures testonly;235cenários/assertions anteriores preservados | Sim |

### Check C: necessidade

| file:line e assertion | AC / critério | Manter |
| --- | --- | --- |
| A:183 snapshot `toEqual`; A:184 POST completo `toHaveBeenCalledExactlyOnceWith` | CAD-30 contratoemitido/retornado | Sim |
| A:196 legado `toEqual`; A:199 vazio `toEqual` | CAD-36 validade sem limites retroativos | Sim |
| A:226 `rejects.toMatchObject`; A:228 chamadaúnica,19casos×5rotas | CAD-36 integridade eCAD-33 noretry | Sim |
| A:235/A:248 `rejects.toMatchObject` | CAD-36 todos14campos/types/datas | Sim |
| A:255 erro204; A:257 chamadaúnica | CAD-33/36 ausência de confirmação | Sim |
| A:262 mensagem/status; A:270 statusrede/JSON; A:263/A:272 chamadaúnica | CAD-33 erros semretry | Sim |
| A:280 `rejects.toBe(erro)`; A:281 signal; A:282 chamadaúnica | CAD-35 abort eCAD-36 contrato GET | Sim |

Adequação PASS: cadacritério T3 temresultado/assertion/localização; `toEqual` confere cada valor do payload/snapshot, não apenas chamada. Testes inversos detectam contratos incorretos; nenhum teste semrequisito, assertion prévia enfraquecida ou cenário removido. Convenções de localização/nome/runner existentes seguidas. Sem SPEC_DEVIATION. Resumo de contagem anterior preservado para T5.

## T1: snapshot com demandas persistidas

PASS. Gate `sistema mvn.cmd -B verify`, JDK25.0.2: 284 testes, zero falhas/erros/skips. Baseline278 preservada e seis casos novos em DemandaConsultaTests. Log: `C:/Users/Marco/AppData/Local/Temp/creral-demandas-backend-20261006/T1-verify.log`. Nenhum banco configurado operado. Não houve alteração dos testes anteriores.

Assumptions: os 14 campos e a compatibilidade de categoria SQLnull vêm do design. Leitura preserva valores antigos sem trim, limites retroativos ou escrita. Files: DemandaResponse, ConfiguracaoEtapasResponse, EtapaService, DemandaConsultaTests e status/evidência desta feature. Success: snapshot filtrado/ordenado, campos completos, contagens da mesma lista, vazio, ativo/arquivo/legados, CRUD e espera de commit.

### Check A: cobertura suficiente e resultados da especificação

Prefixo dos arquivos de teste: `sistema/src/test/java/com/patp/sistema/`. Evidências de campos usam DemandaConsultaTests.java.

| AC / critério | file:line e assertion | Resultado definido | Coberto |
| --- | --- | --- | --- |
| CAD-21 quadro/filtro/ordem | DemandaConsultaTests.java:50 `jsonPath("$.gerenciamento.id").value(quadro.getId())`; :51 `jsonPath("$.demandas.length()").value(2)`; :66 `jsonPath("$.demandas[1].id").value(minima.getId())` | Somente duas demandas do quadro, ordem de ID | Sim |
| CAD-21 id | DemandaConsultaTests.java:52 `jsonPath("$.demandas[0].id").value(completa.getId())` | ID persistido | Sim |
| CAD-21 numeroProcesso | DemandaConsultaTests.java:53 `jsonPath("$.demandas[0].numeroProcesso").value(completa.getNumeroProcesso())` | Número persistido | Sim |
| CAD-21 pessoa | DemandaConsultaTests.java:54 `jsonPath("$.demandas[0].pessoa").value("Pessoa")` | Pessoa persistida | Sim |
| CAD-21 responsavel | DemandaConsultaTests.java:55 `jsonPath("$.demandas[0].responsavel").value("Ana")` | Responsável persistido | Sim |
| CAD-21 status | DemandaConsultaTests.java:56 `jsonPath("$.demandas[0].status").value("Em andamento")` | Status literal persistido | Sim |
| CAD-21 prioridade | DemandaConsultaTests.java:57 `jsonPath("$.demandas[0].prioridade").value("Urgente")` | Prioridade persistida | Sim |
| CAD-21 dataEmissao | DemandaConsultaTests.java:58 `jsonPath("$.demandas[0].dataEmissao").value("2020-02-03")` | Data ISO persistida | Sim |
| CAD-21 prazoEtapa | DemandaConsultaTests.java:59 `jsonPath("$.demandas[0].prazoEtapa").value("2020-02-04")` | Data ISO persistida | Sim |
| CAD-21 prazoGeral | DemandaConsultaTests.java:60 `jsonPath("$.demandas[0].prazoGeral").value("2020-02-05")` | Data ISO persistida | Sim |
| CAD-21 dataConclusao | DemandaConsultaTests.java:61 `jsonPath("$.demandas[0].dataConclusao").value("2020-02-06")` | Data antiga preservada | Sim |
| CAD-21 dataCancelamento | DemandaConsultaTests.java:62 `jsonPath("$.demandas[0].dataCancelamento").value("2020-02-07")` | Data antiga preservada | Sim |
| CAD-21 motivoCancelamento | DemandaConsultaTests.java:63 `jsonPath("$.demandas[0].motivoCancelamento").value("Legado")` | Motivo antigo preservado | Sim |
| CAD-21 observacoes | DemandaConsultaTests.java:64 `jsonPath("$.demandas[0].observacoes").value("<b>Texto antigo</b>\nOutra linha")` | Texto literal persistido | Sim |
| CAD-21 etapaId/flat | DemandaConsultaTests.java:65 `jsonPath("$.demandas[0].etapaId").value(segunda.getId())`; :73 `propertyNames()).containsExactlyInAnyOrder(...)` com os14 nomes explícitos | EtapaID, exatamente14 campos sem cadeia de entidades | Sim |
| CAD-21 opcionaisnull/vazio | DemandaConsultaTests.java:76 `snapshot.get("demandas").get(1).get(campo).isNull()).as(campo).isTrue()` para nove opcionais; :85 `jsonPath("$.demandas.length()").value(0)` | Null explícito e [] vazio | Sim |
| CAD-22 counts/ordem | DemandaConsultaTests.java:68 `jsonPath("$.etapas[0].id").value(primeira.getId())`; :69/:71 `quantidadeDemandas).value(1)`; :85 `quantidadeDemandas).value(0)` | Cada count corresponde aos cartões da etapa; empate por ID | Sim |
| CAD-22 categorias oficiais | DemandaConsultaTests.java:119 `jsonPath("$.etapas[2].categoria").value("CONCLUIDA")`; :120 `jsonPath("$.etapas[3].categoria").value("CANCELADA")` | Finais oficiais ordenadas após trabalhos | Sim |
| CAD-23 ativo/arquivo/legados | DemandaConsultaTests.java:98 `jsonPath("$.gerenciamento.arquivado").value(arquivado)`; :101 status desconhecido; :102 espaços; :103 texto10001; :107 `Map.of(...).isEqualTo(antes)` | Valores antigos e todas tabelas/versão idênticos | Sim |
| CAD-24 pósCRUD | DemandaConsultaTests.java:116/:125/:128 `jsonPath("$.demandas[0].id").value(demanda.getId())`; :126/:130 counts1 | Criar/editar/excluir etapa conserva cartão persistido/count | Sim |
| CAD-25 snapshot apóscommit | DemandaConsultaTests.java:151 `assertThatThrownBy(() -> segunda.get(200, TimeUnit.MILLISECONDS)).isInstanceOf(TimeoutException.class)`; :153 versão1; :154 Confirmada; :155 count1; :157 Depois; :158 etapaId | GET aguarda commit e retorna versão/etapa/cartão/count atuais | Sim |

### Check C: necessidade, mapa inverso

| file:line e assertion | AC / resultado correspondente | Manter |
| --- | --- | --- |
| DemandaConsultaTests.java:50 `gerenciamento.id).value(quadro.getId())`; :51 `demandas.length()).value(2)`; :52 `id).value(completa.getId())`; :66 `id).value(minima.getId())` | CAD-21 filtro e ordem | Sim |
| DemandaConsultaTests.java:53 `numeroProcesso).value(completa.getNumeroProcesso())`; :54 `pessoa).value("Pessoa")`; :55 `responsavel).value("Ana")`; :56 `status).value("Em andamento")`; :57 `prioridade).value("Urgente")` | CAD-21 campos literais | Sim |
| DemandaConsultaTests.java:58 `dataEmissao).value("2020-02-03")`; :59 `prazoEtapa).value("2020-02-04")`; :60 `prazoGeral).value("2020-02-05")`; :61 `dataConclusao).value("2020-02-06")`; :62 `dataCancelamento).value("2020-02-07")` | CAD-21 datas ISO | Sim |
| DemandaConsultaTests.java:63 `motivoCancelamento).value("Legado")`; :64 `observacoes).value("<b>Texto antigo</b>\nOutra linha")`; :65/:67 `etapaId).value(...)`; :73 `propertyNames()).containsExactlyInAnyOrder(...)` | CAD-21 flat, vínculos e conteúdo | Sim |
| DemandaConsultaTests.java:76 `isNull()).as(campo).isTrue()`; :84 `demandas).isArray()`; :85 `demandas.length()).value(0)` e `quantidadeDemandas).value(0)` | CAD-21/22 opcionais e vazio | Sim |
| DemandaConsultaTests.java:68/:70 `etapas[n].id).value(...)`; :69/:71 `quantidadeDemandas).value(1)`; :119/:120 `categoria).value(...)` | CAD-22 sequência/counts/finais | Sim |
| DemandaConsultaTests.java:98 `arquivado).value(arquivado)`; :99 `versao).value(arquivado ? 1 : 0)`; :100 `categoria).value("TRABALHO")`; :101/:102/:103/:104 valores antigos; :105/:106 null; :107 `Map.of(...).isEqualTo(antes)` | CAD-23 arquivo, SQLnull e preservação semwrite | Sim |
| DemandaConsultaTests.java:116/:117/:118/:124/:125/:126/:128/:129/:130 IDs, vínculo, nome/count | CAD-24 cartões preservados após três mutações de etapa | Sim |
| DemandaConsultaTests.java:148/:150 `await(...).isTrue()`; :151 `TimeoutException`; :153/:154/:155/:156/:157/:158 estado póscommit | CAD-25 sincronização e snapshot coerente | Sim |

Adequação PASS: cada AC/edge T1 tem resultado e file:line; 14 campos conferidos por valor, teste de concorrência verifica espera e estado. Nenhum teste sem requisito, assertions vazias, mocks de resultado ou teste prévio removido/atenuado. Convenções de docs/OPERACAO.md e testes de integração existentes seguidas. Sem SPEC_DEVIATION.

## T2: cadastro validado na primeira etapa

PASS. Gate `sistema mvn.cmd -B verify`, JDK25.0.2: 390 testes, zero falhas/erros/skips, baseline278 preservada. Novos casos T2: DemandaCadastroTests92 e DemandaCadastroConcurrencyTests14; T1consulta6 preservados. Log: `C:/Users/Marco/AppData/Local/Temp/creral-demandas-backend-20261006/T2-verify.log`:191/253/406/416. Maven encerrado. Não houve MySQL configurado, push/deploy ou alteração das ações futuras.

Assumptions: nove campos estritos no novo endpoint; datas stringISO/null por deserializer local; trim e limites UTF-16, prioridade livre, prazos invertidos/passados válidos. Legado mantém200/entidade/sem versão; criação começa na primeira TRABALHO e recusa estado final. Files: ProcessoService/ProcessoRepository, GerenciamentoRepository/Guard, DemandaController/CriarDemandaRequest/DataDemandaDeserializer, DemandaCadastroTests/ConcurrencyTests, somente fixture autorizada em EtapaDemandConcurrencyTests e docs da feature. Success: todos CAD-01–20, rollback e races reais, >=baseline278 no gate completo.

### Decisão e diagnóstico de unicidade

O primeiro diagnóstico funcional executou103 casos,102PASS/1FAIL: Igual/igual em quadros distintos completava o segundo cadastro antes do commit do primeiro (`T2-diagnostic.log`:83/84/123). Igual/Igual detectou UNIQUE23505 e devolveu409; todos88 casos de campos/erros/rollback e7 regressões concorrentes passaram. O índice VARCHAR do H2 é case-sensitive; nenhuma URL/collation foi alterada para esconder a falha.

Correção técnica decidida: ambas rotas bloqueiam PESSIMISTIC_WRITE na primeira linha estável de gerenciamento antes do quadro alvo. O lock persiste até commit e serializa cadastros globais antes do precheckIgnoreCase. Essa linha não é excluída por endpoint atual, pode estar arquivada/sem criador e só é bloqueada, sem mutação/versão ou administração/ativo se não for o alvo. Tradeoff aceito: cadastros de quadros diferentes aguardam entre si. Ordem global→alvo; demais mutações atuais só bloqueiam o alvo e preparação percorre IDs ordenados. Busca somente leitura confirmou que controllers são os chamadores de cadastro em aplicação, sem uma mutação atual adquirir alvo e chamar cadastro depois. Nenhuma API futura foi mudada.

APIs confirmadas por javap nos JARs locais: Jackson3.1.5 JsonNode.propertyNames, JsonAnySetter2.21 e deserializers usados; Hibernate7.4.5 ConstraintViolationException.getKind()/ConstraintKind.UNIQUE; SpringTest7.0.9 MockitoSpyBean. Somente violação UNIQUE do INSERT em processos vira409. CHECK de demanda/histórico fica500 e reverte todas as tabelas/versão. O teste da garantia física desativa apenas o precheck com spy; INSERT/constraint/rollback são reais, e as assertions verificam HTTP e dados, sem usar contagem de mocks como evidência.

### Check A: cobertura suficiente e resultados da especificação

Prefixo: `sistema/src/test/java/com/patp/sistema/`. D=DemandaCadastroTests.java; C=DemandaCadastroConcurrencyTests.java. Cada referência D/C abaixo identifica o arquivo completo pelo prefixo e nome aqui definidos. FORMATO=`Dados da requisição inválidos.`; VERSAO=`Gerenciamento alterado por outro usuário. Atualize e tente novamente.`. `estado()` compara linhas de etapas/processos/comentários/históricos e todas as linhas/versões dos gerenciamentos.

| AC / critério | file:line e assertion | Resultado definido pela spec | Coberto |
| --- | --- | --- | --- |
| CAD-01 novo201/persistência/snapshot | D:41 `status().isCreated()`; D:42 `jsonPath("$.demandas.length()").value(1)` e `jsonPath("$.etapas[0].quantidadeDemandas").value(1)`; D:48 `assertThat(json.readTree(resultado.getResponse().getContentAsString()).get("demandas").get(0).get("id").asLong()).isEqualTo(demanda.getId())` |201, cartão com ID realmente persistido e count1; 14campos flat cobertos em T1 | Sim |
| CAD-02 número/pessoa trim | D:78 `assertThat(demanda.getNumeroProcesso()).isEqualTo("MiSto-1")` e `assertThat(demanda.getPessoa()).isEqualTo("João")`; D:92 `assertThat(limite.getNumeroProcesso()).isEqualTo("😀".repeat(127) + "a")` e `assertThat(limite.getPessoa()).isEqualTo("ç".repeat(255))` | Grafia preservada após trim,255 UTF-16 aceitos | Sim |
| CAD-03 responsável/prioridade | D:79 `assertThat(demanda.getResponsavel()).isEqualTo("Ana")` e `assertThat(demanda.getPrioridade()).isEqualTo("Prioridade livre")`; D:93 `assertThat(limite.getResponsavel()).isEqualTo("r".repeat(255))` e `assertThat(limite.getPrioridade()).isEqualTo("p".repeat(255))` | Opcionais com trim/limite255, texto livre | Sim |
| CAD-03 observações | D:80 `assertThat(demanda.getObservacoes()).isEqualTo("Uma\nOutra")`; D:94 `assertThat(limite.getObservacoes()).isEqualTo("o".repeat(10000))` | Trim das bordas, quebra interna e limite10000 | Sim |
| CAD-03 ausente/vazio/null | D:51 `assertThat(demanda.getResponsavel()).isNull(); assertThat(demanda.getPrioridade()).isNull(); assertThat(demanda.getObservacoes()).isNull()`; D:99 mesmas três assertions sobre `vazia` | Omitido/vazio/null vira null | Sim |
| CAD-04 três datas | D:81 `assertThat(demanda.getDataEmissao()).isEqualTo(LocalDate.of(2020, 2, 3))`; D:82 `assertThat(demanda.getPrazoEtapa()).isEqualTo(LocalDate.of(2019, 1, 1))` e `assertThat(demanda.getPrazoGeral()).isEqualTo(LocalDate.of(2018, 1, 1))`; D:52/D:100 assertions `getDataEmissao/getPrazoEtapa/getPrazoGeral().isNull()` | Datas ISO passadas/invertidas preservadas; omitido/null sem hoje | Sim |
| CAD-05 estado inicial/encerramento | D:50 `assertThat(demanda.getStatus()).isEqualTo("Em andamento")`; D:53 `assertThat(demanda.getDataConclusao()).isNull(); assertThat(demanda.getDataCancelamento()).isNull(); assertThat(demanda.getMotivoCancelamento()).isNull()` | Estado exato e três finaisnull | Sim |
| CAD-06 primeiro trabalho | D:136 `jsonPath("$.demandas[0].etapaId").value(primeira.getId())`; D:137 `assertThat(processos.findAll().get(0).getEtapa().getId()).isEqualTo(primeira.getId())`; D:138 `assertThat(jdbc.queryForList("select * from etapas order by id")).isEqualTo(antes)` | Categoria SQLnull, homônimo final, empate/lacuna por ordem/ID; sem inferir nome/escrever etapa | Sim |
| CAD-07 zero trabalho | D:148 `status().isConflict()` e `jsonPath("$.erro").value("Cadastre uma etapa de trabalho antes de criar demandas.")`; D:149 `assertThat(estado()).isEqualTo(antes)` |409 sem gravar, quadro vazio ou só final | Sim |
| CAD-08 todosauth | D:110 `status().isCreated()` e `jsonPath("$.demandas[0].numeroProcesso").value("Autorizada")`; D:111 `assertThat(processos.count()).isEqualTo(1)` e `assertThat(jdbc.queryForList("select usuario from historicos", String.class)).containsExactly(autor.getNome())` | Criador/admin/outro/quadro sem criador autorizados | Sim |
| CAD-09 sessão/quadro/arquivo | D:158 `status().isUnauthorized()` e `assertThat(estado()).isEqualTo(antes)`; D:161 `status().isNotFound()` e `jsonPath("$.erro").value("Gerenciamento não encontrado.")`; D:165 `status().isConflict()` e `jsonPath("$.erro").value("Gerenciamento arquivado. Restaure-o antes de alterar.")`; D:162/D:166 `assertThat(estado()).isEqualTo(antes)` |401/404/409 e estados preservados | Sim |
| CAD-10 campos inválidos nas2rotas | D:175 `status().isBadRequest()` e `jsonPath("$.erro").value(mensagem)`; D:176 `assertThat(estado()).isEqualTo(antes)` |400 com mensagens exatas dos cinco campos; null/branco/256/10001 e UTF-16 excedido, semwrite | Sim |
| CAD-11 JSON/versão/datas estritos | D:200/D:209 `status().isBadRequest()` e `jsonPath("$.erro").value(FORMATO)`; D:201/D:210 `assertThat(estado()).isEqualTo(antes)` |400 para JSON/null-body/versão stringfloatbool; em cada data array/número/bool/vazio/branco/data impossível | Sim |
| CAD-12 desconhecidos | D:221 `status().isBadRequest()` e `jsonPath("$.erro").value(FORMATO)`; D:222 `assertThat(estado()).isEqualTo(antes)` |400 para ID/etapa/status/três finais/desconhecido, mesmo null | Sim |
| CAD-13 globalprecheck/UNIQUE físico | D:242/D:255 `status().isConflict()` e `jsonPath("$.erro").value("Já existe uma demanda com esse número.")`; D:243/D:256 `assertThat(estado()).isEqualTo(antes)` |409 global nas2rotas com caixa diferente; garantia UNIQUE real quando precheck não encontra | Sim |
| CAD-14 versão | D:230 `status().is(versao == 999 ? 409 : 400)`; D:231 `jsonPath("$.erro").value(versao == 999 ? VERSAO : "Informe uma versão válida do gerenciamento.")`; D:232 `assertThat(estado()).isEqualTo(antes)` | Ausente/negativa400; obsoleta409; semwrite | Sim |
| CAD-15 incremento/histórico | D:54 `assertThat(quadros.findById(quadro.getId()).orElseThrow().getVersao()).isEqualTo(1L)`; D:55 `assertThat(jdbc.queryForList("select acao from historicos", String.class)).containsExactly("CRIACAO")`; D:56 descricao exata; D:57 autorOutro; D:58 processoIdpersistido; D:59 countdataHora1 | Versão+1 e exatamenteum CRIACAO/Processo criado./autor da sessão/ID/data na mesma transação | Sim |
| CAD-16 rollback real | D:267 `status().isInternalServerError()` e `jsonPath("$.erro").value("Não foi possível concluir a operação.")`; D:268 `assertThat(estado()).isEqualTo(antes)` | CHECK na demanda ou histórico,500; tudo reverte nas2rotas | Sim |
| CAD-17 race global ambasrotas | C:60 `assertThatThrownBy(() -> segunda.get(200, TimeUnit.MILLISECONDS)).isInstanceOf(TimeoutException.class)`; C:62 `assertThat(resposta.getStatus()).isEqualTo(409)`; C:64 `assertThat(processos.count()).isEqualTo(1)`; C:66 umCRIACAO; C:67 versão0ou1; C:68 versão perdedora0 |8 combinações novo/legado e Igual/igual aguardam commit; um registro/histórico | Sim |
| CAD-17 quadroglobal passivo | D:121 `status().is(legado ? 200 : 201)`; D:122 `assertThat(jdbc.queryForList("select * from gerenciamentos where id=?", global.getId())).isEqualTo(antes)`; D:124 versãoalvo0ou1 | Primeira linha arquivada/semcriador não impede outroativo e preserva dados/versão global | Sim |
| CAD-17 disputas com estrutura/arquivo | C:101 `assertThatThrownBy(() -> segunda.get(200, TimeUnit.MILLISECONDS)).isInstanceOf(TimeoutException.class)`; C:104 `assertThat(resposta.getStatus()).isEqualTo(esperado)`; C:111 demanda0ou1; C:112 histórico0ou1; C:113 órfãos0; C:114 versão1ou2 |6 ordens de arquivo/remoção/reordenação serializadas, com estado/erro do design e nenhuma escrita parcial | Sim |
| CAD-18 legado200/defaults/campos | D:281 `status().isOk()` e `jsonPath("$.numeroProcesso").value("Legado")`; D:282 `jsonPath("$.status").value("Em andamento")`; D:283 etapa/quadro persistidos; D:284/D:285/D:286 opcionais/datas por valor; D:287 finaisnull; D:288 versãozero; D:289 umCRIACAO | Sem versão/incremento, status omitido ou inicial, mesmos campos/defaults/transação/duplicidade | Sim |
| CAD-19 ID/estado/encerramento/vínculo | ProcessoArchiveTests.java:60 `status().isBadRequest()`; :61 `assertThat(conteudoPersistido()).isEqualTo(antes)`; :77 `status().isConflict()` e `jsonPath("$.erro").value(ERRO)`; D:297 `status().isBadRequest()` e `jsonPath("$.erro").value("O estado inicial da demanda é definido pelo sistema.")`; D:298 estadoigual | ID semupdate; vínculo persistido prevalece; estadofinal/datas/motivo400 semwrite | Sim |
| CAD-20 etapa posterior/final/inexistente | D:309 `status().isBadRequest()` e `jsonPath("$.erro").value("Novas demandas devem começar na primeira etapa de trabalho.")`; D:317 `status().isNotFound()` e `jsonPath("$.erro").value("Etapa não encontrada neste gerenciamento.")`; D:310/D:318 estadoigual | Posterior/finais400; inexistente404 | Sim |

### Payload/conjunction: histórico de criação

| Campo | file:line e assertion exata | Resultado |
| --- | --- | --- |
| acao | D:55 `assertThat(jdbc.queryForList("select acao from historicos", String.class)).containsExactly("CRIACAO")` | Exatamente um CRIACAO |
| descricao | D:56 `assertThat(jdbc.queryForList("select descricao from historicos", String.class)).containsExactly("Processo criado.")` | Texto exato |
| usuario | D:57 `assertThat(jdbc.queryForList("select usuario from historicos", String.class)).containsExactly("Outro")` | Autor da sessão |
| processoId | D:58 `assertThat(jdbc.queryForObject("select processo_id from historicos", Long.class)).isEqualTo(demanda.getId())` | ID salvo |
| dataHora | D:59 `assertThat(jdbc.queryForObject("select count(*) from historicos where data_hora is not null", Long.class)).isEqualTo(1L)` | Data existente no único histórico |

### Check C: necessidade, mapa inverso por cenário

| file:line e assertion | AC / resultado correspondente | Manter |
| --- | --- | --- |
| D:41 `status().isCreated()`; D:48 IDresponseigualpersistido; D:53 trêsfinaisnull; D:54 versão1; D:55 `containsExactly("CRIACAO")` | CAD-01/05/15 mínimo/defaults/transaction | Sim |
| D:78 `assertThat(demanda.getNumeroProcesso()).isEqualTo("MiSto-1")`; D:79 `assertThat(demanda.getResponsavel()).isEqualTo("Ana")`; D:82 `assertThat(demanda.getPrazoGeral()).isEqualTo(LocalDate.of(2018, 1, 1))` | CAD-02/03/04 completo/trim/prioridade livre/datas invertidas | Sim |
| D:92 `assertThat(limite.getNumeroProcesso()).isEqualTo("😀".repeat(127) + "a")`; D:94 `assertThat(limite.getObservacoes()).isEqualTo("o".repeat(10000))`; D:99 `assertThat(vazia.getObservacoes()).isNull()` | CAD-02/03/04 limitesUnicode/vazio/null | Sim |
| D:110 `status().isCreated()`; D:111 `assertThat(processos.count()).isEqualTo(1)` e autorporvalor | CAD-08 quatroatores/quadrosemcriador | Sim |
| D:121 `status().is(legado ? 200 : 201)`; D:122 `assertThat(jdbc.queryForList("select * from gerenciamentos where id=?", global.getId())).isEqualTo(antes)` | CAD-08/17/18 globalarquivado semalterar dados/versão | Sim |
| D:136 `jsonPath("$.demandas[0].etapaId").value(primeira.getId())`; D:138 `assertThat(jdbc.queryForList("select * from etapas order by id")).isEqualTo(antes)` | CAD-06 ordem/ID/categoriaSQLnull/homônimo/lacuna | Sim |
| D:148 `status().isConflict()` e mensagemzerotrabalho; D:149 `assertThat(estado()).isEqualTo(antes)` | CAD-07 vazio/sófinal semwrite | Sim |
| D:158 `status().isUnauthorized()`; D:161 `status().isNotFound()`; D:165 `status().isConflict()`; D:166 `assertThat(estado()).isEqualTo(antes)` | CAD-09 auth/404/arquivo | Sim |
| D:175 `status().isBadRequest()` e `jsonPath("$.erro").value(mensagem)`; D:176 `assertThat(estado()).isEqualTo(antes)` | CAD-10/18 inválidosnasduasrotas | Sim |
| D:200/D:209 `status().isBadRequest()` e `jsonPath("$.erro").value(FORMATO)`; D:201/D:210 `assertThat(estado()).isEqualTo(antes)` | CAD-11 JSON/versão/18tiposdatas | Sim |
| D:221 `status().isBadRequest()` e `jsonPath("$.erro").value(FORMATO)`; D:222 `assertThat(estado()).isEqualTo(antes)` | CAD-12 setepropriedadesfora docontrato | Sim |
| D:230 `status().is(versao == 999 ? 409 : 400)`; D:231 `jsonPath("$.erro").value(versao == 999 ? VERSAO : "Informe uma versão válida do gerenciamento.")` | CAD-14 ausente/negativa/obsoleta | Sim |
| D:242/D:255 `status().isConflict()` e `jsonPath("$.erro").value("Já existe uma demanda com esse número.")`; D:243/D:256 `assertThat(estado()).isEqualTo(antes)` | CAD-13/18 dupcaseglobal econstraintreal | Sim |
| D:267 `status().isInternalServerError()` e `jsonPath("$.erro").value("Não foi possível concluir a operação.")`; D:268 `assertThat(estado()).isEqualTo(antes)` | CAD-16/18 rollback por duas CHECKs/reais nas2rotas | Sim |
| D:281 `status().isOk()`; D:282 `jsonPath("$.status").value("Em andamento")`; D:288 `assertThat(quadros.findById(quadro.getId()).orElseThrow().getVersao()).isZero()` | CAD-18 entidade200/campos/defaults/semversão | Sim |
| D:297 `status().isBadRequest()` e `jsonPath("$.erro").value("O estado inicial da demanda é definido pelo sistema.")`; D:298 `assertThat(estado()).isEqualTo(antes)` | CAD-19 seisestados/encerramentoslegados | Sim |
| D:309 `status().isBadRequest()` e `jsonPath("$.erro").value("Novas demandas devem começar na primeira etapa de trabalho.")`; D:317 `status().isNotFound()` e erroetapa | CAD-20 posterior/finais/inexistente | Sim |
| C:60 `assertThatThrownBy(() -> segunda.get(200, TimeUnit.MILLISECONDS)).isInstanceOf(TimeoutException.class)`; C:62 `assertThat(resposta.getStatus()).isEqualTo(409)`; C:64 `assertThat(processos.count()).isEqualTo(1)` | CAD-13/17/18 oitocombinaçõesrace/caixa/2rotas | Sim |
| C:101 `TimeoutException`; C:104 `assertThat(resposta.getStatus()).isEqualTo(esperado)`; C:113 `assertThat(jdbc.queryForObject("select count(*) from processos p left join etapas e on e.id=p.etapa_id where e.id is null", Long.class)).isZero()` | CAD-17 seisdisputas estruturais/arquivo e integridade | Sim |
| EtapaDemandConcurrencyTests.java:78 `assertThat(resposta.getStatus()).isEqualTo(removerPrimeiro ? 404 : 409)`; :88 `assertThat(quadros.findById(quadro.getId()).orElseThrow().getVersao()).isEqualTo(removerPrimeiro ? 1L : 0L)`; :89 órfãoszero; :90 histórico0ou1 | CAD-18/20 + regressão ETA-21, mantendo todos7casos eassertions | Sim |
| ProcessoArchiveTests.java:41 `status().isConflict()`/mensagemarquivo; :60 `status().isBadRequest()`; :77 conflitovínculo; HistoricoArchiveTests.java:104 `status().isUnauthorized()` | CAD-09/19 + regressõesanteriores preservadas semalterações | Sim |

Adequação PASS: CAD-01–20 e os edges delimitados têm assertions de resultado, erros precisos e persistência/versão, com14campos de snapshot conferidos em T1 e payload de histórico campoacampo acima. Cada cenário tem requisito; nenhum teste removido/ignorado ou assertion prévia atenuada. Única fixture antiga mudou ordem de Origem para3 apenas quando criação (mover conserva1); todas as assertions e quatrocombinações concorrentes permanecem. Sem SPEC_DEVIATION. A alteração do mecanismo de unicidade foi registrada no design/context/spec antes do commit.

### Handoff para MySQL fictício

Fontes/gates backend encerrados após este gate; não iniciar Maven paralelo. Classes recomendadas: DemandaConsultaTests(6), DemandaCadastroTests(92), DemandaCadastroConcurrencyTests(14), EtapaDemandConcurrencyTests(7), ProcessoArchiveTests(11), HistoricoArchiveTests(15). Total selecionado145. O teste `falhasReaisDeDemandaOuHistoricoRevertemTudo` usa ADD CONSTRAINT CHECK e DROP CONSTRAINT em processos/historicos; a sintaxe precisa ser provada em MySQL8.0.43, sem excluir preventivamente esses quatrocasos. `restricaoUnicaFisicaRetorna409ERollbackSePrecheckNaoEncontra` deve provar UNIQUE1062/409/rollback nas2rotas, com precheck advisory desativado e INSERT real. `numeroEntreQuadrosDisputaAteCommitEConfirmaSomenteUma` tem8casos incluindo caixa/rotas; `cadastroDisputaArquivoRemocaoEReordenacaoSemEstadoParcial` tem6casos. `linhaGlobalArquivadaSemCriadorNaoImpedeCadastroNemMudaDados` tem2casos. UTF-16/emoji no limite deve persistir com charset fictício apropriado, sem mexer na grafia ou schema configurado.
