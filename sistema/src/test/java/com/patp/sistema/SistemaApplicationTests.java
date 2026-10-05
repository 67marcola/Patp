package com.patp.sistema;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SistemaApplicationTests {

	@Autowired
	private DataSource dataSource;

	@Test
	void contextLoads() throws Exception {
		try (Connection connection = dataSource.getConnection()) {
			assertTrue(connection.getMetaData().getURL().startsWith("jdbc:h2:mem:"),
					"Os testes devem usar somente o banco H2 isolado em memória.");
		}
	}

}
