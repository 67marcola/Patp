package com.patp.sistema.service;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.patp.sistema.dto.GerenciamentoResponse;
import com.patp.sistema.dto.GerenciamentoResponse.CriadorResponse;
import com.patp.sistema.exception.ApiException;
import com.patp.sistema.model.Etapa;
import com.patp.sistema.model.Gerenciamento;
import com.patp.sistema.model.Usuario;
import com.patp.sistema.repository.EtapaRepository;
import com.patp.sistema.repository.GerenciamentoRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;

@Service
public class GerenciamentoService {
    // Same whitespace set as JavaScript String.trim, including BOM and NBSP.
    private static final String ESPACOS = "\\x09-\\x0D\\x20\\x{00A0}\\x{1680}\\x{2000}-\\x{200A}"
            + "\\x{2028}\\x{2029}\\x{202F}\\x{205F}\\x{3000}\\x{FEFF}";
    private static final Pattern BORDAS = Pattern.compile("^[" + ESPACOS + "]+|[" + ESPACOS + "]+$");
    private final GerenciamentoRepository gerenciamentos;
    private final EtapaRepository etapas;
    private final SessaoService sessoes;
    private final GerenciamentoGuard guard;
    private final EntityManager entityManager;

    public GerenciamentoService(GerenciamentoRepository gerenciamentos, EtapaRepository etapas,
            SessaoService sessoes, GerenciamentoGuard guard, EntityManager entityManager) {
        this.gerenciamentos = gerenciamentos;
        this.etapas = etapas;
        this.sessoes = sessoes;
        this.guard = guard;
        this.entityManager = entityManager;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public GerenciamentoResponse criarGerenciamento(String token, String nome, String descricao,
            List<Etapa> iniciais) {
        Usuario usuario = usuario(token);
        String nomeValido = validarNome(nome);
        String descricaoValida = validarDescricao(descricao);
        if (iniciais != null) {
            for (Etapa etapa : iniciais) {
                if (etapa == null || normalizar(etapa.getNome()).isEmpty()
                        || normalizar(etapa.getNome()).length() > 255
                        || normalizar(etapa.getSetor()).isEmpty()
                        || normalizar(etapa.getSetor()).length() > 255
                        || etapa.getOrdem() == null || etapa.getOrdem() < 1) {
                    throw new ApiException(HttpStatus.BAD_REQUEST,
                            "Informe nome, setor e ordem válidos para as etapas.");
                }
                etapa.setNome(normalizar(etapa.getNome()));
                etapa.setSetor(normalizar(etapa.getSetor()));
            }
        }
        Gerenciamento gerenciamento = new Gerenciamento();
        gerenciamento.setNome(nomeValido);
        gerenciamento.setDescricao(descricaoValida);
        gerenciamento.setCriador(usuario);
        gerenciamentos.save(gerenciamento);
        if (iniciais != null) {
            for (Etapa etapa : iniciais) {
                etapa.setGerenciamento(gerenciamento);
                etapas.save(etapa);
            }
        }
        gerenciamentos.flush();
        return resposta(gerenciamento, usuario);
    }

    @Transactional(readOnly = true)
    public List<GerenciamentoResponse> listarGerenciamentos(String token, boolean arquivado) {
        Usuario usuario = usuario(token);
        return gerenciamentos.listarPorEstado(arquivado).stream()
                .map(gerenciamento -> resposta(gerenciamento, usuario)).toList();
    }

    @Transactional(readOnly = true)
    public GerenciamentoResponse buscarPorId(String token, Long id) {
        Usuario usuario = usuario(token);
        Gerenciamento gerenciamento = gerenciamentos.findById(id).orElseThrow(() ->
                new ApiException(HttpStatus.NOT_FOUND, "Gerenciamento não encontrado."));
        return resposta(gerenciamento, usuario);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public GerenciamentoResponse editar(String token, Long id, String nome, String descricao, Long versao) {
        Usuario usuario = usuario(token);
        Gerenciamento gerenciamento = guard.bloquear(id);
        guard.exigirAdministracao(gerenciamento, usuario);
        guard.verificarAtivo(gerenciamento);
        validarVersao(versao);
        compararVersao(gerenciamento, versao);
        String nomeValido = validarNome(nome);
        String descricaoValida = validarDescricao(descricao);
        if (Objects.equals(gerenciamento.getNome(), nomeValido)
                && Objects.equals(gerenciamento.getDescricao(), descricaoValida)) {
            entityManager.lock(gerenciamento, LockModeType.PESSIMISTIC_FORCE_INCREMENT);
        } else {
            gerenciamento.setNome(nomeValido);
            gerenciamento.setDescricao(descricaoValida);
        }
        gerenciamentos.flush();
        return resposta(gerenciamento, usuario);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void mudarEstado(String token, Long id, Long versao, boolean arquivado) {
        Usuario usuario = usuario(token);
        Gerenciamento gerenciamento = guard.bloquear(id);
        guard.exigirAdministracao(gerenciamento, usuario);
        validarVersao(versao);
        if (gerenciamento.isArquivado() == arquivado) {
            return;
        }
        compararVersao(gerenciamento, versao);
        gerenciamento.setArquivado(arquivado);
        gerenciamentos.flush();
    }

    private Usuario usuario(String token) {
        return sessoes.buscarUsuario(token);
    }

    GerenciamentoResponse resposta(Gerenciamento gerenciamento, Usuario usuario) {
        Usuario criador = gerenciamento.getCriador();
        return new GerenciamentoResponse(gerenciamento.getId(), gerenciamento.getNome(),
                gerenciamento.getDescricao(), criador == null ? null
                        : new CriadorResponse(criador.getId(), criador.getNome()),
                gerenciamento.isArquivado(), gerenciamento.getVersao(), guard.podeAdministrar(gerenciamento, usuario));
    }

    private void validarVersao(Long versao) {
        if (versao == null || versao < 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Informe uma versão válida do gerenciamento.");
        }
    }

    private void compararVersao(Gerenciamento gerenciamento, Long versao) {
        if (!Objects.equals(gerenciamento.getVersao(), versao)) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "Gerenciamento alterado por outro usuário. Atualize e tente novamente.");
        }
    }

    private String validarNome(String nome) {
        String normalizado = normalizar(nome);
        if (normalizado.isEmpty() || normalizado.length() > 120) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Informe um nome entre 1 e 120 caracteres.");
        }
        return normalizado;
    }

    private String validarDescricao(String descricao) {
        if (descricao != null && descricao.length() > 255) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "A descrição deve ter até 255 caracteres.");
        }
        return descricao == null ? "" : descricao;
    }

    static String normalizar(String texto) {
        return texto == null ? "" : BORDAS.matcher(texto).replaceAll("");
    }
}
