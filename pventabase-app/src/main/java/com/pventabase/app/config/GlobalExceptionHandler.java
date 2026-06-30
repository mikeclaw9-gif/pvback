package com.pventabase.app.config;

import com.pventabase.common.constants.ErrorCodes;
import com.pventabase.common.exception.BusinessException;
import com.pventabase.common.exception.DuplicateResourceException;
import com.pventabase.common.exception.InvalidStateException;
import com.pventabase.common.exception.ResourceNotFoundException;
import com.pventabase.ventas.exception.StockInsuficienteException;
import com.pventabase.ventas.exception.VentaNoEncontradaException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getErrorCode(), ex.getMessage());
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateResource(DuplicateResourceException ex) {
        return buildResponse(HttpStatus.CONFLICT, ex.getErrorCode(), ex.getMessage());
    }

    @ExceptionHandler(InvalidStateException.class)
    public ResponseEntity<ErrorResponse> handleInvalidState(InvalidStateException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getErrorCode(), ex.getMessage());
    }

    @ExceptionHandler(VentaNoEncontradaException.class)
    public ResponseEntity<ErrorResponse> handleVentaNotFound(VentaNoEncontradaException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getErrorCode(), ex.getMessage());
    }

    @ExceptionHandler(StockInsuficienteException.class)
    public ResponseEntity<ErrorResponse> handleStockInsuficiente(StockInsuficienteException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getErrorCode(), ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> details = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                details.put(error.getField(), error.getDefaultMessage()));
        return buildResponse(HttpStatus.BAD_REQUEST, ErrorCodes.VALIDATION_ERROR,
                "Error de validacion", details);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCodes.INTERNAL_ERROR,
                "Error interno del servidor");
    }

    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String errorCode, String message) {
        return ResponseEntity.status(status)
                .body(new ErrorResponse(errorCode, message, status.value(), LocalDateTime.now(), null));
    }

    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String errorCode,
                                                         String message, Map<String, String> details) {
        return ResponseEntity.status(status)
                .body(new ErrorResponse(errorCode, message, status.value(), LocalDateTime.now(), details));
    }

    private record ErrorResponse(String errorCode, String message, int status,
                                 LocalDateTime timestamp, Map<String, String> details) {}
}
