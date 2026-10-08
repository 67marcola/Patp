package com.patp.sistema.dto;

import java.util.List;

public record ConfiguracaoEtapasResponse(GerenciamentoResponse gerenciamento, List<EtapaResponse> etapas,
        List<DemandaResponse> demandas, List<DemandaResponse> demandasArquivadas) {
    public ConfiguracaoEtapasResponse(GerenciamentoResponse gerenciamento, List<EtapaResponse> etapas,
            List<DemandaResponse> demandas) {
        this(gerenciamento, etapas, demandas, List.of());
    }
}
