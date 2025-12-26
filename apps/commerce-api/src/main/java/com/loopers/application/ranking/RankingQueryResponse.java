package com.loopers.application.ranking;

import com.loopers.application.product.ProductLikeSummary;
import com.loopers.ranking.RankingEntry;

import java.time.LocalDate;
import java.util.List;

public record RankingQueryResponse(
        LocalDate date,
        List<RankingEntry> rankingEntries,
        List<ProductLikeSummary> productLikeSummary
) {
    public static RankingQueryResponse of(LocalDate date, List<RankingEntry> rankingEntries, List<ProductLikeSummary> productLikeSummary) {
        return new RankingQueryResponse(date, rankingEntries, productLikeSummary);
    }
}
