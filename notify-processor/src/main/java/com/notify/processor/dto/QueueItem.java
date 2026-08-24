package com.notify.processor.dto;

import com.notify.dto.NotifyKafkaDTO;
import com.notify.processor.interfaces.NotificationProcessor;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.kafka.support.Acknowledgment;

@Data
@AllArgsConstructor
public class QueueItem {
    private NotifyKafkaDTO dto;
    private NotificationProcessor processor;
    private Acknowledgment ack;
}
