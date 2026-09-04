package com.notify.processor.test.unit;

import com.notify.processor.service.DeduplicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
public class DeduplicationTest {
    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7.4-alpine")
            .withExposedPorts(6379);

    @Autowired
    private DeduplicationService deduplicationService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private String key;
    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);
    }

    @BeforeEach
    void setUp(){
        redisTemplate.keys("dedup:key:*").forEach(redisTemplate::delete);
        key = UUID.randomUUID().toString();
    }

    @Test
    void shouldGotDuplicate(){
        deduplicationService.markAsProcessed(key);

        assertThat(deduplicationService.isDuplicate(key)).isTrue();
    }

    @Test
    void shouldGotDuplicateThenUnique() throws InterruptedException {
        deduplicationService.markAsProcessed(key);

        assertThat(deduplicationService.isDuplicate(key)).isTrue();

        Thread.sleep(5000);

        assertThat(deduplicationService.isDuplicate(key)).isFalse();
    }

    @Test
    void shouldGotDuplicateThenUpdateTTL() throws InterruptedException {
        deduplicationService.markAsProcessed(key);

        assertThat(deduplicationService.isDuplicate(key)).isTrue();

        Thread.sleep(4000);

        deduplicationService.updateTTL(key);

        assertThat(deduplicationService.isDuplicate(key)).isTrue();
    }
}
