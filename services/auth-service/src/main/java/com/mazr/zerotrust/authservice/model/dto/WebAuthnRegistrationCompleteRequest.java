package com.mazr.zerotrust.authservice.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class WebAuthnRegistrationCompleteRequest {

  @NotBlank
  private String mfaSessionToken;

  @NotBlank
  private String credentialId;

  @NotNull
  private byte[] publicKey;

  @NotBlank
  private String signature;

  @NotBlank
  private String deviceName;

}
