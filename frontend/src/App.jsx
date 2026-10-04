import { useState } from "react";

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


    function entrar(dados) {

        setUsuario({
            id: dados.id,
            nome: dados.nome,
            setor: dados.setor,
            email: dados.email
        });

        setMostrarCadastro(false);
    }


    function sair() {

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
                    >
                        Sair
                    </button>

                </div>

            </header>


            <Gerenciamentos />

        </div>
    );
}

export default App;