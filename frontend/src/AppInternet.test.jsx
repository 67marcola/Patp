import { act, fireEvent, render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { expect, test, vi } from "vitest";
import App from "./App";

vi.mock("./pages/Gerenciamentos",()=>({default:()=> <h2>Gerenciamentos</h2>}));
const publico={id:1,nome:"Admin",setor:"TI",email:"admin@example.test"};
const sessao={...publico,token:"sessao",papel:"ADMINISTRADOR"};
const me=(papel="ADMINISTRADOR")=>({...publico,papel});
const json=(body,status=200)=>new Response(JSON.stringify(body),{status});
function cache(extra={}) {localStorage.setItem("token","sessao");localStorage.setItem("usuario",JSON.stringify({...publico,...extra}));}

test("NET-10/14: login consulta/me, guarda quatro campos, abre/aprova/fecha com foco",async()=>{
    const conta={id:4,nome:"Ana",setor:"RH",email:"ana@example.test"};
    const fetch=vi.fn((url)=>{
        if(url.endsWith("/usuarios/login"))return json(sessao);
        if(url.endsWith("/usuarios/me"))return json(me());
        if(url.endsWith("/pendentes"))return json([conta]);
        if(url.endsWith("/4/aprovar"))return json({id:4,estado:"APROVADO"});
        throw new Error(`Rota inesperada ${url}`);
    });vi.stubGlobal("fetch",fetch);render(<App modoInternet/>);const user=userEvent.setup();
    await user.type(screen.getByLabelText("E-mail"),publico.email);await user.type(screen.getByLabelText("Senha"),"ficticia");
    await user.click(screen.getByRole("button",{name:"Entrar",exact:true}));
    const abrir=await screen.findByRole("button",{name:"Aprovar cadastros"});
    expect(JSON.parse(localStorage.getItem("usuario"))).toEqual(publico);expect(localStorage.getItem("token")).toBe("sessao");
    expect(fetch.mock.calls.filter(([url])=>url.endsWith("/usuarios/me"))).toHaveLength(1);
    await user.click(abrir);expect(await screen.findByText(conta.email)).toBeTruthy();
    await user.click(screen.getByRole("button",{name:"Aprovar Ana"}));
    expect((await screen.findByRole("status")).textContent).toBe("Cadastro de Ana aprovado.");
    await user.click(screen.getByRole("button",{name:"Fechar aprovações"}));
    expect(screen.queryByRole("heading",{name:"Cadastros aguardando aprovação"})).toBeNull();expect(document.activeElement).toBe(abrir);
    expect(fetch.mock.calls.every(([,request])=>request.headers.Authorization==="Bearer sessao"||request.method==="POST")).toBe(true);
});

test("NET-10: papel no cache não oferece controle antes da confirmação real",async()=>{
    cache({papel:"ADMINISTRADOR"});let resolver;
    const fetch=vi.fn().mockImplementation(()=>new Promise(r=>{resolver=r;}));vi.stubGlobal("fetch",fetch);render(<App modoInternet/>);
    expect(screen.queryByRole("button",{name:"Aprovar cadastros"})).toBeNull();
    await act(async()=>resolver(json(me("FUNCIONARIO"))));
    expect(screen.queryByRole("button",{name:"Aprovar cadastros"})).toBeNull();expect(fetch).toHaveBeenCalledTimes(1);
});

test.each(["http","id"])("NET-10: conferência/me falha%s sem expor aprovação",async tipo=>{
    cache();const fetch=vi.fn().mockResolvedValue(tipo==="http"?json({erro:"Não autenticado."},401):json({...me(),id:99}));
    vi.stubGlobal("fetch",fetch);render(<App modoInternet/>);
    expect((await screen.findByRole("alert")).textContent).toBe("Não foi possível confirmar seu acesso. Entre novamente para consultar aprovações.");
    expect(screen.queryByRole("button",{name:"Aprovar cadastros"})).toBeNull();expect(fetch).toHaveBeenCalledTimes(1);
});

test("NET-07: modo local não consulta/me nem revoga pelo novo fluxo",async()=>{
    cache();const fetch=vi.fn();vi.stubGlobal("fetch",fetch);render(<App modoInternet={false}/>);const user=userEvent.setup();
    expect(screen.getByRole("heading",{name:"Gerenciamentos"})).toBeTruthy();expect(screen.queryByRole("button",{name:"Aprovar cadastros"})).toBeNull();
    await user.click(screen.getByRole("button",{name:"Sair"}));
    expect(screen.getByRole("heading",{name:"Bem-vindo"})).toBeTruthy();expect(fetch).not.toHaveBeenCalled();
    expect(localStorage.getItem("token")).toBeNull();expect(localStorage.getItem("usuario")).toBeNull();
});

test.each(["rede","malformado"])("NET-17: saída incerta%s remove cache e informa falha sem retry",async tipo=>{
    cache();const fetch=vi.fn().mockResolvedValueOnce(json(me("FUNCIONARIO")));
    if(tipo==="rede")fetch.mockRejectedValueOnce(new TypeError("rede"));else fetch.mockResolvedValueOnce(new Response("outro",{status:200}));
    vi.stubGlobal("fetch",fetch);render(<App modoInternet/>);const user=userEvent.setup();
    await waitFor(()=>expect(fetch).toHaveBeenCalledTimes(1));await user.click(screen.getByRole("button",{name:"Sair"}));
    expect((await screen.findByRole("alert")).textContent).toBe("Você saiu deste navegador, mas a revogação da sessão no servidor não foi confirmada. A sessão online expira em até oito horas.");
    expect(screen.getByRole("heading",{name:"Bem-vindo"})).toBeTruthy();expect(localStorage.getItem("token")).toBeNull();expect(localStorage.getItem("usuario")).toBeNull();
    expect(fetch.mock.calls.filter(([url])=>url.endsWith("/usuarios/logout"))).toHaveLength(1);
    expect(fetch.mock.calls[1][1]).toEqual({method:"POST",headers:{Authorization:"Bearer sessao"},signal:undefined});
});

test("NET-17: saída confirmada bloqueia duplicação e limpa sessão",async()=>{
    cache();let resolver;const fetch=vi.fn().mockResolvedValueOnce(json(me("FUNCIONARIO"))).mockImplementationOnce(()=>new Promise(r=>{resolver=r;}));
    vi.stubGlobal("fetch",fetch);render(<App modoInternet/>);await waitFor(()=>expect(fetch).toHaveBeenCalledTimes(1));
    const sair=screen.getByRole("button",{name:"Sair"});fireEvent.click(sair);fireEvent.click(sair);
    expect(fetch.mock.calls.filter(([url])=>url.endsWith("/usuarios/logout"))).toHaveLength(1);expect(screen.getByRole("button",{name:"Saindo..."}).disabled).toBe(true);
    await act(async()=>resolver(new Response("Logout realizado com sucesso.",{status:200})));
    expect(screen.getByRole("heading",{name:"Bem-vindo"})).toBeTruthy();expect(screen.queryByRole("alert")).toBeNull();
    expect(localStorage.getItem("token")).toBeNull();expect(localStorage.getItem("usuario")).toBeNull();
});
