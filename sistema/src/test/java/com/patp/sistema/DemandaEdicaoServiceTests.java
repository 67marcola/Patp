package com.patp.sistema;

import static org.assertj.core.api.Assertions.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import com.patp.sistema.dto.EditarDemandaRequest;
import com.patp.sistema.exception.ApiException;
import com.patp.sistema.model.CategoriaEtapa;
import com.patp.sistema.model.PapelUsuario;
import com.patp.sistema.service.ProcessoService;

@SpringBootTest @AutoConfigureMockMvc
class DemandaEdicaoServiceTests extends ApiIntegrationSupport {
    @Autowired ProcessoService service;
    EditarDemandaRequest dados(Long v,String pessoa,String responsavel,String prioridade,String observacoes) {
        return new EditarDemandaRequest(v,pessoa,responsavel,prioridade,LocalDate.of(2020,2,29),null,LocalDate.of(2019,1,1),observacoes);
    }
    void recusa(Runnable operacao,int codigo,long quadro,String mensagem) {
        var antes=conteudoPersistido();var versao=quadros.findById(quadro).orElseThrow().getVersao();
        assertThatThrownBy(operacao::run).isInstanceOfSatisfying(ApiException.class,e->{
            assertThat(e.getStatus().value()).isEqualTo(codigo); assertThat(e.getMessage()).isEqualTo(mensagem);
        });
        assertThat(conteudoPersistido()).isEqualTo(antes);
        assertThat(quadros.findById(quadro).orElseThrow().getVersao()).isEqualTo(versao);
    }
    @ParameterizedTest @CsvSource({"Em andamento,false","Concluido,false","Cancelado,false","Em andamento,true","Concluido,true","Cancelado,true"})
    void editaInformacoesPreservandoCicloERegistrosAnteriores(String status,boolean admin) { // EDI-05/08/10/15/16/27/28
        var dono=usuario("Dono");var q=quadro(dono);var e=etapa(q,"Origem",1);
        if(!status.equals("Em andamento")){ e.setCategoria(status.equals("Concluido")?CategoriaEtapa.CONCLUIDA:CategoriaEtapa.CANCELADA);etapas.saveAndFlush(e); }
        var p=demanda(e);p.setStatus(status);p.setDataConclusao(LocalDate.of(2020,2,29));p.setDataCancelamento(LocalDate.of(2021,3,4));p.setMotivoCancelamento("  Antigo\r\n"+"x".repeat(20000));processos.saveAndFlush(p);
        jdbc.update("insert into comentarios(texto,data_hora,funcionario,processo_id) values('Anterior',CURRENT_TIMESTAMP,'Dono',?)",p.getId());
        jdbc.update("insert into historicos(acao,descricao,data_hora,usuario,processo_id) values('CRIACAO','Anterior',CURRENT_TIMESTAMP,'Dono',?)",p.getId());
        var comentarios=jdbc.queryForList("select * from comentarios"); var historico=jdbc.queryForMap("select * from historicos");
        var autor=admin?usuario("Admin"):dono;if(admin){autor.setPapel(PapelUsuario.ADMINISTRADOR);usuarios.saveAndFlush(autor);}
        var antes=LocalDateTime.now().minusSeconds(1);
        var r=service.editarDados(token(autor).substring(7),q.getId(),p.getId(),dados(0L,"  Outro cliente  ","  Ana  ","  ","  Linha\ncompleta  "));
        var salvo=processos.findById(p.getId()).orElseThrow();
        assertThat(salvo).extracting("id","numeroProcesso","pessoa","responsavel","prioridade","dataEmissao","prazoEtapa","prazoGeral","observacoes")
                .containsExactly(p.getId(),p.getNumeroProcesso(),"Outro cliente","Ana",null,LocalDate.of(2020,2,29),null,LocalDate.of(2019,1,1),"Linha\ncompleta");
        assertThat(salvo).extracting("status","dataConclusao","dataCancelamento","motivoCancelamento")
                .containsExactly(status,p.getDataConclusao(),p.getDataCancelamento(),p.getMotivoCancelamento());
        assertThat(salvo.getEtapa().getId()).isEqualTo(e.getId());
        assertThat(r.gerenciamento().versao()).isEqualTo(1L); assertThat(r.demandas()).hasSize(1);
        assertThat(r.demandas().get(0).pessoa()).isEqualTo("Outro cliente");
        assertThat(r.etapas().get(0).quantidadeDemandas()).isEqualTo(1L);
        assertThat(jdbc.queryForList("select * from comentarios")).isEqualTo(comentarios);
        assertThat(jdbc.queryForMap("select * from historicos where id=?",historico.get("id"))).isEqualTo(historico);
        var evento=jdbc.queryForMap("select * from historicos where acao='EDICAO'");
        assertThat(evento).containsEntry("usuario",autor.getNome()).containsEntry("descricao","Processo alterado.").containsEntry("processo_id",p.getId());
        assertThat(jdbc.queryForObject("select data_hora from historicos where acao='EDICAO'",LocalDateTime.class)).isBetween(antes,LocalDateTime.now().plusSeconds(1));
        assertThat(jdbc.queryForObject("select count(*) from historicos",Long.class)).isEqualTo(2L);
    }
    @ParameterizedTest @ValueSource(booleans={false,true})
    void semCriadorSomenteAdmin(boolean admin) { // EDI-04
        var u=usuario("Leitor");if(admin){u.setPapel(PapelUsuario.ADMINISTRADOR);usuarios.saveAndFlush(u);}
        var q=quadro(null);var p=demanda(etapa(q,"Origem",1));var d=dados(0L,"Cliente",null,null,null);
        if(admin){assertThat(service.editarDados(token(u).substring(7),q.getId(),p.getId(),d).gerenciamento().versao()).isEqualTo(1L);}
        else recusa(()->service.editarDados(token(u).substring(7),q.getId(),p.getId(),d),403,q.getId(),"Você não tem permissão para administrar este gerenciamento.");
        assertThat(quadros.findById(q.getId()).orElseThrow().getCriador()).isNull();
    }
    @Test void terceiroEArquivoNaoAlteramDados() { // EDI-02/03; sessão HTTP em T4
        var dono=usuario("Dono");var q=quadro(dono);var p=demanda(etapa(q,"Origem",1));var d=dados(0L,"Cliente",null,null,null);
        recusa(()->service.editarDados(token(usuario("Outro")).substring(7),q.getId(),p.getId(),d),403,q.getId(),"Você não tem permissão para administrar este gerenciamento.");
        q.setArquivado(true);quadros.saveAndFlush(q);
        recusa(()->service.editarDados(token(dono).substring(7),q.getId(),p.getId(),d),409,q.getId(),"Gerenciamento arquivado. Restaure-o antes de alterar.");
    }
    @ParameterizedTest @NullAndEmptySource @ValueSource(strings={"  ","longo"})
    void clienteObrigatorioELimite(String pessoa) { // EDI-06
        var dono=usuario("Dono");var q=quadro(dono);var p=demanda(etapa(q,"Origem",1));String valor="longo".equals(pessoa)?"a".repeat(256):pessoa;
        recusa(()->service.editarDados(token(dono).substring(7),q.getId(),p.getId(),dados(0L,valor,null,null,null)),400,q.getId(),"Informe um cliente/solicitante entre 1 e 255 caracteres.");
    }
    @ParameterizedTest @ValueSource(strings={"responsavel","prioridade","observacoes"})
    void opcionaisRecusamExcessoEAceitamLimite(String campo) { // EDI-07
        var dono=usuario("Dono");var q=quadro(dono);var p=demanda(etapa(q,"Origem",1));String t=token(dono).substring(7);int n=campo.equals("observacoes")?10000:255;
        String mensagem=campo.equals("observacoes")?"Informe observações com até 10000 caracteres.":campo.equals("responsavel")?"Informe um responsável com até 255 caracteres.":"Informe uma prioridade com até 255 caracteres.";
        var rejeitado=dados(0L,"Cliente",campo.equals("responsavel")?"a".repeat(n+1):null,campo.equals("prioridade")?"a".repeat(n+1):null,campo.equals("observacoes")?"a".repeat(n+1):null);
        recusa(()->service.editarDados(t,q.getId(),p.getId(),rejeitado),400,q.getId(),mensagem);
        var aceito=dados(0L,"a".repeat(255),campo.equals("responsavel")?"a".repeat(n):null,campo.equals("prioridade")?"a".repeat(n):null,campo.equals("observacoes")?"a".repeat(n):null);
        service.editarDados(t,q.getId(),p.getId(),aceito);
        var salvo=processos.findById(p.getId()).orElseThrow();assertThat(salvo.getPessoa()).isEqualTo("a".repeat(255));
        assertThat(campo.equals("responsavel")?salvo.getResponsavel():campo.equals("prioridade")?salvo.getPrioridade():salvo.getObservacoes()).isEqualTo("a".repeat(n));
    }
    @Test void limiteUtf16ENullLimpamOpcionaisSemMudarCiclo() { // EDI-07/08
        var dono=usuario("Dono");var q=quadro(dono);var p=demanda(etapa(q,"Origem",1));String t=token(dono).substring(7);
        recusa(()->service.editarDados(t,q.getId(),p.getId(),dados(0L,"Cliente",null,null,"😀".repeat(5001))),400,q.getId(),"Informe observações com até 10000 caracteres.");
        service.editarDados(t,q.getId(),p.getId(),dados(0L,"Cliente",null,null,"😀".repeat(5000)));
        service.editarDados(t,q.getId(),p.getId(),new EditarDemandaRequest(1L,"Cliente",null,"  ",null,null,null,"  "));
        var salvo=processos.findById(p.getId()).orElseThrow();
        assertThat(salvo).extracting("responsavel","prioridade","dataEmissao","prazoEtapa","prazoGeral","observacoes").containsOnlyNulls();
        assertThat(salvo.getStatus()).isEqualTo("Em andamento");assertThat(salvo.getNumeroProcesso()).isEqualTo(p.getNumeroProcesso());
    }
    @ParameterizedTest @ValueSource(longs={-1,1,2})
    void versaoInvalidaOuAntiga(long v) { // EDI-13/14
        var dono=usuario("Dono");var q=quadro(dono);var p=demanda(etapa(q,"Origem",1));
        recusa(()->service.editarDados(token(dono).substring(7),q.getId(),p.getId(),dados(v,"Cliente",null,null,null)),v<0?400:409,q.getId(),v<0?"Informe uma versão válida do gerenciamento.":"Gerenciamento alterado por outro usuário. Atualize e tente novamente.");
    }
    @Test void versaoAusenteEDemandaExternaOuInexistente() { // EDI-11/13
        var dono=usuario("Dono");var q=quadro(dono);var p=demanda(etapa(q,"Origem",1));var outro=quadro(dono);
        recusa(()->service.editarDados(token(dono).substring(7),q.getId(),p.getId(),dados(null,"Cliente",null,null,null)),400,q.getId(),"Informe uma versão válida do gerenciamento.");
        recusa(()->service.editarDados(token(dono).substring(7),outro.getId(),p.getId(),dados(0L,"Cliente",null,null,null)),404,q.getId(),"Demanda não encontrada neste gerenciamento.");
        recusa(()->service.editarDados(token(dono).substring(7),q.getId(),999999L,dados(0L,"Cliente",null,null,null)),404,q.getId(),"Demanda não encontrada neste gerenciamento.");
    }
    @ParameterizedTest @NullAndEmptySource @ValueSource(strings={"Antigo"})
    void statusDesconhecidoPermaneceLiteral(String status) { // EDI-29
        var dono=usuario("Dono");var q=quadro(dono);var p=demanda(etapa(q,"Origem",1));p.setStatus(status);processos.saveAndFlush(p);
        recusa(()->service.editarDados(token(dono).substring(7),q.getId(),p.getId(),dados(0L,"Cliente",null,null,null)),409,q.getId(),"Status antigo não reconhecido. Solicite a correção do registro.");
        assertThat(processos.findById(p.getId()).orElseThrow().getStatus()).isEqualTo(status);
    }
    @ParameterizedTest @ValueSource(strings={"processos","historicos","gerenciamentos"})
    void falhaEmCadaEscritaReverteTudo(String tabela) { // EDI-17
        var dono=usuario("Dono");var q=quadro(dono);var p=demanda(etapa(q,"Origem",1));var antes=conteudoPersistido();String t=token(dono).substring(7);
        String regra=tabela.equals("processos")?"pessoa <> 'Bloqueada'":tabela.equals("historicos")?"acao <> 'EDICAO'":"versao = 0";
        jdbc.execute("alter table "+tabela+" add constraint edicao_falha check ("+regra+")");
        try {assertThatThrownBy(()->service.editarDados(t,q.getId(),p.getId(),dados(0L,"Bloqueada",null,null,null))).isInstanceOf(RuntimeException.class);}
        finally {jdbc.execute("alter table "+tabela+" drop constraint edicao_falha");}
        assertThat(conteudoPersistido()).isEqualTo(antes);assertThat(quadros.findById(q.getId()).orElseThrow().getVersao()).isZero();
    }
}
