import { expect, test } from "@playwright/test";
import { randomUUID } from "node:crypto";
import { mkdtemp } from "node:fs/promises";
import { tmpdir } from "node:os";
import { join } from "node:path";

async function preparar(page, request) {
    const id = randomUUID();
    async function conta(nome) {
        const email = `${nome}-${id}@example.test`;
        const r = await request.post("/api/usuarios/cadastro", { data: { nome, email, setor: "Testes", senha: "teste123" } });
        expect(r.status()).toBe(200); return { ...await r.json(), email };
    }
    const dono = await conta("Dono"); const outro = await conta("Outro");
    const headers = { Authorization: `Bearer ${dono.token}` };
    const r = await request.post("/api/gerenciamentos", { headers, data: { nome: `Transições ${id}`, descricao: "Fictício", etapas: [
        { nome: "Comprar", setor: "Compras", ordem: 1 }, { nome: "Verificar", setor: "Projeto", ordem: 2 }, { nome: "Instalar", setor: "Campo", ordem: 3 }
    ] } });
    expect(r.status()).toBe(201); const quadro = await r.json(); const base = `/api/gerenciamentos/${quadro.id}`;
    async function consultar() { const res = await request.get(`${base}/estrutura-etapas`, { headers }); expect(res.status()).toBe(200); return res.json(); }
    const criado = await request.post(`${base}/demandas`, { headers, data: { versao: 0, numeroProcesso: `D-${id}`, pessoa: "Cliente fictício", responsavel: "Ana", prioridade: "Urgente", dataEmissao: "2020-02-29", prazoEtapa: null, prazoGeral: null, observacoes: "Preservar" } });
    expect(criado.status()).toBe(201); const inicial = await criado.json(); const demanda = inicial.demandas[0];
    async function entrar(contaAtual) {
        await page.goto("/"); await page.getByPlaceholder("seu@email.com").fill(contaAtual.email);
        await page.getByPlaceholder("Digite sua senha").fill("teste123"); await page.getByRole("button", { name: "Entrar", exact: true }).click();
        await page.getByRole("button", { name: `Abrir ${quadro.nome}`, exact: true }).click();
        await expect(page.getByRole("article")).toHaveCount(1);
    }
    await entrar(dono);
    const card = page.getByRole("article", { name: `${demanda.numeroProcesso} — ${demanda.pessoa}`, exact: true });
    async function abrir(acao) {
        const button = card.getByRole("button", { name: `${acao} demanda ${demanda.numeroProcesso}`, exact: true });
        await button.focus(); await page.keyboard.press("Enter"); await expect(page.getByRole("dialog")).toBeVisible();
    }
    return { dono, outro, headers, quadro, base, demanda, inicial, card, abrir, consultar, entrar };
}

