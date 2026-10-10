package com.patp.sistema.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.patp.sistema.model.Atividade;

public interface AtividadeRepository extends JpaRepository<Atividade, Long> {
}
