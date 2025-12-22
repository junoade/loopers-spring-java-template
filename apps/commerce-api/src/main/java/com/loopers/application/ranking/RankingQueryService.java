package com.loopers.application.ranking;

import com.loopers.ranking.DailyRankingResponse;
import com.loopers.ranking.RankingZSetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class RankingQueryService {
    private final RankingZSetRepository rankingZSetRepository;

    public DailyRankingResponse getDailyPopularProducts(String date, int size) {
        LocalDate target = (hasValidDate(date))
                        ? LocalDate.now(ZoneOffset.UTC)
                        : LocalDate.parse(date, DateTimeFormatter.BASIC_ISO_DATE);

        int limit = (size <= 0) ? 20 : Math.min(size, 100);

        return new DailyRankingResponse(
                target,
                rankingZSetRepository.findTopDailyAllByLimit(target, limit)
        );
    }

    private boolean hasValidDate(String date) {
        return date == null || date.isBlank();
    }
}
