package com.patp.sistema;

import static org.assertj.core.api.Assertions.*;
import java.sql.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;

class PrepararBancoInternetTests {
    static final String URL="jdbc:mysql://127.0.0.1:33823/";
    String schema, limitado;
    Connection alvo;

    static Connection conexao(String banco) throws SQLException {
        return DriverManager.getConnection(URL+banco+"?sslMode=DISABLED&allowPublicKeyRetrieval=true", "root", "");
    }
    @BeforeEach void fixtureGuardada() throws Exception {
        try (var root=conexao(""); var st=root.createStatement(); var rs=st.executeQuery("select @@port,@@datadir")) {
            assertThat(rs.next()).isTrue(); assertThat(rs.getInt(1)).isEqualTo(33823);
            assertThat(rs.getString(2).replace('\\','/').toLowerCase()).isEqualTo(
                    "c:/users/marco/appdata/local/temp/creral-arquivo-049cfd5cc86d4d8b840e41f1710d7a43/mysql/");
        }
        schema="internet_t4_"+UUID.randomUUID().toString().replace("-","").substring(0,12); limitado=schema;
        try (var root=conexao(""); var st=root.createStatement()) { st.execute("create database "+schema); }
        alvo=conexao(schema);
    }
    @AfterEach void limparSomenteProprios() throws Exception {
        if (alvo!=null) alvo.close();
        if (schema!=null && schema.matches("internet_t4_[a-f0-9]{12}")) try (var root=conexao(""); var st=root.createStatement()) {
            st.execute("drop database "+schema);
            st.execute("drop user if exists '"+limitado+"'@'127.0.0.1'");
        }
    }
    long numero(String sql) throws Exception { try(var st=alvo.createStatement();var rs=st.executeQuery(sql)){rs.next();return rs.getLong(1);} }
    List<List<Object>> linhas(String sql) throws Exception {
        var linhas=new ArrayList<List<Object>>();
        try(var st=alvo.createStatement();var rs=st.executeQuery(sql)) {
            while(rs.next()) { var linha=new ArrayList<Object>();for(int i=1;i<=rs.getMetaData().getColumnCount();i++) linha.add(rs.getObject(i));linhas.add(linha); }
        } return linhas;
    }

    @Test void planoNaoGravaCriacaoVaziaCompativelComValidateSemImportarDados() throws Exception {
        assertThat(PrepararBancoInternet.prepararEstrutura(alvo,false)).contains("nenhum dado ou estrutura foi gravado");
        assertThat(PrepararBancoInternet.tabelas(alvo)).isEmpty();
        assertThat(PrepararBancoInternet.prepararEstrutura(alvo,true)).contains("8 tabelas, sem contas ou dados importados");
        assertThat(PrepararBancoInternet.tabelas(alvo)).containsExactly("acessos_usuarios","atividades","comentarios","etapas","gerenciamentos","historicos","processos","usuarios");
        for (String tabela:PrepararBancoInternet.tabelas(alvo)) assertThat(numero("select count(*) from "+tabela)).isZero();
        try (var app=new SpringApplicationBuilder(SistemaApplication.class).web(WebApplicationType.NONE).run(
                "--spring.datasource.url="+URL+schema+"?sslMode=DISABLED&allowPublicKeyRetrieval=true",
                "--spring.datasource.username=root","--spring.datasource.password=","--spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver",
                "--spring.jpa.hibernate.ddl-auto=validate","--spring.sql.init.mode=never","--spring.jpa.show-sql=false",
                "--logging.level.root=ERROR","--spring.main.banner-mode=off","--creral.internet.enabled=true")) {
            assertThat(app.isActive()).isTrue();
            assertThat(numero("select count(*) from usuarios")).isZero();
        }
        assertThatThrownBy(()->PrepararBancoInternet.prepararEstrutura(alvo,true)).hasMessageContaining("Schema alvo não vazio");
        assertThat(PrepararBancoInternet.tabelas(alvo)).hasSize(8);
    }

    @Test void qualquerTabelaRecusadaAntesDePrimeiraCriacao() throws Exception {
        try(var st=alvo.createStatement()) { st.execute("create table preservada(id bigint)");st.execute("insert into preservada values(42)"); }
        assertThatThrownBy(()->PrepararBancoInternet.prepararEstrutura(alvo,false)).hasMessageContaining("não vazio");
        assertThatThrownBy(()->PrepararBancoInternet.prepararEstrutura(alvo,true)).hasMessageContaining("não vazio");
        assertThat(PrepararBancoInternet.tabelas(alvo)).containsExactly("preservada");
        assertThat(numero("select id from preservada")).isEqualTo(42);
    }

