package com.notify.processor.entity;

import com.notify.processor.enums.NotificationStatus;
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
public class DeliveryLog {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String notificationId;

    private int attemptNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "result")
    private NotificationStatus result;

    private LocalDateTime createdAt;
}