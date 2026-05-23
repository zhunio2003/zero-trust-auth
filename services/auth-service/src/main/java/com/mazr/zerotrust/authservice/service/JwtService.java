package com.mazr.zerotrust.authservice.service;

import com.mazr.zerotrust.authservice.config.RsaKeyProperties;
import com.mazr.zerotrust.authservice.model.entity.User;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class JwtService {

  private final RsaKeyProperties rsaKeyProperties;

  @Value("${jwt.access-token-expiry}")
  private Duration accessTokenExpiry;

  public String generateToken(User user) {
      return Jwts.builder()
          .subject(user.getId().toString())
          .claim("role", user.getRole())
          .claim("department", user.getDepartment())
          .issuedAt(new Date())
          .expiration(new Date(System.currentTimeMillis() + accessTokenExpiry.toMillis()))
          .signWith(rsaKeyProperties.getPrivateKey())
          .compact();
  }
}
