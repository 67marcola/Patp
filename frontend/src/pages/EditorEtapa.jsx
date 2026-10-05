import { useEffect, useRef, useState } from "react";

function EditorEtapa({ etapa = null, quantidadeEtapas, salvar, cancelar, aoErro, bloqueado = false }) {
    const [nome, setNome] = useState(etapa?.nome || "");
    const [setor, setSetor] = useState(etapa?.setor || "");
    const [ordem, setOrdem] = useState(String(etapa?.ordem || quantidadeEtapas + 1));
    const [erro, setErro] = useState("");
    const [salvando, setSalvando] = useState(false);
    const enviando = useRef(false);
    const campoNome = useRef(null);
    const limitePosicao = quantidadeEtapas + (etapa ? 0 : 1);
    const desabilitado = salvando || bloqueado;

    useEffect(() => { campoNome.current?.focus(); }, []);

    async function enviar(event) {
        event.preventDefault();
        if (enviando.current || bloqueado) return;
        setErro("");
        const nomeNormalizado = nome.trim();
        const setorNormalizado = setor.trim();
        const posicao = Number(ordem);
        if (nomeNormalizado.length < 1 || nomeNormalizado.length > 255) {
            setErro("Informe um nome de etapa entre 1 e 255 caracteres.");
            return;
        }
        if (setorNormalizado.length < 1 || setorNormalizado.length > 255) {
            setErro("Informe um setor entre 1 e 255 caracteres.");
            return;
        }
        if (!Number.isInteger(posicao) || posicao < 1 || posicao > limitePosicao) {
            setErro("Escolha uma posição válida para a etapa.");
            return;
        }
        enviando.current = true;
        setSalvando(true);
        try {
            await salvar({ nome: nomeNormalizado, setor: setorNormalizado, ordem: posicao });
        } catch (error) {
            setErro(error.message);
            aoErro?.(error);
        } finally {
            enviando.current = false;
            setSalvando(false);
        }
    }

    return (
        <form className="form-card editor-etapa" onSubmit={enviar} noValidate aria-busy={salvando}
            aria-labelledby="editor-etapa-titulo">
            <h3 id="editor-etapa-titulo">{etapa ? `Editar etapa: ${etapa.nome}` : "Nova etapa"}</h3>
            <div className="campos-etapa">
                <div className="campo-form">
                    <label htmlFor="editor-etapa-nome">Nome da etapa</label>
                    <input id="editor-etapa-nome" ref={campoNome} value={nome} disabled={desabilitado}
                        onChange={event => setNome(event.target.value)} aria-describedby="editor-etapa-limite" />
                </div>
                <div className="campo-form">
                    <label htmlFor="editor-etapa-setor">Setor responsável</label>
                    <input id="editor-etapa-setor" value={setor} disabled={desabilitado}
                        onChange={event => setSetor(event.target.value)} aria-describedby="editor-etapa-limite" />
                </div>
                <div className="campo-form campo-posicao">
                    <label htmlFor="editor-etapa-posicao">Posição</label>
                    <select id="editor-etapa-posicao" value={ordem} disabled={desabilitado}
                        onChange={event => setOrdem(event.target.value)}>
                        {Array.from({ length: limitePosicao }, (_, index) => index + 1).map(posicao =>
                            <option key={posicao} value={posicao}>{posicao}</option>)}
                    </select>
                </div>
            </div>
            <p id="editor-etapa-limite" className="ajuda-campo">Nome e setor obrigatórios, até 255 caracteres. As demais etapas serão reorganizadas automaticamente.</p>
            {erro && <div className="erro-login" role="alert">{erro}</div>}
            <div className="acoes-form">
                <button type="button" className="btn-secundario" disabled={salvando} onClick={cancelar}>Cancelar</button>
                <button type="submit" className="btn-criar" disabled={desabilitado}>{salvando ? "Salvando..." : "Salvar etapa"}</button>
            </div>
        </form>
    );
}

export default EditorEtapa;
