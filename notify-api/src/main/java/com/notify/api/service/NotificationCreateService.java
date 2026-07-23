package com.notify.api.service;

import com.notify.api.dto.BaseNotificationDTO;
import com.notify.api.entity.Notification;
import com.notify.api.interfaces.NotificationCreateStrategy;
import com.notify.api.mapper.NotificationMapper;
import com.notify.api.repository.NotificationRepository;
import com.notify.dto.Message;
import com.notify.dto.NotifyKafkaDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationCreateService {
    private final NotificationRepository notificationRepository;
    private final KafkaProducer kafkaProducer;
    private final NotificationMapper notificationMapper;
    private final NotificationCreateStrategyFactory strategyFactory;

    @Transactional
    public void create(BaseNotificationDTO request, String userId) {
        NotificationCreateStrategy<BaseNotificationDTO> strategy = strategyFactory.getStrategy(request);

        Notification saved = notificationRepository.save(strategy.create(request, userId));
        Message<NotifyKafkaDTO> message = new Message<>(saved.getChannel().toString().toLowerCase(), notificationMapper.toKafkaDTO(saved));
        kafkaProducer.sendToKafka(message);
        log.info("Уведомление {} отправлено в топик {}", saved.getId(), message.topic());
    }

}
