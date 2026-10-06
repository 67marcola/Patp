import { expect, test } from "@playwright/test";
import { randomUUID } from "node:crypto";
import { mkdtemp } from "node:fs/promises";
import { tmpdir } from "node:os";
import { join } from "node:path";

async function focarPorTab(page, controle) {
    await expect(controle).toBeVisible();
    for (let tentativa = 0; tentativa < 40; tentativa += 1) {
        if (await controle.evaluate(elemento => elemento === document.activeElement)) break;
        await page.keyboard.press("Tab");
    }
    await expect(controle).toBeFocused();
}

async function acionar(page, controle) {
    await focarPorTab(page, controle);
    await page.keyboard.press("Enter");
}

async function preencher(page, controle, texto) {
    await focarPorTab(page, controle);
    await page.keyboard.press("Control+A");
    await page.keyboard.type(texto);
}

async function selecionarPosicao(page, posicao) {
    const controle = page.getByLabel("Posição", { exact: true });
    await focarPorTab(page, controle);
    await page.keyboard.press("Enter");
    await page.keyboard.press("Home");
    for (let index = 1; index < posicao; index += 1) await page.keyboard.press("ArrowDown");
    await page.keyboard.press("Enter");
    await expect(controle).toHaveValue(String(posicao));
}

async function fotografar(page, pasta, nome) {
    await page.setViewportSize({ width: 1280, height: 800 });
    expect(await page.evaluate(() => document.documentElement.scrollWidth <= document.documentElement.clientWidth)).toBe(true);
    await page.screenshot({ path: join(pasta, `${nome}-desktop.png`), fullPage: true });
    await page.setViewportSize({ width: 375, height: 812 });
    expect(await page.evaluate(() => document.documentElement.scrollWidth <= document.documentElement.clientWidth)).toBe(true);
    await page.screenshot({ path: join(pasta, `${nome}-mobile.png`), fullPage: true });
    await page.setViewportSize({ width: 1280, height: 800 });
}

