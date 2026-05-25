package com.example.notifySystem.Entity;

import com.example.notifySystem.Enum.Channel;
import com.example.notifySystem.Enum.NotificationStatus;
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
    private User userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel")
    private Channel channel;

    private String recipient;

    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private NotificationStatus status;

    private int retryCount;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public Notification(User user){
        this.userId = user;
        this.createdAt = LocalDateTime.now();
    }
}
