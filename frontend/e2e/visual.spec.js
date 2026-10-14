import { expect, test } from "@playwright/test";
import { randomUUID } from "node:crypto";
import { mkdtemp } from "node:fs/promises";
import { tmpdir } from "node:os";
import { join } from "node:path";

test("VIS-01–07: identidade, foco, medidas e estados em computador/celular", async ({ page, request }) => {
    const pasta = await mkdtemp(join(tmpdir(), "creral-visual-")), id = randomUUID();
    const email = `visual-${id}@example.test`, senha = "teste123";
    const conta = await request.post("/api/usuarios/cadastro", { data: { nome: "Funcionário " + "Nome".repeat(18), email, setor: "Campo", senha } });
    expect(conta.status()).toBe(200); const usuario = await conta.json(), headers = { Authorization: `Bearer ${usuario.token}` };
    const chamadas = []; page.on("request", r => { if (new URL(r.url()).pathname.startsWith("/api/")) chamadas.push(r.method() + " " + r.url()); });
    async function conferir(nome) {
        for (const width of [1280, 360]) {
            await page.setViewportSize({ width, height: 900 });
            const medidas = await page.evaluate(() => {
                const controles = [...document.querySelectorAll("button,input,select,textarea")].filter(e => e.getClientRects().length);
                return { pagina: document.documentElement.scrollWidth <= innerWidth, controles: controles.map(e => {
                    const r = e.getBoundingClientRect(); return { nome: e.getAttribute("aria-label") || e.textContent || e.id, altura: r.height, dentro: r.left >= 0 && r.right <= innerWidth, fonte: getComputedStyle(e).fontFamily === getComputedStyle(document.body).fontFamily };
                }) };
            });
            expect(medidas.pagina, `${nome}/${width}: página`).toBe(true);
            for (const c of medidas.controles) {
                expect(c.altura, `${nome}/${width}/${c.nome}: altura`).toBeGreaterThanOrEqual(44);
                expect(c.dentro, `${nome}/${width}/${c.nome}: largura`).toBe(true);
                expect(c.fonte, `${nome}/${width}/${c.nome}: fonte`).toBe(true);
            }
            await page.screenshot({ path: join(pasta, `${nome}-${width}.png`), fullPage: true });
        }
    }
    async function foco(controle, escuro = false) {
        await controle.focus(); await page.keyboard.press("Tab"); await page.keyboard.press("Shift+Tab");
        await expect(controle).toBeFocused();
        await expect.poll(() => controle.evaluate(e => { const s = getComputedStyle(e); return [e.matches(":focus-visible"), s.outlineStyle, s.outlineWidth, s.outlineOffset, s.outlineColor]; }))
            .toEqual([true, "solid", "3px", "3px", escuro ? "rgb(139, 224, 187)" : "rgb(20, 125, 100)"]);
    }
    await page.goto("/"); await expect(page).toHaveTitle("Creral | Gestão de demandas"); await expect(page.locator("html")).toHaveAttribute("lang", "pt-BR");
    await expect(page.getByText("Creral", { exact: true })).toBeVisible(); await expect(page.getByRole("heading", { name: "Bem-vindo" })).toBeVisible();
    await page.locator('label[for="login-email"]').click(); await expect(page.getByLabel("E-mail", { exact: true })).toBeFocused();
    await expect(page.getByLabel("E-mail", { exact: true })).toHaveAttribute("autocomplete", "username");
    await page.locator('label[for="login-senha"]').click(); await expect(page.getByLabel("Senha", { exact: true })).toBeFocused();
    await expect(page.getByLabel("Senha", { exact: true })).toHaveAttribute("autocomplete", "current-password");
    await foco(page.getByLabel("E-mail", { exact: true })); await conferir("login");
    expect(await page.evaluate(() => { const s = getComputedStyle(document.documentElement); return [s.getPropertyValue("--azul").trim(), s.getPropertyValue("--verde").trim(), s.getPropertyValue("--texto-secundario").trim()]; })).toEqual(["#14232f", "#5db28f", "#52616b"]);
    expect(await page.locator(".login-subtitulo").evaluate(e => getComputedStyle(e).color)).toBe("rgb(82, 97, 107)");
    await page.getByRole("button", { name: "Criar cadastro", exact: true }).click();
    await expect(page.getByText("Creral", { exact: true })).toBeVisible(); await expect(page.getByRole("heading", { name: "Criar cadastro" })).toBeVisible(); await conferir("cadastro");
    await page.getByRole("button", { name: "Voltar para login" }).click();
    const filtro = /\/api\/gerenciamentos\?arquivado=false$/;
    let liberar;
    await page.route(filtro, route => new Promise(resolve => { liberar = async () => { await route.fulfill({ status: 500, json: { erro: "Falha visual de consulta" } }); resolve(); }; }));
    await page.getByLabel("E-mail", { exact: true }).fill(email); await page.getByLabel("Senha", { exact: true }).fill(senha);
    await page.getByRole("button", { name: "Entrar", exact: true }).click();
    await expect(page.getByRole("status")).toHaveText("Carregando gerenciamentos..."); await conferir("carregando");
    await liberar(); await expect(page.getByRole("alert")).toHaveText("Falha visual de consulta"); await conferir("erro");
    expect(await page.locator(".falha-consulta").evaluate(e => getComputedStyle(e).backgroundColor)).toBe("rgb(255, 245, 244)");
    await page.unroute(filtro); await page.getByRole("button", { name: "Tentar atualizar" }).click();
    await expect(page.getByRole("heading", { name: "Nenhum gerenciamento ativo" })).toBeVisible(); await conferir("lista-vazia");
    await expect(page.getByRole("heading", { name: "Creral", exact: true })).toBeVisible(); await foco(page.getByRole("button", { name: "Sair", exact: true }), true);
    await page.getByRole("button", { name: "+ Criar gerenciamento" }).click();
    await page.getByLabel("Nome do gerenciamento", { exact: true }).fill("Rascunho visual");
    await page.getByRole("button", { name: "+ Adicionar etapa" }).click();
    await foco(page.getByLabel("Descrição", { exact: true })); await conferir("formulario-quadro");
    await expect(page.getByLabel("Nome do gerenciamento", { exact: true })).toHaveValue("Rascunho visual");
    await page.getByRole("button", { name: "Cancelar", exact: true }).click();
    const nome = "Quadro " + "X".repeat(100), etapa = "Etapa " + "Y".repeat(100);
    const criado = await request.post("/api/gerenciamentos", { headers, data: { nome, descricao: "D".repeat(240), etapas: [{ nome: etapa, setor: "Campo", ordem: 1 }] } });
    expect(criado.status()).toBe(201); const quadro = await criado.json(), base = `/api/gerenciamentos/${quadro.id}`;
    const consulta = await request.get(`${base}/estrutura-etapas`, { headers }); expect(consulta.status()).toBe(200); let s = await consulta.json();
    const criada = await request.post(`${base}/demandas`, { headers, data: { versao: s.gerenciamento.versao, numeroProcesso: "V".repeat(180), pessoa: "Cliente " + "Z".repeat(180) } });
    expect(criada.status()).toBe(201); s = await criada.json(); const demanda = s.demandas[0];
    await page.getByRole("button", { name: "Arquivados", exact: true }).click(); await page.getByRole("button", { name: "Ativos", exact: true }).click();
    await expect(page.getByRole("button", { name: `Abrir ${nome}`, exact: true })).toBeVisible(); await conferir("lista");
    await page.getByRole("button", { name: `Abrir ${nome}`, exact: true }).click(); await expect(page.getByRole("button", { name: "+ Nova etapa" })).toBeEnabled();
    const cache = await page.evaluate(() => [localStorage.getItem("token"), localStorage.getItem("usuario")]);
    const antes = chamadas.length; await conferir("quadro"); expect(chamadas.length).toBe(antes);
    expect(await page.evaluate(() => [localStorage.getItem("token"), localStorage.getItem("usuario")])).toEqual(cache);
    await page.getByRole("button", { name: "+ Nova etapa" }).click(); await foco(page.getByLabel("Posição", { exact: true })); await conferir("editor-etapa"); await page.getByRole("button", { name: "Cancelar", exact: true }).click();
    await page.getByRole("button", { name: `Editar demanda ${demanda.numeroProcesso}`, exact: true }).click();
    await page.getByLabel("Cliente/solicitante", { exact: true }).fill("Rascunho da demanda"); await conferir("editor-demanda");
    await expect(page.getByLabel("Cliente/solicitante", { exact: true })).toHaveValue("Rascunho da demanda");
    await page.getByRole("button", { name: "Cancelar", exact: true }).click();
    await page.getByRole("button", { name: `Mover/pular etapa demanda ${demanda.numeroProcesso}`, exact: true }).click();
    await conferir("mover"); await page.getByRole("button", { name: "Voltar sem alterar" }).click();
    await page.getByRole("button", { name: `Arquivar demanda ${demanda.numeroProcesso}`, exact: true }).click(); await conferir("arquivo-demanda"); await page.getByRole("button", { name: "Cancelar", exact: true }).click();
    await page.getByRole("button", { name: `Comentários da demanda ${demanda.numeroProcesso}`, exact: true }).click();
    await page.getByRole("textbox", { name: "Comentário", exact: true }).fill("Rascunho da conversa"); await conferir("comentarios");
    await expect(page.getByRole("textbox", { name: "Comentário", exact: true })).toHaveValue("Rascunho da conversa"); await page.getByRole("button", { name: "Fechar comentários" }).click();
    await page.getByRole("button", { name: "Atividade", exact: true }).click(); await expect(page.getByRole("list", { name: "Registros de atividade" })).toBeVisible(); await conferir("atividade"); await page.getByRole("button", { name: "Fechar atividade" }).click();
    const arquivado = await request.put(`${base}/arquivar`, { headers, data: { versao: s.gerenciamento.versao } }); expect(arquivado.status()).toBe(204);
    await page.getByRole("button", { name: "← Voltar" }).click(); await page.getByRole("button", { name: "Arquivados", exact: true }).click(); await page.getByRole("button", { name: `Abrir ${nome}`, exact: true }).click();
    await expect(page.getByText("Arquivado — somente consulta", { exact: true })).toBeVisible(); await expect(page.getByRole("button", { name: "+ Nova etapa" })).toHaveCount(0); await conferir("arquivado");
    console.log(`Capturas visual: ${pasta}`);
});
