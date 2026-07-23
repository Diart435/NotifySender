package com.notify.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
public class RequestPushDTO extends BaseNotificationDTO{
    private String pushToken;
    private String title;
}
