package com.br.reservas.dto;

import java.util.Map;

/**
 * Formato único de erro da API.
 *
 * @param status   código HTTP (ex.: 404)
 * @param mensagem descrição do problema, pensada para quem consome a API
 * @param campos   para erros de validação: campo -> motivo. Vazio nos demais casos.
 */
public record ErroResponseDTO(int status, String mensagem, Map<String, String> campos) {
}
