import { act, fireEvent, render, screen, waitFor, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { expect, test, vi } from "vitest";
import Quadro from "./Quadro";
import Gerenciamentos from "./Gerenciamentos";
import { ativarPorTeclado } from "../test/keyboard";

const quadro = { id: 9, nome: "Instalações", descricao: "Postes", criador: { id: 3, nome: "Ana" }, arquivado: false, versao: 0, podeAdministrar: true };
const a = { id: 4, nome: "Planejamento", setor: "Técnico", ordem: 1, categoria: "TRABALHO", quantidadeDemandas: 2 };
const b = { id: 5, nome: "Execução", setor: "Campo", ordem: 2, categoria: "TRABALHO", quantidadeDemandas: 0 };
const finais = [
    { id: 20, nome: "Concluídos", setor: null, categoria: "CONCLUIDA", quantidadeDemandas: 3 },
    { id: 21, nome: "Cancelados", setor: null, categoria: "CANCELADA", quantidadeDemandas: 1 }
];
const snapshot = (etapas = [a, b], metadados = {}, incluirFinais = true) => ({
    gerenciamento: { ...quadro, ...metadados },
    etapas: incluirFinais ? [...etapas, ...finais.map((etapa, index) => ({ ...etapa, ordem: etapas.length + index + 1 }))] : etapas
});
const json = (dados, status = 200) => new Response(JSON.stringify(dados), { status });
const endpoint = "http://localhost:8081/api/gerenciamentos/9";

function preparar(handler = () => json(snapshot()), registro = quadro) {
    const fetchMock = vi.fn(handler);
    vi.stubGlobal("fetch", fetchMock);
    localStorage.setItem("token", "sessao");
    const voltar = vi.fn();
    const atualizar = vi.fn();
    render(<Quadro gerenciamento={registro} voltar={voltar} atualizar={atualizar} />);
    return { user: userEvent.setup(), fetchMock, voltar, atualizar };
}

async function abrirEditor(user, nome = "+ Nova etapa") {
    await ativarPorTeclado(user, await screen.findByRole("button", { name: nome }));
}

async function preencher(user, nome, setor, ordem = "1") {
    await user.clear(screen.getByLabelText("Nome da etapa"));
    await user.type(screen.getByLabelText("Nome da etapa"), nome);
    await user.clear(screen.getByLabelText("Setor responsável"));
    await user.type(screen.getByLabelText("Setor responsável"), setor);
    await user.selectOptions(screen.getByLabelText("Posição"), ordem);
}

function conferirFinais(esperadas = finais) {
    for (const etapa of esperadas) {
        const coluna = screen.getByRole("region", { name: etapa.nome, exact: true });
        expect(within(coluna).getByRole("heading").id).toBe(`etapa-titulo-${etapa.id}`);
        expect(within(coluna).getByText("Etapa final obrigatória").textContent).toBe("Etapa final obrigatória");
        expect(within(coluna).queryByText(/Setor responsável:/)).toBeNull();
        expect(within(coluna).queryByText(/^Posição /)).toBeNull();
        expect(within(coluna).queryByRole("button")).toBeNull();
        expect(within(coluna).getByText(`${etapa.quantidadeDemandas} ${etapa.quantidadeDemandas === 1 ? "demanda" : "demandas"}`).textContent)
            .toBe(`${etapa.quantidadeDemandas} ${etapa.quantidadeDemandas === 1 ? "demanda" : "demandas"}`);
    }
}

test("ETA-12/13/15/19/28/30: CRUD por teclado usa snapshots/IDs/versões e reordena o quadro sem GETextra", async () => {
    const c = { id: 6, nome: "Análise", setor: "Engenharia", ordem: 2, categoria: "TRABALHO", quantidadeDemandas: 0 };
    const editada = { ...b, nome: "Entrega", setor: "Operação", ordem: 1 };
    const respostas = [
        snapshot([a, c, { ...b, ordem: 3 }], { versao: 1 }),
        snapshot([editada, { ...a, ordem: 2 }, { ...c, ordem: 3 }], { versao: 2 }),
        snapshot([editada, { ...a, ordem: 2 }], { versao: 3 })
    ];
    const { user, fetchMock, atualizar } = preparar((_url, request) => request.method === "GET"
        ? json(snapshot()) : json(respostas.shift(), request.method === "POST" ? 201 : 200));
    await abrirEditor(user);
    await preencher(user, "Análise", "Engenharia", "2");
    await ativarPorTeclado(user, screen.getByRole("button", { name: "Salvar etapa" }));
    await screen.findByRole("heading", { name: "Análise", exact: true });
    expect(screen.getAllByRole("region").map(item => within(item).getByRole("heading").textContent)).toEqual(["Planejamento", "Análise", "Execução", "Concluídos", "Cancelados"]);
    conferirFinais();
    await abrirEditor(user, "Editar etapa 3: Execução");
    expect(screen.getByLabelText("Nome da etapa").value).toBe("Execução");
    expect(screen.getByLabelText("Setor responsável").value).toBe("Campo");
    expect(screen.getByLabelText("Posição").value).toBe("3");
    await preencher(user, "Entrega", "Operação", "1");
    await ativarPorTeclado(user, screen.getByRole("button", { name: "Salvar etapa" }));
    await screen.findByRole("heading", { name: "Entrega", exact: true });
    expect(screen.getAllByRole("region").map(item => within(item).getByRole("heading").textContent)).toEqual(["Entrega", "Planejamento", "Análise", "Concluídos", "Cancelados"]);
    conferirFinais();
    await abrirEditor(user, "Remover etapa 3: Análise");
    await ativarPorTeclado(user, within(screen.getByRole("dialog")).getByRole("button", { name: "Remover", exact: true }));
    await waitFor(() => expect(screen.queryByRole("dialog")).toBeNull());
    expect(screen.getAllByRole("region").map(item => within(item).getByRole("heading").textContent)).toEqual(["Entrega", "Planejamento", "Concluídos", "Cancelados"]);
    conferirFinais();
    expect(fetchMock.mock.calls.map(([url, request]) => [url, request.method, request.body && JSON.parse(request.body)])).toEqual([
        [`${endpoint}/estrutura-etapas`, "GET", undefined],
        [`${endpoint}/etapas`, "POST", { nome: "Análise", setor: "Engenharia", ordem: 2, versao: 0 }],
        [`${endpoint}/etapas/5`, "PUT", { nome: "Entrega", setor: "Operação", ordem: 1, versao: 1 }],
        [`${endpoint}/etapas/6`, "DELETE", { versao: 2 }]
    ]);
    expect(atualizar.mock.calls.map(([gerenciamento]) => gerenciamento.versao)).toEqual([0, 1, 2, 3]);
    expect(atualizar).toHaveBeenLastCalledWith({ ...quadro, versao: 3 });
    expect(screen.getByText("Setor responsável: Operação").textContent).toBe("Setor responsável: Operação");
});

test.each([
    ["criador", { nome: "Quadro atualizado", descricao: "Descrição atual" }, true],
    ["admin legado", { criador: null }, true],
    ["terceiro", { podeAdministrar: false }, false],
    ["funcionário legado", { criador: null, podeAdministrar: false }, false],
    ["arquivado", { arquivado: true }, false]
])("ETA-03/22: snapshot atual define controles para%s, superando props antigas", async (_papel, flags, permitido) => {
    const { fetchMock, atualizar } = preparar(() => json(snapshot([a, b], flags)));
    await screen.findByRole("heading", { name: "Planejamento", exact: true });
    expect(screen.queryByRole("button", { name: "+ Nova etapa" }) !== null).toBe(permitido);
    expect(screen.queryByRole("button", { name: "Editar etapa 1: Planejamento" }) !== null).toBe(permitido);
    expect(screen.queryByRole("button", { name: "Remover etapa 1: Planejamento" }) !== null).toBe(permitido);
    expect(atualizar).toHaveBeenCalledExactlyOnceWith({ ...quadro, ...flags });
    expect(fetchMock.mock.calls.map(([, request]) => request.method)).toEqual(["GET"]);
    if (flags.nome) {
        expect(screen.getByRole("heading", { name: "Quadro atualizado" }).textContent).toBe("Quadro atualizado");
        expect(screen.getByText("Descrição atual").textContent).toBe("Descrição atual");
    }
    if (flags.arquivado) expect(screen.getByText("Arquivado — somente consulta").textContent).toBe("Arquivado — somente consulta");
});

test("ETA-01/22: contagem real distingue etapa ocupada de vazia", async () => {
    preparar();
    const ocupada = await screen.findByRole("region", { name: "Planejamento" });
    expect(within(ocupada).getByText("2 demandas").textContent).toBe("2 demandas");
    expect(within(ocupada).queryByText("Nenhum processo nesta etapa.")).toBeNull();
    expect(within(ocupada).getByText("2 demandas vinculadas a esta etapa.").textContent).toBe("2 demandas vinculadas a esta etapa.");
    const vazia = screen.getByRole("region", { name: "Execução" });
    expect(within(vazia).getByText("0 demandas").textContent).toBe("0 demandas");
    expect(within(vazia).getByText("Nenhum processo nesta etapa.").textContent).toBe("Nenhum processo nesta etapa.");
});

test.each(["+ Nova etapa", "Editar etapa 2: Execução", "Remover etapa 2: Execução"])("ETA-24/25/30: cancelar%s por teclado restaura foco sem mutação", async nome => {
    const { user, fetchMock } = preparar();
    const origem = await screen.findByRole("button", { name: nome });
    await ativarPorTeclado(user, origem);
    if (nome.startsWith("Remover")) {
        const dialogo = screen.getByRole("dialog", { name: "Remover Execução?" });
        expect(within(dialogo).getByText("Demandas impedem a remoção. Mova-as para outra etapa antes de remover esta coluna.").textContent)
            .toBe("Demandas impedem a remoção. Mova-as para outra etapa antes de remover esta coluna.");
        expect(document.activeElement).toBe(within(dialogo).getByRole("button", { name: "Cancelar" }));
    }
    await ativarPorTeclado(user, screen.getByRole("button", { name: "Cancelar" }));
    expect(screen.queryByRole("dialog")).toBeNull();
    expect(screen.queryByLabelText("Nome da etapa")).toBeNull();
    expect(document.activeElement).toBe(origem);
    expect(fetchMock.mock.calls.map(([, request]) => request.method)).toEqual(["GET"]);
});

test.each(["POST", "PUT"])("ETA-26: %s pendente impede saída e envios repetidos", async metodo => {
    let resolver;
    const { user, fetchMock, voltar } = preparar((_url, request) => request.method === "GET"
        ? json(snapshot()) : new Promise(resolve => { resolver = resolve; }));
    await abrirEditor(user, metodo === "POST" ? "+ Nova etapa" : "Editar etapa 2: Execução");
    if (metodo === "POST") await preencher(user, "Nova", "Setor");
    await user.dblClick(screen.getByRole("button", { name: "Salvar etapa" }));
    expect(screen.getByRole("button", { name: /Voltar/ }).disabled).toBe(true);
    expect(screen.getByRole("button", { name: "Cancelar" }).disabled).toBe(true);
    expect(fetchMock.mock.calls.filter(([, request]) => request.method === metodo)).toHaveLength(1);
    await user.click(screen.getByRole("button", { name: /Voltar/ }));
    expect(voltar).not.toHaveBeenCalled();
    await act(async () => resolver(json(snapshot([a, b], { versao: 1 }), metodo === "POST" ? 201 : 200)));
    await waitFor(() => expect(screen.queryByLabelText("Nome da etapa")).toBeNull());
});

test("ETA-26: DELETE pendente bloqueia voltar/cancelar e não duplica remoção", async () => {
    let resolver;
    const { user, fetchMock, voltar } = preparar((_url, request) => request.method === "GET"
        ? json(snapshot()) : new Promise(resolve => { resolver = resolve; }));
    await abrirEditor(user, "Remover etapa 2: Execução");
    await user.dblClick(within(screen.getByRole("dialog")).getByRole("button", { name: "Remover", exact: true }));
    expect(screen.getByRole("button", { name: "Removendo..." }).disabled).toBe(true);
    expect(screen.getByRole("button", { name: "Cancelar" }).disabled).toBe(true);
    await user.click(screen.getByRole("button", { name: /Voltar/ }));
    expect(voltar).not.toHaveBeenCalled();
    expect(fetchMock.mock.calls.filter(([, request]) => request.method === "DELETE")).toHaveLength(1);
    await act(async () => resolver(json(snapshot([a], { versao: 1 }))));
    await waitFor(() => expect(screen.queryByRole("dialog")).toBeNull());
    expect(screen.queryByRole("heading", { name: "Execução", exact: true })).toBeNull();
});

test.each([
    [409, "Gerenciamento alterado por outro usuário. Atualize e tente novamente."],
    [0, "Não foi possível confirmar a operação. Atualize a lista antes de tentar novamente."]
])("ETA-18/27/29/30: falha%s preserva rascunho, atualização teclado somenteGET e novo envio manual usa versão atual", async (status, mensagem) => {
    let versao = 0;
    let falhar = true;
    const { user, fetchMock } = preparar((_url, request) => {
        if (request.method === "GET") return json(snapshot([a, b], { versao }));
        if (falhar) return status === 0 ? Promise.reject(new TypeError("offline")) : json({ erro: mensagem }, status);
        return json(snapshot([a, { ...b, nome: "Rascunho", setor: "Engenharia", ordem: 2 }], { versao: versao + 1 }));
    });

    await abrirEditor(user, "Editar etapa 2: Execução");
    await preencher(user, "Rascunho", "Engenharia", "2");
    await ativarPorTeclado(user, screen.getByRole("button", { name: "Salvar etapa" }));
    expect((await screen.findByRole("alert")).textContent).toBe(mensagem);
    expect(screen.getByLabelText("Nome da etapa").value).toBe("Rascunho");
    expect(screen.getByLabelText("Setor responsável").value).toBe("Engenharia");
    expect(screen.getByLabelText("Posição").value).toBe("2");
    expect(screen.getByRole("button", { name: "Salvar etapa" }).disabled).toBe(true);
    const chamadasAntes = fetchMock.mock.calls.length;
    versao = 7;
    await ativarPorTeclado(user, screen.getByRole("button", { name: "Atualizar quadro" }));
    expect(fetchMock.mock.calls.slice(chamadasAntes).map(([url, request]) => [url, request.method]))
        .toEqual([[`${endpoint}/estrutura-etapas`, "GET"]]);
    expect(screen.getByLabelText("Nome da etapa").value).toBe("Rascunho");
    falhar = false;
    await ativarPorTeclado(user, screen.getByRole("button", { name: "Salvar etapa" }));
    await waitFor(() => expect(screen.queryByLabelText("Nome da etapa")).toBeNull());
    expect(fetchMock.mock.calls.filter(([, request]) => request.method === "PUT").map(([, request]) => JSON.parse(request.body)))
        .toEqual([{ nome: "Rascunho", setor: "Engenharia", ordem: 2, versao: 0 }, { nome: "Rascunho", setor: "Engenharia", ordem: 2, versao: 7 }]);
    expect(screen.getByRole("heading", { name: "Rascunho", exact: true }).textContent).toBe("Rascunho");
});

test.each([
    [409, "Esta etapa possui demandas. Mova-as para outra etapa antes de removê-la."],
    [403, "Você não tem permissão para administrar este gerenciamento."],
    [404, "Etapa não encontrada neste gerenciamento."],
    [500, "Não foi possível concluir a operação."],
    [0, "Não foi possível confirmar a operação. Atualize a lista antes de tentar novamente."]
])("ETA-16/27/29: remover falha%s conserva coluna/confirmação e refresh não repeteDELETE", async (status, mensagem) => {
    let versao = 0;
    const { user, fetchMock } = preparar((_url, request) => request.method === "GET"
        ? json(snapshot([a, b], { versao })) : status === 0 ? Promise.reject(new TypeError("offline")) : json({ erro: mensagem }, status));
    await abrirEditor(user, "Remover etapa 1: Planejamento");
    await ativarPorTeclado(user, screen.getByRole("button", { name: "Remover", exact: true }));
    expect((await screen.findByRole("alert")).textContent).toBe(mensagem);
    expect(screen.getByRole("region", { name: "Planejamento" })).not.toBeNull();
    expect(screen.getByRole("dialog", { name: "Remover Planejamento?" })).not.toBeNull();
    versao = 1;
    await ativarPorTeclado(user, screen.getByRole("button", { name: "Atualizar quadro" }));
    expect(fetchMock.mock.calls.map(([, request]) => request.method)).toEqual(["GET", "DELETE", "GET"]);
    await ativarPorTeclado(user, screen.getByRole("button", { name: "Cancelar" }));
    expect(screen.queryByRole("dialog")).toBeNull();
    expect(fetchMock.mock.calls.filter(([, request]) => request.method === "DELETE")).toHaveLength(1);
});

test("ETA-27/29/30: GET de atualização falho conserva quadro/rascunho e repetir consulta não reenviaPUT", async () => {
    let consultas = 0;
    const { user, fetchMock } = preparar((_url, request) => {
        if (request.method !== "GET") return json({ erro: "Gerenciamento alterado por outro usuário. Atualize e tente novamente." }, 409);
        consultas += 1;
        return consultas === 2 ? json({ erro: "Consulta indisponível" }, 500) : json(snapshot([a, b], { versao: consultas === 1 ? 0 : 8 }));
    });
    await abrirEditor(user, "Editar etapa 2: Execução");
    await preencher(user, "Rascunho", "Engenharia", "2");
    await ativarPorTeclado(user, screen.getByRole("button", { name: "Salvar etapa" }));
    await screen.findByRole("alert");
    await ativarPorTeclado(user, screen.getByRole("button", { name: "Atualizar quadro" }));
    await screen.findByText("Consulta indisponível");
    expect(screen.getByText("Consulta indisponível").getAttribute("role")).toBe("alert");
    expect(screen.getByLabelText("Nome da etapa").value).toBe("Rascunho");
    expect(screen.getByLabelText("Setor responsável").value).toBe("Engenharia");
    expect(screen.getByLabelText("Posição").value).toBe("2");
    expect(screen.getByRole("button", { name: "Salvar etapa" }).disabled).toBe(true);
    expect(screen.getByRole("region", { name: "Execução" })).not.toBeNull();
    expect(screen.queryByRole("heading", { name: "Nenhuma etapa de trabalho cadastrada" })).toBeNull();
    await ativarPorTeclado(user, screen.getByRole("button", { name: "Tentar novamente" }));
    await waitFor(() => expect(screen.queryByText("Consulta indisponível")).toBeNull());
    expect(screen.getByRole("button", { name: "Salvar etapa" }).disabled).toBe(false);
    expect(fetchMock.mock.calls.map(([, request]) => request.method)).toEqual(["GET", "PUT", "GET", "GET"]);
});

test("ETA-14/22/28: nomes iguais mantêm setores/posições e editar escolhe ID independente", async () => {
    const repetida = { ...b, nome: "Planejamento" };
    const { user, fetchMock } = preparar((_url, request) => request.method === "GET"
        ? json(snapshot([a, repetida])) : json(snapshot([a, { ...repetida, nome: "Revisão" }], { versao: 1 })));
    const colunas = await screen.findAllByRole("region", { name: "Planejamento" });
    expect(colunas).toHaveLength(2);
    expect(within(colunas[0]).getByText("Setor responsável: Técnico").textContent).toBe("Setor responsável: Técnico");
    expect(within(colunas[0]).getByText("Posição 1").textContent).toBe("Posição 1");
    expect(within(colunas[1]).getByText("Setor responsável: Campo").textContent).toBe("Setor responsável: Campo");
    expect(within(colunas[1]).getByText("Posição 2").textContent).toBe("Posição 2");
    await abrirEditor(user, "Editar etapa 2: Planejamento");
    await user.clear(screen.getByLabelText("Nome da etapa"));
    await user.type(screen.getByLabelText("Nome da etapa"), "Revisão");
    await ativarPorTeclado(user, screen.getByRole("button", { name: "Salvar etapa" }));
    await screen.findByRole("region", { name: "Revisão" });
    expect(screen.getAllByRole("region").map(item => within(item).getByRole("heading").textContent)).toEqual(["Planejamento", "Revisão", "Concluídos", "Cancelados"]);
    conferirFinais();
    expect(fetchMock.mock.calls.find(([, request]) => request.method === "PUT")[0]).toBe(`${endpoint}/etapas/5`);
});

test.each([{ arquivado: true }, { podeAdministrar: false }])("ETA-22/27/29: refresh aplica flags atuais sem perder rascunho%s", async flags => {
    let atualizado = false;
    const { user, fetchMock } = preparar((_url, request) => request.method === "GET"
        ? json(snapshot([a, b], atualizado ? flags : {}))
        : json({ erro: "Gerenciamento alterado por outro usuário. Atualize e tente novamente." }, 409));
    await abrirEditor(user);
    await preencher(user, "Rascunho", "Engenharia");
    await ativarPorTeclado(user, screen.getByRole("button", { name: "Salvar etapa" }));
    await screen.findByRole("alert");
    atualizado = true;
    await ativarPorTeclado(user, screen.getByRole("button", { name: "Atualizar quadro" }));
    await waitFor(() => expect(screen.queryByRole("button", { name: "+ Nova etapa" })).toBeNull());
    expect(screen.queryByRole("button", { name: /Editar etapa \d/ })).toBeNull();
    expect(screen.queryByRole("button", { name: /Remover etapa \d/ })).toBeNull();
    expect(screen.getByLabelText("Nome da etapa").value).toBe("Rascunho");
    expect(screen.getByLabelText("Nome da etapa").disabled).toBe(true);
    expect(screen.getByRole("button", { name: "Salvar etapa" }).disabled).toBe(true);
    expect(fetchMock.mock.calls.map(([, request]) => request.method)).toEqual(["GET", "POST", "GET"]);
});

test("ETA-02/23: ordens legadas7/7 editam posição visual sem escrever por consulta e sem inventar setor", async () => {
    const legado = [{ ...a, ordem: 7, setor: null }, { ...b, ordem: 7 }];
    const { user, fetchMock } = preparar(() => json(snapshot(legado)));
    await abrirEditor(user, "Editar etapa 2: Execução");
    expect(screen.getByLabelText("Posição").value).toBe("2");
    expect([...screen.getByLabelText("Posição").options].map(option => option.value)).toEqual(["1", "2"]);
    expect(screen.getByLabelText("Nome da etapa").value).toBe("Execução");
    await ativarPorTeclado(user, screen.getByRole("button", { name: "Cancelar" }));
    await abrirEditor(user, "Editar etapa 1: Planejamento");
    expect(screen.getByLabelText("Posição").value).toBe("1");
    expect(screen.getByLabelText("Setor responsável").value).toBe("");
    expect(fetchMock.mock.calls.map(([, request]) => request.method)).toEqual(["GET"]);
});

test("ETA-15/29: última etapa removida apresenta vazio sem GETextra", async () => {
    const { user, fetchMock } = preparar((_url, request) => json(request.method === "GET"
        ? snapshot([b]) : snapshot([], { versao: 1 })));
    await abrirEditor(user, "Remover etapa 1: Execução");
    await ativarPorTeclado(user, screen.getByRole("button", { name: "Remover", exact: true }));
    expect((await screen.findByRole("heading", { name: "Nenhuma etapa de trabalho cadastrada" })).textContent).toBe("Nenhuma etapa de trabalho cadastrada");
    expect(screen.getAllByRole("region").map(item => within(item).getByRole("heading").textContent)).toEqual(["Concluídos", "Cancelados"]);
    conferirFinais();
    expect(fetchMock.mock.calls.map(([, request]) => request.method)).toEqual(["GET", "DELETE"]);
});

test("ETA-28: voltar do quadro arquiva com versão retornada pelo CRUD sem recarregar lista", async () => {
    let registro = { ...quadro };
    const fetchMock = vi.fn((url, request) => {
        if (url.includes("?arquivado=")) return json(registro.arquivado ? [] : [registro]);
        if (url.endsWith("/estrutura-etapas")) return json(snapshot([b]));
        if (request.method === "GET") return json(registro);
        if (url.endsWith("/etapas/5")) {
            registro = { ...registro, versao: 1 };
            return json(snapshot([], { versao: 1 }));
        }
        if (url.endsWith("/arquivar")) {
            if (JSON.parse(request.body).versao !== 1) return json({ erro: "Versão antiga" }, 409);
            registro.arquivado = true;
            return new Response(null, { status: 204 });
        }
        throw new Error(`Requisição inesperada ${url}`);
    });
    vi.stubGlobal("fetch", fetchMock);
    localStorage.setItem("token", "sessao");
    const user = userEvent.setup();
    render(<Gerenciamentos />);
    await ativarPorTeclado(user, await screen.findByRole("button", { name: "Abrir Instalações" }));
    await abrirEditor(user, "Remover etapa 1: Execução");
    await ativarPorTeclado(user, screen.getByRole("button", { name: "Remover", exact: true }));
    await screen.findByRole("heading", { name: "Nenhuma etapa de trabalho cadastrada" });
    conferirFinais();
    await ativarPorTeclado(user, screen.getByRole("button", { name: /Voltar/ }));
    await ativarPorTeclado(user, screen.getByRole("button", { name: "Arquivar Instalações" }));
    await ativarPorTeclado(user, screen.getByRole("button", { name: "Arquivar", exact: true }));
    expect((await screen.findByRole("heading", { name: "Nenhum gerenciamento ativo" })).textContent).toBe("Nenhum gerenciamento ativo");
    const arquivo = fetchMock.mock.calls.find(([url]) => url.endsWith("/arquivar"));
    expect(JSON.parse(arquivo[1].body)).toEqual({ versao: 1 });
    expect(fetchMock.mock.calls.filter(([url]) => url.endsWith("/estrutura-etapas"))).toHaveLength(1);
    expect(fetchMock.mock.calls.filter(([url]) => url.includes("?arquivado="))).toHaveLength(2);
});

const respostasIlegiveis = ["<html>resposta incompleta</html>", '{"gerenciamento":'];
test.each([["POST", 201], ["PUT", 200], ["DELETE", 200]].flatMap(([metodo, status]) =>
    respostasIlegiveis.map(corpo => [metodo, status, corpo])))
    ("ETA-27/29: %s HTTP%s JSON ilegível%s conserva tela/cache e só reenvia manualmente apósGET", async (metodo, status, corpo) => {
        let consultas = 0;
        let mutacoes = 0;
        const rascunho = { id: 6, nome: "Rascunho", setor: "Engenharia", ordem: 2, quantidadeDemandas: 0 };
        const etapasConfirmadas = metodo === "DELETE" ? [a] : metodo === "PUT"
            ? [a, { ...b, nome: "Rascunho", setor: "Engenharia" }]
            : [a, rascunho, { ...b, ordem: 3 }];
        const { user, fetchMock, atualizar } = preparar((_url, request) => {
            if (request.method === "GET") {
                consultas += 1;
                return json(snapshot([a, b], { versao: consultas === 1 ? 0 : 7 }));
            }
            mutacoes += 1;
            return mutacoes === 1 ? new Response(corpo, { status, headers: { "Content-Type": "application/json" } })
                : json(snapshot(etapasConfirmadas, { versao: 8 }), status);
        });
        const remover = metodo === "DELETE";
        await abrirEditor(user, metodo === "POST" ? "+ Nova etapa" : `${remover ? "Remover" : "Editar"} etapa 2: Execução`);
        if (!remover) await preencher(user, "Rascunho", "Engenharia", "2");
        const acao = remover ? "Remover" : "Salvar etapa";
        await ativarPorTeclado(user, screen.getByRole("button", { name: acao, exact: true }));
        expect((await screen.findByRole("alert")).textContent)
            .toBe("Não foi possível confirmar a operação. Atualize a lista antes de tentar novamente.");
        expect(screen.getAllByRole("region").map(item => within(item).getByRole("heading").textContent))
            .toEqual(["Planejamento", "Execução", "Concluídos", "Cancelados"]);
        conferirFinais();
        expect(screen.getByText("Setor responsável: Técnico").textContent).toBe("Setor responsável: Técnico");
        expect(screen.getByText("Setor responsável: Campo").textContent).toBe("Setor responsável: Campo");
        expect(screen.getByText("2 demandas").textContent).toBe("2 demandas");
        expect(screen.getByText("0 demandas").textContent).toBe("0 demandas");
        expect(screen.queryByRole("heading", { name: "Nenhuma etapa de trabalho cadastrada" })).toBeNull();
        expect(atualizar.mock.calls.map(([registro]) => registro)).toEqual([quadro]);
        if (remover) expect(screen.getByRole("dialog", { name: "Remover Execução?" })).not.toBeNull();
        else {
            expect(screen.getByLabelText("Nome da etapa").value).toBe("Rascunho");
            expect(screen.getByLabelText("Setor responsável").value).toBe("Engenharia");
            expect(screen.getByLabelText("Posição").value).toBe("2");
        }
        expect(screen.getByRole("button", { name: acao, exact: true }).disabled).toBe(true);
        await user.click(screen.getByRole("button", { name: acao, exact: true }));
        expect(fetchMock.mock.calls.map(([, request]) => request.method)).toEqual(["GET", metodo]);
        const chamadasAntes = fetchMock.mock.calls.length;
        await ativarPorTeclado(user, screen.getByRole("button", { name: "Atualizar quadro" }));
        await waitFor(() => expect(atualizar).toHaveBeenCalledTimes(2));
        expect(fetchMock.mock.calls.slice(chamadasAntes).map(([url, request]) => [url, request.method]))
            .toEqual([[`${endpoint}/estrutura-etapas`, "GET"]]);
        expect(atualizar.mock.calls.map(([registro]) => registro)).toEqual([quadro, { ...quadro, versao: 7 }]);
        expect(fetchMock.mock.calls.filter(([, request]) => request.method === metodo)).toHaveLength(1);
        if (remover) expect(screen.getByRole("dialog", { name: "Remover Execução?" })).not.toBeNull();
        else {
            expect(screen.getByLabelText("Nome da etapa").value).toBe("Rascunho");
            expect(screen.getByLabelText("Setor responsável").value).toBe("Engenharia");
            expect(screen.getByLabelText("Posição").value).toBe("2");
        }
        expect(screen.getByRole("button", { name: acao, exact: true }).disabled).toBe(false);
        await ativarPorTeclado(user, screen.getByRole("button", { name: acao, exact: true }));
        await waitFor(() => expect(atualizar).toHaveBeenCalledTimes(3));
        expect(atualizar.mock.calls.map(([registro]) => registro)).toEqual([quadro, { ...quadro, versao: 7 }, { ...quadro, versao: 8 }]);
        expect(screen.queryByRole("alert")).toBeNull();
        expect(screen.queryByLabelText("Nome da etapa")).toBeNull();
        expect(screen.queryByRole("dialog")).toBeNull();
        expect(screen.getAllByRole("region").map(item => within(item).getByRole("heading").textContent))
            .toEqual(metodo === "DELETE" ? ["Planejamento", "Concluídos", "Cancelados"] : metodo === "PUT" ? ["Planejamento", "Rascunho", "Concluídos", "Cancelados"] : ["Planejamento", "Rascunho", "Execução", "Concluídos", "Cancelados"]);
        conferirFinais();
        const payload = remover ? {} : { nome: "Rascunho", setor: "Engenharia", ordem: 2 };
        const rota = metodo === "POST" ? `${endpoint}/etapas` : `${endpoint}/etapas/5`;
        expect(fetchMock.mock.calls.filter(([, request]) => request.method === metodo)
            .map(([url, request]) => [url, request.method, JSON.parse(request.body)]))
            .toEqual([[rota, metodo, { ...payload, versao: 0 }], [rota, metodo, { ...payload, versao: 7 }]]);
        expect(fetchMock.mock.calls.map(([, request]) => request.method)).toEqual(["GET", metodo, "GET", metodo]);
    });

test.each([{ arquivado: false, podeAdministrar: true }, { arquivado: true }, { podeAdministrar: false }])
    ("FIN-15/17/19: quadro somente com finais reais mantém identificação, contagens e proteção%s", async flags => {
        const { fetchMock } = preparar(() => json(snapshot([], flags)));
        expect((await screen.findByRole("heading", { name: "Nenhuma etapa de trabalho cadastrada" })).textContent)
            .toBe("Nenhuma etapa de trabalho cadastrada");
        expect(screen.getAllByRole("region").map(item => within(item).getByRole("heading").textContent))
            .toEqual(["Concluídos", "Cancelados"]);
        conferirFinais();
        expect(screen.queryByRole("button", { name: /Editar etapa|Remover etapa/ })).toBeNull();
        expect(screen.queryByRole("button", { name: "+ Nova etapa" }) !== null)
            .toBe(flags.arquivado !== true && flags.podeAdministrar !== false);
        expect(fetchMock.mock.calls.map(([, request]) => request.method)).toEqual(["GET"]);
    });

test("FIN-16/17: criar no quadro só com finais oferece posição1 e mantém Nome/Setor obrigatórios", async () => {
    const { user, fetchMock } = preparar(() => json(snapshot([])));
    await abrirEditor(user);
    expect([...screen.getByLabelText("Posição").options].map(option => option.value)).toEqual(["1"]);
    expect(screen.getByLabelText("Posição").value).toBe("1");
    await ativarPorTeclado(user, screen.getByRole("button", { name: "Salvar etapa" }));
    expect(screen.getByRole("alert").textContent).toBe("Informe um nome de etapa entre 1 e 255 caracteres.");
    await user.type(screen.getByLabelText("Nome da etapa"), "Trabalho");
    await ativarPorTeclado(user, screen.getByRole("button", { name: "Salvar etapa" }));
    expect(screen.getByRole("alert").textContent).toBe("Informe um setor entre 1 e 255 caracteres.");
    expect(fetchMock.mock.calls.map(([, request]) => request.method)).toEqual(["GET"]);
    conferirFinais();
});

test("FIN-15: finais vazias também permanecem protegidas", async () => {
    const vazias = finais.map((etapa, index) => ({ ...etapa, ordem: index + 1, quantidadeDemandas: 0 }));
    const { fetchMock } = preparar(() => json(snapshot(vazias, {}, false)));
    await screen.findByRole("region", { name: "Concluídos" });
    conferirFinais(vazias);
    expect(screen.getAllByRole("region")).toHaveLength(2);
    expect(fetchMock.mock.calls.map(([, request]) => request.method)).toEqual(["GET"]);
});

test("FIN-16: finais não contam nos intervalos de criação/edição nem nas posições de trabalho", async () => {
    const { user, fetchMock } = preparar();
    await abrirEditor(user);
    expect([...screen.getByLabelText("Posição").options].map(option => option.value)).toEqual(["1", "2", "3"]);
    expect(screen.getByLabelText("Posição").value).toBe("3");
    await ativarPorTeclado(user, screen.getByRole("button", { name: "Cancelar" }));
    await abrirEditor(user, "Editar etapa 2: Execução");
    expect([...screen.getByLabelText("Posição").options].map(option => option.value)).toEqual(["1", "2"]);
    expect(screen.getByLabelText("Posição").value).toBe("2");
    expect(within(screen.getByRole("region", { name: "Planejamento" })).getByText("Posição 1").textContent).toBe("Posição 1");
    expect(within(screen.getByRole("region", { name: "Execução" })).getByText("Posição 2").textContent).toBe("Posição 2");
    conferirFinais();
    expect(fetchMock.mock.calls.map(([, request]) => request.method)).toEqual(["GET"]);
});

test("FIN-14/15/16: trabalhos homônimos mantêm edição/setor/posição ao lado das finais oficiais", async () => {
    const trabalhos = [{ ...a, nome: "Concluídos" }, { ...b, nome: "Cancelados", categoria: null }];
    const { user, fetchMock } = preparar(() => json(snapshot(trabalhos)));
    await screen.findByRole("button", { name: "Editar etapa 1: Concluídos" });
    expect(screen.getAllByRole("region").map(item => within(item).getByRole("heading").textContent))
        .toEqual(["Concluídos", "Cancelados", "Concluídos", "Cancelados"]);
    for (const [index, etapa] of trabalhos.entries()) {
        const coluna = document.getElementById(`etapa-titulo-${etapa.id}`).closest("section");
        expect(within(coluna).queryByText("Etapa final obrigatória")).toBeNull();
        expect(within(coluna).getByText(`Setor responsável: ${etapa.setor}`).textContent).toBe(`Setor responsável: ${etapa.setor}`);
        expect(within(coluna).getByText(`Posição ${index + 1}`).textContent).toBe(`Posição ${index + 1}`);
        expect(within(coluna).getByRole("button", { name: `Remover etapa ${index + 1}: ${etapa.nome}` })).not.toBeNull();
        await abrirEditor(user, `Editar etapa ${index + 1}: ${etapa.nome}`);
        expect(screen.getByLabelText("Nome da etapa").value).toBe(etapa.nome);
        expect(screen.getByLabelText("Setor responsável").value).toBe(etapa.setor);
        expect([...screen.getByLabelText("Posição").options].map(option => option.value)).toEqual(["1", "2"]);
        await ativarPorTeclado(user, screen.getByRole("button", { name: "Cancelar" }));
    }
    for (const etapa of finais) {
        const coluna = document.getElementById(`etapa-titulo-${etapa.id}`).closest("section");
        expect(within(coluna).getByText("Etapa final obrigatória").textContent).toBe("Etapa final obrigatória");
        expect(within(coluna).queryByRole("button")).toBeNull();
    }
    expect(fetchMock.mock.calls.map(([, request]) => request.method)).toEqual(["GET"]);
});

test("FIN-15/29: consulta de legado não inventa finais nem classifica trabalho pelo nome", async () => {
    const legado = [{ ...a, nome: "Concluídos", categoria: undefined }, { ...b, nome: "Cancelados", categoria: null }];
    const { fetchMock } = preparar(() => json(snapshot(legado, {}, false)));
    await screen.findByRole("button", { name: "Editar etapa 1: Concluídos" });
    expect(screen.getAllByRole("region").map(item => within(item).getByRole("heading").id)).toEqual(["etapa-titulo-4", "etapa-titulo-5"]);
    expect(screen.queryByText("Etapa final obrigatória")).toBeNull();
    expect(screen.getByRole("button", { name: "Remover etapa 2: Cancelados" })).not.toBeNull();
    expect(screen.getByText("Setor responsável: Campo").textContent).toBe("Setor responsável: Campo");
    expect(fetchMock.mock.calls.map(([, request]) => request.method)).toEqual(["GET"]);
});

test.each(["PUT", "DELETE"].flatMap(metodo => ["CONCLUIDA", "CANCELADA"].map(categoria => [metodo, categoria])))
    ("FIN-15/19: alvo em cache de%s que passa a%s no refresh não permite nova alteração", async (metodo, categoria) => {
        let consultas = 0;
        const finalAtual = { ...finais.find(etapa => etapa.categoria === categoria), id: b.id };
        const outraFinal = finais.find(etapa => etapa.categoria !== categoria);
        const { user, fetchMock } = preparar((_url, request) => {
            if (request.method !== "GET") return json({ erro: "Gerenciamento alterado por outro usuário. Atualize e tente novamente." }, 409);
            consultas += 1;
            return json(consultas === 1 ? snapshot() : snapshot([a, finalAtual, outraFinal], { versao: 7 }, false));
        });
        await abrirEditor(user, `${metodo === "PUT" ? "Editar" : "Remover"} etapa 2: Execução`);
        const acao = metodo === "PUT" ? "Salvar etapa" : "Remover";
        await ativarPorTeclado(user, screen.getByRole("button", { name: acao, exact: true }));
        await screen.findByRole("alert");
        await ativarPorTeclado(user, screen.getByRole("button", { name: "Atualizar quadro" }));
        await screen.findByRole("region", { name: finalAtual.nome });
        expect(screen.getByRole("button", { name: acao, exact: true }).disabled).toBe(true);
        if (metodo === "PUT") {
            expect(screen.getByLabelText("Posição").disabled).toBe(true);
            fireEvent.submit(screen.getByRole("button", { name: acao }).closest("form"));
        } else fireEvent.click(screen.getByRole("button", { name: acao, exact: true }));
        expect(fetchMock.mock.calls.map(([, request]) => request.method)).toEqual(["GET", metodo, "GET"]);
        const coluna = screen.getByRole("region", { name: finalAtual.nome });
        expect(within(coluna).getByText("Etapa final obrigatória").textContent).toBe("Etapa final obrigatória");
        expect(within(coluna).queryByText(/Setor responsável:|^Posição /)).toBeNull();
        expect(within(coluna).queryByRole("button")).toBeNull();
        await ativarPorTeclado(user, screen.getByRole("button", { name: "Cancelar" }));
        expect(document.activeElement).toBe(screen.getByRole("button", { name: "+ Nova etapa" }));
    });
