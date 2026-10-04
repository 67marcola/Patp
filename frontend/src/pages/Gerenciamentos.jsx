import { useEffect, useState } from "react";
import { listarGerenciamentos } from "../services/api";
import CriarGerenciamento from "./CriarGerenciamento";
import Quadro from "./Quadro";

function Gerenciamentos() {

    const [gerenciamentos, setGerenciamentos] = useState([]);
    const [carregando, setCarregando] = useState(true);
    const [erro, setErro] = useState("");
    const [criando, setCriando] = useState(false);
    const [gerenciamentoSelecionado, setGerenciamentoSelecionado] = useState(null);

    async function carregarGerenciamentos() {

        try {

            setErro("");

            const token = localStorage.getItem("token");

            const dados = await listarGerenciamentos(token);

            setGerenciamentos(dados);

        } catch (error) {

            setErro(error.message);

        } finally {

            setCarregando(false);
        }
    }

    useEffect(() => {
        carregarGerenciamentos();
    }, []);

    if (criando) {

        return (
            <CriarGerenciamento
                voltar={() => setCriando(false)}
                atualizar={carregarGerenciamentos}
            />
        );
    }

    if (gerenciamentoSelecionado) {

        return (
            <Quadro
                gerenciamento={gerenciamentoSelecionado}
                voltar={() => setGerenciamentoSelecionado(null)}
            />
        );
    }

    if (carregando) {

        return (
            <div className="gerenciamentos-page">
                <p>Carregando gerenciamentos...</p>
            </div>
        );
    }

    return (
        <div className="gerenciamentos-page">

            <div className="gerenciamentos-topo">

                <div>

                    <h2>
                        Meus gerenciamentos
                    </h2>

                    <p>
                        Selecione um gerenciamento para acompanhar
                        seus processos.
                    </p>

                </div>

                <button
                    className="btn-criar"
                    onClick={() => setCriando(true)}
                >
                    + Criar gerenciamento
                </button>

            </div>

            {erro && (
                <div className="erro-login">
                    {erro}
                </div>
            )}

            {gerenciamentos.length === 0 && !erro && (

                <div className="vazio">

                    <div className="vazio-icone">
                        +
                    </div>

                    <h3>
                        Nenhum gerenciamento cadastrado
                    </h3>

                    <p>
                        Crie seu primeiro gerenciamento para começar.
                    </p>

                    <button
                        className="btn-criar"
                        onClick={() => setCriando(true)}
                    >
                        + Criar gerenciamento
                    </button>

                </div>
            )}

            <div className="lista-gerenciamentos">

                {gerenciamentos.map((gerenciamento) => (

                    <div
                        className="card-gerenciamento"
                        key={gerenciamento.id}
                    >

                        <div className="card-indicador"></div>

                        <div className="card-conteudo">

                            <h3>
                                {gerenciamento.nome}
                            </h3>

                            <p>
                                {gerenciamento.descricao ||
                                    "Sem descrição cadastrada."}
                            </p>

                        </div>

                        <button
                            className="btn-abrir"
                            onClick={() =>
                                setGerenciamentoSelecionado(gerenciamento)
                            }
                        >
                            Abrir →
                        </button>

                    </div>

                ))}

            </div>

        </div>
    );
}

export default Gerenciamentos;