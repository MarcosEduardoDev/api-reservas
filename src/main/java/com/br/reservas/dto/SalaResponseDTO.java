package com.br.reservas.dto;

import com.br.reservas.model.Sala;

/** Representação de uma sala na API. */
public record SalaResponseDTO(Long id, String nome, Integer capacidade, boolean ativa) {

    public static SalaResponseDTO from(Sala sala) {
        return new SalaResponseDTO(sala.getId(), sala.getNome(), sala.getCapacidade(), sala.isAtiva());
    }
}
