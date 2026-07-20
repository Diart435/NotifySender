package com.notify.api.mapper;

import com.notify.api.entity.Notification;
import com.notify.dto.NotifyKafkaDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NotificationMapper {
    NotifyKafkaDTO toKafkaDTO(Notification notification);
}
