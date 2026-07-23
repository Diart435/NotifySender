package com.notify.api.interfaces;

import com.notify.api.dto.BaseNotificationDTO;
import com.notify.api.entity.Notification;

public interface NotificationCreateStrategy<T extends BaseNotificationDTO> {
    public Notification create(T dto, String userId);
}
