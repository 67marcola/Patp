import { useEffect, useRef, useState } from "react";
import { consultarAtividade } from "../services/api";

const acoes = {
    QUADRO_CRIADO: "Quadro criado", QUADRO_EDITADO: "Quadro editado", QUADRO_ARQUIVADO: "Quadro arquivado", QUADRO_RESTAURADO: "Quadro restaurado",
    ETAPA_CRIADA: "Etapa criada", ETAPA_EDITADA: "Etapa editada", ETAPA_REMOVIDA: "Etapa removida",
    DEMANDA_CRIADA: "Demanda criada", DEMANDA_EDITADA: "Demanda editada", DEMANDA_MOVIDA: "Demanda movida", DEMANDA_CONCLUIDA: "Demanda concluída",
    DEMANDA_CANCELADA: "Demanda cancelada", DEMANDA_REABERTA: "Demanda reaberta", DEMANDA_ARQUIVADA: "Demanda arquivada", DEMANDA_RESTAURADA: "Demanda restaurada", COMENTARIO_ADICIONADO: "Comentário adicionado"
};
const campos = { nome: "Nome", descricao: "Descrição", arquivado: "Arquivado", setor: "Setor", ordem: "Posição", numeroProcesso: "Número", pessoa: "Cliente/solicitante", responsavel: "Responsável", prioridade: "Prioridade", dataEmissao: "Data de emissão", prazoEtapa: "Prazo da etapa", prazoGeral: "Prazo geral", observacoes: "Observações", status: "Status", etapaId: "ID da etapa", etapaNome: "Etapa", dataConclusao: "Data de conclusão", dataCancelamento: "Data de cancelamento", motivoCancelamento: "Motivo do cancelamento" };
function valor(texto, campo) {
    if (texto === null) return "Não informado";
    if (campo === "arquivado" && (texto === "true" || texto === "false")) return texto === "true" ? "Sim" : "Não";
    return texto;
}
const horario = iso => `${iso.slice(8, 10)}/${iso.slice(5, 7)}/${iso.slice(0, 4)} ${iso.slice(11, 19)}`;

function Alteracao({ alteracao, registro }) {
    const ids = registro.alteracoes.find(a => a.campo === "etapaId" && a.alvoTipo === alteracao.alvoTipo && a.alvoId === alteracao.alvoId);
    const homonimas = alteracao.campo === "etapaNome" && alteracao.anterior === alteracao.novo && ids;
    return <div className="atividade-alteracao">
        <strong>{alteracao.alvoNome}: {campos[alteracao.campo]}</strong>
        <dl><div><dt>Antes</dt><dd>{homonimas ? `${alteracao.anterior} (ID ${ids.anterior})` : valor(alteracao.anterior, alteracao.campo)}</dd></div>
            <div><dt>Depois</dt><dd>{homonimas ? `${alteracao.novo} (ID ${ids.novo})` : valor(alteracao.novo, alteracao.campo)}</dd></div></dl>
    </div>;
}

function Painel({ gerenciamentoId, token, fechar }) {
    const [pagina, setPagina] = useState(null), [carregando, setCarregando] = useState(true), [erro, setErro] = useState(""), [naoAtualizada, setNaoAtualizada] = useState(false);
    const consulta = useRef(null), ocupado = useRef(false), vivo = useRef(true), botaoFechar = useRef(null);

    async function carregar(mais = false) {
        if (ocupado.current) return;
        ocupado.current = true; const controller = new AbortController(); consulta.current = controller;
        setCarregando(true); setErro("");
        try {
            const resposta = await consultarAtividade(token, gerenciamentoId, mais ? pagina.proximoCursor : null, { signal: controller.signal });
            if (!vivo.current || controller.signal.aborted) return;
            if (mais) {
                const chaves = new Set(pagina.registros.map(r => r.chave));
                if (resposta.registros.some(r => chaves.has(r.chave))) throw new Error("Não foi possível ler a próxima página de atividade.");
                setPagina({ ...resposta, registros: [...pagina.registros, ...resposta.registros] });
            } else { setPagina(resposta); setNaoAtualizada(false); }
        } catch (error) {
            if (vivo.current && !controller.signal.aborted) { setErro(error.message); if (!mais && pagina) setNaoAtualizada(true); }
        } finally {
            if (vivo.current && !controller.signal.aborted) { ocupado.current = false; setCarregando(false); }
        }
    }

    function encerrar() { vivo.current = false; consulta.current?.abort(); fechar(); }
    useEffect(() => {
        vivo.current = true; botaoFechar.current?.focus(); carregar();
        const escape = event => { if (event.key === "Escape") { event.preventDefault(); encerrar(); } };
        document.addEventListener("keydown", escape);
        return () => { vivo.current = false; consulta.current?.abort(); ocupado.current = false; document.removeEventListener("keydown", escape); };
    }, []);

    return <aside className="atividade-lateral" aria-labelledby="atividade-titulo" aria-busy={carregando}>
        <div className="atividade-topo"><h2 id="atividade-titulo">Atividade</h2><button type="button" className="btn-secundario" ref={botaoFechar} onClick={encerrar}>Fechar atividade</button></div>
        <p>Mais recentes primeiro</p>
        {carregando && <p role="status">Carregando atividade…</p>}
        {erro && <p role="alert" className="erro-login">{erro}</p>}
        {naoAtualizada && <p>Atividade não atualizada.</p>}
        {pagina && (pagina.registros.length === 0 ? <p>Nenhuma atividade registrada.</p> : <ol className="registros-atividade" aria-label="Registros de atividade">
            {pagina.registros.map(r => <li key={r.chave} data-atividade-chave={r.chave}>
                <h3>{r.origem === "HISTORICO_EXISTENTE" ? r.tipo : acoes[r.tipo]}</h3>
                <p>{r.alvoTipo === "QUADRO" ? "Quadro" : r.alvoTipo === "ETAPA" ? "Etapa" : "Demanda"}: {r.alvoNome}</p>
                {r.origem === "HISTORICO_EXISTENTE" && <p className="atividade-origem">Histórico existente</p>}
                <p>Autor: {valor(r.autorNome)}<br />Horário: {r.dataHora === null ? "Não informado" : <time dateTime={r.dataHora}>{horario(r.dataHora)}</time>}</p>
                {r.origem === "HISTORICO_EXISTENTE" ? <p className="atividade-texto">{valor(r.descricao)}</p> : r.alteracoes.length === 0 && ["QUADRO_EDITADO", "ETAPA_EDITADA", "DEMANDA_EDITADA"].includes(r.tipo) ? <p>Nenhum campo foi alterado</p> : null}
                {r.alteracoes.map((a, i) => <Alteracao key={i} alteracao={a} registro={r} />)}
            </li>)}
        </ol>)}
        <div className="atividade-controles">
            {pagina?.proximoCursor && <button type="button" className="btn-secundario" disabled={carregando} onClick={() => carregar(true)}>Carregar mais</button>}
            <button type="button" className="btn-secundario" disabled={carregando} onClick={() => carregar()}>{!pagina && erro ? "Tentar novamente" : "Atualizar"}</button>
        </div>
    </aside>;
}

export default function AtividadeLateral(props) {
    const token = localStorage.getItem("token");
    return <Painel key={`${props.gerenciamentoId}-${token}`} {...props} token={token} />;
}
