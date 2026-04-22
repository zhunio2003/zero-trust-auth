package com.mazr.zerotrust.authservice.model.entity;

import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users")
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id; 

  @Column(length = 255, nullable = false, unique = true)
  private String email;  

  @Column(length = 255, nullable = false)
  private String passwordHash; 

  @Column(length = 50, nullable = false )
  private String role;  
  
  @Column(length = 100, nullable =  false)
  private String department;  

  @Column(nullable = false)
  private boolean enabled; 

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private Instant createdAt;

}
