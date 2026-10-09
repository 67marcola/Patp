import { expect, test } from "@playwright/test";
import { randomUUID } from "node:crypto";
import { mkdtemp } from "node:fs/promises";
import { tmpdir } from "node:os";
import { join } from "node:path";

function registroPreservado({ processo, ...campos }) {
    return { ...campos, processoId: processo.id };
}

async function preparar(page, request, status = "Em andamento") {
    const id = randomUUID();
    async function conta(nome) {
        const email = `${nome}-${id}@example.test`;
        const r = await request.post("/api/usuarios/cadastro", { data: { nome, email, setor: "Testes", senha: "teste123" } });
        expect(r.status()).toBe(200); return { ...await r.json(), email };
    }
    const dono = await conta("Dono"), outro = await conta("Outro"); const headers = { Authorization: `Bearer ${dono.token}` };
    const q = await request.post("/api/gerenciamentos", { headers, data: { nome: `Arquivo ${id}`, etapas: [{ nome: "Trabalho", setor: "Projeto", ordem: 1 }] } });
    expect(q.status()).toBe(201); const quadro = await q.json(); const base = `/api/gerenciamentos/${quadro.id}`;
    const r = await request.post(`${base}/demandas`, { headers, data: { versao: 0, numeroProcesso: `AR-${id}`, pessoa: "Cliente", responsavel: "Ana", prioridade: "Alta",
        dataEmissao: "2020-02-29", prazoEtapa: "2021-03-04", prazoGeral: null, observacoes: "Informação\nLiteral" } });
    expect(r.status()).toBe(201); let inicial = await r.json(); const demandaId = inicial.demandas[0].id;
    if (status !== "Em andamento") {
        const t = await request.put(`${base}/demandas/${demandaId}/${status === "Concluido" ? "concluir" : "cancelar"}`, {
            headers, data: { versao: inicial.gerenciamento.versao, ...(status === "Cancelado" ? { motivo: "Cliente desistiu\nMotivo completo" } : {}) } });
        expect(t.status()).toBe(200); inicial = await t.json();
    }
    const demanda = inicial.demandas[0];
    async function consultar() { const c = await request.get(`${base}/estrutura-etapas`, { headers }); expect(c.status()).toBe(200); return c.json(); }
    const card = page.locator(`[data-demanda-id="${demandaId}"]`);
    async function abrirQuadro(arquivado = false) {
        if (arquivado) await page.getByRole("button", { name: "Arquivados", exact: true }).click();
        await page.getByRole("button", { name: `Abrir ${quadro.nome}`, exact: true }).click(); await expect(card).toBeVisible();
    }
    async function entrar(contaAtual, arquivado = false) {
        await page.goto("/"); await page.getByPlaceholder("seu@email.com").fill(contaAtual.email); await page.getByPlaceholder("Digite sua senha").fill("teste123");
        await page.getByRole("button", { name: "Entrar", exact: true }).click(); await abrirQuadro(arquivado);
    }
    async function recarregar(arquivado = false) { await page.reload(); await abrirQuadro(arquivado); }
    async function sair() { await page.getByRole("button", { name: "← Voltar" }).click(); await page.getByRole("button", { name: "Sair", exact: true }).click(); }
    async function abrir(acao) {
        const b = card.getByRole("button", { name: `${acao === "arquivar" ? "Arquivar" : "Restaurar"} demanda ${demanda.numeroProcesso}`, exact: true });
        await b.focus(); await page.keyboard.press("Enter"); await expect(page.getByRole("dialog")).toBeVisible();
        await expect(page.getByRole("button", { name: "Cancelar", exact: true })).toBeFocused(); return b;
    }
    await entrar(dono);
    return { dono, outro, headers, base, demanda, inicial, consultar, card, abrir, recarregar, entrar, sair, quadro };
}

