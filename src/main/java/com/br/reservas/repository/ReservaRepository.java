package com.br.reservas.repository;

import com.br.reservas.model.Reserva;
import com.br.reservas.model.TipoReserva;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/*
 * Convenção dos períodos: uma reserva [r.inicio, r.fim) e um período novo [inicio, fim)
 * se sobrepõem quando r.inicio < fim  E  r.fim > inicio. Ver Reserva.periodosSeSobrepoem.
 */
public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    /** Há uma reserva ATIVA desta sala que se sobrepõe ao período informado? */
    boolean existsBySala_IdAndTipoReservaAndInicioLessThanAndFimGreaterThan(
            Long salaId,
            TipoReserva tipoReserva,
            LocalDateTime fim,
            LocalDateTime inicio
    );

    /** Igual ao anterior, mas ignora a própria reserva (usado na alteração). */
    boolean existsBySala_IdAndTipoReservaAndInicioLessThanAndFimGreaterThanAndIdNot(
            Long salaId,
            TipoReserva tipoReserva,
            LocalDateTime fim,
            LocalDateTime inicio,
            Long id
    );

    /** A sala tem alguma reserva (ativa ou cancelada)? Usado para impedir a exclusão de salas com histórico. */
    boolean existsBySala_Id(Long salaId);

    /** O usuário tem alguma reserva (ativa ou cancelada)? Usado para impedir a exclusão de usuários com histórico. */
    boolean existsByUsuario_Id(Long usuarioId);

    /**
     * Busca a reserva e trava a linha até o fim da transação. Alteração e cancelamento usam isto,
     * para que um não sobrescreva o outro: o segundo espera e enxerga o status já gravado.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Reserva r where r.id = :id")
    Optional<Reserva> buscarComBloqueio(@Param("id") Long id);

    /** Reservas de qualquer sala que se sobrepõem ao período, de qualquer status. */
    @Query("select r from Reserva r where r.inicio < :fim and r.fim > :inicio")
    List<Reserva> buscarNoPeriodo(@Param("inicio") LocalDateTime inicio,
                                  @Param("fim") LocalDateTime fim);

    @Query("select r from Reserva r where r.inicio < :fim and r.fim > :inicio")
    Page<Reserva> buscarNoPeriodoPaginado(@Param("inicio") LocalDateTime inicio,
                                          @Param("fim") LocalDateTime fim,
                                          Pageable pageable);

    /** Reservas de uma sala específica que se sobrepõem ao período, de qualquer status. */
    @Query("select r from Reserva r where r.sala.id = :salaId and r.inicio < :fim and r.fim > :inicio")
    Page<Reserva> buscarPorSalaNoPeriodo(@Param("salaId") Long salaId,
                                         @Param("inicio") LocalDateTime inicio,
                                         @Param("fim") LocalDateTime fim,
                                         Pageable pageable);
}
