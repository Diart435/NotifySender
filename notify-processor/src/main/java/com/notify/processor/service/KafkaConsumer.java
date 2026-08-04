package com.notify.processor.service;

import com.notify.dto.NotifyKafkaDTO;
import com.notify.processor.interfaces.NotificationProcessor;
import com.notify.processor.service.processor.ProcessorFactory;
import com.notify.processor.service.queue.QueueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import org.springframework.kafka.support.Acknowledgment;
@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaConsumer {
    private final NotifyLogService logService;
    private final ObjectMapper objectMapper;
    private final DeduplicationService deduplicationService;
    private final ProcessorFactory factory;
    private final QueueService queue;


    @KafkaListener(topics = "sms", groupId = "sms-group", containerFactory = "containerFactory")
    public void consumeSmsChannel(ConsumerRecord<String, NotifyKafkaDTO> consumerRecord, Acknowledgment ack){
        consume("sms", consumerRecord, ack);

    }
    @KafkaListener(topics = "push", groupId = "push-group", containerFactory = "containerFactory")
    public void consumePushChannel(ConsumerRecord<String, NotifyKafkaDTO> consumerRecord, Acknowledgment ack){
        consume("push", consumerRecord, ack);
    }
    @KafkaListener(topics = "email", groupId = "email-group", containerFactory = "containerFactory")
    public void consumeEmailChannel(ConsumerRecord<String, NotifyKafkaDTO> consumerRecord, Acknowledgment ack){
        consume("email", consumerRecord, ack);
    }

    private void consume(String channel, ConsumerRecord<String, NotifyKafkaDTO> consumerRecord, Acknowledgment ack){
        try {
            String dedupKey = consumerRecord.value().getDedupKey();
            if (!deduplicationService.isDuplicate(dedupKey)) {
                NotificationProcessor processor = factory.getProcessor(channel);
                NotifyKafkaDTO dto = consumerRecord.value();
                log.info("Пришло сообщение: {}", consumerRecord.value().getId());
                logService.logSave(dto);
                queue.enqueue(channel, dto, processor);
            }
            deduplicationService.markAsProcessed(dedupKey);
            ack.acknowledge();
        }
        catch (JacksonException e){
            log.error("Ошибка парсинга уведомления после передачи kafka");
        }
        catch (InterruptedException e){
            log.error("Ошибка потока");
        }
        catch (Exception e) {
            log.error("Ошибка обработки сообщения: {}", consumerRecord.value().getId(), e);
        }
    }
}
