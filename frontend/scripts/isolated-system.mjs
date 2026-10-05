import { spawn, spawnSync } from "node:child_process";
import { access, mkdtemp, readFile, realpath } from "node:fs/promises";
import { createServer } from "node:net";
import { tmpdir } from "node:os";
import { delimiter, join, resolve } from "node:path";
import { fileURLToPath } from "node:url";

const frontend = fileURLToPath(new URL("..", import.meta.url));
const sistema = resolve(frontend, "../sistema");
const filhos = new Set();
let encerrando = false;

function iniciar(comando, argumentos, options = {}) {
    const filho = spawn(comando, argumentos, { stdio: "inherit", windowsHide: true, ...options });
    filhos.add(filho);
    filho.once("exit", () => filhos.delete(filho));
    return filho;
}

function parar(codigo = 0) {
    if (encerrando) return;
    encerrando = true;
    for (const filho of filhos) {
        if (!filho.pid) continue;
        if (process.platform === "win32") {
            spawnSync("taskkill.exe", ["/PID", String(filho.pid), "/T", "/F"], { windowsHide: true, stdio: "ignore" });
        } else filho.kill("SIGTERM");
    }
    process.exit(codigo);
}

process.once("SIGINT", () => parar());
process.once("SIGTERM", () => parar());

function portaLivre(porta) {
    return new Promise((resolvePromise, reject) => {
        const server = createServer();
        server.once("error", () => reject(new Error(`Porta ${porta} ocupada. Encerre o teste anterior antes de iniciar.`)));
        server.listen(porta, () => server.close(resolvePromise));
    });
}

function terminou(filho) {
    return new Promise((resolvePromise, reject) => {
        filho.once("error", reject);
        filho.once("exit", codigo => codigo === 0 ? resolvePromise() : reject(new Error(`Preparação Maven falhou: ${codigo}`)));
    });
}

async function aguardarApi(filho) {
    for (let tentativa = 0; tentativa < 120; tentativa += 1) {
        if (filho.exitCode !== null) throw new Error("A aplicação H2 encerrou antes de iniciar.");
        try {
            const resposta = await fetch("http://127.0.0.1:18082/api/gerenciamentos", { signal: AbortSignal.timeout(1000) });
            if (resposta.status === 401) return;
        } catch { /* aplicação ainda iniciando */ }
        await new Promise(resolvePromise => setTimeout(resolvePromise, 500));
    }
    throw new Error("A aplicação H2 não iniciou dentro do prazo.");
}

try {
    if (!process.env.JAVA_HOME) throw new Error("Defina JAVA_HOME para o JDK antes de iniciar o ambiente isolado.");
    const java = await realpath(join(process.env.JAVA_HOME, "bin", process.platform === "win32" ? "java.exe" : "java"));
    await access(java);
    await portaLivre(18082);
    await portaLivre(4173);
    const pasta = await mkdtemp(join(tmpdir(), "creral-preview-"));
    const arquivoClasspath = join(pasta, "classpath.txt");
    const argumentosMaven = ["-B", "test-compile", "dependency:build-classpath", "-DincludeScope=test"];
    const maven = process.platform === "win32"
        ? iniciar(process.env.ComSpec || "cmd.exe", [
            "/d", "/s", "/c", `mvn.cmd ${argumentosMaven.join(" ")} "-Dmdep.outputFile=${arquivoClasspath}"`
        ], { cwd: sistema, windowsVerbatimArguments: true })
        : iniciar("mvn", [...argumentosMaven, `-Dmdep.outputFile=${arquivoClasspath}`], { cwd: sistema });
    await terminou(maven);
    const dependencias = (await readFile(arquivoClasspath, "utf8")).trim();
    const classpath = [join(sistema, "target/test-classes"), join(sistema, "target/classes"), dependencias].join(delimiter);
    const backend = iniciar(java, [
        "-cp", classpath, "com.patp.sistema.SistemaApplication",
        "--server.port=18082", "--server.address=127.0.0.1",
        "--spring.datasource.url=jdbc:h2:mem:creral-browser;MODE=MySQL;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=5000",
        "--spring.datasource.driver-class-name=org.h2.Driver",
        "--spring.datasource.username=sa", "--spring.datasource.password=",
        "--spring.jpa.hibernate.ddl-auto=create-drop", "--spring.jpa.open-in-view=false",
        "--spring.jpa.show-sql=false", "--debug=false", "--logging.level.root=WARN",
        "--logging.level.com.zaxxer.hikari=INFO", "--spring.main.banner-mode=off"
    ], { cwd: sistema });
    backend.once("error", error => { console.error(error.message); parar(1); });
    backend.once("exit", () => { if (!encerrando) parar(1); });
    await aguardarApi(backend);
    const vite = iniciar(process.execPath, [join(frontend, "node_modules/vite/bin/vite.js"), "--host", "localhost", "--port", "4173", "--strictPort"], {
        cwd: frontend,
        env: { ...process.env, VITE_API_URL: "/api", CRERAL_TEST_API_TARGET: "http://127.0.0.1:18082" }
    });
    vite.once("error", error => { console.error(error.message); parar(1); });
    vite.once("exit", () => { if (!encerrando) parar(1); });
    console.log("Ambiente isolado: http://localhost:4173 | H2 temporário em memória | Ctrl+C encerra os serviços.");
} catch (error) {
    console.error(error.message);
    parar(1);
}
