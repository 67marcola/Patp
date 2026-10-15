import { test } from "node:test";
import assert from "node:assert/strict";
import { createServer } from "node:net";
import { mkdtemp, access } from "node:fs/promises";
import { tmpdir } from "node:os";
import { join } from "node:path";
import { opcoes, banco, argumentosJava, ambienteJava, portaLivre, limparTemporario, paginaDoPiloto } from "./piloto-config.mjs";

const interfaces = { Rede: [{ address: "192.168.1.20", family: "IPv4", internal: false }] };
test("PIL-01/04: endereço local explícito e porta válida, sem escolher outra interface", () => {
    assert.deepEqual(opcoes(["--host", "192.168.1.20"], interfaces), { host: "192.168.1.20", porta: 8082 });
    for (const args of [[], ["--host", "8.8.8.8"], ["--host", "0.0.0.0"], ["--host", "192.168.1.20", "--porta", "0"], ["--host", "192.168.1.20", "--porta", "8082x"], ["--apagar"], ["--host", "192.168.1.20", "--host", "192.168.1.20"]]) assert.throws(() => opcoes(args, interfaces));
});
test("PIL-02/06: MySQL local somente, configuração existente ou ambiente explícito", () => {
    const propriedades = "spring.datasource.url=jdbc:mysql://localhost:3306/sistema\nspring.datasource.username=operador\nspring.datasource.password=segredo-ficticio";
    assert.deepEqual(banco(propriedades, {}), { url: "jdbc:mysql://localhost:3306/sistema", username: "operador", password: "segredo-ficticio" });
    assert.equal(banco(propriedades, { SPRING_DATASOURCE_URL: "jdbc:mysql://127.0.0.1:33823/piloto_test" }).url, "jdbc:mysql://127.0.0.1:33823/piloto_test");
    for (const url of ["jdbc:h2:mem:teste", "jdbc:mysql://externo:3306/base", "jdbc:mysql://localhost:3306/"]) assert.throws(() => banco(propriedades, { SPRING_DATASOURCE_URL: url }));
});
test("PIL-03/06: servidor só valida schema, não inicializa SQL e argumentos não contêm credenciais", () => {
    const args = argumentosJava({ host: "192.168.1.20", porta: 8082 }, "file:///C:/temporario/interface/");
    assert.ok(args.includes("--spring.jpa.hibernate.ddl-auto=validate"));
    assert.ok(args.includes("--spring.sql.init.mode=never"));
    assert.ok(args.includes("--server.address=192.168.1.20"));
    assert.ok(args.includes("--server.port=8082"));
    assert.ok(args.includes("--spring.web.resources.static-locations=file:///C:/temporario/interface/"));
    assert.ok(args.every(a => !/username|password|datasource.url/.test(a)));
    const env = ambienteJava({ url: "jdbc:mysql://localhost:3306/sistema" }, { SPRING_DATASOURCE_PASSWORD: "literal\\:ficticio" });
    assert.deepEqual(JSON.parse(env.SPRING_APPLICATION_JSON).spring.datasource, { url: "jdbc:mysql://localhost:3306/sistema" });
    assert.equal(env.SPRING_DATASOURCE_PASSWORD, "literal\\:ficticio");
});
test("PIL-04/05: porta ocupada é recusada sem encerrar o serviço existente", async () => {
    const servidor = createServer(); await new Promise(resolve => servidor.listen(0, "127.0.0.1", resolve));
    const porta = servidor.address().port;
    try { await assert.rejects(portaLivre("127.0.0.1", porta), /Porta ocupada/); assert.equal(servidor.listening, true); }
    finally { await new Promise(resolve => servidor.close(resolve)); }
    await portaLivre("127.0.0.1", porta);
});
test("PIL-05: limpeza restrita ao temporário criado, rejeita caminho externo", async () => {
    await assert.rejects(limparTemporario(process.cwd()), /Limpeza recusada/);
    await assert.rejects(limparTemporario(tmpdir()), /Limpeza recusada/);
    const pasta = await mkdtemp(join(tmpdir(), "creral-piloto-"));
    await limparTemporario(pasta); await assert.rejects(access(pasta), { code: "ENOENT" });
});
test("PIL-04: prontidão não aceita página de outro processo na mesma porta", () => {
    const html = '<div id="root"></div><meta name="creral-piloto" content="proprio">';
    assert.equal(paginaDoPiloto(html, "proprio"), true);
    assert.equal(paginaDoPiloto(html, "outro"), false);
    assert.equal(paginaDoPiloto('<div id="root"></div>', "proprio"), false);
});
