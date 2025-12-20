package com.loopers.application.idempotency;

import com.loopers.domain.ProductLikeMetricsModel;
import com.loopers.infrastructure.EventHandleRepository;
import com.loopers.infrastructure.ProductLikeMetricsRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Testcontainers
@SpringBootTest
class EventHandledServiceTest {
    @Autowired
    KafkaTemplate<Object,Object> kafkaTemplate;
    @Autowired
    ProductLikeMetricsRepository metricsRepo;
    @Autowired
    EventHandleRepository handledRepo;

    @Test
    @DisplayName("멱등성 테스트")
    void duplicate_message_should_be_applied_once() throws Exception {
        long productId = 1L;
        ProductLikeMetricsModel metrics = metricsRepo.findById(productId)
                .orElseGet(() -> metricsRepo.save(ProductLikeMetricsModel.of(productId)));

        long before = metrics.getLikeCount();
      //
        String payload = """
      {"eventId": "06b9f00c-04bb-40fb-a96c-a9da5d0ede53", "traceId": "6944e75c781a87d97e02a61daca86d0a", "userPkId": 1, "eventType": "LIKE_CREATED", "productId": 1, "occurredAt": "2025-12-19T05:49:16.719347Z"}
      """;

        kafkaTemplate.send("product-like-events", String.valueOf(productId), payload);
        kafkaTemplate.send("product-like-events", String.valueOf(productId), payload);

        // 컨슈머 처리 대기
        // TODO - 카프카 브로커/컨슈머 테스트환경에서 설정필요
        Thread.sleep(1500);


        // then
        // like 카운트는 1번만 증가
        long after = metricsRepo.findById(productId).orElseThrow().getLikeCount();
        assertThat(after).isEqualTo(before + 1);

        long handledCount = handledRepo.countByConsumerNameAndEventId("product-like-metrics", "06b9f00c-04bb-40fb-a96c-a9da5d0ede53");
        assertThat(handledCount).isEqualTo(1);
    }

}
