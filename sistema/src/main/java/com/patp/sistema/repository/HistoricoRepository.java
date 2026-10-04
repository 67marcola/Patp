package com.patp.sistema.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.patp.sistema.model.Historico;

public interface HistoricoRepository extends JpaRepository<Historico, Long> {

    List<Historico> findByProcessoIdOrderByDataHoraAsc(Long processoId);
}