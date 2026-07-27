package com.notify.processor.repository;

import com.notify.processor.entity.DeliveryLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DeliveryLogRepository extends JpaRepository<DeliveryLog, UUID> {
    Optional<DeliveryLog> findByNotificationId(UUID notificationId);
}
