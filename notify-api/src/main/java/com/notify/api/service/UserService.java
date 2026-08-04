package com.notify.api.service;

import com.notify.api.entity.User;
import com.notify.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder;
    private final RedisTemplate<String, String> redisTemplate;
    private final static String KEY = "api-key:";
    private final static Duration time = Duration.ofSeconds(10);

    @Transactional(readOnly = true)
    public boolean isApiKey(String apiKey){
        String key = KEY + apiKey;
        boolean existing = true;
        if(redisTemplate.hasKey(key)){
            redisTemplate.expire(key, time);
        }
        else {
            existing = userRepository.existsByApiKey(apiKey);
            if (existing) {
                redisTemplate.opsForValue().set(key, apiKey, time);
            }
        }
        return existing;
    }

    @Transactional
    public String createUserAndGetApiKey(String username, String email, String password){
        String apiKey = UUID.randomUUID().toString();
        String hashed = encoder.encode(apiKey);
        User user = new User();
        user.setEmail(email);
        user.setUsername(username);
        user.setPasswordHash(encoder.encode(password));
        user.setCreatedAt(LocalDateTime.now());
        user.setApiKey(hashed);
        userRepository.save(user);
        return hashed;
    }
}
