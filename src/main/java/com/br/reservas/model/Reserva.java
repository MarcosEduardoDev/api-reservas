package com.br.reservas.model;

import com.br.reservas.exception.DadosInvalidosException;
import com.br.reservas.exception.RegraDeNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.time.LocalDateTime;

/**
 * Reserva de uma sala por um usuário em um período.
 *
 * <h2>Período</h2>
 * <p>O período é <b>semiaberto</b>: [início, fim). O início faz parte da reserva; o fim não.
 * Por isso uma reserva que termina às 11h não ocupa o horário das 11h, e outra pode começar exatamente nesse horário.</p>
 *
 * <p>Exemplos:</p>
 * <ul>
 *   <li>10h–11h e 11h–12h: <b>não</b> conflitam (fim igual ao início é permitido);</li>
 *   <li>10h–11h e 10h30–11h30: <b>conflitam</b>;</li>
 *   <li>10h–12h e 10h–11h: <b>conflitam</b>;</li>
 *   <li>10h–11h e 09h–10h: <b>não</b> conflitam.</li>
 * </ul>
 *
 * <h2>Status</h2>
 * <pre>
 *   ATIVA ──cancelar()──▶ CANCELADA
 *   CANCELADA ──▶ (sem transição de volta)
 * </pre>
 * <p>Reservas canceladas não entram na checagem de conflito e não podem mais ser alteradas.
 * Cancelar uma reserva já cancelada não gera erro: a operação é idempotente.</p>
 */
@Entity
public class Reserva {

    /** Gerado pelo banco. Fica nulo até a reserva ser salva. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Início do período (inclusivo). */
    @Column(nullable = false)
    private LocalDateTime inicio;

    /** Fim do período (exclusivo). */
    @Column(nullable = false)
    private LocalDateTime fim;

    /*
     * LAZY: a sala só é carregada do banco se for usada. Uma reserva precisa da sala
     * para existir, por isso a coluna é obrigatória.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sala_id", nullable = false)
    private Sala sala;

    /** LAZY pelo mesmo motivo de {@link #sala}. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    /** STRING grava o nome do enum ("ATIVA"), que continua legível se a ordem dos valores mudar. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoReserva tipoReserva;

    protected Reserva() {
        // Construtor exigido pelo JPA.
    }

    public Reserva(LocalDateTime inicio, LocalDateTime fim, Sala sala, Usuario usuario) {
        validarDados(inicio, fim, sala, usuario);
        this.inicio = inicio;
        this.fim = fim;
        this.sala = sala;
        this.usuario = usuario;
        this.tipoReserva = TipoReserva.ATIVA;
    }

    /**
     * Altera período, sala e usuário de uma reserva ativa.
     * A checagem de conflito com outras reservas é responsabilidade do serviço, porque depende do banco.
     */
    public void atualizar(LocalDateTime inicio, LocalDateTime fim, Sala sala, Usuario usuario) {
        garantirNaoCancelada();
        validarDados(inicio, fim, sala, usuario);
        this.inicio = inicio;
        this.fim = fim;
        this.sala = sala;
        this.usuario = usuario;
    }

    /** Cancela a reserva. Chamar em uma reserva já cancelada não faz nada. */
    public void cancelar() {
        this.tipoReserva = TipoReserva.CANCELADA;
    }

    /** Lança exceção se a reserva já foi cancelada. */
    public void garantirNaoCancelada() {
        if (isCancelada()) {
            throw new RegraDeNegocioException("Reserva cancelada não pode ser alterada.");
        }
    }

    /**
     * Valida um período: início e fim obrigatórios, e fim depois do início.
     * Público porque o serviço também valida filtros de consulta com a mesma regra.
     */
    public static void validarPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        if (inicio == null || fim == null) {
            throw new DadosInvalidosException("Início e fim são obrigatórios.");
        }
        if (!fim.isAfter(inicio)) {
            throw new DadosInvalidosException("O fim precisa ser depois do início.");
        }
    }

    /**
     * Indica se dois períodos semiabertos se sobrepõem: [aIni, aFim) e [bIni, bFim).
     * Dois períodos se sobrepõem quando cada um começa antes do fim do outro.
     */
    public static boolean periodosSeSobrepoem(LocalDateTime aInicio, LocalDateTime aFim,
                                              LocalDateTime bInicio, LocalDateTime bFim) {
        return aInicio.isBefore(bFim) && bInicio.isBefore(aFim);
    }

    /** Indica se esta reserva ocupa algum horário do período informado. Reservas canceladas nunca conflitam. */
    public boolean conflitaCom(LocalDateTime inicioNovo, LocalDateTime fimNovo) {
        return isAtiva() && periodosSeSobrepoem(this.inicio, this.fim, inicioNovo, fimNovo);
    }

    public boolean isAtiva() {
        return tipoReserva == TipoReserva.ATIVA;
    }

    public boolean isCancelada() {
        return tipoReserva == TipoReserva.CANCELADA;
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

    public TipoReserva getTipoReserva() {
        return tipoReserva;
    }

    private static void validarDados(LocalDateTime inicio, LocalDateTime fim, Sala sala, Usuario usuario) {
        validarPeriodo(inicio, fim);
        if (sala == null) {
            throw new DadosInvalidosException("Sala é obrigatória.");
        }
        if (usuario == null) {
            throw new DadosInvalidosException("Usuário é obrigatório.");
        }
        if (!sala.isAtiva()) {
            throw new RegraDeNegocioException("Sala inativa não pode receber reservas.");
        }
    }
}
