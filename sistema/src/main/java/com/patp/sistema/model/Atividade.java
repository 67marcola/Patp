package com.patp.sistema.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "atividades", indexes = @Index(name = "idx_atividade_quadro_data_id",
        columnList = "gerenciamento_id,data_hora,id"))
public class Atividade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "gerenciamento_id", nullable = false)
    private Gerenciamento gerenciamento;

    @Column(nullable = false)
    private String tipo;

    @Column(name = "alvo_tipo", nullable = false)
    private String alvoTipo;

    @Column(name = "alvo_id", nullable = false)
    private Long alvoId;

    @Column(name = "alvo_nome", nullable = false, columnDefinition = "LONGTEXT")
    private String alvoNome;

    @Column(name = "autor_id", nullable = false)
    private Long autorId;

    @Column(name = "autor_nome", nullable = false, columnDefinition = "LONGTEXT")
    private String autorNome;

    @Column(name = "data_hora", nullable = false, columnDefinition = "TIMESTAMP(6)")
    private LocalDateTime dataHora;

    @Column(name = "alteracoes_json", nullable = false, columnDefinition = "LONGTEXT")
    private String alteracoesJson;

    @Column(name = "historico_id", unique = true)
    private Long historicoId;

    protected Atividade() {
    }

    public Atividade(Gerenciamento gerenciamento, String tipo, String alvoTipo, Long alvoId,
            String alvoNome, Long autorId, String autorNome, LocalDateTime dataHora,
            String alteracoesJson, Long historicoId) {
        this.gerenciamento = gerenciamento;
        this.tipo = tipo;
        this.alvoTipo = alvoTipo;
        this.alvoId = alvoId;
        this.alvoNome = alvoNome;
        this.autorId = autorId;
        this.autorNome = autorNome;
        this.dataHora = dataHora;
        this.alteracoesJson = alteracoesJson;
        this.historicoId = historicoId;
    }

    public Long getId() { return id; }
    public Gerenciamento getGerenciamento() { return gerenciamento; }
    public String getTipo() { return tipo; }
    public String getAlvoTipo() { return alvoTipo; }
    public Long getAlvoId() { return alvoId; }
    public String getAlvoNome() { return alvoNome; }
    public Long getAutorId() { return autorId; }
    public String getAutorNome() { return autorNome; }
    public LocalDateTime getDataHora() { return dataHora; }
    public String getAlteracoesJson() { return alteracoesJson; }
    public Long getHistoricoId() { return historicoId; }
}
