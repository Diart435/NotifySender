package com.notify.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RequestRegister {
    @NotBlank(message = "Требуется логин")
    private String username;

    @NotBlank(message = "Требуется адрес электронной почты")
    @Email
    private String email;

    @NotBlank(message = "Требуется пароль")
    @Size(min = 8, max = 100)
    private String password;
}
