package com.notify.processor.service.provider.email;

import com.notify.processor.dto.EmailPayload;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@Slf4j
public class EmailUnisenderProvider extends BaseEmailProvider{

    private final RestClient restClient;
    @Value("${email.unisender.api.key}")
    private String apiKey;
    private static boolean enabled = false;
    public EmailUnisenderProvider(@Qualifier("unisenderEmailRestClient") RestClient restClient){
        this.restClient = restClient;
    }
    @PostConstruct
    public void init() {
        enabled = this.apiKey != null && !this.apiKey.isBlank();
        if (enabled) {
            log.info("Email-канал включен");
        } else {
            log.warn("Email-канал отключен: API-ключ не настроен");
        }
    }
    @Override
    public ResponseEntity<String> send(EmailPayload emailPayload) {
        if(enabled) {
            String uriCreate = "sendEmail?format=json&api_key=" + this.apiKey + "&email=" + emailPayload.getTargetUser() + " <"
                    + emailPayload.getTargetEmail() + ">&sender_name=" + emailPayload.getLogin() +
                    "&sender_email=" + emailPayload.getUserEmail() +
                    "&subject=" + emailPayload.getTitle() +
                    "&body=" + emailPayload.getContent() + "&list_id=" + "0";
            ResponseEntity<String> entitySend = this.restClient.get()
                    .uri(uriCreate)
                    .retrieve()
                    .toEntity(String.class);
            return entitySend;
        }
        return null;
    }
    public boolean isEnabled(){
        return enabled;
    }
}
