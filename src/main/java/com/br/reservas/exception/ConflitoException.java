package com.br.reservas.exception;

/**
 * Operação que colide com um dado já existente: horário ocupado, e-mail repetido
 * ou exclusão de algo que ainda tem histórico. Resulta em HTTP 409.
 */
public class ConflitoException extends RuntimeException {

    public ConflitoException(String mensagem) {
        super(mensagem);
    }
}
