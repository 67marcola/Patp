import { expect, test } from "@playwright/test";
import { randomUUID } from "node:crypto";
import { mkdtemp } from "node:fs/promises";
import { tmpdir } from "node:os";
import { join } from "node:path";

async function focar(page, controle) {
    await expect(controle).toBeVisible();
    for (let tentativa = 0; tentativa < 50; tentativa += 1) {
        if (await controle.evaluate(elemento => elemento === document.activeElement)) break;
        await page.keyboard.press("Tab");
    }
    await expect(controle).toBeFocused();
}

async function acionar(page, controle) {
    await focar(page, controle);
    await page.keyboard.press("Enter");
}

async function preencher(page, controle, texto) {
    await focar(page, controle);
    await page.keyboard.press("Control+A");
    if (await controle.getAttribute("type") === "date") await controle.fill(texto);
    else await page.keyboard.type(texto);
}

async function fotografar(page, pasta, nome) {
    for (const [sufixo, tamanho] of [["desktop", { width: 1280, height: 800 }], ["mobile", { width: 375, height: 812 }]]) {
        await page.setViewportSize(tamanho);
        expect(await page.evaluate(() => document.documentElement.scrollWidth <= document.documentElement.clientWidth)).toBe(true);
        expect(await page.locator(".editor-demanda, .card-demanda").evaluateAll(elementos => elementos.every(elemento => {
            const area = elemento.getBoundingClientRect();
            return area.left >= 0 && area.right <= document.documentElement.clientWidth;
        }))).toBe(true);
        await page.screenshot({ path: join(pasta, `${nome}-${sufixo}.png`), fullPage: true });
    }
    await page.setViewportSize({ width: 1280, height: 800 });
}