test("MOV-05/10/11/15/16/19/21/28/29/30/33: ciclo completo, histórico, recarga e teclado", async ({ page, request }) => {
    const c = await preparar(page, request); let atual = c.inicial;
    const trabalhos = atual.etapas.filter(e => e.categoria === "TRABALHO");
    const finais = Object.fromEntries(atual.etapas.filter(e => e.categoria !== "TRABALHO").map(e => [e.categoria, e.id]));
    async function executar(acao, botao, destino, motivo) {
        await c.abrir(acao === "mover" ? "Mover/pular etapa" : acao === "concluir" ? "Concluir" : acao === "cancelar" ? "Cancelar" : "Reabrir");
        if (destino) {
            await expect(page.getByLabel("Etapa de destino")).toBeFocused();
            expect(await page.getByLabel("Etapa de destino").locator("option").allTextContents()).toEqual(["Escolha uma etapa", ...trabalhos.filter(e => acao !== "mover" || e.id !== atual.demandas[0].etapaId).map(e => e.nome)]);
            await page.getByLabel("Etapa de destino").selectOption(String(destino));
        }
        if (motivo) await page.getByLabel("Motivo do cancelamento").fill(`  ${motivo}  `);
        const aguardada = page.waitForResponse(r => r.url().endsWith(`/demandas/${c.demanda.id}/${acao}`) && r.request().method() === "PUT");
        await page.getByRole("button", { name: botao, exact: true }).focus(); await page.keyboard.press("Enter");
        const res = await aguardada; expect(res.status()).toBe(200);
        expect(res.request().postDataJSON()).toEqual({ versao: atual.gerenciamento.versao, ...(destino ? { etapaId: destino } : {}), ...(motivo ? { motivo } : {}) });
        const salvo = await res.json();
        expect(salvo.gerenciamento.versao).toBe(atual.gerenciamento.versao + 1);
        const status = acao === "concluir" ? "Concluido" : acao === "cancelar" ? "Cancelado" : "Em andamento";
        const etapaId = destino || finais[acao === "concluir" ? "CONCLUIDA" : "CANCELADA"];
        expect(salvo.demandas[0]).toEqual({ ...c.demanda, status, etapaId,
            dataConclusao: acao === "concluir" ? salvo.demandas[0].dataConclusao : null,
            dataCancelamento: acao === "cancelar" ? salvo.demandas[0].dataCancelamento : null, motivoCancelamento: motivo || null });
        if (acao === "concluir" || acao === "cancelar") expect(salvo.demandas[0][acao === "concluir" ? "dataConclusao" : "dataCancelamento"]).toMatch(/^\d{4}-\d{2}-\d{2}$/);
        expect(salvo.etapas.map(e => e.quantidadeDemandas)).toEqual(salvo.etapas.map(e => e.id === etapaId ? 1 : 0));
        await expect(page.getByRole("dialog")).toHaveCount(0);
        await expect(c.card.locator("dt", { hasText: "Status" }).locator("+ dd")).toHaveText(status);
        expect(await page.evaluate(() => document.activeElement.isConnected && document.activeElement.tagName === "BUTTON")).toBe(true);
        expect((await c.consultar()).demandas).toEqual(salvo.demandas); atual = salvo;
    }
    await executar("mover", "Mover demanda", trabalhos[2].id);
    await executar("mover", "Mover demanda", trabalhos[0].id);
    await executar("concluir", "Confirmar conclusão");
    const conclusao = atual.demandas[0].dataConclusao;
    await executar("reabrir", "Reabrir demanda", trabalhos[1].id);
    await executar("cancelar", "Confirmar cancelamento", null, "Cliente desistiu");
    const cancelamento = atual.demandas[0].dataCancelamento;
    await executar("reabrir", "Reabrir demanda", trabalhos[0].id);
    const historico = await request.get(`/api/processos/${c.demanda.id}/historico`, { headers: c.headers }); expect(historico.status()).toBe(200);
    const eventos = await historico.json();
    expect(eventos.map(e => e.acao)).toEqual(["CRIACAO", "MUDANCA_ETAPA", "MUDANCA_ETAPA", "CONCLUSAO", "REABERTURA", "CANCELAMENTO", "REABERTURA"]);
    expect(eventos.map(e => e.usuario)).toEqual(Array(7).fill("Dono"));
    expect(eventos[4].descricao).toContain(conclusao); expect(eventos[6].descricao).toContain(cancelamento); expect(eventos[6].descricao).toContain("Cliente desistiu");
    expect(eventos.every(e => typeof e.dataHora === "string" && e.dataHora.length > 10)).toBe(true);
    await page.reload(); await page.getByRole("button", { name: `Abrir ${c.quadro.nome}`, exact: true }).click();
    await expect(c.card).toHaveAttribute("data-demanda-id", String(c.demanda.id)); expect((await c.consultar()).demandas).toEqual(atual.demandas);
    await c.abrir("Cancelar"); const pasta = await mkdtemp(join(tmpdir(), "creral-transicoes-qa-"));
    for (const [nome, tamanho] of [["desktop", { width: 1280, height: 800 }], ["mobile", { width: 375, height: 812 }]]) {
        await page.setViewportSize(tamanho);
        expect(await page.evaluate(() => document.documentElement.scrollWidth <= document.documentElement.clientWidth)).toBe(true);
        await page.screenshot({ path: join(pasta, `${nome}.png`), fullPage: true });
    }
    console.log(`QA transições: ${pasta}`);
});

