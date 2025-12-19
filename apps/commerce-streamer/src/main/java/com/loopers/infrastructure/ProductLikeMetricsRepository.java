package com.loopers.infrastructure;

import com.loopers.domain.ProductLikeMetricsModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductLikeMetricsRepository extends JpaRepository<ProductLikeMetricsModel, Long> {
}
