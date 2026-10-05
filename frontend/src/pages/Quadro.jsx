import { useEffect, useState } from "react";
import { listarEtapas } from "../services/api";

function Quadro({ gerenciamento, voltar }) {
    const [etapas, setEtapas] = useState([]);
    const [carregando, setCarregando] = useState(true);
    const [erro, setErro] = useState("");
    const [tentativa, setTentativa] = useState(0);

    useEffect(() => {
        const controller = new AbortController();
        setCarregando(true);
        setErro("");
        async function carregar() {
            try {
                const dados = await listarEtapas(localStorage.getItem("token"), gerenciamento.id, { signal: controller.signal });
                if (!controller.signal.aborted) setEtapas(dados);
            } catch (error) {
                if (!controller.signal.aborted) setErro(error.message);
            } finally {
                if (!controller.signal.aborted) setCarregando(false);
            }
        }
        carregar();
        return () => controller.abort();
    }, [gerenciamento.id, tentativa]);

    return (
        <main className="quadro-page">
            <div className="quadro-topo">
                <div>
                    <button className="btn-voltar" onClick={voltar}>← Voltar</button>
                    <h2>{gerenciamento.nome}</h2>
                    <p>{gerenciamento.descricao || "Controle e acompanhamento dos processos."}</p>
                    {gerenciamento.arquivado && <p className="aviso-arquivado">Arquivado — somente consulta</p>}
                </div>
                {!gerenciamento.arquivado && <button className="btn-criar">+ Criar processo</button>}
            </div>
            {carregando && <p role="status">Carregando quadro...</p>}
            {erro && <div className="falha-consulta">
                <div className="erro-login" role="alert">{erro}</div>
                <button className="btn-secundario" onClick={() => setTentativa(valor => valor + 1)}>Tentar novamente</button>
            </div>}
            {!carregando && !erro && etapas.length === 0 && (
                <div className="vazio">
                    <h3>Nenhuma etapa cadastrada</h3>
                    <p>Este gerenciamento ainda não possui etapas.</p>
                </div>
            )}
            {!carregando && !erro && <div className="etapas-quadro">
                {etapas.map(etapa => (
                    <section className="etapa" key={etapa.id}>
                        <div className="etapa-cabecalho">
                            <div className="titulo-etapa">
                                <span className="indicador verde"></span>
                                <div>
                                    <h2>{etapa.nome}</h2>
                                    <p>Setor responsável: {etapa.setor}</p>
                                </div>
                            </div>
                            <span className="quantidade">0 processos</span>
                        </div>
                        <div className="quadro-vazio"><p>Nenhum processo nesta etapa.</p></div>
                    </section>
                ))}
            </div>}
        </main>
    );
}

export default Quadro;
