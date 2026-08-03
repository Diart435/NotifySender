package com.notify.api.controller;

import com.notify.api.dto.RequestRegister;
import com.notify.api.dto.ResponseApiKey;
import com.notify.api.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/notify/user")
@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @Operation(
            summary = "Create a user",
            description = "Returns a api key"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/add")
    public ResponseEntity<ResponseApiKey> addUser(@Valid @RequestBody RequestRegister register){
        ResponseApiKey response = new ResponseApiKey();
        response.setApiKey(userService.createUserAndGetApiKey(register.getUsername(), register.getEmail(), register.getPassword()));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
