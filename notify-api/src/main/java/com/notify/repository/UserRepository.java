package com.notify.repository;

import com.notify.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByPhoneOrEmailOrPushToken(String phone, String email, String pushToken);
}
