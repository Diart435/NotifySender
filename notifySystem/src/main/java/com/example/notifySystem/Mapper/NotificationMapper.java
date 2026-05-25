package com.example.notifySystem.Mapper;

import com.example.notifySystem.DTO.NotifyKafkaDTO;
import com.example.notifySystem.Entity.Notification;
import com.example.notifySystem.Entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface NotificationMapper {
    NotifyKafkaDTO toKafkaDTO(Notification notification);
    default UUID userTOUUID(User user){
        return user.getId();
    }
}
