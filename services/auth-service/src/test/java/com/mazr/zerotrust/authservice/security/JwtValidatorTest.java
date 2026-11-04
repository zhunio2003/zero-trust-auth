package com.mazr.zerotrust.authservice.security;

import com.mazr.zerotrust.authservice.config.RsaKeyProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

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

}
