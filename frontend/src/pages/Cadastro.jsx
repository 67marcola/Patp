import { useState } from "react";
import { cadastrarUsuario } from "../services/api";

function Cadastro({ voltar }) {

    const [nome, setNome] = useState("");
    const [setor, setSetor] = useState("");
    const [email, setEmail] = useState("");
    const [senha, setSenha] = useState("");
    const [confirmarSenha, setConfirmarSenha] = useState("");

    const [erro, setErro] = useState("");
    const [sucesso, setSucesso] = useState("");
    const [carregando, setCarregando] = useState(false);


    async function handleCadastro(event) {

        event.preventDefault();

        setErro("");
        setSucesso("");


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


        setCarregando(true);


        try {

            await cadastrarUsuario({
                nome: nome.trim(),
                setor: setor.trim(),
                email: email.trim(),
                senha: senha
            });


            setSucesso(
                "Cadastro realizado com sucesso!"
            );


            // Limpa formulário
            setNome("");
            setSetor("");
            setEmail("");
            setSenha("");
            setConfirmarSenha("");


        } catch (error) {

            setErro(error.message);

        } finally {

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

                        <label>
                            Nome completo
                        </label>

                        <input
                            type="text"
                            placeholder="Digite seu nome"
                            value={nome}
                            onChange={(e) =>
                                setNome(e.target.value)
                            }
                            required
                        />

                    </div>


                    <div className="campo">

                        <label>
                            Setor
                        </label>

                        <input
                            type="text"
                            placeholder="Ex.: Comercial"
                            value={setor}
                            onChange={(e) =>
                                setSetor(e.target.value)
                            }
                            required
                        />

                    </div>


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


                    <div className="campo">

                        <label>
                            Confirmar senha
                        </label>

                        <input
                            type="password"
                            placeholder="Digite a senha novamente"
                            value={confirmarSenha}
                            onChange={(e) =>
                                setConfirmarSenha(e.target.value)
                            }
                            required
                        />

                    </div>


                    {erro && (

                        <div className="erro-login">
                            {erro}
                        </div>

                    )}


                    {sucesso && (

                        <div className="sucesso-cadastro">
                            {sucesso}
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
                    >
                        Voltar para login
                    </button>

                </p>

            </div>

        </div>
    );
}

export default Cadastro;