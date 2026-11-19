package com.mazr.zerotrust.authservice.service;

import com.mazr.zerotrust.authservice.exception.InvalidTokenException;
import com.mazr.zerotrust.authservice.exception.TokenReuseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Slf4j
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

    public RefreshSession consume(String token) {

        String stored = redisTemplate.opsForValue().get("refresh:" + token);

        if (stored == null) {
            throw new InvalidTokenException();
        }

        RefreshSession refreshSession = parse(stored);

        if (Boolean.TRUE.equals(redisTemplate.hasKey("family:revoked:" + refreshSession.familyId()))) {
            throw new InvalidTokenException();
        }

        Boolean wrote = redisTemplate.opsForValue()
            .setIfAbsent("refresh:used:" + token, "1", Duration.ofDays(7));

        if (!Boolean.TRUE.equals(wrote)) {
            redisTemplate.opsForValue().set("family:revoked:" + refreshSession.familyId(), "1", Duration.ofDays(7));
            log.warn("Refresh token reuse detected for family {}", refreshSession.familyId());
            throw new TokenReuseException();
        }

        return refreshSession;

    }

    private RefreshSession parse(String stored) {

        String[] parts = stored.split(":");

        if (parts.length != 2) {
            throw new InvalidTokenException();
        }

        try {
            UUID userId = UUID.fromString(parts[0]);
            UUID familyId = UUID.fromString(parts[1]);

            return new RefreshSession(userId, familyId);
        } catch (IllegalArgumentException e) {
            throw new InvalidTokenException(e);
        }
    }

}
