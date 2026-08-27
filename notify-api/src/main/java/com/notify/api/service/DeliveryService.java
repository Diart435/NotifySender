package com.notify.api.service;

import com.notify.api.dto.BaseNotificationDTO;
import com.notify.api.entity.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryService {
    private final NotificationService notificationService;
    private final UserService userService;

    public void delivery(BaseNotificationDTO request, String apiKey){
        try {
            Notification notification = notificationService.createNotification(request, userService.getUserByApiKey(apiKey).getId().toString());
            notificationService.saveNotification(notification);
            notificationService.sendMessage(notification);
        }
        catch (Exception e){
            log.info("Сбой отправки, брокер недоступен...");
        }
    }
}
