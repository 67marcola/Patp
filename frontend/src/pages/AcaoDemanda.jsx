import { useEffect, useRef, useState } from "react";

const titulos = { mover: "Mover/pular etapa", concluir: "Concluir demanda", cancelar: "Cancelar demanda", reabrir: "Reabrir demanda" };
const botoes = { mover: "Mover demanda", concluir: "Confirmar conclusão", cancelar: "Confirmar cancelamento", reabrir: "Reabrir demanda" };

function AcaoDemanda({ acao, demanda, etapas, salvar, cancelar, aoErro, bloqueado = false }) {
    const [etapaId, setEtapaId] = useState("");
    const [motivo, setMotivo] = useState("");
    const [erro, setErro] = useState("");
    const [salvando, setSalvando] = useState(false);
    const enviando = useRef(false);
    const entrada = useRef(null);
    const voltar = useRef(null);
    const escolher = acao === "mover" || acao === "reabrir";
    const trabalhos = etapas.filter(etapa => etapa.categoria === "TRABALHO" && (acao !== "mover" || etapa.id !== demanda.etapaId));
    const semDestino = escolher && trabalhos.length === 0;
    const desabilitado = salvando || bloqueado || semDestino;

    useEffect(() => { (entrada.current || voltar.current)?.focus(); }, []);

    async function enviar(event) {
        event.preventDefault();
        if (enviando.current || bloqueado || semDestino) return;
        setErro("");
        const dados = {};
        if (escolher) {
            const id = Number(etapaId);
            if (!trabalhos.some(etapa => etapa.id === id)) {
                setErro("Escolha uma etapa de trabalho."); entrada.current?.focus(); return;
            }
            dados.etapaId = id;
        }
        if (acao === "cancelar") {
            const normalizado = motivo.trim();
            if (!normalizado || normalizado.length > 10000) {
                setErro("Informe um motivo entre 1 e 10000 caracteres."); entrada.current?.focus(); return;
            }
            dados.motivo = normalizado;
        }
        enviando.current = true; setSalvando(true);
        try { await salvar(dados); }
        catch (error) { setErro(error.message); aoErro?.(error); }
        finally { enviando.current = false; setSalvando(false); }
    }

    return <section className="form-card" role="dialog" aria-labelledby="acao-demanda-titulo">
        <h3 id="acao-demanda-titulo">{titulos[acao]}: {demanda.numeroProcesso}</h3>
        <p>{demanda.pessoa}</p>
        <form onSubmit={enviar} noValidate aria-busy={salvando}>
            {escolher && <div className="campo-form">
                <label htmlFor="acao-etapa-destino">Etapa de destino</label>
                <select id="acao-etapa-destino" value={etapaId} ref={entrada} disabled={desabilitado} required
                    onChange={event => setEtapaId(event.target.value)}>
                    <option value="">Escolha uma etapa</option>
                    {trabalhos.map(etapa => <option key={etapa.id} value={etapa.id}>{etapa.nome}</option>)}
                </select>
                <p>Você pode escolher qualquer etapa de trabalho disponível, inclusive uma anterior.</p>
            </div>}
            {semDestino && <p>Nenhuma etapa de trabalho disponível. Configure uma etapa antes de continuar.</p>}
            {acao === "concluir" && <p>A demanda irá para Concluídos.</p>}
            {acao === "cancelar" && <>
                <p>A demanda irá para Cancelados.</p>
                <div className="campo-form">
                    <label htmlFor="acao-motivo">Motivo do cancelamento</label>
                    <textarea id="acao-motivo" ref={entrada} value={motivo} rows={4} disabled={desabilitado} required
                        aria-describedby="acao-motivo-ajuda" onChange={event => setMotivo(event.target.value)} />
                    <p id="acao-motivo-ajuda">Informe um motivo entre 1 e 10000 caracteres.</p>
                </div>
            </>}
            {acao === "reabrir" && <p>A demanda voltará para Em andamento. Os encerramentos anteriores permanecerão no histórico.</p>}
            {erro && <div className="erro-login" role="alert">{erro}</div>}
            <div className="acoes-form">
                <button type="button" className="btn-secundario" ref={voltar} disabled={salvando} onClick={cancelar}>Voltar sem alterar</button>
                <button type="submit" className="btn-criar" disabled={desabilitado}>{salvando ? "Salvando..." : botoes[acao]}</button>
            </div>
        </form>
    </section>;
}

export default AcaoDemanda;
