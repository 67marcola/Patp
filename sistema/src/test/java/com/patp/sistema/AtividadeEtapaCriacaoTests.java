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
import com.patp.sistema.service.AtividadeRegistroService.Alteracao;

@SpringBootTest @AutoConfigureMockMvc
class AtividadeEtapaCriacaoTests extends AtividadeEtapaSupport {
    @ParameterizedTest @ValueSource(ints={1,2,3})
    void criaInicioMeioFimComTodasAsPosicoesAfetadas(int posicao) {
        var u=usuario("Dono");var q=quadro(u);var a=etapa(q,"A",1);var b=etapa(q,"B",2);
        var r=service.criar(token(u).substring(7),q.getId(),"  Nova  ","  Setor  ",posicao,0L);
        var nova=etapas.findByGerenciamentoIdOrderByOrdemAscIdAsc(q.getId()).stream().filter(e -> e.getNome().equals("Nova")).findFirst().orElseThrow();
        List<Alteracao> esperado=new ArrayList<>();
        if(posicao==1)esperado.add(new Alteracao(ETAPA,a.getId(),"A","ordem","1","2"));
        if(posicao<=2)esperado.add(new Alteracao(ETAPA,b.getId(),"B","ordem","2","3"));
        esperado.add(new Alteracao(ETAPA,nova.getId(),"Nova","nome",null,"Nova"));
        esperado.add(new Alteracao(ETAPA,nova.getId(),"Nova","setor",null,"Setor"));
        esperado.add(new Alteracao(ETAPA,nova.getId(),"Nova","ordem",null,Integer.toString(posicao)));
        evento("ETAPA_CRIADA",nova,u,ordenar(esperado));
        assertThat(r.gerenciamento().versao()).isEqualTo(1L);
        assertThat(etapas.findById(a.getId()).orElseThrow().getOrdem()).isEqualTo(posicao==1?2:1);
        assertThat(etapas.findById(b.getId()).orElseThrow().getOrdem()).isEqualTo(posicao<=2?3:2);
        assertThat(nova.getOrdem()).isEqualTo(posicao);
    }
    @Test void primeiraEtapaSemEventosFalsosParaFinaisAutomaticas() {
        var u=usuario("Dono");var q=quadro(u);service.criar(token(u).substring(7),q.getId(),"Nova","Setor",1,0L);
        var nova=etapas.findByGerenciamentoIdOrderByOrdemAscIdAsc(q.getId()).stream().filter(e -> e.getNome().equals("Nova")).findFirst().orElseThrow();
        evento("ETAPA_CRIADA",nova,u,List.of(new Alteracao(ETAPA,nova.getId(),"Nova","nome",null,"Nova"),
            new Alteracao(ETAPA,nova.getId(),"Nova","setor",null,"Setor"),new Alteracao(ETAPA,nova.getId(),"Nova","ordem",null,"1")));
        assertThat(etapas.findByGerenciamentoIdOrderByOrdemAscIdAsc(q.getId())).hasSize(3);
    }
    @Test void lacunasEEmpatesRegistramTodasAsOrdensAnterioresReais() {
        var u=usuario("Dono");var q=quadro(u);var a=etapa(q,"A",5);var b=etapa(q,"B",5);var c=etapa(q,"C",12);
        service.criar(token(u).substring(7),q.getId(),"Nova","Setor",2,0L);
        var nova=etapas.findByGerenciamentoIdOrderByOrdemAscIdAsc(q.getId()).stream().filter(e -> e.getNome().equals("Nova")).findFirst().orElseThrow();
        evento("ETAPA_CRIADA",nova,u,ordenar(List.of(
            new Alteracao(ETAPA,a.getId(),"A","ordem","5","1"),new Alteracao(ETAPA,b.getId(),"B","ordem","5","3"),
            new Alteracao(ETAPA,c.getId(),"C","ordem","12","4"),new Alteracao(ETAPA,nova.getId(),"Nova","nome",null,"Nova"),
            new Alteracao(ETAPA,nova.getId(),"Nova","setor",null,"Setor"),new Alteracao(ETAPA,nova.getId(),"Nova","ordem",null,"2"))));
        assertThat(etapas.findById(a.getId()).orElseThrow().getOrdem()).isEqualTo(1);
        assertThat(etapas.findById(b.getId()).orElseThrow().getOrdem()).isEqualTo(3);
        assertThat(etapas.findById(c.getId()).orElseThrow().getOrdem()).isEqualTo(4);
        assertThat(nova.getOrdem()).isEqualTo(2);
    }
    @ParameterizedTest @ValueSource(strings={"permissao","versao","arquivo"})
    void recusasConservamTodosOsDados(String caso) {
        var u=usuario("Dono");var q=quadro(u);etapa(q,"A",9);var autor=caso.equals("permissao")?usuario("Leitor"):u;
        if(caso.equals("arquivo")){q.setArquivado(true);quadros.saveAndFlush(q);}
        var antes=conteudoComAtividade();String mensagem=caso.equals("permissao")?"Você não tem permissão para administrar este gerenciamento.":caso.equals("versao")?"Gerenciamento alterado por outro usuário. Atualize e tente novamente.":"Gerenciamento arquivado. Restaure-o antes de alterar.";
        assertThatThrownBy(() -> service.criar(token(autor).substring(7),q.getId(),"Nova","Setor",1,caso.equals("versao")?99L:q.getVersao())).hasMessage(mensagem);
        assertThat(conteudoComAtividade()).isEqualTo(antes);
    }
    @ParameterizedTest @ValueSource(strings={"nome","setor","posicao","sessao","ausente"})
    void validacaoSessaoEQuadroAusenteNaoGeramEvento(String caso) {
        var u=usuario("Dono");var q=quadro(u);etapa(q,"A",9);var antes=conteudoComAtividade();
        String mensagem=switch(caso) {
            case "nome" -> "Informe um nome de etapa entre 1 e 255 caracteres.";
            case "setor" -> "Informe um setor entre 1 e 255 caracteres.";
            case "posicao" -> "Escolha uma posição válida para a etapa.";
            case "sessao" -> "Sessão inválida ou expirada.";
            default -> "Gerenciamento não encontrado.";
        };
        assertThatThrownBy(() -> service.criar(caso.equals("sessao")?"invalida":token(u).substring(7),caso.equals("ausente")?-1L:q.getId(),
            caso.equals("nome")?" ":"Nova",caso.equals("setor")?" ":"Setor",caso.equals("posicao")?0:1,0L)).hasMessage(mensagem);
        assertThat(conteudoComAtividade()).isEqualTo(antes);
    }
    @Test void falhaDoDiarioReverteEtapaNormalizacaoFinaisEVersao() {
        var u=usuario("Dono");var q=quadro(u);etapa(q,"A",9);var antes=conteudoComAtividade();
        doThrow(new IllegalStateException("Falha do diário")).when(atividades).saveAndFlush(any(Atividade.class));
        assertThatThrownBy(() -> service.criar(token(u).substring(7),q.getId(),"Nova","Setor",1,0L)).hasMessage("Falha do diário");
        assertThat(conteudoComAtividade()).isEqualTo(antes);
    }
}
