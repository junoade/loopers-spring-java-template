package com.loopers.domain.coupon;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "coupons")
@Getter
public class CouponModel extends BaseEntity {

    @Column(name = "coupon_code", length = 50, nullable = false, unique = true)
    private String couponCode;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", length = 20, nullable = false)
    private DiscountType discountType;

    /**
     * FIXED   : 할인 금액(원)
     * PERCENT : 할인율(%)
     */
    @Column(name = "discount_value", nullable = false)
    private Long discountValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private CouponStatus status;

    @OneToMany(mappedBy = "coupon")
    private List<AssignedCouponModel> assignedCoupons = new ArrayList<>();

    @Builder
    public CouponModel(String couponCode,
                   String name,
                   DiscountType discountType,
                   Long discountValue,
                   CouponStatus status) {
        this.couponCode = couponCode;
        this.name = name;
        this.discountType = discountType;
        this.discountValue = discountValue;
        this.status = status;
    }

    protected CouponModel() {}

    /**
     * 단순 주문 금액 기준 할인 금액 계산
     * [주의] - 할인된 총 금액이 아닙니다
     */
    public long calculateDiscount(long orderAmount) {
        long discount;
        if (discountType == DiscountType.FIXED) {
            discount = discountValue;
        } else { // PERCENT
            discount = orderAmount * discountValue / 100;
        }
        return discount;
    }

    public boolean isStatusActive() {
        return status == CouponStatus.ACTIVE;
    }
}
