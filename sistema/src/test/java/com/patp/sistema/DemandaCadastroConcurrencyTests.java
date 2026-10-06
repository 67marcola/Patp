package com.patp.sistema;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

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

import com.patp.sistema.dto.CriarDemandaRequest;
import com.patp.sistema.model.Etapa;
import com.patp.sistema.model.Processo;
import com.patp.sistema.service.EtapaService;
import com.patp.sistema.service.GerenciamentoService;
import com.patp.sistema.service.ProcessoService;

@SpringBootTest
@AutoConfigureMockMvc
class DemandaCadastroConcurrencyTests extends ApiIntegrationSupport {
    @Autowired ProcessoService processoService;
    @Autowired GerenciamentoService gerenciamentoService;
    @Autowired EtapaService etapaService;
    @Autowired PlatformTransactionManager transactionManager;

    @ParameterizedTest
    @MethodSource("rotasDuplicadas")
    void numeroEntreQuadrosDisputaAteCommitEConfirmaSomenteUma(boolean legadoPrimeiro, boolean legadoSegundo, String segundoNumero) throws Exception { // CAD-13/17/18
        var criador = usuario("Criador"); var primeiro = quadro(criador); var etapaPrimeira = etapa(primeiro, "Inicial", 1);
        var segundo = quadro(criador); var etapaSegunda = etapa(segundo, "Inicial", 1); String bearer = token(criador);
        var escrita = new CountDownLatch(1); var liberar = new CountDownLatch(1); var iniciada = new CountDownLatch(1); var executor = Executors.newFixedThreadPool(2);
        try {
            var primeira = executor.submit(() -> transacao().executeWithoutResult(status -> {
                if (legadoPrimeiro) {
                    Processo demanda = new Processo(); demanda.setNumeroProcesso("Igual"); demanda.setPessoa("Pessoa");
                    Etapa referencia = new Etapa(); referencia.setId(etapaPrimeira.getId()); demanda.setEtapa(referencia);
                    processoService.salvar(demanda, bearer.substring(7));
                } else { processoService.criar(bearer.substring(7), primeiro.getId(), dados("Igual")); }
                escrita.countDown(); aguardar(liberar);
            }));
            assertThat(escrita.await(10, TimeUnit.SECONDS)).isTrue();
            var segunda = executor.submit(() -> { iniciada.countDown(); return mvc.perform(post(legadoSegundo ? "/api/processos" : rota(segundo.getId())).header("Authorization", bearer)
                    .contentType(MediaType.APPLICATION_JSON).content("{\"numeroProcesso\":\"" + segundoNumero + "\",\"pessoa\":\"Pessoa\"," +
                            (legadoSegundo ? "\"etapa\":{\"id\":" + etapaSegunda.getId() + "}" : "\"versao\":0") + "}")).andReturn(); });
            assertThat(iniciada.await(10, TimeUnit.SECONDS)).isTrue();
            assertThatThrownBy(() -> segunda.get(200, TimeUnit.MILLISECONDS)).isInstanceOf(TimeoutException.class);
            liberar.countDown(); primeira.get(10, TimeUnit.SECONDS); var resposta = segunda.get(10, TimeUnit.SECONDS).getResponse();
            assertThat(resposta.getStatus()).isEqualTo(409);
            assertThat(resposta.getContentAsString(StandardCharsets.UTF_8)).contains("Já existe uma demanda com esse número.");
            assertThat(processos.count()).isEqualTo(1); assertThat(processos.findAll().get(0).getNumeroProcesso()).isEqualTo("Igual");
            assertThat(processos.findAll().get(0).getEtapa().getGerenciamento().getId()).isEqualTo(primeiro.getId());
            assertThat(jdbc.queryForList("select acao from historicos", String.class)).containsExactly("CRIACAO");
            assertThat(quadros.findById(primeiro.getId()).orElseThrow().getVersao()).isEqualTo(legadoPrimeiro ? 0L : 1L);
            assertThat(quadros.findById(segundo.getId()).orElseThrow().getVersao()).isZero();
        } finally { liberar.countDown(); executor.shutdownNow(); executor.awaitTermination(10, TimeUnit.SECONDS); }
    }

    static Stream<Arguments> rotasDuplicadas() {
        return Stream.of(false, true).flatMap(p -> Stream.of(false, true).flatMap(s -> Stream.of("Igual", "igual").map(n -> Arguments.of(p, s, n))));
    }

