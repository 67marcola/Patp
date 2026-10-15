package com.patp.sistema.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.core.env.Environment;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    private final Environment environment;

    public CorsConfig(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {

        String origem = "http://localhost:5173";
        if (environment.matchesProfiles("internet")) {
            origem = environment.getRequiredProperty("creral.internet.frontend-origin");
            ValidarAmbienteInternet.validarOrigem(origem);
        }
        registry.addMapping("/api/**")
                .allowedOrigins(origem)
                .allowedMethods(
                        "GET",
                        "POST",
                        "PUT",
                        "DELETE",
                        "OPTIONS"
                )
                .allowedHeaders("*");
    }
}
