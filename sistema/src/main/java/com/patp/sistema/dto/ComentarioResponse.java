package com.patp.sistema.dto;

import java.time.LocalDateTime;
import com.patp.sistema.model.Comentario;

public record ComentarioResponse(Long id, String texto, String funcionario, LocalDateTime dataHora, Long processoId) {
    public static ComentarioResponse de(Comentario comentario) {
        return new ComentarioResponse(comentario.getId(), comentario.getTexto(), comentario.getFuncionario(),
                comentario.getDataHora(), comentario.getProcesso().getId());
    }
}
