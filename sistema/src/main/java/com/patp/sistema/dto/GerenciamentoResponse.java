package com.patp.sistema.dto;

public record GerenciamentoResponse(Long id, String nome, String descricao,
        CriadorResponse criador, boolean arquivado, Long versao, boolean podeAdministrar) {
    public record CriadorResponse(Long id, String nome) { }
}
