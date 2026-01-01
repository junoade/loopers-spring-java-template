package com.loopers.batch.job.rankWeekly.step.dto;

public record WeeklyRankRow(
        String yearWeek,
        Long productId,
        long viewCount,
        long likeCount,
        long orderCount,
        double score
) { }
