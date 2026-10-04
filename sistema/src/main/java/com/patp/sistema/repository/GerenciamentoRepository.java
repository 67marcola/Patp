package com.patp.sistema.repository;

import com.patp.sistema.model.Gerenciamento;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GerenciamentoRepository extends JpaRepository<Gerenciamento, Long> {
}