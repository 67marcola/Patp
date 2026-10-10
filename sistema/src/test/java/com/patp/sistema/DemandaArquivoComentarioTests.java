package com.patp.sistema;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import java.time.LocalDateTime;
import java.util.concurrent.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.transaction.*;
import org.springframework.transaction.support.TransactionTemplate;
import com.patp.sistema.dto.DemandaResponse;
import com.patp.sistema.exception.ApiException;
import com.patp.sistema.service.*;

@SpringBootTest @AutoConfigureMockMvc
class DemandaArquivoComentarioTests extends ApiIntegrationSupport {
    @Autowired ComentarioService comentarios;
    @Autowired ProcessoService service;
    @Autowired GerenciamentoGuard guard;
    @Autowired PlatformTransactionManager manager;
    TransactionTemplate tx(){var t=new TransactionTemplate(manager);t.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);return t;}
    void aguardar(CountDownLatch l){try{if(!l.await(10,TimeUnit.SECONDS))throw new IllegalStateException("Sem sincronização");}catch(InterruptedException e){Thread.currentThread().interrupt();throw new IllegalStateException(e);}}
    @Test void ativoAceitaComentarioEConsultaPermaneceLiteralDepoisDeArquivar() throws Exception { // ARQ-17/22
        var u=usuario("Dono");var q=quadro(u);var p=demanda(etapa(q,"Origem",1));String url="/api/processos/"+p.getId()+"/comentarios";var inicio=LocalDateTime.now().minusSeconds(1);
        mvc.perform(post(url).header("Authorization",token(usuario("Leitor"))).contentType(MediaType.APPLICATION_JSON).content("{\"texto\":\"  Literal\\n antigo  \",\"funcionario\":\"Informado\"}")).andExpect(status().isOk()).andExpect(jsonPath("$.texto").value("  Literal\n antigo  ")).andExpect(jsonPath("$.funcionario").value("Leitor")).andExpect(jsonPath("$.processo.id").value(p.getId()));
        var antigo=jdbc.queryForList("select * from comentarios");assertThat(quadros.findById(q.getId()).orElseThrow().getVersao()).isZero();
        assertThat(jdbc.queryForObject("select data_hora from comentarios",LocalDateTime.class)).isBetween(inicio,LocalDateTime.now().plusSeconds(1));
        service.arquivar(token(u).substring(7),q.getId(),p.getId(),0L);
        mvc.perform(get(url).header("Authorization",token(usuario("Consulta")))).andExpect(status().isOk()).andExpect(jsonPath("$[0].texto").value("  Literal\n antigo  ")).andExpect(jsonPath("$[0].funcionario").value("Leitor")).andExpect(jsonPath("$[0].id").value(antigo.get(0).get("id")));
        assertThat(jdbc.queryForList("select * from comentarios")).isEqualTo(antigo);
    }
    @Test void arquivoRecusaPostSemAlterarNada() throws Exception { // ARQ-22
        var u=usuario("Dono");var q=quadro(u);var p=demanda(etapa(q,"Origem",1));p.setArquivado(true);processos.saveAndFlush(p);var antes=conteudoPersistido();var quadroAntes=jdbc.queryForList("select * from gerenciamentos");
        mvc.perform(post("/api/processos/"+p.getId()+"/comentarios").header("Authorization",token(u)).contentType(MediaType.APPLICATION_JSON).content("{\"texto\":\"Novo\",\"funcionario\":\"Dono\"}")).andExpect(status().isConflict()).andExpect(jsonPath("$.erro").value("Demanda arquivada. Restaure-a antes de alterar."));
        assertThat(conteudoPersistido()).isEqualTo(antes);assertThat(jdbc.queryForList("select * from gerenciamentos")).isEqualTo(quadroAntes);
    }
    @ParameterizedTest @CsvSource({"true,false","true,true","false,false","false,true"})
    void disputaComentarioComArquivoOuRestauracaoNasDuasOrdens(boolean arquivar,boolean comentarioPrimeiro) throws Exception { // ARQ-16/17/22
        var u=usuario("Dono");var q=quadro(u);var p=demanda(etapa(q,"Origem",1));p.setArquivado(!arquivar);processos.saveAndFlush(p);String t=token(u).substring(7);var original=DemandaResponse.de(p);
        var escrito=new CountDownLatch(1);var liberar=new CountDownLatch(1);var iniciou=new CountDownLatch(1);var executor=Executors.newFixedThreadPool(2);var recusada=new java.util.concurrent.atomic.AtomicReference<ApiException>();var antes=conteudoPersistido();
        Runnable acao=()->{if(arquivar)service.arquivar(t,q.getId(),p.getId(),0L);else service.restaurar(t,q.getId(),p.getId(),0L);};
        try{
            var primeira=executor.submit(()->tx().executeWithoutResult(s->{if(comentarioPrimeiro){guard.bloquear(q.getId());try{comentarios.adicionar(t,p.getId(),"  Texto\n literal  ");}catch(ApiException x){recusada.set(x);s.setRollbackOnly();}}else acao.run();escrito.countDown();aguardar(liberar);}));assertThat(escrito.await(10,TimeUnit.SECONDS)).isTrue();
            var segunda=executor.submit(()->{iniciou.countDown();if(comentarioPrimeiro)acao.run();else comentarios.adicionar(t,p.getId(),"  Texto\n literal  ");});assertThat(iniciou.await(10,TimeUnit.SECONDS)).isTrue();assertThatThrownBy(()->segunda.get(200,TimeUnit.MILLISECONDS)).isInstanceOf(TimeoutException.class);
            assertThat(conteudoPersistido()).isEqualTo(antes);
            liberar.countDown();primeira.get(10,TimeUnit.SECONDS);
            if(arquivar&&!comentarioPrimeiro){var committed=conteudoPersistido();assertThatThrownBy(()->segunda.get(10,TimeUnit.SECONDS)).isInstanceOfSatisfying(ExecutionException.class,x->{assertThat(x.getCause()).isInstanceOfSatisfying(ApiException.class,a->{assertThat(a.getStatus().value()).isEqualTo(409);assertThat(a.getMessage()).isEqualTo("Demanda arquivada. Restaure-a antes de alterar.");});});assertThat(conteudoPersistido()).isEqualTo(committed);}else segunda.get(10,TimeUnit.SECONDS);
            if(!arquivar&&comentarioPrimeiro){assertThat(recusada.get()).isNotNull();assertThat(recusada.get().getStatus().value()).isEqualTo(409);assertThat(recusada.get().getMessage()).isEqualTo("Demanda arquivada. Restaure-a antes de alterar.");}
            var salvo=processos.findById(p.getId()).orElseThrow();assertThat(DemandaResponse.de(salvo)).isEqualTo(original);assertThat(salvo.isArquivado()).isEqualTo(arquivar);assertThat(quadros.findById(q.getId()).orElseThrow().getVersao()).isEqualTo(1L);
            boolean escritoComentario=arquivar?comentarioPrimeiro:!comentarioPrimeiro;
            assertThat(jdbc.queryForList("select texto from comentarios",String.class)).containsExactly(escritoComentario?new String[]{"  Texto\n literal  "}:new String[]{});
            assertThat(jdbc.queryForList("select funcionario from comentarios",String.class)).containsExactly(escritoComentario?new String[]{"Dono"}:new String[]{});
            assertThat(jdbc.queryForList("select processo_id from comentarios",Long.class)).containsExactly(escritoComentario?new Long[]{p.getId()}:new Long[]{});
            assertThat(jdbc.queryForList("select acao from historicos",String.class)).containsExactly(arquivar?"ARQUIVAMENTO":"RESTAURACAO");
        }finally{liberar.countDown();executor.shutdownNow();executor.awaitTermination(10,TimeUnit.SECONDS);}
    }
}
