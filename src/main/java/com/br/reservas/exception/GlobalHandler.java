package com.br.reservas.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> tratarErroValidacao(MethodArgumentNotValidException ex) {

        Map<String, String> erros = new HashMap<>();
        var resultado = ex.getBindingResult();
        for (var erro : resultado.getFieldErrors()) {

            String campo = erro.getField();
            String mensagem = erro.getDefaultMessage();

            erros.put(campo, mensagem);
        }
        return ResponseEntity.badRequest().body(erros);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<String> tratarNaoEncontrado(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(ReservaConflitoException.class)
    public ResponseEntity<String> tratarConflitoReserva(ReservaConflitoException ex) {

        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }


}
