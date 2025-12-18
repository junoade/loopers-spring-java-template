package com.loopers.interfaces.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderEventConsumer {

    @KafkaListener(
            topics = "order-events",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            String payload,
            Acknowledgment ack
    ) {
        log.info("Received order event: {}", payload);
        ack.acknowledge();
    }
}
