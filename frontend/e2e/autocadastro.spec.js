import { expect, test } from "@playwright/test";
import { randomUUID } from "node:crypto";

async function focarPorTeclado(page, controle) {
    await expect(controle).toBeVisible();
    for (let tentativa = 0; tentativa < 30; tentativa += 1) {
        if (await controle.evaluate(elemento => elemento === document.activeElement)) break;
        await page.keyboard.press("Tab");
    }
    await expect(controle).toBeFocused();
}

async function acionarPorTeclado(page, controle) {
    await focarPorTeclado(page, controle);
    await page.keyboard.press("Enter");
}

async function cache(page) {
    return page.evaluate(() => ({
        token: localStorage.getItem("token"),
        usuario: JSON.parse(localStorage.getItem("usuario"))
    }));
}

test("AUT-07/09/10/14: cadastro por teclado entra direto, preserva conta/quadro e permite login após saída", async ({ page, request }) => {
    const email = `autocadastro-${randomUUID()}@example.test`;
    const senha = " senha-e2e ";
    const campos = { "Nome completo": " Nova Automática ", Setor: " Testes ", "E-mail": email, Senha: senha, "Confirmar senha": senha };
    const posts = [];
    page.on("request", requisicao => {
        const path = new URL(requisicao.url()).pathname;
        if (requisicao.method() === "POST" && ["/api/usuarios/cadastro", "/api/usuarios/login"].includes(path)) {
            posts.push({ path, body: requisicao.postDataJSON() });
        }
    });
    await page.goto("/");
    await acionarPorTeclado(page, page.getByRole("button", { name: "Criar cadastro", exact: true }));
    for (const [nome, valor] of Object.entries(campos)) {
        await focarPorTeclado(page, page.getByLabel(nome, { exact: true }));
        await page.keyboard.type(valor);
    }
    const respostaCadastro = page.waitForResponse(resposta => resposta.url().endsWith("/api/usuarios/cadastro") && resposta.request().method() === "POST");
    await acionarPorTeclado(page, page.getByRole("button", { name: "Criar cadastro", exact: true }));
    const cadastro = await respostaCadastro;
    expect(cadastro.status()).toBe(200);
    const entrada = await cadastro.json();
    expect(typeof entrada.token).toBe("string");
    expect(entrada.token.trim()).not.toBe("");
    expect(Number.isInteger(entrada.id)).toBe(true);
    expect(entrada.id).toBeGreaterThan(0);
    const usuario = { id: entrada.id, nome: "Nova Automática", setor: "Testes", email };
    expect(entrada).toEqual({ ...usuario, token: entrada.token, papel: "FUNCIONARIO" });
    await expect(page.getByRole("heading", { name: "Gerenciamentos", exact: true })).toBeVisible();
    expect(posts).toEqual([{ path: "/api/usuarios/cadastro", body: { nome: "Nova Automática", setor: "Testes", email, senha } }]);
    expect(await cache(page)).toEqual({ token: entrada.token, usuario });
    expect(Object.keys((await cache(page)).usuario).sort()).toEqual(["email", "id", "nome", "setor"]);
    const headers = { Authorization: `Bearer ${entrada.token}` };
    const me = await request.get("/api/usuarios/me", { headers });
    expect(me.status()).toBe(200);
    expect(await me.json()).toEqual({ ...usuario, papel: "FUNCIONARIO" });

    await acionarPorTeclado(page, page.getByRole("button", { name: /Criar gerenciamento/ }));
    await expect(page.getByText("As etapas finais Concluídos e Cancelados são criadas automaticamente.", { exact: true })).toBeVisible();
    await page.getByLabel("Nome do gerenciamento").fill("Quadro após cadastro");
    await page.getByLabel("Descrição", { exact: true }).fill("Dados fictícios do autocadastro.");
    const respostaQuadro = page.waitForResponse(resposta => resposta.url().endsWith("/api/gerenciamentos") && resposta.request().method() === "POST");
    await acionarPorTeclado(page, page.getByRole("button", { name: "Salvar gerenciamento", exact: true }));
    const criacao = await respostaQuadro;
    expect(criacao.status()).toBe(201);
    expect(criacao.request().postDataJSON()).toEqual({
        nome: "Quadro após cadastro", descricao: "Dados fictícios do autocadastro.", etapas: []
    });
    const quadro = await criacao.json();
    expect(quadro.id).toBeGreaterThan(0);
    expect(quadro).toMatchObject({ nome: "Quadro após cadastro", descricao: "Dados fictícios do autocadastro.", criador: { id: usuario.id, nome: usuario.nome } });
    async function estrutura(autorizacao = headers) {
        const resposta = await request.get(`/api/gerenciamentos/${quadro.id}/estrutura-etapas`, { headers: autorizacao });
        expect(resposta.status()).toBe(200);
        return resposta.json();
    }
    const etapasIniciais = (await estrutura()).etapas;
    expect(etapasIniciais).toHaveLength(2);
    for (const etapa of etapasIniciais) {
        expect(Number.isInteger(etapa.id)).toBe(true);
        expect(etapa.id).toBeGreaterThan(0);
    }
    expect(etapasIniciais[0].id).not.toBe(etapasIniciais[1].id);
    const finaisEsperadas = [
        { id: etapasIniciais[0].id, nome: "Concluídos", setor: null, ordem: 1, quantidadeDemandas: 0, categoria: "CONCLUIDA" },
        { id: etapasIniciais[1].id, nome: "Cancelados", setor: null, ordem: 2, quantidadeDemandas: 0, categoria: "CANCELADA" }
    ];
    expect(etapasIniciais).toEqual(finaisEsperadas);
    async function abrirConferirFinais() {
        await acionarPorTeclado(page, page.getByRole("button", { name: "Abrir Quadro após cadastro", exact: true }));
        await expect(page.getByRole("heading", { name: "Nenhuma etapa de trabalho cadastrada", exact: true })).toBeVisible();
        await expect(page.getByRole("region").getByRole("heading")).toHaveText(["Concluídos", "Cancelados"]);
        for (const final of finaisEsperadas) {
            const coluna = page.getByRole("region", { name: final.nome, exact: true });
            await expect(coluna.getByRole("heading")).toHaveAttribute("id", `etapa-titulo-${final.id}`);
            await expect(coluna.getByText("Etapa final obrigatória", { exact: true })).toBeVisible();
            await expect(coluna.getByText(/Setor responsável:|^Posição /)).toHaveCount(0);
            await expect(coluna.getByRole("button")).toHaveCount(0);
        }
    }
    await expect(page.getByRole("button", { name: "Abrir Quadro após cadastro", exact: true })).toBeVisible();
    await page.reload();
    await expect(page.getByRole("button", { name: "Abrir Quadro após cadastro", exact: true })).toBeVisible();
    expect(await cache(page)).toEqual({ token: entrada.token, usuario });
    expect((await estrutura()).etapas).toEqual(finaisEsperadas);
    expect(posts.map(post => post.path)).toEqual(["/api/usuarios/cadastro"]);
    const lista = await request.get("/api/gerenciamentos", { headers });
    expect(lista.status()).toBe(200);
    expect(await lista.json()).toMatchObject([{ id: quadro.id, nome: "Quadro após cadastro", descricao: "Dados fictícios do autocadastro.", criador: { id: usuario.id, nome: usuario.nome } }]);
    await abrirConferirFinais();
    await acionarPorTeclado(page, page.getByRole("button", { name: /Voltar/ }));

    await acionarPorTeclado(page, page.getByRole("button", { name: "Sair", exact: true }));
    await expect(page.getByRole("heading", { name: "Bem-vindo", exact: true })).toBeVisible();
    expect(await cache(page)).toEqual({ token: null, usuario: null });
    await page.getByPlaceholder("seu@email.com").fill(email);
    await page.getByPlaceholder("Digite sua senha").fill(senha);
    await acionarPorTeclado(page, page.getByRole("button", { name: "Entrar", exact: true }));
    await expect(page.getByRole("heading", { name: "Gerenciamentos", exact: true })).toBeVisible();
    await expect(page.getByRole("button", { name: "Abrir Quadro após cadastro", exact: true })).toBeVisible();
    expect(posts).toEqual([
        { path: "/api/usuarios/cadastro", body: { nome: "Nova Automática", setor: "Testes", email, senha } },
        { path: "/api/usuarios/login", body: { email, senha } }
    ]);
    const aposLogin = await cache(page);
    expect(aposLogin.usuario).toEqual(usuario);
    expect(aposLogin.token.trim()).not.toBe("");
    const headersLogin = { Authorization: `Bearer ${aposLogin.token}` };
    const meLogin = await request.get("/api/usuarios/me", { headers: headersLogin });
    expect(meLogin.status()).toBe(200);
    expect(await meLogin.json()).toEqual({ ...usuario, papel: "FUNCIONARIO" });
    const listaLogin = await request.get("/api/gerenciamentos", { headers: headersLogin });
    expect(listaLogin.status()).toBe(200);
    expect(await listaLogin.json()).toMatchObject([{ id: quadro.id, nome: "Quadro após cadastro", descricao: "Dados fictícios do autocadastro.", criador: { id: usuario.id, nome: usuario.nome } }]);
    expect((await estrutura(headersLogin)).etapas).toEqual(finaisEsperadas);
    await abrirConferirFinais();
});
