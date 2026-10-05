import { useRef, useState } from "react";
import { ApiError, cadastrarUsuario } from "../services/api";

function Cadastro({ voltar, onLogin }) {

    const [nome, setNome] = useState("");
    const [setor, setSetor] = useState("");
    const [email, setEmail] = useState("");
    const [senha, setSenha] = useState("");
    const [confirmarSenha, setConfirmarSenha] = useState("");

    const [erro, setErro] = useState("");
    const [carregando, setCarregando] = useState(false);
    const enviando = useRef(false);


    async function handleCadastro(event) {

        event.preventDefault();

        if (enviando.current) return;
        setErro("");


        // Verifica senha
        if (senha !== confirmarSenha) {

            setErro("As senhas não são iguais.");

            return;
        }


        if (senha.length < 6) {

            setErro(
                "A senha deve possuir pelo menos 6 caracteres."
            );

            return;
        }


        enviando.current = true;
        setCarregando(true);


        try {

            const dados = await cadastrarUsuario({
                nome: nome.trim(),
                setor: setor.trim(),
                email: email.trim(),
                senha: senha
            });


            onLogin(dados);


        } catch (error) {

            setErro(error instanceof ApiError
                && (error.status === 0 || (error.status >= 200 && error.status < 300))
                ? "Não foi possível confirmar o cadastro. Se a conta já foi criada, entre pelo login."
                : error.message);

        } finally {

            enviando.current = false;
            setCarregando(false);

        }
    }


    return (

        <div className="login-page">

            <div className="login-card cadastro-card">

                <div className="login-logo">

                    <span>
                        PM
                    </span>

                </div>


                <h1>
                    Criar cadastro
                </h1>


                <p className="login-subtitulo">
                    Cadastre um novo usuário no sistema
                </p>


                <form onSubmit={handleCadastro}>

                    <div className="campo">

                        <label htmlFor="cadastro-nome">
                            Nome completo
                        </label>

                        <input
                            id="cadastro-nome"
                            type="text"
                            placeholder="Digite seu nome"
                            value={nome}
                            onChange={(e) =>
                                setNome(e.target.value)
                            }
                            required
                            disabled={carregando}
                        />

                    </div>


                    <div className="campo">

                        <label htmlFor="cadastro-setor">
                            Setor
                        </label>

                        <input
                            id="cadastro-setor"
                            type="text"
                            placeholder="Ex.: Comercial"
                            value={setor}
                            onChange={(e) =>
                                setSetor(e.target.value)
                            }
                            required
                            disabled={carregando}
                        />

                    </div>


                    <div className="campo">

                        <label htmlFor="cadastro-email">
                            E-mail
                        </label>

                        <input
                            id="cadastro-email"
                            type="email"
                            placeholder="seu@email.com"
                            value={email}
                            onChange={(e) =>
                                setEmail(e.target.value)
                            }
                            required
                            disabled={carregando}
                        />

                    </div>


                    <div className="campo">

                        <label htmlFor="cadastro-senha">
                            Senha
                        </label>

                        <input
                            id="cadastro-senha"
                            type="password"
                            placeholder="Digite sua senha"
                            value={senha}
                            onChange={(e) =>
                                setSenha(e.target.value)
                            }
                            required
                            disabled={carregando}
                        />

                    </div>


                    <div className="campo">

                        <label htmlFor="cadastro-confirmar-senha">
                            Confirmar senha
                        </label>

                        <input
                            id="cadastro-confirmar-senha"
                            type="password"
                            placeholder="Digite a senha novamente"
                            value={confirmarSenha}
                            onChange={(e) =>
                                setConfirmarSenha(e.target.value)
                            }
                            required
                            disabled={carregando}
                        />

                    </div>


                    {erro && (

                        <div className="erro-login" role="alert">
                            {erro}
                        </div>

                    )}


                    <button
                        type="submit"
                        className="btn-login"
                        disabled={carregando}
                    >

                        {carregando
                            ? "Cadastrando..."
                            : "Criar cadastro"}

                    </button>

                </form>


                <p className="login-cadastro">

                    Já possui uma conta?

                    <button
                        type="button"
                        onClick={voltar}
                        disabled={carregando}
                    >
                        Voltar para login
                    </button>

                </p>

            </div>

        </div>
    );
}

export default Cadastro;
