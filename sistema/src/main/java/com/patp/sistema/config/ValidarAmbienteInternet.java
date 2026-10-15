package com.patp.sistema.config;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Set;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

/** Confere o modo online antes de criar os beans ou conectar ao banco. */
public class ValidarAmbienteInternet implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    @Override
    public void initialize(ConfigurableApplicationContext context) {
        var env = context.getEnvironment();
        boolean perfil = env.matchesProfiles("internet");
        boolean modo = env.getProperty("creral.internet.enabled", Boolean.class, false);
        if (!perfil && !modo) return;
        if (!perfil || !modo) throw falha("Ative o perfil e a politica internet juntos.");
        String url = obrigatorio(env, "CRERAL_DATABASE_URL");
        String usuario = obrigatorio(env, "CRERAL_DATABASE_USERNAME");
        String senha = obrigatorio(env, "CRERAL_DATABASE_PASSWORD");
        if (!url.equals(env.getProperty("spring.datasource.url"))
                || !usuario.equals(env.getProperty("spring.datasource.username"))
                || !senha.equals(env.getProperty("spring.datasource.password"))) {
            throw falha("Use somente a configuracao externa do banco online.");
        }
        validarBanco(url);
        String origem = obrigatorio(env, "CRERAL_FRONTEND_ORIGIN");
        validarOrigem(origem);
        if (!origem.equals(env.getProperty("creral.internet.frontend-origin"))
                || !"validate".equals(env.getProperty("spring.jpa.hibernate.ddl-auto"))
                || !"never".equals(env.getProperty("spring.sql.init.mode"))
                || env.getProperty("spring.jpa.show-sql", Boolean.class, false)) {
            throw falha("Conserve origem, validate, SQL init never e logs SQL desativados.");
        }
    }

    private static String obrigatorio(Environment env, String nome) {
        String valor = env.getProperty(nome);
        if (valor == null || valor.isBlank()) throw falha("Configuracao obrigatoria ausente: " + nome);
        return valor;
    }

    public static void validarOrigem(String valor) {
        try {
            URI uri = URI.create(valor);
            if (!"https".equals(uri.getScheme()) || uri.getHost() == null || uri.getUserInfo() != null
                    || uri.getQuery() != null || uri.getFragment() != null || !uri.getPath().isEmpty()
                    || valor.contains("*") || !valor.equals(valor.trim())) throw new IllegalArgumentException();
        } catch (IllegalArgumentException e) {
            throw falha("Informe origem HTTPS exata, sem caminho, barra final ou curinga.");
        }
    }

    public static void validarBanco(String url) {
        try {
            if (!url.startsWith("jdbc:mysql://")) throw new IllegalArgumentException();
            URI uri = URI.create(url.substring(5));
            if (uri.getHost() == null || uri.getUserInfo() != null || uri.getFragment() != null
                    || !uri.getPath().matches("/[a-zA-Z0-9_]+")) throw new IllegalArgumentException();
            var parametros = new HashMap<String, String>();
            var permitidos = Set.of("sslmode", "trustcertificatekeystoreurl", "trustcertificatekeystoretype",
                    "trustcertificatekeystorepassword", "fallbacktosystemtruststore");
            for (String par : uri.getRawQuery().split("&")) {
                String[] partes = par.split("=", 2);
                if (partes.length != 2) throw new IllegalArgumentException();
                String chave = URLDecoder.decode(partes[0], StandardCharsets.UTF_8).toLowerCase(java.util.Locale.ROOT);
                String valor = URLDecoder.decode(partes[1], StandardCharsets.UTF_8);
                if (!permitidos.contains(chave) || parametros.putIfAbsent(chave, valor) != null) throw new IllegalArgumentException();
            }
            if (!"VERIFY_IDENTITY".equals(parametros.get("sslmode"))
                    || !"false".equals(parametros.get("fallbacktosystemtruststore"))
                    || !"PKCS12".equals(parametros.get("trustcertificatekeystoretype"))
                    || parametros.getOrDefault("trustcertificatekeystorepassword", "").isBlank()) throw new IllegalArgumentException();
            URI store = URI.create(parametros.get("trustcertificatekeystoreurl"));
            if (!"file".equals(store.getScheme()) || !Files.isRegularFile(Path.of(store))
                    || !Files.isReadable(Path.of(store))) throw new IllegalArgumentException();
        } catch (RuntimeException e) {
            throw falha("MySQL exige TLS VERIFY_IDENTITY, CA PKCS12 legivel e nenhuma opcao conflitante.");
        }
    }

    private static IllegalStateException falha(String mensagem) {
        return new IllegalStateException("Configuracao do piloto internet recusada. " + mensagem);
    }
}
