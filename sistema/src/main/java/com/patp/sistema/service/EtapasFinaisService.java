package com.patp.sistema.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.patp.sistema.model.CategoriaEtapa;
import com.patp.sistema.model.Etapa;
import com.patp.sistema.model.Gerenciamento;
import com.patp.sistema.repository.EtapaRepository;

@Service
public class EtapasFinaisService {
    private final EtapaRepository etapas;

    public EtapasFinaisService(EtapaRepository etapas) {
        this.etapas = etapas;
    }

    // The caller owns the board lock and the transaction, including the parent version.
    @Transactional(propagation = Propagation.MANDATORY)
    public List<Etapa> garantirPar(Gerenciamento gerenciamento) {
        List<Etapa> existentes = etapas.findByGerenciamentoIdOrderByOrdemAscIdAsc(gerenciamento.getId());
        verificarDuplicidade(existentes);
        int ordem = existentes.stream().filter(e -> e.getCategoria() == CategoriaEtapa.TRABALHO)
                .mapToInt(Etapa::getOrdem).max().orElse(0);
        for (CategoriaEtapa categoria : List.of(CategoriaEtapa.CONCLUIDA, CategoriaEtapa.CANCELADA)) {
            ordem++;
            if (existentes.stream().noneMatch(e -> e.getCategoria() == categoria)) {
                Etapa etapa = new Etapa();
                etapa.setNome(categoria == CategoriaEtapa.CONCLUIDA ? "Concluídos" : "Cancelados");
                etapa.setCategoria(categoria);
                etapa.setSetor(null);
                etapa.setOrdem(ordem);
                etapa.setGerenciamento(gerenciamento);
                existentes.add(etapas.save(etapa));
            }
        }
        return ordenar(existentes);
    }

    public void verificarDuplicidade(List<Etapa> existentes) {
        for (CategoriaEtapa categoria : List.of(CategoriaEtapa.CONCLUIDA, CategoriaEtapa.CANCELADA)) {
            if (existentes.stream().filter(e -> e.getCategoria() == categoria).count() > 1) {
                throw new IllegalStateException("Etapas finais duplicadas para a categoria " + categoria + ".");
            }
        }
    }

    public List<Etapa> ordenar(List<Etapa> existentes) {
        List<Etapa> ordenadas = new ArrayList<>(existentes);
        ordenadas.sort(Comparator.comparing(Etapa::getCategoria)
                .thenComparing(Etapa::getOrdem).thenComparing(Etapa::getId));
        return ordenadas;
    }
}
