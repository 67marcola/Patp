import { test } from "node:test";
import assert from "node:assert/strict";
import { spawn, spawnSync } from "node:child_process";
import { access, cp, mkdtemp, readFile } from "node:fs/promises";
import { tmpdir } from "node:os";
import { join, resolve, delimiter } from "node:path";
import { fileURLToPath } from "node:url";
import { createServer } from "node:net";
import { randomUUID } from "node:crypto";
import { chromium, expect } from "@playwright/test";
import { limparTemporario } from "./piloto-config.mjs";

const frontend = fileURLToPath(new URL("..", import.meta.url)), sistema = resolve(frontend, "../sistema");
const mysqlExe = process.env.CRERAL_TEST_MYSQL_EXE || "C:/Program Files/MySQL/MySQL Server 8.0/bin/mysql.exe";
function sql(comando) {
    const r = spawnSync(mysqlExe, ["--protocol=TCP", "--host=127.0.0.1", "--port=33823", "--user=root", "--batch", "--raw", "--skip-column-names", `--execute=${comando}`], { windowsHide: true, encoding: "utf8", env: { ...process.env, MYSQL_PWD: "" } });
    assert.equal(r.status, 0, "MySQL fictício indisponível; iniciar somente a fixture TEMP na porta33823."); return r.stdout.trim();
}
function identidade() {
    const [porta, datadir] = sql("SELECT @@port,@@datadir").split("\t");
    assert.equal(porta, "33823");
    assert.equal(datadir.replaceAll("\\", "/").toLowerCase(), "c:/users/marco/appdata/local/temp/creral-arquivo-049cfd5cc86d4d8b840e41f1710d7a43/mysql/");
}
function executar(cmd, args, options = {}) {
    const child = spawn(cmd, args, { windowsHide: true, stdio: ["pipe", "pipe", "pipe"], ...options });
    child.texto = ""; child.stdout.setEncoding("utf8"); child.stderr.setEncoding("utf8");
    child.stdout.on("data", d => { child.texto += d; }); child.stderr.on("data", d => { child.texto += d; });
    child.resultado = new Promise((resolvePromise, reject) => { child.once("error", reject); child.once("exit", resolvePromise); });
    return child;
}
async function esperar(condicao, child, ms = 120000) {
    const inicio = Date.now();
    while (!await condicao()) {
        assert.equal(child.exitCode, null, `Processo encerrou antes de pronto: ${child.texto}`);
        assert.ok(Date.now() - inicio < ms, "Tempo de preparação esgotado");
        await new Promise(r => setTimeout(r, 150));
    }
}
async function matar(child) {
    if (child.exitCode !== null || child.signalCode !== null) return;
    if (process.platform === "win32") spawnSync("taskkill.exe", ["/PID", String(child.pid), "/T", "/F"], { windowsHide: true, stdio: "ignore" });
    else child.kill("SIGKILL");
    await child.resultado;
}
async function porta() { const s = createServer(); await new Promise(r => s.listen(0, "127.0.0.1", r)); const p = s.address().port; await new Promise(r => s.close(r)); return p; }

