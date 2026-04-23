package com.mazr.zerotrust.authservice.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class MfaVerifyRequest {

  @NotBlank
  private String mfaSessionToken;

  @NotBlank
  private String totpCode;

}
