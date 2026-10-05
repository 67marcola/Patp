import { expect, test, vi } from "vitest";
import { ApiError, cadastrarUsuario, login } from "./api";

const cadastro = { nome: "Ana", setor: "Campo", email: "ana@example.test", senha: " senha " };
const session = { token: " sessao original ", id: 3, nome: "Ana", setor: "Campo", email: "ana@example.test", papel: "FUNCIONARIO" };
const message = "Não foi possível confirmar a operação. Atualize a lista antes de tentar novamente.";
const wrappers = [
    ["login", () => login(cadastro.email, cadastro.senha), { email: cadastro.email, senha: cadastro.senha }],
    ["cadastro", () => cadastrarUsuario(cadastro), cadastro]
];

function respond(body, status = 200, raw = false) {
    const fetchMock = vi.fn().mockResolvedValue(new Response(raw ? body : JSON.stringify(body), { status }));
    vi.stubGlobal("fetch", fetchMock);
    return fetchMock;
}

test.each(wrappers.flatMap(([route, run, payload]) => ["Campo", null].map(setor => [route, run, payload, setor])))
    ("AUT-06/10: %s conserva contrato, token original e setor=%s", async (route, run, payload, setor) => {
        const expected = { ...session, setor };
        const fetchMock = respond(expected);
        expect(await run()).toEqual(expected);
        expect(fetchMock.mock.calls[0][0]).toBe(`http://localhost:8081/api/usuarios/${route}`);
        expect(fetchMock.mock.calls[0][1]).toEqual({
            method: "POST", headers: { "Content-Type": "application/json" }, signal: undefined,
            body: JSON.stringify(payload)
        });
        expect(fetchMock).toHaveBeenCalledTimes(1);
    });

const invalid = [
    ["null", null], ["array", []], ["string", "sessao"],
    ["token ausente", { ...session, token: undefined }], ["token vazio", { ...session, token: "" }],
    ["token espaços", { ...session, token: "   " }], ["token número", { ...session, token: 7 }],
    ["id ausente", { ...session, id: undefined }], ["id zero", { ...session, id: 0 }],
    ["id negativo", { ...session, id: -1 }], ["id fracionário", { ...session, id: 1.5 }],
    ["id string", { ...session, id: "3" }],
    ["nome ausente", { ...session, nome: undefined }], ["nome número", { ...session, nome: 7 }],
    ["email ausente", { ...session, email: undefined }], ["email número", { ...session, email: 7 }],
    ["setor ausente", { ...session, setor: undefined }], ["setor número", { ...session, setor: 7 }]
];

test.each(wrappers.flatMap(([route, run]) => invalid.map(([kind, body]) => [route, run, kind, body])))
    ("AUT-06: %s rejeita %s sem retry", async (_route, run, _kind, body) => {
        const fetchMock = respond(body);
        const error = await run().catch(error => error);
        expect(error).toBeInstanceOf(ApiError);
        expect(error).toMatchObject({ status: 200, message });
        expect(fetchMock).toHaveBeenCalledTimes(1);
    });

test.each(wrappers.flatMap(([route, run]) => ["<html>incompleto</html>", '{"token":'].map(body => [route, run, body])))
    ("AUT-06: %s rejeita JSON ilegível %s", async (_route, run, body) => {
        const fetchMock = respond(body, 200, true);
        const error = await run().catch(error => error);
        expect(error).toBeInstanceOf(ApiError);
        expect(error).toMatchObject({ status: 200, message });
        expect(fetchMock).toHaveBeenCalledTimes(1);
    });

test.each(wrappers.flatMap(([route, run]) => [
    [400, { erro: "Dados da requisição inválidos." }, "Dados da requisição inválidos.", false],
    [500, { erro: "Não foi possível concluir a operação." }, "Não foi possível concluir a operação.", false],
    [500, "<html>interno</html>", "Não foi possível concluir a operação.", true]
].map(([status, body, expected, raw]) => [route, run, status, body, expected, raw])))
    ("AUT-13: %s preserva erro HTTP %s", async (_route, run, status, body, expected, raw) => {
        const fetchMock = respond(body, status, raw);
        const error = await run().catch(error => error);
        expect(error).toBeInstanceOf(ApiError);
        expect(error).toMatchObject({ status, message: expected });
        expect(fetchMock).toHaveBeenCalledTimes(1);
    });

test.each(wrappers)("AUT-13: %s preserva erro de rede sem retry", async (_route, run) => {
    const fetchMock = vi.fn().mockRejectedValue(new TypeError("offline fictício"));
    vi.stubGlobal("fetch", fetchMock);
    const error = await run().catch(error => error);
    expect(error).toBeInstanceOf(ApiError);
    expect(error).toMatchObject({ status: 0, message });
    expect(fetchMock).toHaveBeenCalledTimes(1);
});
