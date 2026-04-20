package com.mazr.zerotrust.authservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.mazr.zerotrust.authservice.exception.EmailAlreadyExistsException;
import com.mazr.zerotrust.authservice.model.dto.RegisterRequest;
import com.mazr.zerotrust.authservice.model.dto.RegisterResponse;
import com.mazr.zerotrust.authservice.model.entity.User;
import com.mazr.zerotrust.authservice.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @InjectMocks
  private RegistrationService registrationService;

  @Test
  void registerSuccess() {

    RegisterRequest request = new RegisterRequest();
    request.setEmail("test@example.com");
    request.setPassword("Password123!");
    request.setRole("USER");
    request.setDepartment("Engineering");

    User saved = User.builder()
      .id(UUID.randomUUID())
      .password("hashedPassword")
      .email("test@example.com")
      .department("Engineering")
      .role("USER")
      .createdAt(Instant.now())
      .enabled(true)
      .build();

    when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
    when(passwordEncoder.encode("Password123!")).thenReturn("hashedPassword");
    when(userRepository.save(any(User.class))).thenReturn(saved);

    RegisterResponse response = registrationService.register(request);

    assertNotNull(response.getId());
    assertEquals("test@example.com", response.getEmail());
    assertEquals("USER", response.getRole());
    assertEquals("Engineering", response.getDepartment());
    assertNotNull(response.getCreatedAt());

  }

  @Test
  void registerEmailAlreadyExistsThrowsException() {

    RegisterRequest request = new RegisterRequest();
    request.setEmail("existing@example.com");
    request.setRole("USER");
    request.setDepartment("Engineering");
    request.setPassword("Password123!");

    when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

    assertThrows(EmailAlreadyExistsException.class, 
        () -> registrationService.register(request)
    );
  }

  @Test
  void registerResponseDoesNotContainPassword() {

    RegisterRequest request = new RegisterRequest();

    request.setEmail("test@example.com");
    request.setPassword("Password123!");
    request.setRole("USER");
    request.setDepartment("Engineering");

    User saved = User.builder()
      .id(UUID.randomUUID())
      .email("test@example.com")
      .password("hashedPassword")
      .role("USER")
      .department("Engineering")
      .enabled(true)
      .createdAt(Instant.now())
      .build();

    when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
    when(passwordEncoder.encode("Password123!")).thenReturn("hashedPassword");
    when(userRepository.save(any(User.class))).thenReturn(saved);

    RegisterResponse response = registrationService.register(request);

    assertNotNull(response);
    assertEquals("test@example.com", response.getEmail());

  }



}
