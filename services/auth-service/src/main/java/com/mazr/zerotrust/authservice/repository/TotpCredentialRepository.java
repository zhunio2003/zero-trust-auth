package com.mazr.zerotrust.authservice.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mazr.zerotrust.authservice.model.entity.TotpCredential;

public interface TotpCredentialRepository  extends JpaRepository<TotpCredential, UUID>{

  Optional<TotpCredential> findByUserId(UUID userId);

}
