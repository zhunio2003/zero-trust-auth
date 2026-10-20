package com.mazr.zerotrust.authservice.service;

import com.mazr.zerotrust.authservice.config.RsaKeyProperties;
import com.mazr.zerotrust.authservice.model.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.time.Duration;
import java.util.Base64;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class JwtServiceTest {

    @Mock
    private RsaKeyProperties rsaKeyProperties;

    @InjectMocks
    private JwtService jwtService;

    private User userTest;

    @BeforeEach
    void setup() throws Exception {

        // Generar par de claves RSA real
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        // Inyectar claves en el mock
        when(rsaKeyProperties.getPrivateKey()).thenReturn(keyPair.getPrivate());
        when(rsaKeyProperties.getPublicKey()).thenReturn(keyPair.getPublic());

        // Inyectar el @Value que Mockito no puede
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiry", Duration.ofMinutes(15));

        // Inializar user de prueba
        userTest = User.builder()
            .id(UUID.randomUUID())
            .role("ADMIN")
            .department("IT")
            .build();

    }

    @Test
    public void tokenSuccessful() {

        String token = jwtService.generateToken(userTest);

        Claims claims = Jwts.parser()
            .verifyWith(rsaKeyProperties.getPublicKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();

            assertEquals(userTest.getId().toString(), claims.getSubject());
            assertEquals("ADMIN", claims.get("role"));
            assertEquals("IT", claims.get("department"));
            assertNotNull(claims.getExpiration());
            assertNotNull(claims.getIssuedAt());
    }

    @Test
    public void  tokenNone() {

        String tokenFase = "eyJhbGciOiJub25lIn0." + Base64.getEncoder().encodeToString("{}".getBytes()) + ".";

        assertThrows(Exception.class, () -> {
                Jwts.parser()
                    .verifyWith(rsaKeyProperties.getPublicKey())
                    .build()
                    .parseSignedClaims(tokenFase)
                    .getPayload();
            }
        );
    }



}
