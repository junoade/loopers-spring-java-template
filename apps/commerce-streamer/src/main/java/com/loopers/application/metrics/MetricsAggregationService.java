package com.loopers.application.metrics;

import com.loopers.domain.MetricsType;
import com.loopers.domain.ProductMetricsId;
import com.loopers.domain.ProductMetricsModel;
import com.loopers.infrastructure.ProductMetricsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MetricsAggregationService {
    private final ProductMetricsRepository likeMetricsRepository;

    @Transactional
    public void handleProductLiked(Long productId) {
        log.debug("Handling product liked event");

        ProductMetricsId id = ProductMetricsId.of(productId, MetricsType.LIKE);

        ProductMetricsModel metrics = likeMetricsRepository.findById(id)
                .orElseGet(() -> likeMetricsRepository.save(ProductMetricsModel.of(id)));

        metrics.increase();
    }

}
