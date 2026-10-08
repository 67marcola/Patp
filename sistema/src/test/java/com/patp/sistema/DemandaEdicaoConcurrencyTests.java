package com.patp.sistema;

import static org.assertj.core.api.Assertions.*;
import java.util.concurrent.*;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.Arguments;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import com.patp.sistema.dto.EditarDemandaRequest;
import com.patp.sistema.exception.ApiException;
import com.patp.sistema.service.ProcessoService;
import com.patp.sistema.service.GerenciamentoService;

@SpringBootTest @AutoConfigureMockMvc
class DemandaEdicaoConcurrencyTests extends ApiIntegrationSupport {
    @Autowired ProcessoService service;
    @Autowired GerenciamentoService quadrosService;
    @Autowired PlatformTransactionManager manager;
    EditarDemandaRequest dados(long v) {return new EditarDemandaRequest(v,"Corrigida",null,null,null,null,null,"Texto");}
    TransactionTemplate tx(){var t=new TransactionTemplate(manager);t.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);return t;}
    void aguardar(CountDownLatch l){try{if(!l.await(10,TimeUnit.SECONDS))throw new IllegalStateException("Sem sincronização");}catch(InterruptedException e){Thread.currentThread().interrupt();throw new IllegalStateException(e);}}
    @Test void mesmaVersaoEditaSomenteUmaVez() throws Exception { // EDI-14/15/16/18
        var dono=usuario("Dono");var q=quadro(dono);var e=etapa(q,"Origem",1);var p=demanda(e);String token=token(dono).substring(7);
        var escrito=new CountDownLatch(1);var liberar=new CountDownLatch(1);var iniciou=new CountDownLatch(1);var executor=Executors.newFixedThreadPool(2);
        try {
            var primeira=executor.submit(()->tx().executeWithoutResult(s->{service.editarDados(token,q.getId(),p.getId(),dados(0));escrito.countDown();aguardar(liberar);}));
            assertThat(escrito.await(10,TimeUnit.SECONDS)).isTrue();
            var segunda=executor.submit(()->{iniciou.countDown();return service.editarDados(token,q.getId(),p.getId(),dados(0));});
            assertThat(iniciou.await(10,TimeUnit.SECONDS)).isTrue();
            assertThatThrownBy(()->segunda.get(200,TimeUnit.MILLISECONDS)).isInstanceOf(TimeoutException.class);
            liberar.countDown();primeira.get(10,TimeUnit.SECONDS);var antes=conteudoPersistido();
            assertThatThrownBy(()->segunda.get(10,TimeUnit.SECONDS)).isInstanceOfSatisfying(ExecutionException.class,erro->{
                assertThat(erro.getCause()).isInstanceOfSatisfying(ApiException.class,a->{assertThat(a.getStatus().value()).isEqualTo(409);assertThat(a.getMessage()).isEqualTo("Gerenciamento alterado por outro usuário. Atualize e tente novamente.");});
            });
            assertThat(conteudoPersistido()).isEqualTo(antes);assertThat(quadros.findById(q.getId()).orElseThrow().getVersao()).isEqualTo(1L);
            assertThat(processos.findById(p.getId()).orElseThrow().getPessoa()).isEqualTo("Corrigida");
            assertThat(jdbc.queryForList("select acao from historicos",String.class)).containsExactly("EDICAO");
            assertThat(jdbc.queryForList("select usuario from historicos",String.class)).containsExactly("Dono");
        }finally{liberar.countDown();executor.shutdownNow();executor.awaitTermination(10,TimeUnit.SECONDS);}
    }
    @ParameterizedTest @MethodSource("ordens")
    void edicaoRespeitaCommitDeArquivoOuMovimento(String acao,boolean outraPrimeiro) throws Exception { // EDI-18/10
        var dono=usuario("Dono");var q=quadro(dono);var e=etapa(q,"Origem",1);var destino=etapa(q,"Destino",2);var p=demanda(e);String token=token(dono).substring(7);
        var escrito=new CountDownLatch(1);var liberar=new CountDownLatch(1);var iniciou=new CountDownLatch(1);var executor=Executors.newFixedThreadPool(2);
        java.util.function.LongConsumer outra=v->{if(acao.equals("arquivo"))quadrosService.mudarEstado(token,q.getId(),v,true);else service.transicionar(token,q.getId(),p.getId(),ProcessoService.Acao.MOVER,destino.getId(),null,v);};
        try{
            var primeira=executor.submit(()->tx().executeWithoutResult(s->{if(outraPrimeiro)outra.accept(0);else service.editarDados(token,q.getId(),p.getId(),dados(0));escrito.countDown();aguardar(liberar);}));
            assertThat(escrito.await(10,TimeUnit.SECONDS)).isTrue();
            var segunda=executor.submit(()->{iniciou.countDown();if(outraPrimeiro)service.editarDados(token,q.getId(),p.getId(),dados(1));else outra.accept(1);});
            assertThat(iniciou.await(10,TimeUnit.SECONDS)).isTrue();assertThatThrownBy(()->segunda.get(200,TimeUnit.MILLISECONDS)).isInstanceOf(TimeoutException.class);
            liberar.countDown();primeira.get(10,TimeUnit.SECONDS);boolean recusada=acao.equals("arquivo")&&outraPrimeiro;
            if(recusada){assertThatThrownBy(()->segunda.get(10,TimeUnit.SECONDS)).isInstanceOfSatisfying(ExecutionException.class,x->{assertThat(x.getCause()).isInstanceOfSatisfying(ApiException.class,a->{assertThat(a.getStatus().value()).isEqualTo(409);assertThat(a.getMessage()).isEqualTo("Gerenciamento arquivado. Restaure-o antes de alterar.");});});}
            else segunda.get(10,TimeUnit.SECONDS);
            var salvo=processos.findById(p.getId()).orElseThrow();assertThat(salvo.getPessoa()).isEqualTo(recusada?"Pessoa":"Corrigida");
            assertThat(salvo.getEtapa().getId()).isEqualTo(acao.equals("arquivo")?e.getId():destino.getId());assertThat(salvo.getStatus()).isEqualTo("Em andamento");assertThat(salvo.getNumeroProcesso()).isEqualTo(p.getNumeroProcesso());
            assertThat(quadros.findById(q.getId()).orElseThrow().getVersao()).isEqualTo(recusada?1L:2L);
            assertThat(quadros.findById(q.getId()).orElseThrow().isArquivado()).isEqualTo(acao.equals("arquivo"));
            assertThat(jdbc.queryForList("select acao from historicos order by id",String.class)).containsExactly(recusada?new String[]{}:acao.equals("arquivo")?new String[]{"EDICAO"}:outraPrimeiro?new String[]{"MUDANCA_ETAPA","EDICAO"}:new String[]{"EDICAO","MUDANCA_ETAPA"});
            assertThat(jdbc.queryForList("select usuario from historicos",String.class)).allMatch("Dono"::equals);
        }finally{liberar.countDown();executor.shutdownNow();executor.awaitTermination(10,TimeUnit.SECONDS);}
    }
    static Stream<Arguments> ordens(){return Stream.of("arquivo","movimento").flatMap(a->Stream.of(false,true).map(b->Arguments.of(a,b)));}
}
