package com.patp.sistema.service;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import com.patp.sistema.dto.PaginaAtividadeResponse;
import com.patp.sistema.dto.RegistroAtividadeResponse;
import com.patp.sistema.exception.ApiException;
import com.patp.sistema.service.AtividadeRegistroService.Alteracao;
import tools.jackson.databind.ObjectMapper;

@Service
public class AtividadeService {
    private static final long MAX_SEGURO = 9007199254740991L;
    private record Cursor(long quadro, long atividade, long historico, long deslocamento) { }
    private final JdbcTemplate jdbc;
    private final SessaoService sessoes;
    private final GerenciamentoGuard guard;
    private final ObjectMapper json;

    public AtividadeService(JdbcTemplate jdbc, SessaoService sessoes, GerenciamentoGuard guard, ObjectMapper json) {
        this.jdbc=jdbc;this.sessoes=sessoes;this.guard=guard;this.json=json;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public PaginaAtividadeResponse listar(String token, Long quadroId, String cursor) {
        sessoes.buscarUsuario(token);
        guard.bloquear(quadroId);
        Cursor limite;
        if (cursor == null) {
            long atividade=jdbc.queryForObject("select coalesce(max(id),0) from atividades where gerenciamento_id=?",Long.class,quadroId);
            long historico=jdbc.queryForObject("select coalesce(max(h.id),0) from historicos h join processos p on p.id=h.processo_id join etapas e on e.id=p.etapa_id where e.gerenciamento_id=?",Long.class,quadroId);
            limite=new Cursor(quadroId,atividade,historico,0);
        } else limite=decodificar(cursor,quadroId);
        var registros=jdbc.query("""
                select origem,id,gerenciamento_id,tipo,alvo_tipo,alvo_id,alvo_nome,autor_id,autor_nome,data_hora,descricao,alteracoes_json
                from (
                  select 'AUTOMATICA' as origem,0 as ordem_origem,a.id,a.gerenciamento_id,a.tipo,a.alvo_tipo,a.alvo_id,a.alvo_nome,a.autor_id,a.autor_nome,a.data_hora,null as descricao,a.alteracoes_json
                  from atividades a where a.gerenciamento_id=? and a.id<=?
                  union all
                  select 'HISTORICO_EXISTENTE' as origem,1 as ordem_origem,h.id,e.gerenciamento_id,h.acao as tipo,'DEMANDA' as alvo_tipo,p.id as alvo_id,p.numero_processo as alvo_nome,null as autor_id,h.usuario as autor_nome,h.data_hora,h.descricao,'[]' as alteracoes_json
                  from historicos h join processos p on p.id=h.processo_id join etapas e on e.id=p.etapa_id
                  where e.gerenciamento_id=? and h.id<=? and not exists(select 1 from atividades a where a.historico_id=h.id)
                ) fontes
                order by case when data_hora is null then 1 else 0 end,data_hora desc,ordem_origem,id desc
                limit ? offset ?
                """,(rs,row)->{
                    String origem=rs.getString("origem");long id=rs.getLong("id");var data=rs.getTimestamp("data_hora");
                    Number autor=(Number)rs.getObject("autor_id");
                    List<Alteracao> alteracoes=Arrays.asList(json.readValue(rs.getString("alteracoes_json"),Alteracao[].class));
                    return new RegistroAtividadeResponse(origem+":"+id,origem,id,rs.getLong("gerenciamento_id"),rs.getString("tipo"),rs.getString("alvo_tipo"),rs.getLong("alvo_id"),rs.getString("alvo_nome"),autor==null?null:autor.longValue(),rs.getString("autor_nome"),data==null?null:data.toLocalDateTime(),rs.getString("descricao"),List.copyOf(alteracoes));
                },quadroId,limite.atividade(),quadroId,limite.historico(),51,limite.deslocamento());
        boolean mais=registros.size()>50;
        return new PaginaAtividadeResponse(quadroId,List.copyOf(registros.subList(0,Math.min(50,registros.size()))),
                mais?codificar(new Cursor(quadroId,limite.atividade(),limite.historico(),limite.deslocamento()+50)):null);
    }

    private static Cursor decodificar(String cursor, long quadro) {
        if (cursor.isEmpty() || cursor.length()>512 || !cursor.matches("[A-Za-z0-9_-]+")) throw invalido();
        try {
            byte[] bytes=Base64.getUrlDecoder().decode(cursor);
            if (!Base64.getUrlEncoder().withoutPadding().encodeToString(bytes).equals(cursor)) throw invalido();
            String[] campos=new String(bytes,StandardCharsets.UTF_8).split(":",-1);
            if (campos.length!=4) throw invalido();
            long[] numeros=new long[4];
            for(int i=0;i<4;i++) {
                if (!campos[i].matches("0|[1-9][0-9]*")) throw invalido();
                numeros[i]=Long.parseLong(campos[i]);
                if (numeros[i]>MAX_SEGURO) throw invalido();
            }
            if(numeros[0]<=0 || numeros[0]!=quadro) throw invalido();
            return new Cursor(numeros[0],numeros[1],numeros[2],numeros[3]);
        } catch(IllegalArgumentException erro) { throw invalido(); }
    }
    private static String codificar(Cursor cursor) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString((cursor.quadro()+":"+cursor.atividade()+":"+cursor.historico()+":"+cursor.deslocamento()).getBytes(StandardCharsets.UTF_8));
    }
    private static ApiException invalido() { return new ApiException(HttpStatus.BAD_REQUEST,"Cursor de atividade inválido."); }
}
