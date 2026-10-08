package com.patp.sistema;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;

@SpringBootTest @AutoConfigureMockMvc
class DemandaArquivoHistoricoApiTests extends ApiIntegrationSupport {
    @ParameterizedTest @ValueSource(strings={"MANUAL","ARQUIVAMENTO","RESTAURACAO"})
    void postArquivoNaoPermiteNomeForjadoEGetPreservaTodosCampos(String acao) throws Exception { // ARQ-17/22
        var u=usuario("Dono");var q=quadro(u);var p=demanda(etapa(q,"Origem",1));p.setArquivado(true);processos.saveAndFlush(p);
        jdbc.update("insert into historicos(acao,descricao,data_hora,usuario,processo_id) values('ANTIGO','  Literal\n  ','2001-02-03 04:05:06','Antigo',?)",p.getId());
        var antigo=jdbc.queryForMap("select * from historicos");var antes=conteudoPersistido();var quadroAntes=jdbc.queryForList("select * from gerenciamentos");String url="/api/processos/"+p.getId()+"/historico";String bearer=token(usuario("Leitor"));
        mvc.perform(post(url).header("Authorization",bearer).contentType(MediaType.APPLICATION_JSON).content("{\"acao\":\""+acao+"\",\"descricao\":\"Forjado\",\"usuario\":\"Forjado\"}")).andExpect(status().isConflict()).andExpect(jsonPath("$.erro").value("Demanda arquivada. Restaure-a antes de alterar."));
        mvc.perform(get(url).header("Authorization",bearer)).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1)).andExpect(jsonPath("$[0].id").value(antigo.get("id")))
            .andExpect(jsonPath("$[0].acao").value("ANTIGO")).andExpect(jsonPath("$[0].descricao").value("  Literal\n  ")).andExpect(jsonPath("$[0].usuario").value("Antigo")).andExpect(jsonPath("$[0].dataHora").value("2001-02-03T04:05:06"))
            .andExpect(jsonPath("$[0].processo.id").value(p.getId())).andExpect(jsonPath("$[0].processo.arquivado").value(true)).andExpect(jsonPath("$..senha").doesNotExist());
        assertThat(conteudoPersistido()).isEqualTo(antes);assertThat(jdbc.queryForList("select * from gerenciamentos")).isEqualTo(quadroAntes);
    }
    @Test void postAtivoPermaneceCompativelParaQualquerAutenticado() throws Exception { // ARQ-22 borda ativo
        var q=quadro(usuario("Dono"));var p=demanda(etapa(q,"Origem",1));var processoAntes=jdbc.queryForList("select * from processos");var inicio=LocalDateTime.now().minusSeconds(1);
        mvc.perform(post("/api/processos/"+p.getId()+"/historico").header("Authorization",token(usuario("Outro"))).contentType(MediaType.APPLICATION_JSON).content("{\"acao\":\"MANUAL\",\"descricao\":\"  Literal\\n  \",\"usuario\":\"Informado\"}")).andExpect(status().isOk())
            .andExpect(jsonPath("$.acao").value("MANUAL")).andExpect(jsonPath("$.descricao").value("  Literal\n  ")).andExpect(jsonPath("$.usuario").value("Informado")).andExpect(jsonPath("$.processo.id").value(p.getId()));
        assertThat(jdbc.queryForObject("select data_hora from historicos",LocalDateTime.class)).isBetween(inicio,LocalDateTime.now().plusSeconds(1));
        assertThat(jdbc.queryForMap("select * from historicos")).containsEntry("acao","MANUAL").containsEntry("descricao","  Literal\n  ").containsEntry("usuario","Informado").containsEntry("processo_id",p.getId());
        assertThat(jdbc.queryForList("select * from processos")).isEqualTo(processoAntes);assertThat(quadros.findById(q.getId()).orElseThrow().getVersao()).isZero();
    }
}
