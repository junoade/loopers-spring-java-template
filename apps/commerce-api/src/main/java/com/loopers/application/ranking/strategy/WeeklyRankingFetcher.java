package com.loopers.application.ranking.strategy;

import com.loopers.application.ranking.RankingPeriod;
import com.loopers.infrastructure.ranking.ProductRankWeeklyRepository;
import com.loopers.ranking.RankingEntry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class WeeklyRankingFetcher implements RankingFetchStrategy {
    private final ProductRankWeeklyRepository weeklyRankingRepository;

    @Override
    public RankingPeriod getRankingPeriod() {
        return RankingPeriod.WEEKLY;
    }

    @Override
    public List<RankingEntry> fetchRankingEntries(String key, int limit) {
        log.debug("Fetching ranking entries for key {}", key);
        return weeklyRankingRepository.findTopByYearWeek(key, PageRequest.of(0, limit));
    }
}
