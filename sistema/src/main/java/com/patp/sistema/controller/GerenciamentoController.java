package com.patp.sistema.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.patp.sistema.model.Etapa;
import com.patp.sistema.model.Gerenciamento;
import com.patp.sistema.service.GerenciamentoService;

@RestController
@RequestMapping("/api/gerenciamentos")
public class GerenciamentoController {

    private final GerenciamentoService gerenciamentoService;

    public GerenciamentoController(
            GerenciamentoService gerenciamentoService) {

        this.gerenciamentoService = gerenciamentoService;
    }

    @PostMapping
    public Gerenciamento criar(
            @RequestBody CriarGerenciamentoRequest request) {

        List<Etapa> etapas = request.etapas().stream()
                .map(etapaRequest -> {

                    Etapa etapa = new Etapa();

                    etapa.setNome(etapaRequest.nome());
                    etapa.setSetor(etapaRequest.setor());
                    etapa.setOrdem(etapaRequest.ordem());

                    return etapa;
                })
                .toList();

        return gerenciamentoService.criarGerenciamento(
                request.nome(),
                request.descricao(),
                etapas
        );
    }

    @GetMapping
    public List<Gerenciamento> listar() {
        return gerenciamentoService.listarGerenciamentos();
    }

    @GetMapping("/{id}")
    public Gerenciamento buscarPorId(
            @PathVariable Long id) {

        return gerenciamentoService.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public Gerenciamento editar(
            @PathVariable Long id,
            @RequestBody EditarGerenciamentoRequest request) {

        return gerenciamentoService.editar(
                id,
                request.nome(),
                request.descricao()
        );
    }

    public record CriarGerenciamentoRequest(
            String nome,
            String descricao,
            List<CriarEtapaRequest> etapas
    ) {
    }

    public record CriarEtapaRequest(
            String nome,
            String setor,
            Integer ordem
    ) {
    }

    public record EditarGerenciamentoRequest(
            String nome,
            String descricao
    ) {
    }
}