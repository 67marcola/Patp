package com.patp.sistema;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import com.patp.sistema.model.PapelUsuario;
import com.patp.sistema.service.EtapaService;
import com.patp.sistema.service.GerenciamentoService;

@SpringBootTest
@AutoConfigureMockMvc
class EtapaArchiveTests extends ApiIntegrationSupport {
    @Autowired EtapaService etapaService;
    @Autowired GerenciamentoService gerenciamentoService;
    @Autowired PlatformTransactionManager transactionManager;
    private static final String ERRO = "Gerenciamento arquivado. Restaure-o antes de alterar.";
    private static final String CORPO = "{\"nome\":\"Mudada\",\"setor\":\"Operação\",\"ordem\":2}";

    @ParameterizedTest
    @ValueSource(strings = {"POST", "PUT", "DELETE"})
    void arquivadoBloqueiaTresRotasAteParaAdminEPreservaConsulta(String metodo) throws Exception { // GER-20/21
        var admin = usuario("Admin");
        admin.setPapel(PapelUsuario.ADMINISTRADOR);
        usuarios.saveAndFlush(admin);
        var quadro = quadro(admin);
        var etapa = etapa(quadro, "Original", 1);
        quadro.setArquivado(true);
        quadros.saveAndFlush(quadro);
        var antes = conteudoPersistido();
        String base = "/api/gerenciamentos/" + quadro.getId() + "/etapas";
        var request = switch (metodo) {
            case "POST" -> post(base);
            case "PUT" -> put(base + "/" + etapa.getId());
            default -> delete(base + "/" + etapa.getId());
        };
        mvc.perform(request.header("Authorization", token(admin)).contentType(MediaType.APPLICATION_JSON).content(CORPO))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.erro").value(ERRO));
        assertThat(conteudoPersistido()).isEqualTo(antes);
        mvc.perform(get(base).header("Authorization", token(admin))).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1)).andExpect(jsonPath("$[0].id").value(etapa.getId()))
                .andExpect(jsonPath("$[0].nome").value("Original"));
    }

    @Test
    void ativoMantemCriacaoEdicaoExclusaoPeloFuncionario() throws Exception { // GER-21 comportamento ativo
        var quadro = quadro(usuario("Criador"));
        String token = token(usuario("Outro"));
        String base = "/api/gerenciamentos/" + quadro.getId() + "/etapas";
        mvc.perform(post(base).header("Authorization", token).contentType(MediaType.APPLICATION_JSON).content(CORPO))
                .andExpect(status().isOk()).andExpect(jsonPath("$.nome").value("Mudada"));
        var etapa = etapas.findAll().get(0);
        assertThat(etapa.getGerenciamento().getId()).isEqualTo(quadro.getId());
        mvc.perform(put(base + "/" + etapa.getId()).header("Authorization", token).contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Editada\",\"setor\":\"Engenharia\",\"ordem\":3}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.nome").value("Editada")).andExpect(jsonPath("$.ordem").value(3));
        assertThat(etapas.findById(etapa.getId()).orElseThrow().getNome()).isEqualTo("Editada");
        mvc.perform(delete(base + "/" + etapa.getId()).header("Authorization", token)).andExpect(status().isOk());
        assertThat(etapas.count()).isZero();
    }

    @Test
    void arquivamentoConfirmadoPrimeiroRecusaEtapaSemPersistir() throws Exception { // GER-22 arquivo primeiro
        disputar(true);
    }

    @Test
    void escritaConfirmadaPrimeiroPersisteEtapaAntesDoArquivo() throws Exception { // GER-22 escrita primeiro
        disputar(false);
    }

    private void disputar(boolean arquivoPrimeiro) throws Exception {
        var criador = usuario("Criador");
        var quadro = quadro(criador);
        String bearer = token(criador);
        String token = bearer.substring(7);
        CountDownLatch primeiraEscrita = new CountDownLatch(1);
        CountDownLatch liberarCommit = new CountDownLatch(1);
        CountDownLatch segundaIniciada = new CountDownLatch(1);
        var executor = Executors.newFixedThreadPool(2);
        var tx = new TransactionTemplate(transactionManager);
        tx.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        try {
            var primeira = executor.submit(() -> tx.executeWithoutResult(status -> {
                if (arquivoPrimeiro) {
                    gerenciamentoService.mudarEstado(token, quadro.getId(), 0L, true);
                } else {
                    etapaService.criar(quadro.getId(), "Concorrente", "Engenharia", 1);
                    etapas.flush();
                }
                primeiraEscrita.countDown();
                aguardar(liberarCommit);
            }));
            assertThat(primeiraEscrita.await(10, TimeUnit.SECONDS)).isTrue();
            var segunda = executor.submit(() -> {
                segundaIniciada.countDown();
                return arquivoPrimeiro
                        ? mvc.perform(post("/api/gerenciamentos/" + quadro.getId() + "/etapas").header("Authorization", bearer)
                                .contentType(MediaType.APPLICATION_JSON).content(CORPO)).andReturn()
                        : mvc.perform(put("/api/gerenciamentos/" + quadro.getId() + "/arquivar").header("Authorization", bearer)
                                .contentType(MediaType.APPLICATION_JSON).content("{\"versao\":0}")).andReturn();
            });
            assertThat(segundaIniciada.await(10, TimeUnit.SECONDS)).isTrue();
            assertThatThrownBy(() -> segunda.get(200, TimeUnit.MILLISECONDS)).isInstanceOf(TimeoutException.class);
            liberarCommit.countDown();
            primeira.get(10, TimeUnit.SECONDS);
            var resposta = segunda.get(10, TimeUnit.SECONDS).getResponse();
            assertThat(resposta.getStatus()).isEqualTo(arquivoPrimeiro ? 409 : 204);
            if (arquivoPrimeiro) {
                assertThat(resposta.getContentAsString(java.nio.charset.StandardCharsets.UTF_8)).contains(ERRO);
            }
            assertThat(etapas.count()).isEqualTo(arquivoPrimeiro ? 0 : 1);
            if (!arquivoPrimeiro) {
                assertThat(etapas.findAll().get(0).getNome()).isEqualTo("Concorrente");
            }
            assertThat(quadros.findById(quadro.getId()).orElseThrow().isArquivado()).isTrue();
            assertThat(quadros.findById(quadro.getId()).orElseThrow().getVersao()).isEqualTo(1L);
        } finally {
            liberarCommit.countDown();
            executor.shutdownNow();
            executor.awaitTermination(10, TimeUnit.SECONDS);
        }
    }

    private static void aguardar(CountDownLatch latch) {
        try {
            if (!latch.await(10, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Prazo de sincronização excedido.");
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(ex);
        }
    }
}
