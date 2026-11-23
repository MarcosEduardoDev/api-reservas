package com.br.reservas.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime inicio;
    private LocalDateTime fim;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sala_id") //Informando ao Hibernate a foreign key
    private Sala sala;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id") //Informando ao Hibernate a foreign key
    private Usuario usuario;
    @Enumerated(EnumType.STRING)
    private TipoReserva tipoReserva;

    public TipoReserva getTipoReserva() {
        return tipoReserva;
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getInicio() {
        return inicio;
    }

    public LocalDateTime getFim() {
        return fim;
    }

    public Sala getSala() {
        return sala;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public Reserva(LocalDateTime inicio, LocalDateTime fim, Sala sala, Usuario usuario) {
        if (inicio == null || fim == null) {
            throw new NullPointerException("As datas de reserva precisam ser válidas.");
        }
        if (fim.isBefore(inicio) || inicio.isEqual(fim)) {
            throw new IllegalArgumentException("O fim precisa ser após o ínicio de uma reserva.");
        }
        this.inicio = inicio;
        this.fim = fim;
        this.sala = sala;
        this.usuario = usuario;
        this.tipoReserva = TipoReserva.ATIVA;
    }

    protected Reserva() {
    }

    public void cancelar() {
        this.tipoReserva = TipoReserva.CANCELADA;
    }

    public void setInicio(LocalDateTime inicio) {
        this.inicio = inicio;
    }

    public void setFim(LocalDateTime fim) {
        this.fim = fim;
    }

    public void setSala(Sala sala) {
        this.sala = sala;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

}
