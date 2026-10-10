package com.patp.sistema;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import com.patp.sistema.model.PapelUsuario;
import com.patp.sistema.service.ComentarioService;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest @AutoConfigureMockMvc
class ComentarioDemandaApiTests extends ApiIntegrationSupport {
    @Autowired ObjectMapper json;
    @Autowired ComentarioService comentarios;
    String rota(long q,long p){return "/api/gerenciamentos/"+q+"/demandas/"+p+"/comentarios";}
    String corpo(String texto){var m=new LinkedHashMap<String,Object>();m.put("texto",texto);return json.writeValueAsString(m);}
    void erro(String rota,String t,String corpo,int status,String mensagem) throws Exception {
        var antes=conteudoPersistido();var qs=jdbc.queryForList("select * from gerenciamentos");var r=post(rota).contentType(MediaType.APPLICATION_JSON).content(corpo);if(t!=null)r.header("Authorization",t);
        mvc.perform(r).andExpect(status().is(status)).andExpect(jsonPath("$.erro").value(mensagem));
        assertThat(conteudoPersistido()).isEqualTo(antes);assertThat(jdbc.queryForList("select * from gerenciamentos")).isEqualTo(qs);
    }
    @ParameterizedTest @CsvSource({"criador,false","comum,false","admin,false","comum,true"})
    void confirmaCincoCamposSalvosEConservaDados(String papel,boolean semCriador) throws Exception { // COM-02/10/11/12/16
        var dono=usuario("Dono");var autor=papel.equals("criador")?dono:usuario("Participante");if(papel.equals("admin")){autor.setPapel(PapelUsuario.ADMINISTRADOR);usuarios.saveAndFlush(autor);}
        var q=quadro(semCriador?null:dono);var p=demanda(etapa(q,"Origem",1));var antiga=LocalDateTime.of(2000,2,29,1,2,3);
        jdbc.update("insert into comentarios(texto,funcionario,data_hora,processo_id) values(?,?,?,?)","Antigo"+"x".repeat(10001),"Anterior",antiga,p.getId());
        jdbc.update("insert into historicos(acao,descricao,usuario,data_hora,processo_id) values('ANTIGO','Literal','Anterior',?,?)",antiga,p.getId());var antes=conteudoPersistido();var qs=jdbc.queryForList("select * from gerenciamentos");var mensagens=jdbc.queryForList("select * from comentarios");
        String texto="  <b>Mensagem</b>\r\n fim  ";var inicio=LocalDateTime.now().minusSeconds(1);
        var resultado=mvc.perform(post(rota(q.getId(),p.getId())).header("Authorization",token(autor)).contentType(MediaType.APPLICATION_JSON).content(corpo(texto)))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.texto").value(texto)).andExpect(jsonPath("$.funcionario").value(autor.getNome())).andExpect(jsonPath("$.processoId").value(p.getId())).andReturn();
        var objeto=json.readTree(resultado.getResponse().getContentAsString());long id=objeto.get("id").asLong();var hora=LocalDateTime.parse(objeto.get("dataHora").asText());
        Map<?,?> campos=json.readValue(resultado.getResponse().getContentAsString(),Map.class);assertThat(campos.keySet().stream().map(Object::toString).toList()).containsExactlyInAnyOrderElementsOf(List.of("id","texto","funcionario","dataHora","processoId"));
        assertThat(id).isPositive();assertThat(hora).isBetween(inicio,LocalDateTime.now().plusSeconds(1));
        assertThat(jdbc.queryForMap("select texto,funcionario,processo_id from comentarios where id=?",id)).containsEntry("texto",texto).containsEntry("funcionario",autor.getNome()).containsEntry("processo_id",p.getId());assertThat(jdbc.queryForObject("select data_hora from comentarios where id=?",LocalDateTime.class,id)).isBetween(inicio,LocalDateTime.now().plusSeconds(1));
        assertThat(jdbc.queryForObject("select count(*) from comentarios",Long.class)).isEqualTo(2L);assertThat(jdbc.queryForList("select * from comentarios where id<>?",id)).isEqualTo(mensagens);
        var depois=conteudoPersistido();for(var tabela:List.of("processos","etapas","historicos"))assertThat(depois.get(tabela)).isEqualTo(antes.get(tabela));assertThat(jdbc.queryForList("select * from gerenciamentos")).isEqualTo(qs);
    }
    @ParameterizedTest @ValueSource(strings={"{","[]","null","{\"texto\":1}","{\"texto\":true}","{\"texto\":{}}","{\"texto\":[]}","{\"texto\":\"Mensagem\",\"funcionario\":\"Falso\"}","{\"texto\":\"Mensagem\",\"dataHora\":null}","{\"texto\":\"Mensagem\",\"processoId\":1}"})
    void corpoEstritoRecusaTipoEAutorLivre(String corpo) throws Exception { // COM-05/16
        var q=quadro(usuario("Dono"));var p=demanda(etapa(q,"Origem",1));erro(rota(q.getId(),p.getId()),token(usuario("Leitor")),corpo,400,"Dados da requisição inválidos.");
    }
    @ParameterizedTest @MethodSource("invalidos")
    void limiteNovoSemEfeitos(String texto) throws Exception { // COM-04
        var q=quadro(usuario("Dono"));var p=demanda(etapa(q,"Origem",1));erro(rota(q.getId(),p.getId()),token(usuario("Leitor")),corpo(texto),400,"Comentário deve conter texto e ter no máximo 10000 caracteres.");
    }
    static Stream<String> invalidos(){return Stream.of(null,"","\t\r\n","\u2003","x".repeat(10001));}
    @Test void textoAusenteRecusa() throws Exception { // COM-04
        var q=quadro(usuario("Dono"));var p=demanda(etapa(q,"Origem",1));erro(rota(q.getId(),p.getId()),token(usuario("Leitor")),"{}",400,"Comentário deve conter texto e ter no máximo 10000 caracteres.");
    }
    @Test void limiteExatoLiteral() throws Exception { // COM-04/10
        var q=quadro(usuario("Dono"));var p=demanda(etapa(q,"Origem",1));String texto="😀".repeat(5000);
        mvc.perform(post(rota(q.getId(),p.getId())).header("Authorization",token(usuario("Leitor"))).contentType(MediaType.APPLICATION_JSON).content(corpo(texto))).andExpect(status().isCreated()).andExpect(jsonPath("$.texto").value(texto));assertThat(jdbc.queryForList("select texto from comentarios",String.class)).containsExactly(texto);
    }
    @ParameterizedTest @ValueSource(strings={"sessao","token","quadro","demanda","outro","arquivoQuadro","arquivoDemanda"})
    void falhasHttpPreservamRegistros(String caso) throws Exception { // COM-01/06/07/08
        var dono=usuario("Dono");var q=quadro(dono);var p=demanda(etapa(q,"Origem",1));String t=token(usuario("Leitor"));long qId=q.getId(),pId=p.getId();int codigo=404;String msg="Demanda não encontrada.";
        switch(caso){case "sessao" -> {t=null;codigo=401;msg="Usuário não autenticado.";}case "token" -> {t="Bearer invalido";codigo=401;msg="Sessão inválida ou expirada.";}case "quadro" -> {qId=999999;msg="Gerenciamento não encontrado.";}case "demanda" -> pId=999999;case "outro" -> qId=quadro(dono).getId();case "arquivoQuadro" -> {q.setArquivado(true);quadros.saveAndFlush(q);codigo=409;msg="Gerenciamento arquivado. Restaure-o antes de alterar.";}default -> {p.setArquivado(true);processos.saveAndFlush(p);codigo=409;msg="Demanda arquivada. Restaure-a antes de alterar.";}}
        erro(rota(qId,pId),t,corpo("Mensagem"),codigo,msg);
    }
    @ParameterizedTest @ValueSource(strings={"Em andamento","Concluido","Cancelado","Antigo"})
    void comentarNaoMudaEstado(String estado) throws Exception { // COM-09/12
        var q=quadro(usuario("Dono"));var p=demanda(etapa(q,"Origem",1));p.setStatus(estado);processos.saveAndFlush(p);var antes=jdbc.queryForList("select * from processos");
        mvc.perform(post(rota(q.getId(),p.getId())).header("Authorization",token(usuario("Leitor"))).contentType(MediaType.APPLICATION_JSON).content(corpo("Mensagem"))).andExpect(status().isCreated());assertThat(jdbc.queryForList("select * from processos")).isEqualTo(antes);assertThat(jdbc.queryForList("select texto from comentarios",String.class)).containsExactly("Mensagem");
    }
    @Test void rollbackNoEnvioNovo() { // COM-13
        var q=quadro(usuario("Dono"));var p=demanda(etapa(q,"Origem",1));String t=token(usuario("Leitor")).substring(7);var antes=conteudoPersistido();var qs=jdbc.queryForList("select * from gerenciamentos");jdbc.execute("alter table comentarios add constraint comentario_api_falha check (texto <> 'Falha')");
        try{assertThatThrownBy(()->comentarios.adicionarNoQuadro(t,q.getId(),p.getId(),"Falha")).isInstanceOf(RuntimeException.class);}finally{jdbc.execute("alter table comentarios drop constraint comentario_api_falha");}assertThat(conteudoPersistido()).isEqualTo(antes);assertThat(jdbc.queryForList("select * from gerenciamentos")).isEqualTo(qs);
    }
}
