package com.example.notifySystem.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@NoArgsConstructor
@Setter
@Getter
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UUID userId;

    private String channel;

    private String recipient;

    private String content;

    private String status;

    private int retryCount;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
