package com.loopers.interfaces.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.application.idempotency.EventHandledService;
import com.loopers.application.metrics.MetricsAggregationService;
import com.loopers.contract.event.ProductLikeEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductLikeEventConsumer {

    private static final String CONSUMER_NAME = "product-like-metrics";

    private final ObjectMapper objectMapper;
    private final EventHandledService eventHandledService;
    private final MetricsAggregationService metricsAggregationService;

    @KafkaListener(topics = "product-like-events", groupId = "${spring.kafka.consumer.group-id}")
    public void consume( @Header(KafkaHeaders.RECEIVED_KEY) String key,
                         String payload,
                         Acknowledgment ack) throws Exception {
        log.debug("Consuming product-like-events");
        ProductLikeEvent event =
                objectMapper.readValue(payload, ProductLikeEvent.class);

        boolean first = eventHandledService.tryHandle(
                CONSUMER_NAME,
                event.eventId()
        );

        if (!first) {
            ack.acknowledge();
            return;
        }

        metricsAggregationService.handleProductLiked(event.productId());

        ack.acknowledge();
    }
}
