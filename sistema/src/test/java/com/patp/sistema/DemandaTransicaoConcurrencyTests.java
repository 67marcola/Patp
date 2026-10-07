package com.patp.sistema;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import java.time.LocalDate;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import com.patp.sistema.model.CategoriaEtapa;
import com.patp.sistema.service.ProcessoService;
import com.patp.sistema.service.ProcessoService.Acao;
import com.patp.sistema.service.EtapaService;
import com.patp.sistema.service.GerenciamentoService;

@SpringBootTest
@AutoConfigureMockMvc
class DemandaTransicaoConcurrencyTests extends ApiIntegrationSupport {
    @Autowired ProcessoService service;
    @Autowired EtapaService etapaService;
    @Autowired GerenciamentoService gerenciamentoService;
    @Autowired PlatformTransactionManager transactionManager;

    @ParameterizedTest @EnumSource(Acao.class)
    void duasAcoesComMesmaVersaoConfirmamSomenteUmEvento(Acao acao) throws Exception { // MOV-20/21/23/24
        var dono = usuario("Dono"); var q = quadro(dono);
        var origem = etapa(q, "Origem", 1); var destino = etapa(q, "Destino", 2);
        var concluida = etapa(q, "Concluídos", 3); concluida.setCategoria(CategoriaEtapa.CONCLUIDA); etapas.saveAndFlush(concluida);
        var cancelada = etapa(q, "Cancelados", 4); cancelada.setCategoria(CategoriaEtapa.CANCELADA); etapas.saveAndFlush(cancelada);
        var p = demanda(origem);
        if (acao == Acao.REABRIR) { p.setEtapa(cancelada); p.setStatus("Cancelado"); p.setDataCancelamento(LocalDate.of(2020, 2, 29)); p.setMotivoCancelamento("Anterior"); processos.saveAndFlush(p); }
        String bearer = token(dono);
        var escrita = new CountDownLatch(1); var liberar = new CountDownLatch(1); var iniciada = new CountDownLatch(1);
        var executor = Executors.newFixedThreadPool(2);
        try {
            var primeira = executor.submit(() -> transacao().executeWithoutResult(status -> {
                service.transicionar(bearer.substring(7), q.getId(), p.getId(), acao, destino.getId(), "Motivo", 0L);
                escrita.countDown(); aguardar(liberar);
            }));
            assertThat(escrita.await(10, TimeUnit.SECONDS)).isTrue();
            var segunda = executor.submit(() -> {
                iniciada.countDown();
                String corpo = "{\"versao\":0" + (acao == Acao.MOVER || acao == Acao.REABRIR ? ",\"etapaId\":" + destino.getId() : acao == Acao.CANCELAR ? ",\"motivo\":\"Outro motivo\"" : "") + "}";
                return mvc.perform(put("/api/gerenciamentos/" + q.getId() + "/demandas/" + p.getId() + "/" + acao.name().toLowerCase())
                        .header("Authorization", bearer).contentType(MediaType.APPLICATION_JSON).content(corpo)).andReturn();
            });
            assertThat(iniciada.await(10, TimeUnit.SECONDS)).isTrue();
            assertThatThrownBy(() -> segunda.get(200, TimeUnit.MILLISECONDS)).isInstanceOf(TimeoutException.class);
            liberar.countDown(); primeira.get(10, TimeUnit.SECONDS);
            var antes = conteudoPersistido();
            var resposta = segunda.get(10, TimeUnit.SECONDS).getResponse();
            assertThat(resposta.getStatus()).isEqualTo(409);
            assertThat(resposta.getContentAsString(java.nio.charset.StandardCharsets.UTF_8)).contains("Gerenciamento alterado por outro usuário. Atualize e tente novamente.");
            assertThat(conteudoPersistido()).isEqualTo(antes);
            assertThat(quadros.findById(q.getId()).orElseThrow().getVersao()).isEqualTo(1L);
            var salvo = processos.findById(p.getId()).orElseThrow();
            assertThat(salvo.getEtapa().getId()).isEqualTo(acao == Acao.CONCLUIR ? concluida.getId() : acao == Acao.CANCELAR ? cancelada.getId() : destino.getId());
            assertThat(salvo.getStatus()).isEqualTo(acao == Acao.CONCLUIR ? "Concluido" : acao == Acao.CANCELAR ? "Cancelado" : "Em andamento");
            assertThat(salvo.getDataConclusao()).isEqualTo(acao == Acao.CONCLUIR ? LocalDate.now() : null);
            assertThat(salvo.getDataCancelamento()).isEqualTo(acao == Acao.CANCELAR ? LocalDate.now() : null);
            assertThat(salvo.getMotivoCancelamento()).isEqualTo(acao == Acao.CANCELAR ? "Motivo" : null);
            assertThat(jdbc.queryForList("select acao from historicos order by id", String.class)).containsExactly(acao == Acao.MOVER ? "MUDANCA_ETAPA" : acao == Acao.CONCLUIR ? "CONCLUSAO" : acao == Acao.CANCELAR ? "CANCELAMENTO" : "REABERTURA");
            assertThat(jdbc.queryForList("select usuario from historicos", String.class)).containsExactly("Dono");
        } finally { liberar.countDown(); executor.shutdownNow(); executor.awaitTermination(10, TimeUnit.SECONDS); }
    }

