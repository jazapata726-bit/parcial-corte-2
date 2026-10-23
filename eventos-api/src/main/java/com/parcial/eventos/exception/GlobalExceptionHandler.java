package com.parcial.eventos.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import java.time.Instant;
import java.util.Map;
import java.util.TreeMap;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ApiError> noEncontrado(RecursoNoEncontradoException ex,
                                                HttpServletRequest request) {
        return responder(HttpStatus.NOT_FOUND, ex.getMessage(), request, Map.of());
    }

    @ExceptionHandler(ConflictoException.class)
    public ResponseEntity<ApiError> conflicto(ConflictoException ex,
                                             HttpServletRequest request) {
        return responder(HttpStatus.CONFLICT, ex.getMessage(), request, Map.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> validacion(MethodArgumentNotValidException ex,
                                              HttpServletRequest request) {
        Map<String, String> campos = new TreeMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
            campos.putIfAbsent(error.getField(), error.getDefaultMessage()));
        return responder(HttpStatus.BAD_REQUEST, "Revisa los datos enviados", request, campos);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> jsonInvalido(HttpServletRequest request) {
        return responder(HttpStatus.BAD_REQUEST,
            "JSON inválido. Para fecha usa, por ejemplo, 2027-01-15T09:00:00", request, Map.of());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> idInvalido(HttpServletRequest request) {
        return responder(HttpStatus.BAD_REQUEST, "El identificador debe ser un número entero",
            request, Map.of());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> integridad(HttpServletRequest request) {
        return responder(HttpStatus.CONFLICT,
            "La operación entra en conflicto con un registro existente", request, Map.of());
    }

    private ResponseEntity<ApiError> responder(HttpStatus estado, String mensaje,
                                               HttpServletRequest request,
                                               Map<String, String> campos) {
        return ResponseEntity.status(estado).body(new ApiError(Instant.now(), estado.value(),
            estado.getReasonPhrase(), mensaje, request.getRequestURI(), campos));
    }
}
