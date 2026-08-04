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
    private final NotifyLogService logService;
    private final FeedbackSender fbSender;

    public QueueService(NotifyLogService logService, FeedbackSender fbSender) {
        this.logService = logService;
        this.fbSender = fbSender;
    }

    private final Map<String, BlockingQueue<QueueItem>> queues = Map.of(
            "sms", new LinkedBlockingQueue<>(100),
            "email", new LinkedBlockingQueue<>(100),
            "push", new LinkedBlockingQueue<>(100)
    );

    private final Map<String, RateLimiter> limiters = Map.of(
            "sms", RateLimiter.create(35.0),
            "email", RateLimiter.create(1.0),
            "push", RateLimiter.create(100.0)
    );

    private final ExecutorService virtualExecutor = Executors.newVirtualThreadPerTaskExecutor();

    @PostConstruct
    public void startWorkers() {
        for(String channel : queues.keySet()) {
            BlockingQueue<QueueItem> queue = queues.get(channel);
            for (int i = 0; i < 100; i++) {
                virtualExecutor.submit(() -> worker(queue, channel));
            }
        }
    }

    public void enqueue(String channel, NotifyKafkaDTO dto, NotificationProcessor processor) throws InterruptedException {
        BlockingQueue<QueueItem> queue = getQueue(channel);
        boolean offered = queue.offer(new QueueItem(dto, processor), 5, TimeUnit.SECONDS);
        if (!offered) {
            log.error("Очередь переполнена, сообщение {} не добавлено", dto.getId());
            throw new RuntimeException("Queue is full");
        }
    }

    private void worker(BlockingQueue<QueueItem> queue, String channel) {
        while (true) {
            QueueItem item = null;
            try {
                item = queue.take();
                RateLimiter limiter = limiters.get(channel);
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
    private BlockingQueue<QueueItem> getQueue(String channel){
        BlockingQueue<QueueItem> queue = queues.get(channel);
        if(queue == null){
            throw new IllegalArgumentException("Unknown channel: " + channel);
        }
        return queue;
    }
}
