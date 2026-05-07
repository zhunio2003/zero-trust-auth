package com.mazr.zerotrust.authservice.service;

import java.time.Duration;
import java.util.UUID;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.mazr.zerotrust.authservice.exception.InvalidCredentialsException;
import com.mazr.zerotrust.authservice.model.dto.LoginRequest;
import com.mazr.zerotrust.authservice.model.dto.LoginResponse;
import com.mazr.zerotrust.authservice.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class LoginService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final StringRedisTemplate redisTemplate;

  public LoginResponse login(LoginRequest dto) {
    var user = userRepository.findByEmail(dto.getEmail())
        .orElseThrow(InvalidCredentialsException::new);

    if (!passwordEncoder.matches(dto.getPassword(), user.getPasswordHash())) {
        throw new InvalidCredentialsException();
    }

    String mfaSessionToken = UUID.randomUUID().toString();

    redisTemplate.opsForValue().set(
        "mfa:" + mfaSessionToken,
        user.getId().toString(),
        Duration.ofMinutes(5)
    );

    return LoginResponse.builder()
        .mfaSessionToken(mfaSessionToken)
        .mfaMethod("TOTP")
        .build();

  }
}
