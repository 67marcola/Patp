package com.patp.sistema.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.patp.sistema.dto.PaginaAtividadeResponse;
import com.patp.sistema.service.AtividadeService;

@RestController
@RequestMapping("/api/gerenciamentos/{gerenciamentoId}/atividade")
public class AtividadeController {
    private final AtividadeService atividades;
    public AtividadeController(AtividadeService atividades) { this.atividades=atividades; }
    @GetMapping
    public PaginaAtividadeResponse listar(@RequestHeader("Authorization") String authorization,
            @PathVariable Long gerenciamentoId, @RequestParam(required=false) String cursor) {
        return atividades.listar(authorization.substring(7),gerenciamentoId,cursor);
    }
}
