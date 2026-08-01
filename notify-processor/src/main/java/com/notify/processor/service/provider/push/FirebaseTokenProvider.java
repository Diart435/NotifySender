package com.notify.processor.service.provider.push;

import com.google.auth.oauth2.GoogleCredentials;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class FirebaseTokenProvider {

    private final FirebaseInitializer initializer;

    public FirebaseTokenProvider(FirebaseInitializer initializer) {
        this.initializer = initializer;
    }

    public String getAccessToken() {
        if (!initializer.isInitialized()) {
            log.error("Firebase не инициализирован. Токен не получен.");
            return null;
        }

        try {
            GoogleCredentials credentials = initializer.getCredentials();
            if (credentials == null) {
                log.error("GoogleCredentials отсутствуют.");
                return null;
            }

            credentials.refreshIfExpired();
            return credentials.getAccessToken().getTokenValue();

        } catch (IOException e) {
            log.error("Ошибка получения OAuth токена: {}", e.getMessage(), e);
            return null;
        }
    }
}