package com.patp.sistema;

import static org.assertj.core.api.Assertions.*;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.patp.sistema.dto.EditarDemandaRequest;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
class DemandaEdicaoRequestTests {
    @Autowired ObjectMapper json;

    @Test void entradaContemSomenteDadosInformativosEVersao() { // EDI-05/09/13
        var r=json.readValue("{\"versao\":2,\"pessoa\":\"Cliente\",\"responsavel\":\"Ana\",\"prioridade\":\"Alta\",\"dataEmissao\":\"2020-02-29\",\"prazoEtapa\":null,\"prazoGeral\":\"2026-10-07\",\"observacoes\":\"Texto\"}",EditarDemandaRequest.class);
        assertThat(r).extracting("versao","pessoa","responsavel","prioridade","dataEmissao","prazoEtapa","prazoGeral","observacoes")
                .containsExactly(2L,"Cliente","Ana","Alta",LocalDate.of(2020,2,29),null,LocalDate.of(2026,10,7),"Texto");
    }

    @ParameterizedTest @ValueSource(strings={"numeroProcesso","id","status","etapaId","dataConclusao","dataCancelamento","motivoCancelamento","outro"})
    void entradaNaoRecebeNumeroOuCiclo(String campo) { // EDI-12/27
        assertThatThrownBy(()->json.readValue("{\"pessoa\":\"Cliente\",\""+campo+"\":null}",EditarDemandaRequest.class))
                .hasMessageContaining("Dados da requisição inválidos.");
    }

    @ParameterizedTest @ValueSource(strings={"\"2\"","2.5","true","{}","[]"})
    void versaoNaoSofreCoercao(String valor) { // EDI-13
        assertThatThrownBy(()->json.readValue("{\"versao\":"+valor+"}",EditarDemandaRequest.class))
                .hasMessageContaining("A versão deve ser um número inteiro.");
    }

    @ParameterizedTest @ValueSource(strings={"\"2023-02-29\"","\"07/10/2026\"","5","true","[]"})
    void dataNaoSofreCoercao(String valor) { // EDI-09
        assertThatThrownBy(()->json.readValue("{\"prazoGeral\":"+valor+"}",EditarDemandaRequest.class))
                .hasMessageContaining("Informe uma data ISO válida ou null.");
    }
}
