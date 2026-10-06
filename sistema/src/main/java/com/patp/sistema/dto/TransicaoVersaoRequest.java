package com.patp.sistema.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import tools.jackson.databind.annotation.JsonDeserialize;

public record TransicaoVersaoRequest(@JsonDeserialize(using = VersaoDeserializer.class) Long versao) {
    @JsonAnySetter
    public void desconhecido(String nome, Object valor) {
        throw new IllegalArgumentException("Dados da requisição inválidos.");
    }
}

