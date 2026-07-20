package com.notify.api.mapper;

import com.notify.api.entity.Notification;
import com.notify.api.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", source = "id")
    @Mapping(target = "retryCount", constant = "0")
    @Mapping(target = "updatedAt", ignore = true)
    Notification toNotification(User user);
}
