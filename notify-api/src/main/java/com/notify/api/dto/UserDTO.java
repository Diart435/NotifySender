package com.notify.api.dto;

import lombok.Data;

@Data
public class UserDTO{
    private String login;

    private String phone;

    private String email;

    private String pushToken;

    private String content;

    private String recipient;

    private String channel;
}
