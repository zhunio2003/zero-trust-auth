package com.mazr.zerotrust.authservice.event;

import com.mazr.zerotrust.authservice.model.event.AuthEvent;

public interface EventPublisher {
  void publish(AuthEvent authEvent);
}
