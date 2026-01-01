package com.loopers.infrastructure;

import com.loopers.domain.mv.ProductRankWeekly;
import com.loopers.domain.mv.ProductRankWeeklyId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRankWeeklyRepository extends JpaRepository<ProductRankWeekly, ProductRankWeeklyId> {
}
