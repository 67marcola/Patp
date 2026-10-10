import { act, fireEvent, render, screen, waitFor, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { expect, test, vi } from "vitest";
import ComentariosDemanda from "./ComentariosDemanda";

const demanda = { id: 7, numeroProcesso: "P-7", pessoa: "João" };
const mensagem = (id = 21, texto = "Antiga") => ({ id, texto, funcionario: "Ana", dataHora: "2020-02-29T12:34:56.123456789", processoId: 7 });
const conversa = (comentarios = [], flags = {}) => ({ gerenciamentoId: 9, processoId: 7, quadroArquivado: false, demandaArquivada: false, comentarios, ...flags });
const json = (dados, status = 200) => new Response(JSON.stringify(dados), { status });
function preparar(handler) {
    localStorage.setItem("token", "sessao"); const f = vi.fn(handler); vi.stubGlobal("fetch", f); const fechar = vi.fn(), aoOcupar = vi.fn();
    return { ...render(<ComentariosDemanda gerenciamentoId={9} demanda={demanda} fechar={fechar} aoOcupar={aoOcupar} />), user: userEvent.setup(), f, fechar, aoOcupar };
}
function visivel(elemento) { for (let atual = elemento; atual; atual = atual.parentElement) { const estilo = getComputedStyle(atual); if (atual.hidden || estilo.display === "none" || estilo.visibility === "hidden") return false; } return elemento.isConnected; }
const campo = () => screen.getByRole("textbox", { name: "Comentário" });
const enviar = () => screen.getByRole("button", { name: "Enviar comentário", exact: true });
async function escrever(texto = "Nova") { await screen.findByRole("textbox", { name: "Comentário" }); fireEvent.change(campo(), { target: { value: texto } }); }
test("COM-18/26: identifica demanda, loading, vazio e foco de fechamento", async () => {
    let resolver; preparar(() => new Promise(r => { resolver = r; }));
    expect(screen.getByRole("complementary", { name: "Comentários: P-7" }).textContent).toContain("João"); expect(screen.getByRole("status").textContent).toBe("Carregando comentários...");
    expect(document.activeElement).toBe(screen.getByRole("button", { name: "Fechar comentários" })); expect(screen.queryByRole("textbox")).toBeNull();
    await act(async () => resolver(json(conversa()))); expect(visivel(screen.getByText("Nenhum comentário nesta demanda."))).toBe(true); expect(campo().value).toBe("");
});
test("COM-19: mostra texto escapado, quebras, autor e horário local completo", async () => {
    const texto = '  <img src=x onerror="alert(1)">\r\n segunda  '; preparar(() => json(conversa([mensagem(21, texto)])));
    const lista = await screen.findByRole("list", { name: "Mensagens" }); const item = within(lista).getByRole("listitem");
    expect(item.textContent).toContain(texto); expect(item.textContent).toContain("Ana"); expect(item.textContent).toContain("29/02/2020 12:34:56"); expect(item.querySelector("time").dateTime).toBe("2020-02-29T12:34:56.123456789"); expect(item.querySelector("img")).toBeNull(); expect(item.dataset.comentarioId).toBe("21");
});
test.each(["HTTP", "rede", "contrato"])("COM-18: GET %s falha e Tentar novamente só consulta", async caso => {
    let n = 0; const { user, f } = preparar(() => n++ ? json(conversa()) : caso === "HTTP" ? json({ erro: "Falha de leitura" }, 500) : caso === "rede" ? Promise.reject(new TypeError("Offline")) : json({}));
    await screen.findByRole("alert"); expect(screen.queryByRole("textbox")).toBeNull(); await user.click(screen.getByRole("button", { name: "Tentar novamente" })); await screen.findByRole("textbox"); expect(f.mock.calls.map(([, r]) => r.method)).toEqual(["GET", "GET"]);
});
test.each([[true, false], [false, true], [true, true]])("COM-20: flags %s/%s permitem consulta e explicam bloqueio", async (q, p) => {
    preparar(() => json(conversa([mensagem()], { quadroArquivado: q, demandaArquivada: p }))); await screen.findByRole("list");
    expect(screen.queryByRole("textbox")).toBeNull(); expect(screen.queryByRole("button", { name: "Enviar comentário" })).toBeNull(); expect(visivel(screen.getByText(q ? "Quadro arquivado: comentários somente para consulta." : "Demanda arquivada: comentários somente para consulta."))).toBe(true);
});
test.each(["", "\t\n", "\u2003", "x".repeat(10001), "😀".repeat(5001)])("COM-21: texto inválido não envia e conserva literal %s", async texto => {
    const { user, f } = preparar(() => json(conversa())); await escrever(texto); await user.click(enviar()); expect(screen.getByRole("alert").textContent).toBe("Comentário deve conter texto e ter no máximo 10000 caracteres."); expect(campo().value).toBe(texto); expect(f).toHaveBeenCalledTimes(1);
});
test.each(["😀".repeat(5000), "\u00a0"])("COM-21/23: limite e whitespace Java aceitos literalmente", async texto => {
    const { user } = preparar((_url, r) => r.method === "GET" ? json(conversa()) : json(mensagem(22, texto), 201)); await escrever(texto); await user.click(enviar()); expect(campo().value).toBe(""); expect(screen.getByRole("listitem").textContent).toContain(texto);
});
test("COM-22/23: pendente trava campo/controles/fechamento e duplo submit; sucesso completo uma vez", async () => {
    let resolver; const { user, f, fechar, aoOcupar } = preparar((_url, r) => r.method === "GET" ? json(conversa([mensagem()])) : new Promise(r => { resolver = r; })); await escrever("  Nova\n literal  "); await user.click(enviar());
    expect(campo().disabled).toBe(true); for (const b of screen.getAllByRole("button")) expect(b.disabled).toBe(true); fireEvent.submit(campo().closest("form")); fireEvent.keyDown(document, { key: "Escape" }); expect(fechar).not.toHaveBeenCalled(); expect(f).toHaveBeenCalledTimes(2); expect(aoOcupar.mock.calls).toEqual([[true]]);
    const nova = mensagem(22, "  Nova\n literal  "); nova.funcionario = "Bia"; await act(async () => resolver(json(nova, 201))); expect(campo().value).toBe("");
    const itens = screen.getAllByRole("listitem"); expect(itens).toHaveLength(2); expect(itens[1].dataset.comentarioId).toBe("22"); expect(itens[1].textContent).toContain(nova.texto); expect(itens[1].textContent).toContain("Bia"); expect(itens[1].querySelector("time").dateTime).toBe(nova.dataHora); expect(visivel(screen.getByRole("complementary"))).toBe(true); expect(aoOcupar.mock.calls).toEqual([[true], [false]]);
});
test.each(["HTTP", "rede", "JSON", "status", "contrato"])("COM-24/25: POST %s incerto conserva texto/lista e exige GET manual sem reenvio", async caso => {
    let get = 0; const { user, f } = preparar((_url, r) => {
        if (r.method === "GET") return json(conversa(get++ ? [mensagem(), mensagem(22, "Nova")] : [mensagem()]));
        return caso === "HTTP" ? json({ erro: "Conflito" }, 409) : caso === "rede" ? Promise.reject(new TypeError("Offline")) : caso === "JSON" ? new Response("{", { status: 201 }) : json(mensagem(22, caso === "contrato" ? "Outra" : "Nova"), caso === "status" ? 200 : 201);
    });
    await escrever(); await user.click(enviar()); await screen.findByRole("alert"); expect(campo().value).toBe("Nova"); expect(screen.getAllByRole("listitem")).toHaveLength(1); expect(enviar().disabled).toBe(true); expect(visivel(screen.getByText("Confira as mensagens antes de enviar novamente; o comentário pode já ter sido salvo."))).toBe(true); expect(f.mock.calls.map(([, r]) => r.method)).toEqual(["GET", "POST"]);
    await user.click(screen.getByRole("button", { name: "Atualizar comentários" })); await waitFor(() => expect(screen.getAllByRole("listitem")).toHaveLength(2)); expect(campo().value).toBe("Nova"); expect(enviar().disabled).toBe(false); expect(f.mock.calls.map(([, r]) => r.method)).toEqual(["GET", "POST", "GET"]); expect(visivel(screen.getByText("Confira as mensagens antes de enviar novamente; o comentário pode já ter sido salvo."))).toBe(true);
});
test("COM-24/25: GET manual falho conserva rascunho e envio bloqueado", async () => {
    let get = 0; const { user, f } = preparar((_url, r) => r.method === "POST" ? Promise.reject(new TypeError("Offline")) : get++ ? json({ erro: "Leitura falhou" }, 500) : json(conversa([mensagem()])));
    await escrever(); await user.click(enviar()); await screen.findByRole("alert"); await user.click(screen.getByRole("button", { name: "Atualizar comentários" })); await screen.findByText("Leitura falhou"); expect(campo().value).toBe("Nova"); expect(enviar().disabled).toBe(true); expect(screen.getAllByRole("listitem")).toHaveLength(1); expect(f.mock.calls.map(([, r]) => r.method)).toEqual(["GET", "POST", "GET"]);
});
test.each(["quadroArquivado", "demandaArquivada"])("COM-20/25: GET manual descobre %s e conserva rascunho sem oferecer envio", async flag => {
    let get = 0; const { user, f } = preparar((_url, r) => r.method === "POST" ? json({ erro: "Arquivado" }, 409) : json(conversa([], get++ ? { [flag]: true } : {})));
    await escrever(); await user.click(enviar()); await screen.findByRole("alert"); await user.click(screen.getByRole("button", { name: "Atualizar comentários" })); await waitFor(() => expect(screen.queryByRole("textbox")).toBeNull()); expect(screen.queryByRole("button", { name: "Enviar comentário" })).toBeNull(); expect(screen.getByText("Rascunho não enviado:").nextSibling.textContent).toBe("Nova"); expect(f.mock.calls.map(([, r]) => r.method)).toEqual(["GET", "POST", "GET"]);
});
test.each(["botão", "Escape"])("COM-26: fecha por %s ocioso", async modo => {
    const { user, fechar } = preparar(() => json(conversa())); await escrever(); if (modo === "botão") await user.click(screen.getByRole("button", { name: "Fechar comentários" })); else fireEvent.keyDown(document, { key: "Escape" }); expect(fechar).toHaveBeenCalledTimes(1);
});
test.each(["demanda", "quadro"])("COM-27: troca %s descarta texto e ignora GET atrasado", async modo => {
    let atrasada, get = 0; const { rerender, user, f, fechar, aoOcupar } = preparar(() => get++ === 0 ? json(conversa()) : new Promise(resolve => { atrasada = resolve; }));
    await escrever(); await user.click(screen.getByRole("button", { name: "Atualizar comentários" }));
    const nova = modo === "demanda" ? { ...demanda, id: 8, numeroProcesso: "P-8" } : demanda; const q = modo === "quadro" ? 10 : 9;
    f.mockImplementation(() => json({ ...conversa(), gerenciamentoId: q, processoId: nova.id })); rerender(<ComentariosDemanda gerenciamentoId={q} demanda={nova} fechar={fechar} aoOcupar={aoOcupar} />); await screen.findByRole("textbox"); expect(campo().value).toBe("");
    await act(async () => atrasada(json(conversa([mensagem(21, "Atrasada")])))); expect(screen.queryByText("Atrasada")).toBeNull(); expect(screen.queryByRole("alert")).toBeNull(); expect(campo().value).toBe("");
});
test.each([false, true])("COM-27: desmontagem ignora POST atrasado erro=%s", async falha => {
    let resolver, rejeitar; const { user, unmount, aoOcupar, fechar } = preparar((_url, r) => r.method === "GET" ? json(conversa()) : new Promise((a, b) => { resolver = a; rejeitar = b; }));
    await escrever(); await user.click(enviar()); unmount(); await act(async () => falha ? rejeitar(new TypeError("Offline")) : resolver(json(mensagem(22, "Nova"), 201))); expect(screen.queryByRole("complementary")).toBeNull(); expect(fechar).not.toHaveBeenCalled(); expect(aoOcupar.mock.calls).toEqual([[true], [false]]);
});
