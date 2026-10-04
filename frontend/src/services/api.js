const API_URL = "http://localhost:8081/api";


// =========================
// LOGIN
// =========================

export async function login(email, senha) {

    const resposta = await fetch(`${API_URL}/usuarios/login`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            email,
            senha
        })
    });

    const dados = await resposta.json();

    if (!resposta.ok) {
        throw new Error(
            dados.erro || "E-mail ou senha incorretos."
        );
    }

    return dados;
}


// =========================
// CADASTRO
// =========================

export async function cadastrarUsuario(usuario) {

    const resposta = await fetch(
        `${API_URL}/usuarios/cadastro`,
        {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(usuario)
        }
    );

    const dados = await resposta.json();

    if (!resposta.ok) {
        throw new Error(
            dados.erro || "Não foi possível realizar o cadastro."
        );
    }

    return dados;
}


// =========================
// USUÁRIO LOGADO
// =========================

export async function buscarUsuarioLogado(token) {

    const resposta = await fetch(
        `${API_URL}/usuarios/me`,
        {
            method: "GET",
            headers: {
                Authorization: `Bearer ${token}`
            }
        }
    );

    const dados = await resposta.json();

    if (!resposta.ok) {
        throw new Error(
            dados.erro || "Sessão inválida."
        );
    }

    return dados;
}


// =========================
// GERENCIAMENTOS
// =========================

export async function listarGerenciamentos(token) {

    const resposta = await fetch(
        `${API_URL}/gerenciamentos`,
        {
            method: "GET",
            headers: {
                Authorization: `Bearer ${token}`
            }
        }
    );

    const dados = await resposta.json();

    if (!resposta.ok) {
        throw new Error(
            dados.erro ||
            "Não foi possível carregar os gerenciamentos."
        );
    }

    return dados;
}


export async function criarGerenciamento(
    token,
    gerenciamento
) {

    const resposta = await fetch(
        `${API_URL}/gerenciamentos`,
        {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                Authorization: `Bearer ${token}`
            },
            body: JSON.stringify(gerenciamento)
        }
    );

    const dados = await resposta.json();

    if (!resposta.ok) {
        throw new Error(
            dados.erro ||
            "Não foi possível criar o gerenciamento."
        );
    }

    return dados;
}