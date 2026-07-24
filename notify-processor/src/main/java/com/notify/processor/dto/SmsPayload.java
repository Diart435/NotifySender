package com.notify.processor.dto;

import lombok.Data;

@Data
public class SmsPayload {
    private String content;
    private String userPhone;
    private String targetPhone;
    private String senderId;
}
