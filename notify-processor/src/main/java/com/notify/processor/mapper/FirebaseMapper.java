package com.notify.processor.mapper;

import com.notify.processor.dto.PushPayload;
import com.notify.processor.response.push.FirebaseMessage;
import com.notify.processor.response.push.FirebaseNotification;
import com.notify.processor.response.push.FirebaseRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FirebaseMapper {
    @Mapping(source = "payload", target = "message")
    FirebaseRequest toRequest(PushPayload payload);

    default FirebaseMessage toMessage(PushPayload payload){
        FirebaseNotification notification = new FirebaseNotification();
        notification.setBody(payload.getContent());
        notification.setTitle(payload.getTitle());
        FirebaseMessage msg = new FirebaseMessage();
        msg.setToken(payload.getPushToken());
        msg.setNotification(notification);
        return msg;
    }
}
