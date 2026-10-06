package com.patp.sistema;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import com.patp.sistema.model.CategoriaEtapa;
import com.patp.sistema.model.Etapa;

@SpringBootTest
@AutoConfigureMockMvc
class GerenciamentoFinaisTests extends ApiIntegrationSupport {
    @Test
    void criaSemEComTrabalhosIncluindoHomonimosComParProprio() throws Exception {
        String token = token(usuario("Criador"));
        for (String iniciais : new String[]{"[]", "[{\"nome\":\"Concluídos\",\"setor\":\"Campo\",\"ordem\":3},{\"nome\":\"Cancelados\",\"setor\":\"Projeto\",\"ordem\":7}]"}) {
            mvc.perform(post("/api/gerenciamentos").header("Authorization", token).contentType(MediaType.APPLICATION_JSON)
                    .content("{\"nome\":\"Novo\",\"etapas\":" + iniciais + "}"))
                    .andExpect(status().isCreated()).andExpect(jsonPath("$.versao").value(0));
        }
        var quadrosSalvos = quadros.findAll();
        assertThat(quadrosSalvos).hasSize(2);
        var sem = etapas.findByGerenciamentoIdOrderByOrdemAscIdAsc(quadrosSalvos.get(0).getId());
        var com = etapas.findByGerenciamentoIdOrderByOrdemAscIdAsc(quadrosSalvos.get(1).getId());
        assertThat(sem).hasSize(2);
        assertThat(com).hasSize(4);
        assertThat(com.subList(0, 2)).extracting(Etapa::getNome).containsExactly("Concluídos", "Cancelados");
        assertThat(com.subList(0, 2)).extracting(Etapa::getSetor).containsExactly("Campo", "Projeto");
        assertThat(com.subList(0, 2)).extracting(Etapa::getOrdem).containsExactly(3, 7);
        assertThat(com.subList(0, 2)).extracting(Etapa::getCategoria).containsExactly(CategoriaEtapa.TRABALHO, CategoriaEtapa.TRABALHO);
        for (var par : List.of(sem, com.subList(2, 4))) {
            assertThat(par).extracting(Etapa::getNome).containsExactly("Concluídos", "Cancelados");
            assertThat(par).extracting(Etapa::getCategoria).containsExactly(CategoriaEtapa.CONCLUIDA, CategoriaEtapa.CANCELADA);
            assertThat(par).extracting(Etapa::getSetor).containsExactly(null, null);
            assertThat(par.get(0).getId()).isNotNull().isNotEqualTo(par.get(1).getId());
            assertThat(par).extracting(e -> e.getGerenciamento().getId()).containsExactly(par == sem ? quadrosSalvos.get(0).getId() : quadrosSalvos.get(1).getId(), par == sem ? quadrosSalvos.get(0).getId() : quadrosSalvos.get(1).getId());
        }
        assertThat(sem).extracting(Etapa::getId).doesNotContainAnyElementsOf(com.stream().map(Etapa::getId).toList());
        mvc.perform(get("/api/gerenciamentos/{id}/estrutura-etapas", quadrosSalvos.get(1).getId()).header("Authorization", token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.etapas.length()").value(4))
                .andExpect(jsonPath("$.etapas[2].categoria").value("CONCLUIDA"))
                .andExpect(jsonPath("$.etapas[3].categoria").value("CANCELADA"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"\"categoria\":\"CONCLUIDA\"", "\"categoria\":\"CANCELADA\"", "\"id\":123"})
    void identidadeOuCategoriaFinalInicialRecusadaSemEfeitos(String extra) throws Exception {
        var criador = usuario("Criador");
        var legado = etapa(quadro(criador), "Existente", 1);
        if (extra.startsWith("\"id\"")) extra = "\"id\":" + legado.getId();
        var antes = conteudoPersistido();
        var quadrosAntes = jdbc.queryForList("select * from gerenciamentos order by id");
        mvc.perform(post("/api/gerenciamentos").header("Authorization", token(criador)).contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Novo\",\"etapas\":[{\"nome\":\"Trabalho\",\"setor\":\"Campo\",\"ordem\":1},{\"nome\":\"Inválida\",\"setor\":\"Campo\",\"ordem\":2," + extra + "}]}"))
                .andExpect(status().isBadRequest());
        assertThat(conteudoPersistido()).isEqualTo(antes);
        assertThat(jdbc.queryForList("select * from gerenciamentos order by id")).isEqualTo(quadrosAntes);
    }

    @Test
    void falhaNaSegundaFinalReverteQuadroTrabalhoEPrimeiraFinal() throws Exception {
        jdbc.execute("alter table etapas add constraint falha_segunda_final check (categoria <> 'CANCELADA')");
        try {
            mvc.perform(post("/api/gerenciamentos").header("Authorization", token(usuario("Criador"))).contentType(MediaType.APPLICATION_JSON)
                    .content("{\"nome\":\"Novo\",\"etapas\":[{\"nome\":\"Trabalho\",\"setor\":\"Campo\",\"ordem\":1}]}"))
                    .andExpect(status().isInternalServerError()).andExpect(jsonPath("$.erro").value("Não foi possível concluir a operação."));
            assertThat(quadros.count()).isZero();
            assertThat(etapas.count()).isZero();
        } finally {
            jdbc.execute("alter table etapas drop constraint falha_segunda_final");
        }
    }
}
