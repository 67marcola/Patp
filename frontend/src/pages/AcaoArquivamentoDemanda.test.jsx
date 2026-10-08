import { act, fireEvent, render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { expect, test, vi } from "vitest";
import AcaoArquivamentoDemanda from "./AcaoArquivamentoDemanda";
import { ativarPorTeclado } from "../test/keyboard";

const demanda = { id: 21, numeroProcesso: "Poste-1", pessoa: "João", etapaId: 11, status: "Cancelado" };
function preparar(acao, props = {}) {
    const salvar = vi.fn().mockResolvedValue(undefined), cancelar = vi.fn(), aoErro = vi.fn();
    const view = render(<AcaoArquivamentoDemanda acao={acao} demanda={demanda} etapa={{ id: 11, nome: "Comprar" }}
        salvar={salvar} cancelar={cancelar} aoErro={aoErro} {...props} />);
    return { ...view, salvar: props.salvar || salvar, cancelar, aoErro, user: userEvent.setup() };
}
test.each(["arquivar", "restaurar"])("ARQ-26: %s identifica alvo, efeito e foca Cancelar", acao => {
    preparar(acao);
    expect(screen.getByRole("dialog").getAttribute("aria-labelledby")).toBe("arquivo-demanda-titulo");
    expect(screen.getByRole("heading").textContent).toBe(`${acao === "arquivar" ? "Arquivar" : "Restaurar"} demanda: Poste-1`);
    expect(screen.getByText("João")).not.toBeNull();
    expect(screen.getByText(acao === "arquivar"
        ? "A demanda sairá das colunas e ficará disponível em Arquivadas. Seus dados, comentários e histórico serão preservados."
        : "A demanda voltará à etapa Comprar com status Cancelado. Restaurar não reabre nem move a demanda.")).not.toBeNull();
    expect(document.activeElement).toBe(screen.getByRole("button", { name: "Cancelar" }));
    expect(screen.queryByRole("textbox")).toBeNull(); expect(screen.queryByRole("combobox")).toBeNull();
});
test.each(["arquivar", "restaurar"])("ARQ-26: cancelar %s por teclado não altera", async acao => {
    const { user, cancelar, salvar } = preparar(acao); await ativarPorTeclado(user, screen.getByRole("button", { name: "Cancelar" }));
    expect(cancelar).toHaveBeenCalledTimes(1); expect(salvar).not.toHaveBeenCalled();
});
test.each(["arquivar", "restaurar"])("ARQ-26: confirma %s por teclado sem mudar ciclo", async acao => {
    const { user, salvar } = preparar(acao); await ativarPorTeclado(user, screen.getByRole("button", { name: acao === "arquivar" ? "Arquivar demanda" : "Restaurar demanda" }));
    expect(salvar).toHaveBeenCalledExactlyOnceWith(); expect(screen.getByRole("dialog")).not.toBeNull();
});
test.each(["arquivar", "restaurar"])("ARQ-27: %s pendente bloqueia duplicação e cancelamento", async acao => {
    let resolver; const salvar = vi.fn(() => new Promise(resolve => { resolver = resolve; })); const { user, cancelar } = preparar(acao, { salvar });
    const botao = screen.getByRole("button", { name: acao === "arquivar" ? "Arquivar demanda" : "Restaurar demanda" }); await user.click(botao);
    expect(botao.disabled).toBe(true); expect(screen.getByRole("button", { name: "Cancelar" }).disabled).toBe(true);
    expect(botao.closest("form").getAttribute("aria-busy")).toBe("true");
    fireEvent.submit(botao.closest("form")); fireEvent.click(screen.getByRole("button", { name: "Cancelar" }));
    expect(salvar).toHaveBeenCalledTimes(1); expect(cancelar).not.toHaveBeenCalled();
    await act(async () => resolver()); await waitFor(() => expect(botao.disabled).toBe(false));
});
test.each(["arquivar", "restaurar"])("ARQ-29: erro de %s permanece mesmo com atualização de props", async acao => {
    const error = new Error("Atualize o quadro."); const salvar = vi.fn().mockRejectedValue(error);
    const { user, aoErro, rerender } = preparar(acao, { salvar });
    await user.click(screen.getByRole("button", { name: acao === "arquivar" ? "Arquivar demanda" : "Restaurar demanda" }));
    expect((await screen.findByRole("alert")).textContent).toBe("Atualize o quadro."); expect(aoErro).toHaveBeenCalledExactlyOnceWith(error);
    rerender(<AcaoArquivamentoDemanda acao={acao} demanda={demanda} etapa={{ nome: "Comprar" }} salvar={salvar} cancelar={() => {}} bloqueado />);
    expect(screen.getByRole("alert").textContent).toBe("Atualize o quadro."); expect(screen.getByRole("dialog")).not.toBeNull();
    expect(screen.getByRole("button", { name: "Cancelar" }).disabled).toBe(false); expect(salvar).toHaveBeenCalledTimes(1);
});
test.each(["arquivar", "restaurar"])("ARQ-27/29: bloqueio externo impede %s", acao => {
    const { salvar } = preparar(acao, { bloqueado: true }); const botao = screen.getByRole("button", { name: acao === "arquivar" ? "Arquivar demanda" : "Restaurar demanda" });
    expect(botao.disabled).toBe(true); fireEvent.submit(botao.closest("form")); expect(salvar).not.toHaveBeenCalled();
});
