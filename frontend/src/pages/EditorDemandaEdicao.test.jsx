import { act, fireEvent, render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { expect, test, vi } from "vitest";
import EditorDemanda from "./EditorDemanda";
import { ativarPorTeclado } from "../test/keyboard";

const demanda = { numeroProcesso: "P-1", pessoa: "João", responsavel: "Ana", prioridade: null,
    dataEmissao: "2020-02-29", prazoEtapa: null, prazoGeral: "2019-01-01", observacoes: "Anterior",
    status: "Cancelado", dataConclusao: null, dataCancelamento: "2021-03-04", motivoCancelamento: "Anterior" };
const labels = { pessoa: "Cliente/solicitante", responsavel: "Responsável", prioridade: "Prioridade",
    dataEmissao: "Data de emissão", prazoEtapa: "Prazo da etapa", prazoGeral: "Prazo geral", observacoes: "Observações" };
function preparar(props = {}) {
    const salvar = vi.fn().mockResolvedValue(undefined); const cancelar = vi.fn(); const aoErro = vi.fn();
    render(<EditorDemanda demanda={demanda} salvar={salvar} cancelar={cancelar} aoErro={aoErro} {...props} />);
    return { user: userEvent.setup(), salvar: props.salvar || salvar, cancelar, aoErro };
}
test("EDI-20/27/28: dados atuais, número somente leitura, foco no cliente e ciclo fora do formulário", () => {
    preparar(); expect(screen.getByRole("heading", { name: "Editar demanda" }).textContent).toBe("Editar demanda");
    for (const [campo, label] of Object.entries(labels)) expect(screen.getByLabelText(label).value).toBe(demanda[campo] ?? "");
    const numero = screen.getByLabelText("Número da demanda"); expect(numero.value).toBe("P-1"); expect(numero.readOnly).toBe(true);
    expect(document.activeElement).toBe(screen.getByLabelText("Cliente/solicitante"));
    expect(screen.queryByLabelText(/Status|Etapa de destino|Data de conclusão|Data de cancelamento|Motivo/)).toBeNull();
});
test("EDI-05/08/20/27: envia somente informações, normaliza e permite retirar opcionais", async () => {
    const { user, salvar } = preparar();
    fireEvent.change(screen.getByLabelText("Cliente/solicitante"), { target: { value: "  Maria  " } });
    for (const [campo, label] of Object.entries(labels)) if (campo !== "pessoa") fireEvent.change(screen.getByLabelText(label), { target: { value: "" } });
    await ativarPorTeclado(user, screen.getByRole("button", { name: "Salvar alterações" }));
    expect(salvar).toHaveBeenCalledExactlyOnceWith({ pessoa: "Maria", responsavel: null, prioridade: null,
        dataEmissao: null, prazoEtapa: null, prazoGeral: null, observacoes: null });
    expect(screen.getByLabelText("Número da demanda").value).toBe("P-1");
});
test("EDI-05/07: limites exatos e datas invertidas continuam aceitos", async () => {
    const { user, salvar } = preparar();
    const dados = { pessoa: "😀".repeat(127) + "a", responsavel: "r".repeat(255), prioridade: "p".repeat(255),
        dataEmissao: "2020-02-29", prazoEtapa: null, prazoGeral: "2019-01-01", observacoes: "😀".repeat(5000) };
    for (const [campo, valor] of Object.entries(dados)) fireEvent.change(screen.getByLabelText(labels[campo]), { target: { value: valor ?? "" } });
    await user.click(screen.getByRole("button", { name: "Salvar alterações" })); expect(salvar).toHaveBeenCalledExactlyOnceWith(dados);
});
const invalidos = [
    ["pessoa", "  ", "Informe um cliente/solicitante entre 1 e 255 caracteres."],
    ["pessoa", "😀".repeat(128), "Informe um cliente/solicitante entre 1 e 255 caracteres."],
    ["responsavel", "a".repeat(256), "Informe um responsável com até 255 caracteres."],
    ["prioridade", "😀".repeat(128), "Informe uma prioridade com até 255 caracteres."],
    ["observacoes", "a".repeat(10001), "Informe observações com até 10000 caracteres."]
];
test.each(invalidos)("EDI-06/07: %s inválido conserva rascunho e impede envio", async (campo, valor, mensagem) => {
    const { user, salvar } = preparar(); fireEvent.change(screen.getByLabelText(labels[campo]), { target: { value: valor } });
    await user.click(screen.getByRole("button", { name: "Salvar alterações" }));
    expect(screen.getByRole("alert").textContent).toBe(mensagem); expect(screen.getByLabelText(labels[campo]).value).toBe(valor);
    expect(salvar).not.toHaveBeenCalled();
});
test("EDI-21: cancelar não envia dados", async () => {
    const { user, salvar, cancelar } = preparar(); await user.click(screen.getByRole("button", { name: "Cancelar", exact: true }));
    expect(cancelar).toHaveBeenCalledTimes(1); expect(salvar).not.toHaveBeenCalled();
});
test("EDI-22: pending impede duplicação e cancelamento", async () => {
    let resolver; const salvar = vi.fn(() => new Promise(resolve => { resolver = resolve; }));
    const { user, cancelar } = preparar({ salvar }); await user.click(screen.getByRole("button", { name: "Salvar alterações" }));
    for (const label of ["Número da demanda", ...Object.values(labels)]) expect(screen.getByLabelText(label).disabled).toBe(true);
    expect(screen.getByRole("button", { name: "Salvando..." }).disabled).toBe(true);
    const b = screen.getByRole("button", { name: "Cancelar", exact: true }); expect(b.disabled).toBe(true);
    await user.click(b); fireEvent.submit(screen.getByRole("form", { name: "Editar demanda" }));
    expect(salvar).toHaveBeenCalledTimes(1); expect(cancelar).not.toHaveBeenCalled();
    await act(async () => resolver());
});
test("EDI-23: erro conserva valores e avisa o quadro", async () => {
    const erro = new Error("Conflito"); const salvar = vi.fn().mockRejectedValue(erro); const { user, aoErro } = preparar({ salvar });
    fireEvent.change(screen.getByLabelText("Cliente/solicitante"), { target: { value: "  Corrigir  " } });
    await user.click(screen.getByRole("button", { name: "Salvar alterações" }));
    await waitFor(() => expect(screen.getByRole("alert").textContent).toBe("Conflito"));
    expect(screen.getByLabelText("Cliente/solicitante").value).toBe("  Corrigir  "); expect(aoErro).toHaveBeenCalledExactlyOnceWith(erro);
    expect(salvar).toHaveBeenCalledTimes(1);
});
