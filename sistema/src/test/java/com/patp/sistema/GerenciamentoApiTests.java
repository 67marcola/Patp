package com.patp.sistema;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;

import com.patp.sistema.model.Gerenciamento;
import com.patp.sistema.model.PapelUsuario;
import com.patp.sistema.model.Usuario;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class GerenciamentoApiTests extends ApiIntegrationSupport {
    @Autowired ObjectMapper json;
    private static final String NOME_ERRO = "Informe um nome entre 1 e 120 caracteres.";
    private static final String PERMISSAO_ERRO = "Você não tem permissão para administrar este gerenciamento.";
    private static final String ARQUIVO_ERRO = "Gerenciamento arquivado. Restaure-o antes de alterar.";
    private static final String VERSAO_ERRO = "Gerenciamento alterado por outro usuário. Atualize e tente novamente.";

    @ParameterizedTest
    @ValueSource(ints = {1, 120})
    void criaComLimitesNormalizaWhitespaceJSENaoAceitaAutoria(int tamanho) throws Exception { // GER-01/02/03/04/06/27
        Usuario criador = usuario("Criador");
        String nome = "A".repeat(tamanho);
        String corpo = json.writeValueAsString(Map.of("nome", "\ufeff\u00a0\t" + nome + "\r\n\u00a0", "descricao", "D".repeat(255), "criador", Map.of("id", 999), "arquivado", true));
        mvc.perform(post("/api/gerenciamentos").header("Authorization", token(criador)).contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value(nome))
                .andExpect(jsonPath("$.descricao").value("D".repeat(255)))
                .andExpect(jsonPath("$.criador.id").value(criador.getId()))
                .andExpect(jsonPath("$.criador.nome").value("Criador"))
                .andExpect(jsonPath("$.arquivado").value(false))
                .andExpect(jsonPath("$.versao").value(0))
                .andExpect(jsonPath("$.podeAdministrar").value(true))
                .andExpect(jsonPath("$..senha").doesNotExist());
        Gerenciamento salvo = quadros.findAll().get(0);
        assertThat(salvo.getNome()).isEqualTo(nome);
        assertThat(salvo.getCriador().getId()).isEqualTo(criador.getId());
        assertThat(salvo.isArquivado()).isFalse();
    }

    @Test
    void nomesIguaisCriamIdsEConteudosIndependentesSemDescricao() throws Exception { // GER-05/06
        String token = token(usuario("Criador"));
        for (int i = 0; i < 2; i++) {
            mvc.perform(post("/api/gerenciamentos").header("Authorization", token).contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"Mesmo\"}"))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.descricao").value(""));
        }
        var todos = quadros.findAll();
        assertThat(todos).hasSize(2);
        assertThat(todos.get(0).getId()).isNotEqualTo(todos.get(1).getId());
        etapa(todos.get(0), "Só primeiro", 1);
        assertThat(etapas.findByGerenciamentoIdOrderByOrdem(todos.get(0).getId())).hasSize(1);
        assertThat(etapas.findByGerenciamentoIdOrderByOrdem(todos.get(1).getId())).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " \t\u00a0\ufeff", "121", "emoji"})
    void recusaNomeInvalidoEContaUTF16(String caso) throws Exception { // GER-03
        String nome = switch (caso) { case "121" -> "A".repeat(121); case "emoji" -> "😀".repeat(61); default -> caso; };
        mvc.perform(post("/api/gerenciamentos").header("Authorization", token(usuario("Criador"))).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(Map.of("nome", nome))))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.erro").value(NOME_ERRO));
        assertThat(quadros.count()).isZero();
    }

    @Test
    void aceitaSessentaEmojisENomeNuloEhRecusado() throws Exception { // GER-03 UTF16
        String token = token(usuario("Criador"));
        mvc.perform(post("/api/gerenciamentos").header("Authorization", token).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(Map.of("nome", "😀".repeat(60)))))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.nome").value("😀".repeat(60)));
        mvc.perform(post("/api/gerenciamentos").header("Authorization", token).contentType(MediaType.APPLICATION_JSON).content("{\"nome\":null}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.erro").value(NOME_ERRO));
        assertThat(quadros.count()).isEqualTo(1);
    }

    @Test
    void recusaDescricao256SemInserir() throws Exception { // GER-04
        mvc.perform(post("/api/gerenciamentos").header("Authorization", token(usuario("Criador"))).contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("nome", "Quadro", "descricao", "D".repeat(256)))))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.erro").value("A descrição deve ter até 255 caracteres."));
        assertThat(quadros.count()).isZero();
    }

    @Test
    void etapaInvalidaRecusaCriacaoInteira() throws Exception { // GER-07
        mvc.perform(post("/api/gerenciamentos").header("Authorization", token(usuario("Criador"))).contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Quadro\",\"etapas\":[{\"nome\":\"Uma\",\"setor\":\"Engenharia\",\"ordem\":1},{\"nome\":\"Duas\",\"setor\":\"Engenharia\",\"ordem\":0}]}"))
                .andExpect(status().isBadRequest());
        assertThat(quadros.count()).isZero();
        assertThat(etapas.count()).isZero();
    }

    @Test
    void falhaRealNaSegundaEtapaDesfazQuadroEEtapas() throws Exception { // GER-07 falha parcial
        jdbc.execute("alter table etapas add constraint falha_etapa check (nome <> 'Falha')");
        try {
            mvc.perform(post("/api/gerenciamentos").header("Authorization", token(usuario("Criador"))).contentType(MediaType.APPLICATION_JSON)
                    .content("{\"nome\":\"Quadro\",\"etapas\":[{\"nome\":\"Uma\",\"setor\":\"Engenharia\",\"ordem\":1},{\"nome\":\"Falha\",\"setor\":\"Engenharia\",\"ordem\":2}]}"))
                    .andExpect(status().isInternalServerError()).andExpect(jsonPath("$.erro").value("Não foi possível concluir a operação."));
            assertThat(quadros.count()).isZero();
            assertThat(etapas.count()).isZero();
        } finally {
            jdbc.execute("alter table etapas drop constraint falha_etapa");
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"1.5", "\"1\"", "null", "-1", "true", "2147483648"})
    void ordemInicialExigeInteiroPositivoSemCriacaoParcial(String ordem) throws Exception { // GER-07 contrato etapas
        mvc.perform(post("/api/gerenciamentos").header("Authorization", token(usuario("Criador"))).contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Quadro\",\"etapas\":[{\"nome\":\"Uma\",\"setor\":\"Engenharia\",\"ordem\":" + ordem + "}]}"))
                .andExpect(status().isBadRequest());
        assertThat(quadros.count()).isZero();
        assertThat(etapas.count()).isZero();
    }

    @Test
    void consultaFiltrosLegadoEPermissaoPorUsuario() throws Exception { // GER-08/09/10/26/27
        Usuario criador = usuario("Criador");
        Usuario terceiro = usuario("Terceiro");
        Gerenciamento ativo = quadro(criador);
        Gerenciamento arquivo = quadro(null);
        arquivo.setArquivado(true);
        arquivo = quadros.saveAndFlush(arquivo);
        String token = token(terceiro);
        mvc.perform(get("/api/gerenciamentos").header("Authorization", token)).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1)).andExpect(jsonPath("$[0].id").value(ativo.getId())).andExpect(jsonPath("$[0].podeAdministrar").value(false));
        mvc.perform(get("/api/gerenciamentos?arquivado=true").header("Authorization", token)).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1)).andExpect(jsonPath("$[0].id").value(arquivo.getId())).andExpect(jsonPath("$[0].criador").isEmpty());
        mvc.perform(get("/api/gerenciamentos/" + arquivo.getId()).header("Authorization", token)).andExpect(status().isOk())
                .andExpect(jsonPath("$.arquivado").value(true)).andExpect(jsonPath("$.podeAdministrar").value(false));
        jdbc.update("update usuarios set papel='ADMINISTRADOR' where id=?", terceiro.getId());
        mvc.perform(get("/api/gerenciamentos/" + arquivo.getId()).header("Authorization", token)).andExpect(status().isOk())
                .andExpect(jsonPath("$.podeAdministrar").value(true));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/arquivar", "/restaurar"})
    void terceirosNaoAdministramQuadrosComOuSemCriador(String acao) throws Exception { // GER-13/14
        Usuario criador = usuario("Criador");
        String token = token(usuario("Terceiro"));
        for (Usuario dono : new Usuario[]{criador, null}) {
            Gerenciamento quadro = quadro(dono);
            mvc.perform(put("/api/gerenciamentos/" + quadro.getId() + acao).header("Authorization", token).contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"Alterado\",\"versao\":0}"))
                    .andExpect(status().isForbidden()).andExpect(jsonPath("$.erro").value(PERMISSAO_ERRO));
            Gerenciamento preservado = quadros.findById(quadro.getId()).orElseThrow();
            assertThat(preservado.getNome()).isEqualTo("Quadro");
            assertThat(preservado.isArquivado()).isFalse();
            assertThat(preservado.getVersao()).isZero();
        }
    }

    @Test
    void adminEditaQuadroSemCriadorESemInventarAutoria() throws Exception { // GER-12/14/15/26
        Usuario admin = usuario("Admin");
        admin.setPapel(PapelUsuario.ADMINISTRADOR);
        usuarios.saveAndFlush(admin);
        Gerenciamento legado = quadro(null);
        mvc.perform(put("/api/gerenciamentos/" + legado.getId()).header("Authorization", token(admin)).contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"Nome novo\",\"versao\":0}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.nome").value("Nome novo")).andExpect(jsonPath("$.criador").isEmpty()).andExpect(jsonPath("$.versao").value(1));
        assertThat(quadros.findById(legado.getId()).orElseThrow().getCriador()).isNull();
    }

    @Test
    void edicaoCicloERepeticaoPreservamTodoConteudo() throws Exception { // GER-12/15/17/18/19/20
        Usuario criador = usuario("Criador");
        Gerenciamento quadro = quadro(criador);
        var demanda = demanda(etapa(quadro, "Inicial", 1));
        jdbc.update("insert into comentarios(texto,funcionario,data_hora,processo_id) values('Comentário','Pessoa',CURRENT_TIMESTAMP,?)", demanda.getId());
        jdbc.update("insert into historicos(acao,descricao,data_hora,usuario,processo_id) values('CRIACAO','Histórico',CURRENT_TIMESTAMP,'Pessoa',?)", demanda.getId());
        var conteudo = conteudoPersistido();
        String token = token(criador);
        mvc.perform(put("/api/gerenciamentos/" + quadro.getId()).header("Authorization", token).contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\" Novo \u00a0\",\"descricao\":\"Editada\",\"versao\":0,\"criador\":{\"id\":999},\"arquivado\":true}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.nome").value("Novo")).andExpect(jsonPath("$.descricao").value("Editada"))
                .andExpect(jsonPath("$.versao").value(1)).andExpect(jsonPath("$.arquivado").value(false)).andExpect(jsonPath("$.criador.id").value(criador.getId()));
        assertThat(conteudoPersistido()).isEqualTo(conteudo);
        mvc.perform(put("/api/gerenciamentos/" + quadro.getId() + "/arquivar").header("Authorization", token).contentType(MediaType.APPLICATION_JSON).content("{\"versao\":1}"))
                .andExpect(status().isNoContent()).andExpect(content().string(""));
        assertThat(quadros.findById(quadro.getId()).orElseThrow().isArquivado()).isTrue();
        assertThat(quadros.findById(quadro.getId()).orElseThrow().getVersao()).isEqualTo(2L);
        mvc.perform(put("/api/gerenciamentos/" + quadro.getId() + "/arquivar").header("Authorization", token).contentType(MediaType.APPLICATION_JSON).content("{\"versao\":0}"))
                .andExpect(status().isNoContent());
        assertThat(quadros.findById(quadro.getId()).orElseThrow().getVersao()).isEqualTo(2L);
        mvc.perform(put("/api/gerenciamentos/" + quadro.getId() + "/restaurar").header("Authorization", token).contentType(MediaType.APPLICATION_JSON).content("{\"versao\":2}"))
                .andExpect(status().isNoContent());
        mvc.perform(put("/api/gerenciamentos/" + quadro.getId() + "/restaurar").header("Authorization", token).contentType(MediaType.APPLICATION_JSON).content("{\"versao\":0}"))
                .andExpect(status().isNoContent());
        Gerenciamento salvo = quadros.findById(quadro.getId()).orElseThrow();
        assertThat(salvo.isArquivado()).isFalse();
        assertThat(salvo.getVersao()).isEqualTo(3L);
        assertThat(salvo.getNome()).isEqualTo("Novo");
        assertThat(salvo.getDescricao()).isEqualTo("Editada");
        assertThat(salvo.getCriador().getId()).isEqualTo(criador.getId());
        assertThat(conteudoPersistido()).isEqualTo(conteudo);
    }

    @Test
    void edicaoIdenticaIncrementaVersaoEAntigaNaoSobrescreve() throws Exception { // GER-12/16
        Usuario criador = usuario("Criador");
        Gerenciamento quadro = quadro(criador);
        String token = token(criador);
        mvc.perform(put("/api/gerenciamentos/" + quadro.getId()).header("Authorization", token).contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"Quadro\",\"descricao\":\"Descrição\",\"versao\":0}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.versao").value(1));
        mvc.perform(put("/api/gerenciamentos/" + quadro.getId()).header("Authorization", token).contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"Perdido\",\"versao\":0}"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.erro").value(VERSAO_ERRO));
        assertThat(quadros.findById(quadro.getId()).orElseThrow().getNome()).isEqualTo("Quadro");
        assertThat(quadros.findById(quadro.getId()).orElseThrow().getVersao()).isEqualTo(1L);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/arquivar", "/restaurar"})
    void versaoAntigaEmOperacaoAplicavelRetorna409(String acao) throws Exception { // GER-16
        Usuario criador = usuario("Criador");
        Gerenciamento quadro = quadro(criador);
        quadro.setNome("Versão nova");
        quadro.setArquivado(acao.equals("/restaurar"));
        quadros.saveAndFlush(quadro);
        mvc.perform(put("/api/gerenciamentos/" + quadro.getId() + acao).header("Authorization", token(criador)).contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"Antigo\",\"versao\":0}"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.erro").value(VERSAO_ERRO));
        assertThat(quadros.findById(quadro.getId()).orElseThrow().getNome()).isEqualTo("Versão nova");
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"versao\":null}", "{\"versao\":-1}", "{\"versao\":0.0}", "{\"versao\":\"0\"}", "{\"versao\":true}", "{\"versao\":9223372036854775808}"})
    void versaoExigeInteiroNaoNegativoSemCoercao(String parte) throws Exception { // contrato versão GER-16/17/18/19
        Usuario criador = usuario("Criador");
        Gerenciamento quadro = quadro(criador);
        String token = token(criador);
        for (String acao : new String[]{"", "/arquivar", "/restaurar"}) {
            mvc.perform(put("/api/gerenciamentos/" + quadro.getId() + acao).header("Authorization", token).contentType(MediaType.APPLICATION_JSON).content(parte))
                    .andExpect(status().isBadRequest());
        }
        assertThat(quadros.findById(quadro.getId()).orElseThrow().isArquivado()).isFalse();
        assertThat(quadros.findById(quadro.getId()).orElseThrow().getVersao()).isZero();
    }

    @Test
    void permissaoPrecedeArquivoEArquivoPrecedeVersao() throws Exception { // GER-13/21 precedência
        Usuario criador = usuario("Criador");
        Gerenciamento quadro = quadro(criador);
        quadro.setArquivado(true);
        quadros.saveAndFlush(quadro);
        String rota = "/api/gerenciamentos/" + quadro.getId();
        mvc.perform(put(rota).header("Authorization", token(usuario("Terceiro"))).contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"Novo\",\"versao\":0}"))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.erro").value(PERMISSAO_ERRO));
        mvc.perform(put(rota).header("Authorization", token(criador)).contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"Novo\",\"versao\":0}"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.erro").value(ARQUIVO_ERRO));
        assertThat(quadros.findById(quadro.getId()).orElseThrow().getNome()).isEqualTo("Quadro");
    }

    @Test
    void edicaoAplicaLimitesDeNomeEDescricao() throws Exception { // GER-03/04/12
        Usuario criador = usuario("Criador");
        Gerenciamento quadro = quadro(criador);
        String token = token(criador);
        for (String nome : new String[]{"", "A".repeat(121)}) {
            mvc.perform(put("/api/gerenciamentos/" + quadro.getId()).header("Authorization", token).contentType(MediaType.APPLICATION_JSON)
                    .content(json.writeValueAsString(Map.of("nome", nome, "versao", 0))))
                    .andExpect(status().isBadRequest()).andExpect(jsonPath("$.erro").value(NOME_ERRO));
        }
        mvc.perform(put("/api/gerenciamentos/" + quadro.getId()).header("Authorization", token).contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("nome", "A", "descricao", "D".repeat(256), "versao", 0))))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.erro").value("A descrição deve ter até 255 caracteres."));
        assertThat(quadros.findById(quadro.getId()).orElseThrow().getVersao()).isZero();
    }

    @Test
    void inexistenciaAutenticacaoEFormatoTemStatusDefinidos() throws Exception { // GER-11/23 contratos JSON/filtro
        String token = token(usuario("Criador"));
        mvc.perform(get("/api/gerenciamentos/999999").header("Authorization", token)).andExpect(status().isNotFound()).andExpect(jsonPath("$.erro").value("Gerenciamento não encontrado."));
        for (String acao : new String[]{"", "/arquivar", "/restaurar"}) {
            mvc.perform(put("/api/gerenciamentos/999999" + acao).header("Authorization", token).contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"Quadro\",\"versao\":0}"))
                    .andExpect(status().isNotFound()).andExpect(jsonPath("$.erro").value("Gerenciamento não encontrado."));
            mvc.perform(put("/api/gerenciamentos/999999" + acao).contentType(MediaType.APPLICATION_JSON).content("{\"versao\":0}"))
                    .andExpect(status().isUnauthorized());
        }
        mvc.perform(post("/api/gerenciamentos").contentType(MediaType.APPLICATION_JSON).content("{}")) .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/gerenciamentos").header("Authorization", token).contentType(MediaType.APPLICATION_JSON).content("{")) .andExpect(status().isBadRequest());
        mvc.perform(get("/api/gerenciamentos?arquivado=talvez").header("Authorization", token)).andExpect(status().isBadRequest());
    }
}
