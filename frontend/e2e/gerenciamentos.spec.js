import { expect, test } from "@playwright/test";
import { randomUUID } from "node:crypto";
import { mkdtemp } from "node:fs/promises";
import { tmpdir } from "node:os";
import { join } from "node:path";

async function acionarPorTeclado(page, controle) {
    await expect(controle).toBeVisible();
    for (let tentativa = 0; tentativa < 30; tentativa += 1) {
        if (await controle.evaluate(elemento => elemento === document.activeElement)) break;
        await page.keyboard.press("Tab");
    }
    await expect(controle).toBeFocused();
    await page.keyboard.press("Enter");
}

test("GER-35: CRUD real conserva etapas, estado e campos após recarregar", async ({ page, request }) => {
    const email = `e2e-${randomUUID()}@example.test`;
    const senha = "teste123";
    const cadastro = await request.post("/api/usuarios/cadastro", {
        data: { nome: "Teste Creral", setor: "Testes", email, senha }
    });
    expect(cadastro.status()).toBe(200);
    const usuario = await cadastro.json();
    await page.goto("/");
    await page.locator('input[type="email"]').fill(email);
    await page.locator('input[type="password"]').fill(senha);
    await acionarPorTeclado(page, page.getByRole("button", { name: "Entrar", exact: true }));
    await expect(page.getByRole("heading", { name: "Gerenciamentos", exact: true })).toBeVisible();
    await acionarPorTeclado(page, page.getByRole("button", { name: /Criar gerenciamento/ }));
    await page.getByLabel("Nome do gerenciamento").fill("Instalação de postes");
    await page.getByLabel("Descrição", { exact: true }).fill("Quadro fictício para teste do CRUD.");
    await acionarPorTeclado(page, page.getByRole("button", { name: /Adicionar etapa/ }));
    await page.getByLabel("Nome da etapa 1", { exact: true }).fill("Planejamento");
    await page.getByLabel("Setor responsável da etapa 1", { exact: true }).fill("Técnico");
    await acionarPorTeclado(page, page.getByRole("button", { name: "Salvar gerenciamento", exact: true }));
    await expect(page.getByRole("button", { name: "Abrir Instalação de postes", exact: true })).toBeVisible();
    await page.reload();
    await expect(page.getByRole("button", { name: "Abrir Instalação de postes", exact: true })).toBeVisible();
    const token = await page.evaluate(() => localStorage.getItem("token"));
    const headers = { Authorization: `Bearer ${token}` };
    const lista = await request.get("/api/gerenciamentos", { headers });
    expect(lista.status()).toBe(200);
    const registros = await lista.json();
    expect(registros).toHaveLength(1);
    const criado = registros[0];
    expect(criado).toMatchObject({ nome: "Instalação de postes", descricao: "Quadro fictício para teste do CRUD.", criador: { id: usuario.id, nome: "Teste Creral" }, arquivado: false, versao: 0, podeAdministrar: true });
    const etapasResponse = await request.get(`/api/gerenciamentos/${criado.id}/etapas`, { headers });
    expect(etapasResponse.status()).toBe(200);
    const etapasIniciais = await etapasResponse.json();
    expect(etapasIniciais).toMatchObject([{ nome: "Planejamento", setor: "Técnico", ordem: 1 }]);

    await acionarPorTeclado(page, page.getByRole("button", { name: "Editar Instalação de postes", exact: true }));
    await expect(page.getByLabel("Nome do gerenciamento")).toHaveValue("Instalação de postes");
    await page.getByLabel("Nome do gerenciamento").fill("Postes em acompanhamento");
    await page.getByLabel("Descrição", { exact: true }).fill("Descrição corrigida e persistida.");
    await acionarPorTeclado(page, page.getByRole("button", { name: "Salvar alterações", exact: true }));
    await expect(page.getByRole("button", { name: "Abrir Postes em acompanhamento", exact: true })).toBeVisible();
    await page.reload();
    await expect(page.getByRole("button", { name: "Abrir Postes em acompanhamento", exact: true })).toBeVisible();
    await expect(page.getByText("Descrição corrigida e persistida.", { exact: true })).toBeVisible();

    const imagens = await mkdtemp(join(tmpdir(), "creral-gerenciamentos-e2e-"));
    await page.screenshot({ path: join(imagens, "ativos-desktop.png"), fullPage: true });
    await page.setViewportSize({ width: 375, height: 812 });
    expect(await page.evaluate(() => document.documentElement.scrollWidth <= document.documentElement.clientWidth)).toBe(true);
    await page.screenshot({ path: join(imagens, "ativos-mobile.png"), fullPage: true });
    await page.setViewportSize({ width: 1280, height: 800 });

    const arquivar = page.getByRole("button", { name: "Arquivar Postes em acompanhamento", exact: true });
    await acionarPorTeclado(page, arquivar);
    const dialogo = page.getByRole("dialog", { name: "Arquivar Postes em acompanhamento?", exact: true });
    await expect(dialogo).toContainText("Os dados e vínculos serão preservados");
    await acionarPorTeclado(page, dialogo.getByRole("button", { name: "Cancelar", exact: true }));
    await expect(arquivar).toBeFocused();
    await acionarPorTeclado(page, arquivar);
    await acionarPorTeclado(page, dialogo.getByRole("button", { name: "Arquivar", exact: true }));
    await expect(page.getByRole("heading", { name: "Nenhum gerenciamento ativo", exact: true })).toBeVisible();
    await expect(page.getByRole("button", { name: "Ativos", exact: true })).toHaveAttribute("aria-pressed", "true");
    await acionarPorTeclado(page, page.getByRole("button", { name: "Arquivados", exact: true }));
    await acionarPorTeclado(page, page.getByRole("button", { name: "Abrir Postes em acompanhamento", exact: true }));
    await expect(page.getByText("Arquivado — somente consulta", { exact: true })).toBeVisible();
    await expect(page.getByRole("heading", { name: "Planejamento", exact: true })).toBeVisible();
    await expect(page.getByText("Setor responsável: Técnico", { exact: true })).toBeVisible();
    await expect(page.getByRole("button", { name: /Criar processo/ })).toHaveCount(0);
    await page.screenshot({ path: join(imagens, "arquivado-desktop.png"), fullPage: true });
    await page.setViewportSize({ width: 375, height: 812 });
    expect(await page.evaluate(() => document.documentElement.scrollWidth <= document.documentElement.clientWidth)).toBe(true);
    await page.screenshot({ path: join(imagens, "arquivado-mobile.png"), fullPage: true });
    await page.setViewportSize({ width: 1280, height: 800 });
    console.log(`Screenshots: ${imagens}`);

    await page.reload();
    await acionarPorTeclado(page, page.getByRole("button", { name: "Arquivados", exact: true }));
    await expect(page.getByRole("button", { name: "Restaurar Postes em acompanhamento", exact: true })).toBeVisible();
    const arquivadoResponse = await request.get(`/api/gerenciamentos/${criado.id}`, { headers });
    expect(arquivadoResponse.status()).toBe(200);
    expect(await arquivadoResponse.json()).toMatchObject({ id: criado.id, nome: "Postes em acompanhamento", descricao: "Descrição corrigida e persistida.", criador: { id: usuario.id, nome: "Teste Creral" }, arquivado: true, versao: 2 });
    await acionarPorTeclado(page, page.getByRole("button", { name: "Restaurar Postes em acompanhamento", exact: true }));
    await expect(page.getByRole("heading", { name: "Nenhum gerenciamento arquivado", exact: true })).toBeVisible();
    await expect(page.getByRole("button", { name: "Arquivados", exact: true })).toHaveAttribute("aria-pressed", "true");
    await acionarPorTeclado(page, page.getByRole("button", { name: "Ativos", exact: true }));
    await expect(page.getByRole("button", { name: "Abrir Postes em acompanhamento", exact: true })).toBeVisible();
    await page.reload();
    await expect(page.getByRole("button", { name: "Abrir Postes em acompanhamento", exact: true })).toBeVisible();
    const restauradoResponse = await request.get(`/api/gerenciamentos/${criado.id}`, { headers });
    expect(restauradoResponse.status()).toBe(200);
    expect(await restauradoResponse.json()).toMatchObject({ id: criado.id, nome: "Postes em acompanhamento", descricao: "Descrição corrigida e persistida.", criador: { id: usuario.id, nome: "Teste Creral" }, arquivado: false, versao: 3 });
    const etapasRestauradasResponse = await request.get(`/api/gerenciamentos/${criado.id}/etapas`, { headers });
    expect(etapasRestauradasResponse.status()).toBe(200);
    const etapasRestauradas = await etapasRestauradasResponse.json();
    const campos = ({ id, nome, setor, ordem }) => ({ id, nome, setor, ordem });
    expect(etapasRestauradas.map(campos)).toEqual(etapasIniciais.map(campos));
});
