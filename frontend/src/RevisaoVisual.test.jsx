import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { expect, test, vi } from "vitest";
import { readFileSync } from "node:fs";
import App from "./App";

test("VIS-01/02: Creral no login/cadastro e rótulos focam campos sem perder valores", async () => {
    const user = userEvent.setup(); render(<App />);
    expect(screen.getByText("Creral")).toBeTruthy(); expect(screen.getByRole("heading", { name: "Bem-vindo" })).toBeTruthy();
    for (const [nome, auto, valor] of [["E-mail", "username", "pessoa@example.test"], ["Senha", "current-password", "rascunho"]]) {
        await user.click(screen.getByText(nome, { selector: "label", exact: true }));
        const input = screen.getByLabelText(nome, { exact: true });
        expect(document.activeElement).toBe(input); expect(input.autocomplete).toBe(auto);
        await user.type(input, valor); expect(input.value).toBe(valor);
    }
    await user.click(screen.getByRole("button", { name: "Criar cadastro", exact: true }));
    expect(screen.getByText("Creral")).toBeTruthy(); expect(screen.getByRole("heading", { name: "Criar cadastro" })).toBeTruthy();
});

test("VIS-01/07: cabeçalho Creral preserva usuário e sessão", async () => {
    const usuario = { id: 1, nome: "Ana", email: "ana@example.test", setor: "Campo" };
    localStorage.setItem("usuario", JSON.stringify(usuario)); localStorage.setItem("token", "sessao");
    const fetchMock = vi.fn(() => Promise.resolve(new Response("[]", { status: 200 })));
    vi.stubGlobal("fetch", fetchMock); render(<App />);
    expect(screen.getByRole("heading", { name: "Creral", exact: true })).toBeTruthy();
    expect(await screen.findByText("Nenhum gerenciamento ativo")).toBeTruthy();
    expect(screen.getByText("Ana", { selector: "strong" })).toBeTruthy();
    expect(localStorage.getItem("token")).toBe("sessao"); expect(JSON.parse(localStorage.getItem("usuario"))).toEqual(usuario);
    expect(fetchMock.mock.calls.length).toBe(1);
});

test("VIS-01: documento em português e título identificável na aba", () => {
    const html = readFileSync("index.html", "utf8");
    expect(html).toContain('<html lang="pt-BR">');
    expect(html).toContain("<title>Creral | Gestão de demandas</title>");
});
