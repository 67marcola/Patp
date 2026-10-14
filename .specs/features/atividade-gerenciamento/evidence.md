# Evidências de implementação da atividade

Estado: em andamento. Tarefas Implemented não significam Verified; Verifier independente posterior a T18.

## T18 — fluxo real e paginação mista

13/10/2026:1087/0/0/0 Java/H2 PASS;1176/1176 Vitest em28 arquivos PASS;18/18 Edge/H2 em9 arquivos PASS (15 anteriores+3 novos da feature);847/0/0/0 MySQL fictício PASS;lint0/buildTEMP0. Playwrightretries0 e nenhumskip. LogsTEMP/atividade-T18-{java,vitest,lint,build,browser,mysql}.log. BuildTEMP/atividade-T18-build-fa12a421877d4f13af43c77856cb7b9f. Capturas desktop/360 do gate completo: TEMP/creral-atividade-T18-fluxo-ggWZ1x eTEMP/creral-atividade-T18-mista-Jv7nmF;capturas alvo iniciaisQk2F2Z/9i7iL3 também preservadas. Inspeção visual confirma texto literal/rolagem/controles íntegros;boundingBoxes provam limites de cada controle.

Premissas: ações reais nas APIs existentes/edição/comentário pelo navegador,duas sessões reais;somente H2MEM recebe fixture antiga antes das ações. Arquivosatividade.spec.js,isolated-system.mjs,run-e2e.mjs,OPERACAO e acompanhamento. Sem mudança em produto/outcomes existentes. Fixtures corrigidas mecanicamente: helper aguarda Atividade enabled antes foco/Enter;reload volta à lista e reabre nome atual antes painel,conservando assertion20/foco. Primeirotarget mistaPASS com duas falhas de navegação;logTEMP/atividade-T18-target-inicial-falhas-fixture.log preservado. Target repetido3PASS24.1s e depois gate integral18PASS. Um Vitest antigo QuadroEtapas:65 excedeu5000ms em5033ms,1175PASS/1FAIL;logTEMP/atividade-T18-vitest-inicial-timeout.log preservado. Repetição íntegra isolada1176PASS55.72s,sem alterar teste/helper/timeout.

Fixture121 históricos: o runner marca1 somente no arquivoatividade.spec.js e0 nos outros;cria arquivoSQL em seu mkdtemp próprio (alvo inicialTEMP/creral-preview-lK69QI/atividade-ficticia.sql,23883bytes). H2MEM/driverH2/create-drop já são fixos;flags init diferida/modealways/datafile/UTF8 carregam quadro900000 criadorNULL,etapa/demanda900001 e históricos900001–900121. Só essa fixture H2 relaxa data_hora/descricao para o último legadoNULL. Nenhumusuário/admin/hash/sessão é inserido;contas e comentários são APIs reais. Não há endpoint/backdoor,migração real ou configuração de produção alterada.

MySQL final: seleção Maven `-Dtest=Atividade*Tests,Demanda*Tests,ProcessoArchiveTests,HistoricoArchiveTests,Comentario*Tests,EtapaDemandConcurrencyTests,GerenciamentoArchiveApiTests,EtapaArchiveTests`,ENVdatasourcejdbc:mysql://127.0.0.1:33823/creral_arquivo_test?useSSL=false&allowPublicKeyRetrieval=true&connectionTimeZone=LOCAL,driverMySQL/create-drop,credenciais somente fictícias. Antes de iniciar,datadir resolvido sobTEMP eporta livre;após iniciar,CIM/listener atualPID19572 eSQL `SELECT @@port,@@datadir,DATABASE(),@@version` confirmaram33823,TEMP/creral-arquivo-049cfd5cc86d4d8b840e41f1710d7a43/mysql,creral_arquivo_test,8.0.43. Após847PASS,identidade atual19572 revalidada;mysqladminrootfictíciosem senha shutdown0 elistener ausente. Nunca reutilizadoPID13172nem banco real.

B=`frontend/e2e/atividade.spec.js`.

| Critério / AC | file:line + assertion exata | Outcome da spec / cobertura |
| --- | --- | --- |
| ACT-01–14 ações reais/sem duplicação | B:80 `expect(atividade.registros).toHaveLength(20)` eCursorNull;B:81 `expect(new Set(atividade.registros.map(r => r.tipo))).toEqual(new Set([...16tipos...]))`;B:79 histórico10 |16 tipos dos20 envios aceitos,10históricos de demanda conservados e não duplicados; coberto vertical |
| ACT-15/18/29 metadados reais | B:82 `expect(atividade.registros.every(r => r.origem === "AUTOMATICA" && r.gerenciamentoId === quadro.id && r.autorId === (...) && r.autorNome === (...) && r.dataHora && r.descricao === null)).toBe(true)` | Quadro/cada autor real/horário presentes,sem descrição inventada; coberto |
| ACT-14/19 comentário | B:78 `expect(aposComentario.gerenciamento.versao).toBe(s.gerenciamento.versao)` eDemandasEqual;B:83 `expect(envio).toMatchObject({...dataHora:comentarioSalvo.dataHora,alteracoes:[]})`;B:86 JSONsemMensagem | ContaColegaFluxo,hora exatamente salva,sem texto/ciclo/versão/histórico extra; coberto |
| ACT-03/13 antes/depois arquivo | B:84 `expect(atividade.registros.find(r => r.tipo === tipo).alteracoes).toEqual([{ alvoTipo, alvoId, alvoNome, campo: "arquivado", anterior, novo }])` nosquatro tiposfalse->true/true->false | Strings JSON exatas sem exigir formato técnico naUI; coberto |
| ACT-02/15 snapshot quadro | B:85 `expect(quadroIntermediario.alteracoes).toEqual([...nome/descricao...])`;B:92 painelNomeIntermediario | Nome e descrição anteriores/novos e alvo intermediário preservados após rename posterior; coberto |
| ACT-10 homônimas | B:86 `expect(movimento.alteracoes).toEqual([...etapaId,etapaNome...])`;B:91 UITrabalhoIDorigem/destino | IDs distintos e nomesTrabalho/Trabalho,sem falsa renomeação; coberto |
| ACT-06/15 remoção | B:87 `expect(excluida.alvoId).toBe(removida.id)`,`expect(excluida.alvoNome).toBe("Removida literal")`,nome antigo->null;B:91 UI | Identificação conservada sem entidade; coberto |
| ACT-09/16/36 edições literais10000/null/vazia | B:88 `expect(edicao.alteracoes).toEqual([...pessoa,responsavel,observacoes...])`;B:90 `expect(...dd.allTextContents()).toEqual([antes.trim(), depois.trim()])` eAna/Não informado;img/b/script0;B:92 mensagemNenhumcampo | Dados normalizados reais,literais10000 escapados,vazio explicado; coberto |
| ACT-20/23/30/34 persistência/leitor | B:70 quadroarquivado consultado;B:89 chavesUIExactly20;B:94 reload20/foco;B:79 histórico10 | Leitura mesmo contrato,autorcolega e persistência sem duplicação; coberto |
| ACT-22 legado literal/null | B:106 `expect(await antigo.locator("h3").textContent()).toBe(" Ação antiga 120 ")` eDescriçãoLiteralexata;B:107 semtime;B:112 histórico121/campos/data/null exatos | Origem identificada,textos/autor/data/ausências preservados; coberto |
| ACT-25–27/32/33 conjunto fixo | B:101 página50/primeiroauto/hora;B:104 páginas100/122;B:105 `expect(...dataset.atividadeChave...).toEqual(esperados)` com120datasempatadasIDsDESC+NULLlast eColegaAusente;B:108–110 refresh50/100/123,`expect(atuais.slice(1)).toEqual(esperados)` | Inserção entre páginas só aparece no novo conjunto,sem repetição; coberto |
| ACT-29/36 sem mensagem/controlos | B:110 mensagensprivadasausentes;B:93/111 capturas/B:23/24 boundingBoxes desktop1440/360 | Textoescapado/controles sem corte,com leituraold; coberto |

| Reverse-map cenário / assertions | AC / Done-when | Manter |
| --- | --- | --- |
| Fluxo duas contas B:78–94:20 registros/16tipos/metadados/comentário/4arquivos/snapshots/edicao10000/movimento/removida/foco/reload | ACT-01–16/19/20/23/29/30/34/36 eDone-whenT18 | Sim |
| Fontes mistas B:101–112:50/100/122,fimNULL,ordem/chavesexatas,legado/null,refresh123/preservação | ACT-22/25–27/29/30/32/33/36 eDone-whenT18 | Sim |
| T17 preservado B:23/24/31–38 | ACT-20/30/34/35/36 | Sim |

A/B/C/D PASS:dois novos E2E deT18+T17,payload/valores e resultados reais,não só chamadas;matrizEdgeH2/retries0 seguida. T18 é prova vertical;erros/rollback/concorrência e demais critérios continuam nos mapas co-localizadosT1–17 e seleçãoMySQL,sem exigir cenários duplicados. Sem SPEC_DEVIATION. Incluído no diagnóstico,T1–T18 Implemented. Orquestrador ainda tratará ceiling da primeira resposta e booleano visual Sim/Não,então Verifier fresco/sensor/validate_state;nenhumACT Verified ou feature concluída neste commit.

Payloads completos das assertions abreviadas no mapaT18,mesmo B acima:

```js
// B:81
expect(new Set(atividade.registros.map(r => r.tipo))).toEqual(new Set(["QUADRO_CRIADO", "QUADRO_EDITADO", "QUADRO_ARQUIVADO", "QUADRO_RESTAURADO", "ETAPA_CRIADA", "ETAPA_EDITADA", "ETAPA_REMOVIDA", "DEMANDA_CRIADA", "DEMANDA_EDITADA", "DEMANDA_MOVIDA", "DEMANDA_CONCLUIDA", "DEMANDA_CANCELADA", "DEMANDA_REABERTA", "DEMANDA_ARQUIVADA", "DEMANDA_RESTAURADA", "COMENTARIO_ADICIONADO"]));
// B:82
expect(atividade.registros.every(r => r.origem === "AUTOMATICA" && r.gerenciamentoId === quadro.id && r.autorId === (r.tipo === "COMENTARIO_ADICIONADO" ? colega.id : dona.id) && r.autorNome === (r.tipo === "COMENTARIO_ADICIONADO" ? "ColegaFluxo" : "AutoraFluxo") && r.dataHora && r.descricao === null)).toBe(true);
// B:83
expect(envio).toMatchObject({ alvoTipo: "DEMANDA", alvoId: demanda.id, alvoNome: demanda.numeroProcesso, autorId: colega.id, autorNome: "ColegaFluxo", dataHora: comentarioSalvo.dataHora, descricao: null, alteracoes: [] });
// B:85
expect(quadroIntermediario.alteracoes).toEqual([{ alvoTipo: "QUADRO", alvoId: quadro.id, alvoNome: "<b>Nome intermediário</b>", campo: "nome", anterior: quadro.nome, novo: "<b>Nome intermediário</b>" }, { alvoTipo: "QUADRO", alvoId: quadro.id, alvoNome: "<b>Nome intermediário</b>", campo: "descricao", anterior: "Descrição anterior", novo: "Descrição corrigida\nLiteral" }]);
// B:86
expect(movimento.alteracoes).toEqual([{ alvoTipo: "DEMANDA", alvoId: demanda.id, alvoNome: demanda.numeroProcesso, campo: "etapaId", anterior: String(origem.id), novo: String(destino.id) }, { alvoTipo: "DEMANDA", alvoId: demanda.id, alvoNome: demanda.numeroProcesso, campo: "etapaNome", anterior: "Trabalho", novo: "Trabalho" }]);
// B:88
expect(edicao.alteracoes).toEqual([{ alvoTipo: "DEMANDA", alvoId: demanda.id, alvoNome: demanda.numeroProcesso, campo: "pessoa", anterior: "Cliente anterior", novo: "<b>Cliente novo</b>" }, { alvoTipo: "DEMANDA", alvoId: demanda.id, alvoNome: demanda.numeroProcesso, campo: "responsavel", anterior: "Ana", novo: null }, { alvoTipo: "DEMANDA", alvoId: demanda.id, alvoNome: demanda.numeroProcesso, campo: "observacoes", anterior: antes.trim(), novo: depois.trim() }]);
// B:89
expect(await painel.locator("[data-atividade-chave]").evaluateAll(es => es.map(e => e.dataset.atividadeChave))).toEqual(atividade.registros.map(r => r.chave));
// B:90
expect(await editada.locator(".atividade-alteracao").filter({ hasText: "Observações" }).locator("dd").allTextContents()).toEqual([antes.trim(), depois.trim()]);
expect(await editada.locator(".atividade-alteracao").filter({ hasText: "Responsável" }).locator("dd").allTextContents()).toEqual(["Ana", "Não informado"]);
// B:105/110
expect(await painel.locator("[data-atividade-chave]").evaluateAll(es => es.map(e => e.dataset.atividadeChave))).toEqual(esperados);
expect(atuais.slice(1)).toEqual(esperados);
```

