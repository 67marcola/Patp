package com.patp.sistema.service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.patp.sistema.dto.ConfiguracaoEtapasResponse;
import com.patp.sistema.dto.EtapaResponse;
import com.patp.sistema.exception.ApiException;
import com.patp.sistema.model.Etapa;
import com.patp.sistema.model.Gerenciamento;
import com.patp.sistema.model.Usuario;
import com.patp.sistema.repository.EtapaRepository;
import com.patp.sistema.repository.ProcessoRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;

@Service
public class EtapaService {
    private final EtapaRepository etapas;
    private final ProcessoRepository processos;
    private final GerenciamentoGuard guard;
    private final GerenciamentoService gerenciamentos;
    private final SessaoService sessoes;
    private final EntityManager entityManager;

    public EtapaService(EtapaRepository etapas, ProcessoRepository processos, GerenciamentoGuard guard,
            GerenciamentoService gerenciamentos, SessaoService sessoes, EntityManager entityManager) {
        this.etapas = etapas;
        this.processos = processos;
        this.guard = guard;
        this.gerenciamentos = gerenciamentos;
        this.sessoes = sessoes;
        this.entityManager = entityManager;
    }

    // A locking read keeps the version and children from different commits out of the same snapshot.
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ConfiguracaoEtapasResponse configuracao(String token, Long gerenciamentoId) {
        Usuario usuario = sessoes.buscarUsuario(token);
        Gerenciamento gerenciamento = guard.bloquear(gerenciamentoId);
        return resposta(gerenciamento, usuario);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ConfiguracaoEtapasResponse criar(String token, Long gerenciamentoId, String nome, String setor,
            Integer ordem, Long versao) {
        Usuario usuario = sessoes.buscarUsuario(token);
        Gerenciamento gerenciamento = administrar(gerenciamentoId, usuario);
        validarVersao(gerenciamento, versao);
        String nomeValido = validarCampo(nome, "Informe um nome de etapa entre 1 e 255 caracteres.");
        String setorValido = validarCampo(setor, "Informe um setor entre 1 e 255 caracteres.");
        List<Etapa> sequencia = sequencia(gerenciamentoId);
        validarPosicao(ordem, sequencia.size() + 1);
        Etapa etapa = new Etapa();
        etapa.setNome(nomeValido);
        etapa.setSetor(setorValido);
        etapa.setOrdem(ordem);
        etapa.setGerenciamento(gerenciamento);
        etapas.save(etapa);
        sequencia.add(ordem - 1, etapa);
        confirmar(gerenciamento, sequencia);
        return resposta(gerenciamento, usuario);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ConfiguracaoEtapasResponse editar(String token, Long gerenciamentoId, Long etapaId,
            String nome, String setor, Integer ordem, Long versao) {
        Usuario usuario = sessoes.buscarUsuario(token);
        Gerenciamento gerenciamento = administrar(gerenciamentoId, usuario);
        Etapa etapa = buscarEtapa(gerenciamentoId, etapaId);
        validarVersao(gerenciamento, versao);
        String nomeValido = validarCampo(nome, "Informe um nome de etapa entre 1 e 255 caracteres.");
        String setorValido = validarCampo(setor, "Informe um setor entre 1 e 255 caracteres.");
        List<Etapa> sequencia = sequencia(gerenciamentoId);
        validarPosicao(ordem, sequencia.size());
        etapa.setNome(nomeValido);
        etapa.setSetor(setorValido);
        sequencia.remove(etapa);
        sequencia.add(ordem - 1, etapa);
        confirmar(gerenciamento, sequencia);
        return resposta(gerenciamento, usuario);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ConfiguracaoEtapasResponse excluir(String token, Long gerenciamentoId, Long etapaId, Long versao) {
        Usuario usuario = sessoes.buscarUsuario(token);
        Gerenciamento gerenciamento = administrar(gerenciamentoId, usuario);
        Etapa etapa = buscarEtapa(gerenciamentoId, etapaId);
        validarVersao(gerenciamento, versao);
        if (processos.existsByEtapaId(etapaId)) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "Esta etapa possui demandas. Mova-as para outra etapa antes de removê-la.");
        }
        List<Etapa> sequencia = sequencia(gerenciamentoId);
        sequencia.remove(etapa);
        etapas.delete(etapa);
        confirmar(gerenciamento, sequencia);
        return resposta(gerenciamento, usuario);
    }

    private Gerenciamento administrar(Long id, Usuario usuario) {
        Gerenciamento gerenciamento = guard.bloquear(id);
        guard.exigirAdministracao(gerenciamento, usuario);
        guard.verificarAtivo(gerenciamento);
        return gerenciamento;
    }

    private Etapa buscarEtapa(Long gerenciamentoId, Long etapaId) {
        Etapa etapa = etapas.findByIdAndGerenciamentoId(etapaId, gerenciamentoId);
        if (etapa == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Etapa não encontrada neste gerenciamento.");
        }
        return etapa;
    }

    private List<Etapa> sequencia(Long gerenciamentoId) {
        return etapas.findByGerenciamentoIdOrderByOrdemAscIdAsc(gerenciamentoId);
    }

    private void confirmar(Gerenciamento gerenciamento, List<Etapa> sequencia) {
        for (int i = 0; i < sequencia.size(); i++) {
            sequencia.get(i).setOrdem(i + 1);
        }
        etapas.flush();
        // Children do not dirty the parent; force exactly one version change, including identical edits.
        entityManager.lock(gerenciamento, LockModeType.PESSIMISTIC_FORCE_INCREMENT);
        entityManager.flush();
    }

    private ConfiguracaoEtapasResponse resposta(Gerenciamento gerenciamento, Usuario usuario) {
        Map<Long, Long> quantidades = processos.contarPorEtapa(gerenciamento.getId()).stream()
                .collect(Collectors.toMap(ProcessoRepository.ContagemEtapa::getEtapaId, ProcessoRepository.ContagemEtapa::getQuantidade));
        List<EtapaResponse> resumos = sequencia(gerenciamento.getId()).stream()
                .map(e -> new EtapaResponse(e.getId(), e.getNome(), e.getSetor(), e.getOrdem(), quantidades.getOrDefault(e.getId(), 0L), e.getCategoria()))
                .toList();
        return new ConfiguracaoEtapasResponse(gerenciamentos.resposta(gerenciamento, usuario), resumos);
    }

    private static String validarCampo(String valor, String mensagem) {
        String normalizado = GerenciamentoService.normalizar(valor);
        if (normalizado.isEmpty() || normalizado.length() > 255) {
            throw new ApiException(HttpStatus.BAD_REQUEST, mensagem);
        }
        return normalizado;
    }

    private static void validarPosicao(Integer ordem, int limite) {
        if (ordem == null || ordem < 1 || ordem > limite) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Escolha uma posição válida para a etapa.");
        }
    }

    private static void validarVersao(Gerenciamento gerenciamento, Long versao) {
        if (versao == null || versao < 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Informe uma versão válida do gerenciamento.");
        }
        if (!Objects.equals(gerenciamento.getVersao(), versao)) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "Gerenciamento alterado por outro usuário. Atualize e tente novamente.");
        }
    }
}
