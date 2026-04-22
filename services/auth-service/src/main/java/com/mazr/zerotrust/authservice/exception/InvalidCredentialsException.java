package com.mazr.zerotrust.authservice.exception;

public class InvalidCredentialsException extends RuntimeException{
  
  public InvalidCredentialsException() {
    super("Invalid Credentials");
  }
}
