import { useEffect, useRef, useState } from "react";

import Login from "./pages/Login";
import Cadastro from "./pages/Cadastro";
import Gerenciamentos from "./pages/Gerenciamentos";
import AprovacoesCadastros from "./components/AprovacoesCadastros";
import { buscarUsuarioLogado, encerrarSessao } from "./services/api";


function App({ modoInternet = import.meta.env.VITE_MODO_INTERNET === "true" }) {

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
    const [papelConferido, setPapelConferido] = useState(null);
    const [mostrarAprovacoes, setMostrarAprovacoes] = useState(false);
    const [aprovacaoOcupada, setAprovacaoOcupada] = useState(false);
    const [saindo, setSaindo] = useState(false);
    const [avisoSessao, setAvisoSessao] = useState("");
    const operandoSaida = useRef(false), operandoAprovacao = useRef(false), botaoAprovacoes = useRef(null);

    useEffect(() => {
        if (!modoInternet || !usuario) return;
        let atual = true;
        buscarUsuarioLogado(localStorage.getItem("token")).then(dados => {
            if (!atual) return;
            if (dados?.id !== usuario.id || !["ADMINISTRADOR", "FUNCIONARIO"].includes(dados.papel)) {
                throw new Error("Acesso não confirmado.");
            }
            setPapelConferido(dados.papel);
        }).catch(() => {
            if (atual) setAvisoSessao("Não foi possível confirmar seu acesso. Entre novamente para consultar aprovações.");
        });
        return () => { atual = false; };
    }, [modoInternet, usuario]);

    function ocuparAprovacao(ocupado) {
        operandoAprovacao.current = ocupado; setAprovacaoOcupada(ocupado);
    }

    function fecharAprovacoes() {
        setMostrarAprovacoes(false); botaoAprovacoes.current?.focus();
    }

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
        setPapelConferido(null); setAvisoSessao("");

        setMostrarCadastro(false);
    }


    async function sair() {

        if (operandoEtapas.current || operandoAprovacao.current || operandoSaida.current) return;
        operandoSaida.current = true; setSaindo(true);
        const token = localStorage.getItem("token");
        setAvisoSessao("");
        if (modoInternet) {
            try { await encerrarSessao(token); }
            catch { setAvisoSessao("Você saiu deste navegador, mas a revogação da sessão no servidor não foi confirmada. A sessão online expira em até oito horas."); }
        }

        localStorage.removeItem("token");
        localStorage.removeItem("usuario");

        setUsuario(null);
        setPapelConferido(null); setMostrarAprovacoes(false);
        operandoSaida.current = false; setSaindo(false);
    }


    // =========================
    // TELA DE CADASTRO
    // =========================

    if (!usuario && mostrarCadastro) {

        return (

            <Cadastro
                onLogin={entrar}
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

            <>
            {avisoSessao && <p className="aviso-sessao" role="alert">{avisoSessao}</p>}
            <Login
                onLogin={entrar}
                abrirCadastro={() =>
                    setMostrarCadastro(true)
                }
            />
            </>

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
                        Creral
                    </h1>

                    <p>
                        Gestão de demandas e gerenciamentos
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
                        disabled={etapasOcupadas || aprovacaoOcupada || saindo}
                    >
                        {saindo ? "Saindo..." : "Sair"}
                    </button>

                </div>

            </header>

            {avisoSessao && <p className="aviso-sessao" role="alert">{avisoSessao}</p>}
            {modoInternet && papelConferido === "ADMINISTRADOR" && <div className="aprovacoes-cadastros">
                <button type="button" ref={botaoAprovacoes} aria-expanded={mostrarAprovacoes}
                    disabled={etapasOcupadas || aprovacaoOcupada || saindo}
                    onClick={() => mostrarAprovacoes ? fecharAprovacoes() : setMostrarAprovacoes(true)}>
                    Aprovar cadastros
                </button>
            </div>}
            {modoInternet && papelConferido === "ADMINISTRADOR" && mostrarAprovacoes && <AprovacoesCadastros
                token={localStorage.getItem("token")} aoFechar={fecharAprovacoes} aoOcupar={ocuparAprovacao} />}


            <Gerenciamentos aoOcuparEtapas={atualizarPendenciaEtapas} />

        </div>
    );
}

export default App;
