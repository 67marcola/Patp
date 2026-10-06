package com.patp.sistema.service;

import java.time.LocalDate;
import java.sql.SQLException;
import java.util.List;
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

    public ProcessoService(
            ProcessoRepository processoRepository,
            EtapaRepository etapaRepository,
            HistoricoService historicoService,
            SessaoService sessaoService,
            GerenciamentoGuard guard,
            EtapaService etapas,
            EntityManager entityManager) {

        this.processoRepository = processoRepository;
        this.etapaRepository = etapaRepository;
        this.historicoService = historicoService;
        this.sessaoService = sessaoService;
        this.guard = guard;
        this.etapas = etapas;
        this.entityManager = entityManager;
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

        processo.setStatus(
                dados.getStatus()
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

        // Alteração da etapa
        if (dados.getEtapa() != null &&
                dados.getEtapa().getId() != null) {

            Long gerenciamentoId =
                    processo.getEtapa()
                            .getGerenciamento()
                            .getId();

            Etapa novaEtapa =
                    etapaRepository.findByIdAndGerenciamentoId(
                            dados.getEtapa().getId(),
                            gerenciamentoId
                    );

            if (novaEtapa == null) {
                throw new ApiException(HttpStatus.NOT_FOUND,
                        "Etapa não encontrada neste gerenciamento."
                );
            }

            processo.setEtapa(novaEtapa);

            descricao =
                    "Processo alterado e etapa definida como: "
                    + novaEtapa.getNome();
        }

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

    // Mudar etapa
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Processo mudarEtapa(
            Long processoId,
            Long etapaId,
            String token) {

        Usuario usuario =
                sessaoService.buscarUsuario(token);

        Processo processo =
                buscarParaAlterar(processoId);

        Long gerenciamentoId =
                processo.getEtapa()
                        .getGerenciamento()
                        .getId();

        Etapa etapaAnterior =
                processo.getEtapa();

        Etapa novaEtapa =
                etapaRepository.findByIdAndGerenciamentoId(
                        etapaId,
                        gerenciamentoId
                );

        if (novaEtapa == null) {
            throw new ApiException(HttpStatus.NOT_FOUND,
                    "Etapa não encontrada neste gerenciamento."
            );
        }

        processo.setEtapa(novaEtapa);

        Processo salvo =
                processoRepository.save(processo);

        historicoService.registrar(
                salvo.getId(),
                "MUDANCA_ETAPA",
                "Etapa alterada de '"
                        + etapaAnterior.getNome()
                        + "' para '"
                        + novaEtapa.getNome()
                        + "'.",
                usuario.getNome()
        );

        return salvo;
    }

    // Concluir
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Processo concluir(
            Long id,
            String token) {

        Usuario usuario =
                sessaoService.buscarUsuario(token);

        Processo processo =
                buscarParaAlterar(id);

        processo.setStatus("Concluido");
        processo.setDataConclusao(
                LocalDate.now()
        );

        Processo salvo =
                processoRepository.save(processo);

        historicoService.registrar(
                salvo.getId(),
                "CONCLUSAO",
                "Processo concluído.",
                usuario.getNome()
        );

        return salvo;
    }

    // Cancelar
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Processo cancelar(
            Long id,
            String motivo,
            String token) {

        Usuario usuario =
                sessaoService.buscarUsuario(token);

        Processo processo =
                buscarParaAlterar(id);

        processo.setStatus("Cancelado");
        processo.setDataCancelamento(
                LocalDate.now()
        );

        processo.setMotivoCancelamento(
                motivo
        );

        Processo salvo =
                processoRepository.save(processo);

        historicoService.registrar(
                salvo.getId(),
                "CANCELAMENTO",
                "Processo cancelado. Motivo: "
                        + motivo,
                usuario.getNome()
        );

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
