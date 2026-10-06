import { act, fireEvent, render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { expect, test, vi } from "vitest";
import EditorDemanda from "./EditorDemanda";
import { ApiError } from "../services/api";
import { ativarPorTeclado } from "../test/keyboard";

const labels = { numeroProcesso: "Número da demanda", pessoa: "Cliente/solicitante", responsavel: "Responsável",
    prioridade: "Prioridade", dataEmissao: "Data de emissão", prazoEtapa: "Prazo da etapa",
    prazoGeral: "Prazo geral", observacoes: "Observações" };
const completo = { numeroProcesso: "D-42", pessoa: "Maria", responsavel: "Ana", prioridade: "Livre e urgente",
    dataEmissao: "2020-02-29", prazoEtapa: "2019-01-01", prazoGeral: "2018-01-01", observacoes: "Uma\nOutra" };
const minimo = { numeroProcesso: "D-1", pessoa: "João", responsavel: null, prioridade: null,
    dataEmissao: null, prazoEtapa: null, prazoGeral: null, observacoes: null };

function preparar(props = {}) {
    const salvar = vi.fn().mockResolvedValue(undefined);
    const cancelar = vi.fn();
    const aoErro = vi.fn();
    render(<EditorDemanda salvar={salvar} cancelar={cancelar} aoErro={aoErro} {...props} />);
    return { user: userEvent.setup(), salvar: props.salvar || salvar, cancelar, aoErro };
}

function preencher(dados) {
    for (const [campo, valor] of Object.entries(dados)) {
        fireEvent.change(screen.getByLabelText(labels[campo]), { target: { value: valor ?? "" } });
    }
}

function conferirRascunho(dados) {
    for (const [campo, valor] of Object.entries(dados)) expect(screen.getByLabelText(labels[campo]).value).toBe(valor);
}

test("CAD-28/40: oito campos vazios, dois obrigatórios e foco inicial sem controles do servidor", () => {
    preparar();
    expect(screen.getByRole("heading", { name: "Novo processo" }).textContent).toBe("Novo processo");
    for (const [campo, label] of Object.entries(labels)) {
        const controle = screen.getByLabelText(label);
        expect(controle.value).toBe("");
        expect(controle.required).toBe(campo === "numeroProcesso" || campo === "pessoa");
    }
    expect(document.activeElement).toBe(screen.getByLabelText("Número da demanda"));
    expect(screen.getByLabelText("Prioridade").type).toBe("text");
    for (const campo of ["dataEmissao", "prazoEtapa", "prazoGeral"]) expect(screen.getByLabelText(labels[campo]).type).toBe("date");
    expect(screen.getByLabelText("Observações").tagName).toBe("TEXTAREA");
    expect(screen.queryByRole("combobox")).toBeNull();
    expect(screen.queryByLabelText(/Status|Etapa inicial|Data de conclusão|Data de cancelamento|Motivo/)).toBeNull();
    expect(screen.getByText("A demanda começará automaticamente na primeira etapa de trabalho.").textContent)
        .toBe("A demanda começará automaticamente na primeira etapa de trabalho.");
    expect(screen.getByText("Número e cliente/solicitante são obrigatórios; os demais campos são opcionais.").textContent)
        .toBe("Número e cliente/solicitante são obrigatórios; os demais campos são opcionais.");
});

test("CAD-02/03/04/30: mínimo normaliza Unicode e envia oito campos sem datas inventadas", async () => {
    const { user, salvar } = preparar();
    preencher({ numeroProcesso: " \u00a0D-1\ufeff ", pessoa: " \ufeffJoão\u00a0 " });
    await ativarPorTeclado(user, screen.getByRole("button", { name: "Criar demanda" }));
    expect(salvar).toHaveBeenCalledExactlyOnceWith(minimo);
    expect(screen.queryByRole("alert")).toBeNull();
});

test("CAD-03/04/30: completo preserva prioridade livre, datas passadas/invertidas e linhas", async () => {
    const { user, salvar } = preparar();
    preencher({ ...completo, numeroProcesso: " D-42 ", pessoa: " Maria ", responsavel: " Ana ",
        prioridade: " Livre e urgente ", observacoes: " \nUma\nOutra \n" });
    await user.click(screen.getByRole("button", { name: "Criar demanda" }));
    expect(salvar).toHaveBeenCalledExactlyOnceWith(completo);
});

test("CAD-03/30: textos opcionais em branco viram null", async () => {
    const { user, salvar } = preparar();
    preencher({ ...minimo, responsavel: " \u00a0\ufeff ", prioridade: " \ufeff ", observacoes: " \n\u00a0 " });
    await user.click(screen.getByRole("button", { name: "Criar demanda" }));
    expect(salvar).toHaveBeenCalledExactlyOnceWith(minimo);
});

test("CAD-02/03/29: aceita limites UTF-16 após trim de todos os textos", async () => {
    const { user, salvar } = preparar();
    const dados = { numeroProcesso: "😀".repeat(127) + "a", pessoa: "á".repeat(255), responsavel: "r".repeat(255),
        prioridade: "p".repeat(255), observacoes: "o".repeat(10000), dataEmissao: null, prazoEtapa: null, prazoGeral: null };
    preencher(Object.fromEntries(Object.entries(dados).map(([campo, valor]) => [campo, valor === null ? "" : ` ${valor} `])));
    await user.click(screen.getByRole("button", { name: "Criar demanda" }));
    expect(salvar).toHaveBeenCalledExactlyOnceWith(dados);
});

const invalidos = [
    ...["", " \u00a0\ufeff ", "a".repeat(256), "😀".repeat(128)].map(valor =>
        ["numeroProcesso", valor, "Informe um número de demanda entre 1 e 255 caracteres."]),
    ...["", " \u00a0\ufeff ", "a".repeat(256), "😀".repeat(128)].map(valor =>
        ["pessoa", valor, "Informe um cliente/solicitante entre 1 e 255 caracteres."]),
    ...["a".repeat(256), "😀".repeat(128)].map(valor => ["responsavel", valor, "Informe um responsável com até 255 caracteres."]),
    ...["a".repeat(256), "😀".repeat(128)].map(valor => ["prioridade", valor, "Informe uma prioridade com até 255 caracteres."]),
    ["observacoes", "a".repeat(10001), "Informe observações com até 10000 caracteres."]
];
test.each(invalidos)("CAD-10/29: %s inválido anuncia mensagem, conserva todos os campos e não grava", async (campo, valor, mensagem) => {
    const { user, salvar, aoErro } = preparar();
    const dados = { ...completo, [campo]: valor };
    preencher(dados);
    await user.click(screen.getByRole("button", { name: "Criar demanda" }));
    expect(screen.getByRole("alert").textContent).toBe(mensagem);
    conferirRascunho(dados);
    expect(salvar).not.toHaveBeenCalled();
    expect(aoErro).not.toHaveBeenCalled();
});

test("CAD-28/30/40: teclado percorre os oito campos e envia os valores completos", async () => {
    const { user, salvar } = preparar();
    const valores = { ...completo, dataEmissao: "", prazoEtapa: "", prazoGeral: "" };
    const entradas = Object.entries(labels);
    for (const [index, [campo, label]] of entradas.entries()) {
        expect(document.activeElement).toBe(screen.getByLabelText(label));
        if (valores[campo]) await user.keyboard(valores[campo]);
        if (index < entradas.length - 1) await user.tab();
    }
    await ativarPorTeclado(user, screen.getByRole("button", { name: "Criar demanda" }));
    expect(salvar).toHaveBeenCalledExactlyOnceWith({ ...completo, dataEmissao: null, prazoEtapa: null, prazoGeral: null });
});

test("CAD-40: Cancelar por teclado não grava", async () => {
    const { user, salvar, cancelar } = preparar();
    preencher(completo);
    await ativarPorTeclado(user, screen.getByRole("button", { name: "Cancelar" }));
    expect(cancelar).toHaveBeenCalledTimes(1);
    expect(salvar).not.toHaveBeenCalled();
});

test("CAD-31: envio pendente impede duplicação, campos e cancelamento", async () => {
    let resolver;
    const salvar = vi.fn(() => new Promise(resolve => { resolver = resolve; }));
    const { user, cancelar } = preparar({ salvar });
    preencher(completo);
    await user.dblClick(screen.getByRole("button", { name: "Criar demanda" }));
    fireEvent.submit(screen.getByRole("form", { name: "Novo processo" }));
    expect(salvar).toHaveBeenCalledExactlyOnceWith(completo);
    expect(screen.getByRole("form").getAttribute("aria-busy")).toBe("true");
    for (const label of Object.values(labels)) expect(screen.getByLabelText(label).disabled).toBe(true);
    expect(screen.getByRole("button", { name: "Criando..." }).disabled).toBe(true);
    expect(screen.getByRole("button", { name: "Cancelar" }).disabled).toBe(true);
    await user.click(screen.getByRole("button", { name: "Cancelar" }));
    expect(cancelar).not.toHaveBeenCalled();
    await act(async () => resolver());
    await waitFor(() => expect(screen.getByRole("button", { name: "Criar demanda" }).disabled).toBe(false));
});

test("CAD-31/33: bloqueio externo impede submit forçado e permite cancelar após erro", async () => {
    const { user, salvar, cancelar } = preparar({ bloqueado: true });
    expect(screen.getByRole("button", { name: "Criar demanda" }).disabled).toBe(true);
    for (const label of Object.values(labels)) expect(screen.getByLabelText(label).disabled).toBe(true);
    fireEvent.submit(screen.getByRole("form", { name: "Novo processo" }));
    expect(salvar).not.toHaveBeenCalled();
    await user.click(screen.getByRole("button", { name: "Cancelar" }));
    expect(cancelar).toHaveBeenCalledTimes(1);
});

test.each([
    [400, "Dados da requisição inválidos."], [401, "Usuário não autenticado."], [404, "Gerenciamento não encontrado."],
    [409, "Já existe uma demanda com esse número."], [500, "Não foi possível concluir a operação."],
    [0, "Não foi possível confirmar a operação. Atualize a lista antes de tentar novamente."],
    [201, "Não foi possível confirmar a operação. Atualize a lista antes de tentar novamente."]
])("CAD-33: erro%s anuncia mensagem e preserva os oito campos", async (status, mensagem) => {
    const erro = new ApiError(mensagem, status);
    const salvar = vi.fn().mockRejectedValue(erro);
    const { user, aoErro } = preparar({ salvar });
    preencher(completo);
    await user.click(screen.getByRole("button", { name: "Criar demanda" }));
    expect((await screen.findByRole("alert")).textContent).toBe(mensagem);
    conferirRascunho(completo);
    expect(salvar).toHaveBeenCalledExactlyOnceWith(completo);
    expect(aoErro).toHaveBeenCalledExactlyOnceWith(erro);
});
