import { act, render, screen, waitFor, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { expect, test, vi } from "vitest";
import App from "./App";
import { ativarPorTeclado } from "./test/keyboard";

const usuario = { id: 3, nome: "Ana", setor: "Técnico", email: "ana@example.test" };
const quadro = { id: 9, nome: "Instalações", descricao: "Postes", criador: { id: 3, nome: "Ana" }, arquivado: false, versao: 0, podeAdministrar: true };
const etapa = { id: 4, nome: "Execução", setor: "Campo", ordem: 1, quantidadeDemandas: 0 };
const json = (dados, status = 200) => new Response(JSON.stringify(dados), { status });

function preparar(handler) {
    localStorage.setItem("token", "sessao");
    localStorage.setItem("usuario", JSON.stringify(usuario));
    const fetchMock = vi.fn(handler);
    vi.stubGlobal("fetch", fetchMock);
    render(<App />);
    return { user: userEvent.setup(), fetchMock };
}

test.each(["POST", "PUT", "DELETE"].flatMap(metodo => [[metodo, true], [metodo, false]]))(
    "ETA-26: %s pendente bloqueia Sair e libera ao retornar, sucesso=%s", async (metodo, sucesso) => {
        let resolver;
        const { user, fetchMock } = preparar((url, request) => {
            if (request.method !== "GET") return new Promise(resolve => { resolver = resolve; });
            if (url.includes("?arquivado=")) return json([quadro]);
            if (url.endsWith("/estrutura-etapas")) return json({ gerenciamento: quadro, etapas: [etapa], demandas: [] });
            return json(quadro);
        });
        await ativarPorTeclado(user, await screen.findByRole("button", { name: "Abrir Instalações" }));
        await ativarPorTeclado(user, await screen.findByRole("button", { name: metodo === "POST" ? "+ Nova etapa" : `${metodo === "PUT" ? "Editar" : "Remover"} etapa 1: Execução` }));
        if (metodo === "POST") {
            await user.type(screen.getByLabelText("Nome da etapa"), "Nova");
            await user.type(screen.getByLabelText("Setor responsável"), "Técnico");
        }
        const enviar = metodo === "DELETE"
            ? within(screen.getByRole("dialog")).getByRole("button", { name: "Remover", exact: true })
            : screen.getByRole("button", { name: "Salvar etapa" });
        await ativarPorTeclado(user, enviar);
        expect(screen.getByRole("button", { name: "Sair" }).disabled).toBe(true);
        await user.click(screen.getByRole("button", { name: "Sair" }));
        expect(localStorage.getItem("token")).toBe("sessao");
        expect(JSON.parse(localStorage.getItem("usuario"))).toEqual(usuario);
        expect(screen.queryByRole("heading", { name: "Bem-vindo" })).toBeNull();
        if (metodo === "DELETE") expect(screen.getByRole("dialog", { name: "Remover Execução?" })).not.toBeNull();
        else expect(screen.getByLabelText("Nome da etapa").value).toBe(metodo === "POST" ? "Nova" : "Execução");
        expect(fetchMock.mock.calls.filter(([, request]) => request.method === metodo)).toHaveLength(1);
        const salvo = { gerenciamento: { ...quadro, versao: 1 }, etapas: metodo === "DELETE" ? [] : [etapa], demandas: [] };
        await act(async () => resolver(sucesso ? json(salvo, metodo === "POST" ? 201 : 200)
            : json({ erro: "Não foi possível concluir a operação." }, 500)));
        await waitFor(() => expect(screen.getByRole("button", { name: "Sair" }).disabled).toBe(false));
        expect(localStorage.getItem("token")).toBe("sessao");
        if (!sucesso) {
            expect(screen.getByRole("alert").textContent).toBe("Não foi possível concluir a operação.");
            if (metodo === "DELETE") expect(screen.getByRole("dialog", { name: "Remover Execução?" })).not.toBeNull();
            else expect(screen.getByLabelText("Nome da etapa").value).toBe(metodo === "POST" ? "Nova" : "Execução");
        }
        await ativarPorTeclado(user, screen.getByRole("button", { name: "Sair" }));
        expect((await screen.findByRole("heading", { name: "Bem-vindo" })).textContent).toBe("Bem-vindo");
        expect(localStorage.getItem("token")).toBeNull();
        expect(localStorage.getItem("usuario")).toBeNull();
    }
);

test("ETA-26: consulta GET pendente permite Sair", async () => {
    const { user, fetchMock } = preparar(() => new Promise(() => {}));
    expect(screen.getByRole("status").textContent).toBe("Carregando gerenciamentos...");
    expect(screen.getByRole("button", { name: "Sair" }).disabled).toBe(false);
    await ativarPorTeclado(user, screen.getByRole("button", { name: "Sair" }));
    expect((await screen.findByRole("heading", { name: "Bem-vindo" })).textContent).toBe("Bem-vindo");
    expect(localStorage.getItem("token")).toBeNull();
    expect(fetchMock.mock.calls.map(([, request]) => request.method)).toEqual(["GET"]);
});
