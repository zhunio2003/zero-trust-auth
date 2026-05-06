package com.mazr.zerotrust.authservice.model.event;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Setter
@Getter
@Builder
public class AuthEvent {

  private UUID userId;
  private String ip;

  @Builder.Default
  private OffsetDateTime timestamp = OffsetDateTime.now();

  private String userAgent;
  private String result;

}
