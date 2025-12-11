package com.loopers.application.coupon;

import com.loopers.domain.coupon.DiscountType;

import java.time.LocalDateTime;

public record CreateCouponCommand(
        String couponCode,
        String name,
        DiscountType discountType,
        Long discountValue,
        LocalDateTime validFrom,
        LocalDateTime validTo
) {

}
