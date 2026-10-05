import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { expect, test, vi } from "vitest";
import App from "./App";

const publicUser = { id: 3, nome: "Ana", setor: null, email: "ana@example.test" };
const session = { ...publicUser, token: " sessao original ", papel: "FUNCIONARIO", senha: "não guardar", hash: "não guardar" };
const storageMessage = "Não foi possível salvar sua sessão neste navegador. Permita salvar dados do site e entre pelo login.";
const json = data => new Response(JSON.stringify(data), { status: 200 });

function prepare() {
    const fetchMock = vi.fn((url, request) => {
        if (url.endsWith("/usuarios/login")) return json(session);
        if (url.includes("/gerenciamentos?")) {
            expect(localStorage.getItem("token")).toBe(session.token);
            expect(JSON.parse(localStorage.getItem("usuario"))).toEqual(publicUser);
            expect(request.headers.Authorization).toBe(`Bearer ${session.token}`);
            return json([]);
        }
        throw new Error(`Rota inesperada: ${url}`);
    });
    vi.stubGlobal("fetch", fetchMock);
    return { ...render(<App />), user: userEvent.setup(), fetchMock };
}

async function enter(user) {
    await user.type(screen.getByPlaceholderText("seu@email.com"), publicUser.email);
    await user.type(screen.getByPlaceholderText("Digite sua senha"), "senha-ficticia");
    await user.click(screen.getByRole("button", { name: "Entrar", exact: true }));
}

test("AUT-07/09: App grava cache seguro antes da lista e preserva reload, saída e login", async () => {
    const { user, fetchMock, unmount } = prepare();
    await enter(user);
    expect((await screen.findByRole("heading", { name: "Gerenciamentos" })).textContent).toBe("Gerenciamentos");
    expect(localStorage.getItem("token")).toBe(session.token);
    expect(JSON.parse(localStorage.getItem("usuario"))).toEqual(publicUser);
    expect(Object.keys(JSON.parse(localStorage.getItem("usuario"))).sort()).toEqual(["email", "id", "nome", "setor"]);
    expect(fetchMock.mock.calls.filter(([url]) => url.endsWith("/usuarios/login"))).toHaveLength(1);
    unmount();
    render(<App />);
    expect((await screen.findByRole("heading", { name: "Gerenciamentos" })).textContent).toBe("Gerenciamentos");
    expect(localStorage.getItem("token")).toBe(session.token);
    expect(JSON.parse(localStorage.getItem("usuario"))).toEqual(publicUser);
    expect(fetchMock.mock.calls.filter(([url]) => url.endsWith("/usuarios/login"))).toHaveLength(1);
    await user.click(screen.getByRole("button", { name: "Sair" }));
    expect((await screen.findByRole("heading", { name: "Bem-vindo" })).textContent).toBe("Bem-vindo");
    expect(localStorage.getItem("token")).toBeNull();
    expect(localStorage.getItem("usuario")).toBeNull();
    await enter(user);
    expect((await screen.findByRole("heading", { name: "Gerenciamentos" })).textContent).toBe("Gerenciamentos");
    expect(JSON.parse(localStorage.getItem("usuario"))).toEqual(publicUser);
    expect(fetchMock.mock.calls.filter(([url]) => url.endsWith("/usuarios/login"))).toHaveLength(2);
});

test.each(["token", "usuario"].flatMap(key => [[key, false], [key, true]]))
    ("AUT-08: falha ao gravar %s restaura cache anterior=%s e mantém login", async (key, previous) => {
        const { user, fetchMock } = prepare();
        const oldToken = previous ? "sessao anterior" : null;
        const oldUser = previous ? JSON.stringify({ id: 8, nome: "Anterior", setor: "Campo", email: "anterior@example.test" }) : null;
        if (previous) {
            localStorage.setItem("token", oldToken);
            localStorage.setItem("usuario", oldUser);
        }
        const original = Storage.prototype.setItem;
        let failed = false;
        vi.spyOn(Storage.prototype, "setItem").mockImplementation(function (name, value) {
            if (name === key && !failed) {
                failed = true;
                throw new Error("escrita recusada fictícia");
            }
            return original.call(this, name, value);
        });
        await enter(user);
        expect((await screen.findByText(storageMessage)).textContent).toBe(storageMessage);
        expect(screen.getByRole("heading", { name: "Bem-vindo" }).textContent).toBe("Bem-vindo");
        expect(screen.queryByRole("heading", { name: "Gerenciamentos" })).toBeNull();
        expect(localStorage.getItem("token")).toBe(oldToken);
        expect(localStorage.getItem("usuario")).toBe(oldUser);
        expect(screen.getByPlaceholderText("seu@email.com").value).toBe(publicUser.email);
        expect(screen.getByPlaceholderText("Digite sua senha").value).toBe("senha-ficticia");
        expect(screen.getByRole("button", { name: "Entrar", exact: true }).disabled).toBe(false);
        expect(fetchMock.mock.calls.map(([url]) => url.split("/api")[1])).toEqual(["/usuarios/login"]);
    });

test("AUT-08: recusa de restauração continua na autenticação e tenta ambas as chaves", async () => {
    const { user, fetchMock } = prepare();
    localStorage.setItem("token", "anterior");
    localStorage.setItem("usuario", JSON.stringify(publicUser));
    const setItem = vi.spyOn(Storage.prototype, "setItem").mockImplementation(() => { throw new Error("storage bloqueado"); });
    await enter(user);
    expect((await screen.findByText(storageMessage)).textContent).toBe(storageMessage);
    expect(screen.getByRole("heading", { name: "Bem-vindo" }).textContent).toBe("Bem-vindo");
    expect(screen.queryByRole("heading", { name: "Gerenciamentos" })).toBeNull();
    expect(setItem.mock.calls).toEqual([["token", session.token], ["token", "anterior"], ["usuario", JSON.stringify(publicUser)]]);
    expect(localStorage.getItem("token")).toBe("anterior");
    expect(JSON.parse(localStorage.getItem("usuario"))).toEqual(publicUser);
    expect(fetchMock.mock.calls.map(([url]) => url.split("/api")[1])).toEqual(["/usuarios/login"]);
    await waitFor(() => expect(screen.getByRole("button", { name: "Entrar", exact: true }).disabled).toBe(false));
});
