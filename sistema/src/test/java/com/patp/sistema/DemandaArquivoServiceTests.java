package com.patp.sistema;

import static org.assertj.core.api.Assertions.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import com.patp.sistema.dto.*;
import com.patp.sistema.exception.ApiException;
import com.patp.sistema.model.*;
import com.patp.sistema.service.ProcessoService;

@SpringBootTest @AutoConfigureMockMvc
class DemandaArquivoServiceTests extends ApiIntegrationSupport {
    @Autowired ProcessoService service;
    ConfiguracaoEtapasResponse acao(String t,Long q,Long p,Long v,boolean arquivar){return arquivar?service.arquivar(t,q,p,v):service.restaurar(t,q,p,v);}
    void recusa(Runnable operacao,int codigo,long quadro,String mensagem) { // ARQ-01/02/03/07/08/09/11/12: sem efeitos
        var antes=conteudoPersistido();var quadrosAntes=jdbc.queryForList("select * from gerenciamentos order by id");
        assertThatThrownBy(operacao::run).isInstanceOfSatisfying(ApiException.class,e->{assertThat(e.getStatus().value()).isEqualTo(codigo);assertThat(e.getMessage()).isEqualTo(mensagem);});
        assertThat(conteudoPersistido()).isEqualTo(antes);assertThat(jdbc.queryForList("select * from gerenciamentos order by id")).isEqualTo(quadrosAntes);
    }
    @ParameterizedTest @CsvSource({"Em andamento,false","Concluido,false","Cancelado,false","Em andamento,true","Concluido,true","Cancelado,true"})
    void arquivaERestauraLiteralmenteNosTresEstadosPorCriadorOuAdmin(String status,boolean admin) { // ARQ-05/06/13/14/17
        var dono=usuario("Dono");var q=quadro(dono);var e=etapa(q,"Origem antiga",1);var p=demanda(e);
        p.setStatus(status);p.setPessoa("  Cliente antigo  ");p.setResponsavel("  Ana  ");p.setPrioridade("ALTA");p.setObservacoes("  Linha\r\n"+"x".repeat(20000));
        p.setDataEmissao(LocalDate.of(2020,2,29));p.setPrazoEtapa(LocalDate.of(2018,1,1));p.setPrazoGeral(LocalDate.of(2017,1,1));p.setDataConclusao(LocalDate.of(2021,2,3));p.setDataCancelamento(LocalDate.of(2022,3,4));p.setMotivoCancelamento("  Motivo\r\n"+"y".repeat(20000));processos.saveAndFlush(p);
        jdbc.update("insert into comentarios(texto,data_hora,funcionario,processo_id) values('  Literal\n  ','2001-02-03 04:05:06','Antigo',?)",p.getId());
        jdbc.update("insert into historicos(acao,descricao,data_hora,usuario,processo_id) values('ANTIGO','  Literal\n  ','2001-02-03 04:05:06','Antigo',?)",p.getId());
        var comentarios=jdbc.queryForList("select * from comentarios");var antigo=jdbc.queryForMap("select * from historicos");var fisico=jdbc.queryForMap("select * from processos where id=?",p.getId());var dto=DemandaResponse.de(p);
        var autor=admin?usuario("Admin"):dono;if(admin){autor.setPapel(PapelUsuario.ADMINISTRADOR);usuarios.saveAndFlush(autor);}String t=token(autor).substring(7);var inicio=LocalDateTime.now().minusSeconds(1);
        for(boolean arquivar:new boolean[]{true,false}){
            var r=acao(t,q.getId(),p.getId(),arquivar?0L:1L,arquivar);
            var esperado=new java.util.LinkedHashMap<>(fisico);esperado.replaceAll((chave,valor)->chave.equalsIgnoreCase("arquivado")?arquivar:valor);
            assertThat(jdbc.queryForMap("select * from processos where id=?",p.getId())).isEqualTo(esperado);
            assertThat(r.gerenciamento().versao()).isEqualTo(arquivar?1L:2L);assertThat(r.gerenciamento().criador().id()).isEqualTo(dono.getId());assertThat(r.gerenciamento().podeAdministrar()).isTrue();
            assertThat(arquivar?r.demandasArquivadas():r.demandas()).containsExactly(dto);assertThat(arquivar?r.demandas():r.demandasArquivadas()).isEmpty();
            assertThat(r.etapas().get(0).quantidadeDemandas()).isEqualTo(arquivar?0L:1L);
            assertThat(jdbc.queryForList("select * from comentarios")).isEqualTo(comentarios);assertThat(jdbc.queryForMap("select * from historicos where acao='ANTIGO'")).isEqualTo(antigo);
            var evento=jdbc.queryForMap("select * from historicos where acao=?",arquivar?"ARQUIVAMENTO":"RESTAURACAO");
            assertThat(evento).containsEntry("usuario",autor.getNome()).containsEntry("descricao",arquivar?"Demanda arquivada.":"Demanda restaurada.").containsEntry("processo_id",p.getId());
            assertThat(jdbc.queryForObject("select data_hora from historicos where acao=?",LocalDateTime.class,arquivar?"ARQUIVAMENTO":"RESTAURACAO")).isBetween(inicio,LocalDateTime.now().plusSeconds(1));
            assertThat(jdbc.queryForObject("select count(*) from historicos",Long.class)).isEqualTo(arquivar?2L:3L);
        }
    }
    @ParameterizedTest @CsvSource({"false,false","false,true","true,false","true,true"})
    void quadroSemCriadorSomenteAdmin(boolean admin,boolean arquivar) { // ARQ-04
        var u=usuario("Usuario");if(admin){u.setPapel(PapelUsuario.ADMINISTRADOR);usuarios.saveAndFlush(u);}var q=quadro(null);var p=demanda(etapa(q,"Origem",1));p.setArquivado(!arquivar);processos.saveAndFlush(p);String t=token(u).substring(7);
        if(admin){assertThat(acao(t,q.getId(),p.getId(),0L,arquivar).gerenciamento().versao()).isEqualTo(1L);assertThat(processos.findById(p.getId()).orElseThrow().isArquivado()).isEqualTo(arquivar);}
        else recusa(()->acao(t,q.getId(),p.getId(),0L,arquivar),403,q.getId(),"Você não tem permissão para administrar este gerenciamento.");
        assertThat(quadros.findById(q.getId()).orElseThrow().getCriador()).isNull();
    }
    @ParameterizedTest @ValueSource(booleans={false,true})
    void terceiroEQuadroArquivadoRecusam(boolean arquivar) { // ARQ-02/03
        var dono=usuario("Dono");var q=quadro(dono);var p=demanda(etapa(q,"Origem",1));p.setArquivado(!arquivar);processos.saveAndFlush(p);
        recusa(()->acao(token(usuario("Outro")).substring(7),q.getId(),p.getId(),0L,arquivar),403,q.getId(),"Você não tem permissão para administrar este gerenciamento.");q.setArquivado(true);quadros.saveAndFlush(q);
        recusa(()->acao(token(dono).substring(7),q.getId(),p.getId(),1L,arquivar),409,q.getId(),"Gerenciamento arquivado. Restaure-o antes de alterar.");
    }
    @ParameterizedTest @MethodSource("statusInvalidos")
    void statusAntigoRecusaLiteralmente(String status,boolean arquivar) { // ARQ-07
        var u=usuario("Dono");var q=quadro(u);var p=demanda(etapa(q,"Origem",1));p.setStatus(status);p.setArquivado(!arquivar);processos.saveAndFlush(p);
        recusa(()->acao(token(u).substring(7),q.getId(),p.getId(),0L,arquivar),409,q.getId(),"Status antigo não reconhecido. Solicite a correção do registro.");
        assertThat(processos.findById(p.getId()).orElseThrow().getStatus()).isEqualTo(status);
    }
    static Stream<Arguments> statusInvalidos(){return Stream.of((String)null,"","  ","Concluído","Antigo").flatMap(s->Stream.of(false,true).map(b->Arguments.of(s,b)));}
    @ParameterizedTest @ValueSource(booleans={false,true})
    void repeticaoRecusaSemIncrementoOuEvento(boolean arquivar) { // ARQ-08
        var u=usuario("Dono");var q=quadro(u);var p=demanda(etapa(q,"Origem",1));p.setArquivado(arquivar);processos.saveAndFlush(p);
        recusa(()->acao(token(u).substring(7),q.getId(),p.getId(),0L,arquivar),409,q.getId(),arquivar?"A demanda já está arquivada.":"A demanda já está ativa.");
    }
    @ParameterizedTest @MethodSource("versoes")
    void versaoAusenteNegativaOuAntiga(Long versao,boolean arquivar) { // ARQ-09/11
        var u=usuario("Dono");var q=quadro(u);var p=demanda(etapa(q,"Origem",1));p.setArquivado(!arquivar);processos.saveAndFlush(p);boolean invalida=versao==null||versao<0;
        recusa(()->acao(token(u).substring(7),q.getId(),p.getId(),versao,arquivar),invalida?400:409,q.getId(),invalida?"Informe uma versão válida do gerenciamento.":"Gerenciamento alterado por outro usuário. Atualize e tente novamente.");
    }
    static Stream<Arguments> versoes(){return Stream.of((Long)null,-1L,1L).flatMap(v->Stream.of(false,true).map(b->Arguments.of(v,b)));}
    @ParameterizedTest @ValueSource(booleans={false,true})
    void alvoExternoOuInexistenteRecusa(boolean arquivar) { // ARQ-12
        var u=usuario("Dono");var q=quadro(u);var p=demanda(etapa(q,"Origem",1));var outro=quadro(u);String t=token(u).substring(7);
        recusa(()->acao(t,outro.getId(),p.getId(),0L,arquivar),404,q.getId(),"Demanda não encontrada neste gerenciamento.");recusa(()->acao(t,q.getId(),999999L,0L,arquivar),404,q.getId(),"Demanda não encontrada neste gerenciamento.");
    }
    @ParameterizedTest @CsvSource({"processos,true","historicos,true","gerenciamentos,true","processos,false","historicos,false","gerenciamentos,false"})
    void falhaEmCadaUmaDasTresEscritasReverteTudo(String tabela,boolean arquivar) { // ARQ-15
        var u=usuario("Dono");var q=quadro(u);var p=demanda(etapa(q,"Origem",1));p.setArquivado(!arquivar);processos.saveAndFlush(p);var antes=conteudoPersistido();var quadroAntes=jdbc.queryForList("select * from gerenciamentos");String t=token(u).substring(7);
        String regra=tabela.equals("processos")?"arquivado = "+!arquivar:tabela.equals("historicos")?"acao <> '"+(arquivar?"ARQUIVAMENTO":"RESTAURACAO")+"'":"versao = 0";
        jdbc.execute("alter table "+tabela+" add constraint arquivo_falha check ("+regra+")");
        try{assertThatThrownBy(()->acao(t,q.getId(),p.getId(),0L,arquivar)).isInstanceOf(RuntimeException.class);}finally{jdbc.execute("alter table "+tabela+" drop constraint arquivo_falha");}
        assertThat(conteudoPersistido()).isEqualTo(antes);assertThat(jdbc.queryForList("select * from gerenciamentos")).isEqualTo(quadroAntes);
    }
    @Test void nullLegadoArquivaENumeroContinuaReservadoGlobalmente() { // ARQ-17/18
        var u=usuario("Dono");var q=quadro(u);var p=demanda(etapa(q,"Origem",1));p.setArquivado(null);p.setNumeroProcesso("Reserva-AbC");processos.saveAndFlush(p);String t=token(u).substring(7);
        service.arquivar(t,q.getId(),p.getId(),0L);var outro=quadro(u);etapa(outro,"Outra",1);
        recusa(()->service.criar(t,outro.getId(),new CriarDemandaRequest(0L,"reserva-aBC","Pessoa",null,null,null,null,null,null)),409,outro.getId(),"Já existe uma demanda com esse número.");
        assertThat(processos.findById(p.getId()).orElseThrow().isArquivado()).isTrue();
    }
}
