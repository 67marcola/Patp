import { useState } from "react";
import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { expect, test, vi } from "vitest";
import CriarGerenciamento from "./CriarGerenciamento";
import { ativarPorTeclado } from "../test/keyboard";

test("GER-31: cancelar o formulário retorna sem enviar mutação", async () => {
    const fetchSpy = vi.fn();
    vi.stubGlobal("fetch", fetchSpy);
    function Fluxo() {
        const [formulario, setFormulario] = useState(true);
        return formulario
            ? <CriarGerenciamento voltar={() => setFormulario(false)} atualizar={() => {}} />
            : <h1>Quadro de instalações</h1>;
    }
    const user = userEvent.setup();
    render(<Fluxo />);
    await user.click(screen.getByRole("button", { name: "Cancelar" }));
    expect(screen.getByRole("heading", { name: "Quadro de instalações" }).textContent)
        .toBe("Quadro de instalações");
    expect(fetchSpy).not.toHaveBeenCalled();
});

const quadro = {
    id: 9, nome: "Instalações", descricao: "Postes",
    criador: { id: 3, nome: "Ana" }, arquivado: false, versao: 2, podeAdministrar: true
};
const json = (dados, status = 200) => new Response(JSON.stringify(dados), { status });

function preparar(gerenciamento = null, fetchMock = vi.fn().mockResolvedValue(json(quadro, gerenciamento ? 200 : 201))) {
    vi.stubGlobal("fetch", fetchMock);
    localStorage.setItem("token", "sessao");
    const atualizar = vi.fn();
    const voltar = vi.fn();
    const user = userEvent.setup();
    render(<CriarGerenciamento gerenciamento={gerenciamento} voltar={voltar} atualizar={atualizar} />);
    return { user, fetchMock, atualizar, voltar };
}

test("GER-06: criar sem descrição ou etapas envia nome normalizado e mostra conclusão", async () => {
    const { user, fetchMock, atualizar, voltar } = preparar();
    expect(screen.getByText("As etapas finais Concluídos e Cancelados são criadas automaticamente.").textContent)
        .toBe("As etapas finais Concluídos e Cancelados são criadas automaticamente.");
    await user.type(screen.getByLabelText("Nome do gerenciamento"), "  Instalações  ");
    await user.click(screen.getByRole("button", { name: "Salvar gerenciamento" }));
    await waitFor(() => expect(voltar).toHaveBeenCalledTimes(1));
    expect(JSON.parse(fetchMock.mock.calls[0][1].body)).toEqual({ nome: "Instalações", descricao: "", etapas: [] });
    expect(atualizar).toHaveBeenCalledWith(quadro);
});

test("FIN-18: criação explica finais automáticas e envia somente trabalhos mesmo com nomes homônimos", async () => {
    const { user, fetchMock, atualizar } = preparar();
    expect(screen.getByRole("heading", { name: "Etapas de trabalho iniciais" }).textContent).toBe("Etapas de trabalho iniciais");
    expect(screen.getByText("Opcional. Você pode criar o quadro sem etapas de trabalho.").textContent)
        .toBe("Opcional. Você pode criar o quadro sem etapas de trabalho.");
    expect(screen.getByText("As etapas finais Concluídos e Cancelados são criadas automaticamente.").textContent)
        .toBe("As etapas finais Concluídos e Cancelados são criadas automaticamente.");
    expect(screen.queryByLabelText("Nome da etapa 1")).toBeNull();
    await user.type(screen.getByLabelText("Nome do gerenciamento"), "Quadro");
    for (const [index, nome] of ["Concluídos", "Cancelados"].entries()) {
        await ativarPorTeclado(user, screen.getByRole("button", { name: /Adicionar etapa/ }));
        await user.type(screen.getByLabelText(`Nome da etapa ${index + 1}`), ` ${nome} `);
        await user.type(screen.getByLabelText(`Setor responsável da etapa ${index + 1}`), " Técnico ");
    }
    expect(screen.queryByLabelText("Nome da etapa 3")).toBeNull();
    await ativarPorTeclado(user, screen.getByRole("button", { name: "Salvar gerenciamento" }));
    await waitFor(() => expect(atualizar).toHaveBeenCalledWith(quadro));
    expect(JSON.parse(fetchMock.mock.calls[0][1].body)).toEqual({ nome: "Quadro", descricao: "", etapas: [
        { nome: "Concluídos", setor: "Técnico", ordem: 1 },
        { nome: "Cancelados", setor: "Técnico", ordem: 2 }
    ] });
});

