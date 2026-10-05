# Evidência da interface de etapas

## T2: cliente HTTP

PASS. Quatro wrappers usam o contrato final, incluindo DELETE200 com snapshot JSON. Nenhum retry de gravação foi acrescentado. Arquivos: api.js e api.test.js. Assumimos o DTO e as rotas confirmados em T1; nenhuma biblioteca nova.

Gate: `npm.cmd test -- --reporter=dot`, 81 testes PASS, zero falhas/skips (base71 +10). RED anterior: os dez casos novos falharam por ausência dos wrappers; os71 anteriores passaram. Assertions anteriores conservadas.

### Adequação direta (Check A/B)

| Critério / ETA | Evidência física + assertion | Outcome definido |
| --- | --- | --- |
| GETestrutura sessão/signal/DTO, ETA01/29 | frontend/src/services/api.test.js:119 `expect(await buscarConfiguracaoEtapas("sessao", 9, { signal: controller.signal })).toEqual(configuracao)`; :120 `expect(fetchSpy).toHaveBeenCalledWith("http://localhost:8081/api/gerenciamentos/9/estrutura-etapas", { method: "GET", headers: { Authorization: "Bearer sessao" }, signal: controller.signal })` | GETestrutura, Bearer sessao, mesmo signal, gerenciamento versão3 e etapa id4/nome/ setor/ordem1/quantidade2 |
| POST/PUT/DELETE, ETA12/13/15/28 | frontend/src/services/api.test.js:127 `expect(await executar()).toEqual(configuracao)`; :128 `expect(fetchSpy.mock.calls[0][0]).toBe(`http://localhost:8081/api/gerenciamentos${rota}`)`; :129 `expect(fetchSpy.mock.calls[0][1]).toEqual({ method, headers: { Authorization: "Bearer sessao", "Content-Type": "application/json" }, signal: undefined, body: JSON.stringify(dados) })` | POST201/PUT200/DELETE200 com JSONsnapshot; campos e versão2, token e método corretos; DELETEbody somente versão |
| Erro exato sem retry, ETA27/29 | frontend/src/services/api.test.js:139 `await expect(executar()).rejects.toMatchObject({ message: mensagem, status: 409 })`; :140 `expect(fetchSpy).toHaveBeenCalledTimes(1)` | mensagem de conflito exata/status409 para três gravações, nenhuma repetição |
| Rede ambígua, ETA27 | frontend/src/services/api.test.js:146 `await expect(executar()).rejects.toMatchObject({ message: "Não foi possível confirmar a operação. Atualize a lista antes de tentar novamente.", status: 0 })`; :149 `expect(fetchSpy).toHaveBeenCalledTimes(1)` | rejeição com mensagem/status0, sem sucesso/reenvio nas três gravações |

### Adequação reversa (Check C/D)

| Assertion / caso | Requisito | Manter |
| --- | --- | --- |
| api.test.js:119/120, GETestrutura | ETA01/29 | Sim |
| api.test.js:127–133, três métodos/DTOs/bodies | ETA12/13/15/28 | Sim |
| api.test.js:139/140, três conflitos | ETA27/29 | Sim |
| api.test.js:146/149, três falhas de rede | ETA27 | Sim |

Adequação PASS. As assertions conferem valores do DTO/payload e mensagens, além de chamadas. Padrões Vitest e skill seguidos, sem AGENTS adicional. Nenhum teste removido/ignorado/enfraquecido; nenhuma SPEC_DEVIATION. Formulário/quadro/teclado são verificados nas próximas tarefas.

## T3: formulário de etapa

PASS. EditorEtapa recebe etapa/quantidade e callbacks, sem acoplar o formulário às rotas. Assumimos as posições válidas do contrato T1 e os wrappers T2 disponíveis. Arquivos: EditorEtapa.jsx, EditorEtapa.test.jsx e CSS mínimo em index.css. Success: campos/limites/posição/rascunho/cancelar/pending e teclado comprovados em RTL; retorno de foco no quadro é T4.

Gate: `npm.cmd test -- --reporter=dot`,103 PASS, zero falhas/skips (81+22). RED: suíte nova recusada antes de existir o componente, demais81 PASS. Nenhum teste anterior alterado. Campo não usa maxLength que truncaria silenciosamente o rascunho; validação aplica trim e comprimento UTF16.

### Adequação direta (Check A/B)

