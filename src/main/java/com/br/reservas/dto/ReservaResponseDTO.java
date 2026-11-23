package com.br.reservas.dto;

import com.br.reservas.model.Reserva;
import com.br.reservas.model.TipoReserva;

import java.time.LocalDateTime;

/** Representação de uma reserva na API. Expõe ids de sala e usuário, não as entidades completas. */
public record ReservaResponseDTO(Long id, Long usuarioId, Long salaId,
                                 LocalDateTime inicio, LocalDateTime fim,
                                 TipoReserva tipoReserva) {

    public static ReservaResponseDTO from(Reserva reserva) {
        // getId() de um proxy LAZY não carrega a sala/usuário do banco.
        return new ReservaResponseDTO(
                reserva.getId(),
                reserva.getUsuario().getId(),
                reserva.getSala().getId(),
                reserva.getInicio(),
                reserva.getFim(),
                reserva.getTipoReserva()
        );
    }
}
