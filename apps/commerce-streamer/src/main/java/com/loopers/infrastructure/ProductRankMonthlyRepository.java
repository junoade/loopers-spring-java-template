package com.loopers.infrastructure;

import com.loopers.domain.mv.ProductRankMonthly;
import com.loopers.domain.mv.ProductRankMonthlyId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRankMonthlyRepository extends JpaRepository<ProductRankMonthly, ProductRankMonthlyId> {
}
