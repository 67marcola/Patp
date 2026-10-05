package com.patp.sistema.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.patp.sistema.exception.ApiException;
import com.patp.sistema.model.Gerenciamento;
import com.patp.sistema.repository.GerenciamentoRepository;

@Service
public class GerenciamentoGuard {
    private final GerenciamentoRepository gerenciamentos;

    public GerenciamentoGuard(GerenciamentoRepository gerenciamentos) {
        this.gerenciamentos = gerenciamentos;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public Gerenciamento bloquear(Long id) {
        return gerenciamentos.buscarComLock(id).orElseThrow(() ->
                new ApiException(HttpStatus.NOT_FOUND, "Gerenciamento não encontrado."));
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public Gerenciamento exigirAtivo(Long id) {
        Gerenciamento gerenciamento = bloquear(id);
        verificarAtivo(gerenciamento);
        return gerenciamento;
    }

    public void verificarAtivo(Gerenciamento gerenciamento) {
        if (gerenciamento.isArquivado()) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "Gerenciamento arquivado. Restaure-o antes de alterar.");
        }
    }
}
