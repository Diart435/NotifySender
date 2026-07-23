package com.notify.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class RequestEmailDTO extends BaseNotificationDTO{
    @Pattern(regexp = "([a-z\\d]*[@]{1}[a-z]*\\.[a-z]*)?", message = "Некорректный формат электронной почты")
    private String userEmail;
    @Pattern(regexp = "([a-z\\d]*[@]{1}[a-z]*\\.[a-z]*)?", message = "Некорректный формат электронной почты")
    private String targetEmail;
    private String title;
}
