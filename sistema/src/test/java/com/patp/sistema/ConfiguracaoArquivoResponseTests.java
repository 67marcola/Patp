package com.patp.sistema;

import static org.assertj.core.api.Assertions.*;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.patp.sistema.dto.*;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
class ConfiguracaoArquivoResponseTests {
    @Autowired ObjectMapper json;
    DemandaResponse item(Long id){return new DemandaResponse(id,"N"+id,"Cliente","Ana","Cancelado","Alta",null,null,null,null,null,"Antigo","Texto",10L);}
    @Test void duasListasTemMesmoContratoExato() { // ARQ-19
        var ativa=item(1L);var arquivo=item(2L);
        var r=new ConfiguracaoEtapasResponse(null,List.of(),List.of(ativa),List.of(arquivo));
        var tree=json.readTree(json.writeValueAsString(r));
        assertThat(tree.propertyNames()).containsExactlyInAnyOrder("gerenciamento","etapas","demandas","demandasArquivadas");
        for(String lista:List.of("demandas","demandasArquivadas")){
            assertThat(tree.get(lista).get(0).propertyNames()).containsExactlyInAnyOrder("id","numeroProcesso","pessoa","responsavel","status","prioridade","dataEmissao","prazoEtapa","prazoGeral","dataConclusao","dataCancelamento","motivoCancelamento","observacoes","etapaId");
            assertThat(json.treeToValue(tree.get(lista).get(0),DemandaResponse.class)).isEqualTo(lista.equals("demandas")?ativa:arquivo);
        }
    }
    @Test void construtorAnteriorGeraArquivoVazio() { // ARQ-19 borda compatibilidade
        var r=new ConfiguracaoEtapasResponse(null,List.of(),List.of(item(1L)));
        assertThat(r.demandasArquivadas()).isEmpty();
        assertThat(r.demandas()).containsExactly(item(1L));
        assertThat(json.readTree(json.writeValueAsString(r)).get("demandasArquivadas").isArray()).isTrue();
    }
}
