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
    const dono = await conta("Dono"); const outro = await conta("Outro"); const headers = { Authorization: `Bearer ${dono.token}` };
    const q = await request.post("/api/gerenciamentos", { headers, data: { nome: `Edição ${id}`, etapas: [{ nome: "Trabalho", setor: "Projeto", ordem: 1 }] } });
    expect(q.status()).toBe(201); const quadro = await q.json(); const base = `/api/gerenciamentos/${quadro.id}`;
    const r = await request.post(`${base}/demandas`, { headers, data: { versao: 0, numeroProcesso: `E-${id}`, pessoa: "Cliente", responsavel: "Ana",
        prioridade: "Alta", dataEmissao: "2020-02-29", prazoEtapa: null, prazoGeral: null, observacoes: "Anterior" } });
    expect(r.status()).toBe(201); const inicial = await r.json(); const demanda = inicial.demandas[0];
    async function consultar() { const c = await request.get(`${base}/estrutura-etapas`, { headers }); expect(c.status()).toBe(200); return c.json(); }
    async function abrirQuadro() { await page.getByRole("button", { name: `Abrir ${quadro.nome}`, exact: true }).click(); await expect(page.locator(`[data-demanda-id="${demanda.id}"]`)).toBeVisible(); }
    async function entrar(contaAtual) {
        await page.goto("/"); await page.getByPlaceholder("seu@email.com").fill(contaAtual.email); await page.getByPlaceholder("Digite sua senha").fill("teste123");
        await page.getByRole("button", { name: "Entrar", exact: true }).click(); await abrirQuadro();
    }
    async function recarregar() { await page.reload(); await abrirQuadro(); }
    await entrar(dono); const card = page.locator(`[data-demanda-id="${demanda.id}"]`);
    async function abrirEdicao() { const b = card.getByRole("button", { name: `Editar demanda ${demanda.numeroProcesso}`, exact: true }); await b.focus(); await page.keyboard.press("Enter"); await expect(page.getByRole("form", { name: "Editar demanda" })).toBeVisible(); }
    return { dono, outro, headers, base, demanda, inicial, consultar, entrar, recarregar, card, abrirEdicao };
}

test("EDI-05/10/15/16/20/21/24/27/28: edição real nos três estados, histórico, recarga e teclado", async ({ page, request }) => {
    const c = await preparar(page, request); let atual = c.inicial;
    const novos = { pessoa: "Maria", responsavel: null, prioridade: null, dataEmissao: null, prazoEtapa: "2020-02-29", prazoGeral: null, observacoes: "Outra\nlinha" };
    async function editar(cliente) {
        await c.abrirEdicao(); await expect(page.getByLabel("Cliente/solicitante")).toBeFocused();
        await expect(page.getByLabel("Número da demanda")).toHaveValue(c.demanda.numeroProcesso);
        await expect(page.getByLabel("Número da demanda")).toHaveAttribute("readonly", "");
        await page.getByLabel("Cliente/solicitante").fill(`  ${cliente}  `);
        for (const label of ["Responsável", "Prioridade", "Data de emissão", "Prazo geral"]) await page.getByLabel(label).fill("");
        await page.getByLabel("Prazo da etapa").fill(novos.prazoEtapa); await page.getByLabel("Observações").fill("  Outra\nlinha  ");
        const esperada = page.waitForResponse(r => r.url().endsWith(`/demandas/${c.demanda.id}`) && r.request().method() === "PUT");
        await page.getByRole("button", { name: "Salvar alterações" }).focus(); await page.keyboard.press("Enter");
        const r = await esperada; expect(r.status()).toBe(200); const salvo = await r.json();
        expect(r.request().postDataJSON()).toEqual({ versao: atual.gerenciamento.versao, ...novos, pessoa: cliente });
        expect(salvo.demandas[0]).toEqual({ ...atual.demandas[0], ...novos, pessoa: cliente });
        expect(salvo.gerenciamento.versao).toBe(atual.gerenciamento.versao + 1);
        expect(salvo.etapas.map(e => e.quantidadeDemandas)).toEqual(atual.etapas.map(e => e.quantidadeDemandas));
        await expect(page.getByRole("form", { name: "Editar demanda" })).toHaveCount(0);
        await expect(c.card.getByRole("button", { name: `Editar demanda ${c.demanda.numeroProcesso}` })).toBeFocused();
        await expect(c.card).toContainText(cliente); expect((await c.consultar()).demandas).toEqual(salvo.demandas);
        atual = salvo; await c.recarregar(); await expect(c.card).toContainText(cliente);
    }
    async function transicao(acao, extras = {}) {
        const r = await request.put(`${c.base}/demandas/${c.demanda.id}/${acao}`, { headers: c.headers, data: { versao: atual.gerenciamento.versao, ...extras } });
        expect(r.status()).toBe(200); atual = await r.json(); await c.recarregar();
    }
    await editar("Maria"); await transicao("concluir"); const conclusao = atual.demandas[0].dataConclusao;
    expect(conclusao).toMatch(/^\d{4}-\d{2}-\d{2}$/); await editar("Maria corrigida"); expect(atual.demandas[0].dataConclusao).toBe(conclusao);
    await transicao("reabrir", { etapaId: c.inicial.demandas[0].etapaId }); await transicao("cancelar", { motivo: "Cliente desistiu" });
    const cancelamento = atual.demandas[0].dataCancelamento; await editar("Cliente corrigido");
    expect(atual.demandas[0]).toMatchObject({ status: "Cancelado", dataConclusao: null, dataCancelamento: cancelamento, motivoCancelamento: "Cliente desistiu", numeroProcesso: c.demanda.numeroProcesso });
    const h = await request.get(`/api/processos/${c.demanda.id}/historico`, { headers: c.headers }); expect(h.status()).toBe(200); const eventos = await h.json();
    expect(eventos.map(e => e.acao)).toEqual(["CRIACAO", "EDICAO", "CONCLUSAO", "EDICAO", "REABERTURA", "CANCELAMENTO", "EDICAO"]);
    expect(eventos.map(e => e.usuario)).toEqual(Array(7).fill("Dono")); expect(eventos.filter(e => e.acao === "EDICAO").map(e => e.descricao)).toEqual(Array(3).fill("Processo alterado."));
    expect(eventos.every(e => typeof e.dataHora === "string" && e.dataHora.length > 10)).toBe(true);
    await c.abrirEdicao(); const pasta = await mkdtemp(join(tmpdir(), "creral-edicao-qa-"));
    for (const [nome, tamanho] of [["desktop", { width: 1280, height: 800 }], ["mobile", { width: 375, height: 812 }]]) {
        await page.setViewportSize(tamanho); expect(await page.evaluate(() => document.documentElement.scrollWidth <= document.documentElement.clientWidth)).toBe(true);
        await page.screenshot({ path: join(pasta, `${nome}.png`), fullPage: true });
    }
    console.log(`QA edição: ${pasta}`);
    const antes = await c.consultar(); await page.getByRole("button", { name: "Cancelar", exact: true }).click();
    expect(await c.consultar()).toEqual(antes);
});

