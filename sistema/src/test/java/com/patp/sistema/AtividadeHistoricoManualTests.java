package com.patp.sistema;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import com.patp.sistema.service.HistoricoService;

@SpringBootTest @AutoConfigureMockMvc
class AtividadeHistoricoManualTests extends ApiIntegrationSupport {
    @Autowired HistoricoService historicos;
    private static final String ERRO="Histórico é registrado automaticamente pelas ações do sistema.";
    static Stream<Arguments> forjados(){return Stream.of((Boolean)null,false).flatMap(a->Stream.of("MANUAL","DEMANDA_CONCLUIDA").map(t->Arguments.of(a,t)));}
    @ParameterizedTest @MethodSource("forjados")
    void tipoEAutorForjadosEmDemandaAtivaNaoGravam(Boolean arquivo,String tipo)throws Exception {
        var q=quadro(usuario("Dono"));var p=demanda(etapa(q,"Inicial",1));p.setArquivado(arquivo);processos.saveAndFlush(p);var antes=conteudoComAtividade();
        mvc.perform(post("/api/processos/"+p.getId()+"/historico").header("Authorization",token(usuario("Leitor"))).contentType(MediaType.APPLICATION_JSON).content("{\"acao\":\""+tipo+"\",\"descricao\":\"Forjada\",\"usuario\":\"Administrador\"}")).andExpect(status().isConflict()).andExpect(jsonPath("$.erro").value(ERRO));assertThat(conteudoComAtividade()).isEqualTo(antes);
    }
    @ParameterizedTest @ValueSource(booleans={false,true})
    void semSessaoOuInvalidaPermanece401(boolean invalida)throws Exception {
        var q=quadro(usuario("Dono"));var p=demanda(etapa(q,"Inicial",1));var antes=conteudoComAtividade();var r=post("/api/processos/"+p.getId()+"/historico").contentType(MediaType.APPLICATION_JSON).content("{\"acao\":\"MANUAL\"}");if(invalida)r.header("Authorization","Bearer invalida");mvc.perform(r).andExpect(status().isUnauthorized());assertThat(conteudoComAtividade()).isEqualTo(antes);
    }
    @ParameterizedTest @ValueSource(booleans={false,true})
    void precedenciaDeArquivoPermanece(boolean quadroArquivo)throws Exception {
        var u=usuario("Dono");var q=quadro(u);var p=demanda(etapa(q,"Inicial",1));if(quadroArquivo)jdbc.update("update gerenciamentos set arquivado=true where id=?",q.getId());p.setArquivado(true);processos.saveAndFlush(p);var antes=conteudoComAtividade();
        mvc.perform(post("/api/processos/"+p.getId()+"/historico").header("Authorization",token(u)).contentType(MediaType.APPLICATION_JSON).content("{\"acao\":\"MANUAL\",\"descricao\":\"Forjada\",\"usuario\":\"Forjado\"}")).andExpect(status().isConflict()).andExpect(jsonPath("$.erro").value(quadroArquivo?"Gerenciamento arquivado. Restaure-o antes de alterar.":"Demanda arquivada. Restaure-a antes de alterar."));assertThat(conteudoComAtividade()).isEqualTo(antes);
    }
    @Test void ausenciaPreservaErroAnteriorESemEscrita(){var antes=conteudoComAtividade();assertThatThrownBy(()->historicos.registrarExterno(999999L,"MANUAL","Forjada","Forjado")).hasMessage("Processo não encontrado.");assertThat(conteudoComAtividade()).isEqualTo(antes);}
    @Test void leituraAntigaContinuaLiteralSemNormalizacao()throws Exception {
        var u=usuario("Dono");var q=quadro(u);var p=demanda(etapa(q,"Inicial",1));jdbc.update("insert into historicos(acao,descricao,data_hora,usuario,processo_id) values(?,?,'2001-02-03 04:05:06.123456',null,?)"," Ação antiga ","  <script>literal</script>\n  ",p.getId());var antes=conteudoComAtividade();
        mvc.perform(get("/api/processos/"+p.getId()+"/historico").header("Authorization",token(u))).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1)).andExpect(jsonPath("$[0].acao").value(" Ação antiga ")).andExpect(jsonPath("$[0].descricao").value("  <script>literal</script>\n  ")).andExpect(jsonPath("$[0].usuario").isEmpty()).andExpect(jsonPath("$[0].dataHora").value("2001-02-03T04:05:06.123456"));assertThat(conteudoComAtividade()).isEqualTo(antes);
    }
}
