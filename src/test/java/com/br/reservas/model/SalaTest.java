package com.br.reservas.model;

import com.br.reservas.exception.DadosInvalidosException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SalaTest {

    @Test
    void criar_comDadosValidos_comecaAtiva() {
        Sala sala = new Sala("Sala 101", 10);

        assertThat(sala.getNome()).isEqualTo("Sala 101");
        assertThat(sala.getCapacidade()).isEqualTo(10);
        assertThat(sala.isAtiva()).isTrue();
    }

    @Test
    void criar_removeEspacosDoNome() {
        Sala sala = new Sala("  Sala 101  ", 10);

        assertThat(sala.getNome()).isEqualTo("Sala 101");
    }

    @Test
    void criar_comCapacidadeZero_lancaDadosInvalidos() {
        assertThatThrownBy(() -> new Sala("Sala 101", 0))
                .isInstanceOf(DadosInvalidosException.class)
                .hasMessage("A capacidade deve ser maior que zero.");
    }

    @Test
    void criar_comCapacidadeNegativa_lancaDadosInvalidos() {
        assertThatThrownBy(() -> new Sala("Sala 101", -5))
                .isInstanceOf(DadosInvalidosException.class);
    }

    @Test
    void criar_comCapacidadeNula_lancaDadosInvalidos() {
        assertThatThrownBy(() -> new Sala("Sala 101", null))
                .isInstanceOf(DadosInvalidosException.class);
    }

    @Test
    void criar_comNomeEmBranco_lancaDadosInvalidos() {
        assertThatThrownBy(() -> new Sala("   ", 10))
                .isInstanceOf(DadosInvalidosException.class)
                .hasMessage("Nome da sala é obrigatório.");
    }

    @Test
    void criar_comNomeMuitoLongo_lancaDadosInvalidos() {
        String nomeLongo = "a".repeat(101);

        assertThatThrownBy(() -> new Sala(nomeLongo, 10))
                .isInstanceOf(DadosInvalidosException.class);
    }

    @Test
    void desativar_depoisAtivar_voltaParaAtiva() {
        Sala sala = new Sala("Sala 101", 10);

        sala.desativar();
        assertThat(sala.isAtiva()).isFalse();

        sala.ativar();
        assertThat(sala.isAtiva()).isTrue();
    }

    @Test
    void alterarDados_comValorInvalido_mantemDadosOriginais() {
        Sala sala = new Sala("Sala 101", 10);

        assertThatThrownBy(() -> sala.alterarDados("Sala 102", 0))
                .isInstanceOf(DadosInvalidosException.class);
        assertThat(sala.getNome()).isEqualTo("Sala 101");
        assertThat(sala.getCapacidade()).isEqualTo(10);
    }
}
