package com.notify.processor.service.processor;

import com.notify.dto.NotifyKafkaDTO;
import com.notify.processor.dto.EmailPayload;
import com.notify.processor.interfaces.NotificationProcessor;
import com.notify.processor.service.FeedbackSender;
import com.notify.processor.service.NotifyLogService;
import com.notify.processor.service.provider.email.EmailUnisenderProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailProcessor implements NotificationProcessor {
    private final ObjectMapper objectMapper;
    private final NotifyLogService logService;
    private final FeedbackSender fbSender;
    private final EmailUnisenderProvider emailProvider;
    @Override
    public void process(NotifyKafkaDTO dto) {
        EmailPayload email = objectMapper.readValue(dto.getPayload(), EmailPayload.class);
        logService.logSent(dto);
        log.info("Сообщение отправлено в email");
        if(emailProvider.isEnabled()) {
            if (emailProvider.send(email).getStatusCode() == HttpStatus.OK) {
                logService.logSucceed(dto);
                fbSender.sendFeedback(dto);
            }
        }
        else{
            log.info("Сообщение не отправлено в email, канал выключен");
            logService.logFailed(dto);
            fbSender.sendFeedback(dto);
        }
    }

    @Override
    public String getProcess() {
        return "email";
    }
}
