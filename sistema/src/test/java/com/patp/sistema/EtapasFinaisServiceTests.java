package com.patp.sistema;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import com.patp.sistema.model.CategoriaEtapa;
import com.patp.sistema.model.Etapa;
import com.patp.sistema.model.Gerenciamento;
import com.patp.sistema.service.EtapasFinaisService;
import com.patp.sistema.service.GerenciamentoGuard;

@SpringBootTest
@AutoConfigureMockMvc
class EtapasFinaisServiceTests extends ApiIntegrationSupport {
    @Autowired EtapasFinaisService finais;
    @Autowired GerenciamentoGuard guard;
    @Autowired PlatformTransactionManager transactions;

    private List<Etapa> preparar(Gerenciamento quadro) {
        return new TransactionTemplate(transactions).execute(s -> finais.garantirPar(guard.bloquear(quadro.getId())));
    }

    @Test
    void criaParExatoIndependentePorQuadroEReexecucaoPreservaTudo() {
        var criador = usuario("Criador");
        var primeiro = quadro(criador);
        var segundo = quadro(null);
        List<Etapa> a = preparar(primeiro);
        List<Etapa> b = preparar(segundo);
        for (var par : List.of(a, b)) {
            assertThat(par).hasSize(2);
            assertThat(par).extracting(Etapa::getCategoria).containsExactly(CategoriaEtapa.CONCLUIDA, CategoriaEtapa.CANCELADA);
            assertThat(par).extracting(Etapa::getNome).containsExactly("Concluídos", "Cancelados");
            assertThat(par).extracting(Etapa::getSetor).containsExactly(null, null);
            assertThat(par).extracting(Etapa::getOrdem).containsExactly(1, 2);
            assertThat(par.get(0).getId()).isNotNull().isNotEqualTo(par.get(1).getId());
        }
        assertThat(a).extracting(e -> e.getGerenciamento().getId()).containsExactly(primeiro.getId(), primeiro.getId());
        assertThat(b).extracting(e -> e.getGerenciamento().getId()).containsExactly(segundo.getId(), segundo.getId());
        assertThat(a).extracting(Etapa::getId).doesNotContainAnyElementsOf(b.stream().map(Etapa::getId).toList());
        var antes = conteudoPersistido();
        preparar(primeiro);
        preparar(segundo);
        assertThat(conteudoPersistido()).isEqualTo(antes);
        assertThat(quadros.findById(primeiro.getId()).orElseThrow().getVersao()).isZero();
    }

    @Test
    void homonimosLacunasEEmpatesPermanecemTrabalhoOrdenadoPorOrdemEId() {
        var quadro = quadro(usuario("Criador"));
        var a = etapa(quadro, "Concluídos", 8);
        var b = etapa(quadro, "Cancelados", 2);
        var c = etapa(quadro, "Outro", 2);
        jdbc.update("update etapas set categoria=null where id=?", a.getId());
        var trabalhosAntes = jdbc.queryForList("select * from etapas order by id");
        var par = preparar(quadro);
        // Even corrupt/legacy final order cannot place finals among work in the snapshot.
        jdbc.update("update etapas set ordem=0 where categoria in ('CONCLUIDA','CANCELADA')");
        var ordenadas = finais.ordenar(etapas.findByGerenciamentoIdOrderByOrdemAscIdAsc(quadro.getId()));
        assertThat(ordenadas.subList(0, 3)).extracting(Etapa::getId).containsExactly(b.getId(), c.getId(), a.getId());
        assertThat(ordenadas).extracting(Etapa::getCategoria).containsExactly(CategoriaEtapa.TRABALHO, CategoriaEtapa.TRABALHO,
                CategoriaEtapa.TRABALHO, CategoriaEtapa.CONCLUIDA, CategoriaEtapa.CANCELADA);
        assertThat(jdbc.queryForList("select * from etapas where id in (?,?,?) order by id", a.getId(), b.getId(), c.getId()))
                .isEqualTo(trabalhosAntes);
        assertThat(par).hasSize(5);
    }

    @Test
    void criaSomenteFinalAusenteSemAlterarFinalExistente() {
        var quadro = quadro(usuario("Criador"));
        var existente = etapa(quadro, "Concluídos", 1);
        existente.setCategoria(CategoriaEtapa.CONCLUIDA);
        existente.setSetor(null);
        etapas.saveAndFlush(existente);
        var antes = jdbc.queryForMap("select * from etapas where id=?", existente.getId());
        var par = preparar(quadro);
        assertThat(par).hasSize(2);
        assertThat(par.get(0).getId()).isEqualTo(existente.getId());
        assertThat(jdbc.queryForMap("select * from etapas where id=?", existente.getId())).isEqualTo(antes);
        assertThat(par.get(1).getCategoria()).isEqualTo(CategoriaEtapa.CANCELADA);
    }

    @Test
    void duplicidadeAbortaSemCriarFinalAusente() {
        var quadro = quadro(usuario("Criador"));
        for (int i = 0; i < 2; i++) {
            var etapa = etapa(quadro, "Concluídos", i + 1);
            etapa.setCategoria(CategoriaEtapa.CONCLUIDA);
            etapas.saveAndFlush(etapa);
        }
        var antes = conteudoPersistido();
        assertThatThrownBy(() -> preparar(quadro)).isInstanceOf(IllegalStateException.class)
                .hasMessage("Etapas finais duplicadas para a categoria CONCLUIDA.");
        assertThat(conteudoPersistido()).isEqualTo(antes);
    }
}