    @Test void ddlParcialFalhaPreservaEstruturaERecusaRepeticao() throws Exception {
        try(var root=conexao("");var st=root.createStatement()) {
            st.execute("create user '"+limitado+"'@'127.0.0.1' identified by 'Ficticia_t4!'");
            st.execute("grant create on "+schema+".usuarios to '"+limitado+"'@'127.0.0.1'");
        }
        try(var restrita=DriverManager.getConnection(URL+schema+"?sslMode=DISABLED&allowPublicKeyRetrieval=true",limitado,"Ficticia_t4!")) {
            assertThatThrownBy(()->PrepararBancoInternet.prepararEstrutura(restrita,true)).hasMessageContaining("estrutura parcial preservada");
        }
        assertThat(PrepararBancoInternet.tabelas(alvo)).containsExactly("usuarios");
        assertThatThrownBy(()->PrepararBancoInternet.prepararEstrutura(alvo,true)).hasMessageContaining("não vazio");
        assertThat(numero("select count(*) from usuarios")).isZero();
    }

    @Test void bootstrapExigeIdEmailExatosEPreservaTodasAsOutrasContasESenha() throws Exception {
        PrepararBancoInternet.prepararEstrutura(alvo,true);
        try(var st=alvo.createStatement()) { st.execute("insert into usuarios(id,nome,setor,email,senha,papel) values(1,'Admin','TI','admin@example.test','hash1','FUNCIONARIO'),(2,'Outra','RH','outra@example.test','hash2','FUNCIONARIO')"); }
        var contas=linhas("select * from usuarios order by id");
        assertThatThrownBy(()->PrepararBancoInternet.prepararAdministrador(alvo,1,"outro@example.test")).hasMessageContaining("ID/e-mail não conferem");
        assertThatThrownBy(()->PrepararBancoInternet.prepararAdministrador(alvo,999,"admin@example.test")).hasMessageContaining("ID/e-mail não conferem");
        assertThat(linhas("select * from usuarios order by id")).isEqualTo(contas);
        assertThat(numero("select count(*) from acessos_usuarios")).isZero();
        assertThat(PrepararBancoInternet.prepararAdministrador(alvo,1,"admin@example.test")).contains("ID 1");
        assertThat(linhas("select id,nome,setor,email,senha from usuarios where id=1")).containsExactly(List.of(1L,"Admin","TI","admin@example.test","hash1"));
        assertThat(linhas("select papel from usuarios order by id")).containsExactly(List.of("ADMINISTRADOR"),List.of("FUNCIONARIO"));
        assertThat(linhas("select * from usuarios where id=2")).containsExactly(contas.get(1));
        var primeiro=linhas("select usuario_id,aprovado,aprovado_por,aprovado_em from acessos_usuarios");
        assertThat(numero("select count(*) from acessos_usuarios where usuario_id=1 and aprovado=true and aprovado_por=1 and aprovado_em is not null")).isEqualTo(1);
        PrepararBancoInternet.prepararAdministrador(alvo,1,"admin@example.test");
        assertThat(linhas("select usuario_id,aprovado,aprovado_por,aprovado_em from acessos_usuarios")).isEqualTo(primeiro);
    }

    @Test void falhaBootstrapRevertePapelEConfiguracaoNuncaUsaFallbackLocal() throws Exception {
        PrepararBancoInternet.prepararEstrutura(alvo,true);
        try(var st=alvo.createStatement()) {
            st.execute("insert into usuarios(id,nome,email,senha,papel) values(1,'Admin','admin@example.test','hash','FUNCIONARIO')");
            st.execute("drop table acessos_usuarios");
        }
        var antes=linhas("select * from usuarios");
        assertThatThrownBy(()->PrepararBancoInternet.prepararAdministrador(alvo,1,"admin@example.test")).isInstanceOf(SQLException.class);
        assertThat(linhas("select * from usuarios")).isEqualTo(antes);
        assertThatThrownBy(()->PrepararBancoInternet.executar(new String[]{"--plano"},Map.of())).hasMessageContaining("Configuração obrigatória ausente");
        assertThatThrownBy(()->PrepararBancoInternet.executar(new String[]{"--plano"},Map.of(
                "CRERAL_DATABASE_URL",URL+schema+"?sslMode=DISABLED","CRERAL_DATABASE_USERNAME","ficticio","CRERAL_DATABASE_PASSWORD","segredo-ficticio")))
                .hasMessageContaining("TLS sslMode=VERIFY_IDENTITY").hasMessageNotContaining("segredo-ficticio");
    }
}
