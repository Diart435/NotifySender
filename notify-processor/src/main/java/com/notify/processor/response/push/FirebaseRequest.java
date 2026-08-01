package com.notify.processor.response.push;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class FirebaseRequest {
    @JsonProperty(value = "message")
    private FirebaseMessage message;
}
