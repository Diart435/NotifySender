package com.notify.processor.service;

import com.notify.dto.NotifyKafkaDTO;
import com.notify.processor.service.processor.ProcessorFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.kafka.support.Acknowledgment;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaConsumer {
    private final ProcessorFactory factory;
    private final QueueService queueService;
    private final DeduplicationService deduplicationService;


    @KafkaListener(topics = "sms", groupId = "sms-group", containerFactory = "containerFactory", concurrency = "5")
    public void consumeSmsChannel(ConsumerRecord<String, NotifyKafkaDTO> consumerRecord, Acknowledgment ack) {
        consume("sms", consumerRecord, ack);

    }

    @KafkaListener(topics = "push", groupId = "push-group", containerFactory = "containerFactory", concurrency = "5")
    public void consumePushChannel(ConsumerRecord<String, NotifyKafkaDTO> consumerRecord, Acknowledgment ack) {
        consume("push", consumerRecord, ack);
    }

    @KafkaListener(topics = "email", groupId = "email-group", containerFactory = "containerFactory", concurrency = "5")
    public void consumeEmailChannel(ConsumerRecord<String, NotifyKafkaDTO> consumerRecord, Acknowledgment ack) {
        consume("email", consumerRecord, ack);
    }

    private void consume(String channel, ConsumerRecord<String, NotifyKafkaDTO> record, Acknowledgment ack) {
        try {
            String dedupKey = record.value().getDedupKey();
            if(!deduplicationService.isDuplicate(dedupKey)) {
                NotifyKafkaDTO dto = record.value();
                queueService.enqueue(channel, dto, factory.getProcessor(channel), ack);
                deduplicationService.markAsProcessed(dedupKey);
                log.debug("Сообщение {} добавлено в очередь {}", dto.getId(), channel);
            }
            else {
                ack.acknowledge();
                log.debug("Дубликат {} пропущен", record.value().getId());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.info("Ошибка в воркере");
        }
    }
}
