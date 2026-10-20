package com.mazr.zerotrust.authservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RefreshTokenServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOps;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    private UUID userId;

    @BeforeEach
    public void setUp() {
        userId = UUID.randomUUID();
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
    }

    @Test
    public void existsToken() {

        String refreshToken = refreshTokenService.generateRefreshToken(userId);

        assertNotNull(refreshToken);

        verify(redisTemplate.opsForValue()).set(
            contains("refresh"),
            eq(userId.toString()),
            any(Duration.class)
        );
    }


}
