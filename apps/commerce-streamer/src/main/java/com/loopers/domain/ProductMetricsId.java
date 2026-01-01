package com.loopers.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ProductMetricsId implements Serializable {

    @Column(name = "PRODUCT_ID", nullable = false)
    private Long productId;

    /**
     * yyyyMMdd 로 관리합니다.
     */
    @Column(name = "METRICS_DATE", length = 8, nullable = false)
    private String metricsDate;

    @Column(name = "METRICS_TYPE", length = 50, nullable = false)
    @Enumerated(EnumType.STRING)
    private MetricsType metricsType;


    public static ProductMetricsId of(Long productId, MetricsType metricsType) {
        String nowDate = convertDate(Instant.now());
        return new ProductMetricsId(productId, nowDate, metricsType);
    }


    private static String convertDate(Instant occurredAt) {
        return occurredAt
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .format(DateTimeFormatter.BASIC_ISO_DATE);
    }

}
