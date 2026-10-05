package com.patp.sistema.service;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.patp.sistema.model.Usuario;
import com.patp.sistema.repository.UsuarioRepository;

@Service
public class SessaoService {

    private final ConcurrentHashMap<String, Long> sessoes =
            new ConcurrentHashMap<>();
    private final UsuarioRepository usuarioRepository;

    public SessaoService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public String criarSessao(Usuario usuario) {

        String token = UUID.randomUUID().toString();

        sessoes.put(token, usuario.getId());

        return token;
    }

    public Usuario buscarUsuario(String token) {

        if (token == null || token.isBlank()) {
            throw new RuntimeException("Usuário não autenticado.");
        }

        Long usuarioId = sessoes.get(token);

        if (usuarioId == null) {
            throw new RuntimeException("Sessão inválida ou expirada.");
        }

        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Sessão inválida ou expirada."));
    }

    public void encerrarSessao(String token) {

        if (token != null) {
            sessoes.remove(token);
        }
    }
}
