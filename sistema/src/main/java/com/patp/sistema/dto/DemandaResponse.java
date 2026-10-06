package com.patp.sistema.dto;

import java.time.LocalDate;

import com.patp.sistema.model.Processo;

public record DemandaResponse(Long id, String numeroProcesso, String pessoa, String responsavel, String status,
        String prioridade, LocalDate dataEmissao, LocalDate prazoEtapa, LocalDate prazoGeral, LocalDate dataConclusao,
        LocalDate dataCancelamento, String motivoCancelamento, String observacoes, Long etapaId) {
    public static DemandaResponse de(Processo processo) {
        return new DemandaResponse(processo.getId(), processo.getNumeroProcesso(), processo.getPessoa(),
                processo.getResponsavel(), processo.getStatus(), processo.getPrioridade(), processo.getDataEmissao(),
                processo.getPrazoEtapa(), processo.getPrazoGeral(), processo.getDataConclusao(), processo.getDataCancelamento(),
                processo.getMotivoCancelamento(), processo.getObservacoes(), processo.getEtapa().getId());
    }
}
