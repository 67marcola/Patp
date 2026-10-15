package com.patp.sistema.controller;

import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import com.patp.sistema.exception.ApiException;
import com.patp.sistema.model.PapelUsuario;
import com.patp.sistema.model.Usuario;
import com.patp.sistema.service.AprovacaoCadastroService;
import com.patp.sistema.service.SessaoService;

@RestController
@ConditionalOnProperty(name="creral.internet.enabled", havingValue="true")
@RequestMapping("/api/administracao/cadastros")
public class AprovacaoCadastroController {
    private final AprovacaoCadastroService aprovacao;
    private final SessaoService sessoes;
    public AprovacaoCadastroController(AprovacaoCadastroService aprovacao, SessaoService sessoes) {
        this.aprovacao = aprovacao; this.sessoes = sessoes;
    }

    private Usuario administrador(String authorization) {
        Usuario atual = sessoes.buscarUsuario(authorization.substring(7));
        if (atual.getPapel() != PapelUsuario.ADMINISTRADOR)
            throw new ApiException(HttpStatus.FORBIDDEN, "Somente administradores podem aprovar cadastros.");
        return atual;
    }

    @GetMapping("/pendentes")
    public List<AprovacaoCadastroService.Pendente> pendentes(@RequestHeader("Authorization") String authorization) {
        administrador(authorization);
        return aprovacao.listarPendentes();
    }

    @PostMapping("/{id}/aprovar")
    public Aprovado aprovar(@PathVariable Long id, @RequestHeader("Authorization") String authorization) {
        Usuario atual = administrador(authorization);
        aprovacao.aprovar(id, atual.getId());
        return new Aprovado(id, "APROVADO");
    }

    public record Aprovado(Long id, String estado) {}
}
