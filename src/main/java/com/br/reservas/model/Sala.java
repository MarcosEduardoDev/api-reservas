package com.br.reservas.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Sala {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public Integer getCapacidade() {
        return capacidade;
    }

    public boolean isAtiva() {
        return ativa;
    }

    private String nome;

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setCapacidade(Integer capacidade) {
        this.capacidade = capacidade;
    }

    private Integer capacidade;
    private boolean ativa;

    public Sala(String nome, Integer capacidade, boolean ativa) {
        this.nome = nome;
        this.capacidade = capacidade;
        this.ativa = ativa;
    }

    protected Sala() {}
}
