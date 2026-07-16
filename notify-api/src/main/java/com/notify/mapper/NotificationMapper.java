package com.notify.mapper;

import com.notify.dto.NotifyKafkaDTO;
import com.notify.entity.Notification;
import com.notify.entity.User;
import org.mapstruct.Mapper;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface NotificationMapper {
    NotifyKafkaDTO toKafkaDTO(Notification notification);
    default UUID userTOUUID(User user){
        return user.getId();
    }
}
