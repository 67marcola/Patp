import { networkInterfaces, tmpdir } from "node:os";
import { isIPv4, createServer } from "node:net";
import { realpath, rm } from "node:fs/promises";
import { relative, isAbsolute } from "node:path";

export function enderecos(interfaces = networkInterfaces()) {
    return Object.entries(interfaces).flatMap(([nome, itens]) => (itens || []).filter(i => i.family === "IPv4").map(i => ({ nome, ip: i.address, interno: i.internal })));
}
export function opcoes(args, interfaces = networkInterfaces()) {
    const valores = {};
    for (let i = 0; i < args.length; i += 2) {
        if (!["--host", "--porta"].includes(args[i]) || valores[args[i]] !== undefined || !args[i + 1]) throw new Error("Use --host IP_LOCAL e, opcionalmente, --porta 8082.");
        valores[args[i]] = args[i + 1];
    }
    const host = valores["--host"], porta = valores["--porta"] || "8082";
    if (!isIPv4(host || "") || !enderecos(interfaces).some(i => i.ip === host)) throw new Error("Informe um IPv4 deste computador com --host. Consulte --listar.");
    if (!/^\d+$/.test(porta) || Number(porta) < 1024 || Number(porta) > 65535) throw new Error("Porta deve estar entre 1024 e 65535.");
    return { host, porta: Number(porta) };
}
export function banco(texto, env = process.env) {
    const props = Object.fromEntries(texto.split(/\r?\n/).filter(l => /^spring\.datasource\.[\w.-]+=/.test(l)).map(l => { const p = l.indexOf("="); return [l.slice(0, p), l.slice(p + 1)]; }));
    const config = Object.fromEntries(["url", "username", "password"].map(k => [k, env[`SPRING_DATASOURCE_${k.toUpperCase()}`] ?? props[`spring.datasource.${k}`] ?? ""]));
    if (!/^jdbc:mysql:\/\/(?:localhost|127\.0\.0\.1)(?::\d+)?\/[a-zA-Z0-9_]+(?:\?[^\r\n]*)?$/.test(config.url) || !config.username) throw new Error("Configure um MySQL local e seu usuário no servidor; H2 não é permitido neste modo.");
    return config;
}
export function argumentosJava({ host, porta }, staticUrl) {
    return [`--server.address=${host}`, `--server.port=${porta}`, "--spring.jpa.hibernate.ddl-auto=validate", "--spring.sql.init.mode=never",
        "--spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver", `--spring.web.resources.static-locations=${staticUrl}`,
        "--spring.jpa.show-sql=false", "--spring.main.banner-mode=off", "--logging.level.root=WARN"];
}
export function ambienteJava(config, env = process.env) {
    // Spring continua resolvendo usuário/senha originais, inclusive escapes de .properties.
    return { ...env, SPRING_APPLICATION_JSON: JSON.stringify({ spring: { datasource: { url: config.url } } }) };
}
export async function portaLivre(host, porta) {
    await new Promise((resolve, reject) => {
        const s = createServer(); s.once("error", () => reject(new Error("Porta ocupada ou endereço indisponível; nenhum serviço foi encerrado.")));
        s.listen(porta, host, () => s.close(resolve));
    });
}
export function paginaDoPiloto(html, identificador) {
    return html.includes('id="root"') && html.includes(`<meta name="creral-piloto" content="${identificador}">`);
}
export async function limparTemporario(pasta) {
    const base = await realpath(tmpdir()), atual = await realpath(pasta), caminho = relative(base, atual);
    if (isAbsolute(caminho) || caminho.includes("..") || !/^creral-piloto-[^/\\]+$/.test(caminho)) throw new Error("Limpeza recusada fora do temporário próprio.");
    await rm(atual, { recursive: true, force: true, maxRetries: 3, retryDelay: 200 });
}
