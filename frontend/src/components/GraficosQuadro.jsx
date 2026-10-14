import "./GraficosQuadro.css";

const situacoes = [["Em andamento", "Em andamento"], ["Concluido", "Concluídas"], ["Cancelado", "Canceladas"]];

function Barras({ titulo, linhas }) {
    const maior = Math.max(0, ...linhas.map(linha => linha.quantidade));
    return <div role="group" aria-label={titulo} className="grafico-grupo">
        <h4>{titulo}</h4>
        <ul>
            {linhas.map(linha => <li key={linha.id}>
                <div className="grafico-rotulo"><span>{linha.nome}</span><strong>{linha.quantidade}</strong></div>
                <div className="grafico-trilho" aria-hidden="true"><div className="grafico-barra" aria-hidden="true"
                    style={{ width: `${maior ? linha.quantidade / maior * 100 : 0}%` }} /></div>
            </li>)}
        </ul>
    </div>;
}

export default function GraficosQuadro({ etapas, demandas, demandasArquivadas }) {
    const porSituacao = situacoes.map(([status, nome]) => ({ id: status, nome, quantidade: demandas.filter(d => d.status === status).length }));
    const desconhecidas = demandas.filter(d => !situacoes.some(([status]) => d.status === status)).length;
    if (desconhecidas) porSituacao.push({ id: "desconhecida", nome: "Não informado", quantidade: desconhecidas });
    const contagem = new Map();
    for (const demanda of demandas) contagem.set(demanda.etapaId, (contagem.get(demanda.etapaId) || 0) + 1);
    const porEtapa = etapas.map(etapa => ({ id: etapa.id, nome: etapa.nome, quantidade: contagem.get(etapa.id) || 0 }));
    const ids = new Set(etapas.map(etapa => etapa.id));
    const semEtapa = demandas.filter(d => !ids.has(d.etapaId)).length;
    if (semEtapa) porEtapa.push({ id: "sem-etapa", nome: "Etapa não identificada", quantidade: semEtapa });
    return <section id="graficos-quadro" className="graficos-quadro" aria-labelledby="graficos-quadro-titulo">
        <h3 id="graficos-quadro-titulo">Gráficos do quadro</h3>
        <p>Situação atual dos dados carregados no quadro. Arquivadas são contadas separadamente.</p>
        <div className="graficos-totais"><strong>Total não arquivadas: {demandas.length}</strong><span>Arquivadas: {demandasArquivadas.length}</span></div>
        {demandas.length === 0 && <p>Nenhuma demanda não arquivada neste quadro.</p>}
        <div className="graficos-grade"><Barras titulo="Demandas por situação" linhas={porSituacao} /><Barras titulo="Demandas por etapa" linhas={porEtapa} /></div>
    </section>;
}
