package com.patp.sistema.dto;

import java.time.LocalDateTime;
import java.util.List;
import com.patp.sistema.service.AtividadeRegistroService.Alteracao;

public record RegistroAtividadeResponse(String chave, String origem, Long id, Long gerenciamentoId,
        String tipo, String alvoTipo, Long alvoId, String alvoNome, Long autorId, String autorNome,
        LocalDateTime dataHora, String descricao, List<Alteracao> alteracoes) { }