## T17 — cabeçalho e coordenação

13/10/2026: gatebrowser completo16/16 Edge/H2 em9 arquivos (15 anteriores+1 novo),sem falhas/skips/retries;lint0/buildTEMP0. Java1087 eVitest1176/28 arquivos PASS em12/10 (20 novosVitestT17). LogsTEMP/atividade-T17-{java,vitest,lint,build,browser}.log. BuildTEMP/atividade-T17-build-26bbf69713d842fab5b4ce56967e535b;capturasdesktop/360/arquivadoTEMP/creral-atividade-T17-L3Icgk,inspeção visual confirma controles íntegros/rolagem.

Premissas: atividade ligada ao ID do quadro,apenas leitura para todos;exclusão inclui outrosFormularios e guard dos handlers;foco depois de reabilitar botão só quadro montado correspondente. ArquivosQuadro.jsx,QuadroAtividade.test.jsx,atividade.spec.js,keyboard.js e acompanhamento. PrimeiroVitest1175PASS/1FAIL: limite mecânico30Tabs do helper impedia alcançar controle após cabeçalho extra;ajuste autorizado conta controles realmente focáveis+1 e mantém `expect(document.activeElement).toBe(controle)`/Enter,sem timeout. PrimeiroE2E novo esperava200 no setup arquivo,recebeu204;usuário autorizou explicitamente13/10 somente200→204 nessa assertion. ContratoGerenciamentoController@ResponseStatus(NO_CONTENT)/void já era204;produto inalterado. Um crash nativo3221226505 na retomada ocorreu antes de qualquerteste;portas livres/processos próprios ausentes conferidos e gate repetido íntegro PASS. Nenhum outro outcome mudou,SPEC_DEVIATION ausente.

Q=`frontend/src/pages/QuadroAtividade.test.jsx`;B=`frontend/e2e/atividade.spec.js`.

| Critério / AC | file:line + asserção exata | Outcome / cobertura |
| --- | --- | --- |
| ACT-20/30 criador/admin/leitor/arquivo | Q:20 `expect(within(card).getAllByRole("button").map(b => b.textContent)).toEqual(...)` com listas exatas;Q:21 leitura vazia/URL exata;B:31 `await expect(painel).toContainText("Quadro criado")` eDono | Cabeçalho acessível sem mudar ações/permissões dos cartões; coberto |
| ACT-35 dez formulários/rascunhos | Q:25 `expect(atividade().disabled).toBe(true)`;Q:26 `expect(screen.queryByRole("complementary", { name: "Atividade", exact: true })).toBeNull()`,`expect(campo.value).toBe("Rascunho preservado")` | Guard conserva rascunhos mesmo forçando atributo disabled; coberto |
| Comentários/atividade exclusiva | Q:30 rascunhoRascunho eatividadeNull;Q:34 disabled para8 ações;Q:35 textbox/dialog/comentáriosNull,VoltarNão | Nenhum fluxo simultâneo; coberto |
| ACT-34 retorno foco/ID | Q:40 `expect(document.activeElement).toBe(b)` eNovaEtapaenabled;Q:45 `expect(sinal.aborted).toBe(true)`,painelNull,novoBotãoNãoFocado | Retorno só mesmo quadro,mudança ignora late; coberto |
| Pending preservado | Q:50 atividadeDisabled,painelNull,`expect(screen.getByLabelText("Nome da etapa").value).toBe("Nova")` | Mutação pendente conserva campos/bloqueios; coberto |
| ACT-20/34/35 leitura arquivada/foco real | B:32 botões exatamenteFechar/Atualizar;B:34 Escape/painel0/foco;B:35 HTTP204;B:37 criado+arquivado;B:38 focoFechar | API/sessões reais preservam leitura,sem escrita no painel; coberto |
| ACT-36 dimensões/controlos | B:23 limites esquerda/direita/baixo/semOverflow;B:24 boundingBoxcontroles dentroviewport;B:33/37 capturas1440/360 | Desktop/celular usáveis; coberto |

| Reverse-map grupos / assertions | AC / Done-when | Manter |
| --- | --- | --- |
| Quatro perfis Q:20/21 | ACT-20/30 | Sim |
| Dezformulários Q:25/26;comentário Q:30;atividade Q:34/35 | ACT-35 | Sim |
| Fechar2 Q:40;troca Q:45;pending Q:50 | ACT-34/35 | Sim |
| CenárioAPIreal B:31–38 econtrolesB:23/24 | ACT-20/30/34/35/36 | Sim |

A/B/C/D PASS:20 Vitest+1E2E novo,outcomes exatos/guards/dados/teclado reais;matriz Vitest/EdgeH2 e padrões existentes seguidos. Incluído no diagnóstico,T17 Implemented,Verifier pendente. Precisões pequenas identificadas pelo orquestrador (booleano visual eceiling de primeira resposta) serão tarefas próprias antes da verificação final,sem expandir este commit.

## T16 — painel de leitura

12/10/2026:1156/1156 Vitest em27 arquivos PASS,25 novos;lint0/buildViteTEMP0. Logs TEMP/atividade-T16-{vitest,lint,build}.log,buildTEMP/atividade-T16-build-0a4746f11a2249aa847f74142e32dc9d. Premissas: painel keyed quadro/token,fechar chama retorno seguro da páginaT17; somente GET,sem timer. Arquivos AtividadeLateral.jsx/test.jsx,index.css e acompanhamento. Primeiro gate1154PASS/1FAIL evidenciou renderer de edição vaziaETAPA_EDITADA; corrigido produto sem mudar teste. Revisão StrictMode acrescentou prova do ciclo real e correção do lock no cleanup. Nenhum outcome alterado/skip/SPEC_DEVIATION.

P=`frontend/src/components/AtividadeLateral.test.jsx`.

| Critério / AC | file:line + asserção exata | Outcome / cobertura |
| --- | --- | --- |
| ACT-31/34 StrictMode/loading/vazio/foco | P:21 `expect(f.mock.calls[0][1].signal.aborted).toBe(true)` e novafalse; P:22 `expect(document.activeElement).toBe(screen.getByRole("button", { name: "Fechar atividade" }))`; P:26 status literal; P:27 vazio literal | Consulta cancelada não prende nova;mensagens/foco exatos; coberto |
| ACT-30/36 antes/depois literal10000 | P:33 ação/alvo/autor/hora via `toContain`; P:34 `expect(item.textContent).toContain(r.alteracoes[0].anterior)` e novo; `expect(item.querySelector("img")).toBeNull()`; P:35 Antes/Depois | Todas informações legíveis,escapadas e completas; coberto |
| ACT-22 legado/null | P:39 `expect(itens()[0].textContent).toContain(r.tipo)`,origem/Não informado,semHTML/time/confirmad | Sem confiança ou preenchimento inventados; coberto |
| ACT-16/14 edição vazia/comentário | P:43 mensagem literal3 tipos; P:46 `toContain("Comentário adicionado")`,`not.toContain("Nenhum campo")`,textboxNull | Edição vazia explicada;envio não é edição informativa; coberto |
| ACT-10/36 homônimas/outros alvos | P:50 `toContain("Trabalho (ID 3)")`/ID4; P:54 Não informado/Outra/Posição | Origem/destino distinguíveis,remoção e reordenação identificadas; coberto |
| ACT-31/37 falhas iniciais | P:58 `expect(screen.queryByText("Nenhuma atividade registrada.")).toBeNull()` e retryvazio/métodosGETGET | Não mascara erro como vazio; coberto |
| ACT-32 paginação/duplo clique/falhas | P:62 disabled e duas chamadas; P:63 `expect(itens()).toHaveLength(51)` e última chave50; P:67 tamanho50,apósretry51,URLsiguais | Append único;lista/cursor conservados nas3 falhas; coberto |
| ACT-33 refresh | P:71 `expect(itens()).toHaveLength(50)`,aviso; P:72 tamanho1/chave99,URLsemcursor/avisoNull | Conserva no erro,substitui no sucesso com novo conjunto; coberto |
| ACT-34 fechamento/quadro/token/unmount | P:77 `expect(f.mock.calls[0][1].signal.aborted).toBe(true)` e listNull; P:83 abort/listNull/fecharNão/novoToken; P:86/87 abort/complementaryNull/fecharNão | Nenhuma resposta antiga aplicada,foco callback apenas fechamento; coberto |

| Reverse-map grupos / assertions | AC / Done-when | Manter |
| --- | --- | --- |
| StrictMode P:21/22;loading/vazio/read-only P:26–28 | ACT-31/34 | Sim |
| Metadados/literal P:33–35,legado P:39 | ACT-22/30/36 | Sim |
| Edições3 P:43,comentário P:46 | ACT-16/14/30/36 | Sim |
| Homônimas P:50,remoção/reordenação P:54 | ACT-10/36 | Sim |
| Falhas3 P:58 | ACT-31/37 | Sim |
| Append/duplo P:62/63;falhas3 P:67 | ACT-32/37 | Sim |
| Refresh2 P:71/72 | ACT-33 | Sim |
| Fechar2 P:77;trocas2 P:83;unmount2 P:86/87 | ACT-34 | Sim |

A/B/C/D PASS:25 casos,outcomes reais específicos e dados completos,não só chamadas;matriz/padrões ComentariosDemanda seguidos. CSS emindex.css carregado;dimensões reais serão provadas emT17/T18. Incluído no diagnóstico,T16 Implemented,Verifier pendente.

## T15 — contrato do cliente

12/10/2026: `npm.cmd test` exit0,1131/1131 testes em26 arquivos,zero falhas/skips.131 novos casos no cliente,1000 anteriores preservados. Log TEMP/atividade-T15-vitest.log. Premissas: assinatura token/ID/cursor/options e contrato real de13 campos; null somente onde documentado. Arquivos: api.js,apiAtividade.test.js e acompanhamento. Nenhum outcome antigo alterado. Fixtures malformed de cursor usam50 registros para isolar o formato; o caso de página curta fica separado. Sem SPEC_DEVIATION.

A=`frontend/src/services/apiAtividade.test.js`.

| Critério / AC | file:line + asserção exata | Outcome / cobertura |
| --- | --- | --- |
| ACT-29 contrato inteiro/autoria/vínculos/signal | A:13 `expect(await consultarAtividade("sessao", 9, null, { signal })).toEqual(p)`; A:14 `expect(f.mock.calls[0]).toEqual([...])` com URL/token/signal literais | Todas13 propriedades e alterações6 propriedades conservadas,rota correta; coberto |
| ACT-22/26 vazio/literais/null | A:17/19/20 `expect(await consultarAtividade("s", 9)).toEqual(p)` | Vazio[],legado desconhecido/null/literal sem normalizar; coberto |
| ACT-26/27/28 página e cursor | A:24/26 `expect(await consultarAtividade(...)).toEqual(p)`; A:27 URL exata; A:89 `await expect(consultarAtividade(...)).rejects.toMatchObject({ message: erro })`; A:92/93 mesma rejeição |50,offset+50,ceilings fixos,última página limitada à entrada; coberto |
| ACT-37 fontes sem colidir | A:31 `expect(await consultarAtividade("s", 9)).toEqual(p)` | MesmoID em fontes diferentes permitido; coberto |
| ACT-10 contexto homônimo | A:36 `expect(await consultarAtividade("s", 9)).toEqual(p)` | etapaId3->4 e etapaNomeTrabalho->Trabalho preservados; coberto |
| ACT-15/16/36 literal10000/vazio | A:39/40 `expect(await consultarAtividade("s", 9)).toEqual(p)` | Sem truncamento,lista[] válida; coberto |
| Tipos/etapas secundárias válidos | A:44/48 `expect(await consultarAtividade("s", 9)).toEqual(p)` |16 enums e snapshots/reordenação multialvo aceitos; coberto |
| ACT-28/29/37 página inválida inteira | A:80 `await expect(consultarAtividade("s", 9)).rejects.toMatchObject({ message: erro, status: 200 })`; A:85 mesma expressão | Ausentes/extras/tipos/IDs/vínculos/enum/mudança/igualdade/51/duplicação/cursor e legado inválido recusados integralmente; coberto |
| ACT-31/34/37 transporte/status | A:95/96 `rejects.toMatchObject({ message: erro, status })` ou erro servidor; A:98/99 rejeição exata; A:100 `rejects.toBe(abort)` | Erros explícitos,não lista vazia,abort mantido; coberto |

