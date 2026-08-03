package com.notify.api.repository;

import com.notify.api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    boolean existsByApiKey(String apiKey);
}
