package com.patp.sistema.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;

import com.patp.sistema.exception.ApiException;
import com.patp.sistema.model.PapelUsuario;
import com.patp.sistema.model.Usuario;
import com.patp.sistema.repository.UsuarioRepository;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AprovacaoCadastroService aprovacao;

    public UsuarioService(UsuarioRepository usuarioRepository, AprovacaoCadastroService aprovacao) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.aprovacao = aprovacao;
    }

    // Cadastrar usuário
    @Transactional
    public Usuario cadastrar(Usuario usuario) {

        if (usuario.getId() != null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "O ID do usuário deve ser definido pelo sistema.");
        }
        usuario.setPapel(PapelUsuario.FUNCIONARIO);

        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new RuntimeException(
                    "Já existe um usuário cadastrado com este e-mail."
            );
        }

        String senhaCriptografada =
                passwordEncoder.encode(usuario.getSenha());

        usuario.setSenha(senhaCriptografada);

        Usuario salvo = usuarioRepository.save(usuario);
        usuarioRepository.flush();
        aprovacao.registrarPendente(salvo.getId());
        return salvo;
    }

    // Login
    public Usuario login(
            String email,
            String senha) {

        Usuario usuario = usuarioRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "E-mail ou senha incorretos."
                        ));

        if (!passwordEncoder.matches(
                senha,
                usuario.getSenha())) {

            throw new RuntimeException(
                    "E-mail ou senha incorretos."
            );
        }

        aprovacao.exigirAprovado(usuario.getId());
        return usuario;
    }

    // Buscar usuário por ID
    public Usuario buscarPorId(Long id) {

        return usuarioRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Usuário não encontrado."
                        ));
    }
}
