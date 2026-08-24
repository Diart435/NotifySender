package com.notify.processor.service;

import com.notify.dto.NotifyKafkaDTO;
import com.notify.processor.dto.QueueItem;
import com.notify.processor.interfaces.NotificationProcessor;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.support.Acknowledgment;
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
    private final Map<String, Integer> limits = Map.of(
            "sms", 35, "email", 1, "push", 100
    );
    private final Map<String, BlockingQueue<QueueItem>> queues = Map.of(
            "sms", new LinkedBlockingQueue<>(100), "email", new LinkedBlockingQueue<>(100), "push", new LinkedBlockingQueue<>(100)
    );
    @PostConstruct
    public void init(){
        for(String c : queues.keySet()){
            BlockingQueue<QueueItem> queue = queues.get(c);
            for(int i = 0; i < 100; i++) {
                virtualExecutor.submit(() -> worker(queue, c));
            }
        }
    }

    public void enqueue(String channel, NotifyKafkaDTO dto, NotificationProcessor processor, Acknowledgment ack) throws InterruptedException{
        BlockingQueue<QueueItem> queue = queues.get(channel);
        queue.offer(new QueueItem(dto, processor, ack), 3, TimeUnit.SECONDS);
    }
    private void worker(BlockingQueue<QueueItem> queue, String channel){
        while(true){
            QueueItem item = null;
            try{
                item = queue.take();
                int limit = limits.get(channel);

                while (!rateLimiter.tryAcquire(channel, limit, 2)) {
                    Thread.sleep(50);
                }
                item.getProcessor().process(item.getDto());

                item.getAck().acknowledge();
                logService.logSave(item.getDto());

                log.debug("Обработано {} сообщение {}", channel, item.getDto().getId());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.info("Воркер {} приостановил работу", channel);
                virtualExecutor.submit(() -> worker(queue, channel));
                return;
            }
            catch (Exception e){
                log.error("Ошибка обработки {} сообщения {}", channel, item.getDto().getId(), e);
                logService.logFailed(item.getDto());
                fbSender.sendFeedback(item.getDto());
                fbSender.sendToDLQ(item.getDto());
                item.getAck().acknowledge();
                log.info("Сообщение {} отправлено в DLQ после ошибки", item.getDto().getId());
            }
        }
    }
}
