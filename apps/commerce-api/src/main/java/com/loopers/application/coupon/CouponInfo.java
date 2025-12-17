package com.loopers.application.coupon;

import com.loopers.domain.coupon.CouponModel;
import com.loopers.domain.coupon.CouponStatus;
import com.loopers.domain.coupon.DiscountType;

public record CouponInfo(
        Long id,
        String couponCode,
        String name,
        DiscountType discountType,
        Long discountValue,
        CouponStatus status
) {
    public static CouponInfo from(CouponModel coupon) {
        return new CouponInfo(
                coupon.getId(),
                coupon.getCouponCode(),
                coupon.getName(),
                coupon.getDiscountType(),
                coupon.getDiscountValue(),
                coupon.getStatus()
        );
    }
}
