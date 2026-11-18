package com.coop.payments.exception;

import com.coop.payments.dto.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {


    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiError> handleApi(ApiException ex, HttpServletRequest req) {
        log.warn("Business rule violated: {} (status={})", ex.getMessage(), ex.getStatus().value());
        return build(ex.getStatus(), ex.getMessage(), req.getRequestURI(), null);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraint(ConstraintViolationException ex, HttpServletRequest req) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getConstraintViolations().forEach(v -> errors.put(v.getPropertyPath().toString(), v.getMessage()));
        log.warn("Constraint violation: {}", errors);
        return build(HttpStatus.BAD_REQUEST, "Validation failed", req.getRequestURI(), errors);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> handleBadCredentials(BadCredentialsException ex, HttpServletRequest req) {
        log.warn("Failed login attempt");
        return build(HttpStatus.UNAUTHORIZED, "Invalid username or password", req.getRequestURI(), null);
    }

    @ExceptionHandler({OptimisticLockingFailureException.class, DataIntegrityViolationException.class})
    public ResponseEntity<ApiError> handleConflict(Exception ex, HttpServletRequest req) {
        log.warn("Concurrent update / integrity conflict: {}", ex.getClass().getSimpleName());
        return build(HttpStatus.CONFLICT, "Request conflicts with current state, please retry", req.getRequestURI(), null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception ex, HttpServletRequest req) {
        log.error("Unexpected error on {}", req.getRequestURI(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", req.getRequestURI(), null);
    }


    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(fe -> errors.putIfAbsent(fe.getField(), fe.getDefaultMessage()));
        log.warn("Request validation failed: {}", errors);
        return ResponseEntity.badRequest()
                .body(ApiError.of(HttpStatus.BAD_REQUEST, "Validation failed", path(request), errors));
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.warn("Unreadable request body: {}", ex.getMostSpecificCause().getMessage());
        return ResponseEntity.badRequest()
                .body(ApiError.of(HttpStatus.BAD_REQUEST, "Malformed JSON or invalid field value", path(request), null));
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
            HttpStatusCode statusCode, WebRequest request) {
        HttpStatus status = HttpStatus.valueOf(statusCode.value());
        log.warn("Request rejected: {} ({})", status, ex.getClass().getSimpleName());
        return ResponseEntity.status(status)
                .body(ApiError.of(status, status.getReasonPhrase(), path(request), null));
    }

    private ResponseEntity<ApiError> build(HttpStatus status, String message, String path, Map<String, String> fields) {
        return ResponseEntity.status(status).body(ApiError.of(status, message, path, fields));
    }

    private String path(WebRequest request) {
        return ((ServletWebRequest) request).getRequest().getRequestURI();
    }
}
