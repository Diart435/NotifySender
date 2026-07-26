package com.notify.processor.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class DeduplicationService {
    private final RedisTemplate<String, String> redisTemplate;
    private static Duration TIME_WINDOW = Duration.ofSeconds(15);
    private static String KEY = "dedup:key:";

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
}
