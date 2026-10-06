package com.patp.sistema.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.patp.sistema.dto.ConfiguracaoEtapasResponse;
import com.patp.sistema.dto.CriarDemandaRequest;
import com.patp.sistema.service.ProcessoService;

@RestController
@RequestMapping("/api/gerenciamentos")
public class DemandaController {
    private final ProcessoService processos;

    public DemandaController(ProcessoService processos) { this.processos = processos; }

    @PostMapping("/{gerenciamentoId}/demandas")
    @ResponseStatus(HttpStatus.CREATED)
    public ConfiguracaoEtapasResponse criar(@RequestHeader("Authorization") String authorization,
            @PathVariable Long gerenciamentoId, @RequestBody CriarDemandaRequest request) {
        return processos.criar(authorization.substring(7), gerenciamentoId, request);
    }
}
