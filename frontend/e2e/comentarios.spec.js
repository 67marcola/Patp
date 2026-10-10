import { expect, test } from "@playwright/test";
import { randomUUID } from "node:crypto";
import { mkdtemp } from "node:fs/promises";
import { tmpdir } from "node:os";
import { join } from "node:path";

const campos = ["id", "texto", "funcionario", "dataHora", "processoId"];
const aviso = "Confira as mensagens antes de enviar novamente; o comentário pode já ter sido salvo.";
const antigoFlat = ({ processo, ...dados }) => ({ ...dados, processoId: processo.id });

async function preparar(page, request) {
    const id = randomUUID();
    async function conta(nome) {
        const email = `${nome}-${id}@example.test`; const r = await request.post("/api/usuarios/cadastro", { data: { nome, email, setor: "Testes", senha: "teste123" } });
        expect(r.status()).toBe(200); return { ...await r.json(), email };
    }
    const dono = await conta("Dono"), colega = await conta("Colega"), headers = { Authorization: `Bearer ${dono.token}` };
    const criado = await request.post("/api/gerenciamentos", { headers, data: { nome: `Comentários ${id}`, etapas: [{ nome: "Trabalho", setor: "Projeto", ordem: 1 }] } });
    expect(criado.status()).toBe(201); const quadro = await criado.json(), base = `/api/gerenciamentos/${quadro.id}`; let inicial;
    for (let indice = 1; indice <= 2; indice++) {
        const r = await request.post(`${base}/demandas`, { headers, data: { versao: indice - 1, numeroProcesso: `COM-${indice}-${id}`, pessoa: `Cliente ${indice}`, responsavel: "Ana", prioridade: "Alta", dataEmissao: "2020-02-29", prazoEtapa: null, prazoGeral: "2026-10-10", observacoes: "Informação\nLiteral" } });
        expect(r.status()).toBe(201); inicial = await r.json();
    }
    const [demanda, outra] = inicial.demandas, rota = `${base}/demandas/${demanda.id}/comentarios`;
    const anterior = await request.post(`/api/processos/${demanda.id}/comentarios`, { headers, data: { texto: "  Anterior\nLiteral  ", funcionario: "Nome falso" } });
    expect(anterior.status()).toBe(200); const antiga = antigoFlat(await anterior.json()); expect(antiga.funcionario).toBe("Dono");
    async function consultar() { const r = await request.get(`${base}/estrutura-etapas`, { headers }); expect(r.status()).toBe(200); return r.json(); }
    async function conversa(p = demanda.id) { const r = await request.get(`${base}/demandas/${p}/comentarios`, { headers }); expect(r.status()).toBe(200); return r.json(); }
    async function historico() { const r = await request.get(`/api/processos/${demanda.id}/historico`, { headers }); expect(r.status()).toBe(200); return (await r.json()).map(antigoFlat); }
    const eventos = await historico();
    async function abrirQuadro(arquivado = false) {
        if (arquivado) await page.getByRole("button", { name: "Arquivados", exact: true }).click();
        await page.getByRole("button", { name: `Abrir ${quadro.nome}`, exact: true }).click(); await expect(page.locator(`[data-demanda-id="${demanda.id}"]`)).toBeVisible();
    }
    async function entrar(contaAtual, arquivado = false) {
        await page.goto("/"); await page.getByPlaceholder("seu@email.com").fill(contaAtual.email); await page.getByPlaceholder("Digite sua senha").fill("teste123"); await page.getByRole("button", { name: "Entrar", exact: true }).click(); await abrirQuadro(arquivado);
    }
    async function abrir(p = demanda) {
        const b = page.getByRole("button", { name: `Comentários da demanda ${p.numeroProcesso}`, exact: true }); await b.focus(); await page.keyboard.press("Enter");
        const painel = page.getByRole("complementary", { name: `Comentários: ${p.numeroProcesso}`, exact: true }); await expect(painel).toBeVisible(); await expect(painel.getByRole("button", { name: "Fechar comentários" })).toBeFocused(); return { b, painel };
    }
    async function sair() { await page.getByRole("button", { name: "← Voltar" }).click(); await page.getByRole("button", { name: "Sair", exact: true }).click(); }
    await entrar(dono);
    return { dono, colega, quadro, base, headers, demanda, outra, rota, antiga, inicial, eventos, consultar, conversa, historico, abrirQuadro, entrar, abrir, sair };
}

