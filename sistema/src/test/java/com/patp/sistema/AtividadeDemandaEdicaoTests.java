package com.patp.sistema;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import com.patp.sistema.model.Atividade;
import com.patp.sistema.model.Processo;

@SpringBootTest @AutoConfigureMockMvc
class AtividadeDemandaEdicaoTests extends AtividadeDemandaSupport {
    static Stream<Arguments> campos() {return Stream.of("pessoa","responsavel","prioridade","dataEmissao","prazoEtapa","prazoGeral","observacoes").flatMap(c->Stream.of(false,true).map(l->Arguments.of(c,l)));}
    @ParameterizedTest @MethodSource("campos")
    void cadaCampoNasDuasRotasTemAntesDepoisReal(String campo,boolean legado) throws Exception {
        var u=usuario("Dono");var q=quadro(u);var e=etapa(q,"Inicial",1);var p=completa(e);var d=dados(p,legado);
        String anterior=campo.equals("pessoa")?"Pessoa":campo.equals("observacoes")?"a".repeat(10000):campo.contains("prazo")||campo.equals("dataEmissao")?"2026-10-10":"Anterior";
        String novo=campo.equals("observacoes")?"ç".repeat(10000):campo.contains("prazo")||campo.equals("dataEmissao")?"2026-10-12":"Novo";d.put(campo,novo.length()==10000||novo.startsWith("2026")?novo:"  "+novo+"  ");
        enviar(legado,q.getId(),p.getId(),token(u),d).andExpect(status().isOk());evento("DEMANDA_EDITADA",p,u,"EDICAO",List.of(campo(p,campo,anterior,novo)));
        var salvo=processos.findById(p.getId()).orElseThrow();assertThat(salvo.getNumeroProcesso()).isEqualTo(p.getNumeroProcesso());assertThat(salvo.getStatus()).isEqualTo("Em andamento");assertThat(salvo.getEtapa().getId()).isEqualTo(e.getId());
        assertThat(quadros.findById(q.getId()).orElseThrow().getVersao()).isEqualTo(1L);
        assertThat(json.readTree(json.writeValueAsString(salvo)).get(campo).asText()).isEqualTo(novo);
    }
    @ParameterizedTest @ValueSource(booleans={false,true})
    void limpezaDosSeisOpcionaisRegistraNull(boolean legado) throws Exception {
        var u=usuario("Dono");var q=quadro(u);var p=completa(etapa(q,"Inicial",1));var d=dados(p,legado);
        for(String c:List.of("responsavel","prioridade","dataEmissao","prazoEtapa","prazoGeral","observacoes"))d.put(c,null);
        enviar(legado,q.getId(),p.getId(),token(u),d).andExpect(status().isOk());evento("DEMANDA_EDITADA",p,u,"EDICAO",List.of(campo(p,"responsavel","Anterior",null),campo(p,"prioridade","Anterior",null),campo(p,"dataEmissao","2026-10-10",null),campo(p,"prazoEtapa","2026-10-10",null),campo(p,"prazoGeral","2026-10-10",null),campo(p,"observacoes","a".repeat(10000),null)));
        var salvo=processos.findById(p.getId()).orElseThrow();assertThat(salvo.getResponsavel()).isNull();assertThat(salvo.getPrioridade()).isNull();assertThat(salvo.getDataEmissao()).isNull();assertThat(salvo.getPrazoEtapa()).isNull();assertThat(salvo.getPrazoGeral()).isNull();assertThat(salvo.getObservacoes()).isNull();
    }
    @ParameterizedTest @ValueSource(booleans={false,true})
    void identicaTemAlteracoesVaziasEIncrementoAtual(boolean legado) throws Exception {
        var u=usuario("Dono");var q=quadro(u);var p=completa(etapa(q,"Inicial",1));var antes=jdbc.queryForList("select * from processos");
        enviar(legado,q.getId(),p.getId(),token(u),dados(p,legado)).andExpect(status().isOk());evento("DEMANDA_EDITADA",p,u,"EDICAO",List.of());assertThat(jdbc.queryForList("select * from processos")).isEqualTo(antes);assertThat(quadros.findById(q.getId()).orElseThrow().getVersao()).isEqualTo(1L);
    }
    @ParameterizedTest @ValueSource(strings={"Concluido","Cancelado"})
    void encerramentoPermaneceIntegral(String estado) throws Exception {
        var u=usuario("Dono");var q=quadro(u);var e=etapa(q,"Final",99);e.setCategoria(estado.equals("Concluido")?com.patp.sistema.model.CategoriaEtapa.CONCLUIDA:com.patp.sistema.model.CategoriaEtapa.CANCELADA);etapas.saveAndFlush(e);
        var p=completa(e);p.setStatus(estado);p.setDataConclusao(LocalDate.of(2020,1,2));p.setDataCancelamento(LocalDate.of(2020,2,3));p.setMotivoCancelamento("Literal antigo");processos.saveAndFlush(p);var d=dados(p,false);d.put("pessoa","Corrigida");
        enviar(false,q.getId(),p.getId(),token(u),d).andExpect(status().isOk());evento("DEMANDA_EDITADA",p,u,"EDICAO",List.of(campo(p,"pessoa","Pessoa","Corrigida")));
        var salvo=processos.findById(p.getId()).orElseThrow();assertThat(salvo.getStatus()).isEqualTo(estado);assertThat(salvo.getEtapa().getId()).isEqualTo(e.getId());assertThat(salvo.getDataConclusao()).isEqualTo(LocalDate.of(2020,1,2));assertThat(salvo.getDataCancelamento()).isEqualTo(LocalDate.of(2020,2,3));assertThat(salvo.getMotivoCancelamento()).isEqualTo("Literal antigo");
    }
    @ParameterizedTest @ValueSource(strings={"numero","ciclo","arquivo","demandaArquivo","versao","permissao","sessao","invalido","status"})
    void recusasPreservamTodaPersistencia(String caso) throws Exception {
        var u=usuario("Dono");var q=quadro(u);var p=completa(etapa(q,"Inicial",1));boolean legado=caso.equals("numero")||caso.equals("ciclo");var d=dados(p,legado);
        if(caso.equals("numero"))d.put("numeroProcesso","Outro");if(caso.equals("ciclo"))d.put("status","Concluido");if(caso.equals("versao"))d.put("versao",99);if(caso.equals("invalido"))d.put("pessoa","  ");
        if(caso.equals("arquivo")){q.setArquivado(true);quadros.saveAndFlush(q);}if(caso.equals("demandaArquivo")){p.setArquivado(true);processos.saveAndFlush(p);}if(caso.equals("status")){p.setStatus("Antigo");processos.saveAndFlush(p);}
        var autor=caso.equals("permissao")?usuario("Leitor"):u;var antes=conteudoComAtividade();String mensagem=switch(caso){case "numero"->"O número da demanda não pode ser alterado após o cadastro.";case "ciclo"->"Use as ações de mover, concluir, cancelar ou reabrir para alterar o ciclo da demanda.";case "arquivo"->"Gerenciamento arquivado. Restaure-o antes de alterar.";case "demandaArquivo"->"Demanda arquivada. Restaure-a antes de alterar.";case "versao"->"Gerenciamento alterado por outro usuário. Atualize e tente novamente.";case "permissao"->"Você não tem permissão para administrar este gerenciamento.";case "sessao"->"Sessão inválida ou expirada.";case "invalido"->"Informe um cliente/solicitante entre 1 e 255 caracteres.";default->"Status antigo não reconhecido. Solicite a correção do registro.";};
        enviar(legado,q.getId(),p.getId(),caso.equals("sessao")?"Bearer invalida":token(autor),d).andExpect(status().is(caso.equals("numero")||caso.equals("ciclo")||caso.equals("invalido")?400:caso.equals("permissao")?403:caso.equals("sessao")?401:409)).andExpect(jsonPath("$.erro").value(mensagem));assertThat(conteudoComAtividade()).isEqualTo(antes);
    }
    @ParameterizedTest @ValueSource(booleans={false,true})
    void falhaReverteEdicaoHistoricoEVersao(boolean legado) throws Exception {
        var u=usuario("Dono");var q=quadro(u);var p=completa(etapa(q,"Inicial",1));var d=dados(p,legado);d.put("pessoa","Nova");var antes=conteudoComAtividade();doThrow(new IllegalStateException("Falha do diário")).when(atividades).saveAndFlush(any(Atividade.class));
        enviar(legado,q.getId(),p.getId(),token(u),d).andExpect(status().isInternalServerError());assertThat(conteudoComAtividade()).isEqualTo(antes);
    }
    private Processo completa(com.patp.sistema.model.Etapa e) {var p=demanda(e);p.setResponsavel("Anterior");p.setPrioridade("Anterior");p.setDataEmissao(LocalDate.of(2026,10,10));p.setPrazoEtapa(LocalDate.of(2026,10,10));p.setPrazoGeral(LocalDate.of(2026,10,10));p.setObservacoes("a".repeat(10000));return processos.saveAndFlush(p);}
    private Map<String,Object> dados(Processo p,boolean legado) {var d=new LinkedHashMap<String,Object>();d.put("pessoa",p.getPessoa());d.put("responsavel",p.getResponsavel());d.put("prioridade",p.getPrioridade());d.put("dataEmissao",p.getDataEmissao());d.put("prazoEtapa",p.getPrazoEtapa());d.put("prazoGeral",p.getPrazoGeral());d.put("observacoes",p.getObservacoes());if(!legado)d.put("versao",0);return d;}
    private org.springframework.test.web.servlet.ResultActions enviar(boolean legado,Long q,Long p,String bearer,Map<String,Object>d)throws Exception{return mvc.perform(put(legado?"/api/processos/"+p:base(q,p)).header("Authorization",bearer).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(d)));}
}
