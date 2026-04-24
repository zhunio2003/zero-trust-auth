package com.mazr.zerotrust.authservice.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mazr.zerotrust.authservice.model.entity.WebauthnCredential;

public interface WebAuthnCredentialRepository extends JpaRepository<WebauthnCredential, UUID>{


  Optional<WebauthnCredential> findByUserId(UUID userId);
  Optional<WebauthnCredential> findByCredentialId(String credentialId);

}