async function conferirMensagem(painel, mensagem) {
    const item = painel.locator(`[data-comentario-id="${mensagem.id}"]`); await expect(item).toHaveCount(1);
    expect(await item.locator(".texto-comentario").textContent()).toBe(mensagem.texto); await expect(item.locator("strong")).toHaveText(mensagem.funcionario); await expect(item.locator("time")).toHaveAttribute("datetime", mensagem.dataHora);
    const iso = mensagem.dataHora; await expect(item.locator("time")).toHaveText(`${iso.slice(8, 10)}/${iso.slice(5, 7)}/${iso.slice(0, 4)} ${iso.slice(11, 19)}`);
}

async function enviar(page, c, texto, autor) {
    await page.getByRole("textbox", { name: "Comentário", exact: true }).fill(texto); const inicio = Date.now();
    const esperada = page.waitForResponse(r => r.url().endsWith(c.rota) && r.request().method() === "POST");
    await page.getByRole("button", { name: "Enviar comentário", exact: true }).focus(); await page.keyboard.press("Enter"); const r = await esperada; expect(r.status()).toBe(201); expect(r.request().postDataJSON()).toEqual({ texto });
    const salvo = await r.json(); expect(Object.keys(salvo).sort()).toEqual([...campos].sort()); expect(salvo.id).toBeGreaterThan(0); expect(salvo.texto).toBe(texto); expect(salvo.funcionario).toBe(autor); expect(salvo.processoId).toBe(c.demanda.id);
    const hora = new Date(salvo.dataHora).getTime(); expect(hora).toBeGreaterThanOrEqual(inicio - 1000); expect(hora).toBeLessThanOrEqual(Date.now() + 1000);
    await expect(page.getByRole("textbox", { name: "Comentário", exact: true })).toHaveValue(""); return salvo;
}

async function viewport(page, painel, pasta, nome, tamanho) {
    await page.setViewportSize(tamanho); await painel.evaluate(e => { e.scrollTop = 0; });
    const medidas = await painel.evaluate(e => { const r = e.getBoundingClientRect(); return { left: r.left, right: r.right, top: r.top, bottom: r.bottom, width: innerWidth, height: innerHeight, painelSemOverflow: e.scrollWidth <= e.clientWidth, paginaSemOverflow: document.documentElement.scrollWidth <= document.documentElement.clientWidth, quebras: getComputedStyle(e.querySelector(".texto-comentario")).whiteSpace }; });
    expect(medidas.left).toBeGreaterThanOrEqual(0); expect(medidas.right).toBeLessThanOrEqual(medidas.width); expect(medidas.top).toBeGreaterThanOrEqual(0); expect(medidas.bottom).toBeLessThanOrEqual(medidas.height); expect(medidas.painelSemOverflow).toBe(true); expect(medidas.paginaSemOverflow).toBe(true); expect(medidas.quebras).toBe("pre-wrap");
    await page.screenshot({ path: join(pasta, `${nome}.png`), fullPage: true });
}

