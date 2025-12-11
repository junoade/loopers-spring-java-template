package com.loopers.domain.coupon;

import com.loopers.domain.user.UserModel;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Entity
@Table(name = "assigned_coupons")
@Getter
public class AssignedCouponModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 유저
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_pk_id", nullable = false)
    private UserModel user;

    // 쿠폰 마스터
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id", nullable = false)
    private CouponModel coupon;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private AssignedCouponStatus status;

    @Column(name = "issued_at", nullable = false, updatable = false)
    private ZonedDateTime issuedAt;

    @Column(name = "used_at")
    private ZonedDateTime usedAt;

    @Column(name = "expired_at")
    private ZonedDateTime expiredAt;

    /**
     * 사용된 주문ID
     */
    @Column(name = "order_id")
    private Long orderId;

    @Builder
    public AssignedCouponModel(UserModel user,
                           CouponModel coupon,
                           AssignedCouponStatus status) {
        this.user = user;
        this.coupon = coupon;
        this.status = status;
        this.issuedAt = ZonedDateTime.now();
    }

    public boolean isStatusUsable() {
        return status == AssignedCouponStatus.ISSUED;
    }

    public void use(Long orderId) {
        this.status = AssignedCouponStatus.USED;
        this.orderId = orderId;
        this.usedAt = ZonedDateTime.now();
    }

    public void expire(LocalDateTime expiredAt) {
        this.status = AssignedCouponStatus.EXPIRED;
        this.expiredAt = ZonedDateTime.now();
    }
}