test("PIL-01–07: ciclo MySQL fictício persistente, duas sessões, falhas e encerramento próprio", { timeout: 300000 }, async () => {
    identidade();
    const uid = randomUUID().replaceAll("-", "").slice(0, 12), schema = `creral_piloto_${uid}`, vazio = `creral_piloto_vazio_${uid}`, usuario = `piloto_${uid}`, senha = `Ficticio_${uid}!`;
    const urlBanco = `jdbc:mysql://127.0.0.1:33823/${schema}?useSSL=false&allowPublicKeyRetrieval=true`;
    const env = { ...process.env, SPRING_DATASOURCE_URL: urlBanco, SPRING_DATASOURCE_USERNAME: usuario, SPRING_DATASOURCE_PASSWORD: senha };
    const raiz = await mkdtemp(join(tmpdir(), "creral-piloto-fixture-")), filhos = [];
    let browser;
    sql(`CREATE DATABASE ${schema}; CREATE DATABASE ${vazio}; CREATE USER '${usuario}'@'127.0.0.1' IDENTIFIED BY '${senha}'; GRANT ALL ON ${schema}.* TO '${usuario}'@'127.0.0.1'; GRANT ALL ON ${vazio}.* TO '${usuario}'@'127.0.0.1';`);
    const iniciador = (p, ambiente = env) => { const c = executar(process.execPath, [join(frontend, "scripts/piloto-lan.mjs"), "--host", "127.0.0.1", "--porta", String(p)], { cwd: frontend, env: ambiente }); filhos.push(c); return c; };
    async function pronto(c) { await esperar(() => c.texto.includes("Piloto pronto:"), c); assert.ok(!c.texto.includes(senha)); }
    async function encerrar(c) {
        const temporario = /Temporário do piloto: ([^\r\n]+)/.exec(c.texto)?.[1];
        assert.ok(temporario); c.stdin.write("encerrar\n");
        let limite;
        try {
            assert.equal(await Promise.race([c.resultado, new Promise((_, reject) => { limite = setTimeout(() => reject(new Error("Encerramento do piloto não terminou em10s")), 10000); })]), 0);
        } finally { clearTimeout(limite); }
        await assert.rejects(access(temporario), { code: "ENOENT" });
    }
    try {
        // Somente esta fixture descartável recebe criação de estrutura. O modo piloto usa validate.
        await cp(join(sistema, "src/main"), join(raiz, "sistema/src/main"), { recursive: true }); await cp(join(sistema, "pom.xml"), join(raiz, "sistema/pom.xml"));
        const cpFile = join(raiz, "classpath.txt"), args = ["-B", "-q", "compile", "dependency:build-classpath", "-DincludeScope=runtime"];
        const build = process.platform === "win32"
            ? executar(process.env.ComSpec || "cmd.exe", ["/d", "/s", "/c", `mvn.cmd ${args.join(" ")} "-Dmdep.outputFile=${cpFile}"`], { cwd: join(raiz, "sistema"), windowsVerbatimArguments: true })
            : executar("mvn", [...args, `-Dmdep.outputFile=${cpFile}`], { cwd: join(raiz, "sistema") });
        filhos.push(build); assert.equal(await build.resultado, 0, "Compilação da fixture falhou");
        const java = join(process.env.JAVA_HOME, "bin", process.platform === "win32" ? "java.exe" : "java"), initPort = await porta();
        const classpath = [join(raiz, "sistema/target/classes"), (await readFile(cpFile, "utf8")).trim()].join(delimiter);
        const init = executar(java, ["-cp", classpath, "com.patp.sistema.SistemaApplication", "--server.address=127.0.0.1", `--server.port=${initPort}`, "--spring.jpa.hibernate.ddl-auto=create", "--spring.sql.init.mode=never", "--spring.jpa.show-sql=false", "--logging.level.root=WARN", "--spring.main.banner-mode=off"], { cwd: join(raiz, "sistema"), env: { ...env, SPRING_APPLICATION_JSON: JSON.stringify({ spring: { datasource: { url: urlBanco, username: usuario, password: senha } } }) } });
        filhos.push(init); await esperar(async () => { try { return (await fetch(`http://127.0.0.1:${initPort}/api/gerenciamentos`)).status === 401; } catch { return false; } }, init); await matar(init);
        const p = await porta(), url = `http://127.0.0.1:${p}`, primeiro = iniciador(p); await pronto(primeiro);
        const ocupado = iniciador(p); assert.equal(await ocupado.resultado, 1); assert.ok(!ocupado.texto.includes("Piloto pronto:"));
        const html = await (await fetch(url)).text(); assert.ok(html.includes("Creral | Gestão de demandas"));
        assert.equal((await fetch(`${url}/api/gerenciamentos`)).status, 401);
        const asset = /src="([^"]+\.js)"/.exec(html)[1], js = await (await fetch(url + asset)).text();
        assert.ok(!js.includes(senha)); assert.ok(!js.includes(usuario)); assert.ok(!js.includes("localhost:8081/api"));
        browser = await chromium.launch({ channel: "msedge", headless: true });
        const paginas = [await (await browser.newContext()).newPage(), await (await browser.newContext()).newPage()];
        const credenciais = [{ nome: "Dono Piloto", email: `dono-${uid}@example.test` }, { nome: "Colega Piloto", email: `colega-${uid}@example.test` }];
        const origens = new Set();
        for (let i = 0; i < 2; i++) {
            const page = paginas[i]; page.on("request", r => { if (new URL(r.url()).pathname.startsWith("/api/")) origens.add(new URL(r.url()).origin); });
            const r = await page.request.post(`${url}/api/usuarios/cadastro`, { data: { ...credenciais[i], setor: "Teste", senha: "teste123" } }); assert.equal(r.status(), 200);
            await page.goto(url); await page.getByLabel("E-mail", { exact: true }).fill(credenciais[i].email); await page.getByLabel("Senha", { exact: true }).fill("teste123"); await page.getByRole("button", { name: "Entrar", exact: true }).click(); await expect(page.getByRole("heading", { name: "Gerenciamentos", exact: true })).toBeVisible();
        }
        const [dono, colega] = paginas, nome = `Quadro compartilhado ${uid}`;
        await dono.getByRole("button", { name: "+ Criar gerenciamento" }).click(); await dono.getByLabel("Nome do gerenciamento").fill(nome); await dono.getByRole("button", { name: "+ Adicionar etapa" }).click(); await dono.getByLabel("Nome da etapa 1", { exact: true }).fill("Trabalho"); await dono.getByLabel("Setor responsável da etapa 1", { exact: true }).fill("Campo");
        await dono.getByRole("button", { name: "Salvar gerenciamento", exact: true }).click(); await expect(dono.getByRole("button", { name: `Abrir ${nome}`, exact: true })).toBeVisible();
        await colega.reload(); await colega.getByRole("button", { name: `Abrir ${nome}`, exact: true }).click(); await expect(colega.getByRole("button", { name: "Atividade", exact: true })).toBeEnabled(); await expect(colega.getByRole("button", { name: "+ Nova etapa" })).toHaveCount(0);
        await dono.getByRole("button", { name: `Abrir ${nome}`, exact: true }).click(); await dono.getByRole("button", { name: "+ Criar processo" }).click(); await dono.getByLabel("Número da demanda").fill(`PIL-${uid}`); await dono.getByLabel("Cliente/solicitante").fill("Cliente Piloto"); await dono.getByRole("button", { name: "Criar demanda", exact: true }).click(); await expect(dono.getByRole("article", { name: `PIL-${uid} — Cliente Piloto`, exact: true })).toBeVisible();
        await colega.getByRole("button", { name: "← Voltar" }).click(); await colega.getByRole("button", { name: `Abrir ${nome}`, exact: true }).click(); await expect(colega.getByRole("article", { name: `PIL-${uid} — Cliente Piloto`, exact: true })).toBeVisible();
        assert.deepEqual([...origens], [url]);
        const token = await dono.evaluate(() => localStorage.getItem("token")); const lista = await (await fetch(`${url}/api/gerenciamentos?arquivado=false`, { headers: { Authorization: `Bearer ${token}` } })).json(); const quadro = lista.find(q => q.nome === nome);
        const antes = await (await fetch(`${url}/api/gerenciamentos/${quadro.id}/estrutura-etapas`, { headers: { Authorization: `Bearer ${token}` } })).json();
        const pastaImagem = join(raiz, "duas-sessoes.png"); await colega.screenshot({ path: pastaImagem, fullPage: true });
        await encerrar(primeiro); identidade(); assert.equal(sql(`SELECT COUNT(*) FROM ${schema}.processos`), "1");
        const segundo = iniciador(p); await pronto(segundo);
        const login = await fetch(`${url}/api/usuarios/login`, { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ email: credenciais[0].email, senha: "teste123" }) }); assert.equal(login.status, 200); const conta = await login.json();
        const depois = await (await fetch(`${url}/api/gerenciamentos/${quadro.id}/estrutura-etapas`, { headers: { Authorization: `Bearer ${conta.token}` } })).json(); assert.deepEqual(depois, antes); await encerrar(segundo);
        const falha = iniciador(p, { ...env, SPRING_DATASOURCE_URL: `jdbc:mysql://127.0.0.1:33823/${vazio}?useSSL=false&allowPublicKeyRetrieval=true` });
        await esperar(() => falha.exitCode !== null || falha.texto.includes("Piloto pronto:"), falha);
        assert.ok(!falha.texto.includes("Piloto pronto:")); assert.equal(await falha.resultado, 1); assert.match(falha.texto, /estrutura|migração/); assert.equal(sql(`SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='${vazio}'`), "0"); assert.ok(!falha.texto.includes(senha));
        const pastaFalha = /Temporário do piloto: ([^\r\n]+)/.exec(falha.texto)[1]; await assert.rejects(access(pastaFalha), { code: "ENOENT" });
        const semJava = iniciador(p, { ...env, JAVA_HOME: "" }); assert.equal(await semJava.resultado, 1); assert.match(semJava.texto, /JAVA_HOME/); assert.ok(!semJava.texto.includes("Piloto pronto:"));
        // Captura preservada fora do temporário a ser removido.
        const captura = join(tmpdir(), `piloto-duas-sessoes-${uid}.png`); await cp(pastaImagem, captura); console.log(`Captura piloto: ${captura}`);
    } finally {
        if (browser) await browser.close();
        for (const child of filhos) {
            if (child.exitCode === null && child.signalCode === null) {
                if (child.texto.includes("Piloto pronto:")) {
                    child.stdin.write("encerrar\n");
                    await Promise.race([child.resultado, new Promise(r => setTimeout(r, 5000))]);
                }
                await matar(child);
            }
            const temporario = /Temporário do piloto: ([^\r\n]+)/.exec(child.texto)?.[1];
            if (temporario) await access(temporario).then(() => limparTemporario(temporario), e => e.code === "ENOENT" ? undefined : Promise.reject(e));
        }
        identidade(); sql(`DROP DATABASE ${schema}; DROP DATABASE ${vazio}; DROP USER '${usuario}'@'127.0.0.1';`);
        // Caminho gerado localmente, validado pelo mesmo confinamento de temporários do piloto.
        await limparTemporario(raiz);
    }
});
