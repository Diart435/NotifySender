package com.example.notifySystem.Config;

import com.example.notifySystem.JSON.KafkaJsonSerializer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.UUIDSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import tools.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.UUID;

@Configuration
public class KafkaConfig {

    @Bean
    public ProducerFactory<UUID, Object> producerFactory(ObjectMapper objectMapper){
        HashMap<String, Object> conf = new HashMap<>();
        conf.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        conf.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, UUIDSerializer.class);

        DefaultKafkaProducerFactory<UUID, Object> factory = new DefaultKafkaProducerFactory<>(conf);

        factory.setValueSerializer(new KafkaJsonSerializer<>(objectMapper));
        return factory;
    }

    @Bean
    public KafkaTemplate<UUID, Object> kafkaTemplate(ProducerFactory<UUID, Object> producerFactory){
        return new KafkaTemplate<>(producerFactory);
    }
}
