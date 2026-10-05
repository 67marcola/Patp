import { useRef, useState } from "react";

import Login from "./pages/Login";
import Cadastro from "./pages/Cadastro";
import Gerenciamentos from "./pages/Gerenciamentos";


function App() {

    const [usuario, setUsuario] = useState(() => {

        const usuarioSalvo =
            localStorage.getItem("usuario");

        return usuarioSalvo
            ? JSON.parse(usuarioSalvo)
            : null;
    });


    const [mostrarCadastro, setMostrarCadastro] =
        useState(false);

    const [etapasOcupadas, setEtapasOcupadas] = useState(false);
    const operandoEtapas = useRef(false);

    function atualizarPendenciaEtapas(ocupado) {
        operandoEtapas.current = ocupado;
        setEtapasOcupadas(ocupado);
    }


    function entrar(dados) {

        const usuarioSeguro = {
            id: dados.id,
            nome: dados.nome,
            setor: dados.setor,
            email: dados.email
        };
        const cacheAnterior = {
            token: localStorage.getItem("token"),
            usuario: localStorage.getItem("usuario")
        };

        try {
            localStorage.setItem("token", dados.token);
            localStorage.setItem("usuario", JSON.stringify(usuarioSeguro));
        } catch {
            for (const [chave, valor] of Object.entries(cacheAnterior)) {
                try {
                    if (valor === null) localStorage.removeItem(chave);
                    else localStorage.setItem(chave, valor);
                } catch {
                    // O navegador também pode recusar a restauração do cache.
                }
            }
            throw new Error("Não foi possível salvar sua sessão neste navegador. Permita salvar dados do site e entre pelo login.");
        }

        setUsuario(usuarioSeguro);

        setMostrarCadastro(false);
    }


    function sair() {

        if (operandoEtapas.current) return;

        localStorage.removeItem("token");
        localStorage.removeItem("usuario");

        setUsuario(null);
    }


    // =========================
    // TELA DE CADASTRO
    // =========================

    if (!usuario && mostrarCadastro) {

        return (

            <Cadastro
                voltar={() =>
                    setMostrarCadastro(false)
                }
            />

        );
    }


    // =========================
    // TELA DE LOGIN
    // =========================

    if (!usuario) {

        return (

            <Login
                onLogin={entrar}
                abrirCadastro={() =>
                    setMostrarCadastro(true)
                }
            />

        );
    }


    // =========================
    // SISTEMA
    // =========================

    return (

        <div className="app">

            <header className="topbar">

                <div>

                    <h1>
                        Sistema de Gerenciamento
                    </h1>

                    <p>
                        Controle e acompanhamento dos processos
                    </p>

                </div>


                <div className="usuario">

                    <span>

                        {usuario.nome
                            .substring(0, 2)
                            .toUpperCase()}

                    </span>


                    <strong>
                        {usuario.nome}
                    </strong>


                    <button
                        onClick={sair}
                        className="btn-sair"
                        disabled={etapasOcupadas}
                    >
                        Sair
                    </button>

                </div>

            </header>


            <Gerenciamentos aoOcuparEtapas={atualizarPendenciaEtapas} />

        </div>
    );
}

export default App;
