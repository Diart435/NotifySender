package com.example.notifySystem.Repository;

import com.example.notifySystem.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByPhoneOrEmailOrPushToken(String phone, String email, String pushToken);
}
