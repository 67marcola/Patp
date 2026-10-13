package com.patp.sistema;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import com.patp.sistema.model.Atividade;
import com.patp.sistema.model.CategoriaEtapa;
import com.patp.sistema.service.ProcessoService.Acao;

@SpringBootTest @AutoConfigureMockMvc
class AtividadeDemandaTransicaoTests extends AtividadeDemandaSupport {
    static Stream<Arguments> movimentos(){return Stream.of(false,true).flatMap(l->Stream.of(false,true).map(i->Arguments.of(l,i)));}
    @ParameterizedTest @MethodSource("movimentos")
    void movimentoPreservaIdsENomesDistintosOuIguaisNasDuasRotas(boolean legado,boolean iguais)throws Exception {
        var u=usuario("Dono");var q=quadro(u);var a=etapa(q,"Análise",1);var b=etapa(q,iguais?"Análise":"Execução",2);var p=demanda(a);
        enviar(legado,q.getId(),p.getId(),b.getId(),"mover",token(u),null).andExpect(status().isOk());
        evento("DEMANDA_MOVIDA",p,u,"MUDANCA_ETAPA",List.of(campo(p,"etapaId",a.getId().toString(),b.getId().toString()),campo(p,"etapaNome","Análise",iguais?"Análise":"Execução")));
        var salvo=processos.findById(p.getId()).orElseThrow();assertThat(salvo.getEtapa().getId()).isEqualTo(b.getId());assertThat(salvo.getStatus()).isEqualTo("Em andamento");assertThat(salvo.getDataConclusao()).isNull();assertThat(salvo.getDataCancelamento()).isNull();assertThat(salvo.getMotivoCancelamento()).isNull();assertThat(quadros.findById(q.getId()).orElseThrow().getVersao()).isEqualTo(1L);
    }
    static Stream<Arguments> encerramentos(){return Stream.of(false,true).flatMap(l->Stream.of(false,true).map(c->Arguments.of(l,c)));}
    @ParameterizedTest @MethodSource("encerramentos")
    void encerramentosRegistramTodasAlteracoesELimpamCicloAnterior(boolean legado,boolean concluir)throws Exception {
        var u=usuario("Dono");var q=quadro(u);var a=etapa(q,"Trabalho",1);var b=etapa(q,"Final",99);b.setCategoria(concluir?CategoriaEtapa.CONCLUIDA:CategoriaEtapa.CANCELADA);etapas.saveAndFlush(b);var p=demanda(a);
        p.setDataConclusao(LocalDate.of(2020,1,2));p.setDataCancelamento(LocalDate.of(2020,2,3));p.setMotivoCancelamento("Anterior");processos.saveAndFlush(p);
        enviar(legado,q.getId(),p.getId(),null,concluir?"concluir":"cancelar",token(u),"  Motivo real  ").andExpect(status().isOk());
        evento(concluir?"DEMANDA_CONCLUIDA":"DEMANDA_CANCELADA",p,u,concluir?"CONCLUSAO":"CANCELAMENTO",List.of(campo(p,"status","Em andamento",concluir?"Concluido":"Cancelado"),campo(p,"etapaId",a.getId().toString(),b.getId().toString()),campo(p,"etapaNome","Trabalho","Final"),campo(p,"dataConclusao","2020-01-02",concluir?LocalDate.now().toString():null),campo(p,"dataCancelamento","2020-02-03",concluir?null:LocalDate.now().toString()),campo(p,"motivoCancelamento","Anterior",concluir?null:"Motivo real")));
        var salvo=processos.findById(p.getId()).orElseThrow();assertThat(salvo.getStatus()).isEqualTo(concluir?"Concluido":"Cancelado");assertThat(salvo.getEtapa().getId()).isEqualTo(b.getId());assertThat(salvo.getDataConclusao()).isEqualTo(concluir?LocalDate.now():null);assertThat(salvo.getDataCancelamento()).isEqualTo(concluir?null:LocalDate.now());assertThat(salvo.getMotivoCancelamento()).isEqualTo(concluir?null:"Motivo real");assertThat(quadros.findById(q.getId()).orElseThrow().getVersao()).isEqualTo(1L);
    }
    @ParameterizedTest @ValueSource(strings={"Concluido","Cancelado"})
    void reaberturaPreservaHistoricoELimpaTodosCampos(String estado)throws Exception {
        var u=usuario("Dono");var q=quadro(u);var a=etapa(q,"Final",99);a.setCategoria(estado.equals("Concluido")?CategoriaEtapa.CONCLUIDA:CategoriaEtapa.CANCELADA);etapas.saveAndFlush(a);var b=etapa(q,"Trabalho",1);var p=demanda(a);p.setStatus(estado);p.setDataConclusao(LocalDate.of(2020,1,2));p.setDataCancelamento(LocalDate.of(2020,2,3));p.setMotivoCancelamento("Literal anterior");processos.saveAndFlush(p);
        enviar(false,q.getId(),p.getId(),b.getId(),"reabrir",token(u),null).andExpect(status().isOk());
        evento("DEMANDA_REABERTA",p,u,"REABERTURA",List.of(campo(p,"status",estado,"Em andamento"),campo(p,"etapaId",a.getId().toString(),b.getId().toString()),campo(p,"etapaNome","Final","Trabalho"),campo(p,"dataConclusao","2020-01-02",null),campo(p,"dataCancelamento","2020-02-03",null),campo(p,"motivoCancelamento","Literal anterior",null)));
        var salvo=processos.findById(p.getId()).orElseThrow();assertThat(salvo.getStatus()).isEqualTo("Em andamento");assertThat(salvo.getEtapa().getId()).isEqualTo(b.getId());assertThat(salvo.getDataConclusao()).isNull();assertThat(salvo.getDataCancelamento()).isNull();assertThat(salvo.getMotivoCancelamento()).isNull();assertThat(quadros.findById(q.getId()).orElseThrow().getVersao()).isEqualTo(1L);
        var h=json.readTree(jdbc.queryForObject("select descricao from historicos",String.class));assertThat(h.get("statusAnterior").asText()).isEqualTo(estado);assertThat(h.get("etapaAnteriorId").asLong()).isEqualTo(a.getId());assertThat(h.get("etapaDestinoId").asLong()).isEqualTo(b.getId());assertThat(h.get("dataConclusao").asText()).isEqualTo("2020-01-02");assertThat(h.get("dataCancelamento").asText()).isEqualTo("2020-02-03");assertThat(h.get("motivoCancelamento").asText()).isEqualTo("Literal anterior");
    }
    @ParameterizedTest @ValueSource(strings={"mesma","final","outro","destinoAusente","destinoNull","encerrada","reabrirAtiva","status","categoria","semFinal","duasFinais","motivo","quadroArquivo","demandaArquivo","versao","permissao","sessao","demandaAusente"})
    void todosRamosRecusadosConservamDiarioECiclo(String caso)throws Exception {
        var u=usuario("Dono");var q=quadro(u);var a=etapa(q,"Origem",1);var b=etapa(q,"Destino",2);var p=demanda(a);String acao="mover";Long destino=b.getId();String motivo=null;
        if(caso.equals("mesma"))destino=a.getId();if(caso.equals("final")){b.setCategoria(CategoriaEtapa.CANCELADA);etapas.saveAndFlush(b);}if(caso.equals("outro")){b.setGerenciamento(quadro(u));etapas.saveAndFlush(b);}if(caso.equals("destinoAusente"))destino=999999L;if(caso.equals("destinoNull"))destino=null;
        if(caso.equals("encerrada")){p.setStatus("Concluido");processos.saveAndFlush(p);}if(caso.equals("reabrirAtiva"))acao="reabrir";if(caso.equals("status")){p.setStatus("Antigo");processos.saveAndFlush(p);}if(caso.equals("categoria")){a.setCategoria(CategoriaEtapa.CONCLUIDA);etapas.saveAndFlush(a);}
        if(caso.equals("semFinal")||caso.equals("duasFinais")){acao="concluir";if(caso.equals("duasFinais")){a.setCategoria(CategoriaEtapa.CONCLUIDA);b.setCategoria(CategoriaEtapa.CONCLUIDA);etapas.saveAndFlush(a);etapas.saveAndFlush(b);p.setEtapa(etapa(q,"Trabalho",3));processos.saveAndFlush(p);}}
        if(caso.equals("motivo")){acao="cancelar";motivo="  ";}if(caso.equals("quadroArquivo")){q.setArquivado(true);quadros.saveAndFlush(q);}if(caso.equals("demandaArquivo")){p.setArquivado(true);processos.saveAndFlush(p);}
        var d=new java.util.LinkedHashMap<String,Object>();d.put("versao",caso.equals("versao")?99:0);if(acao.equals("mover")||acao.equals("reabrir"))d.put("etapaId",destino);if(acao.equals("cancelar"))d.put("motivo",motivo);
        String mensagem=switch(caso){case "mesma"->"A demanda já está nesta etapa.";case "final"->"Escolha uma etapa de trabalho; etapas finais usam as ações de concluir e cancelar.";case "outro","destinoAusente"->"Etapa não encontrada neste gerenciamento.";case "destinoNull"->"Informe uma etapa de trabalho válida.";case "encerrada","reabrirAtiva"->"Esta ação não é permitida no estado atual da demanda. Use a reabertura para iniciar um novo ciclo.";case "status"->"Status antigo não reconhecido. Solicite a correção do registro.";case "categoria"->"Estado da demanda incompatível com a etapa atual. Solicite a correção do registro.";case "semFinal","duasFinais"->"Prepare as etapas finais deste gerenciamento antes de encerrar demandas.";case "motivo"->"Informe um motivo de cancelamento entre 1 e 10000 caracteres.";case "quadroArquivo"->"Gerenciamento arquivado. Restaure-o antes de alterar.";case "demandaArquivo"->"Demanda arquivada. Restaure-a antes de alterar.";case "versao"->"Gerenciamento alterado por outro usuário. Atualize e tente novamente.";case "permissao"->"Você não tem permissão para administrar este gerenciamento.";case "sessao"->"Sessão inválida ou expirada.";default->"Demanda não encontrada neste gerenciamento.";};
        var autor=caso.equals("permissao")?usuario("Leitor"):u;var antes=conteudoComAtividade();int status=List.of("final","destinoNull","motivo").contains(caso)?400:List.of("outro","destinoAusente","demandaAusente").contains(caso)?404:caso.equals("permissao")?403:caso.equals("sessao")?401:409;
        mvc.perform(put(base(q.getId(),caso.equals("demandaAusente")?-1L:p.getId())+"/"+acao).header("Authorization",caso.equals("sessao")?"Bearer invalida":token(autor)).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(d))).andExpect(status().is(status)).andExpect(jsonPath("$.erro").value(mensagem));assertThat(conteudoComAtividade()).isEqualTo(antes);
    }
    @ParameterizedTest @EnumSource(Acao.class)
    void falhaReverteCadaTransicaoHistoricoEVersao(Acao acao)throws Exception {
        var u=usuario("Dono");var q=quadro(u);var a=etapa(q,"Inicial",1);var b=etapa(q,"Destino",2);var c=etapa(q,"Concluídos",99);c.setCategoria(CategoriaEtapa.CONCLUIDA);etapas.saveAndFlush(c);var d=etapa(q,"Cancelados",100);d.setCategoria(CategoriaEtapa.CANCELADA);etapas.saveAndFlush(d);var p=demanda(a);if(acao==Acao.REABRIR){p.setStatus("Cancelado");p.setEtapa(d);p.setDataCancelamento(LocalDate.of(2020,1,2));p.setMotivoCancelamento("Anterior");processos.saveAndFlush(p);}var antes=conteudoComAtividade();doThrow(new IllegalStateException("Falha do diário")).when(atividades).saveAndFlush(any(Atividade.class));
        enviar(false,q.getId(),p.getId(),b.getId(),acao.name().toLowerCase(),token(u),"Motivo").andExpect(status().isInternalServerError());assertThat(conteudoComAtividade()).isEqualTo(antes);
    }
    private org.springframework.test.web.servlet.ResultActions enviar(boolean legado,Long q,Long p,Long destino,String acao,String bearer,String motivo)throws Exception {
        String rota=legado?"/api/processos/"+p+(acao.equals("mover")?"/etapa/"+destino:"/"+acao):base(q,p)+"/"+acao;var d=new java.util.LinkedHashMap<String,Object>();if(!legado)d.put("versao",0);if(!legado&&(acao.equals("mover")||acao.equals("reabrir")))d.put("etapaId",destino);if(acao.equals("cancelar"))d.put("motivo",motivo);
        return mvc.perform(put(rota).header("Authorization",bearer).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(d)));
    }
}
