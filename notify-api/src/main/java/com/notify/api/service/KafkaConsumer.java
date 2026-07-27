package com.notify.api.service;

import com.notify.dto.NotifyKafkaDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaConsumer {
    private final NotificationService notificationService;
    @KafkaListener(topics = "notify-feedback", groupId = "feedback-group", containerFactory = "containerFactory")
    public void consumeFeedbackChannel(ConsumerRecord<String, NotifyKafkaDTO> consumerRecord, Acknowledgment ack){
        NotifyKafkaDTO dto = consumerRecord.value();
        log.info("Пришел ответ от: {}, {}", dto.getId(),dto.getStatus());
        notificationService.setStatus(dto);
        ack.acknowledge();
    }
}
