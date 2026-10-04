package com.patp.sistema.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.patp.sistema.model.Etapa;
import com.patp.sistema.model.Gerenciamento;
import com.patp.sistema.repository.EtapaRepository;
import com.patp.sistema.repository.GerenciamentoRepository;

@Service
public class EtapaService {

    private final EtapaRepository etapaRepository;
    private final GerenciamentoRepository gerenciamentoRepository;

    public EtapaService(
            EtapaRepository etapaRepository,
            GerenciamentoRepository gerenciamentoRepository) {

        this.etapaRepository = etapaRepository;
        this.gerenciamentoRepository = gerenciamentoRepository;
    }

    public List<Etapa> listarPorGerenciamento(Long gerenciamentoId) {

        return etapaRepository
                .findByGerenciamentoIdOrderByOrdem(gerenciamentoId);
    }

    public Etapa criar(
            Long gerenciamentoId,
            String nome,
            String setor,
            Integer ordem) {

        Gerenciamento gerenciamento =
                gerenciamentoRepository.findById(gerenciamentoId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Gerenciamento não encontrado."
                                ));

        Etapa etapa = new Etapa();

        etapa.setNome(nome);
        etapa.setSetor(setor);
        etapa.setOrdem(ordem);
        etapa.setGerenciamento(gerenciamento);

        return etapaRepository.save(etapa);
    }

    public Etapa editar(
            Long gerenciamentoId,
            Long etapaId,
            String nome,
            String setor,
            Integer ordem) {

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

    public void excluir(
            Long gerenciamentoId,
            Long etapaId) {

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