test("GER-15/35: editar preenche campos e envia somente nome, descrição e versão", async () => {
    const { user, fetchMock, atualizar } = preparar(quadro);
    expect(screen.getByLabelText("Nome do gerenciamento").value).toBe("Instalações");
    expect(screen.getByLabelText("Descrição").value).toBe("Postes");
    expect(screen.queryByRole("button", { name: /Adicionar etapa/ })).toBeNull();
    await user.clear(screen.getByLabelText("Nome do gerenciamento"));
    await user.type(screen.getByLabelText("Nome do gerenciamento"), "Corrigido");
    await user.click(screen.getByRole("button", { name: "Salvar alterações" }));
    await waitFor(() => expect(atualizar).toHaveBeenCalledWith(quadro));
    expect(JSON.parse(fetchMock.mock.calls[0][1].body)).toEqual({ nome: "Corrigido", descricao: "Postes", versao: 2 });
    expect(fetchMock.mock.calls[0][1].method).toBe("PUT");
});

test.each([
    ["   ", "", "Informe um nome entre 1 e 120 caracteres."],
    ["a".repeat(121), "", "Informe um nome entre 1 e 120 caracteres."],
    ["Válido", "a".repeat(256), "A descrição deve ter até 255 caracteres."]
])("GER-03/04: entrada inválida exibe o limite e conserva valores", async (nome, descricao, mensagem) => {
    const { user, fetchMock } = preparar();
    fireEvent.change(screen.getByLabelText("Nome do gerenciamento"), { target: { value: nome } });
    fireEvent.change(screen.getByLabelText("Descrição"), { target: { value: descricao } });
    await user.click(screen.getByRole("button", { name: "Salvar gerenciamento" }));
    expect(screen.getByRole("alert").textContent).toBe(mensagem);
    expect(screen.getByLabelText("Nome do gerenciamento").value).toBe(nome);
    expect(screen.getByLabelText("Descrição").value).toBe(descricao);
    expect(fetchMock).not.toHaveBeenCalled();
});

test.each([["x", ""], ["a".repeat(120), "b".repeat(255)]])("GER-03/04: limites válidos são enviados sem truncar", async (nome, descricao) => {
    const { user, fetchMock, atualizar } = preparar();
    fireEvent.change(screen.getByLabelText("Nome do gerenciamento"), { target: { value: nome } });
    fireEvent.change(screen.getByLabelText("Descrição"), { target: { value: descricao } });
    await user.click(screen.getByRole("button", { name: "Salvar gerenciamento" }));
    await waitFor(() => expect(atualizar).toHaveBeenCalledWith(quadro));
    expect(JSON.parse(fetchMock.mock.calls[0][1].body)).toEqual({ nome, descricao, etapas: [] });
});

test("GER-07: etapas iniciais opcionais têm rótulos e podem ser removidas antes do envio", async () => {
    const { user, fetchMock, atualizar } = preparar();
    await user.type(screen.getByLabelText("Nome do gerenciamento"), "Quadro");
    await user.click(screen.getByRole("button", { name: /Adicionar etapa/ }));
    await user.click(screen.getByRole("button", { name: "Salvar gerenciamento" }));
    expect(screen.getByRole("alert").textContent).toBe("Preencha o nome e o setor das etapas com até 255 caracteres.");
    expect(fetchMock).not.toHaveBeenCalled();
    await user.type(screen.getByLabelText("Nome da etapa 1"), "Planejamento");
    await user.type(screen.getByLabelText("Setor responsável da etapa 1"), "Técnico");
    await user.click(screen.getByRole("button", { name: "Remover etapa 1" }));
    expect(screen.queryByLabelText("Nome da etapa 1")).toBeNull();
    await user.click(screen.getByRole("button", { name: "Salvar gerenciamento" }));
    await waitFor(() => expect(atualizar).toHaveBeenCalledWith(quadro));
    expect(JSON.parse(fetchMock.mock.calls[0][1].body).etapas).toEqual([]);
});

