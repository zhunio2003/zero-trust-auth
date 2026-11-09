package com.mazr.zerotrust.authservice.security;

import com.mazr.zerotrust.authservice.config.RsaKeyProperties;
import com.mazr.zerotrust.authservice.exception.InvalidTokenException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Date;
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
