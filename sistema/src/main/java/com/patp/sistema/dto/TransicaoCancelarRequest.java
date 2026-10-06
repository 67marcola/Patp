package com.patp.sistema.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import tools.jackson.databind.annotation.JsonDeserialize;

public record TransicaoCancelarRequest(@JsonDeserialize(using = VersaoDeserializer.class) Long versao,
        @JsonDeserialize(using = MotivoDeserializer.class) String motivo) {
    @JsonAnySetter
    public void desconhecido(String nome, Object valor) {
        throw new IllegalArgumentException("Dados da requisição inválidos.");
    }

    public static class MotivoDeserializer extends tools.jackson.databind.ValueDeserializer<String> {
        @Override
        public String deserialize(tools.jackson.core.JsonParser parser, tools.jackson.databind.DeserializationContext context) {
            if (parser.currentToken() != tools.jackson.core.JsonToken.VALUE_STRING) {
                return context.reportInputMismatch(String.class, "O motivo deve ser um texto.");
            }
            return parser.getString();
        }
    }
}

