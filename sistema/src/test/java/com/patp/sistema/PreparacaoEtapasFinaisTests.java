package com.patp.sistema;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.core.env.Environment;
import org.springframework.transaction.support.TransactionTemplate;

import com.patp.sistema.model.CategoriaEtapa;
import com.patp.sistema.model.Etapa;
import com.patp.sistema.model.Gerenciamento;
import com.patp.sistema.model.Processo;
import com.patp.sistema.service.EtapasFinaisService;
import com.patp.sistema.service.PreparacaoEtapasFinaisService;
import com.patp.sistema.service.PreparacaoEtapasFinaisService.StatusInventario;

@SpringBootTest
@AutoConfigureMockMvc
class PreparacaoEtapasFinaisTests extends ApiIntegrationSupport {
    @Autowired PreparacaoEtapasFinaisService preparacao;
    @Autowired EtapasFinaisService finais;
    @Autowired TransactionTemplate transacoes;
    @Autowired Environment ambiente;

    private Map<String, Object> estado() {
        var dados = new LinkedHashMap<String, Object>(conteudoPersistido());
        dados.put("gerenciamentos", jdbc.queryForList("select * from gerenciamentos order by id"));
        dados.put("usuarios", jdbc.queryForList("select * from usuarios order by id"));
        return dados;
    }

    private List<Map<String, Object>> semColuna(String tabela, String coluna) {
        return jdbc.queryForList("select * from " + tabela + " order by id").stream().map(linha -> {
            var copia = new LinkedHashMap<>(linha);
            copia.entrySet().removeIf(e -> e.getKey().equalsIgnoreCase(coluna));
            return (Map<String, Object>) copia;
        }).toList();
    }

    private Processo registro(Etapa etapa, String status, int indice) {
        Processo processo = new Processo();
        processo.setNumeroProcesso("Legado-" + etapa.getId() + "-" + indice);
        processo.setPessoa("Pessoa " + indice);
        processo.setResponsavel("Responsável antigo");
        processo.setStatus(status);
        processo.setPrioridade("Alta");
        processo.setDataEmissao(LocalDate.of(2020, 1, 2));
        processo.setPrazoEtapa(LocalDate.of(2020, 2, 3));
        processo.setPrazoGeral(LocalDate.of(2020, 3, 4));
        // Preserve missing and contradictory terminal dates/reasons, regardless of status.
        processo.setDataConclusao(indice % 2 == 0 ? null : LocalDate.of(2019, 1, 1));
        processo.setDataCancelamento(indice % 2 == 0 ? LocalDate.of(2018, 1, 1) : null);
        processo.setMotivoCancelamento(indice % 2 == 0 ? null : "Motivo antigo contraditório");
        processo.setObservacoes("Observação\noriginal");
        processo.setEtapa(etapa);
        processo = processos.saveAndFlush(processo);
        Timestamp data = Timestamp.valueOf(LocalDateTime.of(2020, 4, 5, 6, 7, 8));
        jdbc.update("insert into comentarios (texto,funcionario,data_hora,processo_id) values (?,?,?,?)",
                "Comentário original", "Autoria original", data, processo.getId());
        jdbc.update("insert into historicos (acao,descricao,data_hora,usuario,processo_id) values (?,?,?,?,?)",
                "Antiga", "Histórico original", data, "Autor antigo", processo.getId());
        return processo;
    }

    private List<Etapa> par(Gerenciamento quadro) {
        return transacoes.execute(tx -> finais.garantirPar(quadro)).stream()
                .filter(e -> e.getCategoria() != CategoriaEtapa.TRABALHO).toList();
    }

    private List<Gerenciamento> cenarioCompleto() {
        var criador = usuario("Criador");
        List<Gerenciamento> resultado = new ArrayList<>();
        String[] status = {"Concluido", "Cancelado", null, "Em andamento", "concluido", "Concluído", "Cancelado ", "cancelado"};
        for (int i = 0; i < 3; i++) {
            var quadro = quadro(i == 2 ? null : criador);
            quadro.setNome("Legado " + i);
            quadro.setArquivado(i == 1);
            quadros.saveAndFlush(quadro);
            var primeira = etapa(quadro, "Concluídos", 9);
            var segunda = etapa(quadro, "Cancelados", 9);
            etapa(quadro, "Lacuna", 20);
            jdbc.update("update etapas set categoria=null where id=?", primeira.getId());
            for (int j = 0; j < status.length; j++) registro(j % 2 == 0 ? primeira : segunda, status[j], j);
            resultado.add(quadro);
        }
        var parcial = quadro(criador); var trabalhoParcial = etapa(parcial, "Trabalho parcial", 7);
        var existentes = par(parcial);
        etapas.deleteById(existentes.get(1).getId());
        registro(trabalhoParcial, "Cancelado", 0); resultado.add(parcial);
        var soReferencia = quadro(criador); var trabalho = etapa(soReferencia, "Já tem finais", 3);
        par(soReferencia); registro(trabalho, "Concluido", 0); resultado.add(soReferencia);
        var preparado = quadro(criador); var pronto = par(preparado);
        registro(pronto.get(0), "Concluido", 0); registro(pronto.get(1), "Cancelado", 1);
        resultado.add(preparado);
        return resultado;
    }

