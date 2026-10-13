import { act, fireEvent, render, screen, waitFor, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { expect, test, vi } from "vitest";
import Quadro from "./Quadro";

const quadro = { id: 9, nome: "Postes", versao: 2, arquivado: false, podeAdministrar: true };
const d = { id: 7, numeroProcesso: "P-7", pessoa: "João", responsavel: null, prioridade: null, dataEmissao: null, prazoEtapa: null, prazoGeral: null, observacoes: null, status: "Em andamento", etapaId: 11, dataConclusao: null, dataCancelamento: null, motivoCancelamento: null };
const json = (v, status = 200) => new Response(JSON.stringify(v), { status });
const pagina = (id = 9) => ({ gerenciamentoId: id, registros: [], proximoCursor: null });
function snapshot(flags = {}) { return { gerenciamento: { ...quadro, ...flags }, etapas: [{ id: 11, nome: "Comprar", setor: "Projeto", categoria: "TRABALHO", ordem: 1, quantidadeDemandas: 1 }, { id: 12, nome: "Livre", setor: "Projeto", categoria: "TRABALHO", ordem: 2, quantidadeDemandas: 0 }], demandas: [d], demandasArquivadas: [{ ...d, id: 8, numeroProcesso: "P-8" }] }; }
function preparar(handler = url => json(url.endsWith("atividade") ? pagina() : snapshot())) {
    localStorage.setItem("token", "sessao"); const f = vi.fn(handler); vi.stubGlobal("fetch", f); const voltar = vi.fn(), aoOcupar = vi.fn();
    return { ...render(<Quadro gerenciamento={quadro} voltar={voltar} aoOcupar={aoOcupar} />), user: userEvent.setup(), f, voltar, aoOcupar };
}
const atividade = () => screen.getByRole("button", { name: "Atividade", exact: true });
async function pronto() { await waitFor(() => expect(atividade().disabled).toBe(false)); }
function forcarClique(b) { b.disabled = false; fireEvent.click(b); }
test.each([[true, false, "criador"], [true, false, "admin"], [false, false, "leitor"], [false, true, "leitor arquivo"]])("ACT-20/30: cabeçalho para%s/%s/%s mantém ações dos cartões", async (podeAdministrar, arquivado) => {
    const { user, f } = preparar(url => json(url.endsWith("atividade") ? pagina() : snapshot({ podeAdministrar, arquivado }))); await pronto();
    const card = screen.getByRole("article", { name: "P-7 — João" }); expect(within(card).getAllByRole("button").map(b => b.textContent)).toEqual(podeAdministrar && !arquivado ? ["Comentários", "Editar", "Mover/pular etapa", "Concluir", "Cancelar", "Arquivar"] : ["Comentários"]);
    await user.click(atividade()); await screen.findByText("Nenhuma atividade registrada."); expect(f.mock.calls[1][0]).toBe("http://localhost:8081/api/gerenciamentos/9/atividade");
});
test.each(["+ Nova etapa", "Editar etapa 1: Comprar", "Remover etapa 2: Livre", "+ Criar processo", "Editar demanda P-7", "Mover/pular etapa demanda P-7", "Concluir demanda P-7", "Cancelar demanda P-7", "Arquivar demanda P-7", "Restaurar demanda P-8"])("ACT-35: formulário%s bloqueia atividade e mantém rascunho", async nome => {
    const { user, f } = preparar(); await pronto(); await user.click(screen.getByRole("button", { name: nome, exact: true }));
    expect(atividade().disabled).toBe(true); const campo = screen.queryAllByRole("textbox")[0]; if (campo) fireEvent.change(campo, { target: { value: "Rascunho preservado" } });
    forcarClique(atividade()); expect(screen.queryByRole("complementary", { name: "Atividade", exact: true })).toBeNull(); if (campo) expect(campo.value).toBe("Rascunho preservado"); expect(f).toHaveBeenCalledTimes(1);
});
test("ACT-35: comentário aberto conserva rascunho e impede atividade", async () => {
    const { user, f } = preparar(url => json(url.endsWith("comentarios") ? { gerenciamentoId: 9, processoId: 7, quadroArquivado: false, demandaArquivada: false, comentarios: [] } : snapshot())); await pronto(); await user.click(screen.getByRole("button", { name: "Comentários da demanda P-7" }));
    const campo = await screen.findByRole("textbox", { name: "Comentário" }); fireEvent.change(campo, { target: { value: "Rascunho" } }); expect(atividade().disabled).toBe(true); forcarClique(atividade()); expect(campo.value).toBe("Rascunho"); expect(screen.queryByRole("complementary", { name: "Atividade", exact: true })).toBeNull(); expect(f).toHaveBeenCalledTimes(2);
});
test("ACT-35: atividade bloqueia handlers de comentários/formulários e Voltar", async () => {
    const { user, voltar, f } = preparar(); await pronto(); await user.click(atividade()); await screen.findByText("Nenhuma atividade registrada.");
    for (const nome of ["+ Nova etapa", "+ Criar processo", "Comentários da demanda P-7", "Editar demanda P-7", "Mover/pular etapa demanda P-7", "Arquivar demanda P-7", "Restaurar demanda P-8", "← Voltar"]) { const b = screen.getByRole("button", { name: nome, exact: true }); expect(b.disabled).toBe(true); forcarClique(b); }
    expect(screen.queryByRole("textbox")).toBeNull(); expect(screen.queryByRole("dialog")).toBeNull(); expect(screen.queryByRole("complementary", { name: "Comentários: P-7" })).toBeNull(); expect(voltar).not.toHaveBeenCalled(); expect(f).toHaveBeenCalledTimes(2);
});
test.each(["botão", "Escape"])("ACT-34: fechar%s habilita quadro e retorna foco ao cabeçalho", async modo => {
    const { user } = preparar(); await pronto(); const b = atividade(); b.focus(); await user.keyboard("{Enter}"); await screen.findByText("Nenhuma atividade registrada.");
    if (modo === "botão") await user.click(screen.getByRole("button", { name: "Fechar atividade" })); else await user.keyboard("{Escape}");
    expect(screen.queryByRole("complementary", { name: "Atividade", exact: true })).toBeNull(); expect(document.activeElement).toBe(b); expect(screen.getByRole("button", { name: "+ Nova etapa" }).disabled).toBe(false);
});
test("ACT-34: troca quadro aborta antiga, fecha painel e não rouba foco novo", async () => {
    let resolver; const { user, f, rerender, voltar } = preparar(url => url.endsWith("atividade") ? new Promise(r => { resolver = r; }) : json(snapshot())); await pronto(); await user.click(atividade()); const sinal = f.mock.calls[1][1].signal;
    f.mockImplementation(() => json(snapshot({ id: 10, nome: "Outro" }))); rerender(<Quadro gerenciamento={{ ...quadro, id: 10 }} voltar={voltar} />); await screen.findByRole("heading", { name: "Outro" }); await act(async () => resolver(json(pagina())));
    expect(sinal.aborted).toBe(true); expect(screen.queryByRole("complementary", { name: "Atividade", exact: true })).toBeNull(); expect(document.activeElement).not.toBe(atividade()); expect(voltar).not.toHaveBeenCalled();
});
test("ACT-35: gravação pendente bloqueia atividade sem perder campos", async () => {
    let resolver; const { user, f } = preparar((_url, r) => r.method === "POST" ? new Promise(a => { resolver = a; }) : json(snapshot())); await pronto(); await user.click(screen.getByRole("button", { name: "+ Nova etapa" }));
    fireEvent.change(screen.getByLabelText("Nome da etapa"), { target: { value: "Nova" } }); fireEvent.change(screen.getByLabelText("Setor responsável"), { target: { value: "Projeto" } }); await user.click(screen.getByRole("button", { name: "Salvar etapa" }));
    expect(atividade().disabled).toBe(true); forcarClique(atividade()); expect(screen.queryByRole("complementary", { name: "Atividade", exact: true })).toBeNull(); expect(screen.getByLabelText("Nome da etapa").value).toBe("Nova"); expect(f).toHaveBeenCalledTimes(2); await act(async () => resolver(json({ erro: "Falha" }, 500)));
});
