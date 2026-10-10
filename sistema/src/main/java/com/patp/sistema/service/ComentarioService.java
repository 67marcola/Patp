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
import com.patp.sistema.dto.ComentarioResponse;
import com.patp.sistema.dto.ConversaComentariosResponse;
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

    @Transactional(readOnly = true)
    public List<Comentario> listarPorProcesso(Long processoId) {
        if (!processoRepository.existsById(processoId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Demanda não encontrada.");
        }
        return comentarioRepository
                .findByProcessoIdOrderByDataHoraAscIdAsc(processoId);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ConversaComentariosResponse consultarNoQuadro(String token, Long gerenciamentoId, Long processoId) {
        sessoes.buscarUsuario(token);
        var quadro = guard.bloquear(gerenciamentoId);
        var processo = processoRepository.findById(processoId)
                .filter(p -> p.getEtapa().getGerenciamento().getId().equals(gerenciamentoId))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Demanda não encontrada."));
        var mensagens = comentarioRepository.findByProcessoIdOrderByDataHoraAscIdAsc(processoId).stream()
                .map(ComentarioResponse::de).toList();
        return new ConversaComentariosResponse(gerenciamentoId, processoId,
                quadro.isArquivado(), processo.isArquivado(), mensagens);
    }
}
