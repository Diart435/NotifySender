package com.example.notifySystem.Service;

import com.example.notifySystem.DTO.Message;
import com.example.notifySystem.DTO.NotifyKafkaDTO;
import com.example.notifySystem.Entity.Notification;
import com.example.notifySystem.Entity.User;
import com.example.notifySystem.Enum.Channel;
import com.example.notifySystem.Enum.NotificationStatus;
import com.example.notifySystem.Mapper.NotificationMapper;
import com.example.notifySystem.Mapper.UserMapper;
import com.example.notifySystem.Repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
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
        notification.setChannel(Channel.fromString(channel));
        notification.setRecipient(recipient);
        notification.setContent(content);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setStatus(NotificationStatus.PENDING);
        Message<NotifyKafkaDTO> message = null;
        switch (notification.getChannel()){
            case Channel.SMS:
                if(user.getPhone() != null){
                    message = new Message<>("sms", notificationMapper.toKafkaDTO(notification));
                    log.info("sms topic at {}", notification.getId());
                }
                break;
            case Channel.EMAIL:
                if(user.getEmail() != null){
                    message = new Message<>("email", notificationMapper.toKafkaDTO(notification));
                    log.info("email topic at {}", notification.getId());
                }
                break;
            case Channel.PUSH:
                if(user.getPushToken() != null){
                    message = new Message<>("push", notificationMapper.toKafkaDTO(notification));
                    log.info("push topic at {}", notification.getId());
                }
                break;
        }
        if(message != null) {
            kafkaProducer.sendToKafka(message);
        }
        notificationRepository.save(notification);
    }

}
