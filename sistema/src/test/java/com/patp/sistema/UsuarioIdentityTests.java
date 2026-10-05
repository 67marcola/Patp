package com.patp.sistema;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;

import com.patp.sistema.model.Gerenciamento;
import com.patp.sistema.model.Usuario;
import com.patp.sistema.repository.GerenciamentoRepository;

@SpringBootTest
@AutoConfigureMockMvc
class UsuarioIdentityTests extends ApiIntegrationSupport {
    @Autowired GerenciamentoRepository gerenciamentos;

    @Test
    void cadastroNaoConcedePapelNemExibeSenha() throws Exception { // GER-24/27
        String resposta = mvc.perform(post("/api/usuarios/cadastro")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Novo\",\"email\":\"novo@example.test\",\"senha\":\"senha-ficticia\",\"papel\":\"ADMINISTRADOR\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.papel").value("FUNCIONARIO"))
                .andExpect(jsonPath("$..senha").doesNotExist())
                .andReturn().getResponse().getContentAsString();
        Usuario salvo = usuarios.findByEmail("novo@example.test").orElseThrow();
        assertThat(jdbc.queryForObject("select papel from usuarios where id=?", String.class, salvo.getId())).isEqualTo("FUNCIONARIO");
        assertThat(salvo.getSenha()).isNotEqualTo("senha-ficticia");
        assertThat(resposta).doesNotContain(salvo.getSenha());
    }

    @Test
    void cadastroComIdExistenteNaoSubstituiConta() throws Exception { // GER-25
        Usuario original = usuario("Original");
        mvc.perform(post("/api/usuarios/cadastro").contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":" + original.getId() + ",\"nome\":\"Substituto\",\"email\":\"outro@example.test\",\"senha\":\"outra\"}"))
                .andExpect(status().isBadRequest());
        assertThat(usuarios.count()).isEqualTo(1);
        Usuario preservado = usuarios.findById(original.getId()).orElseThrow();
        assertThat(preservado.getNome()).isEqualTo("Original");
        assertThat(preservado.getEmail()).isEqualTo("original@example.test");
        assertThat(preservado.getSenha()).isEqualTo("hash-ficticio-nao-publico");
    }

    @Test
    void cadastroComIdEscolhidoNaoCriaConta() throws Exception { // GER-25
        mvc.perform(post("/api/usuarios/cadastro").contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":999999,\"nome\":\"Novo\",\"email\":\"novo@example.test\",\"senha\":\"outra\"}"))
                .andExpect(status().isBadRequest());
        assertThat(usuarios.count()).isZero();
    }

    @Test
    void papelPromovidoNoServidorValeMesmoComSessaoAnterior() throws Exception { // GER-26
        Usuario escolhido = usuario("Escolhido");
        String token = token(escolhido);
        jdbc.update("update usuarios set papel='ADMINISTRADOR' where id=?", escolhido.getId());
        mvc.perform(get("/api/usuarios/me").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(escolhido.getId()))
                .andExpect(jsonPath("$.papel").value("ADMINISTRADOR"))
                .andExpect(jsonPath("$..senha").doesNotExist());
    }

    @Test
    void usuarioLegadoSemPapelContinuaFuncionario() throws Exception { // T2 legado
        Usuario legado = usuario("Legado");
        jdbc.update("update usuarios set papel=null where id=?", legado.getId());
        mvc.perform(get("/api/usuarios/me").header("Authorization", token(legado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.papel").value("FUNCIONARIO"));
    }

    @Test
    void respostasDiretasEAninhadasNaoDivulgamHash() throws Exception { // GER-27
        Usuario criador = usuario("Criador");
        Gerenciamento quadro = new Gerenciamento();
        quadro.setNome("Quadro");
        quadro.setCriador(criador);
        gerenciamentos.saveAndFlush(quadro);
        String token = token(criador);
        for (String rota : new String[]{"/api/usuarios/" + criador.getId(), "/api/gerenciamentos/" + quadro.getId()}) {
            String resposta = mvc.perform(get(rota).header("Authorization", token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$..senha").doesNotExist())
                    .andReturn().getResponse().getContentAsString();
            assertThat(resposta).doesNotContain(criador.getSenha());
        }
    }

    @Test
    void rotasProtegidasRecusamSessaoAusenteOuInvalida() throws Exception { // GER-23
        mvc.perform(get("/api/gerenciamentos")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/gerenciamentos").header("Authorization", "Bearer invalido"))
                .andExpect(status().isUnauthorized());
    }
}
