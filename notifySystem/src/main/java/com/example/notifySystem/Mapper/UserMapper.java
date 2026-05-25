package com.example.notifySystem.Mapper;

import com.example.notifySystem.DTO.UserDTO;
import com.example.notifySystem.Entity.Notification;
import com.example.notifySystem.Entity.User;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", source = "user")
    @Mapping(target = "retryCount", constant = "0")
    @Mapping(target = "updatedAt", ignore = true)
    Notification toNotification(User user);
}
