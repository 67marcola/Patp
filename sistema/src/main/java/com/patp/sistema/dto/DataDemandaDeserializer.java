package com.patp.sistema.dto;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

public class DataDemandaDeserializer extends ValueDeserializer<LocalDate> {
    @Override
    public LocalDate deserialize(JsonParser parser, DeserializationContext context) {
        if (parser.currentToken() != JsonToken.VALUE_STRING) {
            return context.reportInputMismatch(LocalDate.class, "Informe uma data ISO válida ou null.");
        }
        try {
            return LocalDate.parse(parser.getString());
        } catch (DateTimeParseException erro) {
            return context.reportInputMismatch(LocalDate.class, "Informe uma data ISO válida ou null.");
        }
    }
}
