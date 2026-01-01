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

    @Column(name = "year_month_key", length = 8, nullable = false)
    private String yearMonth; // e.g. 202601

    @Column(name = "product_id", nullable = false)
    private Long productId;
}
