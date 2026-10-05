package com.patp.sistema.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.patp.sistema.model.Etapa;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.patp.sistema.dto.GerenciamentoResponse;
import com.patp.sistema.dto.OrdemDeserializer;
import com.patp.sistema.dto.VersaoDeserializer;
import com.patp.sistema.exception.ApiException;
import com.patp.sistema.service.GerenciamentoService;
import tools.jackson.databind.annotation.JsonDeserialize;

@RestController
@RequestMapping("/api/gerenciamentos")
public class GerenciamentoController {

    private final GerenciamentoService gerenciamentoService;

    public GerenciamentoController(
            GerenciamentoService gerenciamentoService) {

        this.gerenciamentoService = gerenciamentoService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GerenciamentoResponse criar(
            @RequestHeader("Authorization") String authorization,
            @RequestBody CriarGerenciamentoRequest request) {

        List<Etapa> etapas = request.etapas() == null ? null : request.etapas().stream()
                .map(etapaRequest -> {
                    if (etapaRequest == null) {
                        return (Etapa) null;
                    }
                    Etapa etapa = new Etapa();

                    etapa.setNome(etapaRequest.nome());
                    etapa.setSetor(etapaRequest.setor());
                    etapa.setOrdem(etapaRequest.ordem());

                    return etapa;
                })
                .toList();

        return gerenciamentoService.criarGerenciamento(
                token(authorization),
                request.nome(),
                request.descricao(),
                etapas
        );
    }

    @GetMapping
    public List<GerenciamentoResponse> listar(@RequestHeader("Authorization") String authorization,
            @RequestParam(required = false) String arquivado) {
        if (arquivado != null && !arquivado.equals("true") && !arquivado.equals("false")) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Filtro de arquivamento inválido.");
        }
        return gerenciamentoService.listarGerenciamentos(token(authorization), "true".equals(arquivado));
    }

    @GetMapping("/{id}")
    public GerenciamentoResponse buscarPorId(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long id) {

        return gerenciamentoService.buscarPorId(token(authorization), id);
    }

    @PutMapping("/{id}")
    public GerenciamentoResponse editar(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long id,
            @RequestBody EditarGerenciamentoRequest request) {

        return gerenciamentoService.editar(
                token(authorization),
                id,
                request.nome(),
                request.descricao(),
                request.versao()
        );
    }

    @PutMapping("/{id}/arquivar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void arquivar(@RequestHeader("Authorization") String authorization, @PathVariable Long id,
            @RequestBody EstadoRequest request) {
        gerenciamentoService.mudarEstado(token(authorization), id, request.versao(), true);
    }

    @PutMapping("/{id}/restaurar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void restaurar(@RequestHeader("Authorization") String authorization, @PathVariable Long id,
            @RequestBody EstadoRequest request) {
        gerenciamentoService.mudarEstado(token(authorization), id, request.versao(), false);
    }

    private String token(String authorization) {
        return authorization.substring(7);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CriarGerenciamentoRequest(
            String nome,
            String descricao,
            List<CriarEtapaRequest> etapas
    ) {
    }

    public record CriarEtapaRequest(
            String nome,
            String setor,
            @JsonDeserialize(using = OrdemDeserializer.class) Integer ordem
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record EditarGerenciamentoRequest(
            String nome,
            String descricao,
            @JsonDeserialize(using = VersaoDeserializer.class) Long versao
    ) {
    }

    public record EstadoRequest(@JsonDeserialize(using = VersaoDeserializer.class) Long versao) { }
}
