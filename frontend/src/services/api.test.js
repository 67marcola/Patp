import { expect, test, vi } from "vitest";
import {
    arquivarGerenciamento, buscarGerenciamento, criarGerenciamento,
    editarGerenciamento, listarEtapas, listarGerenciamentos, restaurarGerenciamento,
    buscarConfiguracaoEtapas, criarEtapa, editarEtapa, removerEtapa, criarDemanda
} from "./api";

const quadro = {
    id: 9, nome: "Instalações", descricao: "Postes",
    criador: { id: 3, nome: "Ana" }, arquivado: false, versao: 2, podeAdministrar: true
};
const json = (dados, status = 200) => new Response(JSON.stringify(dados), {
    status, headers: { "Content-Type": "application/json" }
});

function resposta(dados, status = 200) {
    const fetchSpy = vi.fn().mockResolvedValue(json(dados, status));
    vi.stubGlobal("fetch", fetchSpy);
    return fetchSpy;
}

test.each([false, true])("GER-28: consulta filtro arquivado=%s e envia a sessão", async (arquivado) => {
    const fetchSpy = resposta([quadro]);
    const controller = new AbortController();
    expect(await listarGerenciamentos("sessao", arquivado, { signal: controller.signal }))
        .toEqual([quadro]);
    expect(fetchSpy).toHaveBeenCalledWith(
        `http://localhost:8081/api/gerenciamentos?arquivado=${arquivado}`,
        expect.objectContaining({ method: "GET", headers: { Authorization: "Bearer sessao" }, signal: controller.signal })
    );
});

test("GER-35: buscar quadro mantém o DTO, a autoria resumida e a versão", async () => {
    const fetchSpy = resposta(quadro);
    expect(await buscarGerenciamento("sessao", 9)).toEqual(quadro);
    expect(fetchSpy).toHaveBeenCalledWith("http://localhost:8081/api/gerenciamentos/9", expect.objectContaining({ method: "GET" }));
});

test("GER-06/35: criação envia campos e etapas opcionais com POST", async () => {
    const fetchSpy = resposta(quadro, 201);
    const dados = { nome: "Instalações", descricao: "", etapas: [] };
    expect(await criarGerenciamento("sessao", dados)).toEqual(quadro);
    const [, request] = fetchSpy.mock.calls[0];
    expect(request.method).toBe("POST");
    expect(request.headers).toEqual({ Authorization: "Bearer sessao", "Content-Type": "application/json" });
    expect(JSON.parse(request.body)).toEqual(dados);
});

test("GER-35: edição envia somente nome, descrição e versão ao PUT correto", async () => {
    const fetchSpy = resposta({ ...quadro, nome: "Novo nome", versao: 3 });
    const dados = { nome: "Novo nome", descricao: "Postes", versao: 2 };
    expect(await editarGerenciamento("sessao", 9, dados)).toEqual({ ...quadro, nome: "Novo nome", versao: 3 });
    expect(fetchSpy.mock.calls[0][0]).toBe("http://localhost:8081/api/gerenciamentos/9");
    expect(fetchSpy.mock.calls[0][1].method).toBe("PUT");
    expect(JSON.parse(fetchSpy.mock.calls[0][1].body)).toEqual(dados);
});

test.each([
    ["arquivar", arquivarGerenciamento], ["restaurar", restaurarGerenciamento]
])("GER-34/41: %s aceita 204 vazio e envia versão em PUT", async (acao, executar) => {
    const fetchSpy = vi.fn().mockResolvedValue(new Response(null, { status: 204 }));
    vi.stubGlobal("fetch", fetchSpy);
    expect(await executar("sessao", 9, 2)).toBeUndefined();
    expect(fetchSpy.mock.calls[0][0]).toBe(`http://localhost:8081/api/gerenciamentos/9/${acao}`);
    expect(fetchSpy.mock.calls[0][1].method).toBe("PUT");
    expect(JSON.parse(fetchSpy.mock.calls[0][1].body)).toEqual({ versao: 2 });
});

