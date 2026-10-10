package com.patp.sistema;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static com.patp.sistema.service.AtividadeRegistroService.*;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.transaction.support.TransactionTemplate;
import com.patp.sistema.model.Historico;
import com.patp.sistema.repository.AtividadeRepository;
import com.patp.sistema.repository.HistoricoRepository;
import com.patp.sistema.service.AtividadeRegistroService;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest @AutoConfigureMockMvc
class AtividadeRegistroTests extends ApiIntegrationSupport {
    @Autowired AtividadeRegistroService writer;
    @Autowired AtividadeRepository atividades;
    @Autowired HistoricoRepository historicos;
    @Autowired TransactionTemplate tx;
    @Autowired ObjectMapper json;

    @Test void persisteTodosOsSnapshotsComMicrosEAutorReal() {
        var u=usuario("Autor");var q=quadro(u);var e=etapa(q,"Etapa",3);
        var horario=LocalDateTime.of(2026,1,2,3,4,5,123456789);
        var a=tx.execute(s -> writer.registrar(q,u,Tipo.ETAPA_CRIADA,snapshotEtapa(e),diferencas(List.of(),List.of(snapshotEtapa(e))),null,horario));
        var salvo=atividades.findById(a.getId()).orElseThrow();
        assertThat(salvo.getGerenciamento().getId()).isEqualTo(q.getId());
        assertThat(salvo.getTipo()).isEqualTo("ETAPA_CRIADA");assertThat(salvo.getAlvoTipo()).isEqualTo("ETAPA");
        assertThat(salvo.getAlvoId()).isEqualTo(e.getId());assertThat(salvo.getAlvoNome()).isEqualTo("Etapa");
        assertThat(salvo.getAutorId()).isEqualTo(u.getId());assertThat(salvo.getAutorNome()).isEqualTo("Autor");
        assertThat(salvo.getDataHora()).isEqualTo(LocalDateTime.of(2026,1,2,3,4,5,123456000));
        assertThat(salvo.getHistoricoId()).isNull();
        assertThat(json.readTree(salvo.getAlteracoesJson()).get(2).get("novo").asText()).isEqualTo("3");
        assertThat(salvo.getAlteracoesJson()).doesNotContain("senha","hash-ficticio","email");
    }
    @Test void exigeTransacaoExistenteSemGravar() {
        var u=usuario("Autor");var q=quadro(u);
        assertThatThrownBy(() -> writer.registrar(q,u,Tipo.QUADRO_CRIADO,snapshotQuadro(q),List.of(),null,null))
                .isInstanceOf(org.springframework.transaction.IllegalTransactionStateException.class);
        assertThat(atividades.count()).isZero();
    }
    @Test void diferencasConservamNullEspacosEOrdemFixa() {
        var u=usuario("Autor");var q=quadro(u);
        var antes=new Alvo(AlvoTipo.QUADRO,q.getId(),"Quadro",valores("descricao",null,"nome","  antigo  "));
        var depois=new Alvo(AlvoTipo.QUADRO,q.getId(),"Novo",valores("descricao","  literal  ","nome","Novo"));
        var a=tx.execute(s -> writer.registrar(q,u,Tipo.QUADRO_EDITADO,depois,diferencas(List.of(antes),List.of(depois)),null,null));
        var changes=json.readTree(a.getAlteracoesJson());
        assertThat(changes.size()).isEqualTo(2);
        assertThat(changes.get(0).get("campo").asText()).isEqualTo("nome");
        assertThat(changes.get(0).get("anterior").asText()).isEqualTo("  antigo  ");
        assertThat(changes.get(0).get("novo").asText()).isEqualTo("Novo");
        assertThat(changes.get(1).get("anterior").isNull()).isTrue();
        assertThat(changes.get(1).get("novo").asText()).isEqualTo("  literal  ");
    }
    @Test void gravaAntesDepoisLiteralDeDezMilCaracteres() {
        var u=usuario("Autor");var q=quadro(u);String antes="á".repeat(10000),depois="ç".repeat(10000);
        var alvo=new Alvo(AlvoTipo.DEMANDA,91L,"P-91",valores());
        var a=tx.execute(s -> writer.registrar(q,u,Tipo.DEMANDA_EDITADA,alvo,
                List.of(new Alteracao(AlvoTipo.DEMANDA,91L,"P-91","observacoes",antes,depois)),null,null));
        var changes=json.readTree(atividades.findById(a.getId()).orElseThrow().getAlteracoesJson());
        assertThat(changes.get(0).get("anterior").asText()).isEqualTo(antes);
        assertThat(changes.get(0).get("novo").asText()).isEqualTo(depois);
    }
    @Test void edicaoIdenticaPersisteListaVazia() {
        var u=usuario("Autor");var q=quadro(u);
        var a=tx.execute(s -> writer.registrar(q,u,Tipo.QUADRO_EDITADO,snapshotQuadro(q),diferencas(List.of(snapshotQuadro(q)),List.of(snapshotQuadro(q))),null,null));
        assertThat(a.getAlteracoesJson()).isEqualTo("[]");assertThat(atividades.count()).isEqualTo(1);
    }
    @Test void remocaoEAlteracaoPosteriorNaoMudamIdentificacaoHistorica() {
        var u=usuario("Autor");var q=quadro(u);var e=etapa(q,"Antes",4);var alvo=snapshotEtapa(e);
        var a=tx.execute(s -> writer.registrar(q,u,Tipo.ETAPA_REMOVIDA,alvo,diferencas(List.of(alvo),List.of()),null,null));
        etapas.deleteById(e.getId());u.setNome("Depois");usuarios.saveAndFlush(u);
        var salvo=atividades.findById(a.getId()).orElseThrow();
        assertThat(salvo.getAlvoNome()).isEqualTo("Antes");assertThat(salvo.getAutorNome()).isEqualTo("Autor");
        assertThat(json.readTree(salvo.getAlteracoesJson()).get(2).get("anterior").asText()).isEqualTo("4");
        assertThat(json.readTree(salvo.getAlteracoesJson()).get(2).get("novo").isNull()).isTrue();
    }
    @Test void vinculoHistoricoUnicoReverteSegundaGravacao() {
        var u=usuario("Autor");var q=quadro(u);var p=demanda(etapa(q,"Etapa",1));var h=historico(p);
        var alvo=new Alvo(AlvoTipo.DEMANDA,p.getId(),p.getNumeroProcesso(),valores());
        tx.executeWithoutResult(s -> writer.registrar(q,u,Tipo.DEMANDA_CRIADA,alvo,List.of(),h.getId(),null));
        var antes=conteudoComAtividade();
        assertThatThrownBy(() -> tx.executeWithoutResult(s -> writer.registrar(q,u,Tipo.DEMANDA_CRIADA,alvo,List.of(),h.getId(),null)))
                .isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
        assertThat(conteudoComAtividade()).isEqualTo(antes);
        assertThat(atividades.findAll().get(0).getHistoricoId()).isEqualTo(h.getId());
    }
    @Test void recusaVinculoDeOutroQuadroSemMudancas() {
        var u=usuario("Autor");var q=quadro(u);var h=historico(demanda(etapa(quadro(u),"Outro",1)));
        var antes=conteudoComAtividade();
        assertThatThrownBy(() -> tx.executeWithoutResult(s -> writer.registrar(q,u,Tipo.QUADRO_CRIADO,snapshotQuadro(q),List.of(),h.getId(),null)))
                .isInstanceOf(IllegalArgumentException.class).hasMessage("Histórico pertence a outro gerenciamento.");
        assertThat(conteudoComAtividade()).isEqualTo(antes);
    }
    @Test void falhaDoEventoReverteAcaoEHistorico() {
        var u=usuario("Autor");var q=quadro(u);var p=demanda(etapa(q,"Etapa",1));var antes=conteudoComAtividade();
        assertThatThrownBy(() -> tx.executeWithoutResult(s -> {
            var atual=quadros.findById(q.getId()).orElseThrow();atual.setNome("Alterado");quadros.flush();
            var h=historico(p);
            writer.registrar(atual,u,Tipo.QUADRO_EDITADO,new Alvo(AlvoTipo.QUADRO,q.getId(),null,valores()),List.of(),h.getId(),null);
        })).isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
        assertThat(conteudoComAtividade()).isEqualTo(antes);
    }
    @Test void falhaPosteriorReverteEventoJaGravado() {
        var u=usuario("Autor");var q=quadro(u);var antes=conteudoComAtividade();
        assertThatThrownBy(() -> tx.executeWithoutResult(s -> {
            writer.registrar(q,u,Tipo.QUADRO_CRIADO,snapshotQuadro(q),List.of(),null,null);
            throw new IllegalStateException("Falha da ação");
        })).isInstanceOf(IllegalStateException.class).hasMessage("Falha da ação");
        assertThat(conteudoComAtividade()).isEqualTo(antes);
    }
    @Test void comentarioNaoPermiteMensagemNemAlteracoes() {
        var u=usuario("Autor");var q=quadro(u);var antes=conteudoComAtividade();
        assertThatThrownBy(() -> tx.executeWithoutResult(s -> writer.registrar(q,u,Tipo.COMENTARIO_ADICIONADO,snapshotQuadro(q),
            List.of(new Alteracao(AlvoTipo.DEMANDA,1L,"P","texto",null,"Mensagem")),null,null)))
            .isInstanceOf(IllegalArgumentException.class).hasMessage("Atividade de comentário não contém alterações informativas.");
        assertThat(conteudoComAtividade()).isEqualTo(antes);
    }
    @Test void criacaoOrdenaTodasAsPosicoesPorIdSemNullInventado() {
        var a=new Alvo(AlvoTipo.ETAPA,8L,"Oito",valores("nome","Oito","setor",null,"ordem",2));
        var b=new Alvo(AlvoTipo.ETAPA,3L,"Três",valores("nome","Três","setor","Setor","ordem",1));
        assertThat(diferencas(List.of(),List.of(a,b))).containsExactly(
            new Alteracao(AlvoTipo.ETAPA,3L,"Três","nome",null,"Três"),new Alteracao(AlvoTipo.ETAPA,3L,"Três","setor",null,"Setor"),
            new Alteracao(AlvoTipo.ETAPA,3L,"Três","ordem",null,"1"),new Alteracao(AlvoTipo.ETAPA,8L,"Oito","nome",null,"Oito"),
            new Alteracao(AlvoTipo.ETAPA,8L,"Oito","ordem",null,"2"));
    }
    @Test void criacaoPreservaQuadroEEtapaComMesmoId() {
        var q=new Alvo(AlvoTipo.QUADRO,1L,"Quadro",valores("nome","Quadro","descricao","Descrição","arquivado",false));
        var e=new Alvo(AlvoTipo.ETAPA,1L,"Etapa",valores("nome","Etapa","setor","Setor","ordem",1));
        assertThat(diferencas(List.of(),List.of(e,q))).containsExactly(
            new Alteracao(AlvoTipo.QUADRO,1L,"Quadro","nome",null,"Quadro"),
            new Alteracao(AlvoTipo.QUADRO,1L,"Quadro","descricao",null,"Descrição"),
            new Alteracao(AlvoTipo.QUADRO,1L,"Quadro","arquivado",null,"false"),
            new Alteracao(AlvoTipo.ETAPA,1L,"Etapa","nome",null,"Etapa"),
            new Alteracao(AlvoTipo.ETAPA,1L,"Etapa","setor",null,"Setor"),
            new Alteracao(AlvoTipo.ETAPA,1L,"Etapa","ordem",null,"1"));
    }
    private Historico historico(com.patp.sistema.model.Processo p) {
        var h=new Historico();h.setProcesso(p);h.setAcao("CRIACAO");h.setDescricao("Literal");h.setUsuario("Autor");h.setDataHora(LocalDateTime.now());
        return historicos.saveAndFlush(h);
    }
}
