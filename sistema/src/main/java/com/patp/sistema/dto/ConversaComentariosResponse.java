package com.patp.sistema.dto;

import java.util.List;

public record ConversaComentariosResponse(Long gerenciamentoId, Long processoId,
        boolean quadroArquivado, boolean demandaArquivada, List<ComentarioResponse> comentarios) {}
