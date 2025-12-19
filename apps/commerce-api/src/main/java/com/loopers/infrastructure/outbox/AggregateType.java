package com.loopers.infrastructure.outbox;

public enum AggregateType {
    ORDER("order-events"),
    PRODUCT_LIKE("like-events");

    private final String topic;

    AggregateType(String topic) {
        this.topic = topic;
    }

    public String getTopic() {
        return topic;
    }
}
