package com.br.reservas.service;

import com.br.reservas.dto.ReservaRequestDTO;
import com.br.reservas.exception.ConflitoException;
import com.br.reservas.exception.DadosInvalidosException;
import com.br.reservas.exception.RegraDeNegocioException;
import com.br.reservas.exception.ResourceNotFoundException;
import com.br.reservas.model.Reserva;
import com.br.reservas.model.Sala;
import com.br.reservas.model.TipoReserva;
import com.br.reservas.model.Usuario;
import com.br.reservas.repository.ReservaRepository;
import com.br.reservas.repository.SalaRepository;
import com.br.reservas.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservaServiceTest {

    private static final LocalDateTime DIA = LocalDateTime.of(2026, 11, 23, 0, 0);
    private static final Long SALA_ID = 1L;
    private static final Long USUARIO_ID = 2L;
    private static final Long RESERVA_ID = 3L;

    @Mock
    private ReservaRepository reservaRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private SalaRepository salaRepository;

    private ReservaService service;
    private Sala sala;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        service = new ReservaService(reservaRepository, usuarioRepository, salaRepository);
        sala = new Sala("Sala 101", 10);
        ReflectionTestUtils.setField(sala, "id", SALA_ID);
        usuario = new Usuario("Ana", "ana@exemplo.com");
        ReflectionTestUtils.setField(usuario, "id", USUARIO_ID);
    }

    private ReservaRequestDTO dto(int horaInicio, int horaFim) {
        ReservaRequestDTO dto = new ReservaRequestDTO();
        dto.setSalaId(SALA_ID);
        dto.setUsuarioId(USUARIO_ID);
        dto.setInicio(DIA.plusHours(horaInicio));
        dto.setFim(DIA.plusHours(horaFim));
        return dto;
    }

    private void stubSalaEUsuario() {
        when(salaRepository.buscarComBloqueio(SALA_ID)).thenReturn(Optional.of(sala));
        when(usuarioRepository.findById(USUARIO_ID)).thenReturn(Optional.of(usuario));
    }

    private void stubSemConflito() {
        when(reservaRepository.existsBySala_IdAndTipoReservaAndInicioLessThanAndFimGreaterThan(
                eq(SALA_ID), eq(TipoReserva.ATIVA), any(), any())).thenReturn(false);
    }

    private void stubComConflito() {
        when(reservaRepository.existsBySala_IdAndTipoReservaAndInicioLessThanAndFimGreaterThan(
                eq(SALA_ID), eq(TipoReserva.ATIVA), any(), any())).thenReturn(true);
    }

    @Test
    void criar_comHorarioLivre_salvaReservaAtiva() {
        stubSalaEUsuario();
        stubSemConflito();
        when(reservaRepository.save(any(Reserva.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Reserva resultado = service.criar(dto(10, 11));

        assertThat(resultado.getTipoReserva()).isEqualTo(TipoReserva.ATIVA);
        assertThat(resultado.getSala()).isSameAs(sala);
        assertThat(resultado.getUsuario()).isSameAs(usuario);
        verify(reservaRepository).save(any(Reserva.class));
    }

    @Test
    void criar_comFimIgualAoInicioDeOutraReserva_permiteReserva() {
        // A checagem usa o intervalo semiaberto: a sala já reservada termina às 11h, a nova começa às 11h.
        stubSalaEUsuario();
        stubSemConflito();
        when(reservaRepository.save(any(Reserva.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Reserva resultado = service.criar(dto(11, 12));

        assertThat(resultado.getInicio()).isEqualTo(DIA.plusHours(11));
        verify(reservaRepository).save(any(Reserva.class));
    }

    @Test
    void criar_comHorarioOcupado_lancaConflitoENaoSalva() {
        stubSalaEUsuario();
        stubComConflito();

        assertThatThrownBy(() -> service.criar(dto(10, 11)))
                .isInstanceOf(ConflitoException.class)
                .hasMessage("O horário da reserva precisa estar disponível.");
        verify(reservaRepository, never()).save(any());
    }

    @Test
    void criar_comFimAntesDoInicio_lancaDadosInvalidosSemConsultarBanco() {
        assertThatThrownBy(() -> service.criar(dto(11, 10)))
                .isInstanceOf(DadosInvalidosException.class);
        verify(salaRepository, never()).buscarComBloqueio(anyLong());
        verify(reservaRepository, never()).save(any());
    }

    @Test
    void criar_emSalaInativa_lancaRegraDeNegocioENaoSalva() {
        sala.desativar();
        stubSalaEUsuario();

        assertThatThrownBy(() -> service.criar(dto(10, 11)))
                .isInstanceOf(RegraDeNegocioException.class);
        verify(reservaRepository, never()).save(any());
    }

    @Test
    void criar_comSalaInexistente_lancaNaoEncontrado() {
        when(salaRepository.buscarComBloqueio(SALA_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.criar(dto(10, 11)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Sala inexistente.");
    }

    @Test
    void criar_comUsuarioInexistente_lancaNaoEncontrado() {
        when(salaRepository.buscarComBloqueio(SALA_ID)).thenReturn(Optional.of(sala));
        when(usuarioRepository.findById(USUARIO_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.criar(dto(10, 11)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Usuário inexistente.");
    }

    @Test
    void atualizar_mantendoOProprioHorario_naoConflitaComAPropriaReserva() {
        Reserva existente = new Reserva(DIA.plusHours(10), DIA.plusHours(11), sala, usuario);
        when(reservaRepository.buscarComBloqueio(RESERVA_ID)).thenReturn(Optional.of(existente));
        stubSalaEUsuario();
        // Sem outras reservas: a checagem ignora o próprio id.
        when(reservaRepository.existsBySala_IdAndTipoReservaAndInicioLessThanAndFimGreaterThanAndIdNot(
                eq(SALA_ID), eq(TipoReserva.ATIVA), any(), any(), eq(RESERVA_ID))).thenReturn(false);
        when(reservaRepository.save(any(Reserva.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Reserva resultado = service.atualizar(RESERVA_ID, dto(10, 12));

        assertThat(resultado.getFim()).isEqualTo(DIA.plusHours(12));
    }

    @Test
    void atualizar_invadindoOutraReserva_lancaConflito() {
        Reserva existente = new Reserva(DIA.plusHours(10), DIA.plusHours(11), sala, usuario);
        when(reservaRepository.buscarComBloqueio(RESERVA_ID)).thenReturn(Optional.of(existente));
        stubSalaEUsuario();
        when(reservaRepository.existsBySala_IdAndTipoReservaAndInicioLessThanAndFimGreaterThanAndIdNot(
                eq(SALA_ID), eq(TipoReserva.ATIVA), any(), any(), eq(RESERVA_ID))).thenReturn(true);

        assertThatThrownBy(() -> service.atualizar(RESERVA_ID, dto(14, 15)))
                .isInstanceOf(ConflitoException.class);
        verify(reservaRepository, never()).save(any());
    }

    @Test
    void atualizar_reservaCancelada_lancaRegraDeNegocio() {
        Reserva cancelada = new Reserva(DIA.plusHours(10), DIA.plusHours(11), sala, usuario);
        cancelada.cancelar();
        when(reservaRepository.buscarComBloqueio(RESERVA_ID)).thenReturn(Optional.of(cancelada));

        assertThatThrownBy(() -> service.atualizar(RESERVA_ID, dto(14, 15)))
                .isInstanceOf(RegraDeNegocioException.class);
        verify(reservaRepository, never()).save(any());
    }

    @Test
    void cancelar_marcaReservaComoCancelada() {
        Reserva reserva = new Reserva(DIA.plusHours(10), DIA.plusHours(11), sala, usuario);
        when(reservaRepository.buscarComBloqueio(RESERVA_ID)).thenReturn(Optional.of(reserva));

        service.cancelar(RESERVA_ID);

        assertThat(reserva.getTipoReserva()).isEqualTo(TipoReserva.CANCELADA);
        verify(reservaRepository).save(reserva);
    }

    @Test
    void cancelar_reservaInexistente_lancaNaoEncontrado() {
        when(reservaRepository.buscarComBloqueio(RESERVA_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.cancelar(RESERVA_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Reserva inexistente.");
    }

    @Test
    void listarNoPeriodo_comFimAntesDoInicio_lancaDadosInvalidosSemConsultarBanco() {
        assertThatThrownBy(() -> service.listarNoPeriodo(DIA.plusHours(12), DIA.plusHours(10)))
                .isInstanceOf(DadosInvalidosException.class);
        verify(reservaRepository, never()).buscarNoPeriodo(any(), any());
    }
}
