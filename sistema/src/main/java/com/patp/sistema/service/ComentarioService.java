package com.patp.sistema.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.patp.sistema.model.Comentario;
import com.patp.sistema.model.Processo;
import com.patp.sistema.repository.ComentarioRepository;
import com.patp.sistema.repository.ProcessoRepository;

@Service
public class ComentarioService {

    private final ComentarioRepository comentarioRepository;
    private final ProcessoRepository processoRepository;

    public ComentarioService(
            ComentarioRepository comentarioRepository,
            ProcessoRepository processoRepository) {

        this.comentarioRepository = comentarioRepository;
        this.processoRepository = processoRepository;
    }

    public Comentario adicionar(
            Long processoId,
            String texto,
            String funcionario) {

        Processo processo = processoRepository.findById(processoId)
                .orElseThrow(() ->
                        new RuntimeException("Processo não encontrado."));

        Comentario comentario = new Comentario();

        comentario.setTexto(texto);
        comentario.setFuncionario(funcionario);
        comentario.setDataHora(LocalDateTime.now());
        comentario.setProcesso(processo);

        return comentarioRepository.save(comentario);
    }

    public List<Comentario> listarPorProcesso(Long processoId) {
        return comentarioRepository
                .findByProcessoIdOrderByDataHoraAsc(processoId);
    }
}