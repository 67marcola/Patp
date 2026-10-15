package com.patp.sistema.config;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
@Profile("internet")
public class InternetConfig {
    @Bean
    InitializingBean conferirEstruturaDeAprovacao(JdbcTemplate jdbc) {
        return () -> {
            try {
                jdbc.queryForList("select usuario_id,aprovado,aprovado_por,aprovado_em from acessos_usuarios where 1=0");
            } catch (DataAccessException e) {
                throw new IllegalStateException("Estrutura online de aprovacao incompativel. Execute o preparo explicito da base.");
            }
        };
    }
}
