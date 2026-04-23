package com.mazr.zerotrust.authservice.model.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class TotpSetupResponse {

  private String secret;
  private String qrCodeUrl;

}
