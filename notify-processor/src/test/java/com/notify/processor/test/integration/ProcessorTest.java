package com.notify.processor.test.integration;

import com.notify.dto.NotificationStatus;
import com.notify.dto.NotifyKafkaDTO;
import com.notify.processor.JSON.KafkaJsonDeserializer;
import com.notify.processor.dto.EmailPayload;
import com.notify.processor.dto.PushPayload;
import com.notify.processor.dto.SmsPayload;
import com.notify.processor.entity.DeliveryLog;
import com.notify.processor.exception.NotDeliveredException;
import com.notify.processor.repository.DeliveryLogRepository;
import com.notify.processor.service.FeedbackSender;
import com.notify.processor.service.NotifyLogService;
import com.notify.processor.service.processor.EmailProcessor;
import com.notify.processor.service.processor.PushProcessor;
import com.notify.processor.service.processor.SmsProcessor;
import com.notify.processor.service.provider.email.EmailUnisenderProvider;
import com.notify.processor.service.provider.push.FirebaseProvider;
import com.notify.processor.service.provider.sms.SmsRuProvider;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
public class ProcessorTest {
    @Autowired
    private EmailProcessor emailProcessor;

    @Autowired
    private PushProcessor pushProcessor;

    @Autowired
    private SmsProcessor smsProcessor;

    @Autowired
    private NotifyLogService logService;

    @Autowired
    private FeedbackSender fbSender;

    @Autowired
    private DeliveryLogRepository logRepository;

    @MockitoBean
    private EmailUnisenderProvider emailProvider;

    @MockitoBean
    private SmsRuProvider smsProvider;

