package com.patp.sistema;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import com.patp.sistema.model.CategoriaEtapa;
import com.patp.sistema.model.Etapa;
import com.patp.sistema.model.PapelUsuario;
import com.patp.sistema.service.GerenciamentoService;

@SpringBootTest
@AutoConfigureMockMvc
class EtapaFinaisApiTests extends ApiIntegrationSupport {
    @Autowired GerenciamentoService gerenciamentos;

    private String base(Long id) { return "/api/gerenciamentos/" + id + "/etapas"; }
    private String corpo(String nome, String setor, int ordem, long versao) {
        return "{\"nome\":\"" + nome + "\",\"setor\":\"" + setor + "\",\"ordem\":" + ordem + ",\"versao\":" + versao + "}";
    }
    private Object estado() {
        return List.of(conteudoPersistido(), jdbc.queryForList("select * from gerenciamentos order by id"));
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void criadorEAdminNaoEditamNemRemovemAsDuasFinaisVazias(boolean administrador) throws Exception {
        var criador = usuario("Criador"); var ator = administrador ? usuario("Admin") : criador;
        if (administrador) { ator.setPapel(PapelUsuario.ADMINISTRADOR); usuarios.saveAndFlush(ator); }
        String bearer = token(criador);
        var quadro = gerenciamentos.criarGerenciamento(bearer.substring(7), "Novo", "D", null);
        var antes = estado();
        for (var etapa : etapas.findAll()) {
            mvc.perform(put(base(quadro.id()) + "/" + etapa.getId()).header("Authorization", token(ator))
                    .contentType(MediaType.APPLICATION_JSON).content(corpo("Mudada", "Campo", 1, 0)))
                    .andExpect(status().isConflict()).andExpect(jsonPath("$.erro").value("Etapas finais obrigatórias não podem ser alteradas."));
            mvc.perform(delete(base(quadro.id()) + "/" + etapa.getId()).header("Authorization", token(ator))
                    .contentType(MediaType.APPLICATION_JSON).content("{\"versao\":0}"))
                    .andExpect(status().isConflict()).andExpect(jsonPath("$.erro").value("Etapas finais obrigatórias não podem ser alteradas."));
            assertThat(estado()).isEqualTo(antes);
        }
    }

    @Test
    void cicloTrabalhoHomonimoPreservaFinaisEUsaSomentePosicoesDeTrabalho() throws Exception {
        var criador = usuario("Criador"); var quadro = quadro(criador); String bearer = token(criador);
        var trabalho = etapa(quadro, "Concluídos", 8);
        String base = base(quadro.getId());
        mvc.perform(put(base + "/" + trabalho.getId()).header("Authorization", bearer).contentType(MediaType.APPLICATION_JSON)
                .content(corpo("Cancelados", "Campo", 1, 0))).andExpect(status().isOk())
                .andExpect(jsonPath("$.etapas[0].categoria").value("TRABALHO"))
                .andExpect(jsonPath("$.etapas[1].nome").value("Concluídos"))
                .andExpect(jsonPath("$.etapas[1].categoria").value("CONCLUIDA"))
                .andExpect(jsonPath("$.etapas[2].nome").value("Cancelados"))
                .andExpect(jsonPath("$.etapas[2].categoria").value("CANCELADA"));
        var finaisAntes = jdbc.queryForList("select * from etapas where categoria <> 'TRABALHO' order by id");
        var finais = etapas.findAll().stream().filter(e -> e.getCategoria() != CategoriaEtapa.TRABALHO).toList();
        var primeiraDemanda = demanda(finais.get(0));
        primeiraDemanda.setNumeroProcesso("Primeira-final"); processos.saveAndFlush(primeiraDemanda);
        demanda(finais.get(0)); demanda(finais.get(1));
        mvc.perform(post(base).header("Authorization", bearer).contentType(MediaType.APPLICATION_JSON)
                .content(corpo("Nova", "Projeto", 2, 1))).andExpect(status().isCreated())
                .andExpect(jsonPath("$.etapas[1].nome").value("Nova"))
                .andExpect(jsonPath("$.etapas[2].id").value(finais.get(0).getId()))
                .andExpect(jsonPath("$.etapas[2].quantidadeDemandas").value(2))
                .andExpect(jsonPath("$.etapas[3].id").value(finais.get(1).getId()))
                .andExpect(jsonPath("$.etapas[3].quantidadeDemandas").value(1));
        var nova = etapas.findAll().stream().filter(e -> e.getNome().equals("Nova")).findFirst().orElseThrow();
        mvc.perform(put(base + "/" + trabalho.getId()).header("Authorization", bearer).contentType(MediaType.APPLICATION_JSON)
                .content(corpo("Concluídos", "Campo", 2, 2))).andExpect(status().isOk())
                .andExpect(jsonPath("$.etapas[1].id").value(trabalho.getId()));
        mvc.perform(delete(base + "/" + nova.getId()).header("Authorization", bearer).contentType(MediaType.APPLICATION_JSON)
                .content("{\"versao\":3}")).andExpect(status().isOk()).andExpect(jsonPath("$.etapas.length()").value(3));
        mvc.perform(delete(base + "/" + trabalho.getId()).header("Authorization", bearer).contentType(MediaType.APPLICATION_JSON)
                .content("{\"versao\":4}")).andExpect(status().isOk()).andExpect(jsonPath("$.etapas.length()").value(2));
        assertThat(jdbc.queryForList("select * from etapas where categoria <> 'TRABALHO' order by id")).isEqualTo(finaisAntes);
        assertThat(quadros.findById(quadro.getId()).orElseThrow().getVersao()).isEqualTo(5L);
        assertThat(etapas.findAll()).extracting(Etapa::getCategoria).containsExactly(CategoriaEtapa.CONCLUIDA, CategoriaEtapa.CANCELADA);
    }

    @ParameterizedTest
    @ValueSource(strings = {"criarPosicaoFinal", "editarPosicaoFinal", "criarSetorBranco", "editarSetorBranco"})
    void limitesEValidacaoDeSetorNaoContamFinaisENaoGravam(String caso) throws Exception {
        var criador = usuario("Criador"); String bearer = token(criador);
        var quadro = gerenciamentos.criarGerenciamento(bearer.substring(7), "Novo", "D", null);
        mvc.perform(post(base(quadro.id())).header("Authorization", bearer).contentType(MediaType.APPLICATION_JSON)
                .content(corpo("Uma", "Campo", 1, 0))).andExpect(status().isCreated());
        var trabalho = etapas.findAll().stream().filter(e -> e.getCategoria() == CategoriaEtapa.TRABALHO).findFirst().orElseThrow();
        var antes = estado(); boolean criar = caso.startsWith("criar"); boolean setor = caso.endsWith("Branco");
        var request = criar ? post(base(quadro.id())) : put(base(quadro.id()) + "/" + trabalho.getId());
        mvc.perform(request.header("Authorization", bearer).contentType(MediaType.APPLICATION_JSON)
                .content(corpo("Mudada", setor ? "  " : "Campo", setor ? 1 : criar ? 3 : 2, 1)))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.erro").value(setor ? "Informe um setor entre 1 e 255 caracteres." : "Escolha uma posição válida para a etapa."));
        assertThat(estado()).isEqualTo(antes);
    }

    @Test
    void consultasDeLegadoEStartupNormalNaoPreparamDados() throws Exception {
        var criador = usuario("Criador"); var quadro = quadro(criador); var trabalho = etapa(quadro, "Cancelados", 9);
        jdbc.update("update etapas set categoria=null where id=?", trabalho.getId());
        var antes = estado(); String bearer = token(criador);
        mvc.perform(get(base(quadro.getId())).header("Authorization", bearer)).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1)).andExpect(jsonPath("$[0].categoria").value("TRABALHO"));
        mvc.perform(get("/api/gerenciamentos/{id}/estrutura-etapas", quadro.getId()).header("Authorization", bearer))
                .andExpect(status().isOk()).andExpect(jsonPath("$.etapas.length()").value(1));
        assertThat(estado()).isEqualTo(antes);
    }
}
