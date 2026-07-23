package com.notify.api.service;

import com.notify.api.dto.RequestSmsDTO;
import com.notify.api.entity.Notification;
import com.notify.api.enums.Channel;
import com.notify.api.interfaces.NotificationCreateStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class SmsNotificationCreateStrategy implements NotificationCreateStrategy<RequestSmsDTO> {
    private final ObjectMapper objectMapper;
    @Override
    public Notification create(RequestSmsDTO dto, String userId) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("userPhone", dto.getUserPhone());
        payload.put("targetPhone", dto.getTargetPhone());
        payload.put("senderId", dto.getSenderId());
        payload.put("content", dto.getContent());
        String payloadJSON = objectMapper.writeValueAsString(payload);

        return Notification.builder()
                .userId(userId)
                .channel(Channel.SMS)
                .createdAt(LocalDateTime.now())
                .payload(payloadJSON)
                .status("PENDING")
                .retryCount(0)
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