| Critério / ETA | Evidência física + assertion | Outcome definido |
| --- | --- | --- |
| Nome/setor/posição atuais e seleção válida, ETA23 | frontend/src/pages/EditorEtapa.test.jsx:26 `expect(screen.getByLabelText("Nome da etapa").value).toBe(atual?.nome || "")`; :27 `expect(screen.getByLabelText("Setor responsável").value).toBe(atual?.setor || "")`; :28 `expect(screen.getByLabelText("Posição").value).toBe(String(atual?.ordem || 4))`; :29 `expect([...screen.getByLabelText("Posição").options].map(option => option.value)).toEqual(atual ? ["1", "2", "3"] : ["1", "2", "3", "4"])` | edição Planejamento/Técnico/2, criação vazia/4, setor nulo vazio; posições1..N(+1) |
| N=0, ETA23 | EditorEtapa.test.jsx:36 `expect([...screen.getByLabelText("Posição").options].map(option => option.value)).toEqual(["1"])` | apenas posição1 |
| Limites/whitespace/Unicode + rascunho, ETA09/10/27 | EditorEtapa.test.jsx:53 `expect(screen.getByRole("alert").textContent).toBe(mensagem)`; :54 `expect(screen.getByLabelText("Nome da etapa").value).toBe(nome)`; :55 `expect(screen.getByLabelText("Setor responsável").value).toBe(setor)`; :56 `expect(salvar).not.toHaveBeenCalled()` | oito casos: vazio, NBSP/BOM,256,128emoji=256UTF16 para cada campo; mensagem exata conforme spec |
| Aceitar1/255 e normalizar, ETA09/10/23 | EditorEtapa.test.jsx:63 `expect(salvar).toHaveBeenCalledExactlyOnceWith({ nome: texto, setor: texto, ordem: 2 })`; :64 `expect(screen.queryByRole("alert")).toBeNull()` |1 e255 acentos, trimNBSP/BOM e posição numérica2 |
| Posição inválida, ETA11 | EditorEtapa.test.jsx:71 `expect(screen.getByRole("alert").textContent).toBe("Escolha uma posição válida para a etapa.")`; :72 `expect(salvar).not.toHaveBeenCalled()` | seleção sem opção válida recusa gravação |
| Teclado campos/seleção/salvar, ETA30 | EditorEtapa.test.jsx:79 `expect(document.activeElement).toBe(screen.getByLabelText("Setor responsável"))`; :82 `expect(document.activeElement).toBe(screen.getByLabelText("Posição"))`; :85 `expect(salvar).toHaveBeenCalledExactlyOnceWith({ nome: "Análise", setor: "Engenharia", ordem: 2 })` | Tab alcança campos/select; Enter salva valores. Seleção Arrow/Home real é T5 |
| Cancelar teclado, ETA25/30 | EditorEtapa.test.jsx:91 `expect(cancelar).toHaveBeenCalledTimes(1)`; :92 `expect(salvar).not.toHaveBeenCalled()` | callback sem escrita; foco inicial no nome :31 |
| Pendente único/campos/cancelar, ETA26 | EditorEtapa.test.jsx:100 `expect(salvar).toHaveBeenCalledExactlyOnceWith({ nome: "Planejamento", setor: "Técnico", ordem: 2 })`; :101 `expect(screen.getByLabelText(label).disabled).toBe(true)` para três labels; :102 `expect(screen.getByRole("button", { name: "Salvando..." }).disabled).toBe(true)`; :104 `expect(cancelar).not.toHaveBeenCalled()`; :106 `expect(screen.getByRole("button", { name: "Salvar etapa" }).disabled).toBe(false)` | dois cliques geram único envio, bloquear campos/cancelar até confirmação |
| Erro API/rede anunciado/rascunho, ETA27 | EditorEtapa.test.jsx:120 `expect((await screen.findByRole("alert")).textContent).toBe(mensagem)`; :121 `expect(screen.getByLabelText("Nome da etapa").value).toBe("Rascunho")`; :122 `expect(screen.getByLabelText("Setor responsável").value).toBe("Engenharia")`; :123 `expect(screen.getByLabelText("Posição").value).toBe("2")`; :124 `expect(salvar).toHaveBeenCalledTimes(1)`; :125 `expect(aoErro).toHaveBeenCalledExactlyOnceWith(error)` |403/409/500/0 exatos, valores e posição preservados, sem reenvio; callback permiteGET no quadro |

