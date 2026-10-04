package com.patp.sistema.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.patp.sistema.model.Processo;
import com.patp.sistema.service.ProcessoService;

@RestController
@RequestMapping("/api/processos")
public class ProcessoController {

    private final ProcessoService processoService;

    public ProcessoController(ProcessoService processoService) {
        this.processoService = processoService;
    }

    // Criar processo
    @PostMapping
    public Processo criar(
            @RequestBody Processo processo,
            @RequestHeader("Authorization") String token) {

        return processoService.salvar(
                processo,
                removerBearer(token)
        );
    }

    // Listar / pesquisar processos
    @GetMapping
    public List<Processo> listar(
            @RequestParam(required = false) String pesquisa) {

        return processoService.pesquisar(pesquisa);
    }

    // Buscar processo por ID
    @GetMapping("/{id}")
    public Processo buscarPorId(@PathVariable Long id) {

        return processoService.buscarPorId(id);
    }

    // Listar processos de uma etapa
    @GetMapping("/etapa/{etapaId}")
    public List<Processo> listarPorEtapa(
            @PathVariable Long etapaId) {

        return processoService.listarPorEtapa(etapaId);
    }

    // Editar processo
    @PutMapping("/{id}")
    public Processo editar(
            @PathVariable Long id,
            @RequestBody Processo processo,
            @RequestHeader("Authorization") String token) {

        return processoService.editar(
                id,
                processo,
                removerBearer(token)
        );
    }

    // Mudar processo de etapa
    @PutMapping("/{processoId}/etapa/{etapaId}")
    public Processo mudarEtapa(
            @PathVariable Long processoId,
            @PathVariable Long etapaId,
            @RequestHeader("Authorization") String token) {

        return processoService.mudarEtapa(
                processoId,
                etapaId,
                removerBearer(token)
        );
    }

    // Concluir processo
    @PutMapping("/{id}/concluir")
    public Processo concluir(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {

        return processoService.concluir(
                id,
                removerBearer(token)
        );
    }

    // Cancelar processo
    @PutMapping("/{id}/cancelar")
    public Processo cancelar(
            @PathVariable Long id,
            @RequestBody CancelarProcessoRequest request,
            @RequestHeader("Authorization") String token) {

        return processoService.cancelar(
                id,
                request.motivo(),
                removerBearer(token)
        );
    }

    // Excluir processo
    @DeleteMapping("/{id}")
    public void excluir(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {

        processoService.excluir(
                id,
                removerBearer(token)
        );
    }

    private String removerBearer(String token) {

        if (token != null && token.startsWith("Bearer ")) {
            return token.substring(7);
        }

        return token;
    }

    public record CancelarProcessoRequest(
            String motivo
    ) {}
}