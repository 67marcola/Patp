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
    const botao = page.getByRole("button", { name: "Atividade", exact: true }); await expect(botao).toBeEnabled(); await botao.focus(); await page.keyboard.press("Enter");
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

test("ACT-01–24/29/30/34/36 T18: duas contas,16 ações,before/after literais e snapshots preservados", async ({ page, request }) => {
    const id = randomUUID(), dona = await conta(request, "AutoraFluxo", id), colega = await conta(request, "ColegaFluxo", id), headers = { Authorization: `Bearer ${dona.token}` };
    const criar = await request.post("/api/gerenciamentos", { headers, data: { nome: `Fluxo ${id}`, descricao: "Descrição anterior", etapas: [{ nome: "Trabalho", setor: "Projeto", ordem: 1 }, { nome: "Trabalho", setor: "Campo", ordem: 2 }] } }); expect(criar.status()).toBe(201);
    const quadro = await criar.json(), base = `/api/gerenciamentos/${quadro.id}`;
    async function consultar() { const r = await request.get(`${base}/estrutura-etapas`, { headers }); expect(r.status()).toBe(200); return r.json(); }
    async function executar(metodo, rota, dados, status = 200) { const r = await request[metodo](`${base}${rota}`, { headers, data: dados }); expect(r.status()).toBe(status); return status === 204 ? null : r.json(); }
    let s = await consultar(); const trabalhos = s.etapas.filter(e => e.categoria === "TRABALHO"), origem = trabalhos[0], destino = trabalhos[1];
    s = await executar("post", "/etapas", { nome: "Livre", setor: "Teste", ordem: 3, versao: s.gerenciamento.versao }, 201); const removida = s.etapas.find(e => e.nome === "Livre");
    s = await executar("put", `/etapas/${removida.id}`, { nome: "Removida literal", setor: "Outro setor", ordem: 1, versao: s.gerenciamento.versao });
    s = await executar("delete", `/etapas/${removida.id}`, { versao: s.gerenciamento.versao }); expect(s.etapas.some(e => e.id === removida.id)).toBe(false);
    const prefixoAntes = "<img src=x onerror=alert(1)>\n", prefixoDepois = "<b>Depois literal</b>\n";
    const antes = prefixoAntes + "á".repeat(10000 - prefixoAntes.length), depois = prefixoDepois + "ç".repeat(10000 - prefixoDepois.length);
    s = await executar("post", "/demandas", { versao: s.gerenciamento.versao, numeroProcesso: `ACT-${id}`, pessoa: "Cliente anterior", responsavel: "Ana", prioridade: "Alta", dataEmissao: "2020-02-29", prazoEtapa: null, prazoGeral: null, observacoes: antes }, 201); const demanda = s.demandas[0];
    await entrar(page, dona, quadro); await page.getByRole("button", { name: `Editar demanda ${demanda.numeroProcesso}`, exact: true }).click();
    await page.getByRole("textbox", { name: "Cliente/solicitante", exact: true }).fill("<b>Cliente novo</b>"); await page.getByRole("textbox", { name: "Responsável", exact: true }).fill(""); await page.getByRole("textbox", { name: "Observações", exact: true }).fill(depois);
    await page.getByRole("button", { name: "Salvar alterações", exact: true }).click(); await expect(page.getByRole("form", { name: "Editar demanda", exact: true })).toHaveCount(0); s = await consultar();
    expect(s.demandas[0].observacoes).toBe(depois.trim()); expect(s.demandas[0].responsavel).toBeNull();
    const campos = s.demandas[0]; s = await executar("put", `/demandas/${demanda.id}`, { versao: s.gerenciamento.versao, pessoa: campos.pessoa, responsavel: campos.responsavel, prioridade: campos.prioridade, dataEmissao: campos.dataEmissao, prazoEtapa: campos.prazoEtapa, prazoGeral: campos.prazoGeral, observacoes: campos.observacoes });
    s = await executar("put", `/demandas/${demanda.id}/mover`, { versao: s.gerenciamento.versao, etapaId: destino.id });
    s = await executar("put", `/demandas/${demanda.id}/concluir`, { versao: s.gerenciamento.versao });
    s = await executar("put", `/demandas/${demanda.id}/reabrir`, { versao: s.gerenciamento.versao, etapaId: origem.id });
    s = await executar("put", `/demandas/${demanda.id}/cancelar`, { versao: s.gerenciamento.versao, motivo: "Motivo literal" });
    s = await executar("put", `/demandas/${demanda.id}/reabrir`, { versao: s.gerenciamento.versao, etapaId: origem.id });
    s = await executar("put", `/demandas/${demanda.id}/arquivar`, { versao: s.gerenciamento.versao });
    s = await executar("put", `/demandas/${demanda.id}/restaurar`, { versao: s.gerenciamento.versao });
    await executar("put", "", { versao: s.gerenciamento.versao, nome: "<b>Nome intermediário</b>", descricao: "Descrição corrigida\nLiteral" }); s = await consultar();
    await executar("put", "", { versao: s.gerenciamento.versao, nome: `Nome atual ${id}`, descricao: "Descrição corrigida\nLiteral" }); s = await consultar();
    await executar("put", "/arquivar", { versao: s.gerenciamento.versao }, 204); s = await consultar();
    await page.getByRole("button", { name: "← Voltar" }).click(); await page.getByRole("button", { name: "Sair", exact: true }).click(); await entrar(page, colega, s.gerenciamento, true);
    let painel = await abrir(page); await expect(painel).toContainText("Quadro arquivado"); await expect(painel).toContainText("Demanda arquivada"); await page.keyboard.press("Escape");
    await executar("put", "/restaurar", { versao: s.gerenciamento.versao }, 204); s = await consultar();
    s = await executar("put", `/etapas/${origem.id}`, { nome: "Nome posterior", setor: "Projeto", ordem: 1, versao: s.gerenciamento.versao });
    await page.getByRole("button", { name: "← Voltar" }).click(); await page.getByRole("button", { name: "Ativos", exact: true }).click(); await page.getByRole("button", { name: `Abrir ${s.gerenciamento.nome}`, exact: true }).click();
    const mensagem = "Mensagem privada exclusiva do comentário";
    await page.getByRole("button", { name: `Comentários da demanda ${demanda.numeroProcesso}`, exact: true }).click(); await page.getByRole("textbox", { name: "Comentário", exact: true }).fill(mensagem);
    const respostaComentario = page.waitForResponse(r => r.url().endsWith(`${base}/demandas/${demanda.id}/comentarios`) && r.request().method() === "POST");
    await page.getByRole("button", { name: "Enviar comentário", exact: true }).click(); const comentarioSalvo = await (await respostaComentario).json(); await expect(page.getByRole("textbox", { name: "Comentário", exact: true })).toHaveValue(""); await page.getByRole("button", { name: "Fechar comentários" }).click();
    const aposComentario = await consultar(); expect(aposComentario.gerenciamento.versao).toBe(s.gerenciamento.versao); expect(aposComentario.demandas).toEqual(s.demandas);
    const historico = await request.get(`/api/processos/${demanda.id}/historico`, { headers }); expect(historico.status()).toBe(200); expect((await historico.json()).length).toBe(10);
    const consulta = await request.get(`${base}/atividade`, { headers }); expect(consulta.status()).toBe(200); const atividade = await consulta.json(); expect(atividade.registros).toHaveLength(20); expect(atividade.proximoCursor).toBeNull();
    expect(new Set(atividade.registros.map(r => r.tipo))).toEqual(new Set(["QUADRO_CRIADO", "QUADRO_EDITADO", "QUADRO_ARQUIVADO", "QUADRO_RESTAURADO", "ETAPA_CRIADA", "ETAPA_EDITADA", "ETAPA_REMOVIDA", "DEMANDA_CRIADA", "DEMANDA_EDITADA", "DEMANDA_MOVIDA", "DEMANDA_CONCLUIDA", "DEMANDA_CANCELADA", "DEMANDA_REABERTA", "DEMANDA_ARQUIVADA", "DEMANDA_RESTAURADA", "COMENTARIO_ADICIONADO"]));
    expect(atividade.registros.every(r => r.origem === "AUTOMATICA" && r.gerenciamentoId === quadro.id && r.autorId === (r.tipo === "COMENTARIO_ADICIONADO" ? colega.id : dona.id) && r.autorNome === (r.tipo === "COMENTARIO_ADICIONADO" ? "ColegaFluxo" : "AutoraFluxo") && r.dataHora && r.descricao === null)).toBe(true);
    const envio = atividade.registros.find(r => r.tipo === "COMENTARIO_ADICIONADO"); expect(envio).toMatchObject({ alvoTipo: "DEMANDA", alvoId: demanda.id, alvoNome: demanda.numeroProcesso, autorId: colega.id, autorNome: "ColegaFluxo", dataHora: comentarioSalvo.dataHora, descricao: null, alteracoes: [] });
    for (const [tipo, alvoTipo, alvoId, alvoNome, anterior, novo] of [["QUADRO_ARQUIVADO", "QUADRO", quadro.id, s.gerenciamento.nome, "false", "true"], ["QUADRO_RESTAURADO", "QUADRO", quadro.id, s.gerenciamento.nome, "true", "false"], ["DEMANDA_ARQUIVADA", "DEMANDA", demanda.id, demanda.numeroProcesso, "false", "true"], ["DEMANDA_RESTAURADA", "DEMANDA", demanda.id, demanda.numeroProcesso, "true", "false"]]) expect(atividade.registros.find(r => r.tipo === tipo).alteracoes).toEqual([{ alvoTipo, alvoId, alvoNome, campo: "arquivado", anterior, novo }]);
    const quadroIntermediario = atividade.registros.find(r => r.tipo === "QUADRO_EDITADO" && r.alvoNome === "<b>Nome intermediário</b>"); expect(quadroIntermediario.alteracoes).toEqual([{ alvoTipo: "QUADRO", alvoId: quadro.id, alvoNome: "<b>Nome intermediário</b>", campo: "nome", anterior: quadro.nome, novo: "<b>Nome intermediário</b>" }, { alvoTipo: "QUADRO", alvoId: quadro.id, alvoNome: "<b>Nome intermediário</b>", campo: "descricao", anterior: "Descrição anterior", novo: "Descrição corrigida\nLiteral" }]);
    expect(JSON.stringify(atividade)).not.toContain(mensagem); const movimento = atividade.registros.find(r => r.tipo === "DEMANDA_MOVIDA"); expect(movimento.alteracoes).toEqual([{ alvoTipo: "DEMANDA", alvoId: demanda.id, alvoNome: demanda.numeroProcesso, campo: "etapaId", anterior: String(origem.id), novo: String(destino.id) }, { alvoTipo: "DEMANDA", alvoId: demanda.id, alvoNome: demanda.numeroProcesso, campo: "etapaNome", anterior: "Trabalho", novo: "Trabalho" }]);
    const excluida = atividade.registros.find(r => r.tipo === "ETAPA_REMOVIDA"); expect(excluida.alvoId).toBe(removida.id); expect(excluida.alvoNome).toBe("Removida literal"); expect(excluida.alteracoes).toEqual(expect.arrayContaining([{ alvoTipo: "ETAPA", alvoId: removida.id, alvoNome: "Removida literal", campo: "nome", anterior: "Removida literal", novo: null }]));
    const edicao = atividade.registros.find(r => r.tipo === "DEMANDA_EDITADA" && r.alteracoes.length > 0); expect(edicao.alteracoes).toEqual([{ alvoTipo: "DEMANDA", alvoId: demanda.id, alvoNome: demanda.numeroProcesso, campo: "pessoa", anterior: "Cliente anterior", novo: "<b>Cliente novo</b>" }, { alvoTipo: "DEMANDA", alvoId: demanda.id, alvoNome: demanda.numeroProcesso, campo: "responsavel", anterior: "Ana", novo: null }, { alvoTipo: "DEMANDA", alvoId: demanda.id, alvoNome: demanda.numeroProcesso, campo: "observacoes", anterior: antes.trim(), novo: depois.trim() }]);
    painel = await abrir(page); expect(await painel.locator("[data-atividade-chave]").count()).toBe(20); expect(await painel.locator("[data-atividade-chave]").evaluateAll(es => es.map(e => e.dataset.atividadeChave))).toEqual(atividade.registros.map(r => r.chave));
    for (const [tipo, valores] of [["QUADRO_ARQUIVADO", ["Não", "Sim"]], ["QUADRO_RESTAURADO", ["Sim", "Não"]], ["DEMANDA_ARQUIVADA", ["Não", "Sim"]], ["DEMANDA_RESTAURADA", ["Sim", "Não"]]]) {
        const evento = atividade.registros.find(r => r.tipo === tipo);
        expect(await painel.locator(`[data-atividade-chave="${evento.chave}"]`).locator(".atividade-alteracao").filter({ hasText: "Arquivado" }).locator("dd").allTextContents()).toEqual(valores);
    }
    const editada = painel.locator(`[data-atividade-chave="${edicao.chave}"]`); expect(await editada.locator(".atividade-alteracao").filter({ hasText: "Observações" }).locator("dd").allTextContents()).toEqual([antes.trim(), depois.trim()]); expect(await editada.locator(".atividade-alteracao").filter({ hasText: "Responsável" }).locator("dd").allTextContents()).toEqual(["Ana", "Não informado"]); expect(await editada.locator("img,b,script").count()).toBe(0);
    await expect(painel.locator(`[data-atividade-chave="${excluida.chave}"]`)).toContainText("Removida literal"); await expect(painel.locator(`[data-atividade-chave="${movimento.chave}"]`)).toContainText(`Trabalho (ID ${origem.id})`); await expect(painel.locator(`[data-atividade-chave="${movimento.chave}"]`)).toContainText(`Trabalho (ID ${destino.id})`);
    await expect(painel).toContainText("Nenhum campo foi alterado"); await expect(painel).toContainText("<b>Nome intermediário</b>"); expect(await painel.textContent()).not.toContain(mensagem);
    const pasta = await mkdtemp(join(tmpdir(), "creral-atividade-T18-fluxo-")); await capturar(page, painel, pasta, "fluxo-desktop", { width: 1440, height: 900 }); await capturar(page, painel, pasta, "fluxo-360", { width: 360, height: 740 });
    await page.keyboard.press("Escape"); await expect(page.getByRole("button", { name: "Atividade", exact: true })).toBeFocused(); await page.reload(); await page.getByRole("button", { name: `Abrir ${s.gerenciamento.nome}`, exact: true }).click(); painel = await abrir(page); expect(await painel.locator("[data-atividade-chave]").count()).toBe(20); console.log(`Capturas T18 fluxo: ${pasta}`);
});

