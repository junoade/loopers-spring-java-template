package com.loopers.infrastructure.coupon;

import com.loopers.domain.coupon.AssignedCouponModel;
import com.loopers.domain.coupon.AssignedCouponStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssignedCouponRepository extends JpaRepository<AssignedCouponModel, Long> {
    List<AssignedCouponModel> findByUserId(Long userPkId);
    List<AssignedCouponModel> findByUserIdAndStatusIn(Long userPkId, List<AssignedCouponStatus> statuses);
}
