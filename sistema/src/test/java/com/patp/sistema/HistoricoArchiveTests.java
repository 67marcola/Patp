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

import com.patp.sistema.model.PapelUsuario;

@SpringBootTest
@AutoConfigureMockMvc
class HistoricoArchiveTests extends ApiIntegrationSupport {
    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void arquivadoRecusaHistoricoManualPreservandoTodosOsValoresELeitura(boolean admin) throws Exception { // GER-20/21/22/27
        var criador = usuario("Criador");
        var quadro = quadro(criador);
        var demanda = demanda(etapa(quadro, "Inicial", 1));
        jdbc.update("insert into historicos(acao,descricao,data_hora,usuario,processo_id) values('CRIACAO','Anterior',CURRENT_TIMESTAMP,'Pessoa',?)", demanda.getId());
        quadro.setArquivado(true);
        quadros.saveAndFlush(quadro);
        var leitor = usuario("Leitor");
        if (admin) {
            leitor.setPapel(PapelUsuario.ADMINISTRADOR);
            usuarios.saveAndFlush(leitor);
        }
        String token = token(leitor);
        var antes = conteudoPersistido();
        String rota = "/api/processos/" + demanda.getId() + "/historico";
        mvc.perform(post(rota).header("Authorization", token).contentType(MediaType.APPLICATION_JSON)
                .content("{\"acao\":\"MANUAL\",\"descricao\":\"Nova\",\"usuario\":\"Leitor\"}"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.erro").value("Gerenciamento arquivado. Restaure-o antes de alterar."));
        assertThat(conteudoPersistido()).isEqualTo(antes);
        mvc.perform(get(rota).header("Authorization", token)).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1)).andExpect(jsonPath("$[0].acao").value("CRIACAO"))
                .andExpect(jsonPath("$[0].descricao").value("Anterior")).andExpect(jsonPath("$[0].usuario").value("Pessoa"))
                .andExpect(jsonPath("$[0].dataHora").isNotEmpty()).andExpect(jsonPath("$[0].processo.id").value(demanda.getId()))
                .andExpect(jsonPath("$..senha").doesNotExist());
    }

    @Test
    void ativoRegistraManualEAutomaticoNaMesmaProtecaoDepoisPermiteConsultaArquivada() throws Exception { // GER-20/21/22 comportamento ativo
        var criador = usuario("Criador");
        var quadro = quadro(criador);
        var demanda = demanda(etapa(quadro, "Inicial", 1));
        var concluida = etapa(quadro, "Concluídos", 99);
        concluida.setCategoria(com.patp.sistema.model.CategoriaEtapa.CONCLUIDA); etapas.saveAndFlush(concluida);
        String token = token(usuario("Outro"));
        String base = "/api/processos/" + demanda.getId();
        mvc.perform(post(base + "/historico").header("Authorization", token).contentType(MediaType.APPLICATION_JSON)
                .content("{\"acao\":\"MANUAL\",\"descricao\":\"Observação\",\"usuario\":\"Informado\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.acao").value("MANUAL"))
                .andExpect(jsonPath("$.descricao").value("Observação")).andExpect(jsonPath("$.usuario").value("Informado"))
                .andExpect(jsonPath("$.dataHora").isNotEmpty()).andExpect(jsonPath("$.processo.id").value(demanda.getId()));
        mvc.perform(put(base + "/concluir").header("Authorization", token(criador))).andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Concluido"));
        assertThat(jdbc.queryForList("select acao from historicos order by id", String.class)).containsExactly("MANUAL", "CONCLUSAO");
        assertThat(jdbc.queryForList("select usuario from historicos order by id", String.class)).containsExactly("Informado", "Criador");
        assertThat(jdbc.queryForObject("select count(*) from historicos where data_hora is not null and processo_id=?", Long.class, demanda.getId())).isEqualTo(2L);
        var antes = conteudoPersistido();
        mvc.perform(put("/api/gerenciamentos/" + quadro.getId() + "/arquivar").header("Authorization", token(criador))
                .contentType(MediaType.APPLICATION_JSON).content("{\"versao\":1}")).andExpect(status().isNoContent());
        mvc.perform(get(base + "/historico").header("Authorization", token)).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2)).andExpect(jsonPath("$[0].acao").value("MANUAL"))
                .andExpect(jsonPath("$[1].acao").value("CONCLUSAO"));
        assertThat(conteudoPersistido()).isEqualTo(antes);
    }

    @ParameterizedTest
    @ValueSource(strings = {"quadro", "etapaCriar", "etapaEditar", "etapaExcluir", "demandaCriar", "demandaEditar", "mover", "concluir", "cancelar", "excluir", "comentario", "historico"})
    void matrizDeDozeRotasRecusaSessaoAusenteOuInvalidaSemGravacao(String acao) throws Exception { // GER-23 matriz completa
        var criador = usuario("Criador");
        var quadro = quadro(criador);
        var etapa = etapa(quadro, "Inicial", 1);
        var destino = etapa(quadro, "Destino", 2);
        var demanda = demanda(etapa);
        var antes = conteudoPersistido();
        String ger = "/api/gerenciamentos/" + quadro.getId();
        String pro = "/api/processos/" + demanda.getId();
        for (boolean invalida : new boolean[]{false, true}) {
            var request = switch (acao) {
                case "quadro" -> put(ger);
                case "etapaCriar" -> post(ger + "/etapas");
                case "etapaEditar" -> put(ger + "/etapas/" + etapa.getId());
                case "etapaExcluir" -> delete(ger + "/etapas/" + etapa.getId());
                case "demandaCriar" -> post("/api/processos");
                case "demandaEditar" -> put(pro);
                case "mover" -> put(pro + "/etapa/" + destino.getId());
                case "concluir" -> put(pro + "/concluir");
                case "cancelar" -> put(pro + "/cancelar");
                case "excluir" -> delete(pro);
                case "comentario" -> post(pro + "/comentarios");
                default -> post(pro + "/historico");
            };
            if (invalida) {
                request.header("Authorization", "Bearer invalido");
            }
            request.contentType(MediaType.APPLICATION_JSON)
                    .content("{\"nome\":\"Válido\",\"versao\":0,\"setor\":\"Engenharia\",\"ordem\":1,\"numeroProcesso\":\"Novo\",\"pessoa\":\"Pessoa\",\"etapa\":{\"id\":"
                            + etapa.getId() + "},\"motivo\":\"Solicitado\",\"texto\":\"Texto\",\"funcionario\":\"Pessoa\",\"acao\":\"MANUAL\",\"descricao\":\"Descrição\",\"usuario\":\"Pessoa\"}");
            mvc.perform(request).andExpect(status().isUnauthorized());
            assertThat(conteudoPersistido()).isEqualTo(antes);
            assertThat(quadros.findById(quadro.getId()).orElseThrow().getNome()).isEqualTo("Quadro");
            assertThat(quadros.findById(quadro.getId()).orElseThrow().getVersao()).isZero();
        }
    }
}