test("EDI-02/14/23/26/27: conflito conserva rascunho e terceiro não edita nem renumera", async ({ page, request }) => {
    const c = await preparar(page, request); await c.abrirEdicao(); await page.getByLabel("Cliente/solicitante").fill("  Rascunho  ");
    const enviados = { versao: 1, pessoa: "Externa", responsavel: "Ana", prioridade: "Alta", dataEmissao: "2020-02-29", prazoEtapa: null, prazoGeral: null, observacoes: "Anterior" };
    const externa = await request.put(`${c.base}/demandas/${c.demanda.id}`, { headers: c.headers, data: enviados }); expect(externa.status()).toBe(200);
    await page.getByRole("button", { name: "Salvar alterações" }).click();
    await expect(page.getByRole("alert")).toHaveText("Gerenciamento alterado por outro usuário. Atualize e tente novamente.");
    await expect(page.getByLabel("Cliente/solicitante")).toHaveValue("  Rascunho  "); await expect(page.getByRole("button", { name: "Salvar alterações" })).toBeDisabled();
    await page.getByRole("button", { name: "Atualizar quadro" }).click(); await expect(page.getByRole("button", { name: "Salvar alterações" })).toBeEnabled();
    await expect(page.getByLabel("Cliente/solicitante")).toHaveValue("  Rascunho  "); await page.getByRole("button", { name: "Salvar alterações" }).click();
    await expect(page.getByRole("form", { name: "Editar demanda" })).toHaveCount(0); await expect(c.card).toContainText("Rascunho");
    const antes = await c.consultar(); expect(antes.gerenciamento.versao).toBe(3);
    await page.getByRole("button", { name: "← Voltar" }).click(); await page.getByRole("button", { name: "Sair", exact: true }).click(); await c.entrar(c.outro);
    await expect(c.card.getByRole("button", { name: `Editar demanda ${c.demanda.numeroProcesso}` })).toHaveCount(0);
    const negada = await request.put(`${c.base}/demandas/${c.demanda.id}`, { headers: { Authorization: `Bearer ${c.outro.token}` }, data: { ...enviados, versao: 3 } });
    expect(negada.status()).toBe(403); expect(await c.consultar()).toEqual(antes);
    const numero = await request.put(`/api/processos/${c.demanda.id}`, { headers: c.headers, data: { pessoa: "Outra", numeroProcesso: "Renumeração" } });
    expect(numero.status()).toBe(400); expect((await numero.json()).erro).toBe("O número da demanda não pode ser alterado após o cadastro."); expect(await c.consultar()).toEqual(antes);
});
