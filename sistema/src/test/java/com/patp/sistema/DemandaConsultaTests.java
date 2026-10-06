package com.patp.sistema;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.util.Map;
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

import com.patp.sistema.model.Processo;
import com.patp.sistema.service.EtapaService;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class DemandaConsultaTests extends ApiIntegrationSupport {
    @Autowired ObjectMapper json;
    @Autowired EtapaService etapaService;
    @Autowired PlatformTransactionManager transactionManager;

    @Test
    void snapshotFlatIncluiTodosCamposOrdenaFiltraEContaMesmoConjunto() throws Exception { // CAD-21/22
        var criador = usuario("Criador"); var quadro = quadro(criador);
        var primeira = etapa(quadro, "Primeira", 5); var segunda = etapa(quadro, "Segunda", 5);
        var completa = demanda(segunda);
        completa.setResponsavel("Ana"); completa.setPrioridade("Urgente");
        completa.setDataEmissao(LocalDate.of(2020, 2, 3)); completa.setPrazoEtapa(LocalDate.of(2020, 2, 4));
        completa.setPrazoGeral(LocalDate.of(2020, 2, 5)); completa.setDataConclusao(LocalDate.of(2020, 2, 6));
        completa.setDataCancelamento(LocalDate.of(2020, 2, 7)); completa.setMotivoCancelamento("Legado");
        completa.setObservacoes("<b>Texto antigo</b>\nOutra linha"); processos.saveAndFlush(completa);
        var minima = demanda(primeira);
        demanda(etapa(quadro(criador), "Externa", 1));
        var resultado = mvc.perform(get(rota(quadro.getId())).header("Authorization", token(usuario("Leitor"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.gerenciamento.id").value(quadro.getId()))
                .andExpect(jsonPath("$.demandas.length()").value(2))
                .andExpect(jsonPath("$.demandas[0].id").value(completa.getId()))
                .andExpect(jsonPath("$.demandas[0].numeroProcesso").value(completa.getNumeroProcesso()))
                .andExpect(jsonPath("$.demandas[0].pessoa").value("Pessoa"))
                .andExpect(jsonPath("$.demandas[0].responsavel").value("Ana"))
                .andExpect(jsonPath("$.demandas[0].status").value("Em andamento"))
                .andExpect(jsonPath("$.demandas[0].prioridade").value("Urgente"))
                .andExpect(jsonPath("$.demandas[0].dataEmissao").value("2020-02-03"))
                .andExpect(jsonPath("$.demandas[0].prazoEtapa").value("2020-02-04"))
                .andExpect(jsonPath("$.demandas[0].prazoGeral").value("2020-02-05"))
                .andExpect(jsonPath("$.demandas[0].dataConclusao").value("2020-02-06"))
                .andExpect(jsonPath("$.demandas[0].dataCancelamento").value("2020-02-07"))
                .andExpect(jsonPath("$.demandas[0].motivoCancelamento").value("Legado"))
                .andExpect(jsonPath("$.demandas[0].observacoes").value("<b>Texto antigo</b>\nOutra linha"))
                .andExpect(jsonPath("$.demandas[0].etapaId").value(segunda.getId()))
                .andExpect(jsonPath("$.demandas[1].id").value(minima.getId()))
                .andExpect(jsonPath("$.demandas[1].etapaId").value(primeira.getId()))
                .andExpect(jsonPath("$.etapas[0].id").value(primeira.getId()))
                .andExpect(jsonPath("$.etapas[0].quantidadeDemandas").value(1))
                .andExpect(jsonPath("$.etapas[1].id").value(segunda.getId()))
                .andExpect(jsonPath("$.etapas[1].quantidadeDemandas").value(1)).andReturn();
        var snapshot = json.readTree(resultado.getResponse().getContentAsString());
        assertThat(snapshot.get("demandas").get(0).propertyNames()).containsExactlyInAnyOrder("id", "numeroProcesso", "pessoa", "responsavel", "status", "prioridade",
                "dataEmissao", "prazoEtapa", "prazoGeral", "dataConclusao", "dataCancelamento", "motivoCancelamento", "observacoes", "etapaId");
        for (String campo : new String[]{"responsavel", "prioridade", "dataEmissao", "prazoEtapa", "prazoGeral", "dataConclusao", "dataCancelamento", "motivoCancelamento", "observacoes"}) {
            assertThat(snapshot.get("demandas").get(1).get(campo).isNull()).as(campo).isTrue();
        }
    }

    @Test
    void snapshotVazioMantemListaEContagemZero() throws Exception { // CAD-21/22
        var criador = usuario("Criador"); var quadro = quadro(criador); etapa(quadro, "Vazia", 1);
        mvc.perform(get(rota(quadro.getId())).header("Authorization", token(criador)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.demandas").isArray())
                .andExpect(jsonPath("$.demandas.length()").value(0)).andExpect(jsonPath("$.etapas[0].quantidadeDemandas").value(0));
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void consultaAtivaOuArquivadaPreservaLegadosSemEscrita(boolean arquivado) throws Exception { // CAD-23
        var criador = usuario("Criador"); var quadro = quadro(criador); var etapa = etapa(quadro, "Concluídos", 8);
        jdbc.update("update etapas set categoria=null,setor=null where id=?", etapa.getId());
        var antiga = demanda(etapa); antiga.setStatus("Status antigo desconhecido"); antiga.setResponsavel("  Legado  ");
        antiga.setObservacoes("x".repeat(10001)); antiga.setMotivoCancelamento("Motivo antigo"); processos.saveAndFlush(antiga);
        quadro.setArquivado(arquivado); quadros.saveAndFlush(quadro);
        var antes = Map.of("conteudo", conteudoPersistido(), "quadros", jdbc.queryForList("select * from gerenciamentos order by id"));
        mvc.perform(get(rota(quadro.getId())).header("Authorization", token(usuario("Leitor"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.gerenciamento.arquivado").value(arquivado))
                .andExpect(jsonPath("$.gerenciamento.versao").value(arquivado ? 1 : 0))
                .andExpect(jsonPath("$.etapas[0].categoria").value("TRABALHO"))
                .andExpect(jsonPath("$.demandas[0].status").value("Status antigo desconhecido"))
                .andExpect(jsonPath("$.demandas[0].responsavel").value("  Legado  "))
                .andExpect(jsonPath("$.demandas[0].observacoes").value("x".repeat(10001)))
                .andExpect(jsonPath("$.demandas[0].motivoCancelamento").value("Motivo antigo"))
                .andExpect(jsonPath("$.demandas[0].dataConclusao").value((Object) null))
                .andExpect(jsonPath("$.demandas[0].dataCancelamento").value((Object) null));
        assertThat(Map.of("conteudo", conteudoPersistido(), "quadros", jdbc.queryForList("select * from gerenciamentos order by id"))).isEqualTo(antes);
    }

    @Test
    void respostasCrudDeEtapasPreservamCartoesContagensECategorias() throws Exception { // CAD-22/24
        var criador = usuario("Criador"); var quadro = quadro(criador); var inicial = etapa(quadro, "Inicial", 1);
        var demanda = demanda(inicial); String bearer = token(criador); String base = "/api/gerenciamentos/" + quadro.getId() + "/etapas";
        var criado = mvc.perform(post(base).header("Authorization", bearer).contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Outra\",\"setor\":\"Engenharia\",\"ordem\":2,\"versao\":0}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.demandas[0].id").value(demanda.getId()))
                .andExpect(jsonPath("$.demandas[0].etapaId").value(inicial.getId()))
                .andExpect(jsonPath("$.etapas[0].quantidadeDemandas").value(1))
                .andExpect(jsonPath("$.etapas[2].categoria").value("CONCLUIDA"))
                .andExpect(jsonPath("$.etapas[3].categoria").value("CANCELADA")).andReturn();
        long outra = json.readTree(criado.getResponse().getContentAsString()).get("etapas").get(1).get("id").asLong();
        mvc.perform(put(base + "/" + inicial.getId()).header("Authorization", bearer).contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Renomeada\",\"setor\":\"Projeto\",\"ordem\":2,\"versao\":1}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.demandas.length()").value(1))
                .andExpect(jsonPath("$.demandas[0].id").value(demanda.getId()))
                .andExpect(jsonPath("$.etapas[1].quantidadeDemandas").value(1));
        mvc.perform(delete(base + "/" + outra).header("Authorization", bearer).contentType(MediaType.APPLICATION_JSON).content("{\"versao\":2}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.demandas[0].id").value(demanda.getId()))
                .andExpect(jsonPath("$.etapas.length()").value(3)).andExpect(jsonPath("$.etapas[0].nome").value("Renomeada"))
                .andExpect(jsonPath("$.etapas[0].quantidadeDemandas").value(1));
    }

    @Test
    void snapshotEsperaCommitECombinaVersaoEtapasCartoesEContagens() throws Exception { // CAD-25
        var criador = usuario("Criador"); var quadro = quadro(criador); var inicial = etapa(quadro, "Original", 1); String bearer = token(criador);
        var escrita = new CountDownLatch(1); var liberar = new CountDownLatch(1); var iniciada = new CountDownLatch(1);
        var executor = Executors.newFixedThreadPool(2);
        try {
            var primeira = executor.submit(() -> {
                var tx = new TransactionTemplate(transactionManager); tx.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
                tx.executeWithoutResult(status -> {
                    etapaService.editar(bearer.substring(7), quadro.getId(), inicial.getId(), "Confirmada", "Projeto", 1, 0L);
                    Processo demanda = new Processo(); demanda.setNumeroProcesso("Depois"); demanda.setPessoa("Pessoa"); demanda.setStatus("Em andamento");
                    demanda.setEtapa(etapas.findById(inicial.getId()).orElseThrow()); processos.saveAndFlush(demanda);
                    escrita.countDown(); aguardar(liberar);
                });
            });
            assertThat(escrita.await(10, TimeUnit.SECONDS)).isTrue();
            var segunda = executor.submit(() -> { iniciada.countDown(); return mvc.perform(get(rota(quadro.getId())).header("Authorization", bearer)).andReturn(); });
            assertThat(iniciada.await(10, TimeUnit.SECONDS)).isTrue();
            assertThatThrownBy(() -> segunda.get(200, TimeUnit.MILLISECONDS)).isInstanceOf(TimeoutException.class);
            liberar.countDown(); primeira.get(10, TimeUnit.SECONDS); var resultado = segunda.get(10, TimeUnit.SECONDS);
            status().isOk().match(resultado); jsonPath("$.gerenciamento.versao").value(1).match(resultado);
            jsonPath("$.etapas[0].nome").value("Confirmada").match(resultado);
            jsonPath("$.etapas[0].quantidadeDemandas").value(1).match(resultado);
            jsonPath("$.demandas.length()").value(1).match(resultado);
            jsonPath("$.demandas[0].numeroProcesso").value("Depois").match(resultado);
            jsonPath("$.demandas[0].etapaId").value(inicial.getId()).match(resultado);
        } finally { liberar.countDown(); executor.shutdownNow(); executor.awaitTermination(10, TimeUnit.SECONDS); }
    }

    private static String rota(Long id) { return "/api/gerenciamentos/" + id + "/estrutura-etapas"; }
    private static void aguardar(CountDownLatch latch) {
        try { if (!latch.await(10, TimeUnit.SECONDS)) { throw new IllegalStateException("Prazo de sincronização excedido."); } }
        catch (InterruptedException erro) { Thread.currentThread().interrupt(); throw new IllegalStateException(erro); }
    }
}
