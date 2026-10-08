package com.patp.sistema;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;

@SpringBootTest @AutoConfigureMockMvc
class DemandaEdicaoApiTests extends ApiIntegrationSupport {
    void erro(String caminho,String bearer,String corpo,int codigo,String mensagem,long quadro) throws Exception {
        var antes=conteudoPersistido();var versao=quadros.findById(quadro).orElseThrow().getVersao();
        var r=put(caminho).contentType(MediaType.APPLICATION_JSON).content(corpo);if(bearer!=null)r.header("Authorization",bearer);
        mvc.perform(r).andExpect(status().is(codigo)).andExpect(jsonPath("$.erro").value(mensagem));
        assertThat(conteudoPersistido()).isEqualTo(antes);assertThat(quadros.findById(quadro).orElseThrow().getVersao()).isEqualTo(versao);
    }
    String rota(long q,long p){return "/api/gerenciamentos/"+q+"/demandas/"+p;}
    @Test void sucessoDevolveSnapshotSemMudarNumeroOuContagens() throws Exception { // EDI-05/08/10/15/16/27
        var dono=usuario("Dono");var q=quadro(dono);var e=etapa(q,"Origem",1);var p=demanda(e);
        mvc.perform(put(rota(q.getId(),p.getId())).header("Authorization",token(dono)).contentType(MediaType.APPLICATION_JSON)
                .content("{\"versao\":0,\"pessoa\":\"  Nova  \",\"responsavel\":\"  Ana  \",\"prioridade\":null,\"dataEmissao\":\"2020-02-29\",\"prazoEtapa\":null,\"prazoGeral\":\"2019-01-01\",\"observacoes\":\"Texto\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.gerenciamento.id").value(q.getId())).andExpect(jsonPath("$.gerenciamento.versao").value(1))
                .andExpect(jsonPath("$.gerenciamento.podeAdministrar").value(true)).andExpect(jsonPath("$.demandas[0].id").value(p.getId()))
                .andExpect(jsonPath("$.demandas[0].numeroProcesso").value(p.getNumeroProcesso())).andExpect(jsonPath("$.demandas[0].pessoa").value("Nova"))
                .andExpect(jsonPath("$.demandas[0].responsavel").value("Ana")).andExpect(jsonPath("$.demandas[0].prioridade").isEmpty())
                .andExpect(jsonPath("$.demandas[0].dataEmissao").value("2020-02-29")).andExpect(jsonPath("$.demandas[0].prazoEtapa").isEmpty())
                .andExpect(jsonPath("$.demandas[0].prazoGeral").value("2019-01-01")).andExpect(jsonPath("$.demandas[0].observacoes").value("Texto"))
                .andExpect(jsonPath("$.demandas[0].status").value("Em andamento")).andExpect(jsonPath("$.demandas[0].etapaId").value(e.getId()))
                .andExpect(jsonPath("$.demandas[0].dataConclusao").isEmpty()).andExpect(jsonPath("$.demandas[0].dataCancelamento").isEmpty())
                .andExpect(jsonPath("$.demandas[0].motivoCancelamento").isEmpty()).andExpect(jsonPath("$.etapas[0].quantidadeDemandas").value(1))
                .andExpect(jsonPath("$..senha").doesNotExist());
        assertThat(processos.findById(p.getId()).orElseThrow().getPessoa()).isEqualTo("Nova");
        assertThat(jdbc.queryForList("select descricao from historicos",String.class)).containsExactly("Processo alterado.");
    }
    @ParameterizedTest @ValueSource(strings={"ausente","invalida","encerrada","terceiro","arquivo","externa","inexistente","versao","semversao","negativa","status"})
    void recusasHttpNaoAlteramNada(String caso) throws Exception { // EDI-01/02/03/11/13/14/29
        var dono=usuario("Dono");var q=quadro(dono);var p=demanda(etapa(q,"Origem",1));String bearer=token(dono);String caminho=rota(q.getId(),p.getId());String corpo="{\"versao\":0,\"pessoa\":\"Nova\"}";
        int codigo=409;String mensagem="Gerenciamento alterado por outro usuário. Atualize e tente novamente.";
        switch(caso){
            case "ausente" -> {bearer=null;codigo=401;mensagem="Usuário não autenticado.";}
            case "invalida" -> {bearer="Bearer invalida";codigo=401;mensagem="Sessão inválida ou expirada.";}
            case "encerrada" -> {sessoes.encerrarSessao(bearer.substring(7));codigo=401;mensagem="Sessão inválida ou expirada.";}
            case "terceiro" -> {bearer=token(usuario("Outro"));codigo=403;mensagem="Você não tem permissão para administrar este gerenciamento.";}
            case "arquivo" -> {q.setArquivado(true);quadros.saveAndFlush(q);mensagem="Gerenciamento arquivado. Restaure-o antes de alterar.";}
            case "externa" -> {caminho=rota(quadro(dono).getId(),p.getId());codigo=404;mensagem="Demanda não encontrada neste gerenciamento.";}
            case "inexistente" -> {caminho=rota(q.getId(),999999);codigo=404;mensagem="Demanda não encontrada neste gerenciamento.";}
            case "versao" -> corpo="{\"versao\":1,\"pessoa\":\"Nova\"}";
            case "semversao" -> {corpo="{\"pessoa\":\"Nova\"}";codigo=400;mensagem="Informe uma versão válida do gerenciamento.";}
            case "negativa" -> {corpo="{\"versao\":-1,\"pessoa\":\"Nova\"}";codigo=400;mensagem="Informe uma versão válida do gerenciamento.";}
            default -> {p.setStatus("Desconhecido");processos.saveAndFlush(p);mensagem="Status antigo não reconhecido. Solicite a correção do registro.";}
        }
        erro(caminho,bearer,corpo,codigo,mensagem,q.getId());
    }
    @ParameterizedTest @ValueSource(strings={"\"versao\":\"0\"","\"versao\":0.5","\"versao\":true","\"versao\":{}","\"versao\":[]","\"dataEmissao\":\"2023-02-29\"","\"prazoEtapa\":\"07/10/2026\"","\"prazoGeral\":12","\"numeroProcesso\":\"Novo\"","\"status\":\"Concluido\"","\"etapaId\":1","\"dataConclusao\":null","\"dataCancelamento\":null","\"motivoCancelamento\":null","\"usuario\":\"Falso\""})
    void jsonInvalidoOuCamposProtegidosRecebem400(String campo) throws Exception { // EDI-09/12/13/27
        var dono=usuario("Dono");var q=quadro(dono);var p=demanda(etapa(q,"Origem",1));
        String corpo="{\"pessoa\":\"Nova\","+(campo.startsWith("\"versao\"")?campo:"\"versao\":0,"+campo)+"}";
        erro(rota(q.getId(),p.getId()),token(dono),corpo,400,"Dados da requisição inválidos.",q.getId());
    }
    @ParameterizedTest @ValueSource(strings={"cliente","responsavel","prioridade","observacoes"})
    void limitesDaApiMantemPersistencia(String campo) throws Exception { // EDI-06/07
        var dono=usuario("Dono");var q=quadro(dono);var p=demanda(etapa(q,"Origem",1));
        String corpo="{\"versao\":0,\"pessoa\":\""+(campo.equals("cliente")?" ":"Nova")+"\""+(campo.equals("cliente")?"":",\""+campo+"\":\""+"a".repeat(campo.equals("observacoes")?10001:256)+"\"")+"}";
        String m=campo.equals("cliente")?"Informe um cliente/solicitante entre 1 e 255 caracteres.":campo.equals("responsavel")?"Informe um responsável com até 255 caracteres.":campo.equals("prioridade")?"Informe uma prioridade com até 255 caracteres.":"Informe observações com até 10000 caracteres.";
        erro(rota(q.getId(),p.getId()),token(dono),corpo,400,m,q.getId());
    }
}
