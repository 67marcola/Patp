import { test } from "node:test";
import assert from "node:assert/strict";
import { spawn, spawnSync } from "node:child_process";
import { cp, mkdtemp, readFile, rm, writeFile, mkdir } from "node:fs/promises";
import { tmpdir } from "node:os";
import { join, resolve, delimiter, extname, sep } from "node:path";
import { pathToFileURL, fileURLToPath } from "node:url";
import { createServer as httpsServer } from "node:https";
import { createServer as netServer } from "node:net";
import { request as httpRequest } from "node:http";
import { randomUUID } from "node:crypto";
import { chromium, expect } from "@playwright/test";

const frontend = fileURLToPath(new URL("..", import.meta.url));
const projeto = resolve(frontend, "..");
const mysql = process.env.CRERAL_TEST_MYSQL_EXE || "C:/Program Files/MySQL/MySQL Server 8.0/bin/mysql.exe";
const datadirEsperado = "c:/users/marco/appdata/local/temp/creral-arquivo-049cfd5cc86d4d8b840e41f1710d7a43/mysql/";
const senhaConta = "Internet123!";
function sql(comando) {
    const r = spawnSync(mysql, ["--protocol=TCP", "--host=localhost", "--port=33823", "--connect-timeout=2", "--user=root", "--ssl-mode=VERIFY_IDENTITY", `--ssl-ca=${process.env.CRERAL_TEST_CA_FILE}`, "--batch", "--raw", "--skip-column-names", `--execute=${comando}`], { windowsHide: true, encoding: "utf8", env: { ...process.env, MYSQL_PWD: "" } });
    assert.equal(r.status, 0, "Fixture MySQL TLS33823 indisponível; nenhuma operação pode usar3306.");
    return r.stdout.trim();
}
function identidade() {
    const [porta, datadir] = sql("SELECT @@port,@@datadir").split("\t");
    assert.equal(porta, "33823");
    assert.equal(datadir.replaceAll("\\", "/").toLowerCase(), datadirEsperado);
}
function executar(cmd, args, options = {}) {
    const c = spawn(cmd, args, { windowsHide: true, stdio: ["ignore", "pipe", "pipe"], ...options });
    c.texto = "";
    c.stdout.setEncoding("utf8"); c.stderr.setEncoding("utf8");
    c.stdout.on("data", d => { c.texto += d; }); c.stderr.on("data", d => { c.texto += d; });
    c.resultado = new Promise((r, reject) => { c.once("error", reject); c.once("exit", r); });
    return c;
}
async function parar(c) {
    if (c.exitCode !== null || c.signalCode !== null) return;
    if (process.platform === "win32") spawnSync("taskkill.exe", ["/PID", String(c.pid), "/T", "/F"], { windowsHide: true, stdio: "ignore" });
    else c.kill("SIGKILL");
    await c.resultado;
}
async function portaLivre() {
    const s = netServer(); await new Promise(r => s.listen(0, "127.0.0.1", r));
    const p = s.address().port; await new Promise(r => s.close(r)); return p;
}
async function limite(promise, ms, mensagem) {
    let timer;
    try { return await Promise.race([promise, new Promise((_, reject) => { timer = setTimeout(() => reject(new Error(mensagem)), ms); })]); }
    finally { clearTimeout(timer); }
}
async function pronto(c, api) {
    const inicio = Date.now();
    while (true) {
        assert.equal(c.exitCode, null, `API encerrou antes de pronta: ${c.texto}`);
        try { if ((await fetch(`${api}/gerenciamentos`, { signal: AbortSignal.timeout(1000) })).status === 401) return; } catch { /* Ainda iniciando. */ }
        assert.ok(Date.now() - inicio < 60000, "API não iniciou em60s");
        await new Promise(r => setTimeout(r, 150));
    }
}

