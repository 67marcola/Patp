package com.patp.sistema.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.patp.sistema.model.Etapa;
import com.patp.sistema.model.Gerenciamento;
import com.patp.sistema.repository.EtapaRepository;
import com.patp.sistema.repository.GerenciamentoRepository;

@Service
public class GerenciamentoService {

    private final GerenciamentoRepository gerenciamentoRepository;
    private final EtapaRepository etapaRepository;

    public GerenciamentoService(
            GerenciamentoRepository gerenciamentoRepository,
            EtapaRepository etapaRepository) {

        this.gerenciamentoRepository = gerenciamentoRepository;
        this.etapaRepository = etapaRepository;
    }

    public Gerenciamento criarGerenciamento(
            String nome,
            String descricao,
            List<Etapa> etapas) {

        Gerenciamento gerenciamento = new Gerenciamento();

        gerenciamento.setNome(nome);
        gerenciamento.setDescricao(descricao);

        Gerenciamento gerenciamentoSalvo =
                gerenciamentoRepository.save(gerenciamento);

        if (etapas != null) {
            for (Etapa etapa : etapas) {
                etapa.setGerenciamento(gerenciamentoSalvo);
                etapaRepository.save(etapa);
            }
        }

        return gerenciamentoSalvo;
    }

    public List<Gerenciamento> listarGerenciamentos() {
        return gerenciamentoRepository.findAll();
    }

    public Gerenciamento buscarPorId(Long id) {
        return gerenciamentoRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Gerenciamento não encontrado."
                        ));
    }

    public Gerenciamento editar(
            Long id,
            String nome,
            String descricao) {

        Gerenciamento gerenciamento = buscarPorId(id);

        gerenciamento.setNome(nome);
        gerenciamento.setDescricao(descricao);

        return gerenciamentoRepository.save(gerenciamento);
    }
}