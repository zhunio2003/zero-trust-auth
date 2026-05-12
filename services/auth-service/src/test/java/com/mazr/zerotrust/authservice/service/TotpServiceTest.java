package com.mazr.zerotrust.authservice.service;

import com.mazr.zerotrust.authservice.exception.InvalidCredentialsException;
import com.mazr.zerotrust.authservice.model.dto.MfaVerifyRequest;
import com.mazr.zerotrust.authservice.model.dto.MfaVerifyResponse;
import com.mazr.zerotrust.authservice.model.dto.TotpSetupRequest;
import com.mazr.zerotrust.authservice.model.dto.TotpSetupResponse;
import com.mazr.zerotrust.authservice.model.entity.TotpCredential;
import com.mazr.zerotrust.authservice.repository.TotpCredentialRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TotpServiceTest {

  @Mock
  private TotpCredentialRepository totpCredentialRepository;

  @Mock
  private StringRedisTemplate redisTemplate;

  @Mock
  ValueOperations<String, String> ops;

  @InjectMocks
  private TotpService totpService;

  @Test
  public void toptSetupSuccessful() {

    // Arrange
    String mfaSessionToken = "test-session-token";
    String userId = UUID.randomUUID().toString();

    when(redisTemplate.opsForValue()).thenReturn(ops);
    when(ops.get("mfa:" + mfaSessionToken)).thenReturn(userId);

    TotpSetupRequest request = new TotpSetupRequest();
    request.setMfaSessionToken(mfaSessionToken);

    // Act
    TotpSetupResponse response = totpService.setup(request);

    // ssert
    assertNotNull(response.getSecret());
    assertNotNull(response.getQrCodeUrl());

  }

    @Test
    public void totpVerifyInvalidCodeThrowsException() {

        // Arrange
        String mfaSessionToken = "test-session-token";
        String userId = UUID.randomUUID().toString();

        TotpCredential credential = TotpCredential.builder()
                .userId(UUID.fromString(userId))
                .secret("JBSWY3DPEHPK3PXP")
                .verified(false)
            .build();

        when(redisTemplate.opsForValue()).thenReturn(ops);
        when(ops.get("mfa:" + mfaSessionToken)).thenReturn(userId);
        when(totpCredentialRepository.findByUserId(UUID.fromString(userId))).thenReturn(Optional.of(credential));

        MfaVerifyRequest request = new MfaVerifyRequest();
        request.setMfaSessionToken(mfaSessionToken);
        request.setTotpCode("000000");

        // Act and Assert
        assertThrows(InvalidCredentialsException.class,
            () -> totpService.verifySetup(request)
        );

    }

    @Test
    public void totpSessionExpiredThrowsException() {

        // Arrange
        String mfaSessionToken = "expired-token";

        when(redisTemplate.opsForValue()).thenReturn(ops);
        when(ops.get("mfa:" + mfaSessionToken)).thenReturn(null);

        TotpSetupRequest request = new TotpSetupRequest();
        request.setMfaSessionToken(mfaSessionToken);

        // Act and Assert
        assertThrows(InvalidCredentialsException.class,
            () -> totpService.setup(request)
        );

    }

}
