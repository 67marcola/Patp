package com.patp.sistema;

import static org.assertj.core.api.Assertions.*;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import com.patp.sistema.service.ProcessoService.Acao;

@SpringBootTest @AutoConfigureMockMvc
class AtividadeDemandaTransicaoConcurrencyTests extends AtividadeDemandaSupport {
    @Autowired PlatformTransactionManager transactions;
    static Stream<Arguments> disputas(){return Stream.of(false,true).flatMap(l->Stream.of("quadroArquivo","demandaArquivo","demandaRestaurada").map(c->Arguments.of(l,c)));}
    @ParameterizedTest @MethodSource("disputas")
    void capturaDepoisDoLockNasDuasRotas(boolean legado,String caso)throws Exception {
        var u=usuario("Dono");var q=quadro(u);var a=etapa(q,"Anterior",1);var b=etapa(q,"Nova",2);var p=demanda(a);if(caso.equals("demandaRestaurada")){p.setArquivado(true);processos.saveAndFlush(p);}String bearer=token(u);
        var escrita=new CountDownLatch(1);var liberar=new CountDownLatch(1);var iniciada=new CountDownLatch(1);var executor=Executors.newFixedThreadPool(2);
        try {
            var primeira=executor.submit(()->new TransactionTemplate(transactions).executeWithoutResult(s->{jdbc.queryForList("select id from gerenciamentos where id=? for update",q.getId());if(caso.equals("quadroArquivo"))jdbc.update("update gerenciamentos set arquivado=true where id=?",q.getId());else jdbc.update("update processos set arquivado=? where id=?",!caso.equals("demandaRestaurada"),p.getId());escrita.countDown();aguardar(liberar);}));
            assertThat(escrita.await(10,TimeUnit.SECONDS)).isTrue();var segunda=executor.submit(()->{iniciada.countDown();try{if(legado)service.mudarEtapa(p.getId(),b.getId(),bearer.substring(7));else service.transicionar(bearer.substring(7),q.getId(),p.getId(),Acao.MOVER,b.getId(),null,0L);return "aceita";}catch(com.patp.sistema.exception.ApiException erro){return erro.getMessage();}});
            assertThat(iniciada.await(10,TimeUnit.SECONDS)).isTrue();assertThatThrownBy(()->segunda.get(200,TimeUnit.MILLISECONDS)).isInstanceOf(TimeoutException.class);liberar.countDown();primeira.get(10,TimeUnit.SECONDS);var antes=conteudoComAtividade();
            assertThat(segunda.get(10,TimeUnit.SECONDS)).isEqualTo(caso.equals("demandaRestaurada")?"aceita":caso.equals("quadroArquivo")?"Gerenciamento arquivado. Restaure-o antes de alterar.":"Demanda arquivada. Restaure-a antes de alterar.");
            if(caso.equals("demandaRestaurada")){evento("DEMANDA_MOVIDA",p,u,"MUDANCA_ETAPA",List.of(campo(p,"etapaId",a.getId().toString(),b.getId().toString()),campo(p,"etapaNome","Anterior","Nova")));assertThat(processos.findById(p.getId()).orElseThrow().getMarcadorArquivado()).isFalse();assertThat(processos.findById(p.getId()).orElseThrow().getEtapa().getId()).isEqualTo(b.getId());assertThat(quadros.findById(q.getId()).orElseThrow().getVersao()).isEqualTo(1L);}
            else {assertThat(conteudoComAtividade()).isEqualTo(antes);assertThat(atividades.count()).isZero();assertThat(processos.findById(p.getId()).orElseThrow().getEtapa().getId()).isEqualTo(a.getId());}
        }finally{liberar.countDown();executor.shutdownNow();executor.awaitTermination(10,TimeUnit.SECONDS);}
    }
    private static void aguardar(CountDownLatch l){try{if(!l.await(10,TimeUnit.SECONDS))throw new IllegalStateException("Sincronização excedida");}catch(InterruptedException e){Thread.currentThread().interrupt();throw new IllegalStateException(e);}}
}