    @Test
    void planoInventariaTodosOsQuadrosValoresExatosEAusenciasSemEscrever() {
        var quadrosCenario = cenarioCompleto(); var antes = estado();
        var plano = preparacao.plano();
        assertThat(plano.quadros()).extracting(q -> q.id()).containsExactlyElementsOf(quadrosCenario.stream().map(Gerenciamento::getId).toList());
        assertThat(plano.finaisCriadas()).isZero();
        assertThat(plano.referenciasAlteradas()).isZero();
        for (int i = 0; i < 3; i++) {
            var inventario = plano.quadros().get(i);
            assertThat(inventario.nome()).isEqualTo("Legado " + i);
            assertThat(inventario.arquivado()).isEqualTo(i == 1);
            assertThat(inventario.criadorId()).isEqualTo(i == 2 ? null : quadrosCenario.get(0).getCriador().getId());
            assertThat(inventario.finaisAusentes()).containsExactly(CategoriaEtapa.CONCLUIDA, CategoriaEtapa.CANCELADA);
            assertThat(inventario.status()).containsExactly(new StatusInventario("Concluido", 1), new StatusInventario("Cancelado", 1),
                    new StatusInventario(null, 1), new StatusInventario("Em andamento", 1), new StatusInventario("concluido", 1),
                    new StatusInventario("Concluído", 1), new StatusInventario("Cancelado ", 1), new StatusInventario("cancelado", 1));
            assertThat(inventario.referenciasAjustar()).isEqualTo(2);
        }
        assertThat(plano.quadros().get(3).finaisAusentes()).containsExactly(CategoriaEtapa.CANCELADA);
        assertThat(plano.quadros().get(4).finaisAusentes()).isEmpty();
        assertThat(plano.quadros().get(4).referenciasAjustar()).isEqualTo(1);
        assertThat(plano.quadros().get(5).referenciasAjustar()).isZero();
        assertThat(estado()).isEqualTo(antes);
    }

