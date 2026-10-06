import { useEffect, useRef, useState } from "react";
import { buscarConfiguracaoEtapas, criarEtapa, editarEtapa, removerEtapa } from "../services/api";
import EditorEtapa from "./EditorEtapa";

function etapaFinal(etapa) {
    return etapa?.categoria === "CONCLUIDA" || etapa?.categoria === "CANCELADA";
}

function Quadro({ gerenciamento, voltar, atualizar, aoOcupar }) {
    const [configuracao, setConfiguracao] = useState(null);
    const [carregando, setCarregando] = useState(true);
    const [erroConsulta, setErroConsulta] = useState("");
    const [tentativa, setTentativa] = useState(0);
    const [editor, setEditor] = useState(null);
    const [confirmacao, setConfirmacao] = useState(null);
    const [erroRemocao, setErroRemocao] = useState("");
    const [precisaAtualizar, setPrecisaAtualizar] = useState(false);
    const [ocupado, setOcupado] = useState(false);
    const operando = useRef(false);
    const origem = useRef(null);
    const cancelarConfirmacao = useRef(null);
    const novaEtapa = useRef(null);
    const quadro = configuracao?.gerenciamento || gerenciamento;
    const etapas = configuracao?.etapas || [];
    const trabalhos = etapas.filter(etapa => !etapaFinal(etapa));
    const podeConfigurar = !!configuracao && quadro.podeAdministrar && !quadro.arquivado;
    const bloqueado = ocupado || carregando || precisaAtualizar;

    function finalNoSnapshot(etapa) {
        return etapaFinal(etapa) || etapaFinal(etapas.find(atual => atual.id === etapa?.id));
    }

    function aplicarConfiguracao(dados) {
        setConfiguracao(dados);
        atualizar?.(dados.gerenciamento);
    }

    useEffect(() => {
        const controller = new AbortController();
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
        else if (!editor && origem.current) {
            if (origem.current.isConnected) origem.current.focus();
            else novaEtapa.current?.focus();
        }
    }, [editor, confirmacao]);

    function abrirEditor(event, etapa = null) {
        if (bloqueado || editor || confirmacao || !podeConfigurar || finalNoSnapshot(etapa)) return;
        origem.current = event.currentTarget;
        setErroRemocao("");
        setEditor({ etapa });
    }

    function abrirRemocao(event, etapa) {
        if (bloqueado || editor || confirmacao || !podeConfigurar || finalNoSnapshot(etapa)) return;
        origem.current = event.currentTarget;
        setErroRemocao("");
        setConfirmacao(etapa);
    }

    function cancelar() {
        if (operando.current) return;
        setEditor(null);
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
            aplicarConfiguracao(salvo);
            setEditor(null);
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
            aplicarConfiguracao(salvo);
            setConfirmacao(null);
        } catch (error) {
            setErroRemocao(error.message);
            setPrecisaAtualizar(true);
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
                    <button className="btn-voltar" onClick={voltar} disabled={ocupado}>← Voltar</button>
                    <h2>{quadro.nome}</h2>
                    <p>{quadro.descricao || "Controle e acompanhamento dos processos."}</p>
                    {quadro.arquivado && <p className="aviso-arquivado">Arquivado — somente consulta</p>}
                </div>
                <div className="quadro-acoes">
                    {podeConfigurar && <button className="btn-criar" ref={novaEtapa}
                        disabled={bloqueado || !!editor || !!confirmacao} onClick={abrirEditor}>+ Nova etapa</button>}
                    {!quadro.arquivado && <button className="btn-criar" disabled={ocupado}>+ Criar processo</button>}
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
                aoErro={() => setPrecisaAtualizar(true)} />}
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
            </div>}
            {configuracao && <div className="etapas-quadro">
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
                                <button className="btn-secundario" disabled={bloqueado || !!editor || !!confirmacao}
                                    aria-label={`Editar etapa ${posicao}: ${etapa.nome}`}
                                    onClick={event => abrirEditor(event, { ...etapa, ordem: posicao })}>Editar</button>
                                <button className="btn-secundario" disabled={bloqueado || !!editor || !!confirmacao}
                                    aria-label={`Remover etapa ${posicao}: ${etapa.nome}`}
                                    onClick={event => abrirRemocao(event, etapa)}>Remover</button>
                            </div>}
                        </div>
                        <div className="quadro-vazio"><p>{quantidade === 0 ? "Nenhum processo nesta etapa."
                            : `${quantidade} ${quantidade === 1 ? "demanda vinculada" : "demandas vinculadas"} a esta etapa.`}</p></div>
                    </section>;
                })}
            </div>}
        </main>
    );
}

export default Quadro;
