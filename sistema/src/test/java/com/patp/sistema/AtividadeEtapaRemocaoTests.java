package com.patp.sistema;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static com.patp.sistema.service.AtividadeRegistroService.AlvoTipo.ETAPA;
import java.util.ArrayList;
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
class AtividadeEtapaRemocaoTests extends AtividadeEtapaSupport {
    @ParameterizedTest @ValueSource(ints={1,2,3})
    void removeInicioMeioFimPreservandoAlvoEPosicoesDeTodosAfetados(int posicao) {
        var u=usuario("Dono");var q=quadro(u);var trabalho=List.of(etapa(q,"A",1),etapa(q,"B",2),etapa(q,"C",3));
        var concluida=etapa(q,"Concluídos",99);concluida.setCategoria(CategoriaEtapa.CONCLUIDA);etapas.saveAndFlush(concluida);
        var cancelada=etapa(q,"Cancelados",100);cancelada.setCategoria(CategoriaEtapa.CANCELADA);etapas.saveAndFlush(cancelada);
        var removida=trabalho.get(posicao-1);var r=service.excluir(token(u).substring(7),q.getId(),removida.getId(),0L);
        var esperado=new ArrayList<Alteracao>();esperado.addAll(remocao(removida));
        for(int i=0;i<trabalho.size();i++) {
            var e=trabalho.get(i);
            if(i+1>posicao)esperado.add(new Alteracao(ETAPA,e.getId(),e.getNome(),"ordem",Integer.toString(i+1),Integer.toString(i)));
            if(i+1!=posicao)assertThat(etapas.findById(e.getId()).orElseThrow().getOrdem()).isEqualTo(i+1<posicao?i+1:i);
        }
        assertThat(etapas.findById(removida.getId())).isEmpty();evento("ETAPA_REMOVIDA",removida,u,ordenar(esperado));
        assertThat(r.gerenciamento().versao()).isEqualTo(1L);
        assertThat(etapas.findById(concluida.getId()).orElseThrow().getOrdem()).isEqualTo(99);
        assertThat(etapas.findById(cancelada.getId()).orElseThrow().getOrdem()).isEqualTo(100);
    }
    @Test void removeUnicaEtapaMantendoDiarioSemAlvoPersistido() {
        var u=usuario("Dono");var q=quadro(u);var e=etapa(q,"Última",1);
        service.excluir(token(u).substring(7),q.getId(),e.getId(),0L);
        assertThat(etapas.findById(e.getId())).isEmpty();evento("ETAPA_REMOVIDA",e,u,remocao(e));
        assertThat(etapas.findByGerenciamentoIdOrderByOrdemAscIdAsc(q.getId())).extracting(a -> a.getCategoria()).containsExactly(CategoriaEtapa.CONCLUIDA,CategoriaEtapa.CANCELADA);
    }
    @Test void remocaoNormalizaLacunasEEmpatesSemPerderAntes() {
        var u=usuario("Dono");var q=quadro(u);var a=etapa(q,"A",7);var b=etapa(q,"B",7);var c=etapa(q,"C",20);
        service.excluir(token(u).substring(7),q.getId(),b.getId(),0L);var esperado=new ArrayList<Alteracao>(remocao(b));
        esperado.add(new Alteracao(ETAPA,a.getId(),"A","ordem","7","1"));esperado.add(new Alteracao(ETAPA,c.getId(),"C","ordem","20","2"));
        evento("ETAPA_REMOVIDA",b,u,ordenar(esperado));assertThat(etapas.findById(b.getId())).isEmpty();
        assertThat(etapas.findById(a.getId()).orElseThrow().getOrdem()).isEqualTo(1);assertThat(etapas.findById(c.getId()).orElseThrow().getOrdem()).isEqualTo(2);
    }
    @ParameterizedTest @ValueSource(booleans={false,true})
    void demandaAtivaOuArquivadaImpedeRemocaoSemEvento(boolean arquivo) {
        var u=usuario("Dono");var q=quadro(u);var e=etapa(q,"Ocupada",9);var p=demanda(e);p.setArquivado(arquivo);processos.saveAndFlush(p);var antes=conteudoComAtividade();
        assertThatThrownBy(() -> service.excluir(token(u).substring(7),q.getId(),e.getId(),0L)).hasMessage("Esta etapa possui demandas. Mova-as para outra etapa antes de removê-la.");
        assertThat(conteudoComAtividade()).isEqualTo(antes);
    }
    @ParameterizedTest @ValueSource(strings={"CONCLUIDA","CANCELADA"})
    void finaisNaoPodemSerRemovidasENaoGeramEvento(String categoria) {
        var u=usuario("Dono");var q=quadro(u);var e=etapa(q,"Final",9);e.setCategoria(CategoriaEtapa.valueOf(categoria));etapas.saveAndFlush(e);var antes=conteudoComAtividade();
        assertThatThrownBy(() -> service.excluir(token(u).substring(7),q.getId(),e.getId(),0L)).hasMessage("Etapas finais obrigatórias não podem ser alteradas.");
        assertThat(conteudoComAtividade()).isEqualTo(antes);
    }
    @ParameterizedTest @ValueSource(strings={"permissao","versao","arquivo","sessao","outro","ausente","quadroAusente"})
    void recusasNaoAlteramPersistencia(String caso) {
        var u=usuario("Dono");var q=quadro(u);var e=etapa(q,"A",9);var autor=caso.equals("permissao")?usuario("Leitor"):u;
        if(caso.equals("arquivo")){q.setArquivado(true);quadros.saveAndFlush(q);}
        if(caso.equals("outro")){e.setGerenciamento(quadro(u));etapas.saveAndFlush(e);}
        var antes=conteudoComAtividade();String mensagem=switch(caso) {
            case "permissao" -> "Você não tem permissão para administrar este gerenciamento.";
            case "versao" -> "Gerenciamento alterado por outro usuário. Atualize e tente novamente.";
            case "arquivo" -> "Gerenciamento arquivado. Restaure-o antes de alterar.";
            case "sessao" -> "Sessão inválida ou expirada.";
            case "quadroAusente" -> "Gerenciamento não encontrado.";
            default -> "Etapa não encontrada neste gerenciamento.";
        };
        assertThatThrownBy(() -> service.excluir(caso.equals("sessao")?"invalida":token(autor).substring(7),caso.equals("quadroAusente")?-1L:q.getId(),caso.equals("ausente")?-1L:e.getId(),caso.equals("versao")?99L:q.getVersao())).hasMessage(mensagem);
        assertThat(conteudoComAtividade()).isEqualTo(antes);
    }
    @Test void falhaDoDiarioReverteRemocaoSequenciaFinaisEVersao() {
        var u=usuario("Dono");var q=quadro(u);var a=etapa(q,"A",7);var b=etapa(q,"B",19);var antes=conteudoComAtividade();
        doThrow(new IllegalStateException("Falha do diário")).when(atividades).saveAndFlush(any(Atividade.class));
        assertThatThrownBy(() -> service.excluir(token(u).substring(7),q.getId(),b.getId(),0L)).hasMessage("Falha do diário");
        assertThat(conteudoComAtividade()).isEqualTo(antes);
    }
    private List<Alteracao> remocao(com.patp.sistema.model.Etapa e) {
        return List.of(new Alteracao(ETAPA,e.getId(),e.getNome(),"nome",e.getNome(),null),
            new Alteracao(ETAPA,e.getId(),e.getNome(),"setor","Engenharia",null),
            new Alteracao(ETAPA,e.getId(),e.getNome(),"ordem",e.getOrdem().toString(),null));
    }
}
