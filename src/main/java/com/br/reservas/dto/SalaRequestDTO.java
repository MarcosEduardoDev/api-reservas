package com.br.reservas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class SalaRequestDTO {

    @NotBlank(message = "Nome não pode ser em branco.")
    private String nome;
    @Positive(message = "A capacidade deve ser maior que zero.")
    @NotNull(message = "Capacidade é obrigatória.")
    private Integer capacidade;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Integer getCapacidade() {
        return capacidade;
    }

    public void setCapacidade(Integer capacidade) {
        this.capacidade = capacidade;
    }
}