| Reverse-map teste / asserção | AC / Done-when | Manter |
| --- | --- | --- |
| Valores integrais/sessão A:13/14 | ACT-29/37 | Sim |
| Vazio/legado A:17/19/20 | ACT-22/26/37 | Sim |
| Página/continuação A:24/26/27,89,92/93 | ACT-26/27/28/37 | Sim |
| Fontes distintas A:31 | ACT-37 | Sim |
| Homônimas A:36 | ACT-10,edge homônimos | Sim |
|10000 e vazia A:39/40 | ACT-15/16/36 | Sim |
|16 enums e4 multialvos A:44/48 | ACT-01/07/29/37 | Sim |
| Matriz inválidos A:80,legado A:85 | ACT-28/29/37 | Sim |
| Status/HTTP/rede/JSON/abort A:95–100 | ACT-31/34/37 | Sim |

A/B/C/D PASS: cada campo verificado por igualdade integral/recusa exata; nenhuma asserção somente de chamada substitui resultado. Todos casos reversamente ancorados no contrato/spec; padrões Vitest/apiComentarios e matriz seguidos. Incluído no diagnóstico; T15 Implemented,Verifier pendente. Precisão documental T14 corrigida de14 para13 chaves conforme assertion Q:45,sem mudar Java/testes.

## T1 — entidade

10/10/2026: Maven -B test-compile exit0, BUILD SUCCESS,54 fontes principais/45 fontes de testes. Nenhum teste executado neste gate, conforme matriz de entidade. validate_tasks exit0, aviso esperado Tests:none conferido.

| Critério | Evidência | Resultado esperado | Cobertura |
| --- | --- | --- | --- |
| FK obrigatória ao quadro; sem FK/cascade ao alvo/autor | Atividade.java:24 `@ManyToOne(optional = false)`; :34–44 IDs/nomes escalares | Identificação independente do alvo/autor | Inspeção de mapeamento e compile |
| Valores longos e precisão | Atividade.java:46 `TIMESTAMP(6)`; :49 `LONGTEXT` | Micros e JSON longo | Compile; persistência real atribuída a T2 |
| Vínculo único | Atividade.java:52 `@Column(name = "historico_id", unique = true)` | Vínculo escalar único | Compile; persistência real atribuída a T2 |

Reverse-map: sem testes novos em T1, conforme matriz; nenhuma asserção especulativa. Adequação A/B/C/D: entidade sem lógica, gate compile aprovado; comportamento e rollback serão provados no writer T2. Sem desvio da spec ou alteração de testes antigos.

## T2 — writer transacional

10/10/2026: gate Java completo `mvn.cmd -B test` exit0,853 testes (840 anteriores+13 novos),0 falhas/erros/skips. Log TEMP/atividade-T2-java.log. Fixtures recebem limpeza de atividades e snapshot adicional sem alterar outcomes antigos. Writer MANDATORY, whitelist sem texto de comentário, JSON largo e histórico validado no mesmo quadro. Identidade composta tipo/ID conserva quadros/etapas com IDs iguais. Ajuste mecânico no design: nomes existentes prazoGeral/observacoes.

Todos os caminhos abaixo são `sistema/src/test/java/com/patp/sistema/AtividadeRegistroTests.java`.

| Critério / AC | file:line + asserção | Resultado da spec |
| --- | --- | --- |
| ACT-15 snapshots, autor e alvo | :33 `assertThat(salvo.getGerenciamento().getId()).isEqualTo(q.getId())`; :34 tipo ETAPA_CRIADA/alvo ETAPA; :35 alvoId=e.id/nome Etapa; :36 autorId=u.id/nome Autor | Snapshots completos independentes do estado futuro |
| Horário micros, vínculo ausente | :37 `assertThat(salvo.getDataHora()).isEqualTo(LocalDateTime.of(2026,1,2,3,4,5,123456000))`; :38 `getHistoricoId().isNull()` | Horário preciso e vínculo opcional |
| ACT-29 sem segredo | :40 `assertThat(salvo.getAlteracoesJson()).doesNotContain("senha","hash-ficticio","email")` | Somente valores de atividade |
| Transação existente | :44 `assertThatThrownBy(...).isInstanceOf(IllegalTransactionStateException.class)`; :46 `atividades.count().isZero()` | Não iniciar transação independente |
| Null/literal/ordem | :54 tamanho2; :55 campo nome; :56 anterior literal espaços; :57 novo Novo; :58 `anterior.isNull().isTrue()`; :59 novo literal espaços | Valores persistidos sem normalização inventada |
|10000 caracteres | :67 `assertThat(...anterior...).isEqualTo(antes)`; :68 `assertThat(...novo...).isEqualTo(depois)` | Literais completos á/ç10000 |
| Edição vazia | :73 `assertThat(a.getAlteracoesJson()).isEqualTo("[]")` e count1 | Lista vazia aceita |
| Remoção/snapshot futuro | :80 nomes Antes/Autor; :81 anterior4; :82 novo null | Registro preservado após remoção/renomeação |
| ACT-23 unicidade e vínculo | :89 DataIntegrityViolationException; :91 `conteudoComAtividade().isEqualTo(antes)`; :92 historicoId=h.id | Não vincular duas vezes |
| Mesmo quadro | :97 exceção com mensagem exata; :99 `conteudoComAtividade().isEqualTo(antes)` | Recusar histórico de outro quadro |
| ACT-17 falha do evento | :108 `assertThat(conteudoComAtividade()).isEqualTo(antes)` | Ação/histórico/atividade revertidos |
| ACT-17 falha posterior | :116 `assertThat(conteudoComAtividade()).isEqualTo(antes)` | Evento já inserido revertido |
| Comentário sem mensagem | :120 IllegalArgumentException mensagem exata; :123 snapshot igual | Sem alterações informativas/texto |
| Ordem/alvos/campos criação | :128 `assertThat(diferencas(...)).containsExactly(...)` com IDs3/8, nomes/setor/ordem e anteriores null | Ordenar por ID e omitir null inicial |
| Edge quadro+etapa mesmoID | :136 `assertThat(diferencas(...)).containsExactly(...)` com6 alterações explícitas | Identidade composta, ambos completos |

| Reverse-map (teste e asserção) | AC / Done-when | Manter |
| --- | --- | --- |
| persisteTodosOsSnapshotsComMicrosEAutorReal :33–40 valores/snapshots exatos | ACT-15/29, precisão e persistência T2 | Sim |
| exigeTransacaoExistenteSemGravar :44/46 exceção+zero | Transação existente T2 | Sim |
| diferencasConservamNullEspacosEOrdemFixa :54–59 valores literais | Edge null/espaços e diferenças T2 | Sim |
| gravaAntesDepoisLiteralDeDezMilCaracteres :67/68 igualdade literal | Edge10000 | Sim |
| edicaoIdenticaPersisteListaVazia :73 JSON[]/count1 | Diferenças T2, ACT-16 | Sim |
| remocaoEAlteracaoPosteriorNaoMudamIdentificacaoHistorica :80–82 nomes/antes4/depoisnull | ACT-15, snapshot sem FK | Sim |
| vinculoHistoricoUnicoReverteSegundaGravacao :89/91/92 unicidade/preservação/vínculo | ACT-17/23 | Sim |
| recusaVinculoDeOutroQuadroSemMudancas :97/99 exceção/preservação | Vínculo mesmo quadro T2 | Sim |
| falhaDoEventoReverteAcaoEHistorico :108 snapshot igual | ACT-17 | Sim |
| falhaPosteriorReverteEventoJaGravado :116 snapshot igual | ACT-17 | Sim |
| comentarioNaoPermiteMensagemNemAlteracoes :120/123 recusa/preservação | ACT-29, texto excluído T2 | Sim |
| criacaoOrdenaTodasAsPosicoesPorIdSemNullInventado :128 lista exata | Diferenças ordenadas T2 | Sim |
| criacaoPreservaQuadroEEtapaComMesmoId :136 lista exata | Edge snapshots iniciais ACT-01 | Sim |

Adequação A/B/C/D aprovada: resultados específicos e snapshots completos; nenhuma asserção somente de chamada, teste excluído/ignorado ou novo requisito. Padrões JUnit/H2+ApiIntegrationSupport do projeto; Verifier permanece pendente.

## T3 — criação do quadro

10/10/2026: `mvn.cmd -B test` exit0,860 testes,0 falhas/erros/skips (7 novos). Log TEMP/atividade-T3-java.log. Sem mudança de outcomes anteriores; spy falha na persistência para provar rollback real. Arquivo abaixo: `sistema/src/test/java/com/patp/sistema/AtividadeQuadroCriacaoTests.java`.

| Critério / AC | file:line + asserção | Esperado |
| --- | --- | --- |
| ACT-01/15 evento único/autor/alvo/tempo | :35 `atividades.count().isEqualTo(1)`; :36 tipo QUADRO_CRIADO/alvo QUADRO; :37 IDs quadro; :38 nome Quadro novo/autorID/u.id/autorCriador; :39 `dataHora.isBetween(inicio,now+1s)` e nano%1000 zero | Uma ação com snapshots e tempo servidor |
| ACT-19 versão e iniciais | :40 `criado.versao().isZero()`; :41 tamanho6/3; :42 nome Quadro novo; :43 descrição literal; :44 false | Versão atual e valores reais |
| Edge trabalho inicial e finais automáticas | :46–51 ID/tipo/nome/campo/anteriornull/novo Trabalho/Setor/7; :53 count etapas3/2; :54 `alteracoesJson.doesNotContain("Concluídos","Cancelados")` | Snapshot iniciais fornecidas, sem falsas ações de finais |
| ACT-18 autoria forjada ignorada | :61 `autorId.isEqualTo(u.getId())`, nomeReal; :62 tipo QUADRO_CRIADO e semForjado | Autor/tipo do serviço |
| ACT-18 validação/sessão | :66 mensagem nome exata+:67 snapshot igual; :71 mensagem etapa+:72 snapshot igual; :76 sessão inválida+:77 snapshot igual | Recusas sem escrita |
| ACT-17 rollback | :82 `assertThatThrownBy(...).hasMessage("Falha do diário")`; :83 `conteudoComAtividade().isEqualTo(antes)` | Sem quadro/etapas/evento parcial |

| Reverse-map teste + asserção | AC / Done-when | Manter |
| --- | --- | --- |
| criacaoUnicaConservaQuadroEtapasIniciaisAutorEHorario false/true :35–54 valores completos | ACT-01/15/19 e edge iniciais/finais | Sim |
| payloadForjadoNaoEscolheAutorOuTipo :61/62 autor/tipo | ACT-18 | Sim |
| nomeInvalidoConservaTodaPersistencia :66/67 | ACT-18 validação | Sim |
| etapaInvalidaConservaTodaPersistencia :71/72 | ACT-18 validação | Sim |
| sessaoInvalidaConservaTodaPersistencia :76/77 | ACT-18 sessão | Sim |
| falhaDoDiarioReverteQuadroEIniciaisEFinais :82/83 | ACT-17 | Sim |

Adequação A/B/C/D aprovada: sete cenários, contagens completas, snapshots/rollback e valores específicos; guideline matriz JUnit/H2 existente. Feature em andamento, Verifier pendente.

## T4 — edição e arquivo do quadro

10/10/2026: gate Java completo exit0,873 testes,0 falhas/erros/skips,13 novos. Log TEMP/atividade-T4-java.log. Captura após guard.bloquear; getter `Gerenciamento.getMarcadorArquivado()` com JsonIgnore conserva o marcador bruto null sem mudar isArquivado/DTO. Sem alteração de outcomes antigos.

