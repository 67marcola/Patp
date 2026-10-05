import { useEffect, useRef, useState } from "react";
import {
    arquivarGerenciamento, buscarGerenciamento, listarGerenciamentos, restaurarGerenciamento
} from "../services/api";
import CriarGerenciamento from "./CriarGerenciamento";
import Quadro from "./Quadro";

const AVISO_RECARGA = "Alteração salva; não foi possível atualizar a lista.";

function Gerenciamentos() {
    const [gerenciamentos, setGerenciamentos] = useState([]);
    const [arquivado, setArquivado] = useState(false);
    const [carregando, setCarregando] = useState(true);
    const [erroConsulta, setErroConsulta] = useState("");
    const [erroAcao, setErroAcao] = useState("");
    const [tela, setTela] = useState("lista");
    const [selecionado, setSelecionado] = useState(null);
    const [confirmacao, setConfirmacao] = useState(null);
    const [ocupado, setOcupado] = useState(false);
    const consulta = useRef(0);
    const operando = useRef(false);
    const salvoSemRecarga = useRef(false);
    const origemConfirmacao = useRef(null);
    const cancelarConfirmacao = useRef(null);

    async function carregarGerenciamentos(filtro, aposMutacao = false) {
        const atual = ++consulta.current;
        if (aposMutacao) salvoSemRecarga.current = true;
        setCarregando(true);
        setErroConsulta("");
        try {
            const dados = await listarGerenciamentos(localStorage.getItem("token"), filtro);
            if (atual !== consulta.current) return;
            setGerenciamentos(dados);
            salvoSemRecarga.current = false;
        } catch (error) {
            if (atual === consulta.current) setErroConsulta(salvoSemRecarga.current ? AVISO_RECARGA : error.message);
        } finally {
            if (atual === consulta.current) setCarregando(false);
        }
    }

    useEffect(() => {
        carregarGerenciamentos(arquivado);
        return () => { consulta.current += 1; };
    }, [arquivado]);

    useEffect(() => {
        if (confirmacao) cancelarConfirmacao.current?.focus();
    }, [confirmacao]);

    function mudarFiltro(valor) {
        if (valor === arquivado || operando.current) return;
        descartarConfirmacao();
        setErroAcao("");
        setGerenciamentos([]);
        setArquivado(valor);
    }

    async function abrir(id, destino) {
        if (operando.current) return;
        descartarConfirmacao();
        operando.current = true;
        setOcupado(true);
        setErroAcao("");
        try {
            const atualizado = await buscarGerenciamento(localStorage.getItem("token"), id);
            setGerenciamentos(anteriores => anteriores.flatMap(item => item.id !== id
                ? [item] : atualizado.arquivado === arquivado ? [atualizado] : []));
            if (destino === "editar" && !atualizado.podeAdministrar) {
                setErroAcao("Você não tem permissão para administrar este gerenciamento.");
                return;
            }
            if (destino === "editar" && atualizado.arquivado) {
                setErroAcao("Gerenciamento arquivado. Restaure-o antes de alterar.");
                return;
            }
            setSelecionado(atualizado);
            setTela(destino);
        } catch (error) {
            setErroAcao(error.message);
        } finally {
            operando.current = false;
            setOcupado(false);
        }
    }

    async function formularioSalvo(salvo) {
        setSelecionado(salvo);
        setGerenciamentos(anteriores => {
            const outros = anteriores.filter(item => item.id !== salvo.id);
            return salvo.arquivado === arquivado ? [salvo, ...outros] : outros;
        });
        await carregarGerenciamentos(arquivado, true);
    }

    function quadroAtualizado(atualizado) {
        setSelecionado(atualizado);
        setGerenciamentos(anteriores => anteriores.flatMap(item => item.id !== atualizado.id
            ? [item] : atualizado.arquivado === arquivado ? [atualizado] : []));
    }

    function descartarConfirmacao() {
        setConfirmacao(null);
        origemConfirmacao.current = null;
    }

    function cancelarArquivo() {
        setConfirmacao(null);
        origemConfirmacao.current?.focus();
    }

    async function mudarEstado(item, paraArquivado) {
        if (operando.current) return;
        operando.current = true;
        setOcupado(true);
        setErroAcao("");
        try {
            const executar = paraArquivado ? arquivarGerenciamento : restaurarGerenciamento;
            await executar(localStorage.getItem("token"), item.id, item.versao);
            setConfirmacao(null);
            setGerenciamentos(anteriores => anteriores.filter(registro => registro.id !== item.id));
            await carregarGerenciamentos(arquivado, true);
        } catch (error) {
            setErroAcao(error.message);
        } finally {
            operando.current = false;
            setOcupado(false);
        }
    }

    if (tela === "criar" || tela === "editar") {
        return <CriarGerenciamento gerenciamento={tela === "editar" ? selecionado : null}
            voltar={() => setTela("lista")} atualizar={formularioSalvo} />;
    }
    if (tela === "quadro") {
        return <Quadro gerenciamento={selecionado} voltar={() => setTela("lista")} atualizar={quadroAtualizado} />;
    }

    return (
        <div className="gerenciamentos-page">
            <div className="gerenciamentos-topo">
                <div>
                    <h2>Gerenciamentos</h2>
                    <p>Quadros compartilhados para acompanhar os processos.</p>
                </div>
                <button className="btn-criar" disabled={ocupado}
                    onClick={() => { descartarConfirmacao(); setErroAcao(""); setTela("criar"); }}>+ Criar gerenciamento</button>
            </div>
            <div className="filtro-gerenciamentos" role="group" aria-label="Situação dos gerenciamentos">
                <button className="btn-secundario" aria-pressed={!arquivado} disabled={ocupado}
                    onClick={() => mudarFiltro(false)}>Ativos</button>
                <button className="btn-secundario" aria-pressed={arquivado} disabled={ocupado}
                    onClick={() => mudarFiltro(true)}>Arquivados</button>
            </div>
            {erroAcao && <div className="erro-login" role="alert">{erroAcao}</div>}
            {erroConsulta && (
                <div className="falha-consulta">
                    <div className="erro-login" role="alert">{erroConsulta}</div>
                    <button className="btn-secundario" disabled={ocupado || carregando}
                        onClick={() => carregarGerenciamentos(arquivado)}>Tentar atualizar</button>
                </div>
            )}
            {carregando && <p role="status">Carregando gerenciamentos...</p>}
            {!carregando && !erroConsulta && gerenciamentos.length === 0 && (
                <div className="vazio">
                    <h3>{arquivado ? "Nenhum gerenciamento arquivado" : "Nenhum gerenciamento ativo"}</h3>
                    <p>{arquivado ? "Os quadros arquivados ficam disponíveis aqui para consulta e restauração." : "Crie um gerenciamento para começar."}</p>
                </div>
            )}
            {!carregando && (
                <div className="lista-gerenciamentos">
                    {gerenciamentos.map(item => (
                        <article className="card-gerenciamento" key={item.id}>
                            <div className="card-indicador"></div>
                            <div className="card-conteudo">
                                <h3>{item.nome}</h3>
                                <p>{item.descricao || "Sem descrição cadastrada."}</p>
                                <p className="quadro-autoria">{item.criador ? `Criado por ${item.criador.nome}` : "Criador não identificado"}</p>
                            </div>
                            <div className="card-acoes">
                                <button className="btn-abrir" aria-label={`Abrir ${item.nome}`} disabled={ocupado}
                                    onClick={() => abrir(item.id, "quadro")}>Abrir →</button>
                                {item.podeAdministrar && !item.arquivado && <>
                                    <button className="btn-secundario" aria-label={`Editar ${item.nome}`} disabled={ocupado}
                                        onClick={() => abrir(item.id, "editar")}>Editar</button>
                                    <button className="btn-secundario" aria-label={`Arquivar ${item.nome}`} disabled={ocupado}
                                        onClick={event => { origemConfirmacao.current = event.currentTarget; setConfirmacao(item); }}>Arquivar</button>
                                </>}
                                {item.podeAdministrar && item.arquivado &&
                                    <button className="btn-secundario" aria-label={`Restaurar ${item.nome}`} disabled={ocupado}
                                        onClick={() => mudarEstado(item, false)}>Restaurar</button>}
                            </div>
                        </article>
                    ))}
                </div>
            )}
            {confirmacao && (
                <section className="confirmacao-arquivo" role="dialog"
                    aria-labelledby="arquivar-titulo" aria-describedby="arquivar-descricao">
                    <h3 id="arquivar-titulo">Arquivar {confirmacao.nome}?</h3>
                    <p id="arquivar-descricao">Os dados e vínculos serão preservados. O quadro ficará somente para consulta até ser restaurado.</p>
                    <div className="acoes-form">
                        <button className="btn-secundario" ref={cancelarConfirmacao} disabled={ocupado} onClick={cancelarArquivo}>Cancelar</button>
                        <button className="btn-criar" disabled={ocupado} onClick={() => mudarEstado(confirmacao, true)}>
                            {ocupado ? "Arquivando..." : "Arquivar"}
                        </button>
                    </div>
                </section>
            )}
        </div>
    );
}

export default Gerenciamentos;
