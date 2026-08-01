package com.notify.processor.response.push;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class FirebaseNotification {
    @JsonProperty(value = "body")
    private String body;
    @JsonProperty(value = "title")
    private String title;
}
