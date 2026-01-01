package com.loopers.batch.job.rankMonthly.dto;

public record MonthlyAggRow(
        Long productId,
        long viewCount,
        long likeCount,
        long orderCount
) { }