test.each([
    [400, "Informe um nome entre 1 e 120 caracteres."],
    [401, "Usuário não autenticado."],
    [403, "Você não tem permissão para administrar este gerenciamento."],
    [404, "Gerenciamento não encontrado."],
    [409, "Gerenciamento alterado por outro usuário. Atualize e tente novamente."]
])("GER-33/40: erro HTTP %s conserva mensagem e status", async (status, mensagem) => {
    const fetchSpy = resposta({ erro: mensagem }, status);
    await expect(editarGerenciamento("sessao", 9, { nome: "Outro", versao: 2 }))
        .rejects.toMatchObject({ message: mensagem, status });
    expect(fetchSpy).toHaveBeenCalledTimes(1);
});

test("GER-33: falha de comunicação não afirma rollback nem repete criação", async () => {
    const fetchSpy = vi.fn().mockRejectedValue(new TypeError("Failed to fetch"));
    vi.stubGlobal("fetch", fetchSpy);
    await expect(criarGerenciamento("sessao", { nome: "Novo" })).rejects.toMatchObject({
        message: "Não foi possível confirmar a operação. Atualize a lista antes de tentar novamente.", status: 0
    });
    expect(fetchSpy).toHaveBeenCalledTimes(1);
});

test("GER-33/40: erro sem JSON usa mensagem segura e conserva status", async () => {
    vi.stubGlobal("fetch", vi.fn().mockResolvedValue(new Response("<html>detalhe interno</html>", { status: 500 })));
    await expect(buscarGerenciamento("sessao", 9)).rejects.toMatchObject({
        message: "Não foi possível concluir a operação.", status: 500
    });
});

test("GER-29: consulta etapas pelo contrato central e mantém seus dados", async () => {
    const etapas = [{ id: 4, nome: "Planejamento", setor: "Técnico", ordem: 1 }];
    const fetchSpy = resposta(etapas);
    expect(await listarEtapas("sessao", 9)).toEqual(etapas);
    expect(fetchSpy.mock.calls[0][0]).toBe("http://localhost:8081/api/gerenciamentos/9/etapas");
});

const configuracao = {
    gerenciamento: { ...quadro, versao: 3 },
    etapas: [{ id: 4, nome: "Planejamento", setor: "Técnico", ordem: 1, quantidadeDemandas: 2 }],
    demandas: [1, 2].map(id => ({ id, numeroProcesso: `D-${id}`, pessoa: `Cliente ${id}`,
        responsavel: null, status: "Em andamento", prioridade: null, dataEmissao: null,
        prazoEtapa: null, prazoGeral: null, dataConclusao: null, dataCancelamento: null,
        motivoCancelamento: null, observacoes: null, etapaId: 4 }))
};
const dadosEtapa = { nome: "Planejamento", setor: "Técnico", ordem: 1, versao: 2 };
const mutacoesEtapa = [
    ["POST", "/9/etapas", () => criarEtapa("sessao", 9, dadosEtapa), dadosEtapa, 201],
    ["PUT", "/9/etapas/4", () => editarEtapa("sessao", 9, 4, dadosEtapa), dadosEtapa, 200],
    ["DELETE", "/9/etapas/4", () => removerEtapa("sessao", 9, 4, 2), { versao: 2 }, 200]
];

test("ETA-01/29: GET estrutura envia sessão/signal e retorna snapshot completo", async () => {
    const fetchSpy = resposta(configuracao);
    const controller = new AbortController();
    expect(await buscarConfiguracaoEtapas("sessao", 9, { signal: controller.signal })).toEqual(configuracao);
    expect(fetchSpy).toHaveBeenCalledWith("http://localhost:8081/api/gerenciamentos/9/estrutura-etapas", {
        method: "GET", headers: { Authorization: "Bearer sessao" }, signal: controller.signal
    });
});

test.each(mutacoesEtapa)("ETA-12/13/15/28: %s envia versão/campos e retorna configuração JSON", async (method, rota, executar, dados, status) => {
    const fetchSpy = resposta(configuracao, status);
    expect(await executar()).toEqual(configuracao);
    expect(fetchSpy.mock.calls[0][0]).toBe(`http://localhost:8081/api/gerenciamentos${rota}`);
    expect(fetchSpy.mock.calls[0][1]).toEqual({
        method, headers: { Authorization: "Bearer sessao", "Content-Type": "application/json" },
        signal: undefined, body: JSON.stringify(dados)
    });
    expect(fetchSpy).toHaveBeenCalledTimes(1);
});

