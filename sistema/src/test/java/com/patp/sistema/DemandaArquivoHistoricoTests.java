package com.patp.sistema;

import static org.assertj.core.api.Assertions.*;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import com.patp.sistema.exception.ApiException;
import com.patp.sistema.service.HistoricoService;
import com.patp.sistema.service.ProcessoService;

@SpringBootTest @AutoConfigureMockMvc
class DemandaArquivoHistoricoTests extends ApiIntegrationSupport {
    @Autowired HistoricoService historico;
    @Autowired ProcessoService service;
    @ParameterizedTest @NullSource @ValueSource(booleans={false})
    void externoAceitaAtivoIncluindoNullSemNormalizacao(Boolean arquivo) { // ARQ-18/22
        var q=quadro(usuario("Dono"));var p=demanda(etapa(q,"Origem",1));p.setArquivado(arquivo);processos.saveAndFlush(p);var processoAntes=jdbc.queryForList("select * from processos");
        jdbc.update("insert into historicos(acao,descricao,data_hora,usuario,processo_id) values('MANUAL',?,'2001-02-03 04:05:06','Informado',?)","  Texto\n literal  ",p.getId());var antes=conteudoComAtividade();
        assertThatThrownBy(()->historico.registrarExterno(p.getId(),"MANUAL","  Texto\n literal  ","Informado")).isInstanceOfSatisfying(ApiException.class,x->{assertThat(x.getStatus().value()).isEqualTo(409);assertThat(x.getMessage()).isEqualTo("Histórico é registrado automaticamente pelas ações do sistema.");});
        assertThat(conteudoComAtividade()).isEqualTo(antes);var r=historico.listarPorProcesso(p.getId()).get(0);
        assertThat(r).extracting("acao","descricao","usuario").containsExactly("MANUAL","  Texto\n literal  ","Informado");assertThat(r.getProcesso().getId()).isEqualTo(p.getId());assertThat(r.getDataHora()).isEqualTo(LocalDateTime.of(2001,2,3,4,5,6));
        assertThat(jdbc.queryForObject("select count(*) from historicos",Long.class)).isEqualTo(1L);
        assertThat(jdbc.queryForMap("select * from historicos")).containsEntry("acao","MANUAL").containsEntry("descricao","  Texto\n literal  ").containsEntry("usuario","Informado").containsEntry("processo_id",p.getId());
        assertThat(jdbc.queryForList("select * from processos")).isEqualTo(processoAntes);assertThat(quadros.findById(q.getId()).orElseThrow().getVersao()).isZero();
    }
    @ParameterizedTest @ValueSource(strings={"MANUAL","ARQUIVAMENTO","RESTAURACAO"})
    void externoRecusaArquivoMesmoComNomeAdministrativo(String acao) { // ARQ-22
        var q=quadro(usuario("Dono"));var p=demanda(etapa(q,"Origem",1));p.setArquivado(true);processos.saveAndFlush(p);
        jdbc.update("insert into historicos(acao,descricao,data_hora,usuario,processo_id) values('ANTIGO','  Literal\n  ','2001-02-03 04:05:06','Antigo',?)",p.getId());var antes=conteudoPersistido();var quadroAntes=jdbc.queryForList("select * from gerenciamentos");
        assertThatThrownBy(()->historico.registrarExterno(p.getId(),acao,"Forjado","Forjado")).isInstanceOfSatisfying(ApiException.class,x->{assertThat(x.getStatus().value()).isEqualTo(409);assertThat(x.getMessage()).isEqualTo("Demanda arquivada. Restaure-a antes de alterar.");});
        assertThat(conteudoPersistido()).isEqualTo(antes);assertThat(jdbc.queryForList("select * from gerenciamentos")).isEqualTo(quadroAntes);
    }
    @Test void internoPermiteEventoAdministrativoEArquivoPreservaAnteriorLiteral() { // ARQ-14/17 borda interno
        var u=usuario("Dono");var q=quadro(u);var p=demanda(etapa(q,"Origem",1));jdbc.update("insert into historicos(acao,descricao,data_hora,usuario,processo_id) values('ANTIGO','  Literal\n  ','2001-02-03 04:05:06','Antigo',?)",p.getId());var antigo=jdbc.queryForMap("select * from historicos");String t=token(u).substring(7);
        service.arquivar(t,q.getId(),p.getId(),0L);var inicio=LocalDateTime.now().minusSeconds(1);var r=historico.registrar(p.getId(),"ADMINISTRATIVO","  Interno\n  ","Operação interna");
        assertThat(r).extracting("acao","descricao","usuario").containsExactly("ADMINISTRATIVO","  Interno\n  ","Operação interna");assertThat(r.getProcesso().getId()).isEqualTo(p.getId());assertThat(r.getDataHora()).isBetween(inicio,LocalDateTime.now().plusSeconds(1));
        service.restaurar(t,q.getId(),p.getId(),1L);
        assertThat(jdbc.queryForMap("select * from historicos where acao='ANTIGO'")).isEqualTo(antigo);
        assertThat(jdbc.queryForList("select acao from historicos order by id",String.class)).containsExactly("ANTIGO","ARQUIVAMENTO","ADMINISTRATIVO","RESTAURACAO");
        assertThat(jdbc.queryForMap("select * from historicos where acao='ADMINISTRATIVO'")).containsEntry("descricao","  Interno\n  ").containsEntry("usuario","Operação interna").containsEntry("processo_id",p.getId());
        assertThat(quadros.findById(q.getId()).orElseThrow().getVersao()).isEqualTo(2L);
    }
}
