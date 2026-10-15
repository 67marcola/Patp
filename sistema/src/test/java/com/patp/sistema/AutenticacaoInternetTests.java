package com.patp.sistema;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import java.time.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import com.patp.sistema.model.PapelUsuario;
import com.patp.sistema.service.AprovacaoCadastroService;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest(properties="creral.internet.enabled=true")
@AutoConfigureMockMvc
@Import(AutenticacaoInternetTests.Tempo.class)
class AutenticacaoInternetTests extends ApiIntegrationSupport {
    static class Relogio extends Clock {
        Instant agora = Instant.parse("2026-10-14T12:00:00Z");
        public ZoneId getZone() { return ZoneOffset.UTC; }
        public Clock withZone(ZoneId zone) { return this; }
        public Instant instant() { return agora; }
    }
    @TestConfiguration static class Tempo { @Bean Relogio relogio() { return new Relogio(); } }
    @Autowired Relogio relogio;
    @Autowired ObjectMapper json;
    @Autowired AprovacaoCadastroService aprovacao;

    @Override @BeforeEach void limparDados() {
        jdbc.execute("create table if not exists acessos_usuarios(usuario_id bigint primary key references usuarios(id),aprovado boolean not null,aprovado_por bigint references usuarios(id),aprovado_em timestamp(6))");
        jdbc.update("delete from acessos_usuarios");
        super.limparDados();
        relogio.agora = Instant.parse("2026-10-14T12:00:00Z");
    }
    private org.springframework.mock.web.MockHttpServletResponse cadastro(String email) throws Exception {
        return mvc.perform(post("/api/usuarios/cadastro").contentType("application/json").content(
                "{\"nome\":\"Nova\",\"setor\":\"TI\",\"email\":\""+email+"\",\"senha\":\"ficticia\",\"papel\":\"ADMINISTRADOR\",\"aprovado\":true}"))
                .andReturn().getResponse();
    }
    private org.springframework.mock.web.MockHttpServletResponse login(String email) throws Exception {
        return mvc.perform(post("/api/usuarios/login").contentType("application/json")
                .content("{\"email\":\""+email+"\",\"senha\":\"ficticia\"}" )).andReturn().getResponse();
    }
    private void erro(org.springframework.mock.web.MockHttpServletResponse r, int status, String mensagem) throws Exception {
        assertThat(r.getStatus()).isEqualTo(status);
        assertThat(json.readTree(r.getContentAsString()).path("erro").asString()).isEqualTo(mensagem);
        assertThat(json.readTree(r.getContentAsString()).has("token")).isFalse();
    }

    @Test void cadastroOnline202SemTokenNemPromocaoPorEmail() throws Exception {
        var r = cadastro("admin@gmail.com");
        assertThat(r.getStatus()).isEqualTo(202);
        assertThat(json.readTree(r.getContentAsString())).isEqualTo(json.readTree(
                "{\"estado\":\"PENDENTE\",\"mensagem\":\"Cadastro aguardando aprovação do administrador.\"}"));
        var conta = usuarios.findByEmail("admin@gmail.com").orElseThrow();
        assertThat(conta.getNome()).isEqualTo("Nova"); assertThat(conta.getSetor()).isEqualTo("TI");
        assertThat(conta.getEmail()).isEqualTo("admin@gmail.com");
        assertThat(conta.getPapel()).isEqualTo(PapelUsuario.FUNCIONARIO);
        assertThat(new BCryptPasswordEncoder().matches("ficticia",conta.getSenha())).isTrue();
        assertThat(jdbc.queryForMap("select aprovado,aprovado_por,aprovado_em from acessos_usuarios where usuario_id=?",conta.getId()))
                .containsEntry("APROVADO",false).containsEntry("APROVADO_POR",null).containsEntry("APROVADO_EM",null);
    }

    @Test void pendente403ESemToken401DuplicadoPreservaConta() throws Exception {
        cadastro("nova@example.test");
        var antes = jdbc.queryForList("select * from usuarios");
        erro(login("nova@example.test"),403,AprovacaoCadastroService.MENSAGEM_PENDENTE);
        assertThat(mvc.perform(get("/api/gerenciamentos")).andReturn().getResponse().getStatus()).isEqualTo(401);
        assertThat(cadastro("nova@example.test").getStatus()).isEqualTo(500);
        assertThat(jdbc.queryForList("select * from usuarios")).isEqualTo(antes);
    }

