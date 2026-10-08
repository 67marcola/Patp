import { act, fireEvent, render, screen, waitFor, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { expect, test, vi } from "vitest";
import Quadro from "./Quadro";
import { ativarPorTeclado } from "../test/keyboard";

const quadro = { id: 9, nome: "Postes", versao: 2, arquivado: false, podeAdministrar: true };
const demanda = { id: 21, numeroProcesso: "P-1", pessoa: "João", responsavel: "Ana", prioridade: "Alta",
    dataEmissao: "2020-02-29", prazoEtapa: null, prazoGeral: null, observacoes: "Anterior", status: "Cancelado",
    etapaId: 13, dataConclusao: null, dataCancelamento: "2021-03-04", motivoCancelamento: "Literal antigo" };
const etapas = [{ id: 11, nome: "Comprar", categoria: "TRABALHO", ordem: 1, quantidadeDemandas: 0 },
    { id: 12, nome: "Concluídos", categoria: "CONCLUIDA", ordem: 2, quantidadeDemandas: 0 },
    { id: 13, nome: "Cancelados", categoria: "CANCELADA", ordem: 3, quantidadeDemandas: 1 }];
function snapshot(d = demanda, flags = {}) { return { gerenciamento: { ...quadro, ...flags }, etapas, demandas: [d] }; }
const json = (dados, status = 200) => new Response(JSON.stringify(dados), { status });
const corrigida = { ...demanda, pessoa: "Maria", observacoes: null };
function preparar(handler, registro = quadro) {
    localStorage.setItem("token", "sessao"); const f = vi.fn(handler); vi.stubGlobal("fetch", f);
    const voltar = vi.fn(); const atualizar = vi.fn(); const aoOcupar = vi.fn();
    const view = render(<Quadro gerenciamento={registro} voltar={voltar} atualizar={atualizar} aoOcupar={aoOcupar} />);
    return { user: userEvent.setup(), f, voltar, atualizar, aoOcupar, ...view };
}
async function abrir(user) {
    const b = await screen.findByRole("button", { name: "Editar demanda P-1" });
    await ativarPorTeclado(user, b); return b;
}
test.each(["Em andamento", "Concluido", "Cancelado"])("EDI-20/27/28: edição permitida no estado%s", async status => {
    const { user } = preparar(() => json(snapshot({ ...demanda, status })));
    await abrir(user); expect(screen.getByLabelText("Cliente/solicitante").value).toBe("João");
    expect(screen.getByLabelText("Número da demanda").readOnly).toBe(true);
});
test.each([{ podeAdministrar: false }, { arquivado: true }, { status: null }, { status: "" }, { status: "Antigo" }])("EDI-26/29: consulta sem edição%s", async flags => {
    preparar(() => json(snapshot({ ...demanda, ...(Object.hasOwn(flags, "status") ? { status: flags.status } : {}) }, flags)));
    const card = await screen.findByRole("article", { name: "P-1 — João" });
    expect(within(card).queryByRole("button", { name: "Editar demanda P-1" })).toBeNull();
    expect(card.textContent).toContain("Anterior");
});
test("EDI-21: cancelar fecha sem PUT e devolve foco", async () => {
    const { user, f } = preparar(() => json(snapshot())); const botao = await abrir(user);
    fireEvent.change(screen.getByLabelText("Cliente/solicitante"), { target: { value: "Rascunho" } });
    await user.click(screen.getByRole("button", { name: "Cancelar", exact: true }));
    expect(screen.queryByRole("form", { name: "Editar demanda" })).toBeNull(); expect(document.activeElement).toBe(botao);
    expect(f.mock.calls.map(([, r]) => r.method)).toEqual(["GET"]);
});
test("EDI-24/27/28: salva cartão, campos de encerramento, cache e foco sem mudar contagem", async () => {
    const { user, f, atualizar, aoOcupar } = preparar((_url, r) => json(snapshot(r.method === "GET" ? demanda : corrigida, { versao: r.method === "GET" ? 2 : 3 })));
    await abrir(user); fireEvent.change(screen.getByLabelText("Cliente/solicitante"), { target: { value: "  Maria  " } });
    fireEvent.change(screen.getByLabelText("Observações"), { target: { value: "" } });
    await user.click(screen.getByRole("button", { name: "Salvar alterações" }));
    await waitFor(() => expect(screen.queryByRole("form")).toBeNull());
    const card = screen.getByRole("article", { name: "P-1 — Maria" });
    expect(card.textContent).toContain("Cancelado"); expect(card.textContent).toContain("04/03/2021"); expect(card.textContent).toContain("Literal antigo");
    expect(card.closest("section").getAttribute("aria-labelledby")).toBe("etapa-titulo-13");
    expect(screen.getByRole("region", { name: "Cancelados" }).textContent).toContain("1 demanda");
    expect(atualizar.mock.calls.at(-1)[0].versao).toBe(3); expect(aoOcupar.mock.calls.map(([v]) => v)).toEqual([true, false]);
    expect(document.activeElement).toBe(within(card).getByRole("button", { name: "Editar demanda P-1" }));
    expect(JSON.parse(f.mock.calls[1][1].body)).toEqual({ versao: 2, pessoa: "Maria", responsavel: "Ana", prioridade: "Alta",
        dataEmissao: "2020-02-29", prazoEtapa: null, prazoGeral: null, observacoes: null });
});
test("EDI-22: pending bloqueia formulário, outras mutações e saída", async () => {
    let resolver; const { user, f, voltar } = preparar((_url, r) => r.method === "GET" ? json(snapshot()) : new Promise(resolve => { resolver = resolve; }));
    await abrir(user); await user.click(screen.getByRole("button", { name: "Salvar alterações" }));
    for (const name of ["← Voltar", "+ Criar processo", "+ Nova etapa", "Cancelar", "Salvando...", "Reabrir demanda P-1"])
        expect(screen.getByRole("button", { name, exact: true }).disabled).toBe(true);
    await user.click(screen.getByRole("button", { name: "← Voltar" })); fireEvent.submit(screen.getByRole("form"));
    expect(voltar).not.toHaveBeenCalled(); expect(f).toHaveBeenCalledTimes(2);
    await act(async () => resolver(json(snapshot(demanda, { versao: 3 }))));
    await waitFor(() => expect(screen.queryByRole("form")).toBeNull());
});
test.each(["conflito", "rede", "falso sucesso"])("EDI-23: %s conserva rascunho, GET manual e novo envio manual", async caso => {
    let put = 0; let get = 0;
    const { user, f } = preparar((_url, r) => {
        if (r.method === "GET") return json(snapshot(demanda, { versao: get++ ? 4 : 2 }));
        if (put++ === 0) return caso === "conflito" ? json({ erro: "Conflito" }, 409) : caso === "rede" ? Promise.reject(new TypeError("Offline")) : json(snapshot(demanda, { versao: 3 }));
        return json(snapshot({ ...demanda, pessoa: "Rascunho" }, { versao: 5 }));
    });
    await abrir(user); fireEvent.change(screen.getByLabelText("Cliente/solicitante"), { target: { value: " Rascunho " } });
    await user.click(screen.getByRole("button", { name: "Salvar alterações" }));
    expect(screen.getByLabelText("Cliente/solicitante").value).toBe(" Rascunho ");
    expect(screen.getByRole("button", { name: "Salvar alterações" }).disabled).toBe(true);
    expect(screen.getByRole("alert").textContent).toBe(caso === "conflito" ? "Conflito" : "Não foi possível confirmar a operação. Atualize a lista antes de tentar novamente.");
    expect(f.mock.calls.map(([, r]) => r.method)).toEqual(["GET", "PUT"]);
    await user.click(screen.getByRole("button", { name: "Atualizar quadro" }));
    await waitFor(() => expect(screen.getByRole("button", { name: "Salvar alterações" }).disabled).toBe(false));
    expect(screen.getByLabelText("Cliente/solicitante").value).toBe(" Rascunho ");
    expect(f.mock.calls.map(([, r]) => r.method)).toEqual(["GET", "PUT", "GET"]);
    await user.click(screen.getByRole("button", { name: "Salvar alterações" }));
    await waitFor(() => expect(screen.queryByRole("form")).toBeNull());
    expect(screen.getByRole("article", { name: "P-1 — Rascunho" })).not.toBeNull();
    expect(JSON.parse(f.mock.calls[3][1].body).versao).toBe(4);
});
test("EDI-25: troca de quadro descarta edição e ignora resposta tardia", async () => {
    let resolver; const outro = { ...quadro, id: 10, nome: "Outro" };
    const { user, rerender, atualizar } = preparar((url, r) => r.method === "PUT" ? new Promise(resolve => { resolver = resolve; })
        : json(url.includes("/10/") ? { gerenciamento: outro, etapas: [], demandas: [] } : snapshot()));
    await abrir(user); await user.click(screen.getByRole("button", { name: "Salvar alterações" }));
    rerender(<Quadro gerenciamento={outro} atualizar={atualizar} />);
    await waitFor(() => expect(screen.queryByRole("form")).toBeNull());
    await act(async () => resolver(json(snapshot(demanda, { versao: 3 }))));
    expect(screen.getByRole("heading", { name: "Outro" })).not.toBeNull(); expect(screen.queryByRole("article")).toBeNull();
    expect(atualizar.mock.calls.some(([q]) => q.id === 9 && q.versao === 3)).toBe(false);
});
