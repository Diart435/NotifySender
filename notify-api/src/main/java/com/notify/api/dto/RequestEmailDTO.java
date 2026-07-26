package com.notify.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RequestEmailDTO extends BaseNotificationDTO{
    @Email
    @NotNull
    private String userEmail;
    @Email
    @NotNull
    private String targetEmail;
    @NotBlank
    private String title;
}