Arquivo: `sistema/src/test/java/com/patp/sistema/AtividadeQuadroAlteracaoTests.java`.

| Critério / AC | file:line + asserção | Esperado |
| --- | --- | --- |
| ACT-02/15/19 edição/criador/admin | :33 `assertThat(r.versao()).isEqualTo(1L)`/count1; :34 tipoQUADRO_EDITADO; :35 autorID/nome; :36 alvoNovo; :37 tamanho2/camponome; :38 anteriorQuadro/novoNovo; :39 campodescricao/anteriorDescrição; :40 novo literal espaços | Dois campos diferentes com autor real e versão1 |
| ACT-16 edição idêntica | :44 versão1/count1; :45 `assertThat(atividades.findAll().get(0).getAlteracoesJson()).isEqualTo("[]")` | Um evento vazio preservando incremento |
| ACT-03/38 arquivo/repetição/restauração | :50 `assertThat(conteudoComAtividade()).isEqualTo(antes)` após repetição; :51 versão2; :52 tipos exatos ARQUIVADO/RESTAURADO; :54–56 tamanho1/campoarquivado/false->true/true->false | Repetição sem evento/mudança, dois eventos reais |
| Edge null literal | :61 `assertThat(c.get(0).get("anterior").isNull()).isTrue()` e novo true | Null anterior não normalizado |
| ACT-18 cinco recusas | :74 `assertThatThrownBy(...).hasMessage(mensagem)` com mensagens literais switch:66–72; :75 `assertThat(conteudoComAtividade()).isEqualTo(antes)` | Permissão/versão/arquivo/nome/sessão sem escrita |
| ACT-17 duas falhas | :81 `assertThatThrownBy(...).hasMessage("Falha do diário")`; :82 snapshot igual | Rollback edição e arquivo |
| Captura depois de lock | :91 TimeoutException com latches; :93 tamanho2; :94 `assertThat(c.get(0).get("anterior").asText()).isEqualTo("Primeiro")`/novoSegundo/tamanho1; :95 versão2 | Segunda edição observa primeira confirmada |

| Reverse-map teste + asserção | AC / Done-when | Manter |
| --- | --- | --- |
| edicaoMostraAntesDepoisAutorRealEVersao false/true :33–40 | ACT-02/15/19 | Sim |
| edicaoIdenticaTemListaVaziaEIncrementoAtual :44/45 | ACT-16/19 | Sim |
| arquivoRestauracaoERepeticaoConservamEventosEVersoes :50–56 | ACT-03/38/19 | Sim |
| marcadorLegadoNullPreservaAnteriorLiteral :61 | Edge valores reais/null | Sim |
| recusasNaoAlteramDadosOuAtividade cinco casos :74/75 | ACT-18 | Sim |
| falhaDoDiarioReverteEdicaoOuArquivo dois casos :81/82 | ACT-17 | Sim |
| antesDepoisUsaEdicaoCommitadaAntesDoLock :91/93–95 | ACT-02, lock obrigatório | Sim |

Adequação A/B/C/D aprovada:13 cenários, valores/versão/dados completos e prova coordenada de lock, sem asserções apenas de chamadas; padrões locais JUnit/H2. Verifier pendente.

## T5 — criação de etapa

10/10/2026: gate Java completo exit0,887 testes,0 falhas/erros/skips,14 novos. Primeiro gate882 PASS; revisão de adequação acrescentou cinco branches ACT-18, novo gate887 PASS. Log TEMP/atividade-T5-java.log. Sem outcome antigo alterado. Snapshot escalar da sequência após lock e antes de normalização; finais criadas automaticamente não são ações adicionais. garantirPar não altera ordens das finais existentes, regra preservada.

Arquivo abreviado C=`sistema/src/test/java/com/patp/sistema/AtividadeEtapaCriacaoTests.java`, S=`sistema/src/test/java/com/patp/sistema/AtividadeEtapaSupport.java`.

| Critério / AC | file:line + asserção | Esperado |
| --- | --- | --- |
| ACT-04/15 evento completo único | S:21 `assertThat(atividades.count()).isEqualTo(1)`; S:23–27 tipo/alvoTipo/alvoID/nome/quadro/autorID/nome/historicoNull/dataMicros; S:28 `assertThat(json.readTree(a.getAlteracoesJson())).isEqualTo(json.readTree(json.writeValueAsString(esperado)))` | JSON inteiro com cada campo/valor esperado explícito |
| ACT-07 início/meio/fim | C:30 evento(expected :24–29 com nome/setor/ordem e cada reordenação); C:31 versão1; C:32 `assertThat(etapas.findById(a.getId()).orElseThrow().getOrdem()).isEqualTo(posicao==1?2:1)`; C:33 B=3/2; C:34 Nova=posição | Todos os efeitos anteriores/novos e estado persistido |
| Edge primeira/finais | C:39–40 evento três campos iniciais; C:41 `assertThat(etapas.findByGerenciamentoIdOrderByOrdemAscIdAsc(q.getId())).hasSize(3)` | Uma atividade, três etapas, sem falsas ações finais |
| Lacunas/empates | C:47–50 evento expected A5->1/B5->3/C12->4/Nova null->2; C:51–54 ordens1/3/4/2 | Todos os afetados registrados, sem reconstruir antes |
| ACT-18 permissão/versão/arquivo | C:61 `assertThatThrownBy(...).hasMessage(mensagem)`; C:62 `assertThat(conteudoComAtividade()).isEqualTo(antes)` | Três recusas conservam dados/atividade |
| ACT-18 nome/setor/posição/sessão/ausência | C:74 `assertThatThrownBy(...).hasMessage(mensagem)`; C:76 `assertThat(conteudoComAtividade()).isEqualTo(antes)` | Cinco erros exatos, zero efeitos |
| ACT-17 rollback | C:81 `assertThatThrownBy(...).hasMessage("Falha do diário")`; C:82 snapshot igual | Etapa, finais, normalização e versão revertidos |

| Reverse-map teste + asserção | AC / Done-when | Manter |
| --- | --- | --- |
| criaInicioMeioFimComTodasAsPosicoesAfetadas três casos C:30–34/S:21–28 | ACT-04/07/15/19 | Sim |
| primeiraEtapaSemEventosFalsosParaFinaisAutomaticas C:39–41/S:28 | ACT-04, edge finais | Sim |
| lacunasEEmpatesRegistramTodasAsOrdensAnterioresReais C:47–54/S:28 | ACT-07 | Sim |
| recusasConservamTodosOsDados três casos C:61/62 | ACT-18 | Sim |
| validacaoSessaoEQuadroAusenteNaoGeramEvento cinco casos C:74/76 | ACT-18 | Sim |
| falhaDoDiarioReverteEtapaNormalizacaoFinaisEVersao C:81/82 | ACT-17 | Sim |

Adequação A/B/C/D aprovada:14 cenários; JSON completo de cada alteração e posições de todos afetados, snapshot completo nas recusas. Nenhum teste só de chamada, nenhum teste sem vínculo à spec. Matriz JUnit/H2 seguida; Verifier pendente.

## T6 — edição de etapa

12/10/2026: gate Java completo `mvn.cmd -B test` exit0,904 testes (887 anteriores+17 novos),0 falhas/erros/skips. Log TEMP/atividade-T6-java.log. Trabalho parcial de10/10 preservado e auditado; gate anterior não foi executado porque a revisão automática interrompeu a chamada por limite de uso. Nenhum outcome antigo ou novo alterado na retomada. Captura escalar após lock e antes de editar/normalizar.

E=`sistema/src/test/java/com/patp/sistema/AtividadeEtapaEdicaoTests.java`; S=`sistema/src/test/java/com/patp/sistema/AtividadeEtapaSupport.java`.

| Critério / AC | file:line + asserção | Resultado esperado / cobertura |
| --- | --- | --- |
| ACT-05/15 campos nome/setor | S:28 `assertThat(json.readTree(a.getAlteracoesJson())).isEqualTo(json.readTree(json.writeValueAsString(esperado)))`; E:25 esperado nome A->Novo; E:26 setor Engenharia->Novo setor; E:30 `assertThat(alvo.getNome()).isEqualTo(campo.equals("nome")?"Novo":"A")` e setor correspondente | JSON inteiro com tipo/ID/nome/campo/anterior/novo exatos; somente campo diferente; coberto |
| ACT-07 todas posições | S:28 JSON inteiro; E:27 esperado A1->3/B2->1/C3->2; E:31 `assertThat(alvo.getOrdem()).isEqualTo(campo.equals("ordem")?3:1)`; E:32 B1/2; E:33 C2/3 | Antes/depois de todos os afetados e persistência completa; coberto |
| ACT-15/19 evento único/autor/versão | S:21 `assertThat(atividades.count()).isEqualTo(1)`; S:23–27 tipo/alvo/quadro/autor/histórico/horário; E:29 `assertThat(r.gerenciamento().versao()).isEqualTo(1L)` | Autor da sessão, alvo atual, uma atividade e incremento atual; coberto |
| ACT-16 idêntica | S:28 JSON igual à lista vazia passada E:38; E:38 `assertThat(r.gerenciamento().versao()).isEqualTo(1L)` | Evento de edição com[] e versão1; coberto |
| Edge lacunas/empates | S:28 JSON inteiro de esperado E:44 A7->1/B7->2/C19->3; E:45–47 `getOrdem().isEqualTo(1/2/3)` | Cada posição real anterior e nova; coberto |
| ACT-15 nomes preservados | E:53 `assertThat(preservado.getAlvoNome()).isEqualTo("Primeiro")`; E:54 `assertThat(preservado.getAutorNome()).isEqualTo("Dono")` e JSON literal igual; E:55 anterior A/novo Primeiro; E:56 count2/versão2 | Evento anterior não muda após renomeações; coberto |
| ACT-18 recusas | E:75 `assertThatThrownBy(...).hasMessage(mensagem)` com10 mensagens específicas E:65–73; E:77 `assertThat(conteudoComAtividade()).isEqualTo(antes)` | Permissão/versão/arquivo/final/outroquadro/ausência/nome/setor/posição/sessão sem efeitos; coberto |
| ACT-17 rollback | E:82 `assertThatThrownBy(...).hasMessage("Falha do diário")`; E:83 `assertThat(conteudoComAtividade()).isEqualTo(antes)` | Nome/setor/ordens/finais/versão/atividade revertidos; coberto |

| Reverse-map teste + asserção | AC / Done-when | Manter |
| --- | --- | --- |
| registraSomenteCamposAlteradosETodasAsPosicoes três casos E:29–33/S:28 | ACT-05/07/15/19 | Sim |
| edicaoIdenticaAceitaListaVaziaEIncremento E:38/S:28 | ACT-16/19 | Sim |
| normalizaLacunasEEmpatesMesmoSemAlterarNomeSetor E:43–47/S:28 | ACT-07/edge | Sim |
| renomeacaoPosteriorNaoMudaEventoAnteriorOuAutor E:53–56 | ACT-15 | Sim |
| recusasConservamTodaPersistencia dez casos E:75/77 | ACT-18 | Sim |
| falhaDoDiarioReverteNomeSetorSequenciaEVersao E:82/83 | ACT-17 | Sim |

Adequação A/B/C/D aprovada:17 cenários com valores da spec, payload completo, todas posições e snapshots completos nas falhas; sem asserções só de chamada. Matriz JUnit/H2 seguida, sem SPEC_DEVIATION. T6 Implemented; Verifier da feature pendente.

## T7 — remoção de etapa e fechamento do lote1

12/10/2026: `mvn.cmd -B package` exit0,921 testes (904 anteriores+17 novos),0 falhas/erros/skips,BUILD SUCCESS com jar. O snapshot da etapa removida e a sequência anterior são capturados depois do lock; gravação usa somente escalares após delete/confirmar. Sem alteração de outcomes antigos/novos, sem SPEC_DEVIATION.

R=`sistema/src/test/java/com/patp/sistema/AtividadeEtapaRemocaoTests.java`; S=`sistema/src/test/java/com/patp/sistema/AtividadeEtapaSupport.java`.

