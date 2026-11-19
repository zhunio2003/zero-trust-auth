package com.mazr.zerotrust.authservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class RefreshTokenService {

    private final StringRedisTemplate redisTemplate;

    public String generateRefreshToken(UUID userId) {

        return generateRefreshToken(userId, UUID.randomUUID());

    }

    public String generateRefreshToken(UUID userId, UUID familyId) {

        String token = UUID.randomUUID().toString();

        redisTemplate.opsForValue().set("refresh:" + token, userId + ":" + familyId, Duration.ofDays(7));

        return token;

    }

}
