package com.notify.processor.test.unit;

import com.notify.processor.service.RedisRateLimiter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
public class RedisRateLimiterTest {

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7.4-alpine")
            .withExposedPorts(6379);

    @Autowired
    private RedisRateLimiter rateLimiter;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @BeforeEach
    void setUp(){
        redisTemplate.keys("rate:*").forEach(redisTemplate::delete);
    }

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);
    }

    @Test
    void shouldNotExceedLimit(){
        String channel = "sms";
        int limit = 5;
        int ttl = 2;

        for(int i = 0; i < limit; i++){
            assertThat(rateLimiter.tryAcquire(channel, limit, ttl)).isTrue();
        }

        assertThat(rateLimiter.tryAcquire(channel, limit, ttl)).isFalse();
    }

    @Test
    void shouldResetAfterTTL() throws InterruptedException {
        String channel = "sms";
        int limit = 3;
        int ttl = 2;

        for(int i = 0; i < limit; i++){
            assertThat(rateLimiter.tryAcquire(channel, limit, ttl)).isTrue();
        }

        assertThat(rateLimiter.tryAcquire(channel, limit, ttl)).isFalse();

        Thread.sleep(2000);

        assertThat(rateLimiter.tryAcquire(channel, limit, ttl)).isTrue();
    }

    @Test
    void shouldUseRedisForDistributedLock() {
        String channel = "sms";
        int limit = 3;
        int ttl = 2;

        boolean first = rateLimiter.tryAcquire(channel, limit, ttl);
        assertThat(first).isTrue();

        boolean second = rateLimiter.tryAcquire(channel, limit, ttl);
        assertThat(second).isTrue();

        boolean third = rateLimiter.tryAcquire(channel, limit, ttl);
        assertThat(third).isTrue();

        boolean fourth = rateLimiter.tryAcquire(channel, limit, ttl);
        assertThat(fourth).isFalse();

        Set<String> keys = redisTemplate.keys("rate:" + channel + ":*");
        assertThat(keys).isNotEmpty();

        String key = keys.iterator().next();
        Long ttlActual = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        assertThat(ttlActual).isBetween(0L, 2L);
    }
}