test("NET: pacote online, TLS real, aprovação, dois navegadores e reinício preservam dados", { timeout: 300000 }, async () => {
    for (const nome of ["CRERAL_TEST_CA_FILE", "CRERAL_TEST_TRUSTSTORE", "CRERAL_TEST_SERVER_CERT", "CRERAL_TEST_SERVER_KEY", "JAVA_HOME"]) assert.ok(process.env[nome], `${nome} obrigatório para a fixture.`);
    identidade();
    const uid = randomUUID().replaceAll("-", "").slice(0, 12), schema = `internet_e2e_${uid}`, vazio = `internet_vazio_${uid}`, usuario = `internet_${uid}`, senha = `Ficticio_${uid}!`;
    const raiz = await mkdtemp(join(tmpdir(), "creral-internet-e2e-")), filhos = [], servidores = [];
    const artefatos = process.env.CRERAL_TEST_ARTIFACTS || join(raiz, "evidencias");
    let browser, criados = false;
    try {
        await mkdir(artefatos, { recursive: true });
        const certificados = { cert: await readFile(process.env.CRERAL_TEST_SERVER_CERT), key: await readFile(process.env.CRERAL_TEST_SERVER_KEY) };
        const portaJava = await portaLivre(), api = `http://127.0.0.1:${portaJava}/api`;
        const proxy = httpsServer(certificados, (req, res) => {
            const destino = httpRequest({ hostname: "127.0.0.1", port: portaJava, path: req.url, method: req.method, headers: { ...req.headers, "x-forwarded-proto": "https" } }, resposta => { res.writeHead(resposta.statusCode, resposta.headers); resposta.pipe(res); });
            destino.on("error", () => { res.writeHead(503); res.end(); }); req.pipe(destino);
        });
        await new Promise(r => proxy.listen(0, "127.0.0.1", r)); servidores.push(proxy);
        const apiPublica = `https://127.0.0.1:${proxy.address().port}/api`;
        let dist;
        const ui = httpsServer(certificados, async (req, res) => {
            try {
                if (!dist) { res.writeHead(503); res.end(); return; }
                const caminho = decodeURIComponent(new URL(req.url, "https://localhost").pathname), relativo = caminho === "/" ? "index.html" : caminho.slice(1);
                const arquivo = resolve(dist, relativo);
                if (!arquivo.startsWith(dist + sep)) { res.writeHead(400); res.end(); return; }
                const corpo = await readFile(arquivo), tipo = { ".html": "text/html", ".js": "application/javascript", ".css": "text/css", ".svg": "image/svg+xml" }[extname(arquivo)] || "application/octet-stream";
                res.writeHead(200, { "Content-Type": tipo }); res.end(corpo);
            } catch { res.writeHead(404); res.end(); }
        });
        await new Promise(r => ui.listen(0, "127.0.0.1", r)); servidores.push(ui);
        const origem = `https://127.0.0.1:${ui.address().port}`;
        const trust = encodeURIComponent(pathToFileURL(process.env.CRERAL_TEST_TRUSTSTORE).href);
        const urlBanco = `jdbc:mysql://localhost:33823/${schema}?sslMode=VERIFY_IDENTITY&trustCertificateKeyStoreUrl=${trust}&trustCertificateKeyStoreType=PKCS12&trustCertificateKeyStorePassword=changeit&fallbackToSystemTrustStore=false`;
        const env = { ...process.env, CRERAL_DATABASE_URL: urlBanco, CRERAL_DATABASE_USERNAME: usuario, CRERAL_DATABASE_PASSWORD: senha, CRERAL_FRONTEND_ORIGIN: origem, PORT: String(portaJava) };
        // Nenhum SPRING_* herdado pode substituir a configuração desta fixture.
        for (const nome of Object.keys(env)) if (nome.startsWith("SPRING_")) delete env[nome];
        sql(`CREATE DATABASE ${schema}; CREATE DATABASE ${vazio}; CREATE USER '${usuario}'@'%' IDENTIFIED BY '${senha}'; GRANT ALL ON ${schema}.* TO '${usuario}'@'%'; GRANT ALL ON ${vazio}.* TO '${usuario}'@'%';`); criados = true;
        const copiaSistema = join(raiz, "sistema"), copiaFront = join(raiz, "frontend"), cpFile = join(raiz, "classpath.txt");
        await cp(join(projeto, "sistema/src/main"), join(copiaSistema, "src/main"), { recursive: true, filter: p => p !== join(projeto, "sistema/src/main/resources/application.properties") });
        await cp(join(projeto, "sistema/pom.xml"), join(copiaSistema, "pom.xml"));
        const compile = executar(process.env.ComSpec || "cmd.exe", ["/d", "/s", "/c", `mvn.cmd -B -q compile dependency:build-classpath -DincludeScope=runtime "-Dmdep.outputFile=${cpFile}"`], { cwd: copiaSistema, windowsVerbatimArguments: true });
        filhos.push(compile); assert.equal(await limite(compile.resultado, 120000, "Compilação excedeu120s"), 0, `Compilação falhou: ${compile.texto}`);
        const java = join(process.env.JAVA_HOME, "bin/java.exe"), classpath = [join(copiaSistema, "target/classes"), (await readFile(cpFile, "utf8")).trim()].join(delimiter);
        const cliInvalida = executar(java, ["-cp", classpath, "com.patp.sistema.PrepararBancoInternet", "--plano"], { cwd: copiaSistema, env: { ...env, CRERAL_DATABASE_URL: urlBanco + "&sslMode=DISABLED" } });
        filhos.push(cliInvalida); assert.equal(await limite(cliInvalida.resultado, 15000, "Guarda do preparador não encerrou"), 1); assert.match(cliInvalida.texto, /TLS VERIFY_IDENTITY/);
        const cli = async (...args) => { const c = executar(java, ["-cp", classpath, "com.patp.sistema.PrepararBancoInternet", ...args], { cwd: copiaSistema, env }); filhos.push(c); assert.equal(await limite(c.resultado, 15000, "Preparador excedeu15s"), 0, c.texto); assert.ok(!c.texto.includes(senha)); return c.texto; };
        assert.match(await cli("--plano"), /nenhum dado ou estrutura foi gravado/);
        assert.equal(sql(`SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='${schema}'`), "0");
        assert.match(await cli("--aplicar"), /8 tabelas, sem contas ou dados importados/);
        assert.equal(sql(`SELECT COUNT(*) FROM ${schema}.usuarios`), "0");
        assert.equal(sql(`SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='${schema}'`), "8");
        const iniciar = (ambiente = env) => { const c = executar(java, ["-cp", classpath, "com.patp.sistema.SistemaApplication", "--spring.config.location=classpath:/application-internet.properties", "--spring.profiles.active=internet", "--creral.internet.enabled=true", "--spring.datasource.hikari.connection-timeout=1500", "--spring.datasource.hikari.initialization-fail-timeout=1"], { cwd: copiaSistema, env: ambiente }); filhos.push(c); return c; };
        const semConfig = iniciar({ ...env, CRERAL_DATABASE_PASSWORD: "" });
        assert.equal(await limite(semConfig.resultado, 15000, "Guarda não encerrou"), 1); assert.match(semConfig.texto, /CRERAL_DATABASE_PASSWORD/); assert.ok(!semConfig.texto.includes(senha));
        const tlsFraco = iniciar({ ...env, CRERAL_DATABASE_URL: urlBanco.replace("VERIFY_IDENTITY", "REQUIRED") });
        assert.equal(await limite(tlsFraco.resultado, 15000, "GuardaTLS não encerrou"), 1); assert.match(tlsFraco.texto, /TLS VERIFY_IDENTITY/); assert.ok(!tlsFraco.texto.includes(senha));
        const trustDesconhecido = encodeURIComponent(pathToFileURL(join(process.env.JAVA_HOME, "lib/security/cacerts")).href);
        const caDesconhecida = iniciar({ ...env, CRERAL_DATABASE_URL: urlBanco.replace(trust, trustDesconhecido) });
        assert.equal(await limite(caDesconhecida.resultado, 35000, "TLS não confiável não encerrou"), 1); assert.match(caDesconhecida.texto, /certificate|certification|trustAnchors/i); assert.ok(!caDesconhecida.texto.includes(senha));
        const estrutura = iniciar({ ...env, CRERAL_DATABASE_URL: urlBanco.replace(`/${schema}?`, `/${vazio}?`) });
        assert.equal(await limite(estrutura.resultado, 30000, "Validate não encerrou"), 1); assert.equal(sql(`SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='${vazio}'`), "0"); assert.ok(!estrutura.texto.includes(senha));
        let servidor = iniciar(); await pronto(servidor, api); assert.ok(!servidor.texto.includes(senha));
        const preflight = await fetch(`${api}/administracao/cadastros/pendentes`, { method: "OPTIONS", headers: { Origin: origem, "Access-Control-Request-Method": "GET", "Access-Control-Request-Headers": "authorization" } });
        assert.equal(preflight.status, 200); assert.equal(preflight.headers.get("access-control-allow-origin"), origem);
        const intruso = await fetch(`${api}/gerenciamentos`, { method: "OPTIONS", headers: { Origin: "https://intruso.example", "Access-Control-Request-Method": "GET" } }); assert.equal(intruso.headers.get("access-control-allow-origin"), null);
        const contaAdmin = { nome: "Admin Internet", setor: "Teste", email: `admin-${uid}@example.test`, senha: senhaConta };
        const contaColega = { nome: "Colega Internet", setor: "Teste", email: `colega-${uid}@example.test`, senha: senhaConta };
        const pedido = async (rota, { token, dados, method = dados ? "POST" : "GET" } = {}) => fetch(api + rota, { method, headers: { ...(token ? { Authorization: `Bearer ${token}` } : {}), ...(dados ? { "Content-Type": "application/json" } : {}) }, ...(dados ? { body: JSON.stringify(dados) } : {}) });
        const cadastro = await pedido("/usuarios/cadastro", { dados: { ...contaAdmin, papel: "ADMINISTRADOR", aprovado: true } });
        assert.equal(cadastro.status, 202); assert.deepEqual(await cadastro.json(), { estado: "PENDENTE", mensagem: "Cadastro aguardando aprovação do administrador." });
        assert.equal(sql(`SELECT papel FROM ${schema}.usuarios WHERE email='${contaAdmin.email}'`), "FUNCIONARIO");
        const negado = await pedido("/usuarios/login", { dados: { email: contaAdmin.email, senha: senhaConta } });
        assert.equal(negado.status, 403); assert.deepEqual(await negado.json(), { erro: "Cadastro aguardando aprovação do administrador." });
        const idAdmin = Number(sql(`SELECT id FROM ${schema}.usuarios WHERE email='${contaAdmin.email}'`));
        await cli("--administrador", "--id", String(idAdmin), "--email", contaAdmin.email);
        // Fonte/dependências físicas próprias: nenhum dist/cache do workspace é escrito.
        await cp(frontend, copiaFront, { recursive: true, filter: p => ![join(frontend, "dist"), join(frontend, "playwright-report"), join(frontend, "test-results")].includes(p) });
        const build = executar(process.execPath, [join(copiaFront, "node_modules/vite/bin/vite.js"), "build"], { cwd: copiaFront, env: { ...process.env, VITE_MODO_INTERNET: "true", VITE_API_URL: apiPublica, CRERAL_TEST_INTERNET: "true" } });
        filhos.push(build); assert.equal(await limite(build.resultado, 60000, "Vite excedeu60s"), 0, build.texto); dist = resolve(copiaFront, "dist");
        const html = await readFile(join(dist, "index.html"), "utf8"), asset = /src="([^"]+\.js)"/.exec(html)[1], js = await readFile(join(dist, asset.slice(1)), "utf8");
        assert.ok(js.includes(apiPublica)); assert.ok(!js.includes("localhost:8081")); assert.ok(!js.includes(senha)); assert.ok(!js.includes(usuario));
        browser = await chromium.launch({ channel: "msedge", headless: true });
        const contextoAdmin = await browser.newContext({ ignoreHTTPSErrors: true }), contextoColega = await browser.newContext({ ignoreHTTPSErrors: true, viewport: { width: 360, height: 800 }, isMobile: true, hasTouch: true });
        const admin = await contextoAdmin.newPage(), colega = await contextoColega.newPage(), origensApi = new Set();
        for (const page of [admin, colega]) page.on("request", r => { if (new URL(r.url()).pathname.startsWith("/api/")) origensApi.add(new URL(r.url()).origin); });
        async function login(page, conta) { await page.goto(origem); await page.getByLabel("E-mail", { exact: true }).fill(conta.email); await page.getByLabel("Senha", { exact: true }).fill(senhaConta); await page.getByRole("button", { name: "Entrar", exact: true }).click(); await expect(page.getByRole("heading", { name: "Gerenciamentos", exact: true })).toBeVisible(); }
        await colega.goto(origem); await colega.getByRole("button", { name: "Criar cadastro", exact: true }).click();
        for (const [label, valor] of [["Nome completo", contaColega.nome], ["Setor", contaColega.setor], ["E-mail", contaColega.email], ["Senha", senhaConta], ["Confirmar senha", senhaConta]]) await colega.getByLabel(label, { exact: true }).fill(valor);
        await colega.getByRole("button", { name: "Criar cadastro", exact: true }).click();
        await expect(colega.getByRole("status")).toHaveText("Cadastro aguardando aprovação do administrador.");
        assert.equal(await colega.evaluate(() => localStorage.getItem("token")), null); assert.equal(await colega.evaluate(() => localStorage.getItem("usuario")), null);
        await login(admin, contaAdmin); const tokenAdmin = await admin.evaluate(() => localStorage.getItem("token"));
        const idColega = Number(sql(`SELECT id FROM ${schema}.usuarios WHERE email='${contaColega.email}'`));
        assert.equal((await pedido("/administracao/cadastros/pendentes")).status, 401);
        const pendentes = await pedido("/administracao/cadastros/pendentes", { token: tokenAdmin }); assert.equal(pendentes.status, 200); assert.deepEqual(await pendentes.json(), [{ id: idColega, nome: contaColega.nome, setor: "Teste", email: contaColega.email }]);
        await admin.getByRole("button", { name: "Aprovar cadastros", exact: true }).click(); await expect(admin.getByText(contaColega.email, { exact: true })).toBeVisible();
        // Mede o painel real no mesmo viewport do celular, incluindo textos e controles.
        await admin.setViewportSize({ width: 360, height: 800 });
        const painel = admin.getByRole("region", { name: "Cadastros aguardando aprovação" });
        const dimensoes = await painel.evaluate(el => ({ x: el.getBoundingClientRect().x, right: el.getBoundingClientRect().right, width: el.clientWidth, scroll: el.scrollWidth }));
        assert.ok(dimensoes.x >= 0 && dimensoes.right <= 360 && dimensoes.scroll <= dimensoes.width, `Painel ultrapassa360px: ${JSON.stringify(dimensoes)}`);
        await admin.screenshot({ path: join(artefatos, "aprovacoes-360.png"), fullPage: true });
        await admin.getByRole("button", { name: `Aprovar ${contaColega.nome}`, exact: true }).click(); await expect(admin.getByRole("status")).toHaveText(`Cadastro de ${contaColega.nome} aprovado.`);
        const primeiraAprovacao = sql(`SELECT aprovado,aprovado_por,aprovado_em FROM ${schema}.acessos_usuarios WHERE usuario_id=${idColega}`);
        assert.equal((await pedido(`/administracao/cadastros/${idColega}/aprovar`, { token: tokenAdmin, method: "POST" })).status, 200); assert.equal(sql(`SELECT aprovado,aprovado_por,aprovado_em FROM ${schema}.acessos_usuarios WHERE usuario_id=${idColega}`), primeiraAprovacao);
        assert.equal(sql(`SELECT papel FROM ${schema}.usuarios WHERE id=${idColega}`), "FUNCIONARIO");
        await admin.getByRole("button", { name: "Fechar aprovações", exact: true }).click(); await admin.setViewportSize({ width: 1280, height: 800 });
        await colega.getByRole("button", { name: "Voltar para login", exact: true }).click(); await login(colega, contaColega);
        await expect(colega.getByRole("button", { name: "Aprovar cadastros", exact: true })).toHaveCount(0);
        const tokenColega = await colega.evaluate(() => localStorage.getItem("token")); assert.equal((await pedido("/administracao/cadastros/pendentes", { token: tokenColega })).status, 403);
        const nomeQuadro = `Internet ${uid}`;
        await admin.getByRole("button", { name: "+ Criar gerenciamento" }).click(); await admin.getByLabel("Nome do gerenciamento").fill(nomeQuadro); await admin.getByRole("button", { name: "+ Adicionar etapa" }).click(); await admin.getByLabel("Nome da etapa 1", { exact: true }).fill("Trabalho"); await admin.getByLabel("Setor responsável da etapa 1", { exact: true }).fill("Teste"); await admin.getByRole("button", { name: "Salvar gerenciamento", exact: true }).click();
        await admin.getByRole("button", { name: `Abrir ${nomeQuadro}`, exact: true }).click(); await admin.getByRole("button", { name: "+ Criar processo" }).click(); await admin.getByLabel("Número da demanda").fill(`NET-${uid}`); await admin.getByLabel("Cliente/solicitante").fill("Cliente Internet"); await admin.getByRole("button", { name: "Criar demanda", exact: true }).click();
        await expect(admin.getByRole("article", { name: `NET-${uid} — Cliente Internet`, exact: true })).toBeVisible();
        await colega.reload(); await colega.getByRole("button", { name: `Abrir ${nomeQuadro}`, exact: true }).click(); await expect(colega.getByRole("article", { name: `NET-${uid} — Cliente Internet`, exact: true })).toBeVisible();
        assert.deepEqual([...origensApi], [new URL(apiPublica).origin]);
        await colega.screenshot({ path: join(artefatos, "demanda-360.png"), fullPage: true });
        const quadro = (await (await pedido("/gerenciamentos?arquivado=false", { token: tokenAdmin })).json()).find(q => q.nome === nomeQuadro);
        const antes = await (await pedido(`/gerenciamentos/${quadro.id}/estrutura-etapas`, { token: tokenAdmin })).json();
        await parar(servidor); identidade(); assert.equal(sql(`SELECT COUNT(*) FROM ${schema}.processos`), "1");
        servidor = iniciar(); await pronto(servidor, api);
        assert.equal((await pedido("/gerenciamentos", { token: tokenAdmin })).status, 401);
        const reentrada = await pedido("/usuarios/login", { dados: { email: contaAdmin.email, senha: senhaConta } }); assert.equal(reentrada.status, 200); const novaSessao = await reentrada.json();
        assert.deepEqual(await (await pedido(`/gerenciamentos/${quadro.id}/estrutura-etapas`, { token: novaSessao.token })).json(), antes);
        assert.equal(sql(`SELECT aprovado,aprovado_por,aprovado_em FROM ${schema}.acessos_usuarios WHERE usuario_id=${idColega}`), primeiraAprovacao);
        await contextoAdmin.clearCookies(); await admin.evaluate(() => localStorage.clear()); await login(admin, contaAdmin);
        const tokenSaida = await admin.evaluate(() => localStorage.getItem("token")); await admin.getByRole("button", { name: "Sair", exact: true }).click(); await expect(admin.getByRole("button", { name: "Entrar", exact: true })).toBeVisible();
        assert.equal(await admin.evaluate(() => localStorage.getItem("token")), null); assert.equal(await admin.evaluate(() => localStorage.getItem("usuario")), null); assert.equal((await pedido("/gerenciamentos", { token: tokenSaida })).status, 401);
        await parar(servidor); sql(`DROP TABLE ${schema}.acessos_usuarios`);
        const semTabelaOnline = iniciar(); assert.equal(await limite(semTabelaOnline.resultado, 30000, "Estrutura online ausente não encerrou"), 1); assert.match(semTabelaOnline.texto, /Estrutura online de aprovacao incompativel/); assert.equal(sql(`SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='${schema}'`), "7");
        assert.ok(!filhos.some(c => c.texto.includes(senha)));
        await writeFile(join(artefatos, "resultado.json"), JSON.stringify({ ambiente: "fixture TLS local; não prova nuvem/4G", portaMySQL: 33823, cadastrosPendentes: true, aprovacao: true, persistencia: true, viewport: 360, origensApi: [...origensApi] }, null, 2));
        console.log(`Evidências integradas: ${artefatos}`);
    } finally {
        if (browser) await browser.close();
        for (const c of filhos) await parar(c);
        for (const s of servidores) { s.closeAllConnections(); await new Promise(r => s.close(r)); }
        if (criados) { identidade(); sql(`DROP DATABASE ${schema}; DROP DATABASE ${vazio}; DROP USER '${usuario}'@'%';`); }
        const absoluto = resolve(raiz), base = resolve(tmpdir()) + sep;
        assert.ok(absoluto.startsWith(base) && absoluto.split(sep).at(-1).startsWith("creral-internet-e2e-"));
        await rm(absoluto, { recursive: true, force: true });
    }
});
