package com.patp.sistema;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.patp.sistema.config.ValidarAmbienteInternet;

@SpringBootApplication
public class SistemaApplication {

	public static void main(String[] args) {
		var app = new SpringApplication(SistemaApplication.class);
		app.addInitializers(new ValidarAmbienteInternet());
		app.run(args);
	}

}
