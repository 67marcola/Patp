package com.patp.sistema;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.util.LinkedHashMap;
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
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import com.patp.sistema.model.CategoriaEtapa;
import com.patp.sistema.model.PapelUsuario;
import com.patp.sistema.repository.ProcessoRepository;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class DemandaCadastroTests extends ApiIntegrationSupport {
    @Autowired ObjectMapper json;
    @MockitoSpyBean ProcessoRepository repositoryComSpy;
    private static final String FORMATO = "Dados da requisição inválidos.";
    private static final String VERSAO = "Gerenciamento alterado por outro usuário. Atualize e tente novamente.";

    @Test
    void cadastroMinimoRetornaSnapshotPersistidoDefaultsVersaoEHistoricoDaSessao() throws Exception { // CAD-01/05/15
        var criador = usuario("Criador"); var quadro = quadro(criador); var etapa = etapa(quadro, "Inicial", 1);
        var resultado = enviar(quadro.getId(), token(usuario("Outro")), Map.of("numeroProcesso", "Minima", "pessoa", "Pessoa", "versao", 0))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.gerenciamento.versao").value(1))
                .andExpect(jsonPath("$.demandas.length()").value(1)).andExpect(jsonPath("$.etapas[0].quantidadeDemandas").value(1))
                .andExpect(jsonPath("$.demandas[0].numeroProcesso").value("Minima"))
                .andExpect(jsonPath("$.demandas[0].pessoa").value("Pessoa"))
                .andExpect(jsonPath("$.demandas[0].status").value("Em andamento"))
                .andExpect(jsonPath("$.demandas[0].etapaId").value(etapa.getId())).andReturn();
        var demanda = processos.findAll().get(0);
        assertThat(json.readTree(resultado.getResponse().getContentAsString()).get("demandas").get(0).get("id").asLong()).isEqualTo(demanda.getId());
        assertThat(demanda.getNumeroProcesso()).isEqualTo("Minima"); assertThat(demanda.getPessoa()).isEqualTo("Pessoa");
        assertThat(demanda.getStatus()).isEqualTo("Em andamento"); assertThat(demanda.getEtapa().getId()).isEqualTo(etapa.getId());
        assertThat(demanda.getResponsavel()).isNull(); assertThat(demanda.getPrioridade()).isNull(); assertThat(demanda.getObservacoes()).isNull();
        assertThat(demanda.getDataEmissao()).isNull(); assertThat(demanda.getPrazoEtapa()).isNull(); assertThat(demanda.getPrazoGeral()).isNull();
        assertThat(demanda.getDataConclusao()).isNull(); assertThat(demanda.getDataCancelamento()).isNull(); assertThat(demanda.getMotivoCancelamento()).isNull();
        assertThat(quadros.findById(quadro.getId()).orElseThrow().getVersao()).isEqualTo(1L);
        assertThat(jdbc.queryForList("select acao from historicos", String.class)).containsExactly("CRIACAO");
        assertThat(jdbc.queryForList("select descricao from historicos", String.class)).containsExactly("Processo criado.");
        assertThat(jdbc.queryForList("select usuario from historicos", String.class)).containsExactly("Outro");
        assertThat(jdbc.queryForObject("select processo_id from historicos", Long.class)).isEqualTo(demanda.getId());
        assertThat(jdbc.queryForObject("select count(*) from historicos where data_hora is not null", Long.class)).isEqualTo(1L);
    }

    @Test
    void cadastroCompletoNormalizaTextosEPreservaDatasSemImporOrdem() throws Exception { // CAD-02/03/04
        var criador = usuario("Criador"); var quadro = quadro(criador); etapa(quadro, "Inicial", 1);
        var dados = new LinkedHashMap<String, Object>(); dados.put("versao", 0); dados.put("numeroProcesso", "  MiSto-1  "); dados.put("pessoa", "  João  ");
        dados.put("responsavel", "  Ana  "); dados.put("prioridade", "  Prioridade livre  "); dados.put("observacoes", "  Uma\nOutra  ");
        dados.put("dataEmissao", "2020-02-03"); dados.put("prazoEtapa", "2019-01-01"); dados.put("prazoGeral", "2018-01-01");
        enviar(quadro.getId(), token(criador), dados).andExpect(status().isCreated())
                .andExpect(jsonPath("$.demandas[0].numeroProcesso").value("MiSto-1"))
                .andExpect(jsonPath("$.demandas[0].pessoa").value("João"))
                .andExpect(jsonPath("$.demandas[0].responsavel").value("Ana"))
                .andExpect(jsonPath("$.demandas[0].prioridade").value("Prioridade livre"))
                .andExpect(jsonPath("$.demandas[0].observacoes").value("Uma\nOutra"))
                .andExpect(jsonPath("$.demandas[0].dataEmissao").value("2020-02-03"))
                .andExpect(jsonPath("$.demandas[0].prazoEtapa").value("2019-01-01"))
                .andExpect(jsonPath("$.demandas[0].prazoGeral").value("2018-01-01"));
        var demanda = processos.findAll().get(0);
        assertThat(demanda.getNumeroProcesso()).isEqualTo("MiSto-1"); assertThat(demanda.getPessoa()).isEqualTo("João");
        assertThat(demanda.getResponsavel()).isEqualTo("Ana"); assertThat(demanda.getPrioridade()).isEqualTo("Prioridade livre");
        assertThat(demanda.getObservacoes()).isEqualTo("Uma\nOutra");
        assertThat(demanda.getDataEmissao()).isEqualTo(LocalDate.of(2020, 2, 3));
        assertThat(demanda.getPrazoEtapa()).isEqualTo(LocalDate.of(2019, 1, 1)); assertThat(demanda.getPrazoGeral()).isEqualTo(LocalDate.of(2018, 1, 1));
    }

    @Test
    void limitesUtf16EOpcionaisVaziosOuNullSaoAceitos() throws Exception { // CAD-02/03/04
        var criador = usuario("Criador"); var quadro = quadro(criador); etapa(quadro, "Inicial", 1); String bearer = token(criador);
        var dados = new LinkedHashMap<String, Object>(); dados.put("versao", 0); dados.put("numeroProcesso", "😀".repeat(127) + "a"); dados.put("pessoa", "ç".repeat(255));
        dados.put("responsavel", "r".repeat(255)); dados.put("prioridade", "p".repeat(255)); dados.put("observacoes", "o".repeat(10000));
        enviar(quadro.getId(), bearer, dados).andExpect(status().isCreated());
        var limite = processos.findAll().get(0);
        assertThat(limite.getNumeroProcesso()).isEqualTo("😀".repeat(127) + "a"); assertThat(limite.getPessoa()).isEqualTo("ç".repeat(255));
        assertThat(limite.getResponsavel()).isEqualTo("r".repeat(255)); assertThat(limite.getPrioridade()).isEqualTo("p".repeat(255));
        assertThat(limite.getObservacoes()).isEqualTo("o".repeat(10000));
        dados.put("versao", 1); dados.put("numeroProcesso", "Outra"); dados.put("pessoa", "Pessoa"); dados.put("responsavel", "  "); dados.put("prioridade", null);
        dados.put("observacoes", "\n\t"); dados.put("dataEmissao", null); dados.put("prazoEtapa", null); dados.put("prazoGeral", null);
        enviar(quadro.getId(), bearer, dados).andExpect(status().isCreated());
        var vazia = processos.findByEtapaGerenciamentoIdOrderByIdAsc(quadro.getId()).get(1);
        assertThat(vazia.getResponsavel()).isNull(); assertThat(vazia.getPrioridade()).isNull(); assertThat(vazia.getObservacoes()).isNull();
        assertThat(vazia.getDataEmissao()).isNull(); assertThat(vazia.getPrazoEtapa()).isNull(); assertThat(vazia.getPrazoGeral()).isNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {"criador", "admin", "outro", "legado"})
    void todosAutenticadosCriamInclusiveQuadroSemCriador(String ator) throws Exception { // CAD-08
        var criador = usuario("Criador"); var quadro = quadro(ator.equals("legado") ? null : criador); etapa(quadro, "Inicial", 1);
        var autor = ator.equals("criador") ? criador : usuario("Autor");
        if (ator.equals("admin")) { autor.setPapel(PapelUsuario.ADMINISTRADOR); usuarios.saveAndFlush(autor); }
        enviar(quadro.getId(), token(autor), Map.of("versao", 0, "numeroProcesso", "Autorizada", "pessoa", "Pessoa"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.demandas[0].numeroProcesso").value("Autorizada"));
        assertThat(processos.count()).isEqualTo(1); assertThat(jdbc.queryForList("select usuario from historicos", String.class)).containsExactly(autor.getNome());
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void linhaGlobalArquivadaSemCriadorNaoImpedeCadastroNemMudaDados(boolean legado) throws Exception { // CAD-08/17/18
        var global = quadro(null); etapa(global, "Arquivo", 1); global.setArquivado(true); quadros.saveAndFlush(global);
        var antes = jdbc.queryForList("select * from gerenciamentos where id=?", global.getId());
        var leitor = usuario("Leitor"); var alvo = quadro(null); var etapa = etapa(alvo, "Inicial", 1);
        mvc.perform(post(legado ? "/api/processos" : rota(alvo.getId())).header("Authorization", token(leitor)).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(payload(legado, etapa.getId()))))
                .andExpect(status().is(legado ? 200 : 201));
        assertThat(jdbc.queryForList("select * from gerenciamentos where id=?", global.getId())).isEqualTo(antes);
        assertThat(processos.findAll().get(0).getEtapa().getId()).isEqualTo(etapa.getId());
        assertThat(quadros.findById(alvo.getId()).orElseThrow().getVersao()).isEqualTo(legado ? 0L : 1L);
    }

    @Test
    void primeiraTrabalhoRespeitaCategoriaSqlNullLacunasEmpatesHomonomosEId() throws Exception { // CAD-06
        var criador = usuario("Criador"); var quadro = quadro(criador);
        var finalizada = etapa(quadro, "Comprar poste", -2); finalizada.setCategoria(CategoriaEtapa.CONCLUIDA); etapas.saveAndFlush(finalizada);
        var cancelada = etapa(quadro, "Outra", -1); cancelada.setCategoria(CategoriaEtapa.CANCELADA); etapas.saveAndFlush(cancelada);
        var primeira = etapa(quadro, "Concluídos", 7); jdbc.update("update etapas set categoria=null where id=?", primeira.getId());
        etapa(quadro, "Concluídos", 7); etapa(quadro, "Trabalho posterior", 90);
        var antes = jdbc.queryForList("select * from etapas order by id");
        enviar(quadro.getId(), token(criador), Map.of("versao", 0, "numeroProcesso", "Automatica", "pessoa", "Pessoa"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.demandas[0].etapaId").value(primeira.getId()));
        assertThat(processos.findAll().get(0).getEtapa().getId()).isEqualTo(primeira.getId());
        assertThat(jdbc.queryForList("select * from etapas order by id")).isEqualTo(antes);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void semTrabalhoRecusaSemGravar(boolean finais) throws Exception { // CAD-07
        var criador = usuario("Criador"); var quadro = quadro(criador);
        if (finais) { var etapa = etapa(quadro, "Concluídos", 1); etapa.setCategoria(CategoriaEtapa.CONCLUIDA); etapas.saveAndFlush(etapa); }
        var antes = estado();
        enviar(quadro.getId(), token(criador), Map.of("versao", 0, "numeroProcesso", "Recusada", "pessoa", "Pessoa"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.erro").value("Cadastre uma etapa de trabalho antes de criar demandas."));
        assertThat(estado()).isEqualTo(antes);
    }

    @Test
    void sessaoAusenteInvalidaQuadroInexistenteEArquivoRecusamSemGravar() throws Exception { // CAD-09
        var criador = usuario("Criador"); var quadro = quadro(criador); etapa(quadro, "Inicial", 1); String bearer = token(criador); var antes = estado();
        for (String authorization : new String[]{null, "Bearer invalido"}) {
            var request = post(rota(quadro.getId())).contentType(MediaType.APPLICATION_JSON).content("{\"versao\":0,\"numeroProcesso\":\"Nova\",\"pessoa\":\"Pessoa\"}");
            if (authorization != null) { request.header("Authorization", authorization); }
            mvc.perform(request).andExpect(status().isUnauthorized()); assertThat(estado()).isEqualTo(antes);
        }
        enviar(999999L, bearer, Map.of("versao", 0, "numeroProcesso", "Nova", "pessoa", "Pessoa"))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.erro").value("Gerenciamento não encontrado."));
        assertThat(estado()).isEqualTo(antes);
        quadro.setArquivado(true); quadros.saveAndFlush(quadro); antes = estado();
        enviar(quadro.getId(), bearer, Map.of("versao", 1, "numeroProcesso", "Nova", "pessoa", "Pessoa"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.erro").value("Gerenciamento arquivado. Restaure-o antes de alterar."));
        assertThat(estado()).isEqualTo(antes);
    }

    @ParameterizedTest
    @MethodSource("camposInvalidos")
    void camposInvalidosNasDuasRotasPreservamTudo(String campo, Object valor, String mensagem, boolean legado) throws Exception { // CAD-10/18
        var criador = usuario("Criador"); var quadro = quadro(criador); var etapa = etapa(quadro, "Inicial", 1);
        var dados = payload(legado, etapa.getId()); dados.put(campo, valor); var antes = estado();
        mvc.perform(post(legado ? "/api/processos" : rota(quadro.getId())).header("Authorization", token(criador)).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(dados)))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.erro").value(mensagem));
        assertThat(estado()).isEqualTo(antes);
    }

    static Stream<Arguments> camposInvalidos() {
        var campos = Stream.of(
                Arguments.of("numeroProcesso", null, "Informe um número de demanda entre 1 e 255 caracteres."),
                Arguments.of("numeroProcesso", "  ", "Informe um número de demanda entre 1 e 255 caracteres."),
                Arguments.of("numeroProcesso", "a".repeat(256), "Informe um número de demanda entre 1 e 255 caracteres."),
                Arguments.of("numeroProcesso", "😀".repeat(128), "Informe um número de demanda entre 1 e 255 caracteres."),
                Arguments.of("pessoa", null, "Informe um cliente/solicitante entre 1 e 255 caracteres."),
                Arguments.of("pessoa", "\n\t", "Informe um cliente/solicitante entre 1 e 255 caracteres."),
                Arguments.of("pessoa", "a".repeat(256), "Informe um cliente/solicitante entre 1 e 255 caracteres."),
                Arguments.of("responsavel", "a".repeat(256), "Informe um responsável com até 255 caracteres."),
                Arguments.of("prioridade", "a".repeat(256), "Informe uma prioridade com até 255 caracteres."),
                Arguments.of("observacoes", "a".repeat(10001), "Informe observações com até 10000 caracteres."));
        return campos.flatMap(c -> Stream.of(false, true).map(legado -> Arguments.of(c.get()[0], c.get()[1], c.get()[2], legado)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"{", "[]", "null", "{\"versao\":\"0\"}", "{\"versao\":0.5}", "{\"versao\":true}",
            "{\"versao\":0,\"dataEmissao\":\"2026-02-30\"}", "{\"versao\":0,\"prazoEtapa\":\"ontem\"}", "{\"versao\":0,\"prazoGeral\":[2026,1,1]}"})
    void jsonDatasEVersaoMalformadosRecusamSemGravar(String corpo) throws Exception { // CAD-11
        var criador = usuario("Criador"); var quadro = quadro(criador); etapa(quadro, "Inicial", 1); var antes = estado();
        mvc.perform(post(rota(quadro.getId())).header("Authorization", token(criador)).contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.erro").value(FORMATO));
        assertThat(estado()).isEqualTo(antes);
    }

    @ParameterizedTest
    @MethodSource("datasInvalidas")
    void cadaDataAceitaSomenteStringIsoOuNull(String campo, Object valor) throws Exception { // CAD-11
        var criador = usuario("Criador"); var quadro = quadro(criador); etapa(quadro, "Inicial", 1); var antes = estado();
        var dados = payload(false, 0L); dados.put(campo, valor);
        enviar(quadro.getId(), token(criador), dados).andExpect(status().isBadRequest()).andExpect(jsonPath("$.erro").value(FORMATO));
        assertThat(estado()).isEqualTo(antes);
    }
    static Stream<Arguments> datasInvalidas() {
        return Stream.of("dataEmissao", "prazoEtapa", "prazoGeral").flatMap(c -> Stream.of("", " ", "2026-02-30", 20260101, true, java.util.List.of(2026, 1, 1)).map(v -> Arguments.of(c, v)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"id", "etapa", "status", "dataConclusao", "dataCancelamento", "motivoCancelamento", "desconhecido"})
    void camposForaDosNovePermitidosRecusamMesmoNull(String campo) throws Exception { // CAD-12
        var criador = usuario("Criador"); var quadro = quadro(criador); etapa(quadro, "Inicial", 1); var antes = estado();
        var dados = payload(false, 0L); dados.put(campo, null);
        enviar(quadro.getId(), token(criador), dados).andExpect(status().isBadRequest()).andExpect(jsonPath("$.erro").value(FORMATO));
        assertThat(estado()).isEqualTo(antes);
    }

    @ParameterizedTest
    @ValueSource(longs = {-1, 0, 999})
    void versaoAusenteNegativaOuObsoletaPreservaTudo(long versao) throws Exception { // CAD-14
        var criador = usuario("Criador"); var quadro = quadro(criador); etapa(quadro, "Inicial", 1); var antes = estado();
        var dados = payload(false, 0L); if (versao == 0) { dados.remove("versao"); } else { dados.put("versao", versao); }
        enviar(quadro.getId(), token(criador), dados).andExpect(status().is(versao == 999 ? 409 : 400))
                .andExpect(jsonPath("$.erro").value(versao == 999 ? VERSAO : "Informe uma versão válida do gerenciamento."));
        assertThat(estado()).isEqualTo(antes);
    }

    @ParameterizedTest
    @MethodSource("duplicados")
    void numeroGlobalDuplicadoIgnoraCaixaNasDuasRotas(boolean legado, String numero) throws Exception { // CAD-13/18
        var criador = usuario("Criador"); var primeira = etapa(quadro(criador), "Outra", 1); var existente = demanda(primeira);
        existente.setNumeroProcesso("MiSto"); processos.saveAndFlush(existente);
        var quadro = quadro(criador); var etapa = etapa(quadro, "Inicial", 1); var antes = estado(); var dados = payload(legado, etapa.getId()); dados.put("numeroProcesso", numero);
        mvc.perform(post(legado ? "/api/processos" : rota(quadro.getId())).header("Authorization", token(criador)).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(dados)))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.erro").value("Já existe uma demanda com esse número."));
        assertThat(estado()).isEqualTo(antes);
    }
    static Stream<Arguments> duplicados() { return Stream.of(false, true).flatMap(l -> Stream.of("MiSto", "  misto  ").map(n -> Arguments.of(l, n))); }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void restricaoUnicaFisicaRetorna409ERollbackSePrecheckNaoEncontra(boolean legado) throws Exception { // CAD-13/18
        var criador = usuario("Criador"); var existente = demanda(etapa(quadro(criador), "Outra", 1)); existente.setNumeroProcesso("Fisica"); processos.saveAndFlush(existente);
        var quadro = quadro(criador); var etapa = etapa(quadro, "Inicial", 1); var antes = estado(); var dados = payload(legado, etapa.getId()); dados.put("numeroProcesso", "Fisica");
        // Only the advisory lookup is bypassed. The INSERT and unique violation use the real database.
        doReturn(false).when(repositoryComSpy).existsByNumeroProcessoIgnoreCase("Fisica");
        mvc.perform(post(legado ? "/api/processos" : rota(quadro.getId())).header("Authorization", token(criador)).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(dados)))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.erro").value("Já existe uma demanda com esse número."));
        assertThat(estado()).isEqualTo(antes);
    }

    @ParameterizedTest
    @MethodSource("falhas")
    void falhasReaisDeDemandaOuHistoricoRevertemTudo(boolean historico, boolean legado) throws Exception { // CAD-16/18
        var criador = usuario("Criador"); var quadro = quadro(criador); var etapa = etapa(quadro, "Inicial", 1); var antes = estado();
        String tabela = historico ? "historicos" : "processos";
        jdbc.execute("alter table " + tabela + " add constraint falha_cadastro check (" + (historico ? "acao <> 'CRIACAO'" : "pessoa <> 'Pessoa'") + ")");
        try {
            mvc.perform(post(legado ? "/api/processos" : rota(quadro.getId())).header("Authorization", token(criador)).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(payload(legado, etapa.getId()))))
                    .andExpect(status().isInternalServerError()).andExpect(jsonPath("$.erro").value("Não foi possível concluir a operação."));
            assertThat(estado()).isEqualTo(antes);
        } finally { jdbc.execute("alter table " + tabela + " drop constraint falha_cadastro"); }
    }
    static Stream<Arguments> falhas() { return Stream.of(false, true).flatMap(h -> Stream.of(false, true).map(l -> Arguments.of(h, l))); }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void legadoConserva200EntidadeSemVersaoComDefaultsECamposValidos(boolean statusInformado) throws Exception { // CAD-18
        var criador = usuario("Criador"); var quadro = quadro(criador); var etapa = etapa(quadro, "Inicial", 1); var dados = payload(true, etapa.getId());
        dados.put("numeroProcesso", "  Legado  "); dados.put("pessoa", "  Pessoa  "); dados.put("responsavel", "  Ana  "); dados.put("prioridade", "  Livre  ");
        dados.put("dataEmissao", "2020-01-01"); dados.put("prazoEtapa", "2019-01-01"); dados.put("prazoGeral", "2018-01-01"); dados.put("observacoes", "  Texto  ");
        if (statusInformado) { dados.put("status", "Em andamento"); }
        mvc.perform(post("/api/processos").header("Authorization", token(usuario("Outro"))).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(dados)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.numeroProcesso").value("Legado"))
                .andExpect(jsonPath("$.pessoa").value("Pessoa")).andExpect(jsonPath("$.status").value("Em andamento"))
                .andExpect(jsonPath("$.etapa.id").value(etapa.getId())).andExpect(jsonPath("$.etapa.gerenciamento.id").value(quadro.getId()));
        var demanda = processos.findAll().get(0); assertThat(demanda.getResponsavel()).isEqualTo("Ana"); assertThat(demanda.getPrioridade()).isEqualTo("Livre");
        assertThat(demanda.getObservacoes()).isEqualTo("Texto"); assertThat(demanda.getDataEmissao()).isEqualTo(LocalDate.of(2020, 1, 1));
        assertThat(demanda.getPrazoEtapa()).isEqualTo(LocalDate.of(2019, 1, 1)); assertThat(demanda.getPrazoGeral()).isEqualTo(LocalDate.of(2018, 1, 1));
        assertThat(demanda.getDataConclusao()).isNull(); assertThat(demanda.getDataCancelamento()).isNull(); assertThat(demanda.getMotivoCancelamento()).isNull();
        assertThat(quadros.findById(quadro.getId()).orElseThrow().getVersao()).isZero();
        assertThat(jdbc.queryForList("select acao from historicos", String.class)).containsExactly("CRIACAO");
    }

    @ParameterizedTest
    @MethodSource("estadosLegados")
    void legadoRecusaEstadoOuEncerramentoSemGravar(String campo, Object valor) throws Exception { // CAD-19
        var criador = usuario("Criador"); var quadro = quadro(criador); var etapa = etapa(quadro, "Inicial", 1); var antes = estado(); var dados = payload(true, etapa.getId()); dados.put(campo, valor);
        mvc.perform(post("/api/processos").header("Authorization", token(criador)).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(dados)))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.erro").value("O estado inicial da demanda é definido pelo sistema."));
        assertThat(estado()).isEqualTo(antes);
    }
    static Stream<Arguments> estadosLegados() { return Stream.of(Arguments.of("status", "Concluido"), Arguments.of("status", "Cancelado"), Arguments.of("status", "Outro"),
            Arguments.of("dataConclusao", "2020-01-01"), Arguments.of("dataCancelamento", "2020-01-01"), Arguments.of("motivoCancelamento", "")); }

    @ParameterizedTest
    @ValueSource(strings = {"TRABALHO", "CONCLUIDA", "CANCELADA"})
    void legadoRecusaEtapaPosteriorOuFinal(String categoria) throws Exception { // CAD-20
        var criador = usuario("Criador"); var quadro = quadro(criador); etapa(quadro, "Inicial", 1);
        var destino = etapa(quadro, "Destino", 2); destino.setCategoria(CategoriaEtapa.valueOf(categoria)); etapas.saveAndFlush(destino); var antes = estado();
        mvc.perform(post("/api/processos").header("Authorization", token(criador)).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(payload(true, destino.getId()))))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.erro").value("Novas demandas devem começar na primeira etapa de trabalho."));
        assertThat(estado()).isEqualTo(antes);
    }

    @Test
    void legadoMantem404ParaEtapaInexistente() throws Exception { // CAD-20
        var criador = usuario("Criador"); var antes = estado();
        mvc.perform(post("/api/processos").header("Authorization", token(criador)).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(payload(true, 999999L))))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.erro").value("Etapa não encontrada neste gerenciamento."));
        assertThat(estado()).isEqualTo(antes);
    }

    private org.springframework.test.web.servlet.ResultActions enviar(Long id, String bearer, Map<String, Object> dados) throws Exception {
        return mvc.perform(post(rota(id)).header("Authorization", bearer).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(dados)));
    }
    private static String rota(Long id) { return "/api/gerenciamentos/" + id + "/demandas"; }
    private static LinkedHashMap<String, Object> payload(boolean legado, Long etapaId) {
        var dados = new LinkedHashMap<String, Object>(); dados.put("numeroProcesso", "Nova"); dados.put("pessoa", "Pessoa");
        if (legado) { dados.put("etapa", Map.of("id", etapaId)); } else { dados.put("versao", 0); }
        return dados;
    }
    private Map<String, Object> estado() { return Map.of("conteudo", conteudoPersistido(), "quadros", jdbc.queryForList("select * from gerenciamentos order by id")); }
}
