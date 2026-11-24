package com.br.reservas.exception;

/**
 * Dados que violam uma regra de formato ou de consistência interna. Resulta em HTTP 400.
 * Quando a regra é de um campo específico, {@link #getCampo()} informa qual.
 */
public class DadosInvalidosException extends RuntimeException {

    private final String campo;

    public DadosInvalidosException(String mensagem) {
        this(null, mensagem);
    }

    public DadosInvalidosException(String campo, String mensagem) {
        super(mensagem);
        this.campo = campo;
    }

    /** Nome do campo com problema, ou {@code null} quando a regra não é de um campo só. */
    public String getCampo() {
        return campo;
    }
}
