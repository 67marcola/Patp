package com.patp.sistema.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "gerenciamentos")
public class Gerenciamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    private String descricao;

    @ManyToOne
    @JoinColumn(name = "criador_id")
    private Usuario criador;

    @Column(columnDefinition = "boolean default false")
    private Boolean arquivado = false;

    @Version
    @Column(columnDefinition = "bigint default 0")
    private Long versao;

    public Gerenciamento() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Usuario getCriador() {
        return criador;
    }

    public void setCriador(Usuario criador) {
        this.criador = criador;
    }

    public boolean isArquivado() {
        return Boolean.TRUE.equals(arquivado);
    }

    public void setArquivado(Boolean arquivado) {
        this.arquivado = arquivado;
    }

    public Long getVersao() {
        return versao;
    }
}
