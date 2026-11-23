package com.br.reservas.model;

/** Status de uma reserva. Reservas {@link #CANCELADA} não entram na checagem de conflito. */
public enum TipoReserva {
    ATIVA,
    CANCELADA
}
