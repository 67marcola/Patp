package com.patp.sistema.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.patp.sistema.model.Comentario;
import com.patp.sistema.model.Processo;
import com.patp.sistema.exception.ApiException;
import com.patp.sistema.repository.ComentarioRepository;
import com.patp.sistema.repository.ProcessoRepository;

@Service
public class ComentarioService {

    private final ComentarioRepository comentarioRepository;
    private final ProcessoRepository processoRepository;
    private final GerenciamentoGuard guard;
    private final SessaoService sessoes;

    public ComentarioService(
            ComentarioRepository comentarioRepository,
            ProcessoRepository processoRepository,
            GerenciamentoGuard guard,
            SessaoService sessoes) {

        this.comentarioRepository = comentarioRepository;
        this.processoRepository = processoRepository;
        this.guard = guard;
        this.sessoes = sessoes;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Comentario adicionar(
            String token,
            Long processoId,
            String texto) {

        var autor = sessoes.buscarUsuario(token);

        Long gerenciamentoId = processoRepository.buscarGerenciamentoId(processoId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Demanda não encontrada."));
        guard.exigirAtivo(gerenciamentoId);
        Processo processo = processoRepository.findById(processoId)
                .orElseThrow(() ->
                        new ApiException(HttpStatus.NOT_FOUND, "Demanda não encontrada."));
        if (processo.isArquivado()) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "Demanda arquivada. Restaure-a antes de alterar.");
        }
        if (texto == null || texto.isBlank() || texto.length() > 10000) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Comentário deve conter texto e ter no máximo 10000 caracteres.");
        }

        Comentario comentario = new Comentario();

        comentario.setTexto(texto);
        comentario.setFuncionario(autor.getNome());
        comentario.setDataHora(LocalDateTime.now());
        comentario.setProcesso(processo);

        return comentarioRepository.save(comentario);
    }

    public List<Comentario> listarPorProcesso(Long processoId) {
        return comentarioRepository
                .findByProcessoIdOrderByDataHoraAsc(processoId);
    }
}
