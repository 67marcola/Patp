package com.patp.sistema;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import com.patp.sistema.model.CategoriaEtapa;
import com.patp.sistema.model.Etapa;
import com.patp.sistema.model.Gerenciamento;
import com.patp.sistema.model.Usuario;

@SpringBootTest
@AutoConfigureMockMvc
class EtapaCategoriaTests extends ApiIntegrationSupport {
    @Test
    void persisteCategoriasComoString() {
        assertThat(jdbc.queryForMap("select data_type, is_nullable, character_maximum_length from information_schema.columns "
                + "where table_name='ETAPAS' and column_name='CATEGORIA'"))
                .containsEntry("DATA_TYPE", "CHARACTER VARYING").containsEntry("IS_NULLABLE", "YES")
                .containsEntry("CHARACTER_MAXIMUM_LENGTH", 20L);
        Gerenciamento quadro = quadro(usuario("Criador"));
        for (CategoriaEtapa categoria : CategoriaEtapa.values()) {
            Etapa etapa = etapa(quadro, categoria.name(), categoria.ordinal() + 1);
            etapa.setCategoria(categoria);
            etapas.saveAndFlush(etapa);
            assertThat(jdbc.queryForObject("select categoria from etapas where id = ?", String.class, etapa.getId()))
                    .isEqualTo(categoria.name());
            assertThat(etapas.findById(etapa.getId()).orElseThrow().getCategoria()).isEqualTo(categoria);
        }
    }

    @Test
    void consultaHomonomoLegadoSemEscreverCategoria() throws Exception {
        Usuario criador = usuario("Criador");
        Gerenciamento quadro = quadro(criador);
        Etapa etapa = etapa(quadro, "Concluídos", 7);
        demanda(etapa);
        jdbc.update("update etapas set categoria = null where id = ?", etapa.getId());
        var antes = conteudoPersistido();
        mvc.perform(get("/api/gerenciamentos/{id}/estrutura-etapas", quadro.getId()).header("Authorization", token(criador)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.etapas[0].id").value(etapa.getId()))
                .andExpect(jsonPath("$.etapas[0].nome").value("Concluídos"))
                .andExpect(jsonPath("$.etapas[0].setor").value("Engenharia"))
                .andExpect(jsonPath("$.etapas[0].ordem").value(7))
                .andExpect(jsonPath("$.etapas[0].categoria").value("TRABALHO"))
                .andExpect(jsonPath("$.etapas[0].quantidadeDemandas").value(1));
        assertThat(conteudoPersistido()).isEqualTo(antes);
        assertThat(jdbc.queryForObject("select categoria from etapas where id = ?", String.class, etapa.getId())).isNull();
    }
}
