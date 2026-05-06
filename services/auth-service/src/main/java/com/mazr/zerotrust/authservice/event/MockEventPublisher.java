package com.mazr.zerotrust.authservice.event;

import com.mazr.zerotrust.authservice.model.event.AuthEvent;
import org.springframework.stereotype.Service;

@Service
public class MockEventPublisher implements EventPublisher {
    @Override
    public void publish(AuthEvent authEvent) {

    }
}
