package com.notify.processor.service;

import com.notify.dto.NotifyKafkaDTO;
import com.notify.processor.entity.DeliveryLog;
import com.notify.dto.NotificationStatus;
import com.notify.processor.mapper.DeliveryMapper;
import com.notify.processor.repository.DeliveryLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotifyLogService {
    private final DeliveryLogRepository deliveryLogRepository;
    private final DeliveryMapper deliveryMapper;
    private static final AtomicInteger counter = new AtomicInteger(0);

    @Transactional
    public void logSave(NotifyKafkaDTO dto){
        counter.incrementAndGet();
        DeliveryLog dLog = deliveryMapper.toLog(dto);
        dLog.setResult(NotificationStatus.PROCESSING);
        dto.setStatus(NotificationStatus.PROCESSING.toString());
        deliveryLogRepository.save(dLog);
        log.info("Сообщение принято в обработку: {}, номер: {}", dto.getId(), counter);
    }

    @Transactional
    public void logSent(NotifyKafkaDTO dto){
        Optional<DeliveryLog> existing = deliveryLogRepository.findByNotificationId(dto.getId());
        if(existing.isPresent()){
            DeliveryLog dLog = existing.get();
            dLog.setResult(NotificationStatus.SENT);
            dLog.setUpdatedAt(LocalDateTime.now());
        }
        dto.setStatus(NotificationStatus.SENT.toString());
        dto.setUpdatedAt(LocalDateTime.now());
    }

    @Transactional
    public void logRetry(NotifyKafkaDTO dto, int retryCount){
        Optional<DeliveryLog> existing = deliveryLogRepository.findByNotificationId(dto.getId());
        if(existing.isPresent()){
            DeliveryLog dLog = existing.get();
            dLog.setResult(NotificationStatus.RETRY);
            dLog.setAttemptNumber(retryCount);
            dLog.setUpdatedAt(LocalDateTime.now());
        }
        dto.setStatus(NotificationStatus.RETRY.toString());
        dto.setRetryCount(retryCount);
        dto.setUpdatedAt(LocalDateTime.now());
    }

    @Transactional
    public void logFailed(NotifyKafkaDTO dto){
        Optional<DeliveryLog> existing = deliveryLogRepository.findByNotificationId(dto.getId());
        if(existing.isPresent()){
            DeliveryLog dLog = existing.get();
            dLog.setResult(NotificationStatus.FAILED);
            dLog.setUpdatedAt(LocalDateTime.now());
        }
        dto.setStatus(NotificationStatus.FAILED.toString());
        dto.setUpdatedAt(LocalDateTime.now());
    }

    @Transactional
    public void logSucceed(NotifyKafkaDTO dto){
        Optional<DeliveryLog> existing = deliveryLogRepository.findByNotificationId(dto.getId());
        if(existing.isPresent()){
            DeliveryLog dLog = existing.get();
            dLog.setResult(NotificationStatus.SUCCESS);
            dLog.setUpdatedAt(LocalDateTime.now());
        }
        dto.setStatus(NotificationStatus.SUCCESS.toString());
        dto.setUpdatedAt(LocalDateTime.now());
    }
}
