package com.patp.sistema;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import com.patp.sistema.model.Etapa;
import com.patp.sistema.model.Processo;
import com.patp.sistema.service.EtapaService;
import com.patp.sistema.service.ProcessoService;

@SpringBootTest
@AutoConfigureMockMvc
class EtapaDemandConcurrencyTests extends ApiIntegrationSupport {
    @Autowired EtapaService etapaService;
    @Autowired ProcessoService processoService;
    @Autowired PlatformTransactionManager transactionManager;

    @ParameterizedTest
    @MethodSource("disputas")
    void removerDisputaComCriarOuMoverSemOrfaoEHistoricoParcial(boolean removerPrimeiro, boolean mover) throws Exception { // ETA-21
        var criador = usuario("Criador"); var quadro = quadro(criador);
        var origem = etapa(quadro, "Origem", 1); var destino = etapa(quadro, "Destino", 2);
        var existente = mover ? demanda(origem) : null;
        String dono = token(criador); String colaborador = token(usuario("Outro"));
        var escrita = new CountDownLatch(1); var liberar = new CountDownLatch(1); var iniciada = new CountDownLatch(1);
        var executor = Executors.newFixedThreadPool(2);
        try {
            var primeira = executor.submit(() -> transacao().executeWithoutResult(status -> {
                if (removerPrimeiro) {
                    etapaService.excluir(dono.substring(7), quadro.getId(), destino.getId(), 0L);
                } else if (mover) {
                    processoService.mudarEtapa(existente.getId(), destino.getId(), colaborador.substring(7));
                } else {
                    Processo novo = new Processo(); novo.setNumeroProcesso("Concorrente"); novo.setPessoa("Pessoa"); novo.setStatus("Em andamento");
                    Etapa referencia = new Etapa(); referencia.setId(destino.getId()); novo.setEtapa(referencia);
                    processoService.salvar(novo, colaborador.substring(7));
                }
                processos.flush();
                escrita.countDown(); aguardar(liberar);
            }));
            assertThat(escrita.await(10, TimeUnit.SECONDS)).isTrue();
            var segunda = executor.submit(() -> {
                iniciada.countDown();
                if (!removerPrimeiro) {
                    return mvc.perform(delete(base(quadro.getId()) + "/" + destino.getId()).header("Authorization", dono)
                            .contentType(MediaType.APPLICATION_JSON).content("{\"versao\":0}")).andReturn();
                }
                return mover
                        ? mvc.perform(put("/api/processos/" + existente.getId() + "/etapa/" + destino.getId()).header("Authorization", colaborador)).andReturn()
                        : mvc.perform(post("/api/processos").header("Authorization", colaborador).contentType(MediaType.APPLICATION_JSON)
                                .content("{\"numeroProcesso\":\"Concorrente\",\"pessoa\":\"Pessoa\",\"status\":\"Em andamento\",\"etapa\":{\"id\":" + destino.getId() + "}}")).andReturn();
            });
            assertThat(iniciada.await(10, TimeUnit.SECONDS)).isTrue();
            assertThatThrownBy(() -> segunda.get(200, TimeUnit.MILLISECONDS)).isInstanceOf(TimeoutException.class);
            liberar.countDown(); primeira.get(10, TimeUnit.SECONDS);
            var resposta = segunda.get(10, TimeUnit.SECONDS).getResponse();
            assertThat(resposta.getStatus()).isEqualTo(removerPrimeiro ? 404 : 409);
            assertThat(resposta.getContentAsString(StandardCharsets.UTF_8)).contains(removerPrimeiro
                    ? "Etapa não encontrada neste gerenciamento."
                    : "Esta etapa possui demandas. Mova-as para outra etapa antes de removê-la.");
            assertThat(etapas.existsById(destino.getId())).isEqualTo(!removerPrimeiro);
            assertThat(etapas.count()).isEqualTo(removerPrimeiro ? 3 : 2);
            if (removerPrimeiro) {
                assertThat(etapas.findAll().subList(1, 3)).extracting("nome").containsExactly("Concluídos", "Cancelados");
                assertThat(etapas.findAll().subList(1, 3)).extracting("categoria").containsExactly(com.patp.sistema.model.CategoriaEtapa.CONCLUIDA, com.patp.sistema.model.CategoriaEtapa.CANCELADA);
            }
            assertThat(quadros.findById(quadro.getId()).orElseThrow().getVersao()).isEqualTo(removerPrimeiro ? 1L : 0L);
            assertThat(jdbc.queryForObject("select count(*) from processos p left join etapas e on e.id=p.etapa_id where e.id is null", Long.class)).isZero();
            assertThat(jdbc.queryForObject("select count(*) from historicos", Long.class)).isEqualTo(removerPrimeiro ? 0L : 1L);
            assertThat(processos.count()).isEqualTo(removerPrimeiro && !mover ? 0 : 1);
            if (mover) {
                assertThat(processos.findById(existente.getId()).orElseThrow().getEtapa().getId())
                        .isEqualTo(removerPrimeiro ? origem.getId() : destino.getId());
            }
        } finally { liberar.countDown(); executor.shutdownNow(); executor.awaitTermination(10, TimeUnit.SECONDS); }
    }

    static Stream<Arguments> disputas() { return Stream.of(false, true).flatMap(r -> Stream.of(false, true).map(m -> Arguments.of(r, m))); }

    @Test
    void duasConfiguracoesComVersaoAntigaNaoSobrescrevem() throws Exception { // ETA-18/19
        disputarConfiguracao(false);
    }

    @Test
    void snapshotEsperaCommitECombinaVersaoComEtapasAtuais() throws Exception { // ETA-01/02
        disputarConfiguracao(true);
    }

