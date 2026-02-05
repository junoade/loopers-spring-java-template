package com.loopers.application.ranking.strategy;

import com.loopers.application.ranking.RankingPeriod;
import com.loopers.ranking.RankingEntry;

import java.util.List;


public interface RankingFetchStrategy {
    RankingPeriod getRankingPeriod();
    List<RankingEntry> fetchRankingEntries(String key, int limit);
}
