package com.notify.processor.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
@Slf4j
@RequiredArgsConstructor
public class RedisRateLimiter {
    private final StringRedisTemplate template;

    public boolean tryAcquire(String channel, int limit, int ttlSeconds) {
        String key = "rate:" + channel + ":" + Instant.now().getEpochSecond();

        Long current = template.opsForValue().increment(key);
        if (current == 1) {
            template.expire(key, Duration.ofSeconds(ttlSeconds));
        }

        return current <= limit;
    }
}
