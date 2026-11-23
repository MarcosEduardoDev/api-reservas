package com.br.reservas.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class ReservaRequestDTO {

    @NotNull(message = "ID do usuário é obrigatório.")
    private Long usuarioId;
    @NotNull(message = "ID da sala é obrigatório.")
    private Long salaId;
    @NotNull(message = "Data e horário do ínicio da reserva é obrigatório.")
    private LocalDateTime inicio;
    @NotNull(message = "Data e horário do fim da reserva é obrigatório.")
    private LocalDateTime fim;

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public void setSalaId(Long salaId) {
        this.salaId = salaId;
    }

    public void setInicio(LocalDateTime inicio) {
        this.inicio = inicio;
    }

    public void setFim(LocalDateTime fim) {
        this.fim = fim;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public Long getSalaId() {
        return salaId;
    }

    public LocalDateTime getInicio() {
        return inicio;
    }

    public LocalDateTime getFim() {
        return fim;
    }
}
