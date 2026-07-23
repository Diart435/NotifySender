package com.notify.api.controller;

import com.notify.api.dto.RequestEmailDTO;
import com.notify.api.dto.RequestPushDTO;
import com.notify.api.dto.RequestSmsDTO;
import com.notify.api.service.NotificationCreateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notify/send")
@RequiredArgsConstructor
public class NotifyController {
    private final NotificationCreateService notificationService;

    @PostMapping("/email")
    public ResponseEntity<RequestEmailDTO> sendNotify(@RequestBody RequestEmailDTO emailDTO){
        notificationService.create(emailDTO, "1");
        return ResponseEntity.status(HttpStatus.CREATED).body(emailDTO);
    }

    @PostMapping("/sms")
    public ResponseEntity<RequestSmsDTO> sendNotify(@RequestBody RequestSmsDTO smsDTO){
        notificationService.create(smsDTO, "2");
        return ResponseEntity.status(HttpStatus.CREATED).body(smsDTO);
    }

    @PostMapping("/push")
    public ResponseEntity<RequestPushDTO> sendNotify(@RequestBody RequestPushDTO pushDTO){
        notificationService.create(pushDTO, "3");
        return ResponseEntity.status(HttpStatus.CREATED).body(pushDTO);
    }
}
