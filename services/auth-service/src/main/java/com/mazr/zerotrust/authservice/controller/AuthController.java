package com.mazr.zerotrust.authservice.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mazr.zerotrust.authservice.model.dto.LoginRequest;
import com.mazr.zerotrust.authservice.model.dto.LoginResponse;
import com.mazr.zerotrust.authservice.model.dto.RegisterRequest;
import com.mazr.zerotrust.authservice.model.dto.RegisterResponse;
import com.mazr.zerotrust.authservice.service.LoginService;
import com.mazr.zerotrust.authservice.service.RegistrationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final RegistrationService registrationService;
  private final LoginService loginService;

  @PostMapping("/register")
  public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest dto) {
        
    RegisterResponse response = registrationService.register(dto);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PostMapping("/login")
  public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest dto) {

    return ResponseEntity.ok(loginService.login(dto));
  }
  

}
