package com.mazr.zerotrust.authservice.exception;

public class TokenReuseException extends InvalidTokenException {

    //  Refresh token already consumed: possible theft. Treats the client the same as an invalid token.
    public TokenReuseException() {}

}
