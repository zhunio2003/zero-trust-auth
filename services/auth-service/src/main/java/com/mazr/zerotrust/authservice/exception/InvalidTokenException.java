package com.mazr.zerotrust.authservice.exception;

public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException() {super("Invalid token");}
    public InvalidTokenException(Throwable cause) {super("Invalid token", cause);}
}
