package com.notify.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class RequestEmailDTO extends BaseNotificationDTO{
    @Email
    private String userEmail;
    @Email
    private String targetEmail;
    private String title;
}
