package com.patp.sistema;

import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import com.patp.sistema.model.Processo;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest @AutoConfigureMockMvc
class DemandaArquivoModeloTests extends ApiIntegrationSupport {
    @Autowired ObjectMapper json;

    @ParameterizedTest @NullSource @ValueSource(booleans={false,true})
    void marcadorPersisteSemAlterarDadosOuNormalizarNull(Boolean valor) { // ARQ-18
        var p=demanda(etapa(quadro(usuario("Dono")),"Origem",1));
        var antes=jdbc.queryForMap("select * from processos where id=?",p.getId());
        p.setArquivado(valor);processos.saveAndFlush(p);
        var salvo=processos.findById(p.getId()).orElseThrow();
        assertThat(salvo.isArquivado()).isEqualTo(Boolean.TRUE.equals(valor));
        assertThat(json.readTree(json.writeValueAsString(salvo)).get("arquivado").asBoolean()).isEqualTo(Boolean.TRUE.equals(valor));
        var esperado=new java.util.LinkedHashMap<>(antes);
        esperado.replaceAll((chave,antigo)->chave.equalsIgnoreCase("arquivado")?valor:antigo);
        assertThat(jdbc.queryForMap("select * from processos where id=?",p.getId())).isEqualTo(esperado);
    }

    @ParameterizedTest @ValueSource(booleans={false,true})
    void payloadLegadoNaoControlaMarcadorENovoComecaAtivo(boolean enviado) { // ARQ-18
        var p=json.readValue("{\"numeroProcesso\":\"NOVO\",\"pessoa\":\"Cliente\",\"arquivado\":"+enviado+"}",Processo.class);
        assertThat(p.isArquivado()).isFalse();
        assertThat(new Processo().isArquivado()).isFalse();
        assertThat(p).extracting("numeroProcesso","pessoa").containsExactly("NOVO","Cliente");
    }
}