    @ParameterizedTest @MethodSource("disputas")
    void movimentoDisputaComEstruturaEArquivo(String alteracao, boolean estruturaPrimeiro) throws Exception { // MOV-24
        var dono = usuario("Dono"); var q = quadro(dono);
        var origem = etapa(q, "Origem", 1); var destino = etapa(q, "Destino", 2); var p = demanda(origem); String bearer = token(dono);
        var escrita = new CountDownLatch(1); var liberar = new CountDownLatch(1); var iniciada = new CountDownLatch(1);
        var executor = Executors.newFixedThreadPool(2);
        try {
            var primeira = executor.submit(() -> transacao().executeWithoutResult(status -> {
                if (estruturaPrimeiro) alterar(alteracao, bearer, q.getId(), destino.getId(), 0L);
                else service.transicionar(bearer.substring(7), q.getId(), p.getId(), Acao.MOVER, destino.getId(), null, 0L);
                escrita.countDown(); aguardar(liberar);
            }));
            assertThat(escrita.await(10, TimeUnit.SECONDS)).isTrue();
            var segunda = executor.submit(() -> {
                iniciada.countDown();
                String base = "/api/gerenciamentos/" + q.getId();
                var req = estruturaPrimeiro ? put(base + "/demandas/" + p.getId() + "/mover")
                        : alteracao.equals("arquivo") ? put(base + "/arquivar") : alteracao.equals("remocao")
                        ? org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete(base + "/etapas/" + destino.getId()) : put(base + "/etapas/" + destino.getId());
                String corpo = estruturaPrimeiro ? "{\"versao\":1,\"etapaId\":" + destino.getId() + "}" : alteracao.equals("reordenacao") ? "{\"versao\":1,\"nome\":\"Destino\",\"setor\":\"Engenharia\",\"ordem\":1}" : "{\"versao\":1}";
                return mvc.perform(req.header("Authorization", bearer).contentType(MediaType.APPLICATION_JSON).content(corpo)).andReturn();
            });
            assertThat(iniciada.await(10, TimeUnit.SECONDS)).isTrue();
            assertThatThrownBy(() -> segunda.get(200, TimeUnit.MILLISECONDS)).isInstanceOf(TimeoutException.class);
            liberar.countDown(); primeira.get(10, TimeUnit.SECONDS);
            int esperado = alteracao.equals("arquivo") ? (estruturaPrimeiro ? 409 : 204) : alteracao.equals("remocao") ? (estruturaPrimeiro ? 404 : 409) : 200;
            assertThat(segunda.get(10, TimeUnit.SECONDS).getResponse().getStatus()).isEqualTo(esperado);
            boolean movido = !estruturaPrimeiro || alteracao.equals("reordenacao");
            assertThat(processos.findById(p.getId()).orElseThrow().getEtapa().getId()).isEqualTo(movido ? destino.getId() : origem.getId());
            assertThat(processos.findById(p.getId()).orElseThrow().getStatus()).isEqualTo("Em andamento");
            assertThat(jdbc.queryForList("select acao from historicos", String.class)).containsExactly(movido ? new String[]{"MUDANCA_ETAPA"} : new String[]{});
            assertThat(quadros.findById(q.getId()).orElseThrow().getVersao()).isEqualTo(esperado == 200 || esperado == 204 ? 2L : 1L);
            assertThat(quadros.findById(q.getId()).orElseThrow().isArquivado()).isEqualTo(alteracao.equals("arquivo"));
            assertThat(etapas.existsById(destino.getId())).isEqualTo(!estruturaPrimeiro || !alteracao.equals("remocao"));
            if (alteracao.equals("reordenacao")) {
                assertThat(etapas.findById(destino.getId()).orElseThrow().getOrdem()).isEqualTo(1);
                assertThat(etapas.findById(origem.getId()).orElseThrow().getOrdem()).isEqualTo(2);
            }
            assertThat(jdbc.queryForObject("select count(*) from processos p left join etapas e on e.id=p.etapa_id where e.id is null", Long.class)).isZero();
        } finally { liberar.countDown(); executor.shutdownNow(); executor.awaitTermination(10, TimeUnit.SECONDS); }
    }

    private void alterar(String acao, String bearer, Long quadro, Long destino, Long versao) {
        if (acao.equals("arquivo")) gerenciamentoService.mudarEstado(bearer.substring(7), quadro, versao, true);
        else if (acao.equals("remocao")) etapaService.excluir(bearer.substring(7), quadro, destino, versao);
        else etapaService.editar(bearer.substring(7), quadro, destino, "Destino", "Engenharia", 1, versao);
    }
    static Stream<Arguments> disputas() { return Stream.of("arquivo", "remocao", "reordenacao").flatMap(a -> Stream.of(false, true).map(p -> Arguments.of(a, p))); }
    private TransactionTemplate transacao() { var tx = new TransactionTemplate(transactionManager); tx.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED); return tx; }
    private static void aguardar(CountDownLatch latch) {
        try { if (!latch.await(10, TimeUnit.SECONDS)) throw new IllegalStateException("Prazo de sincronização excedido."); }
        catch (InterruptedException erro) { Thread.currentThread().interrupt(); throw new IllegalStateException(erro); }
    }
}
