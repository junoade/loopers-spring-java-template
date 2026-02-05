package com.loopers.application.ranking.strategy;

import com.loopers.application.ranking.RankingPeriod;
import com.loopers.application.ranking.RankingQuery;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class RankingFetchStrategyResolver {
    private static final DateTimeFormatter YYYYMMDD = DateTimeFormatter.BASIC_ISO_DATE;
    private static final DateTimeFormatter YYYYMM = DateTimeFormatter.ofPattern("yyyyMM");
    private final Map<RankingPeriod, RankingFetchStrategy> policies;

    public RankingFetchStrategyResolver(List<RankingFetchStrategy> policies) {
        this.policies = policies.stream()
                .collect(Collectors.toMap(RankingFetchStrategy::getRankingPeriod, Function.identity()));
    }

    /**
     * 랭킹 조회 방법을 선택합니다.
     * @param period
     * @param date
     * @param size
     * @return
     */
    public Resolved resolve(RankingPeriod period, String date, int size) {
        LocalDate target = initLocalDate(date);
        int limit = normalizeSize(size);

        String key = switch (period) {
            case DAILY -> target.format(YYYYMMDD);
            case WEEKLY -> yearWeekKey(target);   // 2026-W01
            case MONTHLY -> target.format(YYYYMM); // 202601
        };

        RankingFetchStrategy policy = policies.get(period);
        if (policy == null) throw new IllegalArgumentException("Unsupported period: " + period);

        return new Resolved(new RankingQuery(period, key, target, limit), policy);
    }

    public record Resolved(RankingQuery rankingQuery, RankingFetchStrategy policy) {}

    /**
     * 최대 상한선 TOP-100 으로 설정
     * @param size
     * @return
     */
    private int normalizeSize(int size) {
        if (size <= 0) return 20;
        return Math.min(size, 100);
    }

    private LocalDate initLocalDate(String date) {
        return (hasValidDate(date))
                ? LocalDate.now(ZoneId.systemDefault())
                : LocalDate.parse(date, DateTimeFormatter.BASIC_ISO_DATE);
    }

    private String yearWeekKey(LocalDate date) {
        WeekFields wf = WeekFields.ISO;
        int y = date.get(wf.weekBasedYear());
        int w = date.get(wf.weekOfWeekBasedYear());
        return "%d-W%02d".formatted(y, w);
    }

    private boolean hasValidDate(String date) {
        return date == null || date.isBlank();
    }
}
