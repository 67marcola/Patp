package com.patp.sistema;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mockStatic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import java.time.LocalDateTime;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest @AutoConfigureMockMvc
class ComentarioPrecisaoTests extends ApiIntegrationSupport {
    @Autowired ObjectMapper json;
    @ParameterizedTest @ValueSource(booleans={false,true})
    void horarioConfirmadoPermaneceExatamenteIgualNoBancoEConsulta(boolean novo) throws Exception { // COM-10/11/23
        var q=quadro(usuario("Dono"));var p=demanda(etapa(q,"Origem",1));String t=token(usuario("Leitor"));
        String rota=novo?"/api/gerenciamentos/"+q.getId()+"/demandas/"+p.getId()+"/comentarios":"/api/processos/"+p.getId()+"/comentarios";
        var relogioServidor=LocalDateTime.of(2020,2,29,12,34,56,123456789);var esperado=LocalDateTime.of(2020,2,29,12,34,56,123456000);
        long id;LocalDateTime confirmado;
        try(var relogio=mockStatic(LocalDateTime.class,CALLS_REAL_METHODS)) {
            relogio.when(LocalDateTime::now).thenReturn(relogioServidor);
            var r=mvc.perform(post(rota).header("Authorization",t).contentType(MediaType.APPLICATION_JSON).content("{\"texto\":\"Literal\"}"))
                .andExpect(status().is(novo?201:200)).andExpect(jsonPath("$.texto").value("Literal")).andExpect(jsonPath("$.funcionario").value("Leitor")).andReturn();
            var corpo=json.readTree(r.getResponse().getContentAsString());id=corpo.get("id").asLong();confirmado=LocalDateTime.parse(corpo.get("dataHora").asText());
        }
        assertThat(confirmado).isEqualTo(esperado);assertThat(jdbc.queryForObject("select data_hora from comentarios where id=?",LocalDateTime.class,id)).isEqualTo(confirmado);
        mvc.perform(get(rota).header("Authorization",t)).andExpect(status().isOk()).andExpect(jsonPath(novo?"$.comentarios[0].dataHora":"$[0].dataHora").value(confirmado.toString()));
        assertThat(jdbc.queryForObject("select count(*) from comentarios",Long.class)).isEqualTo(1L);
    }
}
