package com.br.reservas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** Dados recebidos para criar ou alterar uma sala. */
public class SalaRequestDTO {

    @NotBlank(message = "Nome da sala é obrigatório.")
    @Size(max = 100, message = "Nome da sala pode ter no máximo 100 caracteres.")
    private String nome;

    @NotNull(message = "Capacidade é obrigatória.")
    @Positive(message = "A capacidade deve ser maior que zero.")
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
