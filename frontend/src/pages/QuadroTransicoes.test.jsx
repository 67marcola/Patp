import { act, fireEvent, render, screen, waitFor, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { expect, test, vi } from "vitest";
import Quadro from "./Quadro";
import App from "../App";
import { ativarPorTeclado } from "../test/keyboard";

const quadro = { id: 9, nome: "Postes", descricao: "Instalações", criador: { id: 3, nome: "Ana" }, arquivado: false, versao: 0, podeAdministrar: true };
const etapas = [{ id: 11, nome: "Comprar", setor: "Compras", ordem: 1, categoria: "TRABALHO" },
    { id: 12, nome: "Verificar local", setor: "Projeto", ordem: 2, categoria: "TRABALHO" },
    { id: 13, nome: "Instalar", setor: "Campo", ordem: 3, categoria: "TRABALHO" },
    { id: 14, nome: "Concluídos", setor: null, ordem: 4, categoria: "CONCLUIDA" },
    { id: 15, nome: "Cancelados", setor: null, ordem: 5, categoria: "CANCELADA" }];
const demanda = { id: 21, numeroProcesso: "Poste-1", pessoa: "João", responsavel: null, status: "Em andamento", prioridade: null,
    dataEmissao: null, prazoEtapa: null, prazoGeral: null, dataConclusao: null, dataCancelamento: null, motivoCancelamento: null, observacoes: "Preservada", etapaId: 11 };
const botoes = { mover: "Mover/pular etapa", concluir: "Concluir", cancelar: "Cancelar", reabrir: "Reabrir" };
const confirmar = { mover: "Mover demanda", concluir: "Confirmar conclusão", cancelar: "Confirmar cancelamento", reabrir: "Reabrir demanda" };
const snapshot = (demandas = [demanda], flags = {}, colunas = etapas) => ({ gerenciamento: { ...quadro, ...flags }, demandas,
    etapas: colunas.map(e => ({ ...e, quantidadeDemandas: demandas.filter(d => d.etapaId === e.id).length })) });
const json = (dados, status = 200) => new Response(JSON.stringify(dados), { status });
function antes(acao) {return acao === "reabrir" ? { ...demanda, status: "Cancelado", etapaId: 15, dataCancelamento: "2020-02-29", motivoCancelamento: "Anterior" } : { ...demanda };}
function depois(acao) {return { ...demanda, etapaId: acao === "concluir" ? 14 : acao === "cancelar" ? 15 : 13,
    status: acao === "concluir" ? "Concluido" : acao === "cancelar" ? "Cancelado" : "Em andamento",
    dataConclusao: acao === "concluir" ? "2026-10-06" : null, dataCancelamento: acao === "cancelar" ? "2026-10-06" : null,
    motivoCancelamento: acao === "cancelar" ? "Solicitado" : null };}
function preparar(handler, registro = quadro) {
    localStorage.setItem("token", "sessao"); const fetchMock = vi.fn(handler); vi.stubGlobal("fetch", fetchMock);
    const voltar = vi.fn(); const atualizar = vi.fn(); const aoOcupar = vi.fn();
    const view = render(<Quadro gerenciamento={registro} voltar={voltar} atualizar={atualizar} aoOcupar={aoOcupar} />);
    return { user: userEvent.setup(), fetchMock, voltar, atualizar, aoOcupar, ...view };
}
async function abrir(user, acao) {
    const card = await screen.findByRole("article", { name: "Poste-1 — João" });
    const botao = within(card).getByRole("button", { name: `${botoes[acao]} demanda Poste-1` });
    await ativarPorTeclado(user, botao);
    if (["mover", "reabrir"].includes(acao)) await user.selectOptions(screen.getByLabelText("Etapa de destino"), "13");
    if (acao === "cancelar") fireEvent.change(screen.getByLabelText("Motivo do cancelamento"), { target: { value: "  Solicitado  " } });
    return botao;
}

test.each(["Em andamento", "Concluido", "Cancelado"])("MOV-28: status%s mostra somente ações permitidas", async status => {
    preparar(() => json(snapshot([{ ...demanda, status, etapaId: status === "Concluido" ? 14 : status === "Cancelado" ? 15 : 11 }])));
    const card = await screen.findByRole("article", { name: "Poste-1 — João" });
    expect(within(card).getAllByRole("button").map(b => b.textContent)).toEqual(status === "Em andamento" ? ["Mover/pular etapa", "Concluir", "Cancelar"] : ["Reabrir"]);
});

test.each([{ podeAdministrar: false }, { arquivado: true }])("MOV-28: sem permissão/arquivado%s só consulta", async flags => {
    const { fetchMock } = preparar(() => json(snapshot([demanda], flags)));
    const card = await screen.findByRole("article", { name: "Poste-1 — João" });
    expect(within(card).queryByRole("button")).toBeNull(); expect(card.textContent).toContain("Preservada"); expect(fetchMock).toHaveBeenCalledTimes(1);
});

test.each([null, "", "Status antigo"])("MOV-35: status desconhecido%s explica bloqueio sem normalizar", async status => {
    preparar(() => json(snapshot([{ ...demanda, status }]))); const card = await screen.findByRole("article", { name: "Poste-1 — João" });
    expect(within(card).queryByRole("button")).toBeNull();
    expect(within(card).getByText("Status antigo não reconhecido. Solicite a correção do registro.")).not.toBeNull();
    expect(within(card).getByText("Status", { exact: true }).nextElementSibling.textContent).toBe(status || "Não informado");
});

test.each(["mover", "concluir", "cancelar", "reabrir"])("MOV-29/33: %s atualiza coluna/count/cache e fecha diálogo", async acao => {
    const { user, fetchMock, atualizar, aoOcupar } = preparar((_url, req) => json(snapshot([req.method === "GET" ? antes(acao) : depois(acao)], { versao: req.method === "GET" ? 0 : 1 })));
    await abrir(user, acao); await ativarPorTeclado(user, screen.getByRole("button", { name: confirmar[acao] }));
    await waitFor(() => expect(screen.queryByRole("dialog")).toBeNull());
    const card = screen.getByRole("article", { name: "Poste-1 — João" });
    expect(card.closest("section").getAttribute("aria-labelledby")).toBe(`etapa-titulo-${depois(acao).etapaId}`);
    expect(card.textContent).toContain(depois(acao).status); expect(card.textContent).toContain("Preservada");
    expect(screen.getByRole("region", { name: etapas.find(e => e.id === depois(acao).etapaId).nome }).textContent).toContain("1 demanda");
    expect(atualizar.mock.calls.at(-1)[0]).toEqual({ ...quadro, versao: 1 }); expect(aoOcupar.mock.calls.map(([v]) => v)).toEqual([true, false]);
    const payload = { versao: 0, ...(["mover", "reabrir"].includes(acao) ? { etapaId: 13 } : {}), ...(acao === "cancelar" ? { motivo: "Solicitado" } : {}) };
    expect(fetchMock.mock.calls.map(([url, req]) => [url, req.method, req.body && JSON.parse(req.body)])).toEqual([
        ["http://localhost:8081/api/gerenciamentos/9/estrutura-etapas", "GET", undefined],
        [`http://localhost:8081/api/gerenciamentos/9/demandas/21/${acao}`, "PUT", payload]
    ]);
    expect(document.activeElement.isConnected).toBe(true); expect(document.activeElement.tagName).toBe("BUTTON");
});

test.each(["mover", "concluir", "cancelar", "reabrir"])("MOV-31: %s trava outros controles e Voltar até confirmar", async acao => {
    let resolver; const { user, fetchMock, voltar, aoOcupar } = preparar((_url, req) => req.method === "GET" ? json(snapshot([antes(acao)]))
        : new Promise(resolve => { resolver = resolve; }));
    await abrir(user, acao); await user.click(screen.getByRole("button", { name: confirmar[acao] }));
    expect(screen.getByRole("button", { name: "← Voltar" }).disabled).toBe(true);
    expect(screen.getByRole("button", { name: "+ Criar processo" }).disabled).toBe(true);
    expect(screen.getByRole("button", { name: "+ Nova etapa" }).disabled).toBe(true);
    expect(screen.getByRole("button", { name: "Voltar sem alterar" }).disabled).toBe(true);
    for (const b of within(screen.getByRole("article", { name: "Poste-1 — João" })).getAllByRole("button")) expect(b.disabled).toBe(true);
    fireEvent.click(screen.getByRole("button", { name: "← Voltar" })); fireEvent.submit(screen.getByRole("dialog").querySelector("form"));
    expect(voltar).not.toHaveBeenCalled(); expect(fetchMock).toHaveBeenCalledTimes(2); expect(aoOcupar).toHaveBeenCalledExactlyOnceWith(true);
    await act(async () => resolver(json(snapshot([depois(acao)], { versao: 1 }))));
    await waitFor(() => expect(screen.queryByRole("dialog")).toBeNull());
    expect(screen.getByRole("button", { name: "← Voltar" }).disabled).toBe(false); expect(aoOcupar.mock.calls.at(-1)).toEqual([false]);
});

test.each(["mover", "concluir", "cancelar", "reabrir"])("MOV-32: %s incoerente conserva diálogo e só GET manual libera", async acao => {
    let leituras = 0; const { user, fetchMock } = preparar((_url, req) => req.method === "GET" ? json(snapshot([antes(acao)], { versao: leituras++ }))
        : json(snapshot([antes(acao)], { versao: 1 })));
    await abrir(user, acao); await user.click(screen.getByRole("button", { name: confirmar[acao] }));
    expect((await screen.findByRole("alert")).textContent).toBe("Não foi possível confirmar a operação. Atualize a lista antes de tentar novamente.");
    expect(screen.getByRole("dialog")).not.toBeNull(); expect(screen.getByRole("button", { name: confirmar[acao] }).disabled).toBe(true);
    if (["mover", "reabrir"].includes(acao)) expect(screen.getByLabelText("Etapa de destino").value).toBe("13");
    if (acao === "cancelar") expect(screen.getByLabelText("Motivo do cancelamento").value).toBe("  Solicitado  ");
    expect(fetchMock.mock.calls.map(([, req]) => req.method)).toEqual(["GET", "PUT"]);
    await user.click(screen.getByRole("button", { name: "Atualizar quadro" }));
    await waitFor(() => expect(screen.getByRole("button", { name: confirmar[acao] }).disabled).toBe(false));
    expect(fetchMock.mock.calls.map(([, req]) => req.method)).toEqual(["GET", "PUT", "GET"]);
    expect(screen.getByRole("dialog")).not.toBeNull();
});

test("MOV-34: mudar quadro ignora resposta antiga da ação e elimina seu diálogo", async () => {
    let resolver; const { user, rerender, atualizar } = preparar((url, req) => req.method === "PUT" ? new Promise(resolve => { resolver = resolve; })
        : json(url.includes("/10/") ? { ...snapshot([], { id: 10, nome: "Outro" }), etapas: [] } : snapshot()));
    await abrir(user, "concluir"); await user.click(screen.getByRole("button", { name: "Confirmar conclusão" }));
    rerender(<Quadro gerenciamento={{ ...quadro, id: 10, nome: "Outro" }} atualizar={atualizar} />);
    await screen.findByRole("heading", { name: "Outro" }); await waitFor(() => expect(screen.queryByRole("dialog")).toBeNull());
    await act(async () => resolver(json(snapshot([depois("concluir")], { versao: 1 }))));
    expect(screen.queryByRole("article", { name: "Poste-1 — João" })).toBeNull(); expect(screen.getByRole("heading", { name: "Outro" })).not.toBeNull();
    expect(atualizar.mock.calls.some(([q]) => q.id === 9 && q.versao === 1)).toBe(false);
});

test.each(["mover", "concluir", "cancelar", "reabrir"].flatMap(acao => ["http", "rede"].map(falha => [acao, falha])))
    ("MOV-32: %s com falha de %s mantém rascunho/cache e exige GET manual", async (acao, falha) => {
        const mensagem = falha === "http" ? "Gerenciamento alterado por outro usuário. Atualize e tente novamente."
            : "Não foi possível confirmar a operação. Atualize a lista antes de tentar novamente.";
        const { user, fetchMock, atualizar } = preparar((_url, req) => {
            if (req.method === "GET") return json(snapshot([antes(acao)]));
            if (falha === "rede") throw new TypeError("Conexão interrompida");
            return json({ erro: mensagem }, 409);
        });
        await abrir(user, acao); await user.click(screen.getByRole("button", { name: confirmar[acao] }));
        expect((await screen.findByRole("alert")).textContent).toBe(mensagem);
        expect(screen.getByRole("dialog")).not.toBeNull();
        expect(screen.getByRole("button", { name: confirmar[acao] }).disabled).toBe(true);
        if (["mover", "reabrir"].includes(acao)) expect(screen.getByLabelText("Etapa de destino").value).toBe("13");
        if (acao === "cancelar") expect(screen.getByLabelText("Motivo do cancelamento").value).toBe("  Solicitado  ");
        expect(atualizar.mock.calls.map(([q]) => q.versao)).toEqual([0]);
        expect(screen.getByRole("article", { name: "Poste-1 — João" }).closest("section").getAttribute("aria-labelledby"))
            .toBe(`etapa-titulo-${antes(acao).etapaId}`);
        expect(fetchMock.mock.calls.map(([, req]) => req.method)).toEqual(["GET", "PUT"]);
        await user.click(screen.getByRole("button", { name: "Atualizar quadro" }));
        await waitFor(() => expect(screen.getByRole("button", { name: confirmar[acao] }).disabled).toBe(false));
        expect(fetchMock.mock.calls.map(([, req]) => req.method)).toEqual(["GET", "PUT", "GET"]);
    });

test("MOV-32/33: voltar sem alterar retorna foco ao botão e não enviaPUT", async () => {
    const { user, fetchMock } = preparar(() => json(snapshot())); const origem = await abrir(user, "cancelar");
    await ativarPorTeclado(user, screen.getByRole("button", { name: "Voltar sem alterar" }));
    expect(screen.queryByRole("dialog")).toBeNull(); expect(document.activeElement).toBe(origem); expect(fetchMock).toHaveBeenCalledTimes(1);
});

test("MOV-31: Sair da aplicação fica bloqueado durante transição", async () => {
    localStorage.setItem("token", "sessao"); let resolver;
    localStorage.setItem("usuario", JSON.stringify({ id: 3, nome: "Ana", email: "ana@example.test", setor: "Técnico" }));
    const fetchMock = vi.fn((url, req) => {
        if (url.endsWith("/usuarios/me")) return json({ id: 3, nome: "Ana", email: "ana@example.test", setor: "Técnico" });
        if (url.includes("?arquivado=")) return json([quadro]);
        if (url.endsWith("/gerenciamentos/9")) return json(quadro);
        if (req.method === "PUT") return new Promise(resolve => { resolver = resolve; });
        return json(snapshot());
    }); vi.stubGlobal("fetch", fetchMock); const user = userEvent.setup(); render(<App />);
    await user.click(await screen.findByRole("button", { name: "Abrir Postes" }));
    await abrir(user, "concluir"); await user.click(screen.getByRole("button", { name: "Confirmar conclusão" }));
    expect(screen.getByRole("button", { name: "Sair" }).disabled).toBe(true); fireEvent.click(screen.getByRole("button", { name: "Sair" }));
    expect(localStorage.getItem("token")).toBe("sessao");
    await act(async () => resolver(json(snapshot([depois("concluir")], { versao: 1 }))));
    await waitFor(() => expect(screen.getByRole("button", { name: "Sair" }).disabled).toBe(false));
});
