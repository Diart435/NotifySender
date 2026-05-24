package com.example.notifySystem.Service;

import com.example.notifySystem.Entity.User;
import com.example.notifySystem.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    @Transactional
    public User createUser(String phone, String email, String pushToken){
        User saved = userRepository.save(new User(phone, email, pushToken));
        saved.setCreatedAt(LocalDateTime.now());
        return saved;
    }
}
