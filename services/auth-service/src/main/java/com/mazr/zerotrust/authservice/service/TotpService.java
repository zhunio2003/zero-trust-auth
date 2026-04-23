package com.mazr.zerotrust.authservice.service;

import java.util.UUID;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.mazr.zerotrust.authservice.exception.InvalidCredentialsException;
import com.mazr.zerotrust.authservice.model.dto.MfaVerifyRequest;
import com.mazr.zerotrust.authservice.model.dto.MfaVerifyResponse;
import com.mazr.zerotrust.authservice.model.dto.TotpSetupRequest;
import com.mazr.zerotrust.authservice.model.dto.TotpSetupResponse;
import com.mazr.zerotrust.authservice.model.entity.TotpCredential;
import com.mazr.zerotrust.authservice.repository.TotpCredentialRepository;

import dev.samstevens.totp.code.CodeGenerator;
import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import static dev.samstevens.totp.util.Utils.getDataUriForImage;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TotpService {

  private final TotpCredentialRepository totpCredentialRepository;
  private final StringRedisTemplate redisTemplate;

  public TotpSetupResponse setup(TotpSetupRequest dto) {

    UUID userId = resolveUserId(dto.getMfaSessionToken());

    String secret = new DefaultSecretGenerator().generate();

    totpCredentialRepository.save(
      TotpCredential.builder()
        .userId(userId)
        .secret(secret)
        .verified(false)
        .build()
    );

    QrData qrData = new QrData.Builder()
      .label("ZeroTrust")
      .secret(secret)
      .issuer("Zero Trust Plataform")
      .algorithm(HashingAlgorithm.SHA1)
      .digits(6)
      .period(30)
      .build();

    try {

      QrGenerator generator = new ZxingPngQrGenerator();
      byte[] imageData = generator.generate(qrData);
      String qrCodeUrl = getDataUriForImage(imageData, generator.getImageMimeType());

      return TotpSetupResponse.builder()
        .secret(secret)
        .qrCodeUrl(qrCodeUrl)       
        .build();


    } catch (Exception e) {

      throw new RuntimeException("Error Generate QR", e);
      
    }

  }

  public MfaVerifyResponse verifySetup(MfaVerifyRequest dto) {

    UUID userId = resolveUserId(dto.getMfaSessionToken());

    TotpCredential credential = totpCredentialRepository.findByUserId(userId)
        .orElseThrow(InvalidCredentialsException::new);
    
    if (!validateCode(credential.getSecret(), dto.getTotpCode())) {
      throw new InvalidCredentialsException();
    }

    credential.setVerified(true);
    totpCredentialRepository.save(credential);

    return MfaVerifyResponse.builder()
      .message("TOTP configured correctly")
      .build();
  }

  public MfaVerifyResponse verify(MfaVerifyRequest dto) {

    UUID userId = resolveUserId(dto.getMfaSessionToken());

    TotpCredential credential = totpCredentialRepository.findByUserId(userId)
        .orElseThrow(InvalidCredentialsException::new);

    if(!credential.isVerified()) {
        throw new InvalidCredentialsException();
    }
    
    if (!validateCode(credential.getSecret(), dto.getTotpCode())) {
      throw new InvalidCredentialsException();
    }

    redisTemplate.delete("mfa:" + dto.getMfaSessionToken());

    return MfaVerifyResponse.builder()
      .message("MFA verified correctly")
      .build();
  }


  

  private UUID resolveUserId(String mfaSessionToken) {

    String userId = redisTemplate.opsForValue().get("mfa:" + mfaSessionToken);

    if (userId == null) {
      throw new InvalidCredentialsException();
    }

    return UUID.fromString(userId);
    
  }

  private boolean validateCode(String secret, String code) {

    CodeGenerator codeGenerator =  new DefaultCodeGenerator();
    CodeVerifier verifier = new DefaultCodeVerifier(codeGenerator, new SystemTimeProvider());

    return verifier.isValidCode(secret, code);

  }

}