    private void disputarConfiguracao(boolean leitura) throws Exception {
        var criador = usuario("Criador"); var quadro = quadro(criador); var etapa = etapa(quadro, "Original", 1); String bearer = token(criador);
        var escrita = new CountDownLatch(1); var liberar = new CountDownLatch(1); var iniciada = new CountDownLatch(1);
        var executor = Executors.newFixedThreadPool(2);
        try {
            var primeira = executor.submit(() -> transacao().executeWithoutResult(status -> {
                etapaService.editar(bearer.substring(7), quadro.getId(), etapa.getId(), "Confirmada", "Projeto", 1, 0L);
                escrita.countDown(); aguardar(liberar);
            }));
            assertThat(escrita.await(10, TimeUnit.SECONDS)).isTrue();
            var segunda = executor.submit(() -> {
                iniciada.countDown();
                return leitura ? mvc.perform(get("/api/gerenciamentos/" + quadro.getId() + "/estrutura-etapas").header("Authorization", bearer)).andReturn()
                        : mvc.perform(post(base(quadro.getId())).header("Authorization", bearer).contentType(MediaType.APPLICATION_JSON)
                                .content("{\"nome\":\"Antiga\",\"setor\":\"Setor\",\"ordem\":1,\"versao\":0}")).andReturn();
            });
            assertThat(iniciada.await(10, TimeUnit.SECONDS)).isTrue();
            assertThatThrownBy(() -> segunda.get(200, TimeUnit.MILLISECONDS)).isInstanceOf(TimeoutException.class);
            liberar.countDown(); primeira.get(10, TimeUnit.SECONDS);
            var resultado = segunda.get(10, TimeUnit.SECONDS);
            if (leitura) {
                mvcResultAssert(resultado);
            } else {
                assertThat(resultado.getResponse().getStatus()).isEqualTo(409);
                assertThat(resultado.getResponse().getContentAsString(StandardCharsets.UTF_8))
                        .contains("Gerenciamento alterado por outro usuário. Atualize e tente novamente.");
            }
            assertThat(etapas.count()).isEqualTo(3);
            assertThat(etapas.findAll().subList(1, 3)).extracting("nome").containsExactly("Concluídos", "Cancelados");
            assertThat(etapas.findAll().subList(1, 3)).extracting("categoria").containsExactly(com.patp.sistema.model.CategoriaEtapa.CONCLUIDA, com.patp.sistema.model.CategoriaEtapa.CANCELADA);
            assertThat(etapas.findById(etapa.getId()).orElseThrow().getNome()).isEqualTo("Confirmada");
            assertThat(etapas.findById(etapa.getId()).orElseThrow().getSetor()).isEqualTo("Projeto");
            assertThat(quadros.findById(quadro.getId()).orElseThrow().getVersao()).isEqualTo(1L);
        } finally { liberar.countDown(); executor.shutdownNow(); executor.awaitTermination(10, TimeUnit.SECONDS); }
    }

    private static void mvcResultAssert(org.springframework.test.web.servlet.MvcResult resultado) throws Exception {
        status().isOk().match(resultado);
        jsonPath("$.gerenciamento.versao").value(1).match(resultado);
        jsonPath("$.etapas.length()").value(3).match(resultado);
        jsonPath("$.etapas[1].nome").value("Concluídos").match(resultado);
        jsonPath("$.etapas[1].categoria").value("CONCLUIDA").match(resultado);
        jsonPath("$.etapas[2].nome").value("Cancelados").match(resultado);
        jsonPath("$.etapas[2].categoria").value("CANCELADA").match(resultado);
        jsonPath("$.etapas[0].nome").value("Confirmada").match(resultado);
        jsonPath("$.etapas[0].setor").value("Projeto").match(resultado);
        jsonPath("$.etapas[0].ordem").value(1).match(resultado);
    }

    @Test
    void edicaoDeDemandaComDestinoRemovidoReverteCamposEHistorico() throws Exception { // ETA-21
        var criador = usuario("Criador"); var quadro = quadro(criador); var origem = etapa(quadro, "Origem", 1);
        var destino = etapa(quadro, "Destino", 2); var processo = demanda(origem); String bearer = token(criador);
        mvc.perform(delete(base(quadro.getId()) + "/" + destino.getId()).header("Authorization", bearer)
                .contentType(MediaType.APPLICATION_JSON).content("{\"versao\":0}")).andExpect(status().isOk());
        var antes = conteudoPersistido();
        mvc.perform(put("/api/processos/" + processo.getId()).header("Authorization", token(usuario("Outro"))).contentType(MediaType.APPLICATION_JSON)
                .content("{\"numeroProcesso\":\"Mudado\",\"pessoa\":\"Outra\",\"etapa\":{\"id\":" + destino.getId() + "}}"))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.erro").value("Etapa não encontrada neste gerenciamento."));
        assertThat(conteudoPersistido()).isEqualTo(antes);
        assertThat(quadros.findById(quadro.getId()).orElseThrow().getVersao()).isEqualTo(1L);
    }

    private TransactionTemplate transacao() {
        var tx = new TransactionTemplate(transactionManager); tx.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED); return tx;
    }
    private static String base(Long id) { return "/api/gerenciamentos/" + id + "/etapas"; }
    private static void aguardar(CountDownLatch latch) {
        try { if (!latch.await(10, TimeUnit.SECONDS)) { throw new IllegalStateException("Prazo de sincronização excedido."); } }
        catch (InterruptedException ex) { Thread.currentThread().interrupt(); throw new IllegalStateException(ex); }
    }
}
