import { act, fireEvent, render, screen, waitFor, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { expect, test, vi } from "vitest";
import Quadro from "./Quadro";
import App from "../App";
import { ativarPorTeclado } from "../test/keyboard";

const quadro = { id: 9, nome: "Postes", versao: 2, arquivado: false, podeAdministrar: true };
const d = { id: 7, numeroProcesso: "P-7", pessoa: "João", responsavel: null, prioridade: null, dataEmissao: null, prazoEtapa: null, prazoGeral: null, observacoes: null, status: "Em andamento", etapaId: 11, dataConclusao: null, dataCancelamento: null, motivoCancelamento: null };
const json = (v, status = 200) => new Response(JSON.stringify(v), { status });
const conversa = (id = 7, q = 9) => ({ gerenciamentoId: q, processoId: id, quadroArquivado: false, demandaArquivada: false, comentarios: [] });
function snapshot(flags = {}, arquivo = false, status = "Em andamento") { return { gerenciamento: { ...quadro, ...flags }, etapas: [{ id: 11, nome: "Comprar", categoria: "TRABALHO", ordem: 1, quantidadeDemandas: arquivo ? 1 : 2 }], demandas: [...(arquivo ? [] : [{ ...d, status }]), { ...d, id: 8, numeroProcesso: "P-8" }], demandasArquivadas: arquivo ? [{ ...d, status }] : [] }; }
function preparar(handler) {
    localStorage.setItem("token", "sessao"); const f = vi.fn(handler); vi.stubGlobal("fetch", f); const voltar = vi.fn(), aoOcupar = vi.fn();
    return { ...render(<Quadro gerenciamento={quadro} voltar={voltar} aoOcupar={aoOcupar} />), user: userEvent.setup(), f, voltar, aoOcupar };
}
async function abrir(user, id = 7) { const b = await screen.findByRole("button", { name: `Comentários da demanda P-${id}` }); await ativarPorTeclado(user, b); return b; }
test.each([[false, false, "Em andamento"], [false, false, "Concluido"], [false, false, "Cancelado"], [false, false, "Antigo"], [true, false, "Em andamento"], [false, true, "Em andamento"]])("COM-17: consulta para leitor em status/arquivo %s/%s/%s", async (q, p, estado) => {
    const { user, f } = preparar(url => url.endsWith("comentarios") ? json({ ...conversa(), quadroArquivado: q, demandaArquivada: p }) : json(snapshot({ podeAdministrar: false, arquivado: q }, p, estado)));
    const card = await screen.findByRole("article", { name: "P-7 — João" }); expect(within(card).getAllByRole("button").map(b => b.textContent)).toEqual(["Comentários"]); await abrir(user); await screen.findByRole("complementary", { name: "Comentários: P-7" });
    expect(f.mock.calls[1][0]).toBe("http://localhost:8081/api/gerenciamentos/9/demandas/7/comentarios"); expect(screen.queryByRole("textbox") === null).toBe(q || p);
});
test.each(["botão", "Escape"])("COM-26: fecha por %s e devolve foco de origem; reabrir descarta texto", async modo => {
    const { user } = preparar(url => json(url.endsWith("comentarios") ? conversa() : snapshot())); const b = await abrir(user); const campo = await screen.findByRole("textbox"); fireEvent.change(campo, { target: { value: "Rascunho" } });
    if (modo === "botão") await user.click(screen.getByRole("button", { name: "Fechar comentários" })); else await user.keyboard("{Escape}");
    expect(screen.queryByRole("complementary", { name: "Comentários: P-7" })).toBeNull(); expect(document.activeElement).toBe(b); await user.click(b); expect((await screen.findByRole("textbox")).value).toBe("");
});
test("COM-26: origem removida devolve foco ao Voltar", async () => {
    const { user } = preparar(url => json(url.endsWith("comentarios") ? conversa() : snapshot())); const b = await abrir(user); await screen.findByRole("textbox"); b.remove(); await user.click(screen.getByRole("button", { name: "Fechar comentários" })); expect(document.activeElement).toBe(screen.getByRole("button", { name: "← Voltar" }));
});
test("COM-22: painel aberto bloqueia mutações, mas permite trocar cartão e descarta rascunho", async () => {
    const { user, f } = preparar(url => json(url.endsWith("comentarios") ? conversa(url.includes("/8/") ? 8 : 7) : snapshot())); await abrir(user); const c = await screen.findByRole("textbox"); fireEvent.change(c, { target: { value: "Só da P7" } });
    expect(screen.getByRole("button", { name: "+ Nova etapa" }).disabled).toBe(true); expect(screen.getByRole("button", { name: "Editar demanda P-7" }).disabled).toBe(true); fireEvent.click(screen.getByRole("button", { name: "Editar demanda P-7" })); expect(screen.queryByRole("form", { name: "Editar demanda" })).toBeNull();
    await abrir(user, 8); await screen.findByRole("complementary", { name: "Comentários: P-8" }); expect((await screen.findByRole("textbox")).value).toBe(""); expect(f.mock.calls.map(([url]) => url)).toEqual(["http://localhost:8081/api/gerenciamentos/9/estrutura-etapas", "http://localhost:8081/api/gerenciamentos/9/demandas/7/comentarios", "http://localhost:8081/api/gerenciamentos/9/demandas/8/comentarios"]);
});
test("COM-22: POST pendente bloqueia quadro e sair/fechar/trocar/duplo clique até confirmar", async () => {
    let resolver; const { user, f, voltar, aoOcupar } = preparar((url, r) => r.method === "POST" ? new Promise(resolve => { resolver = resolve; }) : json(url.endsWith("comentarios") ? conversa() : snapshot())); await abrir(user); const c = await screen.findByRole("textbox"); fireEvent.change(c, { target: { value: "Nova" } }); await user.click(screen.getByRole("button", { name: "Enviar comentário" }));
    for (const b of screen.getAllByRole("button")) expect(b.disabled).toBe(true); expect(c.disabled).toBe(true); fireEvent.click(screen.getByRole("button", { name: "← Voltar" })); fireEvent.click(screen.getByRole("button", { name: "Comentários da demanda P-8" })); fireEvent.submit(c.closest("form")); expect(voltar).not.toHaveBeenCalled(); expect(f.mock.calls.map(([, r]) => r.method)).toEqual(["GET", "GET", "POST"]); expect(aoOcupar.mock.calls).toEqual([[true]]);
    await act(async () => resolver(json({ id: 21, texto: "Nova", funcionario: "Ana", dataHora: "2020-02-29T12:34:56", processoId: 7 }, 201))); expect(c.value).toBe(""); expect(aoOcupar.mock.calls).toEqual([[true], [false]]); expect(screen.getByRole("button", { name: "← Voltar" }).disabled).toBe(false);
});
test("COM-27: troca de quadro ignora GET antigo, fecha conversa e não leva rascunho", async () => {
    let resolver; const { user, f, rerender, voltar, aoOcupar } = preparar(url => url.endsWith("comentarios") ? new Promise(resolve => { resolver = resolve; }) : json(snapshot())); await abrir(user);
    f.mockImplementation(() => json(snapshot({ id: 10, nome: "Novo" }))); rerender(<Quadro gerenciamento={{ ...quadro, id: 10 }} voltar={voltar} aoOcupar={aoOcupar} />); await screen.findByRole("heading", { name: "Novo" }); await act(async () => resolver(json(conversa()))); expect(screen.queryByRole("complementary", { name: "Comentários: P-7" })).toBeNull(); expect(screen.queryByRole("alert")).toBeNull();
});
test("COM-22: App bloqueia Sair durante POST e libera após resposta", async () => {
    localStorage.setItem("token", "sessao"); localStorage.setItem("usuario", JSON.stringify({ id: 1, nome: "Ana", email: "ana@teste", setor: "Teste" })); let resolver; const f = vi.fn((url, r) => {
        if (url.endsWith("/usuarios/me")) return json({ id: 1, nome: "Ana", email: "ana@teste", setor: "Teste" });
        if (url.endsWith("/gerenciamentos?arquivado=false")) return json([quadro]);
        if (url.endsWith("/gerenciamentos/9")) return json(quadro);
        if (url.endsWith("estrutura-etapas")) return json(snapshot());
        if (r.method === "POST") return new Promise(resolve => { resolver = resolve; });
        return json(conversa());
    }); vi.stubGlobal("fetch", f); render(<App />); const user = userEvent.setup(); await user.click(await screen.findByRole("button", { name: "Abrir Postes" })); await abrir(user); const c = await screen.findByRole("textbox"); fireEvent.change(c, { target: { value: "Nova" } }); await user.click(screen.getByRole("button", { name: "Enviar comentário" })); expect(screen.getByRole("button", { name: "Sair" }).disabled).toBe(true);
    await act(async () => resolver(json({ id: 21, texto: "Nova", funcionario: "Ana", dataHora: "2020-02-29T12:34:56", processoId: 7 }, 201))); await waitFor(() => expect(screen.getByRole("button", { name: "Sair" }).disabled).toBe(false)); expect(localStorage.getItem("token")).toBe("sessao");
});