### Adequação reversa (Check C/D)

| Assertion / família em EditorEtapa.test.jsx | Requisito | Manter |
| --- | --- | --- |
| :26–31 valores/seleção/foco, três casos | ETA23/30 | Sim |
| :36/37 vazio | ETA23 | Sim |
| :53–56 validação, oito casos | ETA09/10/27 | Sim |
| :63/64 limites aceitos, dois casos | ETA09/10/23 | Sim |
| :71/72 posição recusada | ETA11 | Sim |
| :79/82/85 campos via teclado | ETA23/30 | Sim |
| :91/92 cancelamento | ETA25/30 | Sim |
| :100–106 pendente | ETA26 | Sim |
| :120–125 erros, quatro casos | ETA27 | Sim |

As expressões exatas constam na tabela direta; cada família tem vínculo necessário. Adequação PASS, todos os critérios desta tarefa cobertos e outcomes exatos. Nenhum teste removido/ignorado/enfraquecido, nenhum SPEC_DEVIATION. Padrão RTL/Vitest/keyboard helper existente; sem guia adicional.

## T4: quadro e cache

PASS. Quadro recebe snapshot atual e usa os retornos das três mutações sem GET posterior. Gerenciamentos recebe callback de metadados para atualizar selecionado/lista e usar a versão correta ao arquivar. Assumimos T1–T3 concluídas. Arquivos: Quadro.jsx, callback mínimo em Gerenciamentos.jsx, QuadroEtapas.test.jsx, fixtures legítimas de Quadro.test.jsx/Gerenciamentos.test.jsx e CSS mínimo em index.css. Design/context documentam a posição visual do legado.

Gate: `npm.cmd test -- --reporter=dot`,130 PASS, zero falhas/skips (103+27). Lint adicional PASS. RED após testes e fixtures snapshot antes do código:26 falhas de outcome; demais98 PASS. Nenhum cenário anterior removido/ignorado; cinco casos de consulta do quadro mudaram somente fixture e rota para estrutura-etapas. Assertions de arquivo/teclado/retry/dados preservadas.

### Adequação direta (Check A/B)

Cada referência curta abaixo é `frontend/src/pages/QuadroEtapas.test.jsx`, exceto onde indicada outra origem.

