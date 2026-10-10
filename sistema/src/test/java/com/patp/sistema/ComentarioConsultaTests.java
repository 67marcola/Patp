package com.patp.sistema;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import com.patp.sistema.model.PapelUsuario;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest @AutoConfigureMockMvc
class ComentarioConsultaTests extends ApiIntegrationSupport {
    @Autowired ObjectMapper json;
    String rota(long q,long p){return "/api/gerenciamentos/"+q+"/demandas/"+p+"/comentarios";}
    long inserir(long p,String texto,LocalDateTime hora){jdbc.update("insert into comentarios(texto,funcionario,data_hora,processo_id) values(?,?,?,?)",texto,"Autor antigo",hora,p);return jdbc.queryForObject("select max(id) from comentarios",Long.class);}
    @ParameterizedTest @CsvSource({"false,false","false,true","true,false","true,true"})
    void consultaFlagsERegistroAntigoSemEfeitos(boolean quadroArquivo,boolean demandaArquivo) throws Exception { // COM-11/15/16
        var q=quadro(usuario("Dono"));var p=demanda(etapa(q,"Origem",1));q.setArquivado(quadroArquivo);quadros.saveAndFlush(q);p.setArquivado(demandaArquivo);processos.saveAndFlush(p);
        String texto="  Antigo\r\n"+"x".repeat(10001);var hora=LocalDateTime.of(2000,2,29,12,34,56);long id=inserir(p.getId(),texto,hora);var antes=conteudoPersistido();var qs=jdbc.queryForList("select * from gerenciamentos");
        var resposta=mvc.perform(get(rota(q.getId(),p.getId())).header("Authorization",token(usuario("Leitor")))).andExpect(status().isOk())
            .andExpect(jsonPath("$.gerenciamentoId").value(q.getId())).andExpect(jsonPath("$.processoId").value(p.getId())).andExpect(jsonPath("$.quadroArquivado").value(quadroArquivo)).andExpect(jsonPath("$.demandaArquivada").value(demandaArquivo))
            .andExpect(jsonPath("$.comentarios.length()").value(1)).andExpect(jsonPath("$.comentarios[0].id").value(id)).andExpect(jsonPath("$.comentarios[0].texto").value(texto))
            .andExpect(jsonPath("$.comentarios[0].funcionario").value("Autor antigo")).andExpect(jsonPath("$.comentarios[0].dataHora").value(hora.toString())).andExpect(jsonPath("$.comentarios[0].processoId").value(p.getId()))
            .andExpect(jsonPath("$..senha").doesNotExist()).andExpect(jsonPath("$..token").doesNotExist()).andExpect(jsonPath("$..processo").doesNotExist()).andReturn();
        Map<?,?> envelope=json.readValue(resposta.getResponse().getContentAsString(),Map.class);
        assertThat(envelope.keySet().stream().map(Object::toString).toList()).containsExactlyInAnyOrderElementsOf(List.of("gerenciamentoId","processoId","quadroArquivado","demandaArquivada","comentarios"));
        assertThat(((Map<?,?>)((List<?>)envelope.get("comentarios")).get(0)).keySet().stream().map(Object::toString).toList()).containsExactlyInAnyOrderElementsOf(List.of("id","texto","funcionario","dataHora","processoId"));
        assertThat(conteudoPersistido()).isEqualTo(antes);assertThat(jdbc.queryForList("select * from gerenciamentos")).isEqualTo(qs);
    }
    @Test void ordemDataHoraDepoisIdSemVazarOutraDemandaEmNenhumaRota() throws Exception { // COM-11/15/16
        var q=quadro(usuario("Dono"));var p=demanda(etapa(q,"Origem",1));var outro=demanda(etapa(q,"Outra",2));var hora=LocalDateTime.of(2000,2,29,12,34,56);
        long fim=inserir(p.getId(),"Último",hora.plusDays(1)),tie1=inserir(p.getId(),"Primeiro empate",hora),tie2=inserir(p.getId(),"Segundo empate",hora);inserir(outro.getId(),"Outra demanda",hora.minusDays(2));long inicio=inserir(p.getId(),"Primeiro",hora.minusDays(1));var antes=conteudoPersistido();String t=token(usuario("Leitor"));
        mvc.perform(get(rota(q.getId(),p.getId())).header("Authorization",t)).andExpect(status().isOk()).andExpect(jsonPath("$.comentarios.length()").value(4))
            .andExpect(jsonPath("$.comentarios[0].id").value(inicio)).andExpect(jsonPath("$.comentarios[1].id").value(tie1)).andExpect(jsonPath("$.comentarios[2].id").value(tie2)).andExpect(jsonPath("$.comentarios[3].id").value(fim))
            .andExpect(jsonPath("$.comentarios[0].texto").value("Primeiro")).andExpect(jsonPath("$.comentarios[1].texto").value("Primeiro empate"))
            .andExpect(jsonPath("$.comentarios[2].texto").value("Segundo empate")).andExpect(jsonPath("$.comentarios[3].texto").value("Último"));
        mvc.perform(get("/api/processos/"+p.getId()+"/comentarios").header("Authorization",t)).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(4))
            .andExpect(jsonPath("$[0].id").value(inicio)).andExpect(jsonPath("$[1].id").value(tie1)).andExpect(jsonPath("$[2].id").value(tie2)).andExpect(jsonPath("$[3].id").value(fim)).andExpect(jsonPath("$[0].processo.id").value(p.getId()));
        assertThat(conteudoPersistido()).isEqualTo(antes);
    }
    @Test void listaVaziaTemMetadadosExatos() throws Exception { // COM-15
        var q=quadro(usuario("Dono"));var p=demanda(etapa(q,"Origem",1));var antes=conteudoPersistido();
        mvc.perform(get(rota(q.getId(),p.getId())).header("Authorization",token(usuario("Leitor")))).andExpect(status().isOk())
            .andExpect(jsonPath("$.gerenciamentoId").value(q.getId())).andExpect(jsonPath("$.processoId").value(p.getId())).andExpect(jsonPath("$.quadroArquivado").value(false)).andExpect(jsonPath("$.demandaArquivada").value(false)).andExpect(jsonPath("$.comentarios").isEmpty());assertThat(conteudoPersistido()).isEqualTo(antes);
    }
    @ParameterizedTest @ValueSource(strings={"quadro","demanda","outro"})
    void alvoInexistenteOuExternoRecusaSemDados(String caso) throws Exception { // COM-06
        var u=usuario("Leitor");var q=quadro(u);var p=demanda(etapa(q,"Origem",1));var outro=quadro(u);inserir(p.getId(),"Privado desta demanda",LocalDateTime.now());var antes=conteudoPersistido();
        long quadroId=caso.equals("quadro")?999999L:caso.equals("outro")?outro.getId():q.getId(),pId=caso.equals("demanda")?999999L:p.getId();
        mvc.perform(get(rota(quadroId,pId)).header("Authorization",token(u))).andExpect(status().isNotFound()).andExpect(jsonPath("$.erro").value(caso.equals("quadro")?"Gerenciamento não encontrado.":"Demanda não encontrada.")).andExpect(jsonPath("$.comentarios").doesNotExist());assertThat(conteudoPersistido()).isEqualTo(antes);
    }
    @Test void getAntigoInexistenteDevolve404() throws Exception { // COM-06 contrato antigo
        var antes=conteudoPersistido();mvc.perform(get("/api/processos/999999/comentarios").header("Authorization",token(usuario("Leitor"))))
            .andExpect(status().isNotFound()).andExpect(jsonPath("$.erro").value("Demanda não encontrada."));assertThat(conteudoPersistido()).isEqualTo(antes);
    }
    @ParameterizedTest @ValueSource(booleans={false,true})
    void leituraNovaExigeSessao(boolean invalida) throws Exception { // COM-01
        var q=quadro(usuario("Dono"));var p=demanda(etapa(q,"Origem",1));var antes=conteudoPersistido();var r=get(rota(q.getId(),p.getId()));if(invalida)r.header("Authorization","Bearer invalido");
        mvc.perform(r).andExpect(status().isUnauthorized()).andExpect(jsonPath("$.erro").value(invalida?"Sessão inválida ou expirada.":"Usuário não autenticado."));assertThat(conteudoPersistido()).isEqualTo(antes);
    }
    @ParameterizedTest @ValueSource(strings={"comum","criador","admin"})
    void leituraParaTodosInclusiveQuadroSemCriador(String papel) throws Exception { // COM-01/15
        var dono=usuario("Dono");var u=papel.equals("criador")?dono:usuario("Leitor");if(papel.equals("admin")){u.setPapel(PapelUsuario.ADMINISTRADOR);usuarios.saveAndFlush(u);}var q=quadro(papel.equals("comum")?null:dono);var p=demanda(etapa(q,"Origem",1));
        mvc.perform(get(rota(q.getId(),p.getId())).header("Authorization",token(u))).andExpect(status().isOk()).andExpect(jsonPath("$.comentarios").isEmpty()).andExpect(jsonPath("$.gerenciamentoId").value(q.getId())).andExpect(jsonPath("$.processoId").value(p.getId()));
    }
}
