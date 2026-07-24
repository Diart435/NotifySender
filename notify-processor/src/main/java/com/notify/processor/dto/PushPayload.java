package com.notify.processor.dto;

import lombok.Data;

@Data
public class PushPayload {
    private String pushToken;
    private String title;
    private String content;
}
