package com.patp.sistema;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import com.patp.sistema.model.PapelUsuario;
import com.patp.sistema.model.Usuario;
import com.patp.sistema.repository.UsuarioRepository;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class CadastroSessaoTests extends ApiIntegrationSupport {
    @Autowired ObjectMapper json;
    @Autowired Environment environment;
    @MockitoSpyBean UsuarioRepository repository;

    private HttpResponse<String> request(String method, String path, String body, String token) throws Exception {
        int port = environment.getRequiredProperty("local.server.port", Integer.class);
        var request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/api" + path))
                .timeout(Duration.ofSeconds(10));
        if (token != null) request.header("Authorization", "Bearer " + token);
        if (body != null) request.header("Content-Type", "application/json");
        return HttpClient.newHttpClient().send(request.method(method, body == null
                ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body)).build(),
                HttpResponse.BodyHandlers.ofString());
    }

    private JsonNode session(HttpResponse<String> response, Usuario saved) {
        assertThat(response.statusCode()).isEqualTo(200);
        JsonNode body = json.readTree(response.body());
        assertThat(body.size()).isEqualTo(6);
        assertThat(body.path("token").asString()).isNotBlank();
        assertThat(body.path("id").asLong()).isEqualTo(saved.getId());
        assertThat(body.path("nome").asString()).isEqualTo(saved.getNome());
        assertThat(body.get("setor").isNull() ? null : body.path("setor").asString()).isEqualTo(saved.getSetor());
        assertThat(body.path("email").asString()).isEqualTo(saved.getEmail());
        assertThat(body.path("papel").asString()).isEqualTo(saved.getPapel().name());
        assertThat(body.has("senha")).isFalse();
        assertThat(body.has("hash")).isFalse();
        assertThat(response.body()).doesNotContain(saved.getSenha());
        return body;
    }

    private void usable(JsonNode session, Usuario saved) throws Exception {
        var me = request("GET", "/usuarios/me", null, session.path("token").asString());
        assertThat(me.statusCode()).isEqualTo(200);
        JsonNode body = json.readTree(me.body());
        assertThat(body.path("id").asLong()).isEqualTo(saved.getId());
        assertThat(body.path("nome").asString()).isEqualTo(saved.getNome());
        assertThat(body.path("email").asString()).isEqualTo(saved.getEmail());
        assertThat(body.path("papel").asString()).isEqualTo(saved.getPapel().name());
        var list = request("GET", "/gerenciamentos", null, session.path("token").asString());
        assertThat(list.statusCode()).isEqualTo(200);
        assertThat(json.readTree(list.body()).isArray()).isTrue();
        assertThat(json.readTree(list.body()).size()).isZero();
    }

    private void failure(HttpResponse<String> response, int status, String message) {
        assertThat(response.statusCode()).isEqualTo(status);
        JsonNode body = json.readTree(response.body());
        assertThat(body.path("erro").asString()).isEqualTo(message);
        assertThat(body.has("token")).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"primeiro", "seguinte"})
    void cadastroCriaSessaoDoIdSalvoSemConcederPapel(String order) throws Exception { // AUT-01/02
        if (order.equals("seguinte")) usuario("Anterior");
        long before = usuarios.count();
        var response = request("POST", "/usuarios/cadastro",
                "{\"nome\":\"Novo\",\"setor\":\"Comercial\",\"email\":\"novo@example.test\",\"senha\":\" senha-ficticia \",\"papel\":\"ADMINISTRADOR\"}", null);
        Usuario saved = usuarios.findByEmail("novo@example.test").orElseThrow();
        assertThat(usuarios.count()).isEqualTo(before + 1);
        assertThat(saved.getNome()).isEqualTo("Novo");
        assertThat(saved.getSetor()).isEqualTo("Comercial");
        assertThat(saved.getEmail()).isEqualTo("novo@example.test");
        assertThat(saved.getPapel()).isEqualTo(PapelUsuario.FUNCIONARIO);
        assertThat(jdbc.queryForObject("select papel from usuarios where id=?", String.class, saved.getId())).isEqualTo("FUNCIONARIO");
        assertThat(new BCryptPasswordEncoder().matches(" senha-ficticia ", saved.getSenha())).isTrue();
        assertThat(saved.getSenha()).startsWith("$2").isNotEqualTo(" senha-ficticia ");
        usable(session(response, saved), saved);
    }

    @ParameterizedTest
    @ValueSource(longs = {0, 999999})
    void idRecebidoNaoSubstituiNemCriaConta(long chosen) throws Exception { // AUT-03
        Usuario original = usuario("Original");
        long id = chosen == 0 ? original.getId() : chosen;
        failure(request("POST", "/usuarios/cadastro", "{\"id\":" + id
                + ",\"nome\":\"Substituto\",\"email\":\"outro@example.test\",\"senha\":\"outra-senha\"}", null),
                400, "O ID do usuário deve ser definido pelo sistema.");
        preserved(original);
    }

    private void preserved(Usuario original) {
        assertThat(usuarios.count()).isEqualTo(1);
        Usuario saved = usuarios.findById(original.getId()).orElseThrow();
        assertThat(saved.getNome()).isEqualTo(original.getNome());
        assertThat(saved.getSetor()).isEqualTo(original.getSetor());
        assertThat(saved.getEmail()).isEqualTo(original.getEmail());
        assertThat(saved.getSenha()).isEqualTo(original.getSenha());
        assertThat(saved.getPapel()).isEqualTo(original.getPapel());
    }

    @Test
    void jsonInvalidoNaoGravaConta() throws Exception { // AUT-04
        Usuario original = usuario("Original");
        failure(request("POST", "/usuarios/cadastro", "{\"nome\":", null), 400, "Dados da requisição inválidos.");
        preserved(original);
    }

    @Test
    void emailDuplicadoNaoAlteraConta() throws Exception { // AUT-04
        Usuario original = usuario("Original");
        failure(request("POST", "/usuarios/cadastro",
                "{\"nome\":\"Outro\",\"email\":\"original@example.test\",\"senha\":\"outra-senha\"}", null),
                500, "Não foi possível concluir a operação.");
        preserved(original);
    }

    @Test
    void falhaAoSalvarNaoDevolveSessaoNemGravaConta() throws Exception { // AUT-04
        Usuario original = usuario("Original");
        doThrow(new IllegalStateException("falha fictícia de persistência")).when(repository).save(any(Usuario.class));
        failure(request("POST", "/usuarios/cadastro",
                "{\"nome\":\"Novo\",\"email\":\"novo@example.test\",\"senha\":\"senha-ficticia\"}", null),
                500, "Não foi possível concluir a operação.");
        preserved(original);
    }

    @ParameterizedTest
    @EnumSource(PapelUsuario.class)
    void loginManualContinuaComDtoSeguroEPapelPersistido(PapelUsuario role) throws Exception { // AUT-05
        Usuario saved = usuario("Manual");
        saved.setPapel(role);
        saved.setSetor(role == PapelUsuario.FUNCIONARIO ? null : "Engenharia");
        saved.setSenha(new BCryptPasswordEncoder().encode("senha-ficticia"));
        saved = usuarios.saveAndFlush(saved);
        var response = request("POST", "/usuarios/login", "{\"email\":\"manual@example.test\",\"senha\":\"senha-ficticia\"}", null);
        usable(session(response, saved), saved);
        assertThat(usuarios.count()).isEqualTo(1);
    }
}
