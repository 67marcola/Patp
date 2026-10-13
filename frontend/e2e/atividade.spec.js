import { expect, test } from "@playwright/test";
import { randomUUID } from "node:crypto";
import { mkdtemp } from "node:fs/promises";
import { tmpdir } from "node:os";
import { join } from "node:path";

async function conta(request, nome, id) {
    const email = `${nome}-${id}@example.test`, resposta = await request.post("/api/usuarios/cadastro", { data: { nome, email, setor: "Teste", senha: "teste123" } });
    expect(resposta.status()).toBe(200); return { ...await resposta.json(), email };
}
async function entrar(page, usuario, quadro, arquivado = false) {
    await page.goto("/"); await page.getByPlaceholder("seu@email.com").fill(usuario.email); await page.getByPlaceholder("Digite sua senha").fill("teste123"); await page.getByRole("button", { name: "Entrar", exact: true }).click();
    if (arquivado) await page.getByRole("button", { name: "Arquivados", exact: true }).click();
    await page.getByRole("button", { name: `Abrir ${quadro.nome}`, exact: true }).click(); await expect(page.getByRole("button", { name: "Atividade", exact: true })).toBeEnabled();
}
async function abrir(page) {
    const botao = page.getByRole("button", { name: "Atividade", exact: true }); await botao.focus(); await page.keyboard.press("Enter");
    const painel = page.getByRole("complementary", { name: "Atividade", exact: true }); await expect(painel).toBeVisible(); await expect(painel.getByRole("button", { name: "Fechar atividade" })).toBeFocused(); await expect(painel.getByRole("list", { name: "Registros de atividade" })).toBeVisible(); return painel;
}
async function capturar(page, painel, pasta, nome, tamanho) {
    await page.setViewportSize(tamanho); await painel.evaluate(e => { e.scrollTop = 0; });
    const medidas = await painel.evaluate(e => { const r = e.getBoundingClientRect(); return { esquerda: r.left, direita: r.right, largura: innerWidth, altura: innerHeight, baixo: r.bottom, semOverflow: e.scrollWidth <= e.clientWidth }; });
    expect(medidas.esquerda).toBeGreaterThanOrEqual(0); expect(medidas.direita).toBeLessThanOrEqual(medidas.largura); expect(medidas.baixo).toBeLessThanOrEqual(medidas.altura); expect(medidas.semOverflow).toBe(true);
    for (const nomeBotao of ["Fechar atividade", "Atualizar"]) { const b = painel.getByRole("button", { name: nomeBotao, exact: true }); await b.scrollIntoViewIfNeeded(); const r = await b.boundingBox(); expect(r.x).toBeGreaterThanOrEqual(0); expect(r.x + r.width).toBeLessThanOrEqual(tamanho.width); expect(r.y).toBeGreaterThanOrEqual(0); expect(r.y + r.height).toBeLessThanOrEqual(tamanho.height); }
    await painel.evaluate(e => { e.scrollTop = 0; }); await page.screenshot({ path: join(pasta, `${nome}.png`), fullPage: true });
}

test("ACT-20/30/34/35/36 T17: leitor abre/fecha atividade em quadro ativo e arquivado,desktop/360", async ({ page, request }) => {
    const id = randomUUID(), dono = await conta(request, "Dono", id), leitor = await conta(request, "Leitor", id), headers = { Authorization: `Bearer ${dono.token}` };
    const criada = await request.post("/api/gerenciamentos", { headers, data: { nome: `Atividade ${id}`, etapas: [{ nome: "Trabalho", setor: "Projeto", ordem: 1 }] } }); expect(criada.status()).toBe(201); const quadro = await criada.json();
    await entrar(page, leitor, quadro); let painel = await abrir(page); await expect(painel).toContainText("Quadro criado"); await expect(painel).toContainText("Dono"); await expect(page.getByRole("button", { name: "+ Criar processo" })).toBeDisabled();
    expect(await painel.getByRole("button").allTextContents()).toEqual(["Fechar atividade", "Atualizar"]);
    const pasta = await mkdtemp(join(tmpdir(), "creral-atividade-T17-")); await capturar(page, painel, pasta, "ativo-desktop", { width: 1440, height: 900 }); await capturar(page, painel, pasta, "ativo-360", { width: 360, height: 740 });
    await page.keyboard.press("Escape"); await expect(painel).toHaveCount(0); await expect(page.getByRole("button", { name: "Atividade", exact: true })).toBeFocused();
    const arquivo = await request.put(`/api/gerenciamentos/${quadro.id}/arquivar`, { headers, data: { versao: quadro.versao } }); expect(arquivo.status()).toBe(204);
    await page.getByRole("button", { name: "← Voltar" }).click(); await page.getByRole("button", { name: "Arquivados", exact: true }).click(); await page.getByRole("button", { name: `Abrir ${quadro.nome}`, exact: true }).click(); painel = await abrir(page);
    await expect(painel).toContainText("Quadro arquivado"); await expect(painel).toContainText("Quadro criado"); await capturar(page, painel, pasta, "arquivado-360", { width: 360, height: 740 });
    await painel.getByRole("button", { name: "Fechar atividade" }).click(); await expect(page.getByRole("button", { name: "Atividade", exact: true })).toBeFocused(); console.log(`Capturas T17: ${pasta}`);
});
