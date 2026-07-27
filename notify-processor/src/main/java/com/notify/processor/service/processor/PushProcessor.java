package com.notify.processor.service.processor;

import com.notify.dto.NotifyKafkaDTO;
import com.notify.processor.dto.PushPayload;
import com.notify.processor.interfaces.NotificationProcessor;
import com.notify.processor.service.FeedbackSender;
import com.notify.processor.service.NotifyLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
@Slf4j
public class PushProcessor implements NotificationProcessor {
    private final ObjectMapper objectMapper;
    private final NotifyLogService logService;
    private final FeedbackSender fbSender;
    @Override
    public void process(NotifyKafkaDTO dto) {
        PushPayload push = objectMapper.readValue(dto.getPayload(), PushPayload.class);
        log.info("Сообщение отправлено в push");
        logService.logSucceed(dto);
        fbSender.sendFeedback(dto);
        //TODO сделать провайдера
    }

    @Override
    public String getProcess() {
        return "push";
    }
}
