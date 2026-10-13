package com.patp.sistema;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import com.patp.sistema.model.Processo;
import com.patp.sistema.model.Usuario;
import com.patp.sistema.repository.AtividadeRepository;
import com.patp.sistema.service.ProcessoService;
import com.patp.sistema.service.AtividadeRegistroService.Alteracao;
import tools.jackson.databind.ObjectMapper;

abstract class AtividadeDemandaSupport extends ApiIntegrationSupport {
    private java.time.LocalDateTime inicio;
    @org.junit.jupiter.api.BeforeEach
    void registrarInicio() { inicio=java.time.LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.MICROS); }
    @Autowired ProcessoService service;
    @Autowired ObjectMapper json;
    @MockitoSpyBean AtividadeRepository atividades;

    protected void evento(String tipo, Processo alvo, Usuario autor, String acao, List<Alteracao> esperado) {
        assertThat(atividades.count()).isEqualTo(1);
        var a=atividades.findAll().get(0);
        assertThat(a.getTipo()).isEqualTo(tipo);assertThat(a.getAlvoTipo()).isEqualTo("DEMANDA");
        assertThat(a.getAlvoId()).isEqualTo(alvo.getId());assertThat(a.getAlvoNome()).isEqualTo(alvo.getNumeroProcesso());
        assertThat(a.getGerenciamento().getId()).isEqualTo(alvo.getEtapa().getGerenciamento().getId());
        assertThat(a.getAutorId()).isEqualTo(autor.getId());assertThat(a.getAutorNome()).isEqualTo(autor.getNome());
        assertThat(a.getDataHora()).isNotNull();assertThat(a.getDataHora().getNano()%1000).isZero();
        assertThat(a.getDataHora()).isBetween(inicio,java.time.LocalDateTime.now());
        assertThat(json.readTree(a.getAlteracoesJson())).isEqualTo(json.readTree(json.writeValueAsString(esperado)));
        assertThat(jdbc.queryForObject("select count(*) from historicos", Long.class)).isEqualTo(1L);
        assertThat(jdbc.queryForObject("select id from historicos", Long.class)).isEqualTo(a.getHistoricoId());
        assertThat(jdbc.queryForObject("select acao from historicos", String.class)).isEqualTo(acao);
        assertThat(jdbc.queryForObject("select usuario from historicos", String.class)).isEqualTo(autor.getNome());
        assertThat(jdbc.queryForObject("select processo_id from historicos", Long.class)).isEqualTo(alvo.getId());
    }
    protected Alteracao campo(Processo p,String campo,String anterior,String novo) {
        return new Alteracao(com.patp.sistema.service.AtividadeRegistroService.AlvoTipo.DEMANDA,p.getId(),p.getNumeroProcesso(),campo,anterior,novo);
    }
    protected String base(Long q, Long p) { return "/api/gerenciamentos/"+q+"/demandas"+(p==null?"":"/"+p); }
}
