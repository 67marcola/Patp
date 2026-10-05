package com.patp.sistema;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import com.patp.sistema.model.Usuario;
import com.patp.sistema.repository.UsuarioRepository;
import com.patp.sistema.service.SessaoService;

abstract class ApiIntegrationSupport {
    @Autowired protected MockMvc mvc;
    @Autowired protected JdbcTemplate jdbc;
    @Autowired protected UsuarioRepository usuarios;
    @Autowired protected SessaoService sessoes;

    @BeforeEach
    void limparDados() {
        jdbc.update("delete from comentarios");
        jdbc.update("delete from historicos");
        jdbc.update("delete from processos");
        jdbc.update("delete from etapas");
        jdbc.update("delete from gerenciamentos");
        jdbc.update("delete from usuarios");
    }

    protected Usuario usuario(String nome) {
        Usuario usuario = new Usuario();
        usuario.setNome(nome);
        usuario.setEmail(nome.toLowerCase() + "@example.test");
        usuario.setSetor("Engenharia");
        usuario.setSenha("hash-ficticio-nao-publico");
        return usuarios.saveAndFlush(usuario);
    }

    protected String token(Usuario usuario) {
        return "Bearer " + sessoes.criarSessao(usuario);
    }
}
