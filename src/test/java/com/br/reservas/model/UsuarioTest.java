package com.br.reservas.model;

import com.br.reservas.exception.DadosInvalidosException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UsuarioTest {

    @Test
    void criar_normalizaEmailParaMinusculoSemEspacos() {
        Usuario usuario = new Usuario("Ana", "  Ana@Exemplo.COM  ");

        assertThat(usuario.getEmail()).isEqualTo("ana@exemplo.com");
    }

    @Test
    void criar_comEmailSemArroba_lancaDadosInvalidos() {
        assertThatThrownBy(() -> new Usuario("Ana", "ana.exemplo.com"))
                .isInstanceOf(DadosInvalidosException.class)
                .hasMessage("E-mail inválido.");
    }

    @Test
    void criar_comEspacoNoMeioDoEmail_lancaDadosInvalidos() {
        assertThatThrownBy(() -> new Usuario("Ana", "ana silva@exemplo.com"))
                .isInstanceOf(DadosInvalidosException.class);
    }

    @Test
    void criar_comNomeEmBranco_lancaDadosInvalidos() {
        assertThatThrownBy(() -> new Usuario(" ", "ana@exemplo.com"))
                .isInstanceOf(DadosInvalidosException.class)
                .hasMessage("Nome do usuário é obrigatório.");
    }

    @Test
    void alterarDados_comEmailInvalido_mantemDadosOriginais() {
        Usuario usuario = new Usuario("Ana", "ana@exemplo.com");

        assertThatThrownBy(() -> usuario.alterarDados("Ana Silva", "invalido"))
                .isInstanceOf(DadosInvalidosException.class);
        assertThat(usuario.getNome()).isEqualTo("Ana");
        assertThat(usuario.getEmail()).isEqualTo("ana@exemplo.com");
    }
}
