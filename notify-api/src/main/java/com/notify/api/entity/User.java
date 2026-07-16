package com.notify.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@NoArgsConstructor
@Getter
@Setter
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String phone;

    private String email;

    private String pushToken;

    private LocalDateTime createdAt;

    public User(String phone, String email, String pushToken){
        this.phone = phone;
        this.email = email;
        this.pushToken = pushToken;
    }
}
