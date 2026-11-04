
package com.lumaledger.exception;

import com.lumaledger.service.TransactionNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger =
    LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(TransactionNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(
            TransactionNotFoundException ex,
            HttpServletRequest request) {

        return buildResponse(
                HttpStatus.NOT_FOUND,
                ex.getMessage(),
                request.getRequestURI()
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(
            IllegalArgumentException ex,
            HttpServletRequest request) {

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                request.getRequestURI()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": "
                        + error.getDefaultMessage())
                .distinct()
                .collect(Collectors.joining("; "));

        if (message.isBlank()) {
            message = "Request validation failed";
        }

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                message,
                request.getRequestURI()
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleUnreadableRequest(
            HttpMessageNotReadableException ex,
            HttpServletRequest request) {

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Invalid request body. Check your JSON, transaction type, amount, and date.",
                request.getRequestURI()
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrity(
            DataIntegrityViolationException ex,
            HttpServletRequest request) {

        return buildResponse(
                HttpStatus.CONFLICT,
                "The request conflicts with existing data or a database constraint.",
                request.getRequestURI()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleUnexpectedError(
            Exception ex,
            HttpServletRequest request) {
        logger.error("Unexpected error at path: {}", request.getRequestURI(), ex);
        
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected server error occurred.",
                request.getRequestURI()
        );
    }

    private ResponseEntity<Map<String, Object>> buildResponse(
            HttpStatus status,
            String message,
            String path) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        body.put("path", path);

        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(NoResourceFoundException.class)
     public ResponseEntity<Map<String, Object>> handleResourceNotFound(
        NoResourceFoundException ex,
        HttpServletRequest request) {

        return buildResponse(
            HttpStatus.NOT_FOUND,
            "The requested resource was not found.",request.getRequestURI());
        }
}
