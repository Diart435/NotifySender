package com.notify.api.test.unit;

import com.notify.api.JSON.KafkaJsonDeserializer;
import com.notify.api.dto.RequestSmsDTO;
import com.notify.api.entity.Notification;
import com.notify.api.enums.Channel;
import com.notify.api.repository.NotificationRepository;
import com.notify.api.service.DeliveryService;
import com.notify.api.service.UserService;
import com.notify.dto.NotificationStatus;
import com.notify.dto.NotifyKafkaDTO;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
class NotificationCreateTest {


    private static final GenericContainer<?> postgres = new GenericContainer<>("postgres:15")
            .withExposedPorts(5432)
            .withEnv("POSTGRES_DB", "testdb")
            .withEnv("POSTGRES_USER", "test")
            .withEnv("POSTGRES_PASSWORD", "test");

    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("apache/kafka:latest"));

    @Autowired
    private DeliveryService deliveryService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private UserService userService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String apiKey;

    private String testUserId;

    @BeforeEach
    void setUp() {
        notificationRepository.deleteAll();
        apiKey = userService.createUserAndGetApiKey("ADMIN", "yo@gmail.com", "1234");
        testUserId = userService.getUserByApiKey(apiKey).getId().toString();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {

        postgres.start();
        kafka.start();

        registry.add("spring.datasource.url", () -> String.format(
                "jdbc:postgresql://%s:%d/%s",
                postgres.getHost(),
                postgres.getFirstMappedPort(),
                "testdb"
        ));
        registry.add("spring.datasource.username",() -> "test");
        registry.add("spring.datasource.password", () -> "test");
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @Test
    void shouldCreateSmsNotificationAndSendToKafka() throws InterruptedException {
        RequestSmsDTO request = new RequestSmsDTO();
        request.setUserPhone("+79991234567");
        request.setTargetPhone("+79991236567");
        request.setContent("Hello, World!");
        deliveryService.delivery(request, apiKey);

        Notification saved = notificationRepository.findAll().stream()
                .filter(n -> n.getUserId().equals(testUserId))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Уведомление не найдено в БД"));

        assertThat(saved.getUserId()).isEqualTo(testUserId);
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
            consumer.subscribe(java.util.List.of("sms"));

            ConsumerRecord<String, NotifyKafkaDTO> record = KafkaTestUtils.getSingleRecord(consumer, "sms", Duration.ofSeconds(5));
            assertThat(record).isNotNull();
            NotifyKafkaDTO dto = record.value();

            assertThat(dto.getUserId()).isEqualTo(testUserId);
            assertThat(dto.getChannel()).isEqualTo("SMS");
            assertThat(dto.getPayload()).contains("+79991234567");
            assertThat(dto.getStatus()).isEqualTo("PENDING");
        }
    }
}