import { useEffect, useRef, useState } from "react";

function AcaoArquivamentoDemanda({ acao, demanda, etapa, salvar, cancelar, aoErro, bloqueado = false }) {
    const [erro, setErro] = useState("");
    const [salvando, setSalvando] = useState(false);
    const enviando = useRef(false);
    const voltar = useRef(null);
    const arquivar = acao === "arquivar";
    useEffect(() => { voltar.current?.focus(); }, []);

    async function enviar(event) {
        event.preventDefault();
        if (enviando.current || bloqueado) return;
        enviando.current = true; setSalvando(true); setErro("");
        try { await salvar(); }
        catch (error) { setErro(error.message); aoErro?.(error); }
        finally { enviando.current = false; setSalvando(false); }
    }

    return <section className="form-card" role="dialog" aria-labelledby="arquivo-demanda-titulo" aria-describedby="arquivo-demanda-efeito">
        <h3 id="arquivo-demanda-titulo">{arquivar ? "Arquivar" : "Restaurar"} demanda: {demanda.numeroProcesso}</h3>
        <p>{demanda.pessoa}</p>
        <p id="arquivo-demanda-efeito">{arquivar
            ? "A demanda sairá das colunas e ficará disponível em Arquivadas. Seus dados, comentários e histórico serão preservados."
            : `A demanda voltará à etapa ${etapa?.nome || "original"} com status ${demanda.status}. Restaurar não reabre nem move a demanda.`}</p>
        <form onSubmit={enviar} aria-busy={salvando}>
            {erro && <div className="erro-login" role="alert">{erro}</div>}
            <div className="acoes-form">
                <button type="button" className="btn-secundario" ref={voltar} disabled={salvando} onClick={cancelar}>Cancelar</button>
                <button type="submit" className="btn-criar" disabled={salvando || bloqueado}>
                    {salvando ? "Salvando..." : arquivar ? "Arquivar demanda" : "Restaurar demanda"}
                </button>
            </div>
        </form>
    </section>;
}

export default AcaoArquivamentoDemanda;
