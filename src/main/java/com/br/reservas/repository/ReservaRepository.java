package com.br.reservas.repository;

import com.br.reservas.model.Reserva;
import com.br.reservas.model.TipoReserva;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {


    boolean existsBySala_IdAndTipoReservaAndInicioLessThanAndFimGreaterThan(
            Long idSala,
            TipoReserva tipoReserva,
            LocalDateTime fim,
            LocalDateTime inicio
    );

    boolean existsBySala_IdAndTipoReservaAndInicioLessThanAndFimGreaterThanAndIdNot(
            Long idSala,
            TipoReserva tipoReserva,
            LocalDateTime fim,
            LocalDateTime inicio,
            Long idReserva
    );

    List<Reserva> findByInicioBetween(LocalDateTime inicio, LocalDateTime fim);

    Page<Reserva> findByInicioBetween(LocalDateTime inicio, LocalDateTime fim, Pageable pageable);
}