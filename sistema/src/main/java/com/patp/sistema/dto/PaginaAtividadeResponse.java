package com.patp.sistema.dto;

import java.util.List;

public record PaginaAtividadeResponse(Long gerenciamentoId, List<RegistroAtividadeResponse> registros,
        String proximoCursor) { }
