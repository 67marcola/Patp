package com.patp.sistema;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static com.patp.sistema.service.AtividadeRegistroService.AlvoTipo.ETAPA;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import com.patp.sistema.model.Atividade;
import com.patp.sistema.model.CategoriaEtapa;
import com.patp.sistema.service.AtividadeRegistroService.Alteracao;

@SpringBootTest @AutoConfigureMockMvc
class AtividadeEtapaEdicaoTests extends AtividadeEtapaSupport {
    @ParameterizedTest @ValueSource(strings={"nome","setor","ordem"})
    void registraSomenteCamposAlteradosETodasAsPosicoes(String campo) {
        var u=usuario("Dono");var q=quadro(u);var a=etapa(q,"A",1);var b=etapa(q,"B",2);var c=etapa(q,"C",3);
        var r=service.editar(token(u).substring(7),q.getId(),a.getId(),campo.equals("nome")?"  Novo  ":"A",campo.equals("setor")?"  Novo setor  ":"Engenharia",campo.equals("ordem")?3:1,0L);
        var alvo=etapas.findById(a.getId()).orElseThrow();
        List<Alteracao> esperado=switch(campo) {
            case "nome" -> List.of(new Alteracao(ETAPA,a.getId(),"Novo","nome","A","Novo"));
            case "setor" -> List.of(new Alteracao(ETAPA,a.getId(),"A","setor","Engenharia","Novo setor"));
            default -> List.of(new Alteracao(ETAPA,a.getId(),"A","ordem","1","3"),new Alteracao(ETAPA,b.getId(),"B","ordem","2","1"),new Alteracao(ETAPA,c.getId(),"C","ordem","3","2"));
        };
        evento("ETAPA_EDITADA",alvo,u,ordenar(esperado));assertThat(r.gerenciamento().versao()).isEqualTo(1L);
        assertThat(alvo.getNome()).isEqualTo(campo.equals("nome")?"Novo":"A");assertThat(alvo.getSetor()).isEqualTo(campo.equals("setor")?"Novo setor":"Engenharia");
        assertThat(alvo.getOrdem()).isEqualTo(campo.equals("ordem")?3:1);
        assertThat(etapas.findById(b.getId()).orElseThrow().getOrdem()).isEqualTo(campo.equals("ordem")?1:2);
        assertThat(etapas.findById(c.getId()).orElseThrow().getOrdem()).isEqualTo(campo.equals("ordem")?2:3);
    }
    @Test void edicaoIdenticaAceitaListaVaziaEIncremento() {
        var u=usuario("Dono");var q=quadro(u);var a=etapa(q,"A",1);
        var r=service.editar(token(u).substring(7),q.getId(),a.getId(),"A","Engenharia",1,0L);
        evento("ETAPA_EDITADA",a,u,List.of());assertThat(r.gerenciamento().versao()).isEqualTo(1L);
    }
    @Test void normalizaLacunasEEmpatesMesmoSemAlterarNomeSetor() {
        var u=usuario("Dono");var q=quadro(u);var a=etapa(q,"A",7);var b=etapa(q,"B",7);var c=etapa(q,"C",19);
        service.editar(token(u).substring(7),q.getId(),b.getId(),"B","Engenharia",2,0L);
        evento("ETAPA_EDITADA",etapas.findById(b.getId()).orElseThrow(),u,List.of(
            new Alteracao(ETAPA,a.getId(),"A","ordem","7","1"),new Alteracao(ETAPA,b.getId(),"B","ordem","7","2"),new Alteracao(ETAPA,c.getId(),"C","ordem","19","3")));
        assertThat(etapas.findById(a.getId()).orElseThrow().getOrdem()).isEqualTo(1);
        assertThat(etapas.findById(b.getId()).orElseThrow().getOrdem()).isEqualTo(2);
        assertThat(etapas.findById(c.getId()).orElseThrow().getOrdem()).isEqualTo(3);
    }
    @Test void renomeacaoPosteriorNaoMudaEventoAnteriorOuAutor() {
        var u=usuario("Dono");var q=quadro(u);var a=etapa(q,"A",1);var t=token(u).substring(7);
        service.editar(t,q.getId(),a.getId(),"Primeiro","Engenharia",1,0L);var anterior=atividades.findAll().get(0);String literal=anterior.getAlteracoesJson();
        service.editar(t,q.getId(),a.getId(),"Segundo","Outro",1,1L);u.setNome("Renomeado");usuarios.saveAndFlush(u);
        var preservado=atividades.findById(anterior.getId()).orElseThrow();assertThat(preservado.getAlvoNome()).isEqualTo("Primeiro");
        assertThat(preservado.getAutorNome()).isEqualTo("Dono");assertThat(preservado.getAlteracoesJson()).isEqualTo(literal);
        assertThat(json.readTree(literal).get(0).get("anterior").asText()).isEqualTo("A");assertThat(json.readTree(literal).get(0).get("novo").asText()).isEqualTo("Primeiro");
        assertThat(atividades.count()).isEqualTo(2);assertThat(quadros.findById(q.getId()).orElseThrow().getVersao()).isEqualTo(2L);
    }
    @ParameterizedTest @ValueSource(strings={"permissao","versao","arquivo","final","outro","ausente","nome","setor","posicao","sessao"})
    void recusasConservamTodaPersistencia(String caso) {
        var u=usuario("Dono");var q=quadro(u);var a=etapa(q,"A",9);var autor=caso.equals("permissao")?usuario("Leitor"):u;
        if(caso.equals("arquivo")){q.setArquivado(true);quadros.saveAndFlush(q);}
        if(caso.equals("final")){a.setCategoria(CategoriaEtapa.CONCLUIDA);etapas.saveAndFlush(a);}
        if(caso.equals("outro")){a.setGerenciamento(quadro(u));etapas.saveAndFlush(a);}
        var antes=conteudoComAtividade();String mensagem=switch(caso) {
            case "permissao" -> "Você não tem permissão para administrar este gerenciamento.";
            case "versao" -> "Gerenciamento alterado por outro usuário. Atualize e tente novamente.";
            case "arquivo" -> "Gerenciamento arquivado. Restaure-o antes de alterar.";
            case "final" -> "Etapas finais obrigatórias não podem ser alteradas.";
            case "outro","ausente" -> "Etapa não encontrada neste gerenciamento.";
            case "nome" -> "Informe um nome de etapa entre 1 e 255 caracteres.";
            case "setor" -> "Informe um setor entre 1 e 255 caracteres.";
            case "posicao" -> "Escolha uma posição válida para a etapa.";
            default -> "Sessão inválida ou expirada.";
        };
        assertThatThrownBy(() -> service.editar(caso.equals("sessao")?"invalida":token(autor).substring(7),q.getId(),caso.equals("ausente")?-1L:a.getId(),
            caso.equals("nome")?" ":"Novo",caso.equals("setor")?" ":"Setor",caso.equals("posicao")?0:1,caso.equals("versao")?99L:q.getVersao())).hasMessage(mensagem);
        assertThat(conteudoComAtividade()).isEqualTo(antes);
    }
    @Test void falhaDoDiarioReverteNomeSetorSequenciaEVersao() {
        var u=usuario("Dono");var q=quadro(u);var a=etapa(q,"A",7);etapa(q,"B",19);var antes=conteudoComAtividade();
        doThrow(new IllegalStateException("Falha do diário")).when(atividades).saveAndFlush(any(Atividade.class));
        assertThatThrownBy(() -> service.editar(token(u).substring(7),q.getId(),a.getId(),"Novo","Setor",2,0L)).hasMessage("Falha do diário");
        assertThat(conteudoComAtividade()).isEqualTo(antes);
    }
}
