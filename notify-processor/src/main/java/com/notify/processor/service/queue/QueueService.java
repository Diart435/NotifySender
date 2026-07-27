package com.notify.processor.service.queue;

import com.google.common.util.concurrent.RateLimiter;
import com.notify.dto.NotifyKafkaDTO;
import com.notify.processor.interfaces.NotificationProcessor;
import com.notify.processor.service.FeedbackSender;
import com.notify.processor.service.NotifyLogService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.*;

@Service
@Slf4j
public class QueueService {
    private final BlockingQueue<QueueItem> queue = new LinkedBlockingQueue<>(100);
    private final ExecutorService executor = Executors.newFixedThreadPool(3);
    private final NotifyLogService logService;;
    private final FeedbackSender fbSender;

    public QueueService(NotifyLogService logService, FeedbackSender fbSender) {  // ← Только sender!
        this.logService = logService;
        this.fbSender = fbSender;
    }

    private final Map<String, RateLimiter> limiters = Map.of(
            "sms", RateLimiter.create(10.0),
            "email", RateLimiter.create(5.0),
            "push", RateLimiter.create(20.0)
    );

    @PostConstruct
    public void startWorkers() {
        for (int i = 0; i < 3; i++) {
            executor.submit(this::worker);
        }
    }

    public void enqueue(NotifyKafkaDTO dto, NotificationProcessor processor) throws InterruptedException {
        boolean offered = queue.offer(new QueueItem(dto, processor), 5, TimeUnit.SECONDS);
        if (!offered) {
            log.error("Очередь переполнена, сообщение {} не добавлено", dto.getId());
            throw new RuntimeException("Queue is full");
        }
    }

    private void worker() {
        while (true) {
            QueueItem item = null;
            try {
                item = queue.take();
                RateLimiter limiter = limiters.get(item.getProcessor().getProcess());
                limiter.acquire();
                item.getProcessor().process(item.getDto());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("Ошибка в воркере", e);
                if (item != null) {
                    int retryCount = item.getDto().getRetryCount() + 1;
                    logService.logRetry(item.getDto(), retryCount);
                    if (retryCount < 3) {
                        try {
                            queue.offer(item, 5, TimeUnit.SECONDS);
                            log.warn("Ретрай #{} для сообщения {}, возвращено в очередь", retryCount, item.getDto().getId());
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    } else {
                        logService.logFailed(item.getDto());
                        fbSender.sendFeedback(item.getDto());
                        fbSender.sendToDLQ(item.getDto());
                        log.error("Сообщение {} отправлено в DLQ после {} ретраев", item.getDto().getId(), retryCount);
                    }
                }
            }
        }
    }
}
