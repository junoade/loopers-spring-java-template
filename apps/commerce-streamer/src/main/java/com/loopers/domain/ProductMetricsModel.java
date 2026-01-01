package com.loopers.domain;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "product_metrics")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductMetricsModel {

    @EmbeddedId
    private ProductMetricsId id;

    @Column(nullable = false)
    private long count;

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "UPDATED_AT", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public static ProductMetricsModel of(ProductMetricsId id) {
        ProductMetricsModel m = new ProductMetricsModel();
        m.id = id;
        m.count = 0;
        return m;
    }

    public void increase() {
        this.count += 1;
    }
}
