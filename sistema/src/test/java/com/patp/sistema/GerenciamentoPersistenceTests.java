package com.patp.sistema;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.transaction.support.TransactionTemplate;

import com.patp.sistema.model.Etapa;
import com.patp.sistema.model.Gerenciamento;
import com.patp.sistema.model.Processo;
import com.patp.sistema.repository.EtapaRepository;
import com.patp.sistema.repository.GerenciamentoRepository;
import com.patp.sistema.repository.ProcessoRepository;

@SpringBootTest
@AutoConfigureMockMvc
class GerenciamentoPersistenceTests extends ApiIntegrationSupport {
    @Autowired GerenciamentoRepository quadros;
    @Autowired EtapaRepository etapas;
    @Autowired ProcessoRepository processos;
    @Autowired TransactionTemplate transacao;

    @Test
    void registroNovoTemEstadoAtivoVersaoZeroESemAutoriaInventada() { // T3 GER-08/10 legado
        Gerenciamento novo = new Gerenciamento();
        novo.setNome("Quadro");
        novo = quadros.saveAndFlush(novo);
        Gerenciamento persistido = quadros.findById(novo.getId()).orElseThrow();
        assertThat(persistido.isArquivado()).isFalse();
        assertThat(persistido.getVersao()).isZero();
        assertThat(persistido.getCriador()).isNull();
    }

    @Test
    void filtroIncluiLegadoNuloSemTruncarNomeAntigo() { // T3 GER-08/09/10 legado
        Gerenciamento legado = new Gerenciamento();
        legado.setNome("L".repeat(180));
        legado = quadros.saveAndFlush(legado);
        jdbc.update("update gerenciamentos set arquivado=null where id=?", legado.getId());
        Gerenciamento arquivo = new Gerenciamento();
        arquivo.setNome("Arquivado");
        arquivo.setArquivado(true);
        arquivo = quadros.saveAndFlush(arquivo);
        assertThat(quadros.listarPorEstado(false)).extracting(Gerenciamento::getId).containsExactly(legado.getId());
        assertThat(quadros.listarPorEstado(true)).extracting(Gerenciamento::getId).containsExactly(arquivo.getId());
        Gerenciamento preservado = quadros.findById(legado.getId()).orElseThrow();
        assertThat(preservado.getNome()).isEqualTo("L".repeat(180));
        assertThat(preservado.getCriador()).isNull();
        assertThat(preservado.isArquivado()).isFalse();
    }

    @Test
    void estadoEIncrementeDeVersaoPersistemSobLock() { // T3 GER-16/17/18/22
        Gerenciamento quadro = new Gerenciamento();
        quadro.setNome("Quadro");
        Long id = quadros.saveAndFlush(quadro).getId();
        transacao.executeWithoutResult(status -> {
            Gerenciamento bloqueado = quadros.buscarComLock(id).orElseThrow();
            bloqueado.setArquivado(true);
            quadros.flush();
            assertThat(bloqueado.getVersao()).isEqualTo(1L);
        });
        Gerenciamento salvo = quadros.findById(id).orElseThrow();
        assertThat(salvo.isArquivado()).isTrue();
        assertThat(salvo.getVersao()).isEqualTo(1L);
    }

    @Test
    void vinculosEscalaresResolvemQuadroPersistido() { // T3 GER-20/22 guarda
        Gerenciamento quadro = new Gerenciamento();
        quadro.setNome("Quadro");
        quadro = quadros.saveAndFlush(quadro);
        Etapa etapa = new Etapa();
        etapa.setNome("Etapa");
        etapa.setOrdem(1);
        etapa.setGerenciamento(quadro);
        etapa = etapas.saveAndFlush(etapa);
        Processo demanda = new Processo();
        demanda.setNumeroProcesso("P-1");
        demanda.setPessoa("Pessoa");
        demanda.setEtapa(etapa);
        demanda = processos.saveAndFlush(demanda);
        assertThat(etapas.buscarGerenciamentoId(etapa.getId())).contains(quadro.getId());
        assertThat(processos.buscarGerenciamentoId(demanda.getId())).contains(quadro.getId());
        assertThat(etapas.buscarGerenciamentoId(-1L)).isEmpty();
        assertThat(processos.buscarGerenciamentoId(-1L)).isEmpty();
    }
}
