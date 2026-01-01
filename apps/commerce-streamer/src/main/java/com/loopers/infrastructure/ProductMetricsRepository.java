package com.loopers.infrastructure;

import com.loopers.domain.ProductMetricsId;
import com.loopers.domain.ProductMetricsModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductMetricsRepository extends JpaRepository<ProductMetricsModel, ProductMetricsId> {
}
