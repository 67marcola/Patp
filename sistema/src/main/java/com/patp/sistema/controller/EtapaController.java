package com.patp.sistema.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.patp.sistema.dto.ConfiguracaoEtapasResponse;
import com.patp.sistema.dto.EtapaResponse;
import com.patp.sistema.dto.OrdemDeserializer;
import com.patp.sistema.dto.VersaoDeserializer;
import com.patp.sistema.service.EtapaService;
import tools.jackson.databind.annotation.JsonDeserialize;

@RestController
@RequestMapping("/api/gerenciamentos")
public class EtapaController {

    private final EtapaService etapaService;

    public EtapaController(EtapaService etapaService) {
        this.etapaService = etapaService;
    }

    @GetMapping("/{gerenciamentoId}/etapas")
    public List<EtapaResponse> listarEtapas(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long gerenciamentoId) {
        return etapaService.configuracao(authorization.substring(7), gerenciamentoId).etapas();
    }

    @GetMapping("/{gerenciamentoId}/estrutura-etapas")
    public ConfiguracaoEtapasResponse configuracao(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long gerenciamentoId) {
        return etapaService.configuracao(authorization.substring(7), gerenciamentoId);
    }

    @PostMapping("/{gerenciamentoId}/etapas")
    @ResponseStatus(HttpStatus.CREATED)
    public ConfiguracaoEtapasResponse criar(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long gerenciamentoId,
            @RequestBody CriarEtapaRequest request) {

        return etapaService.criar(
                authorization.substring(7),
                gerenciamentoId,
                request.nome(),
                request.setor(),
                request.ordem(),
                request.versao()
        );
    }

    @PutMapping("/{gerenciamentoId}/etapas/{etapaId}")
    public ConfiguracaoEtapasResponse editar(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long gerenciamentoId,
            @PathVariable Long etapaId,
            @RequestBody EditarEtapaRequest request) {

        return etapaService.editar(
                authorization.substring(7),
                gerenciamentoId,
                etapaId,
                request.nome(),
                request.setor(),
                request.ordem(),
                request.versao()
        );
    }

    @DeleteMapping("/{gerenciamentoId}/etapas/{etapaId}")
    public ConfiguracaoEtapasResponse excluir(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long gerenciamentoId,
            @PathVariable Long etapaId,
            @RequestBody ExcluirEtapaRequest request) {

        return etapaService.excluir(
                authorization.substring(7),
                gerenciamentoId,
                etapaId,
                request.versao()
        );
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CriarEtapaRequest(
            String nome,
            String setor,
            @JsonDeserialize(using = OrdemDeserializer.class) Integer ordem,
            @JsonDeserialize(using = VersaoDeserializer.class) Long versao
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record EditarEtapaRequest(
            String nome,
            String setor,
            @JsonDeserialize(using = OrdemDeserializer.class) Integer ordem,
            @JsonDeserialize(using = VersaoDeserializer.class) Long versao
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ExcluirEtapaRequest(@JsonDeserialize(using = VersaoDeserializer.class) Long versao) { }
}
