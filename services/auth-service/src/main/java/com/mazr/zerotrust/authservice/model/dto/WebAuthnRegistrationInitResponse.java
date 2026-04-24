package com.mazr.zerotrust.authservice.model.dto;

import java.util.UUID;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class WebAuthnRegistrationInitResponse {
 
  private String challenge;
  private UUID userId;

}
