package com.patp.sistema;

import static org.assertj.core.api.Assertions.*;

import java.util.concurrent.Executors;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import com.patp.sistema.exception.ApiException;
import com.patp.sistema.service.AprovacaoCadastroService;

@SpringJUnitConfig(AprovacaoCadastroServiceInternetTests.Config.class)
class AprovacaoCadastroServiceInternetTests {
    @Configuration @EnableTransactionManagement
    static class Config {
        @Bean DataSource dataSource() { return new DriverManagerDataSource("jdbc:h2:mem:politica-internet;MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", ""); }
        @Bean JdbcTemplate jdbc(DataSource ds) { return new JdbcTemplate(ds); }
        @Bean DataSourceTransactionManager transactionManager(DataSource ds) { return new DataSourceTransactionManager(ds); }
        @Bean AprovacaoCadastroService politica(JdbcTemplate jdbc) { return new AprovacaoCadastroService(jdbc, true); }
    }
    @Autowired JdbcTemplate jdbc;
    @Autowired AprovacaoCadastroService politica;

    @BeforeEach void schemaFicticio() {
        jdbc.execute("drop table if exists acessos_usuarios");
        jdbc.execute("drop table if exists usuarios");
        jdbc.execute("create table usuarios(id bigint primary key,nome varchar(255),setor varchar(255),email varchar(255),senha varchar(255),papel varchar(20))");
        jdbc.execute("create table acessos_usuarios(usuario_id bigint primary key references usuarios(id),aprovado boolean not null,aprovado_por bigint references usuarios(id),aprovado_em timestamp(6))");
        jdbc.update("insert into usuarios values (1,'Ana','TI','ana@example.test','hash','FUNCIONARIO'),(2,'Bia',null,'bia@example.test','hash2','ADMINISTRADOR'),(3,'Caio','RH','caio@example.test','hash3','ADMINISTRADOR')");
    }

    @Test void localNaoExigeNemConsultaTabelaOnline() {
        jdbc.execute("drop table acessos_usuarios");
        var local = new AprovacaoCadastroService(jdbc, false);
        local.registrarPendente(1L); local.exigirAprovado(1L); local.aprovar(1L, 2L);
        assertThat(local.listarPendentes()).isEmpty();
        assertThat(jdbc.queryForObject("select count(*) from usuarios", Long.class)).isEqualTo(3);
    }

    @Test void registroPendenteEAusenciaRecusamAcessoComListaSeguraOrdenada() {
        politica.registrarPendente(1L);
        assertThat(jdbc.queryForMap("select * from acessos_usuarios where usuario_id=1"))
                .containsEntry("APROVADO", false).containsEntry("APROVADO_POR", null).containsEntry("APROVADO_EM", null);
        for (long id : new long[]{1,2}) assertThatThrownBy(() -> politica.exigirAprovado(id))
                .isInstanceOf(ApiException.class).hasMessage(AprovacaoCadastroService.MENSAGEM_PENDENTE);
        assertThat(politica.listarPendentes()).containsExactly(
                new AprovacaoCadastroService.Pendente(1L,"Ana","TI","ana@example.test"),
                new AprovacaoCadastroService.Pendente(2L,"Bia",null,"bia@example.test"),
                new AprovacaoCadastroService.Pendente(3L,"Caio","RH","caio@example.test"));
    }

    @Test void aprovacaoPreservaContaEPrimeiraAutoriaMesmoRepetida() {
        var conta = jdbc.queryForMap("select * from usuarios where id=1");
        politica.registrarPendente(1L); politica.aprovar(1L,2L);
        var primeira = jdbc.queryForMap("select * from acessos_usuarios where usuario_id=1");
        assertThat(primeira).containsEntry("APROVADO",true).containsEntry("APROVADO_POR",2L);
        assertThat(primeira.get("APROVADO_EM")).isNotNull();
        politica.aprovar(1L,3L); politica.exigirAprovado(1L);
        assertThat(jdbc.queryForMap("select * from acessos_usuarios where usuario_id=1")).isEqualTo(primeira);
        assertThat(jdbc.queryForMap("select * from usuarios where id=1")).isEqualTo(conta);
        assertThat(politica.listarPendentes()).extracting(AprovacaoCadastroService.Pendente::id).containsExactly(2L,3L);
    }

    @Test void falhaDeAutoriaReverteEAusenteNaoCriaAcesso() {
        politica.registrarPendente(1L);
        var antes = jdbc.queryForList("select * from acessos_usuarios");
        assertThatThrownBy(() -> politica.aprovar(1L,999L)).isInstanceOf(RuntimeException.class);
        assertThat(jdbc.queryForList("select * from acessos_usuarios")).isEqualTo(antes);
        assertThatThrownBy(() -> politica.aprovar(999L,2L)).isInstanceOfSatisfying(ApiException.class,
                e -> assertThat(e.getStatus().value()).isEqualTo(404));
        assertThat(jdbc.queryForList("select * from acessos_usuarios")).isEqualTo(antes);
    }

    @Test void concorrenciaSemLinhaGuardaUmaUnicaPrimeiraAprovacao() throws Exception {
        var pool = Executors.newFixedThreadPool(2);
        try {
            var a = pool.submit(() -> politica.aprovar(1L,2L));
            var b = pool.submit(() -> politica.aprovar(1L,3L));
            a.get(); b.get();
        } finally { pool.shutdownNow(); }
        var primeira = jdbc.queryForMap("select * from acessos_usuarios where usuario_id=1");
        assertThat(primeira).containsEntry("APROVADO",true);
        assertThat(primeira.get("APROVADO_POR")).isIn(2L,3L);
        assertThat(primeira.get("APROVADO_EM")).isNotNull();
        politica.aprovar(1L,2L);
        assertThat(jdbc.queryForMap("select * from acessos_usuarios where usuario_id=1")).isEqualTo(primeira);
        assertThat(jdbc.queryForObject("select count(*) from acessos_usuarios", Long.class)).isEqualTo(1);
    }
}
