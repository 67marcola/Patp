package com.patp.sistema;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import com.patp.sistema.model.Atividade;

@SpringBootTest @AutoConfigureMockMvc
class AtividadeDemandaArquivoTests extends AtividadeDemandaSupport {
    static Stream<Arguments> estados(){return Stream.of("Em andamento","Concluido","Cancelado").flatMap(s->Stream.of(false,true).map(a->Arguments.of(s,a)));}
    @ParameterizedTest @MethodSource("estados")
    void soMarcadorMudaNosTresEstados(String estado,boolean arquivar)throws Exception {
        var u=usuario("Dono");var q=quadro(u);var p=demanda(etapa(q,"Etapa literal",9));p.setStatus(estado);p.setArquivado(!arquivar);p.setResponsavel("Responsável");p.setObservacoes("Literal\n");p.setDataConclusao(LocalDate.of(2020,1,2));p.setDataCancelamento(LocalDate.of(2020,2,3));p.setMotivoCancelamento("Motivo anterior");processos.saveAndFlush(p);var antes=jdbc.queryForMap("select * from processos");antes.remove("arquivado");
        enviar(q.getId(),p.getId(),token(u),arquivar,0L).andExpect(status().isOk());evento(arquivar?"DEMANDA_ARQUIVADA":"DEMANDA_RESTAURADA",p,u,arquivar?"ARQUIVAMENTO":"RESTAURACAO",List.of(campo(p,"arquivado",Boolean.toString(!arquivar),Boolean.toString(arquivar))));
        var depois=jdbc.queryForMap("select * from processos");assertThat(depois.remove("arquivado")).isEqualTo(arquivar);assertThat(depois).isEqualTo(antes);assertThat(quadros.findById(q.getId()).orElseThrow().getVersao()).isEqualTo(1L);
    }
    @Test void marcadorNullPermaneceLiteralNoAntes()throws Exception {
        var u=usuario("Dono");var q=quadro(u);var p=demanda(etapa(q,"Inicial",1));p.setArquivado(null);processos.saveAndFlush(p);
        enviar(q.getId(),p.getId(),token(u),true,0L).andExpect(status().isOk());evento("DEMANDA_ARQUIVADA",p,u,"ARQUIVAMENTO",List.of(campo(p,"arquivado",null,"true")));assertThat(processos.findById(p.getId()).orElseThrow().getMarcadorArquivado()).isTrue();
    }
    @ParameterizedTest @ValueSource(booleans={false,true})
    void repeticaoContinua409SemNovaAtividade(boolean arquivar)throws Exception {
        var u=usuario("Dono");var q=quadro(u);var p=demanda(etapa(q,"Inicial",1));p.setArquivado(!arquivar);processos.saveAndFlush(p);enviar(q.getId(),p.getId(),token(u),arquivar,0L).andExpect(status().isOk());var antes=conteudoComAtividade();
        enviar(q.getId(),p.getId(),token(u),arquivar,1L).andExpect(status().isConflict()).andExpect(jsonPath("$.erro").value(arquivar?"A demanda já está arquivada.":"A demanda já está ativa."));assertThat(conteudoComAtividade()).isEqualTo(antes);assertThat(atividades.count()).isEqualTo(1);
    }
    @ParameterizedTest @ValueSource(strings={"versao","permissao","sessao","quadroArquivo","outro","ausente","status","versaoNull"})
    void recusasNaoAlteramDados(String caso)throws Exception {
        var u=usuario("Dono");var q=quadro(u);var p=demanda(etapa(q,"Inicial",1));if(caso.equals("status")){p.setStatus("Antigo");processos.saveAndFlush(p);}if(caso.equals("quadroArquivo")){q.setArquivado(true);quadros.saveAndFlush(q);}Long qid=caso.equals("outro")?quadro(u).getId():q.getId();var autor=caso.equals("permissao")?usuario("Leitor"):u;var antes=conteudoComAtividade();
        String mensagem=switch(caso){case "versao"->"Gerenciamento alterado por outro usuário. Atualize e tente novamente.";case "versaoNull"->"Informe uma versão válida do gerenciamento.";case "permissao"->"Você não tem permissão para administrar este gerenciamento.";case "sessao"->"Sessão inválida ou expirada.";case "quadroArquivo"->"Gerenciamento arquivado. Restaure-o antes de alterar.";case "status"->"Status antigo não reconhecido. Solicite a correção do registro.";default->"Demanda não encontrada neste gerenciamento.";};
        enviar(qid,caso.equals("ausente")?-1L:p.getId(),caso.equals("sessao")?"Bearer invalida":token(autor),true,caso.equals("versaoNull")?null:caso.equals("versao")?99L:0L).andExpect(status().is(caso.equals("versaoNull")?400:caso.equals("permissao")?403:caso.equals("sessao")?401:caso.equals("outro")||caso.equals("ausente")?404:409)).andExpect(jsonPath("$.erro").value(mensagem));assertThat(conteudoComAtividade()).isEqualTo(antes);
    }
    @ParameterizedTest @ValueSource(booleans={false,true})
    void falhaReverteMarcadorHistoricoAtividadeEVersao(boolean arquivar)throws Exception {
        var u=usuario("Dono");var q=quadro(u);var p=demanda(etapa(q,"Inicial",1));p.setArquivado(!arquivar);processos.saveAndFlush(p);var antes=conteudoComAtividade();doThrow(new IllegalStateException("Falha do diário")).when(atividades).saveAndFlush(any(Atividade.class));
        enviar(q.getId(),p.getId(),token(u),arquivar,0L).andExpect(status().isInternalServerError());assertThat(conteudoComAtividade()).isEqualTo(antes);
    }
    private org.springframework.test.web.servlet.ResultActions enviar(Long q,Long p,String bearer,boolean arquivar,Long v)throws Exception{return mvc.perform(put(base(q,p)+(arquivar?"/arquivar":"/restaurar")).header("Authorization",bearer).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(java.util.Collections.singletonMap("versao",v))));}
}
