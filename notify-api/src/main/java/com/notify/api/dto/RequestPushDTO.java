package com.notify.api.dto;

import lombok.Data;

@Data
public class RequestPushDTO extends BaseNotificationDTO{
    private String pushToken;
    private String title;
}
