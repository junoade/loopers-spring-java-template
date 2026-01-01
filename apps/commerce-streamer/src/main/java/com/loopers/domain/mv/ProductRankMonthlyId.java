package com.loopers.domain.mv;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EqualsAndHashCode
public class ProductRankMonthlyId implements Serializable {

    @Column(name = "year_month", length = 10, nullable = false)
    private String yearMonth; // e.g. 2026-01 or 202601

    @Column(name = "product_id", nullable = false)
    private Long productId;
}
