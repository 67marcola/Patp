import { useState } from "react";
import { criarGerenciamento } from "../services/api";

function CriarGerenciamento({ voltar, atualizar }) {

    const [nome, setNome] = useState("");
    const [descricao, setDescricao] = useState("");

    const [etapas, setEtapas] = useState([
        {
            nome: "",
            setor: ""
        }
    ]);

    const [erro, setErro] = useState("");
    const [salvando, setSalvando] = useState(false);

    function adicionarEtapa() {

        setEtapas([
            ...etapas,
            {
                nome: "",
                setor: ""
            }
        ]);
    }

    function removerEtapa(index) {

        if (etapas.length === 1) {
            return;
        }

        setEtapas(
            etapas.filter((_, i) => i !== index)
        );
    }

    function alterarEtapa(index, campo, valor) {

        const novasEtapas = [...etapas];

        novasEtapas[index][campo] = valor;

        setEtapas(novasEtapas);
    }

    async function salvar(event) {

        event.preventDefault();

        setErro("");

        if (!nome.trim()) {
            setErro("Informe o nome do gerenciamento.");
            return;
        }

        for (const etapa of etapas) {

            if (!etapa.nome.trim() || !etapa.setor.trim()) {
                setErro(
                    "Preencha o nome e o setor de todas as etapas."
                );
                return;
            }
        }

        setSalvando(true);

        try {

            const token = localStorage.getItem("token");

            const dados = {
                nome: nome,
                descricao: descricao,
                etapas: etapas.map((etapa, index) => ({
                    nome: etapa.nome,
                    setor: etapa.setor,
                    ordem: index + 1
                }))
            };

            await criarGerenciamento(token, dados);

            atualizar();

            voltar();

        } catch (error) {

            setErro(error.message);

        } finally {

            setSalvando(false);
        }
    }

    return (
        <div className="criar-page">

            <div className="criar-cabecalho">

                <div>
                    <button
                        className="btn-voltar"
                        onClick={voltar}
                    >
                        ← Voltar
                    </button>

                    <h2>
                        Criar gerenciamento
                    </h2>

                    <p>
                        Configure o gerenciamento e suas etapas.
                    </p>
                </div>

            </div>

            <form
                className="form-gerenciamento"
                onSubmit={salvar}
            >

                <section className="form-card">

                    <h3>
                        Informações do gerenciamento
                    </h3>

                    <div className="campo-form">

                        <label>
                            Nome do gerenciamento
                        </label>

                        <input
                            type="text"
                            placeholder="Ex.: Projetos de Clientes"
                            value={nome}
                            onChange={(e) =>
                                setNome(e.target.value)
                            }
                        />

                    </div>

                    <div className="campo-form">

                        <label>
                            Descrição
                        </label>

                        <textarea
                            placeholder="Descreva para que este gerenciamento será utilizado..."
                            value={descricao}
                            onChange={(e) =>
                                setDescricao(e.target.value)
                            }
                        />

                    </div>

                </section>

                <section className="form-card">

                    <div className="etapas-titulo">

                        <div>
                            <h3>
                                Etapas
                            </h3>

                            <p>
                                Defina o fluxo do gerenciamento.
                            </p>
                        </div>

                        <button
                            type="button"
                            className="btn-secundario"
                            onClick={adicionarEtapa}
                        >
                            + Adicionar etapa
                        </button>

                    </div>

                    <div className="lista-etapas-form">

                        {etapas.map((etapa, index) => (

                            <div
                                className="etapa-form"
                                key={index}
                            >

                                <div className="numero-etapa">
                                    {index + 1}
                                </div>

                                <div className="campo-form">

                                    <label>
                                        Nome da etapa
                                    </label>

                                    <input
                                        type="text"
                                        placeholder="Nome da etapa"
                                        value={etapa.nome}
                                        onChange={(e) =>
                                            alterarEtapa(
                                                index,
                                                "nome",
                                                e.target.value
                                            )
                                        }
                                    />

                                </div>

                                <div className="campo-form">

                                    <label>
                                        Setor responsável
                                    </label>

                                    <input
                                        type="text"
                                        placeholder="Setor"
                                        value={etapa.setor}
                                        onChange={(e) =>
                                            alterarEtapa(
                                                index,
                                                "setor",
                                                e.target.value
                                            )
                                        }
                                    />

                                </div>

                                <button
                                    type="button"
                                    className="btn-remover"
                                    onClick={() =>
                                        removerEtapa(index)
                                    }
                                    disabled={etapas.length === 1}
                                >
                                    ×
                                </button>

                            </div>

                        ))}

                    </div>

                </section>

                {erro && (
                    <div className="erro-login">
                        {erro}
                    </div>
                )}

                <div className="acoes-form">

                    <button
                        type="button"
                        className="btn-secundario"
                        onClick={voltar}
                    >
                        Cancelar
                    </button>

                    <button
                        type="submit"
                        className="btn-criar"
                        disabled={salvando}
                    >
                        {salvando
                            ? "Salvando..."
                            : "Salvar gerenciamento"}
                    </button>

                </div>

            </form>

        </div>
    );
}

export default CriarGerenciamento;