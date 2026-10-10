package com.patp.sistema.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.patp.sistema.dto.ConversaComentariosResponse;
import com.patp.sistema.service.ComentarioService;

@RestController
@RequestMapping("/api/gerenciamentos/{gerenciamentoId}/demandas/{processoId}/comentarios")
public class ComentarioDemandaController {
    private final ComentarioService comentarios;
    public ComentarioDemandaController(ComentarioService comentarios) { this.comentarios = comentarios; }

    @GetMapping
    public ConversaComentariosResponse consultar(@RequestHeader("Authorization") String authorization,
            @PathVariable Long gerenciamentoId, @PathVariable Long processoId) {
        return comentarios.consultarNoQuadro(authorization.substring(7), gerenciamentoId, processoId);
    }
}
