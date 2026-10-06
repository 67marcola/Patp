import { act, fireEvent, render, screen, waitFor, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { expect, test, vi } from "vitest";
import Quadro from "./Quadro";
import Gerenciamentos from "./Gerenciamentos";
import App from "../App";
import { ativarPorTeclado } from "../test/keyboard";

const quadro = { id: 9, nome: "Instalações", descricao: "Postes", criador: { id: 3, nome: "Ana" },
    arquivado: false, versao: 0, podeAdministrar: true };
const etapas = [
    { id: 4, nome: "Planejamento", setor: "Técnico", ordem: 1, categoria: "TRABALHO" },
    { id: 5, nome: "Execução", setor: "Campo", ordem: 2, categoria: "TRABALHO" },
    { id: 20, nome: "Concluídos", setor: null, ordem: 3, categoria: "CONCLUIDA" },
    { id: 21, nome: "Cancelados", setor: null, ordem: 4, categoria: "CANCELADA" }
];
const demanda = { id: 42, numeroProcesso: "D-42", pessoa: "Maria", responsavel: "Ana", status: "Em andamento",
    prioridade: "Prioridade livre", dataEmissao: "2020-02-29", prazoEtapa: "2019-01-01", prazoGeral: "2018-01-01",
    dataConclusao: null, dataCancelamento: null, motivoCancelamento: null, observacoes: "Uma\nOutra", etapaId: 4 };
const dados = { numeroProcesso: "D-42", pessoa: "Maria", responsavel: "Ana", prioridade: "Prioridade livre",
    dataEmissao: "2020-02-29", prazoEtapa: "2019-01-01", prazoGeral: "2018-01-01", observacoes: "Uma\nOutra" };
const labels = { numeroProcesso: "Número da demanda", pessoa: "Cliente/solicitante", responsavel: "Responsável",
    prioridade: "Prioridade", dataEmissao: "Data de emissão", prazoEtapa: "Prazo da etapa", prazoGeral: "Prazo geral",
    observacoes: "Observações" };
const snapshot = (demandas = [], flags = {}, colunas = etapas) => ({ gerenciamento: { ...quadro, ...flags }, demandas,
    etapas: colunas.map(etapa => ({ ...etapa, quantidadeDemandas: demandas.filter(item => item.etapaId === etapa.id).length })) });
const json = (valor, status = 200) => new Response(JSON.stringify(valor), { status });
const endpoint = "http://localhost:8081/api/gerenciamentos/9";
const erroComunicacao = "Não foi possível confirmar a operação. Atualize a lista antes de tentar novamente.";

function preparar(handler = () => json(snapshot()), registro = quadro) {
    localStorage.setItem("token", "sessao");
    const fetchMock = vi.fn(handler);
    vi.stubGlobal("fetch", fetchMock);
    const voltar = vi.fn();
    const atualizar = vi.fn();
    const aoOcupar = vi.fn();
    const view = render(<Quadro gerenciamento={registro} voltar={voltar} atualizar={atualizar} aoOcupar={aoOcupar} />);
    return { user: userEvent.setup(), fetchMock, voltar, atualizar, aoOcupar, ...view };
}

async function abrir(user) {
    const botao = await screen.findByRole("button", { name: "+ Criar processo", exact: true });
    await waitFor(() => expect(botao.disabled).toBe(false));
    await ativarPorTeclado(user, botao);
    expect(document.activeElement).toBe(screen.getByLabelText("Número da demanda"));
}

function preencher(valores = dados) {
    for (const [campo, valor] of Object.entries(valores)) fireEvent.change(screen.getByLabelText(labels[campo]), { target: { value: valor } });
}

function conferirRascunho(valores = dados) {
    for (const [campo, valor] of Object.entries(valores)) expect(screen.getByLabelText(labels[campo]).value).toBe(valor);
}

function valorCartao(cartao, label) {
    return within(cartao).getByText(label, { exact: true }).nextElementSibling.textContent;
}

test.each([{ podeAdministrar: true }, { podeAdministrar: false }, { criador: null, podeAdministrar: false }])
    ("CAD-08/26/30/32/40: qualquer autenticado cadastra com snapshot/cache/foco atuais%s", async flags => {
        const { user, fetchMock, atualizar } = preparar((_url, request) => request.method === "GET"
            ? json(snapshot([], flags)) : json(snapshot([demanda], { ...flags, versao: 1 }), 201));
        await abrir(user);
        preencher();
        await ativarPorTeclado(user, screen.getByRole("button", { name: "Criar demanda" }));
        const cartao = await screen.findByRole("article", { name: "D-42 — Maria" });
        expect(cartao.dataset.demandaId).toBe("42");
        expect(cartao.closest("section").getAttribute("aria-labelledby")).toBe("etapa-titulo-4");
        expect(screen.queryByRole("form", { name: "Novo processo" })).toBeNull();
        expect(screen.getByRole("region", { name: "Planejamento" }).textContent).toContain("1 demanda");
        expect(atualizar.mock.calls.map(([registro]) => registro)).toEqual([{ ...quadro, ...flags }, { ...quadro, ...flags, versao: 1 }]);
        expect(fetchMock.mock.calls.map(([url, request]) => [url, request.method, request.body && JSON.parse(request.body)]))
            .toEqual([[`${endpoint}/estrutura-etapas`, "GET", undefined], [`${endpoint}/demandas`, "POST", { ...dados, versao: 0 }]]);
        expect(document.activeElement).toBe(screen.getByRole("button", { name: "+ Criar processo" }));
    });

test.each([true, false])("CAD-07/27: somente finais bloqueia cadastro e orienta administração=%s", async podeAdministrar => {
    const { fetchMock } = preparar(() => json(snapshot([], { podeAdministrar }, etapas.slice(2))));
    await screen.findByRole("region", { name: "Concluídos" });
    expect(screen.getByRole("button", { name: "+ Criar processo" }).disabled).toBe(true);
    expect(screen.getByText(podeAdministrar ? "Cadastre uma etapa de trabalho usando Nova etapa antes de criar demandas."
        : "Solicite ao criador do quadro ou a um administrador que cadastre uma etapa de trabalho.")).not.toBeNull();
    expect(screen.queryByRole("button", { name: "+ Nova etapa" }) !== null).toBe(podeAdministrar);
    fireEvent.click(screen.getByRole("button", { name: "+ Criar processo" }));
    expect(screen.queryByRole("form", { name: "Novo processo" })).toBeNull();
    expect(fetchMock.mock.calls.map(([, request]) => request.method)).toEqual(["GET"]);
});

test.each(["pendente", "erro"])("CAD-26/33: consulta %s impede abrir cadastro", async situacao => {
    const { fetchMock } = preparar(() => situacao === "pendente" ? new Promise(() => {}) : json({ erro: "Consulta indisponível" }, 500));
    if (situacao === "erro") await screen.findByRole("alert");
    expect(screen.getByRole("button", { name: "+ Criar processo" }).disabled).toBe(true);
    fireEvent.click(screen.getByRole("button", { name: "+ Criar processo" }));
    expect(screen.queryByLabelText("Número da demanda")).toBeNull();
    expect(fetchMock.mock.calls.map(([, request]) => request.method)).toEqual(["GET"]);
});

test.each([false, true])("CAD-37/38/39: leitura integral por etapa preserva valores antigos, arquivo=%s", async arquivado => {
    const legado = { ...demanda, status: "Situação desconhecida", etapaId: 20, dataConclusao: "2021-03-04",
        dataCancelamento: "2022-05-06", motivoCancelamento: "Motivo antigo" };
    const { fetchMock } = preparar(() => json(snapshot([legado], { arquivado })));
    const cartao = await screen.findByRole("article", { name: "D-42 — Maria" });
    expect(cartao.dataset.demandaId).toBe("42");
    expect(cartao.closest("section").getAttribute("aria-labelledby")).toBe("etapa-titulo-20");
    expect(within(screen.getByRole("region", { name: "Planejamento" })).queryByRole("article")).toBeNull();
    for (const [label, valor] of Object.entries({ Status: "Situação desconhecida", Responsável: "Ana", Prioridade: "Prioridade livre",
        "Data de emissão": "29/02/2020", "Prazo da etapa": "01/01/2019", "Prazo geral": "01/01/2018",
        "Data de conclusão": "04/03/2021", "Data de cancelamento": "06/05/2022", "Motivo do cancelamento": "Motivo antigo", Observações: "Uma\nOutra" })) {
        expect(valorCartao(cartao, label)).toBe(valor);
    }
    expect(within(cartao).queryByRole("heading")).toBeNull();
    expect(within(cartao).queryByRole("button")).toBeNull();
    expect(screen.getAllByRole("region").map(coluna => within(coluna).getByRole("heading").textContent))
        .toEqual(["Planejamento", "Execução", "Concluídos", "Cancelados"]);
    expect(screen.queryByRole("button", { name: "+ Criar processo" }) === null).toBe(arquivado);
    expect(fetchMock.mock.calls.map(([, request]) => request.method)).toEqual(["GET"]);
});

test("CAD-38: nulls indicam ausência e HTML/linhas/texto extenso são literais", async () => {
    const antiga = { ...demanda, numeroProcesso: "<b>D-42</b>", pessoa: "<img src=x>", responsavel: null,
        status: null, prioridade: null, dataEmissao: null, prazoEtapa: null, prazoGeral: null,
        observacoes: "<script>alert(1)</script>\n" + "x".repeat(10001) };
    preparar(() => json(snapshot([antiga])));
    const cartao = await screen.findByRole("article", { name: "<b>D-42</b> — <img src=x>" });
    for (const label of ["Status", "Responsável", "Prioridade", "Data de emissão", "Prazo da etapa", "Prazo geral", "Data de conclusão", "Data de cancelamento"]) {
        expect(valorCartao(cartao, label)).toBe("Não informado");
    }
    expect(valorCartao(cartao, "Observações")).toBe(antiga.observacoes);
    expect(cartao.querySelector("script,img,b")).toBeNull();
    expect(within(cartao).queryByText("Motivo do cancelamento")).toBeNull();
    expect(within(cartao).getByText("<b>D-42</b> — <img src=x>").tagName).toBe("STRONG");
});

test("CAD-31/40: pendência bloqueia navegação/cancelar/etapas e duplica zero cadastros", async () => {
    let resolver;
    const { user, fetchMock, voltar, aoOcupar } = preparar((_url, request) => request.method === "GET"
        ? json(snapshot()) : new Promise(resolve => { resolver = resolve; }));
    await abrir(user);
    preencher();
    await user.dblClick(screen.getByRole("button", { name: "Criar demanda" }));
    fireEvent.submit(screen.getByRole("form", { name: "Novo processo" }));
    for (const label of Object.values(labels)) expect(screen.getByLabelText(label).disabled).toBe(true);
    for (const nome of ["Cancelar", "+ Criar processo", "+ Nova etapa", "Editar etapa 1: Planejamento", "Remover etapa 1: Planejamento"]) {
        expect(screen.getByRole("button", { name: nome }).disabled).toBe(true);
    }
    expect(screen.getByRole("button", { name: /Voltar/ }).disabled).toBe(true);
    await user.click(screen.getByRole("button", { name: /Voltar/ }));
    await user.click(screen.getByRole("button", { name: "Cancelar" }));
    expect(voltar).not.toHaveBeenCalled();
    expect(fetchMock.mock.calls.filter(([, request]) => request.method === "POST").map(([, request]) => JSON.parse(request.body)))
        .toEqual([{ ...dados, versao: 0 }]);
    expect(aoOcupar.mock.calls).toEqual([[true]]);
    await act(async () => resolver(json(snapshot([demanda], { versao: 1 }), 201)));
    await screen.findByRole("article", { name: "D-42 — Maria" });
    expect(aoOcupar.mock.calls).toEqual([[true], [false]]);
});

test("CAD-40: Cancelar retorna foco e exclusividade dos editores sem mutação", async () => {
    const { user, fetchMock } = preparar();
    await abrir(user);
    preencher();
    await ativarPorTeclado(user, screen.getByRole("button", { name: "Cancelar" }));
    expect(screen.queryByLabelText("Número da demanda")).toBeNull();
    expect(document.activeElement).toBe(screen.getByRole("button", { name: "+ Criar processo" }));
    await ativarPorTeclado(user, screen.getByRole("button", { name: "+ Nova etapa" }));
    expect(screen.getByRole("button", { name: "+ Criar processo" }).disabled).toBe(true);
    fireEvent.click(screen.getByRole("button", { name: "+ Criar processo" }));
    expect(screen.getByLabelText("Nome da etapa")).not.toBeNull();
    expect(screen.queryByLabelText("Número da demanda")).toBeNull();
    expect(fetchMock.mock.calls.map(([, request]) => request.method)).toEqual(["GET"]);
});

const falhas = [[400, "Dados da requisição inválidos."], [401, "Usuário não autenticado."], [404, "Gerenciamento não encontrado."],
    [409, "Já existe uma demanda com esse número."], [500, "Não foi possível concluir a operação."], [0, erroComunicacao], [201, erroComunicacao], [204, erroComunicacao]];
test.each(falhas)("CAD-33/34: erro%s conserva snapshot/draft e exige refresh manual antes do POST atualizado", async (status, mensagem) => {
    let consultas = 0;
    let mutacoes = 0;
    const existente = { ...demanda, id: 40, numeroProcesso: "Anterior", pessoa: "Cliente anterior" };
    const { user, fetchMock, atualizar } = preparar((_url, request) => {
        if (request.method === "GET") {
            consultas += 1;
            return json(snapshot([existente], { versao: consultas === 1 ? 0 : 7 }));
        }
        mutacoes += 1;
        if (mutacoes === 2) return json(snapshot([existente, demanda], { versao: 8 }), 201);
        if (status === 0) return Promise.reject(new TypeError("offline"));
        if (status === 204) return new Response(null, { status: 204 });
        return json(status === 201 ? { gerenciamento: quadro, etapas: [] } : { erro: mensagem }, status);
    });
    await abrir(user);
    preencher();
    await ativarPorTeclado(user, screen.getByRole("button", { name: "Criar demanda" }));
    expect((await screen.findByRole("alert")).textContent).toBe(mensagem);
    conferirRascunho();
    expect(screen.getByRole("article", { name: "Anterior — Cliente anterior" })).not.toBeNull();
    expect(screen.queryByRole("article", { name: "D-42 — Maria" })).toBeNull();
    expect(atualizar.mock.calls.map(([registro]) => registro.versao)).toEqual([0]);
    expect(screen.getByRole("button", { name: "Criar demanda" }).disabled).toBe(true);
    fireEvent.submit(screen.getByRole("form", { name: "Novo processo" }));
    expect(fetchMock.mock.calls.map(([, request]) => request.method)).toEqual(["GET", "POST"]);
    await ativarPorTeclado(user, screen.getByRole("button", { name: "Atualizar quadro" }));
    await waitFor(() => expect(screen.getByRole("button", { name: "Criar demanda" }).disabled).toBe(false));
    conferirRascunho();
    expect(fetchMock.mock.calls.map(([, request]) => request.method)).toEqual(["GET", "POST", "GET"]);
    expect(atualizar.mock.calls.map(([registro]) => registro.versao)).toEqual([0, 7]);
    await ativarPorTeclado(user, screen.getByRole("button", { name: "Criar demanda" }));
    await screen.findByRole("article", { name: "D-42 — Maria" });
    expect(screen.getByText("2 demandas").textContent).toBe("2 demandas");
    expect(atualizar.mock.calls.map(([registro]) => registro.versao)).toEqual([0, 7, 8]);
    expect(fetchMock.mock.calls.filter(([, request]) => request.method === "POST").map(([, request]) => JSON.parse(request.body)))
        .toEqual([{ ...dados, versao: 0 }, { ...dados, versao: 7 }]);
});

test.each([[{ arquivado: true }, etapas, true], [{}, etapas.slice(2), true], [{ podeAdministrar: false }, etapas, false]])
    ("CAD-34: refresh aplica arquivo/trabalhos/permissões sem perder draft%s", async (flags, colunas, bloqueado) => {
        let consultas = 0;
        const { user, fetchMock } = preparar((_url, request) => request.method === "GET"
            ? json(++consultas === 1 ? snapshot() : snapshot([], { ...flags, versao: 9 }, colunas))
            : json({ erro: "Versão antiga" }, 409));
        await abrir(user);
        preencher();
        await user.click(screen.getByRole("button", { name: "Criar demanda" }));
        await screen.findByRole("alert");
        await ativarPorTeclado(user, screen.getByRole("button", { name: "Atualizar quadro" }));
        await waitFor(() => expect(screen.queryByRole("button", { name: "Atualizar quadro" })).toBeNull());
        conferirRascunho();
        expect(screen.getByRole("button", { name: "Criar demanda" }).disabled).toBe(bloqueado);
        for (const label of Object.values(labels)) expect(screen.getByLabelText(label).disabled).toBe(bloqueado);
        if (bloqueado) fireEvent.submit(screen.getByRole("form", { name: "Novo processo" }));
        expect(fetchMock.mock.calls.map(([, request]) => request.method)).toEqual(["GET", "POST", "GET"]);
    });

test("CAD-33/34: GET de refresh falho conserva cartões e draft até consulta manual confirmar", async () => {
    let consultas = 0;
    const { user, fetchMock, atualizar } = preparar((_url, request) => request.method === "GET"
        ? ++consultas === 2 ? json({ erro: "Consulta indisponível" }, 500) : json(snapshot([demanda], { versao: consultas === 1 ? 0 : 8 }))
        : json({ erro: "Versão antiga" }, 409));
    await abrir(user);
    preencher();
    await user.click(screen.getByRole("button", { name: "Criar demanda" }));
    await screen.findByRole("alert");
    await ativarPorTeclado(user, screen.getByRole("button", { name: "Atualizar quadro" }));
    await screen.findByText("Consulta indisponível");
    conferirRascunho();
    expect(screen.getByRole("article", { name: "D-42 — Maria" })).not.toBeNull();
    expect(screen.getByRole("button", { name: "Criar demanda" }).disabled).toBe(true);
    expect(atualizar.mock.calls.map(([registro]) => registro.versao)).toEqual([0]);
    await ativarPorTeclado(user, screen.getByRole("button", { name: "Tentar novamente" }));
    await waitFor(() => expect(screen.getByRole("button", { name: "Criar demanda" }).disabled).toBe(false));
    conferirRascunho();
    expect(atualizar.mock.calls.map(([registro]) => registro.versao)).toEqual([0, 8]);
    expect(fetchMock.mock.calls.map(([, request]) => request.method)).toEqual(["GET", "POST", "GET", "GET"]);
});

test.each(["POST", "PUT", "DELETE"])("CAD-24: configurar etapa via%s mantém cartões e contagens do snapshot", async metodo => {
    const atualizadas = metodo === "DELETE" ? etapas.filter(etapa => etapa.id !== 5)
        : etapas.map(etapa => etapa.id === 5 ? { ...etapa, nome: "Entrega" } : etapa);
    const { user, fetchMock } = preparar((_url, request) => request.method === "GET" ? json(snapshot([demanda]))
        : json(snapshot([demanda], { versao: 1 }, atualizadas), metodo === "POST" ? 201 : 200));
    await screen.findByRole("article", { name: "D-42 — Maria" });
    await ativarPorTeclado(user, screen.getByRole("button", { name: metodo === "POST" ? "+ Nova etapa" : `${metodo === "PUT" ? "Editar" : "Remover"} etapa 2: Execução` }));
    if (metodo === "POST") {
        await user.type(screen.getByLabelText("Nome da etapa"), "Entrega");
        await user.type(screen.getByLabelText("Setor responsável"), "Campo");
    }
    await ativarPorTeclado(user, screen.getByRole("button", { name: metodo === "DELETE" ? "Remover" : "Salvar etapa", exact: true }));
    await waitFor(() => expect(screen.queryByRole("form")).toBeNull());
    await waitFor(() => expect(screen.queryByRole("dialog")).toBeNull());
    expect(screen.getByRole("article", { name: "D-42 — Maria" }).dataset.demandaId).toBe("42");
    expect(within(screen.getByRole("region", { name: "Planejamento" })).getByText("1 demanda").textContent).toBe("1 demanda");
    expect(fetchMock.mock.calls.map(([, request]) => request.method)).toEqual(["GET", metodo]);
});

test("CAD-35: troca de ID remove imediatamente cartões antigos enquanto consulta novo quadro", async () => {
    let resolver;
    const novo = { ...quadro, id: 10, nome: "Outro" };
    const { rerender, atualizar } = preparar(url => url.includes("/9/") ? json(snapshot([demanda]))
        : new Promise(resolve => { resolver = resolve; }));
    await screen.findByRole("article", { name: "D-42 — Maria" });
    rerender(<Quadro gerenciamento={novo} atualizar={atualizar} />);
    expect(screen.queryByRole("article", { name: "D-42 — Maria" })).toBeNull();
    expect(screen.getByRole("heading", { name: "Outro" }).textContent).toBe("Outro");
    await act(async () => resolver(json(snapshot([], novo))));
    expect(screen.queryByRole("article")).toBeNull();
    expect(atualizar.mock.calls.map(([registro]) => registro.id)).toEqual([9, 10]);
});

test("CAD-35: resposta GET antiga abortada não mistura cartões/cache do novo ID", async () => {
    let resolver;
    const novo = { ...quadro, id: 10, nome: "Outro" };
    const outra = { ...demanda, id: 99, numeroProcesso: "Outra" };
    const { rerender, fetchMock, atualizar } = preparar(url => url.includes("/9/")
        ? new Promise(resolve => { resolver = resolve; }) : json(snapshot([outra], novo)));
    rerender(<Quadro gerenciamento={novo} atualizar={atualizar} />);
    await screen.findByRole("article", { name: "Outra — Maria" });
    expect(fetchMock.mock.calls[0][1].signal.aborted).toBe(true);
    await act(async () => resolver(json(snapshot([demanda]))));
    expect(screen.queryByRole("article", { name: "D-42 — Maria" })).toBeNull();
    expect(atualizar.mock.calls.map(([registro]) => registro.id)).toEqual([10]);
});

test("CAD-35: unmount aborta GET e não atualiza cache após resposta tardia", async () => {
    let resolver;
    const { unmount, fetchMock, atualizar } = preparar(() => new Promise(resolve => { resolver = resolve; }));
    unmount();
    expect(fetchMock.mock.calls[0][1].signal.aborted).toBe(true);
    await act(async () => resolver(json(snapshot([demanda]))));
    expect(atualizar).not.toHaveBeenCalled();
});

test("CAD-35: resposta POST antiga não aplica cartões/cache depois da troca de quadro", async () => {
    let resolver;
    const novo = { ...quadro, id: 10, nome: "Outro" };
    const { user, rerender, atualizar } = preparar((url, request) => request.method === "POST"
        ? new Promise(resolve => { resolver = resolve; }) : json(snapshot([], url.includes("/10/") ? novo : {})));
    await abrir(user);
    preencher();
    await user.click(screen.getByRole("button", { name: "Criar demanda" }));
    rerender(<Quadro gerenciamento={novo} atualizar={atualizar} />);
    await waitFor(() => expect(atualizar.mock.calls.map(([registro]) => registro.id)).toEqual([9, 10]));
    await act(async () => resolver(json(snapshot([demanda], { versao: 1 }), 201)));
    expect(screen.queryByRole("article", { name: "D-42 — Maria" })).toBeNull();
    expect(atualizar.mock.calls.map(([registro]) => [registro.id, registro.versao])).toEqual([[9, 0], [10, 0]]);
});

test.each([true, false])("CAD-31: cadastro pendente bloqueia Sair e libera após resposta, sucesso=%s", async sucesso => {
    let resolver;
    localStorage.setItem("token", "sessao");
    const usuario = { id: 3, nome: "Ana", setor: "Técnico", email: "ana@example.test" };
    localStorage.setItem("usuario", JSON.stringify(usuario));
    vi.stubGlobal("fetch", vi.fn((url, request) => {
        if (request.method === "POST") return new Promise(resolve => { resolver = resolve; });
        if (url.includes("?arquivado=")) return json([quadro]);
        return json(url.endsWith("/estrutura-etapas") ? snapshot() : quadro);
    }));
    const user = userEvent.setup();
    render(<App />);
    await ativarPorTeclado(user, await screen.findByRole("button", { name: "Abrir Instalações" }));
    await abrir(user);
    preencher();
    await user.click(screen.getByRole("button", { name: "Criar demanda" }));
    expect(screen.getByRole("button", { name: "Sair" }).disabled).toBe(true);
    await user.click(screen.getByRole("button", { name: "Sair" }));
    expect(localStorage.getItem("token")).toBe("sessao");
    expect(JSON.parse(localStorage.getItem("usuario"))).toEqual(usuario);
    await act(async () => resolver(sucesso ? json(snapshot([demanda], { versao: 1 }), 201)
        : json({ erro: "Falha" }, 500)));
    await waitFor(() => expect(screen.getByRole("button", { name: "Sair" }).disabled).toBe(false));
    await ativarPorTeclado(user, screen.getByRole("button", { name: "Sair" }));
    expect((await screen.findByRole("heading", { name: "Bem-vindo" })).textContent).toBe("Bem-vindo");
    expect(localStorage.getItem("token")).toBeNull();
});

test("CAD-32/39: cache real da lista arquiva com versão do cadastro e conserva cartões na consulta", async () => {
    let registro = { ...quadro };
    let demandas = [];
    const fetchMock = vi.fn((url, request) => {
        if (url.includes("?arquivado=")) return json(registro.arquivado === url.endsWith("=true") ? [registro] : []);
        if (url.endsWith("/estrutura-etapas")) return json(snapshot(demandas, registro));
        if (request.method === "GET") return json(registro);
        if (url.endsWith("/demandas")) {
            registro = { ...registro, versao: 1 };
            demandas = [demanda];
            return json(snapshot(demandas, registro), 201);
        }
        if (url.endsWith("/arquivar")) {
            if (JSON.parse(request.body).versao !== 1) return json({ erro: "Versão antiga" }, 409);
            registro = { ...registro, arquivado: true, versao: 2 };
            return new Response(null, { status: 204 });
        }
        throw new Error(`Requisição inesperada ${url}`);
    });
    vi.stubGlobal("fetch", fetchMock);
    localStorage.setItem("token", "sessao");
    const user = userEvent.setup();
    render(<Gerenciamentos />);
    await ativarPorTeclado(user, await screen.findByRole("button", { name: "Abrir Instalações" }));
    await abrir(user);
    preencher();
    await user.click(screen.getByRole("button", { name: "Criar demanda" }));
    await screen.findByRole("article", { name: "D-42 — Maria" });
    await ativarPorTeclado(user, screen.getByRole("button", { name: /Voltar/ }));
    await ativarPorTeclado(user, screen.getByRole("button", { name: "Arquivar Instalações" }));
    await ativarPorTeclado(user, screen.getByRole("button", { name: "Arquivar", exact: true }));
    expect((await screen.findByRole("heading", { name: "Nenhum gerenciamento ativo" })).textContent).toBe("Nenhum gerenciamento ativo");
    expect(fetchMock.mock.calls.filter(([, request]) => request.method !== "GET")
        .map(([url, request]) => [url, request.method, JSON.parse(request.body)]))
        .toEqual([[`${endpoint}/demandas`, "POST", { ...dados, versao: 0 }], [`${endpoint}/arquivar`, "PUT", { versao: 1 }]]);
    await ativarPorTeclado(user, screen.getByRole("button", { name: "Arquivados", exact: true }));
    await ativarPorTeclado(user, await screen.findByRole("button", { name: "Abrir Instalações" }));
    expect((await screen.findByRole("article", { name: "D-42 — Maria" })).dataset.demandaId).toBe("42");
    expect(screen.queryByRole("button", { name: "+ Criar processo" })).toBeNull();
    expect(screen.getByText("Arquivado — somente consulta").textContent).toBe("Arquivado — somente consulta");
    expect(fetchMock.mock.calls.filter(([url]) => url.endsWith("/estrutura-etapas"))).toHaveLength(2);
});
