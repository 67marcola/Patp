import { act, fireEvent, render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { expect, test, vi } from "vitest";
import Cadastro from "./Cadastro";
import { ativarPorTeclado } from "../test/keyboard";

const values = { "Nome completo": " Ana ", Setor: " Campo ", "E-mail": "ana@example.test", Senha: " 1234 ", "Confirmar senha": " 1234 " };
const session = { token: "sessao", id: 3, nome: "Ana", setor: "Campo", email: "ana@example.test", papel: "FUNCIONARIO" };
const uncertain = "Não foi possível confirmar o cadastro. Se a conta já foi criada, entre pelo login.";
const json = (body, status = 200) => new Response(JSON.stringify(body), { status });

function prepare(response = json(session)) {
    const fetchMock = vi.fn().mockResolvedValue(response);
    vi.stubGlobal("fetch", fetchMock);
    const onLogin = vi.fn();
    const voltar = vi.fn();
    render(<Cadastro onLogin={onLogin} voltar={voltar} />);
    return { fetchMock, onLogin, voltar, user: userEvent.setup() };
}

function fill(overrides = {}) {
    const draft = { ...values, ...overrides };
    for (const [name, value] of Object.entries(draft)) fireEvent.change(screen.getByLabelText(name, { exact: true }), { target: { value } });
    return draft;
}

function preserved(draft = values) {
    for (const [name, value] of Object.entries(draft)) {
        expect(screen.getByLabelText(name, { exact: true }).value).toBe(value);
        expect(screen.getByLabelText(name, { exact: true }).disabled).toBe(false);
    }
    expect(screen.getByRole("button", { name: "Criar cadastro", exact: true }).disabled).toBe(false);
    expect(screen.getByRole("button", { name: "Voltar para login" }).disabled).toBe(false);
}

test("AUT-10/12/14: cadastro de seis caracteres por teclado entrega sessão e normaliza somente dados públicos", async () => {
    const { user, fetchMock, onLogin } = prepare();
    fill();
    await ativarPorTeclado(user, screen.getByRole("button", { name: "Criar cadastro", exact: true }));
    expect(onLogin.mock.calls).toEqual([[session]]);
    expect(fetchMock.mock.calls.map(([url]) => url.split("/api")[1])).toEqual(["/usuarios/cadastro"]);
    expect(JSON.parse(fetchMock.mock.calls[0][1].body)).toEqual({ nome: "Ana", setor: "Campo", email: "ana@example.test", senha: " 1234 " });
    expect(screen.queryByRole("alert")).toBeNull();
});

test.each([
    [{ "Confirmar senha": "outra-senha" }, "As senhas não são iguais."],
    [{ Senha: "12345", "Confirmar senha": "12345" }, "A senha deve possuir pelo menos 6 caracteres."]
])("AUT-12: validação mantém os cinco campos, %s", async (overrides, expected) => {
    const { user, fetchMock, onLogin } = prepare();
    const draft = fill(overrides);
    await user.click(screen.getByRole("button", { name: "Criar cadastro", exact: true }));
    expect(screen.getByRole("alert").textContent).toBe(expected);
    preserved(draft);
    expect(fetchMock).not.toHaveBeenCalled();
    expect(onLogin).not.toHaveBeenCalled();
});

test("AUT-12/14: cinco labels associados conservam required e validação nativa de email", async () => {
    const { user, fetchMock, onLogin } = prepare();
    for (const name of Object.keys(values)) {
        const field = screen.getByLabelText(name, { exact: true });
        expect(field.required).toBe(true);
        expect(field.labels[0].htmlFor).toBe(field.id);
        expect(field.checkValidity()).toBe(false);
    }
    await user.click(screen.getByRole("button", { name: "Criar cadastro", exact: true }));
    fill({ "E-mail": "email-invalido" });
    expect(screen.getByLabelText("E-mail", { exact: true }).type).toBe("email");
    expect(screen.getByLabelText("E-mail", { exact: true }).checkValidity()).toBe(false);
    await user.click(screen.getByRole("button", { name: "Criar cadastro", exact: true }));
    expect(fetchMock).not.toHaveBeenCalled();
    expect(onLogin).not.toHaveBeenCalled();
});

test("AUT-11/13: dois submits imediatos enviam uma vez, bloqueiam sete controles e erro libera o rascunho", async () => {
    let resolve;
    const pending = new Promise(done => { resolve = done; });
    const { user, fetchMock, onLogin, voltar } = prepare(pending);
    fill();
    const form = screen.getByRole("button", { name: "Criar cadastro", exact: true }).closest("form");
    act(() => { fireEvent.submit(form); fireEvent.submit(form); });
    for (const name of Object.keys(values)) expect(screen.getByLabelText(name, { exact: true }).disabled).toBe(true);
    expect(screen.getByRole("button", { name: "Cadastrando..." }).disabled).toBe(true);
    expect(screen.getByRole("button", { name: "Voltar para login" }).disabled).toBe(true);
    await user.click(screen.getByRole("button", { name: "Voltar para login" }));
    expect(voltar).not.toHaveBeenCalled();
    expect(fetchMock.mock.calls.map(([url]) => url.split("/api")[1])).toEqual(["/usuarios/cadastro"]);
    expect(onLogin).not.toHaveBeenCalled();
    await act(async () => resolve(json({ erro: "Não foi possível concluir a operação." }, 500)));
    expect((await screen.findByRole("alert")).textContent).toBe("Não foi possível concluir a operação.");
    preserved();
    expect(onLogin).not.toHaveBeenCalled();
    expect(fetchMock).toHaveBeenCalledTimes(1);
});

test.each([400, 500])("AUT-13: erro HTTP %s conserva mensagem e todos os campos", async status => {
    const expected = status === 400 ? "Dados da requisição inválidos." : "Não foi possível concluir a operação.";
    const { user, fetchMock, onLogin } = prepare(json({ erro: expected }, status));
    fill();
    await user.click(screen.getByRole("button", { name: "Criar cadastro", exact: true }));
    expect((await screen.findByRole("alert")).textContent).toBe(expected);
    preserved();
    expect(onLogin).not.toHaveBeenCalled();
    expect(fetchMock).toHaveBeenCalledTimes(1);
});

test.each([
    ["rede", null], ["sessão incompleta", json({ id: 3 })],
    ["JSON 200 ilegível", new Response('{"token":', { status: 200 })],
    ["JSON 201 ilegível", new Response("<html>incompleto</html>", { status: 201 })]
])("AUT-13: %s explica cadastro incerto sem retry ou entrada", async (_kind, response) => {
    const { user, fetchMock, onLogin } = prepare(response);
    if (response === null) fetchMock.mockRejectedValue(new TypeError("offline fictício"));
    fill();
    await user.click(screen.getByRole("button", { name: "Criar cadastro", exact: true }));
    expect((await screen.findByRole("alert")).textContent).toBe(uncertain);
    preserved();
    expect(onLogin).not.toHaveBeenCalled();
    expect(fetchMock).toHaveBeenCalledTimes(1);
});

test("AUT-14: Voltar para login funciona por teclado", async () => {
    const { user, fetchMock, voltar } = prepare();
    await ativarPorTeclado(user, screen.getByRole("button", { name: "Voltar para login" }));
    expect(voltar).toHaveBeenCalledTimes(1);
    expect(fetchMock).not.toHaveBeenCalled();
});
