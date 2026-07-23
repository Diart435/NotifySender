package com.notify.api.service;

import com.notify.api.dto.BaseNotificationDTO;
import com.notify.api.interfaces.NotificationCreateStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class NotificationCreateStrategyFactory {

    private final Map<Class<? extends BaseNotificationDTO>, NotificationCreateStrategy<?>> strategyMap = new HashMap<>();

    public NotificationCreateStrategyFactory(List<NotificationCreateStrategy<?>> strategies) {
        for (NotificationCreateStrategy<?> strategy : strategies) {
            try {
                Class<?> dtoClass = extractDtoClass(strategy);

                if (BaseNotificationDTO.class.isAssignableFrom(dtoClass)) {
                    Class<? extends BaseNotificationDTO> key = (Class<? extends BaseNotificationDTO>) dtoClass;
                    strategyMap.put(key, strategy);
                    log.info("Зарегистрирована стратегия {} для {}",
                            strategy.getClass().getSimpleName(),
                            key.getSimpleName()
                    );
                } else {
                    log.warn("Стратегия {} имеет неверный тип DTO: {}",
                            strategy.getClass().getSimpleName(),
                            dtoClass.getName()
                    );
                }
            } catch (Exception e) {
                log.error("Ошибка регистрации стратегии {}: {}",
                        strategy.getClass().getSimpleName(),
                        e.getMessage()
                );
            }
        }

        log.info("✅ Всего зарегистрировано стратегий: {}", strategyMap.size());
        if (strategyMap.isEmpty()) {
            log.error("НЕ ЗАРЕГИСТРИРОВАНО НИ ОДНОЙ СТРАТЕГИИ!");
        }
    }

    private Class<?> extractDtoClass(NotificationCreateStrategy<?> strategy) {
        var genericInterfaces = strategy.getClass().getGenericInterfaces();

        for (var genericInterface : genericInterfaces) {
            if (genericInterface instanceof ParameterizedType) {
                ParameterizedType paramType = (ParameterizedType) genericInterface;
                Class<?> rawType = (Class<?>) paramType.getRawType();
                if (NotificationCreateStrategy.class.isAssignableFrom(rawType)) {
                    return (Class<?>) paramType.getActualTypeArguments()[0];
                }
            }
        }

        throw new RuntimeException(
                "Не удалось определить тип DTO для стратегии " +
                        strategy.getClass().getSimpleName()
        );
    }

    public <T extends BaseNotificationDTO> NotificationCreateStrategy<T> getStrategy(T request) {
        NotificationCreateStrategy<?> strategy = strategyMap.get(request.getClass());

        if (strategy == null) {
            strategyMap.keySet().forEach(key ->
                    log.error("  - {}", key.getSimpleName())
            );
            throw new IllegalArgumentException(
                    "Стратегия для " + request.getClass().getSimpleName() + " не найдена. " +
                            "Доступны: " + strategyMap.keySet().stream().map(Class::getSimpleName).toList()
            );
        }
        return (NotificationCreateStrategy<T>) strategy;
    }
}
