package com.loopers.application.ranking;

import com.loopers.application.like.event.ProductLikeEvent;
import com.loopers.application.product.ProductLikeSummary;
import com.loopers.application.product.ProductQueryService;
import com.loopers.domain.product.ProductSortType;
import com.loopers.ranking.DailyRankingResponse;
import com.loopers.ranking.RankingEntry;
import com.loopers.ranking.RankingZSetRepository;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;

@Slf4j
@Service
@RequiredArgsConstructor
public class RankingQueryService {
    private final RankingZSetRepository rankingZSetRepository;
    private final ProductQueryService productQueryService;

    @Transactional(readOnly = true)
    public RankingQueryResponse getDailyPopularProducts(String date, int size) {
        LocalDate target = initLocalDate(date);

        int limit = (size <= 0) ? 20 : Math.min(size, 100);

        List<RankingEntry> rankingEntries = rankingZSetRepository.findTopDailyAllByLimit(target, limit);
        List<ProductLikeSummary> productLikeSummaries = findProductSummaryFrom(rankingEntries);

        return new RankingQueryResponse(
                target,
                rankingEntries,
                productLikeSummaries
        );
    }

    @Transactional(readOnly = true)
    public OptionalDouble getDailyRankingScore(Long productId) {
        LocalDate now = LocalDate.now(ZoneId.systemDefault());
        return rankingZSetRepository.findDailyRanking(now, productId);
    }

    private boolean hasValidDate(String date) {
        return date == null || date.isBlank();
    }

    private LocalDate initLocalDate(String date) {
         return (hasValidDate(date))
                ? LocalDate.now(ZoneId.systemDefault())
                : LocalDate.parse(date, DateTimeFormatter.BASIC_ISO_DATE);

    }

    private List<ProductLikeSummary> findProductSummaryFrom(List<RankingEntry> rankingEntries) {
        List<ProductLikeSummary> result = new ArrayList<>();

        for(RankingEntry rankingEntry : rankingEntries) {
            ProductLikeSummary summary;
            try {
                summary = productQueryService.getProductLikeSummary(rankingEntry.productId());
            } catch (CoreException e) {
                if(e.getErrorType() == ErrorType.NOT_FOUND) {
                    log.error("Could not find product like summary for {}", rankingEntry.productId());
                }
                summary =  null;
            }
            result.add(summary);
        }
        return result;
    }
}
