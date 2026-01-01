package com.loopers.domain.mv;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "mv_product_rank_monthly")
@Getter
@NoArgsConstructor
public class ProductRankMonthly {

    @EmbeddedId
    private ProductRankMonthlyId id;

    @Column(name = "view_count", nullable = false)
    private long viewCount;

    @Column(name = "like_count", nullable = false)
    private long likeCount;

    @Column(name = "order_count", nullable = false)
    private long orderCount;

    @Column(name = "score", nullable = false)
    private double score;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static ProductRankMonthly of(String yearMonth, Long productId,
                                        long viewCount, long likeCount, long orderCount) {
        ProductRankMonthly e = new ProductRankMonthly();
        e.id = new ProductRankMonthlyId(yearMonth, productId);
        e.viewCount = viewCount;
        e.likeCount = likeCount;
        e.orderCount = orderCount;
        e.createdAt = LocalDateTime.now();
        e.updatedAt = LocalDateTime.now();
        return e;
    }

    public void overwrite(long viewCount, long likeCount, long orderCount) {
        this.viewCount = viewCount;
        this.likeCount = likeCount;
        this.orderCount = orderCount;
        this.updatedAt = LocalDateTime.now();
    }

    public Long productId() {
        return id.getProductId();
    }

    public String yearMonth() {
        return id.getYearMonth();
    }
}
