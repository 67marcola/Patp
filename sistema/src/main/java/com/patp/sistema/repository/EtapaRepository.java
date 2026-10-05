package com.patp.sistema.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.patp.sistema.model.Etapa;

public interface EtapaRepository extends JpaRepository<Etapa, Long> {

    List<Etapa> findByGerenciamentoIdOrderByOrdem(Long gerenciamentoId);

    List<Etapa> findByGerenciamentoIdOrderByOrdemAscIdAsc(Long gerenciamentoId);

    Etapa findByIdAndGerenciamentoId(Long id, Long gerenciamentoId);

    @Query("select e.gerenciamento.id from Etapa e where e.id = :id")
    Optional<Long> buscarGerenciamentoId(@Param("id") Long id);
}
