package com.br.reservas.model;

import com.br.reservas.exception.DadosInvalidosException;
import com.br.reservas.exception.RegraDeNegocioException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Regras de domínio da reserva. Os horários usam o dia 23/11/2026 para facilitar a leitura.
 * Período semiaberto: [início, fim). Fim igual ao início de outra reserva NÃO é conflito.
 */
class ReservaTest {

    private static final LocalDateTime DIA = LocalDateTime.of(2026, 11, 23, 0, 0);

    private static LocalDateTime as(int hora) {
        return DIA.plusHours(hora);
    }

    private static LocalDateTime asEMeia(int hora) {
        return DIA.plusHours(hora).plusMinutes(30);
    }

    private static Sala salaAtiva() {
        return new Sala("Sala 101", 10);
    }

    private static Usuario usuario() {
        return new Usuario("Ana", "ana@exemplo.com");
    }

    private static Reserva reserva(LocalDateTime inicio, LocalDateTime fim) {
        return new Reserva(inicio, fim, salaAtiva(), usuario());
    }

    @Test
    void criar_comDadosValidos_comecaAtiva() {
        Reserva reserva = reserva(as(10), as(11));

        assertThat(reserva.getTipoReserva()).isEqualTo(TipoReserva.ATIVA);
        assertThat(reserva.isAtiva()).isTrue();
        assertThat(reserva.isCancelada()).isFalse();
    }

    @Test
    void criar_comFimAntesDoInicio_lancaDadosInvalidos() {
        assertThatThrownBy(() -> reserva(as(11), as(10)))
                .isInstanceOf(DadosInvalidosException.class)
                .hasMessage("O fim precisa ser depois do início.");
    }

    @Test
    void criar_comFimIgualAoInicio_lancaDadosInvalidos() {
        // Uma reserva precisa ter duração positiva. Fim igual ao início é inválido por si só.
        assertThatThrownBy(() -> reserva(as(10), as(10)))
                .isInstanceOf(DadosInvalidosException.class);
    }

    @Test
    void criar_comDatasNulas_lancaDadosInvalidos() {
        assertThatThrownBy(() -> reserva(null, as(11)))
                .isInstanceOf(DadosInvalidosException.class);
        assertThatThrownBy(() -> reserva(as(10), null))
                .isInstanceOf(DadosInvalidosException.class);
    }

    @Test
    void criar_emSalaInativa_lancaRegraDeNegocio() {
        Sala sala = salaAtiva();
        sala.desativar();

        assertThatThrownBy(() -> new Reserva(as(10), as(11), sala, usuario()))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessage("Sala inativa não pode receber reservas.");
    }

    @Test
    void periodosSeSobrepoem_fimIgualAoInicioDoOutro_naoConflita() {
        // 10h–11h e 11h–12h: a sala fica livre exatamente às 11h.
        assertThat(Reserva.periodosSeSobrepoem(as(10), as(11), as(11), as(12))).isFalse();
    }

    @Test
    void periodosSeSobrepoem_inicioIgualAoFimDoOutro_naoConflita() {
        // Mesmo caso, lido ao contrário: 09h–10h e 10h–11h.
        assertThat(Reserva.periodosSeSobrepoem(as(9), as(10), as(10), as(11))).isFalse();
    }

    @Test
    void periodosSeSobrepoem_totalmenteSeparados_naoConflita() {
        assertThat(Reserva.periodosSeSobrepoem(as(9), as(10), as(12), as(13))).isFalse();
    }

    @Test
    void periodosSeSobrepoem_comecoAntesEFimDentro_conflita() {
        // 10h–11h e 09h–10h30: o intervalo 10h–10h30 é compartilhado.
        assertThat(Reserva.periodosSeSobrepoem(as(10), as(11), as(9), asEMeia(10))).isTrue();
    }

    @Test
    void periodosSeSobrepoem_comecoDentroEFimDepois_conflita() {
        // 10h–11h e 10h30–11h30.
        assertThat(Reserva.periodosSeSobrepoem(as(10), as(11), asEMeia(10), asEMeia(11))).isTrue();
    }

    @Test
    void periodosSeSobrepoem_umDentroDoOutro_conflita() {
        // 10h–12h contém 10h–11h.
        assertThat(Reserva.periodosSeSobrepoem(as(10), as(12), as(10), as(11))).isTrue();
    }

    @Test
    void periodosSeSobrepoem_mesmoPeriodo_conflita() {
        assertThat(Reserva.periodosSeSobrepoem(as(10), as(11), as(10), as(11))).isTrue();
    }

    @Test
    void conflitaCom_reservaAtiva_usaIntervaloSemiaberto() {
        Reserva reserva = reserva(as(10), as(11));

        assertThat(reserva.conflitaCom(as(11), as(12))).isFalse();
        assertThat(reserva.conflitaCom(asEMeia(10), asEMeia(11))).isTrue();
    }

    @Test
    void conflitaCom_reservaCancelada_nuncaConflita() {
        Reserva reserva = reserva(as(10), as(11));
        reserva.cancelar();

        assertThat(reserva.conflitaCom(as(10), as(11))).isFalse();
    }

    @Test
    void cancelar_mudaStatusParaCancelada() {
        Reserva reserva = reserva(as(10), as(11));

        reserva.cancelar();

        assertThat(reserva.getTipoReserva()).isEqualTo(TipoReserva.CANCELADA);
        assertThat(reserva.isCancelada()).isTrue();
    }

    @Test
    void cancelar_jaCancelada_naoLancaErro() {
        Reserva reserva = reserva(as(10), as(11));
        reserva.cancelar();

        assertThatCode(reserva::cancelar).doesNotThrowAnyException();
        assertThat(reserva.getTipoReserva()).isEqualTo(TipoReserva.CANCELADA);
    }

    @Test
    void atualizar_reservaCancelada_lancaRegraDeNegocio() {
        Reserva reserva = reserva(as(10), as(11));
        reserva.cancelar();

        assertThatThrownBy(() -> reserva.atualizar(as(14), as(15), salaAtiva(), usuario()))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessage("Reserva cancelada não pode ser alterada.");
    }

    @Test
    void atualizar_comPeriodoValido_alteraHorarios() {
        Reserva reserva = reserva(as(10), as(11));

        reserva.atualizar(as(14), as(15), salaAtiva(), usuario());

        assertThat(reserva.getInicio()).isEqualTo(as(14));
        assertThat(reserva.getFim()).isEqualTo(as(15));
    }

    @Test
    void atualizar_comFimAntesDoInicio_mantemDadosOriginais() {
        Reserva reserva = reserva(as(10), as(11));

        assertThatThrownBy(() -> reserva.atualizar(as(15), as(14), salaAtiva(), usuario()))
                .isInstanceOf(DadosInvalidosException.class);
        assertThat(reserva.getInicio()).isEqualTo(as(10));
        assertThat(reserva.getFim()).isEqualTo(as(11));
    }
}
