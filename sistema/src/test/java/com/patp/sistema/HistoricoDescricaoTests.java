package com.patp.sistema;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import com.patp.sistema.service.HistoricoService;

@SpringBootTest
@AutoConfigureMockMvc
class HistoricoDescricaoTests extends ApiIntegrationSupport {
    @Autowired HistoricoService historicos;

    @ParameterizedTest
    @ValueSource(strings = {"a", "ç🙂"})
    void preservaDescricaoLongaCompletaComAutorAcaoEData(String trecho) {
        var criador = usuario("Criador");
        var demanda = demanda(etapa(quadro(criador), "Inicial", 1));
        String descricao = "Encerramento anterior\n" + trecho.repeat(20000) + "\nFim literal.";
        var antes = LocalDateTime.now().minusSeconds(1);
        var evento = historicos.registrar(demanda.getId(), "REABERTURA", descricao, criador.getNome());
        var registro = jdbc.queryForMap("select * from historicos where id = ?", evento.getId());
        assertThat(registro.get("descricao").toString()).isEqualTo(descricao); // MOV-16/19
        assertThat(registro.get("acao")).isEqualTo("REABERTURA");
        assertThat(registro.get("usuario")).isEqualTo("Criador");
        assertThat(((Number) registro.get("processo_id")).longValue()).isEqualTo(demanda.getId());
        assertThat(evento.getDataHora()).isBetween(antes, LocalDateTime.now().plusSeconds(1));
        assertThat(jdbc.queryForObject("select count(*) from historicos", Long.class)).isEqualTo(1L);
        assertThat(processos.findById(demanda.getId()).orElseThrow().getStatus()).isEqualTo("Em andamento");
    }
}
