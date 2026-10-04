package com.patp.sistema.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.patp.sistema.model.Historico;
import com.patp.sistema.service.HistoricoService;

@RestController
@RequestMapping("/api/processos")
public class HistoricoController {

    private final HistoricoService historicoService;

    public HistoricoController(HistoricoService historicoService) {
        this.historicoService = historicoService;
    }

    @PostMapping("/{processoId}/historico")
    public Historico registrar(
            @PathVariable Long processoId,
            @RequestBody CriarHistoricoRequest request) {

        return historicoService.registrar(
                processoId,
                request.acao(),
                request.descricao(),
                request.usuario()
        );
    }

    @GetMapping("/{processoId}/historico")
    public List<Historico> listar(
            @PathVariable Long processoId) {

        return historicoService.listarPorProcesso(processoId);
    }

    public record CriarHistoricoRequest(
            String acao,
            String descricao,
            String usuario
    ) {
    }
}