| Critério / AC | file:line + asserção | Resultado esperado / cobertura |
| --- | --- | --- |
| ACT-06/15 identificação após remoção | R:32 `assertThat(etapas.findById(removida.getId())).isEmpty()`; S:24 `assertThat(a.getAlvoId()).isEqualTo(alvo.getId())` e `assertThat(a.getAlvoNome()).isEqualTo(alvo.getNome())`; S:28 `assertThat(json.readTree(a.getAlteracoesJson())).isEqualTo(json.readTree(json.writeValueAsString(esperado)))` | ID/nome/setor/ordem antigos conservados mesmo sem etapa; coberto |
| Campos de remoção exatos | S:28 JSON inteiro; R:85–88 esperado nome antigo->null/setor Engenharia->null/ordem anterior->null | Cada propriedade alvoTipo/alvoId/alvoNome/campo/anterior/novo da lista verificada; coberto |
| ACT-07 todos afetados no início/meio/fim | S:28 esperado R:26–29; R:30 `assertThat(etapas.findById(e.getId()).orElseThrow().getOrdem()).isEqualTo(i+1<posicao?i+1:i)` | Todas as posições anteriores/novas e persistidas; coberto |
| ACT-19 finais e versão | R:33 `assertThat(r.gerenciamento().versao()).isEqualTo(1L)`; R:34 ordem final99; R:35 ordem final100 | Incremento atual e finais existentes preservadas; coberto |
| Última etapa/sem alvo persistido | R:40 `assertThat(etapas.findById(e.getId())).isEmpty()` e S:28 JSON inteiro de remoção; R:41 categorias exatas CONCLUIDA/CANCELADA | Sem trabalho restante, atividade intacta, finais automáticas sem falsas ações; coberto |
| Edge lacunas/empates | S:28 esperado R:45–46 removidaB7->null/A7->1/C20->2; R:47 ausênciaB; R:48 ordens1/2 | Valores reais antes e posições de todos afetados; coberto |
| ACT-18 demandas ativas/arquivadas | R:53 `assertThatThrownBy(...).hasMessage("Esta etapa possui demandas. Mova-as para outra etapa antes de removê-la.")`; R:54 `assertThat(conteudoComAtividade()).isEqualTo(antes)` | Dois estados recusados sem atividade; coberto |
| ACT-18 duas finais | R:59 `assertThatThrownBy(...).hasMessage("Etapas finais obrigatórias não podem ser alteradas.")`; R:60 snapshot igual | CONCLUIDA/CANCELADA protegidas; coberto |
| ACT-18 demais recusas | R:75 `assertThatThrownBy(...).hasMessage(mensagem)` com mensagens R:67–73; R:76 `assertThat(conteudoComAtividade()).isEqualTo(antes)` | Permissão/versão/arquivo/sessão/outroquadro/ausências sem efeitos; coberto |
| ACT-17 rollback | R:81 `assertThatThrownBy(...).hasMessage("Falha do diário")`; R:82 `assertThat(conteudoComAtividade()).isEqualTo(antes)` | Etapa removida, sequência, finais, versão, histórico e diário revertidos; coberto |
| Evento único/autoria | S:21 `assertThat(atividades.count()).isEqualTo(1)`; S:23 tipoETAPA_REMOVIDA/alvoETAPA; S:25 quadroID; S:26 autorID/nome; S:27 históricoNull/horárioMicros | Um registro automático do autor da sessão; coberto |

| Reverse-map teste + asserção | AC / Done-when | Manter |
| --- | --- | --- |
| removeInicioMeioFimPreservandoAlvoEPosicoesDeTodosAfetados três casos R:30–35/S:21–28 | ACT-06/07/15/19 | Sim |
| removeUnicaEtapaMantendoDiarioSemAlvoPersistido R:40/41/S:28 | ACT-06/edge finais automáticas | Sim |
| remocaoNormalizaLacunasEEmpatesSemPerderAntes R:47/48/S:28 | ACT-07/edge | Sim |
| demandaAtivaOuArquivadaImpedeRemocaoSemEvento dois casos R:53/54 | ACT-18 e bloqueio ocupada | Sim |
| finaisNaoPodemSerRemovidasENaoGeramEvento dois casos R:59/60 | ACT-18/proteção finais | Sim |
| recusasNaoAlteramPersistencia sete casos R:75/76 | ACT-18 | Sim |
| falhaDoDiarioReverteRemocaoSequenciaFinaisEVersao R:81/82 | ACT-17 | Sim |

Adequação A/B/C/D aprovada:17 cenários, payload completo e valores da spec, todas posições e snapshots integrais nas falhas, sem testes de chamadas ou requisitos inventados. Matriz JUnit/H2 seguida. Referências de mapeamento T1 corrigidas mecanicamente nesta revisão para suas linhas exatas, sem modificar código/outcomes.

### Gate de fim da fase1 em12/10/2026

Executados sequencialmente: Maven package/test921 PASS; `npm.cmd test`1000/1000 em25 arquivos PASS,sem skips; `npm.cmd run lint` exit0; `npm.cmd run build -- --outDir <TEMP novo>` exit0. Vite gerou index.html/CSS/JS em `C:/Users/Marco/AppData/Local/Temp/atividade-T7-build-a345c50709ab4478b9a15b15d1432a00`, sem alterar dist real. Aviso normal de outDir externo sem limpeza; artefatos gerados e saída0 conferidos. Logs TEMP/atividade-T7-{java,vitest,lint,build}.log. Nenhuma aplicação iniciada contra MySQL real, nenhum push/deploy/upgrade/limpeza de gerados.

Lote1 T1–T7 Implemented:81 novos casos Java além dos840 anteriores. T8–T18 permanecem pendentes; mysql/browser e Verifier final pertencem às próximas fases. Nenhum ACT Verified ou feature concluída neste fechamento.

Gates documentais antes do commit T7: validate_spec --strict exit0,zero erros/avisos; validate_tasks exit0,zero erros/aviso esperado Tests:none T1 conferido na matriz; check_commit OK e git diff --check exit0. Arquivos de código/testes/documentos do lote conferidos explicitamente; gerados fora dos commits.

## T8 — cadastro de demanda

12/10/2026: `mvn.cmd -B test` exit0,934 testes (921+13 novos),0 falhas/erros/skips. Gate repetido após fortalecer horário. Log TEMP/atividade-T8-java.log. Rotas moderna/antiga conservam versões1/0; helper bruto conserva null e campo arquivado termina ordem da demanda. Sem SPEC_DEVIATION.
C=`sistema/src/test/java/com/patp/sistema/AtividadeDemandaCadastroTests.java`; S=`sistema/src/test/java/com/patp/sistema/AtividadeDemandaSupport.java`.

| Critério / AC | file:line + asserção | Outcome da spec / cobertura |
| --- | --- | --- |
| ACT-08/15/23 evento/vínculo | S:23 `assertThat(atividades.count()).isEqualTo(1)`; S:25–28 tipo/alvo/quadro/autor; S:32–36 count1,ID vinculado,acaoCRIACAO,autor e demanda exatos | Um evento e um histórico da sessão,ID/número snapshot; coberto |
| Horário servidor/micros | S:29 `assertThat(a.getDataHora().getNano()%1000).isZero()`; S:30 `assertThat(a.getDataHora()).isBetween(inicio,java.time.LocalDateTime.now())` | Horário real da ação,micros; coberto |
| ACT-08 valores normalizados/10000 | S:31 `assertThat(json.readTree(a.getAlteracoesJson())).isEqualTo(json.readTree(json.writeValueAsString(esperado)))`; C:27–29 todos12 campos; C:31 observações literal10000 | JSON inteiro com cada propriedade das alterações e ordem; coberto |
| ACT-15 preservação futura | C:33 `assertThat(preservado.getAlvoNome()).isEqualTo("N-1")`,autorLeitor e JSON igual | Identificação/autor não reconstruídos; coberto |
| Null/opcionais | C:40/S:31 JSON exato somente seis campos; C:41 `assertThat(p.getResponsavel()).isNull()` e demais opcionais/encerramento | Ausências não inventadas; coberto |
| ACT-19 rotas/versões | C:26 status200/201; C:31 `assertThat(quadros.findById(q.getId()).orElseThrow().getVersao()).isEqualTo(legado?0L:1L)` | Versões atuais mantidas; coberto |
| ACT-18 recusas | C:53 status/erro exatos sete casos; C:54 `assertThat(conteudoComAtividade()).isEqualTo(antes)` | duplicidade/arquivo/versão/sessão/validação/semtrabalho/forjado sem escrita; coberto |
| ACT-17 rollback | C:60 `andExpect(status().isInternalServerError())` e `assertThat(conteudoComAtividade()).isEqualTo(antes)` | Todas tabelas/versão revertidas nas duas rotas; coberto |

| Reverse-map teste + asserção | AC / edge / Done-when | Manter |
| --- | --- | --- |
| cadastroCompletoNasDuasRotasTemValoresLiteraisAutorRealEVinculo dois casos C:26–33/S:23–36 | ACT-08/15/19/23,edge10000 | Sim |
| opcionaisNullOuVaziosNaoInventamMudancas dois casos C:39–41/S:31 | ACT-08,edge null/normalização | Sim |
| recusasConservamTodasAsTabelas sete casos C:53–54 | ACT-18 | Sim |
| falhaDoDiarioReverteCadastroHistoricoEVersao dois casos C:60 | ACT-17 | Sim |

A/B/C/D PASS: outcomes exatos,payload integral,sem testes rasos/fora da spec; matriz JUnit/H2. Outcomes antigos/novos não alterados. T8 Implemented,Verifier pendente.

## T9 — edição informativa de demanda

12/10/2026: `mvn.cmd -B test` exit0,965 testes (934+31 novos),0 falhas/erros/skips. Log TEMP/atividade-T9-java.log. Antes capturado após lock; somente campos informativos diferentes. Sem outcomes modificados/SPEC_DEVIATION.
E=`sistema/src/test/java/com/patp/sistema/AtividadeDemandaEdicaoTests.java`; S=`sistema/src/test/java/com/patp/sistema/AtividadeDemandaSupport.java`.

| Critério / AC | file:line + asserção | Outcome da spec / cobertura |
| --- | --- | --- |
| ACT-09 todos campos/datas/10000 literal | E:31 esperado um campo; S:31 `assertThat(json.readTree(a.getAlteracoesJson())).isEqualTo(json.readTree(json.writeValueAsString(esperado)))`; E:34 `assertThat(json.readTree(json.writeValueAsString(salvo)).get(campo).asText()).isEqualTo(novo)` | Cada campo normalizado com antes/depois exatos,10000 preservados; coberto |
| Null/limpeza | E:40/S:31 JSON completo seis alterações para null; E:41 `assertThat(salvo.getResponsavel()).isNull()` e cinco outros | Seis opcionais limpos sem inventar valores; coberto |
| ACT-16 idêntica | E:46/S:31 lista[] e `assertThat(jdbc.queryForList("select * from processos")).isEqualTo(antes)` | Edição aceita com evento vazio,versão1; coberto |
| ACT-09/19 número/ciclo | E:32 número/status/etapa exatos; E:53 status/etapa/datas/motivo literais | Encerramentos Concluido/Cancelado e número preservados; coberto |
| ACT-15/19/23 metadados/versão/vínculo | S:23–36 evento único,alvo/autor/quadro/hora/vínculo/açãoEDICAO; E:33 `getVersao().isEqualTo(1L)` | Autor real,micros/intervalo,vínculo sem duplicar e incremento atual; coberto |
| ACT-18 recusas | E:61 status/erro exatos e `assertThat(conteudoComAtividade()).isEqualTo(antes)` | número/ciclo/arquivo/versão/permissão/sessão/validação/status recusados integralmente; coberto |
| ACT-17 rollback | E:66 `andExpect(status().isInternalServerError())` e snapshot integral igual | Edição,histórico,atividade e versão revertidos nas duas rotas; coberto |

| Reverse-map teste + asserção | AC / edge / Done-when | Manter |
| --- | --- | --- |
| cadaCampoNasDuasRotasTemAntesDepoisReal14 casos E:31–34/S:23–36 | ACT-09/15/19/23,10000/normalização | Sim |
| limpezaDosSeisOpcionaisRegistraNull2 casos E:40–41 | ACT-09/null | Sim |
| identicaTemAlteracoesVaziasEIncrementoAtual2 casos E:46 | ACT-16/19 | Sim |
| encerramentoPermaneceIntegral2 casos E:52–53 | ACT-09/19 | Sim |
| recusasPreservamTodaPersistencia9 casos E:61 | ACT-18 | Sim |
| falhaReverteEdicaoHistoricoEVersao2 casos E:66 | ACT-17 | Sim |

