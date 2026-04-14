package com.mazr.zerotrust.authservice.model.dto;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RegisterResponse {

  private UUID id;
  private String email;
  private String role;
  private String department;
  private Instant createdAt;

}
