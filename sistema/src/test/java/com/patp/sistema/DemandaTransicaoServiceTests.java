package com.patp.sistema;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import com.patp.sistema.exception.ApiException;
import com.patp.sistema.model.*;
import com.patp.sistema.service.ProcessoService;
import com.patp.sistema.service.ProcessoService.Acao;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class DemandaTransicaoServiceTests extends ApiIntegrationSupport {
    @Autowired ProcessoService service;
    @Autowired ObjectMapper json;

    private Etapa finalEtapa(Gerenciamento quadro, CategoriaEtapa categoria) {
        var e = etapa(quadro, categoria.name(), 99); e.setCategoria(categoria); return etapas.saveAndFlush(e);
    }
    private String sessao(Usuario usuario) { return token(usuario).substring(7); }
    private void erro(Runnable chamada, int status, Long quadroId) {
        var antes=conteudoPersistido(); var versao=quadros.findById(quadroId).orElseThrow().getVersao();
        assertThatThrownBy(chamada::run).isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.getStatus().value()).isEqualTo(status));
        assertThat(conteudoPersistido()).isEqualTo(antes);
        assertThat(quadros.findById(quadroId).orElseThrow().getVersao()).isEqualTo(versao);
    }

    @ParameterizedTest @EnumSource(Acao.class)
    void terceiroNaoPodeExecutarNenhumaAcao(Acao acao) {
        var dono=usuario("Dono"); var q=quadro(dono); var origem=etapa(q,"Origem",1); var destino=etapa(q,"Destino",2);
        var p=demanda(origem); if(acao==Acao.REABRIR) { p.setStatus("Cancelado"); processos.saveAndFlush(p); }
        String terceiro=sessao(usuario("Outro"));
        erro(() -> service.transicionar(terceiro,q.getId(),p.getId(),acao,destino.getId(),"Motivo",0L),403,q.getId());
    }

    @ParameterizedTest @EnumSource(Acao.class)
    void arquivoRecusaTodasAsAcoes(Acao acao) {
        var dono=usuario("Dono");var q=quadro(dono);var destino=etapa(q,"Destino",2);var p=demanda(etapa(q,"Origem",1));
        q.setArquivado(true);quadros.saveAndFlush(q);String s=sessao(dono);
        erro(() -> service.transicionar(s,q.getId(),p.getId(),acao,destino.getId(),"Motivo",q.getVersao()),409,q.getId());
    }

    @ParameterizedTest @EnumSource(Acao.class)
    void adminAdministraQuadroSemCriador(Acao acao) {
        var admin=usuario("Admin");admin.setPapel(PapelUsuario.ADMINISTRADOR);usuarios.saveAndFlush(admin);
        var q=quadro(null);var origem=etapa(q,"Origem",1);var destino=etapa(q,"Destino",2);
        finalEtapa(q,CategoriaEtapa.CONCLUIDA);finalEtapa(q,CategoriaEtapa.CANCELADA);var p=demanda(origem);
        if(acao==Acao.REABRIR) {p.setStatus("Concluido");processos.saveAndFlush(p);}
        var resposta=service.transicionar(sessao(admin),q.getId(),p.getId(),acao,destino.getId(),"Motivo",0L);
        assertThat(resposta.gerenciamento().versao()).isEqualTo(1L);
        assertThat(jdbc.queryForList("select usuario from historicos",String.class)).containsExactly("Admin");
        assertThat(resposta.demandas()).hasSize(1);
    }

    @Test void moverPulaEVoltaSemAlterarOutrosCampos() {
        var dono=usuario("Dono");var q=quadro(dono);var inicio=etapa(q,"Início",1);etapa(q,"Meio",2);var fim=etapa(q,"Fim",3);
        var p=demanda(inicio);p.setResponsavel("Responsável");p.setPrioridade("Urgente");p.setObservacoes("Observações");
        p.setDataEmissao(LocalDate.of(2020,1,2));p.setPrazoEtapa(LocalDate.of(2020,1,3));p.setPrazoGeral(LocalDate.of(2020,1,4));processos.saveAndFlush(p);
        String s=sessao(dono);
        var antes=LocalDateTime.now().minusSeconds(1);
        for(var destino:List.of(fim,inicio)) {
            long v=quadros.findById(q.getId()).orElseThrow().getVersao();
            var resposta=service.transicionar(s,q.getId(),p.getId(),Acao.MOVER,destino.getId(),null,v);
            var salvo=processos.findById(p.getId()).orElseThrow();
            assertThat(salvo.getEtapa().getId()).isEqualTo(destino.getId());assertThat(salvo.getStatus()).isEqualTo("Em andamento");
            assertThat(salvo).extracting("numeroProcesso","pessoa","responsavel","prioridade","observacoes","dataEmissao","prazoEtapa","prazoGeral","dataConclusao","dataCancelamento","motivoCancelamento")
                .containsExactly(p.getNumeroProcesso(),"Pessoa","Responsável","Urgente","Observações",p.getDataEmissao(),p.getPrazoEtapa(),p.getPrazoGeral(),null,null,null);
            assertThat(resposta.gerenciamento().versao()).isEqualTo(v+1);
            assertThat(resposta.demandas().get(0).etapaId()).isEqualTo(destino.getId());
            assertThat(resposta.etapas().stream().filter(e->e.id().equals(destino.getId())).findFirst().orElseThrow().quantidadeDemandas()).isEqualTo(1L);
        }
        assertThat(jdbc.queryForList("select acao from historicos order by id",String.class)).containsExactly("MUDANCA_ETAPA","MUDANCA_ETAPA");
        assertThat(jdbc.queryForList("select descricao from historicos order by id",String.class))
                .containsExactly("Etapa alterada de 'Início' para 'Fim'.", "Etapa alterada de 'Fim' para 'Início'.");
        assertThat(jdbc.queryForList("select usuario from historicos order by id",String.class)).containsExactly("Dono","Dono");
        assertThat(jdbc.queryForObject("select count(*) from historicos where processo_id=? and data_hora is not null",Long.class,p.getId())).isEqualTo(2L);
        for(var evento:jdbc.queryForList("select data_hora from historicos")) {
            assertThat(((java.sql.Timestamp)evento.get("data_hora")).toLocalDateTime()).isBetween(antes,LocalDateTime.now().plusSeconds(1));
        }
    }

    @ParameterizedTest @ValueSource(strings={"concluir","cancelar"})
    void encerrarTemDestinoDatasMotivoEventoEAutorCorretos(String acao) {
        var dono=usuario("Dono");var q=quadro(dono);var p=demanda(etapa(q,"Qualquer trabalho",5));
        var concluida=finalEtapa(q,CategoriaEtapa.CONCLUIDA);var cancelada=finalEtapa(q,CategoriaEtapa.CANCELADA);
        p.setDataConclusao(LocalDate.of(2000,1,1));p.setDataCancelamento(LocalDate.of(2001,1,1));p.setMotivoCancelamento("Antigo");processos.saveAndFlush(p);
        var antes=LocalDateTime.now().minusSeconds(1);boolean concluir=acao.equals("concluir");
        var r=service.transicionar(sessao(dono),q.getId(),p.getId(),concluir?Acao.CONCLUIR:Acao.CANCELAR,null,"  Solicitação\ncompleta  ",0L);
        var salvo=processos.findById(p.getId()).orElseThrow();
        assertThat(salvo.getStatus()).isEqualTo(concluir?"Concluido":"Cancelado");
        assertThat(salvo.getEtapa().getId()).isEqualTo(concluir?concluida.getId():cancelada.getId());
        assertThat(salvo.getDataConclusao()).isEqualTo(concluir?LocalDate.now():null);
        assertThat(salvo.getDataCancelamento()).isEqualTo(concluir?null:LocalDate.now());
        assertThat(salvo.getMotivoCancelamento()).isEqualTo(concluir?null:"Solicitação\ncompleta");
        var eventos=jdbc.queryForList("select * from historicos");assertThat(eventos).hasSize(1);
        assertThat(eventos.get(0).get("acao")).isEqualTo(concluir?"CONCLUSAO":"CANCELAMENTO");
        assertThat(eventos.get(0).get("usuario")).isEqualTo("Dono");
        assertThat(((java.sql.Timestamp)eventos.get(0).get("data_hora")).toLocalDateTime()).isBetween(antes,LocalDateTime.now().plusSeconds(1));
        assertThat(eventos.get(0).get("descricao")).isEqualTo(concluir?"Processo concluído.":"Processo cancelado. Motivo: Solicitação\ncompleta");
        assertThat(r.gerenciamento().versao()).isEqualTo(1L);
        assertThat(r.demandas().get(0).etapaId()).isEqualTo(salvo.getEtapa().getId());
    }

    @ParameterizedTest @ValueSource(strings={"Concluido","Cancelado"})
    void reabrirPreservaSnapshotAnteriorCompletoInclusiveMotivoLongo(String status) {
        var dono=usuario("Dono");var q=quadro(dono);var origem=finalEtapa(q,status.equals("Concluido")?CategoriaEtapa.CONCLUIDA:CategoriaEtapa.CANCELADA);
        var trabalho=etapa(q,"Retorno",2);var p=demanda(origem);p.setStatus(status);p.setDataConclusao(LocalDate.of(2020,2,29));p.setDataCancelamento(LocalDate.of(2021,3,4));
        String motivo="Legado com \"aspas\"\n"+"ç".repeat(20000);p.setMotivoCancelamento(motivo);p.setObservacoes("Preservada");processos.saveAndFlush(p);
        jdbc.update("insert into historicos(acao,descricao,data_hora,usuario,processo_id) values('ANTERIOR','Intacto',CURRENT_TIMESTAMP,'Antigo',?)",p.getId());
        var antigo=jdbc.queryForMap("select * from historicos");
        var antes=LocalDateTime.now().minusSeconds(1);
        service.transicionar(sessao(dono),q.getId(),p.getId(),Acao.REABRIR,trabalho.getId(),null,0L);
        var salvo=processos.findById(p.getId()).orElseThrow();
        assertThat(salvo.getStatus()).isEqualTo("Em andamento");assertThat(salvo.getEtapa().getId()).isEqualTo(trabalho.getId());
        assertThat(salvo.getDataConclusao()).isNull();assertThat(salvo.getDataCancelamento()).isNull();assertThat(salvo.getMotivoCancelamento()).isNull();
        assertThat(salvo.getObservacoes()).isEqualTo("Preservada");
        var eventos=jdbc.queryForList("select * from historicos order by id");assertThat(eventos).hasSize(2);assertThat(eventos.get(0)).isEqualTo(antigo);
        assertThat(eventos.get(1).get("acao")).isEqualTo("REABERTURA");assertThat(eventos.get(1).get("usuario")).isEqualTo("Dono");
        assertThat(((Number)eventos.get(1).get("processo_id")).longValue()).isEqualTo(p.getId());
        assertThat(((java.sql.Timestamp)eventos.get(1).get("data_hora")).toLocalDateTime()).isBetween(antes,LocalDateTime.now().plusSeconds(1));
        var dados=json.readTree(eventos.get(1).get("descricao").toString());
        assertThat(dados.get("statusAnterior").asText()).isEqualTo(status);
        assertThat(dados.get("etapaAnteriorId").asLong()).isEqualTo(origem.getId());assertThat(dados.get("etapaDestinoId").asLong()).isEqualTo(trabalho.getId());
        assertThat(dados.get("dataConclusao").asText()).isEqualTo("2020-02-29");assertThat(dados.get("dataCancelamento").asText()).isEqualTo("2021-03-04");
        assertThat(dados.get("motivoCancelamento").asText()).isEqualTo(motivo);
    }

    @ParameterizedTest @NullAndEmptySource @ValueSource(strings={" ","concluído","Pendente"})
    void statusDesconhecidoBloqueiaTodasAsAcoesSemNormalizacao(String status) {
        var dono=usuario("Dono");var q=quadro(dono);var trabalho=etapa(q,"Trabalho",1);var destino=etapa(q,"Destino",2);var p=demanda(trabalho);
        p.setStatus(status);processos.saveAndFlush(p);String s=sessao(dono);
        for(var acao:Acao.values()) erro(()->service.transicionar(s,q.getId(),p.getId(),acao,destino.getId(),"Motivo",0L),409,q.getId());
        assertThat(service.buscarPorId(p.getId()).getStatus()).isEqualTo(status);
    }

    @Test void rejeitaDestinosEEstadosSemEfeitos() {
        var dono=usuario("Dono");var q=quadro(dono);var trabalho=etapa(q,"Trabalho",1);var p=demanda(trabalho);String s=sessao(dono);
        var estrangeira=etapa(quadro(dono),"Outro quadro",1);var finalizada=finalEtapa(q,CategoriaEtapa.CONCLUIDA);
        erro(()->service.transicionar(s,q.getId(),p.getId(),Acao.MOVER,trabalho.getId(),null,0L),409,q.getId());
        erro(()->service.transicionar(s,q.getId(),p.getId(),Acao.MOVER,estrangeira.getId(),null,0L),404,q.getId());
        erro(()->service.transicionar(s,q.getId(),p.getId(),Acao.MOVER,finalizada.getId(),null,0L),400,q.getId());
        erro(()->service.transicionar(s,q.getId(),p.getId(),Acao.REABRIR,trabalho.getId(),null,0L),409,q.getId());
        for(String encerrado:List.of("Concluido","Cancelado")) {
            p.setStatus(encerrado);processos.saveAndFlush(p);
            for(var acao:List.of(Acao.MOVER,Acao.CONCLUIR,Acao.CANCELAR)) erro(()->service.transicionar(s,q.getId(),p.getId(),acao,trabalho.getId(),"Motivo",0L),409,q.getId());
            erro(()->service.transicionar(s,q.getId(),p.getId(),Acao.REABRIR,estrangeira.getId(),null,0L),404,q.getId());
            erro(()->service.transicionar(s,q.getId(),p.getId(),Acao.REABRIR,finalizada.getId(),null,0L),400,q.getId());
        }
    }

    @ParameterizedTest @NullAndEmptySource @ValueSource(strings={" ","\n\t"})
    void cancelarExigeMotivo(String motivo) {
        var dono=usuario("Dono");var q=quadro(dono);var p=demanda(etapa(q,"Trabalho",1));finalEtapa(q,CategoriaEtapa.CANCELADA);
        erro(()->service.transicionar(sessao(dono),q.getId(),p.getId(),Acao.CANCELAR,null,motivo,0L),400,q.getId());
    }

    @Test void limiteMotivoEAusenciaDuplicidadeFinal() {
        var dono=usuario("Dono");var q=quadro(dono);var p=demanda(etapa(q,"Trabalho",1));String s=sessao(dono);
        erro(()->service.transicionar(s,q.getId(),p.getId(),Acao.CONCLUIR,null,null,0L),409,q.getId());
        var finalizada=finalEtapa(q,CategoriaEtapa.CONCLUIDA);finalEtapa(q,CategoriaEtapa.CONCLUIDA);
        erro(()->service.transicionar(s,q.getId(),p.getId(),Acao.CONCLUIR,null,null,0L),409,q.getId());
        etapas.deleteById(finalizada.getId());finalEtapa(q,CategoriaEtapa.CANCELADA);
        erro(()->service.transicionar(s,q.getId(),p.getId(),Acao.CANCELAR,null,"a".repeat(10001),0L),400,q.getId());
        service.transicionar(s,q.getId(),p.getId(),Acao.CANCELAR,null,"a".repeat(10000),0L);
        assertThat(processos.findById(p.getId()).orElseThrow().getMotivoCancelamento()).isEqualTo("a".repeat(10000));
    }

    @Test void funcionarioNaoAdministraQuadroSemCriador() {
        var q=quadro(null);var trabalho=etapa(q,"Trabalho",1);var p=demanda(trabalho);String s=sessao(usuario("Outro"));
        for(var acao:Acao.values()) erro(()->service.transicionar(s,q.getId(),p.getId(),acao,trabalho.getId(),"Motivo",0L),403,q.getId());
    }

    @Test void andamentoEmColunaFinalExigeCorrecaoSemNormalizacao() {
        var dono=usuario("Dono");var q=quadro(dono);var finalizada=finalEtapa(q,CategoriaEtapa.CONCLUIDA);var trabalho=etapa(q,"Trabalho",1);var p=demanda(finalizada);String s=sessao(dono);
        for(var acao:List.of(Acao.MOVER,Acao.CONCLUIR,Acao.CANCELAR)) erro(()->service.transicionar(s,q.getId(),p.getId(),acao,trabalho.getId(),"Motivo",0L),409,q.getId());
        assertThat(service.buscarPorId(p.getId()).getEtapa().getId()).isEqualTo(finalizada.getId());
    }

    @Test void reaberturaPreservaAusenciasSemInventarDados() {
        var dono=usuario("Dono");var q=quadro(dono);var finalizada=finalEtapa(q,CategoriaEtapa.CONCLUIDA);var trabalho=etapa(q,"Trabalho",1);
        var p=demanda(finalizada);p.setStatus("Concluido");processos.saveAndFlush(p);
        service.transicionar(sessao(dono),q.getId(),p.getId(),Acao.REABRIR,trabalho.getId(),null,0L);
        var evento=jdbc.queryForMap("select * from historicos");var anterior=json.readTree(evento.get("descricao").toString());
        assertThat(anterior.get("dataConclusao").isNull()).isTrue();assertThat(anterior.get("dataCancelamento").isNull()).isTrue();
        assertThat(anterior.get("motivoCancelamento").isNull()).isTrue();assertThat(anterior.get("statusAnterior").asText()).isEqualTo("Concluido");
        assertThat(anterior.get("etapaAnteriorId").asLong()).isEqualTo(finalizada.getId());assertThat(anterior.get("etapaDestinoId").asLong()).isEqualTo(trabalho.getId());
        assertThat(processos.findById(p.getId()).orElseThrow()).extracting("status","dataConclusao","dataCancelamento","motivoCancelamento")
                .containsExactly("Em andamento",null,null,null);
    }

    @ParameterizedTest @ValueSource(strings={"status","etapa","dataConclusao","dataCancelamento","motivo"})
    void edicaoGenericaNaoContornaTransicao(String campo) {
        var dono=usuario("Dono");var q=quadro(dono);var origem=etapa(q,"Trabalho",1);var destino=etapa(q,"Destino",2);var p=demanda(origem);
        var dados=new Processo();dados.setNumeroProcesso("Alterada");dados.setPessoa("Outra");
        switch(campo) {
            case "status" -> dados.setStatus("Cancelado");case "etapa" -> dados.setEtapa(destino);
            case "dataConclusao" -> dados.setDataConclusao(LocalDate.now());case "dataCancelamento" -> dados.setDataCancelamento(LocalDate.now());
            default -> dados.setMotivoCancelamento("Forjado");
        }
        erro(()->service.editar(p.getId(),dados,sessao(dono)),400,q.getId());
    }

    @Test void edicaoDeOutrosCamposNaoLimpaStatusOuEtapaOmitidos() {
        var dono=usuario("Dono");var q=quadro(dono);var origem=etapa(q,"Trabalho",1);var p=demanda(origem);
        var dados=new Processo();dados.setNumeroProcesso("Alterada");dados.setPessoa("Outra");
        service.editar(p.getId(),dados,sessao(usuario("Colaborador")));
        var salvo=processos.findById(p.getId()).orElseThrow();assertThat(salvo.getStatus()).isEqualTo("Em andamento");
        assertThat(salvo.getEtapa().getId()).isEqualTo(origem.getId());assertThat(salvo.getNumeroProcesso()).isEqualTo("Alterada");
        assertThat(jdbc.queryForList("select acao from historicos",String.class)).containsExactly("EDICAO");
        assertThat(jdbc.queryForList("select usuario from historicos",String.class)).containsExactly("Colaborador");
    }

    @ParameterizedTest @ValueSource(strings={"processos","historicos","gerenciamentos"})
    void falhaEmCadaEscritaReverteTudo(String tabela) {
        var dono=usuario("Dono");var q=quadro(dono);var p=demanda(etapa(q,"Trabalho",1));finalEtapa(q,CategoriaEtapa.CONCLUIDA);
        String regra=switch(tabela){case "processos"->"status <> 'Concluido'";case "historicos"->"acao <> 'CONCLUSAO'";default->"versao = 0";};
        jdbc.execute("alter table "+tabela+" add constraint mov_falha check ("+regra+")");
        var antes=conteudoPersistido();
        try {
            assertThatThrownBy(()->service.transicionar(sessao(dono),q.getId(),p.getId(),Acao.CONCLUIR,null,null,0L)).isInstanceOf(RuntimeException.class);
            assertThat(conteudoPersistido()).isEqualTo(antes);assertThat(quadros.findById(q.getId()).orElseThrow().getVersao()).isZero();
        } finally {jdbc.execute("alter table "+tabela+" drop constraint mov_falha");}
    }
}
