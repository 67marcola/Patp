package com.patp.sistema.service;

import java.util.UUID;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.ObjectProvider;

import com.patp.sistema.model.Usuario;
import com.patp.sistema.repository.UsuarioRepository;

@Service
public class SessaoService {

    private final ConcurrentHashMap<String, Sessao> sessoes =
            new ConcurrentHashMap<>();
    private final UsuarioRepository usuarioRepository;
    private final AprovacaoCadastroService aprovacao;
    private final Clock relogio;

    public SessaoService(UsuarioRepository usuarioRepository, AprovacaoCadastroService aprovacao,
            ObjectProvider<Clock> relogios) {
        this.usuarioRepository = usuarioRepository;
        this.aprovacao = aprovacao;
        this.relogio = relogios.getIfAvailable(Clock::systemUTC);
    }

    public String criarSessao(Usuario usuario) {
        aprovacao.exigirAprovado(usuario.getId());

        String token = UUID.randomUUID().toString();

        sessoes.put(token, new Sessao(usuario.getId(), relogio.instant()));

        return token;
    }

    public Usuario buscarUsuario(String token) {

        if (token == null || token.isBlank()) {
            throw new RuntimeException("Usuário não autenticado.");
        }

        Sessao sessao = sessoes.get(token);

        if (sessao == null) {
            throw new RuntimeException("Sessão inválida ou expirada.");
        }

        if (aprovacao.modoInternet() && !relogio.instant().isBefore(sessao.criadaEm().plus(Duration.ofHours(8)))) {
            sessoes.remove(token);
            throw new RuntimeException("Sessão inválida ou expirada.");
        }
        aprovacao.exigirAprovado(sessao.usuarioId());
        return usuarioRepository.findById(sessao.usuarioId())
                .orElseThrow(() -> new RuntimeException("Sessão inválida ou expirada."));
    }

    public void encerrarSessao(String token) {

        if (token != null) {
            sessoes.remove(token);
        }
    }

    private record Sessao(Long usuarioId, Instant criadaEm) {}
}
