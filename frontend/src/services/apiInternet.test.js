import { expect, test, vi } from "vitest";
import { cadastrarUsuario, login, listarCadastrosPendentes, aprovarCadastro, encerrarSessao } from "./api";

const pendente = { estado: "PENDENTE", mensagem: "Cadastro aguardando aprovação do administrador." };
const conta = { id: 4, nome: "Ana", setor: null, email: "ana@example.test" };
const sessao = { ...conta, token: "sessao", papel: "FUNCIONARIO" };
const json = (body, status = 200) => new Response(JSON.stringify(body), { status });
function responder(response) { const fetch = vi.fn().mockResolvedValue(response); vi.stubGlobal("fetch", fetch); return fetch; }

test("NET-08: cadastro202 exato sem token e POST preserva dados enviados", async () => {
    const fetch = responder(json(pendente,202));
    const dados = { nome: "Ana", setor: "TI", email: conta.email, senha: "ficticia" };
    expect(await cadastrarUsuario(dados)).toEqual(pendente);
    expect(fetch.mock.calls[0][0]).toMatch(/\/usuarios\/cadastro$/);
    expect(fetch.mock.calls[0][1]).toMatchObject({ method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify(dados) });
    expect(fetch).toHaveBeenCalledTimes(1);
});

test.each([login, dados => cadastrarUsuario(dados)])("NET-07: entrada200 local mantém contrato", async executar => {
    responder(json(sessao));
    expect(await executar("ana@example.test","ficticia")).toEqual(sessao);
});

test.each([
    [202, { ...pendente, token: "indevido" }], [202, { estado: "APROVADO", mensagem: pendente.mensagem }],
    [202, { estado: "PENDENTE", mensagem: "outra" }], [200, pendente], [201, sessao], [204, null]
])("NET-08/13: cadastro resposta incerta%s não afirma entrada", async (status, body) => {
    const fetch=responder(status===204?new Response(null,{status}):json(body,status));
    await expect(cadastrarUsuario({})).rejects.toMatchObject({ status, message: expect.stringContaining("Não foi possível confirmar") });
    expect(fetch).toHaveBeenCalledTimes(1);
});

test("NET-09: login não aceita pendência202 e conserva403 do servidor", async () => {
    responder(json(pendente,202));
    await expect(login(conta.email,"ficticia")).rejects.toMatchObject({status:202});
    responder(json({erro:pendente.mensagem},403));
    await expect(login(conta.email,"ficticia")).rejects.toMatchObject({status:403,message:pendente.mensagem});
});

test("NET-10: lista exata/segura, vazia, sessão e cancelamento", async () => {
    const fetch=responder(json([conta])); const signal=new AbortController().signal;
    expect(await listarCadastrosPendentes("sessao",{signal})).toEqual([conta]);
    expect(fetch.mock.calls[0][0]).toMatch(/\/administracao\/cadastros\/pendentes$/);
    expect(fetch.mock.calls[0][1]).toEqual({method:"GET",headers:{Authorization:"Bearer sessao"},signal});
    responder(json([])); expect(await listarCadastrosPendentes("sessao")).toEqual([]);
});

test.each([[{...conta,senha:"hash"}],[{...conta,setor:2}],[conta,conta],[{...conta,id:5},conta],{}].map(lista=>[lista]))
    ("NET-10/14: lista malformada é incerta", async lista => {
        const fetch=responder(json(lista));
        await expect(listarCadastrosPendentes("sessao")).rejects.toMatchObject({status:200});
        expect(fetch).toHaveBeenCalledTimes(1);
    });

test("NET-11: aprova somente ID/estado exatos, sem corpo de papel", async () => {
    const body={id:4,estado:"APROVADO"}; const fetch=responder(json(body));
    expect(await aprovarCadastro("sessao",4)).toEqual(body);
    expect(fetch.mock.calls[0][0]).toMatch(/\/administracao\/cadastros\/4\/aprovar$/);
    expect(fetch.mock.calls[0][1]).toEqual({method:"POST",headers:{Authorization:"Bearer sessao"},signal:undefined});
});

test.each([{id:5,estado:"APROVADO"},{id:4,estado:"PENDENTE"},{id:4,estado:"APROVADO",papel:"ADMINISTRADOR"},{}])
    ("NET-11/14: aprovação incerta não confirma nem repete", async body => {
        const fetch=responder(json(body)); await expect(aprovarCadastro("sessao",4)).rejects.toMatchObject({status:200});
        expect(fetch).toHaveBeenCalledTimes(1);
    });

test.each([401,403,404,500])("NET-10/11: erro HTTP%s conserva mensagem e status",async status => {
    const fetch=responder(json({erro:"Recusado"},status));
    await expect(aprovarCadastro("sessao",4)).rejects.toMatchObject({status,message:"Recusado"});
    expect(fetch).toHaveBeenCalledTimes(1);
});

test("NET-11/14: JSON inválido de sucesso não confirma aprovação",async () => {
    const fetch=responder(new Response("<html>falha</html>",{status:200}));
    await expect(aprovarCadastro("sessao",4)).rejects.toMatchObject({status:200,message:expect.stringContaining("Não foi possível confirmar")});
    expect(fetch).toHaveBeenCalledTimes(1);
});

test("NET-17: logout exige texto/status atuais e envia Bearer/POST",async () => {
    const fetch=responder(new Response("Logout realizado com sucesso.",{status:200}));
    expect(await encerrarSessao("sessao")).toBe("Logout realizado com sucesso.");
    expect(fetch.mock.calls[0][0]).toMatch(/\/usuarios\/logout$/);
    expect(fetch.mock.calls[0][1]).toEqual({method:"POST",headers:{Authorization:"Bearer sessao"},signal:undefined});
});

test.each([new Response("outro",{status:200}),new Response(null,{status:204})])
    ("NET-17: saída malformada é incerta sem retry",async resposta => {
        const fetch=responder(resposta); await expect(encerrarSessao("sessao")).rejects.toMatchObject({message:expect.stringContaining("Não foi possível confirmar")});
        expect(fetch).toHaveBeenCalledTimes(1);
    });

test.each([()=>aprovarCadastro("sessao",4),()=>cadastrarUsuario({}),()=>encerrarSessao("sessao")])
    ("NET-13/14/17: rede falha sem retry",async executar => {
        const fetch=vi.fn().mockRejectedValue(new TypeError("rede"));vi.stubGlobal("fetch",fetch);
        await expect(executar()).rejects.toMatchObject({status:0,message:expect.stringContaining("Não foi possível confirmar")});
        expect(fetch).toHaveBeenCalledTimes(1);
    });
