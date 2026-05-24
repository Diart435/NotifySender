package com.example.notifySystem.JSON;

import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import javax.sql.rowset.serial.SerialException;
import java.io.IOException;
import java.io.OutputStream;


public class KafkaJsonSerializer<T> implements Serializer<T> {
    private final ObjectMapper objectMapper;

    public KafkaJsonSerializer(ObjectMapper objectMapper){
        this.objectMapper = objectMapper;
    }


    @Override
    public byte[] serialize(String topic, T data) {
        if(data == null) return null;

        try{
            return objectMapper.writeValueAsBytes(data);
        }
        catch (JacksonException e){
            throw new SerializationException("JSON serialization failed for topic: " + topic, e);
        }
    }
}
