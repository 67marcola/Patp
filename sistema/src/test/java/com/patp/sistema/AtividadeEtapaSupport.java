package com.patp.sistema;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import com.patp.sistema.model.Atividade;
import com.patp.sistema.model.Etapa;
import com.patp.sistema.model.Usuario;
import com.patp.sistema.repository.AtividadeRepository;
import com.patp.sistema.service.AtividadeRegistroService.Alteracao;
import com.patp.sistema.service.EtapaService;
import tools.jackson.databind.ObjectMapper;

abstract class AtividadeEtapaSupport extends ApiIntegrationSupport {
    @Autowired EtapaService service;
    @Autowired ObjectMapper json;
    @MockitoSpyBean AtividadeRepository atividades;

    protected void evento(String tipo, Etapa alvo, Usuario autor, List<Alteracao> esperado) {
        assertThat(atividades.count()).isEqualTo(1);
        Atividade a=atividades.findAll().get(0);
        assertThat(a.getTipo()).isEqualTo(tipo);assertThat(a.getAlvoTipo()).isEqualTo("ETAPA");
        assertThat(a.getAlvoId()).isEqualTo(alvo.getId());assertThat(a.getAlvoNome()).isEqualTo(alvo.getNome());
        assertThat(a.getGerenciamento().getId()).isEqualTo(alvo.getGerenciamento().getId());
        assertThat(a.getAutorId()).isEqualTo(autor.getId());assertThat(a.getAutorNome()).isEqualTo(autor.getNome());
        assertThat(a.getHistoricoId()).isNull();assertThat(a.getDataHora()).isNotNull();assertThat(a.getDataHora().getNano()%1000).isZero();
        assertThat(json.readTree(a.getAlteracoesJson())).isEqualTo(json.readTree(json.writeValueAsString(esperado)));
    }
    protected List<Alteracao> ordenar(List<Alteracao> esperado) {
        return esperado.stream().sorted(java.util.Comparator.comparing(Alteracao::alvoId)
                .thenComparingInt(a -> List.of("nome","setor","ordem").indexOf(a.campo()))).toList();
    }
}
