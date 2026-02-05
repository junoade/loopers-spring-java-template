package com.loopers.application.ranking;

import com.loopers.application.product.ProductLikeSummary;
import com.loopers.application.product.ProductQueryService;
import com.loopers.application.ranking.strategy.RankingFetchStrategyResolver;
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
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;

@Slf4j
@Service
@RequiredArgsConstructor
public class RankingQueryService {
    private final RankingZSetRepository rankingZSetRepository;
    private final ProductQueryService productQueryService;

    private final RankingFetchStrategyResolver rankingResolver;

    @Transactional(readOnly = true)
    public RankingQueryResponse getDailyPopularProducts(RankingPeriod period, String date, int size) {
        log.debug("Get {} popular products for {}", period, date);

        RankingFetchStrategyResolver.Resolved resolved = rankingResolver.resolve(period, date, size);
        RankingQuery rankingQuery = resolved.rankingQuery();

        List<RankingEntry> rankingEntries = resolved.policy().fetchRankingEntries(rankingQuery.key(), rankingQuery.limit());
        List<ProductLikeSummary> productLikeSummaries = findProductSummaryFrom(rankingEntries);

        return new RankingQueryResponse(
                rankingQuery.date(),
                rankingEntries,
                productLikeSummaries
        );
    }

    @Transactional(readOnly = true)
    public OptionalDouble getDailyRankingScore(Long productId) {
        LocalDate now = LocalDate.now(ZoneId.systemDefault());
        return rankingZSetRepository.findDailyRanking(now, productId);
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
