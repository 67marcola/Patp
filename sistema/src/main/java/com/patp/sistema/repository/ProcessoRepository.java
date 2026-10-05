package com.patp.sistema.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.patp.sistema.model.Processo;

public interface ProcessoRepository extends JpaRepository<Processo, Long> {

    List<Processo> findByNumeroProcessoContainingIgnoreCase(String numeroProcesso);

    List<Processo> findByPessoaContainingIgnoreCase(String pessoa);

    List<Processo> findByNumeroProcessoContainingIgnoreCaseOrPessoaContainingIgnoreCase(
            String numeroProcesso,
            String pessoa
    );

    List<Processo> findByEtapaId(Long etapaId);

    @Query("select p.etapa.gerenciamento.id from Processo p where p.id = :id")
    Optional<Long> buscarGerenciamentoId(@Param("id") Long id);
}
