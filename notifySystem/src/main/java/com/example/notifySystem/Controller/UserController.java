package com.example.notifySystem.Controller;

import com.example.notifySystem.DTO.UserDTO;
import com.example.notifySystem.Entity.User;
import com.example.notifySystem.Service.UserService;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notify")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final

    @PostMapping("/send")
    public ResponseEntity<UserDTO> sendNotify(@RequestBody UserDTO userDTO){
        User user = userService.createUser(userDTO.getPhone(),userDTO.getEmail(),userDTO.getPushToken());

        return ResponseEntity.status(HttpStatus.CREATED).body(userDTO);
    }
}
