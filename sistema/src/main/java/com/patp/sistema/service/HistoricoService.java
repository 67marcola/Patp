package com.patp.sistema.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.patp.sistema.model.Historico;
import com.patp.sistema.model.Processo;
import com.patp.sistema.repository.HistoricoRepository;
import com.patp.sistema.repository.ProcessoRepository;

@Service
public class HistoricoService {

    private final HistoricoRepository historicoRepository;
    private final ProcessoRepository processoRepository;
    private final GerenciamentoGuard guard;

    public HistoricoService(
            HistoricoRepository historicoRepository,
            ProcessoRepository processoRepository,
            GerenciamentoGuard guard) {

        this.historicoRepository = historicoRepository;
        this.processoRepository = processoRepository;
        this.guard = guard;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Historico registrar(
            Long processoId,
            String acao,
            String descricao,
            String usuario) {

        Long gerenciamentoId = processoRepository.buscarGerenciamentoId(processoId)
                .orElseThrow(() -> new RuntimeException("Processo não encontrado."));
        guard.exigirAtivo(gerenciamentoId);
        Processo processo = processoRepository.findById(processoId)
                .orElseThrow(() ->
                        new RuntimeException("Processo não encontrado."));

        Historico historico = new Historico();

        historico.setAcao(acao);
        historico.setDescricao(descricao);
        historico.setUsuario(usuario);
        historico.setDataHora(LocalDateTime.now());
        historico.setProcesso(processo);

        return historicoRepository.save(historico);
    }

    public List<Historico> listarPorProcesso(Long processoId) {

        return historicoRepository
                .findByProcessoIdOrderByDataHoraAsc(processoId);
    }
}
