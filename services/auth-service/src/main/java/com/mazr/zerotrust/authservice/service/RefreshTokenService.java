package com.mazr.zerotrust.authservice.service;

import com.mazr.zerotrust.authservice.exception.InvalidCredentialsException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class RefreshTokenService {

    private final StringRedisTemplate redisTemplate;

    public String generateRefreshToken(UUID userId) {

        String token = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set("refresh:" + token, userId.toString(), Duration.ofDays(7));

        return token;
    }

    public UUID getUserIdFromRefreshToken(String token) {

        String userId = redisTemplate.opsForValue().get("refresh:" + token);
        if(userId == null) {
            throw new InvalidCredentialsException();
        }
        return UUID.fromString(userId);
    }
}