test.each(mutacoesEtapa)("ETA-27/29: %s conserva erro HTTP e não repete gravação", async (_method, _rota, executar) => {
    const mensagem = "Gerenciamento alterado por outro usuário. Atualize e tente novamente.";
    const fetchSpy = resposta({ erro: mensagem }, 409);
    await expect(executar()).rejects.toMatchObject({ message: mensagem, status: 409 });
    expect(fetchSpy).toHaveBeenCalledTimes(1);
});

test.each(mutacoesEtapa)("ETA-27: %s sem confirmação de rede não anuncia sucesso nem reenvia", async (_method, _rota, executar) => {
    const fetchSpy = vi.fn().mockRejectedValue(new TypeError("offline"));
    vi.stubGlobal("fetch", fetchSpy);
    await expect(executar()).rejects.toMatchObject({
        message: "Não foi possível confirmar a operação. Atualize a lista antes de tentar novamente.", status: 0
    });
    expect(fetchSpy).toHaveBeenCalledTimes(1);
});

const corposIlegiveis = ["<html>resposta incompleta</html>", '{"gerenciamento":'];
test.each(mutacoesEtapa.flatMap(([method, _rota, executar, _dados, status]) =>
    corposIlegiveis.map(corpo => [method, status, corpo, executar])))
    ("ETA-27: %s HTTP%s com JSON ilegível%s rejeita sem snapshot ou retry", async (_method, status, corpo, executar) => {
        const fetchSpy = vi.fn().mockResolvedValue(new Response(corpo, {
            status, headers: { "Content-Type": "application/json" }
        }));
        vi.stubGlobal("fetch", fetchSpy);
        await expect(executar()).rejects.toMatchObject({
            name: "ApiError",
            message: "Não foi possível confirmar a operação. Atualize a lista antes de tentar novamente.",
            status
        });
        expect(fetchSpy).toHaveBeenCalledTimes(1);
    });

const dadosDemanda = { versao: 2, numeroProcesso: "D-3", pessoa: "Maria", responsavel: "Ana",
    prioridade: "Livre", dataEmissao: "2020-02-29", prazoEtapa: "2019-01-01", prazoGeral: "2018-01-01",
    observacoes: "Primeira\nSegunda" };
const demandaCriada = { id: 3, numeroProcesso: "D-3", pessoa: "Maria", responsavel: "Ana", status: "Em andamento",
    prioridade: "Livre", dataEmissao: "2020-02-29", prazoEtapa: "2019-01-01", prazoGeral: "2018-01-01",
    dataConclusao: null, dataCancelamento: null, motivoCancelamento: null, observacoes: "Primeira\nSegunda", etapaId: 4 };
const configuracaoCriada = { ...configuracao,
    etapas: [{ ...configuracao.etapas[0], categoria: "TRABALHO", quantidadeDemandas: 3 }],
    demandas: [...configuracao.demandas, demandaCriada] };
const rotasSnapshot = [
    ["GET", () => buscarConfiguracaoEtapas("sessao", 9), 200],
    ...mutacoesEtapa.map(([metodo, _rota, executar, _dados, status]) => [metodo, executar, status]),
    ["POST demanda", () => criarDemanda("sessao", 9, dadosDemanda), 201]
];

test("CAD-30: POST demanda envia somente nove campos e retorna o snapshot completo", async () => {
    const fetchSpy = resposta(configuracaoCriada, 201);
    expect(await criarDemanda("sessao", 9, dadosDemanda)).toEqual(configuracaoCriada);
    expect(fetchSpy).toHaveBeenCalledExactlyOnceWith("http://localhost:8081/api/gerenciamentos/9/demandas", {
        method: "POST", headers: { Authorization: "Bearer sessao", "Content-Type": "application/json" },
        signal: undefined, body: JSON.stringify(dadosDemanda)
    });
});

