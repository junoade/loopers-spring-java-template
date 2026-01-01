package com.loopers.batch.job.rankMonthly.dto;

public record MonthlyRankRow(
        String yearMonth, // e.g) 202601
        Long productId,
        long viewCount,
        long likeCount,
        long orderCount,
        double score
) { }
