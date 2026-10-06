import { act, fireEvent, render, screen, waitFor, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { expect, test, vi } from "vitest";
import AcaoDemanda from "./AcaoDemanda";
import { ativarPorTeclado } from "../test/keyboard";

const demanda = { id: 21, numeroProcesso: "Poste-1", pessoa: "João", etapaId: 11 };
const etapas = [{ id: 11, nome: "Comprar", categoria: "TRABALHO" }, { id: 12, nome: "Instalar", categoria: "TRABALHO" },
    { id: 13, nome: "Concluídos", categoria: "CONCLUIDA" }, { id: 14, nome: "Cancelados", categoria: "CANCELADA" }];
const botoes = { mover: "Mover demanda", reabrir: "Reabrir demanda", concluir: "Confirmar conclusão", cancelar: "Confirmar cancelamento" };
function preparar(acao, props = {}) {
    const salvar = vi.fn().mockResolvedValue(undefined); const cancelar = vi.fn(); const aoErro = vi.fn();
    const view = render(<AcaoDemanda acao={acao} demanda={demanda} etapas={etapas} salvar={salvar} cancelar={cancelar} aoErro={aoErro} {...props} />);
    return { salvar: props.salvar || salvar, cancelar, aoErro, user: userEvent.setup(), ...view };
}

test.each(["mover", "reabrir"])("MOV-29: %s oferece somente trabalhos e foca a seleção", acao => {
    preparar(acao); const select = screen.getByLabelText("Etapa de destino");
    expect(document.activeElement).toBe(select);
    expect(within(select).getAllByRole("option").map(o => [o.value, o.textContent]))
        .toEqual(acao === "mover" ? [["", "Escolha uma etapa"], ["12", "Instalar"]]
            : [["", "Escolha uma etapa"], ["11", "Comprar"], ["12", "Instalar"]]);
    expect(screen.getByRole("dialog").getAttribute("aria-labelledby")).toBe("acao-demanda-titulo");
});

test.each(["mover", "reabrir"])("MOV-29: %s exige seleção e envia ID escolhido por teclado", async acao => {
    const { user, salvar } = preparar(acao);
    await user.click(screen.getByRole("button", { name: botoes[acao] }));
    expect(screen.getByRole("alert").textContent).toBe("Escolha uma etapa de trabalho."); expect(salvar).not.toHaveBeenCalled();
    await user.selectOptions(screen.getByLabelText("Etapa de destino"), "12");
    await ativarPorTeclado(user, screen.getByRole("button", { name: botoes[acao] }));
    expect(salvar).toHaveBeenCalledExactlyOnceWith({ etapaId: 12 });
});

test.each(["concluir", "cancelar"])("MOV-30: %s mostra destino automático sem seleção", acao => {
    preparar(acao); expect(screen.queryByRole("combobox")).toBeNull();
    expect(screen.getByText(acao === "concluir" ? "A demanda irá para Concluídos." : "A demanda irá para Cancelados.")).not.toBeNull();
    if (acao === "cancelar") {
        expect(screen.getByLabelText("Motivo do cancelamento").required).toBe(true);
        expect(screen.getByText("Informe um motivo entre 1 e 10000 caracteres.")).not.toBeNull();
        expect(document.activeElement).toBe(screen.getByLabelText("Motivo do cancelamento"));
    } else expect(document.activeElement).toBe(screen.getByRole("button", { name: "Voltar sem alterar" }));
});

test.each(["", " \u00a0\ufeff ", "a".repeat(10001), "🙂".repeat(5001)])
    ("MOV-12/30: motivo inválido conserva rascunho e não envia", async motivo => {
        const { user, salvar, aoErro } = preparar("cancelar");
        fireEvent.change(screen.getByLabelText("Motivo do cancelamento"), { target: { value: motivo } });
        await user.click(screen.getByRole("button", { name: "Confirmar cancelamento" }));
        expect(screen.getByRole("alert").textContent).toBe("Informe um motivo entre 1 e 10000 caracteres.");
        expect(screen.getByLabelText("Motivo do cancelamento").value).toBe(motivo);
        expect(salvar).not.toHaveBeenCalled(); expect(aoErro).not.toHaveBeenCalled();
    });

test.each([" \u00a0Motivo\ncompleto\ufeff ", "🙂".repeat(5000)])("MOV-30: motivo válido é normalizado sem truncamento", async motivo => {
    const { user, salvar } = preparar("cancelar"); fireEvent.change(screen.getByLabelText("Motivo do cancelamento"), { target: { value: motivo } });
    await ativarPorTeclado(user, screen.getByRole("button", { name: "Confirmar cancelamento" }));
    expect(salvar).toHaveBeenCalledExactlyOnceWith({ motivo: motivo.trim() });
});

test("MOV-30: conclusão não envia motivo, data ou destino", async () => {
    const { user, salvar } = preparar("concluir"); await ativarPorTeclado(user, screen.getByRole("button", { name: "Confirmar conclusão" }));
    expect(salvar).toHaveBeenCalledExactlyOnceWith({});
});

test.each(["mover", "reabrir", "concluir", "cancelar"])("MOV-31: %s pendente impede duplo envio e saída", async acao => {
    let resolver; const salvar = vi.fn(() => new Promise(resolve => { resolver = resolve; })); const { user, cancelar } = preparar(acao, { salvar });
    if (acao === "mover" || acao === "reabrir") await user.selectOptions(screen.getByLabelText("Etapa de destino"), "12");
    if (acao === "cancelar") fireEvent.change(screen.getByLabelText("Motivo do cancelamento"), { target: { value: "Solicitado" } });
    const botao = screen.getByRole("button", { name: botoes[acao] }); await user.click(botao);
    expect(botao.disabled).toBe(true); expect(screen.getByRole("button", { name: "Voltar sem alterar" }).disabled).toBe(true);
    for (const controle of screen.queryAllByRole(acao === "cancelar" ? "textbox" : "combobox")) expect(controle.disabled).toBe(true);
    fireEvent.submit(botao.closest("form")); fireEvent.click(botao); fireEvent.click(screen.getByRole("button", { name: "Voltar sem alterar" }));
    expect(salvar).toHaveBeenCalledTimes(1); expect(cancelar).not.toHaveBeenCalled();
    await act(async () => { resolver(); }); await waitFor(() => expect(botao.disabled).toBe(false));
});

test.each(["mover", "reabrir", "concluir", "cancelar"])("MOV-32: %s com erro mantém valores e comunica falha", async acao => {
    const error = new Error("Atualize o quadro."); const salvar = vi.fn().mockRejectedValue(error); const { user, aoErro } = preparar(acao, { salvar });
    if (["mover", "reabrir"].includes(acao)) await user.selectOptions(screen.getByLabelText("Etapa de destino"), "12");
    if (acao === "cancelar") fireEvent.change(screen.getByLabelText("Motivo do cancelamento"), { target: { value: " Rascunho\ninteiro " } });
    await user.click(screen.getByRole("button", { name: botoes[acao] }));
    expect((await screen.findByRole("alert")).textContent).toBe("Atualize o quadro.");
    if (["mover", "reabrir"].includes(acao)) expect(screen.getByLabelText("Etapa de destino").value).toBe("12");
    if (acao === "cancelar") expect(screen.getByLabelText("Motivo do cancelamento").value).toBe(" Rascunho\ninteiro ");
    expect(aoErro).toHaveBeenCalledExactlyOnceWith(error); expect(salvar).toHaveBeenCalledTimes(1);
    expect(screen.getByRole("dialog")).not.toBeNull();
});

test.each(["mover", "reabrir"])("MOV-29: %s sem destino explica bloqueio", acao => {
    preparar(acao, { etapas: etapas.filter(e => e.categoria !== "TRABALHO") });
    expect(screen.getByText("Nenhuma etapa de trabalho disponível. Configure uma etapa antes de continuar.")).not.toBeNull();
    expect(screen.getByRole("button", { name: botoes[acao] }).disabled).toBe(true);
    expect(screen.getByRole("button", { name: "Voltar sem alterar" }).disabled).toBe(false);
});

test.each(["mover", "reabrir", "concluir", "cancelar"])("MOV-31: bloqueio externo de %s não envia", async acao => {
    const { salvar } = preparar(acao, { bloqueado: true }); const botao = screen.getByRole("button", { name: botoes[acao] });
    expect(botao.disabled).toBe(true); fireEvent.submit(botao.closest("form")); expect(salvar).not.toHaveBeenCalled();
});

test("MOV-32: voltar por teclado descarta operação sem gravar", async () => {
    const { user, salvar, cancelar } = preparar("cancelar"); await ativarPorTeclado(user, screen.getByRole("button", { name: "Voltar sem alterar" }));
    expect(cancelar).toHaveBeenCalledTimes(1); expect(salvar).not.toHaveBeenCalled();
});
