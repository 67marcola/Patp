package com.patp.sistema;

import static org.assertj.core.api.Assertions.assertThat;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.core.env.Environment;

import tools.jackson.databind.ObjectMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class AutenticacaoResponseTests extends ApiIntegrationSupport {
    @Autowired ObjectMapper json;
    @Autowired Environment environment;

    @ParameterizedTest
    @ValueSource(strings = {"", "Bearer invalido"})
    void recusaDeSessaoTemJsonUtf8ComTextoLegivel(String authorization) throws Exception { // GER-23, T14
        int port = environment.getRequiredProperty("local.server.port", Integer.class);
        var request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/api/gerenciamentos"))
                .timeout(Duration.ofSeconds(10));
        if (!authorization.isEmpty()) {
            request.header("Authorization", authorization);
        }
        var response = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build()
                .send(request.GET().build(), HttpResponse.BodyHandlers.ofByteArray());
        assertThat(response.statusCode()).isEqualTo(401);
        String body = StandardCharsets.UTF_8.newDecoder()
                .decode(ByteBuffer.wrap(response.body())).toString();
        assertThat(json.readTree(body).path("erro").asString()).isEqualTo(authorization.isEmpty()
                ? "Usuário não autenticado." : "Sessão inválida ou expirada.");
        assertThat(quadros.count()).isZero();
    }
}
