package com.mazr.zerotrust.authservice.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Component
@Getter
public class RsaKeyProperties {

    @Value("${jwt.private-key}")
    private String privateKeyString;
    @Value("${jwt.public-key}")
    private String publicKeyString;

    private PrivateKey privateKey;
    private PublicKey publicKey;

    @PostConstruct
    public void init() {

        byte[] privateKeyBytes = Base64.getDecoder().decode(clean(privateKeyString));
        PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);

        byte[] publicKeyBytes = Base64.getDecoder().decode(clean(publicKeyString));
        X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);

        try {

            privateKey = KeyFactory.getInstance("RSA").generatePrivate(privateKeySpec);
            publicKey = KeyFactory.getInstance("RSA").generatePublic(publicKeySpec);

        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("ERROR loading RSA key: ", e);
        }

    }

    private String clean(String keyString) {
        return keyString
            .replaceAll("-----BEGIN.*?-----", "")
            .replaceAll("-----END.*?-----", "")
            .replaceAll("\\s", "");
    }
}