| Critério / ETA | Evidência física + assertion | Outcome definido |
| --- | --- | --- |
| CRUD/posição e campos, ETA12/13/15/28 | :51 `expect(screen.getAllByRole("region").map(item => within(item).getByRole("heading").textContent)).toEqual(["Planejamento", "Análise", "Execução"])`; :53 `expect(screen.getByLabelText("Nome da etapa").value).toBe("Execução")`; :54 `expect(screen.getByLabelText("Setor responsável").value).toBe("Campo")`; :55 `expect(screen.getByLabelText("Posição").value).toBe("3")`; :59 mesma expressão de regiões `.toEqual(["Entrega", "Planejamento", "Análise"])`; :63 mesma expressão `.toEqual(["Entrega", "Planejamento"])`; :72 `expect(screen.getByText("Setor responsável: Operação").textContent).toBe("Setor responsável: Operação")` | inserir meio, editar campos/mover início, remover alvo; exibir setores retornados |
| Rotas/IDs/versões sem GETextra, ETA19/28 | :64 `expect(fetchMock.mock.calls.map(([url, request]) => [url, request.method, request.body && JSON.parse(request.body)])).toEqual([[`${endpoint}/estrutura-etapas`, "GET", undefined], [`${endpoint}/etapas`, "POST", { nome: "Análise", setor: "Engenharia", ordem: 2, versao: 0 }], [`${endpoint}/etapas/5`, "PUT", { nome: "Entrega", setor: "Operação", ordem: 1, versao: 1 }], [`${endpoint}/etapas/6`, "DELETE", { versao: 2 }]])`; :70 `expect(atualizar.mock.calls.map(([gerenciamento]) => gerenciamento.versao)).toEqual([0, 1, 2, 3])`; :71 `expect(atualizar).toHaveBeenLastCalledWith({ ...quadro, versao: 3 })` | quadro9, etapa editada5/deletada6, versões0/1/2 e retorno3; DTO inteiro preservado |
| Flags e metadados frescos, ETA03/22/28 | :84 `expect(screen.queryByRole("button", { name: "+ Nova etapa" }) !== null).toBe(permitido)`; :85 `expect(screen.queryByRole("button", { name: "Editar etapa 1: Planejamento" }) !== null).toBe(permitido)`; :86 `expect(screen.queryByRole("button", { name: "Remover etapa 1: Planejamento" }) !== null).toBe(permitido)`; :87 `expect(atualizar).toHaveBeenCalledExactlyOnceWith({ ...quadro, ...flags })`; :90 `expect(screen.getByRole("heading", { name: "Quadro atualizado" }).textContent).toBe("Quadro atualizado")`; :91 `expect(screen.getByText("Descrição atual").textContent).toBe("Descrição atual")`; :93 `expect(screen.getByText("Arquivado — somente consulta").textContent).toBe("Arquivado — somente consulta")` | criador/admin legado sim, terceiro/funcionário legado/arquivo não; props velhas superadas, banner readonly |
| Contagem e corpo real, ETA01/22 | :99 `expect(within(ocupada).getByText("2 demandas").textContent).toBe("2 demandas")`; :100 `expect(within(ocupada).queryByText("Nenhum processo nesta etapa.")).toBeNull()`; :101 `expect(within(ocupada).getByText("2 demandas vinculadas a esta etapa.").textContent).toBe("2 demandas vinculadas a esta etapa.")`; :103 `expect(within(vazia).getByText("0 demandas").textContent).toBe("0 demandas")`; :104 `expect(within(vazia).getByText("Nenhum processo nesta etapa.").textContent).toBe("Nenhum processo nesta etapa.")` | contagem2/0, nenhum falso vazio em ocupada |
| Cancelar/criar/editar/remover, confirmação, ETA24/25/30 | :113 `expect(within(dialogo).getByText("Demandas impedem a remoção. Mova-as para outra etapa antes de remover esta coluna.").textContent).toBe("Demandas impedem a remoção. Mova-as para outra etapa antes de remover esta coluna.")`; :115 `expect(document.activeElement).toBe(within(dialogo).getByRole("button", { name: "Cancelar" }))`; :118 `expect(screen.queryByRole("dialog")).toBeNull()`; :119 `expect(screen.queryByLabelText("Nome da etapa")).toBeNull()`; :120 `expect(document.activeElement).toBe(origem)`; :121 `expect(fetchMock.mock.calls.map(([, request]) => request.method)).toEqual(["GET"])` | dialog identifica Execução, avisa ocupação, focoCancelar; cancelar por Tab/Enter retorna foco sem gravação em três fluxos |
| Três operações pendentes, ETA26 | :131 `expect(screen.getByRole("button", { name: /Voltar/ }).disabled).toBe(true)`; :132 `expect(screen.getByRole("button", { name: "Cancelar" }).disabled).toBe(true)`; :133 `expect(fetchMock.mock.calls.filter(([, request]) => request.method === metodo)).toHaveLength(1)`; :135 `expect(voltar).not.toHaveBeenCalled()`; :137 `expect(screen.queryByLabelText("Nome da etapa")).toBeNull()`; :146 `expect(screen.getByRole("button", { name: "Removendo..." }).disabled).toBe(true)`; :147 `expect(screen.getByRole("button", { name: "Cancelar" }).disabled).toBe(true)`; :149 `expect(voltar).not.toHaveBeenCalled()`; :150 `expect(fetchMock.mock.calls.filter(([, request]) => request.method === "DELETE")).toHaveLength(1)`; :153 `expect(screen.queryByRole("heading", { name: "Execução", exact: true })).toBeNull()` | duplo clique não duplicaPOST/PUT/DELETE, saída/cancelar bloqueados; confirmação fecha após retorno, estado aplicado |
| Conflito/rede PUT, ETA18/27/29/30 | :170 `expect((await screen.findByRole("alert")).textContent).toBe(mensagem)`; :171 `expect(screen.getByLabelText("Nome da etapa").value).toBe("Rascunho")`; :172 `expect(screen.getByLabelText("Setor responsável").value).toBe("Engenharia")`; :173 `expect(screen.getByLabelText("Posição").value).toBe("2")`; :174 `expect(screen.getByRole("button", { name: "Salvar etapa" }).disabled).toBe(true)`; :178 `expect(fetchMock.mock.calls.slice(chamadasAntes).map(([url, request]) => [url, request.method])).toEqual([[`${endpoint}/estrutura-etapas`, "GET"]])`; :184 `expect(fetchMock.mock.calls.filter(([, request]) => request.method === "PUT").map(([, request]) => JSON.parse(request.body))).toEqual([{ nome: "Rascunho", setor: "Engenharia", ordem: 2, versao: 0 }, { nome: "Rascunho", setor: "Engenharia", ordem: 2, versao: 7 }])`; :186 `expect(screen.getByRole("heading", { name: "Rascunho", exact: true }).textContent).toBe("Rascunho")` |409/0 mensagens exatas, campos mantidos, GET via Tab/Enter; nenhum retry de gravação; envio manual seguinte usa versão7 |
| Remoção recusada/ambígua, ETA16/27/29 | :201 `expect((await screen.findByRole("alert")).textContent).toBe(mensagem)`; :202 `expect(screen.getByRole("region", { name: "Planejamento" })).not.toBeNull()`; :203 `expect(screen.getByRole("dialog", { name: "Remover Planejamento?" })).not.toBeNull()`; :206 `expect(fetchMock.mock.calls.map(([, request]) => request.method)).toEqual(["GET", "DELETE", "GET"])`; :208 `expect(screen.queryByRole("dialog")).toBeNull()`; :209 `expect(fetchMock.mock.calls.filter(([, request]) => request.method === "DELETE")).toHaveLength(1)` | ocupado409,403,404,500,rede0 exatos; conservar coluna/confirmação; atualizar somenteGET/cancelar |
| Falha deGET posterior, ETA27/29/30 | :225 `expect(screen.getByText("Consulta indisponível").getAttribute("role")).toBe("alert")`; :226 `expect(screen.getByLabelText("Nome da etapa").value).toBe("Rascunho")`; :227 `expect(screen.getByLabelText("Setor responsável").value).toBe("Engenharia")`; :228 `expect(screen.getByLabelText("Posição").value).toBe("2")`; :229 `expect(screen.getByRole("button", { name: "Salvar etapa" }).disabled).toBe(true)`; :230 `expect(screen.getByRole("region", { name: "Execução" })).not.toBeNull()`; :231 `expect(screen.queryByRole("heading", { name: "Nenhuma etapa cadastrada" })).toBeNull()`; :234 `expect(screen.getByRole("button", { name: "Salvar etapa" }).disabled).toBe(false)`; :235 `expect(fetchMock.mock.calls.map(([, request]) => request.method)).toEqual(["GET", "PUT", "GET", "GET"])` | erro separado de vazio, GETretry via teclado conserva draft/colunas e não repetePUT |
| Repetidos independentes, ETA14/22/28 | :243 `expect(colunas).toHaveLength(2)`; :244 `expect(within(colunas[0]).getByText("Setor responsável: Técnico").textContent).toBe("Setor responsável: Técnico")`; :245 `expect(within(colunas[0]).getByText("Posição 1").textContent).toBe("Posição 1")`; :246 `expect(within(colunas[1]).getByText("Setor responsável: Campo").textContent).toBe("Setor responsável: Campo")`; :247 `expect(within(colunas[1]).getByText("Posição 2").textContent).toBe("Posição 2")`; :253 `expect(screen.getAllByRole("region").map(item => within(item).getByRole("heading").textContent)).toEqual(["Planejamento", "Revisão"])`; :254 `expect(fetchMock.mock.calls.find(([, request]) => request.method === "PUT")[0]).toBe(`${endpoint}/etapas/5`)` | mesmo nome não mistura campos/IDs; edição só altera etapa5 |
| Revogação/arquivo apósGET, ETA22/27/29 | :268 `expect(screen.queryByRole("button", { name: "+ Nova etapa" })).toBeNull()`; :269 `expect(screen.queryByRole("button", { name: /Editar etapa \d/ })).toBeNull()`; :270 `expect(screen.queryByRole("button", { name: /Remover etapa \d/ })).toBeNull()`; :271 `expect(screen.getByLabelText("Nome da etapa").value).toBe("Rascunho")`; :272 `expect(screen.getByLabelText("Nome da etapa").disabled).toBe(true)`; :273 `expect(screen.getByRole("button", { name: "Salvar etapa" }).disabled).toBe(true)`; :274 `expect(fetchMock.mock.calls.map(([, request]) => request.method)).toEqual(["GET", "POST", "GET"])` | flags atuais suprimem configuração, draft mantido somente leitura, sem nova escrita |
| Legado e posição visual, ETA02/23 | :281 `expect(screen.getByLabelText("Posição").value).toBe("2")`; :282 `expect([...screen.getByLabelText("Posição").options].map(option => option.value)).toEqual(["1", "2"])`; :283 `expect(screen.getByLabelText("Nome da etapa").value).toBe("Execução")`; :286 `expect(screen.getByLabelText("Posição").value).toBe("1")`; :287 `expect(screen.getByLabelText("Setor responsável").value).toBe("")`; :288 `expect(fetchMock.mock.calls.map(([, request]) => request.method)).toEqual(["GET"])` | ordens7/7 viram escolha visual1/2 só no formulário; consulta não escreve nem inventa setor |
| Última vazia, ETA15/29 | :296 `expect((await screen.findByRole("heading", { name: "Nenhuma etapa cadastrada" })).textContent).toBe("Nenhuma etapa cadastrada")`; :297 `expect(fetchMock.mock.calls.map(([, request]) => request.method)).toEqual(["GET", "DELETE"])` | vazio após retorno confirmado, zeroGETextra |
| Cache e arquivo seguinte, ETA28 | :328 `expect((await screen.findByRole("heading", { name: "Nenhum gerenciamento ativo" })).textContent).toBe("Nenhum gerenciamento ativo")`; :330 `expect(JSON.parse(arquivo[1].body)).toEqual({ versao: 1 })`; :331 `expect(fetchMock.mock.calls.filter(([url]) => url.endsWith("/estrutura-etapas"))).toHaveLength(1)`; :332 `expect(fetchMock.mock.calls.filter(([url]) => url.includes("?arquivado="))).toHaveLength(2)` | arquivar usa versão1 retornadaDELETE; lista não recarrega antes disso, arquivamento confirmado |
| Consulta inicial/loading/retry anterior preservados, ETA29/30 | frontend/src/pages/Quadro.test.jsx:23 `expect(screen.getByRole("status").textContent).toBe("Carregando quadro...")`; :53 `expect((await screen.findByRole("alert")).textContent).toBe(mensagem)`; :58 `expect(screen.queryByRole("alert")).toBeNull()`; :60 `expect(fetchMock.mock.calls.every(([, request]) => request.method === "GET")).toBe(true)`; :72 `expect(fetchMock.mock.calls.map(([url, request]) => [url, request.method])).toEqual([["http://localhost:8081/api/gerenciamentos/9/estrutura-etapas", "GET"], ["http://localhost:8081/api/gerenciamentos/9/estrutura-etapas", "GET"]])` | carregar não é vazio;500/404/0 alertas; retryclick/TabEnter somenteGET; readonly preservado |

