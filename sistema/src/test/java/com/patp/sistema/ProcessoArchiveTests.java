package com.patp.sistema;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;

@SpringBootTest
@AutoConfigureMockMvc
class ProcessoArchiveTests extends ApiIntegrationSupport {
    @org.springframework.beans.factory.annotation.Autowired com.patp.sistema.service.ProcessoService transicoes;
    private static final String ERRO = "Gerenciamento arquivado. Restaure-o antes de alterar.";

    @ParameterizedTest
    @ValueSource(strings = {"criar", "editar", "mover", "concluir", "cancelar", "excluir"})
    void seisRotasArquivadasRecusamSemAlterarDemandasOuHistoricos(String acao) throws Exception { // GER-20/21/22 matriz processos
        var criador = usuario("Criador");
        var quadro = quadro(criador);
        var origem = etapa(quadro, "Origem", 1);
        var destino = etapa(quadro, "Destino", 2);
        var demanda = demanda(origem);
        jdbc.update("insert into historicos(acao,descricao,data_hora,usuario,processo_id) values('CRIACAO','Anterior',CURRENT_TIMESTAMP,'Criador',?)", demanda.getId());
        quadro.setArquivado(true);
        quadros.saveAndFlush(quadro);
        var antes = conteudoPersistido();
        String base = "/api/processos/" + demanda.getId();
        var request = switch (acao) {
            case "criar" -> post("/api/processos");
            case "editar" -> put(base);
            case "mover" -> put(base + "/etapa/" + destino.getId());
            case "concluir" -> put(base + "/concluir");
            case "cancelar" -> put(base + "/cancelar");
            default -> delete(base);
        };
        String corpo = acao.equals("cancelar") ? "{\"motivo\":\"Solicitado\"}"
                : "{\"numeroProcesso\":\"Novo\",\"pessoa\":\"Outra\",\"status\":\"Em andamento\",\"etapa\":{\"id\":" + origem.getId() + "}}";
        mvc.perform(request.header("Authorization", token(criador)).contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.erro").value(ERRO));
        assertThat(conteudoPersistido()).isEqualTo(antes);
        mvc.perform(get(base).header("Authorization", token(criador))).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(demanda.getId())).andExpect(jsonPath("$.numeroProcesso").value(demanda.getNumeroProcesso()))
                .andExpect(jsonPath("$.etapa.id").value(origem.getId())).andExpect(jsonPath("$..senha").doesNotExist());
    }

