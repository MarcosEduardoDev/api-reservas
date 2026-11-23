package com.br.reservas.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * Dados recebidos para criar ou alterar uma reserva.
 * A ordem "fim depois do início" não é checada aqui; ela faz parte da regra de domínio (ver Reserva).
 */
public class ReservaRequestDTO {

    @NotNull(message = "ID do usuário é obrigatório.")
    private Long usuarioId;

    @NotNull(message = "ID da sala é obrigatório.")
    private Long salaId;

    @NotNull(message = "Data e horário do início da reserva são obrigatórios.")
    private LocalDateTime inicio;

    @NotNull(message = "Data e horário do fim da reserva são obrigatórios.")
    private LocalDateTime fim;

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public Long getSalaId() {
        return salaId;
    }

    public void setSalaId(Long salaId) {
        this.salaId = salaId;
    }

    public LocalDateTime getInicio() {
        return inicio;
    }

    public void setInicio(LocalDateTime inicio) {
        this.inicio = inicio;
    }

    public LocalDateTime getFim() {
        return fim;
    }

    public void setFim(LocalDateTime fim) {
        this.fim = fim;
    }
}
