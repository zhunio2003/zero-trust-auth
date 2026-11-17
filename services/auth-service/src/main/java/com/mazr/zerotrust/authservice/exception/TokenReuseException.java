package com.mazr.zerotrust.authservice.exception;

public class ReuseTokenException extends RuntimeException {

    public ReuseTokenException() {
        super("Invalid token");
    }
    public ReuseTokenException(Throwable cause) {
        super("Invalid token", cause);
    }
}

