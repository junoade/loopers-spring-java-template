package com.loopers.application.idempotency;

import com.loopers.domain.MetricsType;
import com.loopers.domain.ProductMetricsId;
import com.loopers.domain.ProductMetricsModel;
import com.loopers.infrastructure.EventHandleRepository;
import com.loopers.infrastructure.ProductMetricsRepository;
import com.loopers.testcontainers.KafkaTestContainersConfig;
import com.loopers.utils.KafkaCleanUp;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Slf4j
@ActiveProfiles("local")
@Import(KafkaTestContainersConfig.class)
@SpringBootTest
class EventHandledServiceTest {

    @Autowired
    private KafkaCleanUp kafkaCleanUp;

    @Autowired
    KafkaTemplate<Object,Object> kafkaTemplate;
    @Autowired
    ProductMetricsRepository metricsRepo;
    @Autowired
    EventHandleRepository handledRepo;

    @Autowired
    private KafkaProperties kafkaProperties;

    @BeforeEach
    void setUp() {
        // @BeforeEach는 이미 SpringBootTest 컨텍스트 + KafkaListener 컨테이너가 다 올라간 뒤에 실행
        // kafkaCleanUp.resetAllTestTopics();
        kafkaCleanUp.resetAllConsumerGroups();
    }

    @Test
    @DisplayName("멱등성 테스트")
    void duplicate_message_should_be_applied_once() throws Exception {

        long productId = 1L;
        ProductMetricsId id = ProductMetricsId.of(productId, MetricsType.LIKE);
        ProductMetricsModel metrics = metricsRepo.findById(id)
                .orElseGet(() -> metricsRepo.save(ProductMetricsModel.of(id)));

        long before = metrics.getCount();
        log.debug("before count : {}", before);

        String payload = """
      {"eventId": "06b9f00c-04bb-40fb-a96c-a9da5d0ede53", "traceId": "6944e75c781a87d97e02a61daca86d0a", "userPkId": 1, "eventType": "LIKE_CREATED", "productId": 1, "occurredAt": "2025-12-19T05:49:16.719347Z"}
      """;

        kafkaTemplate.send("product-like-events", String.valueOf(productId), payload);
        kafkaTemplate.send("product-like-events", String.valueOf(productId), payload);

        // 컨슈머 처리 대기
        // TODO - 카프카 브로커/컨슈머 테스트환경에서 설정필요
        Thread.sleep(1500);

        Map<String, Object> consumerProps = kafkaProperties.buildConsumerProperties();
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "${spring.kafka.consumer.group-id}");


        // then
        // like 카운트는 1번만 증가
        long after = metricsRepo.findById(id).orElseThrow().getCount();
        assertThat(after).isEqualTo(before + 1);
        log.debug("after count : {}", after);


        long handledCount = handledRepo.countByConsumerNameAndEventId("product-like-metrics", "06b9f00c-04bb-40fb-a96c-a9da5d0ede53");
        assertThat(handledCount).isEqualTo(1);
    }

}
