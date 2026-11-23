package com.br.reservas.repository;

import com.br.reservas.model.Sala;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SalaRepository extends JpaRepository<Sala, Long> {

    /**
     * Busca a sala e trava a linha no banco (SELECT ... FOR UPDATE) até o fim da transação.
     * Assim, dois pedidos de reserva para a mesma sala são processados um depois do outro,
     * e a checagem de conflito sempre enxerga a gravação anterior.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Sala s where s.id = :id")
    Optional<Sala> buscarComBloqueio(@Param("id") Long id);
}
