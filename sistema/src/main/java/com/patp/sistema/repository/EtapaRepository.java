package com.patp.sistema.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.patp.sistema.model.Etapa;

public interface EtapaRepository extends JpaRepository<Etapa, Long> {

    List<Etapa> findByGerenciamentoIdOrderByOrdem(Long gerenciamentoId);

    Etapa findByIdAndGerenciamentoId(Long id, Long gerenciamentoId);
}