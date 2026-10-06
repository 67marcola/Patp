package com.patp.sistema.service;

import java.time.LocalDate;
import java.sql.SQLException;
import java.util.List;
import java.util.LinkedHashMap;
import tools.jackson.databind.ObjectMapper;
import java.util.Objects;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.patp.sistema.exception.ApiException;
import com.patp.sistema.dto.ConfiguracaoEtapasResponse;
import com.patp.sistema.dto.CriarDemandaRequest;
import com.patp.sistema.model.CategoriaEtapa;
import com.patp.sistema.model.Etapa;
import com.patp.sistema.model.Gerenciamento;
import com.patp.sistema.model.Processo;
import com.patp.sistema.model.Usuario;
import com.patp.sistema.repository.EtapaRepository;
import com.patp.sistema.repository.ProcessoRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;

@Service
public class ProcessoService {

    private final ProcessoRepository processoRepository;
    private final EtapaRepository etapaRepository;
    private final HistoricoService historicoService;
    private final SessaoService sessaoService;
    private final GerenciamentoGuard guard;
    private final EtapaService etapas;
    private final EntityManager entityManager;
    private final ObjectMapper json;

    public ProcessoService(
            ProcessoRepository processoRepository,
            EtapaRepository etapaRepository,
            HistoricoService historicoService,
            SessaoService sessaoService,
            GerenciamentoGuard guard,
            EtapaService etapas,
            EntityManager entityManager,
            ObjectMapper json) {

        this.processoRepository = processoRepository;
        this.etapaRepository = etapaRepository;
        this.historicoService = historicoService;
        this.sessaoService = sessaoService;
        this.guard = guard;
        this.etapas = etapas;
        this.entityManager = entityManager;
        this.json = json;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ConfiguracaoEtapasResponse criar(String token, Long gerenciamentoId, CriarDemandaRequest request) {
        Usuario usuario = sessaoService.buscarUsuario(token);
        guard.bloquearCadastros();
        Gerenciamento gerenciamento = guard.exigirAtivo(gerenciamentoId);
        validarVersao(gerenciamento, request.versao());
        Etapa primeira = primeiraTrabalho(gerenciamentoId);
        if (primeira == null) {
            throw new ApiException(HttpStatus.CONFLICT, "Cadastre uma etapa de trabalho antes de criar demandas.");
        }
        salvarInicial(request, primeira, usuario);
        entityManager.lock(gerenciamento, LockModeType.PESSIMISTIC_FORCE_INCREMENT);
        entityManager.flush();
        return etapas.resposta(gerenciamento, usuario);
    }

    // Criar processo
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Processo salvar(
            Processo processo,
            String token) {

        Usuario usuario = sessaoService.buscarUsuario(token);

        if (processo.getId() != null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "O ID do processo deve ser definido pelo sistema.");
        }
        if (processo.getEtapa() == null || processo.getEtapa().getId() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Informe uma etapa existente para o processo.");
        }
        Long etapaId = processo.getEtapa().getId();
        Long gerenciamentoId = etapaRepository.buscarGerenciamentoId(etapaId).orElseThrow(() ->
                new ApiException(HttpStatus.NOT_FOUND, "Etapa não encontrada neste gerenciamento."));
        guard.bloquearCadastros();
        guard.exigirAtivo(gerenciamentoId);
        Etapa etapaPersistida = etapaRepository.findByIdAndGerenciamentoId(etapaId, gerenciamentoId);
        if (etapaPersistida == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Etapa não encontrada neste gerenciamento.");
        }
        Etapa primeira = primeiraTrabalho(gerenciamentoId);
        if (primeira == null || !Objects.equals(primeira.getId(), etapaId)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Novas demandas devem começar na primeira etapa de trabalho.");
        }
        if (processo.getStatus() != null && !processo.getStatus().equals("Em andamento")
                || processo.getDataConclusao() != null || processo.getDataCancelamento() != null || processo.getMotivoCancelamento() != null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "O estado inicial da demanda é definido pelo sistema.");
        }
        return salvarInicial(new CriarDemandaRequest(null, processo.getNumeroProcesso(), processo.getPessoa(),
                processo.getResponsavel(), processo.getPrioridade(), processo.getDataEmissao(), processo.getPrazoEtapa(),
                processo.getPrazoGeral(), processo.getObservacoes()), etapaPersistida, usuario);
    }

    private Etapa primeiraTrabalho(Long gerenciamentoId) {
        return etapaRepository.findByGerenciamentoIdOrderByOrdemAscIdAsc(gerenciamentoId).stream()
                .filter(e -> e.getCategoria() == CategoriaEtapa.TRABALHO).findFirst().orElse(null);
    }

    private Processo salvarInicial(CriarDemandaRequest request, Etapa etapa, Usuario usuario) {
        Processo processo = new Processo();
        processo.setNumeroProcesso(obrigatorio(request.numeroProcesso(), "Informe um número de demanda entre 1 e 255 caracteres."));
        processo.setPessoa(obrigatorio(request.pessoa(), "Informe um cliente/solicitante entre 1 e 255 caracteres."));
        processo.setResponsavel(opcional(request.responsavel(), 255, "Informe um responsável com até 255 caracteres."));
        processo.setPrioridade(opcional(request.prioridade(), 255, "Informe uma prioridade com até 255 caracteres."));
        processo.setObservacoes(opcional(request.observacoes(), 10000, "Informe observações com até 10000 caracteres."));
        processo.setDataEmissao(request.dataEmissao()); processo.setPrazoEtapa(request.prazoEtapa()); processo.setPrazoGeral(request.prazoGeral());
        processo.setStatus("Em andamento"); processo.setEtapa(etapa);
        if (processoRepository.existsByNumeroProcessoIgnoreCase(processo.getNumeroProcesso())) {
            throw duplicado();
        }
        Processo salvo;
        try {
            salvo = processoRepository.saveAndFlush(processo);
        } catch (DataIntegrityViolationException erro) {
            if (violacaoUnica(erro)) { throw duplicado(); }
            throw erro;
        }
        historicoService.registrar(salvo.getId(), "CRIACAO", "Processo criado.", usuario.getNome());
        entityManager.flush();
        return salvo;
    }

    private static String obrigatorio(String valor, String mensagem) {
        String normalizado = GerenciamentoService.normalizar(valor);
        if (normalizado.isEmpty() || normalizado.length() > 255) { throw new ApiException(HttpStatus.BAD_REQUEST, mensagem); }
        return normalizado;
    }

    private static String opcional(String valor, int limite, String mensagem) {
        String normalizado = GerenciamentoService.normalizar(valor);
        if (normalizado.length() > limite) { throw new ApiException(HttpStatus.BAD_REQUEST, mensagem); }
        return normalizado.isEmpty() ? null : normalizado;
    }

    private static void validarVersao(Gerenciamento gerenciamento, Long versao) {
        if (versao == null || versao < 0) { throw new ApiException(HttpStatus.BAD_REQUEST, "Informe uma versão válida do gerenciamento."); }
        if (!Objects.equals(gerenciamento.getVersao(), versao)) {
            throw new ApiException(HttpStatus.CONFLICT, "Gerenciamento alterado por outro usuário. Atualize e tente novamente.");
        }
    }

    private static ApiException duplicado() { return new ApiException(HttpStatus.CONFLICT, "Já existe uma demanda com esse número."); }

    private static boolean violacaoUnica(Throwable erro) {
        for (Throwable causa = erro; causa != null; causa = causa.getCause()) {
            if (causa instanceof ConstraintViolationException violacao && violacao.getKind() == ConstraintViolationException.ConstraintKind.UNIQUE) { return true; }
            if (causa instanceof SQLException sql && ("23505".equals(sql.getSQLState()) || sql.getErrorCode() == 1062)) { return true; }
        }
        return false;
    }

    // Listar todos
    public List<Processo> listarTodos() {
        return processoRepository.findAll();
    }

    // Buscar por ID
    public Processo buscarPorId(Long id) {

        return processoRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Processo não encontrado."
                        ));
    }

    // Pesquisar por número ou pessoa
    public List<Processo> pesquisar(String texto) {

        if (texto == null || texto.isBlank()) {
            return processoRepository.findAll();
        }

        return processoRepository
                .findByNumeroProcessoContainingIgnoreCaseOrPessoaContainingIgnoreCase(
                        texto,
                        texto
                );
    }

    // Listar por etapa
    public List<Processo> listarPorEtapa(Long etapaId) {

        return processoRepository.findByEtapaId(etapaId);
    }

    // Editar processo
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Processo editar(
            Long id,
            Processo dados,
            String token) {

        Usuario usuario = sessaoService.buscarUsuario(token);

        Processo processo = buscarParaAlterar(id);

        if (dados.getStatus() != null && !Objects.equals(dados.getStatus(), processo.getStatus())
                || dados.getEtapa() != null && !Objects.equals(dados.getEtapa().getId(), processo.getEtapa().getId())
                || dados.getDataConclusao() != null && !Objects.equals(dados.getDataConclusao(), processo.getDataConclusao())
                || dados.getDataCancelamento() != null && !Objects.equals(dados.getDataCancelamento(), processo.getDataCancelamento())
                || dados.getMotivoCancelamento() != null && !Objects.equals(dados.getMotivoCancelamento(), processo.getMotivoCancelamento())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Use as ações de mover, concluir, cancelar ou reabrir para alterar o ciclo da demanda.");
        }

        String descricao = "Processo alterado.";

        processo.setNumeroProcesso(
                dados.getNumeroProcesso()
        );

        processo.setPessoa(
                dados.getPessoa()
        );

        processo.setResponsavel(
                dados.getResponsavel()
        );

        processo.setPrioridade(
                dados.getPrioridade()
        );

        processo.setDataEmissao(
                dados.getDataEmissao()
        );

        processo.setPrazoEtapa(
                dados.getPrazoEtapa()
        );

        processo.setPrazoGeral(
                dados.getPrazoGeral()
        );

        processo.setObservacoes(
                dados.getObservacoes()
        );

        Processo salvo =
                processoRepository.save(processo);

        historicoService.registrar(
                salvo.getId(),
                "EDICAO",
                descricao,
                usuario.getNome()
        );

        return salvo;
    }

    public enum Acao { MOVER, CONCLUIR, CANCELAR, REABRIR }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ConfiguracaoEtapasResponse transicionar(String token, Long gerenciamentoId, Long demandaId,
            Acao acao, Long etapaId, String motivo, Long versao) {
        Usuario usuario = sessaoService.buscarUsuario(token);
        Processo processo = executarTransicao(usuario, gerenciamentoId, demandaId, acao, etapaId, motivo, versao, true);
        return etapas.resposta(processo.getEtapa().getGerenciamento(), usuario);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Processo mudarEtapa(Long processoId, Long etapaId, String token) {
        return executarTransicao(sessaoService.buscarUsuario(token), null, processoId, Acao.MOVER, etapaId, null, null, false);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Processo concluir(Long id, String token) {
        return executarTransicao(sessaoService.buscarUsuario(token), null, id, Acao.CONCLUIR, null, null, null, false);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Processo cancelar(Long id, String motivo, String token) {
        return executarTransicao(sessaoService.buscarUsuario(token), null, id, Acao.CANCELAR, null, motivo, null, false);
    }

    private Processo executarTransicao(Usuario usuario, Long quadroId, Long id, Acao acao,
            Long destinoId, String motivo, Long versao, boolean exigeVersao) {
        Long persistido = processoRepository.buscarGerenciamentoId(id).orElseThrow(() ->
                new ApiException(HttpStatus.NOT_FOUND, "Demanda não encontrada neste gerenciamento."));
        if (quadroId != null && !Objects.equals(quadroId, persistido)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Demanda não encontrada neste gerenciamento.");
        }
        Gerenciamento quadro = guard.exigirAtivo(persistido);
        guard.exigirAdministracao(quadro, usuario);
        if (exigeVersao) { validarVersao(quadro, versao); }
        Processo processo = processoRepository.findById(id).orElseThrow(() ->
                new ApiException(HttpStatus.NOT_FOUND, "Demanda não encontrada neste gerenciamento."));
        String status = processo.getStatus();
        boolean andamento = "Em andamento".equals(status);
        boolean encerrada = "Concluido".equals(status) || "Cancelado".equals(status);
        if (!andamento && !encerrada) {
            throw new ApiException(HttpStatus.CONFLICT, "Status antigo não reconhecido. Solicite a correção do registro.");
        }
        if (acao == Acao.REABRIR ? !encerrada : !andamento) {
            throw new ApiException(HttpStatus.CONFLICT, "Esta ação não é permitida no estado atual da demanda. Use a reabertura para iniciar um novo ciclo.");
        }
        Etapa anterior = processo.getEtapa();
        if (andamento && anterior.getCategoria() != CategoriaEtapa.TRABALHO) {
            throw new ApiException(HttpStatus.CONFLICT, "Estado da demanda incompatível com a etapa atual. Solicite a correção do registro.");
        }
        Etapa destino;
        String evento;
        String descricao;
        if (acao == Acao.MOVER || acao == Acao.REABRIR) {
            if (destinoId == null || destinoId <= 0) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Informe uma etapa de trabalho válida.");
            }
            destino = etapaRepository.findByIdAndGerenciamentoId(destinoId, persistido);
            if (destino == null) { throw new ApiException(HttpStatus.NOT_FOUND, "Etapa não encontrada neste gerenciamento."); }
            if (destino.getCategoria() != CategoriaEtapa.TRABALHO) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Escolha uma etapa de trabalho; etapas finais usam as ações de concluir e cancelar.");
            }
            if (acao == Acao.MOVER && Objects.equals(anterior.getId(), destino.getId())) {
                throw new ApiException(HttpStatus.CONFLICT, "A demanda já está nesta etapa.");
            }
            evento = acao == Acao.MOVER ? "MUDANCA_ETAPA" : "REABERTURA";
            if (acao == Acao.REABRIR) {
                var anteriores = new LinkedHashMap<String, Object>();
                anteriores.put("statusAnterior", status);
                anteriores.put("etapaAnteriorId", anterior.getId());
                anteriores.put("etapaDestinoId", destino.getId());
                anteriores.put("dataConclusao", processo.getDataConclusao() == null ? null : processo.getDataConclusao().toString());
                anteriores.put("dataCancelamento", processo.getDataCancelamento() == null ? null : processo.getDataCancelamento().toString());
                anteriores.put("motivoCancelamento", processo.getMotivoCancelamento());
                descricao = json.writeValueAsString(anteriores);
                processo.setStatus("Em andamento");
                processo.setDataConclusao(null); processo.setDataCancelamento(null); processo.setMotivoCancelamento(null);
            } else {
                descricao = "Etapa alterada de '" + anterior.getNome() + "' para '" + destino.getNome() + "'.";
            }
        } else {
            String normalizado = acao == Acao.CANCELAR ? opcional(motivo, 10000, "Informe um motivo de cancelamento entre 1 e 10000 caracteres.") : null;
            if (acao == Acao.CANCELAR && normalizado == null) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Informe um motivo de cancelamento entre 1 e 10000 caracteres.");
            }
            CategoriaEtapa categoria = acao == Acao.CONCLUIR ? CategoriaEtapa.CONCLUIDA : CategoriaEtapa.CANCELADA;
            var finais = etapaRepository.findByGerenciamentoIdOrderByOrdemAscIdAsc(persistido).stream()
                    .filter(etapa -> etapa.getCategoria() == categoria).toList();
            if (finais.size() != 1) {
                throw new ApiException(HttpStatus.CONFLICT, "Prepare as etapas finais deste gerenciamento antes de encerrar demandas.");
            }
            destino = finais.get(0);
            boolean concluir = acao == Acao.CONCLUIR;
            processo.setStatus(concluir ? "Concluido" : "Cancelado");
            processo.setDataConclusao(concluir ? LocalDate.now() : null);
            processo.setDataCancelamento(concluir ? null : LocalDate.now());
            processo.setMotivoCancelamento(normalizado);
            evento = concluir ? "CONCLUSAO" : "CANCELAMENTO";
            descricao = concluir ? "Processo concluído." : "Processo cancelado. Motivo: " + normalizado;
        }
        processo.setEtapa(destino);
        Processo salvo = processoRepository.save(processo);
        historicoService.registrar(id, evento, descricao, usuario.getNome());
        entityManager.flush();
        entityManager.lock(quadro, LockModeType.PESSIMISTIC_FORCE_INCREMENT);
        entityManager.flush();
        return salvo;
    }

    // Excluir
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void excluir(
            Long id,
            String token) {

        Usuario usuario =
                sessaoService.buscarUsuario(token);

        Processo processo =
                buscarParaAlterar(id);

        historicoService.registrar(
                processo.getId(),
                "EXCLUSAO",
                "Processo excluído.",
                usuario.getNome()
        );

        processoRepository.deleteById(id);
    }

    private Processo buscarParaAlterar(Long id) {
        Long gerenciamentoId = processoRepository.buscarGerenciamentoId(id)
                .orElseThrow(() -> new RuntimeException("Processo não encontrado."));
        guard.exigirAtivo(gerenciamentoId);
        return buscarPorId(id);
    }
}