for (const status of ["Em andamento", "Concluido", "Cancelado"]) {
    test(`ARQ-05/06/13/14/17/19/20/21/23/24/26/28: arquivo real ${status}, dados, histórico, recarga e teclado`, async ({ page, request }) => {
        const c = await preparar(page, request, status);
        const comentario = await request.post(`/api/processos/${c.demanda.id}/comentarios`, { headers: c.headers, data: { texto: "Comentário\nLiteral", funcionario: "Dono" } });
        expect(comentario.status()).toBe(200);
        async function comentarios() { const r = await request.get(`/api/processos/${c.demanda.id}/comentarios`, { headers: c.headers }); expect(r.status()).toBe(200); return (await r.json()).map(registroPreservado); }
        async function historico() { const r = await request.get(`/api/processos/${c.demanda.id}/historico`, { headers: c.headers }); expect(r.status()).toBe(200); return (await r.json()).map(registroPreservado); }
        const antigos = await historico(), comentariosAntes = await comentarios(); let atual = await c.consultar(); await c.recarregar();
        expect(comentariosAntes.map(e => e.processoId)).toEqual([c.demanda.id]);
        expect(antigos.map(e => e.processoId)).toEqual(Array(antigos.length).fill(c.demanda.id));
        const origem = atual.etapas.find(e => e.id === c.demanda.etapaId);
        const botao = await c.abrir("arquivar"); await page.keyboard.press("Enter"); await expect(page.getByRole("dialog")).toHaveCount(0); await expect(botao).toBeFocused();
        expect(await c.consultar()).toEqual(atual);
        async function executar(acao) {
            await c.abrir(acao);
            if (acao === "restaurar") await expect(page.getByRole("dialog")).toContainText(`A demanda voltará à etapa ${origem.nome} com status ${status}.`);
            const esperada = page.waitForResponse(r => r.url().endsWith(`/demandas/${c.demanda.id}/${acao}`) && r.request().method() === "PUT");
            await page.getByRole("button", { name: acao === "arquivar" ? "Arquivar demanda" : "Restaurar demanda", exact: true }).focus(); await page.keyboard.press("Enter");
            const r = await esperada; expect(r.status()).toBe(200); const salvo = await r.json();
            expect(r.request().postDataJSON()).toEqual({ versao: atual.gerenciamento.versao });
            expect(salvo.gerenciamento.versao).toBe(atual.gerenciamento.versao + 1);
            expect(salvo.demandas).toEqual(acao === "arquivar" ? [] : [c.demanda]); expect(salvo.demandasArquivadas).toEqual(acao === "arquivar" ? [c.demanda] : []);
            expect(Object.keys((acao === "arquivar" ? salvo.demandasArquivadas : salvo.demandas)[0]).sort()).toEqual(Object.keys(c.demanda).sort());
            expect(salvo.etapas.map(e => e.quantidadeDemandas)).toEqual(salvo.etapas.map(e => acao === "restaurar" && e.id === c.demanda.etapaId ? 1 : 0));
            await expect(page.getByRole("dialog")).toHaveCount(0); expect(await page.evaluate(() => document.activeElement.isConnected && document.activeElement.tagName === "BUTTON")).toBe(true);
            const area = page.getByRole(acao === "arquivar" ? "complementary" : "region", { name: acao === "arquivar" ? "Arquivadas" : origem.nome });
            await expect(area.locator(`[data-demanda-id="${c.demanda.id}"]`)).toBeVisible(); await expect(c.card).toContainText(status);
            expect(await c.consultar()).toEqual(salvo); expect(await comentarios()).toEqual(comentariosAntes); atual = salvo;
            await c.recarregar(); await expect(area.locator(`[data-demanda-id="${c.demanda.id}"]`)).toBeVisible();
        }
        await executar("arquivar");
        const hArquivo = await historico(); expect(hArquivo.slice(0, antigos.length)).toEqual(antigos);
        expect(hArquivo).toHaveLength(antigos.length + 1); expect(hArquivo.at(-1)).toMatchObject({ acao: "ARQUIVAMENTO", descricao: "Demanda arquivada.", usuario: "Dono", processoId: c.demanda.id });
        expect(hArquivo.at(-1).dataHora).toMatch(/^\d{4}-\d{2}-\d{2}T/);
        const outroQuadro = await request.post("/api/gerenciamentos", { headers: c.headers,
            data: { nome: `Outro ${randomUUID()}`, etapas: [{ nome: "Trabalho", setor: "Projeto", ordem: 1 }] } });
        expect(outroQuadro.status()).toBe(201); const outroId = (await outroQuadro.json()).id;
        const duplicada = await request.post(`/api/gerenciamentos/${outroId}/demandas`, { headers: c.headers, data: { versao: 0, numeroProcesso: c.demanda.numeroProcesso.toLowerCase(), pessoa: "Outra" } });
        expect(duplicada.status()).toBe(409); expect((await duplicada.json()).erro).toBe("Já existe uma demanda com esse número."); expect(await c.consultar()).toEqual(atual);
        const exclusao = await request.delete(`/api/processos/${c.demanda.id}`, { headers: c.headers }); expect(exclusao.status()).toBe(409);
        expect((await exclusao.json()).erro).toBe("Exclusão definitiva indisponível. Use o arquivamento da demanda."); expect(await c.consultar()).toEqual(atual);
        if (status === "Em andamento") {
            const remover = await request.delete(`${c.base}/etapas/${origem.id}`, { headers: c.headers, data: { versao: atual.gerenciamento.versao } });
            expect(remover.status()).toBe(409); expect((await remover.json()).erro).toBe("Esta etapa possui demandas. Mova-as para outra etapa antes de removê-la."); expect(await c.consultar()).toEqual(atual);
        }
        if (status === "Cancelado") {
            const pasta = await mkdtemp(join(tmpdir(), "creral-arquivo-qa-"));
            for (const [nome, tamanho] of [["desktop", { width: 1280, height: 800 }], ["mobile", { width: 375, height: 812 }]]) {
                await page.setViewportSize(tamanho); expect(await page.evaluate(() => document.documentElement.scrollWidth <= document.documentElement.clientWidth)).toBe(true);
                await page.screenshot({ path: join(pasta, `${nome}-arquivo.png`), fullPage: true });
            }
            await c.abrir("restaurar"); await page.screenshot({ path: join(pasta, "mobile-confirmacao.png"), fullPage: true });
            await page.getByRole("button", { name: "Cancelar", exact: true }).click(); console.log(`QA arquivo: ${pasta}`);
        }
        await executar("restaurar");
        const hFinal = await historico(); expect(hFinal.slice(0, hArquivo.length)).toEqual(hArquivo); expect(hFinal).toHaveLength(hArquivo.length + 1);
        expect(hFinal.at(-1)).toMatchObject({ acao: "RESTAURACAO", descricao: "Demanda restaurada.", usuario: "Dono", processoId: c.demanda.id }); expect(hFinal.at(-1).dataHora).toMatch(/^\d{4}-\d{2}-\d{2}T/);
        expect(atual.demandas).toEqual([c.demanda]); expect(atual.demandas[0].status).toBe(status);
    });
}

