package com.patp.sistema.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.patp.sistema.model.Atividade;
import com.patp.sistema.model.Etapa;
import com.patp.sistema.model.Gerenciamento;
import com.patp.sistema.model.Processo;
import com.patp.sistema.model.Usuario;
import com.patp.sistema.repository.AtividadeRepository;
import com.patp.sistema.repository.HistoricoRepository;
import tools.jackson.databind.ObjectMapper;

@Service
public class AtividadeRegistroService {
    public enum Tipo {
        QUADRO_CRIADO, QUADRO_EDITADO, QUADRO_ARQUIVADO, QUADRO_RESTAURADO,
        ETAPA_CRIADA, ETAPA_EDITADA, ETAPA_REMOVIDA,
        DEMANDA_CRIADA, DEMANDA_EDITADA, DEMANDA_MOVIDA, DEMANDA_CONCLUIDA,
        DEMANDA_CANCELADA, DEMANDA_REABERTA, DEMANDA_ARQUIVADA, DEMANDA_RESTAURADA,
        COMENTARIO_ADICIONADO
    }
    public enum AlvoTipo { QUADRO, ETAPA, DEMANDA }
    public record Alvo(AlvoTipo tipo, Long id, String nome, Map<String, String> valores) {
        public Alvo {
            valores = java.util.Collections.unmodifiableMap(new LinkedHashMap<>(valores));
        }
    }
    public record Alteracao(AlvoTipo alvoTipo, Long alvoId, String alvoNome,
            String campo, String anterior, String novo) { }
    private record Chave(AlvoTipo tipo, Long id) { }

    private static final List<String> CAMPOS = List.of("nome", "descricao", "setor", "ordem",
            "numeroProcesso", "pessoa", "responsavel", "prioridade", "dataEmissao", "prazoEtapa",
            "prazoGeral", "observacoes", "status", "etapaId", "etapaNome", "dataConclusao",
            "dataCancelamento", "motivoCancelamento", "arquivado");
    private final AtividadeRepository atividades;
    private final HistoricoRepository historicos;
    private final ObjectMapper json;

    public AtividadeRegistroService(AtividadeRepository atividades, HistoricoRepository historicos, ObjectMapper json) {
        this.atividades = atividades;
        this.historicos = historicos;
        this.json = json;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public Atividade registrar(Gerenciamento quadro, Usuario autor, Tipo tipo, Alvo alvo,
            List<Alteracao> alteracoes, Long historicoId, LocalDateTime horario) {
        if (historicoId != null) {
            var historico = historicos.findById(historicoId).orElseThrow(() ->
                    new IllegalArgumentException("Histórico não encontrado para a atividade."));
            if (!Objects.equals(historico.getProcesso().getEtapa().getGerenciamento().getId(), quadro.getId())) {
                throw new IllegalArgumentException("Histórico pertence a outro gerenciamento.");
            }
        }
        if (tipo == Tipo.COMENTARIO_ADICIONADO && !alteracoes.isEmpty()) {
            throw new IllegalArgumentException("Atividade de comentário não contém alterações informativas.");
        }
        for (Alteracao alteracao : alteracoes) {
            if (!CAMPOS.contains(alteracao.campo())) {
                throw new IllegalArgumentException("Campo não permitido na atividade.");
            }
        }
        return atividades.saveAndFlush(new Atividade(quadro, tipo.name(), alvo.tipo().name(), alvo.id(),
                alvo.nome(), autor.getId(), autor.getNome(),
                (horario == null ? LocalDateTime.now() : horario).truncatedTo(ChronoUnit.MICROS),
                json.writeValueAsString(alteracoes), historicoId));
    }

    public static Alvo snapshotQuadro(Gerenciamento quadro) {
        return new Alvo(AlvoTipo.QUADRO, quadro.getId(), quadro.getNome(), valores(
                "nome", quadro.getNome(), "descricao", quadro.getDescricao(), "arquivado", quadro.getMarcadorArquivado()));
    }

    public static Alvo snapshotEtapa(Etapa etapa) {
        return new Alvo(AlvoTipo.ETAPA, etapa.getId(), etapa.getNome(), valores(
                "nome", etapa.getNome(), "setor", etapa.getSetor(), "ordem", etapa.getOrdem()));
    }

    public static Alvo snapshotDemanda(Processo demanda) {
        return new Alvo(AlvoTipo.DEMANDA, demanda.getId(), demanda.getNumeroProcesso(), valores(
                "numeroProcesso", demanda.getNumeroProcesso(), "pessoa", demanda.getPessoa(),
                "responsavel", demanda.getResponsavel(), "prioridade", demanda.getPrioridade(),
                "dataEmissao", demanda.getDataEmissao(), "prazoEtapa", demanda.getPrazoEtapa(),
                "prazoGeral", demanda.getPrazoGeral(), "observacoes", demanda.getObservacoes(),
                "status", demanda.getStatus(), "etapaId", demanda.getEtapa().getId(),
                "etapaNome", demanda.getEtapa().getNome(), "dataConclusao", demanda.getDataConclusao(),
                "dataCancelamento", demanda.getDataCancelamento(), "motivoCancelamento", demanda.getMotivoCancelamento(),
                "arquivado", demanda.getMarcadorArquivado()));
    }

    public static Map<String, String> valores(Object... pares) {
        Map<String, String> valores = new LinkedHashMap<>();
        for (int i = 0; i < pares.length; i += 2) {
            Object valor = pares[i + 1];
            valores.put((String) pares[i], valor == null ? null : valor.toString());
        }
        return valores;
    }

    public static List<Alteracao> diferencas(List<Alvo> antes, List<Alvo> depois) {
        var ordem = java.util.Comparator.comparing(Chave::tipo).thenComparing(Chave::id);
        Map<Chave, Alvo> anteriores = new TreeMap<>(ordem);
        Map<Chave, Alvo> novos = new TreeMap<>(ordem);
        antes.forEach(a -> anteriores.put(new Chave(a.tipo(), a.id()), a));
        depois.forEach(a -> novos.put(new Chave(a.tipo(), a.id()), a));
        Map<Chave, Alvo> todos = new TreeMap<>(ordem);
        todos.putAll(anteriores);
        todos.putAll(novos);
        List<Alteracao> alteracoes = new ArrayList<>();
        todos.forEach((id, alvo) -> {
            Alvo anterior = anteriores.get(id);
            Alvo novo = novos.get(id);
            for (String campo : CAMPOS) {
                String a = anterior == null ? null : anterior.valores().get(campo);
                String n = novo == null ? null : novo.valores().get(campo);
                if (!Objects.equals(a, n)) {
                    alteracoes.add(new Alteracao(alvo.tipo(), alvo.id(), alvo.nome(), campo, a, n));
                }
            }
        });
        return List.copyOf(alteracoes);
    }
}
