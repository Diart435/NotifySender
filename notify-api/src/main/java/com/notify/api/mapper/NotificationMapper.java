package com.notify.api.mapper;

import com.notify.api.entity.Notification;
import com.notify.api.entity.User;
import com.notify.dto.NotifyKafkaDTO;
import org.mapstruct.Mapper;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface NotificationMapper {
    NotifyKafkaDTO toKafkaDTO(Notification notification);
    default UUID userTOUUID(User user){
        return user.getId();
    }
}
