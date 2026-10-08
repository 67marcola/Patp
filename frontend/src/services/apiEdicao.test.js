import { expect, test, vi } from "vitest";
import { editarDemanda } from "./api";

const anterior = { id: 21, numeroProcesso: "P-1", pessoa: "João", responsavel: "Ana", prioridade: "Alta",
    dataEmissao: "2020-02-29", prazoEtapa: null, prazoGeral: null, observacoes: "Anterior", status: "Cancelado",
    etapaId: 13, dataConclusao: "2020-01-01", dataCancelamento: "2021-03-04", motivoCancelamento: "  Literal\r\nAnterior  " };
const enviados = { versao: 2, pessoa: "  Maria  ", responsavel: null, prioridade: "  ", dataEmissao: null,
    prazoEtapa: "2026-10-07", prazoGeral: "2020-02-29", observacoes: "  Outra\nlinha  " };
function resposta(status = "Cancelado") {
    return { gerenciamento: { id: 9, versao: 3, arquivado: false, podeAdministrar: true },
        etapas: [{ id: 13, quantidadeDemandas: 1, categoria: "CANCELADA" }],
        demandas: [{ ...anterior, status, pessoa: "Maria", responsavel: null, prioridade: null,
            dataEmissao: null, prazoEtapa: "2026-10-07", prazoGeral: "2020-02-29", observacoes: "Outra\nlinha" }] };
}
function preparar(corpo, status = 200) {
    const fetchMock = vi.fn().mockResolvedValue(new Response(status === 204 ? null : JSON.stringify(corpo), { status }));
    vi.stubGlobal("fetch", fetchMock); return fetchMock;
}

test.each(["Em andamento", "Concluido", "Cancelado"])("EDI-24/27/28: confirma dados e ciclo %s", async status => {
    const r = resposta(status); const fetchMock = preparar(r);
    expect(await editarDemanda("sessao", 9, { ...anterior, status }, enviados)).toEqual(r);
    const [url, req] = fetchMock.mock.calls[0];
    expect(url).toBe("http://localhost:8081/api/gerenciamentos/9/demandas/21"); expect(req.method).toBe("PUT");
    expect(req.headers).toEqual({ Authorization: "Bearer sessao", "Content-Type": "application/json" });
    expect(JSON.parse(req.body)).toEqual(enviados); expect(fetchMock).toHaveBeenCalledTimes(1);
});

const divergencias = [
    ["quadro", r => { r.gerenciamento.id = 99; }], ["versão", r => { r.gerenciamento.versao = 2; }],
    ["versão excessiva", r => { r.gerenciamento.versao = 4; }], ["arquivo", r => { r.gerenciamento.arquivado = true; }],
    ["permissão", r => { r.gerenciamento.podeAdministrar = false; }], ["sem demanda", r => { r.demandas = []; }],
    ["ID", r => { r.demandas[0].id = 99; }], ["número", r => { r.demandas[0].numeroProcesso = "Outro"; }],
    ["status", r => { r.demandas[0].status = "Em andamento"; }], ["etapa", r => { r.demandas[0].etapaId = 99; }],
    ["conclusão", r => { r.demandas[0].dataConclusao = null; }], ["cancelamento", r => { r.demandas[0].dataCancelamento = null; }],
    ["motivo", r => { r.demandas[0].motivoCancelamento = "Literal"; }], ["cliente", r => { r.demandas[0].pessoa = "Antiga"; }],
    ["responsável", r => { r.demandas[0].responsavel = "Ana"; }], ["prioridade", r => { r.demandas[0].prioridade = "Alta"; }],
    ["emissão", r => { r.demandas[0].dataEmissao = "2020-02-29"; }], ["prazo etapa", r => { r.demandas[0].prazoEtapa = null; }],
    ["prazo geral", r => { r.demandas[0].prazoGeral = null; }], ["observações", r => { r.demandas[0].observacoes = "Antiga"; }],
    ["contagem", r => { r.etapas[0].quantidadeDemandas = 2; }], ["campo omitido", r => { delete r.demandas[0].motivoCancelamento; }]
];
test.each(divergencias)("EDI-23/24: rejeita falso sucesso %s", async (_nome, alterar) => {
    const r = resposta(); alterar(r); const fetchMock = preparar(r);
    await expect(editarDemanda("sessao", 9, anterior, enviados)).rejects.toMatchObject({ name: "ApiError", status: 200,
        message: "Não foi possível confirmar a operação. Atualize a lista antes de tentar novamente." });
    expect(fetchMock).toHaveBeenCalledTimes(1);
});
test.each([201, 204])("EDI-24: somente HTTP200 confirma, recebido%s", async status => {
    preparar(resposta(), status); await expect(editarDemanda("sessao", 9, anterior, enviados)).rejects.toMatchObject({ name: "ApiError", status });
});
test.each([400, 401, 403, 404, 409, 500])("EDI-23: preserva erroHTTP%s sem retry", async status => {
    const f = preparar({ erro: "Mensagem do servidor" }, status);
    await expect(editarDemanda("sessao", 9, anterior, enviados)).rejects.toMatchObject({ status, message: "Mensagem do servidor" });
    expect(f).toHaveBeenCalledTimes(1);
});
test("EDI-23: falha de rede não repete a edição", async () => {
    const f = vi.fn().mockRejectedValue(new TypeError("Offline")); vi.stubGlobal("fetch", f);
    await expect(editarDemanda("sessao", 9, anterior, enviados)).rejects.toMatchObject({ status: 0,
        message: "Não foi possível confirmar a operação. Atualize a lista antes de tentar novamente." });
    expect(f).toHaveBeenCalledTimes(1);
});
