package com.notify.api.unit;

import com.notify.api.JSON.KafkaJsonDeserializer;
import com.notify.api.dto.RequestSmsDTO;
import com.notify.api.entity.Notification;
import com.notify.api.enums.Channel;
import com.notify.api.repository.NotificationRepository;
import com.notify.api.service.NotificationCreateService;
import com.notify.dto.NotificationStatus;
import com.notify.dto.NotifyKafkaDTO;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class NotificationCreateServiceTest {

    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("apache/kafka:latest"));

    @Autowired
    private NotificationCreateService notificationCreateService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String testUserId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID().toString();
        notificationRepository.deleteAll();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        System.setProperty("docker.host", "tcp://localhost:2375");

        postgres.start();
        kafka.start();

        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @Test
    void shouldCreateSmsNotificationAndSendToKafka() {
        RequestSmsDTO request = new RequestSmsDTO();
        request.setUserPhone("+79991234567");
        request.setTargetPhone("+79991236567");
        request.setContent("Hello, World!");
        request.setSenderId(testUserId);

        notificationCreateService.create(request, testUserId);

        Notification saved = notificationRepository.findAll().stream()
                .filter(n -> n.getUserId().equals(testUserId))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Уведомление не найдено в БД"));

        assertThat(saved.getChannel()).isEqualTo(Channel.SMS);
        assertThat(saved.getStatus()).isEqualTo(NotificationStatus.PENDING);
        assertThat(saved.getPayload()).contains("+79991234567");

        Map<String, Object> consumerProps = Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers(),
                ConsumerConfig.GROUP_ID_CONFIG, "test-group-" + System.currentTimeMillis(),
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class
        );

        DefaultKafkaConsumerFactory<String, NotifyKafkaDTO> factory =
                new DefaultKafkaConsumerFactory<>(consumerProps);

        factory.setValueDeserializer(new KafkaJsonDeserializer<>(objectMapper, NotifyKafkaDTO.class));

        try (Consumer<String, NotifyKafkaDTO> consumer = factory.createConsumer()) {
            consumer.subscribe(java.util.List.of("notifications"));
            consumer.poll(Duration.ofMillis(100));

            ConsumerRecords<String, NotifyKafkaDTO> records = KafkaTestUtils.getRecords(consumer, Duration.ofSeconds(5));
            assertThat(records).isNotEmpty();

            var record = records.iterator().next();
            NotifyKafkaDTO dto = record.value();

            assertThat(dto.getUserId()).isEqualTo(testUserId);
            assertThat(dto.getChannel()).isEqualTo("SMS");
            assertThat(dto.getPayload()).contains("+79991234567");
            assertThat(dto.getStatus()).isEqualTo("PENDING");
        }
    }
}