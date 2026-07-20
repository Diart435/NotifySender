package com.notify.api.service;

import com.notify.api.entity.Notification;
import com.notify.api.entity.User;
import com.notify.api.enums.Channel;
import com.notify.api.mapper.NotificationMapper;
import com.notify.api.mapper.UserMapper;
import com.notify.api.repository.NotificationRepository;
import com.notify.dto.Message;
import com.notify.dto.NotifyKafkaDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final KafkaProducer kafkaProducer;
    private final NotificationMapper notificationMapper;
    private final UserMapper userMapper;

    @Transactional
    public void createNotification(String channel, String recipient, String content, User user){
        Notification notification = userMapper.toNotification(user);
        log.info("Notification userID: {}", notification.getUserId());
        notification.setChannel(Channel.fromString(channel));
        notification.setRecipient(recipient);
        notification.setContent(content);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setStatus("PENDING");
        Notification saved = notificationRepository.save(notification);
        log.info("Notification channel: {}", saved.getChannel());
        log.info("Notification recipient: {}", saved.getRecipient());
        log.info("Notification created at: {}",saved.getCreatedAt());
        Message<NotifyKafkaDTO> message = null;
        switch (saved.getChannel()){
            case Channel.SMS:
                if(user.getPhone() != null){
                    message = new Message<>("sms", notificationMapper.toKafkaDTO(saved));
                    log.info("sms topic at {}", saved.getId());
                }
                break;
            case Channel.EMAIL:
                if(user.getEmail() != null){
                    message = new Message<>("email", notificationMapper.toKafkaDTO(saved));
                    log.info("email topic at {}", saved.getId());
                }
                break;
            case Channel.PUSH:
                if(user.getPushToken() != null){
                    message = new Message<>("push", notificationMapper.toKafkaDTO(saved));
                    log.info("push topic at {}", saved.getId());
                }
                break;
        }
        if(message != null) {
            kafkaProducer.sendToKafka(message);
        }
    }

}
