package com.mazr.zerotrust.authservice.model.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class MfaVerifyResponse {

  private String message;    

}
