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
    if (validar && !validar(corpo, resposta.status)) throw new ApiError(ERRO_COMUNICACAO, resposta.status);
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

function cadastroConfirmado(resposta, id, enviados, status) {
    if (status !== 201 || !snapshotValido(resposta, id) || resposta.gerenciamento.arquivado
        || resposta.gerenciamento.versao !== enviados.versao + 1) return false;
    const trabalhos = resposta.etapas.filter(etapa => etapa.categoria === "TRABALHO");
    if (trabalhos.length === 0 || trabalhos.some(etapa => !Number.isSafeInteger(etapa.ordem))) return false;
    const primeira = trabalhos.sort((a, b) => a.ordem - b.ordem || a.id - b.id)[0];
    const campos = { dataEmissao: enviados.dataEmissao ?? null, prazoEtapa: enviados.prazoEtapa ?? null,
        prazoGeral: enviados.prazoGeral ?? null };
    for (const campo of ["numeroProcesso", "pessoa", "responsavel", "prioridade", "observacoes"]) {
        const valor = enviados[campo];
        campos[campo] = typeof valor === "string" ? valor.trim() || null : valor ?? null;
    }
    const correspondentes = resposta.demandas.filter(demanda => demanda.numeroProcesso === campos.numeroProcesso);
    if (correspondentes.length !== 1) return false;
    const demanda = correspondentes[0];
    return Object.entries(campos).every(([campo, valor]) => demanda[campo] === valor)
        && demanda.status === "Em andamento" && demanda.dataConclusao === null
        && demanda.dataCancelamento === null && demanda.motivoCancelamento === null && demanda.etapaId === primeira.id;
}

function transicaoConfirmada(resposta, id, anterior, acao, enviados, status) {
    if (status !== 200 || !snapshotValido(resposta, id) || resposta.gerenciamento.arquivado
        || !resposta.gerenciamento.podeAdministrar || resposta.gerenciamento.versao !== enviados.versao + 1) return false;
    const demanda = resposta.demandas.find(atual => atual.id === anterior.id);
    if (!demanda) return false;
    const preservados = ["numeroProcesso", "pessoa", "responsavel", "prioridade", "dataEmissao", "prazoEtapa", "prazoGeral", "observacoes"];
    if (!preservados.every(campo => demanda[campo] === anterior[campo])) return false;
    const etapa = resposta.etapas.find(atual => atual.id === demanda.etapaId);
    if (acao === "mover" || acao === "reabrir") {
        if (demanda.etapaId !== enviados.etapaId || etapa.categoria !== "TRABALHO" || demanda.status !== "Em andamento") return false;
        return ["dataConclusao", "dataCancelamento", "motivoCancelamento"]
            .every(campo => demanda[campo] === (acao === "reabrir" ? null : anterior[campo]));
    }
    const concluir = acao === "concluir";
    const categoria = concluir ? "CONCLUIDA" : "CANCELADA";
    if (acao !== "concluir" && acao !== "cancelar" || etapa.categoria !== categoria
        || resposta.etapas.filter(atual => atual.categoria === categoria).length !== 1) return false;
    return demanda.status === (concluir ? "Concluido" : "Cancelado")
        && (concluir ? demanda.dataConclusao !== null && demanda.dataCancelamento === null && demanda.motivoCancelamento === null
            : demanda.dataCancelamento !== null && demanda.dataConclusao === null && demanda.motivoCancelamento === enviados.motivo.trim());
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
        validar: (resposta, status) => cadastroConfirmado(resposta, id, dados, status) });
}

export function transicionarDemanda(token, id, demanda, acao, dados) {
    return requisicao(`/gerenciamentos/${id}/demandas/${demanda.id}/${acao}`, { token, method: "PUT", dados,
        validar: (resposta, status) => transicaoConfirmada(resposta, id, demanda, acao, dados, status) });
}
