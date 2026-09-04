package com.notify.processor.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class DeduplicationService {
    private final RedisTemplate<String, String> redisTemplate;
    private static final Duration TIME_WINDOW = Duration.ofSeconds(5);
    private static final String KEY = "dedup:key:";

    public boolean isDuplicate(String key){
        return redisTemplate.hasKey(KEY + key);
    }

    public void markAsProcessed(String key){
        redisTemplate.opsForValue().set(
                KEY + key,
                String.valueOf(System.currentTimeMillis()),
                TIME_WINDOW
        );
    }

    public void updateTTL(String key){
        redisTemplate.expire(key, TIME_WINDOW);
    }
}
