package com.mazr.zerotrust.authservice.model.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponse {

  private String mfaSessionToken;
  private String mfaMethod;
}
