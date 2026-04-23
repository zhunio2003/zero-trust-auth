package com.mazr.zerotrust.authservice.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TotpSetupRequest {

  @NotBlank
  private String mfaSessionToken;

}
