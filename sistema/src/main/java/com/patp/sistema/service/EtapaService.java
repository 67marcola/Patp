package com.patp.sistema.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.patp.sistema.model.Etapa;
import com.patp.sistema.model.Gerenciamento;
import com.patp.sistema.repository.EtapaRepository;

@Service
public class EtapaService {

    private final EtapaRepository etapaRepository;
    private final GerenciamentoGuard guard;

    public EtapaService(
            EtapaRepository etapaRepository,
            GerenciamentoGuard guard) {

        this.etapaRepository = etapaRepository;
        this.guard = guard;
    }

    public List<Etapa> listarPorGerenciamento(Long gerenciamentoId) {

        return etapaRepository
                .findByGerenciamentoIdOrderByOrdem(gerenciamentoId);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Etapa criar(
            Long gerenciamentoId,
            String nome,
            String setor,
            Integer ordem) {

        Gerenciamento gerenciamento = guard.exigirAtivo(gerenciamentoId);

        Etapa etapa = new Etapa();

        etapa.setNome(nome);
        etapa.setSetor(setor);
        etapa.setOrdem(ordem);
        etapa.setGerenciamento(gerenciamento);

        return etapaRepository.save(etapa);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Etapa editar(
            Long gerenciamentoId,
            Long etapaId,
            String nome,
            String setor,
            Integer ordem) {

        guard.exigirAtivo(gerenciamentoId);
        Etapa etapa =
                etapaRepository.findByIdAndGerenciamentoId(
                        etapaId,
                        gerenciamentoId
                );

        if (etapa == null) {
            throw new RuntimeException(
                    "Etapa não encontrada neste gerenciamento."
            );
        }

        etapa.setNome(nome);
        etapa.setSetor(setor);
        etapa.setOrdem(ordem);

        return etapaRepository.save(etapa);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void excluir(
            Long gerenciamentoId,
            Long etapaId) {

        guard.exigirAtivo(gerenciamentoId);
        Etapa etapa =
                etapaRepository.findByIdAndGerenciamentoId(
                        etapaId,
                        gerenciamentoId
                );

        if (etapa == null) {
            throw new RuntimeException(
                    "Etapa não encontrada neste gerenciamento."
            );
        }

        etapaRepository.delete(etapa);
    }
}
