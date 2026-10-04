package com.patp.sistema.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.patp.sistema.model.Etapa;
import com.patp.sistema.service.EtapaService;

@RestController
@RequestMapping("/api/gerenciamentos")
public class EtapaController {

    private final EtapaService etapaService;

    public EtapaController(EtapaService etapaService) {
        this.etapaService = etapaService;
    }

    @GetMapping("/{gerenciamentoId}/etapas")
    public List<Etapa> listarEtapas(
            @PathVariable Long gerenciamentoId) {

        return etapaService.listarPorGerenciamento(gerenciamentoId);
    }

    @PostMapping("/{gerenciamentoId}/etapas")
    public Etapa criar(
            @PathVariable Long gerenciamentoId,
            @RequestBody CriarEtapaRequest request) {

        return etapaService.criar(
                gerenciamentoId,
                request.nome(),
                request.setor(),
                request.ordem()
        );
    }

    @PutMapping("/{gerenciamentoId}/etapas/{etapaId}")
    public Etapa editar(
            @PathVariable Long gerenciamentoId,
            @PathVariable Long etapaId,
            @RequestBody EditarEtapaRequest request) {

        return etapaService.editar(
                gerenciamentoId,
                etapaId,
                request.nome(),
                request.setor(),
                request.ordem()
        );
    }

    @DeleteMapping("/{gerenciamentoId}/etapas/{etapaId}")
    public void excluir(
            @PathVariable Long gerenciamentoId,
            @PathVariable Long etapaId) {

        etapaService.excluir(
                gerenciamentoId,
                etapaId
        );
    }

    public record CriarEtapaRequest(
            String nome,
            String setor,
            Integer ordem
    ) {
    }

    public record EditarEtapaRequest(
            String nome,
            String setor,
            Integer ordem
    ) {
    }
}