    @Test void falhaDaPendenciaReverteCadastroInteiro() throws Exception {
        jdbc.execute("drop table acessos_usuarios");
        assertThat(cadastro("nova@example.test").getStatus()).isEqualTo(500);
        assertThat(usuarios.findByEmail("nova@example.test")).isEmpty();
        assertThat(usuarios.count()).isZero();
    }

    @Test void sessaoExpiraExatamenteOitoHorasSemRenovacaoPorUso() throws Exception {
        cadastro("nova@example.test");
        var conta = usuarios.findByEmail("nova@example.test").orElseThrow();
        aprovacao.aprovar(conta.getId(),conta.getId());
        var r = login(conta.getEmail());
        assertThat(r.getStatus()).isEqualTo(200);
        JsonNode body = json.readTree(r.getContentAsString());
        assertThat(body.size()).isEqualTo(6);
        assertThat(body.path("id").asLong()).isEqualTo(conta.getId());
        assertThat(body.path("nome").asString()).isEqualTo("Nova"); assertThat(body.path("setor").asString()).isEqualTo("TI");
        assertThat(body.path("email").asString()).isEqualTo(conta.getEmail());
        assertThat(body.path("papel").asString()).isEqualTo("FUNCIONARIO");
        assertThat(body.path("token").asString()).isNotBlank();
        String auth = "Bearer "+body.path("token").asString();
        relogio.agora = relogio.agora.plusSeconds(8*3600-1);
        assertThat(mvc.perform(get("/api/usuarios/me").header("Authorization",auth)).andReturn().getResponse().getStatus()).isEqualTo(200);
        relogio.agora = relogio.agora.plusSeconds(1);
        assertThat(mvc.perform(get("/api/usuarios/me").header("Authorization",auth)).andReturn().getResponse().getStatus()).isEqualTo(401);
    }

    @Test void logoutRevogaTokenESenhaErradaNaoEntra() throws Exception {
        cadastro("nova@example.test");
        var conta = usuarios.findByEmail("nova@example.test").orElseThrow(); aprovacao.aprovar(conta.getId(),conta.getId());
        String auth = "Bearer "+json.readTree(login(conta.getEmail()).getContentAsString()).path("token").asString();
        assertThat(mvc.perform(post("/api/usuarios/logout").header("Authorization",auth)).andReturn().getResponse().getStatus()).isEqualTo(200);
        assertThat(mvc.perform(get("/api/usuarios/me").header("Authorization",auth)).andReturn().getResponse().getStatus()).isEqualTo(401);
        var r = mvc.perform(post("/api/usuarios/login").contentType("application/json")
                .content("{\"email\":\"nova@example.test\",\"senha\":\"errada\"}")).andReturn().getResponse();
        assertThat(r.getStatus()).isEqualTo(500);
        assertThat(json.readTree(r.getContentAsString()).has("token")).isFalse();
    }
}

@SpringBootTest(properties="creral.internet.enabled=false")
@AutoConfigureMockMvc
class AutenticacaoLocalInternetTests extends ApiIntegrationSupport {
    @Autowired ObjectMapper json;
    @Test void cadastroLocalMantemSeisCamposESessaoSemTabelaOnline() throws Exception {
        jdbc.execute("drop table if exists acessos_usuarios");
        var r = mvc.perform(post("/api/usuarios/cadastro").contentType("application/json")
                .content("{\"nome\":\"Local\",\"setor\":null,\"email\":\"local@example.test\",\"senha\":\"ficticia\"}"))
                .andReturn().getResponse();
        assertThat(r.getStatus()).isEqualTo(200);
        var b = json.readTree(r.getContentAsString());
        assertThat(b.size()).isEqualTo(6); assertThat(b.path("token").asString()).isNotBlank();
        assertThat(b.path("id").asLong()).isEqualTo(usuarios.findByEmail("local@example.test").orElseThrow().getId());
        assertThat(b.path("nome").asString()).isEqualTo("Local"); assertThat(b.get("setor").isNull()).isTrue();
        assertThat(b.path("email").asString()).isEqualTo("local@example.test"); assertThat(b.path("papel").asString()).isEqualTo("FUNCIONARIO");
        assertThat(mvc.perform(get("/api/usuarios/me").header("Authorization","Bearer "+b.path("token").asString()))
                .andReturn().getResponse().getStatus()).isEqualTo(200);
    }
}