test("COM-02/03/10/11/12/15/17/19/20/23/26/28: colaboração real, isolamento, recarga e arquivo nos dois tamanhos", async ({ page, request }) => {
    await page.setViewportSize({ width: 1280, height: 800 }); const c = await preparar(page, request); let { painel, b } = await c.abrir(); await conferirMensagem(painel, c.antiga);
    const literal = '  <img src=x onerror="alert(1)">\nSegunda linha\n' + "palavralonga".repeat(25) + "  "; const nova = await enviar(page, c, literal, "Dono"); await conferirMensagem(painel, nova); await expect(painel.locator("img")).toHaveCount(0); expect(await c.consultar()).toEqual(c.inicial); expect(await c.historico()).toEqual(c.eventos);
    await page.keyboard.press("Escape"); await expect(b).toBeFocused(); ({ painel } = await c.abrir(c.outra)); await expect(painel.getByText("Nenhum comentário nesta demanda.", { exact: true })).toBeVisible(); await expect(painel.getByRole("listitem")).toHaveCount(0); expect((await c.conversa(c.outra.id)).comentarios).toEqual([]);
    await page.getByRole("button", { name: "Fechar comentários" }).click(); await c.sair(); await c.entrar(c.colega); ({ painel } = await c.abrir()); await conferirMensagem(painel, c.antiga); await conferirMensagem(painel, nova);
    const resposta = await enviar(page, c, "Resposta do colega\nLiteral", "Colega"); await conferirMensagem(painel, resposta); expect((await c.conversa()).comentarios).toEqual([c.antiga, nova, resposta]); expect(await c.consultar()).toEqual(c.inicial); expect(await c.historico()).toEqual(c.eventos);
    await page.reload(); await c.abrirQuadro(); ({ painel, b } = await c.abrir()); for (const m of [c.antiga, nova, resposta]) await conferirMensagem(painel, m);
    const pasta = await mkdtemp(join(tmpdir(), "creral-comentarios-qa-")); await viewport(page, painel, pasta, "desktop-conversa", { width: 1280, height: 800 }); await viewport(page, painel, pasta, "mobile-conversa", { width: 390, height: 844 });
    await page.getByRole("textbox", { name: "Comentário", exact: true }).fill("Descartar"); await painel.getByRole("button", { name: "Fechar comentários" }).focus(); await page.keyboard.press("Enter"); await expect(b).toBeFocused(); ({ painel } = await c.abrir()); await expect(page.getByRole("textbox", { name: "Comentário", exact: true })).toHaveValue(""); await page.keyboard.press("Escape");
    const arquivar = await request.put(`${c.base}/demandas/${c.demanda.id}/arquivar`, { headers: c.headers, data: { versao: c.inicial.gerenciamento.versao } }); expect(arquivar.status()).toBe(200); const arquivo = await arquivar.json(); expect(arquivo.demandasArquivadas).toEqual([c.demanda]);
    await page.reload(); await c.abrirQuadro(); ({ painel } = await c.abrir()); await expect(painel.getByText("Demanda arquivada: comentários somente para consulta.", { exact: true })).toBeVisible(); await expect(painel.getByRole("textbox")).toHaveCount(0); await expect(painel.getByRole("button", { name: "Enviar comentário" })).toHaveCount(0); for (const m of [c.antiga, nova, resposta]) await conferirMensagem(painel, m); expect((await c.conversa()).comentarios).toEqual([c.antiga, nova, resposta]); await page.keyboard.press("Escape");
    const restaurar = await request.put(`${c.base}/demandas/${c.demanda.id}/restaurar`, { headers: c.headers, data: { versao: arquivo.gerenciamento.versao } }); expect(restaurar.status()).toBe(200); const restaurada = await restaurar.json(); expect(restaurada.demandas).toEqual(c.inicial.demandas); expect(restaurada.demandasArquivadas).toEqual([]);
    const antesQuadro = await c.conversa(); const arquivoQuadro = await request.put(`${c.base}/arquivar`, { headers: c.headers, data: { versao: restaurada.gerenciamento.versao } }); expect(arquivoQuadro.status()).toBe(204);
    await c.sair(); await c.entrar(c.colega, true); ({ painel } = await c.abrir()); await expect(painel.getByText("Quadro arquivado: comentários somente para consulta.", { exact: true })).toBeVisible(); await expect(painel.getByRole("textbox")).toHaveCount(0); const depoisQuadro = await c.conversa(); expect(depoisQuadro).toEqual({ ...antesQuadro, quadroArquivado: true }); for (const m of [c.antiga, nova, resposta]) await conferirMensagem(painel, m);
    await viewport(page, painel, pasta, "mobile-arquivo", { width: 390, height: 844 }); console.log(`QA comentários: ${pasta}`);
});

