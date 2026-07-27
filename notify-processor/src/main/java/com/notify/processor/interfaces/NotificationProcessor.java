package com.notify.processor.interfaces;

import com.notify.dto.NotifyKafkaDTO;

public interface NotificationProcessor {
    void process(NotifyKafkaDTO notifyKafkaDTO);
    String getProcess();
}
