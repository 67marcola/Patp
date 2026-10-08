package com.patp.sistema;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;

@SpringBootTest @AutoConfigureMockMvc
class DemandaEdicaoLegadaTests extends ApiIntegrationSupport {
    @ParameterizedTest @CsvSource({"Em andamento,false","Concluido,false","Cancelado,false","Em andamento,true","Concluido,true","Cancelado,true"})
    void numeroOmitidoOuIdenticoPreservaReferenciaECiclo(String status,boolean numeroEnviado) throws Exception { // EDI-19/27/28
        var dono=usuario("Dono");var q=quadro(dono);var e=etapa(q,"Origem",1);var p=demanda(e);p.setStatus(status);p.setDataConclusao(java.time.LocalDate.of(2020,2,29));p.setDataCancelamento(java.time.LocalDate.of(2021,1,2));p.setMotivoCancelamento("  Literal\r\nAntigo  ");processos.saveAndFlush(p);
        String corpo="{\"pessoa\":\"Corrigida\",\"observacoes\":\"Texto\""+(numeroEnviado?",\"numeroProcesso\":\""+p.getNumeroProcesso()+"\"":"")+"}";
        mvc.perform(put("/api/processos/"+p.getId()).header("Authorization",token(dono)).contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isOk()).andExpect(jsonPath("$.numeroProcesso").value(p.getNumeroProcesso())).andExpect(jsonPath("$.pessoa").value("Corrigida"));
        var salvo=processos.findById(p.getId()).orElseThrow();assertThat(salvo).extracting("status","dataConclusao","dataCancelamento","motivoCancelamento").containsExactly(status,p.getDataConclusao(),p.getDataCancelamento(),p.getMotivoCancelamento());
        assertThat(salvo.getEtapa().getId()).isEqualTo(e.getId());assertThat(quadros.findById(q.getId()).orElseThrow().getVersao()).isEqualTo(1L);
        assertThat(jdbc.queryForList("select acao from historicos",String.class)).containsExactly("EDICAO");assertThat(jdbc.queryForList("select usuario from historicos",String.class)).containsExactly("Dono");
    }
    @ParameterizedTest @ValueSource(strings={"terceiro","numero","cliente"})
    void rotaAntigaRecusaContornosSemEfeitos(String caso) throws Exception { // EDI-02/06/19/27
        var dono=usuario("Dono");var q=quadro(dono);var p=demanda(etapa(q,"Origem",1));var antes=conteudoPersistido();
        String bearer=token(caso.equals("terceiro")?usuario("Outro"):dono);
        String corpo=caso.equals("numero")?"{\"numeroProcesso\":\"Outro número\",\"pessoa\":\"Corrigida\"}":caso.equals("cliente")?"{\"pessoa\":\"  \"}":"{\"pessoa\":\"Corrigida\"}";
        String mensagem=caso.equals("terceiro")?"Você não tem permissão para administrar este gerenciamento.":caso.equals("numero")?"O número da demanda não pode ser alterado após o cadastro.":"Informe um cliente/solicitante entre 1 e 255 caracteres.";
        mvc.perform(put("/api/processos/"+p.getId()).header("Authorization",bearer).contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().is(caso.equals("terceiro")?403:400)).andExpect(jsonPath("$.erro").value(mensagem));
        assertThat(conteudoPersistido()).isEqualTo(antes);assertThat(quadros.findById(q.getId()).orElseThrow().getVersao()).isZero();
    }
}
