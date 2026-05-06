package com.mazr.zerotrust.authservice.service;

import com.mazr.zerotrust.authservice.exception.InvalidCredentialsException;
import com.mazr.zerotrust.authservice.model.dto.*;
import com.mazr.zerotrust.authservice.model.entity.WebauthnCredential;
import com.mazr.zerotrust.authservice.repository.WebAuthnCredentialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.time.Duration;
import java.util.Base64;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class WebAuthnService {

  private final WebAuthnCredentialRepository webAuthnCredentialRepository;
  private final StringRedisTemplate redisTemplate;

  public WebAuthnRegistrationInitResponse initializeRegistration(WebAuthnRegistrationInitRequest dto) {

    UUID userId = resolveUseId(dto.getMfaSessionToken());

    String challenge = UUID.randomUUID().toString();

    redisTemplate.opsForValue().set(
      "webauthn:challenge:" + userId,
      challenge,
      Duration.ofMinutes(5)
    );
    return WebAuthnRegistrationInitResponse.builder()
          .challenge(challenge)
          .userId(userId)
        .build();

  }

  public WebAuthnRegistrationResponse completeRegistration(WebAuthnRegistrationCompleteRequest dto) {

    UUID userId = resolveUseId(dto.getMfaSessionToken());

    String challenge = redisTemplate.opsForValue().get("webauthn:challenge:" + userId);

    if (challenge == null) {
        throw new InvalidCredentialsException();
    }

    verifySignature(dto.getPublicKey(), challenge, dto.getSignature());

    redisTemplate.delete("webauthn:challenge:" + userId);
    webAuthnCredentialRepository.save(WebauthnCredential.builder()
            .userId(userId)
            .credentialId(dto.getCredentialId())
            .publicKey(dto.getPublicKey())
            .signCount(0L)
            .deviceName(dto.getDeviceName())
        .build()
    );

    return WebAuthnRegistrationResponse.builder()
        .message("WebAuthn successfully registered")
        .build();

  }

  public WebAuthnAuthenticationResponse initiateAuthentication(WebAuthnAuthenticationRequest dto) {

    UUID userId = resolveUseId(dto.getMfaSessionToken());
    String challenge = UUID.randomUUID().toString();

    redisTemplate.opsForValue().set(
      "webauthn:challenge:" + userId,
        challenge,
        Duration.ofMinutes(5)
    );

    return WebAuthnAuthenticationResponse.builder()
        .challenge(challenge)
        .userId(userId)
        .build();
  }

  public MfaVerifyResponse completeAuthentication(WebAuthnAuthenticationCompleteRequest dto) {
    UUID userId = resolveUseId(dto.getMfaSessionToken());

    String challenge = redisTemplate.opsForValue().get("webauthn:challenge:" + userId);

    if (challenge == null) {
        throw new InvalidCredentialsException();
    }

    WebauthnCredential credential = webAuthnCredentialRepository
        .findByCredentialId(dto.getCredentialId())
        .orElseThrow(InvalidCredentialsException::new);

    verifySignature(credential.getPublicKey(), challenge, dto.getSignature());

    if (dto.getSignCount() <= credential.getSignCount()) {
        throw new InvalidCredentialsException();
    }

    credential.setSignCount(dto.getSignCount());
    webAuthnCredentialRepository.save(credential);

    redisTemplate.delete("webauthn:challenge:" + userId);
    redisTemplate.delete("mfa:" + dto.getMfaSessionToken());

    return MfaVerifyResponse.builder()
        .message("WebAuthn successfully verified")
        .build();
  }

  private void verifySignature(byte[] publicKeyBytes, String challenge, String signature) {

    try {

      KeyFactory keyFactory = KeyFactory.getInstance("EC");
      PublicKey publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(publicKeyBytes));

        Signature sig = Signature.getInstance("SHA256withECDSA");
        sig.initVerify(publicKey);
        sig.update(challenge.getBytes());

        byte[] signatureBytes = Base64.getDecoder().decode(signature);

        if (!sig.verify(signatureBytes)) {
            throw new InvalidCredentialsException();
        }

    } catch (InvalidCredentialsException e) {
      throw new InvalidCredentialsException();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private UUID resolveUseId(String mfaSession) {
      String userId = redisTemplate.opsForValue().get("mfa:" + mfaSession);
      if (userId == null) {
          throw new InvalidCredentialsException();
      }
      return UUID.fromString(userId);
  }


}
