import { spawn } from "node:child_process";
import { readdir } from "node:fs/promises";
import { join } from "node:path";
import { fileURLToPath } from "node:url";

const frontend = fileURLToPath(new URL("..", import.meta.url));
const cli = join(frontend, "node_modules/@playwright/test/cli.js");
const argumentos = process.argv.slice(2);
const opcoes = [];
const filtros = [];
const recebeValor = new Set(["--grep", "-g", "--grep-invert", "--timeout", "--reporter", "--project", "--workers", "-j", "--retries", "--repeat-each", "--config", "-c"]);
for (let index = 0; index < argumentos.length; index += 1) {
    const argumento = argumentos[index];
    if (argumento.startsWith("-")) {
        opcoes.push(argumento);
        if (recebeValor.has(argumento) && argumentos[index + 1] !== undefined) opcoes.push(argumentos[++index]);
    } else filtros.push(new RegExp(argumento.replaceAll("\\", "/")));
}
const arquivos = (await readdir(join(frontend, "e2e"))).filter(nome => nome.endsWith(".spec.js")
    && (filtros.length === 0 || filtros.some(filtro => filtro.test(`e2e/${nome}`)))).sort();
if (arquivos.length === 0) {
    console.error("Nenhum arquivo E2E corresponde ao filtro informado.");
    process.exit(1);
}
for (const arquivo of arquivos) {
    console.log(`E2E isolado: ${arquivo}`);
    const codigo = await new Promise((resolve, reject) => {
        const filho = spawn(process.execPath, [cli, "test", `e2e/${arquivo}`, ...opcoes], {
            cwd: frontend, stdio: "inherit", windowsHide: true
        });
        filho.once("error", reject);
        filho.once("exit", status => resolve(status ?? 1));
    });
    // Playwright encerra o webServer/H2 antes de finalizar esta invocação.
    if (codigo !== 0) process.exit(codigo);
}
