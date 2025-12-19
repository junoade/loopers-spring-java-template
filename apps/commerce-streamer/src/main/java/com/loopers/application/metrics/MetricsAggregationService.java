package com.loopers.application.metrics;

import com.loopers.domain.ProductLikeMetricsModel;
import com.loopers.infrastructure.ProductLikeMetricsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MetricsAggregationService {
    private final ProductLikeMetricsRepository likeMetricsRepository;

    @Transactional
    public void handleProductLiked(Long productId) {
        log.debug("Handling product liked event");

        ProductLikeMetricsModel metrics = likeMetricsRepository.findById(productId)
                .orElseGet(() -> likeMetricsRepository.save(ProductLikeMetricsModel.of(productId)));

        metrics.increase();
    }
}
