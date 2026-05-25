package com.example.notifySystem.Service;

import com.example.notifySystem.DTO.Message;
import com.example.notifySystem.Entity.DedupKey;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.protocol.types.Field;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class KafkaProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaProducer(KafkaTemplate<String, Object> kafkaTemplate){
        this.kafkaTemplate = kafkaTemplate;
    }

    public <T extends DedupKey> void sendToKafka(Message<T> message){
        T data = message.data();

        String key = data.getDedupKey();

        ProducerRecord<String, Object> record = new ProducerRecord<>(
                message.topic(),
                key,
                data
        );

        kafkaTemplate.send(record);
    }
}
