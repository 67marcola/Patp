import { useEffect, useRef, useState } from "react";
import { buscarConfiguracaoEtapas, criarDemanda, criarEtapa, editarEtapa, removerEtapa, transicionarDemanda } from "../services/api";
import EditorEtapa from "./EditorEtapa";
import EditorDemanda from "./EditorDemanda";
import AcaoDemanda from "./AcaoDemanda";

function acoesDaDemanda(demanda) {
    if (demanda?.status === "Em andamento") return ["mover", "concluir", "cancelar"];
    if (demanda?.status === "Concluido" || demanda?.status === "Cancelado") return ["reabrir"];
    return [];
}

const nomesAcoes = { mover: "Mover/pular etapa", concluir: "Concluir", cancelar: "Cancelar", reabrir: "Reabrir" };

function etapaFinal(etapa) {
    return etapa?.categoria === "CONCLUIDA" || etapa?.categoria === "CANCELADA";
}

function texto(valor) {
    return valor === null || valor === "" ? "Não informado" : valor;
}

function data(valor) {
    return valor ? valor.split("-").reverse().join("/") : "Não informado";
}

function Quadro({ gerenciamento, voltar, atualizar, aoOcupar }) {
    const [configuracao, setConfiguracao] = useState(null);
    const [carregando, setCarregando] = useState(true);
    const [erroConsulta, setErroConsulta] = useState("");
    const [tentativa, setTentativa] = useState(0);
    const [editor, setEditor] = useState(null);
    const [editorDemanda, setEditorDemanda] = useState(false);
    const [acaoDemanda, setAcaoDemanda] = useState(null);
    const [confirmacao, setConfirmacao] = useState(null);
    const [erroRemocao, setErroRemocao] = useState("");
    const [precisaAtualizar, setPrecisaAtualizar] = useState(false);
    const [ocupado, setOcupado] = useState(false);
    const operando = useRef(false);
    const origem = useRef(null);
    const cancelarConfirmacao = useRef(null);
    const novaEtapa = useRef(null);
    const criarProcesso = useRef(null);
    const botaoVoltar = useRef(null);
    const quadroAtual = useRef(gerenciamento.id);
    const quadroAnterior = useRef(gerenciamento.id);
    const montado = useRef(true);
    quadroAtual.current = gerenciamento.id;
    const snapshot = configuracao?.gerenciamento.id === gerenciamento.id ? configuracao : null;
    const quadro = snapshot?.gerenciamento || gerenciamento;
    const etapas = snapshot?.etapas || [];
    const demandas = snapshot?.demandas || [];
    const trabalhos = etapas.filter(etapa => !etapaFinal(etapa));
    const podeConfigurar = !!snapshot && quadro.podeAdministrar && !quadro.arquivado;
    const podeCriar = !!snapshot && !quadro.arquivado && trabalhos.length > 0;
    const bloqueado = ocupado || carregando || precisaAtualizar || !!erroConsulta || !snapshot;
    const acaoAtual = acaoDemanda?.gerenciamentoId === gerenciamento.id ? acaoDemanda : null;
    const demandaAtual = acaoAtual && demandas.find(demanda => demanda.id === acaoAtual.demanda.id);
    const podeExecutarAcao = podeConfigurar && !!demandaAtual && acoesDaDemanda(demandaAtual).includes(acaoAtual.acao);
    const formularioAberto = !!editor || !!confirmacao || editorDemanda || !!acaoAtual;

    function finalNoSnapshot(etapa) {
        return etapaFinal(etapa) || etapaFinal(etapas.find(atual => atual.id === etapa?.id));
    }

    function aplicarConfiguracao(dados) {
        if (!montado.current || dados.gerenciamento.id !== quadroAtual.current) return false;
        setConfiguracao(dados);
        atualizar?.(dados.gerenciamento);
        return true;
    }

    useEffect(() => {
        montado.current = true;
        return () => { montado.current = false; };
    }, []);

    useEffect(() => {
        const controller = new AbortController();
        if (quadroAnterior.current !== gerenciamento.id) {
            quadroAnterior.current = gerenciamento.id;
            setConfiguracao(null);
            setEditor(null);
            setEditorDemanda(false);
            setAcaoDemanda(null);
            setConfirmacao(null);
            setPrecisaAtualizar(false);
            setErroRemocao("");
            origem.current = null;
        }
        setCarregando(true);
        setErroConsulta("");
        async function carregar() {
            try {
                const dados = await buscarConfiguracaoEtapas(localStorage.getItem("token"), gerenciamento.id, { signal: controller.signal });
                if (!controller.signal.aborted) {
                    aplicarConfiguracao(dados);
                    setPrecisaAtualizar(false);
                }
            } catch (error) {
                if (!controller.signal.aborted) setErroConsulta(error.message);
            } finally {
                if (!controller.signal.aborted) setCarregando(false);
            }
        }
        carregar();
        return () => controller.abort();
    }, [gerenciamento.id, tentativa]);

    useEffect(() => {
        if (confirmacao) cancelarConfirmacao.current?.focus();
        else if (!editor && !editorDemanda && !acaoAtual && origem.current) {
            if (origem.current.isConnected) origem.current.focus();
            else (novaEtapa.current || criarProcesso.current || botaoVoltar.current)?.focus();
        }
    }, [editor, editorDemanda, confirmacao, acaoAtual]);

    function abrirEditor(event, etapa = null) {
        if (bloqueado || formularioAberto || !podeConfigurar || finalNoSnapshot(etapa)) return;
        origem.current = event.currentTarget;
        setErroRemocao("");
        setEditor({ etapa });
    }

    function abrirRemocao(event, etapa) {
        if (bloqueado || formularioAberto || !podeConfigurar || finalNoSnapshot(etapa)) return;
        origem.current = event.currentTarget;
        setErroRemocao("");
        setConfirmacao(etapa);
    }

    function abrirDemanda(event) {
        if (bloqueado || formularioAberto || !podeCriar) return;
        origem.current = event.currentTarget;
        setEditorDemanda(true);
    }

    function abrirAcao(event, demanda, acao) {
        if (bloqueado || formularioAberto || !podeConfigurar || !acoesDaDemanda(demanda).includes(acao)) return;
        origem.current = event.currentTarget;
        setAcaoDemanda({ gerenciamentoId: quadro.id, demanda, acao });
    }

    function cancelar() {
        if (operando.current) return;
        setEditor(null);
        setEditorDemanda(false);
        setAcaoDemanda(null);
        setConfirmacao(null);
        setErroRemocao("");
    }

    async function salvar(dados) {
        if (operando.current || bloqueado || !podeConfigurar || !editor || finalNoSnapshot(editor.etapa)) return;
        operando.current = true;
        aoOcupar?.(true);
        setOcupado(true);
        try {
            const token = localStorage.getItem("token");
            const payload = { ...dados, versao: quadro.versao };
            const salvo = editor.etapa
                ? await editarEtapa(token, quadro.id, editor.etapa.id, payload)
                : await criarEtapa(token, quadro.id, payload);
            if (aplicarConfiguracao(salvo)) setEditor(null);
        } finally {
            operando.current = false;
            aoOcupar?.(false);
            setOcupado(false);
        }
    }

    async function salvarDemanda(dados) {
        if (operando.current || bloqueado || !podeCriar || !editorDemanda) return;
        operando.current = true;
        aoOcupar?.(true);
        setOcupado(true);
        try {
            const salvo = await criarDemanda(localStorage.getItem("token"), quadro.id, { ...dados, versao: quadro.versao });
            if (aplicarConfiguracao(salvo)) setEditorDemanda(false);
        } finally {
            operando.current = false;
            aoOcupar?.(false);
            setOcupado(false);
        }
    }

    async function salvarAcao(dados) {
        if (operando.current || bloqueado || !podeExecutarAcao) return;
        operando.current = true;
        aoOcupar?.(true);
        setOcupado(true);
        try {
            const salvo = await transicionarDemanda(localStorage.getItem("token"), quadro.id, demandaAtual, acaoAtual.acao,
                { ...dados, versao: quadro.versao });
            if (aplicarConfiguracao(salvo)) setAcaoDemanda(null);
        } finally {
            operando.current = false;
            aoOcupar?.(false);
            setOcupado(false);
        }
    }

    async function remover() {
        if (operando.current || bloqueado || !podeConfigurar || !confirmacao || finalNoSnapshot(confirmacao)) return;
        operando.current = true;
        aoOcupar?.(true);
        setOcupado(true);
        setErroRemocao("");
        try {
            const salvo = await removerEtapa(localStorage.getItem("token"), quadro.id, confirmacao.id, quadro.versao);
            if (aplicarConfiguracao(salvo)) setConfirmacao(null);
        } catch (error) {
            if (montado.current && quadro.id === quadroAtual.current) {
                setErroRemocao(error.message);
                setPrecisaAtualizar(true);
            }
        } finally {
            operando.current = false;
            aoOcupar?.(false);
            setOcupado(false);
        }
    }

    return (
        <main className="quadro-page">
            <div className="quadro-topo">
                <div>
                    <button className="btn-voltar" ref={botaoVoltar} onClick={voltar} disabled={ocupado}>← Voltar</button>
                    <h2>{quadro.nome}</h2>
                    <p>{quadro.descricao || "Controle e acompanhamento dos processos."}</p>
                    {quadro.arquivado && <p className="aviso-arquivado">Arquivado — somente consulta</p>}
                </div>
                <div className="quadro-acoes">
                    {podeConfigurar && <button className="btn-criar" ref={novaEtapa}
                        disabled={bloqueado || formularioAberto} onClick={abrirEditor}>+ Nova etapa</button>}
                    {!quadro.arquivado && <button className="btn-criar" ref={criarProcesso}
                        disabled={bloqueado || formularioAberto || !podeCriar} onClick={abrirDemanda}>+ Criar processo</button>}
                </div>
            </div>
            {carregando && <p role="status">Carregando quadro...</p>}
            {erroConsulta && <div className="falha-consulta">
                <div className="erro-login" role="alert">{erroConsulta}</div>
                <button className="btn-secundario" disabled={ocupado || carregando}
                    onClick={() => setTentativa(valor => valor + 1)}>Tentar novamente</button>
            </div>}
            {precisaAtualizar && !erroConsulta && <div className="falha-consulta">
                <p>Atualize o quadro antes de tentar uma nova alteração.</p>
                <button className="btn-secundario" disabled={ocupado || carregando}
                    onClick={() => setTentativa(valor => valor + 1)}>Atualizar quadro</button>
            </div>}
            {editor && <EditorEtapa etapa={editor.etapa} quantidadeEtapas={trabalhos.length}
                salvar={salvar} cancelar={cancelar} bloqueado={bloqueado || !podeConfigurar || finalNoSnapshot(editor.etapa)}
                aoErro={() => { if (montado.current && quadro.id === quadroAtual.current) setPrecisaAtualizar(true); }} />}
            {editorDemanda && <EditorDemanda salvar={salvarDemanda} cancelar={cancelar} bloqueado={bloqueado || !podeCriar}
                aoErro={() => { if (montado.current && quadro.id === quadroAtual.current) setPrecisaAtualizar(true); }} />}
            {acaoAtual && <AcaoDemanda acao={acaoAtual.acao} demanda={demandaAtual || acaoAtual.demanda} etapas={etapas}
                salvar={salvarAcao} cancelar={cancelar} bloqueado={bloqueado || !podeExecutarAcao}
                aoErro={() => { if (montado.current && quadro.id === quadroAtual.current) setPrecisaAtualizar(true); }} />}
            {confirmacao && <section className="confirmacao-arquivo confirmacao-etapa" role="dialog"
                aria-labelledby="remover-etapa-titulo" aria-describedby="remover-etapa-descricao">
                <h3 id="remover-etapa-titulo">Remover {confirmacao.nome}?</h3>
                <p id="remover-etapa-descricao">Demandas impedem a remoção. Mova-as para outra etapa antes de remover esta coluna.</p>
                {erroRemocao && <div className="erro-login" role="alert">{erroRemocao}</div>}
                <div className="acoes-form">
                    <button className="btn-secundario" ref={cancelarConfirmacao} disabled={ocupado} onClick={cancelar}>Cancelar</button>
                    <button className="btn-criar" disabled={bloqueado || !podeConfigurar || finalNoSnapshot(confirmacao)} onClick={remover}>
                        {ocupado ? "Removendo..." : "Remover"}
                    </button>
                </div>
            </section>}
            {!carregando && !erroConsulta && trabalhos.length === 0 && <div className="vazio">
                <h3>Nenhuma etapa de trabalho cadastrada</h3>
                <p>Este gerenciamento ainda não possui etapas de trabalho.</p>
                {!quadro.arquivado && <p>{quadro.podeAdministrar
                    ? "Cadastre uma etapa de trabalho usando Nova etapa antes de criar demandas."
                    : "Solicite ao criador do quadro ou a um administrador que cadastre uma etapa de trabalho."}</p>}
            </div>}
            {snapshot && <div className="etapas-quadro">
                {etapas.map(etapa => {
                    const quantidade = etapa.quantidadeDemandas || 0;
                    const final = etapaFinal(etapa);
                    const posicao = trabalhos.indexOf(etapa) + 1;
                    return <section className="etapa" key={etapa.id} aria-labelledby={`etapa-titulo-${etapa.id}`}>
                        <div className="etapa-cabecalho">
                            <div className="titulo-etapa">
                                <span className="indicador verde"></span>
                                <div>
                                    <h2 id={`etapa-titulo-${etapa.id}`}>{etapa.nome}</h2>
                                    {final ? <p>Etapa final obrigatória</p> : <>
                                        <p>Setor responsável: {etapa.setor || "Não informado"}</p>
                                        <p>Posição {posicao}</p>
                                    </>}
                                </div>
                            </div>
                            <span className="quantidade">{quantidade} {quantidade === 1 ? "demanda" : "demandas"}</span>
                            {podeConfigurar && !final && <div className="etapa-acoes">
                                <button className="btn-secundario" disabled={bloqueado || formularioAberto}
                                    aria-label={`Editar etapa ${posicao}: ${etapa.nome}`}
                                    onClick={event => abrirEditor(event, { ...etapa, ordem: posicao })}>Editar</button>
                                <button className="btn-secundario" disabled={bloqueado || formularioAberto}
                                    aria-label={`Remover etapa ${posicao}: ${etapa.nome}`}
                                    onClick={event => abrirRemocao(event, etapa)}>Remover</button>
                            </div>}
                        </div>
                        <div className="quadro-vazio"><p>{quantidade === 0 ? "Nenhum processo nesta etapa."
                            : `${quantidade} ${quantidade === 1 ? "demanda vinculada" : "demandas vinculadas"} a esta etapa.`}</p></div>
                        {quantidade > 0 && <div className="demandas-etapa">
                            {demandas.filter(demanda => demanda.etapaId === etapa.id).map(demanda => {
                                const campos = [
                                    ["Status", texto(demanda.status)], ["Responsável", texto(demanda.responsavel)],
                                    ["Prioridade", texto(demanda.prioridade)], ["Data de emissão", data(demanda.dataEmissao)],
                                    ["Prazo da etapa", data(demanda.prazoEtapa)], ["Prazo geral", data(demanda.prazoGeral)],
                                    ["Data de conclusão", data(demanda.dataConclusao)], ["Data de cancelamento", data(demanda.dataCancelamento)],
                                    ["Observações", texto(demanda.observacoes)]
                                ];
                                if (demanda.motivoCancelamento) campos.push(["Motivo do cancelamento", demanda.motivoCancelamento]);
                                return <article className="card-demanda" key={demanda.id} data-demanda-id={demanda.id}
                                    aria-labelledby={`demanda-titulo-${demanda.id}`}>
                                    <strong id={`demanda-titulo-${demanda.id}`}>{texto(demanda.numeroProcesso)} — {texto(demanda.pessoa)}</strong>
                                    <dl>{campos.map(([label, valor]) => <div key={label}
                                        className={label === "Observações" || label === "Motivo do cancelamento" ? "campo-longo" : undefined}>
                                        <dt>{label}</dt><dd>{valor}</dd>
                                    </div>)}</dl>
                                    {acoesDaDemanda(demanda).length === 0 && <p>Status antigo não reconhecido. Solicite a correção do registro.</p>}
                                    {podeConfigurar && acoesDaDemanda(demanda).length > 0 && <div className="acoes-form">
                                        {acoesDaDemanda(demanda).map(acao => <button key={acao} className="btn-secundario"
                                            disabled={bloqueado || formularioAberto} aria-label={`${nomesAcoes[acao]} demanda ${demanda.numeroProcesso}`}
                                            onClick={event => abrirAcao(event, demanda, acao)}>{nomesAcoes[acao]}</button>)}
                                    </div>}
                                </article>;
                            })}
                        </div>}
                    </section>;
                })}
            </div>}
        </main>
    );
}

export default Quadro;
