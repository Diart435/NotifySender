package com.notify.processor.service.processor;

import com.notify.dto.NotifyKafkaDTO;
import com.notify.processor.dto.EmailPayload;
import com.notify.processor.exception.NotDeliveredException;
import com.notify.processor.interfaces.NotificationProcessor;
import com.notify.processor.service.FeedbackSender;
import com.notify.processor.service.NotifyLogService;
import com.notify.processor.service.provider.email.EmailUnisenderProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.resilience.annotation.Retryable;
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
    @Retryable(
            includes = { NotDeliveredException.class },
            maxRetries = 3,
            delay = 500,
            multiplier = 2.0,
            maxDelay = 5000
    )
    public void process(NotifyKafkaDTO dto) {
        EmailPayload email = objectMapper.readValue(dto.getPayload(), EmailPayload.class);
        try {
            if (emailProvider.isEnabled()) {
                logService.logSent(dto);
                log.info("Сообщение отправлено в email");
                if (emailProvider.send(email).getStatusCode() == HttpStatus.OK) {
                    logService.logSucceed(dto);
                    fbSender.sendFeedback(dto);
                }
                else {
                    log.warn("Сообщение не доставлено");
                    throw new NotDeliveredException("Сообщение не доставлено");
                }
            } else {
                log.info("Сообщение не отправлено в email, канал выключен");
                logService.logFailed(dto);
                fbSender.sendToDLQ(dto);
            }
        }
        catch (NotDeliveredException e){
            logService.logRetry(dto, dto.getRetryCount() + 1);
            log.warn("Ошибка Email, попытка #{}", dto.getRetryCount());
            throw e;
        }
    }

    @Override
    public String getProcess() {
        return "email";
    }
}
