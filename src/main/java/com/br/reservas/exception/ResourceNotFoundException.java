package com.br.reservas.exception;

/** Recurso buscado por id não existe. Resulta em HTTP 404. */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String mensagem) {
        super(mensagem);
    }
}