test.each(rotasSnapshot)("CAD-33/36: %s preserva valores antigos e exige confirmação somente no cadastro", async (rota, executar, status) => {
    const legado = structuredClone(rota === "POST demanda" ? configuracaoCriada : configuracao);
    Object.assign(legado.demandas[0], { status: "Desconhecido antigo", observacoes: "x".repeat(10001),
        dataEmissao: "2020-02-29", prazoEtapa: "2019-01-01", prazoGeral: "2018-01-01",
        dataConclusao: "2021-03-04", dataCancelamento: "2022-05-06", motivoCancelamento: "Legado" });
    resposta(legado, status);
    expect(await executar()).toEqual(legado);
    const vazio = { gerenciamento: quadro, etapas: [], demandas: [] };
    resposta(vazio, status);
    if (rota === "POST demanda") {
        await expect(executar()).rejects.toMatchObject({ status: 201,
            message: "Não foi possível confirmar a operação. Atualize a lista antes de tentar novamente." });
    } else expect(await executar()).toEqual(vazio);
});

test.each([
    ["mínimo", { versao: 2, numeroProcesso: "D-3", pessoa: "Maria" }, { responsavel: null, prioridade: null,
        dataEmissao: null, prazoEtapa: null, prazoGeral: null, observacoes: null }],
    ["textos aparados", { ...dadosDemanda, numeroProcesso: " D-3 ", pessoa: " Maria ", responsavel: " Ana ",
        prioridade: " Livre ", observacoes: " Primeira\nSegunda " }, {}],
    ["opcionais vazios", { ...dadosDemanda, responsavel: " \t ", prioridade: "\n", observacoes: " \n " },
        { responsavel: null, prioridade: null, observacoes: null }]
])("CAD-01/03/05/30/33: cadastro %s confirma os valores normalizados e defaults", async (_caso, enviados, campos) => {
    const confirmado = { ...configuracaoCriada, demandas: [...configuracao.demandas, { ...demandaCriada, ...campos }] };
    const fetchSpy = resposta(confirmado, 201);
    expect(await criarDemanda("sessao", 9, enviados)).toEqual(confirmado);
    expect(JSON.parse(fetchSpy.mock.calls[0][1].body)).toEqual(enviados);
    expect(fetchSpy).toHaveBeenCalledTimes(1);
});

test("CAD-06/33: confirmação escolhe primeiro trabalho por ordem/ID sem depender da ordem do array ou do nome", async () => {
    const confirmado = { ...configuracaoCriada, etapas: [
        { id: 20, nome: "Concluídos", ordem: 0, categoria: "CONCLUIDA", quantidadeDemandas: 0 },
        { id: 9, nome: "Trabalho posterior", ordem: 7, categoria: "TRABALHO", quantidadeDemandas: 0 },
        { id: 5, nome: "Concluídos", ordem: 3, categoria: "TRABALHO", quantidadeDemandas: 0 },
        { ...configuracaoCriada.etapas[0], nome: "Cancelados", ordem: 3 }
    ] };
    const fetchSpy = resposta(confirmado, 201);
    expect(await criarDemanda("sessao", 9, dadosDemanda)).toEqual(confirmado);
    expect(fetchSpy).toHaveBeenCalledTimes(1);
});

