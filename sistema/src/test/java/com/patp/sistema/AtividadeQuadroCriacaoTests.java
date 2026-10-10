package com.patp.sistema;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import com.patp.sistema.model.Atividade;
import com.patp.sistema.model.Etapa;
import com.patp.sistema.repository.AtividadeRepository;
import com.patp.sistema.service.GerenciamentoService;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest @AutoConfigureMockMvc
class AtividadeQuadroCriacaoTests extends ApiIntegrationSupport {
    @Autowired GerenciamentoService service;
    @Autowired ObjectMapper json;
    @MockitoSpyBean AtividadeRepository atividades;

    @ParameterizedTest @ValueSource(booleans={false,true})
    void criacaoUnicaConservaQuadroEtapasIniciaisAutorEHorario(boolean comEtapa) {
        var u=usuario("Criador");var inicial=new Etapa();inicial.setNome("  Trabalho  ");inicial.setSetor("  Setor  ");inicial.setOrdem(7);
        var inicio=LocalDateTime.now().minusSeconds(1);
        var criado=service.criarGerenciamento(token(u).substring(7),"  Quadro novo  ","  Descrição literal  ",comEtapa?List.of(inicial):null);
        assertThat(atividades.count()).isEqualTo(1);var a=atividades.findAll().get(0);
        assertThat(a.getTipo()).isEqualTo("QUADRO_CRIADO");assertThat(a.getAlvoTipo()).isEqualTo("QUADRO");
        assertThat(a.getAlvoId()).isEqualTo(criado.id());assertThat(a.getGerenciamento().getId()).isEqualTo(criado.id());
        assertThat(a.getAlvoNome()).isEqualTo("Quadro novo");assertThat(a.getAutorId()).isEqualTo(u.getId());assertThat(a.getAutorNome()).isEqualTo("Criador");
        assertThat(a.getDataHora()).isBetween(inicio,LocalDateTime.now().plusSeconds(1));assertThat(a.getDataHora().getNano()%1000).isZero();
        assertThat(a.getHistoricoId()).isNull();assertThat(criado.versao()).isZero();
        var changes=json.readTree(a.getAlteracoesJson());assertThat(changes.size()).isEqualTo(comEtapa?6:3);
        assertThat(changes.get(0).get("novo").asText()).isEqualTo("Quadro novo");
        assertThat(changes.get(1).get("novo").asText()).isEqualTo("  Descrição literal  ");
        assertThat(changes.get(2).get("novo").asText()).isEqualTo("false");
        if(comEtapa) {
            assertThat(changes.get(3).get("alvoId").asLong()).isEqualTo(inicial.getId());
            assertThat(changes.get(3).get("alvoTipo").asText()).isEqualTo("ETAPA");
            assertThat(changes.get(3).get("alvoNome").asText()).isEqualTo("Trabalho");
            assertThat(changes.get(3).get("campo").asText()).isEqualTo("nome");assertThat(changes.get(3).get("anterior").isNull()).isTrue();
            assertThat(changes.get(3).get("novo").asText()).isEqualTo("Trabalho");
            assertThat(changes.get(4).get("novo").asText()).isEqualTo("Setor");assertThat(changes.get(5).get("novo").asText()).isEqualTo("7");
        }
        assertThat(jdbc.queryForObject("select count(*) from etapas where gerenciamento_id=?",Long.class,criado.id())).isEqualTo(comEtapa?3L:2L);
        assertThat(a.getAlteracoesJson()).doesNotContain("Concluídos","Cancelados");
    }
    @Test void payloadForjadoNaoEscolheAutorOuTipo() throws Exception {
        var u=usuario("Real");
        mvc.perform(post("/api/gerenciamentos").header("Authorization",token(u)).contentType(MediaType.APPLICATION_JSON)
            .content("{\"nome\":\"Criado\",\"autorId\":999,\"autorNome\":\"Forjado\",\"tipo\":\"QUADRO_ARQUIVADO\"}"))
            .andExpect(status().isCreated());
        var a=atividades.findAll().get(0);assertThat(a.getAutorId()).isEqualTo(u.getId());assertThat(a.getAutorNome()).isEqualTo("Real");
        assertThat(a.getTipo()).isEqualTo("QUADRO_CRIADO");assertThat(a.getAlteracoesJson()).doesNotContain("Forjado");
    }
    @Test void nomeInvalidoConservaTodaPersistencia() {
        var u=usuario("Real");var antes=conteudoComAtividade();
        assertThatThrownBy(() -> service.criarGerenciamento(token(u).substring(7)," ",null,null)).hasMessage("Informe um nome entre 1 e 120 caracteres.");
        assertThat(conteudoComAtividade()).isEqualTo(antes);
    }
    @Test void etapaInvalidaConservaTodaPersistencia() {
        var u=usuario("Real");var antes=conteudoComAtividade();var e=new Etapa();e.setNome("Sem setor");e.setOrdem(1);
        assertThatThrownBy(() -> service.criarGerenciamento(token(u).substring(7),"Novo",null,List.of(e))).hasMessage("Informe nome, setor e ordem válidos para as etapas.");
        assertThat(conteudoComAtividade()).isEqualTo(antes);
    }
    @Test void sessaoInvalidaConservaTodaPersistencia() {
        var antes=conteudoComAtividade();
        assertThatThrownBy(() -> service.criarGerenciamento("invalida","Novo",null,null)).hasMessage("Sessão inválida ou expirada.");
        assertThat(conteudoComAtividade()).isEqualTo(antes);
    }
    @Test void falhaDoDiarioReverteQuadroEIniciaisEFinais() {
        var u=usuario("Real");var antes=conteudoComAtividade();var e=new Etapa();e.setNome("Trabalho");e.setSetor("Setor");e.setOrdem(1);
        doThrow(new IllegalStateException("Falha do diário")).when(atividades).saveAndFlush(any(Atividade.class));
        assertThatThrownBy(() -> service.criarGerenciamento(token(u).substring(7),"Novo",null,List.of(e))).hasMessage("Falha do diário");
        assertThat(conteudoComAtividade()).isEqualTo(antes);
    }
}
