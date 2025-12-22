package com.loopers.domain;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "product_like_metrics")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductLikeMetricsModel {
    @Id
    private Long productId;

    @Column(nullable = false)
    private long likeCount;

    public static ProductLikeMetricsModel of(Long productId) {
        ProductLikeMetricsModel m = new ProductLikeMetricsModel();
        m.productId = productId;
        m.likeCount = 0;
        return m;
    }

    public void increase() {
        this.likeCount += 1;
    }
}
