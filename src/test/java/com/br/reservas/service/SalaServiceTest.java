package com.br.reservas.service;

import com.br.reservas.exception.ConflitoException;
import com.br.reservas.exception.ResourceNotFoundException;
import com.br.reservas.model.Sala;
import com.br.reservas.repository.ReservaRepository;
import com.br.reservas.repository.SalaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SalaServiceTest {

    private static final Long SALA_ID = 1L;

    @Mock
    private SalaRepository salaRepository;
    @Mock
    private ReservaRepository reservaRepository;

    private SalaService service;

    @BeforeEach
    void setUp() {
        service = new SalaService(salaRepository, reservaRepository);
    }

    @Test
    void excluir_salaSemReservas_removeSala() {
        Sala sala = new Sala("Sala 101", 10);
        when(salaRepository.findById(SALA_ID)).thenReturn(Optional.of(sala));
        when(reservaRepository.existsBySala_Id(SALA_ID)).thenReturn(false);

        service.excluir(SALA_ID);

        verify(salaRepository).delete(sala);
    }

    @Test
    void excluir_salaComReservas_lancaConflitoENaoRemove() {
        Sala sala = new Sala("Sala 101", 10);
        when(salaRepository.findById(SALA_ID)).thenReturn(Optional.of(sala));
        when(reservaRepository.existsBySala_Id(SALA_ID)).thenReturn(true);

        assertThatThrownBy(() -> service.excluir(SALA_ID))
                .isInstanceOf(ConflitoException.class)
                .hasMessage("Sala possui reservas e não pode ser excluída. Desative-a em vez disso.");
        verify(salaRepository, never()).delete(any(Sala.class));
    }

    @Test
    void excluir_salaInexistente_lancaNaoEncontrado() {
        when(salaRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.excluir(SALA_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Sala inexistente.");
        verify(reservaRepository, never()).existsBySala_Id(anyLong());
    }
}
