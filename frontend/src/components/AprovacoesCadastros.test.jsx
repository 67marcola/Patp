import { act, fireEvent, render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { expect, test, vi } from "vitest";
import { StrictMode } from "react";
import AprovacoesCadastros from "./AprovacoesCadastros";

const ana={id:4,nome:"Ana",setor:null,email:"ana@example.test"};
const bia={id:5,nome:"Bia",setor:"TI",email:"bia@example.test"};
const json=(body,status=200)=>new Response(JSON.stringify(body),{status});
function preparar(fetch) {
    vi.stubGlobal("fetch",fetch); const aoFechar=vi.fn(),aoOcupar=vi.fn();
    render(<AprovacoesCadastros token="sessao" aoFechar={aoFechar} aoOcupar={aoOcupar}/>);
    return {user:userEvent.setup(),aoFechar,aoOcupar};
}

test("NET-14: carrega dados seguros, aprova individualmente, preserva outra conta e foco",async()=>{
    let resolver;const fetch=vi.fn().mockImplementationOnce(()=>new Promise(r=>{resolver=r;}))
        .mockResolvedValueOnce(json({id:4,estado:"APROVADO"}));
    const {user,aoFechar,aoOcupar}=preparar(fetch);
    expect(screen.getByRole("status").textContent).toBe("Carregando cadastros...");
    const titulo=screen.getByRole("heading",{name:"Cadastros aguardando aprovação"});expect(document.activeElement).toBe(titulo);
    await act(async()=>resolver(json([ana,bia])));
    expect(await screen.findByText(ana.email)).toBeTruthy();expect(screen.getByText("Setor não informado")).toBeTruthy();
    expect(screen.getByText("TI")).toBeTruthy();expect(screen.getByText(bia.email)).toBeTruthy();
    await user.click(screen.getByRole("button",{name:"Aprovar Ana"}));
    expect((await screen.findByRole("status")).textContent).toBe("Cadastro de Ana aprovado.");
    expect(screen.queryByText(ana.email)).toBeNull();expect(screen.getByText(bia.email)).toBeTruthy();
    expect(document.activeElement).toBe(titulo);expect(aoOcupar.mock.calls).toEqual([[true],[false]]);
    expect(fetch.mock.calls[1][1]).toEqual({method:"POST",headers:{Authorization:"Bearer sessao"},signal:undefined});
    await user.click(screen.getByRole("button",{name:"Fechar aprovações"}));expect(aoFechar).toHaveBeenCalledTimes(1);
});

test("NET-14: lista vazia explícita",async()=>{
    preparar(vi.fn().mockResolvedValue(json([])));
    expect((await screen.findByText("Nenhum cadastro pendente.")).textContent).toBe("Nenhum cadastro pendente.");
    expect(screen.queryByRole("button",{name:/^Aprovar /})).toBeNull();
});

test("NET-14: erro inicial explícito permite atualizar sem retry automático",async()=>{
    const fetch=vi.fn().mockResolvedValueOnce(json({erro:"Serviço indisponível."},500)).mockResolvedValueOnce(json([ana]));
    const {user}=preparar(fetch);
    expect((await screen.findByRole("alert")).textContent).toBe("Serviço indisponível.");
    expect(fetch).toHaveBeenCalledTimes(1);expect(screen.queryByText("Nenhum cadastro pendente.")).toBeNull();
    await user.click(screen.getByRole("button",{name:"Atualizar lista"}));
    expect(await screen.findByText(ana.email)).toBeTruthy();expect(fetch).toHaveBeenCalledTimes(2);
});

test.each(["rede","malformado"])("NET-14: aprovação incerta%s conserva lista até atualização explícita",async tipo=>{
    const fetch=vi.fn().mockResolvedValueOnce(json([ana,bia]));
    if(tipo==="rede") fetch.mockRejectedValueOnce(new TypeError("rede"));
    else fetch.mockResolvedValueOnce(json({id:999,estado:"APROVADO"}));
    fetch.mockResolvedValueOnce(json([]));
    const {user}=preparar(fetch);await screen.findByText(ana.email);
    await user.click(screen.getByRole("button",{name:"Aprovar Ana"}));
    expect((await screen.findByRole("alert")).textContent).toBe("Não foi possível confirmar a aprovação. Atualize a lista antes de tentar novamente.");
    expect(screen.getByText(ana.email)).toBeTruthy();expect(screen.getByText(bia.email)).toBeTruthy();
    expect(screen.queryByText("Cadastro de Ana aprovado.")).toBeNull();expect(fetch).toHaveBeenCalledTimes(2);
    expect(screen.getByRole("button",{name:"Aprovar Ana"}).disabled).toBe(true);expect(screen.getByRole("button",{name:"Aprovar Bia"}).disabled).toBe(true);
    await user.click(screen.getByRole("button",{name:"Atualizar lista"}));
    expect(await screen.findByText("Nenhum cadastro pendente.")).toBeTruthy();expect(screen.queryByRole("alert")).toBeNull();expect(fetch).toHaveBeenCalledTimes(3);
});

test("NET-14:403 preserva lista e mensagem sem afirmar sucesso",async()=>{
    const fetch=vi.fn().mockResolvedValueOnce(json([ana])).mockResolvedValueOnce(json({erro:"Aprovação recusada."},403));
    const {user}=preparar(fetch);await screen.findByText(ana.email);await user.click(screen.getByRole("button",{name:"Aprovar Ana"}));
    expect((await screen.findByRole("alert")).textContent).toBe("Aprovação recusada.");expect(screen.getByText(ana.email)).toBeTruthy();
    expect(screen.queryByText("Cadastro de Ana aprovado.")).toBeNull();expect(fetch).toHaveBeenCalledTimes(2);
});

test("NET-14: atualização falha conserva dados e exige lista confirmada",async()=>{
    const fetch=vi.fn().mockResolvedValueOnce(json([ana])).mockResolvedValueOnce(json({erro:"Falha ao atualizar."},500));
    const {user}=preparar(fetch);await screen.findByText(ana.email);await user.click(screen.getByRole("button",{name:"Atualizar lista"}));
    expect((await screen.findByRole("alert")).textContent).toBe("Falha ao atualizar.");expect(screen.getByText(ana.email)).toBeTruthy();
    expect(screen.getByRole("button",{name:"Aprovar Ana"}).disabled).toBe(true);expect(fetch).toHaveBeenCalledTimes(2);
});

test("NET-14: envio em curso bloqueia repetir, fechar e atualizar",async()=>{
    let resolver;const fetch=vi.fn().mockResolvedValueOnce(json([ana])).mockImplementationOnce(()=>new Promise(r=>{resolver=r;}));
    preparar(fetch);const botao=await screen.findByRole("button",{name:"Aprovar Ana"});
    fireEvent.click(botao);fireEvent.click(botao);
    expect(fetch).toHaveBeenCalledTimes(2);expect(botao.disabled).toBe(true);
    expect(screen.getByRole("button",{name:"Fechar aprovações"}).disabled).toBe(true);expect(screen.getByRole("button",{name:"Atualizar lista"}).disabled).toBe(true);
    await act(async()=>resolver(json({id:4,estado:"APROVADO"})));
    await waitFor(()=>expect(screen.getByText("Nenhum cadastro pendente.")).toBeTruthy());
});

test("NET-14: inicialização atual com StrictMode conclui carregamento após cancelar montagem anterior",async()=>{
    const fetch=vi.fn().mockImplementationOnce((_url,{signal})=>new Promise((_resolve,reject)=>{
        signal.addEventListener("abort",()=>reject(new DOMException("Cancelado","AbortError")));
    })).mockResolvedValueOnce(json([ana]));
    vi.stubGlobal("fetch",fetch);
    render(<StrictMode><AprovacoesCadastros token="sessao" aoFechar={()=>{}}/></StrictMode>);
    expect(await screen.findByText(ana.email)).toBeTruthy();
    expect(screen.queryByText("Carregando cadastros...")).toBeNull();
    expect(screen.getByRole("button",{name:"Aprovar Ana"}).disabled).toBe(false);
});
