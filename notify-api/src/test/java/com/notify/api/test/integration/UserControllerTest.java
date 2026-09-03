package com.notify.api.test.integration;

import com.notify.api.controller.UserController;
import com.notify.api.dto.RequestRegister;
import com.notify.api.entity.User;
import com.notify.api.repository.UserRepository;
import com.notify.api.service.UserService;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc
@Import({UserService.class, BCryptPasswordEncoder.class})
@ActiveProfiles("test")
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserService userService;

    @MockitoBean
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder encoder;

    @MockitoBean
    private RedisTemplate<String, String> redisTemplate;

    private RequestRegister register;

    private String apiKey;

    private String adminApiKey;

    private User user;

    @BeforeEach
    void setUp(){
        register = new RequestRegister();
        register.setEmail("ivan@gmail.com");
        register.setUsername("ivan");
        register.setPassword("12345678");

        adminApiKey = "test-admin-key";
        apiKey = UUID.randomUUID().toString();
        String hashed = encoder.encode(apiKey);
        user = new User();
        user.setEmail(register.getEmail());
        user.setUsername(register.getUsername());
        user.setPasswordHash(encoder.encode(register.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        user.setApiKey(hashed);
        user.setApiKeyLookup(DigestUtils.sha256Hex(apiKey));
    }

    @Test
    void shouldReturn201() throws Exception {
        when(userRepository.findByUsername(register.getUsername())).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/notify/user/add")
                        .header("X-API-Key", adminApiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.apiKey").exists());
    }

    @Test
    void shouldReturn400() throws Exception{
        register.setPassword("1");
        mockMvc.perform(post("/api/notify/user/add")
                        .header("X-API-Key", adminApiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn401() throws Exception{
        mockMvc.perform(post("/api/notify/user/add")
                        .header("X-API-Key", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn409() throws Exception{
        when(userRepository.findByUsername(register.getUsername())).thenReturn(Optional.of(user));

        mockMvc.perform(post("/api/notify/user/add")
                .header("X-API-Key", adminApiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isConflict());
    }
}
