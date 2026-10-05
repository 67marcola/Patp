import { expect, test, vi } from "vitest";
import {
    arquivarGerenciamento, buscarGerenciamento, criarGerenciamento,
    editarGerenciamento, listarEtapas, listarGerenciamentos, restaurarGerenciamento
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
