package com.mazr.zerotrust.authservice.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.mazr.zerotrust.authservice.config.RsaKeyProperties;
import com.mazr.zerotrust.authservice.exception.InvalidTokenException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    @DisplayName("token válido: devuelve los claims")
    void validateTokenWithValidTokenReturnsClaims() {
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
    @DisplayName("allowlist: rechaza RS512 aunque la firma sea válida")
    void validateTokenWithRs512AlgorithmThrowsInvalidTokenException() {
        String token = validTokenBuilder()
            .signWith(keyPair.getPrivate(), Jwts.SIG.RS512)
            .compact();

        assertThrows(InvalidTokenException.class, () -> jwtValidator.validateToken(token));
    }

    @Test
    @DisplayName("firma hecha con otra clave: rechaza por SignatureException")
    void validateTokenWithWrongSigningKeyThrowsInvalidTokenException() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair otherKeyPair = keyPairGenerator.generateKeyPair();

        String token = validTokenBuilder()
            .signWith(otherKeyPair.getPrivate(), Jwts.SIG.RS256)
            .compact();

        InvalidTokenException ex = assertThrows(InvalidTokenException.class, () -> jwtValidator.validateToken(token));

        assertInstanceOf(SignatureException.class, ex.getCause());
    }

    @Test
    @DisplayName("token expirado: rechaza por ExpiredJwtException")
    void validateTokenWithExpiredTokenThrowsInvalidTokenException() {
        String token = validTokenBuilder()
            .issuedAt(new Date(System.currentTimeMillis() - Duration.ofMinutes(30).toMillis()))
            .expiration(new Date(System.currentTimeMillis() - Duration.ofMinutes(16).toMillis()))
            .signWith(keyPair.getPrivate(), Jwts.SIG.RS256)
            .compact();

        InvalidTokenException ex = assertThrows(InvalidTokenException.class, () -> jwtValidator.validateToken(token));

        assertInstanceOf(ExpiredJwtException.class, ex.getCause());
    }

    @Test
    @DisplayName("token sin firma (alg none): rechaza por UnsupportedJwtException")
    void validateTokenWithUnsignedTokenThrowsInvalidTokenException() {
        String token = validTokenBuilder()
            .compact();

        InvalidTokenException ex = assertThrows(InvalidTokenException.class, () -> jwtValidator.validateToken(token));

        assertInstanceOf(UnsupportedJwtException.class, ex.getCause());
    }

    @ParameterizedTest(name = "token malformado: {0}")
    @NullAndEmptySource
    @ValueSource(strings = {"trash", "a.b.c"})
    @DisplayName("token nulo, vacío o malformado: rechaza")
    void validateTokenWithMalformedTokenThrowsInvalidTokenException(String token) {
        assertThrows(InvalidTokenException.class, () -> jwtValidator.validateToken(token));
    }

    @Test
    @DisplayName("HS256 forjado con la clave pública como secreto: rechaza")
    void validateTokenWithForgedHs256TokenThrowsInvalidTokenException() {
        SecretKey secret = Keys.hmacShaKeyFor(keyPair.getPublic().getEncoded());

        String token = validTokenBuilder()
            .signWith(secret, Jwts.SIG.HS256)
            .compact();

        InvalidTokenException ex = assertThrows(InvalidTokenException.class, () -> jwtValidator.validateToken(token));

        assertInstanceOf(SignatureException.class, ex.getCause());
    }

    @ParameterizedTest(name = "falta el claim {0}")
    @ValueSource(strings = {"sub", "iat", "exp", "role", "department", "jti"})
    @DisplayName("claim obligatorio ausente: rechaza sin causa externa")
    void validateTokenWithMissingClaimThrowsInvalidTokenException(String missingClaim) {
        Map<String, Object> claims = new HashMap<>();

        claims.put("sub", UUID.randomUUID().toString());
        claims.put("iat", new Date());
        claims.put("exp", new Date(System.currentTimeMillis() + Duration.ofMinutes(15).toMillis()));
        claims.put("role", "ADMIN");
        claims.put("department", "IT");
        claims.put("jti", UUID.randomUUID().toString());

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
