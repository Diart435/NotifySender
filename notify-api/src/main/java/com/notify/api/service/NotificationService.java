package com.notify.api.service;

import com.notify.api.dto.BaseNotificationDTO;
import com.notify.api.entity.Notification;
import com.notify.api.interfaces.NotificationCreateStrategy;
import com.notify.api.mapper.NotificationMapper;
import com.notify.api.repository.NotificationRepository;
import com.notify.dto.Message;
import com.notify.dto.NotificationStatus;
import com.notify.dto.NotifyKafkaDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final KafkaProducer kafkaProducer;
    private final NotificationMapper notificationMapper;
    private final NotificationCreateStrategyFactory factory;
    private final UserService userService;

    @Transactional
    public void saveNotification(Notification notification) {
        notificationRepository.save(notification);
    }

    @Transactional
    public void setStatus(NotifyKafkaDTO dto){
        Optional<Notification> existing = notificationRepository.findById(dto.getId());
        if(existing.isPresent()){
            Notification notification = existing.get();
            notification.setStatus(NotificationStatus.valueOf(dto.getStatus()));
            notification.setRetryCount(dto.getRetryCount());
            notification.setUpdatedAt(LocalDateTime.now());
        }
    }

    public void sendMessage(Notification saved){
        Message<NotifyKafkaDTO> message = new Message<>(saved.getChannel().toString().toLowerCase(), notificationMapper.toKafkaDTO(saved));
        kafkaProducer.sendToKafka(message);
        log.info("Уведомление {} отправлено в топик {}", saved.getId(), message.topic());
    }

    public Notification createNotification(BaseNotificationDTO request, String userId){
        NotificationCreateStrategy<BaseNotificationDTO> strategy = factory.getStrategy(request);
        Notification notification = null;
        if(userId != null){
            notification = strategy.create(request, userId);
        }
        else {
            notification = strategy.create(request, "ADMIN");
        }
        return notification;
    }
}
