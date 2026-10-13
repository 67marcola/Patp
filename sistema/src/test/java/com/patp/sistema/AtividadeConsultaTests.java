package com.patp.sistema;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import com.patp.sistema.model.Atividade;
import com.patp.sistema.model.PapelUsuario;
import com.patp.sistema.service.AtividadeService;
import com.patp.sistema.service.ComentarioService;
import com.patp.sistema.service.EtapaService;
import com.patp.sistema.dto.CriarDemandaRequest;
import com.patp.sistema.dto.RegistroAtividadeResponse;

@SpringBootTest @AutoConfigureMockMvc
class AtividadeConsultaTests extends AtividadeDemandaSupport {
    @Autowired AtividadeService consultas;
    @Autowired ComentarioService comentarios;
    @Autowired EtapaService etapasService;
    private static final LocalDateTime DATA=LocalDateTime.of(2001,2,3,4,5,6,123456000);
    static Stream<Arguments> leitores(){return Stream.of("criador","admin","leitor").flatMap(a->Stream.of(false,true).map(q->Arguments.of(a,q)));}
    @ParameterizedTest @MethodSource("leitores")
    void duasFontesContratoCompletoSemDuplicarHistoricoOuExporMensagem(String papel,boolean arquivo)throws Exception {
        var u=usuario("Dono");var q=quadro(u);var e=etapa(q,"Inicial",1);String t=token(u);
        service.criar(t.substring(7),q.getId(),new CriarDemandaRequest(0L,"N-1","Cliente",null,null,null,null,null,null));var p=processos.findAll().get(0);
        comentarios.adicionar(t.substring(7),p.getId(),"Mensagem privada secreta");var autos=atividades.findAll();var criacao=autos.get(0);var comentario=autos.get(1);Long h=legado(p.getId()," Ação antiga "," <script>literal</script>\n "," Autor antigo ",DATA.minusDays(1));
        jdbc.update("update atividades set data_hora=?",DATA);jdbc.update("update processos set arquivado=true,status='Concluido',numero_processo='Atual' where id=?",p.getId());jdbc.update("update gerenciamentos set arquivado=? where id=?",arquivo,q.getId());u.setNome("Posterior");usuarios.saveAndFlush(u);
        var leitor=papel.equals("criador")?u:usuario("Leitor");if(papel.equals("admin")){leitor.setPapel(PapelUsuario.ADMINISTRADOR);usuarios.saveAndFlush(leitor);}var antes=conteudoComAtividade();var resposta=pagina(q.getId(),token(leitor),null);
        var esperado=List.of(new RegistroAtividadeResponse("AUTOMATICA:"+comentario.getId(),"AUTOMATICA",comentario.getId(),q.getId(),"COMENTARIO_ADICIONADO","DEMANDA",p.getId(),"N-1",u.getId(),"Dono",DATA,null,List.of()),
            new RegistroAtividadeResponse("AUTOMATICA:"+criacao.getId(),"AUTOMATICA",criacao.getId(),q.getId(),"DEMANDA_CRIADA","DEMANDA",p.getId(),"N-1",u.getId(),"Dono",DATA,null,List.of(campo(p,"numeroProcesso",null,"N-1"),campo(p,"pessoa",null,"Cliente"),campo(p,"status",null,"Em andamento"),campo(p,"etapaId",null,e.getId().toString()),campo(p,"etapaNome",null,"Inicial"),campo(p,"arquivado",null,"false"))),
            new RegistroAtividadeResponse("HISTORICO_EXISTENTE:"+h,"HISTORICO_EXISTENTE",h,q.getId()," Ação antiga ","DEMANDA",p.getId(),"Atual",null," Autor antigo ",DATA.minusDays(1)," <script>literal</script>\n ",List.of()));
        assertThat(resposta.get("registros")).isEqualTo(json.readTree(json.writeValueAsString(esperado)));assertThat(resposta.get("gerenciamentoId").asLong()).isEqualTo(q.getId());assertThat(resposta.get("proximoCursor").isNull()).isTrue();assertThat(resposta.properties().stream().map(java.util.Map.Entry::getKey)).containsExactlyInAnyOrder("gerenciamentoId","registros","proximoCursor");
        for(var registro:resposta.get("registros"))assertThat(registro.properties().stream().map(java.util.Map.Entry::getKey)).containsExactlyInAnyOrder("chave","origem","id","gerenciamentoId","tipo","alvoTipo","alvoId","alvoNome","autorId","autorNome","dataHora","descricao","alteracoes");
        assertThat(resposta.toString()).doesNotContain("Mensagem privada secreta","senha","hash-ficticio-nao-publico","Bearer","processo","funcionario","historicoId");assertThat(conteudoComAtividade()).isEqualTo(antes);assertThat(jdbc.queryForObject("select count(*) from historicos",Long.class)).isEqualTo(2L);
        mvc.perform(get("/api/processos/"+p.getId()+"/historico").header("Authorization",t)).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2)).andExpect(jsonPath("$[0].acao").value(" Ação antiga ")).andExpect(jsonPath("$[1].acao").value("CRIACAO"));
    }
    @ParameterizedTest @ValueSource(booleans={false,true})
    void vazio200EmQuadroAtivoOuArquivado(boolean arquivo)throws Exception {
        var u=usuario("Dono");var q=quadro(u);jdbc.update("update gerenciamentos set arquivado=? where id=?",arquivo,q.getId());var antes=conteudoComAtividade();var r=pagina(q.getId(),token(usuario("Leitor")),null);assertThat(r.get("gerenciamentoId").asLong()).isEqualTo(q.getId());assertThat(r.get("registros").isArray()).isTrue();assertThat(r.get("registros").size()).isZero();assertThat(r.get("proximoCursor").isNull()).isTrue();assertThat(conteudoComAtividade()).isEqualTo(antes);
    }
    @ParameterizedTest @ValueSource(booleans={false,true})
    void ausenteOuInvalida401SemGravacao(boolean invalida)throws Exception {var q=quadro(usuario("Dono"));var antes=conteudoComAtividade();var req=get(rota(q.getId()));if(invalida)req.header("Authorization","Bearer invalida");mvc.perform(req).andExpect(status().isUnauthorized()).andExpect(jsonPath("$.erro").value(invalida?"Sessão inválida ou expirada.":"Usuário não autenticado."));assertThat(conteudoComAtividade()).isEqualTo(antes);}
    @Test void quadroAusente404SemGravacao()throws Exception {var u=usuario("Dono");var antes=conteudoComAtividade();mvc.perform(get(rota(999999L)).header("Authorization",token(u))).andExpect(status().isNotFound()).andExpect(jsonPath("$.erro").value("Gerenciamento não encontrado."));assertThat(conteudoComAtividade()).isEqualTo(antes);}
    static Stream<String> cursores(){return Stream.of("", "!", "a".repeat(513), codificar("Q?:0:0:0"),codificar("0:0:0:0"),codificar("Q:-1:0:0"),codificar("Q:0:9007199254740992:0"),codificar("Q:0:0:9007199254740992"),codificar("Q:0:0:1.2"),codificar("Q:0:0"),codificar("Q:0:0:0:0"),codificar("Q:00:0:0"));}
    @ParameterizedTest @MethodSource("cursores")
    void cursorMalformado400SemEscrita(String cursor)throws Exception {var u=usuario("Dono");var q=quadro(u);if(!cursor.isEmpty()&&cursor.matches("[A-Za-z0-9_-]+")&&cursor.length()<512){var dec=new String(Base64.getUrlDecoder().decode(cursor),java.nio.charset.StandardCharsets.UTF_8);if(dec.startsWith("Q:"))cursor=codificar(dec.replace("Q:",q.getId()+":"));}var antes=conteudoComAtividade();mvc.perform(get(rota(q.getId())).header("Authorization",token(u)).param("cursor",cursor)).andExpect(status().isBadRequest()).andExpect(jsonPath("$.erro").value("Cursor de atividade inválido."));assertThat(conteudoComAtividade()).isEqualTo(antes);}
    @Test void cursorDeOutroQuadro400EIsolamentoDeFontes()throws Exception {
        var u=usuario("Dono");var q=quadro(u);var outro=quadro(u);var p=demanda(etapa(outro,"Outra",1));legado(p.getId(),"OUTRO","Conteúdo outro","Autor",DATA);auto(outro,u,p,"OUTRO",DATA);var antes=conteudoComAtividade();var r=pagina(q.getId(),token(u),null);assertThat(r.get("registros").size()).isZero();
        mvc.perform(get(rota(q.getId())).header("Authorization",token(u)).param("cursor",codificar(outro.getId()+":0:0:0"))).andExpect(status().isBadRequest()).andExpect(jsonPath("$.erro").value("Cursor de atividade inválido."));assertThat(conteudoComAtividade()).isEqualTo(antes);
    }
    @Test void maisDe100FontesMistasEmpatadasPaginamSemRepetirMesmoComNovosInserts()throws Exception {
        var u=usuario("Dono");var q=quadro(u);var p=demanda(etapa(q,"Inicial",1));var expected=new ArrayList<String>();var automaticos=new ArrayList<Long>();var antigos=new ArrayList<Long>();
        for(int i=0;i<63;i++){automaticos.add(auto(q,u,p,"DEMANDA_EDITADA",DATA));antigos.add(legado(p.getId(),"Antigo"+i,"Literal"+i,null,DATA));}reversa(automaticos).forEach(id->expected.add("AUTOMATICA:"+id));reversa(antigos).forEach(id->expected.add("HISTORICO_EXISTENTE:"+id));String bearer=token(u);var before=conteudoComAtividade();var primeira=pagina(q.getId(),bearer,null);assertThat(chaves(primeira)).containsExactlyElementsOf(expected.subList(0,50));String cursor=primeira.get("proximoCursor").asText();assertThat(cursor.length()).isLessThanOrEqualTo(512);assertThat(cursor).matches("[A-Za-z0-9_-]+");assertThat(conteudoComAtividade()).isEqualTo(before);
        Long novoAuto=auto(q,u,p,"DEMANDA_EDITADA",DATA.plusDays(1));Long novoAntigo=legado(p.getId(),"Novo","Novo",null,DATA.plusDays(1));before=conteudoComAtividade();var segunda=pagina(q.getId(),bearer,cursor);assertThat(chaves(segunda)).containsExactlyElementsOf(expected.subList(50,100));assertThat(pagina(q.getId(),bearer,cursor)).isEqualTo(segunda);var terceira=pagina(q.getId(),bearer,segunda.get("proximoCursor").asText());assertThat(chaves(terceira)).containsExactlyElementsOf(expected.subList(100,126));assertThat(terceira.get("proximoCursor").isNull()).isTrue();var todas=new ArrayList<>(chaves(primeira));todas.addAll(chaves(segunda));todas.addAll(chaves(terceira));assertThat(todas).containsExactlyElementsOf(expected).doesNotHaveDuplicates();assertThat(chaves(pagina(q.getId(),bearer,null)).subList(0,2)).containsExactly("AUTOMATICA:"+novoAuto,"HISTORICO_EXISTENTE:"+novoAntigo);assertThat(conteudoComAtividade()).isEqualTo(before);
    }
    @Test void dataHoraNulaLegadaVaiAoFimSemInventarAutorOuData()throws Exception {
        var u=usuario("Dono");var q=quadro(u);var p=demanda(etapa(q,"Inicial",1));boolean mysql=jdbc.execute((org.springframework.jdbc.core.ConnectionCallback<Boolean>) c->"MySQL".equals(c.getMetaData().getDatabaseProductName()));
        jdbc.execute(mysql?"alter table historicos modify data_hora timestamp(6) null":"alter table historicos alter column data_hora drop not null");
        try {var ids=new ArrayList<Long>();for(int i=0;i<52;i++)ids.add(legado(p.getId()," Sem data "," Literal ",null,null));Long datado=legado(p.getId(),"Datado","Datado","Antigo",DATA);var antes=conteudoComAtividade();var primeira=pagina(q.getId(),token(u),null);var esperado=new ArrayList<String>();esperado.add("HISTORICO_EXISTENTE:"+datado);reversa(ids).forEach(id->esperado.add("HISTORICO_EXISTENTE:"+id));assertThat(chaves(primeira)).containsExactlyElementsOf(esperado.subList(0,50));var segunda=pagina(q.getId(),token(u),primeira.get("proximoCursor").asText());assertThat(chaves(segunda)).containsExactlyElementsOf(esperado.subList(50,53));for(var r:segunda.get("registros")){assertThat(r.get("dataHora").isNull()).isTrue();assertThat(r.get("autorId").isNull()).isTrue();assertThat(r.get("autorNome").isNull()).isTrue();assertThat(r.get("tipo").asText()).isEqualTo(" Sem data ");assertThat(r.get("descricao").asText()).isEqualTo(" Literal ");assertThat(r.get("alteracoes").size()).isZero();}assertThat(segunda.get("proximoCursor").isNull()).isTrue();assertThat(conteudoComAtividade()).isEqualTo(antes);
        }finally{jdbc.update("delete from historicos where data_hora is null");jdbc.execute(mysql?"alter table historicos modify data_hora timestamp(6) not null":"alter table historicos alter column data_hora set not null");}
    }
    @Test void etapaRemovidaContinuaNaConsultaComSnapshot()throws Exception {
        var u=usuario("Dono");var q=quadro(u);var e=etapa(q,"Removida",1);etapasService.excluir(token(u).substring(7),q.getId(),e.getId(),0L);var antes=conteudoComAtividade();var r=pagina(q.getId(),token(usuario("Leitor")),null).get("registros").get(0);assertThat(r.get("tipo").asText()).isEqualTo("ETAPA_REMOVIDA");assertThat(r.get("alvoId").asLong()).isEqualTo(e.getId());assertThat(r.get("alvoNome").asText()).isEqualTo("Removida");assertThat(r.get("alvoTipo").asText()).isEqualTo("ETAPA");assertThat(r.get("alteracoes").get(0).get("anterior").asText()).isEqualTo("Removida");assertThat(r.get("alteracoes").get(0).get("novo").isNull()).isTrue();assertThat(etapas.existsById(e.getId())).isFalse();assertThat(conteudoComAtividade()).isEqualTo(antes);
    }
    @Test void descricaoNulaLegadaPermaneceNulaSemInventarTexto()throws Exception {
        var u=usuario("Dono");var q=quadro(u);var p=demanda(etapa(q,"Inicial",1));boolean mysql=jdbc.execute((org.springframework.jdbc.core.ConnectionCallback<Boolean>) c->"MySQL".equals(c.getMetaData().getDatabaseProductName()));
        jdbc.execute(mysql?"alter table historicos modify descricao LONGTEXT null":"alter table historicos alter column descricao drop not null");
        try {Long h=legado(p.getId(),"  Estranha  ",null," Autor ",DATA);var antes=conteudoComAtividade();var r=pagina(q.getId(),token(usuario("Leitor")),null);var esperado=new RegistroAtividadeResponse("HISTORICO_EXISTENTE:"+h,"HISTORICO_EXISTENTE",h,q.getId(),"  Estranha  ","DEMANDA",p.getId(),p.getNumeroProcesso(),null," Autor ",DATA,null,List.of());assertThat(r.get("registros")).isEqualTo(json.readTree(json.writeValueAsString(List.of(esperado))));assertThat(r.get("registros").get(0).get("descricao").isNull()).isTrue();assertThat(r.get("proximoCursor").isNull()).isTrue();assertThat(conteudoComAtividade()).isEqualTo(antes);
        }finally{jdbc.update("delete from historicos where descricao is null");jdbc.execute(mysql?"alter table historicos modify descricao LONGTEXT not null":"alter table historicos alter column descricao set not null");}
    }
    private tools.jackson.databind.JsonNode pagina(Long q,String bearer,String cursor)throws Exception {var req=get(rota(q)).header("Authorization",bearer);if(cursor!=null)req.param("cursor",cursor);return json.readTree(mvc.perform(req).andExpect(status().isOk()).andReturn().getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8));}
    private List<String> chaves(tools.jackson.databind.JsonNode pagina){var list=new ArrayList<String>();pagina.get("registros").forEach(r->list.add(r.get("chave").asText()));return list;}
    private Long legado(Long p,String acao,String descricao,String autor,LocalDateTime data){jdbc.update("insert into historicos(acao,descricao,usuario,data_hora,processo_id) values(?,?,?,?,?)",acao,descricao,autor,data,p);return jdbc.queryForObject("select max(id) from historicos",Long.class);}
    private Long auto(com.patp.sistema.model.Gerenciamento q,com.patp.sistema.model.Usuario u,com.patp.sistema.model.Processo p,String tipo,LocalDateTime data){return atividades.saveAndFlush(new Atividade(q,tipo,"DEMANDA",p.getId(),p.getNumeroProcesso(),u.getId(),u.getNome(),data,"[]",null)).getId();}
    private <T> List<T> reversa(List<T> valores){var copia=new ArrayList<>(valores);java.util.Collections.reverse(copia);return copia;}
    private static String codificar(String texto){return Base64.getUrlEncoder().withoutPadding().encodeToString(texto.getBytes(java.nio.charset.StandardCharsets.UTF_8));}
    private String rota(Long q){return "/api/gerenciamentos/"+q+"/atividade";}
}
