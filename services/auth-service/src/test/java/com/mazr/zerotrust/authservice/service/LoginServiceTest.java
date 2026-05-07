package com.mazr.zerotrust.authservice.service;

import com.mazr.zerotrust.authservice.exception.InvalidCredentialsException;
import com.mazr.zerotrust.authservice.model.dto.LoginRequest;
import com.mazr.zerotrust.authservice.model.dto.LoginResponse;
import com.mazr.zerotrust.authservice.model.entity.User;
import com.mazr.zerotrust.authservice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LoginServiceTest {

  @Mock
  private UserRepository userRepository;
  @Mock
  private PasswordEncoder passwordEncoder;
  @Mock
  private StringRedisTemplate redisTemplate;
  @Mock
  ValueOperations<String, String> ops;

  @InjectMocks
  private LoginService loginService;

  @Test
  public void loginSuccessful() {

    when(redisTemplate.opsForValue()).thenReturn(ops);

    LoginRequest request = new LoginRequest();

    request.setEmail("test@gmai.com");
    request.setPassword("12345");

    User user = User.builder()
            .id(UUID.randomUUID())
            .email("test@gmail.com")
            .passwordHash("hashedPassword")
            .build();

    when(userRepository.findByEmail("test@gmai.com")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("12345", "hashedPassword")).thenReturn(true);

    LoginResponse response = loginService.login(request);

    assertNotNull(response.getMfaMethod());
    assertNotNull(response.getMfaSessionToken());

  }

    @Test
    public void loginEmailNotExistsThrowsException() {

        LoginRequest request = new LoginRequest();

        request.setEmail("existing@gmail.com");
        request.setPassword("12345");

        when(userRepository.findByEmail("existing@gmail.com")).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class,
          () -> loginService.login(request)
        );
    }

    @Test
    public void loginPasswordIncorrectThrowsException() {

        LoginRequest request = new LoginRequest();

        request.setEmail("existing@gmail.com");
        request.setPassword("12345");

        User user = User.builder()
            .id(UUID.randomUUID())
            .email("test@gmail.com")
            .passwordHash("hashedPassword")
            .build();

        when(userRepository.findByEmail("existing@gmail.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("12345", "hashedPassword")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class,
            () -> loginService.login(request)
        );
    }

}
