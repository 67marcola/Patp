package com.patp.sistema;

import static org.assertj.core.api.Assertions.assertThat;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import com.patp.sistema.repository.AtividadeRepository;
import com.patp.sistema.service.ComentarioService;
import com.patp.sistema.model.Processo;
import com.patp.sistema.model.Usuario;
import tools.jackson.databind.ObjectMapper;

abstract class AtividadeComentarioSupport extends ApiIntegrationSupport {
    @Autowired ComentarioService service;
    @Autowired ObjectMapper json;
    @MockitoSpyBean AtividadeRepository atividades;
    private LocalDateTime inicio;
    @BeforeEach void inicio(){inicio=LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.MICROS);}
    protected void evento(Processo p,Usuario autor,String mensagem) {
        assertThat(atividades.count()).isEqualTo(1);var a=atividades.findAll().get(0);
        assertThat(a.getTipo()).isEqualTo("COMENTARIO_ADICIONADO");assertThat(a.getAlvoTipo()).isEqualTo("DEMANDA");assertThat(a.getAlvoId()).isEqualTo(p.getId());assertThat(a.getAlvoNome()).isEqualTo(p.getNumeroProcesso());assertThat(a.getGerenciamento().getId()).isEqualTo(p.getEtapa().getGerenciamento().getId());
        assertThat(a.getAutorId()).isEqualTo(autor.getId());assertThat(a.getAutorNome()).isEqualTo(autor.getNome());assertThat(a.getHistoricoId()).isNull();assertThat(json.readTree(a.getAlteracoesJson()).isArray()).isTrue();assertThat(json.readTree(a.getAlteracoesJson()).size()).isZero();assertThat(a.getAlteracoesJson()).doesNotContain(mensagem);
        assertThat(a.getDataHora()).isEqualTo(jdbc.queryForObject("select data_hora from comentarios",LocalDateTime.class));assertThat(a.getDataHora()).isBetween(inicio,LocalDateTime.now());assertThat(a.getDataHora().getNano()%1000).isZero();
        assertThat(jdbc.queryForObject("select count(*) from comentarios",Long.class)).isEqualTo(1L);assertThat(jdbc.queryForObject("select texto from comentarios",String.class)).isEqualTo(mensagem);assertThat(jdbc.queryForObject("select funcionario from comentarios",String.class)).isEqualTo(autor.getNome());assertThat(jdbc.queryForObject("select processo_id from comentarios",Long.class)).isEqualTo(p.getId());
        assertThat(jdbc.queryForObject("select count(*) from historicos",Long.class)).isZero();assertThat(quadros.findById(p.getEtapa().getGerenciamento().getId()).orElseThrow().getVersao()).isZero();
    }
}
