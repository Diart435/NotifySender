package com.notify.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public abstract class BaseNotificationDTO {
    @NotBlank
    private String content;
}
