package com.notify.api.service;

import com.notify.api.entity.User;
import com.notify.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder;

    @Transactional
    public boolean isApiKey(String apiKey){
        return userRepository.existsByApiKey(apiKey);
    }

    @Transactional
    public String createUserAndGetApiKey(String username, String email, String password){
        String apiKey = UUID.randomUUID().toString();
        User user = new User();
        user.setEmail(email);
        user.setUsername(username);
        user.setPasswordHash(encoder.encode(password));
        user.setCreatedAt(LocalDateTime.now());
        user.setApiKey(apiKey);
        userRepository.save(user);
        return apiKey;
    }
}
