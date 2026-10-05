import { useRef, useState } from "react";
import { criarGerenciamento, editarGerenciamento } from "../services/api";

function CriarGerenciamento({ voltar, atualizar, gerenciamento = null }) {
    const editando = gerenciamento !== null;
    const [nome, setNome] = useState(gerenciamento?.nome || "");
    const [descricao, setDescricao] = useState(gerenciamento?.descricao || "");
    const [etapas, setEtapas] = useState([]);
    const [erro, setErro] = useState("");
    const [salvando, setSalvando] = useState(false);
    const enviando = useRef(false);

    function alterarEtapa(index, campo, valor) {
        setEtapas(etapas.map((etapa, i) => i === index ? { ...etapa, [campo]: valor } : etapa));
    }

    async function salvar(event) {
        event.preventDefault();
        if (enviando.current) return;
        setErro("");
        const nomeNormalizado = nome.trim();
        if (nomeNormalizado.length < 1 || nomeNormalizado.length > 120) {
            setErro("Informe um nome entre 1 e 120 caracteres.");
            return;
        }
        if (descricao.length > 255) {
            setErro("A descrição deve ter até 255 caracteres.");
            return;
        }
        if (!editando && etapas.some(etapa =>
            !etapa.nome.trim() || !etapa.setor.trim() ||
            etapa.nome.trim().length > 255 || etapa.setor.trim().length > 255)) {
            setErro("Preencha o nome e o setor das etapas com até 255 caracteres.");
            return;
        }
        enviando.current = true;
        setSalvando(true);
        try {
            const token = localStorage.getItem("token");
            const dados = { nome: nomeNormalizado, descricao };
            const salvo = editando
                ? await editarGerenciamento(token, gerenciamento.id, { ...dados, versao: gerenciamento.versao })
                : await criarGerenciamento(token, {
                    ...dados,
                    etapas: etapas.map((etapa, index) => ({
                        nome: etapa.nome.trim(), setor: etapa.setor.trim(), ordem: index + 1
                    }))
                });
            await atualizar(salvo);
            voltar();
        } catch (error) {
            setErro(error.message);
        } finally {
            enviando.current = false;
            setSalvando(false);
        }
    }

    return (
        <div className="criar-page">
            <div className="criar-cabecalho">
                <button className="btn-voltar" onClick={voltar} disabled={salvando}>← Voltar</button>
                <h2>{editando ? "Editar gerenciamento" : "Criar gerenciamento"}</h2>
                <p>{editando ? "Atualize o nome e a descrição do quadro." : "Configure o gerenciamento e suas etapas iniciais."}</p>
            </div>
            <form className="form-gerenciamento" onSubmit={salvar} noValidate aria-busy={salvando}>
                <section className="form-card">
                    <h3>Informações do gerenciamento</h3>
                    <div className="campo-form">
                        <label htmlFor="gerenciamento-nome">Nome do gerenciamento</label>
                        <input id="gerenciamento-nome" type="text" value={nome}
                            onChange={e => setNome(e.target.value)} disabled={salvando}
                            aria-describedby="nome-limite" />
                        <p id="nome-limite" className="ajuda-campo">Obrigatório, até 120 caracteres.</p>
                    </div>
                    <div className="campo-form">
                        <label htmlFor="gerenciamento-descricao">Descrição</label>
                        <textarea id="gerenciamento-descricao" value={descricao}
                            onChange={e => setDescricao(e.target.value)} disabled={salvando}
                            aria-describedby="descricao-limite" />
                        <p id="descricao-limite" className="ajuda-campo">Opcional, até 255 caracteres.</p>
                    </div>
                </section>
                {!editando && (
                    <section className="form-card">
                        <div className="etapas-titulo">
                            <div>
                                <h3>Etapas iniciais</h3>
                                <p>Opcional. Você pode criar o quadro sem etapas.</p>
                            </div>
                            <button type="button" className="btn-secundario" disabled={salvando}
                                onClick={() => setEtapas([...etapas, { nome: "", setor: "" }])}>
                                + Adicionar etapa
                            </button>
                        </div>
                        <div className="lista-etapas-form">
                            {etapas.map((etapa, index) => (
                                <div className="etapa-form" key={index}>
                                    <div className="numero-etapa">{index + 1}</div>
                                    <div className="campo-form">
                                        <label htmlFor={`etapa-nome-${index}`}>Nome da etapa {index + 1}</label>
                                        <input id={`etapa-nome-${index}`} type="text" value={etapa.nome}
                                            onChange={e => alterarEtapa(index, "nome", e.target.value)} disabled={salvando} />
                                    </div>
                                    <div className="campo-form">
                                        <label htmlFor={`etapa-setor-${index}`}>Setor responsável da etapa {index + 1}</label>
                                        <input id={`etapa-setor-${index}`} type="text" value={etapa.setor}
                                            onChange={e => alterarEtapa(index, "setor", e.target.value)} disabled={salvando} />
                                    </div>
                                    <button type="button" className="btn-remover" disabled={salvando}
                                        aria-label={`Remover etapa ${index + 1}`}
                                        onClick={() => setEtapas(etapas.filter((_, i) => i !== index))}>×</button>
                                </div>
                            ))}
                        </div>
                    </section>
                )}
                {erro && <div className="erro-login" role="alert">{erro}</div>}
                <div className="acoes-form">
                    <button type="button" className="btn-secundario" onClick={voltar} disabled={salvando}>Cancelar</button>
                    <button type="submit" className="btn-criar" disabled={salvando}>
                        {salvando ? "Salvando..." : editando ? "Salvar alterações" : "Salvar gerenciamento"}
                    </button>
                </div>
            </form>
        </div>
    );
}

export default CriarGerenciamento;
