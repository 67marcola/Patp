package com.patp.sistema.dto;

import com.patp.sistema.model.CategoriaEtapa;

public record EtapaResponse(Long id, String nome, String setor, Integer ordem, Long quantidadeDemandas,
        CategoriaEtapa categoria) {
}
