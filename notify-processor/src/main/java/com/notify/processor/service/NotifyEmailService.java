package com.notify.processor.service;

import com.notify.dto.NotifyKafkaDTO;
import com.notify.processor.entity.DeliveryLog;
import com.notify.processor.enums.NotificationStatus;
import com.notify.processor.mapper.DeliveryMapper;
import com.notify.processor.repository.DeliveryLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotifyEmailService {
    private final DeliveryLogRepository deliveryLogRepository;
    private final DeliveryMapper deliveryMapper;
    private static int counter = 0;
    @Transactional
    public void logSave(NotifyKafkaDTO notifyKafkaDTO){
        counter++;
        DeliveryLog dLog = deliveryMapper.toLog(notifyKafkaDTO);
        dLog.setResult(NotificationStatus.PROCESSING);
        deliveryLogRepository.save(dLog);
        log.info("total messages: {}", counter);
    }
}
