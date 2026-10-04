import { useEffect, useState } from "react";

function Quadro({ gerenciamento, voltar }) {

    const [etapas, setEtapas] = useState([]);
    const [carregando, setCarregando] = useState(true);
    const [erro, setErro] = useState("");

    useEffect(() => {

        async function carregarEtapas() {

            try {

                const token = localStorage.getItem("token");

                const resposta = await fetch(
                    `http://localhost:8081/api/gerenciamentos/${gerenciamento.id}/etapas`,
                    {
                        headers: {
                            Authorization: `Bearer ${token}`
                        }
                    }
                );

                const dados = await resposta.json();

                if (!resposta.ok) {
                    throw new Error(
                        dados.erro || "Erro ao carregar etapas."
                    );
                }

                setEtapas(dados);

            } catch (error) {

                setErro(error.message);

            } finally {

                setCarregando(false);

            }
        }

        carregarEtapas();

    }, [gerenciamento.id]);

    if (carregando) {
        return (
            <main className="conteudo">
                <p>Carregando quadro...</p>
            </main>
        );
    }

    return (
        <main className="quadro-page">

            <div className="quadro-topo">

                <div>

                    <button
                        className="btn-voltar"
                        onClick={voltar}
                    >
                        ← Voltar
                    </button>

                    <h2>
                        {gerenciamento.nome}
                    </h2>

                    <p>
                        {gerenciamento.descricao ||
                            "Controle e acompanhamento dos processos."}
                    </p>

                </div>

                <button className="btn-criar">
                    + Criar processo
                </button>

            </div>

            {erro && (
                <div className="erro-login">
                    {erro}
                </div>
            )}

            <div className="etapas-quadro">

                {etapas.map((etapa) => (

                    <section
                        className="etapa"
                        key={etapa.id}
                    >

                        <div className="etapa-cabecalho">

                            <div className="titulo-etapa">

                                <span className="indicador verde"></span>

                                <div>

                                    <h2>
                                        {etapa.nome}
                                    </h2>

                                    <p>
                                        Setor responsável:{" "}
                                        {etapa.setor}
                                    </p>

                                </div>

                            </div>

                            <span className="quantidade">
                                0 processos
                            </span>

                        </div>

                        <div className="quadro-vazio">

                            <p>
                                Nenhum processo nesta etapa.
                            </p>

                        </div>

                    </section>

                ))}

            </div>

        </main>
    );
}

export default Quadro;