import { fireEvent, render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { expect, test, vi } from "vitest";
import App from "./App";
import { ativarPorTeclado } from "./test/keyboard";

const publicUser = { id: 31, nome: "Nova", setor: "Campo", email: "nova@example.test" };
const session = { ...publicUser, token: "cadastro-sessao", papel: "FUNCIONARIO" };
const draft = { "Nome completo": " Nova ", Setor: " Campo ", "E-mail": "nova@example.test", Senha: "senha-ficticia", "Confirmar senha": "senha-ficticia" };
const storageMessage = "Não foi possível salvar sua sessão neste navegador. Permita salvar dados do site e entre pelo login.";

async function prepare() {
    const fetchMock = vi.fn((url, request) => {
        if (url.endsWith("/usuarios/cadastro")) return new Response(JSON.stringify(session), { status: 200 });
        if (url.includes("/gerenciamentos?")) {
            expect(localStorage.getItem("token")).toBe(session.token);
            expect(JSON.parse(localStorage.getItem("usuario"))).toEqual(publicUser);
            expect(request.headers.Authorization).toBe(`Bearer ${session.token}`);
            return new Response("[]", { status: 200 });
        }
        throw new Error(`Rota inesperada: ${url}`);
    });
    vi.stubGlobal("fetch", fetchMock);
    const user = userEvent.setup();
    render(<App />);
    await user.click(screen.getByRole("button", { name: "Criar cadastro" }));
    for (const [name, value] of Object.entries(draft)) fireEvent.change(screen.getByLabelText(name, { exact: true }), { target: { value } });
    return { user, fetchMock };
}

test("AUT-07/10: App abre a lista com a sessão do cadastro sem POST login adicional", async () => {
    const { user, fetchMock } = await prepare();
    await user.click(screen.getByRole("button", { name: "Criar cadastro", exact: true }));
    expect((await screen.findByRole("heading", { name: "Gerenciamentos" })).textContent).toBe("Gerenciamentos");
    expect(localStorage.getItem("token")).toBe("cadastro-sessao");
    expect(JSON.parse(localStorage.getItem("usuario"))).toEqual(publicUser);
    expect(Object.keys(JSON.parse(localStorage.getItem("usuario"))).sort()).toEqual(["email", "id", "nome", "setor"]);
    expect(fetchMock.mock.calls.map(([url]) => url.split("/api")[1])).toEqual(["/usuarios/cadastro", "/gerenciamentos?arquivado=false"]);
});

test("AUT-08/13: falha da segunda chave mantém cadastro, rascunho e alerta de storage sem login", async () => {
    const { user, fetchMock } = await prepare();
    const original = Storage.prototype.setItem;
    let failed = false;
    vi.spyOn(Storage.prototype, "setItem").mockImplementation(function (name, value) {
        if (name === "usuario" && !failed) {
            failed = true;
            throw new Error("gravação recusada fictícia");
        }
        return original.call(this, name, value);
    });
    await user.click(screen.getByRole("button", { name: "Criar cadastro", exact: true }));
    expect((await screen.findByRole("alert")).textContent).toBe(storageMessage);
    expect(screen.getByRole("heading", { name: "Criar cadastro" }).textContent).toBe("Criar cadastro");
    expect(screen.queryByRole("heading", { name: "Gerenciamentos" })).toBeNull();
    expect(localStorage.getItem("token")).toBeNull();
    expect(localStorage.getItem("usuario")).toBeNull();
    for (const [name, value] of Object.entries(draft)) {
        expect(screen.getByLabelText(name, { exact: true }).value).toBe(value);
        expect(screen.getByLabelText(name, { exact: true }).disabled).toBe(false);
    }
    expect(screen.getByRole("button", { name: "Criar cadastro", exact: true }).disabled).toBe(false);
    expect(screen.getByRole("button", { name: "Voltar para login" }).disabled).toBe(false);
    expect(fetchMock.mock.calls.map(([url]) => url.split("/api")[1])).toEqual(["/usuarios/cadastro"]);
});

test("AUT-14: Voltar por teclado mostra a tela de login em App", async () => {
    const { user, fetchMock } = await prepare();
    await ativarPorTeclado(user, screen.getByRole("button", { name: "Voltar para login" }));
    expect(screen.getByRole("heading", { name: "Bem-vindo" }).textContent).toBe("Bem-vindo");
    expect(screen.queryByRole("heading", { name: "Criar cadastro" })).toBeNull();
    expect(fetchMock).not.toHaveBeenCalled();
});
