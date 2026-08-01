package com.notify.processor.service.provider.push;

import com.notify.processor.dto.PushPayload;
import com.notify.processor.mapper.FirebaseMapper;
import com.notify.processor.response.push.FirebaseRequest;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.DependsOn;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@DependsOn("firebaseInitializer")
@Slf4j
public class FirebaseProvider {
    private final RestClient restClient;
    private String projectKey;
    private final FirebaseTokenProvider tokenProvider;
    private final FirebaseMapper firebaseMapper;
    private final FirebaseInitializer initializer;
    private static boolean enabled = false;

    public FirebaseProvider(FirebaseTokenProvider firebaseTokenProvider, FirebaseInitializer firebaseInitializer, FirebaseMapper firebaseMapper) {
        this.restClient = RestClient.builder()
                .baseUrl("https://fcm.googleapis.com/v1/projects/")
                .requestFactory(new SimpleClientHttpRequestFactory())
                .build();
        this.firebaseMapper = firebaseMapper;
        this.initializer = firebaseInitializer;
        this.tokenProvider = firebaseTokenProvider;
    }
    @PostConstruct
    public void init(){
        if(this.initializer.isInitialized()) {
            this.projectKey = this.initializer.getProjectId();
            enabled = true;
        }
        else {
            log.error("Push-канал недоступен.");
        }
    }

    public ResponseEntity<String> send(PushPayload payload){
        if(enabled) {
            String oAuthToken = tokenProvider.getAccessToken();
            String uri = projectKey + "/messages:send";
            FirebaseRequest request = firebaseMapper.toRequest(payload);
            ResponseEntity<String> ent = this.restClient.post()
                    .uri(uri)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + oAuthToken)
                    .body(request)
                    .retrieve()
                    .toEntity(String.class);
            return ent;
        }
        return null;
    }

    public boolean isEnabled(){
        return enabled;
    }
}
