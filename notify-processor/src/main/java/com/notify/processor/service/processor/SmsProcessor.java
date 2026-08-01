package com.notify.processor.service.processor;

import com.notify.dto.NotifyKafkaDTO;
import com.notify.processor.dto.SmsPayload;
import com.notify.processor.interfaces.NotificationProcessor;
import com.notify.processor.service.FeedbackSender;
import com.notify.processor.service.NotifyLogService;
import com.notify.processor.service.provider.sms.SmsRuProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
@Slf4j
public class SmsProcessor implements NotificationProcessor {
    private final ObjectMapper objectMapper;
    private final NotifyLogService logService;
    private final FeedbackSender fbSender;
    private final SmsRuProvider smsProvider;
    @Override
    public void process(NotifyKafkaDTO dto) {
        SmsPayload sms = objectMapper.readValue(dto.getPayload(), SmsPayload.class);
        log.info("Сообщение отправлено в sms");
        if(smsProvider.isEnabled()) {
            if (smsProvider.send(sms).getStatusCode() == HttpStatus.OK) {
                logService.logSucceed(dto);
                fbSender.sendFeedback(dto);
            }
        }
        else{
            log.info("Сообщение не отправлено в sms, канал выключен");
            logService.logFailed(dto);
            fbSender.sendFeedback(dto);
        }
    }

    @Override
    public String getProcess() {
        return "sms";
    }
}
