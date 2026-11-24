package com.br.reservas.service;

import com.br.reservas.dto.ReservaRequestDTO;
import com.br.reservas.exception.ConflitoException;
import com.br.reservas.exception.RegraDeNegocioException;
import com.br.reservas.model.Reserva;
import com.br.reservas.model.Sala;
import com.br.reservas.model.TipoReserva;
import com.br.reservas.model.Usuario;
import com.br.reservas.repository.ReservaRepository;
import com.br.reservas.repository.SalaRepository;
import com.br.reservas.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Regras de reserva contra um banco de verdade (H2 em memória, modo PostgreSQL).
 *
 * <p>Diferente dos testes com mocks, aqui as consultas JPQL e os argumentos da checagem de conflito
 * são executados de fato. Se a ordem dos parâmetros for trocada, os testes de fronteira falham.</p>
 *
 * <p>Usa o perfil {@code test}, que não define datasource: o H2 é configurado automaticamente.</p>
 */
@DataJpaTest
@ActiveProfiles("test")
@Import(ReservaService.class)
class ReservaServiceIntegracaoTest {

    private static final LocalDateTime DIA = LocalDateTime.of(2026, 12, 1, 0, 0);

    @Autowired
    private ReservaService service;
    @Autowired
    private ReservaRepository reservaRepository;
    @Autowired
    private SalaRepository salaRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;

    private Sala sala;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        sala = salaRepository.save(new Sala("Sala 101", 10));
        usuario = usuarioRepository.save(new Usuario("Ana", "ana@exemplo.com"));
    }

    private ReservaRequestDTO dto(Long salaId, int horaInicio, int horaFim) {
        ReservaRequestDTO dto = new ReservaRequestDTO();
        dto.setSalaId(salaId);
        dto.setUsuarioId(usuario.getId());
        dto.setInicio(DIA.plusHours(horaInicio));
        dto.setFim(DIA.plusHours(horaFim));
        return dto;
    }

    private Reserva reservar(int horaInicio, int horaFim) {
        return service.criar(dto(sala.getId(), horaInicio, horaFim));
    }

    private LocalDateTime hora(int hora, int minuto) {
        return DIA.plusHours(hora).plusMinutes(minuto);
    }

    // --- Conflito de horário ---

    @Test
    void sobreposicaoParcial_lancaConflito() {
        reservar(10, 11);

        assertThatThrownBy(() -> service.criar(dtoComHorarios(sala.getId(), hora(10, 30), hora(11, 30))))
                .isInstanceOf(ConflitoException.class);
    }

    @Test
    void reservaContida_lancaConflito() {
        reservar(10, 12);

        assertThatThrownBy(() -> reservar(10, 11)).isInstanceOf(ConflitoException.class);
    }

    @Test
    void reservaIdentica_lancaConflito() {
        reservar(10, 11);

        assertThatThrownBy(() -> reservar(10, 11)).isInstanceOf(ConflitoException.class);
    }

    @Test
    void fimIgualAoInicioDaOutra_nao_conflita_nosDoisSentidos() {
        reservar(10, 11);

        assertThatCode(() -> reservar(11, 12)).doesNotThrowAnyException(); // começa onde a outra termina
        assertThatCode(() -> reservar(9, 10)).doesNotThrowAnyException();  // termina onde a outra começa
    }

    @Test
    void mesmoHorarioEmOutraSala_naoConflita() {
        reservar(10, 11);
        Sala outraSala = salaRepository.save(new Sala("Sala 102", 5));

        assertThatCode(() -> service.criar(dto(outraSala.getId(), 10, 11))).doesNotThrowAnyException();
    }

    // --- Cancelamento ---

    @Test
    void reservaCancelada_liberaOHorario() {
        Reserva original = reservar(10, 11);
        service.cancelar(original.getId());

        assertThatCode(() -> reservar(10, 11)).doesNotThrowAnyException();
    }

    @Test
    void cancelarDuasVezes_naoLancaErro() {
        Reserva reserva = reservar(10, 11);
        service.cancelar(reserva.getId());

        assertThatCode(() -> service.cancelar(reserva.getId())).doesNotThrowAnyException();
    }

    @Test
    void alterarReservaCancelada_lancaRegraDeNegocio() {
        Reserva reserva = reservar(10, 11);
        service.cancelar(reserva.getId());

        assertThatThrownBy(() -> service.atualizar(reserva.getId(), dto(sala.getId(), 14, 15)))
                .isInstanceOf(RegraDeNegocioException.class);
    }

    // --- Alteração ---

    @Test
    void atualizarMantendoOProprioHorario_naoConflitaConsigoMesma() {
        Reserva reserva = reservar(10, 11);

        assertThatCode(() -> service.atualizar(reserva.getId(), dto(sala.getId(), 10, 11)))
                .doesNotThrowAnyException();
    }

    @Test
    void atualizarInvadindoOutraReserva_lancaConflito() {
        Reserva primeira = reservar(10, 11);
        reservar(12, 13);

        assertThatThrownBy(() -> service.atualizar(primeira.getId(), dto(sala.getId(), 12, 13)))
                .isInstanceOf(ConflitoException.class);
    }

    // --- Sala ---

    @Test
    void salaInativa_naoRecebeReserva() {
        sala.desativar();
        salaRepository.save(sala);

        assertThatThrownBy(() -> reservar(10, 11)).isInstanceOf(RegraDeNegocioException.class);
    }

    // --- Consultas por período ---

    @Test
    void periodo_incluiReservaQueComecaAntesETermina_dentroDaJanela() {
        // Janela das 10h às 12h.
        reservar(7, 8);   // termina antes da janela: fora
        Reserva atravessa = reservar(9, 11); // começa antes e termina dentro: dentro
        reservar(12, 13); // começa onde a janela termina: fora

        List<Reserva> encontradas = service.listarNoPeriodo(hora(10, 0), hora(12, 0));

        assertThat(encontradas).extracting(Reserva::getId).containsExactly(atravessa.getId());
    }

    @Test
    void porSala_naoTraz_reservasDeOutraSala() {
        reservar(10, 11);
        Sala outraSala = salaRepository.save(new Sala("Sala 102", 5));
        service.criar(dto(outraSala.getId(), 10, 11));

        var pagina = service.listarPorSalaNoPeriodo(sala.getId(), hora(0, 0), hora(23, 0),
                PageRequest.of(0, 20));

        assertThat(pagina.getTotalElements()).isEqualTo(1);
        assertThat(pagina.getContent()).allMatch(r -> r.getSala().getId().equals(sala.getId()));
    }

    @Test
    void consultaDeConflito_ignoraReservasCanceladas() {
        Reserva reserva = reservar(10, 11);
        service.cancelar(reserva.getId());

        assertThat(reservaRepository.existsBySala_IdAndTipoReservaAndInicioLessThanAndFimGreaterThan(
                sala.getId(), TipoReserva.ATIVA, hora(11, 0), hora(10, 0))).isFalse();
    }

    private ReservaRequestDTO dtoComHorarios(Long salaId, LocalDateTime inicio, LocalDateTime fim) {
        ReservaRequestDTO dto = new ReservaRequestDTO();
        dto.setSalaId(salaId);
        dto.setUsuarioId(usuario.getId());
        dto.setInicio(inicio);
        dto.setFim(fim);
        return dto;
    }
}
