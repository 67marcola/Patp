package com.patp.sistema.service;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.patp.sistema.model.Usuario;

@Service
public class SessaoService {

    private final ConcurrentHashMap<String, Usuario> sessoes =
            new ConcurrentHashMap<>();

    public String criarSessao(Usuario usuario) {

        String token = UUID.randomUUID().toString();

        sessoes.put(token, usuario);

        return token;
    }

    public Usuario buscarUsuario(String token) {

        if (token == null || token.isBlank()) {
            throw new RuntimeException("Usuário não autenticado.");
        }

        Usuario usuario = sessoes.get(token);

        if (usuario == null) {
            throw new RuntimeException("Sessão inválida ou expirada.");
        }

        return usuario;
    }

    public void encerrarSessao(String token) {

        if (token != null) {
            sessoes.remove(token);
        }
    }
}