package com.patp.sistema;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import com.patp.sistema.model.*;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest @AutoConfigureMockMvc
class DemandaTransicaoApiTests extends ApiIntegrationSupport {
    @Autowired ObjectMapper json;
    record Ambiente(Usuario dono,Gerenciamento q,Etapa origem,Etapa destino,Etapa concluida,Etapa cancelada,Processo p,String token) {}
    private Ambiente preparar(String acao) {
        var dono=usuario("Dono");var q=quadro(dono);var origem=etapa(q,"Origem",1);var destino=etapa(q,"Destino",3);
        var concluida=etapa(q,"Concluídos",99);concluida.setCategoria(CategoriaEtapa.CONCLUIDA);etapas.saveAndFlush(concluida);
        var cancelada=etapa(q,"Cancelados",100);cancelada.setCategoria(CategoriaEtapa.CANCELADA);etapas.saveAndFlush(cancelada);
        var p=demanda(acao.equals("reabrir")?cancelada:origem);
        if(acao.equals("reabrir")) {p.setStatus("Cancelado");p.setDataCancelamento(LocalDate.of(2020,1,2));p.setMotivoCancelamento("Anterior");processos.saveAndFlush(p);}
        return new Ambiente(dono,q,origem,destino,concluida,cancelada,p,token(dono));
    }
    private String rota(Ambiente a,String acao) {return "/api/gerenciamentos/"+a.q.getId()+"/demandas/"+a.p.getId()+"/"+acao;}
    private Map<String,Object> corpo(Ambiente a,String acao) {
        var dados=new LinkedHashMap<String,Object>();dados.put("versao",0L);
        if(acao.equals("mover")||acao.equals("reabrir"))dados.put("etapaId",a.destino.getId());
        if(acao.equals("cancelar"))dados.put("motivo","  Solicitado  ");return dados;
    }
    private void recusa(Ambiente a,String rota,String token,Object dados,int codigo,String mensagem) throws Exception {
        var antes=conteudoPersistido();var versao=quadros.findById(a.q.getId()).orElseThrow().getVersao();
        var r=put(rota).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(dados));if(token!=null)r.header("Authorization",token);
        var result=mvc.perform(r).andExpect(status().is(codigo));if(mensagem!=null)result.andExpect(jsonPath("$.erro").value(mensagem));
        assertThat(conteudoPersistido()).isEqualTo(antes);assertThat(quadros.findById(a.q.getId()).orElseThrow().getVersao()).isEqualTo(versao);
    }

    @ParameterizedTest @ValueSource(strings={"mover","concluir","cancelar","reabrir"})
    void quatroRotasConfirmamEstadoEventoVersaoEContagens(String acao) throws Exception {
        var a=preparar(acao);long destino=switch(acao){case "concluir"->a.concluida.getId();case "cancelar"->a.cancelada.getId();default->a.destino.getId();};
        String estado=switch(acao){case "concluir"->"Concluido";case "cancelar"->"Cancelado";default->"Em andamento";};
        var result=mvc.perform(put(rota(a,acao)).header("Authorization",a.token).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(corpo(a,acao))))
            .andExpect(status().isOk()).andExpect(jsonPath("$.gerenciamento.id").value(a.q.getId())).andExpect(jsonPath("$.gerenciamento.versao").value(1))
            .andExpect(jsonPath("$.gerenciamento.podeAdministrar").value(true)).andExpect(jsonPath("$.gerenciamento.arquivado").value(false))
            .andExpect(jsonPath("$..senha").doesNotExist()).andReturn();
        var resposta=json.readTree(result.getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8));var demanda=resposta.get("demandas").get(0);
        assertThat(resposta.get("demandas").size()).isEqualTo(1);assertThat(demanda.size()).isEqualTo(14);
        assertThat(demanda.get("id").asLong()).isEqualTo(a.p.getId());assertThat(demanda.get("numeroProcesso").asText()).isEqualTo(a.p.getNumeroProcesso());
        assertThat(demanda.get("pessoa").asText()).isEqualTo("Pessoa");assertThat(demanda.get("status").asText()).isEqualTo(estado);assertThat(demanda.get("etapaId").asLong()).isEqualTo(destino);
        assertThat(demanda.get("dataConclusao").isNull()).isEqualTo(!acao.equals("concluir"));
        if(acao.equals("concluir"))assertThat(demanda.get("dataConclusao").asText()).isEqualTo(LocalDate.now().toString());
        assertThat(demanda.get("dataCancelamento").isNull()).isEqualTo(!acao.equals("cancelar"));
        if(acao.equals("cancelar"))assertThat(demanda.get("dataCancelamento").asText()).isEqualTo(LocalDate.now().toString());
        assertThat(demanda.get("motivoCancelamento").isNull()).isEqualTo(!acao.equals("cancelar"));
        if(acao.equals("cancelar"))assertThat(demanda.get("motivoCancelamento").asText()).isEqualTo("Solicitado");
        for(String campo:new String[]{"responsavel","prioridade","dataEmissao","prazoEtapa","prazoGeral","observacoes"})assertThat(demanda.get(campo).isNull()).isTrue();
        for(var etapa:resposta.get("etapas")) assertThat(etapa.get("quantidadeDemandas").asLong()).isEqualTo(etapa.get("id").asLong()==destino?1L:0L);
        assertThat(processos.findById(a.p.getId()).orElseThrow().getEtapa().getId()).isEqualTo(destino);
        assertThat(quadros.findById(a.q.getId()).orElseThrow().getVersao()).isEqualTo(1L);
        String evento=switch(acao){case "mover"->"MUDANCA_ETAPA";case "concluir"->"CONCLUSAO";case "cancelar"->"CANCELAMENTO";default->"REABERTURA";};
        assertThat(jdbc.queryForList("select acao from historicos",String.class)).containsExactly(evento);assertThat(jdbc.queryForList("select usuario from historicos",String.class)).containsExactly("Dono");
    }

    @ParameterizedTest @ValueSource(strings={"mover","concluir","cancelar","reabrir"})
    void authArquivoVersaoEDemandaExternaRecusados(String acao) throws Exception {
        var a=preparar(acao);var dados=corpo(a,acao);
        recusa(a,rota(a,acao),null,dados,401,null);recusa(a,rota(a,acao),"Bearer invalida",dados,401,null);
        String encerrada=token(a.dono);sessoes.encerrarSessao(encerrada.substring(7));recusa(a,rota(a,acao),encerrada,dados,401,null);
        recusa(a,rota(a,acao),token(usuario("Outro")),dados,403,"Você não tem permissão para administrar este gerenciamento.");
        dados.put("versao",9);recusa(a,rota(a,acao),a.token,dados,409,"Gerenciamento alterado por outro usuário. Atualize e tente novamente.");dados.put("versao",0);
        var estrangeira=demanda(etapa(quadro(a.dono),"Estrangeira",1));
        String base="/api/gerenciamentos/"+a.q.getId()+"/demandas/";
        recusa(a,base+estrangeira.getId()+"/"+acao,a.token,dados,404,"Demanda não encontrada neste gerenciamento.");
        recusa(a,base+999999+"/"+acao,a.token,dados,404,"Demanda não encontrada neste gerenciamento.");
        a.q.setArquivado(true);quadros.saveAndFlush(a.q);
        recusa(a,rota(a,acao),a.token,dados,409,"Gerenciamento arquivado. Restaure-o antes de alterar.");
    }

    @ParameterizedTest @ValueSource(strings={"mover","concluir","cancelar","reabrir"})
    void dtoEstritoRecusaVersoesInvalidasEPropriedadesExtras(String acao) throws Exception {
        var a=preparar(acao);var dados=corpo(a,acao);
        for(Object versao:new Object[]{null,-1,"0",0.5,true,Map.of("id",0)}) {
            dados.put("versao",versao);recusa(a,rota(a,acao),a.token,dados,400,null);
        }
        dados.remove("versao");recusa(a,rota(a,acao),a.token,dados,400,null);dados.put("versao",0);
        for(String extra:new String[]{"status","dataConclusao","autor","etapa","qualquer"}) {
            dados.put(extra,null);recusa(a,rota(a,acao),a.token,dados,400,"Dados da requisição inválidos.");dados.remove(extra);
        }
    }

    @ParameterizedTest @ValueSource(strings={"mover","reabrir"})
    void destinosInvalidosFinaisExternosEFormato(String acao) throws Exception {
        var a=preparar(acao);var dados=corpo(a,acao);
        for(Object id:new Object[]{null,-1,0,"1",1.5,true}) {dados.put("etapaId",id);recusa(a,rota(a,acao),a.token,dados,400,null);}
        dados.put("etapaId",999999);recusa(a,rota(a,acao),a.token,dados,404,"Etapa não encontrada neste gerenciamento.");
        var externo=etapa(quadro(a.dono),"Outro",1);dados.put("etapaId",externo.getId());recusa(a,rota(a,acao),a.token,dados,404,null);
        for(var fim:new Etapa[]{a.concluida,a.cancelada}) {dados.put("etapaId",fim.getId());recusa(a,rota(a,acao),a.token,dados,400,null);}
    }

    @Test void motivoInvalidoNoHttpNaoEConvertidoOuTruncado() throws Exception {
        var a=preparar("cancelar");var dados=corpo(a,"cancelar");
        for(Object motivo:new Object[]{null,""," \n ","x".repeat(10001),123,true,Map.of("texto","motivo")}) {
            dados.put("motivo",motivo);recusa(a,rota(a,"cancelar"),a.token,dados,400,null);
        }
    }

    @ParameterizedTest @ValueSource(strings={"mover","concluir","cancelar","reabrir"})
    void legadosDesconhecidosContinuamConsultaveisSemTransicoes(String acao) throws Exception {
        var a=preparar(acao);var dados=corpo(a,acao);
        for(String antigo:new String[]{null,"","Pendente antigo"}) {
            a.p.setStatus(antigo);processos.saveAndFlush(a.p);
            recusa(a,rota(a,acao),a.token,dados,409,"Status antigo não reconhecido. Solicite a correção do registro.");
            var consulta=mvc.perform(get("/api/processos/"+a.p.getId()).header("Authorization",a.token)).andExpect(status().isOk()).andReturn();
            var valor=json.readTree(consulta.getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8)).get("status");
            if(antigo==null)assertThat(valor.isNull()).isTrue();else assertThat(valor.asText()).isEqualTo(antigo);
        }
    }

    @ParameterizedTest @ValueSource(strings={"mover","concluir","cancelar","reabrir"})
    void estadoIncompativelRecusaNoHttp(String acao) throws Exception {
        var a=preparar(acao);var dados=corpo(a,acao);
        a.p.setStatus(acao.equals("reabrir")?"Em andamento":"Cancelado");processos.saveAndFlush(a.p);
        recusa(a,rota(a,acao),a.token,dados,409,null);
    }

    @Test void falhaDoHistoricoRetorna500EReverteNoHttp() throws Exception {
        var a=preparar("concluir");
        jdbc.execute("alter table historicos add constraint mov_api_falha check (acao <> 'CONCLUSAO')");
        try {recusa(a,rota(a,"concluir"),a.token,corpo(a,"concluir"),500,"Não foi possível concluir a operação.");}
        finally {jdbc.execute("alter table historicos drop constraint mov_api_falha");}
    }

    @ParameterizedTest @ValueSource(strings={"mover","concluir","cancelar"})
    void legadoNaoEscapaDaPermissaoNemIncrementaEmErro(String acao) throws Exception {
        var a=preparar(acao);String rota="/api/processos/"+a.p.getId()+"/"+(acao.equals("mover")?"etapa/"+a.destino.getId():acao);
        var dados=acao.equals("cancelar")?Map.of("motivo","Solicitado"):Map.of();
        recusa(a,rota,token(usuario("Outro")),dados,403,"Você não tem permissão para administrar este gerenciamento.");
        mvc.perform(put(rota).header("Authorization",a.token).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(dados)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(a.p.getId()));
        assertThat(quadros.findById(a.q.getId()).orElseThrow().getVersao()).isEqualTo(1L);
        assertThat(jdbc.queryForObject("select count(*) from historicos",Long.class)).isEqualTo(1L);
    }
}
