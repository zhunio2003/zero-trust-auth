package com.mazr.zerotrust.authservice.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.mazr.zerotrust.authservice.exception.EmailAlreadyExistsException;
import com.mazr.zerotrust.authservice.model.dto.RegisterRequest;
import com.mazr.zerotrust.authservice.model.dto.RegisterResponse;
import com.mazr.zerotrust.authservice.model.entity.User;
import com.mazr.zerotrust.authservice.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class RegistrationService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public RegisterResponse register(RegisterRequest dto) {

    if (userRepository.existsByEmail(dto.getEmail())) {
      throw new EmailAlreadyExistsException(dto.getEmail());
    }   
    
    User user = User.builder()
      .email(dto.getEmail())
      .password(passwordEncoder.encode(dto.getPassword()))
      .role(dto.getRole())
      .department(dto.getDepartment())
      .enabled(true)
      .build();

    User saved = userRepository.save(user);

    return RegisterResponse.builder()
      .id(saved.getId())
      .email(saved.getEmail())
      .role(saved.getRole())
      .department(saved.getDepartment())
      .createdAt(saved.getCreatedAt())
      .build();
    
  }



}
