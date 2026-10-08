package com.patp.sistema;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;

@SpringBootTest @AutoConfigureMockMvc
class DemandaArquivoLegadaTests extends ApiIntegrationSupport {
    @ParameterizedTest @ValueSource(strings={"editar","mover","concluir","cancelar","editarNova","moverNova","concluirNova","cancelarNova","reabrirNova"})
    void rotasDeEscritaRecusamArquivoSemEfeitos(String acao) throws Exception { // ARQ-22
        var u=usuario("Dono");var q=quadro(u);var e=etapa(q,"Origem",1);var destino=etapa(q,"Destino",2);var p=demanda(e);p.setArquivado(true);processos.saveAndFlush(p);var antes=conteudoPersistido();var quadroAntes=jdbc.queryForList("select * from gerenciamentos");
        String base="/api/processos/"+p.getId(),novo="/api/gerenciamentos/"+q.getId()+"/demandas/"+p.getId();
        String url=switch(acao){case "editar"->base;case "mover"->base+"/etapa/"+destino.getId();case "concluir"->base+"/concluir";case "cancelar"->base+"/cancelar";case "editarNova"->novo;default->novo+"/"+acao.replace("Nova","");};
        String corpo=acao.equals("editar")?"{\"pessoa\":\"Outra\",\"arquivado\":false}":acao.equals("editarNova")?"{\"versao\":0,\"pessoa\":\"Outra\"}":acao.startsWith("cancelar")?"{\"versao\":0,\"motivo\":\"Pedido\"}":acao.startsWith("mover")||acao.startsWith("reabrir")?"{\"versao\":0,\"etapaId\":"+destino.getId()+"}":"{\"versao\":0}";
        mvc.perform(put(url).header("Authorization",token(u)).contentType(MediaType.APPLICATION_JSON).content(corpo)).andExpect(status().isConflict()).andExpect(jsonPath("$.erro").value("Demanda arquivada. Restaure-a antes de alterar."));
        assertThat(conteudoPersistido()).isEqualTo(antes);assertThat(jdbc.queryForList("select * from gerenciamentos")).isEqualTo(quadroAntes);
    }
    @ParameterizedTest @ValueSource(booleans={false,true})
    void deleteAutorizadoRecusaAtivaEArquivoSemApagarOuEvento(boolean arquivo) throws Exception { // ARQ-23
        var u=usuario("Dono");var q=quadro(u);var p=demanda(etapa(q,"Origem",1));p.setArquivado(arquivo);processos.saveAndFlush(p);var antes=conteudoPersistido();var quadroAntes=jdbc.queryForList("select * from gerenciamentos");
        mvc.perform(delete("/api/processos/"+p.getId()).header("Authorization",token(u))).andExpect(status().isConflict()).andExpect(jsonPath("$.erro").value("Exclusão definitiva indisponível. Use o arquivamento da demanda."));
        assertThat(conteudoPersistido()).isEqualTo(antes);assertThat(jdbc.queryForList("select * from gerenciamentos")).isEqualTo(quadroAntes);
    }
    @Test void deleteTerceiroRecusa403SemEfeitos() throws Exception { // ARQ-02/23
        var q=quadro(usuario("Dono"));var p=demanda(etapa(q,"Origem",1));var antes=conteudoPersistido();
        mvc.perform(delete("/api/processos/"+p.getId()).header("Authorization",token(usuario("Outro")))).andExpect(status().isForbidden()).andExpect(jsonPath("$.erro").value("Você não tem permissão para administrar este gerenciamento."));
        assertThat(conteudoPersistido()).isEqualTo(antes);assertThat(quadros.findById(q.getId()).orElseThrow().getVersao()).isZero();
    }
    @Test void getsLegadosConsultamArquivoComMarcadorENovoPayloadNaoArquiva() throws Exception { // ARQ-17/18/19
        var u=usuario("Dono");var q=quadro(u);var e=etapa(q,"Origem",1);var p=demanda(e);p.setArquivado(true);processos.saveAndFlush(p);var antes=conteudoPersistido();
        mvc.perform(get("/api/processos/"+p.getId()).header("Authorization",token(u))).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(p.getId())).andExpect(jsonPath("$.arquivado").value(true)).andExpect(jsonPath("$.numeroProcesso").value(p.getNumeroProcesso()));
        for(String url:new String[]{"/api/processos","/api/processos?pesquisa="+p.getNumeroProcesso(),"/api/processos/etapa/"+e.getId()})mvc.perform(get(url).header("Authorization",token(u))).andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(p.getId())).andExpect(jsonPath("$[0].arquivado").value(true));
        assertThat(conteudoPersistido()).isEqualTo(antes);assertThat(quadros.findById(q.getId()).orElseThrow().getVersao()).isZero();
        mvc.perform(post("/api/processos").header("Authorization",token(u)).contentType(MediaType.APPLICATION_JSON).content("{\"numeroProcesso\":\"NOVO\",\"pessoa\":\"Cliente\",\"arquivado\":true,\"etapa\":{\"id\":"+e.getId()+"}}")).andExpect(status().isOk()).andExpect(jsonPath("$.arquivado").value(false));
        assertThat(processos.findAll().stream().filter(x->x.getNumeroProcesso().equals("NOVO")).findFirst().orElseThrow().isArquivado()).isFalse();
    }
}
