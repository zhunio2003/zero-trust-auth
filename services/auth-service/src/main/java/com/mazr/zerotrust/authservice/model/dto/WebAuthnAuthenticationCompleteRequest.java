package com.mazr.zerotrust.authservice.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class WebAuthnAuthenticationCompleteRequest {

  @NotBlank
  private String mfaSessionToken;

  @NotBlank
  private String credentialId;

  @NotBlank
  private  String signature;

  private long signCount;

}
