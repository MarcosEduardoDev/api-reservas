package com.br.reservas.exception;

/** Dados que violam uma regra de formato ou de consistência interna. Resulta em HTTP 400. */
public class DadosInvalidosException extends RuntimeException {

    public DadosInvalidosException(String mensagem) {
        super(mensagem);
    }
}
