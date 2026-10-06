import { expect, test, vi } from "vitest";
import { transicionarDemanda } from "./api";

const original = { id: 21, numeroProcesso: "Poste-1", pessoa: "João", responsavel: "Ana", status: "Em andamento",
    prioridade: "Alta", dataEmissao: "2020-01-02", prazoEtapa: "2020-01-03", prazoGeral: "2020-01-04",
    dataConclusao: null, dataCancelamento: null, motivoCancelamento: null, observacoes: "Linha\ncompleta", etapaId: 11 };
const etapas = [
    { id: 11, nome: "Comprar", ordem: 1, categoria: "TRABALHO", quantidadeDemandas: 0 },
    { id: 12, nome: "Instalar", ordem: 3, categoria: "TRABALHO", quantidadeDemandas: 0 },
    { id: 13, nome: "Concluídos", categoria: "CONCLUIDA", quantidadeDemandas: 0 },
    { id: 14, nome: "Cancelados", categoria: "CANCELADA", quantidadeDemandas: 0 }
];
const acoes = ["mover", "concluir", "cancelar", "reabrir"];
function caso(acao) {
    const antes = { ...original, ...(acao === "reabrir" ? { status: "Cancelado", etapaId: 14,
        dataCancelamento: "2020-02-29", motivoCancelamento: "Anterior" } : {}) };
    const dados = { versao: 2, ...(["mover", "reabrir"].includes(acao) ? { etapaId: 12 } : {}),
        ...(acao === "cancelar" ? { motivo: "  Solicitação\ncompleta  " } : {}) };
    const demanda = { ...antes, etapaId: acao === "concluir" ? 13 : acao === "cancelar" ? 14 : 12,
        status: acao === "concluir" ? "Concluido" : acao === "cancelar" ? "Cancelado" : "Em andamento",
        dataConclusao: acao === "concluir" ? "2026-10-06" : null,
        dataCancelamento: acao === "cancelar" ? "2026-10-06" : null,
        motivoCancelamento: acao === "cancelar" ? "Solicitação\ncompleta" : null };
    const resposta = { gerenciamento: { id: 9, versao: 3, arquivado: false, podeAdministrar: true },
        etapas: etapas.map(e => ({ ...e, quantidadeDemandas: e.id === demanda.etapaId ? 1 : 0 })), demandas: [demanda] };
    return { antes, dados, resposta };
}
function fetchResposta(dados, status = 200) {
    const fetchSpy = vi.fn().mockResolvedValue(new Response(status === 204 ? null : JSON.stringify(dados),
        { status, headers: { "Content-Type": "application/json" } }));
    vi.stubGlobal("fetch", fetchSpy); return fetchSpy;
}

test.each(acoes)("MOV-21/33: %s exige confirmação e envia somente payload próprio", async acao => {
    const c = caso(acao); const fetchSpy = fetchResposta(c.resposta);
    expect(await transicionarDemanda("sessao", 9, c.antes, acao, c.dados)).toEqual(c.resposta);
    expect(fetchSpy.mock.calls[0][0]).toBe(`http://localhost:8081/api/gerenciamentos/9/demandas/21/${acao}`);
    expect(fetchSpy.mock.calls[0][1].method).toBe("PUT");
    expect(fetchSpy.mock.calls[0][1].headers).toEqual({ Authorization: "Bearer sessao", "Content-Type": "application/json" });
    expect(JSON.parse(fetchSpy.mock.calls[0][1].body)).toEqual(c.dados);
    expect(fetchSpy).toHaveBeenCalledTimes(1);
});

const incorretos = [
    ["quadro", r => { r.gerenciamento.id = 99; }], ["versão antiga", r => { r.gerenciamento.versao = 2; }],
    ["versão extra", r => { r.gerenciamento.versao = 4; }], ["arquivo", r => { r.gerenciamento.arquivado = true; }],
    ["sem administração", r => { r.gerenciamento.podeAdministrar = false; }],
    ["sem demanda", r => { r.demandas = []; r.etapas.forEach(e => { e.quantidadeDemandas = 0; }); }],
    ["outra demanda", r => { r.demandas[0].id = 22; }], ["status", r => { r.demandas[0].status = "Pendente"; }],
    ["destino", r => { const d = r.demandas[0]; d.etapaId = 11; r.etapas.forEach(e => { e.quantidadeDemandas = e.id === 11 ? 1 : 0; }); }],
    ["counts", r => { r.etapas[0].quantidadeDemandas = 7; }],
    ["categoria", r => { r.etapas.forEach(e => { if (e.id === r.demandas[0].etapaId) e.categoria = "DESCONHECIDA"; }); }],
    ...["numeroProcesso", "pessoa", "responsavel", "prioridade", "dataEmissao", "prazoEtapa", "prazoGeral", "observacoes"]
        .map(campo => [campo, r => { r.demandas[0][campo] = campo.startsWith("data") || campo.startsWith("prazo") ? "2021-01-01" : "Mudado"; }])
];
test.each(acoes.flatMap(acao => incorretos.map(([nome, mudar]) => [acao, nome, mudar])))
    ("MOV-32: %s rejeita sucesso incoerente em %s sem repetir", async (acao, _nome, mudar) => {
        const c = caso(acao); mudar(c.resposta); const fetchSpy = fetchResposta(c.resposta);
        await expect(transicionarDemanda("sessao", 9, c.antes, acao, c.dados)).rejects.toMatchObject({ name: "ApiError", status: 200,
            message: "Não foi possível confirmar a operação. Atualize a lista antes de tentar novamente." });
        expect(fetchSpy).toHaveBeenCalledTimes(1);
    });

