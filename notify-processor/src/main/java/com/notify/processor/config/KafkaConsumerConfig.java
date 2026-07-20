package com.notify.processor.config;

import com.notify.dto.NotifyKafkaDTO;
import com.notify.processor.JSON.KafkaJsonDeserializer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import tools.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
public class KafkaConsumerConfig {
    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Bean
    public ConsumerFactory<String, NotifyKafkaDTO> consumerFactory(ObjectMapper objectMapper){
        Map<String, Object> confProperties = new HashMap<>();
        confProperties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        confProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        DefaultKafkaConsumerFactory<String, NotifyKafkaDTO> factory = new DefaultKafkaConsumerFactory<>(confProperties);
        factory.setValueDeserializer(new KafkaJsonDeserializer<>(objectMapper, NotifyKafkaDTO.class));

        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, NotifyKafkaDTO> containerFactory(ConsumerFactory<String, NotifyKafkaDTO> consumerFactory){
        var containerFactory = new ConcurrentKafkaListenerContainerFactory<String, NotifyKafkaDTO>();
        containerFactory.setConcurrency(1);
        containerFactory.setConsumerFactory(consumerFactory);
        return containerFactory;
    }
}
