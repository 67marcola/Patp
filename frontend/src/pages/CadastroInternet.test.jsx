import { act, fireEvent, render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { expect, test, vi } from "vitest";
import Cadastro from "./Cadastro";

const dados={nome:"Ana",setor:"TI",email:"ana@example.test",senha:"ficticia"};
const pendente={estado:"PENDENTE",mensagem:"Cadastro aguardando aprovação do administrador."};
const json=(body,status=202)=>new Response(JSON.stringify(body),{status});
async function preparar(fetch) {
    vi.stubGlobal("fetch",fetch);
    const onLogin=vi.fn(),voltar=vi.fn(),user=userEvent.setup();
    render(<Cadastro onLogin={onLogin} voltar={voltar}/>);
    for(const [label,valor] of [["Nome completo",dados.nome],["Setor",dados.setor],["E-mail",dados.email],["Senha",dados.senha],["Confirmar senha",dados.senha]])
        await user.type(screen.getByLabelText(label,{exact:true}),valor);
    return {onLogin,voltar,user};
}

test("NET-13:202 mostra espera, não entra nem altera cache, e permite voltar pelo teclado",async()=>{
    const fetch=vi.fn().mockResolvedValue(json(pendente));
    const {onLogin,voltar,user}=await preparar(fetch);
    localStorage.setItem("token","anterior");localStorage.setItem("usuario","anterior");
    await user.click(screen.getByRole("button",{name:"Criar cadastro",exact:true}));
    expect((await screen.findByRole("status")).textContent).toBe(pendente.mensagem);
    expect(screen.getByRole("heading",{name:"Cadastro enviado"}).textContent).toBe("Cadastro enviado");
    expect(onLogin).not.toHaveBeenCalled();
    expect(localStorage.getItem("token")).toBe("anterior");expect(localStorage.getItem("usuario")).toBe("anterior");
    expect(fetch).toHaveBeenCalledTimes(1);expect(JSON.parse(fetch.mock.calls[0][1].body)).toEqual(dados);
    screen.getByRole("button",{name:"Voltar para login"}).focus();await user.keyboard("{Enter}");
    expect(voltar).toHaveBeenCalledTimes(1);
});

test("NET-13: envio em curso bloqueia duplicação e todos os campos",async()=>{
    let resolver;const fetch=vi.fn().mockImplementation(()=>new Promise(r=>{resolver=r;}));
    const {onLogin}=await preparar(fetch);
    const formulario=screen.getByRole("button",{name:"Criar cadastro",exact:true}).closest("form");
    fireEvent.submit(formulario);fireEvent.submit(formulario);
    expect(fetch).toHaveBeenCalledTimes(1);
    expect(screen.getByRole("button",{name:"Cadastrando..."}).disabled).toBe(true);
    for(const label of ["Nome completo","Setor","E-mail","Senha","Confirmar senha"]) expect(screen.getByLabelText(label,{exact:true}).disabled).toBe(true);
    await act(async()=>resolver(json(pendente)));
    expect((await screen.findByRole("status")).textContent).toBe(pendente.mensagem);expect(onLogin).not.toHaveBeenCalled();
});

test.each(["rede","http","malformado"])("NET-13: falha%s conserva campos sem retry ou sessão",async tipo=>{
    const fetch=tipo==="rede"?vi.fn().mockRejectedValue(new TypeError("rede")):
        vi.fn().mockResolvedValue(tipo==="http"?json({erro:"Falha ao cadastrar."},500):json({...pendente,token:"indevido"}));
    const {onLogin,user}=await preparar(fetch);
    await user.click(screen.getByRole("button",{name:"Criar cadastro",exact:true}));
    const mensagem=tipo==="http"?"Falha ao cadastrar.":"Não foi possível confirmar o cadastro. Se a conta já foi criada, entre pelo login.";
    expect((await screen.findByRole("alert")).textContent).toBe(mensagem);
    for(const [label,valor] of [["Nome completo",dados.nome],["Setor",dados.setor],["E-mail",dados.email],["Senha",dados.senha],["Confirmar senha",dados.senha]])
        expect(screen.getByLabelText(label,{exact:true}).value).toBe(valor);
    expect(screen.queryByRole("status")).toBeNull();expect(onLogin).not.toHaveBeenCalled();
    expect(localStorage.getItem("token")).toBeNull();expect(localStorage.getItem("usuario")).toBeNull();
    expect(fetch).toHaveBeenCalledTimes(1);
    await waitFor(()=>expect(screen.getByRole("button",{name:"Criar cadastro",exact:true}).disabled).toBe(false));
});

test("NET-07:200 local mantém entrada atual",async()=>{
    const sessao={...dados,id:4,token:"sessao",papel:"FUNCIONARIO"};delete sessao.senha;
    const {onLogin,user}=await preparar(vi.fn().mockResolvedValue(json(sessao,200)));
    await user.click(screen.getByRole("button",{name:"Criar cadastro",exact:true}));
    await waitFor(()=>expect(onLogin).toHaveBeenCalledWith(sessao));
    expect(screen.queryByText("Cadastro enviado")).toBeNull();expect(localStorage.getItem("token")).toBeNull();
});
