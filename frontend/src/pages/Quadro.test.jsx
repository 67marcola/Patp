import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { expect, test, vi } from "vitest";
import Quadro from "./Quadro";
import { ativarPorTeclado } from "../test/keyboard";

const quadro = { id: 9, nome: "Instalações", descricao: "Postes", arquivado: true, versao: 3, podeAdministrar: true };
const etapas = [{ id: 4, nome: "Planejamento", setor: "Técnico", ordem: 1 }];
const json = (dados, status = 200) => new Response(JSON.stringify(dados), { status });

function preparar(handler) {
    const fetchMock = vi.fn(handler);
    vi.stubGlobal("fetch", fetchMock);
    localStorage.setItem("token", "sessao");
    const voltar = vi.fn();
    render(<Quadro gerenciamento={quadro} voltar={voltar} />);
    return { user: userEvent.setup(), fetchMock, voltar };
}

test("GER-29/38: arquivado indica consulta durante carregamento e permite voltar por teclado", async () => {
    const { user, fetchMock, voltar } = preparar(() => new Promise(() => {}));
    expect(screen.getByText("Arquivado — somente consulta").textContent).toBe("Arquivado — somente consulta");
    expect(screen.getByRole("status").textContent).toBe("Carregando quadro...");
    expect(screen.queryByRole("button", { name: /Criar processo/ })).toBeNull();
    await user.tab();
    expect(document.activeElement).toBe(screen.getByRole("button", { name: /Voltar/ }));
    await user.keyboard("{Enter}");
    expect(voltar).toHaveBeenCalledTimes(1);
    expect(fetchMock.mock.calls.every(([, request]) => request.method === "GET")).toBe(true);
});

test("GER-29: consulta arquivada preserva nome, descrição, etapas e setor sem mutação", async () => {
    const { fetchMock } = preparar(() => json({ gerenciamento: quadro, etapas }));
    expect((await screen.findByRole("heading", { name: "Planejamento" })).textContent).toBe("Planejamento");
    expect(screen.getByRole("heading", { name: "Instalações" }).textContent).toBe("Instalações");
    expect(screen.getByText("Postes").textContent).toBe("Postes");
    expect(screen.getByText("Setor responsável: Técnico").textContent).toBe("Setor responsável: Técnico");
    expect(screen.queryByRole("button", { name: /Criar processo/ })).toBeNull();
    expect(fetchMock.mock.calls[0][0]).toBe("http://localhost:8081/api/gerenciamentos/9/estrutura-etapas");
    expect(fetchMock.mock.calls.every(([, request]) => request.method === "GET")).toBe(true);
});

test.each([
    [500, "Não foi possível consultar as etapas."],
    [404, "Gerenciamento não encontrado."],
    [0, "Não foi possível confirmar a operação. Atualize a lista antes de tentar novamente."]
])("GER-40: erro %s é anunciado e repetir carrega somente as etapas", async (status, mensagem) => {
    let falhar = true;
    const { user, fetchMock } = preparar(() => {
        if (!falhar) return json({ gerenciamento: quadro, etapas });
        return status === 0 ? Promise.reject(new TypeError("offline")) : json({ erro: mensagem }, status);
    });
    expect((await screen.findByRole("alert")).textContent).toBe(mensagem);
    expect(screen.getByText("Arquivado — somente consulta").textContent).toBe("Arquivado — somente consulta");
    falhar = false;
    await user.click(screen.getByRole("button", { name: "Tentar novamente" }));
    expect((await screen.findByRole("heading", { name: "Planejamento" })).textContent).toBe("Planejamento");
    expect(screen.queryByRole("alert")).toBeNull();
    expect(fetchMock).toHaveBeenCalledTimes(2);
    expect(fetchMock.mock.calls.every(([, request]) => request.method === "GET")).toBe(true);
});

test("GER-38/40: teclado repete consulta do quadro sem mutação", async () => {
    let falhar = true;
    const { user, fetchMock } = preparar(() => falhar
        ? json({ erro: "Não foi possível consultar as etapas." }, 500) : json({ gerenciamento: quadro, etapas }));
    expect((await screen.findByRole("alert")).textContent).toBe("Não foi possível consultar as etapas.");
    falhar = false;
    await ativarPorTeclado(user, screen.getByRole("button", { name: "Tentar novamente" }));
    expect((await screen.findByRole("heading", { name: "Planejamento" })).textContent).toBe("Planejamento");
    expect(screen.queryByRole("alert")).toBeNull();
    expect(fetchMock.mock.calls.map(([url, request]) => [url, request.method])).toEqual([
        ["http://localhost:8081/api/gerenciamentos/9/estrutura-etapas", "GET"],
        ["http://localhost:8081/api/gerenciamentos/9/estrutura-etapas", "GET"]
    ]);
});
