package com.notify.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RequestPushDTO extends BaseNotificationDTO{
    @NotBlank
    private String pushToken;
    @NotBlank
    private String title;
}
