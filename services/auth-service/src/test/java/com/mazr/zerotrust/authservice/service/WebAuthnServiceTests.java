package com.mazr.zerotrust.authservice.service;

import com.mazr.zerotrust.authservice.exception.InvalidCredentialsException;
import com.mazr.zerotrust.authservice.model.dto.WebAuthnAuthenticationCompleteRequest;
import com.mazr.zerotrust.authservice.model.entity.WebauthnCredential;
import com.mazr.zerotrust.authservice.repository.WebAuthnCredentialRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
public class WebAuthnServiceTests {

  @Mock
  private WebAuthnCredentialRepository webAuthnCredentialRepository;

  @Mock
  private StringRedisTemplate redisTemplate;

  @Mock
  ValueOperations<String, String> ops ;

  @InjectMocks
  private WebAuthnService webAuthnService;

  @Test
  public void invalidSignature() {

    // Arrange
    String mfaSessionToken = "test-token";
    String userId = UUID.randomUUID().toString();

    WebauthnCredential credential = WebauthnCredential.builder()
        .userId(UUID.fromString(userId))
        .credentialId("cred-123")
        .publicKey(new byte[]{1, 2, 30})
        .signCount(0L)
        .build();

    when(redisTemplate.opsForValue()).thenReturn(ops);
    when((ops.get("mfa:" + mfaSessionToken))).thenReturn(userId);
    when(ops.get("webauthn:challenge:" + UUID.fromString(userId))).thenReturn("challenge-abc");
    when(webAuthnCredentialRepository.findByCredentialId("cred-123")).thenReturn(Optional.of(credential));

    WebAuthnAuthenticationCompleteRequest request = WebAuthnAuthenticationCompleteRequest.builder()
        .mfaSessionToken(mfaSessionToken)
        .signature("invalidSignature")
        .credentialId("cred-123")
        .signCount(1L)
        .build();

    // Act & Assert
    assertThrows(InvalidCredentialsException.class,
        () -> webAuthnService.completeAuthentication(request)
    );

  }


}
