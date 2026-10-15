package com.patp.sistema;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import com.patp.sistema.model.PapelUsuario;
import com.patp.sistema.model.Usuario;
import com.patp.sistema.service.AprovacaoCadastroService;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest(properties="creral.internet.enabled=true") @AutoConfigureMockMvc
class AprovacaoCadastroApiInternetTests extends ApiIntegrationSupport {
    @Autowired AprovacaoCadastroService politica;
    @Autowired ObjectMapper json;
    @Override @BeforeEach void limparDados() {
        jdbc.execute("create table if not exists acessos_usuarios(usuario_id bigint primary key references usuarios(id),aprovado boolean not null,aprovado_por bigint references usuarios(id),aprovado_em timestamp(6))");
        jdbc.update("delete from acessos_usuarios"); super.limparDados();
    }
    Usuario liberado(String nome, PapelUsuario papel) {
        var u = usuario(nome); u.setPapel(papel); usuarios.saveAndFlush(u);
        politica.aprovar(u.getId(),u.getId()); return u;
    }
    @Test void ambasRotasExigemSessaoSemMudarPendencia() throws Exception {
        var alvo=usuario("Pendente"); politica.registrarPendente(alvo.getId());
        var antes=jdbc.queryForList("select * from acessos_usuarios");
        assertThat(mvc.perform(get("/api/administracao/cadastros/pendentes")).andReturn().getResponse().getStatus()).isEqualTo(401);
        assertThat(mvc.perform(post("/api/administracao/cadastros/"+alvo.getId()+"/aprovar")).andReturn().getResponse().getStatus()).isEqualTo(401);
        assertThat(jdbc.queryForList("select * from acessos_usuarios")).isEqualTo(antes);
    }
    @Test void funcionarioAprovadoRecebe403MesmoComBodyAdmin() throws Exception {
        var comum=liberado("Comum",PapelUsuario.FUNCIONARIO); var alvo=usuario("Pendente");
        var antes=jdbc.queryForList("select * from acessos_usuarios");
        assertThat(mvc.perform(get("/api/administracao/cadastros/pendentes").header("Authorization",token(comum))).andReturn().getResponse().getStatus()).isEqualTo(403);
        assertThat(mvc.perform(post("/api/administracao/cadastros/"+alvo.getId()+"/aprovar").header("Authorization",token(comum))
                .contentType("application/json").content("{\"papel\":\"ADMINISTRADOR\"}")).andReturn().getResponse().getStatus()).isEqualTo(403);
        assertThat(jdbc.queryForList("select * from acessos_usuarios")).isEqualTo(antes);
    }
    @Test void adminListaSomenteCamposPublicosOrdenadosEVazio() throws Exception {
        var admin=liberado("Admin",PapelUsuario.ADMINISTRADOR); String auth=token(admin);
        var vazio=mvc.perform(get("/api/administracao/cadastros/pendentes").header("Authorization",auth)).andReturn().getResponse();
        assertThat(vazio.getStatus()).isEqualTo(200); assertThat(json.readTree(vazio.getContentAsString()).isEmpty()).isTrue();
        var a=usuario("Primeiro"); var b=usuario("Segundo"); politica.registrarPendente(b.getId());
        var r=mvc.perform(get("/api/administracao/cadastros/pendentes").header("Authorization",auth)).andReturn().getResponse();
        assertThat(r.getStatus()).isEqualTo(200);
        assertThat(json.readTree(r.getContentAsString())).isEqualTo(json.readTree(json.writeValueAsString(java.util.List.of(
                new AprovacaoCadastroService.Pendente(a.getId(),a.getNome(),a.getSetor(),a.getEmail()),
                new AprovacaoCadastroService.Pendente(b.getId(),b.getNome(),b.getSetor(),b.getEmail())))));
    }
    @Test void aprovarRetornaContratoExatoEPreservaPapelSenhaPrimeiraAutoria() throws Exception {
        var admin=liberado("Admin",PapelUsuario.ADMINISTRADOR); var segundo=liberado("Segundo",PapelUsuario.ADMINISTRADOR);
        var alvo=usuario("Alvo"); politica.registrarPendente(alvo.getId());
        var conta=jdbc.queryForMap("select * from usuarios where id=?",alvo.getId());
        var r=mvc.perform(post("/api/administracao/cadastros/"+alvo.getId()+"/aprovar").header("Authorization",token(admin))
                .contentType("application/json").content("{\"papel\":\"ADMINISTRADOR\",\"aprovadoPor\":999}" )).andReturn().getResponse();
        assertThat(r.getStatus()).isEqualTo(200);
        assertThat(json.readTree(r.getContentAsString())).isEqualTo(json.readTree("{\"id\":"+alvo.getId()+",\"estado\":\"APROVADO\"}"));
        var primeira=jdbc.queryForMap("select * from acessos_usuarios where usuario_id=?",alvo.getId());
        assertThat(primeira).containsEntry("APROVADO",true).containsEntry("APROVADO_POR",admin.getId());
        assertThat(primeira.get("APROVADO_EM")).isNotNull();
        var repetida=mvc.perform(post("/api/administracao/cadastros/"+alvo.getId()+"/aprovar").header("Authorization",token(segundo))).andReturn().getResponse();
        assertThat(repetida.getStatus()).isEqualTo(200);
        assertThat(json.readTree(repetida.getContentAsString())).isEqualTo(json.readTree(r.getContentAsString()));
        assertThat(jdbc.queryForMap("select * from acessos_usuarios where usuario_id=?",alvo.getId())).isEqualTo(primeira);
        assertThat(jdbc.queryForMap("select * from usuarios where id=?",alvo.getId())).isEqualTo(conta);
    }
    @Test void inexistente404EFalhaPersistencia500SemEfeito() throws Exception {
        var admin=liberado("Admin",PapelUsuario.ADMINISTRADOR); String auth=token(admin); var alvo=usuario("Alvo");
        politica.registrarPendente(alvo.getId()); var antes=jdbc.queryForList("select * from acessos_usuarios");
        var r=mvc.perform(post("/api/administracao/cadastros/999999/aprovar").header("Authorization",auth)).andReturn().getResponse();
        assertThat(r.getStatus()).isEqualTo(404); assertThat(json.readTree(r.getContentAsString()).path("erro").asString()).isEqualTo("Usuário não encontrado.");
        jdbc.execute("alter table acessos_usuarios add constraint falha_ficticia check (aprovado=false or usuario_id<>"+alvo.getId()+")");
        try {
            assertThat(mvc.perform(post("/api/administracao/cadastros/"+alvo.getId()+"/aprovar").header("Authorization",auth)).andReturn().getResponse().getStatus()).isEqualTo(500);
            assertThat(jdbc.queryForList("select * from acessos_usuarios")).isEqualTo(antes);
        } finally { jdbc.execute("alter table acessos_usuarios drop constraint falha_ficticia"); }
    }
}
