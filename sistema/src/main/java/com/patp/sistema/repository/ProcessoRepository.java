package com.patp.sistema.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.patp.sistema.model.Processo;

public interface ProcessoRepository extends JpaRepository<Processo, Long> {

    List<Processo> findByNumeroProcessoContainingIgnoreCase(String numeroProcesso);

    List<Processo> findByPessoaContainingIgnoreCase(String pessoa);

    List<Processo> findByNumeroProcessoContainingIgnoreCaseOrPessoaContainingIgnoreCase(
            String numeroProcesso,
            String pessoa
    );

    List<Processo> findByEtapaId(Long etapaId);
}