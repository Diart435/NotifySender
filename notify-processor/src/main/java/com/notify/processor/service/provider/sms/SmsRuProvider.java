package com.notify.processor.service.provider.sms;

import com.notify.processor.dto.SmsPayload;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@Slf4j
public class SmsRuProvider extends BaseSmsProvider{
    private final RestClient restClient;

    @Value("${smsru.provider.api.key}")
    private String apiKey;
    private static boolean enabled = false;
    public SmsRuProvider(@Qualifier("smsRuRestClient") RestClient restClient){
        this.restClient = restClient;
    }
    @PostConstruct
    public void init() {
        enabled = this.apiKey != null && !this.apiKey.isBlank();
        if (enabled) {
            log.info("SMS-канал включен");
        } else {
            log.warn("SMS-канал отключен: API-ключ не настроен");
        }
    }

    @Override
    public ResponseEntity<String> send(SmsPayload sms) {
        if(enabled) {
            String uri = "sms/send?api_id=" + this.apiKey + "&to=" + sms.getTargetPhone() + "&msg=" + sms.getContent() + "&json=1";
            ResponseEntity<String> entity = restClient.post()
                    .uri(uri)
                    .retrieve()
                    .toEntity(String.class);
            return entity;
        }
        return null;
    }

    public ResponseEntity<String> checkBalance(){
        if(enabled) {
            String uri = "my/balance?api_id=" + this.apiKey + "&json=1";
            ResponseEntity<String> entity = restClient.get()
                    .uri(uri)
                    .retrieve()
                    .toEntity(String.class);
            return entity;
        }
        return null;
    }

    public boolean isEnabled(){
        return enabled;
    }
}
