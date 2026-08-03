package com.notify.processor.response.push;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class FirebaseMessage {
    @JsonProperty(value = "token")
    private String token;
    @JsonProperty(value = "notification")
    private FirebaseNotification notification;
}