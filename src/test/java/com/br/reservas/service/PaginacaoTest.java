package com.br.reservas.service;

import com.br.reservas.exception.DadosInvalidosException;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaginacaoTest {

    @Test
    void valoresValidos_montamPaginacaoOrdenadaPorInicioCrescente() {
        var pageable = Paginacao.criar(0, 20, "inicio,asc");

        assertThat(pageable.getPageNumber()).isZero();
        assertThat(pageable.getPageSize()).isEqualTo(20);
        assertThat(pageable.getSort()).isEqualTo(Sort.by(Sort.Direction.ASC, "inicio"));
    }

    @Test
    void campoSemDirecao_usaCrescente() {
        assertThat(Paginacao.criar(0, 20, "fim").getSort())
                .isEqualTo(Sort.by(Sort.Direction.ASC, "fim"));
    }

    @Test
    void direcaoDesc_eAceitaEmQualquerCaixa() {
        assertThat(Paginacao.criar(0, 20, "inicio,DESC").getSort())
                .isEqualTo(Sort.by(Sort.Direction.DESC, "inicio"));
    }

    @Test
    void campoInexistente_lancaErroDeSortSemChegarAoBanco() {
        assertThatThrownBy(() -> Paginacao.criar(0, 20, "campoInexistente"))
                .isInstanceOf(DadosInvalidosException.class)
                .satisfies(e -> assertThat(((DadosInvalidosException) e).getCampo()).isEqualTo("sort"));
    }

    @Test
    void caminhoDeAssociacao_naoEPermitido() {
        assertThatThrownBy(() -> Paginacao.criar(0, 20, "usuario.nome"))
                .isInstanceOf(DadosInvalidosException.class);
    }

    @Test
    void direcaoInvalida_lancaErro() {
        assertThatThrownBy(() -> Paginacao.criar(0, 20, "inicio,foo"))
                .isInstanceOf(DadosInvalidosException.class);
    }

    @Test
    void sortComTresPartes_lancaErro() {
        assertThatThrownBy(() -> Paginacao.criar(0, 20, "inicio,asc,extra"))
                .isInstanceOf(DadosInvalidosException.class);
    }

    @Test
    void sortVazio_lancaErro() {
        assertThatThrownBy(() -> Paginacao.criar(0, 20, ""))
                .isInstanceOf(DadosInvalidosException.class);
    }

    @Test
    void paginaNegativa_lancaErroNoCampoPage() {
        assertThatThrownBy(() -> Paginacao.criar(-1, 20, "inicio,asc"))
                .isInstanceOf(DadosInvalidosException.class)
                .satisfies(e -> assertThat(((DadosInvalidosException) e).getCampo()).isEqualTo("page"));
    }

    @Test
    void tamanhoZero_lancaErroNoCampoSize() {
        assertThatThrownBy(() -> Paginacao.criar(0, 0, "inicio,asc"))
                .isInstanceOf(DadosInvalidosException.class)
                .satisfies(e -> assertThat(((DadosInvalidosException) e).getCampo()).isEqualTo("size"));
    }

    @Test
    void tamanhoAcimaDoLimite_lancaErro_emVezDeCortarEmSilencio() {
        assertThatThrownBy(() -> Paginacao.criar(0, Paginacao.TAMANHO_MAXIMO + 1, "inicio,asc"))
                .isInstanceOf(DadosInvalidosException.class);
    }
}
