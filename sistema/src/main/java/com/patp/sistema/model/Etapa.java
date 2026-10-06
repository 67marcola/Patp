package com.patp.sistema.model;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "etapas")
public class Etapa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    private String setor;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(length = 20, nullable = true)
    private CategoriaEtapa categoria = CategoriaEtapa.TRABALHO;

    @Column(nullable = false)
    private Integer ordem;

    @ManyToOne
    @JoinColumn(name = "gerenciamento_id", nullable = false)
    private Gerenciamento gerenciamento;

    public Etapa() {
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

    public String getSetor() {
        return setor;
    }

    public CategoriaEtapa getCategoria() {
        return categoria == null ? CategoriaEtapa.TRABALHO : categoria;
    }

    public void setCategoria(CategoriaEtapa categoria) {
        this.categoria = categoria;
    }

    public void setSetor(String setor) {
        this.setor = setor;
    }

    public Integer getOrdem() {
        return ordem;
    }

    public void setOrdem(Integer ordem) {
        this.ordem = ordem;
    }

    public Gerenciamento getGerenciamento() {
        return gerenciamento;
    }

    public void setGerenciamento(Gerenciamento gerenciamento) {
        this.gerenciamento = gerenciamento;
    }
}