test("ARQ-02/03/25/29: conflito exige GET manual, terceiros consultam e arquivo do quadro é independente", async ({ page, request }) => {
    const c = await preparar(page, request); await c.abrir("arquivar");
    const externa = await request.put(`${c.base}/demandas/${c.demanda.id}`, { headers: c.headers, data: { versao: c.inicial.gerenciamento.versao,
        pessoa: "Externa", responsavel: "Ana", prioridade: "Alta", dataEmissao: "2020-02-29", prazoEtapa: "2021-03-04", prazoGeral: null, observacoes: "Informação\nLiteral" } });
    expect(externa.status()).toBe(200); const modificada = await externa.json();
    await page.getByRole("button", { name: "Arquivar demanda", exact: true }).click();
    await expect(page.getByRole("alert")).toHaveText("Gerenciamento alterado por outro usuário. Atualize e tente novamente.");
    await expect(page.getByRole("button", { name: "Arquivar demanda", exact: true })).toBeDisabled(); expect(await c.consultar()).toEqual(modificada);
    await page.getByRole("button", { name: "Atualizar quadro", exact: true }).click(); await expect(page.getByRole("button", { name: "Arquivar demanda", exact: true })).toBeEnabled();
    await expect(page.getByRole("dialog")).toContainText("Externa"); expect(await c.consultar()).toEqual(modificada);
    await page.getByRole("button", { name: "Arquivar demanda", exact: true }).click(); await expect(page.getByRole("dialog")).toHaveCount(0);
    const arquivo = await c.consultar(); expect(arquivo.demandasArquivadas).toEqual(modificada.demandas); expect(arquivo.gerenciamento.versao).toBe(modificada.gerenciamento.versao + 1);
    await c.sair(); await c.entrar(c.outro); await expect(c.card.getByRole("button")).toHaveCount(0); await expect(c.card).toContainText("Externa");
    for (const acao of ["arquivar", "restaurar"]) {
        const negada = await request.put(`${c.base}/demandas/${c.demanda.id}/${acao}`, { headers: { Authorization: `Bearer ${c.outro.token}` }, data: { versao: arquivo.gerenciamento.versao } });
        expect(negada.status()).toBe(403); expect(await c.consultar()).toEqual(arquivo);
    }
    await c.sair(); const arquivarQuadro = await request.put(`${c.base}/arquivar`, { headers: c.headers, data: { versao: arquivo.gerenciamento.versao } });
    expect(arquivarQuadro.status()).toBe(204); await c.entrar(c.dono, true); await expect(c.card.getByRole("button")).toHaveCount(0);
    const noQuadroArquivo = await c.consultar(); expect(noQuadroArquivo.demandasArquivadas).toEqual(arquivo.demandasArquivadas); expect(noQuadroArquivo.demandas).toEqual([]);
    const bloqueada = await request.put(`${c.base}/demandas/${c.demanda.id}/restaurar`, { headers: c.headers, data: { versao: noQuadroArquivo.gerenciamento.versao } });
    expect(bloqueada.status()).toBe(409); expect((await bloqueada.json()).erro).toBe("Gerenciamento arquivado. Restaure-o antes de alterar."); expect(await c.consultar()).toEqual(noQuadroArquivo);
    await c.sair(); const restaurarQuadro = await request.put(`${c.base}/restaurar`, { headers: c.headers, data: { versao: noQuadroArquivo.gerenciamento.versao } }); expect(restaurarQuadro.status()).toBe(204);
    await c.entrar(c.dono); const quadroAtivo = await c.consultar(); expect(quadroAtivo.demandas).toEqual([]); expect(quadroAtivo.demandasArquivadas).toEqual(arquivo.demandasArquivadas);
    await expect(c.card.getByRole("button", { name: `Restaurar demanda ${c.demanda.numeroProcesso}` })).toBeVisible();
});
