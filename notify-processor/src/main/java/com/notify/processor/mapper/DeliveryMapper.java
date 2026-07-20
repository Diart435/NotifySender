package com.notify.processor.mapper;

import com.notify.dto.NotifyKafkaDTO;
import com.notify.processor.entity.DeliveryLog;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DeliveryMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "notificationId", source = "id")
    @Mapping(target = "attemptNumber", constant = "0")
    DeliveryLog toLog(NotifyKafkaDTO notifyKafkaDTO);

}
