package com.br.reservas.exception;

public class ReservaConflitoException extends RuntimeException{

    public ReservaConflitoException(String mensage){
        super(mensage);
    }
}