test("MOV-23/31/32: versão externa, rascunho, atualização manual e bloqueio global", async ({ page, request }) => {
    const c = await preparar(page, request); await c.abrir("Cancelar");
    await page.getByLabel("Motivo do cancelamento").fill("  Manter rascunho  ");
    const externo = await request.put(`${c.base}/demandas/${c.demanda.id}/mover`, { headers: c.headers, data: { versao: 1, etapaId: c.inicial.etapas[1].id } }); expect(externo.status()).toBe(200);
    const puts = []; page.on("request", r => { if (r.method() === "PUT" && r.url().includes(`/demandas/${c.demanda.id}/`)) puts.push(r.postDataJSON()); });
    await page.getByRole("button", { name: "Confirmar cancelamento", exact: true }).click();
    await expect(page.getByRole("alert")).toHaveText("Gerenciamento alterado por outro usuário. Atualize e tente novamente.");
    await expect(page.getByLabel("Motivo do cancelamento")).toHaveValue("  Manter rascunho  ");
    await expect(page.getByRole("button", { name: "Confirmar cancelamento", exact: true })).toBeDisabled(); expect(puts).toEqual([{ versao: 1, motivo: "Manter rascunho" }]);
    await page.getByRole("button", { name: "Atualizar quadro", exact: true }).click();
    await expect(page.getByRole("button", { name: "Confirmar cancelamento", exact: true })).toBeEnabled();
    let liberar; let interceptar; const pausa = new Promise(r => { liberar = r; }); const chegou = new Promise(r => { interceptar = r; });
    await page.route(`**${c.base}/demandas/${c.demanda.id}/cancelar`, async rota => { interceptar(); await pausa; await rota.continue(); });
    await page.getByRole("button", { name: "Confirmar cancelamento", exact: true }).click(); await chegou;
    try {
        for (const nome of ["Sair", "← Voltar", "+ Criar processo", "+ Nova etapa", "Voltar sem alterar"]) await expect(page.getByRole("button", { name: nome, exact: true })).toBeDisabled();
        await expect(page.getByLabel("Motivo do cancelamento")).toBeDisabled();
    } finally { liberar(); }
    await expect(page.getByRole("dialog")).toHaveCount(0); expect(puts).toEqual([{ versao: 1, motivo: "Manter rascunho" }, { versao: 2, motivo: "Manter rascunho" }]);
    expect((await c.consultar()).demandas[0]).toMatchObject({ status: "Cancelado", motivoCancelamento: "Manter rascunho" });
});

test("MOV-02/03/28: funcionário consulta sem ações e arquivo impede transição", async ({ page, request }) => {
    const c = await preparar(page, request); await page.getByRole("button", { name: "Sair", exact: true }).click(); await c.entrar(c.outro);
    await expect(c.card.getByRole("button")).toHaveCount(0);
    const antes = await c.consultar();
    const negada = await request.put(`${c.base}/demandas/${c.demanda.id}/concluir`, { headers: { Authorization: `Bearer ${c.outro.token}` }, data: { versao: 1 } }); expect(negada.status()).toBe(403);
    expect(await c.consultar()).toEqual(antes);
    const arquivo = await request.put(`${c.base}/arquivar`, { headers: c.headers, data: { versao: 1 } }); expect(arquivo.status()).toBe(204);
    const fechada = await request.put(`${c.base}/demandas/${c.demanda.id}/concluir`, { headers: c.headers, data: { versao: 2 } }); expect(fechada.status()).toBe(409);
    expect((await fechada.json()).erro).toBe("Gerenciamento arquivado. Restaure-o antes de alterar.");
    expect((await c.consultar()).demandas).toEqual(antes.demandas);
});
