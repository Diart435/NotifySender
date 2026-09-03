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
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "409", description = "Conflict")
    })
    @PostMapping("/add")
    public ResponseEntity<ResponseApiKey> addUser(@Valid @RequestBody RequestRegister register){
        ResponseApiKey response = new ResponseApiKey();
        String apiKey = userService.createUserAndGetApiKey(register.getUsername(), register.getEmail(), register.getPassword());
        if(apiKey != null) {
            response.setApiKey(apiKey);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }
        else{
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }
}
