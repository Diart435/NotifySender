package com.notify.api.service;

import com.notify.api.entity.User;
import com.notify.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;

    @Transactional
    public User createUser(String phone, String email, String pushToken){
        Optional<User> existing = userRepository.findByPhoneOrEmailOrPushToken(phone, email, pushToken);
        if(existing.isPresent()){
            User user = existing.get();
            if(phone != null) user.setPhone(phone);
            if(email != null) user.setEmail(email);
            if(pushToken != null) user.setPushToken(pushToken);
            log.info("User updated");
            return user;
        }
        else{
            User saved = userRepository.save(new User(phone, email, pushToken));
            log.info("User created: {}", saved.getId());
            return saved;
        }
    }
}
