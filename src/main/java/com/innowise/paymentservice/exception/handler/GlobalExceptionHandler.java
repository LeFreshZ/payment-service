package com.innowise.paymentservice.exception.handler;

import com.innowise.paymentservice.dto.ErrorResponse;
import com.innowise.paymentservice.exception.PaymentNotFoundException;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(PaymentNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleNotFound(PaymentNotFoundException ex) {
    ErrorResponse response = new ErrorResponse(LocalDateTime.now(), 404, ex.getMessage());

    return ResponseEntity.status(404).body(response);
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
    ErrorResponse response = new ErrorResponse(LocalDateTime.now(), 403, "Access denied");

    return ResponseEntity.status(403).body(response);
  }

  @ExceptionHandler(JwtException.class)
  public ResponseEntity<ErrorResponse> handleJwtException(JwtException ex) {
    ErrorResponse response = new ErrorResponse(LocalDateTime.now(), 401, "Invalid or expired "
        + "token");

    return ResponseEntity.status(401).body(response);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
    String message = ex.getBindingResult().getFieldErrors().stream()
        .map(FieldError::getDefaultMessage)
        .collect(Collectors.joining(", "));

    ErrorResponse response = new ErrorResponse(LocalDateTime.now(), 400, message);

    return ResponseEntity.status(400).body(response);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
    ErrorResponse response = new ErrorResponse(LocalDateTime.now(), 500, "Internal server error");

    return ResponseEntity.status(500).body(response);
  }
}
