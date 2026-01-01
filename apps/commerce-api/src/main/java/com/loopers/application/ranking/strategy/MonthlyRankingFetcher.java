package com.loopers.application.ranking.strategy;

import com.loopers.application.ranking.RankingPeriod;
import com.loopers.infrastructure.ranking.ProductRankMonthlyRepository;
import com.loopers.ranking.RankingEntry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MonthlyRankingFetcher implements RankingFetchStrategy {
    private final ProductRankMonthlyRepository monthlyRankingRepository;

    @Override
    public RankingPeriod getRankingPeriod() {
        return RankingPeriod.MONTHLY;
    }

    @Override
    public List<RankingEntry> fetchRankingEntries(String key, int limit) {
        log.debug("Fetching ranking entries for key {}", key);
        return monthlyRankingRepository.findTopByYearMonth(key, PageRequest.of(0, limit));
    }
}
