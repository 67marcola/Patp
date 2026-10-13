package com.patp.sistema;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import com.patp.sistema.model.Atividade;

@SpringBootTest @AutoConfigureMockMvc
class AtividadeComentarioTests extends AtividadeComentarioSupport {
    static Stream<Arguments> estados(){return Stream.of(false,true).flatMap(l->Stream.of("Em andamento","Concluido","Cancelado").map(s->Arguments.of(l,s)));}
    @ParameterizedTest @MethodSource("estados")
    void envioNasDuasRotasRegistraSemMensagemHistoricoOuVersao(boolean legado,String estado)throws Exception {
        var dono=usuario("Dono");var autor=usuario("Leitor");var q=quadro(dono);var p=demanda(etapa(q,"Inicial",1));p.setStatus(estado);processos.saveAndFlush(p);var antes=jdbc.queryForList("select * from processos");String texto="x".repeat(9999)+"ç";
        enviar(legado,q.getId(),p.getId(),token(autor),legado?Map.of("texto",texto,"funcionario","Forjado"):Map.of("texto",texto)).andExpect(status().is(legado?200:201)).andExpect(jsonPath("$.funcionario").value("Leitor"));evento(p,autor,texto);assertThat(jdbc.queryForList("select * from processos")).isEqualTo(antes);
        var antigo=atividades.findAll().get(0);autor.setNome("Posterior");usuarios.saveAndFlush(autor);p.setNumeroProcesso("Posterior");processos.saveAndFlush(p);var a=atividades.findById(antigo.getId()).orElseThrow();assertThat(a.getAutorNome()).isEqualTo("Leitor");assertThat(a.getAlvoNome()).isEqualTo(antigo.getAlvoNome());assertThat(a.getDataHora()).isEqualTo(antigo.getDataHora());
    }
    @ParameterizedTest @ValueSource(strings={"sessao","quadroArquivo","demandaArquivo","ausente","outro","null","vazio","longo","forjado"})
    void recusasNaoGeramAtividade(String caso)throws Exception {
        var u=usuario("Dono");var q=quadro(u);var p=demanda(etapa(q,"Inicial",1));if(caso.equals("quadroArquivo"))jdbc.update("update gerenciamentos set arquivado=true where id=?",q.getId());if(caso.equals("demandaArquivo")){p.setArquivado(true);processos.saveAndFlush(p);}Long qid=caso.equals("outro")?quadro(u).getId():q.getId();var d=new java.util.LinkedHashMap<String,Object>();d.put("texto",caso.equals("null")?null:caso.equals("vazio")?"  ":caso.equals("longo")?"a".repeat(10001):"Mensagem");if(caso.equals("forjado"))d.put("funcionario","Forjado");var antes=conteudoComAtividade();
        String msg=switch(caso){case "sessao"->"Sessão inválida ou expirada.";case "quadroArquivo"->"Gerenciamento arquivado. Restaure-o antes de alterar.";case "demandaArquivo"->"Demanda arquivada. Restaure-a antes de alterar.";case "ausente","outro"->"Demanda não encontrada.";case "forjado"->"Dados da requisição inválidos.";default->"Comentário deve conter texto e ter no máximo 10000 caracteres.";};
        enviar(false,qid,caso.equals("ausente")?-1L:p.getId(),caso.equals("sessao")?"Bearer invalida":token(u),d).andExpect(status().is(caso.equals("sessao")?401:caso.equals("ausente")||caso.equals("outro")?404:caso.contains("Arquivo")?409:400)).andExpect(jsonPath("$.erro").value(msg));assertThat(conteudoComAtividade()).isEqualTo(antes);
    }
    @ParameterizedTest @ValueSource(booleans={false,true})
    void falhaReverteComentarioEDiarioNasDuasRotas(boolean legado)throws Exception {
        var u=usuario("Dono");var q=quadro(u);var p=demanda(etapa(q,"Inicial",1));var antes=conteudoComAtividade();doThrow(new IllegalStateException("Falha do diário")).when(atividades).saveAndFlush(any(Atividade.class));enviar(legado,q.getId(),p.getId(),token(u),Map.of("texto","Privada")).andExpect(status().isInternalServerError());assertThat(conteudoComAtividade()).isEqualTo(antes);
    }
    private org.springframework.test.web.servlet.ResultActions enviar(boolean legado,Long q,Long p,String bearer,Map<String,Object>d)throws Exception{return mvc.perform(post(legado?"/api/processos/"+p+"/comentarios":"/api/gerenciamentos/"+q+"/demandas/"+p+"/comentarios").header("Authorization",bearer).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(d)));}
}
