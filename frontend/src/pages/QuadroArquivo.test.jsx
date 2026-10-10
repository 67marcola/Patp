import { act, fireEvent, render, screen, waitFor, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { expect, test, vi } from "vitest";
import Quadro from "./Quadro";
import { ativarPorTeclado } from "../test/keyboard";

const quadro = { id: 9, nome: "Postes", versao: 2, arquivado: false, podeAdministrar: true };
const demanda = { id: 21, numeroProcesso: "P-1", pessoa: "João", responsavel: "Ana", prioridade: "Alta",
    dataEmissao: "2020-02-29", prazoEtapa: "2021-03-04", prazoGeral: "2022-05-06", observacoes: "Literal\nCompleto",
    status: "Cancelado", etapaId: 13, dataConclusao: null, dataCancelamento: "2021-03-04", motivoCancelamento: "Motivo antigo" };
const etapas = [{ id: 11, nome: "Comprar", categoria: "TRABALHO", ordem: 1 },
    { id: 12, nome: "Concluídos", categoria: "CONCLUIDA", ordem: 2 }, { id: 13, nome: "Cancelados", categoria: "CANCELADA", ordem: 3 }];
function snapshot(arquivo = false, flags = {}, d = demanda) {
    return { gerenciamento: { ...quadro, ...flags }, demandas: arquivo ? [] : [d], demandasArquivadas: arquivo ? [d] : [],
        etapas: etapas.map(e => ({ ...e, quantidadeDemandas: !arquivo && e.id === d.etapaId ? 1 : 0 })) };
}
const json = (dados, status = 200) => new Response(JSON.stringify(dados), { status });
function preparar(handler) {
    localStorage.setItem("token", "sessao"); const f = vi.fn(handler); vi.stubGlobal("fetch", f);
    const voltar = vi.fn(), atualizar = vi.fn(), aoOcupar = vi.fn();
    return { ...render(<Quadro gerenciamento={quadro} voltar={voltar} atualizar={atualizar} aoOcupar={aoOcupar} />),
        user: userEvent.setup(), f, voltar, atualizar, aoOcupar };
}
const acoes = ["arquivar", "restaurar"];
const nome = acao => acao === "arquivar" ? "Arquivar" : "Restaurar";
async function abrir(user, acao) {
    const b = await screen.findByRole("button", { name: `${nome(acao)} demanda P-1` });
    await ativarPorTeclado(user, b); return b;
}
test.each(["Em andamento", "Concluido", "Cancelado"])("ARQ-24/25: consulta completa do arquivo %s", async status => {
    preparar(() => json(snapshot(true, {}, { ...demanda, status })));
    const arquivo = await screen.findByRole("complementary", { name: "Arquivadas" }); const card = within(arquivo).getByRole("article", { name: "P-1 — João" });
    for (const valor of [status, "Ana", "Alta", "29/02/2020", "04/03/2021", "06/05/2022", "Literal\nCompleto", "Motivo antigo", "Cancelados"])
        expect(card.textContent).toContain(valor);
    expect(within(card).getAllByRole("button").map(b => b.textContent)).toEqual(["Comentários", "Restaurar"]);
    expect(screen.getByRole("region", { name: "Cancelados" }).querySelector("article")).toBeNull();
    expect(screen.getByRole("region", { name: "Cancelados" }).textContent).toContain("0 demandas");
    expect(arquivo.textContent).toContain("Restaure e mova a demanda para liberar a etapa original.");
});
for (const acao of acoes) {
    const arquivada = acao === "restaurar";
    test.each(["Em andamento", "Concluido", "Cancelado"])(`ARQ-25/28: ${acao} no estado %s transfere literalmente e atualiza foco/cache`, async status => {
        const d = { ...demanda, status }; const { user, f, atualizar, aoOcupar } = preparar((_url, r) => json(snapshot(r.method === "GET" ? arquivada : !arquivada, { versao: r.method === "GET" ? 2 : 3 }, d)));
        await abrir(user, acao); expect(document.activeElement).toBe(screen.getByRole("button", { name: "Cancelar", exact: true }));
        await ativarPorTeclado(user, screen.getByRole("button", { name: `${nome(acao)} demanda`, exact: true }));
        await waitFor(() => expect(screen.queryByRole("dialog")).toBeNull());
        const destino = screen.getByRole(acao === "arquivar" ? "complementary" : "region", { name: acao === "arquivar" ? "Arquivadas" : "Cancelados" });
        const card = within(destino).getByRole("article", { name: "P-1 — João" });
        expect(card.textContent).toContain(status); expect(card.textContent).toContain("Motivo antigo"); expect(card.textContent).toContain("04/03/2021");
        expect(screen.getByRole("region", { name: "Cancelados" }).textContent).toContain(acao === "arquivar" ? "0 demandas" : "1 demanda");
        expect(atualizar.mock.calls.at(-1)[0]).toEqual({ ...quadro, versao: 3 }); expect(aoOcupar.mock.calls.map(([v]) => v)).toEqual([true, false]);
        expect(JSON.parse(f.mock.calls[1][1].body)).toEqual({ versao: 2 }); expect(f.mock.calls[1][0]).toBe(`http://localhost:8081/api/gerenciamentos/9/demandas/21/${acao}`);
        expect(document.activeElement.isConnected).toBe(true); expect(document.activeElement.tagName).toBe("BUTTON");
    });
    test.each([{ podeAdministrar: false }, { arquivado: true }, { status: null }, { status: "" }, { status: "Antigo" }])(`ARQ-24/25: ${acao} inelegível somente consulta %s`, async flags => {
        preparar(() => json(snapshot(arquivada, flags, { ...demanda, ...(Object.hasOwn(flags, "status") ? { status: flags.status } : {}) })));
        const card = await screen.findByRole("article", { name: "P-1 — João" });
        expect(within(card).getAllByRole("button").map(b => b.textContent)).toEqual(["Comentários"]); expect(card.textContent).toContain("Literal\nCompleto");
    });
    test(`ARQ-26: cancela ${acao} por teclado e devolve foco sem PUT`, async () => {
        const { user, f } = preparar(() => json(snapshot(arquivada))); const b = await abrir(user, acao);
        await ativarPorTeclado(user, screen.getByRole("button", { name: "Cancelar", exact: true }));
        expect(screen.queryByRole("dialog")).toBeNull(); expect(document.activeElement).toBe(b); expect(f.mock.calls.map(([, r]) => r.method)).toEqual(["GET"]);
    });
    test(`ARQ-27: ${acao} pendente trava todos controles e impede outro formulário`, async () => {
        let resolver; const { user, f, voltar, aoOcupar } = preparar((_url, r) => r.method === "GET" ? json(snapshot(arquivada)) : new Promise(resolve => { resolver = resolve; }));
        await abrir(user, acao); await user.click(screen.getByRole("button", { name: `${nome(acao)} demanda`, exact: true }));
        for (const b of screen.getAllByRole("button")) expect(b.disabled).toBe(true);
        fireEvent.click(screen.getByRole("button", { name: "← Voltar" })); fireEvent.submit(screen.getByRole("dialog").querySelector("form"));
        expect(voltar).not.toHaveBeenCalled(); expect(f).toHaveBeenCalledTimes(2); expect(aoOcupar).toHaveBeenCalledExactlyOnceWith(true);
        expect(screen.getAllByRole("dialog")).toHaveLength(1);
        await act(async () => resolver(json(snapshot(!arquivada, { versao: 3 }))));
        await waitFor(() => expect(screen.queryByRole("dialog")).toBeNull()); expect(aoOcupar.mock.calls.at(-1)).toEqual([false]);
    });
    test.each(["HTTP", "rede", "não confirmado"])(`ARQ-29: ${acao} erro %s exige GET e novo envio manual`, async caso => {
        let put = 0, get = 0; const { user, f } = preparar((_url, r) => {
            if (r.method === "GET") return json(snapshot(arquivada, { versao: get++ ? 4 : 2 }));
            if (put++ === 0) return caso === "HTTP" ? json({ erro: "Conflito" }, 409) : caso === "rede" ? Promise.reject(new TypeError("Offline")) : json(snapshot(arquivada, { versao: 3 }));
            return json(snapshot(!arquivada, { versao: 5 }));
        });
        await abrir(user, acao); await user.click(screen.getByRole("button", { name: `${nome(acao)} demanda`, exact: true }));
        const mensagem = caso === "HTTP" ? "Conflito" : "Não foi possível confirmar a operação. Atualize a lista antes de tentar novamente.";
        expect(screen.getByRole("alert").textContent).toBe(mensagem); expect(screen.getByRole("button", { name: `${nome(acao)} demanda`, exact: true }).disabled).toBe(true);
        expect(f.mock.calls.map(([, r]) => r.method)).toEqual(["GET", "PUT"]);
        await user.click(screen.getByRole("button", { name: "Atualizar quadro" }));
        await waitFor(() => expect(screen.getByRole("button", { name: `${nome(acao)} demanda`, exact: true }).disabled).toBe(false));
        expect(screen.getByRole("alert").textContent).toBe(mensagem); expect(f.mock.calls.map(([, r]) => r.method)).toEqual(["GET", "PUT", "GET"]);
        await user.click(screen.getByRole("button", { name: `${nome(acao)} demanda`, exact: true })); await waitFor(() => expect(screen.queryByRole("dialog")).toBeNull());
        expect(JSON.parse(f.mock.calls[3][1].body)).toEqual({ versao: 4 });
    });
    test.each(["transferida", "quadro arquivado", "sem permissão", "status desconhecido"])(`ARQ-29: GET deixa ${acao} inelegível %s sem retry`, async caso => {
        let get = 0; const { user, f } = preparar((_url, r) => {
            if (r.method === "PUT") return json({ erro: "Conflito" }, 409);
            if (!get++) return json(snapshot(arquivada));
            return json(snapshot(caso === "transferida" ? !arquivada : arquivada, { versao: 4, arquivado: caso === "quadro arquivado", podeAdministrar: caso !== "sem permissão" }, { ...demanda, status: caso === "status desconhecido" ? "Antigo" : demanda.status }));
        });
        await abrir(user, acao); await user.click(screen.getByRole("button", { name: `${nome(acao)} demanda`, exact: true }));
        await user.click(screen.getByRole("button", { name: "Atualizar quadro" })); await waitFor(() => expect(screen.queryByText("Carregando quadro...")).toBeNull());
        const b = screen.getByRole("button", { name: `${nome(acao)} demanda`, exact: true }); expect(b.disabled).toBe(true); fireEvent.submit(b.closest("form"));
        expect(f.mock.calls.map(([, r]) => r.method)).toEqual(["GET", "PUT", "GET"]); expect(screen.getByRole("button", { name: "Cancelar", exact: true }).disabled).toBe(false);
        await user.click(screen.getByRole("button", { name: "Cancelar", exact: true })); expect(screen.queryByRole("dialog")).toBeNull();
    });
    test.each(["sucesso", "erro"])(`ARQ-30: troca durante ${acao} ignora resposta tardia %s`, async caso => {
        let resolver; const outro = { ...quadro, id: 10, nome: "Outro" };
        const { user, rerender, atualizar } = preparar((url, r) => r.method === "PUT" ? new Promise(resolve => { resolver = resolve; })
            : json(url.includes("/10/") ? { gerenciamento: outro, etapas: [], demandas: [], demandasArquivadas: [] } : snapshot(arquivada)));
        await abrir(user, acao); await user.click(screen.getByRole("button", { name: `${nome(acao)} demanda`, exact: true }));
        rerender(<Quadro gerenciamento={outro} atualizar={atualizar} />); await waitFor(() => expect(screen.queryByRole("dialog")).toBeNull());
        await act(async () => resolver(caso === "sucesso" ? json(snapshot(!arquivada, { versao: 3 })) : json({ erro: "Antigo" }, 409)));
        expect(screen.getByRole("heading", { name: "Outro" })).not.toBeNull(); expect(screen.queryByRole("article")).toBeNull(); expect(screen.queryByRole("alert")).toBeNull();
        expect(atualizar.mock.calls.some(([q]) => q.id === 9 && q.versao === 3)).toBe(false);
    });
}
test("ARQ-27: confirmação aberta impede abrir outros formulários", async () => {
    const { user } = preparar(() => json(snapshot())); await abrir(user, "arquivar");
    for (const name of ["+ Nova etapa", "+ Criar processo", "Editar demanda P-1", "Reabrir demanda P-1"]) {
        const b = screen.getByRole("button", { name, exact: true }); expect(b.disabled).toBe(true); fireEvent.click(b);
    }
    expect(screen.getAllByRole("dialog")).toHaveLength(1); expect(screen.queryByRole("textbox")).toBeNull();
});
