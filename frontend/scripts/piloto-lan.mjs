import { spawn, spawnSync } from "node:child_process";
import { access, cp, mkdtemp, readFile, realpath, writeFile } from "node:fs/promises";
import { randomUUID } from "node:crypto";
import { tmpdir } from "node:os";
import { delimiter, join, resolve, sep } from "node:path";
import { fileURLToPath, pathToFileURL } from "node:url";
import { enderecos, opcoes, banco, argumentosJava, ambienteJava, portaLivre, limparTemporario, paginaDoPiloto } from "./piloto-config.mjs";

const frontend = fileURLToPath(new URL("..", import.meta.url)), sistema = resolve(frontend, "../sistema");
const filhos = new Set();
let pasta, encerrando, etapa = "configuração";
let preparandoArquivos = Promise.resolve();
const identificador = randomUUID();
function entrada(dados) { if (dados.toString().trim().toLowerCase() === "encerrar") void parar(0); }
function iniciar(comando, args, options = {}) {
    const filho = spawn(comando, args, { windowsHide: true, stdio: ["ignore", "pipe", "pipe"], ...options });
    // Saída das ferramentas pode conter informações do banco. Exibir apenas mensagens próprias.
    filho.stdout?.resume(); filho.stderr?.resume();
    filhos.add(filho); filho.once("exit", () => filhos.delete(filho));
    return filho;
}
function terminou(filho) {
    return new Promise((resolvePromise, reject) => {
        filho.once("error", () => reject(new Error(`Não foi possível iniciar a etapa ${etapa}. Confira as dependências.`)));
        filho.once("exit", codigo => codigo === 0 ? resolvePromise() : reject(new Error(`Falha na etapa ${etapa}. Nenhuma URL foi liberada.`)));
    });
}
async function parar(codigo) {
    if (encerrando) return encerrando;
    encerrando = (async () => {
        process.stdin.off("data", entrada); process.stdin.pause();
        let processosEncerrados = true;
        for (const filho of filhos) {
            if (!filho.pid || filho.exitCode !== null || filho.signalCode !== null) continue;
            const saiu = new Promise(resolvePromise => filho.once("exit", resolvePromise));
            if (process.platform === "win32") spawnSync("taskkill.exe", ["/PID", String(filho.pid), "/T", "/F"], { windowsHide: true, stdio: "ignore" });
            else filho.kill("SIGTERM");
            await Promise.race([saiu, new Promise(resolvePromise => setTimeout(resolvePromise, 3000))]);
            if (filho.exitCode === null && filho.signalCode === null) {
                processosEncerrados = false; codigo = 1;
                console.error("Um processo próprio não confirmou encerramento; temporário preservado. Confira o processo do piloto antes de iniciar novamente.");
            }
        }
        await preparandoArquivos.catch(() => {});
        if (pasta && processosEncerrados) {
            try { await limparTemporario(pasta); }
            catch { console.error("Não foi possível limpar o temporário do piloto. Os dados MySQL não foram apagados."); codigo = 1; }
        }
        process.exitCode = codigo;
        console.log("Piloto encerrado. MySQL e serviços preexistentes preservados.");
    })();
    return encerrando;
}
process.once("SIGINT", () => { void parar(0); });
process.once("SIGTERM", () => { void parar(0); });

