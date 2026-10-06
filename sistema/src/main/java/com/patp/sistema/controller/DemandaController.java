package com.patp.sistema.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.patp.sistema.dto.ConfiguracaoEtapasResponse;
import com.patp.sistema.dto.CriarDemandaRequest;
import com.patp.sistema.dto.TransicaoDestinoRequest;
import com.patp.sistema.dto.TransicaoVersaoRequest;
import com.patp.sistema.dto.TransicaoCancelarRequest;
import com.patp.sistema.service.ProcessoService;
import com.patp.sistema.service.ProcessoService.Acao;

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

    @PutMapping("/{gerenciamentoId}/demandas/{demandaId}/mover")
    public ConfiguracaoEtapasResponse mover(@RequestHeader("Authorization") String authorization,
            @PathVariable Long gerenciamentoId, @PathVariable Long demandaId, @RequestBody TransicaoDestinoRequest request) {
        return processos.transicionar(authorization.substring(7), gerenciamentoId, demandaId,
                Acao.MOVER, request.etapaId(), null, request.versao());
    }

    @PutMapping("/{gerenciamentoId}/demandas/{demandaId}/concluir")
    public ConfiguracaoEtapasResponse concluir(@RequestHeader("Authorization") String authorization,
            @PathVariable Long gerenciamentoId, @PathVariable Long demandaId, @RequestBody TransicaoVersaoRequest request) {
        return processos.transicionar(authorization.substring(7), gerenciamentoId, demandaId,
                Acao.CONCLUIR, null, null, request.versao());
    }

    @PutMapping("/{gerenciamentoId}/demandas/{demandaId}/cancelar")
    public ConfiguracaoEtapasResponse cancelar(@RequestHeader("Authorization") String authorization,
            @PathVariable Long gerenciamentoId, @PathVariable Long demandaId, @RequestBody TransicaoCancelarRequest request) {
        return processos.transicionar(authorization.substring(7), gerenciamentoId, demandaId,
                Acao.CANCELAR, null, request.motivo(), request.versao());
    }

    @PutMapping("/{gerenciamentoId}/demandas/{demandaId}/reabrir")
    public ConfiguracaoEtapasResponse reabrir(@RequestHeader("Authorization") String authorization,
            @PathVariable Long gerenciamentoId, @PathVariable Long demandaId, @RequestBody TransicaoDestinoRequest request) {
        return processos.transicionar(authorization.substring(7), gerenciamentoId, demandaId,
                Acao.REABRIR, request.etapaId(), null, request.versao());
    }
}
