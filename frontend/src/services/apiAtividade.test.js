import { expect, test, vi } from "vitest";
import { consultarAtividade } from "./api";

const erro = "Não foi possível confirmar a operação. Atualize a lista antes de tentar novamente.";
const cursor = texto => btoa(texto).replaceAll("+", "-").replaceAll("/", "_").replace(/=+$/, "");
const mudanca = () => ({ alvoTipo: "DEMANDA", alvoId: 7, alvoNome: "N-7", campo: "pessoa", anterior: " <b>Antes</b> ", novo: " Depois\n " });
const registro = (id = 21) => ({ chave: `AUTOMATICA:${id}`, origem: "AUTOMATICA", id, gerenciamentoId: 9, tipo: "DEMANDA_EDITADA", alvoTipo: "DEMANDA", alvoId: 7, alvoNome: "N-7", autorId: 2, autorNome: "Ana", dataHora: "2026-10-12T12:34:56.123456", descricao: null, alteracoes: [mudanca()] });
const pagina = () => ({ gerenciamentoId: 9, registros: [registro()], proximoCursor: null });
function resposta(dados, status = 200) { const f = vi.fn().mockResolvedValue(new Response(status === 204 ? null : JSON.stringify(dados), { status })); vi.stubGlobal("fetch", f); return f; }

