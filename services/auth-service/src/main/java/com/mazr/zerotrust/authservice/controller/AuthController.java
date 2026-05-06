package com.mazr.zerotrust.authservice.controller;

import com.mazr.zerotrust.authservice.model.dto.*;
import com.mazr.zerotrust.authservice.service.WebAuthnService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mazr.zerotrust.authservice.service.LoginService;
import com.mazr.zerotrust.authservice.service.RegistrationService;
import com.mazr.zerotrust.authservice.service.TotpService;

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
  private final TotpService totpService;
  private final WebAuthnService webAuthnService;

  @PostMapping("/register")
  public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest dto) {

    RegisterResponse response = registrationService.register(dto);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PostMapping("/login")
  public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest dto) {

    return ResponseEntity.ok(loginService.login(dto));
  }

  @PostMapping("/totp/setup")
  public ResponseEntity<TotpSetupResponse> totpSetup(@Valid @RequestBody TotpSetupRequest dto) {
      return ResponseEntity.ok(totpService.setup(dto));
  }

  @PostMapping("/totp/verify-setup")
  public ResponseEntity<MfaVerifyResponse> totpVerifySetup(@Valid @RequestBody MfaVerifyRequest dto) {
      return ResponseEntity.ok(totpService.verifySetup(dto));
  }

  @PostMapping("/mfa/verify")
  public ResponseEntity<MfaVerifyResponse> totpVerify(@Valid @RequestBody MfaVerifyRequest dto) {
      return ResponseEntity.ok(totpService.verify(dto));
  }

  @PostMapping("webauthn/register/init")
  public ResponseEntity<WebAuthnRegistrationInitResponse>  initRegistration(@Valid @RequestBody WebAuthnRegistrationInitRequest dto) {
      return ResponseEntity.ok(webAuthnService.initializeRegistration(dto));
  }

  @PostMapping("webauthn/register/complete")
  public ResponseEntity<WebAuthnRegistrationResponse> completeRegistration(@Valid @RequestBody WebAuthnRegistrationCompleteRequest dto) {
      return ResponseEntity.ok(webAuthnService.completeRegistration(dto));
  }

  @PostMapping("webauthn/authenticate/init")
  public ResponseEntity<WebAuthnAuthenticationResponse> initAuthentication(@Valid @RequestBody WebAuthnAuthenticationRequest dto) {
      return ResponseEntity.ok(webAuthnService.initiateAuthentication(dto));
  }

  @PostMapping("webauthn/authenticate/complete")
  public ResponseEntity<MfaVerifyResponse>  completeAuthentication(@Valid @RequestBody WebAuthnAuthenticationCompleteRequest dto) {
      return ResponseEntity.ok(webAuthnService.completeAuthentication(dto));
  }



}
