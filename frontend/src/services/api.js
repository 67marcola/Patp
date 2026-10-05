const API_URL = import.meta.env.VITE_API_URL || "http://localhost:8081/api";
const ERRO_COMUNICACAO = "Não foi possível confirmar a operação. Atualize a lista antes de tentar novamente.";

export class ApiError extends Error {
    constructor(message, status) {
        super(message);
        this.name = "ApiError";
        this.status = status;
    }
}

async function requisicao(caminho, { token, method = "GET", dados, signal } = {}) {
    const headers = {};
    if (token) headers.Authorization = `Bearer ${token}`;
    if (dados !== undefined) headers["Content-Type"] = "application/json";

    let resposta;
    try {
        resposta = await fetch(`${API_URL}${caminho}`, {
            method, headers, signal,
            ...(dados === undefined ? {} : { body: JSON.stringify(dados) })
        });
    } catch (error) {
        if (error.name === "AbortError") throw error;
        throw new ApiError(ERRO_COMUNICACAO, 0);
    }

    if (resposta.status === 204) return;
    let corpo;
    try {
        corpo = await resposta.json();
    } catch {
        if (resposta.ok) throw new ApiError(ERRO_COMUNICACAO, resposta.status);
    }
    if (!resposta.ok) {
        throw new ApiError(corpo?.erro || "Não foi possível concluir a operação.", resposta.status);
    }
    return corpo;
}

async function autenticar(caminho, dados) {
    const resposta = await requisicao(caminho, { method: "POST", dados });
    if (!resposta || typeof resposta !== "object" || Array.isArray(resposta)
        || typeof resposta.token !== "string" || !resposta.token.trim()
        || !Number.isInteger(resposta.id) || resposta.id <= 0
        || typeof resposta.nome !== "string" || typeof resposta.email !== "string"
        || (resposta.setor !== null && typeof resposta.setor !== "string")) {
        throw new ApiError(ERRO_COMUNICACAO, 200);
    }
    return resposta;
}

export function login(email, senha) {
    return autenticar("/usuarios/login", { email, senha });
}

export function cadastrarUsuario(usuario) {
    return autenticar("/usuarios/cadastro", usuario);
}

export function buscarUsuarioLogado(token) {
    return requisicao("/usuarios/me", { token });
}

export function listarGerenciamentos(token, arquivado = false, options = {}) {
    return requisicao(`/gerenciamentos?arquivado=${arquivado}`, { token, signal: options.signal });
}

export function buscarGerenciamento(token, id, options = {}) {
    return requisicao(`/gerenciamentos/${id}`, { token, signal: options.signal });
}

export function criarGerenciamento(token, gerenciamento) {
    return requisicao("/gerenciamentos", { token, method: "POST", dados: gerenciamento });
}

export function editarGerenciamento(token, id, dados) {
    return requisicao(`/gerenciamentos/${id}`, { token, method: "PUT", dados });
}

export function arquivarGerenciamento(token, id, versao) {
    return requisicao(`/gerenciamentos/${id}/arquivar`, { token, method: "PUT", dados: { versao } });
}

export function restaurarGerenciamento(token, id, versao) {
    return requisicao(`/gerenciamentos/${id}/restaurar`, { token, method: "PUT", dados: { versao } });
}

export function listarEtapas(token, id, options = {}) {
    return requisicao(`/gerenciamentos/${id}/etapas`, { token, signal: options.signal });
}

export function buscarConfiguracaoEtapas(token, id, options = {}) {
    return requisicao(`/gerenciamentos/${id}/estrutura-etapas`, { token, signal: options.signal });
}

export function criarEtapa(token, id, dados) {
    return requisicao(`/gerenciamentos/${id}/etapas`, { token, method: "POST", dados });
}

export function editarEtapa(token, id, etapaId, dados) {
    return requisicao(`/gerenciamentos/${id}/etapas/${etapaId}`, { token, method: "PUT", dados });
}

export function removerEtapa(token, id, etapaId, versao) {
    return requisicao(`/gerenciamentos/${id}/etapas/${etapaId}`, { token, method: "DELETE", dados: { versao } });
}
