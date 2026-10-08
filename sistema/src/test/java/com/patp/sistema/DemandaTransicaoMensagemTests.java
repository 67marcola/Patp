package com.patp.sistema;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import com.patp.sistema.model.CategoriaEtapa;
import com.patp.sistema.model.Etapa;
import com.patp.sistema.model.Gerenciamento;

@SpringBootTest
@AutoConfigureMockMvc
class DemandaTransicaoMensagemTests extends ApiIntegrationSupport {
    @ParameterizedTest
    @CsvSource({"concluir,CONCLUIDA,false", "concluir,CONCLUIDA,true", "cancelar,CANCELADA,false", "cancelar,CANCELADA,true"})
    void fechamentoOrientaPreparacaoSemCriarOuCorrigirFinais(String acao, CategoriaEtapa categoria, boolean duplicada) throws Exception { // MOV-14
        var dono = usuario("Dono"); var q = quadro(dono); var p = demanda(etapa(q, "Trabalho", 1));
        if (duplicada) { finalEtapa(q, categoria); finalEtapa(q, categoria); }
        conferirRecusa(token(dono), q.getId(), p.getId(), acao, "{\"versao\":0" + (acao.equals("cancelar") ? ",\"motivo\":\"Solicitação\"" : "") + "}",
                "Prepare as etapas finais deste gerenciamento antes de encerrar demandas.");
    }

    @ParameterizedTest
    @CsvSource({"mover,CONCLUIDA", "concluir,CONCLUIDA", "cancelar,CONCLUIDA", "mover,CANCELADA", "concluir,CANCELADA", "cancelar,CANCELADA"})
    void andamentoEmFinalOrientaCorrecaoSemNormalizarRegistro(String acao, CategoriaEtapa categoria) throws Exception { // Edge MOV-05/10/11
        var dono = usuario("Dono"); var q = quadro(dono); var p = demanda(finalEtapa(q, categoria));
        var trabalho = etapa(q, "Trabalho", 1);
        String corpo = "{\"versao\":0" + (acao.equals("mover") ? ",\"etapaId\":" + trabalho.getId() : acao.equals("cancelar") ? ",\"motivo\":\"Solicitação\"" : "") + "}";
        conferirRecusa(token(dono), q.getId(), p.getId(), acao, corpo,
                "Estado da demanda incompatível com a etapa atual. Solicite a correção do registro.");
    }

    private void conferirRecusa(String bearer, Long quadro, Long demanda, String acao, String corpo, String mensagem) throws Exception {
        var antes = conteudoPersistido(); var versao = quadros.findById(quadro).orElseThrow().getVersao();
        mvc.perform(put("/api/gerenciamentos/" + quadro + "/demandas/" + demanda + "/" + acao)
                .header("Authorization", bearer).contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.erro").value(mensagem));
        assertThat(conteudoPersistido()).isEqualTo(antes);
        assertThat(quadros.findById(quadro).orElseThrow().getVersao()).isEqualTo(versao);
    }

    private Etapa finalEtapa(Gerenciamento quadro, CategoriaEtapa categoria) {
        var finalizada = etapa(quadro, categoria.name(), 99); finalizada.setCategoria(categoria);
        return etapas.saveAndFlush(finalizada);
    }
}
