package com.mazr.zerotrust.authservice.exception;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(EmailAlreadyExistsException.class)
  public ResponseEntity<String> handleEmailAlreadyExists(EmailAlreadyExistsException ex) {
    return ResponseEntity.status(HttpStatus.CONFLICT).body("Email is already registered");
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<String> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid request data");
  }
  
  @ExceptionHandler(InvalidCredentialsException.class)
  public ResponseEntity<Map<String, String>> invalidCredentialsExceptionException(InvalidCredentialsException ex) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", ex.getMessage()));
  }


}
