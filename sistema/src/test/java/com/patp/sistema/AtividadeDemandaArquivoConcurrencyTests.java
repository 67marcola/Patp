package com.patp.sistema;

import static org.assertj.core.api.Assertions.*;
import java.util.List;
import java.util.concurrent.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest @AutoConfigureMockMvc
class AtividadeDemandaArquivoConcurrencyTests extends AtividadeDemandaSupport {
    @Autowired PlatformTransactionManager transactions;
    @ParameterizedTest @ValueSource(booleans={false,true})
    void arquivoDaDemandaEsperaMudancaDeArquivoDoQuadro(boolean restaurarQuadro)throws Exception {
        var u=usuario("Dono");var q=quadro(u);jdbc.update("update gerenciamentos set arquivado=? where id=?",restaurarQuadro,q.getId());var p=demanda(etapa(q,"Inicial",1));String bearer=token(u);var escrita=new CountDownLatch(1);var liberar=new CountDownLatch(1);var iniciada=new CountDownLatch(1);var executor=Executors.newFixedThreadPool(2);
        try {
            var primeira=executor.submit(()->new TransactionTemplate(transactions).executeWithoutResult(s->{jdbc.queryForList("select id from gerenciamentos where id=? for update",q.getId());jdbc.update("update gerenciamentos set arquivado=? where id=?",!restaurarQuadro,q.getId());escrita.countDown();aguardar(liberar);}));assertThat(escrita.await(10,TimeUnit.SECONDS)).isTrue();
            var segunda=executor.submit(()->{iniciada.countDown();try{service.arquivar(bearer.substring(7),q.getId(),p.getId(),0L);return "aceita";}catch(com.patp.sistema.exception.ApiException e){return e.getMessage();}});assertThat(iniciada.await(10,TimeUnit.SECONDS)).isTrue();assertThatThrownBy(()->segunda.get(200,TimeUnit.MILLISECONDS)).isInstanceOf(TimeoutException.class);liberar.countDown();primeira.get(10,TimeUnit.SECONDS);
            assertThat(segunda.get(10,TimeUnit.SECONDS)).isEqualTo(restaurarQuadro?"aceita":"Gerenciamento arquivado. Restaure-o antes de alterar.");assertThat(processos.findById(p.getId()).orElseThrow().isArquivado()).isEqualTo(restaurarQuadro);assertThat(quadros.findById(q.getId()).orElseThrow().isArquivado()).isEqualTo(!restaurarQuadro);assertThat(quadros.findById(q.getId()).orElseThrow().getVersao()).isEqualTo(restaurarQuadro?1L:0L);
            if(restaurarQuadro)evento("DEMANDA_ARQUIVADA",p,u,"ARQUIVAMENTO",List.of(campo(p,"arquivado","false","true")));else {assertThat(atividades.count()).isZero();assertThat(jdbc.queryForObject("select count(*) from historicos",Long.class)).isZero();}
        }finally{liberar.countDown();executor.shutdownNow();executor.awaitTermination(10,TimeUnit.SECONDS);}
    }
    private static void aguardar(CountDownLatch l){try{if(!l.await(10,TimeUnit.SECONDS))throw new IllegalStateException("Sincronização excedida");}catch(InterruptedException e){Thread.currentThread().interrupt();throw new IllegalStateException(e);}}
}
