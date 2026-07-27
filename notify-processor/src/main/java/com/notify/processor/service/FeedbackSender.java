package com.notify.processor.service;

import com.notify.dto.Message;
import com.notify.dto.NotifyKafkaDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FeedbackSender {
    private final KafkaProducer sender;

    public void sendFeedback(NotifyKafkaDTO dto){
        sender.sendToKafka(new Message<>("notify-feedback", dto));
    }

    public void sendToDLQ(NotifyKafkaDTO dto){
        String dlqTopic = "dlq-" + dto.getChannel();
        sender.sendToKafka(new Message<>(dlqTopic, dto));
    }
}
