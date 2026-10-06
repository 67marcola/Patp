package com.patp.sistema.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.patp.sistema.model.CategoriaEtapa;
import com.patp.sistema.model.Etapa;
import com.patp.sistema.model.Gerenciamento;
import com.patp.sistema.model.Processo;
import com.patp.sistema.repository.EtapaRepository;
import com.patp.sistema.repository.GerenciamentoRepository;
import com.patp.sistema.repository.ProcessoRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;

@Service
public class PreparacaoEtapasFinaisService {
    private final GerenciamentoRepository gerenciamentos;
    private final EtapaRepository etapas;
    private final ProcessoRepository processos;
    private final EtapasFinaisService finais;
    private final EntityManager entityManager;

    public PreparacaoEtapasFinaisService(GerenciamentoRepository gerenciamentos, EtapaRepository etapas,
            ProcessoRepository processos, EtapasFinaisService finais, EntityManager entityManager) {
        this.gerenciamentos = gerenciamentos;
        this.etapas = etapas;
        this.processos = processos;
        this.finais = finais;
        this.entityManager = entityManager;
    }

    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public Relatorio plano() {
        List<QuadroPreparacao> quadros = new ArrayList<>();
        for (Long id : gerenciamentos.listarIds()) {
            quadros.add(inventariar(gerenciamentos.findById(id).orElseThrow()));
        }
        return new Relatorio(quadros, 0, 0);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Relatorio aplicar() {
        // Load only IDs before locking: a previously loaded entity could carry a stale version.
        List<Gerenciamento> bloqueados = gerenciamentos.listarIds().stream()
                .map(id -> gerenciamentos.buscarComLock(id).orElseThrow()).toList();
        List<QuadroPreparacao> quadros = new ArrayList<>();
        long criadas = 0;
        long alteradas = 0;
        for (Gerenciamento gerenciamento : bloqueados) {
            QuadroPreparacao antes = inventariar(gerenciamento);
            List<Etapa> par = finais.garantirPar(gerenciamento);
            long movimentos = 0;
            for (Processo processo : processos.findByEtapaGerenciamentoIdOrderByIdAsc(gerenciamento.getId())) {
                CategoriaEtapa categoria = categoriaFinal(processo.getStatus());
                if (categoria != null) {
                    Etapa destino = par.stream().filter(e -> e.getCategoria() == categoria).findFirst().orElseThrow();
                    if (!processo.getEtapa().getId().equals(destino.getId())) {
                        processo.setEtapa(destino);
                        movimentos++;
                    }
                }
            }
            int novas = antes.finaisAusentes().size();
            entityManager.flush();
            if (novas > 0 || movimentos > 0) {
                entityManager.lock(gerenciamento, LockModeType.PESSIMISTIC_FORCE_INCREMENT);
                entityManager.flush();
            }
            quadros.add(new QuadroPreparacao(antes.id(), antes.nome(), antes.arquivado(), antes.criadorId(),
                    antes.finaisAusentes(), antes.status(), antes.referenciasAjustar(), novas, movimentos, gerenciamento.getVersao()));
            criadas += novas;
            alteradas += movimentos;
        }
        return new Relatorio(quadros, criadas, alteradas);
    }

    private QuadroPreparacao inventariar(Gerenciamento gerenciamento) {
        List<Etapa> existentes = etapas.findByGerenciamentoIdOrderByOrdemAscIdAsc(gerenciamento.getId());
        finais.verificarDuplicidade(existentes);
        List<CategoriaEtapa> ausentes = List.of(CategoriaEtapa.CONCLUIDA, CategoriaEtapa.CANCELADA).stream()
                .filter(c -> existentes.stream().noneMatch(e -> e.getCategoria() == c)).toList();
        Map<String, Long> contagens = new LinkedHashMap<>();
        long referencias = 0;
        for (Processo processo : processos.findByEtapaGerenciamentoIdOrderByIdAsc(gerenciamento.getId())) {
            contagens.merge(processo.getStatus(), 1L, Long::sum);
            CategoriaEtapa categoria = categoriaFinal(processo.getStatus());
            if (categoria != null && processo.getEtapa().getCategoria() != categoria) referencias++;
        }
        List<StatusInventario> status = contagens.entrySet().stream()
                .map(e -> new StatusInventario(e.getKey(), e.getValue())).toList();
        return new QuadroPreparacao(gerenciamento.getId(), gerenciamento.getNome(), gerenciamento.isArquivado(),
                gerenciamento.getCriador() == null ? null : gerenciamento.getCriador().getId(), ausentes,
                status, referencias, 0, 0, gerenciamento.getVersao());
    }

    private static CategoriaEtapa categoriaFinal(String status) {
        if ("Concluido".equals(status)) return CategoriaEtapa.CONCLUIDA;
        if ("Cancelado".equals(status)) return CategoriaEtapa.CANCELADA;
        return null;
    }

    public record StatusInventario(String valor, long quantidade) {}
    public record QuadroPreparacao(Long id, String nome, boolean arquivado, Long criadorId,
            List<CategoriaEtapa> finaisAusentes, List<StatusInventario> status, long referenciasAjustar,
            long finaisCriadas, long referenciasAlteradas, Long versao) {}
    public record Relatorio(List<QuadroPreparacao> quadros, long finaisCriadas, long referenciasAlteradas) {}
}
