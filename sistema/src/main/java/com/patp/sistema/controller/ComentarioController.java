package com.patp.sistema.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.patp.sistema.model.Comentario;
import com.patp.sistema.service.ComentarioService;

@RestController
@RequestMapping("/api/processos")
public class ComentarioController {

    private final ComentarioService comentarioService;

    public ComentarioController(ComentarioService comentarioService) {
        this.comentarioService = comentarioService;
    }

    @PostMapping("/{processoId}/comentarios")
    public Comentario adicionar(
            @PathVariable Long processoId,
            @RequestBody CriarComentarioRequest request) {

        return comentarioService.adicionar(
                processoId,
                request.texto(),
                request.funcionario()
        );
    }

    @GetMapping("/{processoId}/comentarios")
    public List<Comentario> listar(
            @PathVariable Long processoId) {

        return comentarioService.listarPorProcesso(processoId);
    }

    public record CriarComentarioRequest(
            String texto,
            String funcionario
    ) {
    }
}