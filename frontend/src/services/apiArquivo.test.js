import { expect, test, vi } from "vitest";
import { arquivarDemanda, restaurarDemanda, buscarConfiguracaoEtapas, editarDemanda } from "./api";

const anterior = { id: 21, numeroProcesso: "P-1", pessoa: "João", responsavel: " Ana ", prioridade: "Alta",
    dataEmissao: "2020-02-29", prazoEtapa: null, prazoGeral: "2026-10-09", observacoes: " Literal\r\nTexto ",
    status: "Cancelado", etapaId: 13, dataConclusao: "2020-01-01", dataCancelamento: "2021-03-04", motivoCancelamento: "  Motivo  " };
const mensagem = "Não foi possível confirmar a operação. Atualize a lista antes de tentar novamente.";
const acoes = [["arquivar", arquivarDemanda], ["restaurar", restaurarDemanda]];
function snapshot(acao = "arquivar") {
    return { gerenciamento: { id: 9, versao: 3, arquivado: false, podeAdministrar: true },
        etapas: [{ id: 13, quantidadeDemandas: acao === "arquivar" ? 0 : 1, categoria: "CANCELADA" }],
        demandas: acao === "arquivar" ? [] : [{ ...anterior }],
        demandasArquivadas: acao === "arquivar" ? [{ ...anterior }] : [] };
}
function preparar(corpo, status = 200) {
    const f = vi.fn().mockResolvedValue(new Response(status === 204 ? null : JSON.stringify(corpo), { status }));
    vi.stubGlobal("fetch", f); return f;
}
for (const [acao, executar] of acoes) {
    test.each(["Em andamento", "Concluido", "Cancelado"])(`ARQ-05/06/28: ${acao} preserva todos os campos em %s`, async status => {
        const r = snapshot(acao); const lista = acao === "arquivar" ? r.demandasArquivadas : r.demandas;
        lista[0].status = status; const f = preparar(r);
        expect(await executar("sessao", 9, { ...anterior, status }, 2)).toEqual(r);
        const [url, req] = f.mock.calls[0];
        expect(url).toBe(`http://localhost:8081/api/gerenciamentos/9/demandas/21/${acao}`);
        expect(req.method).toBe("PUT"); expect(req.headers).toEqual({ Authorization: "Bearer sessao", "Content-Type": "application/json" });
        expect(JSON.parse(req.body)).toEqual({ versao: 2 }); expect(f).toHaveBeenCalledTimes(1);
    });
    const divergencias = [
        ...Object.keys(anterior).map(campo => [`campo ${campo}`, r => { const d = (acao === "arquivar" ? r.demandasArquivadas : r.demandas)[0];
            d[campo] = campo === "id" ? 22 : campo === "etapaId" ? 99 : campo.startsWith("data") || campo.startsWith("prazo") ? "2022-02-02" : "Diferente"; }]),
        ["arquivo ausente", r => { delete r.demandasArquivadas; }],
        ["arquivo null", r => { r.demandasArquivadas = null; }], ["arquivo objeto", r => { r.demandasArquivadas = {}; }],
        ["lista ativa inválida", r => { r.demandas = {}; }],
        ["quadro", r => { r.gerenciamento.id = 99; }], ["quadro arquivado", r => { r.gerenciamento.arquivado = true; }],
        ["permissão", r => { r.gerenciamento.podeAdministrar = false; }], ["tipo permissão", r => { r.gerenciamento.podeAdministrar = "true"; }],
        ["versão anterior", r => { r.gerenciamento.versao = 2; }], ["versão excessiva", r => { r.gerenciamento.versao = 4; }],
        ["tipo versão", r => { r.gerenciamento.versao = "3"; }], ["versão fracionada", r => { r.gerenciamento.versao = 3.5; }],
        ["contagem", r => { r.etapas[0].quantidadeDemandas++; }], ["tipo contagem", r => { r.etapas[0].quantidadeDemandas = "0"; }],
        ["etapa ausente", r => { r.etapas = []; }], ["etapa duplicada", r => { r.etapas.push({ ...r.etapas[0] }); }],
        ["destino ausente", r => { r.demandas = []; r.demandasArquivadas = []; r.etapas[0].quantidadeDemandas = 0; }],
        ["não transferida", r => { r.demandas = acao === "arquivar" ? [{ ...anterior }] : []; r.demandasArquivadas = acao === "arquivar" ? [] : [{ ...anterior }]; r.etapas[0].quantidadeDemandas = r.demandas.length; }],
        ["ID nas duas listas", r => { (acao === "arquivar" ? r.demandas : r.demandasArquivadas).push({ ...anterior }); r.etapas[0].quantidadeDemandas = r.demandas.length; }],
        ["ID repetido destino", r => { const l = acao === "arquivar" ? r.demandasArquivadas : r.demandas; l.push({ ...anterior }); r.etapas[0].quantidadeDemandas = r.demandas.length; }],
        ["campo omitido", r => { delete (acao === "arquivar" ? r.demandasArquivadas : r.demandas)[0].pessoa; }],
        ["campo extra", r => { (acao === "arquivar" ? r.demandasArquivadas : r.demandas)[0].arquivado = true; }],
        ["tipo número", r => { (acao === "arquivar" ? r.demandasArquivadas : r.demandas)[0].numeroProcesso = 1; }],
        ["data inválida", r => { (acao === "arquivar" ? r.demandasArquivadas : r.demandas)[0].dataEmissao = "2026-02-30"; }]
    ];
    test.each(divergencias)(`ARQ-19/20/28: ${acao} rejeita %s`, async (_nome, alterar) => {
        const r = snapshot(acao); alterar(r); const f = preparar(r);
        await expect(executar("sessao", 9, anterior, 2)).rejects.toMatchObject({ name: "ApiError", status: 200, message: mensagem });
        expect(f).toHaveBeenCalledTimes(1);
    });
    test.each([201, 204])(`ARQ-28: ${acao} não confirma HTTP%s`, async status => {
        preparar(snapshot(acao), status); await expect(executar("sessao", 9, anterior, 2)).rejects.toMatchObject({ status, message: mensagem });
    });
    test.each([400, 401, 403, 404, 409, 500])(`ARQ-29: ${acao} mantém erro HTTP%s sem retry`, async status => {
        const f = preparar({ erro: "Mensagem do servidor" }, status);
        await expect(executar("sessao", 9, anterior, 2)).rejects.toMatchObject({ status, message: "Mensagem do servidor" }); expect(f).toHaveBeenCalledTimes(1);
    });
    test(`ARQ-29: ${acao} mantém falha de rede sem retry`, async () => {
        const f = vi.fn().mockRejectedValue(new TypeError("Offline")); vi.stubGlobal("fetch", f);
        await expect(executar("sessao", 9, anterior, 2)).rejects.toMatchObject({ status: 0, message: mensagem }); expect(f).toHaveBeenCalledTimes(1);
    });
}
test("ARQ-19: contrato anterior sem arquivo permanece consultável", async () => {
    const r = snapshot("restaurar"); delete r.demandasArquivadas; preparar(r);
    expect(await buscarConfiguracaoEtapas("sessao", 9)).toEqual(r);
});
test.each([null, {}, "arquivo", [ { ...anterior } ]])("ARQ-19: presença inválida do arquivo não é ignorada", async arquivo => {
    const r = snapshot("restaurar"); r.demandasArquivadas = arquivo; preparar(r);
    await expect(buscarConfiguracaoEtapas("sessao", 9)).rejects.toMatchObject({ status: 200, message: mensagem });
});
test("ARQ-19/20: edição anterior valida arquivo sem contar arquivadas", async () => {
    const r = snapshot("restaurar"); r.demandasArquivadas = [{ ...anterior, id: 22 }];
    r.demandas[0].responsavel = "Ana"; r.demandas[0].observacoes = "Literal\r\nTexto"; preparar(r);
    expect(await editarDemanda("sessao", 9, anterior, { ...anterior, versao: 2 })).toEqual(r);
});
