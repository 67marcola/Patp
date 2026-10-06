const API_URL = import.meta.env.VITE_API_URL || "http://localhost:8081/api";
const ERRO_COMUNICACAO = "Não foi possível confirmar a operação. Atualize a lista antes de tentar novamente.";

export class ApiError extends Error {
    constructor(message, status) {
        super(message);
        this.name = "ApiError";
        this.status = status;
    }
}

async function requisicao(caminho, { token, method = "GET", dados, signal, validar } = {}) {
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

    if (resposta.status === 204) {
        if (validar) throw new ApiError(ERRO_COMUNICACAO, resposta.status);
        return;
    }
    let corpo;
    try {
        corpo = await resposta.json();
    } catch {
        if (resposta.ok) throw new ApiError(ERRO_COMUNICACAO, resposta.status);
    }
    if (!resposta.ok) {
        throw new ApiError(corpo?.erro || "Não foi possível concluir a operação.", resposta.status);
    }
    if (validar && !validar(corpo)) throw new ApiError(ERRO_COMUNICACAO, resposta.status);
    return corpo;
}

const CAMPOS_DEMANDA = ["id", "numeroProcesso", "pessoa", "responsavel", "status", "prioridade",
    "dataEmissao", "prazoEtapa", "prazoGeral", "dataConclusao", "dataCancelamento", "motivoCancelamento",
    "observacoes", "etapaId"];
const TEXTOS_OPCIONAIS = ["responsavel", "status", "prioridade", "motivoCancelamento", "observacoes"];
const DATAS_DEMANDA = ["dataEmissao", "prazoEtapa", "prazoGeral", "dataConclusao", "dataCancelamento"];
const objeto = valor => valor !== null && typeof valor === "object" && !Array.isArray(valor);
const idValido = valor => Number.isSafeInteger(valor) && valor > 0;

function dataIso(valor) {
    if (valor === null) return true;
    if (typeof valor !== "string" || !/^\d{4}-\d{2}-\d{2}$/.test(valor)) return false;
    const [ano, mes, dia] = valor.split("-").map(Number);
    const bissexto = ano % 4 === 0 && (ano % 100 !== 0 || ano % 400 === 0);
    const dias = [31, bissexto ? 29 : 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31];
    return mes >= 1 && mes <= 12 && dia >= 1 && dia <= dias[mes - 1];
}

function snapshotValido(dados, id) {
    if (!objeto(dados) || !objeto(dados.gerenciamento) || dados.gerenciamento.id !== id
        || !idValido(dados.gerenciamento.id) || !Number.isSafeInteger(dados.gerenciamento.versao)
        || dados.gerenciamento.versao < 0 || typeof dados.gerenciamento.arquivado !== "boolean"
        || typeof dados.gerenciamento.podeAdministrar !== "boolean"
        || !Array.isArray(dados.etapas) || !Array.isArray(dados.demandas)) return false;
    const contagens = new Map();
    for (const etapa of dados.etapas) {
        if (!objeto(etapa) || !idValido(etapa.id) || contagens.has(etapa.id)
            || !Number.isSafeInteger(etapa.quantidadeDemandas) || etapa.quantidadeDemandas < 0) return false;
        contagens.set(etapa.id, 0);
    }
    const ids = new Set();
    for (const demanda of dados.demandas) {
        if (!objeto(demanda) || Object.keys(demanda).length !== CAMPOS_DEMANDA.length
            || !CAMPOS_DEMANDA.every(campo => Object.hasOwn(demanda, campo))
            || !idValido(demanda.id) || ids.has(demanda.id) || !idValido(demanda.etapaId)
            || !contagens.has(demanda.etapaId) || typeof demanda.numeroProcesso !== "string"
            || typeof demanda.pessoa !== "string"
            || !TEXTOS_OPCIONAIS.every(campo => demanda[campo] === null || typeof demanda[campo] === "string")
            || !DATAS_DEMANDA.every(campo => dataIso(demanda[campo]))) return false;
        ids.add(demanda.id);
        contagens.set(demanda.etapaId, contagens.get(demanda.etapaId) + 1);
    }
    return dados.etapas.every(etapa => etapa.quantidadeDemandas === contagens.get(etapa.id));
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
    return requisicao(`/gerenciamentos/${id}/estrutura-etapas`, { token, signal: options.signal,
        validar: dados => snapshotValido(dados, id) });
}

export function criarEtapa(token, id, dados) {
    return requisicao(`/gerenciamentos/${id}/etapas`, { token, method: "POST", dados,
        validar: resposta => snapshotValido(resposta, id) });
}

export function editarEtapa(token, id, etapaId, dados) {
    return requisicao(`/gerenciamentos/${id}/etapas/${etapaId}`, { token, method: "PUT", dados,
        validar: resposta => snapshotValido(resposta, id) });
}

export function removerEtapa(token, id, etapaId, versao) {
    return requisicao(`/gerenciamentos/${id}/etapas/${etapaId}`, { token, method: "DELETE", dados: { versao },
        validar: resposta => snapshotValido(resposta, id) });
}

export function criarDemanda(token, id, dados) {
    return requisicao(`/gerenciamentos/${id}/demandas`, { token, method: "POST", dados,
        validar: resposta => snapshotValido(resposta, id) });
}
