package com.patp.sistema;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import com.patp.sistema.model.Usuario;
import com.patp.sistema.model.Gerenciamento;
import com.patp.sistema.model.Etapa;
import com.patp.sistema.model.Processo;
import com.patp.sistema.repository.GerenciamentoRepository;
import com.patp.sistema.repository.EtapaRepository;
import com.patp.sistema.repository.ProcessoRepository;
import com.patp.sistema.repository.UsuarioRepository;
import com.patp.sistema.service.SessaoService;

abstract class ApiIntegrationSupport {
    @Autowired protected MockMvc mvc;
    @Autowired protected JdbcTemplate jdbc;
    @Autowired protected UsuarioRepository usuarios;
    @Autowired protected SessaoService sessoes;
    @Autowired protected GerenciamentoRepository quadros;
    @Autowired protected EtapaRepository etapas;
    @Autowired protected ProcessoRepository processos;

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

    protected Gerenciamento quadro(Usuario criador) {
        Gerenciamento quadro = new Gerenciamento();
        quadro.setNome("Quadro");
        quadro.setDescricao("Descrição");
        quadro.setCriador(criador);
        return quadros.saveAndFlush(quadro);
    }

    protected Etapa etapa(Gerenciamento quadro, String nome, int ordem) {
        Etapa etapa = new Etapa();
        etapa.setNome(nome);
        etapa.setSetor("Engenharia");
        etapa.setOrdem(ordem);
        etapa.setGerenciamento(quadro);
        return etapas.saveAndFlush(etapa);
    }

    protected Processo demanda(Etapa etapa) {
        Processo processo = new Processo();
        processo.setNumeroProcesso("P-" + etapa.getId());
        processo.setPessoa("Pessoa");
        processo.setStatus("Em andamento");
        processo.setEtapa(etapa);
        return processos.saveAndFlush(processo);
    }

    protected java.util.Map<String, Object> conteudoPersistido() {
        java.util.Map<String, Object> dados = new java.util.LinkedHashMap<>();
        for (String tabela : new String[]{"etapas", "processos", "comentarios", "historicos"}) {
            dados.put(tabela, jdbc.queryForList("select * from " + tabela + " order by id"));
        }
        return dados;
    }
}
