package com.mazr.zerotrust.authservice.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.mazr.zerotrust.authservice.model.entity.User;

public interface UserRepository  extends JpaRepository<User, UUID> {
  
  Optional<User> findByEmail(String email);

  boolean existsByEmail(String email); 
}