test("ACT-22/25–28/30/32/33/36 T18:121 legados e duas contas paginam fontes mistas sem deslocamento", async ({ page, request }) => {
    const id = randomUUID(), leitora = await conta(request, "LeitoraAntiga", id), colega = await conta(request, "ColegaAntigo", id), headers = { Authorization: `Bearer ${leitora.token}` }, quadro = { id: 900000, nome: "Quadro histórico fictício" }, base = `/api/gerenciamentos/${quadro.id}`;
    async function comentar(usuario, texto) { const r = await request.post(`${base}/demandas/900001/comentarios`, { headers: { Authorization: `Bearer ${usuario.token}` }, data: { texto } }); expect(r.status()).toBe(201); return r.json(); }
    const primeiraMensagem = await comentar(leitora, "Primeira mensagem privada"); await entrar(page, leitora, quadro); const painel = await abrir(page); await expect(painel.locator("[data-atividade-chave]")).toHaveCount(50);
    const r = await request.get(`${base}/atividade`, { headers }); expect(r.status()).toBe(200); const inicio = await r.json(); expect(inicio.registros).toHaveLength(50); expect(inicio.registros[0]).toMatchObject({ origem: "AUTOMATICA", tipo: "COMENTARIO_ADICIONADO", autorId: leitora.id, autorNome: "LeitoraAntiga", dataHora: primeiraMensagem.dataHora });
    const segundaMensagem = await comentar(colega, "Segunda mensagem privada"); expect(segundaMensagem.funcionario).toBe("ColegaAntigo");
    const esperados = [inicio.registros[0].chave, ...Array.from({ length: 120 }, (_, i) => `HISTORICO_EXISTENTE:${900120 - i}`), "HISTORICO_EXISTENTE:900121"];
    await painel.getByRole("button", { name: "Carregar mais" }).click(); await expect(painel.locator("[data-atividade-chave]")).toHaveCount(100); await painel.getByRole("button", { name: "Carregar mais" }).click(); await expect(painel.locator("[data-atividade-chave]")).toHaveCount(122);
    expect(await painel.locator("[data-atividade-chave]").evaluateAll(es => es.map(e => e.dataset.atividadeChave))).toEqual(esperados); expect(new Set(esperados).size).toBe(122); await expect(painel.getByRole("button", { name: "Carregar mais" })).toHaveCount(0); expect(await painel.textContent()).not.toContain("ColegaAntigo");
    const antigo = painel.locator('[data-atividade-chave="HISTORICO_EXISTENTE:900120"]'); expect(await antigo.locator("h3").textContent()).toBe(" Ação antiga 120 "); expect(await antigo.locator(".atividade-texto").textContent()).toBe(" <b>Descrição antiga 120</b>\n fim "); await expect(antigo).toContainText(" Autor antigo "); await expect(antigo).toContainText("Histórico existente"); expect(await antigo.locator("b").count()).toBe(0);
    const ausente = painel.locator('[data-atividade-chave="HISTORICO_EXISTENTE:900121"]'); await expect(ausente).toContainText("Não informado"); expect(await ausente.locator("time").count()).toBe(0);
    await painel.getByRole("button", { name: "Atualizar", exact: true }).click(); await expect(painel.locator("[data-atividade-chave]")).toHaveCount(50); await expect(painel.locator("[data-atividade-chave]").first()).toContainText("ColegaAntigo");
    await painel.getByRole("button", { name: "Carregar mais" }).click(); await expect(painel.locator("[data-atividade-chave]")).toHaveCount(100); await painel.getByRole("button", { name: "Carregar mais" }).click(); await expect(painel.locator("[data-atividade-chave]")).toHaveCount(123);
    const atuais = await painel.locator("[data-atividade-chave]").evaluateAll(es => es.map(e => e.dataset.atividadeChave)); expect(atuais.slice(1)).toEqual(esperados); expect(new Set(atuais).size).toBe(123); expect(await painel.textContent()).not.toContain("Primeira mensagem privada"); expect(await painel.textContent()).not.toContain("Segunda mensagem privada");
    const pasta = await mkdtemp(join(tmpdir(), "creral-atividade-T18-mista-")); await capturar(page, painel, pasta, "mista-desktop", { width: 1440, height: 900 }); await capturar(page, painel, pasta, "mista-360", { width: 360, height: 740 });
    const preservados = await request.get("/api/processos/900001/historico", { headers }); expect(preservados.status()).toBe(200); const historicos = await preservados.json(); expect(historicos).toHaveLength(121); expect(historicos.find(h => h.id === 900120)).toMatchObject({ acao: " Ação antiga 120 ", descricao: " <b>Descrição antiga 120</b>\n fim ", usuario: " Autor antigo ", dataHora: "2010-01-02T03:04:05" }); expect(historicos.find(h => h.id === 900121)).toMatchObject({ descricao: null, usuario: null, dataHora: null }); console.log(`Capturas T18 mista: ${pasta}`);
});
