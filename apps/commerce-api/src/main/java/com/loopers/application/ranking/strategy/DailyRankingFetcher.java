package com.loopers.application.ranking.strategy;

import com.loopers.application.ranking.RankingPeriod;
import com.loopers.ranking.RankingEntry;
import com.loopers.ranking.RankingZSetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DailyRankingFetcher implements RankingFetchStrategy {
    private final RankingZSetRepository rankingZSetRepository;

    @Override
    public RankingPeriod getRankingPeriod() {
        return RankingPeriod.DAILY;
    }

    @Override
    public List<RankingEntry> fetchRankingEntries(String key, int limit) {
        LocalDate target = initLocalDate(key);
        return rankingZSetRepository.findTopDailyAllByLimit(target, limit);
    }

    private LocalDate initLocalDate(String date) {
        return (hasValidDate(date))
                ? LocalDate.now(ZoneId.systemDefault())
                : LocalDate.parse(date, DateTimeFormatter.BASIC_ISO_DATE);
    }

    private boolean hasValidDate(String date) {
        return date == null || date.isBlank();
    }
}
