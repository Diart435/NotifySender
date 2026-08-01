package com.notify.processor.service.provider.push;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

@Component
@Slf4j
public class FirebaseInitializer {

    private boolean initialized = false;
    private String projectId;
    private GoogleCredentials credentials; // <-- сохраняем credentials

    @PostConstruct
    public void initialize() {
        try {
            ClassPathResource resource = new ClassPathResource("firebase-service-account.json");

            if (!resource.exists()) {
                log.warn("firebase-service-account.json не найден в resources. Push-уведомления отключены.");
                return;
            }

            // 1. Парсим projectId из JSON
            this.projectId = parseProjectIdFromJson(resource.getInputStream());

            // 2. Создаём credentials с правильными scope
            this.credentials = GoogleCredentials.fromStream(resource.getInputStream())
                    .createScoped(List.of("https://www.googleapis.com/auth/firebase.messaging"));

            // 3. Инициализируем FirebaseApp
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(this.credentials)
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                initialized = true;
                log.info("Firebase успешно инициализирован. projectId: {}", projectId);
            } else {
                initialized = true;
                log.info("Firebase уже был инициализирован. projectId: {}", projectId);
            }

        } catch (IOException e) {
            log.error("Ошибка инициализации Firebase: {}", e.getMessage(), e);
            initialized = false;
        }
    }

    private String parseProjectIdFromJson(java.io.InputStream inputStream) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(inputStream);
        return root.get("project_id").asText();
    }

    public boolean isInitialized() {
        return initialized;
    }

    public String getProjectId() {
        return projectId;
    }

    public GoogleCredentials getCredentials() {
        return credentials;
    }
}