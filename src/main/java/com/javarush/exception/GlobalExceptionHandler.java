package com.javarush.exception;

import com.javarush.dto.error.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler({
            UserNotFoundException.class,
            TaskNotFoundException.class
    })
    public ResponseEntity<Map<String, Object>> handleNotFound(
            RuntimeException ex, HttpServletRequest request) {

        log.warn("Not found: {} [path={}]", ex.getMessage(), request.getRequestURI());
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler({
            UserAlreadyExistsException.class,
            TaskAlreadyExistsException.class,
            UserAlreadyAssignedException.class
    })
    public ResponseEntity<Map<String, Object>> handleConflict(
            RuntimeException ex, HttpServletRequest request) {

        log.warn("Conflict: {} [path={}]", ex.getMessage(), request.getRequestURI());
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler({
            InvalidPasswordException.class,
            InvalidOperationException.class,
            UserNotAssignedException.class,
            TaskAlreadyDeleteException.class,
            TaskNotDeletedException.class,
            UserAlreadyDeleteException.class,
            UserNotDeletedException.class
    })
    public ResponseEntity<Map<String, Object>> handleBadRequest(
            RuntimeException ex, HttpServletRequest request) {

        log.warn("Bad request: {} [path={}]", ex.getMessage(), request.getRequestURI());
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Map<String, Object>> handleUnauthorized(
            UnauthorizedException ex, HttpServletRequest request) {

        log.warn("Unauthorized: {} [path={}]", ex.getMessage(), request.getRequestURI());
        return buildResponse(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(
            org.springframework.security.access.AccessDeniedException ex,
            HttpServletRequest request) {

        log.warn("Access denied: {} [path={}]", ex.getMessage(), request.getRequestURI());
        return buildResponse(HttpStatus.FORBIDDEN, "Доступ запрещён");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException ex) {

        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                fieldErrors.put(error.getField(), error.getDefaultMessage())
        );

        log.warn("Validation failed: {}", fieldErrors);

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", 400);
        body.put("error", "Validation Failed");
        body.put("errors", fieldErrors);

        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(
            Exception ex, HttpServletRequest request) {

        log.error("Internal error: {} [path={}]", ex.getMessage(),
                request.getRequestURI(), ex);   // ← stack trace

        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                "Внутренняя ошибка сервера");
    }

    private ResponseEntity<Map<String, Object>> buildResponse(
            HttpStatus status, String message) {

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);

        return ResponseEntity.status(status).body(body);
    }
}
