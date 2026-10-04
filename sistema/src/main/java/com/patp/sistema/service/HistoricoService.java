package com.patp.sistema.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.patp.sistema.model.Historico;
import com.patp.sistema.model.Processo;
import com.patp.sistema.repository.HistoricoRepository;
import com.patp.sistema.repository.ProcessoRepository;

@Service
public class HistoricoService {

    private final HistoricoRepository historicoRepository;
    private final ProcessoRepository processoRepository;

    public HistoricoService(
            HistoricoRepository historicoRepository,
            ProcessoRepository processoRepository) {

        this.historicoRepository = historicoRepository;
        this.processoRepository = processoRepository;
    }

    public Historico registrar(
            Long processoId,
            String acao,
            String descricao,
            String usuario) {

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