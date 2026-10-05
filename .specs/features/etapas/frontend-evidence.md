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
