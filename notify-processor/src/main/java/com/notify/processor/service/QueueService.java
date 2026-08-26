package com.notify.processor.service;

import com.notify.dto.NotifyKafkaDTO;
import com.notify.processor.dto.QueueItem;
import com.notify.processor.interfaces.NotificationProcessor;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class QueueService {
    private final ExecutorService virtualExecutor = Executors.newVirtualThreadPerTaskExecutor();
    private final RedisRateLimiter rateLimiter;
    private final NotifyLogService logService;
    private final FeedbackSender fbSender;
    private final DeduplicationService deduplicationService;
    private final Map<String, Integer> limits = Map.of(
            "sms", 35, "email", 1, "push", 100
    );
    private final Map<String, BlockingQueue<QueueItem>> queues = Map.of(
            "sms", new LinkedBlockingQueue<>(1000), "email", new LinkedBlockingQueue<>(1000), "push", new LinkedBlockingQueue<>(1000)
    );
    @PostConstruct
    public void init(){
        for(String channel : queues.keySet()){
            BlockingQueue<QueueItem> queue = queues.get(channel);
            for(int i = 0; i < 100; i++) {
                virtualExecutor.submit(() -> startWorker(queue, channel));
            }
        }
    }
    private void startWorker(BlockingQueue<QueueItem> queue, String channel) {
        virtualExecutor.submit(() -> {
            try {
                worker(queue, channel);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Воркер {} прерван, перезапускаем...", channel);
                if (!Thread.currentThread().isInterrupted()) {
                    startWorker(queue, channel);
                }
            } catch (Exception e) {
                log.error("Воркер {} упал, перезапускаем", channel, e);
                startWorker(queue, channel);
            }
        });
    }

    public void enqueue(String channel, NotifyKafkaDTO dto, NotificationProcessor processor, Acknowledgment ack) throws InterruptedException{
        BlockingQueue<QueueItem> queue = queues.get(channel);
        boolean offered = queue.offer(new QueueItem(dto, processor, ack), 3, TimeUnit.SECONDS);
        if(!offered){
            log.info("Канал {} переполнен", channel);
        }
    }
    private void worker(BlockingQueue<QueueItem> queue, String channel) throws InterruptedException {
        while(true){
            QueueItem item = null;
            try{
                item = queue.poll(20, TimeUnit.MILLISECONDS);
                if(item == null){
                    continue;
                }

                int limit = limits.get(channel);
                while (!rateLimiter.tryAcquire(channel, limit, 2)) {
                    Thread.sleep(20);
                }
                item.getProcessor().process(item.getDto());

                item.getAck().acknowledge();
                logService.logSave(item.getDto());
                deduplicationService.markAsProcessed(item.getDto().getDedupKey());
                log.debug("Обработано {} сообщение {}", channel, item.getDto().getId());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.info("Воркер {} приостановил работу", channel);
                throw e;
            }
            catch (Exception e){
                if (item != null) {
                    log.error("Ошибка обработки {} сообщения {}", channel, item.getDto().getId(), e);
                    logService.logFailed(item.getDto());
                    fbSender.sendFeedback(item.getDto());
                    fbSender.sendToDLQ(item.getDto());
                    item.getAck().acknowledge();
                    log.info("Сообщение {} отправлено в DLQ после ошибки", item.getDto().getId());
                } else {
                    log.error("Критическая ошибка в воркере {}: {}", channel, e.getMessage(), e);
                }
            }
        }
    }
    @Scheduled(fixedDelay = 5000)
    public void logQueueSizes() {
        queues.forEach((channel, queue) -> {
            int size = queue.size();
            if (size > 700) {
                log.warn("Очередь {} заполнена на {} элементов", channel, size);
            } else if (size > 100) {
                log.info("Очередь {}: {} элементов", channel, size);
            }
        });
    }
}
