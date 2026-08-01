package com.notify.processor.service.provider.sms;

import com.notify.processor.dto.SmsPayload;
import org.springframework.http.ResponseEntity;

public abstract class BaseSmsProvider {
    public abstract ResponseEntity<String> send(SmsPayload sms);

    public abstract ResponseEntity<String> checkBalance();
}
