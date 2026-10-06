package com.patp.sistema.repository;

import com.patp.sistema.model.Gerenciamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

public interface GerenciamentoRepository extends JpaRepository<Gerenciamento, Long> {
    @Query("select g.id from Gerenciamento g order by g.id")
    List<Long> listarIds();

    @Query("select g from Gerenciamento g where coalesce(g.arquivado, false) = :arquivado order by g.id")
    List<Gerenciamento> listarPorEstado(@Param("arquivado") boolean arquivado);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select g from Gerenciamento g where g.id = :id")
    Optional<Gerenciamento> buscarComLock(@Param("id") Long id);
}
