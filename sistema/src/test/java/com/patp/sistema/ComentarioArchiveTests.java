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
class ComentarioArchiveTests extends ApiIntegrationSupport {
    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void arquivadoRecusaComentarioComumOuAdminEMantemConsulta(boolean administrador) throws Exception { // GER-20/21/22
        var criador = usuario("Criador");
        var quadro = quadro(criador);
        var demanda = demanda(etapa(quadro, "Inicial", 1));
        jdbc.update("insert into comentarios(texto,funcionario,data_hora,processo_id) values('Anterior','Pessoa',CURRENT_TIMESTAMP,?)", demanda.getId());
        quadro.setArquivado(true);
        quadros.saveAndFlush(quadro);
        var leitor = usuario("Leitor");
        if (administrador) {
            leitor.setPapel(PapelUsuario.ADMINISTRADOR);
            usuarios.saveAndFlush(leitor);
        }
        String token = token(leitor);
        var antes = conteudoPersistido();
        String rota = "/api/processos/" + demanda.getId() + "/comentarios";
        mvc.perform(post(rota).header("Authorization", token).contentType(MediaType.APPLICATION_JSON)
                .content("{\"texto\":\"Novo\",\"funcionario\":\"Leitor\"}"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.erro").value("Gerenciamento arquivado. Restaure-o antes de alterar."));
        assertThat(conteudoPersistido()).isEqualTo(antes);
        mvc.perform(get(rota).header("Authorization", token)).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1)).andExpect(jsonPath("$[0].texto").value("Anterior"))
                .andExpect(jsonPath("$[0].funcionario").value("Pessoa")).andExpect(jsonPath("$[0].dataHora").isNotEmpty())
                .andExpect(jsonPath("$[0].processo.id").value(demanda.getId())).andExpect(jsonPath("$..senha").doesNotExist());
    }

    @Test
    void ativoPermiteComentariosDatadosEPersistidosComDadosExistentes() throws Exception { // GER-21 comportamento ativo
        var quadro = quadro(usuario("Criador"));
        var demanda = demanda(etapa(quadro, "Inicial", 1));
        String token = token(usuario("Outro"));
        String rota = "/api/processos/" + demanda.getId() + "/comentarios";
        for (String pessoa : new String[]{"Ana", "Bia"}) {
            mvc.perform(post(rota).header("Authorization", token).contentType(MediaType.APPLICATION_JSON)
                    .content("{\"texto\":\"Comentário de " + pessoa + "\",\"funcionario\":\"" + pessoa + "\"}"))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.texto").value("Comentário de " + pessoa))
                    .andExpect(jsonPath("$.funcionario").value(pessoa)).andExpect(jsonPath("$.dataHora").isNotEmpty())
                    .andExpect(jsonPath("$.processo.id").value(demanda.getId()));
        }
        assertThat(jdbc.queryForList("select texto from comentarios order by id", String.class))
                .containsExactly("Comentário de Ana", "Comentário de Bia");
        assertThat(jdbc.queryForList("select funcionario from comentarios order by id", String.class)).containsExactly("Ana", "Bia");
        assertThat(jdbc.queryForObject("select count(*) from comentarios where data_hora is not null and processo_id=?", Long.class, demanda.getId())).isEqualTo(2L);
        assertThat(processos.findById(demanda.getId()).orElseThrow().getStatus()).isEqualTo("Em andamento");
        assertThat(quadros.findById(quadro.getId()).orElseThrow().getVersao()).isZero();
    }
}
