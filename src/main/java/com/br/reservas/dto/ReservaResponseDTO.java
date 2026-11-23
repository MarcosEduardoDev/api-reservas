package com.br.reservas.dto;

import com.br.reservas.model.TipoReserva;

import java.time.LocalDateTime;

public record ReservaResponseDTO(Long id, Long usuarioId, Long salaId,
                                 LocalDateTime inicio, LocalDateTime fim,
                                 TipoReserva tipoReserva) {
}
