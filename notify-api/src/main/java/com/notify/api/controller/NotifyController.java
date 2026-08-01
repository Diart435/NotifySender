package com.notify.api.controller;

import com.notify.api.dto.RequestEmailDTO;
import com.notify.api.dto.RequestPushDTO;
import com.notify.api.dto.RequestSmsDTO;
import com.notify.api.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/notify/send")
@RequiredArgsConstructor
public class NotifyController {
    private final NotificationService notificationService;

    @Operation(
            summary = "Send a email notification to external APIs",
            description = "Returns a void"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Notification sent"),
            @ApiResponse(responseCode = "400", description = "Bad request")
    })
    @PostMapping("/email")
    public ResponseEntity<Void> sendNotify(@Valid @RequestBody RequestEmailDTO emailDTO){
        notificationService.create(emailDTO, UUID.randomUUID().toString());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(
            summary = "Send a sms notification to external APIs",
            description = "Returns a void"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Notification sent"),
            @ApiResponse(responseCode = "400", description = "Bad request")
    })
    @PostMapping("/sms")
    public ResponseEntity<Void> sendNotify(@Valid @RequestBody RequestSmsDTO smsDTO){
        notificationService.create(smsDTO, UUID.randomUUID().toString());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(
            summary = "Send a push notification to external APIs",
            description = "Returns a void"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Notification sent"),
            @ApiResponse(responseCode = "400", description = "Bad request")
    })
    @PostMapping("/push")
    public ResponseEntity<Void> sendNotify(@Valid @RequestBody RequestPushDTO pushDTO){
        notificationService.create(pushDTO, UUID.randomUUID().toString());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
