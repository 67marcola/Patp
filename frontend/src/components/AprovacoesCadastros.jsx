import { useCallback, useEffect, useRef, useState } from "react";
import { ApiError, aprovarCadastro, listarCadastrosPendentes } from "../services/api";
import "./AprovacoesCadastros.css";

export default function AprovacoesCadastros({ token, aoFechar, aoOcupar }) {
    const [pendentes, setPendentes] = useState(null);
    const [carregando, setCarregando] = useState(true);
    const [aprovando, setAprovando] = useState(false);
    const [erro, setErro] = useState("");
    const [mensagem, setMensagem] = useState("");
    const [confirmada, setConfirmada] = useState(false);
    const operando = useRef(false), requisicao = useRef(null), titulo = useRef(null);

    const carregar = useCallback(async () => {
        if (operando.current && !requisicao.current?.signal.aborted) return;
        operando.current = true;
        const controller = new AbortController(); requisicao.current = controller;
        setCarregando(true); setErro(""); setMensagem(""); setConfirmada(false);
        try {
            const lista = await listarCadastrosPendentes(token, { signal: controller.signal });
            if (!controller.signal.aborted) { setPendentes(lista); setConfirmada(true); }
        } catch (error) {
            if (!controller.signal.aborted) setErro(error.message);
        } finally {
            if (requisicao.current === controller) {
                operando.current = false;
                if (!controller.signal.aborted) setCarregando(false);
            }
        }
    }, [token]);

    useEffect(() => {
        titulo.current?.focus();
        carregar();
        return () => requisicao.current?.abort();
    }, [carregar]);

    async function aprovar(conta) {
        if (operando.current || !confirmada) return;
        operando.current = true; setAprovando(true); aoOcupar?.(true);
        setErro(""); setMensagem("");
        try {
            await aprovarCadastro(token, conta.id);
            setPendentes(atuais => atuais.filter(atual => atual.id !== conta.id));
            setMensagem(`Cadastro de ${conta.nome} aprovado.`);
        } catch (error) {
            const incerto = error instanceof ApiError && (error.status === 0 || error.status >= 200 && error.status < 300);
            if (incerto) setConfirmada(false);
            setErro(incerto ? "Não foi possível confirmar a aprovação. Atualize a lista antes de tentar novamente." : error.message);
        } finally {
            operando.current = false; setAprovando(false); aoOcupar?.(false); titulo.current?.focus();
        }
    }

    return <section className="aprovacoes-cadastros" aria-labelledby="aprovacoes-titulo">
        <div className="aprovacoes-cabecalho">
            <h2 id="aprovacoes-titulo" tabIndex={-1} ref={titulo}>Cadastros aguardando aprovação</h2>
            <button type="button" onClick={aoFechar} disabled={aprovando}>Fechar aprovações</button>
        </div>
        <p>Autorize cada pessoa antes que ela acesse os dados da equipe.</p>
        <button type="button" onClick={carregar} disabled={carregando || aprovando}>Atualizar lista</button>
        {carregando && <p role="status">Carregando cadastros...</p>}
        {erro && <p role="alert">{erro}</p>}
        {mensagem && <p role="status">{mensagem}</p>}
        {!carregando && pendentes?.length === 0 && <p>Nenhum cadastro pendente.</p>}
        {pendentes && <ul className="aprovacoes-lista">
            {pendentes.map(conta => <li key={conta.id}>
                <div><strong>{conta.nome}</strong><span>{conta.setor || "Setor não informado"}</span><span>{conta.email}</span></div>
                <button type="button" aria-label={`Aprovar ${conta.nome}`} disabled={carregando || aprovando || !confirmada}
                    onClick={() => aprovar(conta)}>{aprovando ? "Aprovando..." : "Aprovar"}</button>
            </li>)}
        </ul>}
    </section>;
}
