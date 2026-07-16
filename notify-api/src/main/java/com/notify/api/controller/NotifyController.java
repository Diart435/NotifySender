package com.notify.api.controller;

import com.notify.api.dto.UserDTO;
import com.notify.api.entity.User;
import com.notify.api.service.NotificationService;
import com.notify.api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notify")
@RequiredArgsConstructor
public class NotifyController {
    private final UserService userService;
    private final NotificationService notificationService;

    @PostMapping("/send")
    public ResponseEntity<UserDTO> sendNotify(@RequestBody UserDTO userDTO){
        User user = userService.createUser(userDTO.getPhone(),userDTO.getEmail(),userDTO.getPushToken());
        notificationService.createNotification(userDTO.getChannel(), userDTO.getRecipient(), userDTO.getContent(), user);
        return ResponseEntity.status(HttpStatus.CREATED).body(userDTO);
    }
}
