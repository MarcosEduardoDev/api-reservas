package com.br.reservas.exception;

/**
 * Operação que não é permitida pelo estado atual de uma entidade, como reservar uma sala inativa
 * ou alterar uma reserva cancelada. Resulta em HTTP 422.
 */
public class RegraDeNegocioException extends RuntimeException {

    public RegraDeNegocioException(String mensagem) {
        super(mensagem);
    }
}
