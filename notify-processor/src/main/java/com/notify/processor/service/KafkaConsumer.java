package com.notify.processor.service;

import com.notify.dto.NotifyKafkaDTO;
import com.notify.processor.dto.EmailPayload;
import com.notify.processor.dto.PushPayload;
import com.notify.processor.dto.SmsPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.cache.autoconfigure.CacheProperties;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import org.springframework.kafka.support.Acknowledgment;
@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaConsumer {
    private final NotifyEmailService emailService;
    private final NotifySmsService smsService;
    private final NotifyPushService pushService;
    private final ObjectMapper objectMapper;
    private final DeduplicationService deduplicationService;


    @KafkaListener(topics = "sms", groupId = "sms-group", containerFactory = "containerFactory")
    public void consumeSmsChannel(ConsumerRecord<String, NotifyKafkaDTO> consumerRecord, Acknowledgment ack){
        try {
            String dedupKey = consumerRecord.value().getDedupKey();
            if (!deduplicationService.isDuplicate(dedupKey)) {
                String payload = consumerRecord.value().getPayload();

                SmsPayload smsPayload = objectMapper.readValue(payload, SmsPayload.class);
                smsService.logSave(consumerRecord.value());
                log.info("Пришло сообщение: {}", consumerRecord.value().getId());
            }
            deduplicationService.markAsProcessed(dedupKey);
        }
        catch (JacksonException e){
            log.error("Ошибка парсинга уведомления после передачи kafka");
        }
        finally {
            ack.acknowledge();
        }
    }
    @KafkaListener(topics = "push", groupId = "push-group", containerFactory = "containerFactory")
    public void consumePushChannel(ConsumerRecord<String, NotifyKafkaDTO> consumerRecord, Acknowledgment ack){
        try {
            String dedupKey = consumerRecord.value().getDedupKey();
            if (!deduplicationService.isDuplicate(dedupKey)) {
                String payload = consumerRecord.value().getPayload();

                PushPayload pushPayload = objectMapper.readValue(payload, PushPayload.class);
                pushService.logSave(consumerRecord.value());
                log.info("Пришло сообщение: {}", consumerRecord.value().getId());
            }
            deduplicationService.markAsProcessed(dedupKey);
        }
        catch (JacksonException e){
            log.error("Ошибка парсинга уведомления после передачи kafka");
        }
        finally {
            ack.acknowledge();
        }
    }
    @KafkaListener(topics = "email", groupId = "email-group", containerFactory = "containerFactory")
    public void consumeEmailChannel(ConsumerRecord<String, NotifyKafkaDTO> consumerRecord, Acknowledgment ack){
        try {
            String dedupKey = consumerRecord.value().getDedupKey();
            if (!deduplicationService.isDuplicate(dedupKey)) {
                String payload = consumerRecord.value().getPayload();

                EmailPayload emailPayload = objectMapper.readValue(payload, EmailPayload.class);
                emailService.logSave(consumerRecord.value());
                log.info("Пришло сообщение: {}", consumerRecord.value().getId());
            }
            deduplicationService.markAsProcessed(dedupKey);
        }
        catch (JacksonException e){
            log.error("Ошибка парсинга уведомления после передачи kafka");
        }
        finally {
            ack.acknowledge();
        }
    }
}