test("ETA-12/13/15/22/25/28/30: CRUD real com teclado, posições, IDs, reload e arquivamento com cache atual", async ({ page, request }) => {
    const email = `etapas-${randomUUID()}@example.test`;
    const cadastro = await request.post("/api/usuarios/cadastro", {
        data: { nome: "Teste Etapas", setor: "Testes", email, senha: "teste123" }
    });
    expect(cadastro.status()).toBe(200);
    const usuario = await cadastro.json();
    await page.goto("/");
    await preencher(page, page.locator('input[type="email"]'), email);
    await preencher(page, page.locator('input[type="password"]'), "teste123");
    await acionar(page, page.getByRole("button", { name: "Entrar", exact: true }));
    await expect(page.getByRole("heading", { name: "Gerenciamentos", exact: true })).toBeVisible();
    const token = await page.evaluate(() => localStorage.getItem("token"));
    const headers = { Authorization: `Bearer ${token}` };
    const criacao = await request.post("/api/gerenciamentos", {
        headers, data: { nome: "Fluxo de instalação", descricao: "Configuração fictícia de etapas da Creral.", etapas: [] }
    });
    expect(criacao.status()).toBe(201);
    const quadro = await criacao.json();
    expect(quadro).toMatchObject({ nome: "Fluxo de instalação", versao: 0, arquivado: false, podeAdministrar: true, criador: { id: usuario.id, nome: "Teste Etapas" } });
    async function consultar() {
        const resposta = await request.get(`/api/gerenciamentos/${quadro.id}/estrutura-etapas`, { headers });
        expect(resposta.status()).toBe(200);
        return resposta.json();
    }
    async function abrir() {
        await acionar(page, page.getByRole("button", { name: "Abrir Fluxo de instalação", exact: true }));
        await expect(page.getByRole("heading", { name: "Fluxo de instalação", exact: true })).toBeVisible();
    }
    async function criar(nome, setor, posicao, quantidadeTrabalhos) {
        await acionar(page, page.getByRole("button", { name: "+ Nova etapa", exact: true }));
        await expect(page.getByLabel("Posição", { exact: true }).locator("option"))
            .toHaveText(Array.from({ length: quantidadeTrabalhos + 1 }, (_, index) => String(index + 1)));
        await preencher(page, page.getByLabel("Nome da etapa", { exact: true }), nome);
        await preencher(page, page.getByLabel("Setor responsável", { exact: true }), setor);
        await selecionarPosicao(page, posicao);
        await acionar(page, page.getByRole("button", { name: "Salvar etapa", exact: true }));
        await expect(page.getByRole("heading", { name: nome, exact: true })).toBeVisible();
    }
    const campos = ({ id, nome, setor, ordem, quantidadeDemandas, categoria }) => ({ id, nome, setor, ordem, quantidadeDemandas, categoria });
    const semTrabalhos = await consultar();
    expect(semTrabalhos.etapas).toHaveLength(2);
    const concluidaId = semTrabalhos.etapas[0].id;
    const canceladaId = semTrabalhos.etapas[1].id;
    expect(Number.isInteger(concluidaId)).toBe(true);
    expect(Number.isInteger(canceladaId)).toBe(true);
    expect(concluidaId).toBeGreaterThan(0);
    expect(canceladaId).toBeGreaterThan(0);
    expect(canceladaId).not.toBe(concluidaId);
    const finaisEsperadas = [
        { id: concluidaId, nome: "Concluídos", setor: null, ordem: 1, quantidadeDemandas: 0, categoria: "CONCLUIDA" },
        { id: canceladaId, nome: "Cancelados", setor: null, ordem: 2, quantidadeDemandas: 0, categoria: "CANCELADA" }
    ];
    expect(semTrabalhos.etapas.map(campos)).toEqual(finaisEsperadas);
    async function conferirFinaisNaTela() {
        for (const final of finaisEsperadas) {
            const coluna = page.getByRole("region", { name: final.nome, exact: true });
            await expect(coluna.getByRole("heading")).toHaveAttribute("id", `etapa-titulo-${final.id}`);
            await expect(coluna.getByText("Etapa final obrigatória", { exact: true })).toBeVisible();
            await expect(coluna.getByText("0 demandas", { exact: true })).toBeVisible();
            await expect(coluna.getByText(/Setor responsável:/)).toHaveCount(0);
            await expect(coluna.getByText(/^Posição /)).toHaveCount(0);
            await expect(coluna.getByRole("button")).toHaveCount(0);
        }
    }
    const gravacoes = [];
    page.on("request", requisicao => {
        if (requisicao.method() !== "GET" && requisicao.url().includes(`/gerenciamentos/${quadro.id}/etapas`)) {
            gravacoes.push(requisicao.method());
        }
    });
    const imagens = await mkdtemp(join(tmpdir(), "creral-etapas-e2e-"));
    await page.reload();
    await abrir();
    await expect(page.getByRole("heading", { name: "Nenhuma etapa de trabalho cadastrada", exact: true })).toBeVisible();
    await expect(page.getByRole("region").getByRole("heading")).toHaveText(["Concluídos", "Cancelados"]);
    await conferirFinaisNaTela();
    await fotografar(page, imagens, "finais");

    const nova = page.getByRole("button", { name: "+ Nova etapa", exact: true });
    await acionar(page, nova);
    await expect(page.getByLabel("Posição", { exact: true }).locator("option")).toHaveText(["1"]);
    await preencher(page, page.getByLabel("Nome da etapa", { exact: true }), "Rascunho descartado");
    await acionar(page, page.getByRole("button", { name: "Cancelar", exact: true }));
    await expect(nova).toBeFocused();
    expect(await consultar()).toMatchObject({ gerenciamento: { versao: 0 }, etapas: finaisEsperadas });
    expect(gravacoes).toEqual([]);

    await criar("Planejamento", "Técnico", 1, 0);
    const inicial = await consultar();
    expect(inicial.gerenciamento.versao).toBe(1);
    expect(inicial.etapas.map(campos)).toEqual([
        { id: inicial.etapas[0].id, nome: "Planejamento", setor: "Técnico", ordem: 1, quantidadeDemandas: 0, categoria: "TRABALHO" },
        ...finaisEsperadas
    ]);
    const planejamentoId = inicial.etapas[0].id;
    expect(planejamentoId).toBeGreaterThan(0);
    await criar("Execução", "Campo", 2, 1);
    const dupla = await consultar();
    expect(dupla.gerenciamento.versao).toBe(2);
    expect(dupla.etapas.map(campos)).toEqual([
        { id: planejamentoId, nome: "Planejamento", setor: "Técnico", ordem: 1, quantidadeDemandas: 0, categoria: "TRABALHO" },
        { id: dupla.etapas[1].id, nome: "Execução", setor: "Campo", ordem: 2, quantidadeDemandas: 0, categoria: "TRABALHO" },
        ...finaisEsperadas
    ]);
    const execucaoId = dupla.etapas[1].id;
    expect(execucaoId).toBeGreaterThan(0);
    await criar("Análise", "Engenharia", 2, 2);
    const tripla = await consultar();
    expect(tripla.gerenciamento.versao).toBe(3);
    const analiseId = tripla.etapas[1].id;
    expect(analiseId).toBeGreaterThan(0);
    expect(new Set([planejamentoId, execucaoId, analiseId]).size).toBe(3);
    expect(tripla.etapas.map(campos)).toEqual([
        { id: planejamentoId, nome: "Planejamento", setor: "Técnico", ordem: 1, quantidadeDemandas: 0, categoria: "TRABALHO" },
        { id: analiseId, nome: "Análise", setor: "Engenharia", ordem: 2, quantidadeDemandas: 0, categoria: "TRABALHO" },
        { id: execucaoId, nome: "Execução", setor: "Campo", ordem: 3, quantidadeDemandas: 0, categoria: "TRABALHO" },
        ...finaisEsperadas
    ]);
    await page.reload();
    await abrir();
    await expect(page.getByRole("region").getByRole("heading")).toHaveText(["Planejamento", "Análise", "Execução", "Concluídos", "Cancelados"]);
    await conferirFinaisNaTela();
    expect((await consultar()).etapas.map(campos)).toEqual(tripla.etapas.map(campos));

    await acionar(page, page.getByRole("button", { name: "Editar etapa 3: Execução", exact: true }));
    await expect(page.getByLabel("Nome da etapa", { exact: true })).toHaveValue("Execução");
    await expect(page.getByLabel("Setor responsável", { exact: true })).toHaveValue("Campo");
    await expect(page.getByLabel("Posição", { exact: true })).toHaveValue("3");
    await expect(page.getByLabel("Posição", { exact: true }).locator("option")).toHaveText(["1", "2", "3"]);
    await preencher(page, page.getByLabel("Nome da etapa", { exact: true }), "Entrega");
    await preencher(page, page.getByLabel("Setor responsável", { exact: true }), "Operação");
    await selecionarPosicao(page, 1);
    await acionar(page, page.getByRole("button", { name: "Salvar etapa", exact: true }));
    await expect(page.getByRole("region").getByRole("heading")).toHaveText(["Entrega", "Planejamento", "Análise", "Concluídos", "Cancelados"]);
    const movidaInicio = await consultar();
    expect(movidaInicio.gerenciamento.versao).toBe(4);
    expect(movidaInicio.etapas.map(campos)).toEqual([
        { id: execucaoId, nome: "Entrega", setor: "Operação", ordem: 1, quantidadeDemandas: 0, categoria: "TRABALHO" },
        { id: planejamentoId, nome: "Planejamento", setor: "Técnico", ordem: 2, quantidadeDemandas: 0, categoria: "TRABALHO" },
        { id: analiseId, nome: "Análise", setor: "Engenharia", ordem: 3, quantidadeDemandas: 0, categoria: "TRABALHO" },
        ...finaisEsperadas
    ]);
    await acionar(page, page.getByRole("button", { name: "Editar etapa 1: Entrega", exact: true }));
    await selecionarPosicao(page, 3);
    await acionar(page, page.getByRole("button", { name: "Salvar etapa", exact: true }));
    await expect(page.getByRole("region").getByRole("heading")).toHaveText(["Planejamento", "Análise", "Entrega", "Concluídos", "Cancelados"]);
    const movidaFim = await consultar();
    expect(movidaFim.gerenciamento.versao).toBe(5);
    expect(movidaFim.etapas.map(campos)).toEqual([
        { id: planejamentoId, nome: "Planejamento", setor: "Técnico", ordem: 1, quantidadeDemandas: 0, categoria: "TRABALHO" },
        { id: analiseId, nome: "Análise", setor: "Engenharia", ordem: 2, quantidadeDemandas: 0, categoria: "TRABALHO" },
        { id: execucaoId, nome: "Entrega", setor: "Operação", ordem: 3, quantidadeDemandas: 0, categoria: "TRABALHO" },
        ...finaisEsperadas
    ]);

    const editarAnalise = page.getByRole("button", { name: "Editar etapa 2: Análise", exact: true });
    await acionar(page, editarAnalise);
    await fotografar(page, imagens, "editor");
    await acionar(page, page.getByRole("button", { name: "Cancelar", exact: true }));
    await expect(editarAnalise).toBeFocused();
    const removerAnalise = page.getByRole("button", { name: "Remover etapa 2: Análise", exact: true });
    await acionar(page, removerAnalise);
    const dialogo = page.getByRole("dialog", { name: "Remover Análise?", exact: true });
    await expect(dialogo).toContainText("Demandas impedem a remoção. Mova-as para outra etapa antes de remover esta coluna.");
    await expect(dialogo.getByRole("button", { name: "Cancelar", exact: true })).toBeFocused();
    await fotografar(page, imagens, "confirmacao");
    await acionar(page, dialogo.getByRole("button", { name: "Cancelar", exact: true }));
    await expect(removerAnalise).toBeFocused();
    expect(gravacoes).toEqual(["POST", "POST", "POST", "PUT", "PUT"]);
    expect((await consultar()).etapas.map(campos)).toEqual(movidaFim.etapas.map(campos));
    expect((await consultar()).gerenciamento.versao).toBe(5);
    await acionar(page, removerAnalise);
    await acionar(page, dialogo.getByRole("button", { name: "Remover", exact: true }));
    await expect(page.getByRole("region").getByRole("heading")).toHaveText(["Planejamento", "Entrega", "Concluídos", "Cancelados"]);
    await conferirFinaisNaTela();
    const removida = await consultar();
    expect(removida.gerenciamento.versao).toBe(6);
    expect(gravacoes).toEqual(["POST", "POST", "POST", "PUT", "PUT", "DELETE"]);
    expect(removida.etapas.map(campos)).toEqual([
        { id: planejamentoId, nome: "Planejamento", setor: "Técnico", ordem: 1, quantidadeDemandas: 0, categoria: "TRABALHO" },
        { id: execucaoId, nome: "Entrega", setor: "Operação", ordem: 2, quantidadeDemandas: 0, categoria: "TRABALHO" },
        ...finaisEsperadas
    ]);
    await fotografar(page, imagens, "ativo");

    await acionar(page, page.getByRole("button", { name: /Voltar/ }));
    await acionar(page, page.getByRole("button", { name: "Arquivar Fluxo de instalação", exact: true }));
    const arquivoRequest = page.waitForRequest(resposta => resposta.url().endsWith(`/gerenciamentos/${quadro.id}/arquivar`));
    await acionar(page, page.getByRole("dialog").getByRole("button", { name: "Arquivar", exact: true }));
    expect((await arquivoRequest).postDataJSON()).toEqual({ versao: 6 });
    await expect(page.getByRole("heading", { name: "Nenhum gerenciamento ativo", exact: true })).toBeVisible();
    await acionar(page, page.getByRole("button", { name: "Arquivados", exact: true }));
    await abrir();
    await expect(page.getByText("Arquivado — somente consulta", { exact: true })).toBeVisible();
    await expect(page.getByRole("button", { name: "+ Nova etapa", exact: true })).toHaveCount(0);
    await expect(page.getByRole("button", { name: /Editar etapa \d/ })).toHaveCount(0);
    await expect(page.getByRole("button", { name: /Remover etapa \d/ })).toHaveCount(0);
    await expect(page.getByRole("button", { name: /Criar processo/ })).toHaveCount(0);
    await conferirFinaisNaTela();
    await fotografar(page, imagens, "arquivado");
    const arquivada = await consultar();
    expect(arquivada.gerenciamento).toMatchObject({ id: quadro.id, nome: quadro.nome, descricao: quadro.descricao, criador: quadro.criador, podeAdministrar: true, arquivado: true, versao: 7 });
    expect(arquivada.etapas.map(campos)).toEqual(removida.etapas.map(campos));
    await page.reload();
    await acionar(page, page.getByRole("button", { name: "Arquivados", exact: true }));
    await abrir();
    await expect(page.getByRole("region").getByRole("heading")).toHaveText(["Planejamento", "Entrega", "Concluídos", "Cancelados"]);
    await conferirFinaisNaTela();
    await expect(page.getByText("Setor responsável: Operação", { exact: true })).toBeVisible();
    await expect(page.getByRole("button", { name: /Nova etapa|Editar etapa \d|Remover etapa \d/ })).toHaveCount(0);
    expect((await consultar()).etapas.map(campos)).toEqual(removida.etapas.map(campos));
    console.log(`Screenshots etapas: ${imagens}`);
});
