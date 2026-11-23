package com.br.reservas.exception;

import com.br.reservas.dto.ErroResponseDTO;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.validation.FieldError;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Converte exceções em respostas HTTP no formato {@link ErroResponseDTO}.
 *
 * <ul>
 *   <li>400: dados inválidos (validação, JSON malformado, parâmetro ausente ou com tipo errado);</li>
 *   <li>404: recurso não encontrado;</li>
 *   <li>409: conflito com dados existentes;</li>
 *   <li>422: operação não permitida pelo estado atual.</li>
 * </ul>
 */
@RestControllerAdvice
public class GlobalHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponseDTO> tratarErroValidacao(MethodArgumentNotValidException ex) {
        Map<String, String> campos = new LinkedHashMap<>();
        for (FieldError erro : ex.getBindingResult().getFieldErrors()) {
            campos.putIfAbsent(erro.getField(), erro.getDefaultMessage());
        }
        return resposta(HttpStatus.BAD_REQUEST, "Dados inválidos.", campos);
    }

    @ExceptionHandler(DadosInvalidosException.class)
    public ResponseEntity<ErroResponseDTO> tratarDadosInvalidos(DadosInvalidosException ex) {
        return resposta(HttpStatus.BAD_REQUEST, ex.getMessage(), Map.of());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponseDTO> tratarCorpoInvalido(HttpMessageNotReadableException ex) {
        return resposta(HttpStatus.BAD_REQUEST,
                "Corpo da requisição inválido. Verifique a sintaxe do JSON e o formato das datas (ex.: 2026-11-23T10:00:00).",
                Map.of());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErroResponseDTO> tratarParametroAusente(MissingServletRequestParameterException ex) {
        return resposta(HttpStatus.BAD_REQUEST, "Parâmetro obrigatório ausente.",
                Map.of(ex.getParameterName(), "Parâmetro obrigatório."));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroResponseDTO> tratarTipoInvalido(MethodArgumentTypeMismatchException ex) {
        return resposta(HttpStatus.BAD_REQUEST, "Valor com formato inválido.",
                Map.of(ex.getName(), "Valor inválido para este parâmetro."));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErroResponseDTO> tratarNaoEncontrado(ResourceNotFoundException ex) {
        return resposta(HttpStatus.NOT_FOUND, ex.getMessage(), Map.of());
    }

    @ExceptionHandler(ConflitoException.class)
    public ResponseEntity<ErroResponseDTO> tratarConflito(ConflitoException ex) {
        return resposta(HttpStatus.CONFLICT, ex.getMessage(), Map.of());
    }

    @ExceptionHandler(RegraDeNegocioException.class)
    public ResponseEntity<ErroResponseDTO> tratarRegraDeNegocio(RegraDeNegocioException ex) {
        return resposta(HttpStatus.UNPROCESSABLE_CONTENT, ex.getMessage(), Map.of());
    }

    /** Rede de segurança: restrições do banco (ex.: chave estrangeira) que não passaram pelas checagens do serviço. */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroResponseDTO> tratarIntegridade(DataIntegrityViolationException ex) {
        return resposta(HttpStatus.CONFLICT, "A operação conflita com dados já existentes.", Map.of());
    }

    private ResponseEntity<ErroResponseDTO> resposta(HttpStatus status, String mensagem, Map<String, String> campos) {
        return ResponseEntity.status(status)
                .body(new ErroResponseDTO(status.value(), mensagem, campos));
    }
}