async function main() {
    if (process.argv.slice(2).length === 1 && process.argv[2] === "--listar") {
        for (const item of enderecos()) console.log(`${item.nome}: ${item.ip}${item.interno ? " (somente este computador)" : ""}`);
        return;
    }
    const config = opcoes(process.argv.slice(2));
    const datasource = banco(await readFile(join(sistema, "src/main/resources/application.properties"), "utf8"));
    if (!process.env.JAVA_HOME) throw new Error("Defina JAVA_HOME para o JDK e disponibilize Maven no PATH.");
    const java = await realpath(join(process.env.JAVA_HOME, "bin", process.platform === "win32" ? "java.exe" : "java"));
    await access(java); await access(join(frontend, "node_modules/vite/bin/vite.js"));
    await portaLivre(config.host, config.porta);
    if (encerrando) return;
    preparandoArquivos = (async () => {
        pasta = await mkdtemp(join(tmpdir(), "creral-piloto-"));
    // No Windows, somente o caminho gerado é interpolado no comando Maven.
    if (process.platform === "win32" && /[%!&^|<>\r\n"]/.test(pasta)) throw new Error("Use um diretório TEMP sem caracteres especiais para preparar o piloto.");
        await cp(join(sistema, "src/main"), join(pasta, "sistema/src/main"), { recursive: true });
        await cp(join(sistema, "pom.xml"), join(pasta, "sistema/pom.xml"));
    })();
    await preparandoArquivos;
    if (encerrando) return;
    const backend = join(pasta, "sistema"), interfaceWeb = join(pasta, "interface"), classpathFile = join(pasta, "classpath.txt");
    etapa = "compilação Java"; console.log("Preparando servidor em temporário próprio...");
    const argsMaven = ["-B", "-q", "compile", "dependency:build-classpath", "-DincludeScope=runtime"];
    const maven = process.platform === "win32"
        ? iniciar(process.env.ComSpec || "cmd.exe", ["/d", "/s", "/c", `mvn.cmd ${argsMaven.join(" ")} "-Dmdep.outputFile=${classpathFile}"`], { cwd: backend, windowsVerbatimArguments: true })
        : iniciar("mvn", [...argsMaven, `-Dmdep.outputFile=${classpathFile}`], { cwd: backend });
    await terminou(maven);
    if (encerrando) return;
    etapa = "compilação da interface";
    await terminou(iniciar(process.execPath, [join(frontend, "node_modules/vite/bin/vite.js"), "build", "--outDir", interfaceWeb], { cwd: frontend, env: { ...process.env, VITE_API_URL: "/api" } }));
    if (encerrando) return;
    preparandoArquivos = (async () => {
        const pagina = join(interfaceWeb, "index.html");
        await writeFile(pagina, (await readFile(pagina, "utf8")).replace("</head>", `<meta name="creral-piloto" content="${identificador}"></head>`));
    })();
    await preparandoArquivos;
    if (encerrando) return;
    const classpath = [join(backend, "target/classes"), (await readFile(classpathFile, "utf8")).trim()].join(delimiter);
    if (encerrando) return;
    etapa = "validação do banco e início"; console.log("Validando estrutura MySQL, sem migração automática...");
    const servidor = iniciar(java, ["-cp", classpath, "com.patp.sistema.SistemaApplication", ...argumentosJava(config, pathToFileURL(interfaceWeb + sep).href)], {
        cwd: backend, env: ambienteJava(datasource)
    });
    let erroSpawn = false;
    servidor.once("error", () => { erroSpawn = true; });
    const url = `http://${config.host}:${config.porta}`;
    let pronto = false;
    for (let i = 0; i < 160 && !encerrando; i++) {
        if (erroSpawn || servidor.exitCode !== null) throw new Error("Servidor não iniciou. Confira MySQL e compatibilidade da estrutura; este modo não executa migração. Nenhuma URL foi liberada.");
        try {
            const resposta = await fetch(`${url}/api/gerenciamentos`, { signal: AbortSignal.timeout(750) });
            if (resposta.status === 401) {
                const pagina = await fetch(url, { signal: AbortSignal.timeout(750) });
                if (pagina.ok && pagina.headers.get("content-type")?.includes("text/html") && paginaDoPiloto(await pagina.text(), identificador)) { pronto = true; break; }
            }
        } catch { /* Aguarda somente o endereço escolhido, sem redirecionar para outro serviço. */ }
        await new Promise(resolvePromise => setTimeout(resolvePromise, 500));
    }
    if (encerrando) return;
    if (!pronto) throw new Error("Tempo de início esgotado; confira banco, estrutura e porta. Nenhuma URL foi liberada.");
    console.log(`Piloto pronto: ${url}`);
    console.log("Colegas na mesma rede: abram esse endereço no navegador. Mantenha este computador ligado. Ctrl+C ou encerrar fecha sem apagar os dados MySQL.");
    process.stdin.on("data", entrada);
    const codigo = await new Promise(resolvePromise => servidor.once("exit", resolvePromise));
    if (!encerrando) throw new Error(`O processo do piloto encerrou (código ${codigo}). Inicie novamente após conferir banco e estrutura.`);
}
try { await main(); }
catch (error) { if (!encerrando) { console.error(error.code ? "Falha de preparação. Confira arquivos, JDK, Maven e dependências instaladas." : error.message); await parar(1); } }
