package com.notify.processor.service.provider.email;

import com.notify.processor.dto.EmailPayload;
import org.springframework.http.ResponseEntity;

public abstract class BaseEmailProvider {
    public abstract ResponseEntity<String> send(EmailPayload email);
}
