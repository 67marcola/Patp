package com.patp.sistema.service;

import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.patp.sistema.exception.ApiException;
import com.patp.sistema.model.Gerenciamento;
import com.patp.sistema.model.PapelUsuario;
import com.patp.sistema.model.Usuario;
import com.patp.sistema.repository.GerenciamentoRepository;

@Service
public class GerenciamentoGuard {
    private final GerenciamentoRepository gerenciamentos;

    public GerenciamentoGuard(GerenciamentoRepository gerenciamentos) {
        this.gerenciamentos = gerenciamentos;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void bloquearCadastros() {
        // Boards are never deleted. The first row serializes global demand numbers until commit.
        gerenciamentos.findFirstByOrderByIdAsc();
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

    public boolean podeAdministrar(Gerenciamento gerenciamento, Usuario usuario) {
        return usuario.getPapel() == PapelUsuario.ADMINISTRADOR
                || gerenciamento.getCriador() != null
                && Objects.equals(gerenciamento.getCriador().getId(), usuario.getId());
    }

    public void exigirAdministracao(Gerenciamento gerenciamento, Usuario usuario) {
        if (!podeAdministrar(gerenciamento, usuario)) {
            throw new ApiException(HttpStatus.FORBIDDEN,
                    "Você não tem permissão para administrar este gerenciamento.");
        }
    }
}