A/B/C/D PASS: todos campos/payload inteiros,outcomes da spec,sem testes rasos/fora do alcance; matriz JUnit/H2 seguida. Implemented,Verifier pendente.

## T10 — transições de demanda

12/10/2026: gate Java completo exit0,1003 testes (965+38 novos),0 falhas/erros/skips,log TEMP/atividade-T10-java.log. Primeira execução: uma falha de fixture destinoAusente=-1 rejeitado pelo DTO antes da busca404. Ajuste mecânico para999999 positivo inexistente preservou404/mensagem/snapshot; gate integral repetido PASS. Sem outcomes alterados.
Precisão técnica confirmada pelo orquestrador: etapaNome identifica origem/destino quando etapaId muda,mesmo nome igual; não afirma renomeação. Helper específico de ciclo mantém diferenças informativas estritas. Design/edge spec atualizados sem mudar negócio/DTO.

T=`sistema/src/test/java/com/patp/sistema/AtividadeDemandaTransicaoTests.java`; C=`sistema/src/test/java/com/patp/sistema/AtividadeDemandaTransicaoConcurrencyTests.java`; S=`sistema/src/test/java/com/patp/sistema/AtividadeDemandaSupport.java`.

| Critério / AC | file:line + asserção | Outcome da spec / cobertura |
| --- | --- | --- |
| ACT-10 nomes iguais/distintos e IDs | T:32 esperado etapaId anterior/novo e etapaNome Análise/Análise ou Execução; S:31 `assertThat(json.readTree(a.getAlteracoesJson())).isEqualTo(json.readTree(json.writeValueAsString(esperado)))`; T:33 destino/status/camposNull/versão | Contexto completo nas duas rotas,IDs distintos mesmo nomes iguais; coberto |
| ACT-11 conclusão/cancelamento | T:41/S:31 JSON completo status,etapaId,etapaNome,dataConclusao,dataCancelamento,motivo; T:42 `assertThat(salvo.getMotivoCancelamento()).isEqualTo(concluir?null:"Motivo real")` e todos demais campos | Datas servidor/motivo normalizado/ciclo anterior limpo,somente reais; coberto |
| ACT-12 reabertura dos dois estados | T:48/S:31 seis diferenças exatas; T:49 statusEm andamento/destino/camposNull/versão; T:50 `assertThat(h.get("statusAnterior").asText()).isEqualTo(estado)` e IDs/datas/motivo literais | Campos atuais limpos,histórico anterior conservado; coberto |
| ACT-15/19/23 metadados e vínculo | S:23–36 evento único,tipo/alvo/quadro/autor/hora,micros/intervalo,histórico único/ID/ação/autor/processo; T:33/42/49 versão1 | Uma atividade e um histórico por ação,autoria real e versões atuais; coberto |
| ACT-18 todos ramos recusados | T:62 status/erro exatos e `assertThat(conteudoComAtividade()).isEqualTo(antes)` |18 casos incluindo mesmaetapa/finais/outro/ausência/estado/status/categoria/motivo/arquivo/versão/auth; coberto |
| ACT-17 rollback cada transição | T:67 `andExpect(status().isInternalServerError())` e snapshot integral igual | Quadro/demanda/histórico/atividade revertidos nas quatro ações; coberto |
| ACT-18 concorrência após lock | C:31 espera/bloqueio; C:32 resultado/mensagem exatos; C:33/S:31 evento exato na restauração; C:34 `assertThat(conteudoComAtividade()).isEqualTo(antes)` e atividade0/origem | Arquivo de quadro/demanda recusa; restauração demanda é lida após lock nas duas rotas; coberto |

| Reverse-map teste + asserção | AC / edge / Done-when | Manter |
| --- | --- | --- |
| movimentoPreservaIdsENomesDistintosOuIguaisNasDuasRotas4 casos T:31–33/S:31 | ACT-10/15/19/23,edge homônimos | Sim |
| encerramentosRegistramTodasAlteracoesELimpamCicloAnterior4 casos T:40–42/S:31 | ACT-11/19 | Sim |
| reaberturaPreservaHistoricoELimpaTodosCampos2 casos T:47–50/S:31 | ACT-12/19 | Sim |
| todosRamosRecusadosConservamDiarioECiclo18 casos T:62 | ACT-18 | Sim |
| falhaReverteCadaTransicaoHistoricoEVersao4 casos T:67 | ACT-17 | Sim |
| capturaDepoisDoLockNasDuasRotas6 casos C:31–34/S:31 | ACT-18,edge concorrência | Sim |

A/B/C/D PASS: payload completo e outcomes exatos,provas coordenadas sem alterar timers de testes antigos,matriz JUnit/H2. Sem SPEC_DEVIATION. T10 Implemented,Verifier pendente.

## T11 — arquivo e restauração da demanda

12/10/2026: gate Java completo exit0,1024 testes (1003+21 novos),0 falhas/erros/skips; log TEMP/atividade-T11-java.log. Primeiro gate teve uma falha de fixture: preparar quadro arquivado com saveAndFlush incrementou versão a1 antes do cenário definido em0. Preparo mecânico SQL do marcador mantém baseline0 para isolar lock; ação sob prova continua API/service real,mesmas assertions de aceitação/versão1. Gate integral repetido PASS,outcomes não mudaram.
A=`sistema/src/test/java/com/patp/sistema/AtividadeDemandaArquivoTests.java`; C=`sistema/src/test/java/com/patp/sistema/AtividadeDemandaArquivoConcurrencyTests.java`; S=`sistema/src/test/java/com/patp/sistema/AtividadeDemandaSupport.java`.

| Critério / AC | file:line + asserção | Outcome da spec / cobertura |
| --- | --- | --- |
| ACT-13 somente marcador/três estados | A:27 esperado anteriorfalse/true,novotrue/false; S:31 JSON inteiro; A:28 `assertThat(depois.remove("arquivado")).isEqualTo(arquivar)`,`assertThat(depois).isEqualTo(antes)` | Todos campos de identificação/ciclo/informação preservados,só marcador muda; coberto |
| Null bruto | A:32/S:31 esperado anteriornull/novotrue e `getMarcadorArquivado().isTrue()` | Null não normalizado no snapshot; coberto |
| ACT-15/19/23 metadados,vínculo,versão | S:23–36 todas propriedades/hora servidor,micros,vínculo único; A:28 `getVersao().isEqualTo(1L)` | Um evento e histórico automático por ação,autor real; coberto |
| Repetição409 | A:37 status409/mensagem exata e `assertThat(conteudoComAtividade()).isEqualTo(antes)`,count1 | Repetição conserva tudo; coberto |
| ACT-18 recusas | A:43 status/erro exatos e snapshot integral igual | Versão/versãoNull/permissão/sessão/quadroArquivo/outro/ausência/status recusados; coberto |
| ACT-17 rollback | A:48 `andExpect(status().isInternalServerError())`,snapshot integral igual | Marcador/histórico/atividade/versão revertidos nos dois sentidos; coberto |
| Concorrência quadro | C:22 bloqueio coordenado; C:23 `assertThat(segunda.get(10,TimeUnit.SECONDS)).isEqualTo(restaurarQuadro?"aceita":"Gerenciamento arquivado. Restaure-o antes de alterar.")` e marcadores/versão; C:24/S:31 evento exato ou atividade/histórico0 | Restauração permite após lock,arquivo recusa sem evento; coberto |

| Reverse-map teste + asserção | AC / edge / Done-when | Manter |
| --- | --- | --- |
| soMarcadorMudaNosTresEstados6 casos A:27–28/S:23–36 | ACT-13/15/19/23 | Sim |
| marcadorNullPermaneceLiteralNoAntes A:32 | ACT-13,edge null | Sim |
| repeticaoContinua409SemNovaAtividade2 casos A:37 | ACT-18,repetição | Sim |
| recusasNaoAlteramDados8 casos A:43 | ACT-18 | Sim |
| falhaReverteMarcadorHistoricoAtividadeEVersao2 casos A:48 | ACT-17 | Sim |
| arquivoDaDemandaEsperaMudancaDeArquivoDoQuadro2 casos C:22–24 | ACT-18,edge concorrência | Sim |

A/B/C/D PASS: payload integral,outcomes precisos,mudança somente do marcador,matriz JUnit/H2. Sem SPEC_DEVIATION. Implemented,Verifier pendente.

## T12 — envio de comentário

12/10/2026: gate Java completo exit0,1049 testes (1024+25 novos),0 falhas/erros/skips;log TEMP/atividade-T12-java.log. Um evento no caminho gravar,mesmo horário micros do comentário,sem texto/histórico/incremento de versão. Sem outcomes alterados/SPEC_DEVIATION.
T=`sistema/src/test/java/com/patp/sistema/AtividadeComentarioTests.java`; C=`sistema/src/test/java/com/patp/sistema/AtividadeComentarioConcurrencyTests.java`; S=`sistema/src/test/java/com/patp/sistema/AtividadeComentarioSupport.java`.

| Critério / AC | file:line + asserção | Outcome da spec / cobertura |
| --- | --- | --- |
| ACT-14 envio moderno/antigo e três estados | T:25 status200/201/autorLeitor e evento; S:21 `assertThat(atividades.count()).isEqualTo(1)`; S:22 tipo/alvo/quadro exatos; S:23 autorID/nome,históricoNull,JSON arraysize0/sem mensagem | Um evento sem texto,autor real,alterações[]; coberto |
| Horário exato e servidor | S:24 `assertThat(a.getDataHora()).isEqualTo(jdbc.queryForObject("select data_hora from comentarios",LocalDateTime.class))`,intervalo inicio/now,micros0 | Mesma data persistida,não hora inventada; coberto |
| ACT-15 identificação preservada | T:26 `assertThat(a.getAutorNome()).isEqualTo("Leitor")`,alvo/hora iguais após mudanças | Autor/demanda snapshot do envio; coberto |
| ACT-19 mensagem na conversa/ciclo preservado | S:25 countcomentario1,texto exato10000,funcionario/ID; S:26 `assertThat(jdbc.queryForObject("select count(*) from historicos",Long.class)).isZero()` e versão0; T:25 demanda inteira igual | Comentário conserva contrato/ciclo,sem histórico/versão; coberto |
| ACT-18 recusas | T:32 status/erro exatos e `assertThat(conteudoComAtividade()).isEqualTo(antes)` | Sessão/arquivos/ausência/outroquadro/null/vazio/longo/forjado sem evento; coberto |
| ACT-17 rollback duas rotas | T:36 status500 e snapshot integral igual | Comentário/atividade e todas tabelas revertidos; coberto |
| Concorrência moderna/antiga | C:24 bloqueio coordenado; C:25 `assertThat(segunda.get(10,TimeUnit.SECONDS)).isEqualTo(restaurada?"aceita":quadro?"Gerenciamento arquivado. Restaure-o antes de alterar.":"Demanda arquivada. Restaure-a antes de alterar.")`; eventoS:21–26 ou snapshot igual/contagens0 | Quadro/demanda arquivo/restauração lidos após lock; coberto |

| Reverse-map teste + asserção | AC / edge / Done-when | Manter |
| --- | --- | --- |
| envioNasDuasRotasRegistraSemMensagemHistoricoOuVersao6 casos T:25–26/S:21–26 | ACT-14/15/19 | Sim |
| recusasNaoGeramAtividade9 casos T:32 | ACT-18 | Sim |
| falhaReverteComentarioEDiarioNasDuasRotas2 casos T:36 | ACT-17 | Sim |
| envioEsperaLockELêArquivoMaisRecente8 casos C:24–25/S:21–26 | ACT-18,edge concorrência | Sim |

A/B/C/D PASS: payload completo,outcomes exatos,texto mantido só na conversa,matriz JUnit/H2. T12 Implemented,Verifier pendente. DTO da atividade sem texto será provado diretamente na T14.

## T13 — bloqueio da escrita manual externa