const confirmacoesInvalidas = [
    ["sem demandas", dado => { dado.demandas = []; dado.etapas[0].quantidadeDemandas = 0; }],
    ["sem demanda solicitada", dado => { dado.demandas.pop(); dado.etapas[0].quantidadeDemandas = 2; }],
    ...[
        ["numeroProcesso", "D-OUTRA"], ["pessoa", "Outra pessoa"], ["responsavel", "Outro responsável"],
        ["prioridade", "Outra prioridade"], ["dataEmissao", "2020-02-28"], ["prazoEtapa", "2019-01-02"],
        ["prazoGeral", "2018-01-02"], ["observacoes", "Outras observações"], ["status", "Concluido"],
        ["dataConclusao", "2020-02-29"], ["dataCancelamento", "2020-02-29"], ["motivoCancelamento", "Motivo indevido"]
    ].map(([campo, valor]) => [campo, dado => { dado.demandas[2][campo] = valor; }]),
    ["status ausente", dado => { dado.demandas[2].status = null; }],
    ["versão antiga", dado => { dado.gerenciamento.versao = 2; }],
    ["versão incrementada duas vezes", dado => { dado.gerenciamento.versao = 4; }],
    ["quadro arquivado", dado => { dado.gerenciamento.arquivado = true; }],
    ["sem trabalho", dado => { dado.etapas[0].categoria = "CONCLUIDA"; }],
    ["categoria não informada", dado => { delete dado.etapas[0].categoria; }],
    ["ordem inválida", dado => { dado.etapas[0].ordem = "1"; }],
    ["trabalho posterior", dado => {
        dado.etapas[0].quantidadeDemandas = 2;
        dado.etapas.push({ id: 5, nome: "Posterior", ordem: 2, categoria: "TRABALHO", quantidadeDemandas: 1 });
        dado.demandas[2].etapaId = 5;
    }],
    ["etapa final", dado => {
        dado.etapas[0].quantidadeDemandas = 2;
        dado.etapas.push({ id: 20, nome: "Concluídos", ordem: 2, categoria: "CONCLUIDA", quantidadeDemandas: 1 });
        dado.demandas[2].etapaId = 20;
    }],
    ["trabalho anterior por ordem", dado => {
        dado.etapas.push({ id: 5, nome: "Anterior", ordem: 0, categoria: "TRABALHO", quantidadeDemandas: 0 });
    }],
    ["desempate por ID", dado => {
        dado.etapas.push({ id: 2, nome: "Anterior por ID", ordem: 1, categoria: "TRABALHO", quantidadeDemandas: 0 });
    }],
    ["duas demandas correspondentes", dado => { dado.demandas[1] = { ...demandaCriada, id: 2 }; }]
];
test.each(confirmacoesInvalidas)("CAD-01/05/06/15/30/33: 201 com %s não confirma cadastro nem repete POST", async (_caso, invalidar) => {
    const incorreto = structuredClone(configuracaoCriada);
    invalidar(incorreto);
    const fetchSpy = resposta(incorreto, 201);
    await expect(criarDemanda("sessao", 9, dadosDemanda)).rejects.toMatchObject({ name: "ApiError", status: 201,
        message: "Não foi possível confirmar a operação. Atualize a lista antes de tentar novamente." });
    expect(fetchSpy).toHaveBeenCalledTimes(1);
});

test.each([200, 202])("CAD-01/33: HTTP%s não confirma cadastro mesmo com todos os dados esperados", async status => {
    const fetchSpy = resposta(configuracaoCriada, status);
    await expect(criarDemanda("sessao", 9, dadosDemanda)).rejects.toMatchObject({ status,
        message: "Não foi possível confirmar a operação. Atualize a lista antes de tentar novamente." });
    expect(fetchSpy).toHaveBeenCalledTimes(1);
});

const snapshotsInvalidos = [
    ["null", () => null], ["array", () => []],
    ["sem gerenciamento", dado => { delete dado.gerenciamento; return dado; }],
    ["quadro diferente", dado => { dado.gerenciamento.id = 10; return dado; }],
    ["versão negativa", dado => { dado.gerenciamento.versao = -1; return dado; }],
    ["versão texto", dado => { dado.gerenciamento.versao = "3"; return dado; }],
    ["arquivo ausente", dado => { delete dado.gerenciamento.arquivado; return dado; }],
    ["permissão inválida", dado => { dado.gerenciamento.podeAdministrar = "true"; return dado; }],
    ["sem etapas", dado => { delete dado.etapas; return dado; }],
    ["sem demandas", dado => { delete dado.demandas; return dado; }],
    ["demandas objeto", dado => { dado.demandas = {}; return dado; }],
    ["etapa ID zero", dado => { dado.etapas[0].id = 0; return dado; }],
    ["etapa ID duplicado", dado => { dado.etapas.push({ ...dado.etapas[0] }); return dado; }],
    ["contagem fracionada", dado => { dado.etapas[0].quantidadeDemandas = 1.5; return dado; }],
    ["contagem divergente", dado => { dado.etapas[0].quantidadeDemandas = 1; return dado; }],
    ["demanda ID negativo", dado => { dado.demandas[0].id = -1; return dado; }],
    ["demanda ID duplicado", dado => { dado.demandas[1].id = 1; return dado; }],
    ["etapa de outro quadro", dado => { dado.demandas[0].etapaId = 99; return dado; }],
    ["entidade aninhada", dado => { dado.demandas[0].etapa = { id: 4 }; return dado; }]
];
test.each(rotasSnapshot.flatMap(([rota, executar, status]) => snapshotsInvalidos.map(([caso, invalidar]) =>
    [rota, caso, executar, status, invalidar])))
    ("CAD-36: %s rejeita snapshot %s sem retry", async (rota, _caso, executar, status, invalidar) => {
        const fetchSpy = resposta(invalidar(structuredClone(rota === "POST demanda" ? configuracaoCriada : configuracao)), status);
        await expect(executar()).rejects.toMatchObject({ name: "ApiError", status,
            message: "Não foi possível confirmar a operação. Atualize a lista antes de tentar novamente." });
        expect(fetchSpy).toHaveBeenCalledTimes(1);
    });

