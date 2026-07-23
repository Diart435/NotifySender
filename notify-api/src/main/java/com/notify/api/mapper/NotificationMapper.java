package com.notify.api.mapper;

import com.notify.api.dto.BaseNotificationDTO;
import com.notify.api.dto.RequestEmailDTO;
import com.notify.api.dto.RequestPushDTO;
import com.notify.api.dto.RequestSmsDTO;
import com.notify.api.entity.Notification;
import com.notify.dto.NotifyKafkaDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface NotificationMapper {
    NotifyKafkaDTO toKafkaDTO(Notification notification);
}
