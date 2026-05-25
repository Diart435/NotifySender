package com.example.notifySystem.DTO;

import com.example.notifySystem.Entity.DedupKey;
import lombok.Data;

import java.util.UUID;

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
