package com.loopers.domain.mv;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Table(name = "mv_product_rank_weekly")
@Getter
@NoArgsConstructor
public class ProductRankWeekly {
    @EmbeddedId
    private ProductRankWeeklyId id;

    @Column(name = "view_count", nullable = false)
    private long viewCount;

    @Column(name = "like_count", nullable = false)
    private long likeCount;

    @Column(name = "order_count", nullable = false)
    private long orderCount;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static ProductRankWeekly of(String yearWeek, Long productId,
                                       long viewCount, long likeCount, long orderCount) {
        ProductRankWeekly e = new ProductRankWeekly();
        e.id = new ProductRankWeeklyId(yearWeek, productId);
        e.viewCount = viewCount;
        e.likeCount = likeCount;
        e.orderCount = orderCount;
        e.createdAt = LocalDateTime.now(ZoneId.systemDefault());
        e.updatedAt = LocalDateTime.now(ZoneId.systemDefault());
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

    public String yearWeek() {
        return id.getYearWeek();
    }
}