test("ACT-29/37: valores integrais, sessão e abort pertencem à rota consultada", async () => {
    const p = pagina(), signal = new AbortController().signal, f = resposta(p);
    expect(await consultarAtividade("sessao", 9, null, { signal })).toEqual(p);
    expect(f.mock.calls[0]).toEqual(["http://localhost:8081/api/gerenciamentos/9/atividade", { method: "GET", headers: { Authorization: "Bearer sessao" }, signal }]);
});
test("ACT-22/26/37: vazio e legado preservam null, ação desconhecida e literais", async () => {
    const p = pagina(); p.registros = []; resposta(p); expect(await consultarAtividade("s", 9)).toEqual(p);
    p.registros = [{ ...registro(), chave: "HISTORICO_EXISTENTE:21", origem: "HISTORICO_EXISTENTE", tipo: " Ação antiga ", autorId: null, autorNome: null, dataHora: null, descricao: " <script>literal</script>\n ", alteracoes: [] }];
    resposta(p); expect(await consultarAtividade("s", 9)).toEqual(p);
    p.registros[0].descricao = null; resposta(p); expect(await consultarAtividade("s", 9)).toEqual(p);
});
test("ACT-26/27/28: página50 e continuação canônica conservam limites e avançam50", async () => {
    const p = pagina(); p.registros = Array.from({ length: 50 }, (_, i) => registro(100 - i)); p.proximoCursor = cursor("9:100:0:50"); resposta(p);
    expect(await consultarAtividade("s", 9)).toEqual(p);
    p.proximoCursor = cursor("9:100:0:100"); const entrada = cursor("9:100:0:50"), f = resposta(p);
    expect(await consultarAtividade("s", 9, entrada)).toEqual(p);
    expect(f.mock.calls[0][0]).toBe(`http://localhost:8081/api/gerenciamentos/9/atividade?cursor=${entrada}`);
});
test("ACT-37: IDs iguais em fontes diferentes são registros distintos", async () => {
    const p = pagina(); p.registros.push({ ...registro(), chave: "HISTORICO_EXISTENTE:21", origem: "HISTORICO_EXISTENTE", autorId: null, descricao: "Antigo", alteracoes: [] }); resposta(p);
    expect(await consultarAtividade("s", 9)).toEqual(p);
});
test("ACT-10: etapas homônimas mantêm contexto quando IDs mudam", async () => {
    const p = pagina(); p.registros[0].tipo = "DEMANDA_MOVIDA"; p.registros[0].alteracoes = [
        { ...mudanca(), campo: "etapaId", anterior: "3", novo: "4" }, { ...mudanca(), campo: "etapaNome", anterior: "Trabalho", novo: "Trabalho" }]; resposta(p);
    expect(await consultarAtividade("s", 9)).toEqual(p);
});
test("ACT-15/36: alteração literal10000 e edição vazia são conservadas", async () => {
    const p = pagina(); p.registros[0].alteracoes = [{ ...mudanca(), campo: "observacoes", anterior: null, novo: "á".repeat(10000) }]; resposta(p); expect(await consultarAtividade("s", 9)).toEqual(p);
    p.registros[0].alteracoes = []; resposta(p); expect(await consultarAtividade("s", 9)).toEqual(p);
});
test.each(["QUADRO_CRIADO", "QUADRO_EDITADO", "QUADRO_ARQUIVADO", "QUADRO_RESTAURADO", "ETAPA_CRIADA", "ETAPA_EDITADA", "ETAPA_REMOVIDA", "DEMANDA_CRIADA", "DEMANDA_EDITADA", "DEMANDA_MOVIDA", "DEMANDA_CONCLUIDA", "DEMANDA_CANCELADA", "DEMANDA_REABERTA", "DEMANDA_ARQUIVADA", "DEMANDA_RESTAURADA", "COMENTARIO_ADICIONADO"])("ACT-29/37: aceita tipo automático contratual %s", async tipo => {
    const p = pagina(), r = p.registros[0]; r.tipo = tipo; r.alvoTipo = tipo.startsWith("QUADRO_") ? "QUADRO" : tipo.startsWith("ETAPA_") ? "ETAPA" : "DEMANDA"; r.alvoId = r.alvoTipo === "QUADRO" ? 9 : 7; r.alteracoes = []; resposta(p);
    expect(await consultarAtividade("s", 9)).toEqual(p);
});
test.each(["QUADRO_CRIADO", "ETAPA_CRIADA", "ETAPA_EDITADA", "ETAPA_REMOVIDA"])("ACT-01/07/37: etapas iniciais e posições secundárias em %s", async tipo => {
    const p = pagina(), r = p.registros[0]; Object.assign(r, { tipo, alvoTipo: tipo === "QUADRO_CRIADO" ? "QUADRO" : "ETAPA", alvoId: tipo === "QUADRO_CRIADO" ? 9 : 7 });
    r.alteracoes = [{ alvoTipo: "ETAPA", alvoId: 8, alvoNome: "Outra etapa", campo: "ordem", anterior: "2", novo: "1" }]; resposta(p); expect(await consultarAtividade("s", 9)).toEqual(p);
});
const invalidos = [
    ...["gerenciamentoId", "registros", "proximoCursor"].map(c => [`página sem ${c}`, p => { delete p[c]; }]),
    ["página extra", p => { p.token = "segredo"; }], ["quadro externo", p => { p.gerenciamentoId = 8; }], ["lista inválida", p => { p.registros = {}; }],
    ["51 registros", p => { p.registros = Array.from({ length: 51 }, (_, i) => registro(i + 1)); }],
    ...Object.keys(registro()).map(c => [`registro sem ${c}`, p => { delete p.registros[0][c]; }]),
    ["registro extra", p => { p.registros[0].senha = "segredo"; }], ["registro externo", p => { p.registros[0].gerenciamentoId = 8; }],
    ...["id", "alvoId", "autorId"].flatMap(c => [0, -1, 1.5, "2", Number.MAX_SAFE_INTEGER + 1, null].map(v => [`${c} inválido ${v}`, p => { p.registros[0][c] = v; }])),
    ...["tipo", "alvoNome", "autorNome"].map(c => [`${c} nulo`, p => { p.registros[0][c] = null; }]),
    ["origem desconhecida", p => { p.registros[0].origem = "MANUAL"; }], ["enum desconhecido", p => { p.registros[0].tipo = "MANUAL"; }],
    ["alvo desconhecido", p => { p.registros[0].alvoTipo = "COMENTARIO"; }], ["tipo alvo incompatível", p => { p.registros[0].alvoTipo = "ETAPA"; }],
    ["quadro alvo externo", p => { Object.assign(p.registros[0], { tipo: "QUADRO_EDITADO", alvoTipo: "QUADRO", alvoId: 8, alteracoes: [] }); }],
    ["chave divergente", p => { p.registros[0].chave = "AUTOMATICA:22"; }], ["chave repetida", p => { p.registros.push(registro()); }],
    ["par origemID repetido com chave forjada", p => { p.registros.push({ ...registro(), chave: "outra" }); }],
    ...[null, "2026-02-29T12:00:00", "2026-10-12T24:00:00", "2026-10-12T12:60:00", "2026-10-12T12:00:60", "2026-10-12", "2026-10-12T12:00:00Z"].map(v => [`hora inválida ${v}`, p => { p.registros[0].dataHora = v; }]),
    ["descrição automática", p => { p.registros[0].descricao = "mensagem"; }], ["alterações objeto", p => { p.registros[0].alteracoes = {}; }],
    ...Object.keys(mudanca()).map(c => [`alteração sem ${c}`, p => { delete p.registros[0].alteracoes[0][c]; }]),
    ["alteração extra", p => { p.registros[0].alteracoes[0].texto = "segredo"; }], ["alteração alvo inválido", p => { p.registros[0].alteracoes[0].alvoTipo = "X"; }],
    ["alteração alvo externo", p => { p.registros[0].alteracoes[0].alvoId = 8; }], ["alteração nome divergente", p => { p.registros[0].alteracoes[0].alvoNome = "Outro"; }],
    ["campo desconhecido", p => { p.registros[0].alteracoes[0].campo = "senha"; }], ["campo de outro alvo", p => { p.registros[0].alteracoes[0].campo = "setor"; }],
    ["anterior objeto", p => { p.registros[0].alteracoes[0].anterior = {}; }], ["novo número", p => { p.registros[0].alteracoes[0].novo = 3; }],
    ["diferença igual", p => { p.registros[0].alteracoes[0].novo = p.registros[0].alteracoes[0].anterior; }],
    ["nome igual sem ciclo", p => { p.registros[0].alteracoes = [{ ...mudanca(), campo: "etapaNome", anterior: "A", novo: "A" }]; }],
    ["comentário com mudanças", p => { p.registros[0].tipo = "COMENTARIO_ADICIONADO"; }],
    ["cursor tipo", p => { p.proximoCursor = 5; }], ["cursor vazio", p => { p.proximoCursor = ""; }], ["cursor padding", p => { p.proximoCursor = cursor("9:21:0:50") + "="; }],
    ["cursor longo", p => { p.proximoCursor = "A".repeat(513); }],
    ...["8:21:0:50", "9:021:0:50", "9:-1:0:50", "9:9007199254740992:0:50", "9:21:0:0", "9:21:0:51", "9:21:0", "9:21:0:50:0"].map(v => [`cursor inválido ${v}`, p => { p.proximoCursor = cursor(v); }]),
    ["cursor com página curta", p => { p.proximoCursor = cursor("9:21:0:50"); }]
];
test.each(invalidos)("ACT-28/29/37: recusa página inteira %s", async (_nome, mudar) => {
    const p = pagina(); if (_nome.startsWith("cursor") && _nome !== "cursor com página curta") p.registros = Array.from({ length: 50 }, (_, i) => registro(100 - i));
    mudar(p); resposta(p); await expect(consultarAtividade("s", 9)).rejects.toMatchObject({ message: erro, status: 200 });
});
test.each(["autorId", "alteracoes", "descricao", "autorNome", "dataHora"])("ACT-22/37: legado recusa contrato inválido %s", async campo => {
    const p = pagina(); Object.assign(p.registros[0], { origem: "HISTORICO_EXISTENTE", chave: "HISTORICO_EXISTENTE:21", autorId: null, alteracoes: [] });
    p.registros[0][campo] = campo === "autorId" ? 2 : campo === "alteracoes" ? [mudanca()] : 7; resposta(p);
    await expect(consultarAtividade("s", 9)).rejects.toMatchObject({ message: erro, status: 200 });
});
test("ACT-27/37: continuação divergente não altera máximos nem repete offset", async () => {
    const p = pagina(); p.registros = Array.from({ length: 50 }, (_, i) => registro(100 - i));
    for (const v of ["9:101:0:100", "9:100:1:100", "9:100:0:50"]) { p.proximoCursor = cursor(v); resposta(p); await expect(consultarAtividade("s", 9, cursor("9:100:0:50"))).rejects.toMatchObject({ message: erro }); }
});
test("ACT-27/37: último resultado também respeita limites do cursor consultado", async () => {
    const p = pagina(); resposta(p); await expect(consultarAtividade("s", 9, cursor("9:20:0:50"))).rejects.toMatchObject({ message: erro });
    resposta(p); await expect(consultarAtividade("s", 9, cursor("8:100:0:50"))).rejects.toMatchObject({ message: erro });
});
test.each([201, 202, 204])("ACT-37: status%s não confirma leitura", async status => { resposta(pagina(), status); await expect(consultarAtividade("s", 9)).rejects.toMatchObject({ message: erro, status }); });
test.each([400, 401, 404, 500])("ACT-28/37: HTTP%s conserva erro do servidor", async status => { resposta({ erro: "Erro do servidor" }, status); await expect(consultarAtividade("s", 9)).rejects.toMatchObject({ message: "Erro do servidor", status }); });
test("ACT-34/37: rede, JSON e abort não retornam lista vazia", async () => {
    const f = vi.fn().mockRejectedValue(new TypeError("Offline")); vi.stubGlobal("fetch", f); await expect(consultarAtividade("s", 9)).rejects.toMatchObject({ message: erro, status: 0 });
    f.mockResolvedValue(new Response("{", { status: 200 })); await expect(consultarAtividade("s", 9)).rejects.toMatchObject({ message: erro, status: 200 });
    const abort = new DOMException("Cancelado", "AbortError"); f.mockRejectedValue(abort); await expect(consultarAtividade("s", 9)).rejects.toBe(abort);
});
