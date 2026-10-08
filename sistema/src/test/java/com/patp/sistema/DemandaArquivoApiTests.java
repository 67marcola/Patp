package com.patp.sistema;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import com.patp.sistema.dto.*;
import com.patp.sistema.model.*;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest @AutoConfigureMockMvc
class DemandaArquivoApiTests extends ApiIntegrationSupport {
    @Autowired ObjectMapper json;
    String rota(long q,long p,String acao){return "/api/gerenciamentos/"+q+"/demandas/"+p+"/"+acao;}
    void erro(String caminho,String bearer,String corpo,int codigo,String mensagem) throws Exception { // ARQ-01/02/03/07/08/09/10/11/12
        var antes=conteudoPersistido();var quadrosAntes=jdbc.queryForList("select * from gerenciamentos order by id");
        var r=put(caminho).contentType(MediaType.APPLICATION_JSON).content(corpo);if(bearer!=null)r.header("Authorization",bearer);
        mvc.perform(r).andExpect(status().is(codigo)).andExpect(jsonPath("$.erro").value(mensagem));
        assertThat(conteudoPersistido()).isEqualTo(antes);assertThat(jdbc.queryForList("select * from gerenciamentos order by id")).isEqualTo(quadrosAntes);
    }
    @ParameterizedTest @CsvSource({"Em andamento,false","Concluido,false","Cancelado,false","Em andamento,true","Concluido,true","Cancelado,true"})
    void duasRotasDevolvem200SnapshotCompletoNosTresEstados(String status,boolean admin) throws Exception { // ARQ-05/06/13/14/19/20
        var dono=usuario("Dono");var q=quadro(dono);var origem=etapa(q,"Origem",1);var e2=etapa(q,"Ativa",2);var e3=etapa(q,"Arquivo",3);var p=demanda(origem);var ativa=demanda(e2);var arquivo=demanda(e3);arquivo.setArquivado(true);processos.saveAndFlush(arquivo);
        p.setStatus(status);p.setPessoa("  Cliente antigo  ");p.setResponsavel("  Ana  ");p.setPrioridade("Alta");p.setDataEmissao(LocalDate.of(2020,2,29));p.setPrazoEtapa(LocalDate.of(2019,1,1));p.setPrazoGeral(LocalDate.of(2018,1,1));p.setDataConclusao(LocalDate.of(2021,2,3));p.setDataCancelamento(LocalDate.of(2022,3,4));p.setMotivoCancelamento("  Motivo\n literal  ");p.setObservacoes("  Observação\n literal  ");p.setArquivado(null);processos.saveAndFlush(p);
        var dto=DemandaResponse.de(p);var fisico=jdbc.queryForMap("select * from processos where id=?",p.getId());var restantes=jdbc.queryForList("select * from processos where id<>? order by id",p.getId());
        var autor=admin?usuario("Admin"):dono;if(admin){autor.setPapel(PapelUsuario.ADMINISTRADOR);usuarios.saveAndFlush(autor);}String bearer=token(autor);var inicio=LocalDateTime.now().minusSeconds(1);
        for(boolean arquivar:new boolean[]{true,false}){
            String acao=arquivar?"arquivar":"restaurar";long versao=arquivar?0L:1L;
            var resposta=mvc.perform(put(rota(q.getId(),p.getId(),acao)).header("Authorization",bearer).contentType(MediaType.APPLICATION_JSON).content("{\"versao\":"+versao+"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$..senha").doesNotExist()).andReturn().getResponse().getContentAsString();
            var r=json.readValue(resposta,ConfiguracaoEtapasResponse.class);
            assertThat(r.gerenciamento()).isEqualTo(new GerenciamentoResponse(q.getId(),q.getNome(),q.getDescricao(),new GerenciamentoResponse.CriadorResponse(dono.getId(),dono.getNome()),false,versao+1,true));
            assertThat(r.demandas()).containsExactlyElementsOf(arquivar?List.of(DemandaResponse.de(ativa)):List.of(dto,DemandaResponse.de(ativa)));
            assertThat(r.demandasArquivadas()).containsExactlyElementsOf(arquivar?List.of(dto,DemandaResponse.de(arquivo)):List.of(DemandaResponse.de(arquivo)));
            assertThat(r.etapas()).containsExactly(new EtapaResponse(origem.getId(),origem.getNome(),origem.getSetor(),1,arquivar?0L:1L,CategoriaEtapa.TRABALHO),new EtapaResponse(e2.getId(),e2.getNome(),e2.getSetor(),2,1L,CategoriaEtapa.TRABALHO),new EtapaResponse(e3.getId(),e3.getNome(),e3.getSetor(),3,0L,CategoriaEtapa.TRABALHO));
            var esperado=new java.util.LinkedHashMap<>(fisico);esperado.replaceAll((chave,valor)->chave.equalsIgnoreCase("arquivado")?arquivar:valor);
            assertThat(jdbc.queryForMap("select * from processos where id=?",p.getId())).isEqualTo(esperado);assertThat(jdbc.queryForList("select * from processos where id<>? order by id",p.getId())).isEqualTo(restantes);
            var consulta=mvc.perform(get("/api/gerenciamentos/"+q.getId()+"/estrutura-etapas").header("Authorization",bearer)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
            assertThat(json.readTree(resposta)).isEqualTo(json.readTree(consulta));
            assertThat(jdbc.queryForObject("select count(*) from historicos",Long.class)).isEqualTo(versao+1);
            assertThat(jdbc.queryForMap("select * from historicos where acao=?",arquivar?"ARQUIVAMENTO":"RESTAURACAO")).containsEntry("usuario",autor.getNome()).containsEntry("descricao",arquivar?"Demanda arquivada.":"Demanda restaurada.").containsEntry("processo_id",p.getId());
            assertThat(jdbc.queryForObject("select data_hora from historicos where acao=?",LocalDateTime.class,arquivar?"ARQUIVAMENTO":"RESTAURACAO")).isBetween(inicio,LocalDateTime.now().plusSeconds(1));
        }
    }
    @ParameterizedTest @MethodSource("recusas")
    void cadaRotaRecusaSessaoPermissaoQuadroAlvoVersaoERepeticaoSemEfeito(String acao,String caso) throws Exception { // ARQ-01/02/03/08/09/11/12
        var dono=usuario("Dono");var q=quadro(dono);var p=demanda(etapa(q,"Origem",1));p.setArquivado(acao.equals("restaurar"));processos.saveAndFlush(p);String bearer=token(dono),caminho=rota(q.getId(),p.getId(),acao),corpo="{\"versao\":0}";
        int codigo=409;String mensagem="Gerenciamento alterado por outro usuário. Atualize e tente novamente.";
        switch(caso){
            case "ausente"->{bearer=null;codigo=401;mensagem="Usuário não autenticado.";}
            case "invalida"->{bearer="Bearer invalida";codigo=401;mensagem="Sessão inválida ou expirada.";}
            case "encerrada"->{sessoes.encerrarSessao(bearer.substring(7));codigo=401;mensagem="Sessão inválida ou expirada.";}
            case "terceiro"->{bearer=token(usuario("Outro"));codigo=403;mensagem="Você não tem permissão para administrar este gerenciamento.";}
            case "quadroArquivo"->{q.setArquivado(true);quadros.saveAndFlush(q);mensagem="Gerenciamento arquivado. Restaure-o antes de alterar.";}
            case "externa"->{caminho=rota(quadro(dono).getId(),p.getId(),acao);codigo=404;mensagem="Demanda não encontrada neste gerenciamento.";}
            case "inexistente"->{caminho=rota(q.getId(),999999,acao);codigo=404;mensagem="Demanda não encontrada neste gerenciamento.";}
            case "antiga"->corpo="{\"versao\":1}";
            case "ausenteVersao"->{corpo="{}";codigo=400;mensagem="Informe uma versão válida do gerenciamento.";}
            case "nullVersao"->{corpo="{\"versao\":null}";codigo=400;mensagem="Informe uma versão válida do gerenciamento.";}
            case "negativa"->{corpo="{\"versao\":-1}";codigo=400;mensagem="Informe uma versão válida do gerenciamento.";}
            default->{p.setArquivado(acao.equals("arquivar"));processos.saveAndFlush(p);mensagem=acao.equals("arquivar")?"A demanda já está arquivada.":"A demanda já está ativa.";}
        }
        erro(caminho,bearer,corpo,codigo,mensagem);
    }
    static Stream<Arguments> recusas(){return Stream.of("arquivar","restaurar").flatMap(a->Stream.of("ausente","invalida","encerrada","terceiro","quadroArquivo","externa","inexistente","antiga","ausenteVersao","nullVersao","negativa","repeticao").map(c->Arguments.of(a,c)));}
    @ParameterizedTest @MethodSource("jsons")
    void cadaRotaRecusaJsonInvalidoTipoFracaoOverflowECampoExtra(String acao,String corpo) throws Exception { // ARQ-10
        var u=usuario("Dono");var q=quadro(u);var p=demanda(etapa(q,"Origem",1));p.setArquivado(acao.equals("restaurar"));processos.saveAndFlush(p);
        erro(rota(q.getId(),p.getId(),acao),token(u),corpo,400,"Dados da requisição inválidos.");
    }
    static Stream<Arguments> jsons(){return Stream.of("arquivar","restaurar").flatMap(a->Stream.of("{\"versao\":\"0\"}","{\"versao\":0.5}","{\"versao\":true}","{\"versao\":{}}","{\"versao\":[]}","{\"versao\":9223372036854775808}","{\"versao\":0e0}","{\"versao\":0,\"arquivado\":true}","{\"versao\":0,\"status\":\"Cancelado\"}","{\"versao\":0,\"etapaId\":1}","{\"versao\":0,\"usuario\":\"Forjado\"}","{\"versao\":0,\"outro\":null}","[]","null","{","").map(c->Arguments.of(a,c)));}
    @ParameterizedTest @MethodSource("statusInvalidos")
    void cadaRotaMantemStatusAntigoLiteral(String acao,String status) throws Exception { // ARQ-07
        var u=usuario("Dono");var q=quadro(u);var p=demanda(etapa(q,"Origem",1));p.setArquivado(acao.equals("restaurar"));p.setStatus(status);processos.saveAndFlush(p);
        erro(rota(q.getId(),p.getId(),acao),token(u),"{\"versao\":0}",409,"Status antigo não reconhecido. Solicite a correção do registro.");assertThat(processos.findById(p.getId()).orElseThrow().getStatus()).isEqualTo(status);
    }
    static Stream<Arguments> statusInvalidos(){return Stream.of("arquivar","restaurar").flatMap(a->Stream.of((String)null,"","  ","Concluído","Antigo").map(s->Arguments.of(a,s)));}
    @ParameterizedTest @CsvSource({"arquivar,false","arquivar,true","restaurar,false","restaurar,true"})
    void cadaRotaSemCriadorPermiteSomenteAdminSemAtribuirAutoria(String acao,boolean admin) throws Exception { // ARQ-04
        var u=usuario("Usuario");if(admin){u.setPapel(PapelUsuario.ADMINISTRADOR);usuarios.saveAndFlush(u);}var q=quadro(null);var p=demanda(etapa(q,"Origem",1));p.setArquivado(acao.equals("restaurar"));processos.saveAndFlush(p);
        if(admin){mvc.perform(put(rota(q.getId(),p.getId(),acao)).header("Authorization",token(u)).contentType(MediaType.APPLICATION_JSON).content("{\"versao\":0}")).andExpect(status().isOk()).andExpect(jsonPath("$.gerenciamento.versao").value(1)).andExpect(jsonPath("$.gerenciamento.criador").isEmpty()).andExpect(jsonPath("$.gerenciamento.podeAdministrar").value(true));assertThat(processos.findById(p.getId()).orElseThrow().isArquivado()).isEqualTo(acao.equals("arquivar"));}
        else erro(rota(q.getId(),p.getId(),acao),token(u),"{\"versao\":0}",403,"Você não tem permissão para administrar este gerenciamento.");
        assertThat(quadros.findById(q.getId()).orElseThrow().getCriador()).isNull();
    }
    @ParameterizedTest @ValueSource(strings={"criar","editar","mover","concluir","cancelar","reabrir"})
    void outrasMutacoesDevolvemArquivoCompletoPreservadoEContamSomenteAtivas(String acao) throws Exception { // ARQ-18/19/20
        var u=usuario("Dono");var q=quadro(u);var origem=etapa(q,"Origem",1);var destino=etapa(q,"Destino",2);var arquivoEtapa=etapa(q,"Arquivo",3);var concluida=etapa(q,"Concluídos",99);concluida.setCategoria(CategoriaEtapa.CONCLUIDA);etapas.saveAndFlush(concluida);var cancelada=etapa(q,"Cancelados",100);cancelada.setCategoria(CategoriaEtapa.CANCELADA);etapas.saveAndFlush(cancelada);
        var ativa=demanda(origem);var arquivo=demanda(arquivoEtapa);arquivo.setArquivado(true);arquivo.setObservacoes("  Arquivo literal\n  ");processos.saveAndFlush(arquivo);
        if(acao.equals("reabrir")){ativa.setStatus("Concluido");ativa.setEtapa(concluida);processos.saveAndFlush(ativa);}var dtoArquivo=DemandaResponse.de(arquivo);var fisicoArquivo=jdbc.queryForMap("select * from processos where id=?",arquivo.getId());
        String base="/api/gerenciamentos/"+q.getId()+"/demandas",url=base+"/"+ativa.getId();String corpo="{\"versao\":0}";
        var request=put(url);
        switch(acao){
            case "criar"->{request=post(base);corpo="{\"versao\":0,\"numeroProcesso\":\"NOVO\",\"pessoa\":\"Cliente\"}";}
            case "editar"->corpo="{\"versao\":0,\"pessoa\":\"Corrigida\"}";
            case "mover","reabrir"->{request=put(url+"/"+acao);corpo="{\"versao\":0,\"etapaId\":"+(acao.equals("mover")?destino.getId():origem.getId())+"}";}
            case "cancelar"->{request=put(url+"/cancelar");corpo="{\"versao\":0,\"motivo\":\"Pedido\"}";}
            default->request=put(url+"/concluir");
        }
        var resultado=mvc.perform(request.header("Authorization",token(u)).contentType(MediaType.APPLICATION_JSON).content(corpo)).andExpect(status().is(acao.equals("criar")?201:200)).andReturn().getResponse().getContentAsString();var r=json.readValue(resultado,ConfiguracaoEtapasResponse.class);
        assertThat(r.demandasArquivadas()).containsExactly(dtoArquivo);assertThat(jdbc.queryForMap("select * from processos where id=?",arquivo.getId())).isEqualTo(fisicoArquivo);
        assertThat(r.demandas()).hasSize(acao.equals("criar")?2:1);assertThat(r.demandas()).extracting("id").doesNotContain(arquivo.getId());assertThat(r.gerenciamento().versao()).isEqualTo(1L);
        long etapaAtiva=switch(acao){case "mover"->destino.getId();case "concluir"->concluida.getId();case "cancelar"->cancelada.getId();default->origem.getId();};
        for(var e:r.etapas())assertThat(e.quantidadeDemandas()).isEqualTo(e.id()==etapaAtiva?(acao.equals("criar")?2L:1L):0L);
        assertThat(r.demandas()).extracting("etapaId").containsOnly(etapaAtiva);
        if(acao.equals("criar")){assertThat(processos.findAll().stream().filter(p->p.getNumeroProcesso().equals("NOVO")).findFirst().orElseThrow().isArquivado()).isFalse();}
    }
    @Test void deleteEtapaComSomenteArquivoDevolve409SemEfeitos() throws Exception { // ARQ-21
        var u=usuario("Dono");var q=quadro(u);var e=etapa(q,"Origem",1);var p=demanda(e);p.setArquivado(true);processos.saveAndFlush(p);var antes=conteudoPersistido();var quadrosAntes=jdbc.queryForList("select * from gerenciamentos");
        mvc.perform(delete("/api/gerenciamentos/"+q.getId()+"/etapas/"+e.getId()).header("Authorization",token(u)).contentType(MediaType.APPLICATION_JSON).content("{\"versao\":0}"))
            .andExpect(status().isConflict()).andExpect(jsonPath("$.erro").value("Esta etapa possui demandas. Mova-as para outra etapa antes de removê-la."));
        assertThat(conteudoPersistido()).isEqualTo(antes);assertThat(jdbc.queryForList("select * from gerenciamentos")).isEqualTo(quadrosAntes);
    }
}
