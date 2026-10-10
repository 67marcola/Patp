package com.patp.sistema;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.concurrent.*;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.transaction.*;
import org.springframework.transaction.support.TransactionTemplate;
import com.patp.sistema.exception.ApiException;
import com.patp.sistema.model.PapelUsuario;
import com.patp.sistema.service.*;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest @AutoConfigureMockMvc
class ComentarioEnvioTests extends ApiIntegrationSupport {
    @Autowired ObjectMapper json;
    @Autowired ComentarioService comentarios;
    @Autowired GerenciamentoService gerenciamentos;
    @Autowired GerenciamentoGuard guard;
    @Autowired PlatformTransactionManager manager;
    String rota(long p){return "/api/processos/"+p+"/comentarios";}
    String corpo(String texto){var m=new LinkedHashMap<String,Object>();m.put("texto",texto);m.put("funcionario","Autor falso");return json.writeValueAsString(m);}
    @ParameterizedTest @CsvSource({"criador,false","comum,false","admin,false","comum,true","admin,true"})
    void participanteRealGravaLiteralEConservaTodosDados(String papel,boolean semCriador) throws Exception { // COM-02/03/10/11/12
        var dono=usuario("Dono");var q=quadro(semCriador?null:dono);var p=demanda(etapa(q,"Origem",1));var autor=papel.equals("criador")?dono:usuario("Participante");
        if(papel.equals("admin")){autor.setPapel(PapelUsuario.ADMINISTRADOR);usuarios.saveAndFlush(autor);}
        String antigo="  Antigo\n"+"x".repeat(10001);var horaAntiga=LocalDateTime.of(2000,2,29,1,2,3);
        jdbc.update("insert into comentarios(texto,funcionario,data_hora,processo_id) values(?,?,?,?)",antigo,"Nome antigo",horaAntiga,p.getId());
        jdbc.update("insert into historicos(acao,descricao,usuario,data_hora,processo_id) values('ANTIGO','Literal','Anterior',?,?)",horaAntiga,p.getId());
        var mensagens=jdbc.queryForList("select * from comentarios");var processosAntes=jdbc.queryForList("select * from processos");var quadrosAntes=jdbc.queryForList("select * from gerenciamentos");var etapasAntes=jdbc.queryForList("select * from etapas");var historicosAntes=jdbc.queryForList("select * from historicos");
        String texto="  Mensagem <b>literal</b>\r\n fim  ";var inicio=LocalDateTime.now().minusSeconds(1);
        var resposta=mvc.perform(post(rota(p.getId())).header("Authorization",token(autor)).contentType(MediaType.APPLICATION_JSON).content(corpo(texto)))
            .andExpect(status().isOk()).andExpect(jsonPath("$.id").isNumber()).andExpect(jsonPath("$.texto").value(texto)).andExpect(jsonPath("$.funcionario").value(autor.getNome()))
            .andExpect(jsonPath("$.dataHora").isNotEmpty()).andExpect(jsonPath("$.processo.id").value(p.getId())).andExpect(jsonPath("$..senha").doesNotExist()).andReturn();
        long id=json.readTree(resposta.getResponse().getContentAsString()).get("id").asLong();
        assertThat(id).isPositive();assertThat(jdbc.queryForMap("select texto,funcionario,processo_id from comentarios where id=?",id)).containsEntry("texto",texto).containsEntry("funcionario",autor.getNome()).containsEntry("processo_id",p.getId());
        var hora=jdbc.queryForObject("select data_hora from comentarios where id=?",LocalDateTime.class,id);assertThat(hora).isBetween(inicio,LocalDateTime.now().plusSeconds(1));
        assertThat(LocalDateTime.parse(json.readTree(resposta.getResponse().getContentAsString()).get("dataHora").asText())).isBetween(inicio,LocalDateTime.now().plusSeconds(1));
        assertThat(jdbc.queryForObject("select count(*) from comentarios",Long.class)).isEqualTo(2L);assertThat(jdbc.queryForList("select * from comentarios where id<>?",id)).isEqualTo(mensagens);
        assertThat(jdbc.queryForList("select * from processos")).isEqualTo(processosAntes);assertThat(jdbc.queryForList("select * from gerenciamentos")).isEqualTo(quadrosAntes);assertThat(jdbc.queryForList("select * from etapas")).isEqualTo(etapasAntes);assertThat(jdbc.queryForList("select * from historicos")).isEqualTo(historicosAntes);
    }
    @ParameterizedTest @MethodSource("invalidos")
    void textoInvalidoRecusaSemEfeitos(String texto) throws Exception { // COM-04
        var q=quadro(usuario("Dono"));var p=demanda(etapa(q,"Origem",1));var t=token(usuario("Leitor"));var antes=conteudoPersistido();var qs=jdbc.queryForList("select * from gerenciamentos");
        mvc.perform(post(rota(p.getId())).header("Authorization",t).contentType(MediaType.APPLICATION_JSON).content(corpo(texto))).andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.erro").value("Comentário deve conter texto e ter no máximo 10000 caracteres."));
        assertThat(conteudoPersistido()).isEqualTo(antes);assertThat(jdbc.queryForList("select * from gerenciamentos")).isEqualTo(qs);
    }
    static Stream<String> invalidos(){return Stream.of(null,""," ","\t\r\n","\u2003","x".repeat(10001),"😀".repeat(5001));}
    @Test void textoAusenteRecusaSemGravacao() throws Exception { // COM-04
        var p=demanda(etapa(quadro(usuario("Dono")),"Origem",1));var antes=conteudoPersistido();
        mvc.perform(post(rota(p.getId())).header("Authorization",token(usuario("Leitor"))).contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.erro").value("Comentário deve conter texto e ter no máximo 10000 caracteres."));assertThat(conteudoPersistido()).isEqualTo(antes);
    }
    @ParameterizedTest @ValueSource(strings={"a","😀"})
    void limiteExatoAceitoLiteralmente(String unidade) throws Exception { // COM-04/10
        String texto=unidade.repeat(10000/unidade.length());var p=demanda(etapa(quadro(usuario("Dono")),"Origem",1));
        mvc.perform(post(rota(p.getId())).header("Authorization",token(usuario("Leitor"))).contentType(MediaType.APPLICATION_JSON).content(corpo(texto)))
            .andExpect(status().isOk()).andExpect(jsonPath("$.texto").value(texto)).andExpect(jsonPath("$.funcionario").value("Leitor"));
        assertThat(jdbc.queryForList("select texto from comentarios",String.class)).containsExactly(texto);
    }
    @ParameterizedTest @ValueSource(strings={"Em andamento","Concluido","Cancelado","Antigo",""}) @NullSource
    void conversaNaoNormalizaEstado(String status) throws Exception { // COM-09/12
        var p=demanda(etapa(quadro(usuario("Dono")),"Origem",1));p.setStatus(status);processos.saveAndFlush(p);var antes=jdbc.queryForList("select * from processos");
        mvc.perform(post(rota(p.getId())).header("Authorization",token(usuario("Leitor"))).contentType(MediaType.APPLICATION_JSON).content(corpo("Mensagem"))).andExpect(status().isOk());
        assertThat(jdbc.queryForList("select * from processos")).isEqualTo(antes);assertThat(jdbc.queryForList("select texto from comentarios",String.class)).containsExactly("Mensagem");
    }
    @ParameterizedTest @CsvSource({"false,false","false,true","true,false","true,true"})
    void escritaArquivadaRecusaParaComumEAdmin(boolean quadroArquivado,boolean admin) throws Exception { // COM-07/08
        var q=quadro(usuario("Dono"));var p=demanda(etapa(q,"Origem",1));q.setArquivado(quadroArquivado);quadros.saveAndFlush(q);p.setArquivado(!quadroArquivado);processos.saveAndFlush(p);
        var u=usuario("Leitor");if(admin){u.setPapel(PapelUsuario.ADMINISTRADOR);usuarios.saveAndFlush(u);}var antes=conteudoPersistido();var qs=jdbc.queryForList("select * from gerenciamentos");
        mvc.perform(post(rota(p.getId())).header("Authorization",token(u)).contentType(MediaType.APPLICATION_JSON).content(corpo("Mensagem"))).andExpect(status().isConflict())
            .andExpect(jsonPath("$.erro").value(quadroArquivado?"Gerenciamento arquivado. Restaure-o antes de alterar.":"Demanda arquivada. Restaure-a antes de alterar."));
        assertThat(conteudoPersistido()).isEqualTo(antes);assertThat(jdbc.queryForList("select * from gerenciamentos")).isEqualTo(qs);
    }
    @ParameterizedTest @CsvSource({"false,false","false,true","true,false","true,true"})
    void consultaEEnvioSemSessaoRecusados(boolean leitura,boolean invalida) throws Exception { // COM-01
        var p=demanda(etapa(quadro(usuario("Dono")),"Origem",1));var antes=conteudoPersistido();var r=leitura?get(rota(p.getId())):post(rota(p.getId())).contentType(MediaType.APPLICATION_JSON).content(corpo("Mensagem"));if(invalida)r.header("Authorization","Bearer invalido");
        mvc.perform(r).andExpect(status().isUnauthorized()).andExpect(jsonPath("$.erro").value(invalida?"Sessão inválida ou expirada.":"Usuário não autenticado."));assertThat(conteudoPersistido()).isEqualTo(antes);
    }
    @Test void duasContasPersistemAutoresDistintos() throws Exception { // COM-02/03/10
        var p=demanda(etapa(quadro(usuario("Dono")),"Origem",1));
        for(String nome:new String[]{"Ana","Bia"})mvc.perform(post(rota(p.getId())).header("Authorization",token(usuario(nome))).contentType(MediaType.APPLICATION_JSON).content(corpo("Mensagem de "+nome)))
            .andExpect(status().isOk()).andExpect(jsonPath("$.funcionario").value(nome));
        assertThat(jdbc.queryForList("select funcionario from comentarios order by id",String.class)).containsExactly("Ana","Bia");assertThat(jdbc.queryForList("select texto from comentarios order by id",String.class)).containsExactly("Mensagem de Ana","Mensagem de Bia");
    }
    @Test void falhaFisicaReverteGravacao() { // COM-13
        var u=usuario("Leitor");var p=demanda(etapa(quadro(usuario("Dono")),"Origem",1));var antes=conteudoPersistido();var qs=jdbc.queryForList("select * from gerenciamentos");String t=token(u).substring(7);
        jdbc.execute("alter table comentarios add constraint comentario_falha check (texto <> 'Falha')");
        try{assertThatThrownBy(()->comentarios.adicionar(t,p.getId(),"Falha")).isInstanceOf(RuntimeException.class);}finally{jdbc.execute("alter table comentarios drop constraint comentario_falha");}
        assertThat(conteudoPersistido()).isEqualTo(antes);assertThat(jdbc.queryForList("select * from gerenciamentos")).isEqualTo(qs);
    }
    void aguardar(CountDownLatch l){try{if(!l.await(10,TimeUnit.SECONDS))throw new IllegalStateException("Sem sincronização");}catch(InterruptedException e){Thread.currentThread().interrupt();throw new IllegalStateException(e);}}
    @ParameterizedTest @CsvSource({"true,true","true,false","false,true","false,false"})
    void disputaComArquivoOuRestauracaoDeQuadro(boolean arquivar,boolean comentarioPrimeiro) throws Exception { // COM-07/10/12/14
        var u=usuario("Dono");var q=quadro(u);var p=demanda(etapa(q,"Origem",1));q.setArquivado(!arquivar);quadros.saveAndFlush(q);String t=token(u).substring(7);
        long versaoInicial=quadros.findById(q.getId()).orElseThrow().getVersao();var antes=jdbc.queryForList("select * from processos");var tx=new TransactionTemplate(manager);tx.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        var escrito=new CountDownLatch(1);var liberar=new CountDownLatch(1);var iniciou=new CountDownLatch(1);var executor=Executors.newFixedThreadPool(2);var recusada=new java.util.concurrent.atomic.AtomicReference<ApiException>();
        Runnable comentar=()->comentarios.adicionar(t,p.getId(),"Mensagem");Runnable mudar=()->gerenciamentos.mudarEstado(t,q.getId(),versaoInicial,arquivar);
        try{
            var primeira=executor.submit(()->tx.executeWithoutResult(s->{if(comentarioPrimeiro){guard.bloquear(q.getId());try{comentar.run();}catch(ApiException e){recusada.set(e);s.setRollbackOnly();}}else mudar.run();escrito.countDown();aguardar(liberar);}));assertThat(escrito.await(10,TimeUnit.SECONDS)).isTrue();
            var segunda=executor.submit(()->{iniciou.countDown();if(comentarioPrimeiro)mudar.run();else comentar.run();});assertThat(iniciou.await(10,TimeUnit.SECONDS)).isTrue();assertThatThrownBy(()->segunda.get(200,TimeUnit.MILLISECONDS)).isInstanceOf(TimeoutException.class);
            liberar.countDown();primeira.get(10,TimeUnit.SECONDS);
            if(arquivar&&!comentarioPrimeiro)assertThatThrownBy(()->segunda.get(10,TimeUnit.SECONDS)).isInstanceOfSatisfying(ExecutionException.class,e->assertThat(e.getCause()).isInstanceOfSatisfying(ApiException.class,a->{assertThat(a.getStatus().value()).isEqualTo(409);assertThat(a.getMessage()).isEqualTo("Gerenciamento arquivado. Restaure-o antes de alterar.");}));else segunda.get(10,TimeUnit.SECONDS);
            if(!arquivar&&comentarioPrimeiro){assertThat(recusada.get()).isNotNull();assertThat(recusada.get().getStatus().value()).isEqualTo(409);assertThat(recusada.get().getMessage()).isEqualTo("Gerenciamento arquivado. Restaure-o antes de alterar.");}
            boolean gravado=arquivar?comentarioPrimeiro:!comentarioPrimeiro;assertThat(jdbc.queryForList("select texto from comentarios",String.class)).containsExactly(gravado?new String[]{"Mensagem"}:new String[]{});assertThat(jdbc.queryForList("select funcionario from comentarios",String.class)).containsExactly(gravado?new String[]{"Dono"}:new String[]{});
            assertThat(jdbc.queryForList("select * from processos")).isEqualTo(antes);assertThat(jdbc.queryForList("select * from historicos")).isEmpty();assertThat(quadros.findById(q.getId()).orElseThrow().isArquivado()).isEqualTo(arquivar);assertThat(quadros.findById(q.getId()).orElseThrow().getVersao()).isEqualTo(versaoInicial+1);
        }finally{liberar.countDown();executor.shutdownNow();executor.awaitTermination(10,TimeUnit.SECONDS);}
    }
}