### Adequação reversa (Check C/D)

| Família/assertions exatas reproduzidas acima | Requisito | Manter |
| --- | --- | --- |
| QuadroEtapas.test.jsx:51/53/54/55/59/63/64/70/71/72 CRUD | ETA12/13/15/19/28/30 | Sim |
| :84–93, cinco flags atuais | ETA03/22/28 | Sim |
| :99–104, contagens | ETA01/22 | Sim |
| :113–121, três cancelamentos | ETA24/25/30 | Sim |
| :131–153, três pendentes | ETA26 | Sim |
| :170–186, conflito/redePUT | ETA18/27/29/30 | Sim |
| :201–209, cinco errosDELETE | ETA16/27/29 | Sim |
| :225–235, GETfalho posterior | ETA27/29/30 | Sim |
| :243–254, nomes repetidos | ETA14/22/28 | Sim |
| :268–274, flags alteradas emGET | ETA22/27/29 | Sim |
| :281–288, legado | ETA02/23 | Sim |
| :296/297, última coluna | ETA15/29 | Sim |
| :328/330/331/332, cachearquivo | ETA28 | Sim |
| Quadro.test.jsx:23/53/58/60/72, cenários anteriores | ETA29/30 e regressão GER29/38/40 | Sim |

Adequação PASS. As chamadas são complementadas por estado/campos/foco/contagens/mensagens exatas; nenhum teste prova só invocação. Teclado opera Nova/Editar/Remover/Cancelar/removerconfirm/retry além dos campos do T3. Persistência/reload e seleção nativa via Home/Arrow/Enter são T5. Nenhum SPEC_DEVIATION, nenhuma biblioteca/capacidade posterior acrescentada; guiaRTL/skill existente seguido.
