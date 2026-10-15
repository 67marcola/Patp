package com.patp.sistema.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.patp.sistema.exception.ApiException;

@Service
public class AprovacaoCadastroService {
    public static final String MENSAGEM_PENDENTE = "Cadastro aguardando aprovação do administrador.";
    private final JdbcTemplate jdbc;
    private final boolean internet;

    public AprovacaoCadastroService(JdbcTemplate jdbc,
            @Value("${creral.internet.enabled:false}") boolean internet) {
        this.jdbc = jdbc;
        this.internet = internet;
    }

    public boolean modoInternet() { return internet; }

    @Transactional
    public void registrarPendente(Long id) {
        if (!internet) return;
        jdbc.update("insert into acessos_usuarios(usuario_id, aprovado) values (?, false)", id);
    }

    public void exigirAprovado(Long id) {
        if (!internet) return;
        var aprovados = jdbc.queryForList(
                "select aprovado from acessos_usuarios where usuario_id=?", Boolean.class, id);
        if (aprovados.isEmpty() || !Boolean.TRUE.equals(aprovados.get(0))) {
            throw new ApiException(HttpStatus.FORBIDDEN, MENSAGEM_PENDENTE);
        }
    }

    public List<Pendente> listarPendentes() {
        if (!internet) return List.of();
        return jdbc.query("""
                select u.id,u.nome,u.setor,u.email from usuarios u
                left join acessos_usuarios a on a.usuario_id=u.id
                where a.usuario_id is null or a.aprovado=false order by u.id
                """, (rs, row) -> new Pendente(rs.getLong("id"), rs.getString("nome"),
                        rs.getString("setor"), rs.getString("email")));
    }

    @Transactional
    public void aprovar(Long id, Long autorId) {
        if (!internet) return;
        var alvo = jdbc.queryForList("select id from usuarios where id=? for update", Long.class, id);
        if (alvo.isEmpty()) throw new ApiException(HttpStatus.NOT_FOUND, "Usuário não encontrado.");
        var estado = jdbc.queryForList(
                "select aprovado from acessos_usuarios where usuario_id=?", Boolean.class, id);
        if (estado.isEmpty()) {
            jdbc.update("""
                    insert into acessos_usuarios(usuario_id,aprovado,aprovado_por,aprovado_em)
                    values (?,true,?,CURRENT_TIMESTAMP(6))
                    """, id, autorId);
        } else if (!Boolean.TRUE.equals(estado.get(0))) {
            jdbc.update("""
                    update acessos_usuarios set aprovado=true,aprovado_por=?,aprovado_em=CURRENT_TIMESTAMP(6)
                    where usuario_id=?
                    """, autorId, id);
        }
    }

    public record Pendente(Long id, String nome, String setor, String email) {}
}
