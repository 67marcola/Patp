import { expect, test, vi } from "vitest";
import { buscarComentarios, enviarComentario } from "./api";

const erro = "Não foi possível confirmar a operação. Atualize a lista antes de tentar novamente.";
const registro = () => ({ id: 21, texto: "  <b>Literal</b>\r\n fim  ", funcionario: "Ana", dataHora: "2020-02-29T12:34:56.123456789", processoId: 7 });
const conversa = () => ({ gerenciamentoId: 9, processoId: 7, quadroArquivado: false, demandaArquivada: false, comentarios: [registro()] });
function resposta(corpo, status = 200) { const f = vi.fn().mockResolvedValue(new Response(status === 204 ? null : JSON.stringify(corpo), { status })); vi.stubGlobal("fetch", f); return f; }
test("COM-15/18: GET confirma valores e envia sessão/signal na conversa correta", async () => {
    const dados = conversa(), signal = new AbortController().signal, f = resposta(dados);
    expect(await buscarComentarios("sessao", 9, 7, { signal })).toEqual(dados);
    expect(f.mock.calls[0]).toEqual(["http://localhost:8081/api/gerenciamentos/9/demandas/7/comentarios", { method: "GET", headers: { Authorization: "Bearer sessao" }, signal }]);
});
test.each([[false, false], [true, false], [false, true], [true, true]])("COM-11/20: consulta flags %s/%s e antigos fora do limite", async (q, p) => {
    const c = conversa(); c.quadroArquivado = q; c.demandaArquivada = p; c.comentarios[0].texto = "x".repeat(10001); c.comentarios[0].funcionario = ""; resposta(c);
    expect(await buscarComentarios("sessao", 9, 7)).toEqual(c);
});
test("COM-15: GET vazio e desempate preservam ordem com nanossegundos", async () => {
    const c = conversa(); c.comentarios = []; resposta(c); expect(await buscarComentarios("s", 9, 7)).toEqual(c);
    c.comentarios = [{ ...registro(), id: 20, dataHora: "2020-02-29T12:34:56.1" }, { ...registro(), id: 22, dataHora: "2020-02-29T12:34:56.100000000" }, { ...registro(), id: 19, dataHora: "2020-02-29T12:34:56.100000001" }]; resposta(c); expect(await buscarComentarios("s", 9, 7)).toEqual(c);
});
const invalidosGET = [
    ["quadro diferente", c => { c.gerenciamentoId = 8; }], ["demanda diferente", c => { c.processoId = 8; }],
    ["tipo quadro", c => { c.gerenciamentoId = "9"; }], ["tipo demanda", c => { c.processoId = "7"; }],
    ["flag quadro ausente", c => { delete c.quadroArquivado; }], ["flag demanda ausente", c => { delete c.demandaArquivada; }],
    ["flag quadro string", c => { c.quadroArquivado = "false"; }], ["flag demanda nulo", c => { c.demandaArquivada = null; }],
    ["lista objeto", c => { c.comentarios = {}; }], ["extra envelope", c => { c.token = "vazamento"; }],
    ["ID duplicado", c => { c.comentarios.push({ ...registro() }); }],
    ["desempate invertido", c => { c.comentarios.push({ ...registro(), id: 20 }); }],
    ["data invertida", c => { c.comentarios.push({ ...registro(), id: 22, dataHora: "2020-02-28T12:34:56" }); }],
    ["fração invertida", c => { c.comentarios.push({ ...registro(), id: 22, dataHora: "2020-02-29T12:34:56.123456788" }); }],
    ...["id", "texto", "funcionario", "dataHora", "processoId"].map(campo => [`ausente ${campo}`, c => { delete c.comentarios[0][campo]; }]),
    ["ID zero", c => { c.comentarios[0].id = 0; }], ["ID inseguro", c => { c.comentarios[0].id = Number.MAX_SAFE_INTEGER + 1; }],
    ["ID string", c => { c.comentarios[0].id = "21"; }], ["texto nulo", c => { c.comentarios[0].texto = null; }],
    ["autor número", c => { c.comentarios[0].funcionario = 4; }], ["vínculo diferente", c => { c.comentarios[0].processoId = 8; }],
    ["extra registro", c => { c.comentarios[0].processo = {}; }],
    ...[null, "2023-02-29T12:34:56", "2020-02-30T12:34:56", "2020-02-29T24:00:00", "2020-02-29T12:60:00", "2020-02-29T12:34:60", "2020-02-29T12:34", "2020-02-29T12:34:56Z", "2020-02-29T12:34:56.1234567890"].map(data => [`data ${data}`, c => { c.comentarios[0].dataHora = data; }])
];
test.each(invalidosGET)("COM-18: não aplica GET inválido %s", async (_nome, mudar) => {
    const c = conversa(); mudar(c); const f = resposta(c); await expect(buscarComentarios("s", 9, 7)).rejects.toMatchObject({ message: erro, status: 200 }); expect(f).toHaveBeenCalledTimes(1);
});
test("COM-23: POST confirma cada campo, só texto enviado e nenhuma autoria de cache", async () => {
    const r = registro(), f = resposta(r, 201); expect(await enviarComentario("sessao", 9, 7, r.texto, [])).toEqual(r);
    const [url, req] = f.mock.calls[0]; expect(url).toBe("http://localhost:8081/api/gerenciamentos/9/demandas/7/comentarios"); expect(req.method).toBe("POST"); expect(req.headers).toEqual({ Authorization: "Bearer sessao", "Content-Type": "application/json" }); expect(JSON.parse(req.body)).toEqual({ texto: r.texto }); expect(f).toHaveBeenCalledTimes(1);
});
const invalidosPOST = [
    ...["id", "texto", "funcionario", "dataHora", "processoId"].map(campo => [`ausente ${campo}`, r => { delete r[campo]; }]),
    ["texto divergente", r => { r.texto = "Diferente"; }], ["texto aparado", r => { r.texto = r.texto.trim(); }],
    ["vínculo", r => { r.processoId = 8; }], ["autor vazio", r => { r.funcionario = ""; }], ["autor branco", r => { r.funcionario = " \n"; }],
    ["autor número", r => { r.funcionario = 9; }], ["data inválida", r => { r.dataHora = "2023-02-29T12:34:56"; }],
    ["ID fracionado", r => { r.id = 2.5; }], ["ID zero", r => { r.id = 0; }], ["extra", r => { r.token = "não"; }]
];
test.each(invalidosPOST)("COM-24: recusa POST divergente %s sem retry", async (_nome, mudar) => {
    const r = registro(); mudar(r); const f = resposta(r, 201); await expect(enviarComentario("s", 9, 7, registro().texto, [])).rejects.toMatchObject({ message: erro, status: 201 }); expect(f).toHaveBeenCalledTimes(1);
});
test("COM-24: resposta com ID já listado não confirma novo comentário", async () => {
    const r = registro(), f = resposta(r, 201); await expect(enviarComentario("s", 9, 7, r.texto, [r])).rejects.toMatchObject({ message: erro, status: 201 }); expect(f).toHaveBeenCalledTimes(1);
});
for (const [nome, executar, corpo, valido] of [["GET", () => buscarComentarios("s", 9, 7), conversa, 200], ["POST", () => enviarComentario("s", 9, 7, registro().texto, []), registro, 201]]) {
    test.each([valido === 200 ? 201 : 200, 202, 204])(`COM-18/24: ${nome} recusa HTTP%s`, async status => { resposta(corpo(), status); await expect(executar()).rejects.toMatchObject({ message: erro, status }); });
    test.each([400, 401, 404, 409, 500])(`COM-18/24: ${nome} erro HTTP%s permanece sem retry`, async status => { const f = resposta({ erro: "Erro do servidor" }, status); await expect(executar()).rejects.toMatchObject({ message: "Erro do servidor", status }); expect(f).toHaveBeenCalledTimes(1); });
    test(`COM-18/24: ${nome} rede e JSON conservam falha sem retry`, async () => {
        const f = vi.fn().mockRejectedValue(new TypeError("Offline")); vi.stubGlobal("fetch", f); await expect(executar()).rejects.toMatchObject({ message: erro, status: 0 }); expect(f).toHaveBeenCalledTimes(1);
        f.mockReset().mockResolvedValue(new Response("{", { status: valido })); await expect(executar()).rejects.toMatchObject({ message: erro, status: valido }); expect(f).toHaveBeenCalledTimes(1);
    });
}
