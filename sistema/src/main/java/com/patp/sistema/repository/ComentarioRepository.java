package com.patp.sistema.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.patp.sistema.model.Comentario;

public interface ComentarioRepository extends JpaRepository<Comentario, Long> {

    List<Comentario> findByProcessoIdOrderByDataHoraAsc(Long processoId);
}