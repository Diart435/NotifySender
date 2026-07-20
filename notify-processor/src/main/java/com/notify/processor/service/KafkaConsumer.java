package com.notify.processor.service;

import com.notify.dto.NotifyKafkaDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaConsumer {
    private final NotifyEmailService emailService;

    @KafkaListener(topics = "email", groupId = "email-group", containerFactory = "containerFactory")
    public void consumeEmailChannel(ConsumerRecord<String, NotifyKafkaDTO> consumerRecord){
        log.info("new message arrived ID: {}", consumerRecord.value().getId());
        log.info("new message arrived ID: {}", consumerRecord.value().getRecipient());
        log.info("new message arrived ID: {}", consumerRecord.value().getUserId());
        log.info("new message arrived ID: {}", consumerRecord.value().getContent());
        emailService.logSave(consumerRecord.value());
    }
}