test("COM-07/08/11/12/20/22/24/25/28: POST salvo com resposta perdida, pending e consulta manual sem retry", async ({ page, request }) => {
    await page.setViewportSize({ width: 390, height: 844 }); const c = await preparar(page, request); const { painel } = await c.abrir(); await conferirMensagem(painel, c.antiga); let posts = 0, salvo, liberar; const pendente = new Promise(resolve => { liberar = resolve; }); let leituraFalha = false;
    await page.route(`**${c.rota}`, async route => {
        if (route.request().method() === "POST") {
            posts++; if (posts === 1) { const r = await route.fetch(); expect(r.status()).toBe(201); salvo = await r.json(); await pendente; await route.abort("failed"); } else await route.continue();
        } else if (leituraFalha) { leituraFalha = false; await route.fulfill({ status: 503, contentType: "application/json", body: JSON.stringify({ erro: "Consulta temporariamente indisponível." }) }); } else await route.continue();
    });
    const texto = "Não perder este rascunho\nLiteral"; await page.getByRole("textbox", { name: "Comentário", exact: true }).fill(texto); await page.getByRole("button", { name: "Enviar comentário", exact: true }).focus(); await page.keyboard.press("Enter");
    try {
        await expect.poll(() => posts).toBe(1); for (const botao of await page.getByRole("button").all()) await expect(botao).toBeDisabled(); await expect(page.getByRole("textbox", { name: "Comentário", exact: true })).toBeDisabled(); await page.keyboard.press("Escape"); await expect(painel).toBeVisible();
        await painel.locator("form").evaluate(e => e.dispatchEvent(new Event("submit", { bubbles: true, cancelable: true }))); expect(posts).toBe(1);
    } finally { liberar(); }
    await expect(painel.getByText(aviso, { exact: true })).toBeVisible(); await expect(page.getByRole("textbox", { name: "Comentário", exact: true })).toHaveValue(texto); await expect(page.getByRole("button", { name: "Enviar comentário", exact: true })).toBeDisabled(); await expect(painel.getByRole("listitem")).toHaveCount(1);
    expect(Object.keys(salvo).sort()).toEqual([...campos].sort()); expect(salvo).toMatchObject({ texto, funcionario: "Dono", processoId: c.demanda.id }); expect(salvo.id).toBeGreaterThan(0); expect(salvo.dataHora).toMatch(/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}/); expect((await c.conversa()).comentarios).toEqual([c.antiga, salvo]); expect(await c.consultar()).toEqual(c.inicial); expect(await c.historico()).toEqual(c.eventos);
    leituraFalha = true; await painel.getByRole("button", { name: "Atualizar comentários" }).focus(); await page.keyboard.press("Enter"); await expect(painel.getByText("Consulta temporariamente indisponível.", { exact: true })).toBeVisible(); await expect(page.getByRole("button", { name: "Enviar comentário", exact: true })).toBeDisabled(); await expect(page.getByRole("textbox", { name: "Comentário", exact: true })).toHaveValue(texto); expect(posts).toBe(1);
    await painel.getByRole("button", { name: "Atualizar comentários" }).focus(); await page.keyboard.press("Enter"); await conferirMensagem(painel, salvo); await expect(painel.getByRole("listitem")).toHaveCount(2); await expect(page.getByRole("textbox", { name: "Comentário", exact: true })).toHaveValue(texto); await expect(page.getByRole("button", { name: "Enviar comentário", exact: true })).toBeEnabled(); await expect(painel.getByText(aviso, { exact: true })).toBeVisible(); expect(posts).toBe(1);
    const arquivo = await request.put(`${c.base}/demandas/${c.demanda.id}/arquivar`, { headers: c.headers, data: { versao: c.inicial.gerenciamento.versao } }); expect(arquivo.status()).toBe(200); const snapshotArquivo = await arquivo.json();
    await page.getByRole("button", { name: "Enviar comentário", exact: true }).focus(); await page.keyboard.press("Enter"); await expect(painel.getByText("Demanda arquivada. Restaure-a antes de alterar.", { exact: true })).toBeVisible(); await expect(page.getByRole("textbox", { name: "Comentário", exact: true })).toHaveValue(texto); expect(posts).toBe(2); expect((await c.conversa()).comentarios).toEqual([c.antiga, salvo]); expect(await c.consultar()).toEqual(snapshotArquivo);
    await painel.getByRole("button", { name: "Atualizar comentários" }).focus(); await page.keyboard.press("Enter"); await expect(painel.getByText("Demanda arquivada: comentários somente para consulta.", { exact: true })).toBeVisible(); await expect(painel.getByRole("textbox")).toHaveCount(0); await expect(painel.getByRole("button", { name: "Enviar comentário" })).toHaveCount(0); expect(await painel.locator("strong", { hasText: "Rascunho não enviado:" }).locator("+ p").textContent()).toBe(texto); expect(posts).toBe(2);
    const pasta = await mkdtemp(join(tmpdir(), "creral-comentarios-falha-qa-")); await viewport(page, painel, pasta, "mobile-recuperacao", { width: 390, height: 844 }); console.log(`QA comentários falha: ${pasta}`);
});
