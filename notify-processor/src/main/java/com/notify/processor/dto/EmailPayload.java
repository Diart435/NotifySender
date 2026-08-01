package com.notify.processor.dto;

import lombok.Data;

@Data
public class EmailPayload {
    private String login;
    private String targetUser;
    private String userEmail;
    private String targetEmail;
    private String title;
    private String content;
}
