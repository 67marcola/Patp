import { render, screen, waitFor, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { expect, test, vi } from "vitest";
import Gerenciamentos from "./Gerenciamentos";

const quadro = { id: 9, nome: "Instalações", descricao: "Postes", criador: { id: 3, nome: "Ana" }, arquivado: false, versao: 2, podeAdministrar: true };
const json = (dados, status = 200) => new Response(JSON.stringify(dados), { status });
const vazio = () => new Response(null, { status: 204 });

function preparar(handler) {
    const fetchMock = vi.fn(handler);
    vi.stubGlobal("fetch", fetchMock);
    localStorage.setItem("token", "sessao");
    const user = userEvent.setup();
    render(<Gerenciamentos />);
    return { user, fetchMock };
}

function servidor(registros = [quadro]) {
    const dados = registros.map(item => ({ ...item }));
    return async (url, request = {}) => {
        if (url.includes("?arquivado=")) return json(dados.filter(item => item.arquivado === url.endsWith("=true")));
        if (url.endsWith("/etapas")) return json([]);
        const id = Number(url.match(/gerenciamentos\/(\d+)/)?.[1]);
        const item = dados.find(registro => registro.id === id);
        if (request.method === "GET" && item) return json(item);
        if (request.method === "PUT" && item) {
            const body = JSON.parse(request.body);
            if (url.endsWith("/arquivar") || url.endsWith("/restaurar")) {
                item.arquivado = url.endsWith("/arquivar");
                item.versao += 1;
                return vazio();
            }
            Object.assign(item, { nome: body.nome, descricao: body.descricao, versao: item.versao + 1 });
            return json(item);
        }
        throw new Error(`Request não esperada: ${request.method} ${url}`);
    };
}

test("GER-28/36: inicia em Ativos e distingue carregamento da lista vazia", async () => {
    let resolver;
    const { fetchMock } = preparar(() => new Promise(resolve => { resolver = resolve; }));
    expect(screen.getByRole("status").textContent).toBe("Carregando gerenciamentos...");
    expect(screen.getByRole("button", { name: "Ativos", exact: true }).getAttribute("aria-pressed")).toBe("true");
    expect(fetchMock.mock.calls[0][0]).toBe("http://localhost:8081/api/gerenciamentos?arquivado=false");
    resolver(json([]));
    expect((await screen.findByRole("heading", { name: "Nenhum gerenciamento ativo" })).textContent).toBe("Nenhum gerenciamento ativo");
    expect(screen.queryByRole("status")).toBeNull();
});

test("GER-28: abrir arquivado consulta por ID e voltar mantém filtro", async () => {
    const { user, fetchMock } = preparar(servidor([{ ...quadro, arquivado: true }]));
    await screen.findByRole("heading", { name: "Nenhum gerenciamento ativo" });
    await user.click(screen.getByRole("button", { name: "Arquivados", exact: true }));
    await user.click(await screen.findByRole("button", { name: "Abrir Instalações" }));
    expect((await screen.findByRole("heading", { name: "Instalações" })).textContent).toBe("Instalações");
    expect(fetchMock.mock.calls.some(([url]) => url === "http://localhost:8081/api/gerenciamentos/9")).toBe(true);
    await user.click(screen.getByRole("button", { name: /Voltar/ }));
    expect(screen.getByRole("button", { name: "Arquivados", exact: true }).getAttribute("aria-pressed")).toBe("true");
    expect(await screen.findByRole("button", { name: "Restaurar Instalações" })).not.toBeNull();
});

test.each([
    ["criador", quadro, true],
    ["administrador em legado", { ...quadro, criador: null }, true],
    ["terceiro", { ...quadro, podeAdministrar: false }, false],
    ["funcionário em legado", { ...quadro, criador: null, podeAdministrar: false }, false]
])("GER-30: ações de ativo respeitam a permissão de %s", async (_papel, registro, permitido) => {
    preparar(servidor([registro]));
    await screen.findByRole("button", { name: "Abrir Instalações" });
    expect(screen.queryByRole("button", { name: "Editar Instalações" }) !== null).toBe(permitido);
    expect(screen.queryByRole("button", { name: "Arquivar Instalações" }) !== null).toBe(permitido);
    expect(screen.queryByRole("button", { name: "Restaurar Instalações" })).toBeNull();
});

test.each([true, false])("GER-30: arquivado oferece apenas restauração quando permitido=%s", async permitido => {
    const { user } = preparar(servidor([{ ...quadro, arquivado: true, podeAdministrar: permitido }]));
    await screen.findByRole("heading", { name: "Nenhum gerenciamento ativo" });
    await user.click(screen.getByRole("button", { name: "Arquivados", exact: true }));
    await screen.findByRole("button", { name: "Abrir Instalações" });
    expect(screen.queryByRole("button", { name: "Restaurar Instalações" }) !== null).toBe(permitido);
    expect(screen.queryByRole("button", { name: "Editar Instalações" })).toBeNull();
    expect(screen.queryByRole("button", { name: "Arquivar Instalações" })).toBeNull();
});

test("GER-31/37/38: confirmação identifica quadro e preservar dados; cancelar devolve foco sem PUT", async () => {
    const { user, fetchMock } = preparar(servidor());
    const origem = await screen.findByRole("button", { name: "Arquivar Instalações" });
    origem.focus();
    await user.keyboard("{Enter}");
    const dialogo = screen.getByRole("dialog", { name: "Arquivar Instalações?" });
    expect(within(dialogo).getByText(/Os dados e vínculos serão preservados/).textContent).toContain("Os dados e vínculos serão preservados");
    expect(document.activeElement).toBe(within(dialogo).getByRole("button", { name: "Cancelar" }));
    await user.keyboard("{Enter}");
    expect(screen.queryByRole("dialog")).toBeNull();
    expect(document.activeElement).toBe(origem);
    expect(fetchMock.mock.calls.filter(([, request]) => request.method === "PUT")).toHaveLength(0);
});

test("GER-41: arquivar remove último cartão e restaura no filtro mantido", async () => {
    const { user } = preparar(servidor());
    await user.click(await screen.findByRole("button", { name: "Arquivar Instalações" }));
    await user.click(within(screen.getByRole("dialog")).getByRole("button", { name: "Arquivar", exact: true }));
    expect((await screen.findByRole("heading", { name: "Nenhum gerenciamento ativo" })).textContent).toBe("Nenhum gerenciamento ativo");
    expect(screen.getByRole("button", { name: "Ativos", exact: true }).getAttribute("aria-pressed")).toBe("true");
    await user.click(screen.getByRole("button", { name: "Arquivados", exact: true }));
    await user.click(await screen.findByRole("button", { name: "Restaurar Instalações" }));
    expect((await screen.findByRole("heading", { name: "Nenhum gerenciamento arquivado" })).textContent).toBe("Nenhum gerenciamento arquivado");
    expect(screen.getByRole("button", { name: "Arquivados", exact: true }).getAttribute("aria-pressed")).toBe("true");
});

test("GER-32: duplo clique na confirmação não duplica arquivamento pendente", async () => {
    let resolver;
    const base = servidor();
    const { user, fetchMock } = preparar((url, request) => request.method === "PUT"
        ? new Promise(resolve => { resolver = resolve; }) : base(url, request));
    await user.click(await screen.findByRole("button", { name: "Arquivar Instalações" }));
    await user.dblClick(within(screen.getByRole("dialog")).getByRole("button", { name: "Arquivar", exact: true }));
    expect(fetchMock.mock.calls.filter(([, request]) => request.method === "PUT")).toHaveLength(1);
    expect(within(screen.getByRole("dialog")).getByRole("button", { name: "Arquivando..." }).disabled).toBe(true);
    resolver(vazio());
    await waitFor(() => expect(screen.queryByRole("dialog")).toBeNull());
});

test("GER-33/40: arquivamento negado conserva o cartão e mostra erro exato", async () => {
    const base = servidor();
    const mensagem = "Você não tem permissão para administrar este gerenciamento.";
    const { user } = preparar((url, request) => request.method === "PUT" ? json({ erro: mensagem }, 403) : base(url, request));
    await user.click(await screen.findByRole("button", { name: "Arquivar Instalações" }));
    await user.click(within(screen.getByRole("dialog")).getByRole("button", { name: "Arquivar", exact: true }));
    expect((await screen.findByRole("alert")).textContent).toBe(mensagem);
    expect(screen.getByRole("button", { name: "Abrir Instalações" })).not.toBeNull();
});

test("GER-34: sucesso seguido de GET falho mostra aviso e repete somente consulta", async () => {
    const base = servidor();
    let gravou = false;
    let falhar = true;
    const { user, fetchMock } = preparar(async (url, request) => {
        if (request.method === "PUT") { gravou = true; return base(url, request); }
        if (gravou && url.includes("?arquivado=") && falhar) return json({ erro: "Indisponível" }, 500);
        return base(url, request);
    });
    await user.click(await screen.findByRole("button", { name: "Arquivar Instalações" }));
    await user.click(within(screen.getByRole("dialog")).getByRole("button", { name: "Arquivar", exact: true }));
    expect((await screen.findByRole("alert")).textContent).toBe("Alteração salva; não foi possível atualizar a lista.");
    expect(screen.queryByRole("button", { name: "Abrir Instalações" })).toBeNull();
    falhar = false;
    await user.click(screen.getByRole("button", { name: "Tentar atualizar" }));
    expect((await screen.findByRole("heading", { name: "Nenhum gerenciamento ativo" })).textContent).toBe("Nenhum gerenciamento ativo");
    expect(fetchMock.mock.calls.filter(([, request]) => request.method === "PUT")).toHaveLength(1);
    expect(screen.queryByRole("alert")).toBeNull();
});

test("GER-36/40: falha inicial tem alerta e repetir consulta não envia mutação", async () => {
    let falhar = true;
    const base = servidor();
    const { user, fetchMock } = preparar((url, request) => falhar ? json({ erro: "Consulta indisponível" }, 500) : base(url, request));
    expect((await screen.findByRole("alert")).textContent).toBe("Consulta indisponível");
    expect(screen.queryByRole("heading", { name: /Nenhum gerenciamento/ })).toBeNull();
    falhar = false;
    await user.click(screen.getByRole("button", { name: "Tentar atualizar" }));
    expect(await screen.findByRole("button", { name: "Abrir Instalações" })).not.toBeNull();
    expect(fetchMock.mock.calls.every(([, request]) => request.method === "GET")).toBe(true);
});

test("GER-28/36: consulta antiga não substitui o filtro já selecionado", async () => {
    let resolverAtivos;
    const { user } = preparar(url => url.endsWith("=false")
        ? new Promise(resolve => { resolverAtivos = resolve; })
        : json([{ ...quadro, nome: "Arquivado", arquivado: true }]));
    await user.click(screen.getByRole("button", { name: "Arquivados", exact: true }));
    await screen.findByRole("button", { name: "Abrir Arquivado" });
    resolverAtivos(json([quadro]));
    await waitFor(() => expect(screen.queryByRole("button", { name: "Abrir Instalações" })).toBeNull());
    expect(screen.getByRole("button", { name: "Abrir Arquivado" })).not.toBeNull();
    expect(screen.getByRole("button", { name: "Arquivados", exact: true }).getAttribute("aria-pressed")).toBe("true");
});

test("GER-35: edição busca detalhe fresco e lista/quadro mostram os valores salvos", async () => {
    const base = servidor([{ ...quadro, versao: 7 }]);
    const { user, fetchMock } = preparar(base);
    await user.click(await screen.findByRole("button", { name: "Editar Instalações" }));
    await user.clear(await screen.findByLabelText("Nome do gerenciamento"));
    await user.type(screen.getByLabelText("Nome do gerenciamento"), "Corrigido");
    await user.click(screen.getByRole("button", { name: "Salvar alterações" }));
    await user.click(await screen.findByRole("button", { name: "Abrir Corrigido" }));
    expect((await screen.findByRole("heading", { name: "Corrigido" })).textContent).toBe("Corrigido");
    expect(screen.getByText("Postes").textContent).toBe("Postes");
    const put = fetchMock.mock.calls.find(([, request]) => request.method === "PUT");
    expect(JSON.parse(put[1].body)).toEqual({ nome: "Corrigido", descricao: "Postes", versao: 7 });
});

test("GER-34: edição salva seguida de falha na lista retorna com aviso visível", async () => {
    const base = servidor();
    let salvo = false;
    const { user, fetchMock } = preparar(async (url, request) => {
        if (request.method === "PUT") { salvo = true; return base(url, request); }
        if (salvo && url.includes("?arquivado=")) return json({ erro: "Indisponível" }, 500);
        return base(url, request);
    });
    await user.click(await screen.findByRole("button", { name: "Editar Instalações" }));
    await user.click(await screen.findByRole("button", { name: "Salvar alterações" }));
    expect((await screen.findByRole("alert")).textContent).toBe("Alteração salva; não foi possível atualizar a lista.");
    expect(screen.getByRole("button", { name: "Tentar atualizar" })).not.toBeNull();
    expect(fetchMock.mock.calls.filter(([, request]) => request.method === "PUT")).toHaveLength(1);
});

test("GER-30/33: permissão removida na consulta fresca impede abrir edição", async () => {
    const base = servidor();
    const { user } = preparar((url, request) => url.endsWith("/9")
        ? json({ ...quadro, podeAdministrar: false }) : base(url, request));
    await user.click(await screen.findByRole("button", { name: "Editar Instalações" }));
    expect((await screen.findByRole("alert")).textContent).toBe("Você não tem permissão para administrar este gerenciamento.");
    expect(screen.queryByLabelText("Nome do gerenciamento")).toBeNull();
});

test("GER-30: consulta fresca atualiza também os botões da lista", async () => {
    const base = servidor();
    const { user } = preparar((url, request) => url.endsWith("/9")
        ? json({ ...quadro, podeAdministrar: false }) : base(url, request));
    await user.click(await screen.findByRole("button", { name: "Editar Instalações" }));
    await screen.findByRole("alert");
    expect(screen.queryByRole("button", { name: "Editar Instalações" })).toBeNull();
    expect(screen.queryByRole("button", { name: "Arquivar Instalações" })).toBeNull();
    expect(screen.getByRole("button", { name: "Abrir Instalações" })).not.toBeNull();
});

test.each([
    [409, "Gerenciamento alterado por outro usuário. Atualize e tente novamente."],
    [0, "Não foi possível confirmar a operação. Atualize a lista antes de tentar novamente."]
])("GER-33/40: falha %s na mudança de estado mantém quadro e uma única tentativa", async (status, mensagem) => {
    const base = servidor();
    const { user, fetchMock } = preparar((url, request) => {
        if (request.method !== "PUT") return base(url, request);
        return status === 0 ? Promise.reject(new TypeError("offline")) : json({ erro: mensagem }, status);
    });
    await user.click(await screen.findByRole("button", { name: "Arquivar Instalações" }));
    await user.click(within(screen.getByRole("dialog")).getByRole("button", { name: "Arquivar", exact: true }));
    expect((await screen.findByRole("alert")).textContent).toBe(mensagem);
    expect(screen.getByRole("button", { name: "Abrir Instalações" })).not.toBeNull();
    expect(fetchMock.mock.calls.filter(([, request]) => request.method === "PUT")).toHaveLength(1);
});

test("GER-11/33: quadro removido antes de abrir anuncia 404 sem apresentar edição", async () => {
    const base = servidor();
    const { user, fetchMock } = preparar((url, request) => url.endsWith("/9")
        ? json({ erro: "Gerenciamento não encontrado." }, 404) : base(url, request));
    await user.click(await screen.findByRole("button", { name: "Editar Instalações" }));
    expect((await screen.findByRole("alert")).textContent).toBe("Gerenciamento não encontrado.");
    expect(screen.queryByLabelText("Nome do gerenciamento")).toBeNull();
    expect(fetchMock.mock.calls.every(([, request]) => request.method === "GET")).toBe(true);
});

test("GER-33/34: formulário salvo permanece pendente até encerrar a consulta posterior", async () => {
    const base = servidor();
    let salvo = false;
    let resolverConsulta;
    const { user, fetchMock } = preparar(async (url, request) => {
        if (request.method === "PUT") { salvo = true; return base(url, request); }
        if (salvo && url.includes("?arquivado=")) return new Promise(resolve => { resolverConsulta = resolve; });
        return base(url, request);
    });
    await user.click(await screen.findByRole("button", { name: "Editar Instalações" }));
    await user.clear(await screen.findByLabelText("Nome do gerenciamento"));
    await user.type(screen.getByLabelText("Nome do gerenciamento"), "Corrigido");
    await user.click(screen.getByRole("button", { name: "Salvar alterações" }));
    await waitFor(() => expect(fetchMock.mock.calls.filter(([url]) => url.includes("?arquivado="))).toHaveLength(2));
    expect(screen.getByLabelText("Nome do gerenciamento").value).toBe("Corrigido");
    expect(screen.getByRole("button", { name: "Salvando..." }).disabled).toBe(true);
    expect(screen.queryByRole("button", { name: /Criar gerenciamento/ })).toBeNull();
    resolverConsulta(json({ erro: "Indisponível" }, 500));
    expect((await screen.findByRole("alert")).textContent).toBe("Alteração salva; não foi possível atualizar a lista.");
    expect(screen.getByRole("button", { name: "Tentar atualizar" })).not.toBeNull();
});

test.each(["criar", "abrir"])("GER-31: navegar para %s descarta confirmação antiga e restaura foco da nova", async destino => {
    const { user, fetchMock } = preparar(servidor());
    await user.click(await screen.findByRole("button", { name: "Arquivar Instalações" }));
    if (destino === "criar") {
        await user.click(screen.getByRole("button", { name: /Criar gerenciamento/ }));
        await user.click(await screen.findByRole("button", { name: "Cancelar" }));
    } else {
        await user.click(screen.getByRole("button", { name: "Abrir Instalações" }));
        await user.click(await screen.findByRole("button", { name: /Voltar/ }));
    }
    expect(screen.queryByRole("dialog")).toBeNull();
    const origem = screen.getByRole("button", { name: "Arquivar Instalações" });
    await user.click(origem);
    await user.click(within(screen.getByRole("dialog")).getByRole("button", { name: "Cancelar" }));
    expect(document.activeElement).toBe(origem);
    expect(origem.isConnected).toBe(true);
    expect(fetchMock.mock.calls.filter(([, request]) => request.method === "PUT")).toHaveLength(0);
});
