package com.patp.sistema;

import static org.assertj.core.api.Assertions.*;
import java.util.concurrent.*;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest @AutoConfigureMockMvc
class AtividadeComentarioConcurrencyTests extends AtividadeComentarioSupport {
    @Autowired PlatformTransactionManager transactions;
    static Stream<Arguments> casos(){return Stream.of(false,true).flatMap(l->Stream.of("quadroArquivo","quadroRestaurado","demandaArquivo","demandaRestaurada").map(c->Arguments.of(l,c)));}
    @ParameterizedTest @MethodSource("casos")
    void envioEsperaLockELêArquivoMaisRecente(boolean legado,String caso)throws Exception {
        var u=usuario("Dono");var q=quadro(u);var p=demanda(etapa(q,"Inicial",1));boolean restaurada=caso.endsWith("Restaurado")||caso.endsWith("Restaurada");boolean quadro=caso.startsWith("quadro");if(restaurada)jdbc.update(quadro?"update gerenciamentos set arquivado=true where id=?":"update processos set arquivado=true where id=?",quadro?q.getId():p.getId());String bearer=token(u);var escrita=new CountDownLatch(1);var liberar=new CountDownLatch(1);var iniciada=new CountDownLatch(1);var executor=Executors.newFixedThreadPool(2);
        try {
            var primeira=executor.submit(()->new TransactionTemplate(transactions).executeWithoutResult(s->{jdbc.queryForList("select id from gerenciamentos where id=? for update",q.getId());jdbc.update(quadro?"update gerenciamentos set arquivado=? where id=?":"update processos set arquivado=? where id=?",!restaurada,quadro?q.getId():p.getId());escrita.countDown();aguardar(liberar);}));assertThat(escrita.await(10,TimeUnit.SECONDS)).isTrue();
            var segunda=executor.submit(()->{iniciada.countDown();try{if(legado)service.adicionar(bearer.substring(7),p.getId(),"Privada");else service.adicionarNoQuadro(bearer.substring(7),q.getId(),p.getId(),"Privada");return "aceita";}catch(com.patp.sistema.exception.ApiException e){return e.getMessage();}});assertThat(iniciada.await(10,TimeUnit.SECONDS)).isTrue();assertThatThrownBy(()->segunda.get(200,TimeUnit.MILLISECONDS)).isInstanceOf(TimeoutException.class);liberar.countDown();primeira.get(10,TimeUnit.SECONDS);var antes=conteudoComAtividade();
            assertThat(segunda.get(10,TimeUnit.SECONDS)).isEqualTo(restaurada?"aceita":quadro?"Gerenciamento arquivado. Restaure-o antes de alterar.":"Demanda arquivada. Restaure-a antes de alterar.");if(restaurada)evento(p,u,"Privada");else {assertThat(conteudoComAtividade()).isEqualTo(antes);assertThat(atividades.count()).isZero();assertThat(jdbc.queryForObject("select count(*) from comentarios",Long.class)).isZero();}
        }finally{liberar.countDown();executor.shutdownNow();executor.awaitTermination(10,TimeUnit.SECONDS);}
    }
    private static void aguardar(CountDownLatch l){try{if(!l.await(10,TimeUnit.SECONDS))throw new IllegalStateException("Sincronização excedida");}catch(InterruptedException e){Thread.currentThread().interrupt();throw new IllegalStateException(e);}}
}