test.each([
    ["concluir", "dataConclusao", null], ["concluir", "dataCancelamento", "2020-01-01"], ["concluir", "motivoCancelamento", "Restante"],
    ["cancelar", "dataCancelamento", null], ["cancelar", "dataConclusao", "2020-01-01"], ["cancelar", "motivoCancelamento", "Outro"],
    ["reabrir", "dataConclusao", "2020-01-01"], ["reabrir", "dataCancelamento", "2020-01-01"], ["reabrir", "motivoCancelamento", "Anterior"],
    ["mover", "dataConclusao", "2020-01-01"], ["mover", "dataCancelamento", "2020-01-01"], ["mover", "motivoCancelamento", "Forjado"]
])("MOV-32/33: %s confirma o campo de encerramento %s", async (acao, campo, valor) => {
    const c = caso(acao); c.resposta.demandas[0][campo] = valor; fetchResposta(c.resposta);
    await expect(transicionarDemanda("sessao", 9, c.antes, acao, c.dados)).rejects.toMatchObject({ status: 200 });
});

test.each(acoes)("MOV-32: %s não aceita201/204 nem JSON inválido", async acao => {
    const c = caso(acao);
    for (const status of [201, 204]) {
        const fetchSpy = fetchResposta(c.resposta, status);
        await expect(transicionarDemanda("sessao", 9, c.antes, acao, c.dados)).rejects.toMatchObject({ status });
        expect(fetchSpy).toHaveBeenCalledTimes(1);
    }
    vi.stubGlobal("fetch", vi.fn().mockResolvedValue(new Response("<html>erro</html>", { status: 200 })));
    await expect(transicionarDemanda("sessao", 9, c.antes, acao, c.dados)).rejects.toMatchObject({ status: 200 });
});

test.each(acoes.flatMap(acao => [400, 401, 403, 404, 409, 500].map(status => [acao, status])))
    ("MOV-32: %s conserva erro HTTP%s e não repete", async (acao, status) => {
        const c = caso(acao); const fetchSpy = fetchResposta({ erro: "Mensagem do servidor" }, status);
        await expect(transicionarDemanda("sessao", 9, c.antes, acao, c.dados)).rejects.toMatchObject({ message: "Mensagem do servidor", status });
        expect(fetchSpy).toHaveBeenCalledTimes(1);
    });

test.each(acoes)("MOV-32: rede falha em %s sem retry", async acao => {
    const c = caso(acao); const fetchSpy = vi.fn().mockRejectedValue(new TypeError("offline")); vi.stubGlobal("fetch", fetchSpy);
    await expect(transicionarDemanda("sessao", 9, c.antes, acao, c.dados)).rejects.toMatchObject({ status: 0,
        message: "Não foi possível confirmar a operação. Atualize a lista antes de tentar novamente." });
    expect(fetchSpy).toHaveBeenCalledTimes(1);
});

test("MOV-05: movimento mantém dados de encerramento antigos em andamento", async () => {
    const c = caso("mover");
    Object.assign(c.antes, { dataConclusao: "2020-01-01", dataCancelamento: "2020-02-02", motivoCancelamento: "Legado" });
    Object.assign(c.resposta.demandas[0], { dataConclusao: c.antes.dataConclusao, dataCancelamento: c.antes.dataCancelamento, motivoCancelamento: c.antes.motivoCancelamento });
    fetchResposta(c.resposta); expect(await transicionarDemanda("sessao", 9, c.antes, "mover", c.dados)).toEqual(c.resposta);
});

test.each(["concluir", "cancelar"])("MOV-14/32: %s rejeita destino final com categoria duplicada", async acao => {
    const c = caso(acao); c.resposta.etapas[0].categoria = acao === "concluir" ? "CONCLUIDA" : "CANCELADA";
    const fetchSpy = fetchResposta(c.resposta);
    await expect(transicionarDemanda("sessao", 9, c.antes, acao, c.dados)).rejects.toMatchObject({ status: 200 });
    expect(fetchSpy).toHaveBeenCalledTimes(1);
});
