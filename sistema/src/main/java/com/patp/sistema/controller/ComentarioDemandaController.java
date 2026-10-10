package com.patp.sistema.controller;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.patp.sistema.dto.ConversaComentariosResponse;
import com.patp.sistema.dto.ComentarioResponse;
import com.patp.sistema.exception.ApiException;
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

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ComentarioResponse adicionar(@RequestHeader("Authorization") String authorization,
            @PathVariable Long gerenciamentoId, @PathVariable Long processoId, @RequestBody Map<String, Object> request) {
        if (request.keySet().stream().anyMatch(chave -> !chave.equals("texto"))
                || (request.get("texto") != null && !(request.get("texto") instanceof String))) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Dados da requisição inválidos.");
        }
        return comentarios.adicionarNoQuadro(authorization.substring(7), gerenciamentoId, processoId, (String) request.get("texto"));
    }
}
