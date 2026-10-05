import { act, fireEvent, render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { expect, test, vi } from "vitest";
import EditorEtapa from "./EditorEtapa";
import { ApiError } from "../services/api";
import { ativarPorTeclado } from "../test/keyboard";

const etapa = { id: 4, nome: "Planejamento", setor: "Técnico", ordem: 2 };

function preparar(props = {}) {
    const salvar = vi.fn().mockResolvedValue(undefined);
    const cancelar = vi.fn();
    const aoErro = vi.fn();
    render(<EditorEtapa quantidadeEtapas={3} salvar={salvar} cancelar={cancelar} aoErro={aoErro} {...props} />);
    return { user: userEvent.setup(), salvar: props.salvar || salvar, cancelar, aoErro };
}

function campos(nome, setor, ordem = "1") {
    fireEvent.change(screen.getByLabelText("Nome da etapa"), { target: { value: nome } });
    fireEvent.change(screen.getByLabelText("Setor responsável"), { target: { value: setor } });
    fireEvent.change(screen.getByLabelText("Posição"), { target: { value: ordem } });
}

test.each([null, etapa, { ...etapa, setor: null }])("ETA-23: criar/editar mostra valores e somente posições válidas, etapa=%s", atual => {
    preparar({ etapa: atual });
    expect(screen.getByLabelText("Nome da etapa").value).toBe(atual?.nome || "");
    expect(screen.getByLabelText("Setor responsável").value).toBe(atual?.setor || "");
    expect(screen.getByLabelText("Posição").value).toBe(String(atual?.ordem || 4));
    expect([...screen.getByLabelText("Posição").options].map(option => option.value))
        .toEqual(atual ? ["1", "2", "3"] : ["1", "2", "3", "4"]);
    expect(document.activeElement).toBe(screen.getByLabelText("Nome da etapa"));
});

test("ETA-23: quadro vazio oferece apenas posição1", () => {
    preparar({ quantidadeEtapas: 0 });
    expect([...screen.getByLabelText("Posição").options].map(option => option.value)).toEqual(["1"]);
    expect(screen.getByLabelText("Setor responsável").value).toBe("");
});

test.each([
    ["", "Técnico", "Informe um nome de etapa entre 1 e 255 caracteres."],
    [" \u00a0\ufeff ", "Técnico", "Informe um nome de etapa entre 1 e 255 caracteres."],
    ["a".repeat(256), "Técnico", "Informe um nome de etapa entre 1 e 255 caracteres."],
    ["😀".repeat(128), "Técnico", "Informe um nome de etapa entre 1 e 255 caracteres."],
    ["Etapa", "", "Informe um setor entre 1 e 255 caracteres."],
    ["Etapa", " \u00a0\ufeff ", "Informe um setor entre 1 e 255 caracteres."],
    ["Etapa", "a".repeat(256), "Informe um setor entre 1 e 255 caracteres."],
    ["Etapa", "😀".repeat(128), "Informe um setor entre 1 e 255 caracteres."]
])("ETA-09/10/27: campos inválidos conservam o rascunho e não gravam", async (nome, setor, mensagem) => {
    const { user, salvar } = preparar();
    campos(nome, setor);
    await user.click(screen.getByRole("button", { name: "Salvar etapa" }));
    expect(screen.getByRole("alert").textContent).toBe(mensagem);
    expect(screen.getByLabelText("Nome da etapa").value).toBe(nome);
    expect(screen.getByLabelText("Setor responsável").value).toBe(setor);
    expect(salvar).not.toHaveBeenCalled();
});

test.each(["Á", "á".repeat(255)])("ETA-09/10/23: aceita limites normalizados e envia posição numérica", async texto => {
    const { user, salvar } = preparar();
    campos(` \u00a0${texto}\ufeff `, `\ufeff${texto}\u00a0`, "2");
    await ativarPorTeclado(user, screen.getByRole("button", { name: "Salvar etapa" }));
    expect(salvar).toHaveBeenCalledExactlyOnceWith({ nome: texto, setor: texto, ordem: 2 });
    expect(screen.queryByRole("alert")).toBeNull();
});

test("ETA-11: posição fora da seleção é recusada sem envio", async () => {
    const { user, salvar } = preparar();
    campos("Etapa", "Técnico", "9");
    await user.click(screen.getByRole("button", { name: "Salvar etapa" }));
    expect(screen.getByRole("alert").textContent).toBe("Escolha uma posição válida para a etapa.");
    expect(salvar).not.toHaveBeenCalled();
});

test("ETA-23/30: teclado alcança campos, seleção e salvar sem perder valores", async () => {
    const { user, salvar } = preparar();
    await user.keyboard("Análise");
    await user.tab();
    expect(document.activeElement).toBe(screen.getByLabelText("Setor responsável"));
    await user.keyboard("Engenharia");
    await user.tab();
    expect(document.activeElement).toBe(screen.getByLabelText("Posição"));
    await user.selectOptions(screen.getByLabelText("Posição"), "2");
    await ativarPorTeclado(user, screen.getByRole("button", { name: "Salvar etapa" }));
    expect(salvar).toHaveBeenCalledExactlyOnceWith({ nome: "Análise", setor: "Engenharia", ordem: 2 });
});

test("ETA-25/30: cancelar por teclado chama retorno sem gravação", async () => {
    const { user, salvar, cancelar } = preparar({ etapa });
    await ativarPorTeclado(user, screen.getByRole("button", { name: "Cancelar" }));
    expect(cancelar).toHaveBeenCalledTimes(1);
    expect(salvar).not.toHaveBeenCalled();
});

test("ETA-26: envio pendente impede duplicação, mudança dos campos e cancelamento", async () => {
    let resolver;
    const salvar = vi.fn(() => new Promise(resolve => { resolver = resolve; }));
    const { user, cancelar } = preparar({ etapa, salvar });
    await user.dblClick(screen.getByRole("button", { name: "Salvar etapa" }));
    expect(salvar).toHaveBeenCalledExactlyOnceWith({ nome: "Planejamento", setor: "Técnico", ordem: 2 });
    for (const label of ["Nome da etapa", "Setor responsável", "Posição"]) expect(screen.getByLabelText(label).disabled).toBe(true);
    expect(screen.getByRole("button", { name: "Salvando..." }).disabled).toBe(true);
    await user.click(screen.getByRole("button", { name: "Cancelar" }));
    expect(cancelar).not.toHaveBeenCalled();
    await act(async () => resolver());
    await waitFor(() => expect(screen.getByRole("button", { name: "Salvar etapa" }).disabled).toBe(false));
});

test.each([
    [403, "Você não tem permissão para administrar este gerenciamento."],
    [409, "Gerenciamento alterado por outro usuário. Atualize e tente novamente."],
    [500, "Não foi possível concluir a operação."],
    [0, "Não foi possível confirmar a operação. Atualize a lista antes de tentar novamente."]
])("ETA-27: erro%s anuncia mensagem exata e conserva todos os campos", async (status, mensagem) => {
    const error = new ApiError(mensagem, status);
    const salvar = vi.fn().mockRejectedValue(error);
    const { user, aoErro } = preparar({ salvar });
    campos("Rascunho", "Engenharia", "2");
    await ativarPorTeclado(user, screen.getByRole("button", { name: "Salvar etapa" }));
    expect((await screen.findByRole("alert")).textContent).toBe(mensagem);
    expect(screen.getByLabelText("Nome da etapa").value).toBe("Rascunho");
    expect(screen.getByLabelText("Setor responsável").value).toBe("Engenharia");
    expect(screen.getByLabelText("Posição").value).toBe("2");
    expect(salvar).toHaveBeenCalledTimes(1);
    expect(aoErro).toHaveBeenCalledExactlyOnceWith(error);
});
