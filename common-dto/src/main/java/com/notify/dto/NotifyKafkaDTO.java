package com.notify.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class NotifyKafkaDTO implements DedupKey {
    private UUID id;

    private UUID userId;

    private String channel;

    private String recipient;

    private String content;

    private String status;

    private int retryCount;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Override
    public String getDedupKey(){
        return userId + ":" + channel + content.hashCode();
    }
}
