package com.patp.sistema;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import com.patp.sistema.model.Atividade;

@SpringBootTest @AutoConfigureMockMvc
class AtividadeDemandaCadastroTests extends AtividadeDemandaSupport {
    @ParameterizedTest @ValueSource(booleans={false,true})
    void cadastroCompletoNasDuasRotasTemValoresLiteraisAutorRealEVinculo(boolean legado) throws Exception {
        var dono=usuario("Dono");var autor=usuario("Leitor");var q=quadro(dono);var e=etapa(q,"Inicial",7);
        var dados=dados(legado,e.getId());dados.put("responsavel","  Ana  ");dados.put("prioridade","  Livre  ");
        dados.put("observacoes","a".repeat(9998)+"\nç");dados.put("dataEmissao","2026-10-12");dados.put("prazoEtapa","2026-10-10");dados.put("prazoGeral","2026-10-09");
        enviar(legado,q.getId(),token(autor),dados).andExpect(status().is(legado?200:201));var p=processos.findAll().get(0);
        var esperado=new ArrayList<>(List.of(campo(p,"numeroProcesso",null,"N-1"),campo(p,"pessoa",null,"Cliente"),campo(p,"responsavel",null,"Ana"),campo(p,"prioridade",null,"Livre"),
            campo(p,"dataEmissao",null,"2026-10-12"),campo(p,"prazoEtapa",null,"2026-10-10"),campo(p,"prazoGeral",null,"2026-10-09"),campo(p,"observacoes",null,"a".repeat(9998)+"\nç"),
            campo(p,"status",null,"Em andamento"),campo(p,"etapaId",null,e.getId().toString()),campo(p,"etapaNome",null,"Inicial"),campo(p,"arquivado",null,"false")));
        evento("DEMANDA_CRIADA",p,autor,"CRIACAO",esperado);
        assertThat(p.getObservacoes()).isEqualTo("a".repeat(9998)+"\nç");assertThat(quadros.findById(q.getId()).orElseThrow().getVersao()).isEqualTo(legado?0L:1L);
        var antigo=atividades.findAll().get(0);autor.setNome("Nome posterior");usuarios.saveAndFlush(autor);p.setNumeroProcesso("Futuro");processos.saveAndFlush(p);e.setNome("Futura");etapas.saveAndFlush(e);
        var preservado=atividades.findById(antigo.getId()).orElseThrow();assertThat(preservado.getAlvoNome()).isEqualTo("N-1");assertThat(preservado.getAutorNome()).isEqualTo("Leitor");assertThat(preservado.getAlteracoesJson()).isEqualTo(antigo.getAlteracoesJson());
    }
    @ParameterizedTest @ValueSource(booleans={false,true})
    void opcionaisNullOuVaziosNaoInventamMudancas(boolean legado) throws Exception {
        var u=usuario("Autor");var q=quadro(null);var e=etapa(q,"Primeira",1);var dados=dados(legado,e.getId());
        dados.put("responsavel","  ");dados.put("prioridade",null);dados.put("observacoes","\n\t");dados.put("dataEmissao",null);
        enviar(legado,q.getId(),token(u),dados).andExpect(status().is(legado?200:201));var p=processos.findAll().get(0);
        evento("DEMANDA_CRIADA",p,u,"CRIACAO",List.of(campo(p,"numeroProcesso",null,"N-1"),campo(p,"pessoa",null,"Cliente"),campo(p,"status",null,"Em andamento"),campo(p,"etapaId",null,e.getId().toString()),campo(p,"etapaNome",null,"Primeira"),campo(p,"arquivado",null,"false")));
        assertThat(p.getResponsavel()).isNull();assertThat(p.getPrioridade()).isNull();assertThat(p.getObservacoes()).isNull();assertThat(p.getDataEmissao()).isNull();assertThat(p.getDataConclusao()).isNull();assertThat(p.getDataCancelamento()).isNull();assertThat(p.getMotivoCancelamento()).isNull();
    }
    @ParameterizedTest @ValueSource(strings={"duplicado","arquivo","versao","sessao","invalido","semTrabalho","forjado"})
    void recusasConservamTodasAsTabelas(String caso) throws Exception {
        var u=usuario("Autor");var q=quadro(u);var e=etapa(q,"Primeira",1);var dados=dados(false,e.getId());
        if(caso.equals("duplicado")){var p=demanda(e);p.setNumeroProcesso("n-1");processos.saveAndFlush(p);}
        if(caso.equals("arquivo")){q.setArquivado(true);quadros.saveAndFlush(q);}
        if(caso.equals("versao"))dados.put("versao",99);
        if(caso.equals("invalido"))dados.put("pessoa","  ");
        if(caso.equals("forjado"))dados.put("autorNome","Forjado");
        if(caso.equals("semTrabalho")){e.setCategoria(com.patp.sistema.model.CategoriaEtapa.CONCLUIDA);etapas.saveAndFlush(e);}
        var antes=conteudoComAtividade();String mensagem=switch(caso){case "duplicado" -> "Já existe uma demanda com esse número.";case "arquivo" -> "Gerenciamento arquivado. Restaure-o antes de alterar.";case "versao" -> "Gerenciamento alterado por outro usuário. Atualize e tente novamente.";case "sessao" -> "Sessão inválida ou expirada.";case "invalido" -> "Informe um cliente/solicitante entre 1 e 255 caracteres.";case "forjado" -> "Dados da requisição inválidos.";default -> "Cadastre uma etapa de trabalho antes de criar demandas.";};
        enviar(false,q.getId(),caso.equals("sessao")?"Bearer invalida":token(u),dados).andExpect(status().is(caso.equals("sessao")?401:caso.equals("invalido")||caso.equals("forjado")?400:409)).andExpect(jsonPath("$.erro").value(mensagem));
        assertThat(conteudoComAtividade()).isEqualTo(antes);
    }
    @ParameterizedTest @ValueSource(booleans={false,true})
    void falhaDoDiarioReverteCadastroHistoricoEVersao(boolean legado) throws Exception {
        var u=usuario("Autor");var q=quadro(u);var e=etapa(q,"Primeira",1);var antes=conteudoComAtividade();
        doThrow(new IllegalStateException("Falha do diário")).when(atividades).saveAndFlush(any(Atividade.class));
        enviar(legado,q.getId(),token(u),dados(legado,e.getId())).andExpect(status().isInternalServerError());assertThat(conteudoComAtividade()).isEqualTo(antes);
    }
    private LinkedHashMap<String,Object> dados(boolean legado,Long etapa) {var d=new LinkedHashMap<String,Object>();d.put("numeroProcesso","  N-1  ");d.put("pessoa"," Cliente ");if(legado)d.put("etapa",Map.of("id",etapa));else d.put("versao",0);return d;}
    private org.springframework.test.web.servlet.ResultActions enviar(boolean legado,Long q,String bearer,Map<String,Object>d) throws Exception {return mvc.perform(post(legado?"/api/processos":base(q,null)).header("Authorization",bearer).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(d)));}
}
