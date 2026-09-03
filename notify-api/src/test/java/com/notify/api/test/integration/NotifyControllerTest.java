package com.notify.api.test.integration;

import com.notify.api.controller.NotifyController;
import com.notify.api.dto.RequestEmailDTO;
import com.notify.api.dto.RequestPushDTO;
import com.notify.api.dto.RequestSmsDTO;
import com.notify.api.service.DeliveryService;
import com.notify.api.service.NotificationService;
import com.notify.api.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotifyController.class)
@Import({DeliveryService.class, BCryptPasswordEncoder.class})
@ActiveProfiles("test")
public class NotifyControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DeliveryService deliveryService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private NotificationService notificationService;

    @MockitoBean
    private UserService userService;

    private String apiKey;

    private RequestSmsDTO smsDTO;

    private RequestEmailDTO emailDTO;

    private RequestPushDTO pushDTO;

    @BeforeEach
    void setUp(){
        apiKey = "test-admin-key";
        smsDTO = new RequestSmsDTO();
        smsDTO.setContent("hi");
        smsDTO.setUserPhone("+79123456789");
        smsDTO.setTargetPhone("+79234567891");
        emailDTO = new RequestEmailDTO();
        emailDTO.setContent("hi");
        emailDTO.setLogin("ivan");
        emailDTO.setTitle("hello");
        emailDTO.setUserEmail("ivan@email.com");
        emailDTO.setTargetEmail("vasya@email.com");
        emailDTO.setTargetUser("vasya");
        pushDTO = new RequestPushDTO();
        pushDTO.setContent("hi");
        pushDTO.setTitle("hello");
        pushDTO.setPushToken("1");
    }

    @Test
    void smsShouldReturn201() throws Exception {
        mockMvc.perform(post("/api/notify/send/sms")
                .header("X-API-Key", apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(smsDTO)))
                .andExpect(status().isCreated());
    }

    @Test
    void smsShouldReturn400() throws Exception {
        smsDTO.setTargetPhone("1");
        mockMvc.perform(post("/api/notify/send/sms")
                        .header("X-API-Key", apiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(smsDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void smsShouldReturn401() throws Exception {
        mockMvc.perform(post("/api/notify/send/sms")
                        .header("X-API-Key", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(smsDTO)))
                .andExpect(status().isUnauthorized());
    }
    @Test
    void emailShouldReturn201() throws Exception {
        mockMvc.perform(post("/api/notify/send/email")
                        .header("X-API-Key", apiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emailDTO)))
                .andExpect(status().isCreated());
    }

    @Test
    void emailShouldReturn400() throws Exception {
        emailDTO.setTargetEmail("@gmail.com");
        mockMvc.perform(post("/api/notify/send/email")
                        .header("X-API-Key", apiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emailDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void emailShouldReturn401() throws Exception {
        mockMvc.perform(post("/api/notify/send/email")
                        .header("X-API-Key", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emailDTO)))
                .andExpect(status().isUnauthorized());
    }
    @Test
    void pushShouldReturn201() throws Exception {
        mockMvc.perform(post("/api/notify/send/push")
                        .header("X-API-Key", apiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pushDTO)))
                .andExpect(status().isCreated());
    }

    @Test
    void pushShouldReturn400() throws Exception {
        pushDTO.setPushToken("");
        mockMvc.perform(post("/api/notify/send/push")
                        .header("X-API-Key", apiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(smsDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void pushShouldReturn401() throws Exception {
        mockMvc.perform(post("/api/notify/send/push")
                        .header("X-API-Key", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(smsDTO)))
                .andExpect(status().isUnauthorized());
    }
}
