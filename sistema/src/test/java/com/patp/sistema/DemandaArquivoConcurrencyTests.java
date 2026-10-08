package com.patp.sistema;

import static org.assertj.core.api.Assertions.*;
import java.util.concurrent.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.transaction.*;
import org.springframework.transaction.support.TransactionTemplate;
import com.patp.sistema.dto.EditarDemandaRequest;
import com.patp.sistema.exception.ApiException;
import com.patp.sistema.service.*;

@SpringBootTest @AutoConfigureMockMvc
class DemandaArquivoConcurrencyTests extends ApiIntegrationSupport {
    @Autowired ProcessoService service;
    @Autowired GerenciamentoService quadrosService;
    @Autowired PlatformTransactionManager manager;
    @Autowired GerenciamentoGuard guard;
    TransactionTemplate tx(){var t=new TransactionTemplate(manager);t.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);return t;}
    void aguardar(CountDownLatch l){try{if(!l.await(10,TimeUnit.SECONDS))throw new IllegalStateException("Sem sincronização");}catch(InterruptedException e){Thread.currentThread().interrupt();throw new IllegalStateException(e);}}
    void conflito(Future<?> futuro,String mensagem) {
        assertThatThrownBy(()->futuro.get(10,TimeUnit.SECONDS)).isInstanceOfSatisfying(ExecutionException.class,x->{assertThat(x.getCause()).isInstanceOfSatisfying(ApiException.class,a->{assertThat(a.getStatus().value()).isEqualTo(409);assertThat(a.getMessage()).isEqualTo(mensagem);});});
    }
    @ParameterizedTest @ValueSource(booleans={false,true})
    void mesmaVersaoArquivaOuRestauraUmaVez(boolean arquivar) throws Exception { // ARQ-11/13/14/16
        var u=usuario("Dono");var q=quadro(u);var p=demanda(etapa(q,"Origem",1));p.setArquivado(!arquivar);processos.saveAndFlush(p);String t=token(u).substring(7);
        var escrito=new CountDownLatch(1);var liberar=new CountDownLatch(1);var iniciou=new CountDownLatch(1);var executor=Executors.newFixedThreadPool(2);
        try{
            Runnable acao=()->{if(arquivar)service.arquivar(t,q.getId(),p.getId(),0L);else service.restaurar(t,q.getId(),p.getId(),0L);};
            var primeira=executor.submit(()->tx().executeWithoutResult(s->{acao.run();escrito.countDown();aguardar(liberar);}));assertThat(escrito.await(10,TimeUnit.SECONDS)).isTrue();
            var segunda=executor.submit(()->{iniciou.countDown();acao.run();});assertThat(iniciou.await(10,TimeUnit.SECONDS)).isTrue();assertThatThrownBy(()->segunda.get(200,TimeUnit.MILLISECONDS)).isInstanceOf(TimeoutException.class);
            liberar.countDown();primeira.get(10,TimeUnit.SECONDS);var antes=conteudoPersistido();var quadroAntes=jdbc.queryForList("select * from gerenciamentos");
            conflito(segunda,"Gerenciamento alterado por outro usuário. Atualize e tente novamente.");
            assertThat(conteudoPersistido()).isEqualTo(antes);assertThat(jdbc.queryForList("select * from gerenciamentos")).isEqualTo(quadroAntes);
            assertThat(processos.findById(p.getId()).orElseThrow().isArquivado()).isEqualTo(arquivar);assertThat(quadros.findById(q.getId()).orElseThrow().getVersao()).isEqualTo(1L);
            assertThat(jdbc.queryForList("select acao from historicos",String.class)).containsExactly(arquivar?"ARQUIVAMENTO":"RESTAURACAO");
        }finally{liberar.countDown();executor.shutdownNow();executor.awaitTermination(10,TimeUnit.SECONDS);}
    }
    @ParameterizedTest @CsvSource({"edicao,false","edicao,true","movimento,false","movimento,true","quadro,false","quadro,true"})
    void disputaArquivoComEdicaoTransicaoOuQuadroNasDuasOrdens(String outra,boolean outraPrimeiro) throws Exception { // ARQ-05/16/22
        var u=usuario("Dono");var q=quadro(u);var e=etapa(q,"Origem",1);var destino=etapa(q,"Destino",2);var p=demanda(e);String t=token(u).substring(7);
        java.util.function.LongConsumer operacao=v->{switch(outra){case "edicao"->service.editarDados(t,q.getId(),p.getId(),new EditarDemandaRequest(v,"Corrigida",null,null,null,null,null,null));case "movimento"->service.transicionar(t,q.getId(),p.getId(),ProcessoService.Acao.MOVER,destino.getId(),null,v);default->quadrosService.mudarEstado(t,q.getId(),v,true);}};
        var escrito=new CountDownLatch(1);var liberar=new CountDownLatch(1);var iniciou=new CountDownLatch(1);var executor=Executors.newFixedThreadPool(2);
        try{
            var primeira=executor.submit(()->tx().executeWithoutResult(s->{if(outraPrimeiro)operacao.accept(0);else service.arquivar(t,q.getId(),p.getId(),0L);escrito.countDown();aguardar(liberar);}));assertThat(escrito.await(10,TimeUnit.SECONDS)).isTrue();
            var segunda=executor.submit(()->{iniciou.countDown();if(outraPrimeiro)service.arquivar(t,q.getId(),p.getId(),1L);else operacao.accept(1);});assertThat(iniciou.await(10,TimeUnit.SECONDS)).isTrue();assertThatThrownBy(()->segunda.get(200,TimeUnit.MILLISECONDS)).isInstanceOf(TimeoutException.class);
            liberar.countDown();primeira.get(10,TimeUnit.SECONDS);var fisicoPrimeiro=conteudoPersistido();var quadroPrimeiro=jdbc.queryForList("select * from gerenciamentos");
            boolean recusa=outra.equals("quadro")?outraPrimeiro:!outraPrimeiro;
            if(recusa){conflito(segunda,outra.equals("quadro")?"Gerenciamento arquivado. Restaure-o antes de alterar.":"Demanda arquivada. Restaure-a antes de alterar.");assertThat(conteudoPersistido()).isEqualTo(fisicoPrimeiro);assertThat(jdbc.queryForList("select * from gerenciamentos")).isEqualTo(quadroPrimeiro);}else segunda.get(10,TimeUnit.SECONDS);
            var salvo=processos.findById(p.getId()).orElseThrow();assertThat(salvo.isArquivado()).isEqualTo(!outra.equals("quadro")||!outraPrimeiro);assertThat(salvo.getStatus()).isEqualTo("Em andamento");assertThat(salvo.getNumeroProcesso()).isEqualTo(p.getNumeroProcesso());
            assertThat(salvo.getPessoa()).isEqualTo(outra.equals("edicao")&&outraPrimeiro?"Corrigida":"Pessoa");assertThat(salvo.getEtapa().getId()).isEqualTo(outra.equals("movimento")&&outraPrimeiro?destino.getId():e.getId());
            assertThat(quadros.findById(q.getId()).orElseThrow().getVersao()).isEqualTo(recusa?1L:2L);assertThat(quadros.findById(q.getId()).orElseThrow().isArquivado()).isEqualTo(outra.equals("quadro"));
            assertThat(jdbc.queryForList("select acao from historicos order by id",String.class)).containsExactly(outra.equals("quadro")?(outraPrimeiro?new String[]{}:new String[]{"ARQUIVAMENTO"}):outraPrimeiro?new String[]{outra.equals("edicao")?"EDICAO":"MUDANCA_ETAPA","ARQUIVAMENTO"}:new String[]{"ARQUIVAMENTO"});
        }finally{liberar.countDown();executor.shutdownNow();executor.awaitTermination(10,TimeUnit.SECONDS);}
    }
    @ParameterizedTest @CsvSource({"edicao,false","edicao,true","movimento,false","movimento,true","quadro,false","quadro,true"})
    void disputaRestauracaoNasDuasOrdensInclusiveEscritaRecusadaNoArquivo(String outra,boolean outraPrimeiro) throws Exception { // ARQ-06/16/22
        var u=usuario("Dono");var q=quadro(u);var e=etapa(q,"Origem",1);var destino=etapa(q,"Destino",2);var p=demanda(e);p.setArquivado(true);processos.saveAndFlush(p);String t=token(u).substring(7);
        java.util.function.LongConsumer operacao=v->{switch(outra){case "edicao"->service.editarDados(t,q.getId(),p.getId(),new EditarDemandaRequest(v,"Corrigida",null,null,null,null,null,null));case "movimento"->service.transicionar(t,q.getId(),p.getId(),ProcessoService.Acao.MOVER,destino.getId(),null,v);default->quadrosService.mudarEstado(t,q.getId(),v,true);}};
        var escrito=new CountDownLatch(1);var liberar=new CountDownLatch(1);var iniciou=new CountDownLatch(1);var executor=Executors.newFixedThreadPool(2);var recusada=new java.util.concurrent.atomic.AtomicReference<ApiException>();var antes=conteudoPersistido();
        try{
            var primeira=executor.submit(()->tx().executeWithoutResult(s->{
                if(outraPrimeiro){guard.bloquear(q.getId());try{operacao.accept(0);}catch(ApiException x){recusada.set(x);s.setRollbackOnly();}}else service.restaurar(t,q.getId(),p.getId(),0L);
                escrito.countDown();aguardar(liberar);
            }));assertThat(escrito.await(10,TimeUnit.SECONDS)).isTrue();
            var segunda=executor.submit(()->{iniciou.countDown();if(outraPrimeiro)service.restaurar(t,q.getId(),p.getId(),outra.equals("quadro")?1L:0L);else operacao.accept(1);});assertThat(iniciou.await(10,TimeUnit.SECONDS)).isTrue();assertThatThrownBy(()->segunda.get(200,TimeUnit.MILLISECONDS)).isInstanceOf(TimeoutException.class);
            liberar.countDown();primeira.get(10,TimeUnit.SECONDS);
            if(outraPrimeiro&&outra.equals("quadro")){conflito(segunda,"Gerenciamento arquivado. Restaure-o antes de alterar.");assertThat(conteudoPersistido()).isEqualTo(antes);}else segunda.get(10,TimeUnit.SECONDS);
            if(outraPrimeiro&&!outra.equals("quadro")){assertThat(recusada.get()).isNotNull();assertThat(recusada.get().getStatus().value()).isEqualTo(409);assertThat(recusada.get().getMessage()).isEqualTo("Demanda arquivada. Restaure-a antes de alterar.");}
            var salvo=processos.findById(p.getId()).orElseThrow();assertThat(salvo.isArquivado()).isEqualTo(outraPrimeiro&&outra.equals("quadro"));assertThat(salvo.getStatus()).isEqualTo("Em andamento");assertThat(salvo.getNumeroProcesso()).isEqualTo(p.getNumeroProcesso());
            assertThat(salvo.getPessoa()).isEqualTo(!outraPrimeiro&&outra.equals("edicao")?"Corrigida":"Pessoa");assertThat(salvo.getEtapa().getId()).isEqualTo(!outraPrimeiro&&outra.equals("movimento")?destino.getId():e.getId());
            assertThat(quadros.findById(q.getId()).orElseThrow().getVersao()).isEqualTo(outraPrimeiro?1L:2L);assertThat(quadros.findById(q.getId()).orElseThrow().isArquivado()).isEqualTo(outra.equals("quadro"));
            assertThat(jdbc.queryForList("select acao from historicos order by id",String.class)).containsExactly(outraPrimeiro?(outra.equals("quadro")?new String[]{}:new String[]{"RESTAURACAO"}):outra.equals("quadro")?new String[]{"RESTAURACAO"}:new String[]{"RESTAURACAO",outra.equals("edicao")?"EDICAO":"MUDANCA_ETAPA"});
        }finally{liberar.countDown();executor.shutdownNow();executor.awaitTermination(10,TimeUnit.SECONDS);}
    }
}
