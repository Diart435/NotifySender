package com.notify.api.service;

import com.notify.dto.DedupKey;
import com.notify.dto.Message;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class KafkaProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaProducer(KafkaTemplate<String, Object> kafkaTemplate){
        this.kafkaTemplate = kafkaTemplate;
    }

    public <T extends DedupKey> void sendToKafka(Message<T> message){
        T data = message.data();

        String key = data.getPartitionKey();
        
        ProducerRecord<String, Object> record = new ProducerRecord<>(
                message.topic(),
                key,
                data
        );
        log.info("message sent with partition key: {}", data.getPartitionKey());
        kafkaTemplate.send(record);
    }
}
