package com.notify.mapper;

import com.notify.entity.Notification;
import com.notify.entity.User;
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
