package com.notify.api.controller;

import com.notify.api.dto.RequestEmailDTO;
import com.notify.api.dto.RequestPushDTO;
import com.notify.api.dto.RequestSmsDTO;
import com.notify.api.service.NotificationCreateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/notify/send")
@RequiredArgsConstructor
public class NotifyController {
    private final NotificationCreateService notificationService;

    @PostMapping("/email")
    public ResponseEntity<RequestEmailDTO> sendNotify(@Valid @RequestBody RequestEmailDTO emailDTO){
        notificationService.create(emailDTO, UUID.randomUUID().toString());
        return ResponseEntity.status(HttpStatus.CREATED).body(emailDTO);
    }

    @PostMapping("/sms")
    public ResponseEntity<RequestSmsDTO> sendNotify(@Valid @RequestBody RequestSmsDTO smsDTO){
        notificationService.create(smsDTO, UUID.randomUUID().toString());
        return ResponseEntity.status(HttpStatus.CREATED).body(smsDTO);
    }

    @PostMapping("/push")
    public ResponseEntity<RequestPushDTO> sendNotify(@Valid @RequestBody RequestPushDTO pushDTO){
        notificationService.create(pushDTO, UUID.randomUUID().toString());
        return ResponseEntity.status(HttpStatus.CREATED).body(pushDTO);
    }
}
