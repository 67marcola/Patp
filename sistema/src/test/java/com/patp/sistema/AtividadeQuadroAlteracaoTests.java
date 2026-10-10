package com.patp.sistema;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import java.util.concurrent.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.support.TransactionTemplate;
import com.patp.sistema.model.Atividade;
import com.patp.sistema.model.PapelUsuario;
import com.patp.sistema.repository.AtividadeRepository;
import com.patp.sistema.service.GerenciamentoService;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest @AutoConfigureMockMvc
class AtividadeQuadroAlteracaoTests extends ApiIntegrationSupport {
    @Autowired GerenciamentoService service;
    @Autowired ObjectMapper json;
    @Autowired TransactionTemplate tx;
    @MockitoSpyBean AtividadeRepository atividades;

    @ParameterizedTest @ValueSource(booleans={false,true})
    void edicaoMostraAntesDepoisAutorRealEVersao(boolean admin) {
        var dono=usuario("Dono");var q=quadro(dono);var u=admin?usuario("Admin"):dono;
        if(admin){u.setPapel(PapelUsuario.ADMINISTRADOR);usuarios.saveAndFlush(u);}
        var r=service.editar(token(u).substring(7),q.getId(),"  Novo  ","  Literal  ",0L);
        assertThat(r.versao()).isEqualTo(1L);assertThat(atividades.count()).isEqualTo(1);
        var a=atividades.findAll().get(0);assertThat(a.getTipo()).isEqualTo("QUADRO_EDITADO");
        assertThat(a.getAutorId()).isEqualTo(u.getId());assertThat(a.getAutorNome()).isEqualTo(admin?"Admin":"Dono");
        assertThat(a.getAlvoNome()).isEqualTo("Novo");var c=json.readTree(a.getAlteracoesJson());
        assertThat(c.size()).isEqualTo(2);assertThat(c.get(0).get("campo").asText()).isEqualTo("nome");
        assertThat(c.get(0).get("anterior").asText()).isEqualTo("Quadro");assertThat(c.get(0).get("novo").asText()).isEqualTo("Novo");
        assertThat(c.get(1).get("campo").asText()).isEqualTo("descricao");assertThat(c.get(1).get("anterior").asText()).isEqualTo("Descrição");
        assertThat(c.get(1).get("novo").asText()).isEqualTo("  Literal  ");
    }
    @Test void edicaoIdenticaTemListaVaziaEIncrementoAtual() {
        var u=usuario("Dono");var q=quadro(u);var r=service.editar(token(u).substring(7),q.getId(),"Quadro","Descrição",0L);
        assertThat(r.versao()).isEqualTo(1L);assertThat(atividades.count()).isEqualTo(1);
        assertThat(atividades.findAll().get(0).getAlteracoesJson()).isEqualTo("[]");
    }
    @Test void arquivoRestauracaoERepeticaoConservamEventosEVersoes() {
        var u=usuario("Dono");var q=quadro(u);var t=token(u).substring(7);
        service.mudarEstado(t,q.getId(),0L,true);var antes=conteudoComAtividade();
        service.mudarEstado(t,q.getId(),0L,true);assertThat(conteudoComAtividade()).isEqualTo(antes);
        service.mudarEstado(t,q.getId(),1L,false);assertThat(quadros.findById(q.getId()).orElseThrow().getVersao()).isEqualTo(2L);
        var eventos=atividades.findAll();assertThat(eventos).extracting(Atividade::getTipo).containsExactly("QUADRO_ARQUIVADO","QUADRO_RESTAURADO");
        var a=json.readTree(eventos.get(0).getAlteracoesJson());var b=json.readTree(eventos.get(1).getAlteracoesJson());
        assertThat(a.size()).isEqualTo(1);assertThat(a.get(0).get("campo").asText()).isEqualTo("arquivado");
        assertThat(a.get(0).get("anterior").asText()).isEqualTo("false");assertThat(a.get(0).get("novo").asText()).isEqualTo("true");
        assertThat(b.size()).isEqualTo(1);assertThat(b.get(0).get("anterior").asText()).isEqualTo("true");assertThat(b.get(0).get("novo").asText()).isEqualTo("false");
    }
    @Test void marcadorLegadoNullPreservaAnteriorLiteral() {
        var u=usuario("Dono");var q=quadro(u);jdbc.update("update gerenciamentos set arquivado=null where id=?",q.getId());
        service.mudarEstado(token(u).substring(7),q.getId(),0L,true);var c=json.readTree(atividades.findAll().get(0).getAlteracoesJson());
        assertThat(c.get(0).get("anterior").isNull()).isTrue();assertThat(c.get(0).get("novo").asText()).isEqualTo("true");
    }
    @ParameterizedTest @ValueSource(strings={"permissao","versao","arquivo","nome","sessao"})
    void recusasNaoAlteramDadosOuAtividade(String caso) {
        var u=usuario("Dono");var q=quadro(u);var autor=caso.equals("permissao")?usuario("Leitor"):u;
        if(caso.equals("arquivo")){q.setArquivado(true);quadros.saveAndFlush(q);}
        var antes=conteudoComAtividade();String mensagem=switch(caso) {
            case "permissao" -> "Você não tem permissão para administrar este gerenciamento.";
            case "versao" -> "Gerenciamento alterado por outro usuário. Atualize e tente novamente.";
            case "arquivo" -> "Gerenciamento arquivado. Restaure-o antes de alterar.";
            case "nome" -> "Informe um nome entre 1 e 120 caracteres.";
            default -> "Sessão inválida ou expirada.";
        };
        assertThatThrownBy(() -> service.editar(caso.equals("sessao")?"invalida":token(autor).substring(7),q.getId(),caso.equals("nome")?" ":"Novo","Desc",caso.equals("versao")?99L:q.getVersao())).hasMessage(mensagem);
        assertThat(conteudoComAtividade()).isEqualTo(antes);
    }
    @ParameterizedTest @ValueSource(booleans={false,true})
    void falhaDoDiarioReverteEdicaoOuArquivo(boolean arquivo) {
        var u=usuario("Dono");var q=quadro(u);var antes=conteudoComAtividade();
        doThrow(new IllegalStateException("Falha do diário")).when(atividades).saveAndFlush(any(Atividade.class));
        assertThatThrownBy(() -> {if(arquivo)service.mudarEstado(token(u).substring(7),q.getId(),0L,true);else service.editar(token(u).substring(7),q.getId(),"Novo","Desc",0L);}).hasMessage("Falha do diário");
        assertThat(conteudoComAtividade()).isEqualTo(antes);
    }
    @Test void antesDepoisUsaEdicaoCommitadaAntesDoLock() throws Exception {
        var u=usuario("Dono");var q=quadro(u);var t=token(u).substring(7);
        var escrito=new CountDownLatch(1);var liberar=new CountDownLatch(1);var iniciou=new CountDownLatch(1);var executor=Executors.newFixedThreadPool(2);
        try {
            var primeira=executor.submit(() -> tx.executeWithoutResult(s -> {service.editar(t,q.getId(),"Primeiro","Descrição",0L);escrito.countDown();aguardar(liberar);}));
            assertThat(escrito.await(10,TimeUnit.SECONDS)).isTrue();
            var segunda=executor.submit(() -> {iniciou.countDown();return service.editar(t,q.getId(),"Segundo","Descrição",1L);});
            assertThat(iniciou.await(10,TimeUnit.SECONDS)).isTrue();assertThatThrownBy(() -> segunda.get(200,TimeUnit.MILLISECONDS)).isInstanceOf(TimeoutException.class);
            liberar.countDown();primeira.get(10,TimeUnit.SECONDS);segunda.get(10,TimeUnit.SECONDS);
            var eventos=atividades.findAll();assertThat(eventos).hasSize(2);var c=json.readTree(eventos.get(1).getAlteracoesJson());
            assertThat(c.size()).isEqualTo(1);assertThat(c.get(0).get("anterior").asText()).isEqualTo("Primeiro");assertThat(c.get(0).get("novo").asText()).isEqualTo("Segundo");
            assertThat(quadros.findById(q.getId()).orElseThrow().getVersao()).isEqualTo(2L);
        } finally {liberar.countDown();executor.shutdownNow();executor.awaitTermination(10,TimeUnit.SECONDS);}
    }
    void aguardar(CountDownLatch l) {try{if(!l.await(10,TimeUnit.SECONDS))throw new IllegalStateException("Sem sincronização");}catch(InterruptedException e){Thread.currentThread().interrupt();throw new IllegalStateException(e);}}
}
