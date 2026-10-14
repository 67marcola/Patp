import { expect, test } from "@playwright/test";
import { randomUUID } from "node:crypto";
import { mkdtemp } from "node:fs/promises";
import { tmpdir } from "node:os";
import { join } from "node:path";

test("GRAF-01–09: dados reais, atualização, comentário preservado, leitor arquivado e 360px", async ({ page, request }) => {
    const id = randomUUID();
    async function conta(nome) {
        const email = `${nome}-${id}@example.test`;
        const r = await request.post("/api/usuarios/cadastro", { data: { nome, email, setor: "Teste", senha: "teste123" } });
        expect(r.status()).toBe(200); return { ...await r.json(), email };
    }
    const dono = await conta("Dono"), leitor = await conta("Leitor"), headers = { Authorization: `Bearer ${dono.token}` };
    const criar = await request.post("/api/gerenciamentos", { headers, data: { nome: `Gráficos ${id}`, etapas: [{ nome: "Trabalho " + "x".repeat(70), setor: "Campo", ordem: 1 }] } });
    expect(criar.status()).toBe(201); const quadro = await criar.json(), base = `/api/gerenciamentos/${quadro.id}`;
    const consulta = await request.get(`${base}/estrutura-etapas`, { headers }); expect(consulta.status()).toBe(200); let s = await consulta.json();
    for (let i = 1; i <= 3; i++) {
        const r = await request.post(`${base}/demandas`, { headers, data: { versao: s.gerenciamento.versao, numeroProcesso: `G${i}-${id}`, pessoa: `Cliente ${i}` } });
        expect(r.status()).toBe(201); s = await r.json();
    }
    const demandas = [...s.demandas];
    for (const [demanda, acao, extras] of [[demandas[1], "concluir", {}], [demandas[2], "cancelar", { motivo: "Teste" }]]) {
        const r = await request.put(`${base}/demandas/${demanda.id}/${acao}`, { headers, data: { versao: s.gerenciamento.versao, ...extras } });
        expect(r.status()).toBe(200); s = await r.json();
    }
    async function entrar(usuario, arquivado = false) {
        await page.goto("/"); await page.getByPlaceholder("seu@email.com").fill(usuario.email); await page.getByPlaceholder("Digite sua senha").fill("teste123");
        await page.getByRole("button", { name: "Entrar", exact: true }).click();
        if (arquivado) await page.getByRole("button", { name: "Arquivados", exact: true }).click();
        await page.getByRole("button", { name: `Abrir ${quadro.nome}`, exact: true }).click();
    }
    await entrar(dono);
    const botao = page.getByRole("button", { name: "Gráficos", exact: true }), grafico = page.getByRole("region", { name: "Gráficos do quadro" });
    await expect(botao).toBeEnabled(); await expect(botao).toHaveAttribute("aria-expanded", "false");
    await botao.focus(); await page.keyboard.press("Enter"); await expect(botao).toBeFocused();
    await expect(grafico).toContainText("Total não arquivadas: 3"); await expect(grafico).toContainText("Arquivadas: 0");
    expect(await grafico.getByRole("group", { name: "Demandas por situação" }).getByRole("listitem").allTextContents()).toEqual(["Em andamento1", "Concluídas1", "Canceladas1"]);
    expect(await grafico.getByRole("group", { name: "Demandas por etapa" }).getByRole("listitem").allTextContents()).toEqual(s.etapas.map(e => `${e.nome}1`));
    await page.getByRole("button", { name: `Comentários da demanda ${demandas[0].numeroProcesso}` }).click();
    await page.getByRole("textbox", { name: "Comentário", exact: true }).fill("Meu rascunho");
    await botao.click(); await botao.click(); await expect(page.getByRole("textbox", { name: "Comentário", exact: true })).toHaveValue("Meu rascunho");
    await page.getByRole("button", { name: "Fechar comentários" }).click();
    await page.getByRole("button", { name: `Arquivar demanda ${demandas[0].numeroProcesso}`, exact: true }).click();
    await page.getByRole("dialog").getByRole("button", { name: "Arquivar demanda", exact: true }).click();
    await expect(grafico).toContainText("Total não arquivadas: 2"); await expect(grafico).toContainText("Arquivadas: 1");
    expect(await grafico.getByRole("group", { name: "Demandas por situação" }).getByRole("listitem").allTextContents()).toEqual(["Em andamento0", "Concluídas1", "Canceladas1"]);
    const pasta = await mkdtemp(join(tmpdir(), "creral-graficos-"));
    await page.screenshot({ path: join(pasta, "desktop.png"), fullPage: true });
    const atual = await request.get(`${base}/estrutura-etapas`, { headers }); expect(atual.status()).toBe(200); s = await atual.json();
    const arquivo = await request.put(`${base}/arquivar`, { headers, data: { versao: s.gerenciamento.versao } }); expect(arquivo.status()).toBe(204);
    await page.getByRole("button", { name: "← Voltar" }).click(); await page.getByRole("button", { name: "Sair", exact: true }).click();
    await entrar(leitor, true); await expect(botao).toBeEnabled(); await expect(grafico).toHaveCount(0); await botao.click();
    await expect(grafico).toContainText("Total não arquivadas: 2"); await expect(grafico).toContainText("Arquivadas: 1");
    await page.setViewportSize({ width: 360, height: 740 });
    expect(await grafico.evaluate(e => { const r = e.getBoundingClientRect(); return e.scrollWidth <= e.clientWidth && r.left >= 0 && r.right <= innerWidth; })).toBe(true);
    await page.screenshot({ path: join(pasta, "arquivado-360.png"), fullPage: true });
    await botao.click(); await expect(grafico).toHaveCount(0); console.log(`Capturas gráficos: ${pasta}`);
});
