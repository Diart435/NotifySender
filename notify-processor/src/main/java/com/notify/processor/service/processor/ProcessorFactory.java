package com.notify.processor.service.processor;

import com.notify.processor.interfaces.NotificationProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;

@Component
@Slf4j
public class ProcessorFactory {
    HashMap<String, NotificationProcessor> map = new HashMap<>();

    public ProcessorFactory(List<NotificationProcessor> processors){
        for(NotificationProcessor processor : processors){
            map.put(processor.getProcess(), processor);
            log.info("Зарегестрирована обработка для канала: {}", processor.getProcess());
        }
    }

    public NotificationProcessor getProcessor(String process){
        NotificationProcessor processor = map.get(process);
        if (processor == null) {
            log.error("Процессор для канала {} не найден! Доступны: {}", process, map.keySet());
        }
        return processor;
    }
}
