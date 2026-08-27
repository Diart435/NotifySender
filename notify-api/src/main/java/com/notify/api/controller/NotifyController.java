package com.notify.api.controller;

import com.notify.api.dto.RequestEmailDTO;
import com.notify.api.dto.RequestPushDTO;
import com.notify.api.dto.RequestSmsDTO;
import com.notify.api.service.DeliveryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notify/send")
@RequiredArgsConstructor
public class NotifyController {
    private final DeliveryService deliveryService;

    @Operation(
            summary = "Send a email notification to external APIs",
            description = "Returns a void"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Notification sent"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/email")
    public ResponseEntity<Void> sendNotify(@RequestHeader("X-API-Key") String apiKey, @Valid @RequestBody RequestEmailDTO emailDTO){
        deliveryService.delivery(emailDTO, apiKey);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(
            summary = "Send a sms notification to external APIs",
            description = "Returns a void"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Notification sent"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/sms")
    public ResponseEntity<Void> sendNotify(@RequestHeader("X-API-Key") String apiKey, @Valid @RequestBody RequestSmsDTO smsDTO){
        deliveryService.delivery(smsDTO, apiKey);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(
            summary = "Send a push notification to external APIs",
            description = "Returns a void"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Notification sent"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/push")
    public ResponseEntity<Void> sendNotify(@RequestHeader("X-API-Key") String apiKey, @Valid @RequestBody RequestPushDTO pushDTO){
        deliveryService.delivery(pushDTO, apiKey);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