12/10/2026: gate Java completo exit0,1059 testes (1049+10 novos),0 falhas/erros/skips;log TEMP/atividade-T13-java.log. ACT-24/AD-034 e exatamente três adequações autorizados explicitamente pelo usuário em10/10/2026. Somente registrarExterno passa a409 após existência/arquivos; registrar interno intacto. Nenhum teste excluído/ignorado.
N=`sistema/src/test/java/com/patp/sistema/AtividadeHistoricoManualTests.java`; H=`sistema/src/test/java/com/patp/sistema/HistoricoArchiveTests.java`; A=`sistema/src/test/java/com/patp/sistema/DemandaArquivoHistoricoApiTests.java`; S=`sistema/src/test/java/com/patp/sistema/DemandaArquivoHistoricoTests.java`.

| Critério / AC | file:line + asserção | Outcome da spec / cobertura |
| --- | --- | --- |
| ACT-24 falsificação/null/false | N:26 `andExpect(status().isConflict())`,erro automático exato,`assertThat(conteudoComAtividade()).isEqualTo(antes)` | Quatro tentativas tipo/autor forjados recusadas409 sem escrita; coberto |
| ACT-18 sessão | N:30 `andExpect(status().isUnauthorized())` e snapshot igual | Ausente/inválida401; coberto |
| ACT-24 precedência arquivo | N:35 status409/erro quadro ou demanda exato,snapshot igual | Recusas antigas de arquivo preservadas; coberto |
| Ausência | N:37 `assertThatThrownBy(...).hasMessage("Processo não encontrado.")`,snapshot igual | Existência ainda verificada antes da nova recusa; coberto |
| ACT-22 leitura literal | N:40 status200,length1,ação/descrição/autorNull/dataMicros exatos,snapshot igual | Não normalizar valores antigos/autor; coberto |
| Adequação autorizada1 | H:57–66 POST409/snapshot integral/GET MANUAL,Observação,Informado,horário e demanda; H:67–80 conclusão/autoria/leitura/arquivo preservados | MANUAL via fixture,interno automático intacto; coberto |
| Adequação autorizada2 | A:29–39 POST409/erro/snapshot integral,GET literal,e campos/data antiga/processo/versão | POST não grava,linha anterior literal preservada; coberto |
| Adequação autorizada3 | S:21–29 exceção409/erro/snapshot integral,leitura literal/data fixture/processo/count1/versão | Null/false conservados com recusa,sem normalização; coberto |

| Reverse-map teste + asserção | AC / edge / Done-when | Manter |
| --- | --- | --- |
| tipoEAutorForjadosEmDemandaAtivaNaoGravam4 casos N:26 | ACT-18/24 | Sim |
| semSessaoOuInvalidaPermanece4012 casos N:30 | ACT-18 | Sim |
| precedenciaDeArquivoPermanece2 casos N:35 | ACT-24 | Sim |
| ausenciaPreservaErroAnteriorESemEscrita N:37 | ACT-18/existência | Sim |
| leituraAntigaContinuaLiteralSemNormalizacao N:40 | ACT-22 | Sim |
| Três nomes exatos T13 antigos H/A/S acima,nenhum outro outcome | ACT-24/AD-034 | Sim |

A/B/C/D PASS: outcomes autorizados precisos,integridade/consulta antigas mantidas,matriz JUnit/H2. Sem SPEC_DEVIATION. T13 Implemented,Verifier pendente.

## T14 — consulta por quadro e fechamento do lote2

12/10/2026: Java/H2 completo1087/0/0/0 PASS (1059+28 novos),Vitest1000/1000 PASS em25 arquivos,lint0 e build ViteTEMP0. MySQL fictício8.0.43:847/0/0/0 PASS,todos testes novos Atividade e regressão das rotas tocadas. Logs TEMP/atividade-T14-{java,vitest,lint,build,mysql}.log. Build em `C:/Users/Marco/AppData/Local/Temp/atividade-T14-build-4a2d3f4a6e1443dbb688ac1f192bfc33`,sem tocar dist.
Comando MySQL: Maven `-B "-Dtest=Atividade*Tests,Demanda*Tests,ProcessoArchiveTests,HistoricoArchiveTests,Comentario*Tests,EtapaDemandConcurrencyTests,GerenciamentoArchiveApiTests,EtapaArchiveTests" test`; ENV URLjdbc:mysql://127.0.0.1:33823/creral_arquivo_test?useSSL=false&allowPublicKeyRetrieval=true&connectionTimeZone=LOCAL,drivercom.mysql.cj.jdbc.Driver,userarquivo_test,ddl-autocreate-drop,server.port0. Credencial somente fictícia,não configuração real.

Identidade confirmada por listener/linha de comando e SQL SELECT@@port,@@datadir,DATABASE(),@@version:porta33823,datadirTEMP/creral-arquivo-049cfd5cc86d4d8b840e41f1710d7a43/mysql,schema creral_arquivo_test,versão8.0.43. Primeiro gate846 PASS e shutdownOWNEDPID19992; após adicionar descrição nula,H2 completo e mesma seleção MySQL847 repetidos PASS. Nova instância confirmadaOWNEDPID13172,encerrada via mysqladminrootfictíciosem senha shutdown0;listener33823 ausente. Sem reutilizar PID antigo ou operar banco real.
Compilação inicialmente apontou List.reversed indisponível emrelease17; ajuste mecânico por cópia/Collections.reverse preservou sequências/assertions. Fixture de cursor malformado passou a caractere não numérico Q? para manter ramo400,inicial Q seria substituído pelo quadro real e formaria cursor válido. Nenhum outcome antigo/novo alterado; novo caso de descrição nula foi acrescentado após adequação inicial.
Fixtures de schema legado tornam somente data_hora ou descricao nullable em testes dedicados,removem respectivas linhas nulas e restauram NOTNULL emfinally. Constraints de produção/model não mudam. Consulta usa UNION/NOTEXISTS parametrizados,51 para emitir50,sem backfill ou UPDATE.

Q=`sistema/src/test/java/com/patp/sistema/AtividadeConsultaTests.java`.

| Critério / AC | file:line + asserção | Outcome da spec / cobertura |
| --- | --- | --- |
| ACT-20/15 quadros/demandas arquivados/encerrados e três perfis | Q:44 `assertThat(resposta.get("registros")).isEqualTo(json.readTree(json.writeValueAsString(esperado)))`,esperado completo Q:41–43; Q:44 quadroID/cursorNull | Mesmo contrato200 nos seis casos,autor/número snapshots preservados após mudanças; coberto |
| ACT-22 fonte literal | Q:43 esperado chave/origem/ID/tipo/alvo/quadro/autorIDnull/nome/data/descrição/alterações[] exatos; Q:44 JSON inteiro | Legado literal e origem explícita,sem antes/depois inventado; coberto |
| ACT-23 evitar duplicidade/conservar histórico | Q:44 JSON exatamente3 registros,dois automáticos+um antigo; Q:46 countHist2; Q:47 GET antigo length2/ações exatas | Histórico ligado não aparece duas vezes,não foi apagado; coberto |
| ACT-29 DTO sem entidades/segredos/texto | Q:45 `containsExactlyInAnyOrder("chave","origem","id","gerenciamentoId","tipo","alvoTipo","alvoId","alvoNome","autorId","autorNome","dataHora","descricao","alteracoes")`,13 chaves; Q:46 `assertThat(resposta.toString()).doesNotContain("Mensagem privada secreta","senha","hash-ficticio-nao-publico","Bearer","processo","funcionario","historicoId")` | DTO plano e texto da conversa ausente; coberto |
| Leitura sem escrita | Q:46 `assertThat(conteudoComAtividade()).isEqualTo(antes)`; Q:51/54/55/58/61/65/66/71/75/80 snapshots integrais iguais | Dados existentes intactos em todos cenários aceitos/recusados; coberto |
| ACT-26 vazio | Q:51 `assertThat(r.get("registros").isArray()).isTrue()`,size0,cursorNull,quadroID |200/registros[]/cursorNull em ativo/arquivo; coberto |
| ACT-21 auth | Q:54 status401/erro ausente ou inválida exato/snapshot igual |401 sem escrita; coberto |
| ACT-39 quadro ausente | Q:55 status404/erroGerenciamento não encontrado./snapshot igual |404 sem escrita; coberto |
| ACT-28 cursor malformado | Q:58 status400/erroCursor de atividade inválido./snapshot igual;12 fixtures Q:56 | Vazio/caracteres/513/quadro0/negativo/unsafe/decimal/faltante/excedente/zero inicial recusados; coberto |
| ACT-28/29 outro quadro/isolamento | Q:60 registros0 apesar duas fontes do outro quadro; Q:61 status400/snapshot igual | Sem vazamento,cursor de outro quadro recusado; coberto |
| ACT-25/26/27 paginação126/empates/inserts | Q:65 `assertThat(chaves(primeira)).containsExactlyElementsOf(expected.subList(0,50))`,cursor<=512/Base64URL; Q:66 páginas50/26,mesmo cursor igual,`assertThat(todas).containsExactlyElementsOf(expected).doesNotHaveDuplicates()`,refresh começa novosAuto/Antigo | DataDESC,automática antes de existente,idDESC; conjunto fixo com novos inserts nas duas fontes; coberto |
| ACT-22/25/27 data/autor nulos e continuação | Q:71 páginas50/3 exatas,dataHora/autorId/autorNomeNull,tipo/descrição literal,alterações[],fimNull,snapshot igual | Nulos por último,sem inventar autor/data,sem repetir na página seguinte; coberto |
| ACT-15 remoção de etapa | Q:75 tipoETAPA_REMOVIDA/alvoId/nomeRemovida/tipoETAPA/anteriorRemovida/novonull,`assertThat(etapas.existsById(e.getId())).isFalse()`,snapshot igual | Alvo preservado sem entidade existir; coberto |
| ACT-22 descrição nula | Q:80 JSON registro completo igual a esperado,`assertThat(r.get("registros").get(0).get("descricao").isNull()).isTrue()`,cursorNull,snapshot igual | Sem descrição inventada,autor/tipo/data literais preservados; coberto |

| Reverse-map teste + asserção | AC / edge / Done-when | Manter |
| --- | --- | --- |
| duasFontesContratoCompletoSemDuplicarHistoricoOuExporMensagem6 casos Q:44–47 | ACT-15/20/22/23/29 | Sim |
| vazio200EmQuadroAtivoOuArquivado2 casos Q:51 | ACT-20/26 | Sim |
| ausenteOuInvalida401SemGravacao2 casos Q:54 | ACT-21 | Sim |
| quadroAusente404SemGravacao Q:55 | ACT-39 | Sim |
| cursorMalformado400SemEscrita12 casos Q:58 | ACT-28 | Sim |
| cursorDeOutroQuadro400EIsolamentoDeFontes Q:60–61 | ACT-28/29 | Sim |
| maisDe100FontesMistasEmpatadasPaginamSemRepetirMesmoComNovosInserts Q:65–66 | ACT-25/26/27 | Sim |
| dataHoraNulaLegadaVaiAoFimSemInventarAutorOuData Q:71 | ACT-22/25/27 | Sim |
| etapaRemovidaContinuaNaConsultaComSnapshot Q:75 | ACT-15/29 | Sim |
| descricaoNulaLegadaPermaneceNulaSemInventarTexto Q:80 | ACT-22/29,null do contrato | Sim |

A/B/C/D PASS:28 cenários,outcomes da spec,payload completo/valores de cada campo e páginas inteiras,matriz JUnit/H2/MySQL seguida. Sem SPEC_DEVIATION. Cursor canônico de quatro inteiros separados por dois-pontos documentado para lote3. Escrita/rollback das ações tocadas comprovados na mesma seleção MySQL,sem skips/retries/timeout alterados.
T14 e lote2 T8–T14 Implemented,166 casos novos sobre921 do lote1,total1087=840 antigos+247 novos da feature. Interface/browser e Verifier independente ainda pendentes emT15–T18; nenhum ACT Verified. T13 commit corrigido somente em metadata para `47d710b fix(atividade)!: reject manual activity forgery` com BREAKING CHANGE footer,em lugar de92c9fc1; índices/árvore não alterados pela correção.

Gates documentais finais antes do commit T14: validate_spec --strict exit0,zero erros/avisos; validate_tasks exit0,zero erros,aviso esperado T1 Tests:none confirmado pela matriz entity/compile. check_commit e diff --check fazem parte da preparação do commit. Arquivos explícitos limitados aos cinco novos Java e sete documentos de acompanhamento; target/node_modules/test-results permanecem fora do commit.

