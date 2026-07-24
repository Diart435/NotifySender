package com.notify.processor.service;

import com.notify.dto.NotifyKafkaDTO;
import com.notify.processor.dto.EmailPayload;
import com.notify.processor.dto.PushPayload;
import com.notify.processor.dto.SmsPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaConsumer {
    private final NotifyEmailService emailService;
    private final NotifySmsService smsService;
    private final NotifyPushService pushService;
    private final ObjectMapper objectMapper;


    @KafkaListener(topics = "sms", groupId = "sms-group", containerFactory = "containerFactory")
    public void consumeSmsChannel(ConsumerRecord<String, NotifyKafkaDTO> consumerRecord){
        log.info("Пришло сообщение: {}", consumerRecord.value().getId());
        String payload = consumerRecord.value().getPayload();

        SmsPayload smsPayload = objectMapper.readValue(payload, SmsPayload.class);
        smsService.logSave(consumerRecord.value());
    }
    @KafkaListener(topics = "push", groupId = "push-group", containerFactory = "containerFactory")
    public void consumePushChannel(ConsumerRecord<String, NotifyKafkaDTO> consumerRecord){
        log.info("Пришло сообщение: {}", consumerRecord.value().getId());
        String payload = consumerRecord.value().getPayload();

        PushPayload pushPayload = objectMapper.readValue(payload, PushPayload.class);
        pushService.logSave(consumerRecord.value());
    }
    @KafkaListener(topics = "email", groupId = "email-group", containerFactory = "containerFactory")
    public void consumeEmailChannel(ConsumerRecord<String, NotifyKafkaDTO> consumerRecord){
        log.info("Пришло сообщение: {}", consumerRecord.value().getId());
        String payload = consumerRecord.value().getPayload();

        EmailPayload emailPayload = objectMapper.readValue(payload, EmailPayload.class);
        emailService.logSave(consumerRecord.value());
    }
}
