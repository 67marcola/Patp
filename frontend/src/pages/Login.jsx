import { useState } from "react";
import { login } from "../services/api";

function Login({ onLogin, abrirCadastro }) {

    const [email, setEmail] = useState("");
    const [senha, setSenha] = useState("");

    const [erro, setErro] = useState("");
    const [carregando, setCarregando] = useState(false);


    async function handleLogin(event) {

        event.preventDefault();

        setErro("");
        setCarregando(true);


        try {

            const dados = await login(
                email,
                senha
            );


            onLogin(dados);


        } catch (error) {

            setErro(error.message);

        } finally {

            setCarregando(false);

        }
    }


    return (

        <div className="login-page">

            <div className="login-card">

                <div className="login-logo">

                    <span>
                        PM
                    </span>

                </div>


                <h1>
                    Bem-vindo
                </h1>


                <p className="login-subtitulo">
                    Entre para acessar seus gerenciamentos
                </p>


                <form onSubmit={handleLogin}>

                    <div className="campo">

                        <label>
                            E-mail
                        </label>

                        <input
                            type="email"
                            placeholder="seu@email.com"
                            value={email}
                            onChange={(e) =>
                                setEmail(e.target.value)
                            }
                            required
                        />

                    </div>


                    <div className="campo">

                        <label>
                            Senha
                        </label>

                        <input
                            type="password"
                            placeholder="Digite sua senha"
                            value={senha}
                            onChange={(e) =>
                                setSenha(e.target.value)
                            }
                            required
                        />

                    </div>


                    {erro && (

                        <div className="erro-login">
                            {erro}
                        </div>

                    )}


                    <button
                        type="submit"
                        className="btn-login"
                        disabled={carregando}
                    >

                        {carregando
                            ? "Entrando..."
                            : "Entrar"}

                    </button>

                </form>


                <p className="login-cadastro">

                    Ainda não possui uma conta?

                    <button
                        type="button"
                        onClick={abrirCadastro}
                    >
                        Criar cadastro
                    </button>

                </p>

            </div>

        </div>
    );
}

export default Login;
