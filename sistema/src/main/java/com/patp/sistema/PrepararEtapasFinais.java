package com.patp.sistema;

import java.util.Arrays;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.core.env.MapPropertySource;

import com.patp.sistema.service.PreparacaoEtapasFinaisService;

public class PrepararEtapasFinais {
    public static void main(String[] args) {
        if (args.length == 0 || !("plano".equals(args[0]) || "aplicar".equals(args[0]))) {
            throw new IllegalArgumentException("Informe a ação plano ou aplicar como primeiro argumento.");
        }
        SpringApplication application = new SpringApplication(SistemaApplication.class);
        application.setWebApplicationType(WebApplicationType.NONE);
        // Schema maintenance is separate: even a plan must never execute Hibernate DDL.
        application.addInitializers(contexto -> contexto.getEnvironment().getPropertySources().addFirst(
                new MapPropertySource("preparacao-etapas-finais", Map.of("spring.jpa.hibernate.ddl-auto", "validate"))));
        try (var contexto = application.run(Arrays.copyOfRange(args, 1, args.length))) {
            var preparacao = contexto.getBean(PreparacaoEtapasFinaisService.class);
            System.out.println("plano".equals(args[0]) ? preparacao.plano() : preparacao.aplicar());
        }
    }
}