    @Test
    void aplicarMudaSomenteReferenciasExatasPreservaTodosOsCamposFilhosEVersoesUmaVez() {
        var quadrosCenario = cenarioCompleto();
        var dadosDemandas = semColuna("processos", "etapa_id");
        var dadosQuadros = semColuna("gerenciamentos", "versao");
        var etapasAntes = jdbc.queryForList("select * from etapas order by id");
        var comentariosAntes = jdbc.queryForList("select * from comentarios order by id");
        var historicosAntes = jdbc.queryForList("select * from historicos order by id");
        var usuariosAntes = jdbc.queryForList("select * from usuarios order by id");
        var processosAntes = processos.findAll();
        var versoesAntes = quadrosCenario.stream().map(q -> quadros.findById(q.getId()).orElseThrow().getVersao()).toList();
        var aplicado = preparacao.aplicar();
        assertThat(aplicado.finaisCriadas()).isEqualTo(7);
        assertThat(aplicado.referenciasAlteradas()).isEqualTo(8);
        assertThat(aplicado.quadros()).extracting(q -> q.finaisCriadas()).containsExactly(2L, 2L, 2L, 1L, 0L, 0L);
        assertThat(aplicado.quadros()).extracting(q -> q.referenciasAlteradas()).containsExactly(2L, 2L, 2L, 1L, 1L, 0L);
        assertThat(semColuna("processos", "etapa_id")).isEqualTo(dadosDemandas);
        assertThat(semColuna("gerenciamentos", "versao")).isEqualTo(dadosQuadros);
        for (var linha : etapasAntes) {
            Object id = linha.entrySet().stream().filter(e -> e.getKey().equalsIgnoreCase("id")).findFirst().orElseThrow().getValue();
            assertThat(jdbc.queryForMap("select * from etapas where id=?", id)).isEqualTo(linha);
        }
        assertThat(jdbc.queryForList("select * from comentarios order by id")).isEqualTo(comentariosAntes);
        assertThat(jdbc.queryForList("select * from historicos order by id")).isEqualTo(historicosAntes);
        assertThat(jdbc.queryForList("select * from usuarios order by id")).isEqualTo(usuariosAntes);
        for (var anterior : processosAntes) {
            var atual = processos.findById(anterior.getId()).orElseThrow();
            boolean finalExata = "Concluido".equals(anterior.getStatus()) || "Cancelado".equals(anterior.getStatus());
            if (finalExata) {
                assertThat(atual.getEtapa().getCategoria()).isEqualTo("Concluido".equals(anterior.getStatus()) ? CategoriaEtapa.CONCLUIDA : CategoriaEtapa.CANCELADA);
                assertThat(atual.getEtapa().getGerenciamento().getId()).isEqualTo(anterior.getEtapa().getGerenciamento().getId());
            } else {
                assertThat(atual.getEtapa().getId()).isEqualTo(anterior.getEtapa().getId());
            }
        }
        for (int i = 0; i < quadrosCenario.size(); i++) {
            var quadro = quadros.findById(quadrosCenario.get(i).getId()).orElseThrow();
            assertThat(quadro.getVersao()).isEqualTo(versoesAntes.get(i) + (i == 5 ? 0 : 1));
            assertThat(aplicado.quadros().get(i).versao()).isEqualTo(quadro.getVersao());
            var finaisQuadro = etapas.findByGerenciamentoIdOrderByOrdemAscIdAsc(quadro.getId()).stream()
                    .filter(e -> e.getCategoria() != CategoriaEtapa.TRABALHO).toList();
            assertThat(finaisQuadro).extracting(Etapa::getCategoria).containsExactly(CategoriaEtapa.CONCLUIDA, CategoriaEtapa.CANCELADA);
            assertThat(finaisQuadro).extracting(Etapa::getNome).containsExactly("Concluídos", "Cancelados");
            assertThat(finaisQuadro).extracting(Etapa::getSetor).containsExactly(null, null);
            assertThat(finaisQuadro.get(0).getId()).isNotEqualTo(finaisQuadro.get(1).getId());
        }
    }

    @Test
    void quadroVazioRecebeSomenteParEIncrementaVersaoUmaVez() {
        var quadro = quadro(null); var antes = semColuna("gerenciamentos", "versao");
        var aplicado = preparacao.aplicar();
        assertThat(aplicado.finaisCriadas()).isEqualTo(2);
        assertThat(aplicado.referenciasAlteradas()).isZero();
        assertThat(etapas.findAll()).extracting(Etapa::getCategoria).containsExactly(CategoriaEtapa.CONCLUIDA, CategoriaEtapa.CANCELADA);
        assertThat(etapas.findAll()).extracting(Etapa::getOrdem).containsExactly(1, 2);
        assertThat(quadros.findById(quadro.getId()).orElseThrow().getVersao()).isEqualTo(1);
        assertThat(semColuna("gerenciamentos", "versao")).isEqualTo(antes);
        assertThat(processos.count()).isZero();
    }

    @Test
    void repeticaoTemZeroEfeitosPreservandoIdsDadosEVersoes() {
        cenarioCompleto(); preparacao.aplicar(); var antes = estado();
        var repeticao = preparacao.aplicar();
        assertThat(repeticao.finaisCriadas()).isZero();
        assertThat(repeticao.referenciasAlteradas()).isZero();
        assertThat(repeticao.quadros()).allSatisfy(q -> {
            assertThat(q.finaisAusentes()).isEmpty();
            assertThat(q.referenciasAjustar()).isZero();
            assertThat(q.finaisCriadas()).isZero();
            assertThat(q.referenciasAlteradas()).isZero();
        });
        assertThat(estado()).isEqualTo(antes);
    }

