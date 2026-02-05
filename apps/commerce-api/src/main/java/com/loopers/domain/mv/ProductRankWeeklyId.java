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
public class ProductRankWeeklyId implements Serializable {

    @Column(name = "year_week_key", length = 10, nullable = false)
    private String yearWeek; // e.g. 2026-W01

    @Column(name = "product_id", nullable = false)
    private Long productId;
}
