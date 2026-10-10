import { useEffect, useRef, useState } from "react";
import { buscarComentarios, enviarComentario } from "../services/api";

const aviso = "Confira as mensagens antes de enviar novamente; o comentário pode já ter sido salvo.";
// Mesmos espaços e controles aceitos por Java String.isBlank; NBSP continua texto válido.
// eslint-disable-next-line no-control-regex
const textoInvalido = texto => texto.length > 10000 || /^[\u0009-\u000d\u001c-\u0020\u1680\u2000-\u2006\u2008-\u200a\u2028\u2029\u205f\u3000]*$/.test(texto);
const horario = iso => `${iso.slice(8, 10)}/${iso.slice(5, 7)}/${iso.slice(0, 4)} ${iso.slice(11, 19)}`;

function Conversa({ gerenciamentoId, demanda, fechar, aoOcupar }) {
    const [conversa, setConversa] = useState(null);
    const [carregando, setCarregando] = useState(true);
    const [erroConsulta, setErroConsulta] = useState("");
    const [erroEnvio, setErroEnvio] = useState("");
    const [texto, setTexto] = useState("");
    const [pendente, setPendente] = useState(false);
    const [precisaConsultar, setPrecisaConsultar] = useState(false);
    const [incerto, setIncerto] = useState(false);
    const vivo = useRef(true), enviando = useRef(false), consulta = useRef(null), botaoFechar = useRef(null);
    const callbackOcupar = useRef(aoOcupar); callbackOcupar.current = aoOcupar;
    const somenteConsulta = conversa?.quadroArquivado || conversa?.demandaArquivada;

    async function carregar() {
        if (enviando.current) return;
        consulta.current?.abort(); const controller = new AbortController(); consulta.current = controller;
        setCarregando(true); setErroConsulta("");
        try {
            const dados = await buscarComentarios(localStorage.getItem("token"), gerenciamentoId, demanda.id, { signal: controller.signal });
            if (vivo.current && !controller.signal.aborted) { setConversa(dados); setPrecisaConsultar(false); }
        } catch (error) {
            if (vivo.current && !controller.signal.aborted) setErroConsulta(error.message);
        } finally { if (vivo.current && !controller.signal.aborted) setCarregando(false); }
    }

    useEffect(() => {
        vivo.current = true; botaoFechar.current?.focus(); carregar();
        const escape = event => { if (event.key === "Escape" && !enviando.current) { event.preventDefault(); fechar(); } };
        document.addEventListener("keydown", escape);
        return () => {
            vivo.current = false; consulta.current?.abort(); document.removeEventListener("keydown", escape);
            if (enviando.current) callbackOcupar.current?.(false);
        };
    }, []);

    async function enviar(event) {
        event.preventDefault();
        if (enviando.current || carregando || precisaConsultar || !conversa || somenteConsulta) return;
        setErroEnvio("");
        if (textoInvalido(texto)) { setErroEnvio("Comentário deve conter texto e ter no máximo 10000 caracteres."); return; }
        enviando.current = true; setPendente(true); callbackOcupar.current?.(true);
        try {
            const salvo = await enviarComentario(localStorage.getItem("token"), gerenciamentoId, demanda.id, texto, conversa.comentarios);
            if (vivo.current) { setConversa(atual => ({ ...atual, comentarios: [...atual.comentarios, salvo] })); setTexto(""); setIncerto(false); }
        } catch (error) {
            if (vivo.current) { setErroEnvio(error.message); setPrecisaConsultar(true); setIncerto(true); }
        } finally {
            enviando.current = false;
            if (vivo.current) { setPendente(false); callbackOcupar.current?.(false); }
        }
    }

    return <aside className="comentarios-demanda" aria-labelledby="comentarios-titulo" aria-busy={pendente}>
        <div className="comentarios-topo">
            <h2 id="comentarios-titulo">Comentários: {demanda.numeroProcesso}</h2>
            <button type="button" ref={botaoFechar} className="btn-secundario" disabled={pendente} onClick={() => { if (!enviando.current) fechar(); }}>Fechar comentários</button>
        </div>
        <p>{demanda.pessoa}</p>
        {carregando && <p role="status">Carregando comentários...</p>}
        {erroConsulta && <div role="alert" className="erro-login">{erroConsulta}</div>}
        {conversa && <>
            {conversa.comentarios.length === 0 ? <p>Nenhum comentário nesta demanda.</p> : <ol className="mensagens-demanda" aria-label="Mensagens">
                {conversa.comentarios.map(mensagem => <li key={mensagem.id} data-comentario-id={mensagem.id}>
                    <div><strong>{mensagem.funcionario}</strong> <time dateTime={mensagem.dataHora}>{horario(mensagem.dataHora)}</time></div>
                    <p className="texto-comentario">{mensagem.texto}</p>
                </li>)}
            </ol>}
            {somenteConsulta && <p className="aviso-arquivado">{conversa.quadroArquivado ? "Quadro arquivado: comentários somente para consulta." : "Demanda arquivada: comentários somente para consulta."}</p>}
        </>}
        <button type="button" className="btn-secundario" disabled={pendente || carregando} onClick={carregar}>{!conversa && erroConsulta ? "Tentar novamente" : "Atualizar comentários"}</button>
        {erroEnvio && <div role="alert" className="erro-login">{erroEnvio}</div>}
        {incerto && <p className="aviso-comentario">{aviso}</p>}
        {conversa && !somenteConsulta && <form onSubmit={enviar} noValidate>
            <label htmlFor="novo-comentario">Comentário</label>
            <textarea id="novo-comentario" rows={5} value={texto} disabled={pendente} onChange={event => setTexto(event.target.value)} aria-describedby="comentario-limite" />
            <p id="comentario-limite" className="ajuda-campo">Até 10.000 caracteres. Seu nome e o horário serão registrados no envio.</p>
            <button type="submit" className="btn-criar" disabled={pendente || carregando || precisaConsultar}>{pendente ? "Enviando comentário..." : "Enviar comentário"}</button>
        </form>}
        {somenteConsulta && texto && <div><strong>Rascunho não enviado:</strong><p className="texto-comentario">{texto}</p></div>}
    </aside>;
}

export default function ComentariosDemanda(props) {
    return <Conversa key={`${props.gerenciamentoId}-${props.demanda.id}`} {...props} />;
}
