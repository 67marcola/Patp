import { act, fireEvent, render, screen, waitFor, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { StrictMode } from "react";
import { expect, test, vi } from "vitest";
import AtividadeLateral from "./AtividadeLateral";

const cursor = offset => btoa(`9:100:0:${offset}`).replace(/=+$/, "");
const registro = (id = 100) => ({ chave: `AUTOMATICA:${id}`, origem: "AUTOMATICA", id, gerenciamentoId: 9, tipo: "DEMANDA_EDITADA", alvoTipo: "DEMANDA", alvoId: 7, alvoNome: "N-7", autorId: 2, autorNome: "Ana", dataHora: "2026-10-12T12:34:56.123456", descricao: null, alteracoes: [] });
const pagina = (registros = [registro()], proximoCursor = null) => ({ gerenciamentoId: 9, registros, proximoCursor });
const primeira = () => pagina(Array.from({ length: 50 }, (_, i) => registro(100 - i)), cursor(50));
const json = (p, status = 200) => new Response(JSON.stringify(p), { status });
function preparar(handler) {
    localStorage.setItem("token", "sessao"); const f = vi.fn(handler); vi.stubGlobal("fetch", f); const fechar = vi.fn();
    return { ...render(<AtividadeLateral gerenciamentoId={9} fechar={fechar} />), user: userEvent.setup(), f, fechar };
}
const lista = () => screen.getByRole("list", { name: "Registros de atividade" });
const itens = () => within(lista()).getAllByRole("listitem");
test("ACT-31/34: StrictMode aborta consulta antiga e termina nova leitura com foco", async () => {
    const f = vi.fn().mockImplementationOnce(() => new Promise(() => {})).mockResolvedValue(json(pagina([]))); vi.stubGlobal("fetch", f);
    render(<StrictMode><AtividadeLateral gerenciamentoId={9} fechar={vi.fn()} /></StrictMode>);
    await screen.findByText("Nenhuma atividade registrada."); expect(f).toHaveBeenCalledTimes(2); expect(f.mock.calls[0][1].signal.aborted).toBe(true); expect(f.mock.calls[1][1].signal.aborted).toBe(false);
    expect(screen.queryByRole("status")).toBeNull(); expect(document.activeElement).toBe(screen.getByRole("button", { name: "Fechar atividade" }));
});
test("ACT-31/34: loading, vazio, foco e controles somente leitura", async () => {
    let resolver; preparar(() => new Promise(r => { resolver = r; }));
    expect(screen.getByRole("status").textContent).toBe("Carregando atividade…"); expect(document.activeElement).toBe(screen.getByRole("button", { name: "Fechar atividade" }));
    await act(async () => resolver(json(pagina([])))); expect(screen.getByText("Nenhuma atividade registrada.").isConnected).toBe(true);
    expect(screen.queryByRole("textbox")).toBeNull(); expect(screen.getAllByRole("button").map(b => b.textContent)).toEqual(["Fechar atividade", "Atualizar"]);
});
test("ACT-30/36: ação, alvo, autor, hora e antes/depois literais escapados", async () => {
    const r = registro(); r.alteracoes = [{ alvoTipo: "DEMANDA", alvoId: 7, alvoNome: "N-7", campo: "observacoes", anterior: ' <img src=x onerror="alert(1)">\n ', novo: "á".repeat(10000) }];
    preparar(() => json(pagina([r]))); const l = await screen.findByRole("list"), item = within(l).getByRole("listitem");
    expect(item.textContent).toContain("Demanda editada"); expect(item.textContent).toContain("N-7"); expect(item.textContent).toContain("Ana"); expect(item.textContent).toContain("12/10/2026 12:34:56");
    expect(item.querySelector("time").dateTime).toBe(r.dataHora); expect(item.textContent).toContain(r.alteracoes[0].anterior); expect(item.textContent).toContain(r.alteracoes[0].novo); expect(item.querySelector("img")).toBeNull();
    expect(within(item).getByText("Antes").isConnected).toBe(true); expect(within(item).getByText("Depois").isConnected).toBe(true);
});
test("ACT-22/30/36: legado identifica origem, ação literal, ausências sem autoria confirmada", async () => {
    const r = { ...registro(), chave: "HISTORICO_EXISTENTE:100", origem: "HISTORICO_EXISTENTE", tipo: " <b>Ação antiga</b> ", autorId: null, autorNome: null, dataHora: null, descricao: null };
    preparar(() => json(pagina([r]))); await screen.findByRole("list"); expect(itens()[0].textContent).toContain("Histórico existente"); expect(itens()[0].textContent).toContain(r.tipo); expect(itens()[0].textContent).toContain("Não informado"); expect(itens()[0].querySelector("b")).toBeNull(); expect(itens()[0].querySelector("time")).toBeNull(); expect(itens()[0].textContent).not.toContain("confirmad");
});
test.each(["QUADRO_EDITADO", "ETAPA_EDITADA", "DEMANDA_EDITADA"])("ACT-16/36: %s sem mudanças explica edição vazia", async tipo => {
    const r = registro(); Object.assign(r, { tipo, alvoTipo: tipo.startsWith("QUADRO") ? "QUADRO" : tipo.startsWith("ETAPA") ? "ETAPA" : "DEMANDA", alvoId: tipo.startsWith("QUADRO") ? 9 : 7 }); preparar(() => json(pagina([r])));
    expect((await screen.findByText("Nenhum campo foi alterado")).isConnected).toBe(true);
});
test("ACT-14/30: comentário identifica envio sem mensagem nem edição vazia", async () => {
    const r = { ...registro(), tipo: "COMENTARIO_ADICIONADO" }; preparar(() => json(pagina([r]))); await screen.findByRole("list"); expect(itens()[0].textContent).toContain("Comentário adicionado"); expect(itens()[0].textContent).not.toContain("Nenhum campo"); expect(screen.queryByRole("textbox")).toBeNull();
});
test("ACT-10/36: homônimas identificam IDs da origem e destino", async () => {
    const r = { ...registro(), tipo: "DEMANDA_MOVIDA", alteracoes: [{ alvoTipo: "DEMANDA", alvoId: 7, alvoNome: "N-7", campo: "etapaId", anterior: "3", novo: "4" }, { alvoTipo: "DEMANDA", alvoId: 7, alvoNome: "N-7", campo: "etapaNome", anterior: "Trabalho", novo: "Trabalho" }] };
    preparar(() => json(pagina([r]))); await screen.findByRole("list"); expect(itens()[0].textContent).toContain("Trabalho (ID 3)"); expect(itens()[0].textContent).toContain("Trabalho (ID 4)");
});
test("ACT-36: valor removido e posição de outro alvo são compreensíveis", async () => {
    const r = { ...registro(), tipo: "ETAPA_REMOVIDA", alvoTipo: "ETAPA", alteracoes: [{ alvoTipo: "ETAPA", alvoId: 7, alvoNome: "N-7", campo: "nome", anterior: "N-7", novo: null }, { alvoTipo: "ETAPA", alvoId: 8, alvoNome: "Outra", campo: "ordem", anterior: "3", novo: "2" }] };
    preparar(() => json(pagina([r]))); await screen.findByRole("list"); expect(itens()[0].textContent).toContain("Não informado"); expect(itens()[0].textContent).toContain("Outra"); expect(itens()[0].textContent).toContain("Posição");
});
test.each(["HTTP", "rede", "contrato"])("ACT-31/37: erro%s oferece retry sem falso vazio", async modo => {
    let n = 0; const { user, f } = preparar(() => n++ ? json(pagina([])) : modo === "HTTP" ? json({ erro: "Falha" }, 500) : modo === "rede" ? Promise.reject(new TypeError("Offline")) : json({}));
    await screen.findByRole("alert"); expect(screen.queryByText("Nenhuma atividade registrada.")).toBeNull(); await user.click(screen.getByRole("button", { name: "Tentar novamente" })); await screen.findByText("Nenhuma atividade registrada."); expect(f.mock.calls.map(([, r]) => r.method)).toEqual(["GET", "GET"]);
});
test("ACT-32: acrescenta uma página uma vez e bloqueia duplo clique", async () => {
    let resolver, n = 0; const { user, f } = preparar(() => n++ ? new Promise(r => { resolver = r; }) : json(primeira())); await screen.findByRole("list");
    const botao = screen.getByRole("button", { name: "Carregar mais" }); await user.click(botao); fireEvent.click(botao); expect(f).toHaveBeenCalledTimes(2); expect(botao.disabled).toBe(true); expect(screen.getByRole("button", { name: "Atualizar" }).disabled).toBe(true);
    await act(async () => resolver(json(pagina([registro(50)])))); expect(itens()).toHaveLength(51); expect(itens()[50].dataset.atividadeChave).toBe("AUTOMATICA:50"); expect(screen.queryByRole("button", { name: "Carregar mais" })).toBeNull();
});
test.each(["HTTP", "contrato", "duplicada"])("ACT-32/37: paginação%s conserva lista e repete cursor manualmente", async modo => {
    let n = 0; const { user, f } = preparar(() => n++ === 0 ? json(primeira()) : n === 2 ? modo === "HTTP" ? json({ erro: "Falha" }, 500) : modo === "contrato" ? json({}) : json(pagina([registro(100)])) : json(pagina([registro(50)])));
    await screen.findByRole("list"); await user.click(screen.getByRole("button", { name: "Carregar mais" })); await screen.findByRole("alert"); expect(itens()).toHaveLength(50); await user.click(screen.getByRole("button", { name: "Carregar mais" })); await waitFor(() => expect(itens()).toHaveLength(51)); expect(f.mock.calls[1][0]).toBe(f.mock.calls[2][0]);
});
test.each(["HTTP", "contrato"])("ACT-33: atualização%s mantém lista e avisa, sucesso inicia novo conjunto", async modo => {
    let n = 0; const { user, f } = preparar(() => n++ === 0 ? json(primeira()) : n === 2 ? modo === "HTTP" ? json({ erro: "Falha" }, 500) : json({}) : json(pagina([registro(99)])));
    await screen.findByRole("list"); await user.click(screen.getByRole("button", { name: "Atualizar" })); await screen.findByRole("alert"); expect(itens()).toHaveLength(50); expect(screen.getByText("Atividade não atualizada.").isConnected).toBe(true);
    await user.click(screen.getByRole("button", { name: "Atualizar" })); await waitFor(() => expect(itens()).toHaveLength(1)); expect(itens()[0].dataset.atividadeChave).toBe("AUTOMATICA:99"); expect(f.mock.calls[2][0]).not.toContain("cursor="); expect(screen.queryByText("Atividade não atualizada.")).toBeNull();
});
test.each(["botão", "Escape"])("ACT-34: fechamento%s aborta leitura e chama retorno de foco", async modo => {
    let resolver; const { user, f, fechar } = preparar(() => new Promise(r => { resolver = r; }));
    if (modo === "botão") await user.click(screen.getByRole("button", { name: "Fechar atividade" })); else fireEvent.keyDown(document, { key: "Escape" });
    expect(fechar).toHaveBeenCalledTimes(1); expect(f.mock.calls[0][1].signal.aborted).toBe(true); await act(async () => resolver(json(pagina()))); expect(screen.queryByRole("list")).toBeNull();
});
test.each(["quadro", "token"])("ACT-34: mudança%s aborta e ignora resposta anterior", async modo => {
    let resolver; const { f, rerender, fechar } = preparar(() => new Promise(r => { resolver = r; })); const anterior = f.mock.calls[0][1].signal;
    const id = modo === "quadro" ? 10 : 9; if (modo === "token") localStorage.setItem("token", "outra");
    f.mockImplementation(() => json({ ...pagina([]), gerenciamentoId: id })); rerender(<AtividadeLateral gerenciamentoId={id} fechar={fechar} />); await screen.findByText("Nenhuma atividade registrada.");
    expect(anterior.aborted).toBe(true); await act(async () => resolver(json(pagina()))); expect(screen.queryByRole("list")).toBeNull(); expect(fechar).not.toHaveBeenCalled(); expect(f.mock.calls[1][1].headers.Authorization).toBe(modo === "token" ? "Bearer outra" : "Bearer sessao");
});
test.each([false, true])("ACT-34: unmount ignora resposta ou erro tardio%s", async erro => {
    let resolver, rejeitar; const { unmount, f, fechar } = preparar(() => new Promise((a, b) => { resolver = a; rejeitar = b; })); unmount(); expect(f.mock.calls[0][1].signal.aborted).toBe(true);
    await act(async () => erro ? rejeitar(new TypeError("Offline")) : resolver(json(pagina()))); expect(screen.queryByRole("complementary")).toBeNull(); expect(fechar).not.toHaveBeenCalled();
});
