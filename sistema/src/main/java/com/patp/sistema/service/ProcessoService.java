package com.patp.sistema.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.patp.sistema.model.Etapa;
import com.patp.sistema.model.Processo;
import com.patp.sistema.model.Usuario;
import com.patp.sistema.repository.EtapaRepository;
import com.patp.sistema.repository.ProcessoRepository;

@Service
public class ProcessoService {

    private final ProcessoRepository processoRepository;
    private final EtapaRepository etapaRepository;
    private final HistoricoService historicoService;
    private final SessaoService sessaoService;

    public ProcessoService(
            ProcessoRepository processoRepository,
            EtapaRepository etapaRepository,
            HistoricoService historicoService,
            SessaoService sessaoService) {

        this.processoRepository = processoRepository;
        this.etapaRepository = etapaRepository;
        this.historicoService = historicoService;
        this.sessaoService = sessaoService;
    }

    // Criar processo
    public Processo salvar(
            Processo processo,
            String token) {

        Usuario usuario = sessaoService.buscarUsuario(token);

        Processo salvo = processoRepository.save(processo);

        historicoService.registrar(
                salvo.getId(),
                "CRIACAO",
                "Processo criado.",
                usuario.getNome()
        );

        return salvo;
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
    public Processo editar(
            Long id,
            Processo dados,
            String token) {

        Usuario usuario = sessaoService.buscarUsuario(token);

        Processo processo = buscarPorId(id);

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
                throw new RuntimeException(
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
    public Processo mudarEtapa(
            Long processoId,
            Long etapaId,
            String token) {

        Usuario usuario =
                sessaoService.buscarUsuario(token);

        Processo processo =
                buscarPorId(processoId);

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
            throw new RuntimeException(
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
    public Processo concluir(
            Long id,
            String token) {

        Usuario usuario =
                sessaoService.buscarUsuario(token);

        Processo processo =
                buscarPorId(id);

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
    public Processo cancelar(
            Long id,
            String motivo,
            String token) {

        Usuario usuario =
                sessaoService.buscarUsuario(token);

        Processo processo =
                buscarPorId(id);

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
    public void excluir(
            Long id,
            String token) {

        Usuario usuario =
                sessaoService.buscarUsuario(token);

        Processo processo =
                buscarPorId(id);

        historicoService.registrar(
                processo.getId(),
                "EXCLUSAO",
                "Processo excluído.",
                usuario.getNome()
        );

        processoRepository.deleteById(id);
    }
}