package com.notify.processor.service.queue;

import com.notify.dto.NotifyKafkaDTO;
import com.notify.processor.interfaces.NotificationProcessor;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class QueueItem {
    private final NotifyKafkaDTO dto;
    private final NotificationProcessor processor;
}
