package com.mazr.zerotrust.authservice.security;

import com.mazr.zerotrust.authservice.config.RsaKeyProperties;
import com.mazr.zerotrust.authservice.exception.InvalidTokenException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.crypto.SecretKey;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class JwtValidatorTest {

    @Mock
    private RsaKeyProperties rsaKeyProperties;

    private KeyPair keyPair;

    private JwtValidator jwtValidator;

    @BeforeEach
    void setup() throws NoSuchAlgorithmException {

        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        keyPair = keyPairGenerator.generateKeyPair();

        when(rsaKeyProperties.getPublicKey()).thenReturn(keyPair.getPublic());

        jwtValidator = new JwtValidator(rsaKeyProperties);

    }

    @Test
    void validateToken_withValidToken_returnsClaims() {

        String token = validTokenBuilder()
            .signWith(keyPair.getPrivate(), Jwts.SIG.RS256)
            .compact();

        Claims claims = jwtValidator.validateToken(token);

        assertEquals("ADMIN", claims.get("role", String.class));
        assertEquals("IT", claims.get("department", String.class));
        assertNotNull(claims.getSubject());
        assertNotNull(claims.getId());

    }

    @Test
    void validateToken_withRs512Algorithm_throwsInvalidTokenException() {

        String token = validTokenBuilder()
            .signWith(keyPair.getPrivate(), Jwts.SIG.RS512)
            .compact();

        assertThrows(InvalidTokenException.class, () -> jwtValidator.validateToken(token));

    }

    @Test
    void validateToken_withWrongSigningKey_throwsInvalidTokenException() throws NoSuchAlgorithmException {

        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair2 = keyPairGenerator.generateKeyPair();

        String token = validTokenBuilder()
            .signWith(keyPair2.getPrivate(), Jwts.SIG.RS256)
            .compact();

        InvalidTokenException ex = assertThrows(InvalidTokenException.class, () -> jwtValidator.validateToken(token));

        assertInstanceOf(SignatureException.class, ex.getCause());
    }

    @Test
    void validateToken_withExpiredToken_throwsInvalidTokenException() {

        String token = validTokenBuilder()
            .expiration(new Date(System.currentTimeMillis() - Duration.ofMinutes(16).toMillis()))
            .signWith(keyPair.getPrivate(), Jwts.SIG.RS256)
            .compact();

        InvalidTokenException ex = assertThrows(InvalidTokenException.class, () -> jwtValidator.validateToken(token));

        assertInstanceOf(ExpiredJwtException.class, ex.getCause());
    }

    @Test
    void validateToken_withUnsignedToken_throwsInvalidTokenException() {

        String token = validTokenBuilder()
            .compact();

        InvalidTokenException ex = assertThrows(InvalidTokenException.class, () -> jwtValidator.validateToken(token));

        assertInstanceOf(UnsupportedJwtException.class, ex.getCause());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"trash", "a.b.c"})
    void validateToken_withMalformedToken_throwsInvalidTokenException(String token) {

        assertThrows(InvalidTokenException.class, () -> jwtValidator.validateToken(token));

    }

    @Test
    void validateToken_withForgedHs256Token_throwsInvalidTokenException() {

        SecretKey secret = Keys.hmacShaKeyFor(keyPair.getPublic().getEncoded());

        String token = validTokenBuilder()
            .signWith(secret, Jwts.SIG.HS256)
            .compact();

        InvalidTokenException ex = assertThrows(InvalidTokenException.class, () -> jwtValidator.validateToken(token));
        assertInstanceOf(SignatureException.class, ex.getCause());

    }

    @ParameterizedTest
    @ValueSource(strings = {"sub", "iat", "exp", "role", "department", "jti"})
    void validateToken_withMissingClaim_throwsInvalidTokenException(String missingClaim) {

        Map<String, Object> claims = new HashMap<>();

        claims.put("sub", "1L");
        claims.put("iat", new Date());
        claims.put("exp", new Date(System.currentTimeMillis() + Duration.ofMinutes(15).toMillis()));
        claims.put("role", "ADMIN");
        claims.put("department", "IT");
        claims.put("jti", "a7f2e8d4-3c9b-4f1a-b2e7-8d4c9f1a2b3e");

        claims.remove(missingClaim);

        String token = Jwts.builder()
            .claims(claims)
            .signWith(keyPair.getPrivate(), Jwts.SIG.RS256)
            .compact();

        InvalidTokenException ex = assertThrows(InvalidTokenException.class, () -> jwtValidator.validateToken(token));

        assertNull(ex.getCause());

    }

    private JwtBuilder validTokenBuilder() {
        return Jwts.builder()
            .subject(UUID.randomUUID().toString())
            .claim("role", "ADMIN")
            .claim("department", "IT")
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + Duration.ofMinutes(15).toMillis()))
            .id(UUID.randomUUID().toString());

    }

}