test("GER-07: configuração inicial válida mantém nome, setor e ordem", async () => {
    const { user, fetchMock, atualizar } = preparar();
    await user.type(screen.getByLabelText("Nome do gerenciamento"), "Quadro");
    await user.click(screen.getByRole("button", { name: /Adicionar etapa/ }));
    await user.type(screen.getByLabelText("Nome da etapa 1"), "Planejamento");
    await user.type(screen.getByLabelText("Setor responsável da etapa 1"), "Técnico");
    await user.click(screen.getByRole("button", { name: "Salvar gerenciamento" }));
    await waitFor(() => expect(atualizar).toHaveBeenCalledWith(quadro));
    expect(JSON.parse(fetchMock.mock.calls[0][1].body).etapas).toEqual([{ nome: "Planejamento", setor: "Técnico", ordem: 1 }]);
});

test("GER-31: cancelar edição conserva o quadro e não envia PUT", async () => {
    const { user, fetchMock, voltar } = preparar(quadro);
    await user.type(screen.getByLabelText("Nome do gerenciamento"), " rascunho");
    await user.click(screen.getByRole("button", { name: "Cancelar" }));
    expect(voltar).toHaveBeenCalledTimes(1);
    expect(fetchMock).not.toHaveBeenCalled();
});

test("GER-32: envios consecutivos enquanto pendente produzem uma mutação", async () => {
    let concluir;
    const pendente = new Promise(resolve => { concluir = resolve; });
    const { fetchMock, atualizar } = preparar(quadro, vi.fn().mockReturnValue(pendente));
    const form = screen.getByRole("button", { name: "Salvar alterações" }).closest("form");
    fireEvent.submit(form);
    fireEvent.submit(form);
    expect(fetchMock).toHaveBeenCalledTimes(1);
    expect(screen.getByRole("button", { name: "Salvando..." }).disabled).toBe(true);
    concluir(json({ ...quadro, versao: 3 }));
    await waitFor(() => expect(atualizar).toHaveBeenCalledWith({ ...quadro, versao: 3 }));
});

test.each([
    [400, "A descrição deve ter até 255 caracteres."],
    [403, "Você não tem permissão para administrar este gerenciamento."],
    [409, "Gerenciamento alterado por outro usuário. Atualize e tente novamente."]
])("GER-33/40: recusa HTTP %s preserva rascunho e anuncia erro", async (status, mensagem) => {
    const { user, atualizar, voltar } = preparar(quadro, vi.fn().mockResolvedValue(json({ erro: mensagem }, status)));
    await user.clear(screen.getByLabelText("Nome do gerenciamento"));
    await user.type(screen.getByLabelText("Nome do gerenciamento"), "Rascunho");
    await user.click(screen.getByRole("button", { name: "Salvar alterações" }));
    expect((await screen.findByRole("alert")).textContent).toBe(mensagem);
    expect(screen.getByLabelText("Nome do gerenciamento").value).toBe("Rascunho");
    expect(screen.getByLabelText("Descrição").value).toBe("Postes");
    expect(atualizar).not.toHaveBeenCalled();
    expect(voltar).not.toHaveBeenCalled();
});

test("GER-33: falha de rede preserva nome sem sucesso ou criação repetida", async () => {
    const { user, fetchMock, atualizar, voltar } = preparar(null, vi.fn().mockRejectedValue(new TypeError("offline")));
    await user.type(screen.getByLabelText("Nome do gerenciamento"), "Rascunho");
    await user.click(screen.getByRole("button", { name: "Salvar gerenciamento" }));
    expect((await screen.findByRole("alert")).textContent).toBe("Não foi possível confirmar a operação. Atualize a lista antes de tentar novamente.");
    expect(screen.getByLabelText("Nome do gerenciamento").value).toBe("Rascunho");
    expect(fetchMock).toHaveBeenCalledTimes(1);
    expect(atualizar).not.toHaveBeenCalled();
    expect(voltar).not.toHaveBeenCalled();
});

test("GER-38/39: teclado alcança campos rotulados e envia edição", async () => {
    const { user, atualizar } = preparar(quadro);
    await user.tab();
    expect(document.activeElement).toBe(screen.getByRole("button", { name: /Voltar/ }));
    await user.tab();
    expect(document.activeElement).toBe(screen.getByLabelText("Nome do gerenciamento"));
    await user.tab();
    expect(document.activeElement).toBe(screen.getByLabelText("Descrição"));
    await user.tab();
    expect(document.activeElement).toBe(screen.getByRole("button", { name: "Cancelar" }));
    await user.tab();
    expect(document.activeElement).toBe(screen.getByRole("button", { name: "Salvar alterações" }));
    await user.keyboard("{Enter}");
    await waitFor(() => expect(atualizar).toHaveBeenCalledWith(quadro));
});

