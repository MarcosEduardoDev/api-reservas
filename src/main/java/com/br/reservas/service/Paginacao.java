package com.br.reservas.service;

import com.br.reservas.exception.DadosInvalidosException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Locale;
import java.util.Set;

/**
 * Monta a paginação das listagens a partir de parâmetros validados.
 *
 * <p>Parâmetros inválidos viram erro 400. O Spring, por padrão, ignora valores ruins em silêncio,
 * o que esconde erros do cliente. Por isso a validação é feita aqui, e não com {@code Pageable} direto.</p>
 *
 * <p>Formato de {@code sort}: {@code campo} ou {@code campo,asc|desc}. Só os campos listados em
 * {@link #CAMPOS_ORDENACAO} são aceitos, para não expor colunas internas nem gerar erro de banco.</p>
 */
public final class Paginacao {

    /** Tamanho máximo de página: evita respostas enormes. */
    public static final int TAMANHO_MAXIMO = 100;

    private static final Set<String> CAMPOS_ORDENACAO = Set.of("id", "inicio", "fim", "tipoReserva");

    private Paginacao() {
        // Classe utilitária.
    }

    public static Pageable criar(int page, int size, String sort) {
        if (page < 0) {
            throw new DadosInvalidosException("page", "Página deve ser zero ou maior.");
        }
        if (size < 1 || size > TAMANHO_MAXIMO) {
            throw new DadosInvalidosException("size", "Tamanho da página deve estar entre 1 e " + TAMANHO_MAXIMO + ".");
        }
        return PageRequest.of(page, size, criarOrdenacao(sort));
    }

    private static Sort criarOrdenacao(String sort) {
        String[] partes = sort.split(",", -1);
        if (partes.length > 2) {
            throw new DadosInvalidosException("sort", "Use o formato campo ou campo,asc|desc.");
        }

        String campo = partes[0].trim();
        if (!CAMPOS_ORDENACAO.contains(campo)) {
            throw new DadosInvalidosException("sort",
                    "Campo de ordenação inválido. Use um destes: " + String.join(", ", CAMPOS_ORDENACAO) + ".");
        }

        Sort.Direction direcao = Sort.Direction.ASC;
        if (partes.length == 2) {
            String direcaoInformada = partes[1].trim().toLowerCase(Locale.ROOT);
            if (direcaoInformada.equals("desc")) {
                direcao = Sort.Direction.DESC;
            } else if (!direcaoInformada.equals("asc")) {
                throw new DadosInvalidosException("sort", "A direção deve ser asc ou desc.");
            }
        }
        return Sort.by(direcao, campo);
    }
}
