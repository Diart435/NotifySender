package com.notify.api.service;

import com.notify.api.entity.User;
import com.notify.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder;
    private final RedisTemplate<String, String> redisTemplate;
    private final static String KEY = "api-key:";
    private final static Duration time = Duration.ofMinutes(30);

    @Transactional(readOnly = true)
    public boolean validateKey(String rawKey){
        String rawLookup = DigestUtils.sha256Hex(rawKey);
        String key = KEY + rawLookup;
        boolean existing = false;
        if(Boolean.TRUE.equals(redisTemplate.hasKey(key))){
            redisTemplate.expire(key, time);
            existing = true;
        }
        else {
            Optional<User> user = userRepository.findByApiKeyLookup(rawLookup);
            existing = user.isPresent();
            boolean isValid = false;
            if(existing) {
                isValid = encoder.matches(rawKey, user.get().getApiKey());
                if(isValid){
                    redisTemplate.opsForValue().set(key, String.valueOf(user.get().getId()), time);
                }
                existing = isValid;
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
        user.setApiKeyLookup(DigestUtils.sha256Hex(apiKey));
        userRepository.save(user);
        return apiKey;
    }
}