    @ParameterizedTest
    @MethodSource("disputas")
    void cadastroDisputaArquivoRemocaoEReordenacaoSemEstadoParcial(String acao, boolean mutacaoPrimeiro) throws Exception { // CAD-17
        var criador = usuario("Criador"); var quadro = quadro(criador); var inicial = etapa(quadro, "Inicial", 1); etapa(quadro, "Seguinte", 2); String bearer = token(criador);
        var escrita = new CountDownLatch(1); var liberar = new CountDownLatch(1); var iniciada = new CountDownLatch(1); var executor = Executors.newFixedThreadPool(2);
        try {
            var primeira = executor.submit(() -> transacao().executeWithoutResult(status -> {
                if (!mutacaoPrimeiro) { processoService.criar(bearer.substring(7), quadro.getId(), dados("Disputa")); }
                else if (acao.equals("arquivo")) { gerenciamentoService.mudarEstado(bearer.substring(7), quadro.getId(), 0L, true); }
                else if (acao.equals("remocao")) { etapaService.excluir(bearer.substring(7), quadro.getId(), inicial.getId(), 0L); }
                else { etapaService.editar(bearer.substring(7), quadro.getId(), inicial.getId(), "Reordenada", "Projeto", 2, 0L); }
                escrita.countDown(); aguardar(liberar);
            }));
            assertThat(escrita.await(10, TimeUnit.SECONDS)).isTrue();
            var segunda = executor.submit(() -> {
                iniciada.countDown();
                if (mutacaoPrimeiro) { return mvc.perform(post(rota(quadro.getId())).header("Authorization", bearer).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"numeroProcesso\":\"Disputa\",\"pessoa\":\"Pessoa\",\"versao\":0}")).andReturn(); }
                String base = "/api/gerenciamentos/" + quadro.getId();
                if (acao.equals("arquivo")) { return mvc.perform(put(base + "/arquivar").header("Authorization", bearer).contentType(MediaType.APPLICATION_JSON).content("{\"versao\":1}")).andReturn(); }
                if (acao.equals("remocao")) { return mvc.perform(delete(base + "/etapas/" + inicial.getId()).header("Authorization", bearer).contentType(MediaType.APPLICATION_JSON).content("{\"versao\":1}")).andReturn(); }
                return mvc.perform(put(base + "/etapas/" + inicial.getId()).header("Authorization", bearer).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Reordenada\",\"setor\":\"Projeto\",\"ordem\":2,\"versao\":1}")).andReturn();
            });
            assertThat(iniciada.await(10, TimeUnit.SECONDS)).isTrue();
            assertThatThrownBy(() -> segunda.get(200, TimeUnit.MILLISECONDS)).isInstanceOf(TimeoutException.class);
            liberar.countDown(); primeira.get(10, TimeUnit.SECONDS); var resposta = segunda.get(10, TimeUnit.SECONDS).getResponse();
            int esperado = mutacaoPrimeiro || acao.equals("remocao") ? 409 : acao.equals("arquivo") ? 204 : 200;
            assertThat(resposta.getStatus()).isEqualTo(esperado);
            if (mutacaoPrimeiro) {
                assertThat(resposta.getContentAsString(StandardCharsets.UTF_8)).contains(acao.equals("arquivo")
                        ? "Gerenciamento arquivado. Restaure-o antes de alterar." : "Gerenciamento alterado por outro usuário. Atualize e tente novamente.");
            } else if (acao.equals("remocao")) {
                assertThat(resposta.getContentAsString(StandardCharsets.UTF_8)).contains("Esta etapa possui demandas. Mova-as para outra etapa antes de removê-la.");
            }
            assertThat(processos.count()).isEqualTo(mutacaoPrimeiro ? 0 : 1);
            assertThat(jdbc.queryForObject("select count(*) from historicos", Long.class)).isEqualTo(mutacaoPrimeiro ? 0L : 1L);
            assertThat(jdbc.queryForObject("select count(*) from processos p left join etapas e on e.id=p.etapa_id where e.id is null", Long.class)).isZero();
            assertThat(quadros.findById(quadro.getId()).orElseThrow().getVersao()).isEqualTo(mutacaoPrimeiro || acao.equals("remocao") ? 1L : 2L);
            assertThat(quadros.findById(quadro.getId()).orElseThrow().isArquivado()).isEqualTo(acao.equals("arquivo"));
            assertThat(etapas.existsById(inicial.getId())).isEqualTo(!mutacaoPrimeiro || !acao.equals("remocao"));
            if (!mutacaoPrimeiro) { assertThat(processos.findAll().get(0).getEtapa().getId()).isEqualTo(inicial.getId()); }
            if (acao.equals("reordenacao")) { assertThat(etapas.findById(inicial.getId()).orElseThrow().getOrdem()).isEqualTo(2); }
        } finally { liberar.countDown(); executor.shutdownNow(); executor.awaitTermination(10, TimeUnit.SECONDS); }
    }

    static Stream<Arguments> disputas() { return Stream.of("arquivo", "remocao", "reordenacao").flatMap(a -> Stream.of(false, true).map(p -> Arguments.of(a, p))); }
    private static CriarDemandaRequest dados(String numero) { return new CriarDemandaRequest(0L, numero, "Pessoa", null, null, null, null, null, null); }
    private static String rota(Long id) { return "/api/gerenciamentos/" + id + "/demandas"; }
    private TransactionTemplate transacao() { var tx = new TransactionTemplate(transactionManager); tx.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED); return tx; }
    private static void aguardar(CountDownLatch latch) {
        try { if (!latch.await(10, TimeUnit.SECONDS)) { throw new IllegalStateException("Prazo de sincronização excedido."); } }
        catch (InterruptedException erro) { Thread.currentThread().interrupt(); throw new IllegalStateException(erro); }
    }
}
