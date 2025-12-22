package com.loopers.ranking;

import java.time.LocalDate;
import java.util.List;

public record DailyRankingResponse(LocalDate date, List<RankingEntry> rankingEntries) {
}
