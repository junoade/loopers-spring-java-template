package com.loopers.application.ranking;

import java.time.LocalDate;

public record RankingQuery(
        RankingPeriod period,
        String key,
        LocalDate date,
        int limit
) { }
