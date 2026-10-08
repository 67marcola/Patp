package com.patp.sistema;

import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import com.patp.sistema.dto.DemandaResponse;
import com.patp.sistema.exception.ApiException;
import com.patp.sistema.service.EtapaService;
import com.patp.sistema.service.GerenciamentoService;

@SpringBootTest @AutoConfigureMockMvc
class DemandaArquivoLeituraTests extends ApiIntegrationSupport {
    @Autowired EtapaService service;
    @Autowired GerenciamentoService quadrosService;
    @ParameterizedTest @ValueSource(booleans={false,true})
    void consultaParticionaPreservaNullEContaSomenteAtivasMesmoNoQuadroArquivado(boolean quadroArquivado) { // ARQ-18/19/20
        var dono=usuario("Dono");var q=quadro(dono);var e=etapa(q,"Origem",1);var e2=etapa(q,"Arquivo",2);
        var nulo=demanda(e);nulo.setArquivado(null);processos.saveAndFlush(nulo);
        var arquivo=demanda(e2);arquivo.setArquivado(true);processos.saveAndFlush(arquivo);
        var ativa=new com.patp.sistema.model.Processo();ativa.setEtapa(e);ativa.setPessoa("Pessoa");ativa.setStatus("Em andamento");ativa.setNumeroProcesso("ATIVA");processos.saveAndFlush(ativa);
        q.setArquivado(quadroArquivado);quadros.saveAndFlush(q);var antes=conteudoPersistido();var versao=quadros.findById(q.getId()).orElseThrow().getVersao();
        var r=service.configuracao(token(usuario("Leitor")).substring(7),q.getId());
        assertThat(r.demandas()).containsExactly(DemandaResponse.de(nulo),DemandaResponse.de(ativa));
        assertThat(r.demandasArquivadas()).containsExactly(DemandaResponse.de(arquivo));
        assertThat(r.etapas()).extracting("quantidadeDemandas").containsExactly(2L,0L);
        assertThat(r.gerenciamento().arquivado()).isEqualTo(quadroArquivado);
        assertThat(conteudoPersistido()).isEqualTo(antes);
        assertThat(quadros.findById(q.getId()).orElseThrow().getVersao()).isEqualTo(versao);
    }
    @Test void mutacoesDeEtapaDevolvemArquivoEZeroContagem() { // ARQ-19/20
        var dono=usuario("Dono");var q=quadro(dono);var e=etapa(q,"Origem",1);var p=demanda(e);p.setArquivado(true);processos.saveAndFlush(p);String t=token(dono).substring(7);
        var criado=service.criar(t,q.getId(),"Nova","Setor",2,0L);
        var editado=service.editar(t,q.getId(),e.getId(),"Origem renomeada","Setor",1,1L);
        var nova=criado.etapas().stream().filter(x->x.nome().equals("Nova")).findFirst().orElseThrow();
        var removido=service.excluir(t,q.getId(),nova.id(),2L);
        for(var r:java.util.List.of(criado,editado,removido)){
            assertThat(r.demandas()).isEmpty();assertThat(r.demandasArquivadas()).containsExactly(DemandaResponse.de(p));
            assertThat(r.etapas()).extracting("quantidadeDemandas").containsOnly(0L);
        }
        assertThat(removido.gerenciamento().versao()).isEqualTo(3L);
    }
    @Test void etapaComSomenteArquivoNaoPodeSerRemovida() { // ARQ-21
        var dono=usuario("Dono");var q=quadro(dono);var e=etapa(q,"Origem",1);var p=demanda(e);p.setArquivado(true);processos.saveAndFlush(p);var antes=conteudoPersistido();
        assertThatThrownBy(()->service.excluir(token(dono).substring(7),q.getId(),e.getId(),0L)).isInstanceOfSatisfying(ApiException.class,x->{assertThat(x.getStatus().value()).isEqualTo(409);assertThat(x.getMessage()).isEqualTo("Esta etapa possui demandas. Mova-as para outra etapa antes de removê-la.");});
        assertThat(conteudoPersistido()).isEqualTo(antes);assertThat(quadros.findById(q.getId()).orElseThrow().getVersao()).isZero();
    }
    @Test void restaurarQuadroNaoRestauraDemanda() { // ARQ-19 borda independência
        var dono=usuario("Dono");var q=quadro(dono);var p=demanda(etapa(q,"Origem",1));p.setArquivado(true);processos.saveAndFlush(p);String t=token(dono).substring(7);var antes=conteudoPersistido();
        quadrosService.mudarEstado(t,q.getId(),0L,true);quadrosService.mudarEstado(t,q.getId(),1L,false);
        var r=service.configuracao(t,q.getId());
        assertThat(r.demandasArquivadas()).containsExactly(DemandaResponse.de(p));assertThat(r.demandas()).isEmpty();
        assertThat(conteudoPersistido()).isEqualTo(antes);assertThat(r.gerenciamento().versao()).isEqualTo(2L);
    }
}
