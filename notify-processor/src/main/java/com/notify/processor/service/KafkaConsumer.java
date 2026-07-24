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
    private final NotifySmsService smsService;
    private final NotifyPushService pushService;

    @KafkaListener(topics = "notifications", groupId = "notify-group", containerFactory = "containerFactory")
    public void consumeNotifyChannel(ConsumerRecord<String, NotifyKafkaDTO> consumerRecord){
        log.info("Пришло сообщение: {}", consumerRecord.value().getId());
    }

    /*
    @KafkaListener(topics = "sms", groupId = "sms-group", containerFactory = "containerFactory")
    public void consumeSmsChannel(ConsumerRecord<String, NotifyKafkaDTO> consumerRecord){
        log.info("Пришло сообщение: {}", consumerRecord.value().getId());
    }
    @KafkaListener(topics = "push", groupId = "push-group", containerFactory = "containerFactory")
    public void consumePushChannel(ConsumerRecord<String, NotifyKafkaDTO> consumerRecord){
        log.info("Пришло сообщение: {}", consumerRecord.value().getId());
    }
    @KafkaListener(topics = "email", groupId = "email-group", containerFactory = "containerFactory")
    public void consumeEmailChannel(ConsumerRecord<String, NotifyKafkaDTO> consumerRecord){
        log.info("Пришло сообщение: {}", consumerRecord.value().getId());
    }*/
}