test.each(["Nome da etapa 1", "Setor responsável da etapa 1"])("GER-07: %s com 256 caracteres é recusado sem envio", async campo => {
    const { user, fetchMock } = preparar();
    await user.type(screen.getByLabelText("Nome do gerenciamento"), "Quadro");
    await user.click(screen.getByRole("button", { name: /Adicionar etapa/ }));
    fireEvent.change(screen.getByLabelText("Nome da etapa 1"), { target: { value: "Etapa" } });
    fireEvent.change(screen.getByLabelText("Setor responsável da etapa 1"), { target: { value: "Setor" } });
    fireEvent.change(screen.getByLabelText(campo), { target: { value: "a".repeat(256) } });
    await user.click(screen.getByRole("button", { name: "Salvar gerenciamento" }));
    expect(screen.getByRole("alert").textContent).toBe("Preencha o nome e o setor das etapas com até 255 caracteres.");
    expect(screen.getByLabelText(campo).value).toBe("a".repeat(256));
    expect(fetchMock).not.toHaveBeenCalled();
});

test("GER-07: nome e setor iniciais com 255 caracteres são aceitos sem truncar", async () => {
    const { user, fetchMock, atualizar } = preparar();
    await user.type(screen.getByLabelText("Nome do gerenciamento"), "Quadro");
    await user.click(screen.getByRole("button", { name: /Adicionar etapa/ }));
    fireEvent.change(screen.getByLabelText("Nome da etapa 1"), { target: { value: "a".repeat(255) } });
    fireEvent.change(screen.getByLabelText("Setor responsável da etapa 1"), { target: { value: "b".repeat(255) } });
    await user.click(screen.getByRole("button", { name: "Salvar gerenciamento" }));
    await waitFor(() => expect(atualizar).toHaveBeenCalledWith(quadro));
    expect(JSON.parse(fetchMock.mock.calls[0][1].body).etapas).toEqual([{ nome: "a".repeat(255), setor: "b".repeat(255), ordem: 1 }]);
});

test("GER-38/39: teclado preenche e remove uma etapa inicial antes de salvar", async () => {
    const { user, fetchMock, atualizar } = preparar();
    await user.tab();
    expect(document.activeElement).toBe(screen.getByRole("button", { name: /Voltar/ }));
    await user.tab();
    expect(document.activeElement).toBe(screen.getByLabelText("Nome do gerenciamento"));
    await user.keyboard("Quadro");
    await user.tab();
    expect(document.activeElement).toBe(screen.getByLabelText("Descrição"));
    await user.keyboard("Descrição por teclado");
    await user.tab();
    expect(document.activeElement).toBe(screen.getByRole("button", { name: /Adicionar etapa/ }));
    await user.keyboard("{Enter}");
    await user.tab();
    expect(document.activeElement).toBe(screen.getByLabelText("Nome da etapa 1"));
    await user.keyboard("Etapa temporária");
    expect(screen.getByLabelText("Nome da etapa 1").value).toBe("Etapa temporária");
    await user.tab();
    expect(document.activeElement).toBe(screen.getByLabelText("Setor responsável da etapa 1"));
    await user.keyboard("Técnico");
    expect(screen.getByLabelText("Setor responsável da etapa 1").value).toBe("Técnico");
    await user.tab();
    expect(document.activeElement).toBe(screen.getByRole("button", { name: "Remover etapa 1" }));
    await user.keyboard("{Enter}");
    expect(screen.queryByLabelText("Nome da etapa 1")).toBeNull();
    expect(screen.queryByLabelText("Setor responsável da etapa 1")).toBeNull();
    expect(fetchMock).not.toHaveBeenCalled();
    await ativarPorTeclado(user, screen.getByRole("button", { name: "Salvar gerenciamento" }));
    await waitFor(() => expect(atualizar).toHaveBeenCalledWith(quadro));
    expect(JSON.parse(fetchMock.mock.calls[0][1].body)).toEqual({ nome: "Quadro", descricao: "Descrição por teclado", etapas: [] });
});

test.each([/Voltar/, "Cancelar"])("GER-31/38: sair do formulário por teclado não envia mutação (%s)", async nome => {
    const { user, fetchMock, voltar } = preparar(quadro);
    await ativarPorTeclado(user, screen.getByRole("button", { name: nome }));
    expect(voltar).toHaveBeenCalledTimes(1);
    expect(fetchMock).not.toHaveBeenCalled();
});