test("CAD-01/05/13/26/30/31/32/37/39/40/41: funcionário cria mínimo/completo, reload, duplicidade global e arquivo reais", async ({ page, request }) => {
    const identificador = randomUUID();
    async function cadastrar(nome, prefixo) {
        const email = `${prefixo}-${identificador}@example.test`;
        const resposta = await request.post("/api/usuarios/cadastro", { data: { nome, setor: "Testes", email, senha: "teste123" } });
        expect(resposta.status()).toBe(200);
        return { ...await resposta.json(), email };
    }
    const criador = await cadastrar("Criador do quadro", "demandas-criador");
    const funcionario = await cadastrar("Funcionário de teste", "demandas-funcionario");
    const headersCriador = { Authorization: `Bearer ${criador.token}` };
    const headersFuncionario = { Authorization: `Bearer ${funcionario.token}` };
    async function criarQuadro(nome) {
        const resposta = await request.post("/api/gerenciamentos", { headers: headersCriador, data: {
            nome, descricao: "Dados fictícios de cadastro e acompanhamento.", etapas: [
                { nome: "Comprar poste", setor: "Almoxarifado", ordem: 1 }, { nome: "Instalar poste", setor: "Campo", ordem: 2 }
            ] } });
        expect(resposta.status()).toBe(201);
        return resposta.json();
    }
    const quadro = await criarQuadro("Cadastro de demandas");
    const outro = await criarQuadro("Outro quadro de demandas");
    async function consultar(id, headers = headersFuncionario) {
        const resposta = await request.get(`/api/gerenciamentos/${id}/estrutura-etapas`, { headers });
        expect(resposta.status()).toBe(200);
        return resposta.json();
    }
    async function entrar(usuario) {
        await preencher(page, page.getByPlaceholder("seu@email.com"), usuario.email);
        await preencher(page, page.getByPlaceholder("Digite sua senha"), "teste123");
        await acionar(page, page.getByRole("button", { name: "Entrar", exact: true }));
        await expect(page.getByRole("heading", { name: "Gerenciamentos", exact: true })).toBeVisible();
    }
    async function abrir(nome) {
        await acionar(page, page.getByRole("button", { name: `Abrir ${nome}`, exact: true }));
        await expect(page.getByRole("heading", { name: nome, exact: true })).toBeVisible();
        await expect(page.getByRole("region", { name: "Comprar poste", exact: true })).toBeVisible();
    }
    async function formulario() {
        await acionar(page, page.getByRole("button", { name: "+ Criar processo", exact: true }));
        await expect(page.getByLabel("Número da demanda", { exact: true })).toBeFocused();
        await expect(page.getByText("A demanda começará automaticamente na primeira etapa de trabalho.", { exact: true })).toBeVisible();
        await expect(page.getByRole("combobox")).toHaveCount(0);
    }
    const posts = [];
    page.on("request", requisicao => {
        if (requisicao.method() === "POST" && /\/api\/gerenciamentos\/\d+\/demandas$/.test(new URL(requisicao.url()).pathname)) {
            posts.push({ path: new URL(requisicao.url()).pathname, dados: requisicao.postDataJSON() });
        }
    });
    const labels = { numeroProcesso: "Número da demanda", pessoa: "Cliente/solicitante", responsavel: "Responsável",
        prioridade: "Prioridade", dataEmissao: "Data de emissão", prazoEtapa: "Prazo da etapa", prazoGeral: "Prazo geral", observacoes: "Observações" };
    const minimo = { numeroProcesso: `DEM-${identificador}`, pessoa: "Cliente mínimo", responsavel: null, prioridade: null,
        dataEmissao: null, prazoEtapa: null, prazoGeral: null, observacoes: null };
    const completo = { numeroProcesso: `COM-${identificador}`, pessoa: "Maria de teste", responsavel: "Responsável de teste",
        prioridade: "Urgente por solicitação", dataEmissao: "2020-02-29", prazoEtapa: "2019-01-01", prazoGeral: "2018-01-01",
        observacoes: "Linha um\n<em>Texto literal informado pelo cliente</em>\nAcompanhar o serviço solicitado." };
    await page.goto("/");
    await entrar(funcionario);
    await abrir(quadro.nome);
    const inicial = await consultar(quadro.id);
    expect(inicial.gerenciamento).toMatchObject({ id: quadro.id, versao: 0, podeAdministrar: false, arquivado: false });
    expect(inicial.demandas).toEqual([]);
    expect(inicial.etapas.map(etapa => etapa.quantidadeDemandas)).toEqual([0, 0, 0, 0]);
    const primeiraId = inicial.etapas[0].id;
    expect(inicial.etapas[0]).toMatchObject({ nome: "Comprar poste", categoria: "TRABALHO" });
    await expect(page.getByRole("button", { name: "+ Nova etapa", exact: true })).toHaveCount(0);
    await formulario();
    await preencher(page, page.getByLabel("Número da demanda", { exact: true }), ` ${minimo.numeroProcesso} `);
    await preencher(page, page.getByLabel("Cliente/solicitante", { exact: true }), ` ${minimo.pessoa} `);
    let liberar;
    let interceptado;
    const pausa = new Promise(resolve => { liberar = resolve; });
    const chegou = new Promise(resolve => { interceptado = resolve; });
    const rota = `**/api/gerenciamentos/${quadro.id}/demandas`;
    await page.route(rota, async requisicao => { interceptado(); await pausa; await requisicao.continue(); });
    const respostaMinima = page.waitForResponse(resposta => resposta.url().endsWith(`/gerenciamentos/${quadro.id}/demandas`) && resposta.request().method() === "POST");
    await acionar(page, page.getByRole("button", { name: "Criar demanda", exact: true }));
    await chegou;
    try {
        for (const label of Object.values(labels)) await expect(page.getByLabel(label, { exact: true })).toBeDisabled();
        for (const nome of ["Cancelar", "Sair", "+ Criar processo"]) await expect(page.getByRole("button", { name: nome, exact: true })).toBeDisabled();
        await expect(page.getByRole("button", { name: /Voltar/ })).toBeDisabled();
        await expect(page.getByRole("button", { name: "Criando...", exact: true })).toBeDisabled();
        await page.keyboard.press("Enter");
        expect(posts).toEqual([{ path: `/api/gerenciamentos/${quadro.id}/demandas`, dados: { ...minimo, versao: 0 } }]);
    } finally { liberar(); }
    const minimaResponse = await respostaMinima;
    expect(minimaResponse.status()).toBe(201);
    const minimoSalvo = await minimaResponse.json();
    await page.unroute(rota);
    const minimaId = minimoSalvo.demandas[0].id;
    expect(Number.isInteger(minimaId)).toBe(true);
    expect(minimaId).toBeGreaterThan(0);
    expect(minimoSalvo.demandas).toEqual([{ ...minimo, id: minimaId, etapaId: primeiraId, status: "Em andamento",
        dataConclusao: null, dataCancelamento: null, motivoCancelamento: null }]);
    expect(minimoSalvo.gerenciamento.versao).toBe(1);
    expect(minimoSalvo.etapas.map(etapa => etapa.quantidadeDemandas)).toEqual([1, 0, 0, 0]);
    await expect(page.getByRole("form", { name: "Novo processo", exact: true })).toHaveCount(0);
    await expect(page.getByRole("button", { name: "+ Criar processo", exact: true })).toBeFocused();
    const cartaoMinimo = page.getByRole("article", { name: `${minimo.numeroProcesso} — ${minimo.pessoa}`, exact: true });
    await expect(cartaoMinimo).toHaveAttribute("data-demanda-id", String(minimaId));
    await expect(page.getByRole("region", { name: "Comprar poste", exact: true }).getByRole("article")).toHaveCount(1);
    await expect(cartaoMinimo.locator("dt", { hasText: "Status" }).locator("+ dd")).toHaveText("Em andamento");
    await page.reload();
    await abrir(quadro.nome);
    await expect(cartaoMinimo).toHaveAttribute("data-demanda-id", String(minimaId));
    expect((await consultar(quadro.id)).demandas).toEqual(minimoSalvo.demandas);
    await formulario();
    for (const [campo, valor] of Object.entries(completo)) await preencher(page, page.getByLabel(labels[campo], { exact: true }), valor);
    const imagens = await mkdtemp(join(tmpdir(), "creral-demandas-e2e-"));
    await fotografar(page, imagens, "formulario");
    const respostaCompleta = page.waitForResponse(resposta => resposta.url().endsWith(`/gerenciamentos/${quadro.id}/demandas`) && resposta.request().method() === "POST");
    await acionar(page, page.getByRole("button", { name: "Criar demanda", exact: true }));
    const completaResponse = await respostaCompleta;
    expect(completaResponse.status()).toBe(201);
    expect(completaResponse.request().postDataJSON()).toEqual({ ...completo, versao: 1 });
    const completoSalvo = await completaResponse.json();
    const completaId = completoSalvo.demandas[1].id;
    expect(Number.isInteger(completaId)).toBe(true);
    expect(completaId).toBeGreaterThan(minimaId);
    expect(completoSalvo.demandas).toEqual([...minimoSalvo.demandas, { ...completo, id: completaId, etapaId: primeiraId,
        status: "Em andamento", dataConclusao: null, dataCancelamento: null, motivoCancelamento: null }]);
    expect(completoSalvo.gerenciamento.versao).toBe(2);
    expect(completoSalvo.etapas.map(etapa => etapa.quantidadeDemandas)).toEqual([2, 0, 0, 0]);
    const cartaoCompleto = page.getByRole("article", { name: `${completo.numeroProcesso} — ${completo.pessoa}`, exact: true });
    await expect(cartaoCompleto).toHaveAttribute("data-demanda-id", String(completaId));
    for (const [label, valor] of Object.entries({ Responsável: completo.responsavel, Prioridade: completo.prioridade,
        "Data de emissão": "29/02/2020", "Prazo da etapa": "01/01/2019", "Prazo geral": "01/01/2018", Observações: completo.observacoes })) {
        await expect(cartaoCompleto.locator("dt", { hasText: label }).locator("+ dd")).toHaveText(valor);
    }
    await expect(cartaoCompleto.locator("em")).toHaveCount(0);
    await expect(cartaoCompleto.getByRole("button")).toHaveCount(0);
    await expect(cartaoCompleto.getByRole("heading")).toHaveCount(0);
    await fotografar(page, imagens, "cartoes");
    await page.reload();
    await abrir(quadro.nome);
    await expect(cartaoMinimo).toHaveAttribute("data-demanda-id", String(minimaId));
    await expect(cartaoCompleto).toHaveAttribute("data-demanda-id", String(completaId));
    expect(await consultar(quadro.id)).toEqual(completoSalvo);
    await acionar(page, page.getByRole("button", { name: /Voltar/ }));
    await abrir(outro.nome);
    await formulario();
    await preencher(page, page.getByLabel("Número da demanda", { exact: true }), minimo.numeroProcesso.toLowerCase());
    await preencher(page, page.getByLabel("Cliente/solicitante", { exact: true }), "Outro cliente");
    const respostaDuplicada = page.waitForResponse(resposta => resposta.url().endsWith(`/gerenciamentos/${outro.id}/demandas`) && resposta.request().method() === "POST");
    await acionar(page, page.getByRole("button", { name: "Criar demanda", exact: true }));
    const duplicadaResponse = await respostaDuplicada;
    expect(duplicadaResponse.status()).toBe(409);
    expect(await duplicadaResponse.json()).toEqual({ erro: "Já existe uma demanda com esse número." });
    await expect(page.getByRole("alert")).toHaveText("Já existe uma demanda com esse número.");
    await expect(page.getByLabel("Número da demanda", { exact: true })).toHaveValue(minimo.numeroProcesso.toLowerCase());
    await expect(page.getByLabel("Cliente/solicitante", { exact: true })).toHaveValue("Outro cliente");
    await expect(page.getByRole("button", { name: "Criar demanda", exact: true })).toBeDisabled();
    expect(posts).toHaveLength(3);
    const vazio = await consultar(outro.id);
    expect(vazio.gerenciamento.versao).toBe(0);
    expect(vazio.demandas).toEqual([]);
    expect(vazio.etapas.map(etapa => etapa.quantidadeDemandas)).toEqual([0, 0, 0, 0]);
    expect(await consultar(quadro.id)).toEqual(completoSalvo);
    await acionar(page, page.getByRole("button", { name: "Atualizar quadro", exact: true }));
    await expect(page.getByRole("button", { name: "Criar demanda", exact: true })).toBeEnabled();
    await expect(page.getByLabel("Número da demanda", { exact: true })).toHaveValue(minimo.numeroProcesso.toLowerCase());
    expect(posts).toHaveLength(3);
    await acionar(page, page.getByRole("button", { name: "Cancelar", exact: true }));
    await expect(page.getByRole("button", { name: "+ Criar processo", exact: true })).toBeFocused();
    await acionar(page, page.getByRole("button", { name: "Sair", exact: true }));
    await expect(page.getByRole("heading", { name: "Bem-vindo", exact: true })).toBeVisible();
    await entrar(criador);
    await abrir(quadro.nome);
    await expect(cartaoCompleto).toHaveAttribute("data-demanda-id", String(completaId));
    await acionar(page, page.getByRole("button", { name: /Voltar/ }));
    await acionar(page, page.getByRole("button", { name: "Arquivar Cadastro de demandas", exact: true }));
    const respostaArquivo = page.waitForResponse(resposta => resposta.url().endsWith(`/gerenciamentos/${quadro.id}/arquivar`));
    await acionar(page, page.getByRole("dialog").getByRole("button", { name: "Arquivar", exact: true }));
    const arquivoResponse = await respostaArquivo;
    expect(arquivoResponse.status()).toBe(204);
    expect(arquivoResponse.request().postDataJSON()).toEqual({ versao: 2 });
    await acionar(page, page.getByRole("button", { name: "Arquivados", exact: true }));
    await abrir(quadro.nome);
    await expect(page.getByRole("button", { name: "+ Criar processo", exact: true })).toHaveCount(0);
    await expect(page.getByText("Arquivado — somente consulta", { exact: true })).toBeVisible();
    await expect(cartaoMinimo).toHaveAttribute("data-demanda-id", String(minimaId));
    await expect(cartaoCompleto).toHaveAttribute("data-demanda-id", String(completaId));
    await expect(page.getByRole("region", { name: "Comprar poste", exact: true }).getByText("2 demandas", { exact: true })).toBeVisible();
    const arquivado = await consultar(quadro.id, headersCriador);
    expect(arquivado.gerenciamento).toMatchObject({ arquivado: true, versao: 3, podeAdministrar: true });
    expect(arquivado.demandas).toEqual(completoSalvo.demandas);
    expect(arquivado.etapas.map(etapa => etapa.quantidadeDemandas)).toEqual([2, 0, 0, 0]);
    await fotografar(page, imagens, "arquivado");
    await page.reload();
    await acionar(page, page.getByRole("button", { name: "Arquivados", exact: true }));
    await abrir(quadro.nome);
    await expect(cartaoMinimo).toHaveAttribute("data-demanda-id", String(minimaId));
    await expect(cartaoCompleto).toHaveAttribute("data-demanda-id", String(completaId));
    await expect(page.getByRole("button", { name: /Criar processo|Nova etapa|Editar etapa|Remover etapa/ })).toHaveCount(0);
    expect(await consultar(quadro.id, headersCriador)).toEqual(arquivado);
    expect(posts.map(post => post.dados)).toEqual([{ ...minimo, versao: 0 }, { ...completo, versao: 1 },
        { ...minimo, numeroProcesso: minimo.numeroProcesso.toLowerCase(), pessoa: "Outro cliente", versao: 0 }]);
    console.log(`Screenshots demandas: ${imagens}`);
});
