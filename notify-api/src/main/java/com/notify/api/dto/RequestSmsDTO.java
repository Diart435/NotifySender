package com.notify.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class RequestSmsDTO extends BaseNotificationDTO{
    @Pattern(regexp = "([+]{0,1}[7-8]\\d{10})?", message = "Некорректный формат телефона")
    private String userPhone;
    @Pattern(regexp = "([+]{0,1}[7-8]\\d{10})?", message = "Некорректный формат телефона")
    private String targetPhone;
    @NotBlank
    private String senderId;
}