    @MockitoBean
    private FirebaseProvider firebaseProvider;

    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("apache/kafka:latest"));

    private EmailPayload emailPayload;

    private SmsPayload smsPayload;

    private PushPayload pushPayload;

    static DefaultKafkaConsumerFactory<String, NotifyKafkaDTO> factory;

    private static ObjectMapper objectMapper = new ObjectMapper();

    @DynamicPropertySource
    static void init(DynamicPropertyRegistry registry){
        kafka.start();
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        Map<String, Object> consumerProps = Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers(),
                ConsumerConfig.GROUP_ID_CONFIG, "test-group-" + System.currentTimeMillis(),
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class
        );

        factory = new DefaultKafkaConsumerFactory<>(consumerProps);

        factory.setValueDeserializer(new KafkaJsonDeserializer<>(objectMapper, NotifyKafkaDTO.class));
    }
    @BeforeEach
    void setUp(){
        emailPayload = new EmailPayload();
        emailPayload.setLogin("ivan");
        emailPayload.setTargetUser("vasya");
        emailPayload.setUserEmail("ivan@gmail.com");
        emailPayload.setTargetEmail("vasya@gmail.com");
        emailPayload.setTitle("hello");
        emailPayload.setContent("hi");

        smsPayload = new SmsPayload();
        smsPayload.setContent("hi");
        smsPayload.setUserPhone("+79123456789");
        smsPayload.setTargetPhone("+79234567891");

        pushPayload = new PushPayload();
        pushPayload.setContent("hi");
        pushPayload.setTitle("hello");
        pushPayload.setPushToken("1");

        logRepository.deleteAll();
    }

    @Test
    void deliveryEmailShouldSuccess(){
        NotifyKafkaDTO dto = new NotifyKafkaDTO();
        dto.setPayload(objectMapper.writeValueAsString(emailPayload));
        logService.logSave(dto);
        when(emailProvider.isEnabled()).thenReturn(true);
        when(emailProvider.send(emailPayload)).thenReturn(ResponseEntity.ok().build());
        emailProcessor.process(dto);

        DeliveryLog saved = logRepository.findAll().stream()
                .filter(n -> n.getResult().equals(NotificationStatus.SUCCESS))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Уведомление не найдено в БД"));

        try (Consumer<String, NotifyKafkaDTO> consumer = factory.createConsumer()) {
            consumer.subscribe(java.util.List.of("notify-feedback"));

            ConsumerRecord<String, NotifyKafkaDTO> record = KafkaTestUtils.getSingleRecord(consumer, "notify-feedback", Duration.ofSeconds(5));
            assertThat(record).isNotNull();
            NotifyKafkaDTO feedbackDto = record.value();

            assertThat(feedbackDto.getStatus()).isEqualTo(NotificationStatus.SUCCESS.toString());
            assertThat(feedbackDto.getPayload()).isEqualTo(dto.getPayload());
        }
    }
    @Test
    void deliverySmsShouldSuccess(){
        NotifyKafkaDTO dto = new NotifyKafkaDTO();
        dto.setPayload(objectMapper.writeValueAsString(smsPayload));
        logService.logSave(dto);
        when(smsProvider.isEnabled()).thenReturn(true);
        when(smsProvider.send(smsPayload)).thenReturn(ResponseEntity.ok().build());
        smsProcessor.process(dto);

        DeliveryLog saved = logRepository.findAll().stream()
                .filter(n -> n.getResult().equals(NotificationStatus.SUCCESS))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Уведомление не найдено в БД"));

        try (Consumer<String, NotifyKafkaDTO> consumer = factory.createConsumer()) {
            consumer.subscribe(java.util.List.of("notify-feedback"));

            ConsumerRecord<String, NotifyKafkaDTO> record = KafkaTestUtils.getSingleRecord(consumer, "notify-feedback", Duration.ofSeconds(5));
            assertThat(record).isNotNull();
            dto = record.value();

            assertThat(dto.getStatus()).isEqualTo(NotificationStatus.SUCCESS.toString());
        }
    }
    @Test
    void deliveryPushShouldSuccess(){
        NotifyKafkaDTO dto = new NotifyKafkaDTO();
        dto.setPayload(objectMapper.writeValueAsString(pushPayload));
        logService.logSave(dto);
        when(firebaseProvider.isEnabled()).thenReturn(true);
        when(firebaseProvider.send(pushPayload)).thenReturn(ResponseEntity.ok().build());
        pushProcessor.process(dto);
        DeliveryLog saved = logRepository.findAll().stream()
                .filter(n -> n.getResult().equals(NotificationStatus.SUCCESS))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Уведомление не найдено в БД"));

        try (Consumer<String, NotifyKafkaDTO> consumer = factory.createConsumer()) {
            consumer.subscribe(java.util.List.of("notify-feedback"));

            ConsumerRecord<String, NotifyKafkaDTO> record = KafkaTestUtils.getSingleRecord(consumer, "notify-feedback", Duration.ofSeconds(5));
            assertThat(record).isNotNull();
            dto = record.value();

            assertThat(dto.getStatus()).isEqualTo(NotificationStatus.SUCCESS.toString());
        }
    }

    @Test
    void deliveryEmailServiceUnavailable(){
        NotifyKafkaDTO dto = new NotifyKafkaDTO();
        dto.setPayload(objectMapper.writeValueAsString(emailPayload));
        when(emailProvider.isEnabled()).thenReturn(true);
        when(emailProvider.send(emailPayload)).thenReturn(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build());

        assertThatThrownBy(() -> emailProcessor.process(dto))
                .isInstanceOf(NotDeliveredException.class);
    }

    @Test
    void deliverySmsProviderUnavailable(){
        NotifyKafkaDTO dto = new NotifyKafkaDTO();
        dto.setPayload(objectMapper.writeValueAsString(smsPayload));
        when(smsProvider.isEnabled()).thenReturn(true);
        when(smsProvider.send(smsPayload)).thenReturn(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build());

        assertThatThrownBy(() -> smsProcessor.process(dto))
                .isInstanceOf(NotDeliveredException.class);
    }

    @Test
    void deliveryPushProviderUnavailable(){
        NotifyKafkaDTO dto = new NotifyKafkaDTO();
        dto.setPayload(objectMapper.writeValueAsString(pushPayload));
        when(firebaseProvider.isEnabled()).thenReturn(true);
        when(firebaseProvider.send(pushPayload)).thenReturn(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build());

        assertThatThrownBy(() -> pushProcessor.process(dto))
                .isInstanceOf(NotDeliveredException.class);

    }

    @Test
    void deliveryEmailProviderDisabled(){
        NotifyKafkaDTO dto = new NotifyKafkaDTO();
        String channel = "EMAIL";
        dto.setChannel(channel);
        dto.setPayload(objectMapper.writeValueAsString(emailPayload));
        logService.logSave(dto);
        when(emailProvider.isEnabled()).thenReturn(false);

        emailProcessor.process(dto);

        DeliveryLog saved = logRepository.findAll().stream()
                .filter(n -> n.getResult().equals(NotificationStatus.FAILED))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Уведомление не найдено в БД"));

        try (Consumer<String, NotifyKafkaDTO> consumer = factory.createConsumer()) {
            consumer.subscribe(java.util.List.of("dlq-" + channel));

            ConsumerRecord<String, NotifyKafkaDTO> record = KafkaTestUtils.getSingleRecord(consumer, "dlq-" + channel, Duration.ofSeconds(5));
            assertThat(record).isNotNull();
            NotifyKafkaDTO feedbackDto = record.value();

            assertThat(feedbackDto.getStatus()).isEqualTo(NotificationStatus.FAILED.toString());
            assertThat(feedbackDto.getPayload()).isEqualTo(dto.getPayload());
        }
    }

    @Test
    void deliverySmsProviderDisabled(){
        NotifyKafkaDTO dto = new NotifyKafkaDTO();
        String channel = "SMS";
        dto.setChannel(channel);
        dto.setPayload(objectMapper.writeValueAsString(smsPayload));
        logService.logSave(dto);
        when(smsProvider.isEnabled()).thenReturn(false);

        smsProcessor.process(dto);

        DeliveryLog saved = logRepository.findAll().stream()
                .filter(n -> n.getResult().equals(NotificationStatus.FAILED))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Уведомление не найдено в БД"));

        try (Consumer<String, NotifyKafkaDTO> consumer = factory.createConsumer()) {
            consumer.subscribe(java.util.List.of("dlq-" + channel));

            ConsumerRecord<String, NotifyKafkaDTO> record = KafkaTestUtils.getSingleRecord(consumer, "dlq-" + channel, Duration.ofSeconds(5));
            assertThat(record).isNotNull();
            NotifyKafkaDTO feedbackDto = record.value();

            assertThat(feedbackDto.getStatus()).isEqualTo(NotificationStatus.FAILED.toString());
            assertThat(feedbackDto.getPayload()).isEqualTo(dto.getPayload());
        }
    }

    @Test
    void deliveryPushProviderDisabled(){
        NotifyKafkaDTO dto = new NotifyKafkaDTO();
        String channel = "PUSH";
        dto.setChannel(channel);
        dto.setPayload(objectMapper.writeValueAsString(pushPayload));
        logService.logSave(dto);
        when(firebaseProvider.isEnabled()).thenReturn(false);

        pushProcessor.process(dto);

        DeliveryLog saved = logRepository.findAll().stream()
                .filter(n -> n.getResult().equals(NotificationStatus.FAILED))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Уведомление не найдено в БД"));

        try (Consumer<String, NotifyKafkaDTO> consumer = factory.createConsumer()) {
            consumer.subscribe(java.util.List.of("dlq-" + channel));

            ConsumerRecord<String, NotifyKafkaDTO> record = KafkaTestUtils.getSingleRecord(consumer, "dlq-" + channel, Duration.ofSeconds(5));
            assertThat(record).isNotNull();
            NotifyKafkaDTO feedbackDto = record.value();

            assertThat(feedbackDto.getStatus()).isEqualTo(NotificationStatus.FAILED.toString());
            assertThat(feedbackDto.getPayload()).isEqualTo(dto.getPayload());
        }
    }
}