    @ParameterizedTest
    @ValueSource(strings = {"CONCLUIDA", "CANCELADA"})
    void duplicidadeEmUltimoQuadroReverteTodaExecucao(String categoria) {
        var criador = usuario("Criador"); var primeiro = quadro(criador); registro(etapa(primeiro, "Primeira", 1), "Concluido", 0);
        var ultimo = quadro(null); var oficial = par(ultimo).stream().filter(e -> e.getCategoria().name().equals(categoria)).findFirst().orElseThrow();
        var duplicada = etapa(ultimo, oficial.getNome(), 99); duplicada.setCategoria(oficial.getCategoria()); etapas.saveAndFlush(duplicada);
        var antes = estado();
        assertThatThrownBy(() -> preparacao.aplicar()).isInstanceOf(IllegalStateException.class)
                .hasMessage("Etapas finais duplicadas para a categoria " + categoria + ".");
        assertThat(estado()).isEqualTo(antes);
        assertThatThrownBy(() -> preparacao.plano()).isInstanceOf(IllegalStateException.class)
                .hasMessage("Etapas finais duplicadas para a categoria " + categoria + ".");
        assertThat(estado()).isEqualTo(antes);
    }

    @Test
    void falhaNaUltimaFinalReverteEstruturasReferenciasEVersoesDeQuadrosAnteriores() {
        var criador = usuario("Criador"); var primeiro = quadro(criador); registro(etapa(primeiro, "Primeira", 1), "Concluido", 0);
        var ultimo = quadro(null); ultimo.setArquivado(true); quadros.saveAndFlush(ultimo);
        registro(etapa(ultimo, "Última", 8), "Cancelado", 0); var antes = estado();
        jdbc.execute("alter table etapas add constraint falha_preparacao check (gerenciamento_id <> " + ultimo.getId() + " or categoria <> 'CANCELADA')");
        try {
            assertThatThrownBy(() -> preparacao.aplicar()).isInstanceOf(RuntimeException.class)
                    .satisfies(falha -> assertThat(falha.getMessage().toLowerCase(Locale.ROOT)).contains("falha_preparacao"));
            assertThat(estado()).isEqualTo(antes);
        } finally {
            jdbc.execute("alter table etapas drop constraint falha_preparacao");
        }
    }

    private String[] configuracaoContexto() {
        return new String[]{"--spring.datasource.url=" + ambiente.getProperty("spring.datasource.url"),
                "--spring.datasource.username=" + ambiente.getProperty("spring.datasource.username"),
                "--spring.datasource.password=" + ambiente.getProperty("spring.datasource.password", ""),
                "--spring.datasource.driver-class-name=" + ambiente.getProperty("spring.datasource.driver-class-name"),
                "--spring.jpa.hibernate.ddl-auto=validate", "--spring.jpa.show-sql=false", "--spring.jpa.open-in-view=false",
                "--spring.main.banner-mode=off", "--logging.level.root=ERROR"};
    }

    @Test
    void startupNormalNaoPreparaEEntryPointSoAplicaComAcaoExplicita() {
        var quadro = quadro(null); registro(etapa(quadro, "Legado", 9), "Concluido", 0); var antes = estado();
        var normal = new SpringApplication(SistemaApplication.class); normal.setWebApplicationType(WebApplicationType.NONE);
        try (var contexto = normal.run(configuracaoContexto())) {
            assertThat(contexto.isActive()).isTrue();
            assertThat(estado()).isEqualTo(antes);
        }
        String[] parametros = configuracaoContexto();
        // The operator entry point must force validation even if a destructive DDL mode was supplied.
        parametros[4] = "--spring.jpa.hibernate.ddl-auto=create-drop";
        List<String> plano = new ArrayList<>(); plano.add("plano"); plano.addAll(List.of(parametros));
        PrepararEtapasFinais.main(plano.toArray(String[]::new));
        assertThat(estado()).isEqualTo(antes);
        plano.set(0, "aplicar"); PrepararEtapasFinais.main(plano.toArray(String[]::new));
        assertThat(etapas.count()).isEqualTo(3);
        assertThat(processos.findAll().get(0).getEtapa().getCategoria()).isEqualTo(CategoriaEtapa.CONCLUIDA);
        assertThat(quadros.findById(quadro.getId()).orElseThrow().getVersao()).isEqualTo(1);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "invalid", "PLANO", "--spring.datasource.url=jdbc:invalid:nao-conectar"})
    void mainInvalidoRecusaAntesDeContextoOuConexao(String acao) {
        String[] args = acao.isEmpty() ? new String[0] : new String[]{acao, "--spring.datasource.url=jdbc:invalid:nao-conectar"};
        assertThatThrownBy(() -> PrepararEtapasFinais.main(args)).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Informe a ação plano ou aplicar como primeiro argumento.");
    }
}
