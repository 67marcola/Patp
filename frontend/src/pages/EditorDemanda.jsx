import { useEffect, useRef, useState } from "react";

const campos = [
    { nome: "numeroProcesso", label: "Número da demanda", obrigatorio: true, limite: 255,
        mensagem: "Informe um número de demanda entre 1 e 255 caracteres." },
    { nome: "pessoa", label: "Cliente/solicitante", obrigatorio: true, limite: 255,
        mensagem: "Informe um cliente/solicitante entre 1 e 255 caracteres." },
    { nome: "responsavel", label: "Responsável", limite: 255, mensagem: "Informe um responsável com até 255 caracteres." },
    { nome: "prioridade", label: "Prioridade", limite: 255, mensagem: "Informe uma prioridade com até 255 caracteres." },
    { nome: "dataEmissao", label: "Data de emissão", tipo: "date" },
    { nome: "prazoEtapa", label: "Prazo da etapa", tipo: "date" },
    { nome: "prazoGeral", label: "Prazo geral", tipo: "date" },
    { nome: "observacoes", label: "Observações", tipo: "textarea", limite: 10000,
        mensagem: "Informe observações com até 10000 caracteres." }
];

function EditorDemanda({ salvar, cancelar, aoErro, bloqueado = false }) {
    const [valores, setValores] = useState(() => Object.fromEntries(campos.map(campo => [campo.nome, ""])));
    const [erro, setErro] = useState("");
    const [salvando, setSalvando] = useState(false);
    const enviando = useRef(false);
    const numero = useRef(null);
    const desabilitado = salvando || bloqueado;

    useEffect(() => { numero.current?.focus(); }, []);

    async function enviar(event) {
        event.preventDefault();
        if (enviando.current || bloqueado) return;
        setErro("");
        const dados = {};
        for (const campo of campos) {
            const valor = valores[campo.nome].trim();
            if ((campo.obrigatorio && !valor) || (campo.limite && valor.length > campo.limite)) {
                setErro(campo.mensagem);
                return;
            }
            dados[campo.nome] = valor || null;
        }
        enviando.current = true;
        setSalvando(true);
        try {
            await salvar(dados);
        } catch (error) {
            setErro(error.message);
            aoErro?.(error);
        } finally {
            enviando.current = false;
            setSalvando(false);
        }
    }

    return <form className="form-card editor-demanda" onSubmit={enviar} noValidate aria-busy={salvando}
        aria-labelledby="editor-demanda-titulo">
        <h3 id="editor-demanda-titulo">Novo processo</h3>
        <p className="ajuda-campo">A demanda começará automaticamente na primeira etapa de trabalho.</p>
        <div className="campos-demanda">
            {campos.map(campo => {
                const propriedades = { id: `demanda-${campo.nome}`, value: valores[campo.nome], disabled: desabilitado,
                    required: !!campo.obrigatorio, "aria-describedby": "demanda-ajuda",
                    onChange: event => setValores(atuais => ({ ...atuais, [campo.nome]: event.target.value })) };
                return <div className={`campo-form${campo.tipo === "textarea" ? " campo-observacoes" : ""}`} key={campo.nome}>
                    <label htmlFor={propriedades.id}>{campo.label}</label>
                    {campo.tipo === "textarea" ? <textarea {...propriedades} rows={4} />
                        : <input {...propriedades} type={campo.tipo || "text"} ref={campo.nome === "numeroProcesso" ? numero : null} />}
                </div>;
            })}
        </div>
        <p id="demanda-ajuda" className="ajuda-campo">Número e cliente/solicitante são obrigatórios; os demais campos são opcionais.</p>
        {erro && <div className="erro-login" role="alert">{erro}</div>}
        <div className="acoes-form">
            <button type="button" className="btn-secundario" disabled={salvando} onClick={cancelar}>Cancelar</button>
            <button type="submit" className="btn-criar" disabled={desabilitado}>{salvando ? "Criando..." : "Criar demanda"}</button>
        </div>
    </form>;
}

export default EditorDemanda;
