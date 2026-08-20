package com.notify.processor.service;

import com.notify.dto.NotifyKafkaDTO;
import com.notify.processor.interfaces.NotificationProcessor;
import com.notify.processor.service.processor.ProcessorFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;
import org.springframework.kafka.support.Acknowledgment;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaConsumer {
    private final NotifyLogService logService;
    private final ObjectMapper objectMapper;
    private final DeduplicationService deduplicationService;
    private final ProcessorFactory factory;
    private final FeedbackSender fbSender;
    private final RedisRateLimiter rateLimiter;
    private final Map<String, Integer> limits = Map.of(
            "sms", 35, "email", 1, "push", 100
    );


    @KafkaListener(topics = "sms", groupId = "sms-group", containerFactory = "containerFactory", concurrency = "5")
    public void consumeSmsChannel(ConsumerRecord<String, NotifyKafkaDTO> consumerRecord, Acknowledgment ack) {
        consume("sms", consumerRecord.value(), ack);

    }

    @KafkaListener(topics = "push", groupId = "push-group", containerFactory = "containerFactory", concurrency = "5")
    public void consumePushChannel(ConsumerRecord<String, NotifyKafkaDTO> consumerRecord, Acknowledgment ack) {
        consume("push", consumerRecord.value(), ack);
    }

    @KafkaListener(topics = "email", groupId = "email-group", containerFactory = "containerFactory", concurrency = "5")
    public void consumeEmailChannel(ConsumerRecord<String, NotifyKafkaDTO> consumerRecord, Acknowledgment ack) {
        consume("email", consumerRecord.value(), ack);
    }

    private void consume(String channel, NotifyKafkaDTO dto, Acknowledgment ack) {
        try {
            int limit = limits.getOrDefault(channel, 10);
            while (!rateLimiter.tryAcquire(channel, limit, 2)) {
                    Thread.sleep(50);
            }

            logService.logSave(dto);

            NotificationProcessor processor = factory.getProcessor(channel);
            processor.process(dto);

            ack.acknowledge();

            log.debug("Обработано {} сообщение {}", channel, dto.getId());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Поток прерван, останавливаем обработку {}", dto.getId());

        } catch (Exception e) {
            log.error("Ошибка обработки {} сообщения {}", channel, dto.getId(), e);
            logService.logFailed(dto);
            fbSender.sendFeedback(dto);
            fbSender.sendToDLQ(dto);
            ack.acknowledge();
            log.warn("Сообщение {} отправлено в DLQ после ошибки", dto.getId());
        }
    }
}