### Campos comuns das provas de escrita do lote2

S=`sistema/src/test/java/com/patp/sistema/AtividadeDemandaSupport.java`; C=`sistema/src/test/java/com/patp/sistema/AtividadeComentarioSupport.java`. Esta tabela explicita as propriedades de payload citadas nos mapas T8–T12; cada assertion é usada pelos cenários já relacionados nos respectivos reverse-maps.

| Campo / AC | file:line + assertion exata | Outcome da spec |
| --- | --- | --- |
| Tipo/alvoTipo ACT-08–13 | S:25 `assertThat(a.getTipo()).isEqualTo(tipo)` e `assertThat(a.getAlvoTipo()).isEqualTo("DEMANDA")` | Enum da ação e DEMANDA |
| AlvoID/número ACT-15 | S:26 `assertThat(a.getAlvoId()).isEqualTo(alvo.getId())` e `assertThat(a.getAlvoNome()).isEqualTo(alvo.getNumeroProcesso())` | Identificação no momento da ação |
| Quadro ACT-08–13/29 | S:27 `assertThat(a.getGerenciamento().getId()).isEqualTo(alvo.getEtapa().getGerenciamento().getId())` | Mesmo quadro autorizado |
| AutorID/nome ACT-15/18 | S:28 `assertThat(a.getAutorId()).isEqualTo(autor.getId())` e `assertThat(a.getAutorNome()).isEqualTo(autor.getNome())` | Conta real da sessão |
| Data ACT-08–13/15 | S:29 `assertThat(a.getDataHora().getNano()%1000).isZero()`; S:30 `assertThat(a.getDataHora()).isBetween(inicio,java.time.LocalDateTime.now())` | Hora do servidor com micros |
| Alterações ACT-08–13/16 | S:31 `assertThat(json.readTree(a.getAlteracoesJson())).isEqualTo(json.readTree(json.writeValueAsString(esperado)))` | Lista inteira com alvoTipo/alvoId/alvoNome/campo/anterior/novo e ordem exatos |
| HistóricoID ACT-23 | S:32 `assertThat(jdbc.queryForObject("select count(*) from historicos", Long.class)).isEqualTo(1L)`; S:33 `assertThat(jdbc.queryForObject("select id from historicos", Long.class)).isEqualTo(a.getHistoricoId())` | Um histórico ligado à própria ação |
| Histórico conteúdo ACT-19/23 | S:34 `assertThat(jdbc.queryForObject("select acao from historicos", String.class)).isEqualTo(acao)`; S:35 usuário igual autor; S:36 processo_id igual alvo.getId() | Histórico automático preservado com autor/demanda reais |
| Comentário metadados ACT-14/15 | C:22 `assertThat(a.getTipo()).isEqualTo("COMENTARIO_ADICIONADO")`,alvoTipoDEMANDA,alvoID/número/quadro exatos; C:23 autorID/nome iguais à sessão | Registro do envio identifica ação,demanda,autor e quadro |
| Comentário sem texto ACT-14/19 | C:23 `assertThat(a.getHistoricoId()).isNull()`,`assertThat(json.readTree(a.getAlteracoesJson()).size()).isZero()`,`assertThat(a.getAlteracoesJson()).doesNotContain(mensagem)` | Sem texto,histórico ou diferenças informativas |
| Comentário data ACT-14 | C:24 `assertThat(a.getDataHora()).isEqualTo(jdbc.queryForObject("select data_hora from comentarios",LocalDateTime.class))`,intervalo inicio/now e micros0 | Horário exatamente igual ao comentário persistido |
| Comentário preservação ACT-19 | C:25 count1/texto/funcionario/processo_id exatos; C:26 `assertThat(jdbc.queryForObject("select count(*) from historicos",Long.class)).isZero()` e versão0 | Mensagem somente na conversa,sem histórico/incremento |

## T19 — limites da primeira página no cliente

13/10/2026: Implemented, incluído no diagnóstico; Verifier pendente. Premissas: máximos do cursor pertencem a cada fonte e incluem os IDs já retornados, ACT-27/37; não alterar formato, dados ou regras do servidor. Arquivos: api.js,apiAtividade.test.js e documentos de rastreio. Sucesso:50 registros com máximo igual ao maior ID aceitos; máximo menor recusado nas duas fontes, com a página inteira rejeitada.

Quatro casos escritos antes da correção. O controle inicial executou135 casos do arquivo:133 PASS e os dois negativos FAIL porque a resposta inconsistente era aceita. Log TEMP/atividade-T19-red.log. A alteração mínima acrescenta a conferência de cada ID contra o máximo de sua fonte no próximo cursor; os testes anteriores permanecem intactos. Gate quick completo:1180/1180 Vitest em28 arquivos,zero falhas/skips,55.43s,log TEMP/atividade-T19-vitest.log. Os1176 casos anteriores foram preservados.

| AC / critério | file:line + assertion | Outcome / cobertura |
| --- | --- | --- |
| ACT-27/37 máximo inclusivo das duas fontes | frontend/src/services/apiAtividade.test.js:111 `expect(await consultarAtividade("s", 9)).toEqual(p)` | Página50 e todos os valores preservados com maior ID100 e máximo100; dois casos,coberto |
| ACT-27/37 cursor inconsistente | frontend/src/services/apiAtividade.test.js:116 `await expect(consultarAtividade("s", 9)).rejects.toMatchObject({ message: erro, status: 200 })` | Mesmo conjunto50/maiorID100, máximo99; recusa integral com ApiError,dois casos,coberto |

| Reverse-map teste + assertion | Âncora | Manter |
| --- | --- | --- |
| Primeira página aceita máximo igual,origens AUTOMATICA/HISTORICO_EXISTENTE;apiAtividade.test.js:111 toEqual(p) | ACT-27/37,limite inclusivo | Sim |
| Primeira página recusa máximo menor,mesmas duas origens;apiAtividade.test.js:116 rejects.toMatchObject | ACT-27/37,cursor incorreto | Sim |

A/B/C/D PASS: resultado exato e payload completo,casos positivos/negativos isolam o limite com cursor canônico e página50; litmus negativo detectou o erro antes da implementação. Padrões da matriz Vitest/API seguidos,sem guidelines adicionais ou SPEC_DEVIATION. T20 ainda pendente; nenhuma conclusão técnica ou ACT Verified antecipada.

## T20 — arquivo em linguagem da interface

13/10/2026: Implemented e incluído no diagnóstico; Verifier independente pendente. Premissa ACT-36: Sim/Não apresenta o marcador de arquivo,sem mudar DTOs ou outros textos. Arquivos: AtividadeLateral.jsx,AtividadeLateral.test.jsx,quatro asserções adicionais em atividade.spec.js e documentos. Sucesso: Não→Sim ao arquivar,Sim→Não ao restaurar; informações e históricos antigos conservam false/true literais.

Três casos novos escritos antes do renderer: controle28 casos,26 PASS e2 FAIL nos marcadores ainda exibidos em inglês,logTEMP/atividade-T20-red.log. Correção limitada ao campo arquivado; o teste de texto literal foi ampliado com duas descrições antigas isoladas,conservando a expectativa anterior false true. Nenhum outcome antigo mudou. API/servidor não alterados nesta tarefa.

Gates aprovados:1087 Java/H2/0falhas/0erros/0skips;1183 Vitest/28arquivos (1180 anteriores+3novos),zero falhas/skips,55.52s no último gate após ampliar a fixture literal;lint0;buildVite0 em TEMP/atividade-T20-build-75902ef942c543d197719b79c336d059;18/18 Edge/H2 em9arquivos,sem retries/skips. Logs TEMP/atividade-T20-{java,vitest,lint,build,browser}.log. O MySQL847T18 permanece evidência da implementação Java inalterada. Os serviços fictícios18082/4173 foram encerrados pelo runner;33823 permanece inativo.

Capturas reaisT20: TEMP/creral-atividade-T17-6o2dgB,creral-atividade-T18-fluxo-cuIkIQ,creral-atividade-T18-mista-5Nwtqi. Orquestrador inspecionou ativo-360.png: campo Arquivado mostra Não,controles e quebras íntegros. Capturas/asserções desktop e360 mantidas; nenhum dado real ou produção utilizado.

| Critério / AC | file:line + assertion | Outcome / cobertura |
| --- | --- | --- |
| ACT-36 marcador legível,arquivo/restauração | frontend/src/components/AtividadeLateral.test.jsx:96 `expect([...lista.querySelectorAll(".atividade-alteracao dd")].map(e => e.textContent)).toEqual([antes, depois])` | Dois casos definidos como Não/Sim e Sim/Não; coberto |
| ACT-36 texto informativo literal | frontend/src/components/AtividadeLateral.test.jsx:104 `expect([...lista.querySelectorAll(".atividade-alteracao dd")].map(e => e.textContent)).toEqual(["false", "true"])` | Campo observacoes não recebe tradução; coberto |
| ACT-22/36 histórico literal | frontend/src/components/AtividadeLateral.test.jsx:106 `expect([...lista.querySelectorAll(".atividade-texto")].map(e => e.textContent)).toEqual(["false true", "false", "true"])` | Três descrições antigas permanecem exatamente iguais; coberto |
| ACT-36 navegador e ACT-03/13 valores originais | frontend/e2e/atividade.spec.js:92 `expect(await painel.locator(...).locator(".atividade-alteracao").filter({ hasText: "Arquivado" }).locator("dd").allTextContents()).toEqual(valores)`; :84 `expect(atividade.registros.find(r => r.tipo === tipo).alteracoes).toEqual([{ alvoTipo, alvoId, alvoNome, campo: "arquivado", anterior, novo }])` | Quatro eventos reais exibem Sim/Não; mesmas respostas preservam strings false/true,alvo e ID; coberto |

| Reverse-map teste + assertion | AC / edge | Manter |
| --- | --- | --- |
| Dois casos marcador,AtividadeLateral.test.jsx:96 toEqual([antes,depois]) | ACT-36,apresentação compreensível | Sim |
| Caso informativo/legado,AtividadeLateral.test.jsx:104/106 listas exatas | ACT-22/36,preservar literais | Sim |
| Asserts extras no cenário completo,atividade.spec.js:92/84 valores/UI/DTO exatos | ACT-03/13/36,mesmo registro com apresentação local | Sim |

A/B/C/D PASS: cada novo caso deriva da precisão documentada de ACT-36,asserts de valores reais e todas expectativas antigas preservadas. Matriz Vitest+E2E seguida,sem SPEC_DEVIATION ou regras novas. T1–T20 Implemented; Verifier fresco obrigatório após commitT20. Nenhum ACT Verified nem conclusão técnica antecipada.

## Fechamento independente em13/10/2026

**Concluído e verificado — tudo ok, incluído no diagnóstico.** Verifier fresco `/root/verifier_atividade` rederivou39/39 critérios,edges eDonewhen e retornou PASS no range084757d..18d27a8. Relatório integral em[validation.md](validation.md). Todos os gates repetidos independentemente:1087 Java/H2,1183 Vitest,18 Edge/H2,847 MySQL fictício,lint/buildTEMP evalidate_state0. Baseline840 Java/1000 Vitest reexecutada emcópia,sem testes removidos/ignorados ou outcomes não autorizados.

Sensor8/8 killed por assertions comportamentais,controles247 Java/183 Vitest PASS. Cópia física própria removida depois de verificar caminho; HEAD/porcelain e4392 hashes reais idênticos antes/depois,sem writes emcódigo/testes reais. MySQL próprio atual18700 foi identificado porCIM/listener/SQL e conexões runtime daJVM,encerrado comshutdown0/porta33823ausente. Logs e traces da interrupção inicial do webServer4173 foram preservados; gate completo posterior PASS sem mudar testes/timeouts/retries. Nenhum gap de critério,mutante sobrevivente ou lição nova.

Spec eT1–T20 marcados Verified somente após o relatório PASS evalidate_state0. Documentação de fechamento não altera implementação/testes verificados. Aceitação humana permanece pendente,roteiro emOPERACAO.md. Próximo requisito original: gráficos,ainda requer definição de alcance/indicadores; não está implementado nem aprovado por este fechamento.