    @ParameterizedTest
    @ValueSource(longs = {0, 999999})
    void postComIdNaoCriaOuSubstituiRegistro(long idEnviado) throws Exception { // contrato POST sem update
        var criador = usuario("Criador");
        var etapa = etapa(quadro(criador), "Inicial", 1);
        var existente = demanda(etapa);
        long id = idEnviado == 0 ? existente.getId() : idEnviado;
        var antes = conteudoPersistido();
        mvc.perform(post("/api/processos").header("Authorization", token(criador)).contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":" + id + ",\"numeroProcesso\":\"Alterado\",\"pessoa\":\"Outra\",\"etapa\":{\"id\":" + etapa.getId() + "}}"))
                .andExpect(status().isBadRequest());
        assertThat(conteudoPersistido()).isEqualTo(antes);
        assertThat(processos.count()).isEqualTo(1);
    }

    @Test
    void postResolveQuadroPersistidoIgnorandoRelacaoAtivaFalsificada() throws Exception { // GER-21 vínculo persistido
        var criador = usuario("Criador");
        var arquivo = quadro(criador);
        var etapaArquivo = etapa(arquivo, "Arquivada", 1);
        arquivo.setArquivado(true);
        quadros.saveAndFlush(arquivo);
        var ativo = quadro(criador);
        var antes = conteudoPersistido();
        mvc.perform(post("/api/processos").header("Authorization", token(criador)).contentType(MediaType.APPLICATION_JSON)
                .content("{\"numeroProcesso\":\"Forjado\",\"pessoa\":\"Pessoa\",\"etapa\":{\"id\":" + etapaArquivo.getId()
                        + ",\"gerenciamento\":{\"id\":" + ativo.getId() + ",\"arquivado\":false}}}"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.erro").value(ERRO));
        assertThat(conteudoPersistido()).isEqualTo(antes);
    }

    @Test
    void ativoConservaCriacaoEdicaoMovimentoFinalizacaoEHistoricosAutomaticos() throws Exception { // GER-21/22 comportamento ativo
        var criador = usuario("Criador");
        var quadro = quadro(criador);
        var origem = etapa(quadro, "Origem", 1);
        var destino = etapa(quadro, "Destino", 3);
        var concluida = etapa(quadro, "Concluídos", 99);
        concluida.setCategoria(com.patp.sistema.model.CategoriaEtapa.CONCLUIDA); etapas.saveAndFlush(concluida);
        var cancelada = etapa(quadro, "Cancelados", 100);
        cancelada.setCategoria(com.patp.sistema.model.CategoriaEtapa.CANCELADA); etapas.saveAndFlush(cancelada);
        var forjado = quadro(null);
        String token = token(usuario("Outro"));
        mvc.perform(post("/api/processos").header("Authorization", token).contentType(MediaType.APPLICATION_JSON)
                .content("{\"numeroProcesso\":\"Ativo\",\"pessoa\":\"Pessoa\",\"status\":\"Em andamento\",\"etapa\":{\"id\":" + origem.getId()
                        + ",\"gerenciamento\":{\"id\":" + forjado.getId() + ",\"arquivado\":true}}}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.etapa.gerenciamento.id").value(quadro.getId()));
        var demanda = processos.findAll().get(0);
        String base = "/api/processos/" + demanda.getId();
        mvc.perform(put(base).header("Authorization", token).contentType(MediaType.APPLICATION_JSON)
                .content("{\"numeroProcesso\":\"Editado\",\"pessoa\":\"Outra\",\"status\":\"Em andamento\",\"observacoes\":\"Observada\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.numeroProcesso").value("Editado"));
        mvc.perform(put(base + "/etapa/" + destino.getId()).header("Authorization", token(criador))).andExpect(status().isOk())
                .andExpect(jsonPath("$.etapa.id").value(destino.getId()));
        mvc.perform(put(base + "/concluir").header("Authorization", token(criador))).andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Concluido")).andExpect(jsonPath("$.dataConclusao").isNotEmpty());
        transicoes.transicionar(token(criador).substring(7), quadro.getId(), demanda.getId(),
                com.patp.sistema.service.ProcessoService.Acao.REABRIR, destino.getId(), null, 2L);
        mvc.perform(put(base + "/cancelar").header("Authorization", token(criador)).contentType(MediaType.APPLICATION_JSON).content("{\"motivo\":\"Solicitado\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("Cancelado"))
                .andExpect(jsonPath("$.motivoCancelamento").value("Solicitado")).andExpect(jsonPath("$.dataCancelamento").isNotEmpty());
        var salvo = processos.findById(demanda.getId()).orElseThrow();
        assertThat(salvo.getNumeroProcesso()).isEqualTo("Editado");
        assertThat(salvo.getEtapa().getId()).isEqualTo(cancelada.getId());
        assertThat(salvo.getStatus()).isEqualTo("Cancelado");
        assertThat(salvo.getMotivoCancelamento()).isEqualTo("Solicitado");
        assertThat(jdbc.queryForList("select acao from historicos order by id", String.class))
                .containsExactly("CRIACAO", "EDICAO", "MUDANCA_ETAPA", "CONCLUSAO", "REABERTURA", "CANCELAMENTO");
        assertThat(jdbc.queryForList("select usuario from historicos order by id", String.class)).containsExactly("Outro", "Outro", "Criador", "Criador", "Criador", "Criador");
    }

    @Test
    void falhaPreexistenteDeExclusaoAtivaNaoDeixaHistoricoParcial() throws Exception { // consistência GER-22 limite ativo preexistente
        var criador = usuario("Criador");
        var demanda = demanda(etapa(quadro(criador), "Inicial", 1));
        var antes = conteudoPersistido();
        mvc.perform(delete("/api/processos/" + demanda.getId()).header("Authorization", token(criador)))
                .andExpect(status().isInternalServerError()).andExpect(jsonPath("$.erro").value("Não foi possível concluir a operação."));
        assertThat(conteudoPersistido()).isEqualTo(antes);
    }
}
