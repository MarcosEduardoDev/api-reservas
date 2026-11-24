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
    void criar_comCaractereNuloNoNome_lancaDadosInvalidosComCampo() {
        // \u0000 não é espaço: trim() o removeria em silêncio. Por isso a checagem é antes do trim.
        assertThatThrownBy(() -> new Usuario("Joao\u0000", "joao@exemplo.com"))
                .isInstanceOf(DadosInvalidosException.class)
                .satisfies(e -> assertThat(((DadosInvalidosException) e).getCampo()).isEqualTo("nome"));
    }

    @Test
    void criar_comCaractereDeControleNoEmail_lancaDadosInvalidos() {
        assertThatThrownBy(() -> new Usuario("Joao", "joao\u0000@exemplo.com"))
                .isInstanceOf(DadosInvalidosException.class)
                .satisfies(e -> assertThat(((DadosInvalidosException) e).getCampo()).isEqualTo("email"));
    }

    @Test
    void criar_comEmailInvalido_informaOCampoEmail() {
        assertThatThrownBy(() -> new Usuario("Ana", "ana.exemplo.com"))
                .isInstanceOf(DadosInvalidosException.class)
                .satisfies(e -> assertThat(((DadosInvalidosException) e).getCampo()).isEqualTo("email"));
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
