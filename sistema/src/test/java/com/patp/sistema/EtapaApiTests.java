package com.patp.sistema;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.patp.sistema.model.PapelUsuario;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class EtapaApiTests extends ApiIntegrationSupport {
    @Autowired ObjectMapper json;
    private static final String PERMISSAO = "Você não tem permissão para administrar este gerenciamento.";
    private static final String ARQUIVO = "Gerenciamento arquivado. Restaure-o antes de alterar.";
    private static final String VERSAO = "Gerenciamento alterado por outro usuário. Atualize e tente novamente.";
    private static final String ETAPA = "Etapa não encontrada neste gerenciamento.";
    private static final String FORMATO = "Dados da requisição inválidos.";

    @ParameterizedTest
    @ValueSource(strings = {"criador", "terceiro", "admin", "legado", "arquivado"})
    void snapshotPreservaLegadoContaTodosStatusEDevolveMetadadosAtuais(String ator) throws Exception { // ETA-01/02/03
        var criador = usuario("Criador");
        var leitor = ator.equals("criador") ? criador : usuario("Leitor");
        if (ator.equals("admin")) { leitor.setPapel(PapelUsuario.ADMINISTRADOR); usuarios.saveAndFlush(leitor); }
        var quadro = quadro(ator.equals("legado") ? null : criador);
        if (ator.equals("arquivado")) { quadro.setArquivado(true); quadros.saveAndFlush(quadro); }
        var primeira = etapa(quadro, "Antiga", 7);
        var segunda = etapa(quadro, "Empatada", 7);
        jdbc.update("update etapas set setor=null where id=?", primeira.getId());
        for (String status : List.of("Em andamento", "Concluido", "Cancelado")) {
            var processo = demanda(segunda); processo.setNumeroProcesso("P-" + segunda.getId() + "-" + status);
            processo.setStatus(status); processos.saveAndFlush(processo);
        }
        var externo = etapa(quadro(criador), "Externa", 1); demanda(externo);
        var antes = estado();
        String bearer = token(leitor);
        mvc.perform(get("/api/gerenciamentos/" + quadro.getId() + "/estrutura-etapas").header("Authorization", bearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gerenciamento.id").value(quadro.getId()))
                .andExpect(jsonPath("$.gerenciamento.nome").value("Quadro"))
                .andExpect(jsonPath("$.gerenciamento.descricao").value("Descrição"))
                .andExpect(jsonPath("$.gerenciamento.arquivado").value(ator.equals("arquivado")))
                .andExpect(jsonPath("$.gerenciamento.versao").value(ator.equals("arquivado") ? 1 : 0))
                .andExpect(jsonPath("$.gerenciamento.podeAdministrar").value(ator.equals("criador") || ator.equals("admin")))
                .andExpect(jsonPath("$.etapas.length()").value(2))
                .andExpect(jsonPath("$.etapas[0].id").value(primeira.getId()))
                .andExpect(jsonPath("$.etapas[0].nome").value("Antiga"))
                .andExpect(jsonPath("$.etapas[0].setor").isEmpty())
                .andExpect(jsonPath("$.etapas[0].ordem").value(7))
                .andExpect(jsonPath("$.etapas[0].quantidadeDemandas").value(0))
                .andExpect(jsonPath("$.etapas[1].id").value(segunda.getId()))
                .andExpect(jsonPath("$.etapas[1].nome").value("Empatada"))
                .andExpect(jsonPath("$.etapas[1].setor").value("Engenharia"))
                .andExpect(jsonPath("$.etapas[1].ordem").value(7))
                .andExpect(jsonPath("$.etapas[1].quantidadeDemandas").value(3))
                .andExpect(jsonPath("$..senha").doesNotExist());
        mvc.perform(get("/api/gerenciamentos/" + quadro.getId() + "/estrutura-etapas").header("Authorization", bearer))
                .andExpect(jsonPath(ator.equals("legado") ? "$.gerenciamento.criador" : "$.gerenciamento.criador.id")
                        .value(ator.equals("legado") ? null : criador.getId()));
        if (!ator.equals("legado")) {
            mvc.perform(get("/api/gerenciamentos/" + quadro.getId() + "/estrutura-etapas").header("Authorization", bearer))
                    .andExpect(jsonPath("$.gerenciamento.criador.nome").value("Criador"));
        }
        mvc.perform(get(base(quadro.getId())).header("Authorization", bearer)).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2)).andExpect(jsonPath("$[0].id").value(primeira.getId()))
                .andExpect(jsonPath("$[0].setor").isEmpty()).andExpect(jsonPath("$[0].ordem").value(7))
                .andExpect(jsonPath("$[1].id").value(segunda.getId())).andExpect(jsonPath("$[1].ordem").value(7));
        assertThat(estado()).isEqualTo(antes);
    }

    @ParameterizedTest
    @ValueSource(strings = {"criador", "admin", "adminLegado"})
    void cicloCrudPosicoesNormalizaLegadoPreservaConteudoEUsaIdsGerados(String ator) throws Exception { // ETA-04/12/13/14/15/19
        var criador = usuario("Criador");
        var escritor = ator.equals("criador") ? criador : usuario("Admin");
        if (!ator.equals("criador")) { escritor.setPapel(PapelUsuario.ADMINISTRADOR); usuarios.saveAndFlush(escritor); }
        var quadro = quadro(ator.equals("adminLegado") ? null : criador);
        var primeira = etapa(quadro, "Mesmo", 8);
        var ultima = etapa(quadro, "Legada", 8);
        jdbc.update("update etapas set setor=null where id=?", ultima.getId());
        var processo = demanda(primeira);
        jdbc.update("insert into historicos(processo_id,acao,descricao,usuario,data_hora) values(?,?,?,?,current_timestamp)",
                processo.getId(), "MUDANCA_ETAPA", "Nome antigo preservado.", "Pessoa");
        jdbc.update("insert into comentarios(processo_id,funcionario,texto,data_hora) values(?,?,?,current_timestamp)",
                processo.getId(), "Criador", "Comentário preservado.");
        var conteudo = somenteDemandas();
        String bearer = token(escritor);
        var criado = mvc.perform(post(base(quadro.getId())).header("Authorization", bearer).contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("nome", "Mesmo", "setor", "Operação", "ordem", 2, "versao", 0,
                        "id", primeira.getId(), "gerenciamento", Map.of("id", 9999), "criador", Map.of("id", 9999)))))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.gerenciamento.versao").value(1))
                .andExpect(jsonPath("$.gerenciamento.id").value(quadro.getId())).andExpect(jsonPath("$.gerenciamento.nome").value("Quadro"))
                .andExpect(jsonPath("$.gerenciamento.descricao").value("Descrição")).andExpect(jsonPath("$.gerenciamento.arquivado").value(false))
                .andExpect(jsonPath(ator.equals("adminLegado") ? "$.gerenciamento.criador" : "$.gerenciamento.criador.id")
                        .value(ator.equals("adminLegado") ? null : criador.getId()))
                .andExpect(jsonPath("$.gerenciamento.podeAdministrar").value(true))
                .andExpect(jsonPath("$.etapas.length()").value(3))
                .andExpect(jsonPath("$.etapas[0].id").value(primeira.getId())).andExpect(jsonPath("$.etapas[0].ordem").value(1))
                .andExpect(jsonPath("$.etapas[0].quantidadeDemandas").value(1))
                .andExpect(jsonPath("$.etapas[1].nome").value("Mesmo")).andExpect(jsonPath("$.etapas[1].setor").value("Operação"))
                .andExpect(jsonPath("$.etapas[1].ordem").value(2)).andExpect(jsonPath("$.etapas[1].quantidadeDemandas").value(0))
                .andExpect(jsonPath("$.etapas[2].id").value(ultima.getId())).andExpect(jsonPath("$.etapas[2].nome").value("Legada"))
                .andExpect(jsonPath("$.etapas[2].setor").isEmpty()).andExpect(jsonPath("$.etapas[2].quantidadeDemandas").value(0))
                .andExpect(jsonPath("$.etapas[2].ordem").value(3)).andReturn();
        long novoId = json.readTree(criado.getResponse().getContentAsString()).get("etapas").get(1).get("id").asLong();
        assertThat(novoId).isNotEqualTo(primeira.getId()).isNotEqualTo(ultima.getId());
        assertThat(etapas.findById(novoId).orElseThrow().getGerenciamento().getId()).isEqualTo(quadro.getId());
        // Editing an occupied stage must preserve every demand/comment/history field.
        mvc.perform(put(base(quadro.getId()) + "/" + primeira.getId()).header("Authorization", bearer).contentType(MediaType.APPLICATION_JSON)
                .content(corpo("Renomeada", "Projeto", 3, 1)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.gerenciamento.versao").value(2))
                .andExpect(jsonPath("$.gerenciamento.id").value(quadro.getId())).andExpect(jsonPath("$.gerenciamento.nome").value("Quadro"))
                .andExpect(jsonPath("$.gerenciamento.descricao").value("Descrição")).andExpect(jsonPath("$.gerenciamento.arquivado").value(false))
                .andExpect(jsonPath("$.gerenciamento.podeAdministrar").value(true))
                .andExpect(jsonPath(ator.equals("adminLegado") ? "$.gerenciamento.criador" : "$.gerenciamento.criador.id")
                        .value(ator.equals("adminLegado") ? null : criador.getId()))
                .andExpect(jsonPath("$.etapas[0].id").value(novoId)).andExpect(jsonPath("$.etapas[0].nome").value("Mesmo"))
                .andExpect(jsonPath("$.etapas[0].ordem").value(1))
                .andExpect(jsonPath("$.etapas[1].id").value(ultima.getId())).andExpect(jsonPath("$.etapas[1].ordem").value(2))
                .andExpect(jsonPath("$.etapas[2].id").value(primeira.getId())).andExpect(jsonPath("$.etapas[2].nome").value("Renomeada"))
                .andExpect(jsonPath("$.etapas[2].setor").value("Projeto")).andExpect(jsonPath("$.etapas[2].ordem").value(3))
                .andExpect(jsonPath("$.etapas[2].quantidadeDemandas").value(1));
        mvc.perform(delete(base(quadro.getId()) + "/" + novoId).header("Authorization", bearer).contentType(MediaType.APPLICATION_JSON)
                .content("{\"versao\":2}"))
                .andExpect(status().isOk()).andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.gerenciamento.id").value(quadro.getId())).andExpect(jsonPath("$.gerenciamento.versao").value(3))
                .andExpect(jsonPath("$.gerenciamento.nome").value("Quadro")).andExpect(jsonPath("$.gerenciamento.descricao").value("Descrição"))
                .andExpect(jsonPath("$.gerenciamento.arquivado").value(false)).andExpect(jsonPath("$.gerenciamento.podeAdministrar").value(true))
                .andExpect(jsonPath(ator.equals("adminLegado") ? "$.gerenciamento.criador" : "$.gerenciamento.criador.id")
                        .value(ator.equals("adminLegado") ? null : criador.getId()))
                .andExpect(jsonPath("$.etapas.length()").value(2))
                .andExpect(jsonPath("$.etapas[0].id").value(ultima.getId())).andExpect(jsonPath("$.etapas[0].ordem").value(1))
                .andExpect(jsonPath("$.etapas[0].nome").value("Legada")).andExpect(jsonPath("$.etapas[0].setor").isEmpty())
                .andExpect(jsonPath("$.etapas[0].quantidadeDemandas").value(0))
                .andExpect(jsonPath("$.etapas[1].id").value(primeira.getId())).andExpect(jsonPath("$.etapas[1].ordem").value(2))
                .andExpect(jsonPath("$.etapas[1].nome").value("Renomeada")).andExpect(jsonPath("$.etapas[1].setor").value("Projeto"))
                .andExpect(jsonPath("$.etapas[1].quantidadeDemandas").value(1));
        assertThat(etapas.existsById(novoId)).isFalse();
        assertThat(somenteDemandas()).isEqualTo(conteudo);
        assertThat(jdbc.queryForMap("select nome,descricao,criador_id,arquivado,versao from gerenciamentos where id=?", quadro.getId()))
                .containsEntry("nome", "Quadro").containsEntry("descricao", "Descrição").containsEntry("versao", 3L)
                .containsEntry("criador_id", ator.equals("adminLegado") ? null : criador.getId()).containsEntry("arquivado", false);
    }

    @Test
    void consultasOrdenamAntesPorOrdemMesmoQuandoIdDaEtapaEhMaior() throws Exception { // ETA-02 prioridade ordem e legado readonly
        var criador = usuario("Criador"); var quadro = quadro(criador);
        var antiga = etapa(quadro, "Antiga", 9); var nova = etapa(quadro, "Nova", 2); var antes = estado(); String bearer = token(criador);
        mvc.perform(get(base(quadro.getId())).header("Authorization", bearer)).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(nova.getId())).andExpect(jsonPath("$[0].ordem").value(2))
                .andExpect(jsonPath("$[1].id").value(antiga.getId())).andExpect(jsonPath("$[1].ordem").value(9));
        mvc.perform(get("/api/gerenciamentos/" + quadro.getId() + "/estrutura-etapas").header("Authorization", bearer)).andExpect(status().isOk())
                .andExpect(jsonPath("$.etapas[0].id").value(nova.getId())).andExpect(jsonPath("$.etapas[0].ordem").value(2))
                .andExpect(jsonPath("$.etapas[1].id").value(antiga.getId())).andExpect(jsonPath("$.etapas[1].ordem").value(9));
        assertThat(estado()).isEqualTo(antes);
    }

    @Test
    void edicaoDaUltimaParaPrimeiraReorganizaTodosEPreservaIdEVinculo() throws Exception { // ETA-13 ambas direções
        var criador = usuario("Criador"); var quadro = quadro(criador);
        var primeira = etapa(quadro, "Primeira", 1); var segunda = etapa(quadro, "Segunda", 2); var ultima = etapa(quadro, "Ultima", 3);
        demanda(ultima); var conteudo = somenteDemandas();
        mvc.perform(put(base(quadro.getId()) + "/" + ultima.getId()).header("Authorization", token(criador)).contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("nome", "Movida", "setor", "Projeto", "ordem", 1, "versao", 0,
                        "id", primeira.getId(), "gerenciamento", Map.of("id", 999999)))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.gerenciamento.id").value(quadro.getId()))
                .andExpect(jsonPath("$.gerenciamento.versao").value(1))
                .andExpect(jsonPath("$.etapas[0].id").value(ultima.getId())).andExpect(jsonPath("$.etapas[0].ordem").value(1))
                .andExpect(jsonPath("$.etapas[0].nome").value("Movida")).andExpect(jsonPath("$.etapas[0].setor").value("Projeto"))
                .andExpect(jsonPath("$.etapas[0].quantidadeDemandas").value(1))
                .andExpect(jsonPath("$.etapas[1].id").value(primeira.getId())).andExpect(jsonPath("$.etapas[1].ordem").value(2))
                .andExpect(jsonPath("$.etapas[2].id").value(segunda.getId())).andExpect(jsonPath("$.etapas[2].ordem").value(3));
        assertThat(etapas.findById(ultima.getId()).orElseThrow().getGerenciamento().getId()).isEqualTo(quadro.getId());
        assertThat(etapas.findById(primeira.getId()).orElseThrow().getNome()).isEqualTo("Primeira");
        assertThat(somenteDemandas()).isEqualTo(conteudo);
    }

    @Test
    void vazioCriaNaPosicaoUmEditaIdenticoIncrementaUmaEPermiteRemoverUltima() throws Exception { // ETA-12/15/19
        var criador = usuario("Criador"); var quadro = quadro(criador); String bearer = token(criador);
        mvc.perform(post(base(quadro.getId())).header("Authorization", bearer).contentType(MediaType.APPLICATION_JSON).content(corpo("Uma", "Setor", 1, 0)))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.etapas[0].ordem").value(1)).andExpect(jsonPath("$.gerenciamento.versao").value(1));
        var unica = etapas.findAll().get(0);
        mvc.perform(put(base(quadro.getId()) + "/" + unica.getId()).header("Authorization", bearer).contentType(MediaType.APPLICATION_JSON).content(corpo("Uma", "Setor", 1, 1)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.gerenciamento.versao").value(2))
                .andExpect(jsonPath("$.etapas[0].id").value(unica.getId())).andExpect(jsonPath("$.etapas[0].nome").value("Uma"))
                .andExpect(jsonPath("$.etapas[0].setor").value("Setor")).andExpect(jsonPath("$.etapas[0].ordem").value(1));
        mvc.perform(delete(base(quadro.getId()) + "/" + unica.getId()).header("Authorization", bearer).contentType(MediaType.APPLICATION_JSON).content("{\"versao\":2}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.gerenciamento.versao").value(3)).andExpect(jsonPath("$.etapas").isEmpty());
        assertThat(etapas.count()).isZero();
        assertThat(quadros.findById(quadro.getId()).orElseThrow().getVersao()).isEqualTo(3L);
    }

    @ParameterizedTest
    @MethodSource("permissoes")
    void terceiroOuSemCriadorRecusaTodaMutacaoAntesDeCampos(String metodo, boolean legado, boolean arquivado) throws Exception { // ETA-05 precede arquivo/versão/campos
        var quadro = quadro(legado ? null : usuario("Criador")); var etapa = etapa(quadro, "Uma", 1);
        if (arquivado) { quadro.setArquivado(true); quadros.saveAndFlush(quadro); }
        var antes = estado();
        mvc.perform(mutacao(metodo, quadro.getId(), etapa.getId()).header("Authorization", token(usuario("Outro")))
                .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.erro").value(PERMISSAO));
        assertThat(estado()).isEqualTo(antes);
    }

    static Stream<Arguments> permissoes() {
        return Stream.of("POST", "PUT", "DELETE").flatMap(m -> Stream.of(false, true)
                .flatMap(l -> Stream.of(false, true).map(a -> Arguments.of(m, l, a))));
    }

    @ParameterizedTest
    @MethodSource("administradoresArquivados")
    void criadorOuAdminNaoMutaArquivadoNemVersao(String metodo, boolean admin) throws Exception { // ETA-07
        var criador = usuario("Criador"); var quadro = quadro(criador); var etapa = etapa(quadro, "Uma", 1);
        var escritor = admin ? usuario("Admin") : criador;
        if (admin) { escritor.setPapel(PapelUsuario.ADMINISTRADOR); usuarios.saveAndFlush(escritor); }
        quadro.setArquivado(true); quadros.saveAndFlush(quadro); var antes = estado();
        mvc.perform(mutacao(metodo, quadro.getId(), etapa.getId()).header("Authorization", token(escritor))
                .contentType(MediaType.APPLICATION_JSON).content(corpo("Nome", "Setor", 1, 1)))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.erro").value(ARQUIVO));
        assertThat(estado()).isEqualTo(antes);
    }

    static Stream<Arguments> administradoresArquivados() {
        return Stream.of("POST", "PUT", "DELETE").flatMap(m -> Stream.of(false, true).map(a -> Arguments.of(m, a)));
    }

    @ParameterizedTest
    @MethodSource("rotasSemSessao")
    void rotasExigemSessaoAntesDeJSON(String metodo, String sessao) throws Exception { // ETA-06
        var quadro = quadro(usuario("Criador")); var etapa = etapa(quadro, "Uma", 1); var antes = estado();
        var request = metodo.equals("GET") ? get(base(quadro.getId()))
                : metodo.equals("SNAPSHOT") ? get("/api/gerenciamentos/" + quadro.getId() + "/estrutura-etapas")
                : mutacao(metodo, quadro.getId(), etapa.getId()).contentType(MediaType.APPLICATION_JSON).content("{");
        if (!sessao.isEmpty()) { request.header("Authorization", sessao); }
        mvc.perform(request).andExpect(status().isUnauthorized());
        assertThat(estado()).isEqualTo(antes);
    }

    static Stream<Arguments> rotasSemSessao() {
        return Stream.of("GET", "SNAPSHOT", "POST", "PUT", "DELETE").flatMap(m -> Stream.of("", "Bearer inexistente").map(s -> Arguments.of(m, s)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"POST", "PUT", "DELETE", "GET", "SNAPSHOT"})
    void quadroAusenteRetorna404(String metodo) throws Exception { // ETA-08
        String bearer = token(usuario("Criador")); var antes = estado();
        var request = metodo.equals("GET") ? get(base(999999L))
                : metodo.equals("SNAPSHOT") ? get("/api/gerenciamentos/999999/estrutura-etapas")
                : mutacao(metodo, 999999L, 999999L).contentType(MediaType.APPLICATION_JSON).content(corpo("Nome", "Setor", 1, 0));
        mvc.perform(request.header("Authorization", bearer)).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.erro").value("Gerenciamento não encontrado."));
        assertThat(estado()).isEqualTo(antes);
    }

    @ParameterizedTest
    @MethodSource("etapasAusentes")
    void etapaAusenteOuDeOutroQuadroRetorna404SemTransferencia(String metodo, boolean estrangeira) throws Exception { // ETA-08
        var criador = usuario("Criador"); var quadro = quadro(criador);
        long id = estrangeira ? etapa(quadro(criador), "Externa", 1).getId() : 999999L;
        var antes = estado();
        mvc.perform(mutacao(metodo, quadro.getId(), id).header("Authorization", token(criador)).contentType(MediaType.APPLICATION_JSON)
                .content(corpo("Nome", "Setor", 1, 0))).andExpect(status().isNotFound()).andExpect(jsonPath("$.erro").value(ETAPA));
        assertThat(estado()).isEqualTo(antes);
    }

    static Stream<Arguments> etapasAusentes() { return Stream.of("PUT", "DELETE").flatMap(m -> Stream.of(false, true).map(f -> Arguments.of(m, f))); }

    @ParameterizedTest
    @MethodSource("camposInvalidos")
    void validaNomeSetorUTF16SemEscrever(String metodo, String campo, String caso) throws Exception { // ETA-09/10
        var criador = usuario("Criador"); var quadro = quadro(criador); var etapa = etapa(quadro, "Uma", 1); var antes = estado();
        Map<String, Object> dados = new LinkedHashMap<>(Map.of("nome", "Nome", "setor", "Setor", "ordem", 1, "versao", 0));
        switch (caso) {
            case "ausente" -> dados.remove(campo); case "nulo" -> dados.put(campo, null);
            case "256" -> dados.put(campo, "A".repeat(256)); case "emoji" -> dados.put(campo, "\uD83D\uDE00".repeat(128));
            default -> dados.put(campo, caso);
        }
        mvc.perform(mutacao(metodo, quadro.getId(), etapa.getId()).header("Authorization", token(criador)).contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(dados))).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value(campo.equals("nome") ? "Informe um nome de etapa entre 1 e 255 caracteres." : "Informe um setor entre 1 e 255 caracteres."));
        assertThat(estado()).isEqualTo(antes);
    }

    static Stream<Arguments> camposInvalidos() {
        return Stream.of("POST", "PUT").flatMap(m -> Stream.of("nome", "setor")
                .flatMap(c -> Stream.of("ausente", "nulo", "", " \t\u00a0\ufeff", "256", "emoji").map(v -> Arguments.of(m, c, v))));
    }

    @ParameterizedTest
    @MethodSource("limitesValidos")
    void aceitaLimitesNormalizadosWhitespaceJS(String metodo, int tamanho) throws Exception { // ETA-09/10
        var criador = usuario("Criador"); var quadro = quadro(criador); var etapa = etapa(quadro, "Uma", 1);
        String texto = tamanho == 255 ? "\uD83D\uDE00".repeat(127) + "A" : "A";
        mvc.perform(mutacao(metodo, quadro.getId(), etapa.getId()).header("Authorization", token(criador)).contentType(MediaType.APPLICATION_JSON)
                .content(corpo("\ufeff\u00a0\t" + texto + "\u3000", "\u2000" + texto + "\r\n", 1, 0)))
                .andExpect(status().is(metodo.equals("POST") ? 201 : 200)).andExpect(jsonPath("$.etapas[0].nome").value(texto))
                .andExpect(jsonPath("$.etapas[0].setor").value(texto)).andExpect(jsonPath("$.gerenciamento.versao").value(1));
        assertThat(etapas.findByGerenciamentoIdOrderByOrdem(quadro.getId()).get(0).getNome()).isEqualTo(texto);
    }

    static Stream<Arguments> limitesValidos() { return Stream.of("POST", "PUT").flatMap(m -> Stream.of(1, 255).map(t -> Arguments.of(m, t))); }

    @ParameterizedTest
    @MethodSource("posicoesInvalidas")
    void posicaoExigeInteiroEIntervalo(String metodo, String valor) throws Exception { // ETA-11
        var criador = usuario("Criador"); var quadro = quadro(criador); var etapa = etapa(quadro, "Uma", 1); var antes = estado();
        String erro = List.of("-1", "0", "null", "ausente", "3").contains(valor) ? "Escolha uma posição válida para a etapa." : FORMATO;
        String corpo = "{\"nome\":\"Nome\",\"setor\":\"Setor\",\"versao\":0" + (valor.equals("ausente") ? "" : ",\"ordem\":" + valor) + "}";
        mvc.perform(mutacao(metodo, quadro.getId(), etapa.getId()).header("Authorization", token(criador)).contentType(MediaType.APPLICATION_JSON)
                .content(corpo)).andExpect(status().isBadRequest()).andExpect(jsonPath("$.erro").value(erro));
        assertThat(estado()).isEqualTo(antes);
    }

    static Stream<Arguments> posicoesInvalidas() {
        return Stream.of("POST", "PUT").flatMap(m -> Stream.of("-1", "0", "null", "ausente", "3", "1.1", "\"1\"", "true", "2147483648").map(v -> Arguments.of(m, v)));
    }

    @Test
    void editarNaoAceitaPosicaoNMaisUm() throws Exception { // ETA-11 PUT máximo N
        var criador = usuario("Criador"); var quadro = quadro(criador); var etapa = etapa(quadro, "Uma", 1); var antes = estado();
        mvc.perform(put(base(quadro.getId()) + "/" + etapa.getId()).header("Authorization", token(criador)).contentType(MediaType.APPLICATION_JSON)
                .content(corpo("Nome", "Setor", 2, 0))).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Escolha uma posição válida para a etapa."));
        assertThat(estado()).isEqualTo(antes);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3})
    void criaEmTodasAsPosicoesInclusiveUltima(int posicao) throws Exception { // ETA-12
        var criador = usuario("Criador"); var quadro = quadro(criador);
        var primeira = etapa(quadro, "Primeira", 1); var segunda = etapa(quadro, "Segunda", 2);
        mvc.perform(post(base(quadro.getId())).header("Authorization", token(criador)).contentType(MediaType.APPLICATION_JSON).content(corpo("Nova", "Setor", posicao, 0)))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.etapas[" + (posicao - 1) + "].nome").value("Nova"))
                .andExpect(jsonPath("$.etapas[" + (posicao - 1) + "].ordem").value(posicao));
        assertThat(etapas.findById(primeira.getId()).orElseThrow().getOrdem()).isEqualTo(posicao == 1 ? 2 : 1);
        assertThat(etapas.findById(segunda.getId()).orElseThrow().getOrdem()).isEqualTo(posicao <= 2 ? 3 : 2);
    }

    @ParameterizedTest
    @MethodSource("versoesInvalidas")
    void versaoEstritaRecusaSemEscrever(String metodo, String valor) throws Exception { // ETA-17
        var criador = usuario("Criador"); var quadro = quadro(criador); var etapa = etapa(quadro, "Uma", 1); var antes = estado();
        String erro = List.of("-1", "null", "ausente").contains(valor) ? "Informe uma versão válida do gerenciamento." : FORMATO;
        String corpo = "{\"nome\":\"Nome\",\"setor\":\"Setor\",\"ordem\":1" + (valor.equals("ausente") ? "" : ",\"versao\":" + valor) + "}";
        mvc.perform(mutacao(metodo, quadro.getId(), etapa.getId()).header("Authorization", token(criador)).contentType(MediaType.APPLICATION_JSON)
                .content(corpo)).andExpect(status().isBadRequest()).andExpect(jsonPath("$.erro").value(erro));
        assertThat(estado()).isEqualTo(antes);
    }

    static Stream<Arguments> versoesInvalidas() {
        return Stream.of("POST", "PUT", "DELETE").flatMap(m -> Stream.of("-1", "null", "ausente", "0.1", "\"0\"", "true", "9223372036854775808").map(v -> Arguments.of(m, v)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"POST", "PUT", "DELETE"})
    void versaoAntigaConflitaAntesDosCampos(String metodo) throws Exception { // ETA-18
        var criador = usuario("Criador"); var quadro = quadro(criador); var etapa = etapa(quadro, "Uma", 1);
        quadro.setDescricao("Outra"); quadros.saveAndFlush(quadro); var antes = estado();
        mvc.perform(mutacao(metodo, quadro.getId(), etapa.getId()).header("Authorization", token(criador)).contentType(MediaType.APPLICATION_JSON)
                .content("{\"versao\":0}")).andExpect(status().isConflict()).andExpect(jsonPath("$.erro").value(VERSAO));
        assertThat(estado()).isEqualTo(antes);
    }

    @ParameterizedTest
    @ValueSource(strings = {"POST", "PUT", "DELETE"})
    void versaoFuturaTambemConflitaSemEscrever(String metodo) throws Exception { // ETA-18 difere em qualquer direção
        var criador = usuario("Criador"); var quadro = quadro(criador); var etapa = etapa(quadro, "Uma", 1); var antes = estado();
        mvc.perform(mutacao(metodo, quadro.getId(), etapa.getId()).header("Authorization", token(criador)).contentType(MediaType.APPLICATION_JSON)
                .content(corpo("Nome", "Setor", 1, 1))).andExpect(status().isConflict()).andExpect(jsonPath("$.erro").value(VERSAO));
        assertThat(estado()).isEqualTo(antes);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Em andamento", "Concluido", "Cancelado"})
    void ocupadaNaoRemoveIndependentementeDoStatus(String status) throws Exception { // ETA-16
        var criador = usuario("Criador"); var quadro = quadro(criador); var etapa = etapa(quadro, "Uma", 1);
        var processo = demanda(etapa); processo.setStatus(status); processos.saveAndFlush(processo); var antes = estado();
        mvc.perform(delete(base(quadro.getId()) + "/" + etapa.getId()).header("Authorization", token(criador)).contentType(MediaType.APPLICATION_JSON)
                .content("{\"versao\":0}")).andExpect(status().isConflict())
                .andExpect(jsonPath("$.erro").value("Esta etapa possui demandas. Mova-as para outra etapa antes de removê-la."));
        assertThat(estado()).isEqualTo(antes);
    }

    @ParameterizedTest
    @ValueSource(strings = {"POST", "PUT", "DELETE"})
    void falhaDeBancoReverteEtapasOrdemVersaoEConteudo(String metodo) throws Exception { // ETA-20
        var criador = usuario("Criador"); var quadro = quadro(criador);
        var primeira = etapa(quadro, "Primeira", 1); etapa(quadro, "Segunda", 2); etapa(quadro, "Ultima", 3); demanda(primeira);
        long alvo = metodo.equals("DELETE") ? etapas.findAll().stream().filter(e -> e.getNome().equals("Segunda")).findFirst().orElseThrow().getId() : primeira.getId();
        var antes = estado();
        jdbc.execute("alter table etapas add constraint falha_eta check (nome <> 'Falha' and (nome <> 'Ultima' or ordem <> 2))");
        try {
            mvc.perform(mutacao(metodo, quadro.getId(), alvo).header("Authorization", token(criador)).contentType(MediaType.APPLICATION_JSON)
                    .content(corpo("Falha", "Setor", 3, 0))).andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.erro").value("Não foi possível concluir a operação."));
            assertThat(estado()).isEqualTo(antes);
        } finally { jdbc.execute("alter table etapas drop constraint falha_eta"); }
    }

    @ParameterizedTest
    @ValueSource(strings = {"POST", "PUT", "DELETE"})
    void jsonImpossivelRetorna400SemMutacao(String metodo) throws Exception { // ETA-17/20: formato precede payload
        var criador = usuario("Criador"); var quadro = quadro(criador); var etapa = etapa(quadro, "Uma", 1); var antes = estado();
        mvc.perform(mutacao(metodo, quadro.getId(), etapa.getId()).header("Authorization", token(criador)).contentType(MediaType.APPLICATION_JSON)
                .content("{")).andExpect(status().isBadRequest()).andExpect(jsonPath("$.erro").value(FORMATO));
        assertThat(estado()).isEqualTo(antes);
    }

    private String corpo(String nome, String setor, int ordem, long versao) {
        return json.writeValueAsString(Map.of("nome", nome, "setor", setor, "ordem", ordem, "versao", versao));
    }
    private static String base(Long id) { return "/api/gerenciamentos/" + id + "/etapas"; }
    private static MockHttpServletRequestBuilder mutacao(String metodo, Long quadroId, Long etapaId) {
        return switch (metodo) { case "POST" -> post(base(quadroId)); case "PUT" -> put(base(quadroId) + "/" + etapaId); default -> delete(base(quadroId) + "/" + etapaId); };
    }
    private Map<String, Object> estado() {
        var dados = new LinkedHashMap<>(conteudoPersistido()); dados.put("gerenciamentos", jdbc.queryForList("select * from gerenciamentos order by id")); return dados;
    }
    private Map<String, Object> somenteDemandas() {
        var dados = new LinkedHashMap<>(conteudoPersistido()); dados.remove("etapas"); return dados;
    }
}
