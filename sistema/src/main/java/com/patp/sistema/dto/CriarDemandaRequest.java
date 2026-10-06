package com.patp.sistema.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import tools.jackson.databind.annotation.JsonDeserialize;

public record CriarDemandaRequest(@JsonDeserialize(using = VersaoDeserializer.class) Long versao,
        String numeroProcesso, String pessoa, String responsavel, String prioridade,
        @JsonDeserialize(using = DataDemandaDeserializer.class) LocalDate dataEmissao,
        @JsonDeserialize(using = DataDemandaDeserializer.class) LocalDate prazoEtapa,
        @JsonDeserialize(using = DataDemandaDeserializer.class) LocalDate prazoGeral, String observacoes) {
    @JsonAnySetter
    public void desconhecido(String nome, Object valor) {
        throw new IllegalArgumentException("Dados da requisição inválidos.");
    }
}
