package com.notify.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class NotifyKafkaDTO implements DedupKey {
    private UUID id;

    private String userId;

    private String channel;

    private String payload;

    private String status;

    private int retryCount;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Override
    public String getDedupKey(){
        return userId + ":" + channel + payload.hashCode();
    }

    @Override
    public String getPartitionKey(){
        return userId.toString();
    }
}