test.each(Object.keys(configuracao.demandas[0]))("CAD-36: campo obrigatório do DTO %s ausente rejeita sucesso", async campo => {
    const incompleto = structuredClone(configuracao);
    delete incompleto.demandas[0][campo];
    resposta(incompleto);
    await expect(buscarConfiguracaoEtapas("sessao", 9)).rejects.toMatchObject({ status: 200,
        message: "Não foi possível confirmar a operação. Atualize a lista antes de tentar novamente." });
});

test.each([
    ["id", "1"], ["numeroProcesso", null], ["pessoa", 1], ["responsavel", {}], ["status", false],
    ["prioridade", []], ["dataEmissao", "2021-02-29"], ["prazoEtapa", "2020-13-01"],
    ["prazoGeral", "01/01/2020"], ["dataConclusao", 2020], ["dataCancelamento", "2020-04-31"],
    ["motivoCancelamento", true], ["observacoes", 1], ["etapaId", "4"]
])("CAD-36: campo %s com tipo/data inválido rejeita sucesso", async (campo, valor) => {
    const incorreto = structuredClone(configuracao);
    incorreto.demandas[0][campo] = valor;
    resposta(incorreto);
    await expect(buscarConfiguracaoEtapas("sessao", 9)).rejects.toMatchObject({ status: 200,
        message: "Não foi possível confirmar a operação. Atualize a lista antes de tentar novamente." });
});

test.each(rotasSnapshot)("CAD-33/36: %s não aceita 204 como confirmação do quadro", async (_rota, executar) => {
    const fetchSpy = vi.fn().mockResolvedValue(new Response(null, { status: 204 }));
    vi.stubGlobal("fetch", fetchSpy);
    await expect(executar()).rejects.toMatchObject({ status: 204,
        message: "Não foi possível confirmar a operação. Atualize a lista antes de tentar novamente." });
    expect(fetchSpy).toHaveBeenCalledTimes(1);
});

test.each([400, 401, 404, 409, 500])("CAD-33: cadastro HTTP%s conserva mensagem/status sem retry", async status => {
    const fetchSpy = resposta({ erro: "Mensagem do servidor" }, status);
    await expect(criarDemanda("sessao", 9, dadosDemanda)).rejects.toMatchObject({ message: "Mensagem do servidor", status });
    expect(fetchSpy).toHaveBeenCalledTimes(1);
});

test.each(["rede", "JSON"])("CAD-33: cadastro com falha de %s não confirma nem repete POST", async falha => {
    const fetchSpy = falha === "rede" ? vi.fn().mockRejectedValue(new TypeError("offline"))
        : vi.fn().mockResolvedValue(new Response("<html>incompleto</html>", { status: 201 }));
    vi.stubGlobal("fetch", fetchSpy);
    await expect(criarDemanda("sessao", 9, dadosDemanda)).rejects.toMatchObject({ status: falha === "rede" ? 0 : 201,
        message: "Não foi possível confirmar a operação. Atualize a lista antes de tentar novamente." });
    expect(fetchSpy).toHaveBeenCalledTimes(1);
});

test("CAD-35/36: GET abortado conserva AbortError e signal sem retry", async () => {
    const controller = new AbortController();
    const erro = new DOMException("Abortado", "AbortError");
    const fetchSpy = vi.fn().mockRejectedValue(erro);
    vi.stubGlobal("fetch", fetchSpy);
    await expect(buscarConfiguracaoEtapas("sessao", 9, { signal: controller.signal })).rejects.toBe(erro);
    expect(fetchSpy.mock.calls[0][1].signal).toBe(controller.signal);
    expect(fetchSpy).toHaveBeenCalledTimes(1);
});
