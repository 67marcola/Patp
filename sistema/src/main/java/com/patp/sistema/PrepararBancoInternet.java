package com.patp.sistema;

import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.*;

/** Ferramenta explícita, sem Spring ou leitura de application.properties. */
public final class PrepararBancoInternet {
    private PrepararBancoInternet() {}

    public static void main(String[] args) {
        try {
            System.out.println(executar(args, System.getenv()));
        } catch (Exception e) {
            System.err.println(e instanceof IllegalArgumentException || e instanceof IllegalStateException
                    ? e.getMessage() : "Não foi possível preparar a base online; confira configuração, TLS e permissões.");
            System.exit(1);
        }
    }

    static String executar(String[] args, Map<String,String> ambiente) throws Exception {
        boolean estrutura = args.length == 1 && (args[0].equals("--plano") || args[0].equals("--aplicar"));
        boolean admin = args.length == 5 && args[0].equals("--administrador") && args[1].equals("--id") && args[3].equals("--email");
        if (!estrutura && !admin) throw new IllegalArgumentException(
                "Use --plano, --aplicar ou --administrador --id ID --email EMAIL.");
        Long id = admin ? Long.valueOf(args[2]) : null;
        if (admin && (id <= 0 || args[4].isBlank())) throw new IllegalArgumentException("Informe ID positivo e e-mail exato da conta.");
        String url = ambiente.get("CRERAL_DATABASE_URL");
        String usuario = ambiente.get("CRERAL_DATABASE_USERNAME"), senha = ambiente.get("CRERAL_DATABASE_PASSWORD");
        if (url == null || !url.startsWith("jdbc:mysql://") || usuario == null || usuario.isBlank() || senha == null || senha.isBlank())
            throw new IllegalArgumentException("Configuração obrigatória ausente: CRERAL_DATABASE_URL/USERNAME/PASSWORD.");
        if (!url.matches("(?i).*([?&])sslMode=VERIFY_IDENTITY(&.*)?$"))
            throw new IllegalArgumentException("A base online exige TLS sslMode=VERIFY_IDENTITY e certificado confiável.");
        com.patp.sistema.config.ValidarAmbienteInternet.validarBanco(url);
        try (Connection alvo = DriverManager.getConnection(url, usuario, senha)) {
            return admin ? prepararAdministrador(alvo,id,args[4]) : prepararEstrutura(alvo,args[0].equals("--aplicar"));
        }
    }

    public static List<String> tabelas(Connection alvo) throws SQLException {
        if (alvo.getCatalog() == null || alvo.getCatalog().isBlank())
            throw new IllegalArgumentException("Selecione explicitamente o schema alvo na conexão.");
        List<String> nomes = new ArrayList<>();
        try (ResultSet rs = alvo.getMetaData().getTables(alvo.getCatalog(), null, "%", new String[]{"TABLE","VIEW"})) {
            while (rs.next()) nomes.add(rs.getString("TABLE_NAME"));
        }
        Collections.sort(nomes); return nomes;
    }

    public static String prepararEstrutura(Connection alvo, boolean aplicar) throws Exception {
        var existentes = tabelas(alvo);
        if (!existentes.isEmpty()) throw new IllegalStateException("Schema alvo não vazio; nenhuma criação executada. Tabelas existentes: " + existentes);
        if (!aplicar) return "Plano: criar 8 tabelas na base vazia indicada; nenhum dado ou estrutura foi gravado.";
        String sql;
        try (var recurso = PrepararBancoInternet.class.getResourceAsStream("/internet/estrutura.sql")) {
            if (recurso == null) throw new IllegalStateException("Recurso de estrutura online ausente.");
            sql = new String(recurso.readAllBytes(), StandardCharsets.UTF_8);
        }
        try (Statement ddl = alvo.createStatement()) {
            for (String comando : sql.split(";")) if (!comando.isBlank()) ddl.execute(comando);
        } catch (SQLException falha) {
            throw new IllegalStateException("Preparação não concluída; estrutura parcial preservada. Tabelas existentes: " + tabelas(alvo));
        }
        return "Estrutura online preparada: 8 tabelas, sem contas ou dados importados.";
    }

    public static String prepararAdministrador(Connection alvo, long id, String email) throws SQLException {
        if (id <= 0 || email == null || email.isBlank()) throw new IllegalArgumentException("Informe ID positivo e e-mail exato da conta.");
        boolean autoCommit = alvo.getAutoCommit(); alvo.setAutoCommit(false);
        try {
            try (PreparedStatement conta = alvo.prepareStatement("select email from usuarios where id=? for update")) {
                conta.setLong(1,id);
                try (ResultSet rs = conta.executeQuery()) {
                    if (!rs.next() || !email.equals(rs.getString(1))) throw new IllegalArgumentException("ID/e-mail não conferem; nenhuma conta alterada.");
                }
            }
            try (PreparedStatement papel = alvo.prepareStatement("update usuarios set papel='ADMINISTRADOR' where id=?")) {
                papel.setLong(1,id); papel.executeUpdate();
            }
            Boolean aprovado = null;
            try (PreparedStatement acesso = alvo.prepareStatement("select aprovado from acessos_usuarios where usuario_id=?")) {
                acesso.setLong(1,id);
                try (ResultSet rs = acesso.executeQuery()) { if (rs.next()) aprovado=rs.getBoolean(1); }
            }
            if (!Boolean.TRUE.equals(aprovado)) {
                String sql = aprovado == null
                        ? "insert into acessos_usuarios(aprovado_por,usuario_id,aprovado,aprovado_em) values (?,?,true,CURRENT_TIMESTAMP(6))"
                        : "update acessos_usuarios set aprovado_por=?,aprovado=true,aprovado_em=CURRENT_TIMESTAMP(6) where usuario_id=?";
                try (PreparedStatement acesso = alvo.prepareStatement(sql)) {
                    acesso.setLong(1,id); acesso.setLong(2,id); acesso.executeUpdate();
                }
            }
            alvo.commit(); return "Administrador explicitamente conferido e preparado: ID " + id + ".";
        } catch (SQLException | RuntimeException falha) {
            alvo.rollback(); throw falha;
        } finally { alvo.setAutoCommit(autoCommit); }
    }
}
